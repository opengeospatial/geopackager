/*
 * Copyright (c) 2013 Unpublished, Compusult Limited.  All Rights Reserved.
 *
 * This software contains proprietary and confidential information of
 * Compusult Limited and its suppliers.  Use, disclosure or reproduction is
 * prohibited without the prior express written consent of Compusult Limited.
 */

package net.compusult.owscontext;

public class AuthorInfo {

	private String name = "";
	private String email = "";
	private String uri = "";
	
	public AuthorInfo() {
	}
	
	public AuthorInfo(String name, String email, String uri) {
		this.name = name;
		this.email = email;
		this.uri = uri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public boolean isEmpty() {
		return "".equals(name) && "".equals(email) && "".equals(uri);
	}

	@Override
	public String toString() {
		return "AuthorInfo [name=" + name + ", email=" + email + ", uri=" + uri + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthorInfo other = (AuthorInfo) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
	
}
