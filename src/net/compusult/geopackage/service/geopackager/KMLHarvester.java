/*
 * Simple3857TileHarvester.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.model.FeatureColumnInfo;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.geopackage.service.model.LayerInformation;
import net.compusult.geopackage.service.model.FeatureColumnInfo.ColumnType;
import net.compusult.geopackage.service.model.LayerInformation.Type;
import net.compusult.owscontext.Content;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Resource;
import net.compusult.xml.DOMUtil;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Coordinate;

public class KMLHarvester extends AbstractHarvester {
	
	private static final Logger LOG = Logger.getLogger(KMLHarvester.class);
	
	private static final String KML_NS = "http://www.opengis.net/kml/2.2";
	
	private final DOMUtil domUtil = new DOMUtil();

	public KMLHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}
	
	private interface PlacemarkCallback {
		void onPlacemark(Element placemark);
	}

	@Override
	public void harvest(final GeoPackage gpkg, Resource resource, Offering offering) throws GeoPackageException {
		
		final String tableName = sanitizeTableName(resource.getId());
		
		final LayerInformation layerInfo = new LayerInformation(gpkg, Type.FEATURES, tableName);
		layerInfo.setTitle(resource.getTitle().getText());
		layerInfo.setCrs("4326");
		
		//ugh
		FeatureColumnInfo[] fci = {
				new FeatureColumnInfo(ColumnType.INTEGER, "id", true, true, null),
				new FeatureColumnInfo(ColumnType.INTEGER, "featureid", false, false, null),
				new FeatureColumnInfo(ColumnType.INTEGER, "name", false, false, null),
				new FeatureColumnInfo(ColumnType.INTEGER, "desc", false, false, null),
				new FeatureColumnInfo(ColumnType.INTEGER, "type", false, false, null),
				new FeatureColumnInfo(ColumnType.INTEGER, "geom", false, false, "GEOMETRY"),
		};
		gpkg.updateFeatureTableSchema(tableName, layerInfo, Arrays.asList(fci));
		
		for (Content content : offering.getContents()) {
			
			Element kml = null;
			if (content.getUrl() != null) {
				Document kmlDoc;
				try {
					kmlDoc = readExternalKML(content.getUrl());
					kml = kmlDoc.getDocumentElement();
				} catch (GeoPackageException e) {
					LOG.error(e);
					// fall through, leaving kml == null
				}
				
			} else if (content.getActualContent() instanceof Document) {
				kml = ((Document) content.getActualContent()).getDocumentElement();

			} else if (content.getActualContent() instanceof Element) {
				kml = (Element) content.getActualContent();
			}

			if (kml != null) {
				findPlacemarks(kml, new PlacemarkCallback() {
					@Override
					public void onPlacemark(Element placemark) {
						
						String name = "";
						String desc = "";
						String type = "";
						
						boolean needName = true;
						boolean needDesc = true;
						boolean needType = true;
						
						String id = domUtil.getAttributeValue(placemark, "id");
						
						Element nameElement = domUtil.findFirstChildNamed(placemark, KML_NS, "name");
						if (nameElement != null) {
							needName = false;
							name = domUtil.nodeTextContent(nameElement);
						}
						
						Element descElement = domUtil.findFirstChildNamed(placemark, KML_NS, "description");
						if (descElement != null) {
							needDesc = false;
							desc = domUtil.nodeTextContent(descElement);
						}
						
						// Consult the <ExtendedData> element, if present.
						Element extended = domUtil.findFirstChildNamed(placemark, KML_NS, "ExtendedData");
						if (extended != null) {
							for (Element schemaData : domUtil.findChildrenNamed(extended, KML_NS, "SchemaDataList")) {
								for (Element simpleData : domUtil.findChildElements(schemaData)) {
									String objectName = simpleData.getLocalName();
									if (needName && "Name".equalsIgnoreCase(objectName)) {
										name = domUtil.nodeTextContent(simpleData);
										needName = false;
									}
									if (needDesc && "Description".equalsIgnoreCase(objectName)) {
										desc = domUtil.nodeTextContent(simpleData);
										needDesc = false;
									}
									if (needType && "type".equalsIgnoreCase(objectName)) {
										type = domUtil.nodeTextContent(simpleData);
										needType = false;
									}
								}
							}
						}
						
						String geomString = null;
						
						Element point = domUtil.findFirstChildNamed(placemark, KML_NS, "Point");
						if (point != null) {
							Element coordList = domUtil.findFirstChildNamed(point, KML_NS, "coordinates");
							if (coordList != null) {
								String coords = domUtil.nodeTextContent(coordList);
								List<Coordinate> points = parseCoordinates(coords);
								
								geomString = points.isEmpty() ? null : "POINT" + coordListToWKTList(points.subList(0, 1));
							}
							
						} else {
							Element lineString = domUtil.findFirstChildNamed(placemark, KML_NS, "LineString");
							if (lineString != null) {
								Element coordList = domUtil.findFirstChildNamed(point, KML_NS, "coordinates");
								if (coordList != null) {
									String coords = domUtil.nodeTextContent(coordList);
									List<Coordinate> points = parseCoordinates(coords);
									
									geomString = points.isEmpty() ? null : "LINESTRING" + coordListToWKTList(points);
								}
								
							} else {
								Element polygon = domUtil.findFirstChildNamed(placemark, KML_NS, "Polygon");
								if (polygon != null) {
									List<String> rings = new ArrayList<String>();
									Element outerBound = domUtil.findFirstChildNamed(polygon, KML_NS, "outerBoundaryIs");
									Element shell = domUtil.findFirstChildNamed(outerBound, KML_NS, "LinearRing");
									Element coordList = domUtil.findFirstChildNamed(shell, KML_NS, "coordinates");
									String coords = domUtil.nodeTextContent(coordList);
									List<Coordinate> outerCoords = parseCoordinates(coords);
									rings.add(coordListToWKTList(outerCoords));
									
									for (Element innerRing : domUtil.findChildrenNamed(polygon, KML_NS, "innerBoundaryIs")) {
										shell = domUtil.findFirstChildNamed(innerRing, KML_NS, "LinearRing");
										coordList = domUtil.findFirstChildNamed(shell, KML_NS, "coordinates");
										coords = domUtil.nodeTextContent(coordList);
										List<Coordinate> innerCoords = parseCoordinates(coords);
										rings.add(coordListToWKTList(innerCoords));
									}
									
									geomString = "POLYGON(" + StringUtils.collectionToDelimitedString(rings, ", ") + ")";
								}
							}
						}

						if (geomString != null) {
							Map<String,String> fields = new HashMap<String, String>();
							fields.put("featureid", id);
							fields.put("name", name);
							fields.put("desc", desc);
							fields.put("type", type);
							fields.put("geom", geomString);
							
							try {
								gpkg.addVectorFeature(tableName, fields, Collections.singleton("geom"));
								gpkg.commit();
							} catch (GeoPackageException e) {
								LOG.warn("Failed to add feature with ID '" + id + "'", e);
							}
						}
					}
				});
			}
		}
	}
	
	/**
	 * Find all the Placemark elements in the kml document starting at <code>startingPoint</code>,
	 * and invoke the given <code>callback</code> for each.
	 * 
	 * @param gpkg
	 * @param layerInfo
	 * @param params
	 * @param startingPoint
	 * @param callback
	 */
	private void findPlacemarks(Element startingPoint, PlacemarkCallback callback) {
		if ("Placemark".equals(startingPoint.getLocalName())) {
			callback.onPlacemark(startingPoint);
			
		} else if ("kml".equals(startingPoint.getLocalName())
				|| "Document".equals(startingPoint.getLocalName())
				|| "Folder".equals(startingPoint.getLocalName())) {
			for (Element child : domUtil.findChildElements(startingPoint)) {
				findPlacemarks(child, callback);
			}
		}
	}
	
	private Document readExternalKML(String url) throws GeoPackageException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(url);
		} catch (Exception e) {
			throw new GeoPackageException("Parsing remote KML file at " + url, e);
		}
	}
	
	/*
	 * Convert a sequence of coordinates into a WKT-formatted list:
	 * (x1 y1, x2 y2, ..., xn yn)
	 */
	private String coordListToWKTList(List<Coordinate> coords) {
		StringBuilder buf = new StringBuilder("(");
		boolean first = true;
		for (Coordinate coord : coords) {
			if (!first) {
				buf.append(",");
			}
			first = false;
			buf.append(coord.x).append(" ").append(coord.y);
		}
		buf.append(")");

		return buf.toString();
	}
	
	private enum ParsingState { IN_WS, IN_X, IN_Y, SKIP };

	private List<Coordinate> parseCoordinates(String in) {
		List<Coordinate> coords = new ArrayList<Coordinate>(in.length() / 15);	// estimate
		
		ParsingState state = ParsingState.IN_WS;
		StringBuilder doubleBuf = new StringBuilder(20);
		float x = 0.0f, y = 0.0f;
		
		for (int i = 0, n = in.length(); i < n; ++ i) {
			char c = in.charAt(i);
			switch (state) {
			case IN_WS:
				if (Character.isWhitespace(c)) {
					continue;
				} else {
					state = ParsingState.IN_X;
					doubleBuf.setLength(0);
					doubleBuf.append(c);
				}
				break;
			
			case IN_X:
				if (c == ',') {
					x = Float.parseFloat(doubleBuf.toString());
					doubleBuf.setLength(0);
					state = ParsingState.IN_Y;
				} else {
					doubleBuf.append(c);
				}
				break;
			
			case IN_Y:
				if (c == ',' || Character.isWhitespace(c)) {
					y = Float.parseFloat(doubleBuf.toString());
					coords.add(new Coordinate(x, y));
					doubleBuf.setLength(0);
					state = (c == ',') ? ParsingState.SKIP : ParsingState.IN_WS;
				} else {
					doubleBuf.append(c);
				}
				break;
			
			case SKIP:
				if (Character.isWhitespace(c)) {
					state = ParsingState.IN_WS;
				}
				break;
			}
		}
		if (doubleBuf.length() > 0) {
			// can only be ended by a Y value
			y = Float.parseFloat(doubleBuf.toString());
			coords.add(new Coordinate(x, y));
		}
		
		return coords;
	}

}
