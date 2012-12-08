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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * The Settings class handles the setting of exposed preferences
 * 
 * @author Siramix Labs
 */
public class SettingsPreferenceActivity extends PreferenceActivity {
  
  final private int RESET_CARDS_DIALOG = -1;
  
  // Track the current shown toast
  private Toast mHelpToast = null;

  private boolean mContinueMusic = false; // Flag to continue music across
                                          // Activities

  private OnPreferenceClickListener mPrefClickListener = new OnPreferenceClickListener() {
    
    public boolean onPreferenceClick(Preference preference) {
      if (preference.getKey().equals(Consts.PREFKEY_RESET_PACKS)) {
        showDialog(RESET_CARDS_DIALOG);
      }
      return false;
    }
  };
  
  /**
   * Watch the settings to update any changes (like start up music, reset
   * subtext, etc.)
   */
  private OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
        String key) {
      if (key.equals(Consts.PREFKEY_MUSIC)) {
        // Start or stop the music
        BuzzWordsApplication application = (BuzzWordsApplication) SettingsPreferenceActivity.this
            .getApplication();
        MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
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
        SettingsPreferenceActivity.this.updateTimerSummary();
      }
      
      else if (key.equals(Consts.PREFKEY_NUM_BUZZWORDS)) {
        // Update caption
        SettingsPreferenceActivity.this.updatePreferenceSummary(key);
      }
      
      else if (key.equals(Consts.PREFKEY_RIGHT_SCORE) || 
               key.equals(Consts.PREFKEY_WRONG_SCORE) || 
               key.equals(Consts.PREFKEY_SKIP_SCORE)) {
        // Update score caption
        SettingsPreferenceActivity.this.updateScorePreferenceSummary(key);
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

    mContinueMusic = false;

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    this.addPreferencesFromResource(R.xml.settings);

    // When turn timer is loaded, update the caption
    this.updateTimerSummary();
    this.updatePreferenceSummary(Consts.PREFKEY_NUM_BUZZWORDS);
    this.updateScorePreferenceSummary(Consts.PREFKEY_RIGHT_SCORE);
    this.updateScorePreferenceSummary(Consts.PREFKEY_WRONG_SCORE);
    this.updateScorePreferenceSummary(Consts.PREFKEY_SKIP_SCORE);

    // Register onclick listener
    Preference resetPref = (Preference) findPreference(Consts.PREFKEY_RESET_PACKS);
    resetPref.setOnPreferenceClickListener(mPrefClickListener);
    
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
      SafeLog.e(TAG, e.getMessage());
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
   * Update the text beneath a List Preference to display the current selection
   * @param updateKey Preference key of preference to update
   */
  private void updatePreferenceSummary(String updateKey) {
    // When difficulty is changed, update our caption
    ListPreference lp = (ListPreference) findPreference(updateKey);
    lp.setSummary(lp.getEntry());
  }
  
  /**
   * Update the text beneath a List Preference that pertains to scoring.  We
   * use this for right / wrong /skip score change preference.
   * @param updateKey Preference key of preference to update
   */
  private void updateScorePreferenceSummary(String updateKey) {
    // When difficulty is changed, update our caption
    ListPreference lp = (ListPreference) findPreference(updateKey);
    String scorePrefVal = lp.getValue();
    String pointString = "points";
    // Handle the singular case
    if (scorePrefVal.compareTo("-1") == 0 || scorePrefVal.compareTo("1") == 0) {
      pointString = "point";
    }
    lp.setSummary(getResources().getString(R.string.settings_scorechange_summary) 
                    + " " + scorePrefVal + " " + pointString);
  }
 
  /**
   * Override back button to carry music on back to the Title activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled()) {
      // Keep music playing
      mContinueMusic = true;
    }

    return super.onKeyUp(keyCode, event);
  }
  
  /**
   * Handle creation of dialogs used in TurnSummary
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    Dialog dialog = null;
    AlertDialog.Builder builder = null;

    switch (id) {
    case RESET_CARDS_DIALOG:
      builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.shuffleDialog_title));
      builder.setMessage(getString(R.string.shuffleDialog_text))
          .setPositiveButton(getString(R.string.shuffleDialog_positiveBtn),
               new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound
              SoundManager sm = SoundManager
                  .getInstance(getBaseContext());
              sm.playSound(SoundManager.Sound.CONFIRM);
              Deck deck = new Deck(getBaseContext());
              deck.shuffleAllPacks();
              showToast(getString(R.string.toast_settings_shuffled));
            }
          }).setNegativeButton(this.getString(R.string.shuffleDialog_negativeBtn),
              new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound
              SoundManager sm = SoundManager
                  .getInstance(getBaseContext());
              sm.playSound(SoundManager.Sound.CONFIRM);
              dialog.cancel();
            }
          });
      dialog = builder.create();
      break;
    default:
      dialog = null;
    }
    return dialog;
  }
  
  /**
   * Handle showing a toast or refreshing an existing toast
   */
  private void showToast(String text) {
    if(mHelpToast == null) {
      mHelpToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
    } else {
      mHelpToast.setText(text);
      mHelpToast.setDuration(Toast.LENGTH_LONG);
    }
    mHelpToast.show();
  }


  /**
   * Override onPause to prevent activity specific processes from running while
   * app is in background
   */
  @Override
  public void onPause() {
    super.onPause();

    // Unregister settings listeners
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    sp.unregisterOnSharedPreferenceChangeListener(mPrefListener);

    // Pause music
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
    if (!mContinueMusic && mp.isPlaying()) {
      application.cleanUpMusicPlayer();
    }
  }

  /**
   * Override OnResume to resume activity specific processes
   */
  @Override
  public void onResume() {
    super.onResume();

    // Resume Title Music
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
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
