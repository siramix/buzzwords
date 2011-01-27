package com.taboozle;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.taboozle.TaboozleApplication;

/**
 * The GameEnd class is the final screen of the application, called
 * when either the number of turns is up, the time is up, the end game
 * button is clicked, or any other number of ways to end a game.
 *
 * @author Taboozle team
 *
 */
public class GameEnd extends Activity
{

  /**
   * logging tag
   */
  public static String TAG = "GameEnd";

  /**
   * This is a reference to the current game manager
   */
  private GameManager curGameManager;
  
  private ArrayList<Award> awards;
  
  private class AwardTimer extends CountDownTimer
  {
    public AwardTimer(long millisInFuture, long countDownInterval)
    {
      super(millisInFuture, countDownInterval);
      Log.d( TAG, "AwardTimer AwardTimer(" + millisInFuture + ", " + countDownInterval + ")" );
    }

    @Override
    public void onFinish()
    {
      Log.d( TAG, "AwardTimer onFinish()" );
      GameEnd.this.showNextAward();
      GameEnd.this.startAwardTimer();
    }

    @Override
    public void onTick(long millisUntilFinished)
    {
      //Log.d( TAG, "AwardTimer onTick(" + millisUntilFinished + ")");
    }
  };
  
  /**
   * Instance of the timer used to cycle through awards
   */
  private AwardTimer cycleTimer;
  private static final long TICK = 200;
  
  /**
   * Tracks the currently displayed award
   */
  private int awardIndex = -1;
  
  private void startAwardTimer()
  {
    Log.d( TAG, "startAwardTimer()");
	  this.cycleTimer = new AwardTimer(3000, TICK);
	  this.cycleTimer.start();
  }
  
  /**
   * Cycles the award display to the award for the next team (minimum of two teams)
   */
  private void showNextAward()
  {
    Log.d( TAG, "showNextAward()");
    GameManager gm = ((TaboozleApplication)this.getApplication()).GetGameManager();
    final int[] TEAM_COLOR_IDS = new int[] { R.color.teamA_text, R.color.teamB_text, R.color.teamC_text, R.color.teamD_text };
    final String[] stringAwards = new String[gm.GetTeamIDs().length];
    for( int i = 0; i < stringAwards.length; ++i )
    {
      stringAwards[i] = this.awards.get( i ).name;
    }
    
    TextView awardName = (TextView) findViewById(R.id.EndGameAwards);
    TextView awardTeamName = (TextView) findViewById(R.id.EndGameAwardTeamName);
    this.awardIndex = (this.awardIndex + 1) % curGameManager.GetNumTeams();
    awardName.setText(stringAwards[this.awardIndex]);
    awardTeamName.setText(curGameManager.GetTeamNames()[this.awardIndex]);
    awardTeamName.setTextColor(this.getResources().getColor( TEAM_COLOR_IDS[this.awardIndex] ));
  }
  
  /**
   * Listener for the 'Main Menu' button. Sends user back to the main screen on click.
   */
  private final OnClickListener MainMenuListener = new OnClickListener()
  {
    public void onClick(View v)
    {
      Log.d( TAG, "MainMenuListener onClick()" );
      startActivity(new Intent( getApplication().getString( R.string.IntentTitle ),
                                 getIntent().getData()));
    }
  }; // End MainMenuListener

  /**
   * Listener for the 'Rematch' button. Starts a new game with same team names.
   */
  private final OnClickListener RematchListener = new OnClickListener()
  {
      public void onClick(View v)
      {
        Log.d( TAG, "MainMenuListener onClick()" );
        
        TaboozleApplication application =
          (TaboozleApplication) GameEnd.this.getApplication();
        
        GameManager curgm = application.GetGameManager();
        GameManager newgm = new GameManager(GameEnd.this);
        newgm.PrepDeck();
        newgm.StartGame( curgm.GetTeamNames(), curgm.GetNumRounds() );
        application.SetGameManager( newgm );
        
        startActivity(new Intent(getApplication().getString(R.string.IntentTurn), getIntent().getData()));        
      }
  }; // End MainMenuListener
  
    /**
     * GameEnd on create handles all logic, including calls to query the db, populate
     * views, and display them.
     */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        Log.d( TAG, "onCreate()" );
  		super.onCreate(savedInstanceState);
  		this.setContentView( R.layout.gameend );
  
  		TaboozleApplication application =
  			(TaboozleApplication) this.getApplication();
  		curGameManager = application.GetGameManager();		
  		
  		int numRounds = curGameManager.GetNumRounds();
  		int numTeams = curGameManager.GetNumTeams();
  		String[] teamNames = curGameManager.GetTeamNames();
  		
