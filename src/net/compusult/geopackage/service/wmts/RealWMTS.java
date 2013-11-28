package net.compusult.geopackage.service.wmts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import net.compusult.geopackage.service.model.WMTSTileMatrix;
import net.compusult.geopackage.service.wmts.WMTSCapabilitiesDocument.WMTSLayerInfo;
import net.compusult.geopackage.service.wmts.WMTSCapabilitiesDocument.TileMatrixSetScanner;

import org.stringtemplate.v4.ST;
import org.xml.sax.SAXException;

public class RealWMTS extends TileServer {

	protected final String capabilitiesUrl;
	protected final WMTSLayerInfo layerInfo;
	protected final String layerName;
	protected final String tileMatrixSet;
	protected final List<WMTSTileMatrix> tileMatrices;		// insertion order is important
	protected final Map<String, String> params;

	public RealWMTS(String capabilitiesUrl, Map<String, String> params) {
		super();
		
		this.capabilitiesUrl = capabilitiesUrl;
		this.layerName = params.get("LayerName");
		this.tileMatrixSet = params.get("TileMatrixSet");
		this.tileMatrices = new ArrayList<WMTSTileMatrix>();
		this.params = params;
		
		try {
			this.layerInfo = parseCapabilities();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to parse capabilities document", e);
		}
	}
	
	private WMTSLayerInfo parseCapabilities() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, XPathFactoryConfigurationException {
		String uri = capabilitiesUrl;
		WMTSCapabilitiesDocument capabilities = new WMTSCapabilitiesDocument(uri);
		
		capabilities.scanTileMatrixSet(tileMatrixSet, new TileMatrixSetScanner() {
			@Override
			public void onTileMatrix(String identifier, double scaleDenom, double ulx, double uly,
					int tileWidth, int tileHeight, int matrixWidth, int matrixHeight) {
				
				tileMatrices.add(
						new WMTSTileMatrix(identifier, scaleDenom, ulx, uly,
								tileWidth, tileHeight, matrixWidth, matrixHeight));
			}
		});
		
		return capabilities.getLayerInfo(layerName);
	}

	@Override
	public String getUrl(String tileMatrix, int tileRow, int tileCol) {
		
		String style = params.get("style");
		if (style == null) {
			style = layerInfo.getDefaultStyle();
		}
		if (style == null) {
			style = "";
		}
		
		ST template = new ST(layerInfo.getTemplate(), '{', '}');
		template.add("LayerName", layerName);
		template.add("TileMatrixSet", tileMatrixSet);
		template.add("TileMatrix", tileMatrix);
		template.add("TileCol", String.valueOf(tileCol));
		template.add("TileRow", String.valueOf(tileRow));
		template.add("style", style);
		
		template.add("ImageFormat", layerInfo.getFormat());
		String[] pieces = layerInfo.getFormat().split("/");
		if (pieces != null && pieces.length > 0) {
			template.add("ImageFormatSuffix", pieces[pieces.length - 1]);
		}
		
		if (tileMatrix.matches("^\\d+$")) {
			int zoomScale = Integer.parseInt(tileMatrix);
			template.add("QuadTreeId", getQuadTreeId(1 << zoomScale, 1 << zoomScale, tileRow, tileCol));
		}
		
		return template.render();
	}

	@Override
	public boolean hasZoomScale(String identifier) {
		return getTileMatrix(identifier, false) != null;
	}
	
	@Override
	public int getTileWidthPixels(String identifier) {
		WMTSTileMatrix tileMatrix = getTileMatrix(identifier);
		return tileMatrix.getTileWidth();
	}
	
	@Override
	public int getTileHeightPixels(String identifier) {
		WMTSTileMatrix tileMatrix = getTileMatrix(identifier);
		return tileMatrix.getTileHeight();
	}
	
	@Override
	public int getMatrixWidth(String identifier) {
		WMTSTileMatrix tileMatrix = getTileMatrix(identifier);
		return tileMatrix.getMatrixWidth();
	}

	@Override
	public int getMatrixHeight(String identifier) {
		WMTSTileMatrix tileMatrix = getTileMatrix(identifier);
		return tileMatrix.getMatrixHeight();
	}

	@Override
	public double getPixelWidthInMeters(String identifier) {
		WMTSTileMatrix tileMatrix = getTileMatrix(identifier);
		// See section 6.1 and Annex E of OGC 07-057r7
		return tileMatrix.getScaleDenom() * PHYS_PIXEL_SIZE;
	}

	@Override
	public double getPixelHeightInMeters(String identifier) {
		WMTSTileMatrix tileMatrix = getTileMatrix(identifier);
		// See section 6.1 and Annex E of OGC 07-057r7
		return tileMatrix.getScaleDenom() * PHYS_PIXEL_SIZE;
	}

	@Override
	public double getMinX() {
		return layerInfo.getBbox().getLlx();
	}

	@Override
	public double getMinY() {
		return layerInfo.getBbox().getLly();
	}

	@Override
	public double getMaxX() {
		return layerInfo.getBbox().getUrx();
	}

	@Override
	public double getMaxY() {
		return layerInfo.getBbox().getUry();
	}

	private WMTSTileMatrix getTileMatrix(String identifier) {
		return getTileMatrix(identifier, true);
	}

	private WMTSTileMatrix getTileMatrix(String identifier, boolean fatal) {
		for (WMTSTileMatrix m : tileMatrices) {
			if (identifier.equals(m.getIdentifier())) {
				return m;
			}
		}
		if (fatal) {
			throw new IllegalArgumentException("Tile matrix '" + identifier + "' is not defined");
		}
		return null;
	}
	
	// first and/or last may be null to refer to the beginning and end of the list, resp.
	@Override
	public List<String> getTileMatricesBetween(String first, String last) {
		List<String> result = new ArrayList<String>();
		boolean producing = false;
		
		for (WMTSTileMatrix m : tileMatrices) {
			if (!producing && (first == null || first.equals(m.getIdentifier()))) {
				producing = true;
			}
			if (producing) {
				result.add(m.getIdentifier());
				if (last != null && last.equals(m.getIdentifier())) {
					break;
				}
			}
		}
		return result;
	}

	// Find the 0-based index of the given identifier in the tile matrix list
	@Override
	public int findTileMatrixIndex(String identifier) {
		int index = 0;
		for (WMTSTileMatrix m : tileMatrices) {
			if (identifier.equals(m.getIdentifier())) {
				return index;
			}
			++ index;
		}
		return 0;
	}

	@Override
	public String getCRS() {
		String crs = layerInfo.getCrs();
		if (crs.startsWith("urn:ogc:def:crs:EPSG::")) {
			crs = crs.substring(22);
		}
		return crs;
	}
	
}
