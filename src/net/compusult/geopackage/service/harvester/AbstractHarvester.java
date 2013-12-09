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
   
package net.compusult.geopackage.service.harvester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.geopackager.ProgressTracker;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Operation;
import net.compusult.owscontext.Resource;
import net.compusult.xml.DOMUtil;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractHarvester implements Harvester {

	protected static final String TESTBED_NS = "http://schemas.compusult.net/ows10/ows-context";
	protected static final String EXTENSION_PARAM = "parameter";
	
	private final DOMUtil domUtil;
	private final ProgressTracker progressTracker;
	
	protected AbstractHarvester(ProgressTracker progressTracker) {
		this.domUtil = new DOMUtil();
		this.progressTracker = progressTracker;
	}

	@Override
	public ProgressTracker getProgressTracker() {
		return progressTracker;
	}

	/**
	 * Determine the Envelope of the AOI expressed or implied by the given Resource.
	 * 
	 * @param resource
	 * @return
	 */
	protected Envelope selectEnvelope(Resource resource) {
		Object result = resource.getGeospatialExtent();
		if (result == null) {
			result = (Geometry) resource.getContext().getAreaOfInterest();
		}
		if (result == null) {
			// world extents
			return new Envelope(-180, 180, -90, 90);
		} else {
			return ((Geometry) result).getEnvelopeInternal();
		}
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
	
	protected Map<String, String> parseParameters(Offering offering) throws GeoPackageException {
		Map<String, String> params = new HashMap<String, String>();
		
		for (Node node : offering.getExtensions()) {
			Element extension = (Element) node;
			
			List<Element> parameters = domUtil.findChildrenNamed(extension, TESTBED_NS, EXTENSION_PARAM);
			for (Element parameter : parameters) {
				String name = domUtil.getAttributeValue(parameter, "name");
				saveParameter(params, name, parameter);
			}
		}

		return params;
	}
	
	// Override in subclasses to handle specially formatted parameters
	protected void saveParameter(Map<String, String> params, String name, Element parameter) {
		String value = domUtil.getAttributeValue(parameter, "value");
		params.put(name,  value);
	}
	
}
