package com.wordfrenzy;

import com.wordfrenzy.R;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * @author The WordFrenzy Team
 *
 * The Settings class handles the first screen of the Settings page.
 */
public class Settings extends PreferenceActivity 
{
  /**
   * logging tag
   */
  public static String TAG = "Settings";
  
	/**
	* onCreate - initializes a settings screen
	*/
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
    Log.d( TAG, "onCreate()" ); 
		
		addPreferencesFromResource(R.xml.settings);
		
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
