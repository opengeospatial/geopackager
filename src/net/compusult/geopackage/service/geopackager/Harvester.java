package net.compusult.geopackage.service.geopackager;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Resource;

public interface Harvester {

	void harvest(GeoPackage gpkg, Resource resource, Offering offering) throws GeoPackageException;
	
}