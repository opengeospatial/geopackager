package net.compusult.geopackage.service.resource;

import net.compusult.geopackage.service.geopackager.GeoPackager;
import net.compusult.geopackage.service.geopackager.GeoPackager.ProcessingStatus;
import net.compusult.geopackage.service.geopackager.GeoPackagingPool;

import org.restlet.data.Disposition;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.springframework.beans.factory.annotation.Autowired;

public class GeoPackageRetrievalResource extends WPSResource {

	private GeoPackagingPool packagerPool;

	@Get
	public Representation retrieveFile() {
		GeoPackager gpkger = packagerPool.find(getAttribute("id"));
		if (gpkger == null) {
			return error(ExceptionCode.InvalidParameterValue, "No GeoPackage with that ID");
		} else if (! gpkger.getCurrentStatus().isFinal()) {
			return error(ExceptionCode.InvalidParameterValue, "GeoPackaging for that ID is still in progress");
		} else if (gpkger.getCurrentStatus() != ProcessingStatus.SUCCEEDED) {
			return error(ExceptionCode.InvalidParameterValue, "GeoPackaging previously failed for that ID");
		}
		
		Representation result = new FileRepresentation(gpkger.getFile(), gpkger.getMediaType());
		result.getDisposition().setType(Disposition.TYPE_ATTACHMENT);
		
		return result;
	}
	
	@Autowired
	public void setPackagerPool(GeoPackagingPool packagerPool) {
		this.packagerPool = packagerPool;
	}
	
}
