package com.taboozle;

import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
/**
 * @author The Taboozle Team
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and team names
 */
public class GameSetup extends Activity
{ 

  static final int DIALOG_TEAMERROR = 0;
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
	        
	        // Validate team numbers
	        if( GameSetup.this.teamList.size() <= 1 )
	        {
	            GameSetup.this.showDialog( DIALOG_TEAMERROR );
	            return;
	        }
	        
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
     	  	
     	  	MediaPlayer mp = application.GetMusicPlayer();
     	  	mp.stop();
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
	   * @return The animation that fades in helper text
	   * screen
	   */
	  private Animation FadeInHelpText(long delay)
	  {
	    Log.d( TAG, "FadeInHelpText()" );
	    Animation fade = new AlphaAnimation(0.0f, 1.0f);
	    fade.setStartOffset(delay);
	    fade.setDuration(2000);
	    return fade;
	  }
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
	teamList.add( Team.TEAMC );
	teamBButton.setBackgroundResource( R.color.inactiveButton );
	teamBButton.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
	teamDButton.setBackgroundResource( R.color.inactiveButton );
	teamDButton.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
	
	TextView helpText = (TextView) this.findViewById(R.id.GameSetup_HelpText_Team);
	helpText.setAnimation(this.FadeInHelpText(1000));
	helpText = (TextView) this.findViewById(R.id.GameSetup_HelpText_Turn);
	helpText.setAnimation(this.FadeInHelpText(3000));
}

  /**
  * Handle creation of team warning dialog, used to prevent starting a game with too few teams.
  * returns Dialog object explaining team error
  */
  @Override
  protected Dialog onCreateDialog(int id)
  {
   Log.d( TAG, "onCreateDialog(" + id + ")" );
   Dialog dialog = null;
   AlertDialog.Builder builder = null;
   
   switch(id) {
   case DIALOG_TEAMERROR:
     builder = new AlertDialog.Builder(this);
     builder.setMessage( "You must have at least two teams to start the game." )
            .setCancelable(false)
            .setTitle("Team Error")
            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                }
              });       
     dialog = builder.create();
     break;
   default:
       dialog = null;
   }
   return dialog;
  
  }

}
