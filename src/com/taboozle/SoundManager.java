package com.taboozle;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {

  private AudioManager mgr;
  private SoundPool pool;
  private float volume;

  public static final int SOUND_RIGHT = 0;
  public static final int SOUND_WRONG = 1;
  public static final int SOUND_SKIP = 2;
  public static final int SOUND_TEAMREADY = 3;
  public static final int SOUND_WIN = 4;
  
  enum Sounds
  {
    RIGHT,
    WRONG,
    SKIP,
    TEAMREADY
  }
  
  private int[] soundIds;

  
  public SoundManager(Context baseContext)
  {
    this.mgr = ( AudioManager) baseContext.getSystemService( Context.AUDIO_SERVICE );
    float streamVolumeCurrent = mgr.getStreamVolume( AudioManager.STREAM_MUSIC );
    float streamVolumeMax = mgr.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
    this.volume = streamVolumeCurrent / streamVolumeMax;
    
    this.pool = new SoundPool( 4, AudioManager.STREAM_MUSIC, 100 );
    
    // Load all sounds on creation of Sound Manager.  Could pull this into a separate function 
    // if we don't want these paired.
    soundIds = new int[ 5 ];
    soundIds[ SOUND_RIGHT ] = pool.load( baseContext, R.raw.fx_right, 1);
    soundIds[ SOUND_WRONG ] = pool.load( baseContext, R.raw.fx_wrong, 1);
    soundIds[ SOUND_SKIP ] = pool.load( baseContext, R.raw.fx_skip, 1);
    soundIds[ SOUND_TEAMREADY ] = pool.load( baseContext, R.raw.fx_teamready, 1);
    soundIds[ SOUND_WIN ] = pool.load( baseContext, R.raw.fx_win, 1);
    
  }
  
  public int PlaySound( int FXIndex )
  {
    return pool.play( soundIds[ FXIndex ], volume, volume, 1, 0, 1.0f );
  }
}
