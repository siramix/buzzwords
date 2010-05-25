/**
 * 
 */
package com.taboozle;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author taboozle team
 *
 */
public class Card
{
  public static ArrayList<String> BustString( String commaSeparated )
  {
    ArrayList<String> ret = new ArrayList<String>();
    StringTokenizer tok = new StringTokenizer( commaSeparated );

    while( tok.hasMoreTokens() )
    {
      ret.add( tok.nextToken( "," ).toUpperCase() );
    }
    
    return ret;
  }
  
  public long id;
  public String title;
  public ArrayList<String> badWords;
  
  public Card()
  {
    this.id = -1;
    this.title = "";
    this.badWords = new ArrayList<String>();
  }

}
