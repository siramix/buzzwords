package com.taboozle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

/**
 * @author The Taboozle Team
 * This activity class is responsible for displaying the rules of taboozle to the user.
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
}
