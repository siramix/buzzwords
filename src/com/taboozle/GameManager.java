package com.taboozle;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;

/**
 * @author The Taboozle Team
 * 
 * The Game Manager is a class that will manage all aspects of the game scoring
 * and general bookkeeping. This is the go-to class for creating new games,
 * turns, and teams. The application shall also use this class for preparing
 * and retrieving cards from the virtual deck.
 */
public class GameManager
{
  /**
   * The game object used for database access
   */
  private Game game;
  
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
   * The index of the round being played
   */
  private long currentRound;
  
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
  private final long[] RWS_VALUE_RULES = {1,-1,0};
  
  /**
   * Standard Constructor
   * @param context - required for game to instantiate the database
   */
  public GameManager( Context context )
  {
    this.currentRound = 0;
    this.activeTeamIndex = 0;
    this.currentCards = new LinkedList<Card>();
    this.game = new Game( context );
  }
  
  /**
   * Start the game given a set of team names. This creates both a game and
   * a set of teams in the database
   * @param teams - a string array of team names
   */
  public void StartGame( String[] teams )
  {
    this.currentGameId = this.game.newGame();
    this.teamIds = new long[teams.length];
    this.teamScores = new long[teams.length];
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
    this.WriteTurnResults();
    this.teamScores[this.activeTeamIndex] += GetTurnScore();
    this.WriteGameResults();
  }
  
  /**
   * Write the results of a turn to the database.  Totals the score of all 
   * cards for a round, following any end-round modifications (if this is 
   * allowed.)  Also enters the results for each card.
   */
  private void WriteTurnResults()
  {
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
	    Card card = (Card) it.next();
	    game.completeCard( this.currentGameId, this.teamIds[this.activeTeamIndex], 
	                       card.getId(), currentTurnScoreID, 
	                       card.getRws());
	  }
  }
  /**
   * Write the game results to the database.  Game results consist of an entry for
   * each team in the game, including their score and ID.
   */
  private void WriteGameResults()
  {
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
    this.currentCard.setRws( rws );
    this.currentCards.add( this.currentCard );
  }
  
  /**
   * Prepare the deck to be dealt. Essentially a pass-through call to Game.
   */
  public void PrepDeck()
  {
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
    this.currentCard = this.game.getNextCard();
    return this.currentCard;
  }
  
  /**
   * Get a list of all cards that have been acted on in a given turn.
   * @return list of all cards 
   */
  public LinkedList<Card> GetCurrentCards()
  {
	  return this.currentCards;
  }
  
  /**
   * Iterate through through all cards for the current turn and return the 
   * total score
   * @return score for the round 
   */
  public long GetTurnScore()
  {
	  long ret = 0;
	  for( Iterator<Card> it = currentCards.iterator(); it.hasNext(); )
	  {
	    Card card = (Card) it.next();
	    ret += RWS_VALUE_RULES[(int)card.getRws()];
	  }
	  return ret;
  }
  
  /**
   * Return an array of scores representing a running score total.
   * @return Array of longs with an element for each team's latest total score.
   */
  public long[] GetTeamScores()
  {
	  return this.teamScores;
  }

  /**
   * Return an array of scores for each round for a given team.
   * @return Array of longs with an element for the team's scores for every round, first to last.
   */
  public long[] GetRoundScores(long teamIndex)
  {
	  return this.game.getRoundScores(this.teamIds[(int)teamIndex], this.currentGameId);
  }
  
  /**
   * Return the index of the team currently in play.
   * @return integer representing the index of the current team starting at 0.
   */
  public int GetActiveTeamIndex()
  {
	  return this.activeTeamIndex;
  }
  
  /**
   * Return the number of teams set up by the game manager.
   * @return integer representing the number of teams ie. the length of teamIds[]
   */
  public int GetNumTeams()
  {
	  return this.teamIds.length;
  }
}
