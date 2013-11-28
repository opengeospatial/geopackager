package net.compusult.geopackage.service.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.compusult.geopackage.service.GeoPackageException;
import net.compusult.geopackage.service.database.GeoPackageDAO;
import net.compusult.geopackage.service.model.LayerInformation.Type;

public class GeoPackage {

	private GeoPackageDAO dao; // our handle to the database

	private String title;
	private String theAbstract;
	private final String identifier;
	private final Rectangle boundsRectangle;
	private final List<LayerInformation> layers;
//	private final OWSContextManifest manifest;	
	private final File file;
	private final char[] password;

	public GeoPackage(File file, char[] password) throws GeoPackageException {
		try {
			this.dao = new GeoPackageDAO();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read database scripts",
					e);
		}
		dao.setFileLocation(file);
		dao.setPassPhrase(new String(password));
		dao.createGeneralTables();
		dao.commit();

		this.file = file;
		this.password = password;
		this.identifier = file.getName();
		this.title = "";
		this.theAbstract = "";
		this.boundsRectangle = new Rectangle();
		this.layers = dao.getLayers(this);
	}

	public File getFile() {
		return file;
	}
	
	public char[] getPassword() {
		return password;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTheAbstract() {
		return theAbstract;
	}

	public void setTheAbstract(String theAbstract) {
		this.theAbstract = theAbstract;
	}

	public String getIdentifier() {
		return identifier;
	}

	public Rectangle getBoundsRectangle() {
		return boundsRectangle;
	}

	public void setBoundsRectangle(Rectangle r) {
		this.boundsRectangle.setFrom(r);
	}

	public List<LayerInformation> getLayers() {
		return layers;
	}

	public boolean hasLayerTable(Type layerType, String tableName)
			throws GeoPackageException {
		if (layerType == Type.TILES) {
			return dao.hasTileset(tableName);
		} else {
			return dao.hasTable(tableName);
		}
	}

	public void newLayer(LayerInformation layerInfo) throws GeoPackageException {
		if(!layers.contains(layerInfo)) {
			if (layerInfo.getType() == Type.TILES) {
				createTilesetTables(layerInfo.getTableName());
			} else {
				// already created
			}
			layers.add(layerInfo);
		}
	}

	public void deleteLayer(int index) throws GeoPackageException {
		LayerInformation layerInfo = layers.get(index);
		if (layerInfo.getType() == Type.TILES) {
			deleteTileLayer(layerInfo);
		} else {
			deleteFeatureLayer(layerInfo);
		}
		layers.remove(layerInfo);
	}

	public void close() throws GeoPackageException {
		dao.commit();
		dao.close();
	}

	public void commit() throws GeoPackageException {
		dao.commit();
	}

	public void createGeneralTables() throws GeoPackageException {
		dao.createGeneralTables();
	}

	public void createTileLayer(LayerInformation layerInfo) throws GeoPackageException {
		dao.createTileLayer(layerInfo);
	}

	public void createTilesetTables(String tableName)
			throws GeoPackageException {
		
		dao.createTilesetTables(tableName);
	}

	public void deleteTileLayer(LayerInformation layerInfo) throws GeoPackageException {
		dao.deleteTileLayer(layerInfo);
	}

	public void deleteFeatureLayer(LayerInformation layerInfo) throws GeoPackageException {
		dao.deleteFeatureLayer(layerInfo);
	}

	public void createTileMatrixZoomLevel(String tableName, int zoomScale,
			int matrixWidth, int matrixHeight, int tileWidth, int tileHeight,
			double xSize, double ySize) throws GeoPackageException {
		
		dao.createTileMatrixZoomLevel(tableName, zoomScale, matrixWidth,
				matrixHeight, tileWidth, tileHeight, xSize, ySize);
	}

	public void createTileMatrixSet(String tableName, double minX, double minY, double maxX, double maxY) 
			throws GeoPackageException {
		
		dao.createTileMatrixSet(tableName, minX, minY, maxX, maxY);
	}

	public void createTile(String tableName, int zoomScale, int tileCol,
			int tileRow, byte[] tileData) throws GeoPackageException {
		
		dao.createTile(tableName, zoomScale, tileCol, tileRow, tileData);
	}

	public void createGeometryColumn(String tableName, String columnName,
			String geometryType, boolean notNull) throws GeoPackageException {
		
		dao.createGeometryColumn(tableName, columnName, geometryType, notNull);
	}

	public void addVectorFeature(String tableName, Map<String, String> fields,
			Set<String> geometryColumnNames) throws GeoPackageException {
		
		dao.addVectorFeature(tableName, fields, geometryColumnNames);
	}

	/*
	 * Scan the schema plus the geometry_columns and raster_columns tables to
	 * determine the exact type of each column.
	 */
	public List<FeatureColumnInfo> parseFeatureColumns(String tableName)
			throws GeoPackageException {
		return dao.parseFeatureColumns(tableName);
	}

	public void updateFeatureTableSchema(String tableName,
			LayerInformation layerInfo, List<FeatureColumnInfo> columns)
			throws GeoPackageException {
		
		boolean success = false;
		try {
			if (dao.hasTable(tableName)) {
				dao.dropTable(tableName);
				dao.commit();
			}
			dao.createFeatureTable(tableName, layerInfo, columns);
			success = true;
		} finally {
			if (success) {
				dao.commit();
			} else {
				dao.rollback();
			}
		}
	}
}
