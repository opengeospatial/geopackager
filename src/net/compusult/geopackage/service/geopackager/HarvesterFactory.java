package net.compusult.geopackage.service.geopackager;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Simple3857TilesOffering;
import net.compusult.owscontext.WMTSOffering;

public class HarvesterFactory {
	
	private static final HarvesterFactory INSTANCE = new HarvesterFactory();
	
	public static HarvesterFactory getInstance() {
		return INSTANCE;
	}
	
	public Harvester createHarvester(Offering offering, ProgressTracker progressTracker) throws GeoPackageException {
		
		if (offering instanceof WMTSOffering) {
			return new RealWMTSHarvester(progressTracker);
			
		} else if (offering instanceof Simple3857TilesOffering) {
			return new Simple3857TileHarvester(progressTracker);
			
//		} else if (offering instanceof WFSOffering) {
//			return new WFSHarvester(progressTracker);
		}
			
		throw new GeoPackageException("Unsupported Offering class " + offering.getClass().getName());
	}

}
