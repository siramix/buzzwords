/**
 * 
 */
package com.taboozle;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;

/**
 * @author The Taboozle Team
 */
public class GameManager
{
  private class cardPair
  {
    public long id;
    public long rws;
  }
  
  private Game game;
  private long currentGameId;
  private int activeTeamIndex;
  private long[] teamIds;
  private long currentRound;
  private long currentCardId;
  
  private LinkedList<cardPair> currentCardPairs;
  
  private final long[] RWS_VALUE_RULES = {1,-1,0};
  
  public GameManager( Context context )
  {
    this.currentRound = 0;
    this.activeTeamIndex = 0;
    this.currentCardPairs = new LinkedList<cardPair>();
    this.game = new Game( context );
  }
  
  public void StartGame( String[] teams )
  {
    this.currentGameId = this.game.newGame();
    this.teamIds = new long[teams.length];
    for( int i = 0; i < teams.length; ++i )
    {
      this.teamIds[i] = game.newTeam( teams[i] );
    }
  }
  
  public void NextTurn( )
  {
    this.WriteTurnResults();
    this.activeTeamIndex++;
    if( this.activeTeamIndex == this.teamIds.length )
    {
      this.activeTeamIndex = 0;
      this.currentRound++;
    }
    this.currentCardPairs = new LinkedList<cardPair>();
  }
  
  /*
   * Write the results of a turn to the database.  Totals the score of all cards for a round, following
   * any end-round modifications (if this is allowed.)  Also enters the results for each card.
   */
  private void WriteTurnResults( )
  {
	  long scoreTotal = 0;
	  
	  for( Iterator<cardPair> it = currentCardPairs.iterator(); it.hasNext(); )
	  {
	    cardPair card = (cardPair) it.next();
	    scoreTotal += RWS_VALUE_RULES[(int)card.rws];
	  }
	  
	  long currentTurnScoreID = game.newTurn( this.currentGameId, 
	                                          this.teamIds[this.activeTeamIndex], 
	                                          this.currentRound, 
	                                          scoreTotal );
	  
	  for( Iterator<cardPair> it = currentCardPairs.iterator(); it.hasNext(); )
    {
	    cardPair card = (cardPair) it.next();
	    game.completeCard( this.currentGameId, this.teamIds[this.activeTeamIndex], 
	                       card.id, currentTurnScoreID, 
	                       card.rws);
	  }
  }
  
  public void ProcessCard( int rws )
  {
    cardPair curCardPair = new cardPair();
    curCardPair.id = this.currentCardId;
    curCardPair.rws = rws;
    this.currentCardPairs.add( curCardPair ); 
  }
  
  public void PrepDeck()
  {
    this.game.prepDeck();
  }
  
  public Card GetNextCard()
  {
    return this.game.getNextCard();
  }
  
}
