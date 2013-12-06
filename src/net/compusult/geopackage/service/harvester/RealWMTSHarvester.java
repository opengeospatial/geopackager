/*
 * RealWMTSHarvester.java
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
   
package net.compusult.geopackage.service.harvester;

import java.util.Map;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.geopackager.ProgressTracker;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.geopackage.service.model.LayerInformation;
import net.compusult.geopackage.service.model.LayerInformation.Type;
import net.compusult.geopackage.service.wmts.RealWMTS;
import net.compusult.geopackage.service.wmts.TileServer;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Operation;
import net.compusult.owscontext.Resource;

public class RealWMTSHarvester extends AbstractWMTSHarvester {

	public RealWMTSHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}
	
	@Override
	public void harvest(GeoPackage gpkg, Resource resource, Offering offering) throws GeoPackageException {

		Operation getCaps = findRequiredOperation(offering, "GetCapabilities");
		
		Map<String, String> params = parseParameters(offering);
		
		TileServer wmts = new RealWMTS(getCaps.getRequestURL(), params);
		
		LayerInformation layerInfo = new LayerInformation(gpkg, Type.TILES, sanitizeTableName(resource.getId()));
		layerInfo.setTitle(resource.getTitle().getText());
		layerInfo.setCrs(wmts.getCRS());
		
		HarvestTiles tileHarvester = new HarvestTiles(this, gpkg, wmts, layerInfo, selectEnvelope(resource), params);
		tileHarvester.harvestTiles();
	}
	
}