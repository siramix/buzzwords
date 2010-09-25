package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;
/**
 * @author The Taboozle Team
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and team names
 */
public class GameSetup extends Activity
{

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
	        EditText textField = (EditText) GameSetup.this.findViewById(R.id.SetTeamAName);
	        String teamNameA = textField.getText().toString();
	        textField = (EditText) GameSetup.this.findViewById(R.id.SetTeamBName);
	        String teamNameB = textField.getText().toString();
	        
	        String[] teams = new String[]{ teamNameA, teamNameB };
	        gm.StartGame( teams );
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
}

}
