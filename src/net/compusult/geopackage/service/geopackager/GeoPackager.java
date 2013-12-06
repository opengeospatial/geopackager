/*
 * GeoPackager.java
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.harvester.Harvester;
import net.compusult.geopackage.service.harvester.HarvesterFactory;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.owscontext.ContextDoc;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

public class GeoPackager implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(GeoPackager.class);
	
	public static final String MIME_TYPE_GPKG = "application/vnd.ogc.gpkg";
	public static final String MIME_TYPE_SGPKG = "application/x-sgpkg";
	
	public enum ProcessingStatus {
		ACCEPTED("ProcessAccepted", false),
		STARTED("ProcessStarted", false),
		PAUSED("ProcessPaused", false),
		SUCCEEDED("ProcessSucceeded", true),
		FAILED("ProcessFailed", true);

		private String elementName;
		private boolean isFinal;
		
		private ProcessingStatus(String elementName, boolean isFinal) {
			this.elementName = elementName;
			this.isFinal = isFinal;
		}
		
		public String getElementName() {
			return elementName;
		}
		
		public boolean isFinal() {
			return isFinal;
		}
	}
	
	private final ContextDoc owsContext;
	private final String passPhrase;
	private final boolean secure;
	
	@SuppressWarnings("unused")
	private boolean storeGeoPackageAsReference;
	@SuppressWarnings("unused")
	private boolean storeOWSContextAsReference;
	private boolean includeLineage;
	private Element dataInputs;
	private List<Element> outputDefns;
	
	private String id;
	private String workDirectory;
	private ProcessingStatus currentStatus;
	private ProgressTracker progressTracker;
	private ContextDoc resultingContext;
	private String fileName;
	
	private HarvesterFactory harvesterFactory;
	private OfferingSelectionAlgorithm offeringSelector;
	
	public GeoPackager(ContextDoc owsContext, String passPhrase) {
		this.owsContext = owsContext;
		this.passPhrase = (passPhrase == null) ? "" : passPhrase.trim();
		this.secure = !"".equals(this.passPhrase);
		this.resultingContext = null;
		this.currentStatus = ProcessingStatus.ACCEPTED;
		this.progressTracker = null;
		this.fileName = null;
	}

	public void setStoreGeoPackageAsReference(boolean asReference) {
		this.storeGeoPackageAsReference = asReference;
	}

	public void setStoreOWSContextAsReference(boolean asReference) {
		this.storeOWSContextAsReference = asReference;
	}
	
	public boolean getIncludeLineage() {
		return includeLineage;
	}

	public void setIncludeLineage(boolean includeLineage) {
		this.includeLineage = includeLineage;
	}

	public Element getDataInputs() {
		return dataInputs;
	}

	public void setDataInputs(Element dataInputs) {
		this.dataInputs = dataInputs;
	}

	public List<Element> getOutputDefns() {
		return outputDefns;
	}

	public void setOutputDefns(List<Element> outputDefns) {
		this.outputDefns = outputDefns;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWorkDirectory() {
		return workDirectory;
	}

	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	public String getFileName() {
		return fileName;
	}

	public ContextDoc getResultingContext() {
		return resultingContext;
	}
	
	public ProcessingStatus getCurrentStatus() {
		synchronized (this) {
			return currentStatus;
		}
	}
	
	private void setCurrentStatus(ProcessingStatus newStatus) {
		synchronized (this) {
			currentStatus = newStatus;
		}
	}

	public int getPercentComplete() {
		return (progressTracker == null) ? 0 : progressTracker.getPercentComplete();
	}

	public boolean isSecure() {
		return secure;
	}

	@Override
	public void run() {
		setCurrentStatus(ProcessingStatus.STARTED);
		
		boolean succeeded = false;
		try {
			fileName = id + "." + (secure ? "s" : "") + "gpkg";
			File gpkgFile = new File(workDirectory, fileName);
			GeoPackage gpkg = new GeoPackage(gpkgFile, passPhrase.toCharArray());
			
			List<ResourceOffering> resourceOfferings = new ArrayList<ResourceOffering>();
			progressTracker = new ProgressTracker();
			
			/*
			 * Gather up our work items first so we can count them.  This will allow
			 * us to present an accurate status (% complete).
			 * 
			 * Each Resource represents a layer.  Each Offering within that Resource
			 * represents an alternative way to view/retrieve the data.
			 */
			for (Resource resource : owsContext.getResources()) {
				
				Offering chosenOffering = offeringSelector.selectOffering(resource);
				if (chosenOffering == null) {
					LOG.warn("Unable to determine a harvesting approach for layer (resource) with identifier '" + resource.getId() + "'");
					
				} else {
					try {
						Harvester harvester = harvesterFactory.createHarvester(chosenOffering, progressTracker);
						resourceOfferings.add(new ResourceOffering(resource, chosenOffering, harvester));
						
					} catch (GeoPackageException e) {
						LOG.warn("Could not harvest layer (resource) with identifier '" + resource.getId() + "'", e);
					}
				}
			}

			progressTracker.setItemCount(resourceOfferings.size());
			
			int successfulLayers = 0;
			for (ResourceOffering ro : resourceOfferings) {
				try {
					ro.harvest(gpkg);
					++ successfulLayers;
				} catch (GeoPackageException e) {
					LOG.warn("Could not harvest layer (resource) with identifier '" + ro.getResource().getId() + "'", e);
				}
				progressTracker.newItem();		// advance to the next, regardless of whether we succeeded
			}
			
			if (successfulLayers == 0) {
				LOG.warn("No layers were successfully imported into the GeoPackage");
			} else {
				LOG.info("Successfully imported " + successfulLayers + " layer(s) into the GeoPackage");
				succeeded = true;
			}
		
		} catch (Throwable t) {
			LOG.error("Failed to create GeoPackage", t);
			System.err.println("Failed to create GeoPackage");
			t.printStackTrace(System.err);
			
		} finally {
			setCurrentStatus(succeeded ? ProcessingStatus.SUCCEEDED : ProcessingStatus.FAILED);
		}
	}
	
	
	private static class ResourceOffering {
		private final Resource resource;
		private final Offering offering;
		private final Harvester harvester;
		
		public ResourceOffering(Resource resource, Offering offering, Harvester harvester) {
			this.resource = resource;
			this.offering = offering;
			this.harvester = harvester;
		}

		public Resource getResource() {
			return resource;
		}

		public void harvest(GeoPackage gpkg) throws GeoPackageException {
			harvester.harvest(gpkg, resource, offering);
		}
	}

	@Autowired
	public void setHarvesterFactory(HarvesterFactory harvesterFactory) {
		this.harvesterFactory = harvesterFactory;
	}

	@Autowired
	public void setOfferingSelector(OfferingSelectionAlgorithm offeringSelector) {
		this.offeringSelector = offeringSelector;
	}
	
}
