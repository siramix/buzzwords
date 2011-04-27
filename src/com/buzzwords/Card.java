package com.buzzwords;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.buzzwords.R;

import android.util.Log;

/**
 * @author The BuzzWords Team
 *
 * The Card class is a simple container class for storing the
 *
 */
public class Card
{
  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "Card";

  public static final int RIGHT = 0;
  public static final int WRONG = 1;
  public static final int SKIP = 2;
  
  /**
   * The db id of the card
   */
  private int id;
  
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
    Log.d( TAG, "BustString()" );
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
    Log.d( TAG, "Card()" );
    this.init(-1, -1, "", new ArrayList<String>() );
  }

  /**
   * Copy Constructor
   */
  public Card( Card rhs )
  {
    Log.d( TAG, "Card( Card )" );
    ArrayList<String> bws = new ArrayList<String>( rhs.getBadWords() );
    this.init( rhs.getId(), rhs.getRws(), rhs.getTitle(), bws );
  }
  
  /**
   * Standard Constructor
   */
  public Card( int id, int rws, String title,
               ArrayList<String> badWords )
  {
    this.init( id, rws, title, badWords );
  }
  
  public Card( int id, String title, String badWords )
  {
    this.init( id, -1, title, Card.BustString(badWords));
  }
  
  @Override
  public boolean equals(Object compareObj)
  {
    if(this == compareObj)
    {
      return true;
    }
   
    if(compareObj == null)
    {
      return false;
    }
   
    if(!(compareObj instanceof Card))
    {
      return false;
    }
    Card rhs = (Card) compareObj;
    return this.badWords.equals( rhs.getBadWords() ) && 
      this.rws == rhs.getRws() &&
      this.title.equals( rhs.getTitle() );    
  }

  /**
   * Function for initializing card state
   */
  private void init( int id, int rws, String title,
                     ArrayList<String> badWords )
  {
    Log.d( TAG, "init()" );
    this.rws = rws;
    this.title = title;
    this.badWords = badWords;
  }

  public int getRws()
  {
    Log.d( TAG, "getRws()" );
    return this.rws;
  }


  public void setRws( int rws )
  {
    Log.d( TAG, "setRws()" );
    this.rws = rws;
  }


  public String getTitle()
  {
    Log.d( TAG, "getTitle()" );
    return this.title;
  }


  public void setTitle( String title )
  {
    Log.d( TAG, "setTitle()" );
    this.title = title;
  }


  public ArrayList<String> getBadWords()
  {
    Log.d( TAG, "getBadWords()" );
    return this.badWords;
  }


  public void setBadWords( ArrayList<String> badWords )
  {
    Log.d( TAG, "setBadWords(ArrayList<String>)" );
    this.badWords = badWords;
  }

  /**
   * Override setter for a comma-separated string
   * @param commaSeparated
   */
  public void setBadWords( String commaSeparated )
  {
    Log.d( TAG, "setBadWords(String)" );
    this.badWords = Card.BustString( commaSeparated );
  }

  /**
   * Get the resource ID for this card's right wrong skip icon
   */
  public int getRowEndDrawableId()
  {
    Log.d( TAG, "getRowEndDrawableId()" );
  	switch ( this.rws )
  	{
  	case 0:
  	  return R.drawable.right;
  	case 1:
  	  return R.drawable.wrong;
  	case 2:
  	  return R.drawable.skip;
  	default:
  	  return 0;
  	}
  }
  
/**
   * Get the resource ID for this card's right wrong skip icon Mid-turn (when user hits back).
   * These IDs must differ from those on Turn Result Screen.
 */
  public static int getCardMarkDrawableId(int cardRWS)
  {
    Log.d( TAG, "getDrawableId()" );
    switch ( cardRWS )
    {
    case RIGHT:
      return R.drawable.controls_right;
    case WRONG:
      return R.drawable.controls_wrong;
    case SKIP:
      return R.drawable.controls_skip;
    default:
      return 0;
    }
  }
  
  /**
   * Cycle right/wrong/skip for the turn summary
   */
  public void cycleRws()
  {
    Log.d( TAG, "cycleRws()" );
    this.rws++;
    if( rws == 3 )
    {
      rws = 0;
    }
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

}
