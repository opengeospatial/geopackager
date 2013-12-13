/*
 * GeoPackageDAO.java
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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.model.FeatureColumnInfo;
import net.compusult.geopackage.service.model.FeatureColumnInfo.ColumnType;
import net.compusult.geopackage.service.model.GeoPackage;
import net.compusult.geopackage.service.model.LayerInformation;
import net.compusult.geopackage.service.model.LayerInformation.Type;
import net.compusult.geopackage.service.model.Rectangle;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class GeoPackageDAO {	
	private final static Logger LOG = Logger.getLogger(GeoPackageDAO.class);

	private final DatabaseScript createGeneralScript;
	private final DatabaseScript createTilesetScript;
	private final DatabaseScript deleteTilesetScript;
	private final DatabaseScript deleteFeatureLayerScript;
	private final DatabaseScript createFeatureMetadataScript;

	private Connection connection = null;
	private File fileLocation = null;
	private String passPhrase = null;
	private boolean secure = false;


	public GeoPackageDAO() throws IOException {
		this.createGeneralScript = new DatabaseScript();
		createGeneralScript.readScript("scripts/create_general.sql");

		this.createTilesetScript = new DatabaseScript();
		createTilesetScript.readScript("scripts/create_tile_layer.sql");

		this.deleteTilesetScript = new DatabaseScript();
		deleteTilesetScript.readScript("scripts/delete_tile_layer.sql");

		this.deleteFeatureLayerScript = new DatabaseScript();
		deleteFeatureLayerScript.readScript("scripts/delete_feature_layer.sql");

		this.createFeatureMetadataScript = new DatabaseScript();
		createFeatureMetadataScript.readScript("scripts/create_feature_metadata.sql");
	}

	public void setFileLocation(File fileLocation) {
		this.fileLocation = fileLocation;
	}

	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
		this.secure = passPhrase != null && !"".equals(passPhrase);
	}

	public void createGeneralTables() throws GeoPackageException {
		try {
			if (!hasTable("gpkg_contents")) {
				createGeneralScript.executeScript(getConnection());
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create general tables", e);
		}
	}

	public List<LayerInformation> getLayers(GeoPackage gpkg) throws GeoPackageException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<LayerInformation> layers = new ArrayList<LayerInformation>();
		
		try {
			ps = getConnection().prepareStatement("SELECT table_name, data_type, identifier, description FROM gpkg_contents");
			rs = ps.executeQuery();
			
			while(rs.next()){
				LayerInformation layer = new LayerInformation(gpkg, Type.fromXmlType(rs.getString(2)), rs.getString(1));
				layer.setTitle(rs.getString(3));
				layer.setDescription(rs.getString(4));

				layers.add(layer);
			}			
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create tile table metadata", e);
		} finally {
			cleanUp(rs, ps);
		}
		
		return layers;
	}

	public void createTileLayer(LayerInformation layerInfo) throws GeoPackageException {

		String tableName = layerInfo.getTableName();
		createTilesetTables(tableName);

		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement("SELECT table_name FROM gpkg_contents WHERE table_name=?");
			ps.setString(1, tableName);
			ps.executeQuery();
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create tile table metadata", e);
		} finally {
			cleanUp(null, ps);
		}

		updateLayerInTOC(layerInfo);
	}

	public void createTilesetTables(String tableName) throws GeoPackageException {

		try {
			if (!hasTileset(tableName)) {
				createTilesetScript.executeScript(getConnection(), Collections.singletonMap("TABLENAME", tableName));
			}

		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create tileset tables", e);
		}
	}

	public boolean hasTileset(String tableName) throws GeoPackageException{
		return hasTable(tableName);
	}

	public void deleteTileLayer(LayerInformation layerInfo) throws GeoPackageException {

		try {
			deleteTilesetScript.executeScript(getConnection(), Collections.singletonMap("TABLENAME", layerInfo.getTableName()));
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to delete tileset", e);
		}
	}

	public void createTileMatrixZoomLevel(String tableName,
			int zoomScale, int matrixWidth, int matrixHeight,
			int tileWidth, int tileHeight, double xSize, double ySize) throws GeoPackageException {

		PreparedStatement ps = null;
		try {
			String sql = "INSERT INTO gpkg_tile_matrix (table_name, zoom_level, " +
					"matrix_width, matrix_height, tile_width, tile_height, pixel_x_size, pixel_y_size) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			
			ps = getConnection().prepareStatement(sql);

			ps.setString(1, tableName);
			ps.setInt(2, zoomScale);
			ps.setInt(3, matrixWidth);
			ps.setInt(4, matrixHeight);
			ps.setInt(5, tileWidth);
			ps.setInt(6, tileHeight);
			ps.setDouble(7, xSize);
			ps.setDouble(8, ySize);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create tile matrix metadata", e);

		} finally {
			cleanUp(null, ps);
		}
	}
	
	public void createTileMatrixSet(String tableName, double minX, double minY, double maxX, double maxY) 
			throws GeoPackageException {
		
		PreparedStatement ps = null;
		try {
			String sql = "INSERT INTO gpkg_tile_matrix_set (table_name, srs_id, min_x, min_y, max_x, max_y)"
					+ " VALUES (?, (SELECT srs_id FROM gpkg_contents WHERE table_name=?), ?, ?, ?, ?)";
			ps = getConnection().prepareStatement(sql);

			ps.setString(1, tableName);
			ps.setString(2, tableName);
			ps.setDouble(3, minX);
			ps.setDouble(4, minY);
			ps.setDouble(5, maxX);
			ps.setDouble(6, maxY);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create tile matrix metadata", e);

		} finally {
			cleanUp(null, ps);
		}
	}

	public void createTile(String tableName, int zoomScale, int tileCol, int tileRow, byte[] tileData) throws GeoPackageException {

		PreparedStatement ps = null;
		String sql = null;
		try {
			sql = "INSERT INTO " + tableName + " (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)";
			ps = getConnection().prepareStatement(sql);

			ps.setInt(1, zoomScale);
			ps.setInt(2, tileCol);
			ps.setInt(3, tileRow);
			ps.setBytes(4, tileData);						
			ps.executeUpdate();
		} catch (SQLException e) {
			LOG.error("=================================");
			LOG.error("===== Failed to create tile");
			LOG.error("===== zoomScale: " + zoomScale);
			LOG.error("===== tileCol: " + tileCol);
			LOG.error("===== tileRow: " + tileRow);
			LOG.error("===== tileData: " + tileData);
			LOG.error("=================================");
			
			throw new GeoPackageException("Failed to create tile", e);

		} finally {
			cleanUp(null, ps);
		}
	}

	public void deleteFeatureLayer(LayerInformation layerInfo) throws GeoPackageException {
		try {
			deleteFeatureLayerScript.executeScript(getConnection(), Collections.singletonMap("TABLENAME", layerInfo.getTableName()));
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to delete feature layer", e);
		}
	}

	public void createFeatureTableMetadata(String tableName) throws GeoPackageException {

		try {
			if (! hasTable(tableName + "_rt_metadata")) {
				createFeatureMetadataScript.executeScript(getConnection(),
						Collections.singletonMap("FEATURE_TABLENAME", tableName));
			}

		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create feature table metadata", e);
		}
	}
	
	/**
	 * Add a single vector feature record to the given table.  Geometry values are always inserted relative to the EPSG:4326 coordinate
	 * system; that is, as lats and longs.
	 * TODO support other CRSs
	 * @param srid TODO
	 */
	public void addVectorFeature(String tableName, Map<String, String> fields, Set<String> geometryColumnNames, String srid) throws GeoPackageException {
		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(tableName).append('(');
		sql.append(StringUtils.collectionToCommaDelimitedString(fields.keySet()));
		sql.append(") VALUES (");
		boolean first = true;
		for (Entry<String, String> entry : fields.entrySet()) {
			if (!first) {
				sql.append(',');
			}
			first = false;
			if (geometryColumnNames.contains(entry.getKey())) {
				sql.append("ST_GeomFromText(?,(SELECT srs_id FROM gpkg_spatial_ref_sys WHERE upper(organization)='EPSG' AND organization_coordsys_id=" + srid + "))");
			} else {
				sql.append('?');
			}
		}
		sql.append(')');

		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(sql.toString());

			int i = 1;
			for (Entry<String, String> entry : fields.entrySet()) {
				ps.setString(i++, entry.getValue());
			}
			ps.executeUpdate();
			ps.close();

		} catch (SQLException e) {
			throw new GeoPackageException("Failed to add vector feature", e);

		} finally {
			cleanUp(null, ps);
		}
	}


	public void createGeometryColumn(String tableName, String columnName, String geometryType, boolean notNull, String srid) throws GeoPackageException {
		
		if (srid.startsWith("EPSG:")) {
			srid = srid.substring(5);
		}
		ensureSridExists(srid);

		PreparedStatement ps = null;
		try {
			String sql = "SELECT AddGeometryColumn(?, ?, ?, (SELECT srs_id FROM gpkg_spatial_ref_sys WHERE upper(organization)='EPSG' AND organization_coordsys_id=" + srid + "));";

			ps = getConnection().prepareStatement(sql);
			ps.setString(1, tableName);
			ps.setString(2, columnName);
			ps.setString(3, geometryType);
			ps.executeQuery();
			ps.close();

			ps = getConnection().prepareStatement("SELECT CreateSpatialIndex(?, ?, ?);");
			ps.setString(1, tableName);
			ps.setString(2, columnName);
			ps.setString(3, "id"); //For now there has to be a column titled id that is the primary key
			ps.executeQuery();

		} catch (SQLException e) {
			System.out.println(e);
			throw new GeoPackageException("Failed to create geometry column", e);

		} finally {
			cleanUp(null, ps);
		}

	}

	public String getGeometryColumnType(String tableName, String columnName) throws GeoPackageException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// 'geometry_type' is the name of the geometry type; e.g. LINESTRING
			ps = getConnection().prepareStatement("SELECT geometry_type_name FROM gpkg_geometry_columns where table_name=? and column_name=?");
			ps.setString(1, tableName);
			ps.setString(2, columnName);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
			return null;

		} catch (SQLException e) {
			throw new GeoPackageException("Failed to query geometry_columns regarding " + tableName + "." + columnName, e);

		} finally {
			cleanUp(rs, ps);
		}
	}

	public boolean isGeometryColumn(String tableName, String columnName) throws GeoPackageException {
		return getGeometryColumnType(tableName, columnName) != null;
	}

	public void updateLayerInTOC(LayerInformation layerInfo) throws GeoPackageException {

		String sql;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		String srid = layerInfo.getCrs();
		if (srid.startsWith("EPSG:")) {
			srid = srid.substring(5);
		}
		ensureSridExists(srid);

		try {
			boolean exists = false;
			String tableName = layerInfo.getTableName();

			ps = getConnection().prepareStatement("SELECT table_name FROM gpkg_contents WHERE table_name=?");
			ps.setString(1, tableName);
			rs = ps.executeQuery();
			if (rs.next()) {
				exists = true;
			}
			cleanUp(rs, ps);
			rs = null;
			ps = null;

			if (exists) {
				sql = "UPDATE gpkg_contents SET description=?, min_x=?, min_y=?, max_x=?, max_y=?, identifier=?, data_type=?, srs_id=(SELECT srs_id FROM gpkg_spatial_ref_sys WHERE upper(organization)='EPSG' AND organization_coordsys_id=" + srid + ") WHERE table_name=?";
			} else {
				sql = "INSERT INTO gpkg_contents (description, min_x, min_y, max_x, max_y, srs_id, identifier, data_type, table_name) "
						+ "VALUES (?,?,?,?,?,(SELECT srs_id FROM gpkg_spatial_ref_sys WHERE upper(organization)='EPSG' AND organization_coordsys_id=" + srid + "),?,?,?)";
			}
			ps = getConnection().prepareStatement(sql);
			ps.setString(1, layerInfo.getTheAbstract());
			Rectangle r = layerInfo.getBoundsRectangle();
			ps.setDouble(2, r.getLlx());
			ps.setDouble(3, r.getLly());
			ps.setDouble(4, r.getUrx());
			ps.setDouble(5, r.getUry());
			ps.setString(6, layerInfo.getTitle());
			ps.setString(7, layerInfo.getType().getXmlType());
			ps.setString(8, tableName);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create/update layer in TOC", e);
		} finally {
			cleanUp(rs, ps);
		}
	}

	public void deleteLayerFromTOC(String fullTableName) throws GeoPackageException {

		PreparedStatement ps = null;

		try {
			ps = getConnection().prepareStatement("DELETE FROM geopackage_contents where table_name=?");
			ps.setString(1, fullTableName);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new GeoPackageException("Failed to delete layer from TOC", e);
		} finally {
			cleanUp(null, ps);
		}
	}

	public void createFeatureTable(String tableName, LayerInformation layerInfo, List<FeatureColumnInfo> columns) throws GeoPackageException {
		List<FeatureColumnInfo> geomColumns = new ArrayList<FeatureColumnInfo>();

		StringBuilder buf = new StringBuilder("CREATE TABLE ");
		buf.append(tableName).append(" (");
		boolean first = true;
		for (FeatureColumnInfo col : columns) {
			if (col.getType() == ColumnType.GEOMETRY) {
				geomColumns.add(col);

			} else {
				if (!first) {
					buf.append(", ");
				}

				buf.append(col.getName()).append(' ').append(col.getType().getLabel());
				if (first && col.isPriKey()) {
					buf.append(" primary key");
				}
				if (col.isNotNull()) {
					buf.append(" not null");
				}
			}
			first = false;
		}
		buf.append(')');

		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(buf.toString());
			ps.executeUpdate();
			ps.close();

			for (FeatureColumnInfo col : geomColumns) {
				createGeometryColumn(tableName, col.getName(), col.getGeometryType(), col.isNotNull(), layerInfo.getCrs());
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create table " + tableName, e);

		} finally {
			cleanUp(null, ps);
		}

		updateLayerInTOC(layerInfo);

		createFeatureTableMetadata(tableName);
	}
	
	public boolean hasTable(String tableName) throws GeoPackageException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = getConnection().prepareStatement("select count(*) from sqlite_master where type='table' and upper(name)=?");
			ps.setString(1, tableName.toUpperCase());
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) == 1;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Couldn't query sqlite_master: " + e.getMessage(), e);
		}
		finally {
			cleanUp(rs, ps);
		}
		return false;
	}

	public boolean hasColumn(String tableName, String columnName) throws GeoPackageException {
		if (!hasTable(tableName)) {
			return false;
		}

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = getConnection().prepareStatement("pragma table_info(" + tableName + ")");
			rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getString("name").toUpperCase().equals(columnName)) {
					return true;
				}
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Couldn't query table info: " + e.getMessage(), e);
		}
		finally {
			cleanUp(rs, ps);
		}
		return false;
	}

	private static final Pattern COLUMN_DEFS = Pattern.compile("^\\s*[^(]+[(](.*)[)]\\s*$", Pattern.MULTILINE);
	private static final Pattern ONE_COLUMN = Pattern.compile("^\\s*[\"]?(\\w+)[\"]?(\\s+(\\w+))?(\\s+primary\\s+key)?(\\s+not\\s+null)?(\\s+default\\s+.*)?$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

	/*
	 * This is horrible.  We _should_ just be able to execute this query:
	 * 		pragma table_info(" + tableName + ")
	 * but that fails on the executeQuery call, complaining "query does not return results".
	 * So the gruesome hack is to parse the table definition available in the sqlite_master
	 * table.
	 */
	public List<DatabaseColumnInfo> readColumnInfo(String tableName) throws GeoPackageException {

		List<DatabaseColumnInfo> result = new ArrayList<DatabaseColumnInfo>();

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = getConnection().prepareStatement("select sql from sqlite_master where type='table' and name=?");
			ps.setString(1, tableName);
			rs = ps.executeQuery();
			if (rs.next()) {
				String sql = rs.getString(1);
				Matcher m = COLUMN_DEFS.matcher(sql);
				if (!m.matches()) {
					throw new IllegalStateException("Coding error: failed to match table definition");
				}
				sql = m.group(1);
				String[] columnDefs = sql.split(",");
				for (String column : columnDefs) {
					column = column.trim();

					m = ONE_COLUMN.matcher(column);
					if (! m.matches()) {
						throw new IllegalStateException("Coding error: failed to match column definition");
					}

					String name = m.group(1);
					String type = m.group(3) == null ? ColumnType.TEXT.getLabel() : m.group(3);
					boolean priKey = m.group(4) != null;
					boolean notNull = m.group(5) != null;
					result.add(new DatabaseColumnInfo(name, type, priKey, notNull));
				}
			}
			return result;

		} catch (SQLException e) {
			throw new GeoPackageException("Couldn't query table info: " + e.getMessage(), e);
		}
		finally {
			cleanUp(rs, ps);
		}
	}

	public List<FeatureColumnInfo> parseFeatureColumns(String tableName)
			throws GeoPackageException {
		List<FeatureColumnInfo> featureColumns = new ArrayList<FeatureColumnInfo>();

		for (DatabaseColumnInfo databaseColumn : readColumnInfo(tableName)) {
			String geometryType = null;
			String name = databaseColumn.getName();
			ColumnType type = ColumnType.fromLabel(databaseColumn.getType());
			if (type == ColumnType.BLOB) {
				
					geometryType = getGeometryColumnType(tableName, name);
					if (geometryType != null) {
						type = ColumnType.GEOMETRY;
					}
			} else if (type == ColumnType.GEOMETRY) {
				geometryType = getGeometryColumnType(tableName, name);
			}

			featureColumns.add(new FeatureColumnInfo(type, name, databaseColumn.isPriKey(),
					databaseColumn.isNotNull(), geometryType));
		}

		return featureColumns;
	}

	private static final Map<String, String> EPSG_TRANSLATIONS;
	static {
		EPSG_TRANSLATIONS = new HashMap<String, String>();
		EPSG_TRANSLATIONS.put("epsg/3857", "sr-org/6864");		// ugh!
	}
	
	/**
	 * If the spatial reference system with the given EPSG code does not exist in the
	 * gpkg_spatial_ref_sys table, then fetch its definition from spatialreference.org
	 * and insert it.
	 * 
	 * @param srid
	 * @throws GeoPackageException
	 */
	private void ensureSridExists(String srid) throws GeoPackageException {
		
		String sql;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		try {
			ps = getConnection().prepareStatement("SELECT 1 FROM gpkg_spatial_ref_sys WHERE upper(organization)='EPSG' AND organization_coordsys_id=?");
			ps.setString(1, srid);
			rs = ps.executeQuery();
			if (rs.next()) {
				return;			// nothing to do
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to query for SRS EPSG:" + srid, e);
		} finally {
			cleanUp(rs, ps);
			rs = null;
			ps = null;
		}
		
		BufferedReader reader = null;
		try {
			String expectedPath = "epsg/" + srid;
			String realPath = EPSG_TRANSLATIONS.get(expectedPath);
			if (realPath == null) {
				realPath = expectedPath;
			}
			URL epsgUrl = new URL("http://spatialreference.org/ref/" + realPath + "/ogcwkt/");
			StringBuilder content = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(epsgUrl.openStream()));
			char[] buf = new char[1024];
			int n;
			while ((n = reader.read(buf)) > 0) {
				content.append(buf, 0, n);
			}
			
			sql = "INSERT INTO gpkg_spatial_ref_sys(srs_name, srs_id, organization, organization_coordsys_id, definition, description) "
					+ "VALUES(?, ?, 'EPSG', ?, ?, ?)";
			String srsName = "EPSG " + srid;
			
	
			ps = getConnection().prepareStatement(sql);
			ps.setString(1, srsName);
			ps.setString(2, srid);
			ps.setString(3, srid);
			ps.setString(4, content.toString());
			ps.setString(5, srsName);
			ps.executeUpdate();

		} catch (Exception e) {
			throw new GeoPackageException("Failed to create/update layer in TOC", e);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
			cleanUp(rs, ps);
		}
	}

	public void dropTable(String tableName) throws GeoPackageException {
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement("DROP TABLE " + tableName);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new GeoPackageException("Failed to drop table " + tableName, e);

		} finally {
			cleanUp(null, ps);
		}
	}

	public boolean executeSQL(String sql) {
		Statement statement = null;

		try {
			statement = getConnection().createStatement();
			statement.execute(sql);
			return true;
		} catch (SQLException e) {
			LOG.error("Failed to execute sql \"" + sql + "\"", e);
			return false;
		}
		finally {
			cleanUp(null, statement);
		}
	}

	public void commit() throws GeoPackageException {
		try {
			// don't call getConnection - if there isn't already a connection open
			// then we don't want to create a new one just to call commit on it!
			if (connection != null) {
				connection.commit();
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Couldn't commit", e);
		}
	}

	public void rollback() throws GeoPackageException {
		try {
			// don't call getConnection - if there isn't already a connection open
			// then we don't want to create a new one just to call commit on it!
			if (connection != null) {
				connection.rollback();
			}
		} catch (SQLException e) {
			System.err.println("Couldn't roll back: " + e);
		}
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
			}
			connection = null;
		}
	}

	private Connection getConnection() throws SQLException {
		if (connection == null && fileLocation != null) {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("org.sqlite.JDBC not found on the classpath!");
			}
			connection = DriverManager.getConnection("jdbc:sqlite:" + fileLocation.getAbsolutePath(), setEnableLoadExtensions(true));

			if (secure) {
				/*
				 * We would like to do this:
				 *		PreparedStatement ps = connection.prepareStatement("PRAGMA key = '?'");
				 *		ps.setString(1, passPhrase);
				 * but SQLite throws an ArrayIndexOutOfBoundsException on the setString.
				 */
				String pass = this.passPhrase.replace("'", "''"); //escape quotes per SQLite docs
				Statement ps = connection.createStatement();
				ps.execute("PRAGMA key = '" + pass + "'");
				ps.close();
			}
			
			initializeSpatialite();
			
			// This must be AFTER InitSpatialMetadata (Spatialite 3.0.1 bug)
			connection.setAutoCommit(false);
		}

		return connection;
	}

	/*
	 * Reflectively allocate a SQLiteConfig object, if available, and call
	 * enableLoadExtension(true) on it.  Return the resulting Properties object
	 * for use in the DriverManager.getConnection() call.  It is done using
	 * reflection because SQLiteConfig is not provided by all the known SQLite
	 * JDBC drivers (specifically and most importantly, the Zentus one).  This
	 * way the classpath just needs to be updated with the desired jar and things
	 * will just "work".
	 */
	private Properties setEnableLoadExtensions(boolean enable) {
		try {
			Class<?> configClass = Class.forName("org.sqlite.SQLiteConfig");
			Object configObject = configClass.newInstance();
			Method enableLoadExtension = configClass.getDeclaredMethod("enableLoadExtension", boolean.class);
			enableLoadExtension.invoke(configObject, enable);
			Method toProperties = configClass.getDeclaredMethod("toProperties");
			return (Properties) toProperties.invoke(configClass);
		} catch (Exception e) {
			Properties result = new Properties();
			return result;
		}
	}
	
	private void initializeSpatialite() throws SQLException {
		Statement statement = connection.createStatement();

		try {
			if(System.getProperty("os.name").startsWith("Windows")) {
				statement.execute("SELECT load_extension('libspatialite-4.dll')"); //TODO get libgpkg compiled on Windows
			}
			else {
				statement.execute("SELECT load_extension('" + getClass().getResource("/libgpkg.so").getPath()  + "', 'sqlite3_gpkg_init')");
			}			
			
			statement.execute("SELECT InitSpatialMetadata()");
			
		} finally {
			cleanUp(null, statement);
		}
	}

	public static void cleanUp(ResultSet rs, Statement st) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {}
		}
		if (st != null) {
			try {
				st.close();
			} catch (Exception e) {}
		}
	}
}
