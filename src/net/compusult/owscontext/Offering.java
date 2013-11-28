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

public class Offering {

	private String offeringCode;		// URI
	private final List<Operation> operations;
	private final List<Content> contents;
	private final List<StyleSet> styles;
	private final List<Node> extensions;
	
	public Offering() {
		this(null);
	}
	
	public Offering(String code) {
		this.offeringCode = code;
		this.operations = new ArrayList<Operation>();
		this.contents = new ArrayList<Content>();
		this.styles = new ArrayList<StyleSet>();
		this.extensions = new ArrayList<Node>();
	}

	public String getOfferingCode() {
		return offeringCode;
	}

	public void setOfferingCode(String offeringCode) {
		this.offeringCode = offeringCode;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public List<Content> getContents() {
		return contents;
	}

	public List<StyleSet> getStyles() {
		return styles;
	}
	
	public List<Node> getExtensions() {
		return extensions;
	}

	@Override
	public String toString() {
		return "Offering [code=" + offeringCode + ", operations="
				+ operations + ", contents=" + contents + ", styles=" + styles
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contents == null) ? 0 : contents.hashCode());
		result = prime * result
				+ ((offeringCode == null) ? 0 : offeringCode.hashCode());
		result = prime * result
				+ ((operations == null) ? 0 : operations.hashCode());
		result = prime * result + ((styles == null) ? 0 : styles.hashCode());
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
		Offering other = (Offering) obj;
		if (contents == null) {
			if (other.contents != null)
				return false;
		} else if (!contents.equals(other.contents))
			return false;
		if (offeringCode == null) {
			if (other.offeringCode != null)
				return false;
		} else if (!offeringCode.equals(other.offeringCode))
			return false;
		if (operations == null) {
			if (other.operations != null)
				return false;
		} else if (!operations.equals(other.operations))
			return false;
		if (styles == null) {
			if (other.styles != null)
				return false;
		} else if (!styles.equals(other.styles))
			return false;
		return true;
	}

}
