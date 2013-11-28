/*
 * Copyright (c) 2013 Unpublished, Compusult Limited.  All Rights Reserved.
 *
 * This software contains proprietary and confidential information of
 * Compusult Limited and its suppliers.  Use, disclosure or reproduction is
 * prohibited without the prior express written consent of Compusult Limited.
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
