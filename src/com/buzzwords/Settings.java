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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

/**
 * The Settings class handles the setting of exposed preferences
 * 
 * @author Siramix Labs
 */
public class Settings extends PreferenceActivity {

  private boolean mContinueMusic = false; // Flag to continue music across
                                          // Activities

  /**
   * Watch the settings to update any changes (like start up music, reset
   * subtext, etc.)
   */
  private OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
        String key) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "onSharedPreferencesChanged()");
      }
      if (key.equals(Consts.PREFKEY_MUSIC)) {
        // Start or stop the music
        BuzzWordsApplication application = (BuzzWordsApplication) Settings.this
            .getApplication();
        MediaPlayer mp = application.getMusicPlayer();
        if (sharedPreferences.getBoolean(Consts.PREFKEY_MUSIC, true)) {
          if (!mp.isPlaying()) {
            mp.start();
          }
        } else {
          if (mp.isPlaying()) {
            mp.pause();
          }
        }
      }

      else if (key.equals(Consts.PREFKEY_TIMER)) {
        // When turn timer is changed, update the caption
        Settings.this.updateTimerSummary();
      }
      
      else if (key.equals(Consts.PREFKEY_DIFFICULTY)) {
        // Update caption
        Settings.this.updateDifficultySummary();
      }
    }
  };

  /**
   * logging tag 
   */
  public static String TAG = "Settings";

  /**
   * onCreate - initializes a settings screen
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

    this.addPreferencesFromResource(R.xml.settings);

    // When turn timer is loaded, update the caption
    this.updateTimerSummary();
    this.updateDifficultySummary();

    // Update the version preference caption to the existing app version
    Preference version = findPreference("app_version");
    try {
      version.setTitle(this.getString(R.string.AppName));
      version
          .setSummary("Version "
              + this.getPackageManager().getPackageInfo(this.getPackageName(),
                  0).versionName);
    } catch (NameNotFoundException e) {
      e.printStackTrace();
      Log.e(TAG, e.getMessage());
    }
  }

  /**
   * Updates the timer label by checking the preference for the current time
   */
  private void updateTimerSummary() {
    // When turn timer is loaded, update the caption
    ListPreference lp = (ListPreference) findPreference(Consts.PREFKEY_TIMER);
    lp.setSummary(lp.getValue() + " seconds");
  }
  
  /**
   * Update the text beneath difficulty to display the current selection
   */
  private void updateDifficultySummary() {
    // When difficulty is changed, update our caption
    ListPreference lp = (ListPreference) findPreference(Consts.PREFKEY_DIFFICULTY);
    lp.setSummary(lp.getEntry());
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
      // Keep music playing
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

    // Unregister settings listeners
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    sp.unregisterOnSharedPreferenceChangeListener(mPrefListener);

    // Pause music
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer();
    if (!mContinueMusic && mp.isPlaying()) {
      mp.pause();
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

    // Resume Title Music
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer();
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    if (!mp.isPlaying() && sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
      mp.start();
    }

    // Register preference listener with SharedPreferences
    sp.registerOnSharedPreferenceChangeListener(mPrefListener);

    mContinueMusic = false;
  }
}
