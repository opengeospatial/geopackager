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
   
package net.compusult.geopackage.service.harvester;

import java.util.Map;

import net.compusult.geopackage.service.geopackager.ProgressTracker;
import net.compusult.xml.DOMUtil;

import org.w3c.dom.Element;

public abstract class AbstractWMTSHarvester extends AbstractHarvester {

	private final DOMUtil domUtil = new DOMUtil();

	protected AbstractWMTSHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}
	
	@Override
	protected void saveParameter(Map<String, String> params, String name, Element parameter) {

		if ("TileMatrix".equals(name)) {
			params.put("fromMatrix", domUtil.getAttributeValue(parameter, "from"));
			params.put("toMatrix", domUtil.getAttributeValue(parameter, "to"));
		} else {
			super.saveParameter(params, name, parameter);
		}
	}

}