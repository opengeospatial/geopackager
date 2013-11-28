package net.compusult.geopackage.service;

@SuppressWarnings("serial")
public class GeoPackageException extends Exception {

	public GeoPackageException() {
	}

	public GeoPackageException(String message) {
		super(message);
	}

	public GeoPackageException(Throwable cause) {
		super(cause);
	}

	public GeoPackageException(String message, Throwable cause) {
		super(message, cause);
	}

}
