/*
 * WMSHarvester.java
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
   
package net.compusult.geopackage.service.geopackager;

import java.util.Map;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.geopackage.service.model.LayerInformation;
import net.compusult.geopackage.service.model.LayerInformation.Type;
import net.compusult.geopackage.service.wmts.TileServer;
import net.compusult.geopackage.service.wmts.TiledWMS;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Operation;
import net.compusult.owscontext.Resource;

import org.restlet.data.Form;
import org.restlet.data.Reference;

import com.vividsolutions.jts.geom.Envelope;

public class WMSHarvester extends AbstractWMTSHarvester {
	
	public WMSHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}
	
	@Override
	public void harvest(GeoPackage gpkg, Resource resource, Offering offering) throws GeoPackageException {

		Operation getMap = findRequiredOperation(offering, "GetMap");
		
		Map<String, String> params = parseParameters(offering);
		
		Reference getMapRequest = new Reference(getMap.getRequestURL());
		
		Form query = getMapRequest.getQueryAsForm(true);
		String srs = query.getFirstValue("SRS");
		
		Envelope rect;
		String bboxStr = query.getFirstValue("BBOX", "");
		String[] pieces = bboxStr.split(",");
		if (pieces.length == 4) {
			// specified bounding box
			rect = new Envelope(
						Double.parseDouble(pieces[0]),
						Double.parseDouble(pieces[2]),
						Double.parseDouble(pieces[1]),
						Double.parseDouble(pieces[3])
					);
			if (srs.startsWith("EPSG:")) {
				srs = srs.substring(5);
			}
		} else {
			// use extents given in a georss:where clause
			srs = "4326";
			rect = selectEnvelope(resource);
		}
		
		String requestedTileFormat = query.getFirstValue("FORMAT", "image/png");
		
		if (params.get("fromMatrix") == null && params.get("toMatrix") == null) {
			params.put("fromMatrix", "0");
			params.put("toMatrix", "0");
		}
		TileServer wms = new TiledWMS(getMapRequest, srs, rect, requestedTileFormat, params);
		
		LayerInformation layerInfo = new LayerInformation(gpkg, Type.TILES, sanitizeTableName(resource.getId()));
		layerInfo.setTitle(resource.getTitle().getText());
		layerInfo.setCrs(srs);
		
		HarvestTiles tileHarvester = new HarvestTiles(this, gpkg, wms, layerInfo, null, params);
		tileHarvester.harvestTiles();
	}

}
