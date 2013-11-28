package net.compusult.geopackage.service.geopackager;

import java.util.Map;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.geopackage.service.model.LayerInformation;
import net.compusult.geopackage.service.model.LayerInformation.Type;
import net.compusult.geopackage.service.wmts.RealWMTS;
import net.compusult.geopackage.service.wmts.TileServer;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Operation;
import net.compusult.owscontext.Resource;

public class RealWMTSHarvester extends AbstractWMTSHarvester {

	public RealWMTSHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}
	
	@Override
	public void harvest(GeoPackage gpkg, Resource resource, Offering offering) throws GeoPackageException {

		Operation getCaps = findRequiredOperation(offering, "GetCapabilities");
		
		Map<String, String> params = parseParameters(offering);
		
		TileServer wmts = new RealWMTS(getCaps.getRequestURL(), params);
		
		LayerInformation layerInfo = new LayerInformation(gpkg, Type.TILES, sanitizeTableName(resource.getId()));
		layerInfo.setTitle(resource.getTitle().getText());
		layerInfo.setCrs(wmts.getCRS());
		
		HarvestTiles tileHarvester = new HarvestTiles(gpkg, wmts, layerInfo, params, progressTracker);
		tileHarvester.harvestTiles();
	}
	
}
