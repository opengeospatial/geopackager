/*
 * OfferingFactory.java
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
