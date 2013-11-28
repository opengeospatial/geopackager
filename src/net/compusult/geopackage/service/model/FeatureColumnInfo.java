package net.compusult.geopackage.service.model;

public class FeatureColumnInfo {
	
	// Valid concrete subtypes of Geometry, per SpatiaLite
	public static final String[] GEOM_TYPES = {
		"POINT", "LINESTRING", "POLYGON", "MULTIPOINT", "MULTILINESTRING", "MULTIPOLYGON", "GEOMETRYCOLLECTION"
	};
	
	public enum ColumnType {
		INTEGER("integer"),
		REAL("real"),
		TEXT("text"),
		BLOB("blob"),
		GEOMETRY("geometry"),
		RASTER("raster");
		
		private final String label;
		private ColumnType(String label) {
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
		
		public static ColumnType fromLabel(String label) {
			if (INTEGER.getLabel().equalsIgnoreCase(label)) { return INTEGER; }
			else if (REAL.getLabel().equalsIgnoreCase(label)) { return REAL; }
			else if (TEXT.getLabel().equalsIgnoreCase(label)) { return TEXT; }
			else if (BLOB.getLabel().equalsIgnoreCase(label)) { return BLOB; }
			else if (GEOMETRY.getLabel().equalsIgnoreCase(label)) { return GEOMETRY; }
			else if (isGeometryType(label)) { return GEOMETRY; }
			else if (RASTER.getLabel().equalsIgnoreCase(label)) { return RASTER; }
			else {
				throw new IllegalArgumentException("Unknown value '" + label + "' for ColumnType enumeration");
			}
		}
		
		public static boolean isGeometryType(String label) {
			for (String type : GEOM_TYPES) {
				if (type.equalsIgnoreCase(label)) {
					return true;
				}
			}
			return false;
		}
		
		public static String geometryTypeName(String label) {
			if (label == null || "".equals(label) || isGeometryType(label)) {
				return label;		// no change required
			}
			
			int geomType = Integer.parseInt(label) % 10;
			if (geomType < 1 || geomType > GEOM_TYPES.length) {
				return label;	// no match - punt!
			}
			
			return GEOM_TYPES[geomType - 1];
		}
		
		public static String[] getLabels() {
			return new String[]{ INTEGER.label, REAL.label, TEXT.label, BLOB.label, GEOMETRY.label, RASTER.label };
		}
	}

	private ColumnType type;
	private String name;
	private boolean priKey;
	private boolean notNull;
	private String geometryType;
	
	public FeatureColumnInfo(ColumnType type, String name, boolean priKey, boolean notNull, String geometryType) {
		this.type = type;
		this.name = name;
		this.priKey = priKey;
		this.notNull = notNull;
		this.geometryType = ColumnType.geometryTypeName(geometryType);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ColumnType getType() {
		return type;
	}
	
	public void setType(ColumnType type) {
		this.type = type;
	}

	public boolean isPriKey() {
		return priKey;
	}

	public void setPriKey(boolean priKey) {
		this.priKey = priKey;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}

	public String getGeometryType() {
		return geometryType;
	}

	public void setGeometryType(String geometryType) {
		this.geometryType = geometryType;
	}
	
}
