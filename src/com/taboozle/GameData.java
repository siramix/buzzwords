package com.taboozle;

import android.provider.BaseColumns;

/**
 * Naming class for database
 */
public final class GameData
{

  // This class cannot be instantiated
  private GameData()
  {
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
    public static final String INDEX = "index";
    
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
    
  }
}
