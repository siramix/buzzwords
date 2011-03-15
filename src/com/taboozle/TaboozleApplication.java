package com.taboozle;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * @author The Taboozle Team
 * Class extending the standard android application. This allows us to refer
 * to one GameManager from every activity within taboozle.
 */
public class TaboozleApplication extends Application
{
  /**
   * logging tag
   */
  public static String TAG = "TaboozleApplication";
  
  /**
   * The GameManager for all of taboozle
   */
  private GameManager gameManager;

  /**
   * MediaPlayer for music
   */
  private MediaPlayer mp;
  
  /**
   * Default constructor
   */
  public TaboozleApplication()
  {
    super();
    Log.d( TAG, "TaboozleApplication()" ); 
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
