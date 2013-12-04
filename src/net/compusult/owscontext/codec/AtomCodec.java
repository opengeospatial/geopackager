/*
 * AtomCodec.java
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
   
package net.compusult.owscontext.codec;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.compusult.datetime.DateFormatManager;
import net.compusult.geometry.gml.GMLConverterInterface;
import net.compusult.owscontext.AuthorInfo;
import net.compusult.owscontext.CategorizedTerm;
import net.compusult.owscontext.Content;
import net.compusult.owscontext.ContextDoc;
import net.compusult.owscontext.Creator;
import net.compusult.owscontext.CreatorApplication;
import net.compusult.owscontext.CreatorDisplay;
import net.compusult.owscontext.DateTimeInterval;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.OfferingFactory;
import net.compusult.owscontext.Operation;
import net.compusult.owscontext.Resource;
import net.compusult.owscontext.StyleSet;
import net.compusult.owscontext.TypedLink;
import net.compusult.owscontext.TypedText;
import net.compusult.xml.DOMUtil;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


public class AtomCodec implements OWSContextCodec {
	
	public static final String MIME_TYPE = "application/atom+xml";
	
	private static final String ATOM_ENCODING_SPEC_REF = "http://www.opengis.net/spec/owc-atom/1.0/req/core";
	private static final String RESOURCE_ACTIVE_SCHEME = "http://www.opengis.net/spec/owc/active";
	private static final String RESOURCE_FOLDER_SCHEME = "http://www.opengis.net/spec/owc/folder";
	
	public static final String XML_NS         = "http://www.w3.org/XML/1998/namespace";
	public static final String ATOM_NS        = "http://www.w3.org/2005/Atom";
	public static final String CONTEXT_NS     = "http://www.opengis.net/owc/1.0";
	public static final String DUBLIN_CORE_NS = "http://purl.org/dc/elements/1.1/";
	public static final String GEORSS_NS      = "http://www.georss.org/georss";
	public static final String GML_NS         = "http://www.opengis.net/gml";
	
	private final DOMUtil dom;
	private final GMLConverterInterface gmlConverter;
	
	protected AtomCodec(GMLConverterInterface gmlConverter) {
		this.dom = new DOMUtil();
		this.gmlConverter = gmlConverter;
	}
	
	
	@Override
	public String getName() {
		return "atom";
	}

	@Override
	public String getMimeType() {
		return MIME_TYPE;
	}
	
	private GMLConverterInterface getGMLConverter() throws EncodingException {
		if (gmlConverter == null) {
			throw new EncodingException("Atom codec has not been set up for GML conversion in this environment!");
		}
		
		return gmlConverter;
	}

	/*
	 * =================================================================================================
	 * Atom (XML) encoding
	 */
	
	@Override
	public String encode(ContextDoc context) throws EncodingException {
		// Create an XML document representing the OWS Context doc in Atom encoding
		Document doc = encodeAtomDocument(context);

		// Serialize the XML into a string
		ByteArrayOutputStream baos = new ByteArrayOutputStream(32768);
		try {
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
			xformer.transform(new DOMSource(doc), new StreamResult(baos));
		} catch (TransformerException e) {
			throw new EncodingException("Failed to run XML->String transformation", e);
		}

		try {
			return new String(baos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new EncodingException("Failed to convert byte array to String", e);
		}
	}
	
	private Document encodeAtomDocument(ContextDoc context) throws EncodingException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		Document doc;
		try {
			doc = docFactory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new EncodingException("Failed to create a new XML document", e);
		}

		try {
			doc.appendChild(encodeContextDoc(context, doc));
		} catch (DOMException e) {
			throw new EncodingException("Failed to create Atom encoding of OWS Context", e);
		}

		return doc;
	}
	
	private Element encodeContextDoc(ContextDoc context, Document doc) throws EncodingException {
		Element elem = doc.createElementNS(ATOM_NS, "feed");
		elem.setAttributeNS(XML_NS, "xml:lang", context.getLanguage());
		
		Element specLink = doc.createElementNS(ATOM_NS, "link");
		specLink.setAttribute("rel", "profile");
		specLink.setAttribute("href", ATOM_ENCODING_SPEC_REF);
		specLink.setAttribute("title", "This file is compliant with version 1.0 of OWS Context");
		elem.appendChild(specLink);
		
		if (context.getId() != null) {
			Element id = doc.createElementNS(ATOM_NS, "id");
			id.setTextContent(context.getId());
			elem.appendChild(id);
		}
		
		if (context.getTitle() != null) {
			Element title = doc.createElementNS(ATOM_NS, "title");
			if (context.getTitle().getType() != null) {
				title.setAttribute("type", context.getTitle().getType());
			}
			title.setTextContent(context.getTitle().getText());
			elem.appendChild(title);
		}
		
		if (context.getSubtitle() != null) {
			Element subtitle = doc.createElementNS(ATOM_NS, "subtitle");
			if (context.getSubtitle().getType() != null) {
				subtitle.setAttribute("type", context.getSubtitle().getType());
			}
			subtitle.setTextContent(context.getSubtitle().getText());
			elem.appendChild(subtitle);
		}
		
		Element updateDate = doc.createElementNS(ATOM_NS, "updated");
		updateDate.setTextContent(encodeDateTime(context.getUpdateDate()));
		elem.appendChild(updateDate);
		
		for (AuthorInfo author : context.getAuthors()) {
			elem.appendChild(encodeAuthorInfo(author, doc));
		}
		
		if (context.getPublisher() != null) {
			Element publisher = doc.createElementNS(DUBLIN_CORE_NS, "publisher");
			publisher.setTextContent(context.getPublisher());
			elem.appendChild(publisher);
		}
		
		encodeCreator(elem, context.getCreator(), doc);
		
		if (context.getRights() != null) {
			Element rights = doc.createElementNS(ATOM_NS, "rights");
			rights.setTextContent(context.getRights());
			elem.appendChild(rights);
		}
		
		if (context.getAreaOfInterest() != null) {
			Element where = doc.createElementNS(GEORSS_NS, "where");
			Element aoi = getGMLConverter().gmlFromGeometry(context.getAreaOfInterest(), doc);
			where.appendChild(aoi);
			elem.appendChild(where);
		}

		if (context.getTimeIntervalOfInterest() != null) {
			Element interval = encodeDateTimeInterval(DUBLIN_CORE_NS, "date", context.getTimeIntervalOfInterest(), doc);
			if (interval != null) {
				elem.appendChild(interval);
			}
		}
		
		for (Resource resource : context.getResources()) {
			elem.appendChild(encodeResource(resource, doc));
		}
		
		for (TypedLink contextMeta : context.getContextMetadata()) {
			Element contextMetadata = doc.createElementNS(ATOM_NS, "link");
			contextMetadata.setAttribute("rel", "via");
			contextMetadata.setAttribute("href", contextMeta.getUrl());
			if (contextMeta.getTitle() != null) {
				contextMetadata.setAttribute("title", contextMeta.getTitle());
			}
			elem.appendChild(contextMetadata);
		}
		
		for (CategorizedTerm keyword : context.getKeywords()) {
			elem.appendChild(encodeCategorizedTerm(keyword, doc));
		}
		
		addExtensions(doc, elem, context.getExtensions());

		return elem;
	}
	
	private Element encodeResource(Resource resource, Document doc) throws EncodingException {
		Element elem = doc.createElementNS(ATOM_NS, "entry");
		
		Element id = doc.createElementNS(ATOM_NS, "id");
		id.setTextContent(resource.getId());
		elem.appendChild(id);
		
		if (resource.getTitle() != null) {
			Element title = doc.createElementNS(ATOM_NS, "title");
			if (resource.getTitle().getType() != null) {
				title.setAttribute("type", resource.getTitle().getType());
			}
			title.setTextContent(resource.getTitle().getText());
			elem.appendChild(title);
		}
		
		if (resource.getAbstrakt() != null) {
			Element abstrakt = doc.createElementNS(ATOM_NS, "content");
			if (resource.getAbstrakt().getType() != null) {
				abstrakt.setAttribute("type", resource.getAbstrakt().getType());
			}
			abstrakt.setTextContent(resource.getAbstrakt().getText());
			elem.appendChild(abstrakt);
		}
		
		Element updateDate = doc.createElementNS(ATOM_NS, "updated");
		updateDate.setTextContent(encodeDateTime(resource.getUpdateDate()));
		elem.appendChild(updateDate);
		
		for (AuthorInfo author : resource.getAuthors()) {
			elem.appendChild(encodeAuthorInfo(author, doc));
		}
		
		if (resource.getPublisher() != null) {
			Element publisher = doc.createElementNS(DUBLIN_CORE_NS, "publisher");
			publisher.setTextContent(resource.getPublisher());
			elem.appendChild(publisher);
		}
		
		if (resource.getRights() != null) {
			Element rights = doc.createElementNS(ATOM_NS, "rights");
			rights.setTextContent(resource.getRights());
			elem.appendChild(rights);
		}
		
		if (resource.getGeospatialExtent() != null) {
			Element where = doc.createElementNS(GEORSS_NS, "where");
			Element aoi = getGMLConverter().gmlFromGeometry(resource.getGeospatialExtent(), doc);
			where.appendChild(aoi);
			elem.appendChild(where);
		}
		
		if (resource.getTemporalExtent() != null) {
			Element interval = encodeDateTimeInterval(DUBLIN_CORE_NS, "date", resource.getTemporalExtent(), doc);
			if (interval != null) {
				elem.appendChild(interval);
			}
		}
		
		if (resource.getContentDescription() != null) {
			Element contentDesc = doc.createElementNS(ATOM_NS, "link");
			contentDesc.setAttribute("rel", "alternate");
			contentDesc.setAttribute("href", resource.getContentDescription().getUrl());
			if (resource.getContentDescription().getType() != null) {
				contentDesc.setAttribute("type", resource.getContentDescription().getType());
			}
		}

		if (resource.getPreview() != null) {
			Element preview = doc.createElementNS(ATOM_NS, "link");
			preview.setAttribute("rel", "icon");
			preview.setAttribute("href", resource.getPreview().getUrl());
			if (resource.getPreview().getLength() != null) {
				preview.setAttribute("length", String.valueOf(resource.getPreview().getLength()));
			}
			if (resource.getPreview().getType() != null) {
				preview.setAttribute("type", resource.getPreview().getType());
			}
			elem.appendChild(preview);
		}
		
		for (TypedLink contentByRef : resource.getContentByRefs()) {
			Element ref = doc.createElementNS(ATOM_NS, "link");
			ref.setAttribute("rel", "enclosure");
			ref.setAttribute("href", contentByRef.getUrl());
			if (contentByRef.getType() != null) {
				ref.setAttribute("type", contentByRef.getType());
			}
			elem.appendChild(ref);
		}

		Element activeTerm = encodeCategorizedTerm(new CategorizedTerm(RESOURCE_ACTIVE_SCHEME, String.valueOf(resource.isActive()), null), doc);
		elem.appendChild(activeTerm);
		
		for (CategorizedTerm keyword : resource.getKeywords()) {
			elem.appendChild(encodeCategorizedTerm(keyword, doc));
		}
		
		if (resource.getMinScaleDenominator() != null) {
			Element scaleDenom = doc.createElementNS(CONTEXT_NS, "minScaleDenominator");
			scaleDenom.setTextContent(String.valueOf(resource.getMinScaleDenominator()));
			elem.appendChild(scaleDenom);
		}
		
		if (resource.getMaxScaleDenominator() != null) {
			Element scaleDenom = doc.createElementNS(CONTEXT_NS, "maxScaleDenominator");
			scaleDenom.setTextContent(String.valueOf(resource.getMaxScaleDenominator()));
			elem.appendChild(scaleDenom);
		}
		
		if (resource.getFolder() != null) {
			elem.appendChild(encodeCategorizedTerm(new CategorizedTerm(RESOURCE_FOLDER_SCHEME, resource.getFolder(), resource.getFolderLabel()), doc));
		}
		
		for (Offering offering : resource.getOfferings()) {
			elem.appendChild(encodeOffering(offering, doc));
		}
		
		addExtensions(doc, elem, resource.getExtensions());
		
		return elem;
	}

	private Element encodeAuthorInfo(AuthorInfo author, Document doc) {
		Element elem = doc.createElementNS(ATOM_NS, "author");
		
		if (author.getName() != null) {
			Element name = doc.createElementNS(ATOM_NS, "name");
			name.setTextContent(author.getName());
			elem.appendChild(name);
		}
		
		if (author.getEmail() != null) {
			Element email = doc.createElementNS(ATOM_NS, "email");
			email.setTextContent(author.getEmail());
			elem.appendChild(email);
		}
		
		if (author.getUri() != null) {
			Element uri = doc.createElementNS(ATOM_NS, "uri");
			uri.setTextContent(author.getUri());
			elem.appendChild(uri);
		}

		return elem;
	}
	
	private void encodeCreator(Element parent, Creator creator, Document doc) {
		if (creator != null) {
			if (creator.getApplication() != null) {
				Element child = encodeCreatorApplication(creator.getApplication(), doc);
				if (child != null) {
					parent.appendChild(child);
				}
			}
			if (creator.getDisplay() != null) {
				Element child = encodeCreatorDisplay(creator.getDisplay(), doc);
				if (child != null) {
					parent.appendChild(child);
				}
			}
		}
	}
	
	private Element encodeCreatorApplication(CreatorApplication app, Document doc) {
		Element elem = doc.createElementNS(ATOM_NS, "generator");
		
		if (app.getTitle() != null) {
			elem.setTextContent(app.getTitle());
		}
		if (app.getUri() != null) {
			elem.setAttribute("uri", app.getUri());
		}
		if (app.getVersion() != null) {
			elem.setAttribute("version", app.getVersion());
		}
		
		return elem;
	}
	
	private Element encodeCreatorDisplay(CreatorDisplay display, Document doc) {
		
		Element disp = doc.createElementNS(CONTEXT_NS, "display");
		if (display.getPixelWidth() != null) {
			Element width = doc.createElementNS(CONTEXT_NS, "pixelWidth");
			width.setTextContent(String.valueOf(display.getPixelWidth()));
			disp.appendChild(width);
		}
		if (display.getPixelHeight() != null) {
			Element height = doc.createElementNS(CONTEXT_NS, "pixelHeight");
			height.setTextContent(String.valueOf(display.getPixelHeight()));
			disp.appendChild(height);
		}
		if (display.getMmPerPixel() != null) {
			Element mmPer = doc.createElementNS(CONTEXT_NS, "mmPerPixel");
			mmPer.setTextContent(String.valueOf(display.getMmPerPixel()));
			disp.appendChild(mmPer);
		}
		
		addExtensions(doc, disp, display.getExtensions());
		
		return (display.getPixelWidth() != null || display.getPixelHeight() != null || display.getMmPerPixel() != null) ? disp : null;
	}
	
	private Element encodeDateTimeInterval(String ns, String localName, DateTimeInterval interval, Document doc) {
		Element elem = doc.createElementNS(ns, localName);

		/*
		 * If one (either) of the dates is specified, then return the plain date string.
		 * Otherwise if both are specified return "date1/date2".
		 */
		String intervalText = null;
		Date start = interval.getStart();
		if (start == null) {
			start = interval.getEnd();
			if (start == null) {
				return null;		// ignore a bogus interval
			}
		}
		intervalText = encodeDateTime(start);
		Date end = interval.getEnd();
		if (end != null) {
			if (intervalText != null) {
				intervalText += "/";
			}
			intervalText += encodeDateTime(end);
		}
		elem.setTextContent(intervalText);
		
		return elem;
	}
	
	private String encodeDateTime(Date date) {
		String dt = DateFormatManager.getInstance().formatLocalAsUTC(date);
		// dt ends with Z, we need to insert the proper number of milliseconds if that is not zero
		long millis = date.getTime() % 1000;
		if (millis != 0) {
			dt = dt.substring(0, dt.length() - 1) + String.format(".%03d", millis) + "Z";
		}
		return dt;
	}
	
	private Element encodeCategorizedTerm(CategorizedTerm keyword, Document doc) {
		Element elem = doc.createElementNS(ATOM_NS, "category");
		elem.setAttribute("scheme", keyword.getScheme());
		elem.setAttribute("term", keyword.getTerm());
		if (keyword.getLabel() != null) {
			elem.setAttribute("label", keyword.getLabel());
		}
		return elem;
	}
	
	private Element encodeOffering(Offering offering, Document doc) {
		Element elem = doc.createElementNS(CONTEXT_NS, "offering");
		elem.setAttribute("code", offering.getOfferingCode());
		
		for (Operation operation : offering.getOperations()) {
			elem.appendChild(encodeOperation(operation, doc));
		}
		
		for (Content content : offering.getContents()) {
			elem.appendChild(encodeContent(CONTEXT_NS, "content", content, doc));
		}
		
		for (StyleSet style : offering.getStyles()) {
			elem.appendChild(encodeStyleSet(style, doc));
		}
		
		addExtensions(doc, elem, offering.getExtensions());
		
		return elem;
	}
	
	private Element encodeOperation(Operation operation, Document doc) {
		Element elem = doc.createElementNS(CONTEXT_NS, "operation");
		elem.setAttribute("code", operation.getOperationCode());
		elem.setAttribute("method", operation.getOperationMethod());
		if (operation.getType() != null) {
			elem.setAttribute("type", operation.getType());
		}
		elem.setAttribute("href", operation.getRequestURL());
		
		if (operation.getPayload() != null) {
			elem.appendChild(encodeContent(CONTEXT_NS, "payload", operation.getPayload(), doc));
		}

		if (operation.getResult() != null) {
			elem.appendChild(encodeContent(CONTEXT_NS, "result", operation.getResult(), doc));
		}
		
		addExtensions(doc, elem, operation.getExtensions());
		
		return elem;
	}

	private Element encodeContent(String ns, String local, Content content, Document doc) {
		Element elem = doc.createElementNS(ns, local);
		elem.setAttribute("type", content.getType());
		if (content.getUrl() != null) {
			elem.setAttribute("href", content.getUrl());
		}
		
		Object _actualContent = content.getActualContent();
		if (_actualContent != null) {
			if (_actualContent instanceof String) {
				elem.setTextContent((String) _actualContent);
				
			} else if (_actualContent instanceof Document) {
				Document nested = (Document) _actualContent;
				elem.appendChild(doc.adoptNode(nested.getDocumentElement().cloneNode(true)));
				
			} else if (_actualContent instanceof Element) {
				Element nested = (Element) _actualContent;
				elem.appendChild(doc.adoptNode(nested.cloneNode(true)));
			}
		}
		
		addExtensions(doc, elem, content.getExtensions());
		
		return elem;
	}

	private Element encodeStyleSet(StyleSet style, Document doc) {
		Element elem = doc.createElementNS(CONTEXT_NS, "styleSet");
		elem.setAttribute("default", String.valueOf(style.getDefalt()));
		
		Element name = doc.createElementNS(CONTEXT_NS, "name");
		name.setTextContent(style.getName());
		elem.appendChild(name);
		
		Element title = doc.createElementNS(CONTEXT_NS, "title");
		title.setTextContent(style.getTitle());
		elem.appendChild(title);

		if (style.getAbstrakt() != null) {
			Element abstrakt = doc.createElementNS(CONTEXT_NS, "abstract");
			abstrakt.setTextContent(style.getAbstrakt());
			elem.appendChild(abstrakt);
		}

		if (style.getLegendURL() != null) {
			Element legend = doc.createElementNS(CONTEXT_NS, "legendURL");
			legend.setAttribute("href", style.getLegendURL());
			elem.appendChild(legend);
		}
		
		if (style.getContent() != null) {
			elem.appendChild(encodeContent(CONTEXT_NS, "content", style.getContent(), doc));
		}
		
		addExtensions(doc, elem, style.getExtensions());
		
		return elem;
	}
	
	private void addExtensions(Document doc, Element elem, List<Node> extensions) {
		for (Node node : extensions){
			elem.appendChild(doc.importNode(node, true));
		}
	}

	/*
	 * =================================================================================================
	 * Atom (XML) decoding
	 */
	
	@Override
	public ContextDoc decode(String in) throws EncodingException {
		return decode(new InputSource(new StringReader(in)));
	}
	
	@Override
	public ContextDoc decode(InputStream in) throws EncodingException {
		return decode(new InputSource(new InputStreamReader(in)));
	}
	
	private ContextDoc decode(InputSource in) throws EncodingException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		dbf.setCoalescing(true);
		dbf.setIgnoringComments(true);
		dbf.setExpandEntityReferences(true);
		dbf.setIgnoringElementContentWhitespace(true);
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document doc = db.parse(in);
			
			return decode(doc.getDocumentElement());

		} catch (EncodingException e) {
			throw e;
		} catch (Exception e) {
			throw new EncodingException("Failure decoding XML document", e);
		}
	}

	public ContextDoc decode(Element feed) throws EncodingException {
		ContextDoc context = new ContextDoc();
		
		/*
		 * Special case: the feed may consist entirely of a single <entry> element
		 * at the top level.
		 */
		if ("entry".equalsIgnoreCase(feed.getLocalName())) {
			context.getResources().add(decodeResource(feed, context));
			return context;
		}
		
		if (! "feed".equalsIgnoreCase(feed.getLocalName())) {
			throw new EncodingException("Not an OWS Context document");
		}
		
		Element generator = null;
		Element display = null;

		for (Element child : dom.findChildElements(feed)) {
			boolean handled = false;
			String ns = child.getNamespaceURI();
			String local = child.getLocalName();
			
			if (ATOM_NS.equals(ns)) {
				if ("id".equals(local)) {
					context.setId(dom.nodeTextContent(child));
					handled = true;
				} else if ("title".equals(local)) {
					String type = dom.getAttributeValue(child, "type");
					context.setTitle(new TypedText(dom.nodeTextContent(child), type));
					handled = true;
				} else if ("subtitle".equals(local)) {
					String type = dom.getAttributeValue(child, "type");
					context.setSubtitle(new TypedText(dom.nodeTextContent(child), type));
					handled = true;
				} else if ("updated".equals(local)) {
					context.setUpdateDate(decodeDateTime("atom:updated", dom.nodeTextContent(child)));
					handled = true;
				} else if ("author".equals(local)) {
					context.getAuthors().add(decodeAuthorInfo(child));
					handled = true;
				} else if ("rights".equals(local)) {
					context.setRights(dom.nodeTextContent(child));
					handled = true;
				} else if ("generator".equals(local)) {
					generator = child;
					handled = true;
				} else if ("link".equals(local)) {
					String rel = dom.getAttributeValue(child, "rel");
					String uri = dom.getAttributeValue(child, "href");
					String type = dom.getAttributeValue(child, "type");
					String titleStr = dom.getAttributeValue(child, "title");
					
					if ("profile".equals(rel)) {
						// It is supplied, so validate it
						if (!ATOM_ENCODING_SPEC_REF.equals(uri)) {
							throw new EncodingException("Profile link element has href='" + uri + "' rather than '" + ATOM_ENCODING_SPEC_REF + "'; cannot proceed");
						}
						
					} else if ("via".equals(rel)) {
						TypedLink lnk = new TypedLink(uri, type);
						lnk.setTitle(titleStr);
						context.getContextMetadata().add(lnk);
					}
					handled = true;
				} else if ("category".equals(local)) {
					String scheme = dom.getAttributeValue(child, "scheme");
					String term = dom.getAttributeValue(child, "term");
					String label = dom.getAttributeValue(child, "label");
					context.getKeywords().add(new CategorizedTerm(scheme, term, label));
					handled = true;
				} else if ("entry".equals(local)) {
					context.getResources().add(decodeResource(child, context));
					handled = true;
				}

			} else if (DUBLIN_CORE_NS.equals(ns)) {
				if ("date".equals(local)) {
					context.setTimeIntervalOfInterest(decodeDateTimeInterval("dc:date", dom.nodeTextContent(child)));
					handled = true;
				} else if ("publisher".equals(local)) {
					context.setPublisher(dom.nodeTextContent(child));
					handled = true;
				} else if ("rights".equals(local)) {
					context.setRights(dom.nodeTextContent(child));
					handled = true;
				}
				// Hack: ignore any other Dublin Core elements, don't call them "extensions"
				handled = true;
			
			} else if (GEORSS_NS.equals(ns)) {
				if ("where".equals(local)) {
					// georss:where has one child element which is the geometry
					List<Element> children = dom.findChildElements(child);
					switch (children.size()) {
					case 0:
						throw new EncodingException("georss:where has no child elements!");
					case 1:
						context.setAreaOfInterest(getGMLConverter().geometryFromGML(children.get(0)));
						break;
					default:
						throw new EncodingException("georss:where has too many child elements (" + children.size() + ")!");
					}
					handled = true;
				}
				
			} else if (CONTEXT_NS.equals(ns)) {
				if ("display".equals(local)) {
					display = child;
					handled = true;
				}
			}
			
			if (!handled) {
				context.getExtensions().add(child);
			}
		}
		
		context.setCreator(decodeCreator(generator, display));
		
		if (context.getId() == null) {
			throw new EncodingException("A context ID must be supplied");
		}
		
		return context;
	}
	
	private Resource decodeResource(Element entry, ContextDoc context) throws EncodingException {
		Resource resource = new Resource(context);
		
		for (Element child : dom.findChildElements(entry)) {
			boolean handled = false;
			String ns = child.getNamespaceURI();
			String local = child.getLocalName();
			
			if (ATOM_NS.equals(ns)) {
				/*
				 * Check for each of the possible elements in the atom: namespace.
				 */
				if ("id".equals(local)) {
					resource.setId(dom.nodeTextContent(child));
					handled = true;
				} else if ("title".equals(local)) {
					String type = dom.getAttributeValue(child, "type");
					resource.setTitle(new TypedText(dom.nodeTextContent(child), type));
					handled = true;
				} else if ("content".equals(local)) {
					String type = dom.getAttributeValue(child, "type");
					resource.setAbstrakt(new TypedText(dom.nodeTextContent(child), type));
					handled = true;
				} else if ("updated".equals(local)) {
					resource.setUpdateDate(decodeDateTime("atom:updated in entry with id " + resource.getId(), dom.nodeTextContent(child)));
					handled = true;
				} else if ("author".equals(local)) {
					resource.getAuthors().add(decodeAuthorInfo(child));
					handled = true;
				} else if ("rights".equals(local)) {
					resource.setRights(dom.nodeTextContent(child));
					handled = true;
				} else if ("link".equals(local)) {
					String rel = dom.getAttributeValue(child, "rel");
					String uri = dom.getAttributeValue(child, "href");
					String type = dom.getAttributeValue(child, "type");
					String len = dom.getAttributeValue(child, "length");
					Long length = (len == null) ? null : Long.parseLong(len);
					
					if ("alternate".equals(rel)) {
						resource.setContentDescription(new TypedLink(uri, type));
					} else if ("enclosure".equals(rel)) {
						resource.getContentByRefs().add(new TypedLink(uri, type));
					} else if ("icon".equals(rel)) {
						resource.setPreview(new TypedLink(uri, type, null, length));
					}
					handled = true;
				} else if ("category".equals(local)) {
					String scheme = dom.getAttributeValue(child, "scheme");
					String term = dom.getAttributeValue(child, "term");
					String label = dom.getAttributeValue(child, "label");
					
					if (RESOURCE_ACTIVE_SCHEME.equals(scheme)) {
						resource.setActive(Boolean.parseBoolean(term));
					} else if (RESOURCE_FOLDER_SCHEME.equals(scheme)) {
						resource.setFolder(term);
						resource.setFolderLabel(label);
					} else {
						resource.getKeywords().add(new CategorizedTerm(scheme, term, label));
					}
					handled = true;
				}
				
			} else if (DUBLIN_CORE_NS.equals(ns)) {
				if ("publisher".equals(local)) {
					resource.setPublisher(dom.nodeTextContent(child));
					handled = true;
				} else if ("date".equals(local)) {
					resource.setTemporalExtent(decodeDateTimeInterval("dc:date", dom.nodeTextContent(child)));
					handled = true;
				} else if ("rights".equals(local)) {
					resource.setRights(dom.nodeTextContent(child));
					handled = true;
				}
				// Hack: ignore any other Dublin Core elements, don't call them "extensions"
				handled = true;
				
			} else if (GEORSS_NS.equals(ns)) {
				if ("where".equals(local)) {
					// georss:where has one child element which is the geometry
					List<Element> childElements = dom.findChildElements(child);
					switch (childElements.size()) {
					case 0:
						throw new EncodingException("georss:where has no child elements!");
					case 1:
						resource.setGeospatialExtent(getGMLConverter().geometryFromGML(childElements.get(0)));
						break;
					default:
						throw new EncodingException("georss:where has too many child elements (" + childElements.size() + ")!");
					}
					handled = true;
				}
				
			} else if (CONTEXT_NS.equals(ns)) {
				if ("offering".equals(local)) {
					resource.getOfferings().add(decodeOffering(child));
					handled = true;
				} else if ("minScaleDenominator".equals(local)) {
					resource.setMinScaleDenominator(decodeDouble("owc:minScaleDenominator", child));
					handled = true;
				} else if ("maxScaleDenominator".equals(local)) {
					resource.setMaxScaleDenominator(decodeDouble("owc:maxScaleDenominator", child));
					handled = true;
				}
				
			}
			
			if (!handled) {
				resource.getExtensions().add(child);
			}
		}
		
		if (resource.getId() == null) {
			throw new EncodingException("A resource ID must be supplied");
		}
		
		return resource;
	}
	

	private double decodeDouble(String elementName, Element doubleNode) throws EncodingException {
		try {
			return Double.parseDouble(dom.nodeTextContent(doubleNode));
		} catch (NumberFormatException e) {
			throw new EncodingException("Bad value for " + elementName, e);
		}
	}
	
	private Date decodeDateTime(String elementName, String dateTime) throws EncodingException {
		try {
			Calendar cal = DateFormatManager.getInstance().parseDateTime(dateTime);
			return cal.getTime();
		} catch (ParseException e) {
			throw new EncodingException("Badly formatted date/time in " + elementName, e);
		}
	}
	
	/*
	 * start (equal to end)
	 * start/end
	 */
	private static final Pattern DATE_TIME_INTERVAL_PATTERN = Pattern.compile("([^/]+)(/([^/]+))?");
	
	private DateTimeInterval decodeDateTimeInterval(String elementName, String dateTimeInterval) throws EncodingException {
		
		Matcher m = DATE_TIME_INTERVAL_PATTERN.matcher(dateTimeInterval);
		if (! m.matches()) {
			throw new EncodingException("Badly formatted date/time interval in " + elementName);
		}
		
		try {
			Calendar cal = DateFormatManager.getInstance().parseDateTime(m.group(1));
			Date start = cal.getTime();
		
			Date end = start;
			if (m.group(2) != null) {
				cal = DateFormatManager.getInstance().parseDateTime(m.group(3));
				end = cal.getTime();
			}

			return new DateTimeInterval(start, end);
			
		} catch (ParseException e) {
			throw new EncodingException("Badly formatted date/time in " + elementName, e);
		}
	}
	
	private Creator decodeCreator(Element generator, Element display) throws EncodingException {
		CreatorApplication app;
		if (generator == null) {
			app = null;
		} else {
			app = new CreatorApplication();
			app.setUri(dom.getAttributeValue(generator, "uri"));
			app.setVersion(dom.getAttributeValue(generator, "version"));
			app.setTitle(dom.nodeTextContent(generator));
		}
		
		CreatorDisplay disp;
		if (display == null) {
			disp = null;
		} else {
			Element displayWidth = dom.findFirstChildNamed(display, CONTEXT_NS, "pixelWidth");
			Element displayHeight = dom.findFirstChildNamed(display, CONTEXT_NS, "pixelHeight");
			Element mmPerPixel = dom.findFirstChildNamed(display, CONTEXT_NS, "mmPerPixel");
			
			Integer pixelWidth;
			Integer pixelHeight;
			Double mmPerPix;
			try {
				pixelWidth = (displayWidth == null) ? null : Integer.parseInt(dom.nodeTextContent(displayWidth));
				pixelHeight = (displayHeight == null) ? null : Integer.parseInt(dom.nodeTextContent(displayHeight));
				mmPerPix = (mmPerPixel == null) ? null : Double.parseDouble(dom.nodeTextContent(mmPerPixel));
			} catch (NumberFormatException e) {
				throw new EncodingException("Badly formatted owc:display parameters", e);
			}
			
			disp = new CreatorDisplay(pixelWidth, pixelHeight);
			disp.setMmPerPixel(mmPerPix);
		}
		
//		for (Element extension : dom.findChildrenNamed(display, CONTEXT_NS, "extension")) {
//			disp.getExtensions().addAll(dom.findChildElements(extension));
//		}
		
		return (app == null && disp == null) ? null : new Creator(app, disp);
	}
	
	private Offering decodeOffering(Element offering) throws EncodingException {
		Offering offer = OfferingFactory.getInstance().createOffering(dom.getAttributeValue(offering, "code"));
		
		for (Element child : dom.findChildElements(offering)) {
			boolean handled = false;
			String ns = child.getNamespaceURI();
			String local = child.getLocalName();
			
			if (CONTEXT_NS.equals(ns)) {
				if ("operation".equals(local)) {
					offer.getOperations().add(decodeOperation(child));
					handled = true;
				} else if ("content".equals(local)) {
					offer.getContents().add(decodeContent(child));
					handled = true;
				} else if ("styleSet".equals(local)) {
					offer.getStyles().add(decodeStyleSet(child));
					handled = true;
				}
			}
			
			if (!handled) {
				offer.getExtensions().add(child);
			}
		}

		return offer;
	}
	
	private Operation decodeOperation(Element operation) throws EncodingException {
		Operation oper = new Operation();

		oper.setOperationCode(dom.getAttributeValue(operation, "code"));
		oper.setOperationMethod(dom.getAttributeValue(operation, "method"));
		oper.setType(dom.getAttributeValue(operation, "type"));
		oper.setRequestURL(dom.getAttributeValue(operation, "href"));
		
		for (Element child : dom.findChildElements(operation)) {
			boolean handled = false;
			String ns = child.getNamespaceURI();
			String local = child.getLocalName();
			
			if (CONTEXT_NS.equals(ns)) {
				if ("request".equals(local)) {
					oper.setPayload(decodeContent(child));
					handled = true;
				} else if ("result".equals(local)) {
					oper.setResult(decodeContent(child));
					handled = true;
				}
			}
			
			if (!handled) {
				oper.getExtensions().add(child);
			}
		}
		
		return oper;
	}
	
	private Content decodeContent(Element content) throws EncodingException {
		Content cont = new Content();
		
		cont.setType(dom.getAttributeValue(content, "type"));
		cont.setUrl(dom.getAttributeValue(content, "href"));
		String contentText = dom.nodeTextContent(content);
		if (contentText != null && !"".equals(contentText.trim())) {
			cont.setActualContent(contentText.trim());
		} else {
			/*
			 * If it's not a string, the content had better be a well-formed XML document.
			 * Pick out the first Element node from the children, and call it the
			 * root node.
			 */
			List<Element> children = dom.findChildElements(content);
			switch (children.size()) {
			case 0:
//				throw new EncodingException("No content found in owc:content element!");
				cont.setActualContent("");	// be more resilient
				break;
			case 1:
				cont.setActualContent(children.get(0));
				break;
			default:
				throw new EncodingException("Too many child elements (" + children.size() + ") found under an owc:content, owc:payload or owc:result!");
			}
		}
		
		return cont;
	}
	
	private StyleSet decodeStyleSet(Element styleSet) throws EncodingException {
		StyleSet style = new StyleSet();
		
		String defalt = dom.getAttributeValue(styleSet, "default");
		style.setDefalt(defalt == null ? false : Boolean.parseBoolean(defalt));

		for (Element child : dom.findChildElements(styleSet)) {
			boolean handled = false;
			String ns = child.getNamespaceURI();
			String local = child.getLocalName();
			
			if (CONTEXT_NS.equals(ns)) {
				if ("name".equals(local)) {
					style.setName(dom.nodeTextContent(child));
					handled = true;
				} else if ("title".equals(local)) {
					style.setTitle(dom.nodeTextContent(child));
					handled = true;
				} else if ("abstract".equals(local)) {
					style.setAbstrakt(dom.nodeTextContent(child));
					handled = true;
				} else if ("legendURL".equals(local)) {
					style.setLegendURL(dom.getAttributeValue(child, "href"));
					handled = true;
				} else if ("content".equals(local)) {
					style.setContent(decodeContent(child));
					handled = true;
				}
			}
			
			if (!handled) {
				style.getExtensions().add(child);
			}
		}
		
		return style;
	}
	
	private AuthorInfo decodeAuthorInfo(Element author) throws EncodingException {
		Element name  = dom.findFirstChildNamed(author, ATOM_NS, "name");
		Element email = dom.findFirstChildNamed(author, ATOM_NS, "email");
		Element uri   = dom.findFirstChildNamed(author, ATOM_NS, "uri");
		return new AuthorInfo(dom.nodeTextContent(name), dom.nodeTextContent(email), dom.nodeTextContent(uri));
	}
	
}
