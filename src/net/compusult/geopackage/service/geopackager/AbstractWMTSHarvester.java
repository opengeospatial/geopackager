/*
 * AbstractWMTSHarvester.java
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.owscontext.Offering;
import net.compusult.xml.DOMUtil;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class AbstractWMTSHarvester extends AbstractHarvester {

	protected AbstractWMTSHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}
	
	protected Map<String, String> parseParameters(Offering offering) throws GeoPackageException {
		Map<String, String> params = new HashMap<String, String>();
		
		DOMUtil domUtil = new DOMUtil();
		
		for (Node node : offering.getExtensions()) {
			Element extension = (Element) node;
			
			List<Element> parameters = domUtil.findChildrenNamed(extension, TESTBED_NS, EXTENSION_PARAM);
			for (Element parameter : parameters) {
				String name = domUtil.getAttributeValue(parameter, "name");
				String value = domUtil.getAttributeValue(parameter, "value");
				
				if (name.equals("TileMatrix")) {
					params.put("fromMatrix", domUtil.getAttributeValue(parameter, "from"));
					params.put("toMatrix", domUtil.getAttributeValue(parameter, "to"));
				} else {
					params.put(name, value);
				}
			}
		}

		return params;
	}

}