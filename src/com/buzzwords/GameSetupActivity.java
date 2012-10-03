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

import com.buzzwords.GameManager;
import com.buzzwords.R;
import com.buzzwords.Consts;
import com.buzzwords.GameManager.GameType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;

/**
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and turns
 * 
 * @author Siramix Labs
 */
public class GameSetupActivity extends Activity {

	static final int DIALOG_TEAMERROR = 0;
	private LinkedList<Team> mTeamList = new LinkedList<Team>();
	private static SharedPreferences mGameSetupPrefs;
	private static SharedPreferences.Editor mGameSetupPrefEditor;
	private static SharedPreferences mPackPrefs;

	// Store game limit parameters
	private final int mGAMELIMIT_MIN = 1;
	private final int mGAMELIMIT_MAX = 99;
	private int mGameLimits[] = new int[GameManager.GameType.values().length];
	private int mGameType;
	
	// References to views to limit calls to FindViewById
	private TextView mGameLimitView;
	private RadioGroup mRadioGroup;

  // Track the current shown toast
  private Toast mHelpToast = null;
  
	// Ids for TeamSelectLayouts
	final int[] TEAM_SELECT_LAYOUTS = new int[] { R.id.GameSetup_TeamALayout,
			R.id.GameSetup_TeamBLayout, R.id.GameSetup_TeamCLayout,
			R.id.GameSetup_TeamDLayout };

	// Preference keys (indicating quadrant)
	public static final String PREFS_NAME = "gamesetupprefs";

	// Flag to play music into the next Activity
	private boolean mContinueMusic = false;

	// Request code for EditTeam activity result
	static final int EDITTEAMNAME_REQUEST_CODE = 1;

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
			v.setEnabled(false);

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
			// TODO I don't think this looping is necessary anymore
			BuzzWordsApplication application = (BuzzWordsApplication) GameSetupActivity.this
					.getApplication();
			boolean keepLooping = true;
			while (keepLooping) {
				try {
					GameManager gm = new GameManager(GameSetupActivity.this);
					gm.maintainDeck();
					gm.startGame(mTeamList,
							GameType.values()[mGameType],
							mGameLimits[mGameType]);
					application.setGameManager(gm);
					keepLooping = false;
				} catch (SQLiteException e) {
					keepLooping = true;
				}
			}

			// Launch into Turn activity
			startActivity(new Intent(getApplication().getString(
					R.string.IntentTurn), getIntent().getData()));

