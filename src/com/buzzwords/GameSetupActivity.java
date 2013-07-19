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

import java.util.ArrayList;

import com.buzzwords.GameManager;
import com.buzzwords.R;
import com.buzzwords.Consts;
import com.buzzwords.GameManager.GameType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and turns
 * 
 * @author Siramix Labs
 */
public class GameSetupActivity extends Activity {

  static final int DIALOG_TEAMERROR = 0;
  private ArrayList<Team> mTeamList = new ArrayList<Team>();
  private static SharedPreferences mGameSetupPrefs;
  private static SharedPreferences.Editor mGameSetupPrefEditor;
  
  /**
   * Flag to prevent other activities from opening after one is launched
   */
  private boolean mIsActivityClosing;
  
  // Store game limit parameters
  private final int mGAMELIMIT_MIN = 1;
  private final int mGAMELIMIT_MAX = 99;
  private int mGameLimits[] = new int[GameManager.GameType.values().length];
  private int mGameType;

  // References to views to limit calls to FindViewById
  private TextView mGameLimitView;
  private RadioGroup mRadioGroup;
  private TutorialLayout mTutorialLayout;

  // Track the current shown toast
  private Toast mHelpToast = null;

  // Ids for TeamSelectLayouts
  final int[] TEAM_SELECT_LAYOUTS = new int[] { R.id.GameSetup_TeamALayout,
      R.id.GameSetup_TeamBLayout, R.id.GameSetup_TeamCLayout,
      R.id.GameSetup_TeamDLayout };

  // Preference keys (indicating quadrant)
  public static final String PREFS_NAME = "gamesetupprefs";
  
  /**
   * Track which part of the tutorial the user is in.
   */
  private TutorialPage mTutorialPage;

  /**
   * Enum gives a name to each tutorial page
   */
  private enum TutorialPage {GAME, SCREEN, TEAMS, WIN, END, NOADVANCE};
  
  // Flag to play music into the next Activity
  private boolean mContinueMusic = false;

  // Request code for EditTeam activity result
  static final int EDITTEAMNAME_REQUEST_CODE = 1;

  /**
   * logging tag
   */
  public static String TAG = "GameSetup";

