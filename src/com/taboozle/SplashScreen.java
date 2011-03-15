package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

/**
 * This is the splash screen for the app's intro.  It should be started before the title
 * and should actually start the music.
 * 
 * @author Taboozle Team
 *
 */
public class SplashScreen extends Activity
{
  protected int SPLASHTIME = 3000; //length of time splash screen will show.
  protected Handler exitHandler = null;
  protected Runnable exitRunnable = null;  
  
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splashscreen);
    
    // Runnable exiting the splash screen and launching the menu
    exitRunnable = new Runnable() 
    {
      public void run()
      {
        exitSplash();
      }
    };
    
    // Run the exitRunnable in in _splashTime ms
    exitHandler = new Handler();
    exitHandler.postDelayed(exitRunnable, SPLASHTIME);
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    if (event.getAction() == MotionEvent.ACTION_DOWN)
    {
      // Remove the exitRunnable callback from the handler queue
      exitHandler.removeCallbacks(exitRunnable);
      
      // Run the exit code manually
      exitSplash();
    }
    return true;
  }
  
  private void exitSplash()
  {
    finish();
    startActivity(new Intent("com.taboozle.intent.action.TITLE"));
  }
}
