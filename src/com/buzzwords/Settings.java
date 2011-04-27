package com.buzzwords;

import com.buzzwords.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author The BuzzWords Team
 *
 * The Settings class handles the first screen of the Settings page.
 */
public class Settings extends PreferenceActivity 
{
  
  /**
   * Watch the settings to update any changes (like start up music, reset subtext, etc.)
   */
  private OnSharedPreferenceChangeListener ListenPrefs = new OnSharedPreferenceChangeListener()
  {

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) 
    {
      if ( key.equals( "music_enabled"))
      {
        // start or stop the music
        BuzzWordsApplication application = (BuzzWordsApplication) Settings.this.getApplication();
        MediaPlayer mp = application.GetMusicPlayer();
        if( sharedPreferences.getBoolean("music_enabled", true))
        {
            mp.start();
        }
        else
        {
          mp.pause();
        }
      }
      
    }
    
  };
      
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

    //Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    
		this.addPreferencesFromResource(R.xml.settings);
		// Register preference listener with SharedPreferences
	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
	    sp.registerOnSharedPreferenceChangeListener(this.ListenPrefs);
	}

	/**
	 * Override onPause to prevent activity specific processes from running while app is in background
	 */
	@Override
	public void onPause()
	{
	   Log.d( TAG, "onPause()" );   
	   super.onPause();
	   BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
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
	   BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
	   MediaPlayer mp = application.GetMusicPlayer();
	   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
	   if( !mp.isPlaying() && sp.getBoolean("music_enabled", true))
	   {
	       mp.start();   
	   }
	}
}
