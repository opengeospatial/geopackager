<?xml version="1.0" encoding="UTF-8"?>

<ProcessDescriptions service="WPS" version="1.0.0" xml:lang="en-US"
		xmlns="http://www.opengis.net/wps/1.0.0"
		xmlns:wps="http://www.opengis.net/wps/1.0.0"
		xmlns:ows="http://www.opengis.net/ows/1.1"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd">

	<ProcessDescription processVersion="1" storeSupported="true" statusSupported="true">
		<ows:Identifier>$process$</ows:Identifier>
		<ows:Title>Create a GeoPackage</ows:Title>
		<ows:Abstract>
			Create a GeoPackage based on the instructions given in an OWS Context document.
			If a passphrase is provided then the GeoPackage is encrypted with that passphrase.
		</ows:Abstract>
		<ows:Metadata xlink:title="GeoPackage" />
		<ows:Metadata xlink:title="OWS Context" />
		<ows:Metadata xlink:title="encryption" />

		<DataInputs>
			<Input minOccurs="1" maxOccurs="1">
				<ows:Identifier>$input_owc$</ows:Identifier>
				<ows:Title>OWS Context document</ows:Title>
				<ows:Abstract>An OWS Context document describing the data that should be packaged into the GeoPackage.</ows:Abstract>
				<ComplexData>
					<Default>
						<Format>
							<MimeType>application/atom+xml</MimeType>
						</Format>
					</Default>
					<Supported>
						<Format>
							<MimeType>application/atom+xml</MimeType>
						</Format>
					</Supported>
				</ComplexData>
			</Input>
			
			<Input minOccurs="0" maxOccurs="1">
				<ows:Identifier>$input_psw$</ows:Identifier>
				<ows:Title>Passphrase</ows:Title>
				<ows:Abstract>An optional passphrase used to encrypt the resulting GeoPackage.</ows:Abstract>
				<LiteralData>
					<ows:DataType ows:reference="http://www.w3.org/TR/xmlschema-2/#string">string</ows:DataType>
					<ows:AnyValue/>
					<DefaultValue></DefaultValue>
				</LiteralData>
			</Input>
		</DataInputs>

		<ProcessOutputs>
			<Output>
				<ows:Identifier>$output_gpkg$</ows:Identifier>
				<ows:Title>GeoPackage</ows:Title>
				<ows:Abstract>The resulting GeoPackage, optionally encrypted.</ows:Abstract>
				<ComplexOutput>
					<Default>
						<Format>
							<MimeType>application/vnd.ogc.gpkg</MimeType>
						</Format>
					</Default>
					<Supported>
						<Format>
							<MimeType>application/vnd.ogc.gpkg</MimeType>
						</Format>
						<Format>
							<MimeType>application/x-sgpkg</MimeType>
						</Format>
					</Supported>
				</ComplexOutput>
			</Output>

			<Output minOccurs="0" maxOccurs="1">
				<ows:Identifier>$output_owc$</ows:Identifier>
				<ows:Title>OWS Context</ows:Title>
				<ows:Abstract>
					An OWS Context document describing the contents of the output GeoPackage.
					May be slightly different than the input OWS Context document.
				</ows:Abstract>
				<ComplexOutput>
					<Default>
						<Format>
							<MimeType>application/atom+xml</MimeType>
						</Format>
					</Default>
					<Supported>
						<Format>
							<MimeType>application/atom+xml</MimeType>
						</Format>
					</Supported>
				</ComplexOutput>
			</Output>
		</ProcessOutputs>

	</ProcessDescription>

</ProcessDescriptions>
