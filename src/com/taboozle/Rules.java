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
    
    prefBuilder.append("Turn Length: " + sp.getString("turn_timer", "60") + " seconds");
    prefBuilder.append("\nSkip Penalty: " + sp.getBoolean("skip_penalty",false));
    
    TextView rulePrefs = (TextView) this.findViewById(R.id.RulePreferences);
    rulePrefs.setText(prefBuilder);
  }
}