  /**
   * Watches the button that handles hand-off to the next activity.
   */
  private final OnClickListener mNextActivityListener = new OnClickListener() {
    public void onClick(View v) {
      // Throw out any queued onClicks.
      if (!v.isEnabled() || mIsActivityClosing) {
        return;
      }

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(GameSetupActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      // Validate team numbers
      if (GameSetupActivity.this.mTeamList.size() <= 1) {
        GameSetupActivity.this.showDialog(DIALOG_TEAMERROR);
        v.setEnabled(true);
        return;
      }

      // Store off game's attributes as preferences
      GameSetupActivity.mGameSetupPrefEditor.putInt(
          getString(R.string.PREFKEY_GAMETYPE),
          GameSetupActivity.this.mGameType);
      GameSetupActivity.mGameSetupPrefEditor.commit();

      // Create a GameManager to manage attributes about the current game.
      // the while loop around the try-catch block makes sure the database
      // has loaded before actually starting the game.
      BuzzWordsApplication application = (BuzzWordsApplication) GameSetupActivity.this
          .getApplication();
      boolean keepLooping = true;
      while (keepLooping) {
        try {
          GameManager gm = new GameManager(GameSetupActivity.this);
          gm.setupGameAttributes(mTeamList, GameType.values()[mGameType],
              mGameLimits[mGameType]);
          application.setGameManager(gm);
          keepLooping = false;
          
          // Disable other buttons and this one to prevent double clicks
          mIsActivityClosing = true;
          v.setEnabled(false);
    
        } catch (SQLiteException e) {
          keepLooping = true;
        }
      }

      mContinueMusic = true;
      
      // Launch into PackPurchase activity
      startActivity(new Intent(getApplication().getString(R.string.IntentPackPurchase),
          getIntent().getData()));
    }
  };

  /*
   * Edit team name listener to launch Edit Team name dialog
   */
  private final OnTeamEditedListener mTeamEditedListener = new OnTeamEditedListener() {
    public void onTeamEdited(Team team) {
      // Throw out call if they have started the game
      if(mIsActivityClosing){
        return;
      }
      
      SoundManager sm = SoundManager.getInstance(GameSetupActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      Intent editTeamNameIntent = new Intent(
          getString(R.string.IntentEditTeamName), getIntent().getData());
      editTeamNameIntent.putExtra(getString(R.string.teamBundleKey), team);
      startActivityForResult(editTeamNameIntent, EDITTEAMNAME_REQUEST_CODE);

      /*
       * // Launch into Turn activity startActivity(new
       * Intent(getApplication().getString(R.string.IntentEditTeamName),
       * getIntent().getData()));
       */
      mContinueMusic = true;
    }
  };

  /*
   * Listener that watches the TeamSelectLayouts for events when the teams are
   * added or removed. It modifies the preferences and the list of teams
   * accordingly.
   */
  private final OnTeamAddedListener mTeamAddedListener = new OnTeamAddedListener() {
    public void onTeamAdded(View v, Team team) {
      // Throw out call if they have started the game
      if(mIsActivityClosing){
        return;
      }

      // Toggle the view's display status
      boolean isTeamOn = !((TeamSelectLayout)v).getActiveness();
      ((TeamSelectLayout)v).setActiveness(isTeamOn);
      
      SoundManager sm = SoundManager.getInstance((GameSetupActivity.this
          .getBaseContext()));

      if (isTeamOn) {
        // Add the team to the list
        mTeamList.add(team);
        // Store off this selection so it is remember between activities
        mGameSetupPrefEditor.putBoolean(team.getPreferenceKey(), true);
        // Play confirm sound on add
        sm.playSound(SoundManager.Sound.CONFIRM);
      } else {
        // Remove the team from the list
        mTeamList.remove(team);
        // Store off this selection so it is remember between activities
        mGameSetupPrefEditor.putBoolean(team.getPreferenceKey(), false);
        // Play back sound on remove
        sm.playSound(SoundManager.Sound.BACK);
      }
    }
  };

  /**
   * Watches the button to add a point to Limit (can add a negative)
   */
  private final OnClickListener mAddPointLimit = new OnClickListener() {
    public void onClick(View v) {
      // Throw out call if they have started the game
      if(mIsActivityClosing){
        return;
      }

      int newLimit = mGameLimits[mGameType] + (Integer) v.getTag();

      if (newLimit <= mGAMELIMIT_MAX && newLimit >= mGAMELIMIT_MIN) {
        mGameLimits[mGameType] = newLimit;
        mGameLimitView.setText(Integer.toString(mGameLimits[mGameType]));

        // play confirm sound when points are added
        SoundManager sm = SoundManager.getInstance(GameSetupActivity.this
            .getBaseContext());
        sm.playSound(SoundManager.Sound.CONFIRM);
      }
    }
  };

  /**
   * Watches the hint butons for clicks
   */
  private final OnClickListener mHintListener = new OnClickListener() {
    public void onClick(View v) {
      // Throw out call if they have started the game
      if(mIsActivityClosing){
        return;
      }
      showToast((String) v.getTag());
    }
  };

  /**
   * Watches the radio group for game type changes
   */
  private final OnClickListener mGameTypeListener = new OnClickListener() {
    public void onClick(View v) {
      // Throw out call if they have started the game
      if(mIsActivityClosing){
        return;
      }

      mGameType = (Integer) v.getTag();
      updateViewForNewGameType(mGameType);

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(GameSetupActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);
    }
  };
  
  /**
   * AdvanceTutorialListener advances to the next page in the tutorial when
   * it is clicked.
   */
  private OnClickListener mAdvanceTutorialListener = new OnClickListener() {
    public void onClick(View v) {
      // Throw out any queued onClicks.
      if(!v.isEnabled()){
        return;
      }
      
      if(mTutorialPage != TutorialPage.NOADVANCE){
        advanceTutorial();  
      }
    }
  };
  
  /**
   * Initializes and starts the tutorial
   */
  private void startTutorial()
  {
    mTutorialPage = TutorialPage.GAME;
    advanceTutorial();
  }

  /**
   * Advance the tutorial and the content to the next stage
   */
  private void advanceTutorial() {
    // Sets the content and the next tutorial page for the given tutorial page
    switch (mTutorialPage) {
    case GAME:
      mTutorialLayout.setContent(
          getResources().getString(R.string.tutorial_gamesetup_game),
          TutorialLayout.BOTTOM);
      mTutorialPage = TutorialPage.SCREEN;
      break;
    case SCREEN:
      mTutorialLayout.setContent(
          getResources().getString(R.string.tutorial_gamesetup_screen),
          TutorialLayout.BOTTOM);
      mTutorialPage = TutorialPage.TEAMS;
      break;
    case TEAMS:
      mTutorialLayout.setContent(findViewById(R.id.GameSetup_TeamsGroup),
          getResources().getString(R.string.tutorial_gamesetup_teams),
          TutorialLayout.BOTTOM);
      mTutorialPage = TutorialPage.WIN;
      break;
    case WIN:
      mTutorialLayout.setContent(findViewById(R.id.GameSetup_GameType_Group),
          getResources().getString(R.string.tutorial_gamesetup_win),
          TutorialLayout.BOTTOM);
      mTutorialPage = TutorialPage.END;
      break;
    case END:
      // Flag tutorial as seen
      SharedPreferences sp = PreferenceManager
          .getDefaultSharedPreferences(getBaseContext());
      SharedPreferences.Editor spEditor = sp.edit();
      spEditor.putBoolean(Consts.TutorialPrefkey.SETUP.getKey(), false);
      spEditor.commit();
      
      mTutorialLayout.hide();
      mTutorialPage = TutorialPage.NOADVANCE;
      break;
    case NOADVANCE:
      break;
    }
  }

  /**
   * This function is called when the EditTeamName activity finishes. It
   * refreshes all Layouts.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == EDITTEAMNAME_REQUEST_CODE
        && resultCode == Activity.RESULT_OK && data.getExtras() != null) {

      // Get team and team name from dialog
      String curTeamName = data
          .getStringExtra(getString(R.string.teamNameBundleKey));
      Team curTeam = (Team) data
          .getSerializableExtra(getString(R.string.teamBundleKey));

      if (curTeamName != null && curTeam != null) {

        String defaultName = getString(curTeam.getDefaultName());
        // If they blanked out the name, restore it to default
        if (curTeamName.length() == 0) {
          curTeamName = defaultName;
        }
        // Set the team name and update the layout
        curTeam.setName(curTeamName);
        TeamSelectLayout teamSelect = (TeamSelectLayout) this
            .findViewById(TEAM_SELECT_LAYOUTS[curTeam.ordinal()]);
        teamSelect.setTeam(curTeam);

        // Set the name as a pref
        mGameSetupPrefEditor.putString(defaultName, curTeamName);
        mGameSetupPrefEditor.commit();

      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  /*
   * Here we establish permanent references to views to limit calls to
   * findViewById()
   */
  private void setupViewReferences() {
    mGameLimitView = (TextView) findViewById(R.id.GameSetup_GameTypeParameter_Value);
    mRadioGroup = (RadioGroup) findViewById(R.id.GameSetup_GameType_RadioGroup);
    mTutorialLayout = (TutorialLayout) findViewById(R.id.GameSetup_TutorialLayout);
  }

  /**
   * Setup any special properties on views such as onClick events and tags
   */
  private void setupUIProperties() {

    // Assign listeners to teamSelectLayouts
    TeamSelectLayout teamSelect;
    for (int i = 0; i < TEAM_SELECT_LAYOUTS.length; ++i) {
      teamSelect = (TeamSelectLayout) this.findViewById(TEAM_SELECT_LAYOUTS[i]);
      teamSelect.setOnTeamEditedListener(mTeamEditedListener);
      teamSelect.setOnTeamAddedListener(mTeamAddedListener);
    }

    // Set radio button labels
    RadioButton radio;
    for (GameManager.GameType mode : GameManager.GameType.values()) {
      radio = (RadioButton) mRadioGroup.getChildAt(mode.ordinal());
      radio.setText(mode.getName(getBaseContext()));
      radio.setOnClickListener(mGameTypeListener);
      radio.setTag(mode.ordinal());
    }

    Button button = (Button) findViewById(R.id.GameSetup_GameTypeParameter_Minus);
    button.setTag(-1);
    button.setOnClickListener(mAddPointLimit);
    button = (Button) findViewById(R.id.GameSetup_GameTypeParameter_Plus);
    button.setTag(1);
    button.setOnClickListener(mAddPointLimit);

    // Bind start buttons
    Button startGameButton = (Button) this
        .findViewById(R.id.GameSetup_NextButton);
    startGameButton.setOnClickListener(mNextActivityListener);

    // Do hint bindings
    Button hintButton = (Button) this
        .findViewById(R.id.GameSetup_TeamsHintButton);
    hintButton.setOnClickListener(mHintListener);
    hintButton.setTag(getString(R.string.gameSetup_hint_teams));

    hintButton = (Button) this.findViewById(R.id.GameSetup_GameType_HintButton);
    hintButton.setOnClickListener(mHintListener);
    hintButton.setTag(getString(R.string.gameSetup_hint_GameType));

    hintButton = (Button) this
        .findViewById(R.id.GameSetup_GameTypeParameter_HintButton);
    hintButton.setOnClickListener(mHintListener);
    // Default hint tag to something to avoid null data
    hintButton.setTag(getString(R.string.gameSetup_hint_score));
  }

  /**
   * Initialize the activity
   * 
   * @param savedInstanceState
   *          bundle used for saved state of the activity
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Setup the view
    this.setContentView(R.layout.gamesetup);

    setupViewReferences();
    setupUIProperties();

    // Initialize flag to carry music from one activity to the next
    mContinueMusic = false;

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    restorePreferences();
    
    // Setup and start the tutorial
    mTutorialLayout.setClickListener(mAdvanceTutorialListener);
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(getBaseContext());
    boolean showTutorial = sp.getBoolean(
        Consts.TutorialPrefkey.SETUP.getKey(), true);
    if (showTutorial) {
      startTutorial();
    }
  }

  /**
   * Restores preferences into member variables and sets the view to represent
   * them
   */
  private void restorePreferences() {
    // Get the current game setup preferences
    GameSetupActivity.mGameSetupPrefs = getSharedPreferences(PREFS_NAME, 0);
    GameSetupActivity.mGameSetupPrefEditor = GameSetupActivity.mGameSetupPrefs
        .edit();

    // Assign teams to TeamSelectLayouts
    TeamSelectLayout teamSelect;
    Team curTeam;
    for (int i = 0; i < TEAM_SELECT_LAYOUTS.length; ++i) {
      curTeam = Team.values()[i];
      String defaultName = getString(curTeam.getDefaultName());
      String curTeamName = mGameSetupPrefs.getString(defaultName, defaultName);
      curTeam.setName(curTeamName);
      teamSelect = (TeamSelectLayout) this.findViewById(TEAM_SELECT_LAYOUTS[i]);
      teamSelect.setTeam(curTeam);

      if (GameSetupActivity.mGameSetupPrefs.getBoolean(
          curTeam.getPreferenceKey(), false)) {
        teamSelect.setActiveness(true);
        mTeamList.add(curTeam);
      } else {
        teamSelect.setActiveness(false);
      }
    }

    // Set the radio button to the previous preference
    int radio_default = GameSetupActivity.mGameSetupPrefs.getInt(
        getString(R.string.PREFKEY_GAMETYPE), 1);
    RadioButton defaultRadio = (RadioButton) mRadioGroup
        .getChildAt(radio_default);
    defaultRadio.setChecked(true);

    mGameType = radio_default;

    // Set default value for turns limit
    mGameLimits[0] = GameSetupActivity.mGameSetupPrefs.getInt(
        GameSetupActivity.this.getString(R.string.PREFKEY_GAMELIMIT_TURNS), 7);
    // Set default value for score limit
    mGameLimits[1] = GameSetupActivity.mGameSetupPrefs.getInt(
        GameSetupActivity.this.getString(R.string.PREFKEY_GAMELIMIT_SCORE), 21);

    updateViewForNewGameType(mGameType);
  }

  /**
   * Refresh any views that need to change based on the specified game type
   * 
   * @param gameType
   */
  private void updateViewForNewGameType(int gameType) {
    if (gameType == GameManager.GameType.FREEPLAY.ordinal()) {
      findViewById(R.id.GameSetup_GameTypeParamter_Group).setVisibility(
          View.GONE);
    } else {
      // Set the game type parameter header to the correct string
      TextView gameLimitHeader = (TextView) findViewById(R.id.GameSetup_GameTypeParameter_Title);
      gameLimitHeader.setText(GameManager.GameType.values()[gameType]
          .getParamName(getBaseContext()));

      // Set the text on the view to represent the value
      mGameLimitView.setText(Integer.toString(mGameLimits[gameType]));

      findViewById(R.id.GameSetup_GameTypeParamter_Group).setVisibility(
          View.VISIBLE);

      // Set hint button to give the correct hint
      int hintID;
      if (gameType == GameManager.GameType.TURNS.ordinal()) {
        hintID = R.string.gameSetup_hint_turns;
      } else {
        hintID = R.string.gameSetup_hint_score;
      }
      Button hintButton = (Button) this
          .findViewById(R.id.GameSetup_GameTypeParameter_HintButton);
      hintButton.setTag(getString(hintID));
    }
  }

  /**
   * Handle showing a toast or refreshing an existing toast
   */
  private void showToast(String text) {
    if (mHelpToast == null) {
      mHelpToast = Toast.makeText(getApplicationContext(), text,
          Toast.LENGTH_LONG);
    } else {
      mHelpToast.setText(text);
      mHelpToast.setDuration(Toast.LENGTH_LONG);
    }
    mHelpToast.show();
  }

  /**
   * Handle creation of team warning dialog, used to prevent starting a game
   * with too few teams. returns Dialog object explaining team error
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    Dialog dialog = null;
    AlertDialog.Builder builder = null;

    switch (id) {
    case DIALOG_TEAMERROR:
      builder = new AlertDialog.Builder(this);
      builder.setMessage("You must have at least two teams to start the game.")
          .setCancelable(false).setTitle("Need more teams!")
          .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
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
    super.onPause();
    Log.d(TAG, "onPause()");
    // Pause the music unless going to an Activity where it is supposed to
    // continue through
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
    if (!mContinueMusic && mp.isPlaying()) {
      application.cleanUpMusicPlayer();
    }

    // Store off game's attributes as preferences. This is done in Pause to
    // maintain selections
    // when they press "back" to main title then return.
    saveGameSetupPrefs();
  }

  /*
   * Stores off game's attributes as preferences
   */
  private void saveGameSetupPrefs() {
    GameSetupActivity.mGameSetupPrefEditor.putInt(
        getString(R.string.PREFKEY_GAMETYPE), mGameType);
    GameSetupActivity.mGameSetupPrefEditor.putInt(
        getString(R.string.PREFKEY_GAMELIMIT_TURNS), mGameLimits[0]);
    GameSetupActivity.mGameSetupPrefEditor.putInt(
        getString(R.string.PREFKEY_GAMELIMIT_SCORE), mGameLimits[1]);
    GameSetupActivity.mGameSetupPrefEditor.commit();
  }

  /**
   * Override OnResume to resume activity specific processes
   */
  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume()");

    mIsActivityClosing = false;
    
    // Re-enable buttons that were disabled to prevent double click.
    Button btn = (Button) this.findViewById(R.id.GameSetup_NextButton);
    btn.setEnabled(true); 

    // Resume Title Music
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    if (!mp.isPlaying() && sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
      mp.start();
    }

    mContinueMusic = false;
  }
}
