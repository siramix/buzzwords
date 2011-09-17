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
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

/**
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and turns
 * 
 * @author Siramix Labs
 */
public class GameSetup extends Activity {

  static final int DIALOG_TEAMERROR = 0;
  private LinkedList<Team> mTeamList = new LinkedList<Team>();
  private static SharedPreferences mGameSetupPrefs;
  private static SharedPreferences.Editor mGameSetupPrefEditor;

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
  private boolean mContinueMusic = false;

  /**
   * logging tag
   */
  public static String TAG = "GameSetup";

  /**
   * Watches the button that handles hand-off to the Turn activity.
   */
  private final OnClickListener mStartGameListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "StartGameListener onClick()");
      }

      // Validate team numbers
      if (GameSetup.this.mTeamList.size() <= 1) {
        GameSetup.this.showDialog(DIALOG_TEAMERROR);
        return;
      }

      // Store off game's attributes as preferences
      GameSetup.mGameSetupPrefEditor.putInt(GameSetup.RADIO_INDEX,
          GameSetup.this.getCheckedRadioIndex());
      GameSetup.mGameSetupPrefEditor.commit();

      // Create a GameManager to manage attributes about the current game.
      // the while loop around the try-catch block makes sure the database
      // has loaded before actually starting the game.
      BuzzWordsApplication application = (BuzzWordsApplication) GameSetup.this
          .getApplication();
      boolean keepLooping = true;
      while (keepLooping) {
        try {
          GameManager gm = new GameManager(GameSetup.this);
          gm.startGame(mTeamList, ROUND_RADIOS[GameSetup.this
              .getCheckedRadioIndex()][1]);
          application.setGameManager(gm);
          keepLooping = false;
        } catch (SQLiteException e) {
          keepLooping = true;
        }
      }

      // Launch into Turn activity
      startActivity(new Intent(getApplication().getString(R.string.IntentTurn),
          getIntent().getData()));

      // Stop the music
      MediaPlayer mp = application.getMusicPlayer();
      mp.stop();
    }
  };

  /**
   * Watches the button that adds the first team to the list
   */
  private final OnClickListener mAddTeamAListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "AddTeamAListener onClick()");
      }
      Button b = (Button) v;
      View editIcon = GameSetup.this.findViewById(R.id.GameSetup_TeamEditIconA);
      SoundManager sm = SoundManager.getInstance(GameSetup.this
          .getBaseContext());

      if (mTeamList.remove(Team.TEAMA)) {
        // Update UI
        disableTeamUI(b, editIcon);

        // Store off this selection so it is remember between activities
        GameSetup.mGameSetupPrefEditor.putBoolean(GameSetup.PREFKEY_TEAMA,
            false);

        // Play back sound on remove
        sm.playSound(SoundManager.Sound.BACK);

      } else {
        // Add the team to the list
        mTeamList.add(Team.TEAMA);

        // Update UI
        enableTeamUI(b, editIcon);

        // Store off this selection so it is remember between activities
        GameSetup.mGameSetupPrefEditor
            .putBoolean(GameSetup.PREFKEY_TEAMA, true);

        // Play confirm sound on add
        sm.playSound(SoundManager.Sound.CONFIRM);

      }
    }
  };

  /**
   * Watches the button that adds the second team to the list
   */
  private final OnClickListener mAddTeamBListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "AddTeamBListener onClick()");
      }
      Button b = (Button) v;
      View editIcon = GameSetup.this.findViewById(R.id.GameSetup_TeamEditIconB);
      SoundManager sm = SoundManager.getInstance(GameSetup.this
          .getBaseContext());

      if (mTeamList.remove(Team.TEAMB)) {

        // Update UI
        disableTeamUI(b, editIcon);

        // Store off this selection so it is remember between activities
        GameSetup.mGameSetupPrefEditor.putBoolean(GameSetup.PREFKEY_TEAMB,
            false);

        // Play back sound on remove
        sm.playSound(SoundManager.Sound.BACK);

      } else {
        // Add the team to the list
        mTeamList.add(Team.TEAMB);
        // Update UI
        enableTeamUI(b, editIcon);

        // Store off this selection so it is remember between activities
        GameSetup.mGameSetupPrefEditor
            .putBoolean(GameSetup.PREFKEY_TEAMB, true);

        // Play confirm sound on add
        sm.playSound(SoundManager.Sound.CONFIRM);
      }
    }
  };

  /**
   * Watches the button that adds the third team to the list
   */
  private final OnClickListener mAddTeamCListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "AddTeamCListener onClick()");
      }

      EditText textField = (EditText) GameSetup.this
          .findViewById(R.id.GameSetup_TeamNameC);
      View editText = (View) GameSetup.this
          .findViewById(R.id.GameSetup_ButtonTeamC_EditName);
      View editTextIcon = (View) GameSetup.this
          .findViewById(R.id.GameSetup_TeamEditIconC);

      SoundManager sm = SoundManager.getInstance(GameSetup.this
          .getBaseContext());
      // Play confirm sound
      if (mTeamList.remove(Team.TEAMC)) {
        textField.setBackgroundResource(R.color.inactiveButton);
        textField.setTextColor(GameSetup.this.getResources().getColor(
            R.color.genericBG));
        GameSetup.mGameSetupPrefEditor.putBoolean(GameSetup.PREFKEY_TEAMC,
            false);
        sm.playSound(SoundManager.Sound.BACK);

        // Hide edit team name button
        editText.setVisibility(View.INVISIBLE);
        // Disallow selection on text element
        textField.setEnabled(false);
        // Hide edit icon
        editTextIcon.setVisibility(View.INVISIBLE);
      } else {
        mTeamList.add(Team.TEAMC);
        textField.setBackgroundResource(mTeamList.getLast().getPrimaryColor());
        textField.setTextColor(GameSetup.this.getResources().getColor(
            mTeamList.getLast().getSecondaryColor()));
        GameSetup.mGameSetupPrefEditor
            .putBoolean(GameSetup.PREFKEY_TEAMC, true);
        sm.playSound(SoundManager.Sound.CONFIRM);

        // Show edit team name button
        editText.setVisibility(View.VISIBLE);
        // Allow name to be edited
        textField.setEnabled(true);
        // Show edit icon
        editTextIcon.setVisibility(View.VISIBLE);
      }
    }
  };

  /**
   * Edit name for TeamC
   */
  private final OnClickListener mEditTeamCName = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "mEditTeamCName onClick()");
      }

      EditText textField = (EditText) GameSetup.this
          .findViewById(R.id.GameSetup_TeamNameC);
      InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
      imm.showSoftInput(textField, 0);

      textField.setCursorVisible(true);

      textField.requestFocus();

      View frame = (View) GameSetup.this.findViewById(R.id.FrameLayout1);
      frame.setBackgroundColor(GameSetup.this.getResources().getColor(
          R.color.white));

      View addTeam = (View) GameSetup.this
          .findViewById(R.id.GameSetup_ButtonTeamC);
      addTeam.setVisibility(View.INVISIBLE);

    }
  };

  private OnEditorActionListener mEditTextListener = new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      if (actionId == EditorInfo.IME_ACTION_DONE) {

        v.setCursorVisible(false);
        v.setBackgroundResource(mTeamList.getLast().getPrimaryColor());
        View frame = (View) GameSetup.this.findViewById(R.id.FrameLayout1);
        frame.setBackgroundColor(GameSetup.this.getResources().getColor(
            R.color.black));

        View addTeam = (View) GameSetup.this
            .findViewById(R.id.GameSetup_ButtonTeamC);
        addTeam.setVisibility(View.VISIBLE);
      }
      return false;
    }
  };

  /*
   * Helper function used to set UI properties when a team is removed
   */
  private void disableTeamUI(Button teamButton, View editIcon) {
    // Change background colors
    teamButton.setBackgroundResource(R.color.inactiveButton);
    teamButton.setTextColor(GameSetup.this.getResources().getColor(
        R.color.genericBG));

    // Hide team name edit icon when team is removed
    editIcon.setVisibility(View.INVISIBLE);
  }

  /*
   * Helper function used to set UI properties when a team is added
   */
  private void enableTeamUI(Button teamButton, View editIcon) {
    // Change background colors
    teamButton.setBackgroundResource(mTeamList.getLast().getPrimaryColor());
    teamButton.setTextColor(GameSetup.this.getResources().getColor(
        mTeamList.getLast().getSecondaryColor()));

    // Show team name edit icon when team is added
    editIcon.setVisibility(View.VISIBLE);
  }

  /**
   * Creates the animation that fades in the helper text
   * 
   * @return the animation that fades in the helper text
   */
  private Animation fadeInHelpText(long delay) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "FadeInHelpText()");
    }
    Animation fade = new AlphaAnimation(0.0f, 1.0f);
    fade.setStartOffset(delay);
    fade.setDuration(2000);
    return fade;
  }

  /**
   * Initializes the activity to display the results of the turn.
   * 
   * @param savedInstanceState
   *          bundle used for saved state of the activity
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    // Initialize flag to carry music from one activity to the next
    mContinueMusic = false;

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Setup the view
    this.setContentView(R.layout.gamesetup);

    // Get the current game setup preferences
    GameSetup.mGameSetupPrefs = getSharedPreferences(PREFS_NAME, 0);
    GameSetup.mGameSetupPrefEditor = GameSetup.mGameSetupPrefs.edit();

    // set fonts on titles
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");

    TextView label = (TextView) this.findViewById(R.id.GameSetup_Title);
    label.setTypeface(antonFont);
    label = (TextView) this.findViewById(R.id.GameSetup_TeamsTitle);
    label.setTypeface(antonFont);
    label = (TextView) this.findViewById(R.id.GameSetup_SubHeader_Turns_Title);
    label.setTypeface(antonFont);

    // Set radio button labels
    RadioButton radio;
    for (int i = 0; i < GameSetup.ROUND_RADIOS.length; ++i) {
      radio = (RadioButton) this.findViewById(GameSetup.ROUND_RADIOS[i][0]);
      radio.setText(String.valueOf(GameSetup.ROUND_RADIOS[i][1]));
    }

    // Set the radio button to the previous preference
    int radio_default = GameSetup.mGameSetupPrefs.getInt(GameSetup.RADIO_INDEX,
        1);
    radio = (RadioButton) this
        .findViewById(GameSetup.ROUND_RADIOS[radio_default][0]);
    radio.setChecked(true);

    // Bind view buttons
    Button startGameButton = (Button) this
        .findViewById(R.id.GameSetup_StartGameButton);
    startGameButton.setOnClickListener(mStartGameListener);

    // Add listeners
    Button teamAButton = (Button) this.findViewById(R.id.GameSetup_ButtonTeamA);
    teamAButton.setOnClickListener(mAddTeamAListener);
    Button teamBButton = (Button) this.findViewById(R.id.GameSetup_ButtonTeamB);
    teamBButton.setOnClickListener(mAddTeamBListener);
    View teamCButton = this.findViewById(R.id.GameSetup_ButtonTeamC);
    teamCButton.setOnClickListener(mAddTeamCListener);

    // EditTeamName Listeners
    View editTeamCName = (View) this
        .findViewById(R.id.GameSetup_ButtonTeamC_EditName);
    editTeamCName.setOnClickListener(mEditTeamCName);
    
    // Assign teams to TeamSelectLayouts
    TeamSelectLayout teamD = (TeamSelectLayout) this.findViewById(R.id.GameSetup_TeamDLayout);
    teamD.assignTeam(Team.TEAMD, mTeamList, mGameSetupPrefEditor, PREFKEY_TEAMD);
    if(GameSetup.mGameSetupPrefs.getBoolean(PREFKEY_TEAMD, false)) {
    	teamD.setTeamLayoutActiveness(true);
    	mTeamList.add(Team.TEAMD);
    }
    else {
    	teamD.setTeamLayoutActiveness(false);
    }

    // Set focus watcher to textField
    EditText textField = (EditText) this.findViewById(R.id.GameSetup_TeamNameC);
    textField.setOnEditorActionListener(mEditTextListener);

    // Steal focus from any of the text views... this stinks for accessibility,
    // but something needs focus
    View focusStealer = (View) this.findViewById(R.id.GameSetup_FocusStealer);
    focusStealer.requestFocus();

    // Look at the setup preferences at each team variable and set the team
    // defaults appropriately
    // Set team A default selection
    if (GameSetup.mGameSetupPrefs.getBoolean(PREFKEY_TEAMA, false)) {
      mTeamList.add(Team.TEAMA);
    } else {
      disableTeamUI(teamAButton, this
          .findViewById(R.id.GameSetup_TeamEditIconA));
    }
    // Set team B default selection
    if (GameSetup.mGameSetupPrefs.getBoolean(PREFKEY_TEAMB, false)) {
      mTeamList.add(Team.TEAMB);
    } else {
      disableTeamUI(teamBButton, this
          .findViewById(R.id.GameSetup_TeamEditIconB));
    }
    // Set team C default selection
    if (GameSetup.mGameSetupPrefs.getBoolean(PREFKEY_TEAMC, false)) {
      mTeamList.add(Team.TEAMC);
    } else {
      textField = (EditText) this.findViewById(R.id.GameSetup_TeamNameC);
      textField.setBackgroundResource(R.color.inactiveButton);
      textField.setTextColor(GameSetup.this.getResources().getColor(
          R.color.genericBG));
    }

    // Do helper text animations
    TextView helpText = (TextView) this
        .findViewById(R.id.GameSetup_HelpText_Team);
    helpText.setAnimation(this.fadeInHelpText(1000));
    helpText = (TextView) this.findViewById(R.id.GameSetup_HelpText_Turn);
    helpText.setAnimation(this.fadeInHelpText(3000));
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
   * 
   * @return whether the event has been consumed or not
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled()) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "BackKeyUp()");
      }
      // Flag to keep music playing
      GameSetup.this.mContinueMusic = true;
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
    MediaPlayer mp = application.getMusicPlayer();
    if (!mContinueMusic && mp.isPlaying()) {
      mp.pause();
    }

    // Store off game's attributes as preferences. This is done in Pause to
    // maintain selections
    // when they press "back" to main title then return.
    GameSetup.mGameSetupPrefEditor.putInt(GameSetup.RADIO_INDEX, GameSetup.this
        .getCheckedRadioIndex());
    GameSetup.mGameSetupPrefEditor.commit();
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

    mContinueMusic = false;
  }
}
