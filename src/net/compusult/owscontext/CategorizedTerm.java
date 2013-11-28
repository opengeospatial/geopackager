/*
 * Copyright (c) 2013 Unpublished, Compusult Limited.  All Rights Reserved.
 *
 * This software contains proprietary and confidential information of
 * Compusult Limited and its suppliers.  Use, disclosure or reproduction is
 * prohibited without the prior express written consent of Compusult Limited.
 */

package net.compusult.owscontext;

public class CategorizedTerm {

	private String scheme;			// URI
	private String term;
	private String label;
	
	public CategorizedTerm() {
	}

	public CategorizedTerm(String scheme, String term, String label) {
		this.scheme = scheme;
		this.term = term;
		this.label = label;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "CategorizedTerm [scheme=" + scheme + ", term=" + term + ", label=" + label + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
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
		CategorizedTerm other = (CategorizedTerm) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (scheme == null) {
			if (other.scheme != null)
				return false;
		} else if (!scheme.equals(other.scheme))
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}
	
}
