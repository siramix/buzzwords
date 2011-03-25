package com.taboozle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

/**
 * This is the splash screen for the app's intro.  It should be started before the title
 * and should actually start the music.
 * 
 * @author Taboozle Team
 *
 */
public class SplashScreen extends Activity
{
  /**
   * logging tag
   */
  public static String TAG = "SplashScreen";
  
  protected int SPLASHTIME = 3000; //length of time splash screen will show.
  protected Handler exitHandler = null; //will be used to delay the run of splash's exit
  protected Runnable exitRunnable = null; //will be called when splash is ready to close
  
  /**
   * called on creation of splash screen activity
   */
  public void onCreate(Bundle savedInstanceState)
  {
    Log.d( TAG, "onCreate()" );
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splashscreen);
    
    //Fade in the logo
    this.fadeIn();
        
    // Runnable exiting the splash screen and launching the menu
    exitRunnable = new Runnable() 
    {
      public void run()
      {
        exitSplash();
      }
    };
    
    // Run the exitRunnable in in SPLASHTIME ms
    exitHandler = new Handler();
    exitHandler.postDelayed(exitRunnable, SPLASHTIME);
  }
  
  /**
   * onTouchEvent to handle interrupts to splash screen
   */
  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    Log.d( TAG, "onTouchEvent()" );
    
    if (event.getAction() == MotionEvent.ACTION_DOWN)
    {
      // Remove the exitRunnable callback from the handler queue
      exitHandler.removeCallbacks(exitRunnable);
      
      // Run the exit code manually
      exitSplash();
    }
    
    return true;
  }
  
  /**
   * called when exithandler is reached or a touch event occurs
   * hides the images and then calls the title activity
   */
  private void exitSplash()
  {
    Log.d( TAG, "exitSplash()" );
    
    ImageView logotext = 
      (ImageView) this.findViewById( R.id.logo_text );

    ImageView logoram = 
      (ImageView) this.findViewById( R.id.logo_ram );
    
    logotext.setVisibility(View.GONE);
    logoram.setVisibility(View.GONE);
    
    finish();
    startActivity(new Intent("com.taboozle.intent.action.TITLE"));
  }
  
  /**
   * Retrieves the logo images and fades them in using AlphaAnimation.
   */
  private void fadeIn()
  {
    AlphaAnimation textfadein = new AlphaAnimation( 0, 1 );
    AlphaAnimation ramfadein = new AlphaAnimation( 0, 1 ); 

    textfadein.setDuration(1000);
    textfadein.setStartOffset(500);
    
    ramfadein.setDuration(1000);

    AlphaAnimation fadeout = new AlphaAnimation( 1, 0 );
    fadeout.setDuration(500);
    fadeout.setStartOffset(2500);
    
    AnimationSet ramset = new AnimationSet(true);
    ramset.addAnimation( ramfadein );
    ramset.addAnimation( fadeout );
    
    //staggered animation requires different animation set
    AnimationSet textset = new AnimationSet(true);
    textset.addAnimation( textfadein );
    textset.addAnimation( fadeout );
    
    ImageView logotext = 
      (ImageView) this.findViewById( R.id.logo_text );    
    
    ImageView logoram = 
      (ImageView) this.findViewById( R.id.logo_ram );
    
    logoram.startAnimation( ramset);
    logotext.startAnimation( textset );
  }
  
}