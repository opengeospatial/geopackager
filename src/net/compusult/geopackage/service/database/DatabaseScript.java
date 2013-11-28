/*
 * DatabaseScript.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DatabaseScript {

	private final List<String> statements;
	
	public DatabaseScript() {
		this.statements = new ArrayList<String>();
	}
	
	public void readScript(String filename) throws IOException {
		
		StringBuilder stmtBuf = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
		String line;
		
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("--")) {
				// ignore this line
			} else if ("/".equals(line)) {
				// end of statement
				String stmt = massage(stmtBuf.toString());
				if (stmt.length() != 0) {
					statements.add(stmt);
				}
				stmtBuf.setLength(0);	// prep for next one
			} else {
				stmtBuf.append(line).append(' ');
			}
		}
		
		if (stmtBuf.length() > 0) {		// last statement has no trailing slash
			statements.add(massage(stmtBuf.toString()));
		}
	}
	
	private String massage(String statement) {
		statement = statement.trim();
		if (statement.endsWith(";")) {
			statement = statement.substring(0, statement.length() - 1).trim();
		}
		return statement;
	}
	
	public void executeScript(Connection connection, Map<String, String> substitutions) throws SQLException {
		
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			for (String stmt : statements) {
				for (Entry<String, String> entry : substitutions.entrySet()) {
					stmt = stmt.replace("$" + entry.getKey() + "$", entry.getValue());
				}
				
				statement.execute(stmt);
			}
			connection.commit();
			
		} catch (SQLException e) {
			// ignore "not an error" exceptions   >:-(
			if (! e.getMessage().contains("not an error")) {
				throw e;
			}
			
		} finally {
			GeoPackageDAO.cleanUp(null, statement);
		}
	}
	
	public void executeScript(Connection connection) throws SQLException {
		Map<String,String> noSubstitutions = Collections.emptyMap();
		executeScript(connection, noSubstitutions);
	}
	
}
