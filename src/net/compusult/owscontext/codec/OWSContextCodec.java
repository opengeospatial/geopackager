/*
 * Copyright (c) 2013 Unpublished, Compusult Limited.  All Rights Reserved.
 *
 * This software contains proprietary and confidential information of
 * Compusult Limited and its suppliers.  Use, disclosure or reproduction is
 * prohibited without the prior express written consent of Compusult Limited.
 */

package net.compusult.owscontext.codec;

import java.io.InputStream;

import net.compusult.owscontext.ContextDoc;

public interface OWSContextCodec {

	String getName();
	String getMimeType();
	String encode(ContextDoc context) throws EncodingException;
	ContextDoc decode(String in) throws EncodingException;
	ContextDoc decode(InputStream in) throws EncodingException;
	
	@SuppressWarnings("serial")
	public class EncodingException extends Exception {

		public EncodingException(String message) {
			super(message);
		}
		
		public EncodingException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
