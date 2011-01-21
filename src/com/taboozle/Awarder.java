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

  /*public String[] getAwards( int gameid )
  {
    Log.d( TAG, "getAwards()" );
    ArrayList<Award> ret = this.calcAwards( gameid );
    return ret;
  }*/
  
  public ArrayList<Award> calcAwards( long gameid )
  {
    Log.d( TAG, "calcAwards()" );
    ArrayList<Award> possibleAwards = Award.awards;
    long[] teamIds = this.gameManager.GetTeamIDs();
    ArrayList<Award> ret = new ArrayList<Award>(teamIds.length);
    boolean[] awarded = new boolean[teamIds.length];
    for( int i = 0; i < teamIds.length; ++i )
    {
      awarded[i] = false;
      ret.add( i, null );
    }
    for( Iterator<Award> itr = possibleAwards.iterator(); itr.hasNext(); )
    {
      Award cur = itr.next();
      double[][] res = this.gameManager.awardsQuery( cur.id, gameid );
      Log.d( TAG, Double.toString( res[0][0] ) + " " + Double.toString( res[0][1] ) );
      Log.d( TAG, Double.toString( res[1][0] ) + " " + Double.toString( res[1][1] ) );
      Log.d( TAG, Double.toString( res[2][0] ) + " " + Double.toString( res[2][1] ) );
      Log.d( TAG, Double.toString( res[3][0] ) + " " + Double.toString( res[3][1] ) );
      int processed = 0;
      for( int i = 0; i < teamIds.length; ++i )
      {
        if( !awarded[i] && res[0][0] == (double)teamIds[i] )
        {
          awarded[i] = true;
          Log.d( TAG, Integer.toString( i ) );
          ret.set( i, cur );
          processed++;
        }
      }
      if( processed == teamIds.length )
      {
        break;
      }
    }

    for( int i = 0; i < ret.size(); ++i )
    {
      if( ret.get( i ) == null )
      {
        ret.set( i, new Award( -1, "Awardless", "", -1 ) );
      }
    }
    
    return ret;
  }
  
  public ArrayList<Award> calcAwards()
  {
    return this.calcAwards( this.gameManager.GetGameId() );
  }

}
