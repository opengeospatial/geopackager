package net.compusult.geopackage.service.resource.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class TemplateManager {
	
	private static final TemplateManager INSTANCE = new TemplateManager();
	
	public static TemplateManager getInstance() {
		return INSTANCE;
	}
	
	public String readTemplate(String name) throws IOException {
		InputStream is = null;
		Reader r = null;
		try {
			is = getClass().getResourceAsStream("templates/" + name);
			if (is == null) {
				
			}
			StringBuilder buf = new StringBuilder();
			
			r = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			char[] inbuf = new char[1024];
			int n;
			while ((n = r.read(inbuf, 0, inbuf.length)) > 0) {
				buf.append(inbuf, 0, n);
			}
			return buf.toString();
			
		} finally {
			if (r != null) {
				try { r.close(); } catch (IOException e) {}
			}
			if (is != null) {
				try { is.close(); } catch (IOException e) {}
			}
		}
	}
	
	public ContactInfo getContactInfo() {
		return new ContactInfo();
	}
	
	public static class ContactInfo {
		public String company = "Compusult Ltd.";
		public String url = "http://www.compusult.net/";
		public String name = "Sean Hogan";
		public String title = "Project Engineer";
		public String email = "sean@compusult.net";
		public String voice = "+1 709 745-7914";
		public String fax = "+1 709 745-7927";
		public String addr = "40 Bannister Street";
		public String city = "Mount Pearl";
		public String prov = "NL";
		public String country = "Canada";
		public String postalCode = "A1N 1W1";
	}
	
}
