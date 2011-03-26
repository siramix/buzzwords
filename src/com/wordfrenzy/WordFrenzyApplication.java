package com.wordfrenzy;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * @author The WordFrenzy Team
 * Class extending the standard android application. This allows us to refer
 * to one GameManager from every activity within WordFrenzy.
 */
public class WordFrenzyApplication extends Application
{
  /**
   * logging tag
   */
  public static String TAG = "WordFrenzyApplication";
  
  /**
   * The GameManager for all of WordFrenzy
   */
  private GameManager gameManager;

  /**
   * The SoundFXManager for all of WordFrenzy
   */
  private SoundManager soundManager;
  
  /**
   * MediaPlayer for music
   */
  private MediaPlayer mp;
  
  /**
   * Default constructor
   */
  public WordFrenzyApplication()
  {
    super();
    Log.d( TAG, "WordFrenzyApplication()" ); 
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
