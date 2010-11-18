/**
 * 
 */
package com.taboozle;

import java.util.ArrayList;


/**
 * @author Taboozle Team
 *
 */
public class Awarder
{

  protected ArrayList<String[]> awards;
  protected static final int NUM_AWARDS = 2;
  protected int teamId;
  protected int gameId;
  
  /**
   * 
   */
  public Awarder()
  {
    this.awards = new ArrayList<String[]>();
  }

  public ArrayList<String[]> getAwards( int teamid, int gameid )
  {
    this.teamId = teamid;
    this.gameId = gameid;
    
    this.calcAwards();
    
    ArrayList<String[]> ret = new ArrayList<String[]>(NUM_AWARDS);
    for( int i = 0; i < ret.size(); ++i )
    {
      ret.get( i )[0] = "";
      ret.get( i )[1] = "";
    }
    
    return ret;
  }
  
  protected void calcAwards()
  { 
  }

}
