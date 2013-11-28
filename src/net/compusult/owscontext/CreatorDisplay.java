/*
 * CreatorDisplay.java
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

public class CreatorDisplay {

	private Integer pixelWidth;
	private Integer pixelHeight;
	private Double mmPerPixel;
	private final List<Node> extensions;
	
	public CreatorDisplay(Integer pixelWidth, Integer pixelHeight) {
		this.pixelWidth = pixelWidth;
		this.pixelHeight = pixelHeight;
		this.extensions = new ArrayList<Node>();
	}

	public Integer getPixelWidth() {
		return pixelWidth;
	}

	public void setPixelWidth(Integer pixelWidth) {
		this.pixelWidth = pixelWidth;
	}

	public Integer getPixelHeight() {
		return pixelHeight;
	}

	public void setPixelHeight(Integer pixelHeight) {
		this.pixelHeight = pixelHeight;
	}

	public Double getMmPerPixel() {
		return mmPerPixel;
	}

	public void setMmPerPixel(Double mmPerPixel) {
		this.mmPerPixel = mmPerPixel;
	}

	@Override
	public String toString() {
		return "CreatorDisplay [pixelWidth=" + pixelWidth + ", pixelHeight="
				+ pixelHeight + ", mmPerPixel=" + mmPerPixel + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mmPerPixel == null) ? 0 : mmPerPixel.hashCode());
		result = prime * result
				+ ((pixelHeight == null) ? 0 : pixelHeight.hashCode());
		result = prime * result
				+ ((pixelWidth == null) ? 0 : pixelWidth.hashCode());
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
		CreatorDisplay other = (CreatorDisplay) obj;
		if (mmPerPixel == null) {
			if (other.mmPerPixel != null)
				return false;
		} else if (!mmPerPixel.equals(other.mmPerPixel))
			return false;
		if (pixelHeight == null) {
			if (other.pixelHeight != null)
				return false;
		} else if (!pixelHeight.equals(other.pixelHeight))
			return false;
		if (pixelWidth == null) {
			if (other.pixelWidth != null)
				return false;
		} else if (!pixelWidth.equals(other.pixelWidth))
			return false;
		return true;
	}
	
	public List<Node> getExtensions() {
		return extensions;
	}
}
