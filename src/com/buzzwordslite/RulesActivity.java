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
package com.buzzwordslite;

import com.buzzwordslite.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

/**
 * This activity class is responsible for displaying the rules of buzzwords to
 * the user.
 * 
 * @author Siramix Labs
 */
public class RulesActivity extends Activity {
  /**
   * logging tag
   */
  public static String TAG = "Rules";

  private boolean mIsMusicPaused = false;
  private boolean mContinueMusic = false;

  /**
   * onCreate - initializes the activity to display the rules.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContinueMusic = false;

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    this.setContentView(R.layout.rules);

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());

    // String used for displaying the customizable preferences to the user
    StringBuilder prefBuilder = new StringBuilder();
    prefBuilder
        .append("(These rules can be changed any time from the Settings screen.)");

    // Turn Length rule display
    prefBuilder.append("\n\nTurn Length: " + sp.getString("turn_timer", "60")
        + " seconds");

    // Allow Skipping rule display
    if (sp.getBoolean(Consts.PREFKEY_SKIP, true)) {
      prefBuilder.append("\nAllow Skipping: Players may skip words.");
    } else {
      prefBuilder.append("\nAllow Skipping: Players can not skip words.");
    }
    TextView rulePrefs = (TextView) this.findViewById(R.id.Rules_Preferences);
    rulePrefs.setText(prefBuilder);
  }

  /**
   * Override back button to carry music on back to the Title activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled()) {
      mContinueMusic = true; // Flag to keep music playing
    }

    return super.onKeyUp(keyCode, event);
  }

  /**
   * Override onPause to prevent activity specific processes from running while
   * app is in background
   */
  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, "onPause()");

    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
    // If music is playing, we must pause it and flag to resume it onResume().
    // This solves the problem where Rules was never playing music to begin with
    // (which happens
    // when entering Rules from Turn or TurnSummary
    if (!mContinueMusic && mp.isPlaying()) {
      application.cleanUpMusicPlayer();
      mIsMusicPaused = true;
    }
  }

  /**
   * Override OnResume to resume activity specific processes
   */
  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume()");

    // Resume Title Music -- Only do this if we paused DURING rules
    if (mIsMusicPaused) {
      BuzzWordsApplication application = (BuzzWordsApplication) this
          .getApplication();
      MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
          .getBaseContext());
      if (!mp.isPlaying() && sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
        mp.start();
      }
    }

    mContinueMusic = false;
  }
}
