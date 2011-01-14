package com.taboozle;

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
  
  private int numTeams;

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
	        
	        // Get text from each team EditText view
	        final int[] TEAM_NAME_VIEWS = new int[] {R.id.SetTeamNameA, R.id.SetTeamNameB, 
	        								R.id.SetTeamNameC, R.id.SetTeamNameD};
	        String[] teams = new String[ GameSetup.this.numTeams ];
	        for( int i = 0; i < numTeams; i++)
	        {
		        EditText textField = (EditText) GameSetup.this.findViewById( TEAM_NAME_VIEWS[i] );
	        	teams[i] = textField.getText().toString();
	        }
	        
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
	        gm.StartGame( teams, rounds );
	        application.SetGameManager( gm );
	        
     	  	startActivity(new Intent(getApplication().getString(R.string.IntentTurn), getIntent().getData()));
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

	        ViewFlipper flipper = (ViewFlipper) GameSetup.this.findViewById( R.id.AddTeamCFlipper );
	        flipper.showNext();
	        
	        ViewFlipper nextFlipper = (ViewFlipper) GameSetup.this.findViewById( R.id.AddTeamDFlipper );
	        nextFlipper.setVisibility( View.VISIBLE );
	        
	        GameSetup.this.numTeams++;
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
	
	        ViewFlipper flipper = (ViewFlipper) GameSetup.this.findViewById( R.id.AddTeamDFlipper );
	        flipper.showNext();
	        
	        GameSetup.this.numTeams++;
	      }
	  };
	  
	  /**
	  * Watches the button that removes the third team from the list
	  */	  
	  private final OnClickListener RemoveTeamCListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
	        Log.d( TAG, "AddTeamDListener onClick()" );
	
	        ViewFlipper flipper = (ViewFlipper) GameSetup.this.findViewById( R.id.AddTeamCFlipper );
	        flipper.showPrevious();
	        
	        ViewFlipper flipperD = (ViewFlipper) GameSetup.this.findViewById( R.id.AddTeamDFlipper );
	        flipperD.setVisibility( View.INVISIBLE );
	        
	        GameSetup.this.numTeams--;
	      }
	  };	
	  
	  /**
	  * Watches the button that removes the fourth team from the list
	  */	  
	  private final OnClickListener RemoveTeamDListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
	        Log.d( TAG, "AddTeamDListener onClick()" );
	
	        ViewFlipper flipper = (ViewFlipper) GameSetup.this.findViewById( R.id.AddTeamDFlipper );
	        flipper.showPrevious();
	        
	        GameSetup.this.numTeams--;
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
	
	Button addTeamCButton = (Button) this.findViewById( R.id.AddTeamCButton );
	addTeamCButton.setOnClickListener( AddTeamCListener );
	
	Button addTeamDButton = (Button) this.findViewById( R.id.AddTeamDButton );
	addTeamDButton.setOnClickListener( AddTeamDListener );

	Button removeTeamCButton = (Button) this.findViewById( R.id.RemoveTeamCButton );
	removeTeamCButton.setOnClickListener( RemoveTeamCListener );
	
	Button removeTeamDButton = (Button) this.findViewById( R.id.RemoveTeamDButton );
	removeTeamDButton.setOnClickListener( RemoveTeamDListener );
	
	// Initialize number of teams
	this.numTeams = 2;
}

}
