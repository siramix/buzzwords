package com.taboozle;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * @author The Taboozle Team
 *
 * The Buzzer class is the activity for a simple image button that acts as a portable buzzer.
 * It plays a sound when held, and stops on release.
 */
public class Buzzer extends Activity
{
  /**
   * logging tag
   */
  public static String TAG = "Buzzer";
  
	/**
	* onCreate - initializes a buzzer screen
	*/
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
    Log.d( TAG, "onCreate()" );
 
    this.setContentView(R.layout.buzzer);
	}
}
