package com.buzzwords;

import com.buzzwords.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
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
  private OnSharedPreferenceChangeListener PrefListener = new OnSharedPreferenceChangeListener()
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
      
      else if ( key.equals( "turn_timer" ))
      {
        // when turn timer is changed, update the caption
        ListPreference lp = (ListPreference) findPreference( "turn_timer" );
        lp.setSummary( "Currently set to " + lp.getValue() + " seconds." ); 
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
	  sp.registerOnSharedPreferenceChangeListener(this.PrefListener);
	  
    // when turn timer is loaded, update the caption
    ListPreference lp = (ListPreference) findPreference( "turn_timer" );
    lp.setSummary( "Currently set to " + lp.getValue() + " seconds." );	  
    
    // update the version preference caption to the existing app version
    Preference version = findPreference( "app_version" );
    try {
      version.setTitle( this.getString( R.string.AppName ) );
      version.setSummary(" Version " + this.getPackageManager().getPackageInfo( this.getPackageName(), 0).versionName );
    } 
    catch (NameNotFoundException e) {
      e.printStackTrace();
      Log.e ( TAG, e.getMessage() );
    }
    
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
