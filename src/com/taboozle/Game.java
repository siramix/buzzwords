package com.taboozle;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author The Taboozle Team
 *
 * The Game class is a database abstraction class that deals with the database
 * transaction necessary to care a game of taboozle forward. Game should only
 * be used by game manager as a matter of design.
 */
public class Game extends SQLiteOpenHelper
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
  private ArrayList<Card> cards;

  /**
   * The position in the list of card ids (where we are in the "deck")
   */
  private int cardPosition;

  /**
   * Standard constructor. If you're wondering about the necessity of the
   * context, it is used to create the database of the superclass and we need
   * it to populate the card table with the xml resource containing the starter
   * pack.
   * @param context - the context to pass to the superclass and initialize the
   * card table.
   */
  public Game( Context context )
  {
    super( context, GameData.DATABASE_NAME, null,
           GameData.DATABASE_VERSION );
    Log.d( TAG, "Game()" );
    this.curContext = context;
    this.clearDeck();
  }
  
  public void clearDeck()
  {
    this.cards = new ArrayList<Card>();
    this.cardPosition = -1;
  }
  
  /**
   * Kill all cards that came before
   */
  public void pruneDeck()
  {
    for( int i = 0; i < this.cards.size(); ++i )
    {
      if( this.cards.get( i ).getRws() != -1 )
      {
        this.cards.remove( i );
        i--; // we removed so we need to hop back 
      }
      else
      {
        this.cards.remove( i );
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

    // query for ids
    SQLiteDatabase db = this.getReadableDatabase();
    String[] columns = new String[] {GameData.Cards._ID, GameData.Cards.TITLE,
                                     GameData.Cards.BAD_WORDS};
    Cursor cur = db.query( GameData.CARD_TABLE_NAME, columns, null, null,
                         null, null, "RANDOM()");

    // iterate through the cursor pushing the ids into cardIds
    if( cur.moveToFirst() )
    {
      do
      {
        int idColumn = cur.getColumnIndex( GameData.Cards._ID );
        int titleColumn = cur.getColumnIndex( GameData.Cards.TITLE );
        int badWordsColumn = cur.getColumnIndex( GameData.Cards.BAD_WORDS );

        Card card = new Card();
        card.setId( cur.getLong( idColumn ) );
        card.setTitle( cur.getString( titleColumn ) );
        card.setBadWords( cur.getString( badWordsColumn ) );
        this.cards.add( card );

      } while( cur.moveToNext() );
    }
    cur.close();
  }

  /**
   * Query the database for all round scores for a team in a given game.
   *
   * @param teamID - the database ID of the team whose scores are being retrieved
   * @param gameID - the database ID of the game whose scores are being retrieved
   * @return an array of scores, one element per round
   */
  public long[] getRoundScores(long teamID, long gameID)
  {
    Log.d( TAG, "getRoundScores()" );
    // query for scores
    SQLiteDatabase db = this.getReadableDatabase();
    String[] columns = new String[] {GameData.TurnScores.SCORE};
    Cursor cur = db.query( GameData.TURN_SCORES_TABLE_NAME, columns,
    		               GameData.TurnScores.TEAM_ID + "=" + teamID + " AND " +
    		               GameData.TurnScores.GAME_ID + "=" + gameID, null, null, null,
    		               GameData.TurnScores.ROUND);

    long[] scores = new long[cur.getCount()];

    // iterate through the cursor populating array of scores
    if( cur.moveToFirst() )
    {
      int i = 0;
      do
      {
        int scoreColumn = cur.getColumnIndex( GameData.TurnScores.SCORE );
        scores[i] = cur.getLong( scoreColumn );
        i++;
      } while( cur.moveToNext() );
    }

    cur.close();
    return scores;
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
    if( this.cardPosition >= this.cards.size()-1 || this.cardPosition == -1 )
    {
      this.prepDeck();
    }

    // return the card (it could be blank)
    return this.cards.get( ++this.cardPosition );
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
    return this.cards.get( --this.cardPosition );
  }

  /**
   * Create a game identified by the current date and return its database id
   * @return the id of the newly created game
   */
  public long newGame()
  {
    Log.d( TAG, "newGame()" );
    // Prepare the current date for insertion
    Date currentTime = new Date();
    String dateString = currentTime.toString();
    ContentValues values = new ContentValues();
    values.put( GameData.Games.TIME, dateString );
    SQLiteDatabase db = this.getWritableDatabase();

    // Do the insert and return the row id
    return db.insert(GameData.GAME_TABLE_NAME, "", values);
  }

  /**
   * Create a team identified by name and return the team's id
   * @param name - the name of the team
   * @return the id of the newly created team
   */
  public long newTeam( String name )
  {
    Log.d( TAG, "newTeam()" );
    ContentValues values = new ContentValues();
    values.put(GameData.Teams.NAME, name);
    SQLiteDatabase db = this.getWritableDatabase();
    return db.insert(GameData.TEAM_TABLE_NAME, "", values);
  }
  /**
   * Creates an entry in the Game History table for each team representing the
   * game's final scores.
   */
  public long completeGame( long gameId, long teamId, long score )
  {
    Log.d( TAG, "completeGame()" );
    ContentValues values = new ContentValues();
    values.put( GameData.FinalScores.GAME_ID, gameId );
    values.put( GameData.FinalScores.TEAM_ID, teamId );
    values.put( GameData.FinalScores.SCORE, score );
    SQLiteDatabase db = this.getWritableDatabase();
    return db.insert( GameData.FINAL_SCORES_TABLE_NAME, "", values );
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
  public long newTurn( long gameId, long teamId, long round, long score )
  {
    Log.d( TAG, "newTurn()" );
    ContentValues values = new ContentValues();
    values.put( GameData.TurnScores.GAME_ID, gameId );
    values.put( GameData.TurnScores.TEAM_ID, teamId );
    values.put( GameData.TurnScores.ROUND, round );
    values.put( GameData.TurnScores.SCORE, score );
    SQLiteDatabase db = this.getWritableDatabase();
    return db.insert(GameData.TURN_SCORES_TABLE_NAME, "", values);
  }

  /**
   * Adds a record of a played card to the database
   * @param gameId - the game the card occurred in
   * @param teamId - the team calling the card
   * @param cardId - the card played
   * @param turnScoreId - the turn the card was called in
   * @param rws - whether the card was right, wrong or skipped
   */
  public void completeCard( long gameId, long teamId, long cardId,
		  					long turnScoreId, long rws, long time )
  {
    Log.d( TAG, "completeCard()" );
    ContentValues values = new ContentValues();
    values.put(  GameData.GameHistory.GAME_ID, gameId );
    values.put( GameData.GameHistory.TEAM_ID, teamId );
    values.put( GameData.GameHistory.CARD_ID, cardId );
    values.put( GameData.GameHistory.TURN_SCORE_ID, turnScoreId );
    values.put( GameData.GameHistory.RWS, rws );
    values.put( GameData.GameHistory.TIME, time );
    SQLiteDatabase db = this.getWritableDatabase();
    db.insert( GameData.GAME_HISTORY_TABLE_NAME, "", values );
  }


  /*
   * AwardsQuery takes the awardID and gameID and returns data related to that award and
   * game.  Award.java should then be able to handle this data and assign awards to their
   * appropriate teams.
   * 
   * Results are returned as a 4x2 matrix of doubles, where column A represents the team_ids
   * of eligible teams to be given the award and column B is a wildcard for the relevant
   * value being searched, such as card time or number of skips.
   * 
   * SELECT team_id
   * FROM gamehistory
   * WHERE game_id = gameID
   * GROUP BY team_id
   * HAVING RWS = 2
   * ORDER BY COUNT(*) DESC
   * LIMIT 1;
   * 
   */
  public double[][] awardsQuery( int awardID, long gameID )
  {
	Log.d( TAG, "awardsQuery (" + awardID + ", " + gameID + ")" );
   	//String[] results = {"", ""}; //TeamID, AwardValue or Word
   	double[][] results = new double[4][2]; //TeamID, AwardValue or Word
  	SQLiteDatabase db = this.getReadableDatabase();
  	String queryStr = "select -1, -1";
  	Cursor cursor = null;
  	
  	switch (awardID) 
  	{  	
  	  
      case 0: 
        //Most Correct in the Game
        //Find the teams that have total corrects equal to the highest number by any team
        Log.d(TAG, "Query for most correct in game.  Col2 is Num Correct.");
        queryStr = "SELECT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_CORRECT" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME +
          " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
            " AND " + GameData.GameHistory.RWS + "=0" +            
          " GROUP BY " + GameData.GameHistory.TEAM_ID +
          " HAVING " + "COUNT(*)=" +
          //Retrieve the highest number of wrongs by any team
          " (SELECT COUNT(*) as NUM_SKIPS" +  
              " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " " +
              " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
             " and " + GameData.GameHistory.RWS + "=" + GameData.RIGHT +
             " GROUP BY " + GameData.GameHistory.TEAM_ID + " " + 
             " ORDER BY 1 DESC" + 
             " LIMIT 1)";
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
  	  
      case 1: 
        //Most Incorrect in the Game
        //Find the teams that have total wrongs equal to the highest number by any team
        Log.d(TAG, "Query for most incorrect in game. Col2 is Num Incorrect.");
        queryStr = "SELECT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_INCORRECT" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME +
          " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
            " AND " + GameData.GameHistory.RWS + "=" + GameData.WRONG +            
          " GROUP BY " + GameData.GameHistory.TEAM_ID +
          " HAVING " + "COUNT(*)=" +
          //Retrieve the highest number of wrongs by any team
          " (SELECT COUNT(*) as NUM_SKIPS" +  
              " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " " +
              " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
                " and " + GameData.GameHistory.RWS + "=" + GameData.SKIP +
              " GROUP BY " + GameData.GameHistory.TEAM_ID + " " + 
              " ORDER BY 1 DESC" + 
              " LIMIT 1)";
        Log.d(TAG, queryStr);        
        cursor = db.rawQuery(queryStr, null);
        break;        
            
  	  case 2: 
  		//Most Skips in the Game
  		//Find the teams that have total skips equal to the highest number by any team
  	    Log.d(TAG, "Query for most skips in game. Col2 is Num Skipped.");
  	    queryStr = "SELECT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_SKIPPED" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME +
          " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
            " AND " + GameData.GameHistory.RWS + "=" + GameData.SKIP +
          " GROUP BY " + GameData.GameHistory.TEAM_ID +
          " HAVING " + "COUNT(*)=" +
          //Retrieve the highest number of skips by any team
          " (SELECT COUNT(*) as NUM_SKIPS" +  
              " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " " +
              " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
                " and " + GameData.GameHistory.RWS + "=" + GameData.SKIP + 
              " GROUP BY " + GameData.GameHistory.TEAM_ID + " " + 
              " ORDER BY 1 DESC" + 
              " LIMIT 1)";
  	    Log.d(TAG, queryStr);
  		  cursor = db.rawQuery(queryStr, null);
      	break;
      	  
  	  case 3: //Highest single turn score
  	    Log.d(TAG, "Query for highest single turn score in game. Col2 is Score.");
  	    queryStr = "SELECT DISTINCT " + GameData.TurnScores.TEAM_ID + ", " + GameData.TurnScores.SCORE +
          " FROM " + GameData.TURN_SCORES_TABLE_NAME +
          " WHERE " + GameData.TurnScores.GAME_ID + "=" + gameID + 
          " ORDER BY " + GameData.TurnScores.SCORE + " DESC" + 
          " LIMIT 2";
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
  	    break;
  	    
      case 4: //Most Correct in Round and not Highest Scoring
        Log.d(TAG, "Query for most correct in round and not highest scoring.  Col2 is Num Correct.");
        queryStr = "SELECT DISTINCT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_CORRECT" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
          " WHERE gh." + GameData.GameHistory.GAME_ID + "=" + gameID +
             " and gh." + GameData.GameHistory.RWS + "=" + + GameData.RIGHT + 
             " and gh." + GameData.GameHistory.TEAM_ID + " NOT IN " +
             // Find out which teams match the highest score
             " (SELECT ts." + GameData.TurnScores.TEAM_ID + 
               " FROM " + GameData.TURN_SCORES_TABLE_NAME + " ts" + 
               " WHERE ts." + GameData.TurnScores.GAME_ID + "=" + gameID +
                 " and ts." + GameData.TurnScores.SCORE + " = " +
                 // Retrieve the highest score
                 " (SELECT ts2." + GameData.TurnScores.SCORE +
                   " FROM " + GameData.TURN_SCORES_TABLE_NAME + " ts2" +
                   " WHERE ts2." + GameData.TurnScores.GAME_ID + "=" + gameID +
                   " ORDER BY ts2." + GameData.TurnScores.SCORE + " DESC" + 
                   " LIMIT 1)" +
              ")" +
          " GROUP BY gh." + GameData.GameHistory.TURN_SCORE_ID + 
          " ORDER BY 2 DESC";
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
        
      case 5: //Most skipped in a single round
        Log.d(TAG, "Query for most skipped in a single round. Col2 is Num Skipped.");
        queryStr = "SELECT DISTINCT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_SKIPS" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME +
          " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
            " and " + GameData.GameHistory.RWS + "=" + GameData.SKIP + 
          " GROUP BY " + GameData.GameHistory.TURN_SCORE_ID +
          " HAVING " + "COUNT(*)=" +
             //Retrieve the highest number of skipped cards in a single turn
             " (SELECT COUNT(*)" +
               " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh2" +
               " WHERE gh2." + GameData.GameHistory.GAME_ID + "=" + gameID +
                 " and gh2." + GameData.GameHistory.RWS + "=" + GameData.SKIP +
               " GROUP BY gh2." + GameData.GameHistory.TURN_SCORE_ID + 
               " ORDER BY 1 DESC" +
               " LIMIT 1)" +
          " ORDER BY 2 DESC";
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);        
        break;
        
      case 6: //Most incorrect in a single round
        Log.d(TAG, "Query for most incorrect in a single round. Col2 is Num Incorrect.");
        queryStr = "SELECT DISTINCT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_INCORRECT" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME +
          " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
            " and " + GameData.GameHistory.RWS + "=" + GameData.WRONG + 
          " GROUP BY " + GameData.GameHistory.TURN_SCORE_ID +
          " HAVING " + "COUNT(*)=" +
             //Retrieve the highest number of incorrect cards in a single turn
             " (SELECT COUNT(*)" +
               " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh2" +
               " WHERE gh2." + GameData.GameHistory.GAME_ID + "=" + gameID +
                 " and gh2." + GameData.GameHistory.RWS + "=" + GameData.WRONG + 
               " GROUP BY gh2." + GameData.GameHistory.TURN_SCORE_ID + 
               " ORDER BY 1 DESC" +
               " LIMIT 1)" +
          " ORDER BY 2 DESC";                 
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);        
        break;  
        
      case 7: //Only skipped cards in a single round
        Log.d(TAG, "Query for only skipped cards in a single round. Col2 is Num Skips.");
        queryStr ="SELECT DISTINCT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_SKIPS" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME +
          " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
             " and " + GameData.GameHistory.RWS + "=" + GameData.SKIP + 
             " and " + GameData.GameHistory.TURN_SCORE_ID + " NOT IN " +
             //Exclude all turns that had something other than a skip
             " (SELECT DISTINCT gh2." + GameData.GameHistory.TURN_SCORE_ID +
               " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " as gh2" +
               " WHERE " + GameData.GameHistory.RWS + "=" + GameData.RIGHT +
                 " OR " + GameData.GameHistory.RWS + "=" + GameData.WRONG + ")" +                   
          " GROUP BY " + GameData.GameHistory.TURN_SCORE_ID + 
          " HAVING NUM_SKIPS > 0" +
          " ORDER BY 2 DESC";            
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;    
        
      case 8: //All teams that got negative points in a round
        Log.d(TAG, "Query for teams with negative points in a round. Col2 is empty.");
        queryStr = "SELECT DISTINCT " + GameData.GameHistory.TEAM_ID +
          " FROM " + GameData.TURN_SCORES_TABLE_NAME +
          " WHERE " + GameData.TurnScores.GAME_ID + "=" + gameID +
              " and " + GameData.TurnScores.SCORE + "<0";
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;   
        
      case 9: //All teams that got zero points in a round
        Log.d(TAG, "Query for teams with zero points in a round. Col2 is empty.");
        queryStr = "SELECT DISTINCT " + GameData.TurnScores.TEAM_ID +
          " FROM " + GameData.TURN_SCORES_TABLE_NAME +
          " WHERE " + GameData.TurnScores.GAME_ID + "=" + gameID +
              " and " + GameData.TurnScores.SCORE + "=0";
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
        
      case 10: //No actions performed
        Log.d(TAG, "Query for teams with no actions performed. Col2 is empty.");
        queryStr = "SELECT DISTINCT " + GameData.TurnScores.TEAM_ID +
          " FROM " + GameData.TURN_SCORES_TABLE_NAME +
          " WHERE " + GameData.TurnScores.GAME_ID + "=" + gameID +
            " and " + GameData.TurnScores.SCORE + "=0" + 
            " and " + GameData.TurnScores._ID + " NOT IN " +
            //Exclude turns that have registered an action performed by a team
            " (SELECT DISTINCT gh." + GameData.GameHistory.TURN_SCORE_ID + 
              " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " as gh" +
              " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID + ")";    
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;        
        
      case 11: //Fastest Correct Under 5s
        //Returns each team's fastest card, sorted by fastest to slowest
        Log.d(TAG, "Query for fastest correct under 5. Col2 is Card_ID.");
        queryStr = "SELECT gh." + GameData.GameHistory.TEAM_ID + ", gh." + GameData.GameHistory.CARD_ID +
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
          " INNER JOIN (" + 
          //Find the fastest correct entry in gamehistory and tie it to the team and card
          " SELECT " + GameData.GameHistory.TEAM_ID + ", " +
                       GameData.GameHistory.CARD_ID + ", " +
                      "MIN(" + GameData.GameHistory.TIME + ") as mintime" +
            " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " inr_gh" +
            " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
            " and " + GameData.GameHistory.TIME + "< 5000" +
            " and " + GameData.GameHistory.RWS + "=" + GameData.RIGHT + ") as inr_gh" +
          " ON " + "gh." + GameData.GameHistory.TEAM_ID + " = inr_gh." + GameData.GameHistory.TEAM_ID +
          " AND " + "gh." + GameData.GameHistory.CARD_ID + " = inr_gh." + GameData.GameHistory.CARD_ID +
          " ORDER BY " + GameData.GameHistory.TIME + " ASC" + 
          " LIMIT 4 "; 
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
        
      case 12: //Fastest Skip Under 5s
        //Returns each team's fastest skipped card, sorted by fastest to slowest
        Log.d(TAG, "Query for fastest skip under 5. Col2 is Card_ID.");
        queryStr = "SELECT gh." + GameData.GameHistory.TEAM_ID + ", gh." + GameData.GameHistory.CARD_ID +
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
          " INNER JOIN (" + 
          //Find the fastest skip entry in gamehistory and tie it to the team and card
            " SELECT " + GameData.GameHistory.TEAM_ID + ", " +
                   GameData.GameHistory.CARD_ID + ", " +
                   "MIN(" + GameData.GameHistory.TIME + ") as mintime" +
            " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " inr_gh" +
            " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
            " and " + GameData.GameHistory.TIME + "< 5000" +
            " and " + GameData.GameHistory.RWS + "=" + GameData.SKIP + ") as inr_gh" +
          " ON " + "gh." + GameData.GameHistory.TEAM_ID + " = inr_gh." + GameData.GameHistory.TEAM_ID +
          " AND " + "gh." + GameData.GameHistory.CARD_ID + " = inr_gh." + GameData.GameHistory.CARD_ID +
          " ORDER BY " + GameData.GameHistory.TIME + " ASC" + 
          " LIMIT 4 "; 
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;                     
                    
      case 13: //Fastest Wrong Under 5s
        //Returns each team's fastest wrong card, sorted by fastest to slowest
        Log.d(TAG, "Query for fastest wrong under 5. Col2 is Card_ID.");
        queryStr = "SELECT gh." + GameData.GameHistory.TEAM_ID + ", gh." + GameData.GameHistory.CARD_ID +
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
          " INNER JOIN (" + 
          //Find the fastest wrong entry in gamehistory and tie it to the team and card
            " SELECT " + GameData.GameHistory.TEAM_ID + ", " +
                         GameData.GameHistory.CARD_ID + ", " +
                        "MIN(" + GameData.GameHistory.TIME + ") as mintime" +
            " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " inr_gh" +
            " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
            " and " + GameData.GameHistory.TIME + "< 5000" +
            " and " + GameData.GameHistory.RWS + "=" + GameData.WRONG + ") as inr_gh" +
          " ON " + "gh." + GameData.GameHistory.TEAM_ID + " = inr_gh." + GameData.GameHistory.TEAM_ID +
          " AND " + "gh." + GameData.GameHistory.CARD_ID + " = inr_gh." + GameData.GameHistory.CARD_ID +
          " ORDER BY " + GameData.GameHistory.TIME + " ASC" + 
          " LIMIT 4 "; 
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;           

      case 14: //Slowest Correct Longer than 30s
        //Returns each team's slowest correct card, sorted by slowest to fastest
        Log.d(TAG, "Query for slowest correct longer than 30s. Col2 is Card_ID.");
        queryStr = "SELECT gh." + GameData.GameHistory.TEAM_ID + ", gh." + GameData.GameHistory.CARD_ID +
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
          " INNER JOIN (" + 
          //Find the slowest correct entry in gamehistory and tie it to the team and card
            " SELECT " + GameData.GameHistory.TEAM_ID + ", " +
                         GameData.GameHistory.CARD_ID + ", " +
                        "MAX(" + GameData.GameHistory.TIME + ") as MAXTIME" +
            " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " inr_gh" +
            " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
            " and " + GameData.GameHistory.TIME + "> 30000" +
            " and " + GameData.GameHistory.RWS + "=" + GameData.RIGHT + ") as inr_gh" +
          " ON " + "gh." + GameData.GameHistory.TEAM_ID + " = inr_gh." + GameData.GameHistory.TEAM_ID +
          " AND " + "gh." + GameData.GameHistory.CARD_ID + " = inr_gh." + GameData.GameHistory.CARD_ID +
          " ORDER BY " + GameData.GameHistory.TIME + " DESC" + 
          " LIMIT 4 "; 
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;           
            
      case 15: //Slowest Wrong Longer than 30s
        //Returns each team's slowest wrong card, sorted by slowest to fastest
        Log.d(TAG, "Query for slowest wrong longer than 30s. Col2 is Card_ID.");
        queryStr = "SELECT gh." + GameData.GameHistory.TEAM_ID + ", gh." + GameData.GameHistory.CARD_ID +
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
          " INNER JOIN (" + 
          //Find the slowest wrong entry in gamehistory and tie it to the team and card
            " SELECT " + GameData.GameHistory.TEAM_ID + ", " +
                         GameData.GameHistory.CARD_ID + ", " +
                        "MAX(" + GameData.GameHistory.TIME + ") as MAXTIME" +
             " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " inr_gh" +
             " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
             " and " + GameData.GameHistory.TIME + "> 30000" +
             " and " + GameData.GameHistory.RWS + "=" + GameData.WRONG + ") as inr_gh" +
          " ON " + "gh." + GameData.GameHistory.TEAM_ID + " = inr_gh." + GameData.GameHistory.TEAM_ID +
          " AND " + "gh." + GameData.GameHistory.CARD_ID + " = inr_gh." + GameData.GameHistory.CARD_ID +
          " ORDER BY " + GameData.GameHistory.TIME + " DESC" + 
          " LIMIT 4 "; 
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;  
            
      case 16: //Slowest Skip Longer than 30s
        //Returns each team's slowest skipped card, sorted by slowest to fastest
        Log.d(TAG, "Query for slowest skip longer than 30s. Col2 is Card_ID.");
        queryStr = "SELECT gh." + GameData.GameHistory.TEAM_ID + ", gh." + GameData.GameHistory.CARD_ID +
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
          " INNER JOIN (" + 
          //Find the slowest skip entry in gamehistory and tie it to the team and card
            " SELECT " + GameData.GameHistory.TEAM_ID + ", " +
                         GameData.GameHistory.CARD_ID + ", " +
                        "MAX(" + GameData.GameHistory.TIME + ") as MAXTIME" +
             " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " inr_gh" +
             " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
             " and " + GameData.GameHistory.TIME + "> 30000" +
             " and " + GameData.GameHistory.RWS + "=" + GameData.SKIP + ") as inr_gh" +
          " ON " + "gh." + GameData.GameHistory.TEAM_ID + " = inr_gh." + GameData.GameHistory.TEAM_ID +
          " AND " + "gh." + GameData.GameHistory.CARD_ID + " = inr_gh." + GameData.GameHistory.CARD_ID +
          " ORDER BY " + GameData.GameHistory.TIME + " DESC" + 
          " LIMIT 4 "; 
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;        
        
      case 17: //Longest Correct streak 
        // Reference: http://www.sqlteam.com/article/detecting-runs-or-streaks-in-your-data
        Log.d(TAG, "Query for longest correct streak. Col2 is Max Correct Streak.");
        queryStr = "SELECT " + GameData.GameHistory.TEAM_ID + ", MAX(STREAK)" +
          " FROM " +
          " (SELECT " + GameData.GameHistory.TEAM_ID + ", " 
                      + GameData.GameHistory.RWS + ", COUNT(*) as STREAK" +
            " FROM " +
              //Result table contains team_id, rws, and its 'group'.  Every streak of numbers
              //belongs to the same streak.
              " (SELECT " + GameData.GameHistory.TEAM_ID + ", " + GameData.GameHistory.RWS + "," +
                //Count the number of times up to the current row where a value other than 
                //the current has been seen (so the count repeats as long as the rws doesn't change).
                //This is the 'group' that the entry belongs to
                " (SELECT COUNT(*)" +
                  " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh2" +
                  " WHERE gh2." + GameData.GameHistory.GAME_ID + " = " + gameID +
                  " and gh2." + GameData.GameHistory.RWS + " <> gh." + GameData.GameHistory.RWS +
                  " and gh2." + GameData.GameHistory._ID + " <= gh." + GameData.GameHistory._ID +
                  " and gh2." + GameData.GameHistory.TEAM_ID + " = gh." + 
                                        GameData.GameHistory.TEAM_ID + ") as GROUPNUM" +
                " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
                " WHERE " + GameData.GameHistory.GAME_ID + "= " + gameID + ")" + 
            " WHERE " + GameData.GameHistory.RWS + " =" + GameData.RIGHT +
            " GROUP BY GROUPNUM, " + GameData.GameHistory.TEAM_ID + ", " + 
                     GameData.GameHistory.RWS + ")" +
          " GROUP BY " + GameData.GameHistory.TEAM_ID;
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;        
        
      case 18: //Longest Wrong streak 
        // Reference: http://www.sqlteam.com/article/detecting-runs-or-streaks-in-your-data
        Log.d(TAG, "Query for longest wrong streak. Col2 is Max Wrong Streak.");
        queryStr = "SELECT " + GameData.GameHistory.TEAM_ID + ", MAX(STREAK)" +
          " FROM " +
          " (SELECT " + GameData.GameHistory.TEAM_ID + ", " 
                      + GameData.GameHistory.RWS + ", COUNT(*) as STREAK" +
            " FROM " +
              //Result table contains team_id, rws, and its 'group'.  Every streak of numbers
              //belongs to the same streak.
              " (SELECT " + GameData.GameHistory.TEAM_ID + ", " + GameData.GameHistory.RWS + "," +
                //Count the number of times up to the current row where a value other than 
                //the current has been seen (so the count repeats as long as the rws doesn't change).
                //This is the 'group' that the entry belongs to
                " (SELECT COUNT(*)" +
                  " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh2" +
                  " WHERE gh2." + GameData.GameHistory.GAME_ID + " = " + gameID +
                  " and gh2." + GameData.GameHistory.RWS + " <> gh." + GameData.GameHistory.RWS +
                  " and gh2." + GameData.GameHistory._ID + " <= gh." + GameData.GameHistory._ID +
                  " and gh2." + GameData.GameHistory.TEAM_ID + " = gh." + 
                                        GameData.GameHistory.TEAM_ID + ") as GROUPNUM" +
                " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
                " WHERE " + GameData.GameHistory.GAME_ID + "= " + gameID + ")" + 
            " WHERE " + GameData.GameHistory.RWS + " = " + GameData.WRONG + 
            //This above line is the line that changes between the wrong, skip, and right streaks
            " GROUP BY GROUPNUM, " + GameData.GameHistory.TEAM_ID + ", " + 
                     GameData.GameHistory.RWS + ")" +
          " GROUP BY " + GameData.GameHistory.TEAM_ID; 
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break; 
        
      case 19: //Longest Skip streak 
        // Reference: http://www.sqlteam.com/article/detecting-runs-or-streaks-in-your-data
        Log.d(TAG, "Query for longest skip streak. Col2 is Max Skip Streak.");
        queryStr = "SELECT " + GameData.GameHistory.TEAM_ID + ", MAX(STREAK)" +
          " FROM " +
          " (SELECT " + GameData.GameHistory.TEAM_ID + ", " 
                      + GameData.GameHistory.RWS + ", COUNT(*) as STREAK" +
            " FROM " +
              //Result table contains team_id, rws, and its 'group'.  Every streak of numbers
              //belongs to the same streak.
              " (SELECT " + GameData.GameHistory.TEAM_ID + ", " + GameData.GameHistory.RWS + "," +
                //Count the number of times up to the current row where a value other than 
                //the current has been seen (so the count repeats as long as the rws doesn't change).
                //This is the 'group' that the entry belongs to
                " (SELECT COUNT(*)" +
                  " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh2" +
                  " WHERE gh2." + GameData.GameHistory.GAME_ID + " = " + gameID +
                  " and gh2." + GameData.GameHistory.RWS + " <> gh." + GameData.GameHistory.RWS +
                  " and gh2." + GameData.GameHistory._ID + " <= gh." + GameData.GameHistory._ID +
                  " and gh2." + GameData.GameHistory.TEAM_ID + " = gh." + 
                                        GameData.GameHistory.TEAM_ID + ") as GROUPNUM" +
                " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh" +
                " WHERE " + GameData.GameHistory.GAME_ID + "= " + gameID + ")" + 
            " WHERE " + GameData.GameHistory.RWS + " = " + GameData.SKIP + 
            //This above line is the line that changes between the wrong, skip, and right streaks
            " GROUP BY GROUPNUM, " + GameData.GameHistory.TEAM_ID + ", " + 
                     GameData.GameHistory.RWS + ")" +
          " GROUP BY " + GameData.GameHistory.TEAM_ID; 
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
        
      case 20: //Comeback Kings
    	// Come back from a 10 point deficit and win
    	//TODO HackFixMe
    	Log.d(TAG, "Query for coming back by a 10 point defecit and winning.");
        queryStr = "SELECT " + GameData.FinalScores.TEAM_ID + 
        	" FROM " + GameData.FINAL_SCORES_TABLE_NAME +
        	" WHERE " + GameData.FinalScores.GAME_ID + "=" + gameID +	 
        	" ORDER BY " + GameData.FinalScores.SCORE + " DESC" + 
        	" LIMIT 1";
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
        
      case 22: //Be last and lose to next lowest player by half their score
        Log.d(TAG, "Query for be last and lose to next lowest by half their score. Col2 is Num Seen.");
        queryStr = "SELECT DISTINCT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_SEEN" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME +
          " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
          " GROUP BY " + GameData.GameHistory.TEAM_ID +
          " HAVING " + "COUNT(*)=" +
             //Retrieve the team id for the team that was last
             " (SELECT team_id " +
               " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh2" +
               " WHERE gh2." + GameData.GameHistory.GAME_ID + "=" + gameID +
               " GROUP BY gh2." + GameData.GameHistory.TEAM_ID + 
               " ORDER BY 1 DESC" +
               " LIMIT 1)";         
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
        
      case 23: //Fewest Cards Seen 
        Log.d(TAG, "Query for fewest cards seen. Col2 is Num Seen.");
        queryStr = "SELECT DISTINCT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_SEEN" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME +
          " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
          " GROUP BY " + GameData.GameHistory.TEAM_ID +
          " HAVING " + "COUNT(*)=" +
             //Retrieve the fewest number of cards seen for any team
             " (SELECT COUNT(*)" +
               " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh2" +
               " WHERE gh2." + GameData.GameHistory.GAME_ID + "=" + gameID +
               " GROUP BY gh2." + GameData.GameHistory.TEAM_ID + 
               " ORDER BY 1 ASC" +
               " LIMIT 1)";         
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;      
        
      case 24: //Most Cards Seen
        Log.d(TAG, "Query for most cards seen. Col2 is Num Seen.");
        queryStr = "SELECT DISTINCT " + GameData.GameHistory.TEAM_ID + ", COUNT(*) as NUM_SEEN" +  
          " FROM " + GameData.GAME_HISTORY_TABLE_NAME +
          " WHERE " + GameData.GameHistory.GAME_ID + "=" + gameID +
          " GROUP BY " + GameData.GameHistory.TEAM_ID +
          " HAVING " + "COUNT(*)=" +
             //Retrieve the fewest number of cards seen for any team
             " (SELECT COUNT(*)" +
               " FROM " + GameData.GAME_HISTORY_TABLE_NAME + " gh2" +
               " WHERE gh2." + GameData.GameHistory.GAME_ID + "=" + gameID +
               " GROUP BY gh2." + GameData.GameHistory.TEAM_ID + 
               " ORDER BY 1 DESC" +
               " LIMIT 1)";
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
        
      case 25: //1st Place
        Log.d(TAG, "Query for first place. Col2 is Score.");
        queryStr = "SELECT " + GameData.TurnScores.TEAM_ID + 
                           ", SUM(" + GameData.TurnScores.SCORE + ")" + " as FINAL_SCORE" + 
          " FROM " + GameData.TURN_SCORES_TABLE_NAME +
          " WHERE " + GameData.TurnScores.GAME_ID + "=" + gameID + 
          " ORDER BY " + GameData.TurnScores.SCORE + " DESC" + 
          " LIMIT 1";
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;

      case 26: //2nd Place
        Log.d(TAG, "Query for second place. Col2 is Score.");
        queryStr = "SELECT " + GameData.TurnScores.TEAM_ID + 
                           ", SUM(" + GameData.TurnScores.SCORE + ")" + " as FINAL_SCORE" + 
          " FROM " + GameData.TURN_SCORES_TABLE_NAME +
          " WHERE " + GameData.TurnScores.GAME_ID + "=" + gameID + 
          " ORDER BY " + GameData.TurnScores.SCORE + " DESC" + 
          " LIMIT 1 OFFSET 1";         
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
        
      case 27: //3rd Place
        Log.d(TAG, "Query for third place. Col2 is Score.");
        queryStr = "SELECT " + GameData.TurnScores.TEAM_ID + 
                           ", SUM(" + GameData.TurnScores.SCORE + ")" + " as FINAL_SCORE" + 
          " FROM " + GameData.TURN_SCORES_TABLE_NAME +
          " WHERE " + GameData.TurnScores.GAME_ID + "=" + gameID + 
          " ORDER BY " + GameData.TurnScores.SCORE + " DESC" + 
          " LIMIT 1 OFFSET 2";        
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
        
      case 28: //4th Place
        Log.d(TAG, "Query for fourth place. Col2 is Score.");
        queryStr = "SELECT " + GameData.TurnScores.TEAM_ID + 
                           ", SUM(" + GameData.TurnScores.SCORE + ")" + " as FINAL_SCORE" + 
          " FROM " + GameData.TURN_SCORES_TABLE_NAME +
          " WHERE " + GameData.TurnScores.GAME_ID + "=" + gameID + 
          " ORDER BY " + GameData.TurnScores.SCORE + " DESC" + 
          " LIMIT 1 OFFSET 3";        
        Log.d(TAG, queryStr);
        cursor = db.rawQuery(queryStr, null);
        break;
  	}
  	
  	for(int i=0; i<results.length; ++i)
  	{
  		for(int j=0; j<results[0].length; ++j)
  		{
  			results[i][j] = -1;
  		}
  	}
  	
  	// Set results array to the answers returned from sqlite query
  	int rownum = 0;
  	if( cursor.moveToFirst() && cursor.getColumnCount() > 0)
  	{
  	  // handle 1 row answers first
      Log.d(TAG,Integer.toString(rownum));
      results[rownum][0] =  Double.valueOf(cursor.getString(0)); 
      
      if (cursor.getColumnCount() == 2)
      {
        results[0][1] = Double.valueOf(cursor.getString(1));
      }            
      
      // handle multiple row answers by looping until end of cursor is reached
      while(cursor.moveToNext())
      {
        if( cursor.getColumnCount() < 0 )
        {
          continue;
        }
        rownum++;
        Log.d(TAG,Integer.toString(rownum));
        
        results[rownum][0] =  Double.valueOf(cursor.getString(0));
        
        if (cursor.getColumnCount() == 2)
        {
          results[0][1] = Double.valueOf(cursor.getString(1));
        }        
      }
  	}
        
    cursor.close();
    
  	return results;
  }
  
  /* (non-Javadoc)
   * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
   */
  @Override
  public void onCreate( SQLiteDatabase db )
  {
    Log.d( TAG, "onCreate()" );
    db.execSQL( "CREATE TABLE " + GameData.TEAM_TABLE_NAME + " (" +
                GameData.Teams._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GameData.Teams.NAME + " TEXT);" );
    db.execSQL( "CREATE TABLE " + GameData.GAME_TABLE_NAME + " (" +
                GameData.Games._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GameData.Games.TIME + " TEXT);" );
    db.execSQL( "CREATE TABLE " + GameData.TURN_SCORES_TABLE_NAME + " (" +
                GameData.TurnScores._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GameData.TurnScores.TEAM_ID + " INTEGER," +
                GameData.TurnScores.GAME_ID + " INTEGER," +
                GameData.TurnScores.ROUND + " INTEGER," +
                GameData.TurnScores.SCORE + " INTEGER);" );
    db.execSQL( "CREATE TABLE " + GameData.FINAL_SCORES_TABLE_NAME + " (" +
                GameData.FinalScores._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GameData.FinalScores.TEAM_ID + " INTEGER," +
                GameData.FinalScores.GAME_ID + " INTEGER," +
                GameData.FinalScores.SCORE + " INTEGER);");
    db.execSQL( "CREATE TABLE " + GameData.GAME_HISTORY_TABLE_NAME + " (" +
                GameData.GameHistory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GameData.GameHistory.TEAM_ID + " INTEGER," +
                GameData.GameHistory.GAME_ID + " INTEGER," +
                GameData.GameHistory.CARD_ID + " INTEGER," +
                GameData.GameHistory.TURN_SCORE_ID + " INTEGER," +
                GameData.GameHistory.RWS + " INTEGER," +
                GameData.GameHistory.TIME + " INTEGER);" );
    db.execSQL( "CREATE TABLE " + GameData.CARD_TABLE_NAME + " (" +
                GameData.Cards._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GameData.Cards.PACK_NAME + " TEXT," +
                GameData.Cards.TITLE + " TEXT," +
                GameData.Cards.BAD_WORDS + " TEXT," +
                GameData.Cards.CATEGORIES + " TEXT);" );

    InputStream starterXML =
      curContext.getResources().openRawResource(R.raw.starter);
    DocumentBuilderFactory docBuilderFactory =
      DocumentBuilderFactory.newInstance();
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
        Node categoriesNode = null;
        Node packNameNode = null;
        for( int j = 0; j < titleWhiteAndBads.getLength(); j++ )
        {
          String candidateName = titleWhiteAndBads.item( j ).getNodeName();
          if( candidateName.equals( "title" ) )
          {
            titleNode = titleWhiteAndBads.item( j );
          }
          else if( candidateName.equals( "pack-name" ) )
          {
            packNameNode = titleWhiteAndBads.item( j );
          }
          else if( candidateName.equals( "bad-words" ) )
          {
            badsNode = titleWhiteAndBads.item( j );
          }
          else if( candidateName.equals( "categories" ) )
          {
            categoriesNode = titleWhiteAndBads.item( j );
          }
          else
          {
            continue; // We found some #text
          }
        }
        String title = titleNode.getFirstChild().getNodeValue();
        String packName = packNameNode.getFirstChild().getNodeValue();
        String categories = categoriesNode.getFirstChild().getNodeValue();
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

        // use a hard-coded query for performance and readability
        db.execSQL( "INSERT INTO " + GameData.CARD_TABLE_NAME + " (" +
                    GameData.Cards.PACK_NAME + "," + GameData.Cards.TITLE  + ", " +
                    GameData.Cards.BAD_WORDS + ", " + GameData.Cards.CATEGORIES +
                    ") VALUES (\"" +
                    packName + "\",\"" +
                    title + "\",\"" +
                    badWords + "\",\"" +
                    categories + "\");" );
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

  /* (non-Javadoc)
   * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
   */
  @Override
  public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
  {
    Log.d( TAG, "onUpgrade()" );
    Log.w( TAG, "Upgrading database from version " + oldVersion + " to "
           + newVersion + ", which will destroy all old data" );
    db.execSQL( "DROP TABLE IF EXISTS" + GameData.TEAM_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS" + GameData.GAME_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS" + GameData.TURN_SCORES_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS" + GameData.FINAL_SCORES_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS" + GameData.GAME_HISTORY_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS" + GameData.CARD_TABLE_NAME + ";" );
    onCreate( db );
  }
  
}
