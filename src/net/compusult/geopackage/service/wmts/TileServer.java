package net.compusult.geopackage.service.wmts;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public abstract class TileServer {
	
	protected static final double PHYS_PIXEL_SIZE = 0.28e-3;			// from OGC 07-057r7
	
	public TileServer() {
	}

	public InputStream openTileStream(String tileUrl) throws MalformedURLException, IOException {
		return new URL(tileUrl).openStream();
	}
	
	public void closeTileStream(InputStream is) throws IOException {
		if(is != null) {
			is.close();
		}
	}
	
	protected String getQuadTreeId(int rows, int cols, int i, int j) {
		if (rows <= 1 || cols <= 1) {
			return "";
		}
		int rowSplit = rows / 2;
		int colSplit = cols / 2;
		if (i < rowSplit && j < colSplit) {
			return 0 + getQuadTreeId(rowSplit, colSplit, i, j);
		} else if (i < rowSplit) {
			return 1 + getQuadTreeId(rowSplit, colSplit, i, j - colSplit);
		} else if (j < colSplit) {
			return 2 + getQuadTreeId(rowSplit, colSplit, i - rowSplit, j);
		} else {
			return 3 + getQuadTreeId(rowSplit, colSplit, i - rowSplit, j - colSplit);
		}
	}
	
	public boolean isTileAvailable(String tileMatrix, int tileRow, int tileCol) {
		return true;
	}
	
	public abstract String getUrl(String tileMatrix, int tileRow, int tileCol);

	public abstract int getTileWidthPixels(String identifier);
	
	public abstract int getTileHeightPixels(String identifier);
	
	/**
	 * Get the approximate width in meters of a single tile at the given scale.
	 */
	public abstract double getPixelWidthInMeters(String identifier);
	
	/**
	 * Get the approximate height in meters of a single tile at the given scale.
	 */
	public abstract double getPixelHeightInMeters(String identifier);
	
	/**
	 * Returns the number of tiles across the whole matrix at the given zoom scale.
	 */
	public abstract int getMatrixWidth(String identifier);

	/**
	 * Returns the number of tiles vertically at the given zoom scale.
	 */
	public abstract int getMatrixHeight(String identifier);

	public abstract boolean hasZoomScale(String identifier);
	
	/**
	 * Get the physical X coordinate of the left edge of the entire matrix.
	 */
	public abstract double getMinX();

	/**
	 * Get the physical Y coordinate of the bottom edge of the entire matrix.
	 */
	public abstract double getMinY();

	/**
	 * Get the physical X coordinate of the right edge of the entire matrix.
	 */
	public abstract double getMaxX();

	/**
	 * Get the physical Y coordinate of the top edge of the entire matrix.
	 */
	public abstract double getMaxY();
	
	public abstract List<String> getTileMatricesBetween(String first, String last);
	public abstract int findTileMatrixIndex(String identifier);
	
	public abstract String getCRS();
	
}
