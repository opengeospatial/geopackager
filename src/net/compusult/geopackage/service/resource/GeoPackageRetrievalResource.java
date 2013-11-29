package net.compusult.geopackage.service.resource;

import java.io.File;

import net.compusult.geopackage.service.geopackager.GeoPackager;
import net.compusult.geopackage.service.geopackager.GeoPackager.ProcessingStatus;
import net.compusult.geopackage.service.geopackager.GeoPackagingPool;

import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
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
		
		File gpkgFile = new File(gpkger.getWorkDirectory(), gpkger.getFileName());
		MediaType mediaType = new MediaType(gpkger.isSecure() ? GeoPackager.MIME_TYPE_SGPKG : GeoPackager.MIME_TYPE_GPKG);
		Representation result = new FileRepresentation(gpkgFile, mediaType);
		result.getDisposition().setType(Disposition.TYPE_ATTACHMENT);
		
		return result;
	}
	
	@Autowired
	public void setPackagerPool(GeoPackagingPool packagerPool) {
		this.packagerPool = packagerPool;
	}
	
}
