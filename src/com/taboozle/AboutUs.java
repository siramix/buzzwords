package com.taboozle;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

/**
 * @author The Taboozle Team
 * This activity class is responsible for displaying the company information to the user.
 */
public class AboutUs extends Activity
{
  /**
   * logging tag
   */
  public static String TAG = "AboutUs";
  
  /**
  * onCreate - initializes the activity to display the About Us page.
  */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.d( TAG, "onCreate()" ); 
    
    this.setContentView(R.layout.aboutus);
  }
   
  /**
   * Override onPause to prevent activity specific processes from running while app is in background
   */
  @Override
  public void onPause()
  {
     Log.d( TAG, "onPause()" );   
     super.onPause();
     TaboozleApplication application = (TaboozleApplication) this.getApplication();
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
     TaboozleApplication application = (TaboozleApplication) this.getApplication();
     MediaPlayer mp = application.GetMusicPlayer();
     if( !mp.isPlaying())
     {
         mp.start();   
     }
  }
}
