package com.taboozle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author The Taboozle Team
 *
 * The Settings class handles the first screen of the Settings page.
 */
public class Settings extends Activity 
{


	/**
	* onCreate - initializes a settings screen
	*/
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		// Setup the view
		this.setContentView(R.layout.settings);
		
		TextView settingsTitle = (TextView) findViewById(R.id.SettingsScreenTitle);
		settingsTitle.setText("SETTINGS YO!");
	}

	
}
