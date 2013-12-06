/*
 * JTSGMLConverter.java
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
   
package net.compusult.geometry.gml;

import java.util.ArrayList;
import java.util.List;

import net.compusult.xml.DOMUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class JTSGMLConverter implements GMLConverterInterface {

	private static final String GML_NS = "http://www.opengis.net/gml";
	private static final int SRID_WGS_84 = 4326;
	
	private final DOMUtil domUtil = new DOMUtil();
	
	private enum OrdinateOrder {
		X_Y, Y_X
	}

	@Override
	public Element gmlFromGeometry(Object _geom, Document doc) {

		if (_geom instanceof Envelope) {
			Envelope env = (Envelope) _geom;
			Element envelope = doc.createElementNS(GML_NS, "Envelope");
			
			Element lowerCorner = doc.createElementNS(GML_NS, "lowerCorner");
			lowerCorner.setTextContent(env.getMinX() + " " + env.getMinY());
			Element upperCorner = doc.createElementNS(GML_NS, "upperCorner");
			upperCorner.setTextContent(env.getMaxX() + " " + env.getMaxY());
			
			envelope.appendChild(lowerCorner);
			envelope.appendChild(upperCorner);
			return envelope;
			
		} else if (_geom instanceof Polygon) {
			Polygon poly = (Polygon) _geom;
			int srid = poly.getSRID();
			
			Element polygon = doc.createElementNS(GML_NS, "Polygon");
			if (srid != 0 && srid != SRID_WGS_84) {			// valid, non-default SRS
				polygon.setAttribute("srsName", "urn:ogc:def:crs:EPSG::" + srid);
			}
			
			Element exterior = doc.createElementNS(GML_NS, "exterior");
			Element linearRing = doc.createElementNS(GML_NS, "LinearRing");
			LineString lr = poly.getExteriorRing();
			
			Element posList = doc.createElementNS(GML_NS, "posList");
			StringBuilder buf = new StringBuilder();
			for (int i = 0, n = lr.getNumPoints(); i < n; ++ i) {
				Point pt = lr.getPointN(i);
				if (buf.length() > 0) {
					buf.append(' ');
				}
				if (determineOrdinateOrder(srid) == OrdinateOrder.X_Y) {
					buf.append(pt.getX()).append(' ').append(pt.getY());
				} else {
					buf.append(pt.getY()).append(' ').append(pt.getX());
				}
			}
			posList.setTextContent(buf.toString());
			linearRing.appendChild(posList);
			exterior.appendChild(linearRing);
			polygon.appendChild(exterior);
			
			return polygon;
			
		}
		
		return null;
	}

	@Override
	public Object geometryFromGML(Element gml) {
		Object geom = null;
		
		if ("Envelope".equals(gml.getLocalName())) {
			GeometryFactory gf = new GeometryFactory(new PrecisionModel(), SRID_WGS_84);
			OrdinateOrder ordinateOrder = determineOrdinateOrder(SRID_WGS_84);

			Element ll = domUtil.findFirstChildNamed(gml, GML_NS, "lowerCorner");
			Element ur = domUtil.findFirstChildNamed(gml, GML_NS, "upperCorner");
			if (ll == null || ur == null) {
				return null;
			}
			
			List<Point> llList = parsePoints(gf, ordinateOrder, 2, domUtil.nodeTextContent(ll));
			List<Point> urList = parsePoints(gf, ordinateOrder, 2, domUtil.nodeTextContent(ur));
			if (llList.size() != 1 || urList.size() != 1) {
				// malformed lowerCorner or upperCorner
				return null;
			}
			
			Coordinate llPoint = llList.get(0).getCoordinate();
			Coordinate urPoint = urList.get(0).getCoordinate();
			geom = new Envelope(llPoint, urPoint);
		}
		
		else if ("Polygon".equals(gml.getLocalName())) {
			int srid = determineSRID(gml);
			GeometryFactory gf = new GeometryFactory(new PrecisionModel(), srid);
			OrdinateOrder ordinateOrder = determineOrdinateOrder(srid);

			Element exterior = domUtil.findFirstChildNamed(gml, GML_NS, "exterior");
			if (exterior != null) {
				Element linearRing = domUtil.findFirstChildNamed(exterior, GML_NS, "LinearRing");
				if (linearRing != null) {
					Element posList = domUtil.findFirstChildNamed(linearRing, GML_NS, "posList");
					if (posList != null) {
						List<Point> points = parsePoints(gf, ordinateOrder, 2, domUtil.nodeTextContent(posList));
						if (points.size() >= 3) {
							Coordinate[] coords = new Coordinate[points.size()];
							for (int i = 0, n = points.size(); i < n; ++ i) {
								coords[i] = points.get(i).getCoordinate();
							}
							LinearRing lr = gf.createLinearRing(coords);
							geom = gf.createPolygon(lr, new LinearRing[0]);
						} else {
							// error: too few points for the ring to represent a non-empty space
						}
					}
				}
			}
			// TODO: deal with interior holes
			
			if (geom == null) {
				// Error: bad Polygon
			}
		}
		
		return geom;
	}

	protected enum ParsingState { WANT_P1, WANT_P2, WANT_P3 }
	
	protected List<Point> parsePoints(GeometryFactory gf, OrdinateOrder ordinateOrder, int dimensions, String in) {
		String[] split = in.split("\\s+");
		
		List<Point> points = new ArrayList<Point>(in.length() / 15);	// estimate
		
		ParsingState state = ParsingState.WANT_P1;
		double p1 = 0.0, p2 = 0.0;
		
		for (int i = 0, n = split.length; i < n; ++ i) {
			switch (state) {
				case WANT_P1:
					p1 = Double.parseDouble(split[i]);
					state = ParsingState.WANT_P2;
					break;
				
				case WANT_P2:
					p2 = Double.parseDouble(split[i]);
					if (dimensions > 2) {
						state = ParsingState.WANT_P3;
					} else {
						if (ordinateOrder == OrdinateOrder.Y_X) {
							points.add(gf.createPoint(new Coordinate(p2, p1)));
						} else {
							points.add(gf.createPoint(new Coordinate(p1, p2)));
						}
						state = ParsingState.WANT_P1;
					}
					break;
					
				case WANT_P3:
					// TODO do something with the 3rd dimension
					state = ParsingState.WANT_P1;
					break;
			}
		}
		
		return points;
	}
	
	private int determineSRID(Element gml) {
		String srsName = domUtil.getAttributeValue(gml, "srsName");
		if (srsName == null) {
			return SRID_WGS_84;			// default to WGS:84
		}
		
		String[] pieces = srsName.split(":");
		try {
			if (pieces.length > 0) {
				return Integer.parseInt(pieces[pieces.length - 1]);
			} else {
				return Integer.parseInt(srsName);
			}
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	private boolean isGeographicSRID(int srid) {
		return srid == SRID_WGS_84;		// TODO others?
	}
	
	private OrdinateOrder determineOrdinateOrder(int srid) {
		return isGeographicSRID(srid) ? OrdinateOrder.Y_X : OrdinateOrder.X_Y;
	}

}
