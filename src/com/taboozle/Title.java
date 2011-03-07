package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
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
   * flag used for stopping music OnStop() event.
   */
  private boolean musicHandled;
  
  /**
  * PlayGameListener is used for the start game button.  It launches the next 
  * activity.
  */
  private OnClickListener PlayGameListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
        Log.d( TAG, "PlayGameListener OnClick()" );
        musicHandled = true;
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
      musicHandled = true;
  	  startActivity(new Intent(Title.this.getApplication().getString( R.string.IntentSettings ), 
  	        getIntent().getData()));
  	}
  }; //End SettingsListener
  
  /**
   * Listener to determine when the Rules button is clicked on the title screen.  Includes
   * an onClick method that will start the Rule activity.
   */
  private OnClickListener RulesListener = new OnClickListener() 
  {
    public void onClick(View v) 
    {
      Log.d( TAG, "RulesListener OnClick()" );
      musicHandled = true;
      startActivity(new Intent(getApplication().getString( R.string.IntentRules ), 
          getIntent().getData()));
      
    }
  }; // End RulesListener

  /**
   * Listener to determine when the BuzzMode button is clicked
   */
  private OnClickListener BuzzerListener = new OnClickListener() 
  {
    public void onClick(View v) 
    {
      Log.d( TAG, "BuzzerListener OnClick()" );
      musicHandled = false;
      startActivity(new Intent(getApplication().getString( R.string.IntentBuzzer ), 
          getIntent().getData()));
    }
  }; // End BuzzerListener 
  
  /**
   * @return The animation that brings in the buttons
   * screen
   */
  private Animation TranslateButtons(int buttonNum)
  {
    Log.d( TAG, "TranslateButtons()" );
    
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  (-1.0f * buttonNum), Animation.RELATIVE_TO_PARENT,  0.0f,
        Animation.RELATIVE_TO_PARENT,  (0.7f * buttonNum), Animation.RELATIVE_TO_PARENT,   0.0f );
    slideIn.setDuration(600 + ( 200 * buttonNum) );
    slideIn.setInterpolator(new DecelerateInterpolator());
    return slideIn;
  }

  /**
   * @return The animation that brings in labels
   * screen
   */
  private AnimationSet TranslateLabels(int labelNum)
  {
    Log.d( TAG, "TranslateLabels()" );

    final int MOVETIME = 400;
    AnimationSet set = new AnimationSet(true);

    // Define the translate animation
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  ( 1.0f * labelNum ), Animation.RELATIVE_TO_PARENT,  0.0f,
        Animation.RELATIVE_TO_PARENT,  ( 0.7f * labelNum ), Animation.RELATIVE_TO_PARENT,   0.0f );
    slideIn.setDuration( MOVETIME );
    slideIn.setInterpolator(new DecelerateInterpolator());
    slideIn.setStartOffset( MOVETIME * labelNum );

    // Define Pulse anim
    ScaleAnimation pulse = new ScaleAnimation(1.0f, 1.05f, 1.0f, 1.05f, Animation.RELATIVE_TO_SELF,
        0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    pulse.setDuration(500);
    pulse.setInterpolator(new LinearInterpolator());
    pulse.setRepeatCount(Animation.INFINITE);
    pulse.setRepeatMode(Animation.REVERSE);
    
    // Create entire sequence
    set.addAnimation(slideIn);
//    set.addAnimation(pulse);
    return set;
  }

  /**
   * @return The animation that scrolls the title
   * screen
   */
  private Animation ScrollTitle()
  {
    Log.d( TAG, "ScrollTitle()" );
    
    TranslateAnimation scroll = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF,  -1.0f, Animation.RELATIVE_TO_SELF,  1.0f,
        Animation.RELATIVE_TO_SELF,  1.0f, Animation.RELATIVE_TO_SELF,   -1.0f );
    scroll.setDuration(5000);
    scroll.setInterpolator(new LinearInterpolator());
    scroll.setRepeatCount(Animation.INFINITE);
    
    ScaleAnimation pulse = new ScaleAnimation(1.0f, 1.05f, 1.0f, 1.05f, Animation.RELATIVE_TO_SELF,
        0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    pulse.setDuration(500);
    pulse.setInterpolator(new LinearInterpolator());
    pulse.setRepeatCount(Animation.INFINITE);
    pulse.setRepeatMode(Animation.REVERSE);
    
    return pulse;
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
  
  ImageButton buzzerButton = (ImageButton) this.findViewById( R.id.Title_BuzzButton );
  buzzerButton.setOnClickListener( BuzzerListener );
  
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
  
  View title = (View) this.findViewById( R.id.Title_Title);
  title.startAnimation(this.ScrollTitle());

  TaboozleApplication application = (TaboozleApplication) this.getApplication();
  MediaPlayer mp = application.CreateMusicPlayer(this.getBaseContext(), R.raw.mus_title);
  mp.setLooping(true);
  mp.start();
  
  musicHandled = false;
}

/**
 * Override onPause to prevent activity specific processes from running while app is in background
 */
@Override
public void onPause()
{
   Log.d( TAG, "onPause()" );   
   super.onPause();
   if( !musicHandled )
   {
     TaboozleApplication application = (TaboozleApplication) this.getApplication();
     MediaPlayer mp = application.GetMusicPlayer();
     mp.pause();
   }
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
   TaboozleApplication application = (TaboozleApplication) this.getApplication();
   MediaPlayer mp = application.GetMusicPlayer();
   mp.start();   
   // set flag to let onStop handle music
   musicHandled = false;
}
}
