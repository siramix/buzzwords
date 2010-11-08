package com.taboozle;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
/**
 * @author The Taboozle Team
 * This activity class is responsible for summarizing the turn and the hand-off
 * into the next turn or game end.
 */
public class TurnSummary extends Activity
{
  /**
   * logging tag
   */
  public static String TAG = "TurnSummary";

  private LinkedList<Card> cardList;
  private LinkedList<ImageView> cardViewList;

	/**
	  * Watches the button that handles hand-off to the next turn activity.
	  */
	  private final OnClickListener NextTurnListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
	        Log.d( TAG, "NextTurnListener OnClick()" );
	        TaboozleApplication application =
	          (TaboozleApplication) TurnSummary.this.getApplication();
	        GameManager gm = application.GetGameManager();
	        gm.NextTurn();
     	  	startActivity(new Intent(TurnSummary.this.getApplication().getString(R.string.IntentTurn),
     	  								getIntent().getData()));
	      }
	  }; // End NextTurnListener

	  private final OnClickListener CardIconListener = new OnClickListener()
    {
        public void onClick(View v)
        {
          ImageView iv = (ImageView) v;
          int cardIndex = TurnSummary.this.cardViewList.indexOf( v );
          Card curCard = TurnSummary.this.cardList.get( cardIndex );
          curCard.cycleRws();
          Log.d( TAG, Integer.toString( cardIndex ) );
          iv.setImageResource( curCard.getDrawableId() );
          TurnSummary.this.UpdateScoreViews();
        }
    };

	  /**
	   * Watches the end turn button that cancels the game.
	   */
	  private final OnClickListener EndGameListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
          Log.d( TAG, "EndGameListener OnClick()" );
	        AlertDialog confirmEnd = new AlertDialog.Builder(v.getContext()).create();
	        confirmEnd.setTitle("Confirm End Game");
	        confirmEnd.setMessage("Are you sure you want to end the current game?");

	        confirmEnd.setButton("Cancel", new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface dialog, int which) {
            }
          });

	        confirmEnd.setButton2("Yes", new DialogInterface.OnClickListener()
	        {
            public void onClick(DialogInterface dialog, int which) {
              TaboozleApplication application =
                (TaboozleApplication) TurnSummary.this.getApplication();
              GameManager gm = application.GetGameManager();
              gm.EndGame();
              startActivity(new Intent(Intent.ACTION_CALL, getIntent().getData()));
            }
          });

	        confirmEnd.show();
	      }
	  }; // End OnClickListener

  /**
  * onCreate - initializes the activity to display the results of the turn.
  */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
  	super.onCreate( savedInstanceState );
    Log.d( TAG, "onCreate()" );
  	// Setup the view
  	this.setContentView(R.layout.turnsummary);

    TaboozleApplication application =
        (TaboozleApplication) this.getApplication();
    GameManager game = application.GetGameManager();

  	// Populate and display list of cards
  	ScrollView list = (ScrollView) findViewById(R.id.TurnSumCardList);
  	LinearLayout layout = new LinearLayout(this.getBaseContext());
  	layout.setOrientation(LinearLayout.VERTICAL);

	// iterate through all completed cards and set layout accordingly
    this.cardViewList = new LinkedList<ImageView>();
  	this.cardList = game.GetCurrentCards();
  	Card card = null;
  	int count = 0;
  	for( Iterator<Card> it = this.cardList.iterator(); it.hasNext(); )
  	{
  	  card = it.next();

  	  LinearLayout line = (LinearLayout) LinearLayout.inflate(this.getBaseContext(), R.layout.turnsumrow, layout);
  	  LinearLayout realLine = (LinearLayout) line.getChildAt(count);

  	  TextView cardTitle = (TextView) realLine.getChildAt(0);
  	  cardTitle.setText(card.getTitle());

  	  ImageView cardIcon = (ImageView) realLine.getChildAt(1);
  	  this.cardViewList.addLast( cardIcon );
  	  cardIcon.setImageResource(card.getDrawableId());
  	  cardIcon.setOnClickListener( CardIconListener );
  	  count++;
  	}
  	list.addView(layout);

  	// Update the scoreboard views
  	UpdateScoreViews();

  	// Bind Next / End buttons
  	Button playGameButton = (Button)this.findViewById( R.id.TurnSumNextTurn );
  	playGameButton.setOnClickListener( NextTurnListener );

  	Button endGameButton = (Button)this.findViewById( R.id.TurnSumEndGame );
  	endGameButton.setOnClickListener( EndGameListener );
  }

  /**
   * Update the views to display the proper scores for the current round
   */
  private void UpdateScoreViews()
  {
    Log.d( TAG, "UpdateScoreViews()" );
    TaboozleApplication application =
          (TaboozleApplication) this.getApplication();
    GameManager game = application.GetGameManager();

  	long turnscore = game.GetTurnScore();
  	long[] totalscores = game.GetTeamScores().clone();

  	// Set new total score for the current team
  	totalscores[game.GetActiveTeamIndex()] += turnscore;

  	// Display total score for the current team
  	TextView scoreview = (TextView) findViewById(R.id.TurnTotalScore);
  	scoreview.setText(Long.toString(turnscore));

  	// Populate Scoreboard scores
  	final int[] SCORE_VIEW_IDS = new int[]{R.id.TeamAScore, R.id.TeamBScore,
  											R.id.TeamCScore, R.id.TeamDScore};
  	for (int i = 0; i < totalscores.length; i++)
  	{
  		TextView teamTotalScoreView = (TextView) findViewById( SCORE_VIEW_IDS[i] );
  		teamTotalScoreView.setText(Long.toString(totalscores[i]));
  	}
    // Populate Scoreboard names
    final int[] SCORE_TEAMNAME_IDS = new int[]{R.id.TurnSummaryScoreATeamname, R.id.TurnSummaryScoreBTeamname,
                                               R.id.TurnSummaryScoreCTeamname, R.id.TurnSummaryScoreDTeamname};
    for (int i = 0; i < totalscores.length; i++)
    {
      TextView teamnameView = (TextView) findViewById( SCORE_TEAMNAME_IDS[i] );
      teamnameView.setText(game.GetTeamNames()[i]);
    }

  	// Hide teams that are not being played
    final int[] SCORE_VIEW_GROUP_IDS = new int[]{R.id.TurnSummaryScoreA, R.id.TurnSummaryScoreB,
    											 R.id.TurnSummaryScoreC, R.id.TurnSummaryScoreD};
  	for (int i = totalscores.length; i < SCORE_VIEW_GROUP_IDS.length; i++)
  	{
  		LinearLayout teamScoreGroupView = (LinearLayout) findViewById( SCORE_VIEW_GROUP_IDS[i] );
  		teamScoreGroupView.setVisibility( View.GONE );
  	}

  	// Display current team name
    TextView curTeam = (TextView) findViewById(R.id.TurnTeamName);
    curTeam.setText(game.GetTeamNames()[game.GetActiveTeamIndex()]);
    // Set team name color
	  final int[] TEAM_COLOR_IDS = new int[] { R.color.teamA_text, R.color.teamB_text, R.color.teamC_text, R.color.teamD_text };
    curTeam.setTextColor(this.getResources().getColor( TEAM_COLOR_IDS[game.GetActiveTeamIndex()]));
  }

  /**
   * Handler for key down events
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    // Handle the back button
    if( keyCode == KeyEvent.KEYCODE_BACK
        && event.getRepeatCount() == 0 )
      {
        event.startTracking();
        return true;
      }

    return super.onKeyDown(keyCode, event);
   }

  /**
   * Handler for key up events
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event)
  {
    // Make back do nothing on key-up instead of climb the action stack
    if( keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled() )
      {
      return true;
      }

    return super.onKeyUp(keyCode, event);
  }

}
