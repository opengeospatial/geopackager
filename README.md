geopackager
===========

  A Java-based GeoPackaging service with a WPS interface


This Java web application is based on Restlet (http://restlet.org) and runs in a servlet 2.5+ container.  It exposes an OGC WPS 1.0 interface with one process called 'GeoPackaging' that may be invoked by a generic WPS client.  The result of the process, which may be made available synchronously or asynchronously, is a GeoPackage (optionally encrypted).

Where practical the application leverages Spring and the src/applicationContext.xml file to initialize application objects.

This GeoPackager application is licensed under the Apache license, version 2.0.
