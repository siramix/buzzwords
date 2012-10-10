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

package com.buzzwords;

/**
 * This class holds global constants that are used throughout the application
 * including preferences and constants that support in-app billing.
 */
public class Consts {

    public static final boolean DEBUG = true;
    public static final String PREFKEY_NUM_BUZZWORDS = "com.buzzwords.NUM_BUZZWORDS";
    public static final String PREFKEY_TIMER = "com.buzzwords.TURN_TIMER";
    public static final String PREFKEY_MUSIC = "com.buzzwords.MUSIC_ENABLED";
    public static final String PREFKEY_SFX = "com.buzzwords.SFX_ENABLED";
    public static final String PREFKEY_SKIP = "com.buzzwords.ALLOW_SKIP";
    public static final String PREFKEY_GESTURES= "com.buzzwords.ALLOW_GESTURES";
    public static final String PREFKEY_RIGHT_SCORE = "com.buzzwords.RIGHT_SCORE";
    public static final String PREFKEY_WRONG_SCORE = "com.buzzwords.WRONG_SCORE";
    public static final String PREFKEY_SKIP_SCORE = "com.buzzwords.SKIP_SCORE";
    public static final String PREFKEY_RESET_PACKS = "com.buzzwords.RESET_PACKS";
    public static final String PREFKEY_DB_INITIALIZED = "com.buzzwords.DB_INITIALIZED";
    public static final String PREFKEY_SYNC_REQUIRED = "com.buzzwords.PACK_SYNC_REQUIRED";
    public static final String PREFKEY_LAST_USER = "com.buzzwords.PACK_SYNC_LAST_USER";
    public static final String PREFKEY_FACEBOOK_REQUEST_CODE = "com.buzzwords.RECENT_REQUEST_CODE";
    public static final String PREFKEY_FACEBOOK_PACK_ID = "com.buzzwords.FACEBOOK_PACK_ID";

    public static final String PREFFILE_SYNC_REQUIRED = "com.buzzwords.SYNC_PREF";
    public static final String PREFFILE_PACK_SELECTIONS = "com.buzzwords.PACK_SELECTIONS";
}
