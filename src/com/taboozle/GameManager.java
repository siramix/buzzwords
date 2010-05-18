/**
 * 
 */
package com.taboozle;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;


/**
 * @author cpatrick
 *
 */
public class GameManager
{
  private class cardPair
  {
    public long id;
    public long rws;
  }

  private Context context;
  
  private Game game;
  private long currentGameId;
  private int activeTeamIndex;
  private long[] teamIds;
  private long currentTurnId;
  private long currentRound;
  private long currentCardId;
  
  private LinkedList<cardPair> currentCardPairs;
  
  private final long[] RWS_VALUE_RULES = {1,-1,0};
  
  public GameManager()
  {
    this.currentRound = 0;
    this.activeTeamIndex = 0;
    this.currentCardPairs = new LinkedList<cardPair>();
  }
  
  public void StartGame( Context context, String[] teams )
  {
    this.context = context;
    this.currentGameId = this.game.newGame();
    this.teamIds = new long[teams.length];
    for( int i = 0; i < teams.length; ++i )
    {
      this.teamIds[i] = game.newTeam( teams[i] );
    }
  }
  
  public void NextTurn( )
  {
   // game.newTurn( this.currentGameId, this.teamIds[this.activeTeamIndex], this.currentRound, score );
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
	  
	  // Which of these is a better implementation for iterating a linked list?
	  for ( int i = 0; i < this.currentCardPairs.size(); i++ )
	  {
		  scoreTotal += RWS_VALUE_RULES[(int)this.currentCardPairs.get(i).rws];
	  }
//	  for( Iterator<cardPair> it = currentCardPairs.iterator(); it.hasNext(); )
//	  {
//		 cardPair card = (cardPair) it.next();
//		 scoreTotal += RWS_VALUE_RULES[(int)card.rws];
//	  }
	  long currentTurnScoreID = game.newTurn( this.currentGameId, 
			  			this.teamIds[this.activeTeamIndex], this.currentRound, scoreTotal );
	  
	  for ( int i = 0; i < this.currentCardPairs.size(); i++ )
	  {
		  game.completeCard( this.currentGameId, this.teamIds[this.activeTeamIndex], 
				  this.currentCardId, currentTurnScoreID, this.currentCardPairs.get(i).rws);
	  }
  }
  
  public void ProcessCard( int rws )
  {
    cardPair curCardPair = new cardPair();
    curCardPair.id = this.currentCardId;
    curCardPair.rws = rws;
    this.currentCardPairs.add( curCardPair ); 
  }
  
  public String GetCurrentCardTitle()
  {
    return "";
  }
  
  public String[] GetCurrentCardBadWords()
  {
    return null;
  }
  
}