			// Stop the music
			MediaPlayer mp = application.getMusicPlayer();
			mp.stop();
		}
	};

	/*
	 * Edit team name listener to launch Edit Team name dialog
	 */
	private final OnTeamEditedListener mTeamEditedListener = new OnTeamEditedListener() {
		public void onTeamEdited(Team team) {
			SoundManager sm = SoundManager.getInstance(GameSetupActivity.this
					.getBaseContext());
			sm.playSound(SoundManager.Sound.CONFIRM);

			Intent editTeamNameIntent = new Intent(
					getString(R.string.IntentEditTeamName), getIntent()
							.getData());
			editTeamNameIntent
					.putExtra(getString(R.string.teamBundleKey), team);
			startActivityForResult(editTeamNameIntent,
					EDITTEAMNAME_REQUEST_CODE);

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
		public void onTeamAdded(Team team, boolean isTeamOn) {
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
	 * Watches the button to add a point to Limit
	 */
	private final OnClickListener mAddPointLimit = new OnClickListener() {
		public void onClick(View v) {
			if (BuzzWordsApplication.DEBUG) {
				Log.d(TAG, "mAddPointLimit onClick()");
			}
			
			int newLimit = mGameLimits[mGameType] + (Integer) v.getTag();

			if (newLimit <= mGAMELIMIT_MAX && newLimit >= mGAMELIMIT_MIN) {
				mGameLimits[mGameType] = newLimit;
				mGameLimitView.setText(Integer.toString(mGameLimits[mGameType]));

				// play confirm sound when points are added
				SoundManager sm = SoundManager
						.getInstance(GameSetupActivity.this.getBaseContext());
				sm.playSound(SoundManager.Sound.CONFIRM);
			}
		}
	};

  /**
   * Watches the hint butons for clicks
   */
  private final OnClickListener mHintListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "mHintListener onClick()");
      }
      showToast((String) v.getTag());  
    }
  };
	

  /**
   * Watches the radio group for game type changes
   */
  private final OnClickListener mGameTypeListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "mAddPointLimit onClick()");
      }
      
      mGameType = (Integer) v.getTag();
      updateViewForNewGameType(mGameType);

      // play confirm sound
      SoundManager sm = SoundManager
          .getInstance(GameSetupActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);
      }
    };

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

				// Set the team name and update the layout
				curTeam.setName(curTeamName);
				TeamSelectLayout teamSelect = (TeamSelectLayout) this
						.findViewById(TEAM_SELECT_LAYOUTS[curTeam.ordinal()]);
				teamSelect.setTeam(curTeam);

				// Set the name as a pref
				mGameSetupPrefEditor.putString(curTeam.getDefaultName(),
						curTeam.getName());
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
  }

	/**
	 * Setup any special properties on views such as onClick events and tags
	 */
	private void setupUIProperties() {

    // Assign listeners to teamSelectLayouts
    TeamSelectLayout teamSelect;
    for (int i = 0; i < TEAM_SELECT_LAYOUTS.length; ++i) {
      teamSelect = (TeamSelectLayout) this
          .findViewById(TEAM_SELECT_LAYOUTS[i]);
      teamSelect.setOnTeamEditedListener(mTeamEditedListener);
      teamSelect.setOnTeamAddedListener(mTeamAddedListener);
    }

    // Set radio button labels
    RadioButton radio;
    for( GameManager.GameType mode : GameManager.GameType.values())
    {
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
        .findViewById(R.id.GameSetup_StartGameButton);
    startGameButton.setOnClickListener(mStartGameListener);
    
    // Do hint bindings
    Button hintButton = (Button) this.findViewById(R.id.GameSetup_TeamsHintButton);
    hintButton.setOnClickListener(mHintListener);
    hintButton.setTag(getString(R.string.gameSetup_hint_teams));
    
    hintButton = (Button) this.findViewById(R.id.GameSetup_GameType_HintButton);
    hintButton.setOnClickListener(mHintListener);
    hintButton.setTag(getString(R.string.gameSetup_hint_GameType));
    
    hintButton = (Button) this.findViewById(R.id.GameSetup_GameTypeParameter_HintButton);
    hintButton.setOnClickListener(mHintListener);
    // Default hint tag to something to avoid null data
    hintButton.setTag(getString(R.string.gameSetup_hint_score));
	}
	
	/**
	 * Initialize the activity
	 * 
	 * @param savedInstanceState
	 *            bundle used for saved state of the activity
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuzzWordsApplication.DEBUG) {
			Log.d(TAG, "onCreate()");
		}

		// Setup the view
		this.setContentView(R.layout.gamesetup);

		setupViewReferences();
		setupUIProperties();		
		
		// Initialize flag to carry music from one activity to the next
		mContinueMusic = false;

		// Force volume controls to affect Media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		restorePreferences();
	}
  
  /**
   * Restores preferences into member variables and sets the view to represent them
   */
  private void restorePreferences()
  {
     // Get the current game setup preferences
    GameSetupActivity.mGameSetupPrefs = getSharedPreferences(PREFS_NAME, 0);
    GameSetupActivity.mGameSetupPrefEditor = GameSetupActivity.mGameSetupPrefs
        .edit();
    // Get our pack preferences
    GameSetupActivity.mPackPrefs = getSharedPreferences(
        Consts.PREFFILE_PACK_SELECTIONS, Context.MODE_PRIVATE);

    // Assign teams to TeamSelectLayouts
    TeamSelectLayout teamSelect;
    Team curTeam;
    for (int i = 0; i < TEAM_SELECT_LAYOUTS.length; ++i) {
      curTeam = Team.values()[i];
      String curTeamName = mGameSetupPrefs.getString(
          curTeam.getDefaultName(), curTeam.getDefaultName());
      curTeam.setName(curTeamName);
      teamSelect = (TeamSelectLayout) this
          .findViewById(TEAM_SELECT_LAYOUTS[i]);
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
    RadioButton defaultRadio = (RadioButton) mRadioGroup.getChildAt(radio_default);
    defaultRadio.setChecked(true);
    
    mGameType = radio_default;
  
    // Set default value for turns limit
    mGameLimits[0] = GameSetupActivity.mGameSetupPrefs
        .getInt(GameSetupActivity.this
            .getString(R.string.PREFKEY_GAMELIMIT_TURNS), 7);
    // Set default value for score limit
    mGameLimits[1] = GameSetupActivity.mGameSetupPrefs
        .getInt(GameSetupActivity.this
            .getString(R.string.PREFKEY_GAMELIMIT_SCORE), 21);
 
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
    if(mHelpToast == null) {
      mHelpToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
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
		if (BuzzWordsApplication.DEBUG) {
			Log.d(TAG, "onCreateDialog(" + id + ")");
		}
		Dialog dialog = null;
		AlertDialog.Builder builder = null;

		switch (id) {
		case DIALOG_TEAMERROR:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"You must have at least two teams to start the game.")
					.setCancelable(false)
					.setTitle("Need more teams!")
					.setPositiveButton("Okay",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
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
			mContinueMusic = true;
		}

		return super.onKeyUp(keyCode, event);
	}

	/**
	 * Override onPause to prevent activity specific processes from running
	 * while app is in background
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
		if (BuzzWordsApplication.DEBUG) {
			Log.d(TAG, "onResume()");
		}
		super.onResume();

		// Resume Title Music
		BuzzWordsApplication application = (BuzzWordsApplication) this
				.getApplication();
		MediaPlayer mp = application.getMusicPlayer();
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this.getBaseContext());
		if (!mp.isPlaying() && sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
			mp.start();
		}

		mContinueMusic = false;
	}
}
