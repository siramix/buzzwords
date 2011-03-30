package com.wordfrenzy;

import com.wordfrenzy.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * This is the activity class that kicks off WordFrenzy
 * 
 * @author The WordFrenzy Team
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
   * Listener to determine when the About Us button is clicked on the title screen.  Includes
   * an onClick method that will start the Rule activity.
   */
  private OnClickListener AboutUsListener = new OnClickListener() 
  {
    public void onClick(View v) 
    {
      Log.d( TAG, "AboutUsListener OnClick()" );
      musicHandled = false;
      
      Uri uri = Uri.parse("http://www.rockandrowe.com/");  
      Intent intent=new Intent(Intent.ACTION_VIEW,uri);
      startActivity(intent);

    }
  }; // End AboutUsListener
  
  /**
   * @return The animation that brings in the buttons
   * screen
   */
  private Animation TranslateButtons(int buttonNum)
  {
    Log.d( TAG, "TranslateButtons()" );
    
    final int MOVETIME = 600;
    
    // Slide in from off-screen
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
        Animation.ABSOLUTE,  ( -600f ), Animation.RELATIVE_TO_PARENT,   0.0f );
    slideIn.setDuration(MOVETIME + ( 200 * buttonNum) );
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

    final int MOVETIME = 800;
    AnimationSet set = new AnimationSet(true);

    // Define the translate animation
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  ( -1.0f * labelNum ), Animation.RELATIVE_TO_PARENT,  0.0f,
        Animation.RELATIVE_TO_PARENT,  ( 0.0f ), Animation.RELATIVE_TO_PARENT,   0.0f );
    slideIn.setDuration( MOVETIME );
    slideIn.setInterpolator(new DecelerateInterpolator());
    slideIn.setStartOffset( 300 * (labelNum+1) );
  
    // Create entire sequence
    set.addAnimation(slideIn);
    return set;
  }

  /**
   * @return The animation that wiggles the W in the title
   */
  private Animation WiggleW()
  {
    Log.d( TAG, "WiggleW()" );
    
    TranslateAnimation scroll = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,  -0.05f,
        Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,   -0.1f );
    scroll.setDuration(800);
    scroll.setInterpolator(new LinearInterpolator());
    scroll.setRepeatCount(Animation.INFINITE);
    scroll.setRepeatMode( Animation.REVERSE);
    
    ScaleAnimation pulse = new ScaleAnimation(1.0f, 1.05f, 1.0f, 1.05f, Animation.RELATIVE_TO_SELF,
        0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    pulse.setDuration(500);
    pulse.setInterpolator(new LinearInterpolator());
    pulse.setRepeatCount(Animation.INFINITE);
    pulse.setRepeatMode(Animation.REVERSE);
    
    return scroll;
  }

  /**
   * @return The animation that wiggles the O in the title
   */
  private Animation WiggleO()
  {
    Log.d( TAG, "WiggleO()" );
    
    TranslateAnimation scroll = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,  0.025f,
        Animation.RELATIVE_TO_SELF,  0.05f, Animation.RELATIVE_TO_SELF,   -0.05f );
    scroll.setDuration(500);
    scroll.setInterpolator(new LinearInterpolator());
    scroll.setRepeatCount(Animation.INFINITE);
    scroll.setRepeatMode( Animation.REVERSE);

    return scroll;
  }


  /**
   * @return The animation that wiggles the R in the title
   */
  private Animation WiggleR()
  {
    Log.d( TAG, "WiggleR()" );
    
    TranslateAnimation scroll = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,  0.05f,
        Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,   0.025f );
    scroll.setDuration(400);
    scroll.setInterpolator(new LinearInterpolator());
    scroll.setRepeatCount(Animation.INFINITE);
    scroll.setRepeatMode( Animation.REVERSE);

    return scroll;
  }

  /**
   * @return The animation that wiggles the D in the title
   */
  private Animation WiggleD()
  {
    Log.d( TAG, "WiggleD()" );
    
    TranslateAnimation scroll = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,  0.05f,
        Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,   -0.05f );
    scroll.setDuration(750);
    scroll.setInterpolator(new LinearInterpolator());
    scroll.setRepeatCount(Animation.INFINITE);
    scroll.setRepeatMode( Animation.REVERSE);

    return scroll;
  }
  
/**
* onCreate - initializes a welcome screen that starts the game.
*/
@Override
public void onCreate( Bundle savedInstanceState )
{
	super.onCreate( savedInstanceState );
  Log.d( TAG, "onCreate()" );               
  
  WordFrenzyApplication application = (WordFrenzyApplication) this.getApplication();
  MediaPlayer mp = application.CreateMusicPlayer(this.getBaseContext(), R.raw.mus_title);
  application.CreateSoundManager( this.getBaseContext() );
  mp.setLooping(true);
  SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
  if( sp.getBoolean("music_enabled", true))
  {
    mp.start();
  }
  
  musicHandled = false;
  
	// Setup the Main Title Screen view
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
  
  ImageButton aboutusButton = (ImageButton) this.findViewById( R.id.Title_AboutUs );
  aboutusButton.setOnClickListener( AboutUsListener );
  
  View button = (View) this.findViewById( R.id.Title_PlayButton);
  button.startAnimation(this.TranslateButtons(4));
  button = (View) this.findViewById( R.id.Title_BuzzButton);
  button.startAnimation(this.TranslateButtons(3));
  button = (View) this.findViewById( R.id.Title_SettingsButton);
  button.startAnimation(this.TranslateButtons(2));
  button = (View) this.findViewById( R.id.Title_RulesButton);
  button.startAnimation(this.TranslateButtons(1));
  
  // set font
  Typeface antonFont = Typeface.createFromAsset(getAssets(), "fonts/Anton.ttf");

  TextView label = (TextView) this.findViewById( R.id.Title_PlayText);
  label.startAnimation(this.TranslateLabels(4));
  label.setTypeface(antonFont);
  label = (TextView) this.findViewById( R.id.Title_BuzzText);
  label.startAnimation(this.TranslateLabels(3));
  label.setTypeface(antonFont);
  label = (TextView) this.findViewById( R.id.Title_SettingsText);
  label.startAnimation(this.TranslateLabels(2));
  label.setTypeface(antonFont);
  label = (TextView) this.findViewById( R.id.Title_RulesText);
  label.startAnimation(this.TranslateLabels(1));
  label.setTypeface(antonFont);
  
  ImageView letter = (ImageView) this.findViewById( R.id.Title_Word_W);
  letter.startAnimation(this.WiggleW());
  letter = (ImageView) this.findViewById( R.id.Title_Word_O);
  letter.startAnimation(this.WiggleO());
  letter = (ImageView) this.findViewById( R.id.Title_Word_R);
  letter.startAnimation(this.WiggleR());
  letter = (ImageView) this.findViewById( R.id.Title_Word_D);
  letter.startAnimation(this.WiggleD());
  
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
     WordFrenzyApplication application = (WordFrenzyApplication) this.getApplication();
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
   WordFrenzyApplication application = (WordFrenzyApplication) this.getApplication();
   MediaPlayer mp = application.GetMusicPlayer();
   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
   if( sp.getBoolean("music_enabled", true))
   {
     mp.start();
   }
   // set flag to let onStop handle music
   musicHandled = false;
}

}
