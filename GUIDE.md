HTTP Methods
------------

Source files under net.compusult.geopackage.service.resource are the HTTP entry points.  They are service methods corresponding to the HTTP resource paths listed in applicationContext.xml under geopackageRouter.attachments.  Each resource is defined using a Spring prototype bean, meaning that a new instance is created for each HTTP request.

GET /wps?SERVICE=WPS&ACCEPTVERSIONS=1.0.0&REQUEST=GetCapabilities or REQUEST=DescribeProcess&IDENTIFIER=GeoPackaging

  The response is either the capabilities document, or a document describing the inputs and outputs of the 'GeoPackaging' process.

POST /wps

  The POST body is an XML-formatted ExecuteProcess WPS request.  The response includes a long base64-encoded string which is the process ID.

GET /wps/status/{processId}

  Given a process ID, this entry point retrieves the status document.  A 404 error will be produced if the server no longer knows about that process, which can happen when the server has been restarted or a long time has passed since the job was processed.
  
GET /wps/gpkg/{processId}

  This method retrieves the GeoPackage created as a result of the given job (identified by its process ID).

The GeoPackaging Request Flow
-----------------------------

The handlePostRequest method of MainWPSResource creates a GeoPackager object representing the processing request, and submits it to the GeoPackagingPool.  Depending on the value of the storeExecuteResponse request flag, handlePostRequest either returns immediately or waits for the process to complete.

The GeoPackager has a ProcessingStatus enum that lists the lifecycle states of the process.  When the pool's thread pool executes the GeoPackager, a GeoPackage is created (in /var/lib/geopackager by default, set in applicationContext.xml) to hold the processing result.  The run method then does two scans over the context document's list of resources.  The first pass selects an offering and corresponding harvester, and computes the total number of layers that will be harvested.  The second actually runs the harvesters.  A ProgressTracker instance keeps track of the percentage complete.

Instances of the Harvester interface (subclasses of AbstractHarvester) are constructed by a factory that is configured in the Spring applicationContext.xml file.  In that file, offering codes are mapped to Spring beans that implement the harvesting (one instance per request).
