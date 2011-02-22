package com.taboozle;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
   * Set of awards to be iterated through on display.
   */
  private List<Award> awards;
  
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
    final String[] stringAwards = new String[gm.GetTeams().size()];
    final String[] stringDescriptions = new String[gm.GetTeams().size()];
    final int[] colors = new int[gm.GetTeams().size()];
    for( int i = 0; i < stringAwards.length; ++i )
    {
      stringAwards[i] = this.awards.get( i ).name;
      stringDescriptions[i] = this.awards.get( i ).getExplanation();
      colors[i] = gm.GetTeams().get(i).getText();
    }

    TextView awardName = (TextView) findViewById(R.id.EndGame_AwardShowcase_Name);
    TextView awardDescription = (TextView) findViewById(R.id.EndGame_AwardShowcase_Subtext);
//    TextView awardTeamName = (TextView) findViewById(R.id.EndGameAwardTeamName);
    this.awardIndex = (this.awardIndex + 1) % curGameManager.GetNumTeams();
    awardName.setText(stringAwards[this.awardIndex]);
    awardDescription.setText(stringDescriptions[this.awardIndex]);

    ImageView smallaward = (ImageView) findViewById( R.id.GameEnd_AwardShowcase_Icon );
    //smallaward.setImageDrawable( TODO: GetAwardIcon());
    Drawable d = getResources().getDrawable( R.drawable.award_cosmo );
    //smallaward.setImageDrawable(adjust(d));
    smallaward.setImageDrawable(d);
    smallaward.setColorFilter( res.getColor(colors[this.awardIndex]), Mode.MULTIPLY );
    
//    awardTeamName.setText(curGameManager.GetTeams().get(this.awardIndex).getName());
//    awardTeamName.setTextColor(this.getResources().getColor( TEAM_COLOR_IDS[this.awardIndex] ));
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
        final int[] TEAM_SCORE_GROUPS = new int[]{ R.id.EndGame_Team1Group, R.id.EndGame_Team2Group,
            R.id.EndGame_Team3Group, R.id.EndGame_Team4Group};  	    
  	    
  	    // Ids for score placement text views.  These should only be changed in the event of ties
        final int[] TEAM_PLACE_IDS = new int[]{ R.id.EndGame_Score1_1st, R.id.EndGame_Score2_2nd,
            R.id.EndGame_Score3_3rd, R.id.EndGame_Score4_4th};  	    
  	    
        // Ids for team names
        final int[] TEAM_NAME_IDS = new int[]{ R.id.EndGame_Score1_Name, R.id.EndGame_Score2_Name,
            R.id.EndGame_Score3_Name, R.id.EndGame_Score4_Name};
  	    
        // Ids for score values
  		final int[] TEAM_SCORE_IDS = new int[]{ R.id.EndGame_Score1_Score, R.id.EndGame_Score2_Score,
  												R.id.EndGame_Score3_Score, R.id.EndGame_Score4_Score};
    	
  		// Ids for award icons on team list
  		/*final int[] TEAM_AWARD_IDS = new int[]{ R.id.EndGame_Score1_Award, R.id.EndGame_Score2_Award,
            R.id.EndGame_Score3_Award, R.id.EndGame_Score4_Award};*/

  		// Display all team scores.  Iterate through all team groups.  Hide ranks that no team earned, 
  		// and set values on existing teams
  		for (int i = 0; i < TEAM_SCORE_GROUPS.length; i++)
  		{
  		  if(i >= teams.size())
  		  {
  		    LinearLayout teamTotalScoreView = (LinearLayout) findViewById( TEAM_SCORE_GROUPS[i] );
  		    teamTotalScoreView.setVisibility( View.GONE );
  		  }
  		  else
  		  {
  		    // team list is sorted lowest score to highest, so we want to add them highest first.
  		    int teamIndex = ( ( teams.size() - 1 ) - i );
  		    // Set ranking
  		    TextView text = (TextView) findViewById( TEAM_PLACE_IDS[i]);
  		    text.setTextColor( res.getColor( teams.get( teamIndex ).getText() ));
  		    //text.setText( ToDo: GetTeamRank() -- Return 1 for multiple teams for tie)
  		    // Set team name and color
  		    text = (TextView) findViewById( TEAM_NAME_IDS[i]);
            text.setTextColor( res.getColor( teams.get( teamIndex ).getText() ));
            text.setText(teams.get(teamIndex).getName());
            // Set team score and color
            text = (TextView) findViewById( TEAM_SCORE_IDS[i]);
            text.setTextColor( res.getColor( teams.get( teamIndex ).getText() ));
            text.setText(Integer.toString(teams.get(teamIndex).getScore()));
            //ImageView smallaward = (ImageView) findViewById( TEAM_AWARD_IDS[i]);
            
  		  }
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
