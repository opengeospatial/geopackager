/*
 * HarvestTiles.java
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.geopackage.service.model.LayerInformation;
import net.compusult.geopackage.service.model.Rectangle;
import net.compusult.geopackage.service.wmts.TileServer;

import org.apache.log4j.Logger;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Envelope;

public class HarvestTiles {
	
	private static final Logger LOG = Logger.getLogger(HarvestTiles.class);
	
	private static final int COMMIT_BATCH_SIZE = 2000;
	private static final long MAX_BYTES = 1900 * 1024 * 1024;		// 1.9G gives some space for overhead

	private final Harvester harvester;
	private GeoPackage gpkg;
	private final TileServer wmts;
	private final LayerInformation layerInfo;
	private final Envelope clipRect;
	private final Map<String, String> params;
	
	private int tilesExpected;
	private int tilesSaved;

	public HarvestTiles(Harvester harvester, GeoPackage gpkg, TileServer wmts, LayerInformation layerInfo, Envelope clipRect, Map<String, String> params) throws GeoPackageException {
		this.harvester = harvester;
		this.gpkg = gpkg;
		this.wmts = wmts;
		this.layerInfo = layerInfo;
		this.clipRect = translateCRS(clipRect, layerInfo.getCrs(), harvester);
		this.params = params;
		
		// At each level Z the number of tiles is typically (2^Z)^2; i.e. 2^(2Z)
		// However there may be WMTSs where some zoom scales are not complete.
		this.tilesExpected = 0;
		for (String z : wmts.getTileMatricesBetween(params.get("fromMatrix"), params.get("toMatrix"))) {
			tilesExpected += wmts.getMatrixWidth(z) * wmts.getMatrixHeight(z);
		}
		this.tilesSaved = 0;
	}

	public void harvestTiles() throws GeoPackageException {
		double overallMinX, overallMinY, overallMaxX, overallMaxY;

		overallMinX = wmts.getMinX();
		overallMinY = wmts.getMinY();
		overallMaxX = wmts.getMaxX();
		overallMaxY = wmts.getMaxY();

		TileWriter tileWriter = new TileWriter();
		
		layerInfo.setBoundsRectangle(new Rectangle(overallMinX, overallMinY, overallMaxX, overallMaxY));
		gpkg.createTileLayer(layerInfo);
		gpkg.createTileMatrixSet(layerInfo.getTableName(), overallMinX, overallMinY, overallMaxX, overallMaxY);

		writeMatrixLayerInfo(gpkg, wmts);
		
		int layerIndex = wmts.findTileMatrixIndex(params.get("fromMatrix"));
		for (String zoomScale : wmts.getTileMatricesBetween(params.get("fromMatrix"), params.get("toMatrix"))) {

			int matrixWidth = wmts.getMatrixWidth(zoomScale);
			int matrixHeight = wmts.getMatrixHeight(zoomScale);

			for (int tileCol = 0; tileCol < matrixWidth; tileCol++) {
				for (int tileRow = 0; tileRow < matrixHeight; tileRow++) {
					if (wmts.isTileAvailable(zoomScale, tileRow, tileCol) &&
							overlapsClipRect(zoomScale, tileRow, tileCol, matrixWidth, matrixHeight)) {
						
						TileFetcher fetcher = new TileFetcher(tileWriter, zoomScale, tileRow, tileCol, layerIndex);
						fetcher.fetch();
					}
				}
			}
			++ layerIndex;
		}
		
		tileWriter.finished();
	}

	private static Envelope translateCRS(Envelope clipRect, String crs, Harvester harvester) throws GeoPackageException {
		if (clipRect == null) {
			return null;
		}

		CoordinateReferenceSystem epsg4326 = null;
		CoordinateReferenceSystem targetCRS = null;

		try {
			epsg4326 = CRS.decode("EPSG:4326");
			targetCRS = CRS.decode(crs);
			
			MathTransform transform = CRS.findMathTransform(epsg4326, targetCRS, true);
			
			DirectPosition2D from = new DirectPosition2D(clipRect.getMinY(), clipRect.getMinX());
			DirectPosition2D toLL = new DirectPosition2D();
			transform.transform(from, toLL);
			
			from = new DirectPosition2D(clipRect.getMaxY(), clipRect.getMaxX());
			DirectPosition2D toUR = new DirectPosition2D();
			transform.transform(from, toUR);
			return new Envelope(toLL.x, toUR.x, toLL.y, toUR.y);
	
		} catch (ProjectionException e) {
			/*
			 * Chances are that we failed to transform the incoming envelope because it is
			 * global (-180..180, -90..90) and one or more of those does not exist in the
			 * target CRS.  For example, with EPS:4326 to EPSG:3857 if fails because
			 * "90 degrees S is too close to a pole".
			 */
			org.opengis.geometry.Envelope total = CRS.getEnvelope(targetCRS);
			return new Envelope(total.getMinimum(1), total.getMaximum(1), total.getMinimum(0), total.getMaximum(0));
			
		} catch (Exception e) {
			throw new GeoPackageException("Transforming clipping envelope to " + crs, e);
		}
	}
	
	private static final double EPSILON = 1e-18;
	
	private boolean overlapsClipRect(String zoomScale, int tileRow, int tileCol, int matrixWidth, int matrixHeight) {
		if (clipRect == null) {
			return true;
		}
		double minx = wmts.getMinX();
		double miny = wmts.getMinY();
		double maxx = wmts.getMaxX();
		double maxy = wmts.getMaxY();
		double tileWidth  = (maxx - minx) / matrixWidth;
		double tileHeight = (maxy - miny) / matrixHeight;
		double useminx = minx + tileCol * tileWidth;
		double useminy = maxy - (tileRow + 1) * tileHeight;
		double usemaxx = useminx + tileWidth;
		double usemaxy = useminy + tileHeight;
		
		Envelope against = new Envelope(useminx, usemaxx, useminy, usemaxy);
		Envelope intersection = clipRect.intersection(against);
		return !intersection.isNull() &&
				intersection.getWidth() * intersection.getHeight() > EPSILON;
	}

	private void writeMatrixLayerInfo(GeoPackage geopackage, TileServer wmts) throws GeoPackageException {
		int layerIndex = wmts.findTileMatrixIndex(params.get("fromMatrix"));
		for (String zoomScale : wmts.getTileMatricesBetween(params.get("fromMatrix"), params.get("toMatrix"))) {

			int matrixWidth = wmts.getMatrixWidth(zoomScale);
			int matrixHeight = wmts.getMatrixHeight(zoomScale);

			int tileWidth = wmts.getTileWidthPixels(zoomScale);
			int tileHeight = wmts.getTileHeightPixels(zoomScale);

			double pixelWidth = wmts.getPixelWidthInMeters(zoomScale);
			double pixelHeight = wmts.getPixelHeightInMeters(zoomScale);

			geopackage.createTileMatrixZoomLevel(layerInfo.getTableName(), layerIndex, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelWidth, pixelHeight);
			++ layerIndex;
		}
	}
	
	private class TileFetcher {

		private final TileWriter tileWriter;
		private final String zoomScale;
		private final int tileRow, tileCol;
		private final int layerIndex;
		private byte[] tileData;
		private long startTime;
		
		public TileFetcher(TileWriter tileWriter, String zoomScale, int tileRow, int tileCol, int layerIndex) {
			this.tileWriter = tileWriter;
			this.zoomScale = zoomScale;
			this.tileRow = tileRow;
			this.tileCol = tileCol;
			this.layerIndex = layerIndex;
			this.tileData = null;
		}

		public void fetch() throws GeoPackageException {
			String tileUrl = wmts.getUrl(zoomScale, tileRow, tileCol);

			this.startTime = System.currentTimeMillis();
			System.out.println("\nFetching: " + tileUrl);

			// Download tile to a byte array.
			ByteArrayOutputStream baos = new ByteArrayOutputStream(256 * 256);
			byte[] byteChunk = new byte[8192]; // Or whatever size you want to read in at a time.
			
			int tries = 0;
			while (++tries <= 3) {
				InputStream is = null;
				try {
					is = wmts.openTileStream(tileUrl);
					if(is != null) {
						int n;

						while ( (n = is.read(byteChunk)) > 0 ) {
							baos.write(byteChunk, 0, n);
						}
					}
					
					break;		// success; break out of the retry loop
				} 
				catch (IOException e) {
					LOG.warn("Failed to download tile, will attempt retry", e);
					baos.reset();
				}
				finally {
					try {
						wmts.closeTileStream(is);
					} catch (IOException e) {
						LOG.warn("Failed to close tile stream", e);
					}
				}
			}

			this.tileData = baos.toByteArray();
			
			if(tileData.length > 0) {
				tileWriter.write(this);
			} else {
				-- tilesExpected;		// exclude missing tiles from our progress bar
			}
		}
		
		public int getLayerIndex() {
			return layerIndex;
		}

		public int getTileRow() {
			return tileRow;
		}

		public int getTileCol() {
			return tileCol;
		}

		public long getStartTime() {
			return startTime;
		}
		
		public byte[] getTileData() {
			return tileData;
		}

	}
	
	private class TileWriter {

		private int uncommittedTiles;
		private long bytesRetrieved;
		private int fileIndex;
		private long overallStartTime;
		
		public TileWriter() {
			this.uncommittedTiles = 0;
			this.bytesRetrieved = 0;
			this.fileIndex = 1;
			this.overallStartTime = System.currentTimeMillis();
		}
		
		public void write(TileFetcher fetcher) throws GeoPackageException {
			GeoPackage origGPKG = gpkg;

			byte[] tileData = fetcher.getTileData();
			
			bytesRetrieved += tileData.length;
			
			gpkg.createTile(layerInfo.getTableName(), fetcher.getLayerIndex(), fetcher.getTileCol(), fetcher.getTileRow(), tileData);									
			
			++ tilesSaved;
			harvester.getProgressTracker().setProgress((int) (tilesSaved * 100.0 / tilesExpected + 0.5));
			
			uncommittedTiles++;									
			if(uncommittedTiles >= COMMIT_BATCH_SIZE) {
				uncommittedTiles = 0;
				System.out.println("\tCOMMIT");
				gpkg.commit();
			}
			
			System.out.println("\tSAVED(" + tileData.length + " bytes) (" + (System.currentTimeMillis() - fetcher.getStartTime()) + "ms)");
			
			//Split
			if(bytesRetrieved > MAX_BYTES) {
				gpkg.commit();
				gpkg.newLayer(layerInfo);
				
				File currentFile = origGPKG.getFile();
				File newFile = new File(currentFile.getParentFile(), currentFile.getName().replace(".", "_" + (++fileIndex) +"."));
				LOG.debug("Switching to " + newFile);
				
				GeoPackage newGPKG = new GeoPackage(newFile, origGPKG.getPassword());
				writeMatrixLayerInfo(newGPKG, wmts);
				newGPKG.createTileLayer(layerInfo);
				gpkg = newGPKG;
				bytesRetrieved = 0;
				uncommittedTiles = 0;
			}
		}
		
		public void finished() throws GeoPackageException {
			System.out.println("\tCOMMIT");
			gpkg.commit();
			gpkg.newLayer(layerInfo);
			
			long totalMillis  = System.currentTimeMillis() - overallStartTime;
			long totalSeconds = totalMillis  / 1000;	totalMillis  %= 1000;
			long totalMinutes = totalSeconds / 60;		totalSeconds %= 60;
			long totalHours   = totalMinutes / 60;		totalMinutes %= 60;
			String formatted = String.format("%03d:%02d:%02d.%03d", totalHours, totalMinutes, totalSeconds, totalMillis);
			LOG.info("OVERALL RUN TIME " + formatted);
			
			System.out.println("\tDONE");
		}
		
	}

}
