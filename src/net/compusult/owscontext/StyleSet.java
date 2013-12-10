/*
 * StyleSet.java
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
   
package net.compusult.owscontext;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;


public class StyleSet {

	private String name;
	private String title;
	private String abstrakt;
	private boolean defalt;
	private String legendURL;		// URI
	private Content content;
	private final List<Node> extensions;
	
	public StyleSet() {
		this.defalt = false;
		this.extensions = new ArrayList<Node>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstrakt() {
		return abstrakt;
	}

	public void setAbstrakt(String abstrakt) {
		this.abstrakt = abstrakt;
	}

	public boolean getDefalt() {
		return defalt;
	}

	public void setDefalt(boolean defalt) {
		this.defalt = defalt;
	}

	public String getLegendURL() {
		return legendURL;
	}

	public void setLegendURL(String legendURL) {
		this.legendURL = legendURL;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public List<Node> getExtensions() {
		return extensions;
	}
	
	@Override
	public String toString() {
		return "StyleSet [name=" + name + ", title=" + title + ", abstrakt="
				+ abstrakt + ", defalt=" + defalt + ", legendURL=" + legendURL
				+ ", content=" + content + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((abstrakt == null) ? 0 : abstrakt.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (defalt ? 1231 : 1237);
		result = prime * result
				+ ((legendURL == null) ? 0 : legendURL.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		StyleSet other = (StyleSet) obj;
		if (abstrakt == null) {
			if (other.abstrakt != null)
				return false;
		} else if (!abstrakt.equals(other.abstrakt))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (defalt != other.defalt)
			return false;
		if (legendURL == null) {
			if (other.legendURL != null)
				return false;
		} else if (!legendURL.equals(other.legendURL))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
}
