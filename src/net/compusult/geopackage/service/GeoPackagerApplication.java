/*
 * Copyright (c) 2013 Unpublished, Compusult Limited.  All Rights Reserved.
 *
 * This software contains proprietary and confidential information of
 * Compusult Limited and its suppliers.  Use, disclosure or reproduction is
 * prohibited without the prior express written consent of Compusult Limited.
 */

package net.compusult.geopackage.service;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * This Restlet application is loaded via Spring, invoked in web.xml.
 * 
 * @author sean
 */

public class GeoPackagerApplication extends Application implements InitializingBean {

	private Restlet router;

	public GeoPackagerApplication() {
		super();
	}
	
	@Override
	public Restlet createInboundRoot() {
		// Ensure the server honours Connection:keep-alive
		getContext().getAttributes().put("persistingConnections", true);
		return router;
	}
	
	/*
	 *------------------------------------------------------------------------
	 */
	
	// Setters are invoked by Spring
	
	@Required
	public void setRouter(Restlet router) {
		this.router = router;
	}
	
	public void afterPropertiesSet() throws Exception {
		getEncoderService().setEnabled(true);
		getEncoderService().setMinimumSize(200);
		
		// This is to support file uploads (if needed in the future).
		getMetadataService().addExtension("multipart-form", MediaType.MULTIPART_FORM_DATA);
	}
	
}
