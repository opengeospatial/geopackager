package net.compusult.geopackage.service.model;

public class LayerInformation {
	
	public enum Type {
		FEATURES("Features", "features"),
		TILES("Tiles", "tiles");
		
		private final String label;
		private String xmlType;
		
		private Type(String label, String xmlType) {
			this.label = label;
			this.xmlType = xmlType;
		}
		
		public String getLabel() {
			return label;
		}

		public String getXmlType() {
			return xmlType;
		}
		
		public static Type fromXmlType(String xmlType) {
			if (FEATURES.xmlType.equalsIgnoreCase(xmlType)) { return FEATURES; }
			else if (TILES.xmlType.equalsIgnoreCase(xmlType)) { return TILES; }
			else { throw new IllegalArgumentException("Unknown XML layer type '" + xmlType + "'"); }
		}
	}
	
	private final Type type;
	private String title;
	private String tableName;
	private String identifier;
	private String theAbstract;
	private String publisher;
	private String coverage;
	private final Rectangle boundsRectangle;
	private String description;
	private String crs;
	
	public LayerInformation(GeoPackage gpkg, Type type, String tableName) {
		this.type = type;
		this.tableName = tableName;
		this.boundsRectangle = new Rectangle();
		this.identifier = gpkg.getIdentifier() + "." + tableName;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTheAbstract() {
		return theAbstract;
	}

	public void setTheAbstract(String theAbstract) {
		this.theAbstract = theAbstract;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getCoverage() {
		return coverage;
	}

	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}
	
	public Rectangle getBoundsRectangle() {
		return boundsRectangle;
	}

	public void setBoundsRectangle(Rectangle boundsRectangle) {
		this.boundsRectangle.setFrom(boundsRectangle);
	}

	public boolean hasDCMetadata() {
		return publisher != null && !"".equals(publisher.trim())
				|| coverage != null && !"".equals(coverage.trim());
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}
	
}
