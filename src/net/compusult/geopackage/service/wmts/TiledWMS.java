/*
 * TiledWMS.java
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
import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.Reference;

import com.vividsolutions.jts.geom.Envelope;

public class TiledWMS extends Simple3857TileServer {
	
	private static final Logger LOG = Logger.getLogger(TiledWMS.class);
	
	private final Reference request;
	private final Form query;
	private final String crs;
	private final Envelope availableData;
	
	public TiledWMS(Reference request, String crs, Envelope availableData, String tileFormat, Map<String, String> params) {
		super("", tileFormat, params);
		this.request = request;
		this.query = request.getQueryAsForm(true);
		this.crs = crs;
		this.availableData = availableData;
	}

	@Override
	public String getUrl(String tileMatrix, int tileRow, int tileCol) {
		Reference synthesized = new Reference(request);
		
		/*
		 * Determine an appropriate bounding box for this request.
		 */
		double width = availableData.getMaxX() - availableData.getMinX();
		double height = availableData.getMaxY() - availableData.getMinY();
		int matrixWidth = getMatrixWidth(tileMatrix);
		int matrixHeight = getMatrixHeight(tileMatrix);
		double llx = availableData.getMinX() + tileCol * width / matrixWidth;
		double lly = availableData.getMinY() + tileRow * height / matrixHeight;
		double urx = llx + width / matrixWidth;
		double ury = lly + height / matrixHeight;
		StringBuilder buf = new StringBuilder();
		buf.append(llx).append(',');
		buf.append(lly).append(',');
		buf.append(urx).append(',');
		buf.append(ury);
		query.set("BBOX", buf.toString());
		
		query.set("WIDTH", String.valueOf(getTileWidthPixels(tileMatrix)));
		query.set("HEIGHT", String.valueOf(getTileHeightPixels(tileMatrix)));
		
		try {
			synthesized.setQuery(query.encode());
		} catch (IOException e) {
			LOG.error("Synthesizing WMS request", e);
			return null;
		}
		return synthesized.toString();
	}

	@Override
	public double getPixelWidthInMeters(String identifier) {
		return 0;
	}

	@Override
	public double getPixelHeightInMeters(String identifier) {
		return 0;
	}

	@Override
	public double getMinX() {
		return availableData.getMinX();
	}

	@Override
	public double getMinY() {
		return availableData.getMinY();
	}

	@Override
	public double getMaxX() {
		return availableData.getMaxX();
	}

	@Override
	public double getMaxY() {
		return availableData.getMaxY();
	}

	@Override
	public String getCRS() {
		return crs;
	}

}
