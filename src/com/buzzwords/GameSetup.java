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

import java.util.LinkedList;

import com.buzzwords.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;

/**
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and turns
 * 
 * @author The BuzzWords Team
 */
public class GameSetup extends Activity {

  static final int DIALOG_TEAMERROR = 0;
  private LinkedList<Team> teamList = new LinkedList<Team>();
  private static SharedPreferences gameSetupPrefs;
  private static SharedPreferences.Editor gameSetupPrefEditor;

  // A two dimensional array to store the radioID/value pair.
  private static final int[][] ROUND_RADIOS = new int[][] {
      { R.id.GameSetup_Rounds0, 2 }, { R.id.GameSetup_Rounds1, 4 },
      { R.id.GameSetup_Rounds2, 6 }, { R.id.GameSetup_Rounds3, 8 } };

  // Preference keys (indicating quadrant)
  public static final String PREFS_NAME = "gamesetupprefs";
  private static final String PREFKEY_TEAMA = "teamA_enabled";
  private static final String PREFKEY_TEAMB = "teamB_enabled";
  private static final String PREFKEY_TEAMC = "teamC_enabled";
  private static final String PREFKEY_TEAMD = "teamD_enabled";

  // Index of the selected radio indicating number of rounds
  private static final String RADIO_INDEX = "round_radio_index";

  // Flag to play music into the next Activity
  private boolean continueMusic = false;

  /**
   * logging tag
   */
  public static String TAG = "GameSetup";

