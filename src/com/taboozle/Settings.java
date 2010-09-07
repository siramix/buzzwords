package com.taboozle;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author The Taboozle Team
 *
 * The Settings class handles the first screen of the Settings page.
 */
public class Settings extends PreferenceActivity 
{

	/**
	* onCreate - initializes a settings screen
	*/
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		addPreferencesFromResource(R.xml.settings);
		
	}
}
