/*
 * Simple3857TileServer.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;

public class Simple3857TileServer extends TileServer {
	
	private static final double MAX_SCALE_DENOM = 559082264.0287178;	// from OGC 07-057r7
	private static final double OSM_EXTENT = 20037508.3427892;

	private final String urlTemplate;
	private final String tileFormat;
	private final String layerName;
	private final String tileMatrixSet;
	private final Map<String, String> params;
	
	public Simple3857TileServer(String urlTemplate, String tileFormat, Map<String, String> params) {
		super();
		this.urlTemplate = urlTemplate;
		this.tileFormat = tileFormat;
		this.layerName = params.get("LayerName");
		this.tileMatrixSet = params.get("TileMatrixSet");
		this.params = params;
	}

	@Override
	public int getTileWidthPixels(String identifier) {
		return 256;
	}
	
	@Override
	public int getTileHeightPixels(String identifier) {
		return 256;
	}
	
	/**
	 * Get the approximate width in meters of a single pixel at the given scale.
	 */
	@Override
	public double getPixelWidthInMeters(String identifier) {
		// This corresponds to the GoogleCRS84Quad scale set in Annex E of OGC 07-057r7
		// .00028 is an estimate of physical pixel size in meters; i.e. 0.28mm
		double largestScaleDenominator = MAX_SCALE_DENOM;
		double scaleDenominatorForThisScale = largestScaleDenominator / getMatrixWidth(identifier);
		return scaleDenominatorForThisScale * PHYS_PIXEL_SIZE;
	}
	
	/**
	 * Get the approximate height in meters of a single pixel at the given scale.
	 */
	@Override
	public double getPixelHeightInMeters(String identifier) {
		// This corresponds to the GoogleCRS84Quad scale set in Annex E of OGC 07-057r7
		// .00028 is an estimate of physical pixel size in meters; i.e. 0.28mm
		double largestScaleDenominator = MAX_SCALE_DENOM;
		double scaleDenominatorForThisScale = largestScaleDenominator / getMatrixWidth(identifier);
		return scaleDenominatorForThisScale * PHYS_PIXEL_SIZE;
	}

	/**
	 * Returns the number of tiles across the whole matrix at the given zoom scale.
	 */
	@Override
	public int getMatrixWidth(String identifier) {
		int zoomScale = Integer.parseInt(identifier);
		return 1 << zoomScale;
	}

	/**
	 * Returns the number of tiles vertically at the given zoom scale.
	 */
	@Override
	public int getMatrixHeight(String identifier) {
		int zoomScale = Integer.parseInt(identifier);
		return 1 << zoomScale;
	}

	@Override
	public boolean hasZoomScale(String identifier) {
		int zoomScale = Integer.parseInt(identifier);
		return zoomScale >= 0 && zoomScale <= 18;
	}
	
	@Override
	public double getMinX() {
		return - OSM_EXTENT;
	}

	@Override
	public double getMinY() {
		return - OSM_EXTENT;
	}

	@Override
	public double getMaxX() {
		return OSM_EXTENT;
	}

	@Override
	public double getMaxY() {
		return OSM_EXTENT;
	}

	@Override
	public String getUrl(String tileMatrix, int tileRow, int tileCol) {
		
		String style = params.get("style");
		
		ST template = new ST(urlTemplate, '{', '}');
		template.add("LayerName", layerName == null ? "" : layerName);
		template.add("TileMatrixSet", tileMatrixSet == null ? "" : tileMatrixSet);
		template.add("TileMatrix", tileMatrix);
		template.add("TileCol", String.valueOf(tileCol));
		template.add("TileRow", String.valueOf(tileRow));
		template.add("style", style == null ? "" : style);
		
		template.add("ImageFormat", tileFormat);
		String[] pieces = tileFormat.split("/");
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
	public List<String> getTileMatricesBetween(String first, String last) {
		List<String> result = new ArrayList<String>();
		boolean producing = false;
		
		for (int i = 0; i <= 18; ++ i) {
			String ident = String.valueOf(i);
			if (!producing && (first == null || first.equals(ident))) {
				producing = true;
			}
			if (producing) {
				result.add(ident);
				if (last != null && last.equals(ident)) {
					break;
				}
			}
		}
		return result;
	}

	@Override
	public int findTileMatrixIndex(String identifier) {
		return Integer.parseInt(identifier);
	}

	@Override
	public String getCRS() {
		return "3857";
	}
	
}
