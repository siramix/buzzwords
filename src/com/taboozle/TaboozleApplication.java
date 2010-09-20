package com.taboozle;

import android.app.Application;
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

}
