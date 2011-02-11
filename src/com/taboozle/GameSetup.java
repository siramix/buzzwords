package com.taboozle;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
/**
 * @author The Taboozle Team
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and team names
 */
public class GameSetup extends Activity
{ 
  
  private LinkedList<Team> teamList = new LinkedList<Team>();  

  /**
   * logging tag
   */
  public static String TAG = "GameSetup";
  
	/**
	  * Watches the button that handles hand-off to the Turn activity.
	  */
	  private final OnClickListener StartGameListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
	        Log.d( TAG, "StartGameListener onClick()" );
	        TaboozleApplication application =
	          (TaboozleApplication) GameSetup.this.getApplication();
	        GameManager gm = new GameManager(GameSetup.this);
	        gm.PrepDeck();
	        
	        // Get number of rounds based on radio button selection
	        final int[] ROUND_RADIO_BUTTONS = new int[] {R.id.GameSetupRounds1, R.id.GameSetupRounds5, R.id.GameSetupRounds10,
	        		                          R.id.GameSetupRounds15,};
	        final int[] ROUND_CHOICES = new int[] {1, 5, 10, 15,};
	        int rounds = 0;
	        for ( int i = 0; i < ROUND_RADIO_BUTTONS.length; i++)
	        {
	        	RadioButton test = (RadioButton) GameSetup.this.findViewById( ROUND_RADIO_BUTTONS[i]);
	        	if (test.isChecked())
	        	{
	        		rounds = ROUND_CHOICES[i];
	        		break;
	        	}
	        }
	        
	        gm.StartGame( teamList, rounds );
	        application.SetGameManager( gm );
	        
     	  	startActivity(new Intent(getApplication().getString(R.string.IntentTurn), getIntent().getData()));
	      }
	  };
	  
	  /**
     * Watches the button that adds the third team to the list
     */    
     private final OnClickListener AddTeamAListener = new OnClickListener()
     {
         public void onClick(View v)
         {
           Log.d( TAG, "AddTeamAListener onClick()" );
           Button b = (Button) v;
           
           if( teamList.remove( Team.TEAMA ) )
           {
             b.setBackgroundResource( R.color.inactiveButton );
             b.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
           }
           else
           {
             teamList.add( Team.TEAMA);
             b.setBackgroundResource( R.color.teamA_text );
             b.setTextColor( GameSetup.this.getResources().getColor( R.color.teamA_secondary ) );
           }
         }
     };

     /**
     * Watches the button that adds the fourth team to the list
     */    
     private final OnClickListener AddTeamBListener = new OnClickListener()
     {
         public void onClick(View v)
         {
           Log.d( TAG, "AddTeamBListener onClick()" );
           Button b = (Button) v;
           
           if( teamList.remove( Team.TEAMB ) )
           {
             b.setBackgroundResource( R.color.inactiveButton );
             b.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
           }
           else
           {
             teamList.add( Team.TEAMB );
             b.setBackgroundResource( R.color.teamB_text );
             b.setTextColor( GameSetup.this.getResources().getColor( R.color.teamB_secondary ) );
           }
         }
     };
	  
    /**
	  * Watches the button that adds the third team to the list
	  */	  
	  private final OnClickListener AddTeamCListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
	        Log.d( TAG, "AddTeamCListener onClick()" );
	        Button b = (Button) v;
          
          if( teamList.remove( Team.TEAMC ) )
          {
            b.setBackgroundResource( R.color.inactiveButton );
            b.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
          }
          else
          {
            teamList.add( Team.TEAMC);
            b.setBackgroundResource( R.color.teamC_text );
            b.setTextColor( GameSetup.this.getResources().getColor( R.color.teamC_secondary ) );
          }
	      }
	  };

	  /**
	  * Watches the button that adds the fourth team to the list
	  */	  
	  private final OnClickListener AddTeamDListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
	        Log.d( TAG, "AddTeamDListener onClick()" );
	        Button b = (Button) v;
          
          if( teamList.remove( Team.TEAMD ) )
          {
            b.setBackgroundResource( R.color.inactiveButton );
            b.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
          }
          else
          {
            teamList.add( Team.TEAMD );
            b.setBackgroundResource( R.color.teamD_text );
            b.setTextColor( GameSetup.this.getResources().getColor( R.color.teamD_secondary ) );
          }
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
	this.setContentView(R.layout.gamesetup);

	// Bind view buttons
	Button startGameButton = (Button)this.findViewById( R.id.StartGameButton );
	startGameButton.setOnClickListener( StartGameListener );
	
	Button teamAButton = (Button) this.findViewById( R.id.GameSetup_ButtonTeamA );
  teamAButton.setOnClickListener( AddTeamAListener );
  
  Button teamBButton = (Button) this.findViewById( R.id.GameSetup_ButtonTeamB );
  teamBButton.setOnClickListener( AddTeamBListener );
	
	Button teamCButton = (Button) this.findViewById( R.id.GameSetup_ButtonTeamC );
	teamCButton.setOnClickListener( AddTeamCListener );
	
	Button teamDButton = (Button) this.findViewById( R.id.GameSetup_ButtonTeamD );
	teamDButton.setOnClickListener( AddTeamDListener );
  
  teamList.add( Team.TEAMA );
  teamList.add( Team.TEAMB );
  teamCButton.setBackgroundResource( R.color.inactiveButton );
  teamCButton.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
  teamDButton.setBackgroundResource( R.color.inactiveButton );
  teamDButton.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
  
}

}
