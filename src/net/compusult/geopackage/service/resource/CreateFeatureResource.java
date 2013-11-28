package net.compusult.geopackage.service.resource;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class CreateFeatureResource extends ServerResource {
	
	private String txnId;
	private String featureType;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		
		this.txnId = getAttribute("txnId");
		this.featureType = getAttribute("featureType");
	}

	@Post
	public String createFeature(JsonRepresentation newFeatureRep) {
		return null;		// new feature's ID
	}

}
