
CREATE TRIGGER 'gpkg_metadata_md_scope_insert'
BEFORE INSERT ON 'gpkg_metadata'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table gpkg_metadata violates constraint: md_scope must be one of undefined | fieldSession | collectionSession | series | dataset | featureType | feature | attributeType | attribute | tile | model | catalogue | schema | taxonomy | software | service | collectionHardware | nonGeographicDataset | dimensionGroup')
          WHERE NOT(NEW.md_scope IN
          ('undefined','fieldSession','collectionSession','series','dataset', 'featureType','feature','attributeType','attribute','tile','model', 'catalogue','schema','taxonomy','software','service', 'collectionHardware','nonGeographicDataset','dimensionGroup'));
END
/

CREATE TRIGGER 'gpkg_metadata_md_scope_update'
BEFORE UPDATE OF 'md_scope' ON 'gpkg_metadata'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table gpkg_metadata violates constraint: md_scope must be one of undefined | fieldSession | collectionSession | series | dataset | featureType | feature | attributeType | attribute | tile | model | catalogue | schema | taxonomy | software | service | collectionHardware | nonGeographicDataset | dimensionGroup')
          WHERE NOT(NEW.md_scope IN ('undefined','fieldSession','collectionSession','series','dataset', 'featureType','feature','attributeType','attribute','tile','model', 'catalogue','schema','taxonomy','software','service', 'collectionHardware','nonGeographicDataset','dimensionGroup'));
END
/

CREATE TRIGGER 'gpkg_metadata_reference_reference_scope_insert'
BEFORE INSERT ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: reference_scope must be one of "geopackage", table", "column", "row", "row/col"')
          WHERE NOT NEW.reference_scope IN ('geopackage','table','column','row','row/col');
END
/

CREATE TRIGGER 'gpkg_metadata_reference_reference_scope_update'
BEFORE UPDATE OF 'reference_scope' ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: referrence_scope must be one of "geopackage", "table", "column", "row", "row/col"')
          WHERE NOT NEW.reference_scope IN ('geopackage','table','column','row','row/col');
END
/

CREATE TRIGGER 'gpkg_metadata_reference_column_name_insert'
BEFORE INSERT ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: column name must be NULL when reference_scope is "geopackage", "table" or "row"')
          WHERE (NEW.reference_scope IN ('geopackage','table','row') 
          AND NEW.column_name IS NOT NULL);
     SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: column name must be defined for the specified table when reference_scope is "column" or "row/col"')
          WHERE (NEW.reference_scope IN ('column','row/col') 
          AND NOT NEW.table_name IN (
               SELECT name FROM SQLITE_MASTER 
                    WHERE type = 'table' 
                    AND name = NEW.table_name 
                    AND sql LIKE ('%' || NEW.column_name || '%')));
END
/

CREATE TRIGGER 'gpkg_metadata_reference_column_name_update'
BEFORE UPDATE OF column_name ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: column name must be NULL when reference_scope is "geopackage", "table" or "row"')
          WHERE (NEW.reference_scope IN ('geopackage','table','row') 
          AND NEW.column_nameIS NOT NULL);
     SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: column name must be defined for the specified table when reference_scope is "column" or "row/col"')
          WHERE (NEW.reference_scope IN ('column','row/col') 
          AND NOT NEW.table_name IN (
               SELECT name FROM SQLITE_MASTER 
                    WHERE type = 'table' 
                    AND name = NEW.table_name 
                    AND sql LIKE ('%' || NEW.column_name || '%')));
END
/

CREATE TRIGGER 'gpkg_metadata_reference_row_id_value_insert'
BEFORE INSERT ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: row_id_value must be NULL when reference_scope is "geopackage", "table" or "column"')
          WHERE NEW.reference_scope IN ('geopackage','table','column') 
          AND NEW.row_id_value IS NOT NULL;
     SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: row_id_value must exist in specified table when reference_scope is "row" or "row/col"')
          WHERE NEW.reference_scope IN ('row','row/col') 
          AND NOT EXISTS (
               SELECT rowid FROM (SELECT NEW.table_name AS table_name) 
                    WHERE rowid = NEW.row_id_value);
END
/

CREATE TRIGGER 'gpkg_metadata_reference_row_id_value_update'
BEFORE UPDATE OF 'row_id_value' ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: row_id_value must be NULL when reference_scope is "geopackage", "table" or "column"')
          WHERE NEW.reference_scope IN ('geopackage','table','column') 
          AND NEW.row_id_value IS NOT NULL;
     SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: row_id_value must exist in specified table when reference_scope is "row" or "row/col"')
          WHERE NEW.reference_scope IN ('row','row/col') 
          AND NOT EXISTS (
               SELECT rowid FROM (SELECT NEW.table_name AS table_name) 
                    WHERE rowid = NEW.row_id_value);
END
/

CREATE TRIGGER 'gpkg_metadata_reference_timestamp_insert'
BEFORE INSERT ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table gpkg_metadata_reference violates constraint: timestamp must be a valid time in ISO 8601 "yyyy-mm-ddThh-mm-ss.cccZ" form')
          WHERE NOT (NEW.timestamp GLOB '[1-2][0-9][0-9][0-9]-[0-1][0-9]-[1-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z' 
          AND strftime('%s',NEW.timestamp) NOT NULL);
END
/

CREATE TRIGGER 'gpkg_metadata_reference_timestamp_update'
BEFORE UPDATE OF 'timestamp' ON 'gpkg_metadata_reference'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table gpkg_metadata_reference violates constraint: timestamp must be a valid time in ISO 8601 "yyyy-mm-ddThh-mm-ss.cccZ" form')
          WHERE NOT (NEW.timestamp GLOB '[1-2][0-9][0-9][0-9]-[0-1][0-9]-[1-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z' 
          AND strftime('%s',NEW.timestamp) NOT NULL);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_zoom_level_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: zoom_level cannot be less than 0')
          WHERE (NEW.zoom_level < 0);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_zoom_level_update'
BEFORE UPDATE of zoom_level ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: zoom_level cannot be less than 0')
          WHERE (NEW.zoom_level < 0);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_matrix_width_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: matrix_width cannot be less than 1')
          WHERE (NEW.matrix_width < 1);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_matrix_width_update'
BEFORE UPDATE OF matrix_width ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: matrix_width cannot be less than 1')
          WHERE (NEW.matrix_width < 1);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_matrix_height_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: matrix_height cannot be less than 1')
          WHERE (NEW.matrix_height < 1);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_matrix_height_update'
BEFORE UPDATE OF matrix_height ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: matrix_height cannot be less than 1')
          WHERE (NEW.matrix_height < 1);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_pixel_x_size_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: pixel_x_size must be greater than 0')
          WHERE NOT (NEW.pixel_x_size > 0);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_pixel_x_size_update'
BEFORE UPDATE OF pixel_x_size ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: pixel_x_size must be greater than 0')
          WHERE NOT (NEW.pixel_x_size > 0);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_pixel_y_size_insert'
BEFORE INSERT ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table ''gpkg_tile_matrix'' violates constraint: pixel_y_size must be greater than 0')
          WHERE NOT (NEW.pixel_y_size > 0);
END
/

CREATE TRIGGER 'gpkg_tile_matrix_pixel_y_size_update'
BEFORE UPDATE OF pixel_y_size ON 'gpkg_tile_matrix'
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table ''gpkg_tile_matrix'' violates constraint: pixel_y_size must be greater than 0')
          WHERE NOT (NEW.pixel_y_size > 0);
END
/


