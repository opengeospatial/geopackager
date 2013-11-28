DELETE FROM gpkg_geometry_columns WHERE table_name='$TABLENAME$';
/

DELETE FROM gpkg_data_columns WHERE table_name='$TABLENAME$';
/

DELETE FROM gpkg_contents WHERE table_name='$TABLENAME$';
/

DROP TABLE $TABLENAME$;
/