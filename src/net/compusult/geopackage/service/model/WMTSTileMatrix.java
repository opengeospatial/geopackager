package net.compusult.geopackage.service.model;

public class WMTSTileMatrix {

	private final String identifier;
	private final double scaleDenom;
	private final double ulx, uly;
	private final int tileWidth, tileHeight;
	private final int matrixWidth, matrixHeight;

	public WMTSTileMatrix(String identifier, double scaleDenom, double ulx,
			double uly, int tileWidth, int tileHeight, int matrixWidth,
			int matrixHeight) {
		this.identifier = identifier;
		this.scaleDenom = scaleDenom;
		this.ulx = ulx;
		this.uly = uly;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.matrixWidth = matrixWidth;
		this.matrixHeight = matrixHeight;
	}

	public String getIdentifier() {
		return identifier;
	}

	public double getScaleDenom() {
		return scaleDenom;
	}

	public double getUlx() {
		return ulx;
	}

	public double getUly() {
		return uly;
	}

	public int getTileWidth() {
		return tileWidth;
	}

	public int getTileHeight() {
		return tileHeight;
	}

	public int getMatrixWidth() {
		return matrixWidth;
	}

	public int getMatrixHeight() {
		return matrixHeight;
	}
	
}
