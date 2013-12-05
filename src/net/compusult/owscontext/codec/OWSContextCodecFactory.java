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

import org.springframework.beans.factory.annotation.Autowired;

import net.compusult.geometry.gml.GMLConverterInterface;


public class OWSContextCodecFactory {

	private GMLConverterInterface gmlConverter;
	
	// This is only strictly required with the Atom codec
	@Autowired(required = false)
	public void setGMLConverter(GMLConverterInterface gmlConverter) {
		this.gmlConverter = gmlConverter;
	}
	
	public AtomCodec createAtomCodec() {
		if (gmlConverter == null) {
			throw new IllegalStateException("gmlConverter has not been initialized for the OWS Context AtomCodec");
		}
		return new AtomCodec(gmlConverter);
	}
	
	public OWSContextCodec createJSONCodec() {
//		return new JSONCodec();
		return null;
	}
	
	public OWSContextCodec createCodec(String mimeType) {
		if (AtomCodec.MIME_TYPE.equals(mimeType)) {
			return createAtomCodec();
//		} else if (JSONCodec.MIME_TYPE.equals(mimeType)) {
//			return createJSONCodec();
		} else {
			return null;
		}
	}
	
}
