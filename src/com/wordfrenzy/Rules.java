package com.wordfrenzy;

import com.wordfrenzy.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

/**
 * @author The WordFrenzy Team
 * This activity class is responsible for displaying the rules of wordfrenzy to the user.
 */
public class Rules extends Activity
{
  /**
   * logging tag
   */
  public static String TAG = "Rules";
  
  /**
  * onCreate - initializes the activity to display the rules.
  */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.d( TAG, "onCreate()" ); 
    
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
    TextView rulePrefs = (TextView) this.findViewById(R.id.RulesPreferences);
    rulePrefs.setText(prefBuilder);
  }
   
  /**
   * Override onPause to prevent activity specific processes from running while app is in background
   */
  @Override
  public void onPause()
  {
     Log.d( TAG, "onPause()" );   
     super.onPause();
     WordFrenzyApplication application = (WordFrenzyApplication) this.getApplication();
     MediaPlayer mp = application.GetMusicPlayer();
     mp.pause();
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
     if( !mp.isPlaying())
     {
         mp.start();   
     }
  }
}
