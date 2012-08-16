/*****************************************************************************
 *  Buzzwords is a family friendly word game for mobile phones.
 *  Copyright (C) 2011 Siramix Team
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.buzzwords;

import android.provider.BaseColumns;

public class PackColumns implements BaseColumns {

  public static final String TABLE_NAME = "packs";

  public static final String NAME = "name";

  public static final String PATH = "path";

  public static final String DESCRIPTION = "description";

  public static final String PURCHASE_TYPE = "purchase_type";

  public static final String VERSION = "version";

  public static final String[] COLUMNS = {_ID, NAME, PATH, DESCRIPTION, PURCHASE_TYPE, VERSION};

  public static final String TABLE_CREATE = "CREATE TABLE " 
      + TABLE_NAME + "( " +
          _ID + " INTEGER PRIMARY KEY, " +
          NAME + " TEXT, " +
          PATH + " TEXT, " +
          DESCRIPTION + " TEXT, " +
          PURCHASE_TYPE + " INTEGER, " +
          VERSION + " INTEGER );";
}
