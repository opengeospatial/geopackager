/*
 * GeoPackagingPool.java
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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;


import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class GeoPackagingPool implements InitializingBean {
	
	private final Map<String, GeoPackager> running;
	private final AtomicInteger packagerSequence;
	
	private String workDirectory;
	private ExecutorService executor;
	
	public GeoPackagingPool() {
		this.running = new HashMap<String, GeoPackager>();
		this.packagerSequence = new AtomicInteger(1);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		/*
		 * We can't do anything with files already in our work directory, so
		 * get rid of them.
		 */
		for (File existingFile : new File(workDirectory).listFiles()) {
			if (existingFile.isFile()) {
				existingFile.delete();
			}
		}
	}

	public String start(final GeoPackager packager) {
		final String hashed;
		try {
			String now = String.valueOf(Math.abs(packagerSequence.getAndIncrement() ^ System.currentTimeMillis()));
			hashed = DigestUtils.sha256Hex(now.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("JRE doesn't support UTF-8?! Find another one!");
		}
		
		packager.setId(hashed);
		packager.setWorkDirectory(workDirectory);
		running.put(hashed, packager);
		
		executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.currentThread().setName("GeoPackager-" + hashed);
					packager.run();
				} finally {
					Thread.currentThread().setName("GeoPackager-Idle");
				}
			}
		});
		
		return hashed;
	}

	public GeoPackager find(final String id) {
		return running.get(id);
	}
	
	@Required
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	@Required
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}
	
}
