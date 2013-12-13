/*
 * AbstractFeatureHarvester.java
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
import net.compusult.lang.Mutable;

import org.apache.log4j.Logger;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractFeatureHarvester extends AbstractHarvester {
	
	private static final Logger LOG = Logger.getLogger(AbstractFeatureHarvester.class);

	// how many features to insert before committing
	protected static final int MAX_UNCOMMITTED_FEATURES = 250;

	protected AbstractFeatureHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}

	@Override
	protected Map<String, String> parseParameters(List<Node> extensionElements)
			throws GeoPackageException {
		Map<String, String> result = super.parseParameters(extensionElements);
		Map<String, String> newEntries = new HashMap<String, String>();

		/*
		 * Expand a parameter of the form name="{in,ex}clude-geometr{y,ies}"
		 * value="linestring multilinestring" into a series of discrete
		 * parameters name="include-linestring" value="true" and so on.
		 */
		for (Entry<String, String> entry : result.entrySet()) {
			if (entry.getKey().startsWith("include-geometr")
					|| entry.getKey().startsWith("exclude-geometr")) {
				String pfx = entry.getKey().substring(0, 2);
				for (String piece : entry.getValue().split("\\s+")) {
					newEntries.put(pfx + "clude-" + piece.toLowerCase(), "true");
				}
			}
		}

		result.putAll(newEntries);
		return result;
	}

	protected Geometry transformJTSGeometry(Geometry geom, final CoordinateReferenceSystem sourceCRS, final CoordinateReferenceSystem crs) throws Exception {
		final MathTransform transform = CRS.findMathTransform(sourceCRS, crs, true);
		final Mutable<Boolean> anyChanged = new Mutable<Boolean>(false);
		
		geom = (Geometry) geom.clone();
		geom.apply(new CoordinateFilter() {
			@Override
			public void filter(Coordinate c) {
				DirectPosition dpFrom = new DirectPosition2D(sourceCRS, c.x, c.y);
				DirectPosition dpTo = new DirectPosition2D();
				try {
					transform.transform(dpFrom, dpTo);
					c.x = dpTo.getOrdinate(0);
					c.y = dpTo.getOrdinate(1);
					anyChanged.set(true);
				} catch (TransformException e) {
					LOG.warn("Failed to transform point " + c, e);
				}
			}
		});
		if (anyChanged.get()) {
			geom.geometryChanged();
		}
		return geom;
	}

}
