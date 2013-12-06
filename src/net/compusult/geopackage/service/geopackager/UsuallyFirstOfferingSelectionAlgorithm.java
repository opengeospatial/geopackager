/*
 * UsuallyFirstOfferingSelectionAlgorithm.java
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
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Resource;
import net.compusult.owscontext.WFSOffering;

/**
 * Prefer a simple tile or WMTS offering whenever we have one.
 * A WFS offering is never a first choice.
 */
public class UsuallyFirstOfferingSelectionAlgorithm implements OfferingSelectionAlgorithm {

	@Override
	public Offering selectOffering(Resource resource) throws GeoPackageException {
		
		Offering chosenOffering = null;
		for (Offering offering : resource.getOfferings()) {
			chosenOffering = offering;
			if (! (offering instanceof WFSOffering)) {
				// i.e., if it's WFS, keep looking for another one
				break;
			}
		}

		return chosenOffering;
	}

}
