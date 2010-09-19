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
public class Title extends Activity
{
  /**
  * PlayGameListener is used for the start game button.  It launches the next 
  * activity.
  */
  private OnClickListener PlayGameListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
        TaboozleApplication application = 
          (TaboozleApplication) Title.this.getApplication();
        GameManager gm = new GameManager(Title.this);
        gm.PrepDeck();
        String[] teams = new String[]{ "Good Guys", "Bad Guys" };
        gm.StartGame( teams );
        application.SetGameManager( gm );
        startActivity(new Intent(Intent.ACTION_RUN, getIntent().getData()));
      }
  };
  
  /**
   * Listener to determine when the settings button is clicked.  Includes an onClick function
   * that starts the settingsActivity.
   */
  private OnClickListener SettingsListener = new OnClickListener()
  {
  	public void onClick(View v) 
  	{
  	  startActivity(new Intent(getApplication().getString( R.string.IntentSettings ), 
  	        getIntent().getData()));
  	}
  }; //End SettingsListener
  
  /**
   * Listener to determiner when the Rules button is clicked on the title screen.  Includes
   * an onClick method that will start the Rule activity.
   */
  private OnClickListener RulesListener = new OnClickListener() 
  {
    public void onClick(View v) 
    {
      startActivity(new Intent(getApplication().getString( R.string.IntentRules ), 
          getIntent().getData()));
      
    }
  };
	
/**
* onCreate - initializes a welcome screen that starts the game.
*/
@Override
public void onCreate( Bundle savedInstanceState )
{
	super.onCreate( savedInstanceState );
	// Setup the view
	this.setContentView(R.layout.title );

  ImageButton playGameButton = 
    (ImageButton) this.findViewById( R.id.PlayGameButton );
  playGameButton.setOnClickListener( PlayGameListener );
  
  ImageButton settingsButton = (ImageButton) this.findViewById( R.id.SettingsIcon );  
  settingsButton.setOnClickListener( SettingsListener );
  
  ImageButton rulesButton = (ImageButton) this.findViewById( R.id.GameRulesIcon );
  rulesButton.setOnClickListener( RulesListener );
}

}
