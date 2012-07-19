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

public class CardColumns implements BaseColumns {

  public static final String TABLE_NAME = "cards";

  public static final String TITLE = "title";
  
  public static final String BADWORDS = "badwords";

  public static final String PLAY_DATE = "play_date";

  public static final String PACK_ID = "pack_id";

  public static final String TIMES_SEEN = "times_seen";

  public static final String[] COLUMNS = {_ID, TITLE, BADWORDS, PLAY_DATE, TIMES_SEEN, PACK_ID};

  public static final String TABLE_CREATE = "CREATE TABLE "
      + TABLE_NAME + "( " +
      _ID + " INTEGER PRIMARY KEY, " +
      TITLE + " TEXT, " +
      BADWORDS+ " TEXT, " +
      PLAY_DATE + " INTEGER, " +
      TIMES_SEEN + " INTEGER, " +
      PACK_ID + " INTEGER );";
}