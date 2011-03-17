package com.taboozle;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
  private int currentGameId;

  /**
   * List of team objects
   */
  private List<Team> teams; 
  private Iterator<Team> teamIterator;
  private Team currentTeam;
  
  /**
   * The maximum number of rounds for this game
   */
  private int numRounds;
  
  /**
   * The index of the round being played
   */
  private int currentRound;
  
  private int numTurns;
  private int currentTurn;

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
  private int[] rws_value_rules;
  
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
    this.currentTurn = 0;
    this.currentCards = new LinkedList<Card>();
    this.game = new Game( context );
    this.rws_resourceIDs = new int[] {R.drawable.right, R.drawable.wrong, R.drawable.skip};
    
    this.turn_time = Integer.parseInt(sp.getString("turn_timer", "10")) * 1000;
    
    System.out.println("Turn time is " + turn_time );    
    this.rws_value_rules = new int[3];
    
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
  public void StartGame( List<Team> teams, int rounds )
  {
    Log.d( TAG, "StartGame()" );
    this.currentGameId = this.game.newGame();
    this.teams = teams;
    Iterator<Team> itr = teams.iterator();
    for(itr = teams.iterator(); itr.hasNext();)
    {
      itr.next().setScore( 0 );
    }
    this.teamIterator = teams.iterator();
    this.currentTeam = teamIterator.next();
    this.numRounds = rounds;
    this.numTurns = this.teams.size()*this.numRounds;
    this.currentTurn++;
    this.game.clearDeck();
  }

  /**
   * Starts a new turn incrementing the round and/or team index as necessary.
   * This function also empties the collection of active cards.
   */
  public void NextTurn()
  {
    Log.d( TAG, "NextTurn()" );
    this.WriteTurnResults();
    int score = this.currentTeam.getScore() + GetTurnScore();
    this.currentTeam.setScore( score );
    this.incrementActiveTeamIndex();
    this.currentCards = new LinkedList<Card>();
    this.currentTurn++;
  }
  
  public void incrementActiveTeamIndex()
  {
    if( this.teamIterator.hasNext() )
    {
      this.currentTeam = this.teamIterator.next();
    }
    else
    {
      this.teamIterator = this.teams.iterator();
      this.currentTeam = this.teamIterator.next();
      this.currentRound++;
    }
  }

  /**
   * Write turn and game relevant data to the database.
   */
  public void EndGame()
  {
    Log.d( TAG, "EndGame()" );
    this.WriteTurnResults();
    int score = this.currentTeam.getScore() + GetTurnScore();
    this.currentTeam.setScore( score );
    this.WriteGameResults();
    this.teamIterator = this.teams.iterator();
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
	                                          this.currentTeam.ordinal(),
	                                          this.currentRound,
	                                          scoreTotal );

	  /* Once we've calculated the score and gotten the ID, write to DB */
	  for( Iterator<Card> it = currentCards.iterator(); it.hasNext(); )
	  {
	    Card card = it.next();
	    game.completeCard( this.currentGameId, this.currentTeam.ordinal(),
	                       card.getId(), currentTurnScoreID,
	                       card.getRws(), card.getTime() );
	  }
	  this.game.pruneDeck();
  }
  /**
   * Write the game results to the database.  Game results consist of an entry for
   * each team in the game, including their score and ID.
   */
  private void WriteGameResults()
  {
    Log.d( TAG, "WriteGameResults()" );
    Iterator<Team> iterator = this.teams.iterator();
    while( iterator.hasNext() )
    {
      Team cur = iterator.next();
      game.completeGame( this.currentGameId, cur.ordinal(), cur.getScore());
    }
  }

  /**
   * Adds the current card to the active cards
   * @param rws - the right, wrong, skip status
   */
  public void ProcessCard( int rws )
  {
    Log.d( TAG, "ProcessCard(" + rws + ")" );      
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
   * Get the previous card. This is like going back to a previously called
   * card.
   * @return the previously played card
   */
  public Card GetPreviousCard()
  {
    Log.d( TAG, "GetPreviousCard()" );
    this.currentCard = this.game.getPreviousCard();
    if( !this.currentCards.isEmpty() )
    {
      this.currentCards.removeLast();
    }
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
  public int GetTurnScore()
  {
    Log.d( TAG, "GetTurnScore()" );              
    int ret = 0;
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
  public List<Team> GetTeams()
  {
    Log.d( TAG, "GetTeams()" );                  
	  return this.teams;
  }

  /**
   * Return an array of scores for each round for a given team.
   * @return Array of longs with an element for the team's scores for every 
   * round, first to last.
   */
  public int[] GetRoundScores(Team team)
  {
    Log.d( TAG, "GetRoundScores()" );                      
    return this.game.getRoundScores( team.ordinal(), 
                                     this.currentGameId );
  }
  
  /**
   * Return the index of the team currently in play.
   * @return integer representing the index of the current team starting at 0.
   */
  public Team GetActiveTeam()
  {
    Log.d( TAG, "GetActiveTeamIndex()" );                      
    return this.currentTeam;
  }
  
  /**
   * Return the number of teams set up by the game manager.
   * @return integer representing the number of teams ie. the length of 
   * teamIds[]
   */
  public int GetNumTeams()
  {
    Log.d( TAG, "GetNumTeams()" );                          
	  return this.teams.size();
  }
  
  /**
   * Return the number of rounds that have fully taken place
   * @return int representing the number of rounds thus far in a game 
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
   * Accessor to return the amount of time in each turn.
   * @return integer representing the number of miliseconds in each turn.
   */
  public int GetTurnTime()
  {
    Log.d( TAG, "GetTurnTime()" );    
    return this.turn_time;
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
  
  public int GetNumberOfTurnsRemaining()
  {
    return this.numTurns-this.currentTurn;
  }  
}
