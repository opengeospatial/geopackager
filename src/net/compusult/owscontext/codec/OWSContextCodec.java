/*
 * OWSContextCodec.java
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
