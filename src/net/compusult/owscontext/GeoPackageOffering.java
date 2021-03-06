/*
 * WMTSOffering.java
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
   
package net.compusult.owscontext;

public class GeoPackageOffering extends Offering {
	
	public static final String OFFERING_CODE = "http://www.opengis.net/spec/owc-atom/1.0/req/gpkg";

	public GeoPackageOffering() {
		super(OFFERING_CODE);
	}
}
