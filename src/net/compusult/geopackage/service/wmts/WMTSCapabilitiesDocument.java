/*
 * WMTSCapabilitiesDocument.java
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
   
package net.compusult.geopackage.service.wmts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.compusult.geopackage.service.model.Rectangle;
import net.compusult.xml.DOMUtil;

import org.apache.log4j.Logger;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WMTSCapabilitiesDocument {
	
	private static final Logger LOG = Logger.getLogger(WMTSCapabilitiesDocument.class);

	private static final String WMTS_NS  = "http://www.opengis.net/wmts/1.0";
	private static final String OWS_NS   = "http://www.opengis.net/ows/1.1";
	private static final String XLINK_NS = "http://www.w3.org/1999/xlink";
	
	private static final SimpleNamespaceContext MY_NAMESPACE_CONTEXT = new SimpleNamespaceContext();
    static {
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("wmts",  WMTS_NS);
		namespaces.put("ows",   OWS_NS);
		namespaces.put("xlink", XLINK_NS);
		
		MY_NAMESPACE_CONTEXT.setBindings(namespaces);
    }

	private final Document capabilities;
	private final DOMUtil domUtil;
	private final XPathFactory xpathFactory;
	
	private final Element contents;
	private final Map<String, Element> tileMatrixSets;
	
	private String kvpGetTile;
	
	
	public WMTSCapabilitiesDocument(String url) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		this.capabilities = db.parse(url);
		this.domUtil = new DOMUtil();
		this.contents = domUtil.findFirstChildNamed(capabilities.getDocumentElement(), WMTS_NS, "Contents");
		this.tileMatrixSets = new HashMap<String, Element>();
		
		for (Element tms : domUtil.findChildrenNamed(contents, WMTS_NS, "TileMatrixSet")) {
			Element identifier = domUtil.findFirstChildNamed(tms, OWS_NS, "Identifier");
			if (identifier != null) {
				String ident = domUtil.nodeTextContent(identifier);
				tileMatrixSets.put(ident, tms);
			}
		}
		
		this.xpathFactory = XPathFactory.newInstance();
		
		XPath xpath = xpathFactory.newXPath();
		xpath.setNamespaceContext(MY_NAMESPACE_CONTEXT);
		this.kvpGetTile = (String)
				xpath.evaluate("//ows:OperationsMetadata/ows:Operation[@name='GetTile']/ows:DCP/ows:HTTP/ows:Get/@xlink:href",
						capabilities.getDocumentElement(), XPathConstants.STRING);
	}
	
	public Element findLayer(String layerName) {
		
		for (Element layer : domUtil.findChildrenNamed(contents, WMTS_NS, "Layer")) {
			Element identifier = domUtil.findFirstChildNamed(layer, OWS_NS, "Identifier");
			if (identifier != null) {
				String ident = domUtil.nodeTextContent(identifier);
				if (ident.equals(layerName)) {
					return layer;
				}
			}
		}
		
		return null;
	}
	
	public WMTSLayerInfo getLayerInfo(String layerName) {
		Element layer = findLayer(layerName);
		if (layer != null) {
			String defaultStyle = "";
			for (Element style : domUtil.findChildrenNamed(layer, WMTS_NS, "Style")) {
				if (Boolean.parseBoolean(domUtil.getAttributeValue(style, "isDefault"))) {
					Element identifier = domUtil.findFirstChildNamed(style, OWS_NS, "Identifier");
					if (identifier != null) {
						defaultStyle = domUtil.nodeTextContent(identifier);
					}
				}
			}
			Element layerInfoElem = domUtil.findFirstChildNamed(layer, WMTS_NS, "ResourceURL");
			WMTSLayerInfo layerInfo;
			
			if (layerInfoElem != null) {
				layerInfo = new WMTSLayerInfo(
						defaultStyle,
						domUtil.getAttributeValue(layerInfoElem, "format"),
						domUtil.getAttributeValue(layerInfoElem, "template"),
						true);
			} else {
				layerInfo = new WMTSLayerInfo(
						defaultStyle,
						"image/png",		// FIXME
						kvpGetTile,
						false);				// not a restful service
			}
			
			try {
				getAdditionalTileMatrixSetInfo(layerName, layerInfo);
			} catch (XPathExpressionException e) {
				LOG.error("Failed to gather additional tile matrix set information", e);
				return null;
			}
			return layerInfo;
		}
		return null;
	}
	
	private String findLinkedTileMatrixSet(String layerName) {
		Element layer = findLayer(layerName);
		if (layer != null) {
			Element tmsLink = domUtil.findFirstChildNamed(layer, WMTS_NS, "TileMatrixSetLink");
			if (tmsLink != null) {
				Element tms = domUtil.findFirstChildNamed(tmsLink, WMTS_NS, "TileMatrixSet");
				if (tms != null) {
					return domUtil.nodeTextContent(tms);
				}
			}
		}
		
		return null;
	}
	
	private void getAdditionalTileMatrixSetInfo(String layerName, WMTSLayerInfo layerInfo) throws XPathExpressionException {
		String tileMatrixSet = findLinkedTileMatrixSet(layerName);
		Element tms = tileMatrixSets.get(tileMatrixSet);
		if (tms != null) {
			Element title = domUtil.findFirstChildNamed(tms, OWS_NS, "Title");
			if (title == null) {
				layerInfo.setTitle(tileMatrixSet);
			} else {
				layerInfo.setTitle(domUtil.nodeTextContent(title));
			}
			
			Element crs = domUtil.findFirstChildNamed(tms, OWS_NS, "SupportedCRS");
			layerInfo.setCrs(domUtil.nodeTextContent(crs));
			
			Element bboxElem = domUtil.findFirstChildNamed(tms, OWS_NS, "BoundingBox");
			if (bboxElem != null) {
				Element ll = domUtil.findFirstChildNamed(bboxElem, OWS_NS, "LowerCorner");
				Element ur = domUtil.findFirstChildNamed(bboxElem, OWS_NS, "UpperCorner");
				String lltext = domUtil.nodeTextContent(ll);
				String urtext = domUtil.nodeTextContent(ur);
				String[] llpieces = lltext.split("\\s+");
				String[] urpieces = urtext.split("\\s+");
				double llx = Double.parseDouble(llpieces[0]);
				double lly = Double.parseDouble(llpieces[1]);
				double urx = Double.parseDouble(urpieces[0]);
				double ury = Double.parseDouble(urpieces[1]);
				layerInfo.setBbox(new Rectangle(llx, lly, urx, ury));
			} else {
				layerInfo.setBbox(new Rectangle(-180,-90,180,90));
			}
		}
		
		Element layer = findLayer(layerName);
		XPath xpath = xpathFactory.newXPath();
		xpath.setNamespaceContext(MY_NAMESPACE_CONTEXT);
		NodeList limitNodes = (NodeList)
				xpath.evaluate("./wmts:TileMatrixSetLink[wmts:TileMatrixSet/text() = '" + tileMatrixSet + "']/wmts:TileMatrixSetLimits/wmts:TileMatrixLimits",
						layer, XPathConstants.NODESET);
		
		for (int limit = 0, nlimit = limitNodes.getLength(); limit < nlimit; ++ limit) {
			Element thisNode = (Element) limitNodes.item(limit);
			Element tileMatrix = domUtil.findFirstChildNamed(thisNode, WMTS_NS, "TileMatrix");
			Element minTileRowElem = domUtil.findFirstChildNamed(thisNode, WMTS_NS, "MinTileRow");
			Element maxTileRowElem = domUtil.findFirstChildNamed(thisNode, WMTS_NS, "MaxTileRow");
			Element minTileColElem = domUtil.findFirstChildNamed(thisNode, WMTS_NS, "MinTileCol");
			Element maxTileColElem = domUtil.findFirstChildNamed(thisNode, WMTS_NS, "MaxTileCol");
			
			RowColLimits rcl = new RowColLimits(
					Integer.parseInt(domUtil.nodeTextContent(minTileRowElem)),
					Integer.parseInt(domUtil.nodeTextContent(maxTileRowElem)),
					Integer.parseInt(domUtil.nodeTextContent(minTileColElem)),
					Integer.parseInt(domUtil.nodeTextContent(maxTileColElem)));
			layerInfo.addLimits(tileMatrixSet, domUtil.nodeTextContent(tileMatrix), rcl);
		}
	}
	
	public void scanTileMatrixSet(String tileMatrixSet, TileMatrixSetScanner callback) {
		
		Element tms = tileMatrixSets.get(tileMatrixSet);
		if (tms != null) {
			for (Element elem : domUtil.findChildrenNamed(tms, WMTS_NS, "TileMatrix")) {
				Element identElem = domUtil.findFirstChildNamed(elem, OWS_NS, "Identifier");
				if (identElem != null) {
					String identifier = domUtil.nodeTextContent(identElem);

					Element scaleDenomElem = domUtil.findFirstChildNamed(elem, WMTS_NS, "ScaleDenominator");
					double scaleDenom = Double.parseDouble(domUtil.nodeTextContent(scaleDenomElem));
					
					Element doublePairElem = domUtil.findFirstChildNamed(elem, WMTS_NS, "TopLeftCorner");
					String[] ul = domUtil.nodeTextContent(doublePairElem).split("\\s+");
					double ulx = Double.parseDouble(ul[0]);
					double uly = Double.parseDouble(ul[1]);
					
					Element tileWidthElem = domUtil.findFirstChildNamed(elem, WMTS_NS, "TileWidth");
					int tileWidth = Integer.parseInt(domUtil.nodeTextContent(tileWidthElem));
					
					Element tileHeightElem = domUtil.findFirstChildNamed(elem, WMTS_NS, "TileHeight");
					int tileHeight = Integer.parseInt(domUtil.nodeTextContent(tileHeightElem));
					
					Element matrixWidthElem = domUtil.findFirstChildNamed(elem, WMTS_NS, "MatrixWidth");
					int matrixWidth = Integer.parseInt(domUtil.nodeTextContent(matrixWidthElem));
					
					Element matrixHeightElem = domUtil.findFirstChildNamed(elem, WMTS_NS, "MatrixHeight");
					int matrixHeight = Integer.parseInt(domUtil.nodeTextContent(matrixHeightElem));
					
					callback.onTileMatrix(identifier, scaleDenom, ulx, uly, tileWidth, tileHeight, matrixWidth, matrixHeight);
				}
			}
		}
	}
	
	public interface TileMatrixSetScanner {
		void onTileMatrix(String identifier, double scaleDenom, double ulx, double uly,
				int tileWidth, int tileHeight, int matrixWidth, int matrixHeight);
	}
	
	public class WMTSLayerInfo {
		private final String defaultStyle;
		private final String format;
		private final String template;
		private final boolean restful;
		private String title;
		private String crs;
		private Rectangle bbox;
		private final Map<String, Map<String, RowColLimits>> limits;		// indexed by TileMatrixSet name, value is a map from TileMatrix name to the limits for that matrix
		
		public WMTSLayerInfo(String defaultStyle, String format, String template, boolean restful) {
			this.defaultStyle = defaultStyle;
			this.format = format;
			this.template = template;
			this.restful = restful;
			this.limits = new HashMap<String, Map<String, RowColLimits>>();
		}

		public String getDefaultStyle() {
			return defaultStyle;
		}

		public String getFormat() {
			return format;
		}
		
		public String getTemplate() {
			return template;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getCrs() {
			return crs;
		}

		public void setCrs(String crs) {
			this.crs = crs;
		}

		public Rectangle getBbox() {
			return bbox;
		}

		public void setBbox(Rectangle bbox) {
			this.bbox = bbox;
		}

		public boolean isRestful() {
			return restful;
		}
		
		public void addLimits(String tileMatrixSet, String tileMatrix, RowColLimits lim) {
			Map<String, RowColLimits> map = limits.get(tileMatrixSet);
			if (map == null) {
				map = new HashMap<String, RowColLimits>();
				limits.put(tileMatrixSet, map);
			}
			map.put(tileMatrix, lim);
		}

		public Map<String, Map<String, RowColLimits>> getLimits() {
			return limits;
		}
		
	}
	
	public class RowColLimits {
		private final int minRow;
		private final int maxRow;
		private final int minCol;
		private final int maxCol;
		
		public RowColLimits(int minRow, int maxRow, int minCol, int maxCol) {
			this.minRow = minRow;
			this.maxRow = maxRow;
			this.minCol = minCol;
			this.maxCol = maxCol;
		}

		public int getMinRow() {
			return minRow;
		}

		public int getMaxRow() {
			return maxRow;
		}

		public int getMinCol() {
			return minCol;
		}

		public int getMaxCol() {
			return maxCol;
		}

	}
	
}
