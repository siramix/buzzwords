package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
                
        String[] currentNames = application.GetGameManager().GetTeamNames();
        
        GameManager newgm = new GameManager(GameEnd.this);
        newgm.PrepDeck();
        newgm.StartGame( currentNames );
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
  		GameManager game = application.GetGameManager();		
  		
  		int numRounds = (int) game.GetNumRounds();
  		int numTeams = game.GetNumTeams();
  		String[] teamNames = game.GetTeamNames();
  		
  		// Populate storage table for end game round results
  		long[][] endTable = new long[numRounds][numTeams];
  		for ( int i = 0; i < numTeams; ++i )
  		{
  			long[] roundscores = game.GetRoundScores((long) i);
  			for ( int j = 0; j < roundscores.length; j++)
  			{
  				endTable[j][i] = roundscores[j];
  			}
  		}

  	  	// Populate and display list of cards
  	  	ScrollView list = (ScrollView) findViewById(R.id.EndGameTurnList);
  	  	LinearLayout layout = new LinearLayout(this.getBaseContext());
  	  	layout.setOrientation(LinearLayout.VERTICAL);

  		// iterate through each row of the end game scores table
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
  		long[] finalScores = game.GetTeamScores();
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
  
  		final int[] TEAM_COLOR_IDS = new int[] { R.color.teamA, R.color.teamB, R.color.teamC, R.color.teamD };
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
  		
  		//Set onclick listeners for game end buttons
  		Button mainMenuButton = (Button)this.findViewById( R.id.EndGameMainMenu );
  		mainMenuButton.setOnClickListener( MainMenuListener );

  		Button rematchButton = (Button)this.findViewById( R.id.EndGameRematch );
  		rematchButton.setOnClickListener( RematchListener );
    }
}
