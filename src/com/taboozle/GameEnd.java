package com.taboozle;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
   * Listener for the 'Correct' button. It deals with the flip to the next
   * card.
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
  
  		// Populate list display
  		ListView list = (ListView) findViewById(R.id.EndGameTurnList);		
  		ArrayList<HashMap<String, String>> endTableRows = new ArrayList<HashMap<String, String>>();
  		for (int i = 0; i < numRounds; ++i)
  		{
  			HashMap<String, String> map = new HashMap<String, String>();
  			for(int j = 0; j < numTeams; ++j)
  			{
  				map.put("team" + Integer.toString(j+1), Long.toString(endTable[i][j]));
  			}
  			endTableRows.add(map);
  		}
  
  		SimpleAdapter gameEndTable = new SimpleAdapter(this, endTableRows, R.layout.gameendrow,
  	            new String[] {"team1", "team2"}, new int[] {R.id.GameEnd_Team1, 
  				R.id.GameEnd_Team2});
  		
  		list.setAdapter(gameEndTable);			
  		
  		// Display final scores
  		long[] finalScores = game.GetTeamScores();
  		int[] scoreViewIds = new int[]{R.id.EndGameTeamAScore, R.id.EndGameTeamBScore};
  		for (int i = 0; i < scoreViewIds.length; i++)
  		{
  			TextView teamTotalScoreView = (TextView) findViewById(scoreViewIds[i]);
  			teamTotalScoreView.setText(teamNames[i] + ": " + Long.toString(finalScores[i]));
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
  		
  		TextView winner = (TextView) findViewById(R.id.EndGameWinner);
  		if (!tieGame)
  		{
  			winner.setText(teamNames[winningTeamIndex] + " wins!!!!");
  		}
  		else
  		{
  			winner.setText("TIE GAME");
  		}
  		
  		Button mainMenuButton = (Button)this.findViewById( R.id.EndGameMainMenu );
  			mainMenuButton.setOnClickListener( MainMenuListener );

    }
}
