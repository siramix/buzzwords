package com.buzzwords;

import android.provider.BaseColumns;

public class PackColumns implements BaseColumns {

  public static final String TABLE_NAME = "packs";

  public static final String NAME = "name";

  public static final String PATH = "path";

  public static final String DESCRIPTION = "description";

  public static final String SIZE = "size";

  public static final String VERSION = "version";

  public static final String[] COLUMNS = {_ID, NAME, PATH, DESCRIPTION, SIZE, VERSION};

  public static final String TABLE_CREATE = "CREATE TABLE " 
      + TABLE_NAME + "( " +
          _ID + " INTEGER PRIMARY KEY, " +
          NAME + " TEXT, " +
          PATH + " TEXT, " +
          DESCRIPTION + " TEXT, " +
          SIZE + " INTEGER, " +
          VERSION + " INTEGER );";
}
