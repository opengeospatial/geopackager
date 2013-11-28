package net.compusult.geopackage.service.wmts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.compusult.geopackage.service.model.Rectangle;
import net.compusult.xml.DOMUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class WMTSCapabilitiesDocument {

	private static final String WMTS_NS = "http://www.opengis.net/wmts/1.0";
	private static final String OWS_NS = "http://www.opengis.net/ows/1.1";
	
	private final Document capabilities;
	private final DOMUtil domUtil;
	
	private final Element contents;
	private final Map<String, Element> tileMatrixSets;
	
	public WMTSCapabilitiesDocument(String url) throws IOException, ParserConfigurationException, SAXException {
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
			if (layerInfoElem != null) {
				WMTSLayerInfo layerInfo = new WMTSLayerInfo(
						defaultStyle,
						domUtil.getAttributeValue(layerInfoElem, "format"),
						domUtil.getAttributeValue(layerInfoElem, "template"));
				getAdditionalTileMatrixSetInfo(findLinkedTileMatrixSet(layerName), layerInfo);
				return layerInfo;
			}
		}
		return null;
	}
	
	public String findLinkedTileMatrixSet(String layerName) {
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
	
	private void getAdditionalTileMatrixSetInfo(String tileMatrixSet, WMTSLayerInfo layerInfo) {
		Element tms = tileMatrixSets.get(tileMatrixSet);
		if (tms != null) {
			Element title = domUtil.findFirstChildNamed(tms, OWS_NS, "Title");
			layerInfo.setTitle(domUtil.nodeTextContent(title));
			
			Element crs = domUtil.findFirstChildNamed(tms, OWS_NS, "SupportedCRS");
			layerInfo.setCrs(domUtil.nodeTextContent(crs));
			
			Element bboxElem = domUtil.findFirstChildNamed(tms, OWS_NS, "BoundingBox");
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
	
	public static class WMTSLayerInfo {
		private final String defaultStyle;
		private final String format;
		private final String template;
		private String title;
		private String crs;
		private Rectangle bbox;
		
		public WMTSLayerInfo(String defaultStyle, String format, String template) {
			this.defaultStyle = defaultStyle;
			this.format = format;
			this.template = template;
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
		
	}
	
}
