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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.buzzwords.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * This activity class is responsible for summarizing the turn and the hand-off
 * into the next turn or game end.
 * 
 * @author Siramix Labs
 */
public class TurnSummary extends Activity {
  /**
   * logging tag
   */
  public static String TAG = "TurnSummary";

  static final int DIALOG_GAMEOVER_ID = 0;
  
  static final int CARDREVIEW_REQUEST_CODE = 1;

  private List<Card> mCardList;
  private List<ImageView> mCardViewList;
  private List<View> mCardLineList;
  
  /**
   * Sound Manager stored as an instance variable to reduce calls to
   * GetSoundManager
   */
  private SoundManager mSoundManager;  

  /**
   * Watches the button that handles hand-off to the next turn activity.
   */
  private final OnClickListener mNextTurnListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "NextTurnListener OnClick()");
      }
      BuzzWordsApplication application = (BuzzWordsApplication) TurnSummary.this
          .getApplication();
      GameManager gm = application.getGameManager();

      if (gm.getNumberOfTurnsRemaining() == 0) {
        gm.endGame();
        startActivity(new Intent(getApplication().getString(
            R.string.IntentEndGame), getIntent().getData()));
      } else {
        gm.nextTurn();
        Intent clearStackIntent = new Intent(getApplication().getString(
            R.string.IntentTurn), getIntent().getData());
        clearStackIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(clearStackIntent);
      }

    }
  }; // End NextTurnListener

  /**
   * Deal with changing the right-wrong-skip state of cards from the previous
   * turn by clicking on the status indicating icon
   */
  private final OnClickListener mCardIconListener = new OnClickListener() {
    public void onClick(View v) {
      int cardIndex = mCardLineList.indexOf(v);
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, Integer.toString(cardIndex));
      }
      
      Card curCard = mCardList.get(cardIndex);
      
      Intent cardReviewIntent = new Intent(getString(R.string.IntentCardReview),
        getIntent().getData());
      cardReviewIntent.putExtra(getString(R.string.cardIndexBundleKey), cardIndex);
      cardReviewIntent.putExtra(getString(R.string.cardBundleKey), curCard);
      startActivityForResult(cardReviewIntent, CARDREVIEW_REQUEST_CODE);
    }
  };

  /**
   * Initializes the activity to display the results of the turn.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Setup the view
    this.setContentView(R.layout.turnsummary);

    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    GameManager game = application.getGameManager();

    // Save sound manager as a local variable
    mSoundManager = application.getSoundManager();
    
    // Populate and display list of cards
    ScrollView list = (ScrollView) findViewById(R.id.TurnSummary_CardList);
    LinearLayout layout = new LinearLayout(this.getBaseContext());
    layout.setOrientation(LinearLayout.VERTICAL);

    // iterate through all completed cards and set layout accordingly
    mCardViewList = new LinkedList<ImageView>();
    mCardLineList = new LinkedList<View>();
    mCardList = game.getCurrentCards();
    Card card = null;
    int count = 0;

    for (Iterator<Card> it = mCardList.iterator(); it.hasNext();) {
      card = it.next();

      LinearLayout line = (LinearLayout) LinearLayout.inflate(this
          .getBaseContext(), R.layout.turnsumrow, layout);
      RelativeLayout realLine = (RelativeLayout) line.getChildAt(count);
      // Make every line alternating color
      if (count % 2 == 0) {
        View background = (View) realLine.getChildAt(0);
        background.setBackgroundResource(R.color.genericBG_trim);
      }

      // Set Title
      TextView cardTitle = (TextView) realLine.getChildAt(1);
      cardTitle.setText(card.getTitle());

      // Set Row end icon
      ImageView cardIcon = (ImageView) realLine.getChildAt(2);
      mCardViewList.add(cardIcon);
      mCardLineList.add(realLine);
      cardIcon.setImageResource(card.getRowEndDrawableId());
      realLine.setOnClickListener(mCardIconListener);
      count++;
    }
    list.addView(layout);

    // Update the scoreboard views
    updateScoreViews();

    // Update numRounds
    TextView rounds = (TextView) this.findViewById(R.id.TurnSummary_Rounds);
    rounds.setText("Round: " + game.getCurrentRound() + "/"
        + game.getNumRounds());

    // Update Turn Order display
    updateTurnOrderDisplay();

    // Bind Next button
    Button playGameButton = (Button) this
        .findViewById(R.id.TurnSummary_NextTurn);
    playGameButton.setOnClickListener(mNextTurnListener);

    // Handle activity changes for final turn
    if (game.getNumberOfTurnsRemaining() == 0) {
      // Change "Next Team" button
      playGameButton.setText("Game Results");
      // Change round display
      rounds.setText("Game Over");
      // Hide scoreboard for suspense
      RelativeLayout scores = (RelativeLayout) this
          .findViewById(R.id.TurnSummary_ScoreGroup);
      scores.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * Creates the menu items for the options menu
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreateOptionsMenu()");
    }
    menu.add(0, R.string.menu_EndGame, 0, "End Game");
    menu.add(0, R.string.menu_Rules, 0, "Rules");

    return true;
  }

  /**
   * Handle menu clicks
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onOptionsItemSelected()");
    }
    
    // Handle item selection
    switch (item.getItemId()) {
    case R.string.menu_EndGame:
      // Play confirmation sound              
      mSoundManager.playSound(SoundManager.Sound.CONFIRM);
      this.showDialog(DIALOG_GAMEOVER_ID);
      return true;
    case R.string.menu_Rules:
      // Play confirmation sound              
      mSoundManager.playSound(SoundManager.Sound.CONFIRM);
      startActivity(new Intent(
          getApplication().getString(R.string.IntentRules), getIntent()
              .getData()));
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Handle creation of dialogs used in TurnSummary
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreateDialog(" + id + ")");
    }
    Dialog dialog = null;
    AlertDialog.Builder builder = null;

    switch (id) {
    case DIALOG_GAMEOVER_ID:
      builder = new AlertDialog.Builder(this);
      builder.setMessage("Are you sure you want to end the current game?")
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound              
              mSoundManager.playSound(SoundManager.Sound.CONFIRM);              
              BuzzWordsApplication application = (BuzzWordsApplication) TurnSummary.this
                  .getApplication();
              GameManager gm = application.getGameManager();
              gm.endGame();
              startActivity(new Intent(getString(R.string.IntentEndGame),
                  getIntent().getData()));
            }
          }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound              
              mSoundManager.playSound(SoundManager.Sound.CONFIRM);
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
   * Update the views to display the proper scores for the current round
   */
  private void updateScoreViews() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "UpdateScoreViews()");
    }
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    GameManager game = application.getGameManager();
    List<Team> teams = game.getTeams();

    int turnscore = game.getTurnScore();

    // Display total score for the current team
    TextView scoreview = (TextView) findViewById(R.id.TurnSummary_TurnScore);
    scoreview.setText("Total: " + Long.toString(turnscore));

    // References to Scoreboard team scores
    final int[] SCORE_VIEW_IDS = new int[] { R.id.TurnSummary_Scores_TeamANum,
        R.id.TurnSummary_Scores_TeamBNum, R.id.TurnSummary_Scores_TeamCNum,
        R.id.TurnSummary_Scores_TeamDNum };
    // References to Scoreboard team names
    final int[] SCORE_TEAMNAME_IDS = new int[] {
        R.id.TurnSummary_Scores_TeamAName, R.id.TurnSummary_Scores_TeamBName,
        R.id.TurnSummary_Scores_TeamCName, R.id.TurnSummary_Scores_TeamDName };

    // References to Scoreboard team Groups
    final int[] SCORE_VIEW_GROUP_IDS = new int[] {
        R.id.TurnSummary_Scores_TeamA, R.id.TurnSummary_Scores_TeamB,
        R.id.TurnSummary_Scores_TeamC, R.id.TurnSummary_Scores_TeamD };
    // Hide all
    for (int i = 0; i < SCORE_VIEW_GROUP_IDS.length; i++) {
      // Clear background
      LinearLayout teamScoreGroupView = (LinearLayout) findViewById(SCORE_VIEW_GROUP_IDS[i]);
      teamScoreGroupView.setBackgroundResource(R.color.genericBG_trimDark);
      // Hide Team name
      TextView text = (TextView) findViewById(SCORE_TEAMNAME_IDS[i]);
      text.setVisibility(View.INVISIBLE);
      // Hide Score
      text = (TextView) findViewById(SCORE_VIEW_IDS[i]);
      text.setVisibility(View.INVISIBLE);
    }
    // Show for teams that exist
    for (Iterator<Team> itr = teams.iterator(); itr.hasNext();) {
      Team team = itr.next();
      LinearLayout teamScoreGroupView = (LinearLayout) findViewById(SCORE_VIEW_GROUP_IDS[team
          .ordinal()]);
      teamScoreGroupView.setBackgroundResource(R.color.genericBG);
      // Show Team name
      TextView text = (TextView) findViewById(SCORE_TEAMNAME_IDS[team.ordinal()]);
      text.setVisibility(View.VISIBLE);
      // Show Score
      text = (TextView) findViewById(SCORE_VIEW_IDS[team.ordinal()]);
      text.setVisibility(View.VISIBLE);

      // Set Name
      TextView teamnameView = (TextView) findViewById(SCORE_TEAMNAME_IDS[team
          .ordinal()]);
      teamnameView.setText(team.getName());
      // Set Score
      TextView teamTotalScoreView = (TextView) findViewById(SCORE_VIEW_IDS[team
          .ordinal()]);
      int score = team.getScore();
      // if this is the current team's score, add in the temp score from the
      // turn
      if (game.getActiveTeam().ordinal() == team.ordinal()) {
        score += turnscore;
      }
      teamTotalScoreView.setText(Long.toString(score));

    }

    // Color activity views according to team
    View curTeamHeader = (View) findViewById(R.id.TurnSummary_TitleBG);
    int teamColor = this.getResources().getColor(
        game.getActiveTeam().getPrimaryColor());
    curTeamHeader.setBackgroundColor(teamColor);

  }

  /**
   * Updates the widget group for turn order display
   */
  private void updateTurnOrderDisplay() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "UpdateTurnOrderDisplay()");
    }
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    GameManager game = application.getGameManager();
    List<Team> teams = game.getTeams();

    // References to Scoreboard team Groups
    final int[] TURNORDER_GROUPS = new int[] {
        R.id.TurnSummary_TurnOrder_TeamA, R.id.TurnSummary_TurnOrder_TeamB,
        R.id.TurnSummary_TurnOrder_TeamC, R.id.TurnSummary_TurnOrder_TeamD };
    // References to Scoreboard team Groups
    final int[] TURNORDER_ICONS = new int[] {
        R.id.TurnSummary_TurnOrder_TeamAc, R.id.TurnSummary_TurnOrder_TeamBc,
        R.id.TurnSummary_TurnOrder_TeamCc, R.id.TurnSummary_TurnOrder_TeamDc };
    // References to Scoreboard separators
    final int[] TURNORDER_SEPARATORS = new int[] {
        R.id.TurnSummary_TurnOrder_Separator1,
        R.id.TurnSummary_TurnOrder_Separator2,
        R.id.TurnSummary_TurnOrder_Separator3 };
    // References to Scoreboard team Groups
    final int[] TURNORDER_MARKERS = new int[] {
        R.id.TurnSummary_TurnOrder_TeamAmark,
        R.id.TurnSummary_TurnOrder_TeamBmark,
        R.id.TurnSummary_TurnOrder_TeamCmark,
        R.id.TurnSummary_TurnOrder_TeamDmark };

    // Iterate through Turn Order elements, setting attributes to match the
    // current Turn order, including active team marker
    for (int i = 0; i < TURNORDER_GROUPS.length; i++) {
      if (i >= teams.size()) {
        // Hide groups for teams that aren't playing
        LinearLayout turnGroup = (LinearLayout) findViewById(TURNORDER_GROUPS[i]);
        turnGroup.setVisibility(View.GONE);
        View separator = (View) findViewById(TURNORDER_SEPARATORS[i - 1]);
        separator.setVisibility(View.GONE);
      } else {
        View turnView = (View) findViewById(TURNORDER_ICONS[i]);
        turnView.setBackgroundColor(this.getResources().getColor(
            teams.get(i).getPrimaryColor()));
      }

      // Update Marker position
      ImageView marker = (ImageView) findViewById(TURNORDER_MARKERS[i]);
      if (teams.indexOf(game.getActiveTeam()) == i) {
        marker.setVisibility(View.VISIBLE);
      } else {
        marker.setVisibility(View.GONE);
      }
    }
  }
  
  /**
   * Resume the activity when it comes to the foreground. If the calling
   * Intent bundles a new card index and state the card in question is
   * update accordingly.
   */
  @Override
  protected void onResume() {

    super.onResume();
    
  }
  
  /**
   * When the card review activity finishes, this function is called. Well,
   * actually, any activity called with a request code will invoke this
   * function. If the card review activity returns, we use the result to
   * change the card state indicated by the result intent stored in data. 
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Bundle curBundle = null;
    if(data != null) {
      curBundle = data.getExtras(); 
    }
    if(requestCode == CARDREVIEW_REQUEST_CODE &&
       curBundle != null &&
       curBundle.containsKey(getString(R.string.cardIndexBundleKey)) &&
       curBundle.containsKey(getString(R.string.cardStateBundleKey))) {
      int curCardIndex = curBundle.getInt(getString(R.string.cardIndexBundleKey));
      int curCardState = curBundle.getInt(getString(R.string.cardStateBundleKey));
      Card curCard = mCardList.get(curCardIndex);
      curCard.setRws(curCardState);
      ImageView curImageView = mCardViewList.get(curCardIndex);
      curImageView.setImageResource(curCard.getRowEndDrawableId());
      TurnSummary.this.updateScoreViews();
    }
    
    super.onActivityResult(requestCode, resultCode, data);
  }

  /**
   * Start tracking the back button so we can properly handle catching it in the
   * onKeyUp
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Handle the back button
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      event.startTracking();
      return true;
    }

    return super.onKeyDown(keyCode, event);
  }

  /**
   * Do not allow the user to go back with the back button from this activity
   * (the turn is over)
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
