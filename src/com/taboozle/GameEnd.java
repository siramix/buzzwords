package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

		//ListView list = (ListView) findViewById(R.id.EndGameTurnList);

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

  	Button mainMenuButton = (Button)this.findViewById( R.id.EndGameMainMenu );
		mainMenuButton.setOnClickListener( MainMenuListener );

    }
}
