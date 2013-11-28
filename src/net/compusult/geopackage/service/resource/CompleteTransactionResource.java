package net.compusult.geopackage.service.resource;

import org.restlet.resource.Delete;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class CompleteTransactionResource extends ServerResource {
	
	private String txnId;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		
		this.txnId = getAttribute("txnId");
	}

	@Put
	public void commitTransaction() {
	}

	@Delete
	public void abortTransaction() {
	}
	
}
