/**
 * 
 */
package com.taboozle;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Taboozle Team
 *
 */
public class Awarder
{
  
  protected static final int NUM_AWARDS = 2;
  protected Game game;
  
  /**
   * 
   */
  public Awarder()
  {
  }
  
  public void setGame( Game game )
  {
    this.game = game;
  }
  
  public Game getGame()
  {
    return this.game;
  }

  public ArrayList<Award> getAwards( int teamid, int gameid )
  {
    ArrayList<Award> ret = this.calcAwards();
    ArrayList<Award> possibleAwards = Award.awards;
    for( Iterator<Award> itr = possibleAwards.iterator(); itr.hasNext(); )
    {
      Award cur = itr.next();
      double[][] res = this.game.awardsQuery( cur.id, gameid );
    }
    
    return ret;
  }
  
  protected ArrayList<Award> calcAwards()
  {
    ArrayList<Award> ret = new ArrayList<Award>(NUM_AWARDS);
    return ret;
  }

}
