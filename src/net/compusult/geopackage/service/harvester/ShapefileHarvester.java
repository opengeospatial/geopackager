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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.geopackager.ProgressTracker;
import net.compusult.geopackage.service.model.FeatureColumnInfo;
import net.compusult.geopackage.service.model.FeatureColumnInfo.ColumnType;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.geopackage.service.model.LayerInformation;
import net.compusult.geopackage.service.model.LayerInformation.Type;
import net.compusult.owscontext.Content;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Resource;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Geometry;

public class ShapefileHarvester extends AbstractHarvester {
	
	private static final Logger LOG = Logger.getLogger(ShapefileHarvester.class);
	
	// how many features to insert before committing
	private static final int MAX_UNCOMMITTED_FEATURES = 250;
	
	public ShapefileHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}
	
	@Override
	public void harvest(GeoPackage gpkg, Resource resource, Offering offering) throws GeoPackageException {
		
		final String tableName = sanitizeTableName(resource.getId());
		
		final LayerInformation layerInfo = new LayerInformation(gpkg, Type.FEATURES, tableName);
		layerInfo.setTitle(resource.getTitle().getText());
		layerInfo.setCrs("4326");
		
		try {
			/*
			 * Two effects:
			 * 1. Count the number of feature types.
			 * 2. Create a coalesced data model that is the union of all of them.
			 */
			getProgressTracker().setItemCount(createFeatureTable(gpkg, offering, layerInfo));
		} catch (GeoPackageException e) {
			throw e;
		} catch (Exception e) {
			// wrap any other type of exception
			throw new GeoPackageException("Error processing shapefile", e);
		}
		
		for (Content content : offering.getContents()) {
			
			Map<String, String> params = parseParameters(content.getExtensions());
			
			/*
			 * Set defaultIsExclude true if and only if "default-geometries" is set
			 * to "exclude".  If that parameter is unset or set to something else,
			 * we default to including all geometries.
			 */
			String defaultIsExcludeStr = params.get("default-geometries");
			boolean defaultIsExclude = "exclude".equals(defaultIsExcludeStr);
			
			Map<String, Object> connect = new HashMap<String, Object>();
			connect.put("url", content.getUrl());
			
			int uncommittedFeatures = 0;
			
			try {
				DataStore dataStore = DataStoreFinder.getDataStore(connect);
				String[] typeNames = dataStore.getTypeNames();
				for (String typeName : typeNames) {
				
					SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
					SimpleFeatureCollection collection = featureSource.getFeatures();
					SimpleFeatureIterator iterator = collection.features();
					
					try {
						while (iterator.hasNext()) {
							SimpleFeature feature = iterator.next();
							
							Map<String, String> props = new HashMap<String, String>();
							Set<String> geomColumns = new HashSet<String>();
							
							/*
							 * If we're excluding all features by default, then 'skip' is true
							 * until we hit a geometry we know we're including.
							 */
							boolean skip = defaultIsExclude;
							
							LOG.trace("--- Converting Feature ID: " + feature.getID());
							props.put("featureid", feature.getID());
							
							for (Property prop : feature.getProperties()) {
								
								String key = prop.getName().getLocalPart().toLowerCase();
								String val;
								
								boolean isGeom = false;
								String geomType = null;
								
								if (prop.getValue() instanceof Geometry) {
									val = ((Geometry) prop.getValue()).toText();
									geomType = ((Geometry) prop.getValue()).getGeometryType().toLowerCase();
									isGeom = true;
								} else if (prop.getValue() instanceof org.opengis.geometry.Geometry) {
									val = ((org.opengis.geometry.Geometry) prop.getValue()).toString();
									geomType = val.toLowerCase().replaceFirst("[^a-z].*", "");
									isGeom = true;
								} else {
									val = String.valueOf(prop.getValue());
								}
								props.put(key, val);
								
								if (isGeom) {
									if (!defaultIsExclude && params.containsKey("exclude-" + geomType)) {
										// We are including everything except this specific type of geometry
										skip = true;
									} else if (defaultIsExclude && params.containsKey("include-" + geomType)) {
										// We are excluding everything except this specific type of geometry
										skip = false;
									}
									geomColumns.add(key);
								}
							}
							
							if (! skip) {
								gpkg.addVectorFeature(tableName, props, geomColumns);
								
								if (++ uncommittedFeatures == MAX_UNCOMMITTED_FEATURES) {
									gpkg.commit();
									uncommittedFeatures = 0;
								}
							}
						}
					} finally {
						iterator.close();
					}
					
					getProgressTracker().newItem();
				}
				
			} catch (GeoPackageException e) {
				throw e;
			} catch (Exception e) {
				// wrap any other type of exception
				throw new GeoPackageException("Error processing shapefile", e);
			}
			
			// Unconditionally commit at the end of each <content> element
			gpkg.commit();
			uncommittedFeatures = 0;
		}
	}
	
	/**
	 * One offering element can have multiple content elements, referencing Shapefiles with
	 * differing feature schemas.  Try to accommodate the simple cases: that all the schemas
	 * are the same, or some have an extra attribute or two.  We spin through all the content
	 * elements, gathering up a complete list of required feature columns.
	 * 
	 * @param gpkg
	 * @param offering
	 * @param layerInfo
	 * @return the number of "items" that have to be processed
	 * @throws GeoPackageException
	 */
	private int createFeatureTable(GeoPackage gpkg, Offering offering, LayerInformation layerInfo) throws Exception {

		Set<String> foundNames = new HashSet<String>();
		List<FeatureColumnInfo> featCols = new ArrayList<FeatureColumnInfo>();
		featCols.add(new FeatureColumnInfo(ColumnType.INTEGER, "id", true, true, null));	// autoincrement ID
		featCols.add(new FeatureColumnInfo(ColumnType.TEXT, "featureid", false, false, null));
		
		int items = 0;
		
		for (Content content : offering.getContents()) {
			Map<String, Object> connect = new HashMap<String, Object>();
			connect.put("url", content.getUrl());
			
			DataStore dataStore = DataStoreFinder.getDataStore(connect);
			String[] typeNames = dataStore.getTypeNames();
			for (String typeName : typeNames) {
			
				SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
				SimpleFeatureType featureType = featureSource.getSchema();
				
				/*
				 * Map from the feature type's attribute descriptors to fields in our new
				 * SQLite table.  Type matching is "best guess" but SQLite's type flexibility
				 * works to our advantage.
				 */
				for (AttributeDescriptor descript : featureType.getAttributeDescriptors()) {
					String colName = descript.getLocalName().toLowerCase();
					if (! foundNames.contains(colName)) {
						Class<?> binding = descript.getType().getBinding();
						ColumnType colType = null;
						String geomType = null;
						if (binding == Integer.class || binding == Long.class) {
							colType = ColumnType.INTEGER;
						} else if (Number.class.isAssignableFrom(binding)) {
							colType = ColumnType.REAL;
						} else if (Geometry.class.isAssignableFrom(binding) || org.opengis.geometry.Geometry.class.isAssignableFrom(binding)) {
							colType = ColumnType.GEOMETRY;
							geomType = "GEOMETRY";		// can we be more precise?
						} else if (binding == String.class) {
							colType = ColumnType.TEXT;
						}
	
						featCols.add(new FeatureColumnInfo(colType, colName, false, false, geomType));
						foundNames.add(colName);
					}
				}
			}
			
			items += typeNames.length;
		}
		
		/*
		 * Finally, create the new feature table.
		 */
		gpkg.updateFeatureTableSchema(layerInfo.getTableName(), layerInfo, featCols);
		
		return items;
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
