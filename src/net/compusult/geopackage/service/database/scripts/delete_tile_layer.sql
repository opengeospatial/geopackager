DELETE FROM gpkg_tile_matrix_set WHERE table_name='$TABLENAME$';
/

DELETE FROM gpkg_tile_matrix WHERE table_name='$TABLENAME$';
/

DELETE FROM gpkg_contents WHERE table_name='$TABLENAME$';
/

DROP TABLE $TABLENAME$;
/