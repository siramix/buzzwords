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
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
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
   * Listener for menu button
   */
  private final OnClickListener mMenuListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "mMenuListener OnClick()");
      }
      
      TurnSummary.this.openOptionsMenu();
    }
  };

  /**
   * Watches the button that handles hand-off to the next turn activity.
   */
  private final OnClickListener mNextTurnListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "NextTurnListener OnClick()");
      }
      v.setEnabled(false);
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

      Intent cardReviewIntent = new Intent(
          getString(R.string.IntentCardReview), getIntent().getData());
      cardReviewIntent.putExtra(getString(R.string.cardIndexBundleKey),
          cardIndex);
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

      LinearLayout line = (LinearLayout) LinearLayout.inflate(
          this.getBaseContext(), R.layout.turnsumrow, layout);
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
      ImageView cardIcon = (ImageView) realLine.getChildAt(4);
      setCardIcon(cardIcon, card);
      
      mCardViewList.add(cardIcon);
      mCardLineList.add(realLine);

      // Add single pixel bar of lightened color to give depth
      View lightBar = (View) realLine.getChildAt(3);
      lightBar.setBackgroundColor(getResources().getColor(R.color.white));
      AlphaAnimation alpha = new AlphaAnimation(0.2f, 0.2f);
      alpha.setFillAfter(true);
      lightBar.startAnimation(alpha);
      
      ImageView rowEnd = (ImageView) realLine.getChildAt(2);
      Drawable d = getResources().getDrawable(R.drawable.turnsum_row_end_white);
      // Don't need to mutate, since all row end pieces should be the same color.
      d.setColorFilter(getResources().getColor(R.color.genericBG_trim), Mode.MULTIPLY);
      rowEnd.setBackgroundDrawable(d);
      
      // Assign the card review listener to the row
      realLine.setOnClickListener(mCardIconListener);
      
      // increment to next line
      count++;
    }
    list.addView(layout);

    // Set fonts
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");

    // Set font on Title text
    TextView scoreTitle = (TextView) findViewById(R.id.TurnSummary_ScoreboardTitle);
    scoreTitle.setTypeface(antonFont);

    TextView resultsTitle = (TextView) findViewById(R.id.TurnSummary_Title);
    resultsTitle.setTypeface(antonFont);
    
    // TODO This should be in a thread, but I'm not sure how to access the game from inside the thread
    game.maintainDeck();
    
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
    
    // Bind menu button
    Button menuButton = (Button) this.findViewById(R.id.TurnSummary_Menu);
    menuButton.setOnClickListener(mMenuListener);

    // Handle activity changes for final turn
    if (game.getNumberOfTurnsRemaining() == 0) {
      // Change "Next Team" button
      playGameButton.setText("Game Results");
      // Change round display
      rounds.setText("Game Over");
      // Hide scoreboard for suspense
      LinearLayout scores = (LinearLayout) this
          .findViewById(R.id.TurnSummary_ScoreGroup);
      scores.setVisibility(View.INVISIBLE);
      RelativeLayout scoreHeader = (RelativeLayout) this
          .findViewById(R.id.TurnSummary_ScoreboardTitle_Group);
      scoreHeader.setVisibility(View.INVISIBLE);
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
    
    menu.add(0, R.string.menu_EndGame, 0, R.string.menu_EndGame_Title);
    menu.add(0, R.string.menu_Rules, 0, R.string.menu_Rules_Title);

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
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    // Handle item selection
    switch (item.getItemId()) {
    case R.string.menu_EndGame:
      // Play confirmation sound
      sm.playSound(SoundManager.Sound.CONFIRM);
      this.showDialog(DIALOG_GAMEOVER_ID);
      return true;
    case R.string.menu_Rules:
      // Play confirmation sound
      sm.playSound(SoundManager.Sound.CONFIRM);
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
      builder.setMessage(this.getString(R.string.menu_EndGame_Text))
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound
              SoundManager sm = SoundManager.getInstance(TurnSummary.this
                  .getBaseContext());
              sm.playSound(SoundManager.Sound.CONFIRM);
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
              SoundManager sm = SoundManager.getInstance(TurnSummary.this
                  .getBaseContext());
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

    // References to Scoreboard team Groups
    final int[] TEAM_SCORE_GROUPS = new int[] { R.id.TurnSummary_Scores_TeamA,
        R.id.TurnSummary_Scores_TeamB, R.id.TurnSummary_Scores_TeamC,
        R.id.TurnSummary_Scores_TeamD };

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
      }
    }

    // Update Turn score total
    TextView turnTotal = (TextView) this
        .findViewById(R.id.TurnSummary_TurnScore);
    turnTotal.setText("Total: " + Integer.toString(game.getTurnScore()));
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
   * Resume the activity when it comes to the foreground. If the calling Intent
   * bundles a new card index and state the card in question is update
   * accordingly.
   */
  @Override
  protected void onResume() {

    super.onResume();

  }

  /**
   * When the card review activity finishes, this function is called. Well,
   * actually, any activity called with a request code will invoke this
   * function. If the card review activity returns, we use the result to change
   * the card state indicated by the result intent stored in data.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Bundle curBundle = null;
    if (data != null) {
      curBundle = data.getExtras();
    }
    if (requestCode == CARDREVIEW_REQUEST_CODE && curBundle != null
        && curBundle.containsKey(getString(R.string.cardIndexBundleKey))
        && curBundle.containsKey(getString(R.string.cardStateBundleKey))) {
      int curCardIndex = curBundle
          .getInt(getString(R.string.cardIndexBundleKey));
      int curCardState = curBundle
          .getInt(getString(R.string.cardStateBundleKey));

      // Ammend the card
      BuzzWordsApplication application = (BuzzWordsApplication) TurnSummary.this
          .getApplication();
      GameManager gm = application.getGameManager();
      gm.ammendCard(curCardIndex, curCardState);

      // Update the individual card's UI in the list
      Card curCard = mCardList.get(curCardIndex);
      ImageView curImageView = mCardViewList.get(curCardIndex);
      setCardIcon(curImageView, curCard);

      TurnSummary.this.updateScoreViews();
    }

    super.onActivityResult(requestCode, resultCode, data);
  }
  
  /*
   * Sets the specified image to the card's RWS drawable
   */
  private void setCardIcon(ImageView icon, Card card)
  {
    // Set Row end icon
    int iconID = card.getRowEndDrawableId();
    if(iconID > 0)
    {
      //cardIcon.setImageResource(iconID);
      icon.setImageDrawable(getResources().getDrawable(iconID));
    }
    else
    {
      icon.setImageDrawable(null);
    }
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
