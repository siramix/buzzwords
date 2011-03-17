/**
 * 
 */
package com.taboozle.test;

import com.taboozle.Game;
import com.taboozle.GameData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;


/**
 * Unit tests for game.
 * @author The Taboozle Team
 *
 */
public class GameTest extends AndroidTestCase
{
  
  private Game game;
  private Context context;
  private SQLiteDatabase db;
  private final String testdbname = "taboozleGameTest.db";

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    context = this.getContext();
    game = new Game( context, testdbname );
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    db = game.getWritableDatabase();
    db.execSQL( "DROP TABLE IF EXISTS " + GameData.TEAM_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS " + GameData.GAME_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS " + GameData.TURN_SCORES_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS " + GameData.FINAL_SCORES_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS " + GameData.GAME_HISTORY_TABLE_NAME + ";" );
    db.execSQL( "DROP TABLE IF EXISTS " + GameData.CARD_TABLE_NAME + ";" );
    super.tearDown();
  }

  /**
   * Test method for {@link com.taboozle.Game#clearDeck()}.
   */
  public void testClearDeck()
  {
    game.prepDeck();
    game.getNextCard();
    game.clearDeck();
    assertEquals(game.getCardPosition(), -1);
    assertEquals(game.getDeck().size(), 0);
  }

  /**
   * Test method for {@link com.taboozle.Game#pruneDeck()}.
   */
  public void testPruneDeck()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#prepDeck()}.
   */
  public void testPrepDeck()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#getRoundScores(long, long)}.
   */
  public void testGetRoundScores()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#getNextCard()}.
   */
  public void testGetNextCard()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#getPreviousCard()}.
   */
  public void testGetPreviousCard()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#newGame()}.
   */
  public void testNewGame()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#newTeam(java.lang.String)}.
   */
  public void testNewTeam()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#completeGame(long, long, long)}.
   */
  public void testCompleteGame()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#newTurn(long, long, long, long)}.
   */
  public void testNewTurn()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#completeCard(long, long, long, long, long, long)}.
   */
  public void testCompleteCard()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#onCreate(android.database.sqlite.SQLiteDatabase)}.
   */
  public void testOnCreateSQLiteDatabase()
  {
    fail( "Not yet implemented" ); // TODO
  }

  /**
   * Test method for {@link com.taboozle.Game#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)}.
   */
  public void testOnUpgradeSQLiteDatabaseIntInt()
  {
    fail( "Not yet implemented" ); // TODO
  }

}
