package com.taboozle;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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

  static final int DIALOG_GAMEOVER_ID = 0;
  
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
	        if( gm.GetNumTurnsRemaining() == 0 )
	        {
	        	gm.EndGame();
	        	startActivity(new Intent(Intent.ACTION_CALL, getIntent().getData()));
	        }
	        else
	        {
	        	gm.NextTurn();
	     	  	startActivity(new Intent(TurnSummary.this.getApplication().getString(R.string.IntentTurn),
							getIntent().getData()));
	        }

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

  	// Show or hide prompt indicating no cards were completed
  	int visibility = View.INVISIBLE;
    TextView noCardsPrompt = (TextView) this.findViewById( R.id.TurnSumNoCards);
  	if( this.cardList.isEmpty())
  	{
  	  visibility = View.VISIBLE;
  	}
    noCardsPrompt.setVisibility( visibility );
    
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

  	// Bind Next button
  	Button playGameButton = (Button)this.findViewById( R.id.TurnSumNextTurn );
  	playGameButton.setOnClickListener( NextTurnListener );
  	
  	// Change Next Game prompt to "Game Results" when the game is over.  Remove EndGame button
  	if ( game.GetNumTurnsRemaining() == 0 )
  	{
  		playGameButton.setText( "Game Results" );
  	}
  }

  /**
   *  Creates the menu items for the options menu
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    Log.d( TAG, "onCreateOptionsMenu()" );
    menu.add(0, R.string.menu_EndGame, 0, "End Game");
    menu.add(0, R.string.menu_Rules, 0, "Rules");

    return true;
  }

  /**
   * Handle menu clicks
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    Log.d( TAG, "onOptionsItemSelected()" );
    // Handle item selection
    switch (item.getItemId())
    {
      case R.string.menu_EndGame:
        this.showDialog( DIALOG_GAMEOVER_ID );
        return true;
      case R.string.menu_Rules:
        startActivity(new Intent(getApplication().getString( R.string.IntentRules ),
            getIntent().getData()));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }  

  /**
  * Handle creation of dialogs used in TurnSummary
  */
 @Override
 protected Dialog onCreateDialog(int id)
 {
   Log.d( TAG, "onCreateDialog(" + id + ")" );
   Dialog dialog = null;
   AlertDialog.Builder builder = null;
   
   switch(id) {
   case DIALOG_GAMEOVER_ID:
     builder = new AlertDialog.Builder(this);
     builder.setMessage( "Are you sure you want to end the current game?" )
            .setTitle("Confirm End Game")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                TaboozleApplication application = (TaboozleApplication) TurnSummary.this.getApplication();
                GameManager gm = application.GetGameManager();
                gm.EndGame();
                startActivity(new Intent(Intent.ACTION_CALL, getIntent().getData()));
                }
              })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
  	TextView scoreview = (TextView) findViewById(R.id.TurnSummaryTurnScore);
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
  	
  	// Hide current team (it has its own line)
	LinearLayout layout = (LinearLayout) findViewById( SCORE_VIEW_GROUP_IDS[game.GetActiveTeamIndex()] );
	layout.setVisibility( View.GONE );

  	// Display current team name and score
    TextView curTeamHeader = (TextView) findViewById(R.id.TurnSummaryTeamName);
    TextView curTeamName = (TextView) findViewById(R.id.TurnSummaryCurrentScoreTeamname);
    TextView curTeamScore = (TextView) findViewById(R.id.TurnSummaryCurrentScoreNum);
    String teamName = game.GetTeamNames()[game.GetActiveTeamIndex()];
    curTeamHeader.setText(teamName);
    curTeamName.setText(teamName);
    // Set team name color
	final int[] TEAM_COLOR_IDS = new int[] { R.color.teamA_text, R.color.teamB_text, R.color.teamC_text, R.color.teamD_text };
    int teamColor = this.getResources().getColor( TEAM_COLOR_IDS[game.GetActiveTeamIndex()]);
	curTeamHeader.setTextColor(teamColor);
	curTeamName.setTextColor(teamColor);
	curTeamScore.setTextColor(teamColor);
	curTeamScore.setText(Long.toString(totalscores[game.GetActiveTeamIndex()]));
    
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
