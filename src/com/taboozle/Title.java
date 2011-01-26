package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

/**
 * This is the activity class that kicks off Taboozle
 * 
 * @author The Taboozle Team
 */
public class Title extends Activity
{
  /**
   * logging tag
   */
  public static String TAG = "Title";
  
  /**
  * PlayGameListener is used for the start game button.  It launches the next 
  * activity.
  */
  private OnClickListener PlayGameListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
        Log.d( TAG, "PlayGameListener OnClick()" );         
        startActivity(new Intent(Title.this.getApplication().getString( R.string.IntentGameSetup),
        		                 getIntent().getData()));
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
      Log.d( TAG, "SettingsListener OnClick()" );           	  
  	  startActivity(new Intent(Title.this.getApplication().getString( R.string.IntentSettings ), 
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
      Log.d( TAG, "RulesListener OnClick()" );                     
      startActivity(new Intent(getApplication().getString( R.string.IntentRules ), 
          getIntent().getData()));
      
    }
  };

  /**
   * @return The animation that orients the button list 45
   * screen
   */
  private Animation TranslateButtons(int buttonNum)
  {
    
    Log.d( TAG, "RotateButtons()" );
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  (-1.0f * buttonNum), Animation.RELATIVE_TO_PARENT,  0.0f,
        Animation.RELATIVE_TO_PARENT,  (0.7f * buttonNum), Animation.RELATIVE_TO_PARENT,   0.0f );
    slideIn.setDuration(600 + ( 200 * buttonNum) );
    slideIn.setInterpolator(new LinearInterpolator());
    return slideIn;
  }

  /**
   * @return The animation that orients the button list 45
   * screen
   */
  private Animation TranslateLabels(int labelNum)
  {
    
    Log.d( TAG, "RotateButtons()" );
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  (1.0f * labelNum), Animation.RELATIVE_TO_PARENT,  0.0f,
        Animation.RELATIVE_TO_PARENT,  (0.7f * labelNum), Animation.RELATIVE_TO_PARENT,   0.0f );
    slideIn.setDuration(800 + ( 200 * ( labelNum + 1 ) ) );
    slideIn.setInterpolator(new LinearInterpolator());
    return slideIn;
  }
  
  
/**
* onCreate - initializes a welcome screen that starts the game.
*/
@Override
public void onCreate( Bundle savedInstanceState )
{
	super.onCreate( savedInstanceState );
  Log.d( TAG, "onCreate()" );                     	
	// Setup the view
	this.setContentView(R.layout.title );

  ImageButton playGameButton = 
    (ImageButton) this.findViewById( R.id.Title_PlayButton );
  playGameButton.setOnClickListener( PlayGameListener );
  
  ImageButton settingsButton = (ImageButton) this.findViewById( R.id.Title_SettingsButton );  
  settingsButton.setOnClickListener( SettingsListener );
  
  ImageButton rulesButton = (ImageButton) this.findViewById( R.id.Title_RulesButton );
  rulesButton.setOnClickListener( RulesListener );
  
  View button = (View) this.findViewById( R.id.Title_PlayButton);
  button.startAnimation(this.TranslateButtons(1));
  button = (View) this.findViewById( R.id.Title_BuzzButton);
  button.startAnimation(this.TranslateButtons(2));
  button = (View) this.findViewById( R.id.Title_SettingsButton);
  button.startAnimation(this.TranslateButtons(3));
  button = (View) this.findViewById( R.id.Title_RulesButton);
  button.startAnimation(this.TranslateButtons(4));
  
  View label = (View) this.findViewById( R.id.Title_PlayButton_Label);
  label.startAnimation(this.TranslateLabels(1));
  label = (View) this.findViewById( R.id.Title_BuzzButton_Label);
  label.startAnimation(this.TranslateLabels(2));
  label = (View) this.findViewById( R.id.Title_SettingsButton_Label);
  label.startAnimation(this.TranslateLabels(3));
  label = (View) this.findViewById( R.id.Title_RulesButton_Label);
  label.startAnimation(this.TranslateLabels(4));
}

}
