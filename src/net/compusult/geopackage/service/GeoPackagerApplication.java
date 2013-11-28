/*
/*
 * GeoPackagerApplication.java
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
