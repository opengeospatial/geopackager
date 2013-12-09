Quick Guide to the GeoPackager
==============================


HTTP Methods
------------

Source files under `net.compusult.geopackage.service.resource` are the HTTP entry points.  They are service methods corresponding to the HTTP resource paths listed in `applicationContext.xml` under geopackageRouter.attachments.  Each resource is defined using a Spring prototype bean, meaning that a new instance is created for each HTTP request.

* `GET /wps?SERVICE=WPS&ACCEPTVERSIONS=1.0.0&REQUEST=GetCapabilities` or `REQUEST=DescribeProcess&IDENTIFIER=GeoPackaging`

  >  The response is either the capabilities document, or a document describing the inputs and outputs of the 'GeoPackaging' process.

* `POST /wps`

  >  The POST body is an XML-formatted ExecuteProcess WPS request.  The response includes a long base64-encoded string which is the process ID.

* `GET /wps/status/{processId}`

  >  Given a process ID, this entry point retrieves the status document.  A 404 error will be produced if the server no longer knows about that process, which can happen when the server has been restarted or a long time has passed since the job was processed.
  
* `GET /wps/gpkg/{processId}`

  >  This method retrieves the GeoPackage created as a result of the given job (identified by its process ID).

The GeoPackaging Request Flow
-----------------------------

The handlePostRequest method of MainWPSResource creates a GeoPackager object representing the processing request, and submits it to the GeoPackagingPool.  Depending on the value of the storeExecuteResponse request flag, handlePostRequest either returns immediately or waits for the process to complete.

The GeoPackager has a ProcessingStatus enum that lists the lifecycle states of the process.  When the pool's thread pool executes the GeoPackager, a GeoPackage is created (in `/var/lib/geopackager` by default, set in `applicationContext.xml`) to hold the processing result.  The run method then does two scans over the context document's list of resources.  The first pass selects an offering (usually the first one) and corresponding harvester, and computes the total number of layers that will be harvested.  The second actually runs the harvesters.  A ProgressTracker instance keeps track of the percentage complete.

Instances of the Harvester interface (subclasses of AbstractHarvester) are constructed by a factory that is configured in the Spring `applicationContext.xml` file.  In that file, offering codes are mapped to Spring beans that implement the harvesting (one instance per request).

Harvesters
----------

Harvesters are responsible for extracting data from target data sources, and writing that data into tile or feature layers in the current GeoPackage.  How they interpret data from the resource and/or offering elements in the context document depends on the harvester; that is, it depends on the offering code of the selected offering.
* A WMTS harvester such as RealWMTSHarvester has to use the `<testbed:parameters>` extension block in the resource, in order to define the tile matrix set, layer name and range of tile matrices desired.
* A WMS harvester may optionally use `<testbed:parameters>` to define a similar kind of tiling range.
* A KML harvester uses either or both of inline data or URL reference in a `<content>` element.
* A WFS harvester would use multiple `<operation>` elements under the selected offering.

OWS Context
-----------

The package `net.compusult.owscontext` contains a Java model for an OWS Context document.  The top-level class is called `ContextDoc`, which has attributes and child objects in a manner similar to that defined in the OGC OWS Context Conceptual Model (OGC doc no. 12-080r2) draft 17.

The subpackage `codec` contains a factory for constructing OWS Context encoder/decoders based on desired MIME type.  At present only the Atom format (`application/atom+xml`) is defined.  The Atom codec is compliant with OGC OWS Context Atom Encoding Specification (OGC doc no. 12-084r2) draft 21.
