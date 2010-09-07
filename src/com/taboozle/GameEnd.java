package com.taboozle;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
   * Listener for the 'Correct' button. It deals with the flip to the next
   * card.
   */
  private final OnClickListener MainMenuListener = new OnClickListener()
  {
      public void onClick(View v)
      {
        startActivity(new Intent( getApplication().getString( R.string.IntentTitle ),
                                  getIntent().getData()));
      }
  }; // End MainMenuListener

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
		super.onCreate(savedInstanceState);
		this.setContentView( R.layout.gameend );

		TaboozleApplication application =
			(TaboozleApplication) this.getApplication();
		GameManager game = application.GetGameManager();		
		
		int numRounds = (int) game.GetNumRounds();
		int numTeams = game.GetNumTeams();
		
		long[][] endTable = new long[numRounds][numTeams];

		for ( int i = 0; i < numTeams; ++i )
		{
			long[] roundscores = game.GetRoundScores((long) i);
			for ( int j = 0; j < roundscores.length; j++)
			{
				endTable[j][i] = roundscores[j];
			}
		}

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
		
		long[] finalScores = game.GetTeamScores();
		
		TextView teamATotalScore = (TextView) findViewById(R.id.EndGameTeamAScore);
		teamATotalScore.setText("Team A: " + Long.toString(finalScores[0]));

		TextView teamBTotalScore = (TextView) findViewById(R.id.EndGameTeamBScore);
		teamBTotalScore.setText("Team B: " + Long.toString(finalScores[1]));
		
		Button mainMenuButton = (Button)this.findViewById( R.id.EndGameMainMenu );
			mainMenuButton.setOnClickListener( MainMenuListener );

    }
}
