CREATE TABLE $TABLENAME$ (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     zoom_level INTEGER NOT NULL,
     tile_column INTEGER NOT NULL,
     tile_row INTEGER NOT NULL,
     tile_data BLOB NOT NULL,
     UNIQUE (zoom_level, tile_column, tile_row)
);
/

CREATE UNIQUE INDEX "$TABLENAME$_zoomrowcol_index"
	ON $TABLENAME$ (zoom_level, tile_row, tile_column);
/

CREATE TRIGGER "$TABLENAME$_zoom_insert"
BEFORE INSERT ON "$TABLENAME$"
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table ''$TABLENAME$'' violates constraint: zoom_level not specified for table in gpkg_tile_matrix')
          WHERE NOT (NEW.zoom_level IN (SELECT zoom_level FROM gpkg_tile_matrix WHERE table_name = '$TABLENAME$')) ;
END
/

CREATE TRIGGER "$TABLENAME$_zoom_update"
BEFORE UPDATE OF zoom_level ON "$TABLENAME$"
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table ''$TABLENAME$'' violates constraint: zoom_level not specified for table in gpkg_tile_matrix')
          WHERE NOT (NEW.zoom_level IN (SELECT zoom_level FROM gpkg_tile_matrix WHERE table_name = '$TABLENAME$')) ;
END
/

CREATE TRIGGER "$TABLENAME$_tile_column_insert"
BEFORE INSERT ON "$TABLENAME$"
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table ''$TABLENAME$'' violates constraint: tile_column cannot be < 0')
          WHERE (NEW.tile_column < 0) ;
     SELECT RAISE(ABORT, 'insert on table ''$TABLENAME$'' violates constraint: tile_column must by < matrix_width specified for table and zoom level in gpkg_tile_matrix')
          WHERE NOT (NEW.tile_column < (SELECT matrix_width FROM gpkg_tile_matrix WHERE table_name = '$TABLENAME$' AND zoom_level = NEW.zoom_level));
END
/

CREATE TRIGGER "$TABLENAME$_tile_column_update"
BEFORE UPDATE OF tile_column ON "$TABLENAME$"
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table ''$TABLENAME$'' violates constraint: tile_column cannot be < 0')
          WHERE (NEW.tile_column < 0) ;
     SELECT RAISE(ABORT, 'update on table ''$TABLENAME$'' violates constraint: tile_column must by < matrix_width specified for table and zoom level in gpkg_tile_matrix')
          WHERE NOT (NEW.tile_column < (SELECT matrix_width FROM gpkg_tile_matrix WHERE table_name = '$TABLENAME$' AND zoom_level = NEW.zoom_level));
END
/

CREATE TRIGGER "$TABLENAME$_tile_row_insert"
BEFORE INSERT ON "$TABLENAME$"
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'insert on table ''$TABLENAME$'' violates constraint: tile_row cannot be < 0')
          WHERE (NEW.tile_row < 0) ;
     SELECT RAISE(ABORT, 'insert on table ''$TABLENAME$'' violates constraint: tile_row must by < matrix_height specified for table and zoom level in gpkg_tile_matrix')
          WHERE NOT (NEW.tile_row < (SELECT matrix_height FROM gpkg_tile_matrix WHERE table_name = '$TABLENAME$' AND zoom_level = NEW.zoom_level));
END
/

CREATE TRIGGER "$TABLENAME$_tile_row_update"
BEFORE UPDATE OF tile_row ON "$TABLENAME$"
FOR EACH ROW BEGIN
     SELECT RAISE(ABORT, 'update on table ''$TABLENAME$'' violates constraint: tile_row cannot be < 0')
          WHERE (NEW.tile_row < 0) ;
     SELECT RAISE(ABORT, 'update on table ''$TABLENAME$'' violates constraint: tile_row must by < matrix_height specified for table and zoom level in gpkg_tile_matrix')
          WHERE NOT (NEW.tile_row < (SELECT matrix_height FROM gpkg_tile_matrix WHERE table_name = '$TABLENAME$' AND zoom_level = NEW.zoom_level));
END
/

INSERT INTO gpkg_contents (table_name, data_type) VALUES ('$TABLENAME$', 'tiles');
/
