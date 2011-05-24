package com.buzzwords;

import com.buzzwords.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

/**
 * @author Siramix Labs
 * This activity class is responsible for displaying the rules of buzzwords to the user.
 */
public class Rules extends Activity
{
  /**
   * logging tag
   */
  public static String TAG = "Rules";
  
  private boolean isMusicPaused = false;
  private boolean continueMusic = false;
  
  /**
  * onCreate - initializes the activity to display the rules.
  */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
if( BuzzWordsApplication.DEBUG) { Log.d( TAG, "onCreate()" );  }
    
    continueMusic = false;
    
    //Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    
    this.setContentView(R.layout.rules);
    
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
    
    //String used for displaying the customizable preferences to the user
    StringBuilder prefBuilder = new StringBuilder();
    prefBuilder.append("(These rules can be changed any time from the Settings screen.)");
    
    //Turn Length rule display
    prefBuilder.append("\n\nTurn Length: " + sp.getString("turn_timer", "60") + " seconds");
    
    //Allow Skipping rule display
    if (sp.getBoolean("allow_skip",true))
    {
      prefBuilder.append("\nAllow Skipping: Players may skip words.");
    }
    else 
    {
      prefBuilder.append("\nAllow Skipping: Players can not skip words.");
    }
    TextView rulePrefs = (TextView) this.findViewById(R.id.Rules_Preferences);
    rulePrefs.setText(prefBuilder);
  }
  
  /**
   * Override back button to carry music on back to the Title activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event)
  {
    if( keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled() )
      {
if( BuzzWordsApplication.DEBUG) { Log.d( TAG, "BackKeyUp()" );  }
        // Flag to keep music playing
        Rules.this.continueMusic = true;
      }
    
    return super.onKeyUp(keyCode, event);
  }
    
   
  /**
   * Override onPause to prevent activity specific processes from running while app is in background
   */
  @Override
  public void onPause()
  {
if( BuzzWordsApplication.DEBUG) { Log.d( TAG, "onPause()" );  }
     super.onPause();
     BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
     MediaPlayer mp = application.getMusicPlayer();
     // If music is playing, we must pause it and flag to resume it onResume().
     // This solves the problem where Rules was never playing music to begin with (which happens
     // when entering Rules from Turn or TurnSummary
     if( !continueMusic && mp.isPlaying())
     {
       mp.pause();
       this.isMusicPaused = true;
     }
  }

  /**
   * Override OnResume to resume activity specific processes
   */
  @Override
  public void onResume()
  {
if( BuzzWordsApplication.DEBUG) { Log.d( TAG, "onResume()" );  }
     super.onResume();
     
     // Resume Title Music -- Only do this if we paused DURING rules
     if( this.isMusicPaused)
     {
       BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
       MediaPlayer mp = application.getMusicPlayer();
       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
       if( !mp.isPlaying() && sp.getBoolean("music_enabled", true))
       {
           mp.start();   
       }
     }
     
     continueMusic = false;
  }
}
