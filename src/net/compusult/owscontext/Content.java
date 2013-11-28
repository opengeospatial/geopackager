/*
 * Copyright (c) 2013 Unpublished, Compusult Limited.  All Rights Reserved.
 *
 * This software contains proprietary and confidential information of
 * Compusult Limited and its suppliers.  Use, disclosure or reproduction is
 * prohibited without the prior express written consent of Compusult Limited.
 */

package net.compusult.owscontext;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class Content {

	private String type;
	private String url;			// URI
	private Object actualContent;
	private final List<Node> extensions;
	
	public Content(){
		this.extensions = new ArrayList<Node>();
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Object getActualContent() {
		return actualContent;
	}

	public void setActualContent(Object actualContent) {
		this.actualContent = actualContent;
	}

	@Override
	public String toString() {
		return "Content [type=" + type + ", url=" + url + ", actualContent=" + actualContent + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((actualContent == null) ? 0 : actualContent.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Content other = (Content) obj;
		if (actualContent == null) {
			if (other.actualContent != null)
				return false;
		} else if (!actualContent.equals(other.actualContent))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	public List<Node> getExtensions() {
		return extensions;
	}
	
}
