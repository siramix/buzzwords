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

import com.buzzwords.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

/**
 * This activity class is responsible for displaying the rules of buzzwords to
 * the user.
 * 
 * @author Siramix Labs
 */
public class CreditsActivity extends Activity {
  /**
   * logging tag
   */
  public static String TAG = "Credits";

  private boolean mIsMusicPaused = false;
  private boolean mContinueMusic = false;

  /**
   * onCreate - initializes the activity to display the credits.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    mContinueMusic = false;

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    this.setContentView(R.layout.credits);
  }

  /**
   * Override back button to carry music on back to the Title activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled()) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "BackKeyUp()");
      }
      // Flag to keep music playing
      mContinueMusic = true;
    }

    return super.onKeyUp(keyCode, event);
  }

  /**
   * Override onPause to prevent activity specific processes from running while
   * app is in background
   */
  @Override
  public void onPause() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onPause()");
    }
    super.onPause();
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer();
    // If music is playing, we must pause it and flag to resume it onResume().
    if (!mContinueMusic && mp.isPlaying()) {
      mp.pause();
      mIsMusicPaused = true;
    }
  }

  /**
   * Override OnResume to resume activity specific processes
   */
  @Override
  public void onResume() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onResume()");
    }
    super.onResume();

    // Resume Title Music -- Only do this if we paused DURING credits
    if (mIsMusicPaused) {
      BuzzWordsApplication application = (BuzzWordsApplication) this
          .getApplication();
      MediaPlayer mp = application.getMusicPlayer();
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
          .getBaseContext());
      if (!mp.isPlaying() && sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
        mp.start();
      }
    }

    mContinueMusic = false;
  }
}
