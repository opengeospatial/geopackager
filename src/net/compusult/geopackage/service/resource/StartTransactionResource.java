/*
 * StartTransactionResource.java
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

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class StartTransactionResource extends ServerResource {

	@Post
	public String startTransaction() throws UnsupportedEncodingException {
		/*
		 * Create a transaction ID string.
		 */
		String now = String.valueOf(Math.abs(new Random().nextLong() ^ System.currentTimeMillis()));
		String hashed = DigestUtils.sha256Hex(now.getBytes("UTF-8"));
		
		/*
		 * Put 'hashed' into the active-transaction table.
		 */
		
		return hashed;
	}
	
}
