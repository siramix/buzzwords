package com.taboozle;

import android.app.Activity;
import android.os.Bundle;

/**
 * The GameEnd class is the final screen of the application, called
 * when either the number of turns is up, the time is up, the end game
 * button is clicked, or any other number of ways to end a game.
 * 
 * @author Taboozle team
 *
 */
public class GameEnd extends Activity
{
      
    public void onCreate( Bundle savedInstanceState ) 
    {
      super.onCreate(savedInstanceState);
      this.setContentView( R.layout.gameend );
    }
}
