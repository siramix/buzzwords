package com.taboozle;

import android.app.Application;

/**
 * @author The Taboozle Team
 * Class extending the standard android application. This allows us to refer
 * to one GameManager from every activity within taboozle.
 */
public class TaboozleApplication extends Application
{

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
  }
  
  /**
   * @return a reference to the game manager
   */
  public GameManager GetGameManager()
  {
    return this.gameManager;
  }
  
  /**
   * @param gm - a reference to the game manager
   */
  public void SetGameManager( GameManager gm )
  {
    this.gameManager = gm;
  }

}
