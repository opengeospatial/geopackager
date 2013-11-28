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
