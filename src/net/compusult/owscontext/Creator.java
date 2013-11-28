/*
 * Creator.java
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


public class Creator {

	private CreatorApplication application;
	private CreatorDisplay display;
	
	/*
	 * 'extensions' is not included because there is no <owc:creator> element in
	 * the Atom encoding (though it looks like there might have been at one time).
	 */
//	private final List<Node> extensions;
	
	public Creator(CreatorApplication application, CreatorDisplay display) {
		this.application = application;
		this.display = display;
//		this.extensions = new ArrayList<Node>();
	}

	public CreatorApplication getApplication() {
		return application;
	}
	
	public void setApplication(CreatorApplication application) {
		this.application = application;
	}
	
	public CreatorDisplay getDisplay() {
		return display;
	}
	
	public void setDisplay(CreatorDisplay display) {
		this.display = display;
	}

	@Override
	public String toString() {
		return "Creator [application=" + application + ", display=" + display + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((display == null) ? 0 : display.hashCode());
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
		Creator other = (Creator) obj;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (display == null) {
			if (other.display != null)
				return false;
		} else if (!display.equals(other.display))
			return false;
		return true;
	}
	
//	public List<Node> getExtensions() {
//		return extensions;
//	}
}
