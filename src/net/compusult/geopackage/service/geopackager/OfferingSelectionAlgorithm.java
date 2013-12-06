/*
 * OfferingSelectionAlgorithm.java
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

public interface OfferingSelectionAlgorithm {

	/**
	 * Choose one Offering from among potentially several provided for the
	 * given Resource.
	 * 
	 * @param resource - the Resource whose offerings are being examined
	 * @return null if no offerings are suitable
	 * @throws GeoPackageException
	 */
	Offering selectOffering(Resource resource) throws GeoPackageException;
}