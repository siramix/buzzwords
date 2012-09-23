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

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.buzzwords.R;
import com.buzzwords.BuzzWordsApplication;

/**
 * The GameEnd class is the final screen of the application, called when either
 * the number of turns is up, the time is up, the end game button is clicked, or
 * any other number of ways to end a game.
 * 
 * @author BuzzWords team
 * 
 */
public class GameEndActivity extends Activity {

  /**
   * logging tag
   */
  public static final String TAG = "GameEnd";

  /**
   * This is a reference to the current game manager
   */
  private GameManager mGameManager;

  /**
   * Resources to be retrieved throughout GameEnd display
   */
  private Resources mResources;

  /**
   * Animates all views in GameEnd screen in an interesting sequence.
   */
  private void animateGameEnd(int numteams) {

    long offset = 500;

    // Fade in Winner text
    AlphaAnimation fadeInWinner = new AlphaAnimation(0.0f, 1.0f);
    fadeInWinner.setStartOffset(offset);
    fadeInWinner.setDuration(30);
    View winnerText = (View) this.findViewById(R.id.GameEnd_Winner);
    winnerText.startAnimation(fadeInWinner);

    AlphaAnimation fadeInWinnerText = new AlphaAnimation(0.0f, 1.0f);
    offset = fadeInWinner.getStartOffset() + fadeInWinner.getDuration() + 500;
    fadeInWinnerText.setStartOffset(offset);
    fadeInWinnerText.setDuration(30);
    fadeInWinnerText.setAnimationListener(mGameOverListener);
    View winnerTeamText = (View) this.findViewById(R.id.GameEnd_WinnerText);
    winnerTeamText.startAnimation(fadeInWinnerText);

    // Fade in Title
    AlphaAnimation fadeInAfterWinner = new AlphaAnimation(0.0f, 1.0f);
    offset = fadeInWinnerText.getStartOffset() + fadeInWinnerText.getDuration()
        + 500;
    fadeInAfterWinner.setStartOffset(offset);
    fadeInAfterWinner.setDuration(500);
    TextView title = (TextView) this.findViewById(R.id.GameEnd_Title);
    title.startAnimation(fadeInAfterWinner);
    View scoreboardHeader = (View) this
        .findViewById(R.id.GameEnd_ScoreboardHeader_Group);
    scoreboardHeader.startAnimation(fadeInAfterWinner);

    // Animate buttons to fade in as scoreboard translates
    AlphaAnimation fadeInButtons = new AlphaAnimation(0.0f, 1.0f);
    fadeInButtons.setStartOffset(offset);
    fadeInButtons.setDuration(500);
    fadeInButtons.setAnimationListener(mButtonFadeListener);
    Button tempButton = (Button) this.findViewById(R.id.GameEnd_MainMenu);
    tempButton.startAnimation(fadeInButtons);
    tempButton = (Button) this.findViewById(R.id.GameEnd_Rematch);
    tempButton.startAnimation(fadeInButtons);

    // Slide in panels one at a time ( could do this in some sort of loop... )
    final int PANEL_DELAY = 250;
    TranslateAnimation transPanel4 = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    transPanel4.setStartOffset(offset);
    transPanel4.setDuration(250);
    transPanel4.setFillBefore(true);
    transPanel4.setInterpolator(new DecelerateInterpolator());
    RelativeLayout panel4 = (RelativeLayout) this
        .findViewById(R.id.GameEnd_Scores_4);
    panel4.startAnimation(transPanel4);

    TranslateAnimation transPanel3 = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    // set offset based on previous element
    if (numteams >= 3) {
      offset += (transPanel4.getDuration() + PANEL_DELAY);
    } else {
      // Slide in with panel3 if this is an invalid team
      offset = transPanel4.getStartOffset();
    }
    transPanel3.setStartOffset(offset);
    transPanel3.setDuration(250);
    transPanel3.setFillBefore(true);
    transPanel3.setInterpolator(new DecelerateInterpolator());
    RelativeLayout panel3 = (RelativeLayout) this
        .findViewById(R.id.GameEnd_Scores_3);
    panel3.startAnimation(transPanel3);

    TranslateAnimation transPanel2 = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    offset = transPanel3.getStartOffset() + transPanel3.getDuration()
        + PANEL_DELAY;
    transPanel2.setStartOffset(offset);
    transPanel2.setDuration(250);
    transPanel2.setFillBefore(true);
    transPanel2.setInterpolator(new DecelerateInterpolator());
    RelativeLayout panel2 = (RelativeLayout) this
        .findViewById(R.id.GameEnd_Scores_2);
    panel2.startAnimation(transPanel2);

    TranslateAnimation transPanel1 = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    offset = transPanel2.getStartOffset() + transPanel2.getDuration()
        + PANEL_DELAY;
    transPanel1.setStartOffset(offset);
    transPanel1.setDuration(250);
    transPanel1.setFillBefore(true);
    transPanel1.setInterpolator(new DecelerateInterpolator());

    RelativeLayout panel1 = (RelativeLayout) this
        .findViewById(R.id.GameEnd_Scores_1);
    panel1.startAnimation(transPanel1);
  }

