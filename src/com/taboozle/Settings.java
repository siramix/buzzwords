package com.taboozle;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * @author The Taboozle Team
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
}
