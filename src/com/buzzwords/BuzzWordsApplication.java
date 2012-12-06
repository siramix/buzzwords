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

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * Class extending the standard android application. This allows us to refer to
 * one GameManager from every activity within BuzzWords.
 * 
 * @author Siramix Labs
 */
public class BuzzWordsApplication extends Application {
  /**
   * Global Debug constant
   */
  public static final boolean DEBUG = true;
  public static final boolean DEBUG_TIMERTICKS = false;
  public static final Markets MARKET = Markets.AMAZON;
  public static final boolean USE_TEST_PACKS = true;
  public static Uri storeURI_Buzzwords;
  public static Uri storeURI_BuzzwordsLite;
  public static enum Markets {
    ANDROID, AMAZON, BN
  };
  
  /**
   * logging tag
   */
  public static String TAG = "BuzzWordsApplication";

  /**
   * The GameManager for all of BuzzWords
   */
  private GameManager mGameManager;

  /**
   * MediaPlayer for music
   */
  private MediaPlayer mMediaPlayer;

  /**
   * Default constructor
   */
  public BuzzWordsApplication() {
    super();
  }

  public void onCreate() {
    switch (BuzzWordsApplication.MARKET) {
      case ANDROID:
        storeURI_BuzzwordsLite = Uri.parse(getApplicationContext().getString(R.string.URI_android_market_buzzwordslite));
        storeURI_Buzzwords = Uri.parse(getApplicationContext().getString(R.string.URI_android_market_buzzwords));
        break;
      case AMAZON:
        storeURI_BuzzwordsLite = Uri.parse(getApplicationContext().getString(R.string.URI_amazon_market_buzzwordslite));
        storeURI_Buzzwords = Uri.parse(getApplicationContext().getString(R.string.URI_amazon_market_buzzwords));
        break;    
      default:
        storeURI_BuzzwordsLite = Uri.parse(getApplicationContext().getString(R.string.URI_buzzwords_redirect));
        storeURI_Buzzwords = Uri.parse(getApplicationContext().getString(R.string.URI_buzzwords_redirect));
        break;
    }
  }
  
  /**
   * @return a reference to the game manager
   */
  public GameManager getGameManager() {
    return this.mGameManager;
  }

  /**
   * @param gm
   *          - a reference to the game manager
   */
  public void setGameManager(GameManager gm) {
    this.mGameManager = gm;
  }

  /**
   * @param context
   *          in which to create the media player
   * @param id
   *          of the music to play
   * @return a reference to the media player
   */
  public MediaPlayer createMusicPlayer(Context context, int id) {
    SafeLog.d(TAG, "CreateMusicPlayer(" + context + "," + id + ")");
    // Clean up resources. This fixed a leak issue caused by starting many games
    // over and over.
    if (mMediaPlayer != null) {
      mMediaPlayer.release();
    }
    mMediaPlayer = MediaPlayer.create(context, id);
    return mMediaPlayer;
  }

  /**
   * @return a reference to the current media player
   */
  public MediaPlayer getMusicPlayer() {
    return mMediaPlayer;
  }
}
