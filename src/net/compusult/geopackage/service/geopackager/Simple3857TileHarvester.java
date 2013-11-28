package net.compusult.geopackage.service.geopackager;

import java.util.Map;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.geopackage.service.model.LayerInformation;
import net.compusult.geopackage.service.model.LayerInformation.Type;
import net.compusult.geopackage.service.wmts.Simple3857TileServer;
import net.compusult.geopackage.service.wmts.TileServer;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Operation;
import net.compusult.owscontext.Resource;

public class Simple3857TileHarvester extends AbstractWMTSHarvester {

	public Simple3857TileHarvester(ProgressTracker progressTracker) {
		super(progressTracker);
	}
	
	@Override
	public void harvest(GeoPackage gpkg, Resource resource, Offering offering) throws GeoPackageException {

		Operation getTile = findRequiredOperation(offering, "GetTile");
		
		Map<String, String> params = parseParameters(offering);
		
		TileServer bing = new Simple3857TileServer(getTile.getRequestURL(), getTile.getType(), params);
		
		LayerInformation layerInfo = new LayerInformation(gpkg, Type.TILES, sanitizeTableName(resource.getId()));
		layerInfo.setTitle(resource.getTitle().getText());
		layerInfo.setCrs("3857");
		
		HarvestTiles tileHarvester = new HarvestTiles(gpkg, bing, layerInfo, params, progressTracker);
		tileHarvester.harvestTiles();
	}
	
}
