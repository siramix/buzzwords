package com.taboozle;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author The Taboozle Team
 *
 * The Game Manager is a class that will manage all aspects of the game scoring
 * and general bookkeeping. This is the go-to class for creating new games,
 * turns, and teams. The application shall also use this class for preparing
 * and retrieving cards from the virtual deck.
 */
public class GameManager implements Serializable
{
  /**
   * logging tag
   */
  public static String TAG = "GameManager";
  
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * The game object used for database access
   */
  private final Game game;

  /**
   * The id of the game currently being played
   */
  private long currentGameId;

  /**
   * The position in the teamIds collection of the team currently doing the
   * guessing
   */
  private int activeTeamIndex;

  /**
   * A collection of teamIds indicating the teams that are currently playing
   * the game
   */
  private long[] teamIds;
  
  /**
   * An array of the team names
   */
  private String[] teamNames;

  /**
   * The maximum number of rounds for this game
   */
  private int numRounds;
  
  /**
   * The index of the round being played
   */
  private int currentRound;
  
  /**
   * Running total of scores
   */
  private long[] teamScores;

  /**
   * The id of the card in play
   */
  private Card currentCard;

  /**
   * The set of cards that have been activated in the latest turn
   */
  private LinkedList<Card> currentCards;

  /**
   * An array indicating scoring for right, wrong, and skip (in that order)
   */
  private long[] rws_value_rules;
  
  /**
   * An array of resource IDs to each right, wrong, skip sprite
   */
  public final int[] rws_resourceIDs;
  
  /**
   * Time for the Timer in miliseconds
   */
  private int turn_time;

  /**
   * Standard Constructor
   * @param context - required for game to instantiate the database
   */
  public GameManager( Context context )
  {
    Log.d( TAG, "GameManager()" );
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    
    this.currentRound = 0;
    this.activeTeamIndex = 0;
    this.currentCards = new LinkedList<Card>();
    this.game = new Game( context );
    this.rws_resourceIDs = new int[] {R.drawable.right, R.drawable.wrong, R.drawable.skip};
    
    this.turn_time = Integer.parseInt(sp.getString("turn_timer", "10")) * 1000;
    
    System.out.println("Turn time is " + turn_time );    
    this.rws_value_rules = new long[3];
    
    //Set score values for game
    this.rws_value_rules[0] = 1;  //Value for correct cards
    this.rws_value_rules[1] = -1; //Value for wrong cards
    this.rws_value_rules[2] = 0;  //set skip value to 0 if skip penalty is not on
  }

  /**
   * Start the game given a set of team names. This creates both a game and
   * a set of teams in the database
   * @param teams - a string array of team names
   */
  public void StartGame( String[] teams, int rounds )
  {
    Log.d( TAG, "StartGame()" );
    this.currentGameId = this.game.newGame();
    this.teamIds = new long[teams.length];
    this.teamScores = new long[teams.length]; 
    this.teamNames = teams.clone();
    this.numRounds = rounds;
    
    for( int i = 0; i < teams.length; ++i )
    {
      this.teamIds[i] = game.newTeam( teams[i] );
    }
  }

  /**
   * Starts a new turn incrementing the round and/or team index as necessary.
   * This function also empties the collection of active cards.
   */
  public void NextTurn()
  {
    Log.d( TAG, "NextTurn()" );
    this.WriteTurnResults();
    this.teamScores[this.activeTeamIndex] += GetTurnScore();
    this.activeTeamIndex++;
    if( this.activeTeamIndex == this.teamIds.length )
    {
      this.activeTeamIndex = 0;
      this.currentRound++;
    }
    this.currentCards = new LinkedList<Card>();
  }

  /**
   * Write turn and game relevant data to the database.
   */
  public void EndGame()
  {
    Log.d( TAG, "EndGame()" );
    this.WriteTurnResults();
    this.teamScores[this.activeTeamIndex] += GetTurnScore();
    this.WriteGameResults();
	this.activeTeamIndex = -1;
  }

  /**
   * Write the results of a turn to the database.  Totals the score of all
   * cards for a round, following any end-round modifications (if this is
   * allowed.)  Also enters the results for each card.
   */
  private void WriteTurnResults()
  {
    Log.d( TAG, "WriteTurnResults()" );
	  long scoreTotal = 0;

	  /* Iterate through cards and total the score for the round */
	  scoreTotal = this.GetTurnScore();

	  long currentTurnScoreID = game.newTurn( this.currentGameId,
	                                          this.teamIds[this.activeTeamIndex],
	                                          this.currentRound,
	                                          scoreTotal );

	  /* Once we've calculated the score and gotten the ID, write to DB */
	  for( Iterator<Card> it = currentCards.iterator(); it.hasNext(); )
	  {
	    Card card = it.next();
	    game.completeCard( this.currentGameId, this.teamIds[this.activeTeamIndex],
	                       card.getId(), currentTurnScoreID,
	                       card.getRws(), card.getTime() );
	  }
  }
  /**
   * Write the game results to the database.  Game results consist of an entry for
   * each team in the game, including their score and ID.
   */
  private void WriteGameResults()
  {
    Log.d( TAG, "WriteGameResults()" );   
    for( int i = 0; i < teamIds.length; i++ )
    {
      game.completeGame( this.currentGameId, this.teamIds[i], this.teamScores[i]);
    }
  }

