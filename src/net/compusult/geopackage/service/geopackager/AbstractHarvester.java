package net.compusult.geopackage.service.geopackager;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.owscontext.Offering;
import net.compusult.owscontext.Operation;

public abstract class AbstractHarvester implements Harvester {

	protected static final String TESTBED_NS = "http://schemas.compusult.net/ows10/ows-context";
	protected static final String EXTENSION_PARAM = "parameter";
	
	protected ProgressTracker progressTracker;
	
	protected AbstractHarvester(ProgressTracker progressTracker) {
		this.progressTracker = progressTracker;
	}
	
	protected Operation findRequiredOperation(Offering offering, String desiredCode) throws GeoPackageException {
		for (Operation op : offering.getOperations()) {
			if (desiredCode.equalsIgnoreCase(op.getOperationCode())) {
				return op;
			}
		}
		throw new GeoPackageException("Offering lacks a " + desiredCode + " operation, cannot proceed");
	}
	
	protected String sanitizeTableName(String tableName) {
		tableName = tableName.replaceAll("[^\\w\\d]", "");
		if (tableName.length() > 32) {
			tableName = tableName.substring(0, 32);
		}
		return tableName;
	}

}
