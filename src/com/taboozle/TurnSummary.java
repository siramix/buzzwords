package com.taboozle;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	  * Watches the button that handles hand-off to the next turn activity.
	  */
	  private final OnClickListener NextTurnListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
	        // You have to draw before getting the view
	        /*ListView list = (ListView) findViewById(R.id.TurnSumCardList);
	        LinearLayout lay = (LinearLayout) list.getChildAt( 1 );
	        ImageView iv = (ImageView) lay.getChildAt( 1 );
	        iv.setBackgroundResource( R.drawable.wrong );*/

	        TaboozleApplication application =
	          (TaboozleApplication) TurnSummary.this.getApplication();
	        GameManager gm = application.GetGameManager();
	        gm.NextTurn();
     	  	startActivity(new Intent(Intent.ACTION_RUN, getIntent().getData()));
	      }
	  }; // End NextTurnListener

	  /**
	   * Watches the end turn button that cancels the game.
	   */
	  private final OnClickListener EndTurnListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
          TaboozleApplication application =
            (TaboozleApplication) TurnSummary.this.getApplication();
          GameManager gm = application.GetGameManager();
          gm.EndGame();
          startActivity(new Intent(Intent.ACTION_CALL, getIntent().getData()));
	      }
	  }; // End OnClickListener
/**
* onCreate - initializes the activity to display the results of the turn.
*/
@Override
public void onCreate( Bundle savedInstanceState )
{
	super.onCreate( savedInstanceState );
	// Setup the view
	this.setContentView(R.layout.turnsummary);

	ScrollView list = (ScrollView) findViewById(R.id.TurnSumCardList);
	LinearLayout layout = new LinearLayout(this.getBaseContext());
	layout.setOrientation(LinearLayout.VERTICAL);
    TaboozleApplication application =
        (TaboozleApplication) this.getApplication();
    GameManager game = application.GetGameManager();

	LinkedList<Card> cardlist = game.GetCurrentCards();
	Card card = null;
	int count = 0;
	for( Iterator<Card> it = cardlist.iterator(); it.hasNext(); )
	{
	  card = it.next();
		
	  LinearLayout line = (LinearLayout) LinearLayout.inflate(this.getBaseContext(), R.layout.turnsumrow, layout);
	  LinearLayout realLine = (LinearLayout) line.getChildAt(count);
	  
	  TextView cardTitle = (TextView) realLine.getChildAt(0);
	  cardTitle.setText(card.getTitle());
	  
	  ImageView cardIcon = (ImageView) realLine.getChildAt(1);
	  cardIcon.setImageResource(card.getDrawableId());
	  count++;
	}
	list.addView(layout);
	
	UpdateScoreViews();

	Button playGameButton = (Button)this.findViewById( R.id.TurnSumNextTurn );
	playGameButton.setOnClickListener( NextTurnListener );

	Button endGameButton = (Button)this.findViewById( R.id.TurnSumEndGame );
	endGameButton.setOnClickListener( EndTurnListener );
}

/**
 * Update the views to display the proper scores for the current round
 */
private void UpdateScoreViews()
{
    TaboozleApplication application =
        (TaboozleApplication) this.getApplication();
    GameManager game = application.GetGameManager();

	long turnscore = game.GetTurnScore();
	long[] totalscores = game.GetTeamScores().clone();

	totalscores[game.GetActiveTeamIndex()] += turnscore;

	TextView scoreview = (TextView) findViewById(R.id.TurnTotalScore);
	scoreview.setText(Long.toString(turnscore));

	TextView teamATotalScore = (TextView) findViewById(R.id.TeamAScore);
	teamATotalScore.setText("Team A: " + Long.toString(totalscores[0]));

	TextView teamBTotalScore = (TextView) findViewById(R.id.TeamBScore);
	teamBTotalScore.setText("Team B: " + Long.toString(totalscores[1]));

  TextView curTeam = (TextView) findViewById(R.id.CurTeamIndex);
  curTeam.setText("Current Team: " + Long.toString(game.GetActiveTeamIndex()));
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