  /**
   * Adds the current card to the active cards
   * @param rws - the right, wrong, skip status
   */
  public void ProcessCard( int rws )
  {
    Log.d( TAG, "ProcessCard()" );      
    this.currentCard.setRws( rws );
    this.currentCards.add( this.currentCard );
  }

  /**
   * Prepare the deck to be dealt. Essentially a pass-through call to Game.
   */
  public void PrepDeck()
  {
    Log.d( TAG, "PrepDeck()" );          
    this.game.prepDeck();
  }

  /**
   * Get the next card in our Game's "deck" and set a reference to it to
   * currentCard. This is the virtual equivalent of placing the taboozle card
   * on the staging area.
   * @return the card currently in play
   */
  public Card GetNextCard()
  {
    Log.d( TAG, "GetNextCard()" );              
    this.currentCard = this.game.getNextCard();
    return this.currentCard;
  }
  
  /**
   * Return the card currently in play without moving through the deck
   * @return the card currently in play
   */
  public Card GetCurrentCard()
  {
    Log.d( TAG, "GetCurrentCard()" );
    return this.currentCard;
  }

  /**
   * Get a list of all cards that have been acted on in a given turn.
   * @return list of all cards
   */
  public LinkedList<Card> GetCurrentCards()
  {   
    Log.d( TAG, "GetCurrentCards()" );              
	  return this.currentCards;
  }

  /**
   * Iterate through through all cards for the current turn and return the
   * total score
   * @return score for the round
   */
  public long GetTurnScore()
  {
    Log.d( TAG, "GetTurnScore()" );              
    long ret = 0;
	  for( Iterator<Card> it = currentCards.iterator(); it.hasNext(); )
	  {
	    Card card = it.next();
	    ret += rws_value_rules[card.getRws()];
	  }
	  return ret;
  }

  /**
   * Return an array of scores representing a running score total.
   * @return Array of longs with an element for each team's latest total score.
   */
  public long[] GetTeamScores()
  {
    Log.d( TAG, "GetTeamScores()" );                  
	  return this.teamScores;
  }

  /**
   * Return an array of scores for each round for a given team.
   * @return Array of longs with an element for the team's scores for every 
   * round, first to last.
   */
  public long[] GetRoundScores(long teamIndex)
  {
    Log.d( TAG, "GetRoundScores()" );                      
    return this.game.getRoundScores( this.teamIds[(int)teamIndex], 
                                     this.currentGameId );
  }
  
  /**
   * Return the index of the team currently in play.
   * @return integer representing the index of the current team starting at 0.
   */
  public int GetActiveTeamIndex()
  {
    Log.d( TAG, "GetActiveTeamIndex()" );                      
    return this.activeTeamIndex;
  }
  
  /**
   * Return the number of teams set up by the game manager.
   * @return integer representing the number of teams ie. the length of 
   * teamIds[]
   */
  public int GetNumTeams()
  {
    Log.d( TAG, "GetNumTeams()" );                          
	  return this.teamIds.length;
  }
  
  /**
   * Return the number of rounds that have fully taken place
   * @return int representing the number of rounds thusfar in a game 
   */
  public int GetCurrentRound()
  {
    Log.d( TAG, "GetCurrentRound()" );                          
    return this.currentRound+1;
  }

  /**
   * Return the maximum number of rounds in this game
   * @return int representing the maximum number of rounds in this game
   */
  public int GetNumRounds()
  {
    Log.d( TAG, "GetNumRounds()" );                          
    return this.numRounds;
  }
  
  /**
   * Return the number of turns still left to play
   * @return int representing number of turns before max rounds reached
   */
  public int GetNumTurnsRemaining()
  {
    Log.d( TAG, "GetNumTurnsRemaining()" );                          
    return ((this.numRounds - this.currentRound) * this.teamIds.length) - (this.activeTeamIndex + 1);
  }
  
  /**
   * Accessor to return teamIDs which are the IDs stored in the database for each team.
   * @return array of longs representing each team's unique ID as stored in the db
   */
  public long[] GetTeamIDs()
  {
    Log.d( TAG, "GetTeamIDs()" );    
	  return this.teamIds;
  }
  
  
  /**
   * Returns a string array of the team names.
   */
  public String[] GetTeamNames()
  {
    Log.d( TAG, "GetTeamNames()" );    
	  return this.teamNames;
  }
  
  /**
   * Accessor to return the amount of time in each turn.
   * @return integer representing the number of miliseconds in each turn.
   */
  public int GetTurnTime()
  {
    Log.d( TAG, "GetTurnTime()" );    
    return this.turn_time;
  }
  
  /**
   * Return the current team name
   * @return string for the current team name
   */
  public String GetActiveTeamName()
  {
    Log.d( TAG, "GetActiveTeamName()" );
    return this.teamNames[this.activeTeamIndex];
  }
  
  /**
   * Accessor for the game ID for the game in progress.
   * @return long representing the database ID of the game in progress.
   */
  public long GetGameId()
  {
    Log.d( TAG, "GetGameId()" );
    return this.currentGameId;
  }
  
  /**
   * Game method that calls the database methods in Game and passes 
   * on it's results.
   * 
   * @param awardID The ID of the award to be queried for.  IDs map to values in the XML constants.
   * @param gameID The ID of the game 
   * @return
   */
  public double[][] awardsQuery( int awardID, long gameID)
  {
    Log.d( TAG, "awardsQuery(" + awardID + ", " + gameID + ")" );
    return this.game.awardsQuery(awardID, gameID);
  }
}
