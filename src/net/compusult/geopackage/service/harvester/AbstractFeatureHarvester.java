/*
 * ShapefileHarvester.java
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.geopackager.ProgressTracker;

import org.w3c.dom.Node;

public abstract class AbstractFeatureHarvester extends AbstractHarvester {

	// how many features to insert before committing
	protected static final int MAX_UNCOMMITTED_FEATURES = 250;
	
	protected AbstractFeatureHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}

	@Override
	protected Map<String, String> parseParameters(List<Node> extensionElements) throws GeoPackageException {
		Map<String, String> result = super.parseParameters(extensionElements);
		Map<String, String> newEntries = new HashMap<String, String>();
		
		/*
		 * Expand a parameter of the form name="{in,ex}clude-geometr{y,ies}" value="linestring multilinestring"
		 * into a series of discrete parameters name="include-linestring" value="true" and so on.
		 */
		for (Entry<String, String> entry : result.entrySet()) {
			if (entry.getKey().startsWith("include-geometr") || entry.getKey().startsWith("exclude-geometr")) {
				String pfx = entry.getKey().substring(0, 2);
				for (String piece : entry.getValue().split("\\s+")) {
					newEntries.put(pfx + "clude-" + piece.toLowerCase(), "true");
				}
			}
		}
		
		result.putAll(newEntries);
		return result;
	}
	
}
