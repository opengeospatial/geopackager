/*
 * AbstractHarvester.java
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
import net.compusult.owscontext.Operation;

public abstract class AbstractHarvester implements Harvester {

	protected static final String TESTBED_NS = "http://schemas.compusult.net/ows10/ows-context";
	protected static final String EXTENSION_PARAM = "parameter";
	
	protected ProgressTracker progressTracker;
	
	protected AbstractHarvester(ProgressTracker progressTracker) {
		this.progressTracker = progressTracker;
	}
	
	protected Operation findRequiredOperation(Offering offering, String desiredCode) throws GeoPackageException {
		for (Operation op : offering.getOperations()) {
			if (desiredCode.equalsIgnoreCase(op.getOperationCode())) {
				return op;
			}
		}
		throw new GeoPackageException("Offering lacks a " + desiredCode + " operation, cannot proceed");
	}
	
	protected String sanitizeTableName(String tableName) {
		tableName = tableName.replaceAll("[^\\w\\d]", "");
		if (tableName.length() > 32) {
			tableName = tableName.substring(0, 32);
		}
		return tableName;
	}

}
