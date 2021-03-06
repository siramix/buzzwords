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
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Class extending the standard android application. This allows us to refer to
 * one GameManager from every activity within BuzzWords.
 * 
 * @author Siramix Labs
 */
public class BuzzWordsApplication extends Application {

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
   * Track last played music track, to restore it on resume
   */
  private int mTrackID;

  /**
   * Default constructor
   */
  public BuzzWordsApplication() {
    super();
  }

  
  /**
   * @return a reference to the game manager
   */
  public GameManager getGameManager() {
    if(mGameManager == null) {
      mGameManager = GameManager.restoreState(this.getBaseContext());
    }
    return mGameManager;
  }

  /**
   * @param gm
   *          - a reference to the game manager
   */
  public void setGameManager(GameManager gm) {
    mGameManager = gm;
  }

  /**
   * @param context
   *          in which to create the media player
   * @param id
   *          of the music to play
   * @return a reference to the media player
   */
  public MediaPlayer createMusicPlayer(Context context, int id) {
    Log.d(TAG, "CreateMusicPlayer(" + context + "," + id + ")");
    // Clean up resources. This fixed a leak issue caused by starting many games
    // over and over.
    if (mMediaPlayer != null) {
      mMediaPlayer.release();
    }
    mTrackID = id;
    SharedPreferences musicPrefs = this.getSharedPreferences(Consts.PREFFILE_MUSIC_STATE, Context.MODE_PRIVATE);
    SharedPreferences.Editor musicPrefsEditor = musicPrefs.edit();
    musicPrefsEditor.putInt(Consts.PREFKEY_MUSIC_RESOURCE, mTrackID);
    musicPrefsEditor.commit();
    mMediaPlayer = MediaPlayer.create(context, id);
    return mMediaPlayer;
  }

  /**
   * @return a reference to the current media player
   */
  public MediaPlayer getMusicPlayer(Context context) {
    if(mMediaPlayer == null)
    {
      SharedPreferences musicPrefs = this.getSharedPreferences(Consts.PREFFILE_MUSIC_STATE, Context.MODE_PRIVATE);
      mTrackID = musicPrefs.getInt(Consts.PREFKEY_MUSIC_RESOURCE, mTrackID);
      boolean isLooping = musicPrefs.getBoolean(Consts.PREFKEY_MUSIC_LOOPING, false);
      mMediaPlayer = MediaPlayer.create(context, mTrackID);
      mMediaPlayer.setLooping(isLooping);
    }
    return mMediaPlayer;
  }
  
  /**
   * Clean up resources required by the media player. This should
   * be called whenever you know there will be no music.
   */
  public void cleanUpMusicPlayer() {
    if (mMediaPlayer != null) {
      SharedPreferences musicPrefs = this.getSharedPreferences(Consts.PREFFILE_MUSIC_STATE, Context.MODE_PRIVATE);
      SharedPreferences.Editor musicPrefsEditor = musicPrefs.edit();
      musicPrefsEditor.putBoolean(Consts.PREFKEY_MUSIC_LOOPING, mMediaPlayer.isLooping());
      musicPrefsEditor.commit();
      
      if (mMediaPlayer.isPlaying()) {
        mMediaPlayer.stop();
      }
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }
}
