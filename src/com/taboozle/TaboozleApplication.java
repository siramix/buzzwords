/**
 * 
 */
package com.taboozle;

import android.app.Application;


/**
 * @author cpatrick
 *
 */
public class TaboozleApplication extends Application
{

  private GameManager gameManager;
  
  /**
   * 
   */
  public TaboozleApplication()
  {
    super();
  }
  
  public GameManager GetGameManager()
  {
    return this.gameManager;
  }
  
  public void SetGameManager( GameManager gm )
  {
    this.gameManager = gm;
  }

}
