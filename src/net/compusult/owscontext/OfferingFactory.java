/*
 * Copyright (c) 2013 Unpublished, Compusult Limited.  All Rights Reserved.
 *
 * This software contains proprietary and confidential information of
 * Compusult Limited and its suppliers.  Use, disclosure or reproduction is
 * prohibited without the prior express written consent of Compusult Limited.
 */

package net.compusult.owscontext;

public class OfferingFactory {

	private static final OfferingFactory INSTANCE = new OfferingFactory();
	
	public static OfferingFactory getInstance() {
		return INSTANCE;
	}
	
	public Offering createOffering(String code) {
		if (WMSOffering.OFFERING_CODE.equals(code)) {						return new WMSOffering();
		} else if (WMTSOffering.OFFERING_CODE.equals(code)) {				return new WMTSOffering();
		} else if (Simple3857TilesOffering.OFFERING_CODE.equals(code)) {	return new Simple3857TilesOffering();
		} else if (WFSOffering.OFFERING_CODE.equals(code)) {				return new WFSOffering();
		} else if (KMLOffering.OFFERING_CODE.equals(code)) {				return new KMLOffering();
		} else {															return new Offering(code);
		}
	}
}
