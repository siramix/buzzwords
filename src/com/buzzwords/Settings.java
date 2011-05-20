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
import android.view.KeyEvent;

/**
 * @author The BuzzWords Team
 *
 * The Settings class handles the first screen of the Settings page.
 */
public class Settings extends PreferenceActivity 
{
  
  private boolean continueMusic = false;
  
  /**
   * Watch the settings to update any changes (like start up music, reset subtext, etc.)
   */
  private OnSharedPreferenceChangeListener PrefListener = new OnSharedPreferenceChangeListener()
  {

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) 
    {
      Log.d( TAG, "onSharedPreferencesChanged()" );
      if ( key.equals( "music_enabled"))
      {
        // start or stop the music
        BuzzWordsApplication application = (BuzzWordsApplication) Settings.this.getApplication();
        MediaPlayer mp = application.GetMusicPlayer();
        if( sharedPreferences.getBoolean("music_enabled", true))
        {
          Log.d( TAG, "Music ON" );
          if( !mp.isPlaying())
          {
            Log.d( TAG, "it's Not Playing()  -- PLAY" );
            mp.start();
          }
        }
        else
        {
          Log.d( TAG, "Music OFF" );
          if( mp.isPlaying())
          {
            mp.pause();
          }
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
    
    continueMusic = false;

    //Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    
		this.addPreferencesFromResource(R.xml.settings);
	  
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
   * Override back button to carry music on back to the Title activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event)
  {
    if( keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled() )
      {
        Log.d( TAG, "BackKeyUp()" );
        // Flag to keep music playing
        Settings.this.continueMusic = true;
      }
    
    return super.onKeyUp(keyCode, event);
  }
  	
	
	/**
	 * Override onPause to prevent activity specific processes from running while app is in background
	 */
	@Override
	public void onPause()
	{
	   Log.d( TAG, "onPause()" );   
	   super.onPause();
	   
	   //Unregister settings listeners
	   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
	   sp.unregisterOnSharedPreferenceChangeListener( PrefListener );	   
	   
	   // Pause music
	   BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
	   MediaPlayer mp = application.GetMusicPlayer();
	   if ( !Settings.this.continueMusic && mp.isPlaying())
	   {
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
	   BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
	   MediaPlayer mp = application.GetMusicPlayer();
	   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
	   if( !mp.isPlaying() && sp.getBoolean("music_enabled", true))
	   {
	       mp.start();   
	   }
	   
	    // Register preference listener with SharedPreferences
	    sp.registerOnSharedPreferenceChangeListener(Settings.this.PrefListener);
	   
	   continueMusic = false;
	}
}
