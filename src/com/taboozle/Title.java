/**
 * 
 */
package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
/**
 * This is the activity class that kicks off Taboozle
 * 
 * @author The Taboozle Team
 */
public class Title extends Activity {

  private OnClickListener PlayGameListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
    	  	startActivity(new Intent(Intent.ACTION_RUN, getIntent().getData()));
      }
  };	
	
/**
* onCreate - initializes the activity to display the word you have to cause
* your team mates to say with the words you cannot say below.
*/
@Override
public void onCreate( Bundle savedInstanceState )
{
	super.onCreate( savedInstanceState );
	// Setup the view
	this.setContentView(R.layout.title );

    ImageButton playGameButton = (ImageButton)this.findViewById( R.id.PlayGameButton );
    playGameButton.setOnClickListener( PlayGameListener );
    
}
}
