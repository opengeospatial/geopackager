/*
 * Copyright (c) 2013 Unpublished, Compusult Limited.  All Rights Reserved.
 *
 * This software contains proprietary and confidential information of
 * Compusult Limited and its suppliers.  Use, disclosure or reproduction is
 * prohibited without the prior express written consent of Compusult Limited.
 */

package net.compusult.owscontext;

public class WMTSOffering extends Offering {
	
	public static final String OFFERING_CODE = "http://www.opengis.net/spec/owc-atom/1.0/req/wmts";

	public WMTSOffering() {
		super(OFFERING_CODE);
	}
}
