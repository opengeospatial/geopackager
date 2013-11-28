package net.compusult.geopackage.service.database;

public class DatabaseColumnInfo {

	private final String name;
	private final String type;
	private final boolean priKey;
	private final boolean notNull;
	
	public DatabaseColumnInfo(String name, String type, boolean priKey, boolean notNull) {
		super();
		this.name = name;
		this.type = type;
		this.priKey = priKey;
		this.notNull = notNull;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isPriKey() {
		return priKey;
	}

	public boolean isNotNull() {
		return notNull;
	}

}
