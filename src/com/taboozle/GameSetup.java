package com.taboozle;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ViewFlipper;
/**
 * @author The Taboozle Team
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and team names
 */
public class GameSetup extends Activity
{
  private class Team
  {
    public int color;
    public String name;
  }
  
  private final int A = 0;
  private final int B = 1;
  private final int C = 2;
  private final int D = 3;
  
  private Team[] teams = new Team[4];
  
  
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
	        String[] teams = new String[GameSetup.this.teamList.size()];
	        int[] colors = new int[GameSetup.this.teamList.size()];
	        int index = 0;
	        Iterator<Team> itr = GameSetup.this.teamList.iterator();
	        while(itr.hasNext())
	        {
	          teams[index] = itr.next().name;
	          colors[index] = itr.next().color;
	          ++index;
	        }
	        
	        gm.StartGame( teams, colors, rounds );
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
           
           if( teamList.remove( teams[A] ) )
           {
             b.setBackgroundResource( R.color.inactiveButton );
             b.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
           }
           else
           {
             teamList.add( teams[A] );
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
           
           if( teamList.remove( teams[B] ) )
           {
             b.setBackgroundResource( R.color.inactiveButton );
             b.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
           }
           else
           {
             teamList.add( teams[B] );
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
          
          if( teamList.remove( teams[C] ) )
          {
            b.setBackgroundResource( R.color.inactiveButton );
            b.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
          }
          else
          {
            teamList.add( teams[C] );
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
          
          if( teamList.remove( teams[D] ) )
          {
            b.setBackgroundResource( R.color.inactiveButton );
            b.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
          }
          else
          {
            teamList.add( teams[D] );
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
	
	this.teams[0] = new Team();
  this.teams[0].name = GameSetup.this.getString( R.string.teamnameA );
  this.teams[0].color = 0;
  
  this.teams[1] = new Team();
  this.teams[1].name = GameSetup.this.getString( R.string.teamnameB );
  this.teams[1].color = 1;
	
  this.teams[2] = new Team();
  this.teams[2].name = GameSetup.this.getString( R.string.teamnameC );
  this.teams[2].color = 2;
  
  this.teams[3] = new Team();
  this.teams[3].name = GameSetup.this.getString( R.string.teamnameD );
  this.teams[3].color = 3;
}

}
