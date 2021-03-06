/*
 * MainWPSResource.java
 * 
 * Copyright 2013, Compusult Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
   
package net.compusult.geopackage.service.resource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.compusult.geopackage.service.geopackager.GeoPackager;
import net.compusult.geopackage.service.resource.helper.TemplateManager;
import net.compusult.owscontext.ContextDoc;
import net.compusult.owscontext.codec.AtomCodec;
import net.compusult.owscontext.codec.OWSContextCodec.EncodingException;
import net.compusult.owscontext.codec.OWSContextCodecFactory;
import net.compusult.xml.XMLFetcher;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.stringtemplate.v4.ST;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MainWPSResource extends WPSResource {
	
	@Autowired private ApplicationContext applicationContext;
	@Autowired private TemplateManager templateManager;
	@Autowired private OWSContextCodecFactory contextCodecFactory;
	
	public MainWPSResource() {
		super();
	}

	@Get
	public Representation handleGetRequest() {
		
		Form query = getQuery();
		
		if (!"WPS".equals(query.getFirstValue("service", true, null))) {
			return error(ExceptionCode.InvalidParameterValue, "Service must be WPS", "service");
		}
		
		String version = query.getFirstValue("acceptversions", true, null);		// GetCapabilities
		if (version == null) {
			version = query.getFirstValue("version", true, null);				// others
		}
		if (!"1.0.0".equals(version)) {
			return error(ExceptionCode.VersionNegotiationFailed, "Only version 1.0.0 is supported", "version");
		}
		
		String request = query.getFirstValue("request", true, null);

		if ("GetCapabilities".equals(request)) {
			return getCapabilities();
			
		} else if ("DescribeProcess".equals(request)) {
			return describeProcess();
			
		} else {
			return error(ExceptionCode.OperationNotSupported, "Unrecognized request '" + request + "'");
		}
	}

	private Representation getCapabilities() {
		try {
			ST template = new ST(templateManager.readTemplate("GetCapabilities.st"), '$', '$');
			template.add("ref", getReference());
			template.add("req", getRequest());
			template.add("process", IDENT_PROCESS_GEOPACKAGE);
			template.add("provider", templateManager.getContactInfo());
			return new StringRepresentation(template.render());
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Internal error getting capabilities", e);
		}
	}

	private Representation describeProcess() {
		String identifierString = getQuery().getFirstValue("identifier", true, null);
		if (identifierString == null) {
			return error(ExceptionCode.MissingParameterValue, "One or more identifiers must be provided to DescribeProcess", "identifier");
		}
		
		if (!IDENT_PROCESS_GEOPACKAGE.equalsIgnoreCase(identifierString)) {
			return error(ExceptionCode.OptionNotSupported, "Only process identifier '" + IDENT_PROCESS_GEOPACKAGE + "' is supported at present", "identifier");
		}
		
//		List<String> identifiers = Arrays.asList(identifierString.split(","));
//		// If any of the identifiers is the special one "ALL", then use null as a marker of that.
//		for (String s : identifiers) {
//			if ("ALL".equals(s)) {
//				identifiers = null;
//				break;
//			}
//		}
		
		try {
			ST template = new ST(templateManager.readTemplate("DescribeProcess_" + IDENT_PROCESS_GEOPACKAGE + ".st"), '$', '$');
			template.add("ref", getReference());
			template.add("req", getRequest());
			template.add("process", IDENT_PROCESS_GEOPACKAGE);
			template.add("input_owc", IDENT_OWSCONTEXT);
			template.add("input_psw", IDENT_PASSPHRASE);
			template.add("output_gpkg", IDENT_OUTPUT_GPKG);
			template.add("output_owc", IDENT_OWSCONTEXT);
			return new StringRepresentation(template.render());
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Internal error getting capabilities", e);
		}
	}
	
	/**
	 * @param incomingRepresentation - a DomRepresentation containing the XML request data.
	 * @return a DomRepresentation containing the XML response to the Execute request.
	 */
	@Post
	public Representation handlePostRequest(DomRepresentation incomingRepresentation) {

		incomingRepresentation.setValidatingDtd(true);
		incomingRepresentation.setCoalescing(true);
		incomingRepresentation.setIgnoringComments(true);
		incomingRepresentation.setNamespaceAware(true);
		
		Document doc;
		try {
			doc = incomingRepresentation.getDocument();
		} catch (IOException e) {
			return error(ExceptionCode.NoApplicableCode, "Unable to read incoming request: " + e.getMessage());
		}
		Element root = doc.getDocumentElement();
		
		/*
		 * Because we asked for a validating DomRepresentation, we don't need to
		 * check service and version, nor check for schema-validity in general.
		 */
		
//		Element langElement = domUtil.findFirstChildNamed(root, OWS_NS, "language");
//		String language = (langElement == null) ? "en-US" : domUtil.nodeTextContent(langElement);

		Element identElement = domUtil.findFirstChildNamed(root, OWS_NS, "Identifier");
		// identElement can't be null
		String identifier = domUtil.nodeTextContent(identElement);
		if (! IDENT_PROCESS_GEOPACKAGE.equals(identifier)) {
			return error(ExceptionCode.OptionNotSupported, "Only '" + IDENT_PROCESS_GEOPACKAGE + "' is supported as a process identifier", "Identifier");
		}
		
		/*
		 * Determine input characteristics.
		 */
		
		Element dataInputs = domUtil.findFirstChildNamed(root, WPS_NS, "DataInputs");
		// dataInputs can't be null
		List<Element> inputs = domUtil.findChildrenNamed(dataInputs, WPS_NS, "Input");
		// but inputs can be, and it can be empty or too big
		if (inputs == null || inputs.isEmpty() || inputs.size() > 2) {
			return error(ExceptionCode.MissingParameterValue, "'" + IDENT_PROCESS_GEOPACKAGE + "' requires one or two data inputs", "Input");
		}
		
		ContextDoc owsContext = null;
		String passPhrase = null;
		
		for (Element input : inputs) {
			Element inputIdentElement = domUtil.findFirstChildNamed(input, OWS_NS, "Identifier");
			// can't be null

			String inputIdent = domUtil.nodeTextContent(inputIdentElement);
			if (IDENT_OWSCONTEXT.equalsIgnoreCase(inputIdent)) {
				try {
					owsContext = readContextDoc(input);
				} catch (ResourceException e) {
					return error(e, "Input");
				}
				
			} else if (IDENT_PASSPHRASE.equalsIgnoreCase(inputIdent)) {
				try {
					passPhrase = readPassPhrase(input);
				} catch (ResourceException e) {
					return error(e, "Input");
				}
			}
		}
		
		if (owsContext == null) {
			return error(ExceptionCode.MissingParameterValue, "The '" + IDENT_OWSCONTEXT + "' input must be provided", "Input");
		}
		
		/*
		 * Determine output characteristics.
		 */
		
		boolean rawOutput = false;
		List<Element> outputElementList;
		boolean storeExecuteResponse = false;
		boolean includeLineage = false;
		
		Element responseFormElement = domUtil.findFirstChildNamed(root, WPS_NS, "ResponseForm");
		Element rawDataElement = domUtil.findFirstChildNamed(responseFormElement, WPS_NS, "RawDataOutput");
		if (rawDataElement != null) {
			outputElementList = Collections.emptyList();
			rawOutput = true;
			
		} else {
			Element responseDocElement = domUtil.findFirstChildNamed(responseFormElement, WPS_NS, "ResponseDocument");
			// responseDocElement cannot be null here
			storeExecuteResponse = Boolean.parseBoolean(responseDocElement.getAttribute("storeExecuteResponse"));
			includeLineage = Boolean.parseBoolean(responseDocElement.getAttribute("lineage"));
	//		boolean storeStatus = Boolean.parseBoolean(responseDocElement.getAttribute("status"));
			
			outputElementList = domUtil.findChildrenNamed(responseDocElement, WPS_NS, "Output");
			// One output: just the GeoPackage
			// Two outputs: the GeoPackage and the updated ContextDoc
			if (outputElementList.isEmpty() || outputElementList.size() > 2) {
				return error(ExceptionCode.InvalidParameterValue, "Either one or two data outputs must be requested", "Output");
			}
		}

		GeoPackager packager = (GeoPackager) applicationContext.getBean("geopackager", owsContext, passPhrase);

		boolean contextAsReference = false;
		
		for (Element outputElement : outputElementList) {
			boolean asReference = Boolean.parseBoolean(outputElement.getAttribute("asReference"));
			identElement = domUtil.findFirstChildNamed(outputElement, OWS_NS, "Identifier");
			// identElement cannot be null
			identifier = domUtil.nodeTextContent(identElement);
			if (IDENT_OUTPUT_GPKG.equals(identifier)) {
				// nothing to do
			} else if (IDENT_OWSCONTEXT.equals(identifier)) {
				contextAsReference = asReference;
			} else {
				return error(ExceptionCode.OptionNotSupported, "Unsupported output identifier '" + identifier + "'", "Identifier");
			}
		}
		
		packager.setStoreGeoPackageAsReference(true);
		packager.setStoreOWSContextAsReference(contextAsReference);
		
		packager.setDataInputs(dataInputs);
		packager.setOutputDefns(outputElementList);
		packager.setIncludeLineage(includeLineage);
		
		packagerPool.start(packager);
		
		if (! storeExecuteResponse) {
			/*
			 * Wait for the results to become available (i.e. simulate a blocking call).
			 */
			boolean interrupted = false;
			while (!interrupted && !packager.getCurrentStatus().isFinal()) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
			if (interrupted) {
				return error(ExceptionCode.NoApplicableCode, "Server interruption, try again later");
			}
		}
		
		try {
			if (rawOutput) {
				return new FileRepresentation(packager.getFile(), packager.getMediaType());
			} else {
				return generateResponseDoc(packager);
			}
		} catch (Exception e) {
			return error(ExceptionCode.NoApplicableCode, e.getMessage());
		}
	}
	
	private ContextDoc readContextDoc(Element inputElement) throws ResourceException {
		Element feedElement;
		String mimeType;
		
		String owsContextDocumentString = null;

		// One or the other of Data or Reference will be present, because the doc is schema-valid.
		Element refElement = domUtil.findFirstChildNamed(inputElement, WPS_NS, "Reference");
		if (refElement != null) {
			
			// The schema says this should be in the xlink namespace, unlike OutputReferenceType.href for example
			String href = domUtil.getAttributeValue(refElement, XLINK_NS, "href");
			
			mimeType = domUtil.getAttributeValue(refElement, "mimeType");
			if (mimeType == null) {
				// Hmm, should this really be the default?
				mimeType = AtomCodec.MIME_TYPE;
			}
			
			Document contextDoc;
			try {
				contextDoc = new XMLFetcher(href).fetch();
			} catch (IOException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Failed to retrieve external OWS Context document", e);
			}
			
			feedElement = contextDoc.getDocumentElement();

		} else {
			Element dataElement = domUtil.findFirstChildNamed(inputElement, WPS_NS, "Data");
			if (dataElement == null) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Input type 'Data' is required");
			}
			
			Element complexDataElement = domUtil.findFirstChildNamed(dataElement, WPS_NS, "ComplexData");
			if (complexDataElement == null) {
				// some other kind of <Data> subelement must have been provided
				throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, "Only 'ComplexData' input is supported for 'OWSContext'");
			}
			
			/*
			 * Decode the OWS Context document payload of this input.
			 * TODO figure out how a JSON-encoded context document would look.
			 * We might need to push some of the following logic down into the AtomCodec itself.
			 */
			mimeType = complexDataElement.getAttribute("mimeType");
			// Either a <feed> or <entry> element will be at the top level of the input
			feedElement = domUtil.findFirstChildNamed(complexDataElement, AtomCodec.ATOM_NS, "feed");
			if (feedElement == null) {
				feedElement = domUtil.findFirstChildNamed(complexDataElement, AtomCodec.ATOM_NS, "entry");
			}
			if (feedElement == null) {
				owsContextDocumentString = domUtil.nodeTextContent(complexDataElement);
			}
		}
		
		try {
			AtomCodec contextCodec = (AtomCodec) contextCodecFactory.createCodec(mimeType);
			if (owsContextDocumentString != null) {
				return contextCodec.decode(owsContextDocumentString);
			} else {
				return contextCodec.decode(feedElement);
			}
		} catch (EncodingException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Failed to parse OWS Context document", e);
		}
	}
	
	private String readPassPhrase(Element inputElement) throws ResourceException {
		// One or the other of these will be present, because the doc is schema-valid.
		Element refElement = domUtil.findFirstChildNamed(inputElement, WPS_NS, "Reference");
		if (refElement != null) {
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, "Input type 'Reference' is not supported yet");
		}
		
		Element dataElement = domUtil.findFirstChildNamed(inputElement, WPS_NS, "Data");
		if (dataElement == null) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Input type 'Data' is required");
		}
		
		Element literalDataElement = domUtil.findFirstChildNamed(dataElement, WPS_NS, "LiteralData");
		if (literalDataElement == null) {
			// some other kind of <Data> subelement must have been provided
			throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, "Only 'LiteralData' input is supported for 'Passphrase'");
		}
		
		return domUtil.nodeTextContent(literalDataElement);
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setContextCodecFactory(OWSContextCodecFactory contextCodecFactory) {
		this.contextCodecFactory = contextCodecFactory;
	}

}
