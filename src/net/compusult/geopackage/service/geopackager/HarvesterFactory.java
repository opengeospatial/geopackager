/*
 * HarvesterFactory.java
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

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.owscontext.KMLOffering;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Simple3857TilesOffering;
import net.compusult.owscontext.WFSOffering;
import net.compusult.owscontext.WMTSOffering;

public class HarvesterFactory {
	
	private static final HarvesterFactory INSTANCE = new HarvesterFactory();
	
	public static HarvesterFactory getInstance() {
		return INSTANCE;
	}
	
	public Harvester createHarvester(Offering offering, ProgressTracker progressTracker) throws GeoPackageException {
		
		if (offering instanceof WMTSOffering) {
			return new RealWMTSHarvester(progressTracker);
			
		} else if (offering instanceof Simple3857TilesOffering) {
			return new Simple3857TileHarvester(progressTracker);

		} else if (offering instanceof KMLOffering) {
			return new KMLHarvester(progressTracker);

//		} else if (offering instanceof WFSOffering) {
//			return new WFSHarvester(progressTracker);
		}
			
		throw new GeoPackageException("Unsupported Offering class " + offering.getClass().getName());
	}

}