  /**
   * Setup the sounds to go with the animation
   */
  private final AnimationListener mGameOverListener = new AnimationListener() {
    public void onAnimationEnd(Animation animation) {
      // Play win sound
      SoundManager sm = SoundManager.getInstance(GameEndActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.WIN);
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }
  };

  /**
   * Make buttons usable once faded in
   */
  private final AnimationListener mButtonFadeListener = new AnimationListener() {
    public void onAnimationEnd(Animation animation) {
      // Make buttons usable
      Button mainmenuButton = (Button) findViewById(R.id.GameEnd_MainMenu);
      Button rematchButton = (Button) findViewById(R.id.GameEnd_Rematch);
      mainmenuButton.setClickable(true);
      rematchButton.setClickable(true);
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }
  };

  /**
   * Listener for the 'Main Menu' button. Sends user back to the main screen on
   * click.
   */
  private final OnClickListener mMainMenuListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "MainMenuListener onClick()");
      }
      // Play confirm sound
      SoundManager sm = SoundManager.getInstance(GameEndActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      Intent clearStackIntent = new Intent(getApplication().getString(
          R.string.IntentTitle), getIntent().getData());
      clearStackIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(clearStackIntent);
    }
  }; // End MainMenuListener

  /**
   * Listener for the 'Rematch' button. Starts a new game with same team names.
   */
  private final OnClickListener mRematchListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "RematchListener onClick()");
      }
      v.setEnabled(false);

      BuzzWordsApplication application = (BuzzWordsApplication) getApplication();

      GameManager curgm = application.getGameManager();
      GameManager newgm = new GameManager(GameEndActivity.this);
      newgm.startGame(curgm.getTeams(), curgm.getGameType(), curgm.getGameLimitValue());
      application.setGameManager(newgm);

      Intent clearStackIntent = new Intent(getApplication().getString(
          R.string.IntentTurn), getIntent().getData());
      clearStackIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(clearStackIntent);
    }
  }; // End MainMenuListener

  /**
   * GameEnd on create handles all logic, including calls to query the db,
   * populate views, and display them.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.gameend);
    mResources = this.getResources();

    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    mGameManager = application.getGameManager();

    // Capture our preference variable for playcounter
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(getBaseContext());

    // Initialize our preference Editor
    SharedPreferences.Editor prefEditor = sp.edit();

    // Get our play count preference title string
    String playCountKey = this.getResources().getString(
        R.string.PREFKEY_PLAYCOUNT);
    String showReminderKey = this.getResources().getString(
        R.string.PREFKEY_SHOWREMINDER);

    // Get number of plays
    int playCount = sp.getInt(playCountKey, 0);

    // When we hit 3 or 6 playthroughs, trigger the reminder
    // If they've rated us, playCount will never hit 3 or 6 (it gets set to 7)
    if (playCount == 2 || playCount == 5) {
      prefEditor.putBoolean(showReminderKey, true);
    }

    prefEditor.putInt(playCountKey, playCount + 1);
    prefEditor.commit();

    List<Team> teams = mGameManager.getTeams();

    // Sort the list by scores to determine the winner(s)
    Collections.sort(teams, (Team.TEAMA).new ScoreComparator());
    Collections.reverse(teams);

    // Assign rankings to the teams (in worst to best, to match team list)
    int[] rankings = new int[teams.size()];
    int rank = 0;
    rankings[0] = rank;
    for (int i = 1; i < teams.size(); ++i) {
      // Continue to count up, regardless of ties.
      // This gives results such as 1st, 1st, 3rd, 4th instead of 1st, 1st, 2nd,
      // 3rd
      rank++;
      // Stomp rank of tying team with that of the team they tied with (the
      // higher rank)
      if (teams.get(i - 1).getScore() == teams.get(i).getScore()) {
        rankings[i] = rankings[i - 1];
      } else {
        rankings[i] = rank;
      }
    }
    // If the two highest ranks are the same, we have a tie on 1st place
    boolean tieGame = rankings[0] == rankings[1];

    // Ids for Scoreboard list rows (one per team).
    final int[] TEAM_SCORE_GROUPS = new int[] { R.id.GameEnd_Scores_1,
        R.id.GameEnd_Scores_2, R.id.GameEnd_Scores_3, R.id.GameEnd_Scores_4 };

    final String[] RANKS = new String[] { "1st", "2nd", "3rd", "4th" };

    ScoreboardRowLayout row;
    // Setup score displays. Iterate through all team groups, setting scores for
    // teams that played
    // and disabling the group for teams that did not play
    for (int i = 0; i < TEAM_SCORE_GROUPS.length; i++) {
      row = (ScoreboardRowLayout) this.findViewById(TEAM_SCORE_GROUPS[i]);
      if (i >= teams.size()) {
        // Gray out rows for teams that didn't play
        row.setActiveness(false);
      } else {

        // Show teams that played, and set their rank
        row.setTeam(teams.get(i));
        row.setActiveness(true);
        row.setStanding(RANKS[rankings[i]]);
      }
    }

    TextView text = (TextView) findViewById(R.id.GameEnd_WinnerText);
    if (tieGame) {
      // Set text to Tie game!
      text.setTextColor(mResources.getColor(R.color.white));
      text.setText("Tie Game!");
    } else {
      // Announce winning team
      text.setTextColor(mResources.getColor(teams.get(0).getPrimaryColor()));
      text.setText(teams.get(0).getName());
    }

    // Set onclick listeners for game end buttons
    Button mainMenuButton = (Button) this.findViewById(R.id.GameEnd_MainMenu);
    Button rematchButton = (Button) this.findViewById(R.id.GameEnd_Rematch);
    mainMenuButton.setOnClickListener(mMainMenuListener);
    rematchButton.setOnClickListener(mRematchListener);
    // buttons start disabled and get enabled once faded in
    mainMenuButton.setClickable(false);
    rematchButton.setClickable(false);

    // Animate the whole thing
    animateGameEnd(teams.size());
    mGameManager.maintainDeck();
  }

  /**
   * Method handles stopping of any outstanding timers during closing of GameEnd
   */
  @Override
  public void onStop() {
    super.onStop();
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onStop()");
    }
  }

  /**
   * Method handles stopping of any outstanding timers during closing of GameEnd
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onDestroy()");
    }
  }

  /**
   * Handler for key up events
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    // Make back do nothing on key-up instead of climb the action stack
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled()) {
      return true;
    }

    return super.onKeyUp(keyCode, event);
  }
}
