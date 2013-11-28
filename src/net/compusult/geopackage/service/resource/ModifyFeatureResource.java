/*
 * ModifyFeatureResource.java
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
   
package net.compusult.geopackage.service.resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class ModifyFeatureResource extends ServerResource {
	
	private String txnId;
	private String featureType;
	private String featureId;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		
		this.txnId = getAttribute("txnId");
		this.featureType = getAttribute("featureType");
		this.featureId = getAttribute("featureId");
	}

	@Put
	public void updateFeature(JsonRepresentation newFeatureRep) throws JSONException {
		// Locate the transaction by its ID
		// Locate the feature by its ID
		JSONObject json = newFeatureRep.getJsonObject();
	}

	@Put
	public void deleteFeature() throws Exception {
		// Locate the transaction by its ID
		// Locate the feature by its ID
	}

}
