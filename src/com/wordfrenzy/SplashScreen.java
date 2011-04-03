package com.wordfrenzy;

import com.wordfrenzy.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

/**
 * This is the splash screen for the app's intro.  It should be started before the title
 * and should actually start the music.
 * 
 * @author WordFrenzy Team
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
   * Listener that ends splash screen when it is done animating.
   */
  private final AnimationListener fadeListener = new AnimationListener()
  {
    public void onAnimationEnd( Animation animation )
    { 

      Log.d( TAG, "onAnimEnd()" );
      SplashScreen.this.exitSplash();
    }

    public void onAnimationRepeat( Animation animation )
    { 
    }

    public void onAnimationStart( Animation animation )
    {
    }
  };
  
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
      // Clearing the animations causes animEnd event to fire
      ImageView logotext = 
        (ImageView) this.findViewById( R.id.logo_text );

      ImageView logoram = 
        (ImageView) this.findViewById( R.id.logo_ram );

      logoram.clearAnimation();
      logotext.clearAnimation();
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
    startActivity(new Intent("com.wordfrenzy.intent.action.TITLE"));
  }
  
  /**
   * Retrieves the logo images and fades them in using AlphaAnimation.
   */
  private void fadeIn()
  {
    Log.d( TAG, "fadeIn()" );
    
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
    textset.setAnimationListener( fadeListener ); //listener called to move to next activity
    
    ImageView logotext = 
      (ImageView) this.findViewById( R.id.logo_text );    
    
    ImageView logoram = 
      (ImageView) this.findViewById( R.id.logo_ram );
    
    logoram.startAnimation( ramset);
    logotext.startAnimation( textset );
    
  }
}
