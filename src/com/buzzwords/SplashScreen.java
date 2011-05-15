/*****************************************************************************
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.buzzwords;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

/**
 * This is the splash screen for the app's introduction. It should be started
 * before the title and be interruptible before the title activity.
 * 
 * @author BuzzWords Team
 */
public class SplashScreen extends Activity {
  /**
   * Logging tag
   */
  public static String TAG = "SplashScreen";

  // Length of time splash screen will show.
  protected int SPLASHTIME = 3000;
  
  // Flag prevents double calls to open the next activity
  private boolean SplashDone = false;

  /**
   * Called on creation of splash screen activity
   */
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate()");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.splashscreen);
    
    // Create a Deck so we load the cards into the SQL DB on first run
    // this operation will occur in a threaded manner (hot, I know).
    Deck tempDeck = new Deck(this);
    Log.d(TAG,tempDeck.toString());

    // Fade in the logo
    this.fadeIn();
  }

  /**
   * Handle interrupts to splash screen
   */
  public boolean onTouchEvent(MotionEvent event) {
    Log.d(TAG, "onTouchEvent()");

    if (event.getAction() == MotionEvent.ACTION_DOWN) 
    {
      Log.d(TAG, "onTouchEvent->ActionDown()");
      // Clearing the animations causes animEnd event to fire
      ImageView logotext = (ImageView) this.findViewById(R.id.Splash_LogoText);
      ImageView logoram = (ImageView) this.findViewById(R.id.Splash_LogoRam);

      logoram.clearAnimation();
      logotext.clearAnimation();
      
      // Workaround for HTC - Hero firmware version 2.1
      //   For some reason, clearing the animation does not call EndAnimation listener on this
      //   platform.  We'd like to rely on that event to call exitSplash().  
      SplashScreen.this.exitSplash();
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Listener that ends splash screen when it is done animating.
   */
  private final AnimationListener fadeListener = new AnimationListener() {
    public void onAnimationEnd(Animation animation) {
      Log.d(TAG, "onAnimEnd()");
      SplashScreen.this.exitSplash();
    }

    /**
     * Implementation required by AnimationListener.
     */
    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
    }
  };
  
  /**
   * Called when exitHandler is reached or a touch event occurs. This hides
   * the images and then calls the title activity.
   */
  synchronized private void exitSplash() {
    Log.d(TAG, "exitSplash()");
    
    // Do nothing if the splash has already been cancelled once
    if(this.SplashDone)
    {
      return;
    }
    // Setting boolean prevents the endAnimation from calling exitSplash a second time.
    this.SplashDone = true;

    ImageView logotext = (ImageView) this.findViewById(R.id.Splash_LogoText);
    ImageView logoram = (ImageView) this.findViewById(R.id.Splash_LogoRam);

    logotext.setVisibility(View.GONE);
    logoram.setVisibility(View.GONE);

    finish();
    startActivity(new Intent(getString(R.string.IntentTitle)));
  }

  /**
   * Retrieves the logo images and fades them in using AlphaAnimation.
   */
  private void fadeIn() {
    Log.d(TAG, "fadeIn()");
    
    // Build animation for text and logo images
    AlphaAnimation textfadein = new AlphaAnimation(0, 1);
    AlphaAnimation ramfadein = new AlphaAnimation(0, 1);

    textfadein.setDuration(1000);
    textfadein.setStartOffset(500);

    ramfadein.setDuration(1000);

    AlphaAnimation fadeout = new AlphaAnimation(1, 0);
    fadeout.setDuration(500);
    fadeout.setStartOffset(2500);

    AnimationSet ramset = new AnimationSet(true);
    ramset.addAnimation(ramfadein);
    ramset.addAnimation(fadeout);

    // Staggered animation requires different animation set
    AnimationSet textset = new AnimationSet(true);
    textset.addAnimation(textfadein);
    textset.addAnimation(fadeout);
    
    // Listener called to move to next activity
    textset.setAnimationListener(fadeListener);
   
    ImageView logotext = (ImageView) this.findViewById(R.id.Splash_LogoText);
    ImageView logoram = (ImageView) this.findViewById(R.id.Splash_LogoRam);
    
    logotext.startAnimation(textset);
    logoram.startAnimation(ramset);
  }
}
