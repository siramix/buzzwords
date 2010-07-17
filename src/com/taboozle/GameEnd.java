package com.taboozle;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
      
    public void onCreate( Bundle savedInstanceState ) 
    {
		super.onCreate(savedInstanceState);
		this.setContentView( R.layout.gameend );
		  
		ListView list = (ListView) findViewById(R.id.EndGameTurnList);
		
		TaboozleApplication application = 
			(TaboozleApplication) this.getApplication();
		GameManager game = application.GetGameManager();      
		
		//ArrayList<Integer> roundScoreList = new ArrayList<Integer>();
		
		//for(int i=0; i<game.GetNumTeams(); ++i)
		//{
		//	round
		//}
		/*
		SimpleAdapter roundScores = new SimpleAdapter(this, roundScoreList, R.layout.gameend,
		            new String[] {"title", "rws"}, new int[] {R.id.TurnSum_CardTitle, R.id.TurnSum_CardRWS});
		list.setAdapter(roundScores);	  
	  */
		long[] totalscores = game.GetTeamScores().clone();	
		
		TextView teamATotalScore = (TextView) findViewById(R.id.EndGameTeamAScore);
		teamATotalScore.setText("Team A: " + Long.toString(totalscores[0]));
		
		TextView teamBTotalScore = (TextView) findViewById(R.id.EndGameTeamBScore);
		teamBTotalScore.setText("Team B: " + Long.toString(totalscores[1]));
		
		TextView curTeam = (TextView) findViewById(R.id.EndGameCurTeamIndex);
			curTeam.setText("Current Team: " + Long.toString(game.GetActiveTeamIndex())); 	  
	  
    }
}
