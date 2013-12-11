/*
 * WPSResource.java
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.compusult.geopackage.service.geopackager.GeoPackager;
import net.compusult.geopackage.service.geopackager.GeoPackager.ProcessingStatus;
import net.compusult.geopackage.service.geopackager.GeoPackagingPool;
import net.compusult.xml.DOMUtil;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class WPSResource extends ServerResource {
	
	private static final Logger LOG = Logger.getLogger(WPSResource.class);
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static {
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public static final String RESP_ATTR_EXCEPTION_CODE = "net.compusult.geopackager.exception_code";
	public static final String RESP_ATTR_EXCEPTION_TEXT = "net.compusult.geopackager.exception_text";
	public static final String RESP_ATTR_EXCEPTION_LOCATOR = "net.compusult.geopackager.exception_locator";
	public static final String RESP_ATTR_EXCEPTION_THROWABLE = "net.compusult.geopackager.exception_throwable";
	
	protected static final String XML_NS = "http://www.w3.org/1999/xhtml";
	protected static final String OWS_NS = "http://www.opengis.net/ows/1.1";
	protected static final String WPS_NS = "http://www.opengis.net/wps/1.0.0";
	protected static final String XLINK_NS = "http://www.w3.org/1999/xlink";
	
	protected static final String IDENT_PROCESS_GEOPACKAGE = "GeoPackaging";
	protected static final String IDENT_OWSCONTEXT = "OWSContext";
	protected static final String IDENT_PASSPHRASE = "Passphrase";
	protected static final String IDENT_OUTPUT_GPKG = "GeoPackage";
	
	
	public enum ExceptionCode {
		OperationNotSupported(Status.SERVER_ERROR_NOT_IMPLEMENTED),
		MissingParameterValue(Status.CLIENT_ERROR_BAD_REQUEST),
		InvalidParameterValue(Status.CLIENT_ERROR_BAD_REQUEST),
		VersionNegotiationFailed(Status.CLIENT_ERROR_BAD_REQUEST),
		InvalidUpdateSequence(Status.CLIENT_ERROR_BAD_REQUEST),
		OptionNotSupported(Status.SERVER_ERROR_NOT_IMPLEMENTED),
		NoApplicableCode(Status.SERVER_ERROR_INTERNAL);
		
		private Status status;
		
		private ExceptionCode(Status status) {
		}
		
		public Status getStatus() {
			return status;
		}
	}
	
	protected final DOMUtil domUtil;
	protected GeoPackagingPool packagerPool;
	
	protected WPSResource() {
		this.domUtil = new DOMUtil();
	}

	@Autowired
	public void setPackagerPool(GeoPackagingPool packagerPool) {
		this.packagerPool = packagerPool;
	}
	
	protected String formatDate(Date in) {
		return DATE_FORMAT.format(in);
	}
	
	protected Representation error(ExceptionCode errorCode, String msg, String locator, Throwable t) {
		LOG.error(msg, t);
		Map<String, Object> attrs = getResponseAttributes();
		attrs.put(RESP_ATTR_EXCEPTION_CODE, errorCode.name());
		attrs.put(RESP_ATTR_EXCEPTION_TEXT, msg == null ? "" : msg);
		attrs.put(RESP_ATTR_EXCEPTION_LOCATOR, locator == null ? "" : locator);
		attrs.put(RESP_ATTR_EXCEPTION_THROWABLE, t == null ? new Throwable() : t);
		throw new ResourceException(errorCode.getStatus());
	}
	
	protected Representation error(ExceptionCode errorCode, String msg, String locator) {
		return error(errorCode, msg, locator, null);
	}
	
	protected Representation error(ExceptionCode errorCode, String msg) {
		return error(errorCode, msg, null, null);
	}
	
	protected Representation error(ResourceException ex, String locator) {
		return error(ExceptionCode.NoApplicableCode, ex.getMessage(), locator, ex.getCause());
	}
	
	protected Representation generateResponseDoc(GeoPackager packager) throws ParserConfigurationException {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document outputDoc = db.newDocument();
		
		Element newRoot = outputDoc.createElementNS(WPS_NS, "ExecuteResponse");
		outputDoc.appendChild(newRoot);
		
		newRoot.setAttribute("service", "WPS");
		newRoot.setAttribute("version", "1.0.0");
		newRoot.setAttributeNS(XML_NS, "lang", "en-US");
		
		Element newProcess = outputDoc.createElementNS(WPS_NS, "Process");
		newRoot.appendChild(newProcess);
		
		Element identElement = outputDoc.createElementNS(OWS_NS, "Identifier");
		identElement.setTextContent(IDENT_PROCESS_GEOPACKAGE);
		newProcess.appendChild(identElement);
		
		Element newStatus = createStatusElement(outputDoc, packager);
		newRoot.appendChild(newStatus);
		
		if (packager.getIncludeLineage()) {
			newRoot.appendChild(outputDoc.adoptNode(packager.getDataInputs().cloneNode(true)));
			
			Element newOutputDefs = outputDoc.createElementNS(WPS_NS, "OutputDefinitions");
			for (Element outputElement : packager.getOutputDefns()) {
				newOutputDefs.appendChild(outputDoc.adoptNode(outputElement.cloneNode(true)));
			}
			newRoot.appendChild(newOutputDefs);
		}

		if (packager.getCurrentStatus() == ProcessingStatus.SUCCEEDED) {
			/*
			 * ProcessOutputs is only used when status is ProcessSucceeded.
			 */
			Element newProcOutputs = outputDoc.createElementNS(WPS_NS, "ProcessOutputs");
			Element newOutput = outputDoc.createElementNS(WPS_NS, "Output");
			
			identElement = outputDoc.createElementNS(OWS_NS, "Identifier");
			identElement.setTextContent(IDENT_OUTPUT_GPKG);
			newOutput.appendChild(identElement);
			
			Element newTitle = outputDoc.createElementNS(OWS_NS, "Title");
			newTitle.setTextContent("GeoPackage created as the result of an OWS Context-based request");
			newOutput.appendChild(newTitle);

			Element newOutputRef = outputDoc.createElementNS(WPS_NS, "OutputReference");
			newOutputRef.setAttribute("href", constructRetrievalReference(packager.getId()));
			newOutput.appendChild(newOutputRef);
			
			newProcOutputs.appendChild(newOutput);
			newRoot.appendChild(newProcOutputs);
		}

		return new DomRepresentation(MediaType.TEXT_XML, outputDoc);
	}
	
	private String constructRetrievalReference(String id) {
		
		Reference us = getReference();
		List<String> segments = us.getSegments();
		int found = segments.indexOf("wps");
		if (found < 0) {
			throw new IllegalStateException("Expected '/wps' in the current request's URL");
		}
		
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i <= found; ++ i) {
			buf.append('/').append(segments.get(i));
		}
		buf.append("/gpkg/").append(id);
		
		return Reference.toString(us.getScheme(), us.getHostDomain(), us.getHostPort(), buf.toString(), null, null);
	}

	protected Element createStatusElement(Document doc, GeoPackager packager) {
		Element newStatus = doc.createElementNS(WPS_NS, "Status");
		newStatus.setAttribute("creationTime", formatDate(new Date()));
		
		ProcessingStatus procStat = packager.getCurrentStatus();
		Element newProcStatus = doc.createElementNS(WPS_NS, packager.getCurrentStatus().getElementName());
		switch (procStat) {
		case ACCEPTED:
			newProcStatus.setTextContent("GeoPackaging request has been accepted for processing");
			break;
		case STARTED:
			int percentComplete = Math.min(packager.getPercentComplete(), 99);				// 100 is an illegal value
			newProcStatus.setAttribute("percentComplete", String.valueOf(percentComplete));
			newProcStatus.setTextContent("GeoPackaging request is being processed");
			break;
		case PAUSED:
			// We don't actually use this status.
			newProcStatus.setTextContent("GeoPackaging request has been paused");
			break;
		case SUCCEEDED:
			newProcStatus.setTextContent("GeoPackaging request has been completed");
			break;
		case FAILED:
			newProcStatus.setTextContent("GeoPackaging request has failed");
			break;
		}
		newStatus.appendChild(newProcStatus);
		
		return newStatus;
	}

}
