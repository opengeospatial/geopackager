package net.compusult.geopackage.service.model;

import java.io.File;
import java.io.IOException;

import net.opengis.kml.x22.ExtendedDataType;
import net.opengis.kml.x22.KmlDocument;
import net.opengis.kml.x22.LineStringType;
import net.opengis.kml.x22.PlacemarkType;
import net.opengis.kml.x22.PointType;
import net.opengis.kml.x22.PolygonType;
import net.opengis.kml.x22.SchemaDataType;
import net.opengis.kml.x22.SimpleDataType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

public class KMLFile {
	
	private static final String NS_KML = "http://www.opengis.net/kml/2.2";
	private static final String NS_DECLARATIONS = "declare namespace kml='" + NS_KML + "'; ";

	private final KmlDocument kmlDoc;
	
	public KMLFile(String filename) throws IOException, XmlException {
		kmlDoc = KmlDocument.Factory.parse(new File(filename));
	}
	
	public void scan(KMLScanner callback) {
		for (XmlObject _placemark : kmlDoc.selectPath(NS_DECLARATIONS + " //kml:Placemark")) {
			PlacemarkType placemark = (PlacemarkType) _placemark;
			String name = placemark.getName();
			String desc = placemark.getDescription();
			String type = null;
			
			boolean needName = name == null || "".equals(name);
			boolean needDesc = desc == null || "".equals(desc);
			boolean needType = true;
			
			// Consult the <ExtendedData> element, if present.
			ExtendedDataType extended = placemark.getExtendedData();
			if (extended != null) {
				for (SchemaDataType schemaData : extended.getSchemaDataList()) {
					for (SimpleDataType simpleData : schemaData.getSimpleDataList()) {
						String objectName = simpleData.getName();
						if (needName && "Name".equalsIgnoreCase(objectName)) {
							name = simpleData.getStringValue();
							needName = false;
						}
						if (needDesc && "Description".equalsIgnoreCase(objectName)) {
							desc = simpleData.getStringValue();
							needDesc = false;
						}
						if (needType && "type".equalsIgnoreCase(objectName)) {
							desc = simpleData.getStringValue();
							needType = false;
						}
					}
				}
			}
			
			XmlObject[] coords = placemark.selectChildren(NS_KML, "Point");
			if (coords != null && coords.length > 0) {
				callback.onPlacemark(name, desc, type, (PointType) coords[0]);
			} else {
				coords = placemark.selectChildren(NS_KML, "LineString");
				if (coords != null && coords.length > 0) {
					callback.onPlacemark(name, desc, type, (LineStringType) coords[0]);
				} else {
					coords = placemark.selectChildren(NS_KML, "Polygon");
					if (coords != null && coords.length > 0) {
						callback.onPlacemark(name, desc, type, (PolygonType) coords[0]);
					}
				}
			}
		}
	}
	
	public interface KMLScanner {
		void onPlacemark(String name, String description, String type, PointType point);
		void onPlacemark(String name, String description, String type, LineStringType lineString);
		void onPlacemark(String name, String description, String type, PolygonType polygon);
	}
	
}
