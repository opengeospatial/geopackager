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

import net.compusult.geometry.gml.GMLConverterInterface;


public class OWSContextCodecFactory {

	private static final OWSContextCodecFactory INSTANCE = new OWSContextCodecFactory();
	
	public static OWSContextCodecFactory getInstance() {
		return INSTANCE;
	}
	
	private GMLConverterInterface gmlConverter;
	
	public static void setGMLConverter(GMLConverterInterface gmlConverter) {
		INSTANCE.gmlConverter = gmlConverter;
	}
	
	public AtomCodec createAtomCodec() {
		return new AtomCodec(gmlConverter);
	}
	
	public OWSContextCodec createJSONCodec() {
		return null;
	}
	
	public OWSContextCodec createCodec(String mimeType) {
		if (AtomCodec.MIME_TYPE.equals(mimeType)) {
			return new AtomCodec(gmlConverter);
		} else {
			return null;
		}
	}
	
}