  		// Populate storage table for end game round results
  		long[][] endTable = new long[numRounds][numTeams];
  		for ( int i = 0; i < numTeams; ++i )
  		{
  			long[] roundscores = curGameManager.GetRoundScores((long) i);
  			for ( int j = 0; j < roundscores.length; j++)
  			{
  				endTable[j][i] = roundscores[j];
  			}
  		}

  	  	// Populate and display round scores
  	  	ScrollView list = (ScrollView) findViewById(R.id.EndGameTurnList);
  	  	LinearLayout layout = new LinearLayout(this.getBaseContext());
  	  	layout.setOrientation(LinearLayout.VERTICAL);

  		// Iterate through each row of the end game scores table
  	  	int count = 0;
  	  	for( int i = 0; i < endTable.length; i++ )
  	  	{
  	  	  LinearLayout line = (LinearLayout) LinearLayout.inflate(this.getBaseContext(), 
  	  			  												  R.layout.gameendrow, layout);
  	  	  LinearLayout realLine = (LinearLayout) line.getChildAt(count);
  	  	  
  	  	  for (int j = 0; j < realLine.getChildCount(); j++ )
  	  	  {
  	  		  TextView txt = (TextView) realLine.getChildAt(j);
  	  		  // HACK - Set width to fill parent... really I want to inherit this from the xml
  	  		  txt.setWidth( 400 / numTeams );
  	  		  if(  j < numTeams )
  	  		  {
  	  			  txt.setText(Long.toString(endTable[i][j]));
  	  		  }
  	  		  else
  	  		  {
  	  			  txt.setVisibility( View.GONE );
  	  		  }
  	  	  }
  	  	  ++count;
  	  	}
  	  	list.addView(layout);

  		// Display final scores
  		long[] finalScores = curGameManager.GetTeamScores();
  		final int[] SCORE_VIEW_IDS = new int[]{ R.id.EndGameTeamAScore, R.id.EndGameTeamBScore,
  												R.id.EndGameTeamCScore, R.id.EndGameTeamDScore};
  		for (int i = 0; i < finalScores.length; i++)
  		{
  			TextView teamTotalScoreView = (TextView) findViewById( SCORE_VIEW_IDS[i] );
  			teamTotalScoreView.setText( teamNames[i] + ": " + Long.toString( finalScores[i] ) );
  		}
  		// Hide scores for teams who did not participate
  		for (int i = finalScores.length; i < SCORE_VIEW_IDS.length; i++)
  		{
  			TextView teamTotalScoreView = (TextView) findViewById( SCORE_VIEW_IDS[i] );
  			teamTotalScoreView.setVisibility( View.GONE );
  		}
  		
  		// Display winning team
  		int winningTeamIndex = 0;
  		boolean tieGame = false;
  		
  		for (int i = 0; i < finalScores.length; ++i)
  		{
  			if ( i == winningTeamIndex )
  			{
  				continue;
  			}
  			if (finalScores[winningTeamIndex] < finalScores[i])
  			{
  				winningTeamIndex = i;
  			}
  			else if (finalScores[winningTeamIndex] == finalScores[i])
  			{
  				tieGame = true;
  			}
  		}
  		final int[] TEAM_COLOR_IDS = new int[] { R.color.teamA_text, R.color.teamB_text, R.color.teamC_text, R.color.teamD_text };
  		TextView winner = (TextView) findViewById(R.id.EndGameWinner);
  		if (!tieGame)
  		{
  			winner.setText(teamNames[winningTeamIndex] + " wins!!!!");
  			winner.setTextColor( this.getResources().getColor( TEAM_COLOR_IDS[winningTeamIndex] ) );
  		}
  		else
  		{
  			winner.setText("TIE GAME");
  		}
  		
  		//Display Awards
      Awarder a = new Awarder();
      a.setGameManager( curGameManager );
      this.awards = a.calcAwards();
  		this.startAwardTimer();
  		this.showNextAward();
  		
  		//Set onclick listeners for game end buttons
  		Button mainMenuButton = (Button)this.findViewById( R.id.EndGameMainMenu );
  		mainMenuButton.setOnClickListener( MainMenuListener );

  		Button rematchButton = (Button)this.findViewById( R.id.EndGameRematch );
  		rematchButton.setOnClickListener( RematchListener );
    }

    /**
    * Method handles stopping of any outstanding timers during closing of GameEnd
    */
   @Override
   public void onStop()
   {
     super.onStop();
     Log.d( TAG, "onStop()" );
     this.cycleTimer.cancel();
   }

   /**
    * Method handles stopping of any outstanding timers during closing of GameEnd
    */
   @Override
   public void onDestroy()
   {
     super.onDestroy();
     Log.d( TAG, "onDestroy()" );
     this.cycleTimer.cancel();
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
