/*
 * WPSStatusResource.java
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
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

}
