package com.taboozle;

import android.provider.BaseColumns;

/**
 * Naming class for database
 */
public final class GameData
{
  
  public static final String DATABASE_NAME = "taboozle.db";
  public static final int DATABASE_VERSION = 2;
  public static final String CARD_TABLE_NAME = "cards";
  public static final String TEAM_TABLE_NAME = "teams";
  public static final String GAME_TABLE_NAME = "games";
  public static final String TURN_SCORES_TABLE_NAME = "turnscores";
  public static final String FINAL_SCORES_TABLE_NAME = "finalscores";
  public static final String GAME_HISTORY_TABLE_NAME = "gamehistory";
  public static final int RIGHT = 0;
  public static final int WRONG = 1;
  public static final int SKIP = 2;
  
  // This class cannot be instantiated
  private GameData()
  {
  }

  public static final class Cards implements BaseColumns
  {

    // This class cannot be instantiated
    private Cards()
    {
    }

    /**
     * The title of the card
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String TITLE = "title";
    
    /**
     * name of the pack that it comes from 
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String PACK_NAME = "pack_name";

    /**
     * The words the user cannot say when describing the card
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String BAD_WORDS = "badwords";
    
    /**
     * The categories the word falls into
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String CATEGORIES = "categories";
  }
  
  /**
   * Team Table
   */
  public static final class Teams implements BaseColumns
  {

    // This class cannot be instantiated
    private Teams()
    {
    }

    /**
     * Name of the team
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String NAME = "name";    
  }
  
  /**
   * Games Table
   */
  public static final class Games implements BaseColumns
  {

    // This class cannot be instantiated
    private Games()
    {
    }

    /**
     * The time that the game occurred
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String TIME = "time";    
  }
  
  /**
   * TurnScores Table
   */
  public static final class TurnScores implements BaseColumns
  {

    // This class cannot be instantiated
    private TurnScores()
    {
    }

    /**
     * The id of the team that corresponds to the TurnScore
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String TEAM_ID = "team_id";
    
    /**
     * The id of the game that corresponds to the TurnScore
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String GAME_ID = "game_id";
    
    /**
     * Which turn is it
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String ROUND = "round";
    
    /**
     * The number of points
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String SCORE = "score";
    
  }
  
  /**
   * FinalScores Table
   */
  public static final class FinalScores implements BaseColumns
  {

    // This class cannot be instantiated
    private FinalScores()
    {
    }

    /**
     * The id of the team that corresponds to the TurnScore
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String TEAM_ID = "team_id";
    
    /**
     * The id of the game that corresponds to the TurnScore
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String GAME_ID = "game_id";
    
    /**
     * The number of points
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String SCORE = "score";
    
  }
  
  /**
   * FinalScores Table
   */
  public static final class GameHistory implements BaseColumns
  {

    // This class cannot be instantiated
    private GameHistory()
    {
    }

    /**
     * The id of the team that corresponds to the GameHistory
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String TEAM_ID = "team_id";
    
    /**
     * The id of the game that corresponds to the GameHistory
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String GAME_ID = "game_id";
    
    /**
     * The id of the card that corresponds to the GameHistory
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String CARD_ID = "card_id";
    
    /**
     * The id of the TurnScore that corresponds to the GameHistory
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String TURN_SCORE_ID = "turn_score_id";
    
    /**
     * Right 0, Wrong 1, Skip 2 
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String RWS = "rws";
    
    /**
     * The number of milliseconds taken for the card to be completed
     * <P>
     * Type: INTEGER
     */
    public static final String TIME = "time";
    
  }
}
