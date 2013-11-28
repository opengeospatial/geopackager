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
