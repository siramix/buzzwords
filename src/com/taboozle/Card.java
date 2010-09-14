package com.taboozle;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author The Taboozle Team
 * 
 * The Card class is a simple container class for storing the
 *
 */
public class Card
{
  
  /**
   * The id of the card in the database
   */
  private long id;
  
  /**
   * The right,wrong,skip {0,1,2} state of the card
   */
  private int rws;
  
  /**
   * The title of the card, the word to be guessed
   */
  private String title;
  
  /**
   * An array list of the words you cannot say
   */
  private ArrayList<String> badWords;
  
  /**
   * Function for breaking a string into an array list of strings based on the
   * presence of commas. The bad words are stored in the database as a comma
   * separated list for each card.
   * @param commaSeparated - a comma separated string
   * @return an array list of the substrings
   */
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
  
  /**
   * Default constructor 
   */
  public Card()
  {
    this.init( -1, -1, "", new ArrayList<String>() );
  }
  
  /**
   * Copy Constructor
   */
  public Card( Card rhs )
  {
    ArrayList<String> bws = new ArrayList<String>( rhs.getBadWords() );
    this.init( rhs.getId(), this.getRws(), this.getTitle(), bws );
  }
  
  /**
   * Function for initializing card state
   */
  private void init( long id, int rws, String title, 
                     ArrayList<String> badWords )
  {
    this.id = id;
    this.rws = rws;
    this.title = title;
    this.badWords = badWords;
  }
  
  public long getId()
  {
    return this.id;
  }

  
  public void setId( long id )
  {
    this.id = id;
  }

  
  public int getRws()
  {
    return this.rws;
  }

  
  public void setRws( int rws )
  {
    this.rws = rws;
  }

  
  public String getTitle()
  {
    return this.title;
  }

  
  public void setTitle( String title )
  {
    this.title = title;
  }

  
  public ArrayList<String> getBadWords()
  {
    return this.badWords;
  }

  
  public void setBadWords( ArrayList<String> badWords )
  {
    this.badWords = badWords;
  }
  
  /**
   * Override setter for a comma-separated string
   * @param commaSeparated
   */
  public void setBadWords( String commaSeparated )
  {
    this.badWords = Card.BustString( commaSeparated ); 
  }
  
  /**
   * Get the resource ID for this card's right wrong skip icon
   */
  public int getDrawableId()
  {
	switch ( this.rws )
	{
	case 0:
	  return R.drawable.correct;
	case 1:
	  return R.drawable.wrong;
	case 2:
	default:
	  return R.drawable.skip;
	}
  }

}
