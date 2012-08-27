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
import com.buzzwords.Consts;

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
	private int mGameLimit;
	
	// References to views to limit calls to FindViewById
	private TextView mGameLimitView;
	private RadioGroup mRadioGroup;

	// Ids for TeamSelectLayouts
	final int[] TEAM_SELECT_LAYOUTS = new int[] { R.id.GameSetup_TeamALayout,
			R.id.GameSetup_TeamBLayout, R.id.GameSetup_TeamCLayout,
			R.id.GameSetup_TeamDLayout };

	// Preference keys (indicating quadrant)
	public static final String PREFS_NAME = "gamesetupprefs";

	// Index of the selected radio indicating number of rounds
	private static final String RADIO_INDEX = "round_radio_index";

	// Flag to play music into the next Activity
	private boolean mContinueMusic = false;

	// Request code for EditTeam activity result
	static final int EDITTEAMNAME_REQUEST_CODE = 1;

	/**
	 * logging tag
	 */
	public static String TAG = "GameSetup";

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
					GameSetupActivity.RADIO_INDEX,
					GameSetupActivity.this.getCheckedRadioIndex());
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
					gm.startGame(mTeamList, mGameLimit);
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

			if (mGameLimit < mGAMELIMIT_MAX) {
				mGameLimit += 1;
				TextView limitText = (TextView) v.getTag();
				limitText.setText(Integer.toString(mGameLimit));

				// play confirm sound when points are added
				SoundManager sm = SoundManager
						.getInstance(GameSetupActivity.this.getBaseContext());
				sm.playSound(SoundManager.Sound.CONFIRM);
			}
		}
	};

	/**
	 * Watches the button to remove a point from the Limit
	 */
	private final OnClickListener mSubtractPointLimit = new OnClickListener() {
		public void onClick(View v) {
			if (BuzzWordsApplication.DEBUG) {
				Log.d(TAG, "mSubtractPointLimit onClick()");
			}

			// Don't let them set a score limit below 1
			if (mGameLimit > mGAMELIMIT_MIN) {
				mGameLimit -= 1;
				TextView limitText = (TextView) v.getTag();
				limitText.setText(Integer.toString(mGameLimit));

				// play confirm sound when points are added
				SoundManager sm = SoundManager
						.getInstance(GameSetupActivity.this.getBaseContext());
				sm.playSound(SoundManager.Sound.BACK);
			}
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
	private void setupViewReferences()
 {
		mGameLimitView = (TextView) findViewById(R.id.GameSetup_GameParameter_Value);
		mRadioGroup = (RadioGroup) findViewById(R.id.GameSetup_GameMode_RadioGroup);
	}

	/**
	 * Setup any special properties on views such as onClick events and tags
	 */
	private void setupUIProperties() {
		Button button = (Button) findViewById(R.id.GameSetup_GameParameter_Minus);
		button.setTag(mGameLimitView);
		button.setOnClickListener(mSubtractPointLimit);
		button = (Button) findViewById(R.id.GameSetup_GameParameter_Plus);
		button.setTag(mGameLimitView);
		button.setOnClickListener(mAddPointLimit);
	}

	/**
	 * Initializes the activity to display the results of the turn.
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
		
		// Get the current game setup preferences
		GameSetupActivity.mGameSetupPrefs = getSharedPreferences(PREFS_NAME, 0);
		GameSetupActivity.mGameSetupPrefEditor = GameSetupActivity.mGameSetupPrefs
				.edit();
		// Get our pack preferences
		GameSetupActivity.mPackPrefs = getSharedPreferences(
				Consts.PREFFILE_PACK_SELECTIONS, Context.MODE_PRIVATE);

		// Set default value for score limit
		mGameLimit = GameSetupActivity.mGameSetupPrefs
				.getInt(GameSetupActivity.this
						.getString(R.string.PREFKEY_GAMELIMIT), 7);
		mGameLimitView.setText(Integer.toString(mGameLimit));

		
		// set fonts on titles
		Typeface antonFont = Typeface.createFromAsset(getAssets(),
				"fonts/Anton.ttf");

		TextView label = (TextView) this.findViewById(R.id.GameSetup_Title);
		label.setTypeface(antonFont);
		label = (TextView) this.findViewById(R.id.GameSetup_TeamsTitle);
		label.setTypeface(antonFont);
		label = (TextView) this
				.findViewById(R.id.GameSetup_SubHeader_Turns_Title);
		label.setTypeface(antonFont);
		mGameLimitView.setTypeface(antonFont);

		// Set radio button labels
		// HACK ToDo: Clean this up by using a gameMode enum probably
		RadioButton radio;
		final int[] GameModes = {0, 1, 2};
		final String[] GameModeNames = {"Turns", "Score", "Free"};
		for (int i = 0; i < mRadioGroup.getChildCount(); ++i) {
			radio = (RadioButton) mRadioGroup.getChildAt(i);
			radio.setText(GameModeNames[i]);
		}

		// Set the radio button to the previous preference
		int radio_default = GameSetupActivity.mGameSetupPrefs.getInt(
				GameSetupActivity.RADIO_INDEX, 1);
		radio = (RadioButton) mRadioGroup.getChildAt(radio_default);
		radio.setChecked(true);

		// Bind view buttons
		Button startGameButton = (Button) this
				.findViewById(R.id.GameSetup_StartGameButton);
		startGameButton.setOnClickListener(mStartGameListener);

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
			teamSelect.setOnTeamEditedListener(mTeamEditedListener);
			teamSelect.setOnTeamAddedListener(mTeamAddedListener);
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
		// Iterate through radio buttons to find the one that is checked and
		// return it.
		for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
			RadioButton test = (RadioButton) mRadioGroup.getChildAt(i);
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
			GameSetupActivity.this.mContinueMusic = true;
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
	private void saveGameSetupPrefs()
	{
		GameSetupActivity.mGameSetupPrefEditor.putInt(
				GameSetupActivity.RADIO_INDEX,
				GameSetupActivity.this.getCheckedRadioIndex());
		GameSetupActivity.mGameSetupPrefEditor.putInt(
				GameSetupActivity.this.getString(R.string.PREFKEY_GAMELIMIT),
				mGameLimit);
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
