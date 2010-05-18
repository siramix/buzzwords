/**
 * 
 */
package com.taboozle;

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
  
  public void DoTurn( long score )
  {
    game.newTurn( this.currentGameId, this.teamIds[this.activeTeamIndex], this.currentRound, score );
    this.activeTeamIndex++;
    if( this.activeTeamIndex == this.teamIds.length )
    {
      this.activeTeamIndex = 0;
      this.currentRound++;
    }
    this.currentCardPairs = new LinkedList<cardPair>();
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
