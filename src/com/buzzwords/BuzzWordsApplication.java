package com.buzzwords;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * @author The BuzzWords Team
 * Class extending the standard android application. This allows us to refer
 * to one GameManager from every activity within BuzzWords.
 */
public class BuzzWordsApplication extends Application
{
  /**
   * logging tag
   */
  public static String TAG = "BuzzWordsApplication";
  
  /**
   * The GameManager for all of BuzzWords
   */
  private GameManager gameManager;

  /**
   * The SoundFXManager for all of BuzzWords
   */
  private SoundManager soundManager;
  
  /**
   * MediaPlayer for music
   */
  private MediaPlayer mp;
  
  /**
   * Default constructor
   */
  public BuzzWordsApplication()
  {
    super();
    Log.d( TAG, "BuzzWordsApplication()" ); 
  }

  /**
   * @return a reference to the game manager
   */
  public GameManager GetGameManager()
  {
    Log.d( TAG, "GetGameManager()" ); 
    return this.gameManager;
  }
  
  /**
   * @param gm - a reference to the game manager
   */
  public void SetGameManager( GameManager gm )
  {
    Log.d( TAG, "SetGameManager()" );     
    this.gameManager = gm;
  }
  
  /**
   * @return a reference to the sound manager
   */
  public SoundManager GetSoundManager()
  {
    Log.d( TAG, "GetSoundManager()" ); 
    return this.soundManager;
  }

  
  public SoundManager CreateSoundManager( Context context)
  {
    Log.d( TAG, "CreateSound Manager(" + context );
      soundManager = new SoundManager( context ); 
      return soundManager;
  }

  
  public MediaPlayer CreateMusicPlayer( Context context, int id)
  {
    Log.d( TAG, "CreateMusicPlayer(" + context + "," + id + ")" );
	  mp = MediaPlayer.create(context, id);
	  return mp;
  }
  
  public MediaPlayer GetMusicPlayer()
  {
    Log.d( TAG, "GetMusicPlayer()" );
	  return mp;
  }

}
