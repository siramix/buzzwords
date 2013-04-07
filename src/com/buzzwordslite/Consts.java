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
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.buzzwordslite;

/**
 * This class holds global constants that are used throughout the application
 * including preferences and constants that support in-app billing.
 */
public class Consts {

    public static final boolean DEBUG = true;
    public static final String PREFKEY_NUM_BUZZWORDS = "com.buzzwordslite.NUM_BUZZWORDS";

    public static final String PREFKEY_TIMER = "com.buzzwordslite.TURN_TIMER";
    public static final String PREFKEY_MUSIC = "com.buzzwordslite.MUSIC_ENABLED";
    public static final String PREFKEY_SFX = "com.buzzwordslite.SFX_ENABLED";
    public static final String PREFKEY_SKIP = "com.buzzwordslite.ALLOW_SKIP";
    public static final String PREFKEY_GESTURES= "com.buzzwordslite.ALLOW_GESTURES";
    public static final String PREFKEY_RIGHT_SCORE = "com.buzzwordslite.RIGHT_SCORE";
    public static final String PREFKEY_WRONG_SCORE = "com.buzzwordslite.WRONG_SCORE";
    public static final String PREFKEY_SKIP_SCORE = "com.buzzwordslite.SKIP_SCORE";
    public static final String PREFKEY_RESET_PACKS = "com.buzzwordslite.RESET_PACKS";
    public static final String PREFKEY_DB_INITIALIZED = "com.buzzwordslite.DB_INITIALIZED";
    public static final String PREFKEY_MUSIC_RESOURCE = "com.buzzwordslite.MUSIC_RESOURCE";
    public static final String PREFKEY_MUSIC_LOOPING = "com.buzzwordslite.MUSIC_LOOPING";
    public static final String PREFKEY_IS_TURN_IN_PROGRESS = "com.buzzwordslite.TURN_IN_PROGRESS";
    public static final String PREFKEY_A_IS_ACTIVE = "com.buzzwordslite.A_IS_ACTIVE";
    public static final String PREFKEY_IS_BACK = "com.buzzwordslite.IS_BACK";
    public static final String PREFKEY_IS_TICKING = "com.buzzwordslite.IS_TICKING";
    public static final String PREFKEY_IS_PAUSED = "com.buzzwordslite.IS_PAUSED";
    public static final String PREFKEY_IS_TURN_IN_START_DIALOG = "com.buzzwordslite.IS_TURN_IN_START_DIALOG";
    public static final String PREFKEY_TURN_TIME_REMAINING = "com.buzzwordslite.TURN_TIME_REMAINING";
    public static final String PREFFILE_PACK_SELECTIONS = "com.buzzwordslite.PACK_SELECTIONS";
    public static final String PREFFILE_MUSIC_STATE = "com.buzzwordslite.MUSIC_STATE";
    public static final String PREFFILE_TURN_STATE = "com.buzzwordslite.TURN_STATE";

    public static final String DECK_TEMP_FILE = "cur_deck.ser";
    public static final String GAME_MANAGER_TEMP_FILE = "cur_gm.ser";
    public static final String TIMER_TEMP_FILE = "cur_timer.ser";
    
    public static final String DATABASE_NAME = "buzzwords";
    public static final int DATABASE_VERSION = 3;
    
    public static final int CACHE_MAXSIZE = 100;
    public static final int CACHE_TURNSIZE = 20;
    
    public static final int PACK_CURRENT = -1;
    public static final int PACK_NOT_PRESENT = -2;
}
