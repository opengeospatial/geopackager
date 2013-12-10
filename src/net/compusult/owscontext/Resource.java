/*
 * Resource.java
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;

public class Resource {

	private final ContextDoc context;
	private String id;
	private TypedText title;
	private TypedText abstrakt;
	private Date updateDate = new Date();
	private final List<AuthorInfo> authors;
	private String publisher;
	private String rights;
	private Object geospatialExtent;	// either a wes.restricted.Geometry or a ccps.obj...Geometry, depending on the environment
	private DateTimeInterval temporalExtent;
	private TypedLink contentDescription;		// URI etc.
	private TypedLink preview;		// URI etc.
	private final List<TypedLink> contentByRefs;
	private final List<Offering> offerings;
	private boolean active = true;
	private final Set<CategorizedTerm> keywords;
	private Double minScaleDenominator;
	private Double maxScaleDenominator;
	private String folder;
	private String folderLabel;
	private final List<Node> extensions;
	
	public Resource(ContextDoc context) {
		this.context = context;
		this.updateDate = new Date();
		this.authors = new ArrayList<AuthorInfo>();
		this.contentByRefs = new ArrayList<TypedLink>();
		this.offerings = new ArrayList<Offering>();
		this.active = true;
		this.keywords = new HashSet<CategorizedTerm>();
		this.extensions = new ArrayList<Node>();
	}

	public ContextDoc getContext() {
		return context;
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

	public TypedText getAbstrakt() {
		return abstrakt;
	}

	public void setAbstrakt(TypedText abstrakt) {
		this.abstrakt = abstrakt;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public List<AuthorInfo> getAuthors() {
		return authors;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public Object getGeospatialExtent() {
		return geospatialExtent;
	}

	public void setGeospatialExtent(Object geospatialExtent) {
		this.geospatialExtent = geospatialExtent;
	}

	public DateTimeInterval getTemporalExtent() {
		return temporalExtent;
	}

	public void setTemporalExtent(DateTimeInterval temporalExtent) {
		this.temporalExtent = temporalExtent;
	}

	public TypedLink getContentDescription() {
		return contentDescription;
	}

	public void setContentDescription(TypedLink contentDescription) {
		this.contentDescription = contentDescription;
	}

	public TypedLink getPreview() {
		return preview;
	}

	public void setPreview(TypedLink preview) {
		this.preview = preview;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Double getMinScaleDenominator() {
		return minScaleDenominator;
	}

	public void setMinScaleDenominator(Double minScaleDenominator) {
		this.minScaleDenominator = minScaleDenominator;
	}

	public Double getMaxScaleDenominator() {
		return maxScaleDenominator;
	}

	public void setMaxScaleDenominator(Double maxScaleDenominator) {
		this.maxScaleDenominator = maxScaleDenominator;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getFolderLabel() {
		return folderLabel;
	}

	public void setFolderLabel(String folderLabel) {
		this.folderLabel = folderLabel;
	}

	public List<Offering> getOfferings() {
		return offerings;
	}

	public Set<CategorizedTerm> getKeywords() {
		return keywords;
	}

	public List<TypedLink> getContentByRefs() {
		return contentByRefs;
	}

	public List<Node> getExtensions() {
		return extensions;
	}
	
	@Override
	public String toString() {
		return "Resource [id=" + id + ", title=" + title + ", abstrakt="
				+ abstrakt + ", updateDate=" + updateDate + ", authors="
				+ authors + ", publisher=" + publisher + ", rights=" + rights
				+ ", geospatialExtent=" + geospatialExtent
				+ ", temporalExtent=" + temporalExtent
				+ ", contentDescription=" + contentDescription + ", preview="
				+ preview + ", contentByRefs=" + contentByRefs + ", offerings="
				+ offerings + ", active=" + active + ", keywords=" + keywords
				+ ", minScaleDenominator=" + minScaleDenominator
				+ ", maxScaleDenominator=" + maxScaleDenominator + ", folder="
				+ folder + ", folderLabel=" + folderLabel + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((abstrakt == null) ? 0 : abstrakt.hashCode());
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((authors == null) ? 0 : authors.hashCode());
		result = prime * result
				+ ((contentByRefs == null) ? 0 : contentByRefs.hashCode());
		result = prime
				* result
				+ ((contentDescription == null) ? 0 : contentDescription
						.hashCode());
		result = prime * result + ((folder == null) ? 0 : folder.hashCode());
		result = prime * result
				+ ((folderLabel == null) ? 0 : folderLabel.hashCode());
		result = prime
				* result
				+ ((geospatialExtent == null) ? 0 : geospatialExtent.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((keywords == null) ? 0 : keywords.hashCode());
		result = prime
				* result
				+ ((maxScaleDenominator == null) ? 0 : maxScaleDenominator
						.hashCode());
		result = prime
				* result
				+ ((minScaleDenominator == null) ? 0 : minScaleDenominator
						.hashCode());
		result = prime * result
				+ ((offerings == null) ? 0 : offerings.hashCode());
		result = prime * result + ((preview == null) ? 0 : preview.hashCode());
		result = prime * result
				+ ((publisher == null) ? 0 : publisher.hashCode());
		result = prime * result + ((rights == null) ? 0 : rights.hashCode());
		result = prime * result
				+ ((temporalExtent == null) ? 0 : temporalExtent.hashCode());
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
		Resource other = (Resource) obj;
		if (abstrakt == null) {
			if (other.abstrakt != null)
				return false;
		} else if (!abstrakt.equals(other.abstrakt))
			return false;
		if (active != other.active)
			return false;
		if (authors == null) {
			if (other.authors != null)
				return false;
		} else if (!authors.equals(other.authors))
			return false;
		if (contentByRefs == null) {
			if (other.contentByRefs != null)
				return false;
		} else if (!contentByRefs.equals(other.contentByRefs))
			return false;
		if (contentDescription == null) {
			if (other.contentDescription != null)
				return false;
		} else if (!contentDescription.equals(other.contentDescription))
			return false;
		if (folder == null) {
			if (other.folder != null)
				return false;
		} else if (!folder.equals(other.folder))
			return false;
		if (folderLabel == null) {
			if (other.folderLabel != null)
				return false;
		} else if (!folderLabel.equals(other.folderLabel))
			return false;
		if (geospatialExtent == null) {
			if (other.geospatialExtent != null)
				return false;
		} else if (!geospatialExtent.equals(other.geospatialExtent))
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
		if (maxScaleDenominator == null) {
			if (other.maxScaleDenominator != null)
				return false;
		} else if (!maxScaleDenominator.equals(other.maxScaleDenominator))
			return false;
		if (minScaleDenominator == null) {
			if (other.minScaleDenominator != null)
				return false;
		} else if (!minScaleDenominator.equals(other.minScaleDenominator))
			return false;
		if (offerings == null) {
			if (other.offerings != null)
				return false;
		} else if (!offerings.equals(other.offerings))
			return false;
		if (preview == null) {
			if (other.preview != null)
				return false;
		} else if (!preview.equals(other.preview))
			return false;
		if (publisher == null) {
			if (other.publisher != null)
				return false;
		} else if (!publisher.equals(other.publisher))
			return false;
		if (rights == null) {
			if (other.rights != null)
				return false;
		} else if (!rights.equals(other.rights))
			return false;
		if (temporalExtent == null) {
			if (other.temporalExtent != null)
				return false;
		} else if (!temporalExtent.equals(other.temporalExtent))
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

}
