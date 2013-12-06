/*
 * HarvesterFactory.java
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
   
package net.compusult.geopackage.service.geopackager;

import java.util.Map;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.owscontext.Offering;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

public class HarvesterFactory {
	
	@Autowired
	private ApplicationContext applicationContext;
	private Map<String, String> beanNames;
	
	public Harvester createHarvester(Offering offering, ProgressTracker progressTracker) throws GeoPackageException {
		
		String beanName = beanNames.get(offering.getOfferingCode());
		if (beanName == null) {
			throw new GeoPackageException("Unsupported Offering class " + offering.getClass().getName() + " (" + offering.getOfferingCode() + ")");
		}
		
		return (Harvester) applicationContext.getBean(beanName, progressTracker);
	}

	@Required
	public void setBeanNames(Map<String, String> beanNames) {
		this.beanNames = beanNames;
	}

}
