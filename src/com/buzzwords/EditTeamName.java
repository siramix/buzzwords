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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This handles changing a specified team's name
 * 
 * @author Siramix Labs
 */
public class EditTeamName extends Activity {

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "EditTeamName";

  private Team mTeam;

  /*
   * References to views
   */
  private EditText mEditTeamName;
  private Button mButtonCancel;
  private Button mButtonAccept;

  /**
   * flag used for stopping music OnStop() event.
   */
  private boolean mContinueMusic;

  /**
   * Set the references to the elements from the layout file
   */
  private void setupViewReferences() {
    mEditTeamName = (EditText) this.findViewById(R.id.EditTeamName_EditField);
    mButtonCancel = (Button) this
        .findViewById(R.id.EditTeamName_Buttons_Cancel);
    mButtonAccept = (Button) this
        .findViewById(R.id.EditTeamName_Buttons_Accept);
  }

  /**
   * Watches the button that handles returning to previous activity with no
   * changes
   */
  private final OnClickListener mCancelListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "Cancel onClick()");
      }
      // Keep music playing
      mContinueMusic = true;
      finish();
    }
  };

  /**
   * Watches the button that handles returning to previous activity with changes
   * to team
   */
  private final OnClickListener mAcceptListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "Cancel onClick()");
      }

      // Cache the team name
      String teamName = mEditTeamName.getText().toString();

      // Keep music playing
      mContinueMusic = true;

      // Pass back the team and the name
      Intent curIntent = new Intent();
      curIntent.putExtra(getString(R.string.teamBundleKey), mTeam);
      curIntent.putExtra(getString(R.string.teamNameBundleKey), teamName);
      EditTeamName.this.setResult(Activity.RESULT_OK, curIntent);
      finish();
    }
  };

  /**
   * Create the activity and display the card bundled in the intent.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    this.setContentView(R.layout.editteamname);

    setupViewReferences();

    // Set fonts on titles
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");

    TextView label = (TextView) this.findViewById(R.id.EditTeamName_Title);
    label.setTypeface(antonFont);

    // Get the team from the passed in Bundle
    Intent curIntent = this.getIntent();
    Bundle teamBundle = curIntent.getExtras();
    mTeam = (Team) teamBundle
        .getSerializable(getString(R.string.teamBundleKey));

    // Set initial value for EditTeamName field
    mEditTeamName.setText(mTeam.getName());
    // Select the last character (this seems to be typical behavior)
    mEditTeamName.setSelection(mEditTeamName.getText().length());

    // Initialize hint text
    TextView hint = (TextView) this.findViewById(R.id.EditTeamName_Hint);
    hint.setText(getString(R.string.editTeamName_hint, mTeam.getDefaultName()));

    // Set listener for accept
    mButtonCancel.setOnClickListener(mCancelListener);
    mButtonAccept.setOnClickListener(mAcceptListener);
  }

  /**
   * Override back button to carry music on back to previous activity
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
   * Override onPause to prevent music from playing while backgrounded
   */
  @Override
  public void onPause() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onPause()");
    }
    super.onPause();
    if (!mContinueMusic) {
      BuzzWordsApplication application = (BuzzWordsApplication) this
          .getApplication();
      MediaPlayer mp = application.getMusicPlayer();
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
          .getBaseContext());
      if (mp.isPlaying() && sp.getBoolean("music_enabled", true)) {
        mp.pause();
      }
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
    if (!mp.isPlaying() && sp.getBoolean("music_enabled", true)) {
      mp.start();
    }

    // set flag to let onPause handle music
    mContinueMusic = false;
  }
}
