/**
 * 
 */
package com.taboozle;

import java.util.Date;

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
  
  public Game( Context context )
  {
    super( context, GameData.DATABASE_NAME, null, 
           GameData.DATABASE_VERSION );
  }
  
  /**
   * 
   */
  public int newGame()
  {
    Date currentTime = new Date();
    String dateString = currentTime.toString();    
    SQLiteDatabase db = this.getWritableDatabase();    
    db.execSQL( "INSERT INTO " + GameData.GAME_TABLE_NAME + " (" +
                GameData.Games.TIME + ") VALUES (\"" +
                dateString + "\");" );
    return 0;
  }
  
  /**
   * 
   * @param name
   * @return
   */
  public int newTeam( String name )
  {
    return 0;
  }
  
  /**
   * 
   * @param gameId
   * @param teamId
   * @param index
   * @return
   */
  public int newTurn( int gameId, int teamId, int index )
  {
    return 0;
  }
  
  /**
   * 
   * @param turnScoreId
   * @param score
   * @return
   */
  public int endTurn( int turnScoreId, int score )
  {
    return 0;
  }
  
  /**
   * 
   * @param gameId
   * @param teamId
   * @param cardId
   * @param turnScoreId
   * @param rws
   */
  public void completeCard( int gameId, int teamId, int cardId, 
                            int turnScoreId, int rws )
  {
    String strTeamId = Integer.toString( teamId );
    String strGameId = Integer.toString( gameId );
    String strCardId = Integer.toString( cardId );
    String strTurnScoreId = Integer.toString( turnScoreId );
    String strRWS = Integer.toString( rws );
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
    onCreate( db );
  }

}
