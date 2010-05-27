package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
/**
 * @author The Taboozle Team
 * This activity class is responsible for summarizing the turn and the hand-off 
 * into the next turn or game end.
 */
public class TurnSummary extends Activity
{

	/**
	  * Watches the button that handles hand-off to the next turn activity.
	  */
	  private OnClickListener NextTurnListener = new OnClickListener() 
	  {
	      public void onClick(View v) 
	      {
	    	  	startActivity(new Intent(Intent.ACTION_RUN, getIntent().getData()));
	      }
	  }; // End NextTurnListener
			
	
/**
* onCreate - initializes the activity to display the results of the turn.
*/
@Override
public void onCreate( Bundle savedInstanceState )
{
	super.onCreate( savedInstanceState );
	// Setup the view
	this.setContentView(R.layout.turnsummary);
	
  Button playGameButton = (Button)this.findViewById( R.id.TurnSumNextTurn );
  playGameButton.setOnClickListener( NextTurnListener );
}

}
