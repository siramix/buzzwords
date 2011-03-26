package com.wordfrenzy;

import java.util.LinkedList;

import com.wordfrenzy.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
 * @author The WordFrenzy Team
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and team names
 */
public class GameSetup extends Activity
{ 

  static final int DIALOG_TEAMERROR = 0;
  private LinkedList<Team> teamList = new LinkedList<Team>();  
  private static SharedPreferences gameSetupPrefs;
  private static SharedPreferences.Editor gameSetupPrefEditor;
  
  //A two dimensional array to store the radioID/value pair.
  private static final int[][] ROUND_RADIOS = new int[][] {
            {R.id.GameSetupRounds0,4}, 
            {R.id.GameSetupRounds1,8}, 
            {R.id.GameSetupRounds2,12},
            {R.id.GameSetupRounds3,16}};

  public static final String PREFS_NAME = "gamesetupprefs";     //stored in data/data/wordfrenzy/shared_preferences
  private static final String TEAMA_PREFKEY = "teamA_enabled";  //StringID for Team A quadrant
  private static final String TEAMB_PREFKEY = "teamB_enabled";  //StringID for Team B quadrant
  private static final String TEAMC_PREFKEY = "teamC_enabled";  //StringID for Team C quadrant
  private static final String TEAMD_PREFKEY = "teamD_enabled";  //StringID for Team D quadrant
  private static final String RADIO_INDEX = "round_radio_index"; //Index of the selected round radio 0-3

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
	        
	        int rounds = getCheckedRadioValue();
	        
	        WordFrenzyApplication application =
	          (WordFrenzyApplication) GameSetup.this.getApplication();
	        GameManager gm = new GameManager(GameSetup.this);
	        gm.prepDeck();
        
	        gm.StartGame( teamList, rounds );
	        application.SetGameManager( gm );

