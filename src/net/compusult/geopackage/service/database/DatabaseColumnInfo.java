/*
 * DatabaseColumnInfo.java
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
   
package net.compusult.geopackage.service.database;

public class DatabaseColumnInfo {

	private final String name;
	private final String type;
	private final boolean priKey;
	private final boolean notNull;
	
	public DatabaseColumnInfo(String name, String type, boolean priKey, boolean notNull) {
		super();
		this.name = name;
		this.type = type;
		this.priKey = priKey;
		this.notNull = notNull;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isPriKey() {
		return priKey;
	}

	public boolean isNotNull() {
		return notNull;
	}

}
