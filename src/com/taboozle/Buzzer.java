package com.taboozle;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

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
   * buzz resource id
   */
  private int buzzSoundId;

  
  /**
   * class sound pool
   */
  private SoundPool soundPool;

  
  /**
   * OnTouch listener handles starting and stopping the buzzer sound
   */
  private OnTouchListener BuzzTouch = new OnTouchListener()
  {
    
    /**
     * buzz stream id for stopping the buzz
     */
    private int buzzStreamId;
    
    public boolean onTouch( View yourButton , MotionEvent motion ) 
    {
      AudioManager mgr =
        (AudioManager) Buzzer.this.getBaseContext().getSystemService( Context.AUDIO_SERVICE );
      float streamVolumeCurrent = mgr.getStreamVolume( AudioManager.STREAM_MUSIC );
      float streamVolumeMax = mgr.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
      float volume = streamVolumeCurrent / streamVolumeMax;
        
      switch ( motion.getAction() ) 
      {
        case MotionEvent.ACTION_DOWN: 
          buzzStreamId = Buzzer.this.soundPool.play( 
                                      Buzzer.this.buzzSoundId, volume, volume, 1, -1, 1.0f );
          break;
        case MotionEvent.ACTION_UP: 
          Buzzer.this.soundPool.stop( buzzStreamId );
          break;
      }
      return true;
    }
  };
  
	/**
	* onCreate - initializes a buzzer screen
	*/
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
	  super.onCreate( savedInstanceState );
      Log.d( TAG, "onCreate()" );

      //Only play sound once card has been processed so we don't confuse the user
      soundPool = new SoundPool( 4, AudioManager.STREAM_MUSIC, 100 );
      buzzSoundId = soundPool.load(this, R.raw.wrong, 1);
      
      this.setContentView(R.layout.buzzer);
      
      ImageButton buzzButton = (ImageButton) this.findViewById(R.id.BuzzerButton);
      buzzButton.setOnTouchListener( this.BuzzTouch );
	}

}
