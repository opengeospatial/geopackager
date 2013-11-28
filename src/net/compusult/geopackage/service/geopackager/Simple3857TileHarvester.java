/*
 * Simple3857TileHarvester.java
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
import net.compusult.geopackage.service.wmts.Simple3857TileServer;
import net.compusult.geopackage.service.wmts.TileServer;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Operation;
import net.compusult.owscontext.Resource;

public class Simple3857TileHarvester extends AbstractWMTSHarvester {

	public Simple3857TileHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}
	
	@Override
	public void harvest(GeoPackage gpkg, Resource resource, Offering offering) throws GeoPackageException {

		Operation getTile = findRequiredOperation(offering, "GetTile");
		
		Map<String, String> params = parseParameters(offering);
		
		TileServer bing = new Simple3857TileServer(getTile.getRequestURL(), getTile.getType(), params);
		
		LayerInformation layerInfo = new LayerInformation(gpkg, Type.TILES, sanitizeTableName(resource.getId()));
		layerInfo.setTitle(resource.getTitle().getText());
		layerInfo.setCrs("3857");
		
		HarvestTiles tileHarvester = new HarvestTiles(gpkg, bing, layerInfo, params, progressTracker);
		tileHarvester.harvestTiles();
	}
	
}
