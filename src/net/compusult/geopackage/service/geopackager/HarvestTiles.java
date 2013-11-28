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
   
package net.compusult.geopackage.service.geopackager;

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

public class HarvestTiles {
	
	private static final Logger LOG = Logger.getLogger(HarvestTiles.class);
	
	private static final int COMMIT_BATCH_SIZE = 2000;
	private static final long MAX_BYTES = 1900 * 1024 * 1024;		// 1.9G gives some space for overhead

	private GeoPackage gpkg;
	private final TileServer wmts;
	private final LayerInformation layerInfo;
	private final Map<String, String> params;
	private final ProgressTracker progressTracker;
	
	private int tilesExpected;
	private int tilesSaved;

	public HarvestTiles(GeoPackage gpkg, TileServer wmts, LayerInformation layerInfo, Map<String, String> params, ProgressTracker progressTracker) {
		this.gpkg = gpkg;
		this.wmts = wmts;
		this.layerInfo = layerInfo;
		this.params = params;
		this.progressTracker = progressTracker;
		
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
					if (wmts.isTileAvailable(zoomScale, tileRow, tileCol)) {
						TileFetcher fetcher = new TileFetcher(tileWriter, zoomScale, tileRow, tileCol, layerIndex);
						fetcher.fetch();
					}
				}
			}
			++ layerIndex;
		}
		
		tileWriter.finished();
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
			progressTracker.setProgress((int) (tilesSaved * 100.0 / tilesExpected + 0.5));
			
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
