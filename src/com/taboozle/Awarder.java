/**
 * 
 */
package com.taboozle;

import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;


/**
 * @author Taboozle Team
 *
 */
public class Awarder
{
  
  private static final String TAG = "Awarder";
  
  protected static final int NUM_AWARDS = 2;
  protected GameManager gameManager;
  
  /**
   * 
   */
  public Awarder()
  {
    Log.d( TAG, "Awarder()" );
  }
  
  public void setGameManager( GameManager gameManager )
  {
    Log.d( TAG, "setGameManager()" );
    this.gameManager = gameManager;
  }
  
  public GameManager getGameManager()
  {
    Log.d( TAG, "getGameManager()" );
    return this.gameManager;
  }

  public ArrayList<Award> getAwards( int teamid, int gameid )
  {
    Log.d( TAG, "getAwards()" );
    ArrayList<Award> ret = this.calcAwards( gameid );
    return ret;
  }
  
  protected ArrayList<Award> calcAwards( int gameid )
  {
    Log.d( TAG, "calcAwards()" );
    ArrayList<Award> ret = new ArrayList<Award>(NUM_AWARDS);
    ArrayList<Award> possibleAwards = Award.awards;
    for( Iterator<Award> itr = possibleAwards.iterator(); itr.hasNext(); )
    {
      Award cur = itr.next();
      double[][] res = this.gameManager.awardsQuery( cur.id, gameid );
      Log.d( TAG, Double.toString( res[0][0] ) + " " + Double.toString( res[0][1] ) );
      Log.d( TAG, Double.toString( res[1][0] ) + " " + Double.toString( res[1][1] ) );
      Log.d( TAG, Double.toString( res[2][0] ) + " " + Double.toString( res[2][1] ) );
      Log.d( TAG, Double.toString( res[3][0] ) + " " + Double.toString( res[3][1] ) );
    }
    
    return ret;
  }

}
