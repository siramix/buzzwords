/*****************************************************************************
 *  Buzzwords is a family friendly word game for mobile phones.
 *  Copyright (C) 2011 Siramix Team
 *  
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
package com.buzzwordslite;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

/**
 * The Buzzer class is the activity for a simple image button that acts as a
 * portable buzzer. It plays a sound when held, and stops on release.
 * 
 * @author Siramix Labs
 */
public class Buzzer extends Activity {
  /**
   * logging tag
   */
  public static String TAG = "Buzzer";

  /**
   * OnTouch listener handles starting and stopping the buzzer sound
   */
  private OnTouchListener mBuzzTouch = new OnTouchListener() {

    /**
     * buzz stream id for stopping the buzz
     */
    private int buzzStreamId;

    public boolean onTouch(View yourButton, MotionEvent motion) {

      SoundManager mSoundManager = SoundManager.getInstance(Buzzer.this
          .getBaseContext());
      ImageButton buzzButton = (ImageButton) Buzzer.this
          .findViewById(R.id.Buzzer_Button);

      switch (motion.getAction()) {
      case MotionEvent.ACTION_DOWN:

        buzzStreamId = mSoundManager.playLoop(SoundManager.Sound.BUZZ);
        // Spoof an onclick state
        buzzButton.setBackgroundResource(R.drawable.buzzer_button_onclick);

        break;
      case MotionEvent.ACTION_UP:
        mSoundManager.stopSound(buzzStreamId);

        // Return from spoofed onclick state
        buzzButton.setBackgroundResource(R.drawable.buzzer_button);
        break;
      }
      return true;
    }
  };

  /**
   * onCreate - initializes a buzzer screen
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }
    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    this.setContentView(R.layout.buzzer);

    ImageButton buzzButton = (ImageButton) this
        .findViewById(R.id.Buzzer_Button);
    buzzButton.setOnTouchListener(mBuzzTouch);
  }

}
