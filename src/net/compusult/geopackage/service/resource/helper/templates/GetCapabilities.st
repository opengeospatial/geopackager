<?xml version="1.0" encoding="UTF-8"?>

<wps:Capabilities service="WPS" version="1.0.0" xml:lang="en-US"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		xmlns:wps="http://www.opengis.net/wps/1.0.0"
		xmlns:ows="http://www.opengis.net/ows/1.1"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd"
		updateSequence="1">

	<ows:ServiceIdentification>
		<ows:Title>GeoPackaging WPS server</ows:Title>
		<ows:Abstract>GeoPackaging WPS server developed for OWS-10.</ows:Abstract>
		<ows:Keywords>
			<ows:Keyword>WPS</ows:Keyword>
			<ows:Keyword>GeoPackage</ows:Keyword>
		</ows:Keywords>
		<ows:ServiceType>WPS</ows:ServiceType>
		<ows:ServiceTypeVersion>1.0.0</ows:ServiceTypeVersion>
		<ows:Fees>NONE</ows:Fees>
		<ows:AccessConstraints>NONE</ows:AccessConstraints>
	</ows:ServiceIdentification>

	<ows:ServiceProvider>
		<ows:ProviderName>$provider.company$</ows:ProviderName>
		<ows:ProviderSite xlink:href="$provider.url$"/>
		<ows:ServiceContact>
			<ows:IndividualName>$provider.name$</ows:IndividualName>
			<ows:PositionName>$provider.title$</ows:PositionName>
			<ows:ContactInfo>
				<ows:Phone>
					<ows:Voice>$provider.voice$</ows:Voice>
					<ows:Facsimile>$provider.fax$</ows:Facsimile>
				</ows:Phone>
				<ows:Address>
					<ows:DeliveryPoint>$provider.addr$</ows:DeliveryPoint>
					<ows:City>$provider.city$</ows:City>
					<ows:AdministrativeArea>$provider.prov$</ows:AdministrativeArea>
					<ows:PostalCode>$provider.postalCode$</ows:PostalCode>
					<ows:Country>$provider.country$</ows:Country>
					<ows:ElectronicMailAddress>$provider.email$</ows:ElectronicMailAddress>
				</ows:Address>
			</ows:ContactInfo>
		</ows:ServiceContact>
	</ows:ServiceProvider>

	<ows:OperationsMetadata>
		<ows:Operation name="GetCapabilities">
			<ows:DCP>
				<ows:HTTP>
					<ows:Get xlink:href="$ref.scheme$:$ref.hierarchicalPart$?"/>
				</ows:HTTP>
			</ows:DCP>
		</ows:Operation>
		<ows:Operation name="DescribeProcess">
			<ows:DCP>
				<ows:HTTP>
					<ows:Get xlink:href="$ref.scheme$:$ref.hierarchicalPart$?"/>
					<ows:Post xlink:href="$ref.scheme$:$ref.hierarchicalPart$"/>
				</ows:HTTP>
			</ows:DCP>
		</ows:Operation>
		<ows:Operation name="Execute">
			<ows:DCP>
				<ows:HTTP>
					<ows:Get xlink:href="$ref.scheme$:$ref.hierarchicalPart$?"/>
					<ows:Post xlink:href="$ref.scheme$:$ref.hierarchicalPart$"/>
				</ows:HTTP>
			</ows:DCP>
		</ows:Operation>
	</ows:OperationsMetadata>

	<wps:ProcessOfferings>
		<wps:Process wps:processVersion="1">
			<ows:Identifier>$process$</ows:Identifier>
			<ows:Title>Create a GeoPackage</ows:Title>
			<ows:Abstract>
				Create a GeoPackage based on the instructions given in an OWS Context document.
				If a passphrase is provided then the GeoPackage is encrypted with that passphrase.
			</ows:Abstract>
			<ows:Metadata xlink:title="GeoPackage" />
			<ows:Metadata xlink:title="OWS Context" />
			<ows:Metadata xlink:title="encryption" />
		</wps:Process>
	</wps:ProcessOfferings>

	<wps:Languages>
		<wps:Default>
			<ows:Language>en-US</ows:Language>
		</wps:Default>
		<wps:Supported>
			<ows:Language>en-US</ows:Language>
		</wps:Supported>
	</wps:Languages> 

</wps:Capabilities>
