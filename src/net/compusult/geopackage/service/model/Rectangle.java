/*
 * Rectangle.java
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
   
package net.compusult.geopackage.service.model;

public class Rectangle {

	public double llx, lly, urx, ury;

	public Rectangle() {
		this(-180, -90, 180, 90);
	}

	public Rectangle(double llx, double lly, double urx, double ury) {
		this.llx = llx;
		this.lly = lly;
		this.urx = urx;
		this.ury = ury;
	}

	public void setFrom(Rectangle r) {
		this.llx = r.llx;
		this.lly = r.lly;
		this.urx = r.urx;
		this.ury = r.ury;
	}
	
}
