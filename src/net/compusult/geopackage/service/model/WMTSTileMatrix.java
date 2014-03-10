/*
 * WMTSTileMatrix.java
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
