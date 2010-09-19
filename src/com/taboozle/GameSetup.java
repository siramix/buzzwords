package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
/**
 * @author The Taboozle Team
 * This activity class is responsible for gathering game information before the
 * game starts such as number of teams and team names
 */
public class GameSetup extends Activity
{

	/**
	  * Watches the button that handles hand-off to the Turn activity.
	  */
	  private final OnClickListener StartGameListener = new OnClickListener()
	  {
	      public void onClick(View v)
	      {
	        TaboozleApplication application =
	          (TaboozleApplication) GameSetup.this.getApplication();
	        GameManager gm = new GameManager(GameSetup.this);
	        gm.PrepDeck();
	        
	        // Get text from each team EditText view
	        EditText textField = (EditText) GameSetup.this.findViewById(R.id.TeamA);
	        String teamNameA = textField.getText().toString();
	        textField = (EditText) GameSetup.this.findViewById(R.id.TeamB);
	        String teamNameB = textField.getText().toString();
	        
	        String[] teams = new String[]{ teamNameA, teamNameB };
	        gm.StartGame( teams );
	        application.SetGameManager( gm );
	        
     	  	startActivity(new Intent(getApplication().getString(R.string.IntentTurn), getIntent().getData()));
	      }
	  };
/**
* onCreate - initializes the activity to display the results of the turn.
*/
@Override
public void onCreate( Bundle savedInstanceState )
{
	super.onCreate( savedInstanceState );
	// Setup the view
	this.setContentView(R.layout.gamesetup);

	Button startGameButton = (Button)this.findViewById( R.id.StartGameButton );
	startGameButton.setOnClickListener( StartGameListener );
}

}
