/*
 * GeoPackagerStatusService.java
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

import java.util.Map;

import net.compusult.geopackage.service.resource.WPSResource;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.service.StatusService;

/**
 * An instance of this is set on the 'statusService' property of the Restlet
 * Application class to override the default error page with the required XML.
 * 
 * @author sean
 */
public class GeoPackagerStatusService extends StatusService {

	public GeoPackagerStatusService() {
		super();
	}

	public GeoPackagerStatusService(boolean enabled) {
		super(enabled);
	}

	@Override
	public Representation getRepresentation(Status status, Request request, Response response) {
        final StringBuilder sb = new StringBuilder();
        
        Map<String, Object> attrs = response.getAttributes();
        
        String code = (String) attrs.get(WPSResource.RESP_ATTR_EXCEPTION_CODE);
        String text = (String) attrs.get(WPSResource.RESP_ATTR_EXCEPTION_TEXT);
        String locator = (String) attrs.get(WPSResource.RESP_ATTR_EXCEPTION_LOCATOR);
        Throwable throwable = (Throwable) attrs.get(WPSResource.RESP_ATTR_EXCEPTION_THROWABLE);
        
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<ows:ExceptionReport xmlns:ows=\"http://www.opengis.net/ows/1.1\"");
        sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        sb.append(" xsi:schemaLocation=\"http://www.opengis.net/ows/1.1 http://schemas.opengis.net/ows/1.1.0/owsExceptionReport.xsd\"");
        sb.append(" version=\"1.0.0\" xml:lang=\"en-CA\">\n");
        sb.append("  <ows:Exception exceptionCode=\"").append(code).append("\"");
        if (locator != null) {
        	sb.append(" locator=\"").append(locator).append("\"");
        }
        sb.append(">\n");
        sb.append("    <ows:ExceptionText>").append(text);
        if (throwable != null) {
        	sb.append(" - ").append(throwable.getMessage());
        }
        sb.append("    </ows:ExceptionText>\n");
        sb.append("  </ows:Exception>\n");
        sb.append("</ows:ExceptionReport>\n");
        
        return new StringRepresentation(sb.toString(), MediaType.TEXT_XML);
	}

}
