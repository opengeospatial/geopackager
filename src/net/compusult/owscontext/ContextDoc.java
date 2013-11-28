/*
 * ContextDoc.java
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
import java.util.Date;
import java.util.List;

import org.w3c.dom.Node;

public class ContextDoc {

	private String language;
	private String id;
	private TypedText title;
	private TypedText subtitle;
	private String abstrakt;
	private Date updateDate;
	private final List<AuthorInfo> authors;
	private String publisher;
	private Creator creator;
	private String rights;
	private final List<TypedLink> contextMetadata;		// URI
	private Object areaOfInterest;	// either a wes.restricted.Geometry or a ccps.obj...Geometry, depending on the environment 
	private DateTimeInterval timeIntervalOfInterest;
	private final List<Resource> resources;
	private final List<CategorizedTerm> keywords;
	private final List<Node> extensions;
	
	public ContextDoc() {
		this.language = "en";
		this.updateDate = new Date();
		this.authors = new ArrayList<AuthorInfo>();
		this.contextMetadata = new ArrayList<TypedLink>();
		this.resources = new ArrayList<Resource>();
		this.keywords = new ArrayList<CategorizedTerm>();
		this.extensions = new ArrayList<Node>();
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TypedText getTitle() {
		return title;
	}

	public void setTitle(TypedText title) {
		this.title = title;
	}

	public TypedText getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(TypedText subtitle) {
		this.subtitle = subtitle;
	}

	public String getAbstrakt() {
		return abstrakt;
	}

	public void setAbstrakt(String abstrakt) {
		this.abstrakt = abstrakt;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public Creator getCreator() {
		return creator;
	}

	public void setCreator(Creator creator) {
		this.creator = creator;
	}

	public Object getAreaOfInterest() {
		return areaOfInterest;
	}

	public void setAreaOfInterest(Object areaOfInterest) {
		this.areaOfInterest = areaOfInterest;
	}

	public DateTimeInterval getTimeIntervalOfInterest() {
		return timeIntervalOfInterest;
	}

	public void setTimeIntervalOfInterest(DateTimeInterval timeIntervalOfInterest) {
		this.timeIntervalOfInterest = timeIntervalOfInterest;
	}

	public List<AuthorInfo> getAuthors() {
		return authors;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public List<TypedLink> getContextMetadata() {
		return contextMetadata;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public List<CategorizedTerm> getKeywords() {
		return keywords;
	}

	@Override
	public String toString() {
		return "ContextDoc [language=" + language + ", id=" + id + ", title="
				+ title + ", subtitle=" + subtitle + ", abstrakt=" + abstrakt
				+ ", updateDate=" + updateDate + ", authors=" + authors
				+ ", publisher=" + publisher + ", creator=" + creator
				+ ", rights=" + rights + ", contextMetadata=" + contextMetadata
				+ ", areaOfInterest=" + areaOfInterest
				+ ", timeIntervalOfInterest=" + timeIntervalOfInterest
				+ ", resources=" + resources + ", keywords=" + keywords + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((abstrakt == null) ? 0 : abstrakt.hashCode());
		result = prime * result
				+ ((areaOfInterest == null) ? 0 : areaOfInterest.hashCode());
		result = prime * result + ((authors == null) ? 0 : authors.hashCode());
		result = prime * result
				+ ((contextMetadata == null) ? 0 : contextMetadata.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result
				+ ((language == null) ? 0 : language.hashCode());
		result = prime * result
				+ ((publisher == null) ? 0 : publisher.hashCode());
		result = prime * result
				+ ((resources == null) ? 0 : resources.hashCode());
		result = prime * result + ((rights == null) ? 0 : rights.hashCode());
		result = prime * result
				+ ((subtitle == null) ? 0 : subtitle.hashCode());
		result = prime
				* result
				+ ((timeIntervalOfInterest == null) ? 0
						: timeIntervalOfInterest.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result
				+ ((updateDate == null) ? 0 : updateDate.hashCode());
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
		ContextDoc other = (ContextDoc) obj;
		if (abstrakt == null) {
			if (other.abstrakt != null)
				return false;
		} else if (!abstrakt.equals(other.abstrakt))
			return false;
		if (areaOfInterest == null) {
			if (other.areaOfInterest != null)
				return false;
		} else if (!areaOfInterest.equals(other.areaOfInterest))
			return false;
		if (authors == null) {
			if (other.authors != null)
				return false;
		} else if (!authors.equals(other.authors))
			return false;
		if (contextMetadata == null) {
			if (other.contextMetadata != null)
				return false;
		} else if (!contextMetadata.equals(other.contextMetadata))
			return false;
		if (creator == null) {
			if (other.creator != null)
				return false;
		} else if (!creator.equals(other.creator))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (keywords == null) {
			if (other.keywords != null)
				return false;
		} else if (!keywords.equals(other.keywords))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (publisher == null) {
			if (other.publisher != null)
				return false;
		} else if (!publisher.equals(other.publisher))
			return false;
		if (resources == null) {
			if (other.resources != null)
				return false;
		} else if (!resources.equals(other.resources))
			return false;
		if (rights == null) {
			if (other.rights != null)
				return false;
		} else if (!rights.equals(other.rights))
			return false;
		if (subtitle == null) {
			if (other.subtitle != null)
				return false;
		} else if (!subtitle.equals(other.subtitle))
			return false;
		if (timeIntervalOfInterest == null) {
			if (other.timeIntervalOfInterest != null)
				return false;
		} else if (!timeIntervalOfInterest.equals(other.timeIntervalOfInterest))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (updateDate == null) {
			if (other.updateDate != null)
				return false;
		} else if (!updateDate.equals(other.updateDate))
			return false;
		return true;
	}

	public List<Node> getExtensions() {
		return extensions;
	}

}
