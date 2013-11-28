package net.compusult.geopackage.service.resource;

import javax.xml.parsers.ParserConfigurationException;

import net.compusult.geopackage.service.geopackager.GeoPackager;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;


public class WPSStatusResource extends WPSResource {
	
	private String processId;
	private GeoPackager packager;
	
	public WPSStatusResource() {
		super();
	}

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		this.processId = getAttribute("processId");
		this.packager = packagerPool.find(processId);
		if (packager == null) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Requested process does not exist");
		}
	}

	@Get
	public Representation checkStatus() {
		try {
			return generateResponseDoc(packager);
		} catch (ParserConfigurationException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

}