  /**
   * Watches the button that handles hand-off to the Turn activity.
   */
  private final OnClickListener StartGameListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "StartGameListener onClick()");
      }

      // Validate team numbers
      if (GameSetup.this.teamList.size() <= 1) {
        GameSetup.this.showDialog(DIALOG_TEAMERROR);
        return;
      }

      // Store off game's attributes as preferences
      GameSetup.gameSetupPrefEditor.putInt(GameSetup.RADIO_INDEX,
          GameSetup.this.getCheckedRadioIndex());
      GameSetup.gameSetupPrefEditor.commit();

      // Create a GameManager to manage attributes about the current game
      BuzzWordsApplication application = (BuzzWordsApplication) GameSetup.this
          .getApplication();
      GameManager gm = new GameManager(GameSetup.this);
      gm.StartGame(teamList,
          ROUND_RADIOS[GameSetup.this.getCheckedRadioIndex()][1]);
      application.SetGameManager(gm);

      // Launch into Turn activity
      startActivity(new Intent(getApplication().getString(R.string.IntentTurn),
          getIntent().getData()));

      // Stop the music
      MediaPlayer mp = application.GetMusicPlayer();
      mp.stop();
    }
  };

  /**
   * Watches the button that adds the first team to the list
   */
  private final OnClickListener AddTeamAListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "AddTeamAListener onClick()");
      }
      Button b = (Button) v;

      // Play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) GameSetup.this
          .getApplication();
      SoundManager sound = application.GetSoundManager();

      if (teamList.remove(Team.TEAMA)) {
        b.setBackgroundResource(R.color.inactiveButton);
        b.setTextColor(GameSetup.this.getResources()
            .getColor(R.color.genericBG));
        GameSetup.gameSetupPrefEditor
            .putBoolean(GameSetup.PREFKEY_TEAMA, false);
        sound.PlaySound(SoundManager.SOUND_BACK);
      } else {
        teamList.add(Team.TEAMA);
        b.setBackgroundResource(R.color.teamA_text);
        b.setTextColor(GameSetup.this.getResources().getColor(
            R.color.teamA_secondary));
        GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.PREFKEY_TEAMA, true);
        sound.PlaySound(SoundManager.SOUND_CONFIRM);
      }
    }
  };

  /**
   * Watches the button that adds the second team to the list
   */
  private final OnClickListener AddTeamBListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "AddTeamBListener onClick()");
      }
      Button b = (Button) v;

      // Play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) GameSetup.this
          .getApplication();
      SoundManager sound = application.GetSoundManager();

      if (teamList.remove(Team.TEAMB)) {
        b.setBackgroundResource(R.color.inactiveButton);
        b.setTextColor(GameSetup.this.getResources()
            .getColor(R.color.genericBG));
        GameSetup.gameSetupPrefEditor
            .putBoolean(GameSetup.PREFKEY_TEAMB, false);
        sound.PlaySound(SoundManager.SOUND_BACK);
      } else {
        teamList.add(Team.TEAMB);
        b.setBackgroundResource(R.color.teamB_text);
        b.setTextColor(GameSetup.this.getResources().getColor(
            R.color.teamB_secondary));
        GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.PREFKEY_TEAMB, true);
        sound.PlaySound(SoundManager.SOUND_CONFIRM);
      }
    }
  };

  /**
   * Watches the button that adds the third team to the list
   */
  private final OnClickListener AddTeamCListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "AddTeamCListener onClick()");
      }
      Button b = (Button) v;

      // Play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) GameSetup.this
          .getApplication();
      SoundManager sound = application.GetSoundManager();

      if (teamList.remove(Team.TEAMC)) {
        b.setBackgroundResource(R.color.inactiveButton);
        b.setTextColor(GameSetup.this.getResources()
            .getColor(R.color.genericBG));
        GameSetup.gameSetupPrefEditor
            .putBoolean(GameSetup.PREFKEY_TEAMC, false);
        sound.PlaySound(SoundManager.SOUND_BACK);
      } else {
        teamList.add(Team.TEAMC);
        b.setBackgroundResource(R.color.teamC_text);
        b.setTextColor(GameSetup.this.getResources().getColor(
            R.color.teamC_secondary));
        GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.PREFKEY_TEAMC, true);
        sound.PlaySound(SoundManager.SOUND_CONFIRM);
      }
    }
  };

  /**
   * Watches the button that adds the fourth team to the list
   */
  private final OnClickListener AddTeamDListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "AddTeamDListener onClick()");
      }
      Button b = (Button) v;

      // Play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) GameSetup.this
          .getApplication();
      SoundManager sound = application.GetSoundManager();

      if (teamList.remove(Team.TEAMD)) {
        b.setBackgroundResource(R.color.inactiveButton);
        b.setTextColor(GameSetup.this.getResources()
            .getColor(R.color.genericBG));
        GameSetup.gameSetupPrefEditor
            .putBoolean(GameSetup.PREFKEY_TEAMD, false);
        sound.PlaySound(SoundManager.SOUND_BACK);
      } else {
        teamList.add(Team.TEAMD);
        b.setBackgroundResource(R.color.teamD_text);
        b.setTextColor(GameSetup.this.getResources().getColor(
            R.color.teamD_secondary));
        GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.PREFKEY_TEAMD, true);
        sound.PlaySound(SoundManager.SOUND_CONFIRM);
      }
    }
  };

  /**
   * @return The animation that fades in helper text screen
   */
  private Animation FadeInHelpText(long delay) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "FadeInHelpText()");
    }
    Animation fade = new AlphaAnimation(0.0f, 1.0f);
    fade.setStartOffset(delay);
    fade.setDuration(2000);
    return fade;
  }

  /**
   * onCreate - initializes the activity to display the results of the turn.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    // Initialize flag to carry music from one activity to the next
    continueMusic = false;

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Setup the view
    this.setContentView(R.layout.gamesetup);

    // Get the current game setup preferences
    GameSetup.gameSetupPrefs = getSharedPreferences(PREFS_NAME, 0);
    GameSetup.gameSetupPrefEditor = GameSetup.gameSetupPrefs.edit();

    // Set radio button labels
    RadioButton radio;
    for (int i = 0; i < GameSetup.ROUND_RADIOS.length; ++i) {
      radio = (RadioButton) this.findViewById(GameSetup.ROUND_RADIOS[i][0]);
      radio.setText(String.valueOf(GameSetup.ROUND_RADIOS[i][1]));
    }

    // Set the radio button to the previous preference
    int radio_default = GameSetup.gameSetupPrefs.getInt(GameSetup.RADIO_INDEX,
        1);
    radio = (RadioButton) this
        .findViewById(GameSetup.ROUND_RADIOS[radio_default][0]);
    radio.setChecked(true);

    // Bind view buttons
    Button startGameButton = (Button) this
        .findViewById(R.id.GameSetup_StartGameButton);
    startGameButton.setOnClickListener(StartGameListener);

    // Add listeners
    Button teamAButton = (Button) this.findViewById(R.id.GameSetup_ButtonTeamA);
    teamAButton.setOnClickListener(AddTeamAListener);
    Button teamBButton = (Button) this.findViewById(R.id.GameSetup_ButtonTeamB);
    teamBButton.setOnClickListener(AddTeamBListener);
    Button teamCButton = (Button) this.findViewById(R.id.GameSetup_ButtonTeamC);
    teamCButton.setOnClickListener(AddTeamCListener);
    Button teamDButton = (Button) this.findViewById(R.id.GameSetup_ButtonTeamD);
    teamDButton.setOnClickListener(AddTeamDListener);

    // Look at the setup preferences at each team variable and set the team
    // defaults appropriately
    // Set team A default selection
    if (GameSetup.gameSetupPrefs.getBoolean(PREFKEY_TEAMA, false)) {
      teamList.add(Team.TEAMA);
    } else {
      teamAButton.setBackgroundResource(R.color.inactiveButton);
      teamAButton.setTextColor(GameSetup.this.getResources().getColor(
          R.color.genericBG));
    }
    // Set team B default selection
    if (GameSetup.gameSetupPrefs.getBoolean(PREFKEY_TEAMB, false)) {
      teamList.add(Team.TEAMB);
    } else {
      teamBButton.setBackgroundResource(R.color.inactiveButton);
      teamBButton.setTextColor(GameSetup.this.getResources().getColor(
          R.color.genericBG));
    }
    // Set team C default selection
    if (GameSetup.gameSetupPrefs.getBoolean(PREFKEY_TEAMC, false)) {
      teamList.add(Team.TEAMC);
    } else {
      teamCButton.setBackgroundResource(R.color.inactiveButton);
      teamCButton.setTextColor(GameSetup.this.getResources().getColor(
          R.color.genericBG));
    }
    // Set team D default selection
    if (GameSetup.gameSetupPrefs.getBoolean(PREFKEY_TEAMD, false)) {
      teamList.add(Team.TEAMD);
    } else {
      teamDButton.setBackgroundResource(R.color.inactiveButton);
      teamDButton.setTextColor(GameSetup.this.getResources().getColor(
          R.color.genericBG));
    }

    // Do helper text animations
    TextView helpText = (TextView) this
        .findViewById(R.id.GameSetup_HelpText_Team);
    helpText.setAnimation(this.FadeInHelpText(1000));
    helpText = (TextView) this.findViewById(R.id.GameSetup_HelpText_Turn);
    helpText.setAnimation(this.FadeInHelpText(3000));
  }

  /**
   * Getter that returns the index of the checked radio button.
   * 
   * @return index of the checked radio button (-1 if none found)
   */
  private int getCheckedRadioIndex() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getCheckedRadioValue()");
    }

    int checkedRadioIndex = -1;
    // Iterate through radio buttons to find the one that is checked and return
    // it.
    for (int i = 0; i < GameSetup.ROUND_RADIOS.length; i++) {
      RadioButton test = (RadioButton) GameSetup.this
          .findViewById(GameSetup.ROUND_RADIOS[i][0]);
      if (test.isChecked()) {
        checkedRadioIndex = i;
        break;
      }
    }

    return checkedRadioIndex;
  }

  /**
   * Handle creation of team warning dialog, used to prevent starting a game
   * with too few teams. returns Dialog object explaining team error
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreateDialog(" + id + ")");
    }
    Dialog dialog = null;
    AlertDialog.Builder builder = null;

    switch (id) {
    case DIALOG_TEAMERROR:
      builder = new AlertDialog.Builder(this);
      builder.setMessage("You must have at least two teams to start the game.")
          .setCancelable(false).setTitle("Need more teams!").setPositiveButton(
              "Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
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
      GameSetup.this.continueMusic = true;
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
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "onPause()");
      }
    }
    super.onPause();

    // Pause the music unless going to an Activity where it is supposed to
    // continue through
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.GetMusicPlayer();
    if (!continueMusic && mp.isPlaying()) {
      mp.pause();
    }

    // Store off game's attributes as preferences. This is done in Pause to
    // maintain selections
    // when they press "back" to main title then return.
    GameSetup.gameSetupPrefEditor.putInt(GameSetup.RADIO_INDEX, GameSetup.this
        .getCheckedRadioIndex());
    GameSetup.gameSetupPrefEditor.commit();
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
    MediaPlayer mp = application.GetMusicPlayer();
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    if (!mp.isPlaying() && sp.getBoolean("music_enabled", true)) {
      mp.start();
    }

    continueMusic = false;
  }
}
