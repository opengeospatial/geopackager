/*
 * ProgressTracker.java
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
   
package net.compusult.geopackage.service.geopackager;

public class ProgressTracker {

	private int itemCount;
	private int percentComplete;
	private int thisItem;
	
	public ProgressTracker() {
		this.itemCount = 0;
		this.percentComplete = 0;
		this.thisItem = 0;
	}
	
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public synchronized void newItem() {
		++ thisItem;
		this.percentComplete = 0;
	}
	
	public synchronized void setProgress(int percentage) {
		this.percentComplete = percentage;
	}
	
	public synchronized int getPercentComplete() {
		// 75% of the way through item 2 of 3 means (200 + 75) / 3 = 92
		return (int) ((100.0 * thisItem + percentComplete) / itemCount + 0.5);
	}

}
