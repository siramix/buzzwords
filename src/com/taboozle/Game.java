/**
 * 
 */
package com.taboozle;

import java.io.IOException;
import java.io.InputStream;
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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author taboozle team
 *
 */
public class Game extends SQLiteOpenHelper
{

  /*
   * class constants
   */
  public static final String TAG = "Game";
  
  private Context curContext;
  
  public Game( Context context )
  {
    super( context, GameData.DATABASE_NAME, null, 
           GameData.DATABASE_VERSION );
    this.curContext = context;
  }
  
  /**
   * 
   */
  public long newGame()
  {
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
   * 
   * @param name
   * @return
   */
  public long newTeam( String name )
  {
    ContentValues values = new ContentValues();
    values.put(GameData.Teams.NAME, name);
    SQLiteDatabase db = this.getWritableDatabase();
    return db.insert(GameData.TEAM_TABLE_NAME, "", values);
  }
  
  /**
   * 
   * @param gameId
   * @param teamId
   * @param index
   * @return
   */
  public long newTurn( long gameId, long teamId, long round, long score )
  {
    ContentValues values = new ContentValues();
    values.put( GameData.TurnScores.GAME_ID, gameId );
    values.put( GameData.TurnScores.TEAM_ID, teamId );
    values.put( GameData.TurnScores.ROUND, round );
    values.put( GameData.TurnScores.SCORE, score );
    SQLiteDatabase db = this.getWritableDatabase();
    return db.insert(GameData.TURN_SCORES_TABLE_NAME, "", values);
  }
  
  /**
   * 
   * @param gameId
   * @param teamId
   * @param cardId
   * @param turnScoreId
   * @param rws
   */
  public void completeCard( long gameId, long teamId, long cardId, 
		  					long turnScoreId, long rws )
  {
    String strTeamId = Long.toString( teamId );
    String strGameId = Long.toString( gameId );
    String strCardId = Long.toString( cardId );
    String strTurnScoreId = Long.toString( turnScoreId );
    String strRWS = Long.toString( rws );
    SQLiteDatabase db = this.getWritableDatabase();    
    db.execSQL( "INSERT INTO " + GameData.GAME_HISTORY_TABLE_NAME + " (" +
                GameData.GameHistory.TEAM_ID + "," +
                GameData.GameHistory.GAME_ID + "," +
                GameData.GameHistory.CARD_ID + "," +
                GameData.GameHistory.TURN_SCORE_ID + "," +
                GameData.GameHistory.RWS + ") VALUES (\"" +
                strTeamId + "\",\"" +
                strGameId + "\",\"" +
                strCardId + "\",\"" +
                strTurnScoreId + "\",\"" +
                strRWS + "\");" ); 
  }

  /* (non-Javadoc)
   * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
   */
  @Override
  public void onCreate( SQLiteDatabase db )
  {
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
                GameData.GameHistory.RWS + " INTEGER);" );
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
