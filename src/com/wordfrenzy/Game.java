package com.wordfrenzy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.wordfrenzy.R;

import android.content.Context;
import android.util.Log;

/**
 * @author The WordFrenzy Team
 *
 * The Game class is a database abstraction class that deals with the database
 * transaction necessary to care a game of wordfrenzy forward. Game should only
 * be used by game manager as a matter of design.
 */
public class Game
{

  /**
   * logging tag
   */
  public static String TAG = "Game";

  /**
   * The current context (used for database creation and initialization)
   */
  private final Context curContext;

  /**
   * The list of cardIds that we pull from (our "deck" of cards)
   */
  private ArrayList<Card> deck;

  /**
   * The position in the list of card ids (where we are in the "deck")
   */
  private int cardPosition;
  
  private Map<Integer,Integer> scores;

  
  /**
   * @return the cardPosition
   */
  public int getCardPosition()
  {
    return this.cardPosition;
  }
  
  /**
   * @param cardPosition the cardPosition to set
   */
  public void setCardPosition( int cardPosition )
  {
    this.cardPosition = cardPosition;
  }
  /**
   * @return the arraylist of cards in your deck
   */
  public ArrayList<Card> getDeck()
  {
    return this.deck;
  }

  /**
   * Standard constructor. If you're wondering about the necessity of the
   * context, it is used to create the database of the superclass and we need
   * it to populate the card table with the xml resource containing the starter
   * pack.
   * @param context - the context to pass to the superclass and initialize the
   * @param dbname - the name of the database to create, if null is stored in memory (according to super)
   * card table.
   */
  public Game( Context context )
  {
	Log.d( TAG, "Game(Context)" );
    this.curContext = context;
    this.scores = new HashMap<Integer,Integer>();
    this.clearDeck();
  }
  
  /**
   * Empties the current deck and instantiates a new ArrayList of cards.
   */
  public void clearDeck()
  {
    this.deck = new ArrayList<Card>();
    this.cardPosition = -1;
  }
  
  /**
   * Kill all cards that came before
   */
  public void pruneDeck()
  {
    for( int i = 0; i < this.deck.size(); ++i )
    {
      if( this.deck.get( i ).getRws() != -1 )
      {
        this.deck.remove( i );
        i--; // we removed so we need to hop back 
      }
      else
      {
        this.deck.remove( i );
        break;
      }
    }
    this.cardPosition = -1;
  }

  /**
   * Query the database for all the cards it has. That query specifies a random
   * order; thus, a cursor full of longs is returned. We push those longs into
   * our newly initialized ArrayList, cardIds. Note the cardIdPosition is set
   * to zero indicating the first card id in our "deck."
   */
  public void prepDeck()
  {
    Log.d( TAG, "prepDeck()" );
    
    InputStream starterXML =
        curContext.getResources().openRawResource(R.raw.starter);
      DocumentBuilderFactory docBuilderFactory =
        DocumentBuilderFactory.newInstance();
      
      Log.d( TAG, "Building DocBuilderFactory for card pack parsing from " + R.class.toString() );
      try
      {
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(starterXML);
        NodeList cardNodes = doc.getElementsByTagName( "card" );
        for(int i = 0; i < cardNodes.getLength(); i++)
        {
          NodeList titleWhiteAndBads = cardNodes.item( i ).getChildNodes();
          Node titleNode = null;
          Node badsNode = null;
          for( int j = 0; j < titleWhiteAndBads.getLength(); j++ )
          {
            String candidateName = titleWhiteAndBads.item( j ).getNodeName();
            if( candidateName.equals( "title" ) )
            {
              titleNode = titleWhiteAndBads.item( j );
            }
            else if( candidateName.equals( "bad-words" ) )
            {
              badsNode = titleWhiteAndBads.item( j );
            }
            else
            {
              continue; // We found some #text
            }
          }
          String title = titleNode.getFirstChild().getNodeValue();
          String badWords = "";
          NodeList bads = badsNode.getChildNodes();
          for( int j = 0; j < bads.getLength(); j++ )
          {
            String candidateName = bads.item( j ).getNodeName();
            if( candidateName.equals( "word" ) )
            {
              badWords += bads.item( j ).getFirstChild().getNodeValue() + ",";
            }
          }
          // hack because I have a comma at the end
          badWords = badWords.substring( 0, badWords.length() - 1 );

          Card card = new Card();
          card.setTitle( title );
          card.setBadWords( badWords );
          this.deck.add( card );
        }
      }
      catch( ParserConfigurationException e )
      {
        e.printStackTrace();
      }
      catch( SAXException e )
      {
        e.printStackTrace();
      }
      catch( IOException e )
      {
        e.printStackTrace();
      }
  }

  /**
   * Get the card indicated by the cardIdPosition. If we've dealt past the end
   * of the deck, we should prep the deck.
   * @return the card we want
   */
  public Card getNextCard()
  {
    Log.d( TAG, "getNextCard()" );
    // check deck bounds
    if( this.cardPosition >= this.deck.size()-1 || this.cardPosition == -1 )
    {
      this.prepDeck();
    }

    // return the card (it could be blank)
    return this.deck.get( ++this.cardPosition );
  }

  /**
   * Return the previous card
   * @return the previous card in the deck
   */
  public Card getPreviousCard()
  {
    Log.d( TAG, "getPreviousCard()" );
    
    if( this.cardPosition == 0 )
    {
      this.cardPosition = 1; 
    }
    return this.deck.get( --this.cardPosition );
  }

  /**
   * Create a game identified by the current date and return its database id
   * @return the id of the newly created game
   */
  public int newGame()
  {
    Log.d( TAG, "newGame()" );
    return 0;
  }
  
  /**
   * Creates a turn record in the database. The turn knows about the game,
   * team, and round in which it occurred, as well as its score.
   * @param gameId - the id of the game in which the turn took place
   * @param teamId - the id of the team doing the guessing on the turn
   * @param round - the round in which the turn took place
   * @param score - the score for the turn
   * @return the id of the turn
   */
  public void newTurn( int teamId, int score )
  {
    Log.d( TAG, "newTurn()" );
    int prevScore = 0;
    if( this.scores.containsKey(teamId) )
      {
        prevScore = this.scores.get(teamId);
      }
    this.scores.put( teamId, prevScore + score);
  }
  
}
