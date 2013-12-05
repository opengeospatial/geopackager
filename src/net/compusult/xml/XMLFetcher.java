/*
 * XMLFetcher.java
 * 
 * Copyright 2013, Compusult Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
   
package net.compusult.xml;

import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class XMLFetcher {
	
	private static final Logger LOG = Logger.getLogger(XMLFetcher.class);

	private final String url;
	private Schema schema;
	
	public XMLFetcher(String url) {
		this.url = url;
		this.schema = null;
	}
	
	public void setSchema(String schemaLocation) {
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			this.schema = sf.newSchema(new URL(schemaLocation));
		} catch (Exception e) {
			LOG.warn("Failed to retrieve schema from " + schemaLocation + ", ignoring", e);
		}
	}

	public Document fetch() throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		if (schema != null) {
			dbf.setSchema(schema);
		}
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(url);
		} catch (Exception e) {
			throw new IOException("Parsing remote XML file at " + url, e);
		}

	}
}