     	  	startActivity( new Intent(getApplication().getString(R.string.IntentTurn), getIntent().getData()) );
     	  	
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
             GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.TEAMA_PREFKEY, false);
           }
           else
           {
             teamList.add( Team.TEAMA);             
             b.setBackgroundResource( R.color.teamA_text );
             b.setTextColor( GameSetup.this.getResources().getColor( R.color.teamA_secondary ) );
             GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.TEAMA_PREFKEY, true);
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
             GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.TEAMB_PREFKEY, false);
           }
           else
           {             
             teamList.add( Team.TEAMB );
             b.setBackgroundResource( R.color.teamB_text );
             b.setTextColor( GameSetup.this.getResources().getColor( R.color.teamB_secondary ) );
             GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.TEAMB_PREFKEY, true);
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
              GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.TEAMC_PREFKEY, false);              
            }
            else
            {
              teamList.add( Team.TEAMC);
              b.setBackgroundResource( R.color.teamC_text );
              b.setTextColor( GameSetup.this.getResources().getColor( R.color.teamC_secondary ) );
              GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.TEAMC_PREFKEY, true);    
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
            GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.TEAMD_PREFKEY, false);    
          }
          else
          {
            teamList.add( Team.TEAMD );
            b.setBackgroundResource( R.color.teamD_text );
            b.setTextColor( GameSetup.this.getResources().getColor( R.color.teamD_secondary ) );
            GameSetup.gameSetupPrefEditor.putBoolean(GameSetup.TEAMD_PREFKEY, true);    
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
	
	// Get the current game setup preferences
	GameSetup.gameSetupPrefs = getSharedPreferences(PREFS_NAME, 0 );	
	GameSetup.gameSetupPrefEditor = GameSetup.gameSetupPrefs.edit();	

  // Get the default radio button
  int radio_default = GameSetup.gameSetupPrefs.getInt(GameSetup.RADIO_INDEX, 1);

  RadioButton radio = (RadioButton) this.findViewById( GameSetup.ROUND_RADIOS[0][0] );
  
  for( int i=0; i<GameSetup.ROUND_RADIOS.length; ++i )
  {
    radio = (RadioButton) this.findViewById( GameSetup.ROUND_RADIOS[i][0] );
    radio.setText(String.valueOf(GameSetup.ROUND_RADIOS[i][1]));
  }
  
  radio = (RadioButton) this.findViewById( GameSetup.ROUND_RADIOS[radio_default][0] );
  radio.setChecked(true);  
  
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
  
	// Look at the setup preferences at each team variable and set the team
	// defaults appropriately
	
	// Set team A default selection
	if ( GameSetup.gameSetupPrefs.getBoolean(TEAMA_PREFKEY, false) )
	{
	  teamList.add( Team.TEAMA );
	}
	else
	{
	  teamAButton.setBackgroundResource( R.color.inactiveButton );
	  teamAButton.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
	}
	
	// Set team B default selection
	if ( GameSetup.gameSetupPrefs.getBoolean(TEAMB_PREFKEY, false) )
	{
	  teamList.add( Team.TEAMB );
	}
	else
  {
    teamBButton.setBackgroundResource( R.color.inactiveButton );
    teamBButton.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
  }
	
	//Set team C default selection
	if ( GameSetup.gameSetupPrefs.getBoolean(TEAMC_PREFKEY, false) )
	{ 
	  teamList.add( Team.TEAMC );
	} 	
	else
  {
    teamCButton.setBackgroundResource( R.color.inactiveButton );
    teamCButton.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
  }
	
	//Set team D default selection
	if ( GameSetup.gameSetupPrefs.getBoolean(TEAMD_PREFKEY, false) )
	{
	  teamList.add( Team.TEAMD );
	}
	else
  {
    teamDButton.setBackgroundResource( R.color.inactiveButton );
    teamDButton.setTextColor( GameSetup.this.getResources().getColor( R.color.genericBG ) );
  }
	
	TextView helpText = (TextView) this.findViewById(R.id.GameSetup_HelpText_Team);
	helpText.setAnimation(this.FadeInHelpText(1000));
	helpText = (TextView) this.findViewById(R.id.GameSetup_HelpText_Turn);
	helpText.setAnimation(this.FadeInHelpText(3000));

}

  /**
   * Getter that returns the number of turns that matches the checked radio button.
   * Called during onPause and StartGame to allow preferences to save.
   * @return
   */
  private int getCheckedRadioValue()
  {    
    Log.d( TAG, "getCheckedRadioValue()" );
    // Get number of rounds based on radio button selection
    
    int rounds = 0;
    for ( int i = 0; i < GameSetup.ROUND_RADIOS.length; i++)
    {
      RadioButton test = (RadioButton) GameSetup.this.findViewById( GameSetup.ROUND_RADIOS[i][0]);
      if (test.isChecked())
      {
        rounds = GameSetup.ROUND_RADIOS[i][1];
        GameSetup.gameSetupPrefEditor.putInt(GameSetup.RADIO_INDEX, i);
        break;
      }
    }
    
    return rounds;  
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
            .setTitle("Need more teams!")
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
  
  /**
   * Override onPause to prevent activity specific processes from running while app is in background
   */
  @Override
  public void onPause()
  {
     Log.d( TAG, "onPause()" );   
     super.onPause();
     WordFrenzyApplication application = (WordFrenzyApplication) this.getApplication();
     MediaPlayer mp = application.GetMusicPlayer();
     mp.pause();
     
     getCheckedRadioValue(); // Called before pref commit to save radio value     
     GameSetup.gameSetupPrefEditor.commit();
  }

  /**
   * Override OnResume to resume activity specific processes
   */
  @Override
  public void onResume()
  {
     Log.d( TAG, "onResume()" );   
     super.onResume();
     
     // Resume Title Music
     WordFrenzyApplication application = (WordFrenzyApplication) this.getApplication();
     MediaPlayer mp = application.GetMusicPlayer();
     if( !mp.isPlaying())
     {
         mp.start();   
     }
  }
}
