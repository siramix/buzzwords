package com.taboozle;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
   * logging tag
   */
  public static String TAG = "GameEnd";

  /**
   * This is a reference to the current game manager
   */
  private GameManager curGameManager;
 
  /**
   * Resources to be retrieved throughout GameEnd display 
   */
  private Resources res;

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
        newgm.StartGame( curgm.GetTeams(), curgm.GetNumRounds() );
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
  	    this.res = this.getResources();
  
  		TaboozleApplication application =
  			(TaboozleApplication) this.getApplication();
  		curGameManager = application.GetGameManager();		

  	    List<Team> teams = curGameManager.GetTeams();
  	    
        // Sort the list by scores to determine the winner(s)
        Collections.sort( teams, (Team.TEAMA).new ScoreComparator() );

  	    // Ids for Scoreboard list rows (one per team).
        final int[] TEAM_SCORE_GROUPS = new int[]{ R.id.GameEnd_Scores_1, R.id.GameEnd_Scores_2,
            R.id.GameEnd_Scores_3, R.id.GameEnd_Scores_4};  	    
  	    
  	    // Ids for score placement text views.  These should only be changed in the event of ties
        final int[] TEAM_PLACE_IDS = new int[]{ R.id.GameEnd_Scores_1_place, R.id.GameEnd_Scores_2_place,
            R.id.GameEnd_Scores_3_place, R.id.GameEnd_Scores_4_place};  	    
  	    
        // Ids for team names
        final int[] TEAM_NAME_IDS = new int[]{ R.id.GameEnd_Scores_1_name, R.id.GameEnd_Scores_2_name,
            R.id.GameEnd_Scores_3_name, R.id.GameEnd_Scores_4_name};
  	    
        // Ids for score values
  		final int[] TEAM_SCORE_IDS = new int[]{ R.id.GameEnd_Scores_1_score, R.id.GameEnd_Scores_2_score,
  												R.id.GameEnd_Scores_3_score, R.id.GameEnd_Scores_4_score};
  		
        // Ids for scoreboard backgrounds
        final int[] TEAM_SCORE_BGS = new int[]{ R.id.GameEnd_Scores_1_BG, R.id.GameEnd_Scores_2_BG,
                                                R.id.GameEnd_Scores_3_BG, R.id.GameEnd_Scores_4_BG};

  		// Setup score displays.  Iterate through all team groups, setting scores for teams that played
  		// and disabling the group for teams that did not play
  		for (int i = 0; i < TEAM_SCORE_GROUPS.length; i++)
  		{
  		  if(i >= teams.size())
  		  {
  		    // Gray out rows for teams that didn't play
            View bg = (View) findViewById( TEAM_SCORE_BGS[i]);
            bg.setBackgroundResource( R.color.turnSummaryListItem);
            // Hide place, Hide Name, Hide Score
            TextView text = (TextView) findViewById( TEAM_PLACE_IDS[i]);
            text.setVisibility(View.INVISIBLE);
            text = (TextView) findViewById( TEAM_NAME_IDS[i]);
            text.setVisibility(View.INVISIBLE);
            text = (TextView) findViewById( TEAM_SCORE_IDS[i]);
            text.setVisibility(View.INVISIBLE);
  		  }
  		  else
  		  {
            // team list is sorted lowest score to highest, so we want to add them highest first.
            int teamIndex = ( ( teams.size() - 1 ) - i );
  		    
  		    // Set ranking
  		    TextView text = (TextView) findViewById( TEAM_PLACE_IDS[i]);
  		    //text.setTextColor( res.getColor( teams.get( teamIndex ).getText() ));
  		    //text.setText( ToDo: GetTeamRank() -- Return 1 for multiple teams for tie)
  		    // Set team name and color
  		    text = (TextView) findViewById( TEAM_NAME_IDS[i]);
            text.setTextColor( res.getColor( teams.get( teamIndex ).getSecondaryColor() ));
            text.setText(teams.get(teamIndex).getName());
            // Set team score and color
            text = (TextView) findViewById( TEAM_SCORE_IDS[i]);
            text.setTextColor( res.getColor( teams.get( teamIndex ).getText() ));
            text.setText(Integer.toString(teams.get(teamIndex).getScore()));
            // Set background color
            View bg = (View) findViewById( TEAM_SCORE_BGS[i]);
            bg.setBackgroundResource( teams.get( teamIndex).getText());
            
  		  }
  		}
  		
  		// Set Winner text
  		int winnerIndex = teams.size() - 1;
  		TextView text = (TextView) findViewById( R.id.GameEnd_WinnerText);
        text.setTextColor( res.getColor( teams.get( winnerIndex ).getText() ));
        text.setText( teams.get( winnerIndex ).getName() + " Wins!");
        // set font
        Typeface antonFont = Typeface.createFromAsset(getAssets(), "fonts/Anton.ttf");
        text.setTypeface( antonFont );
  		
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
   }

   /**
    * Method handles stopping of any outstanding timers during closing of GameEnd
    */
   @Override
   public void onDestroy()
   {
     super.onDestroy();
     Log.d( TAG, "onDestroy()" );
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
