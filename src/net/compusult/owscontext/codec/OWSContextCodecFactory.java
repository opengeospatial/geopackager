/*
 * OWSContextCodecFactory.java
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

import java.util.Map;

import net.compusult.owscontext.codec.OWSContextCodec.EncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;


public class OWSContextCodecFactory {
	
	@Autowired
	private ApplicationContext applicationContext;
	private Map<String, String> beanNames;

	public OWSContextCodec createCodec(String mimeType) throws EncodingException {
	
		String beanName = beanNames.get(mimeType);
		if (beanName == null) {
			throw new EncodingException("Unsupported OWS Context codec MIME type " + mimeType);
		}
		
		return (OWSContextCodec) applicationContext.getBean(beanName);
	}

	@Required
	public void setBeanNames(Map<String, String> beanNames) {
		this.beanNames = beanNames;
	}
	
}
