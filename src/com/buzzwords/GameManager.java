/*****************************************************************************
 *  Buzzwords is a family friendly word game for mobile phones.
 *  Copyright (C) 2011 Siramix Team
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.buzzwords;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.buzzwords.Card;
import com.buzzwords.Pack;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Siramix Labs
 * 
 *         The Game Manager is a class that will manage all aspects of the game
 *         scoring and general bookkeeping. This is the go-to class for creating
 *         new games, turns, and teams. The application shall also use this
 *         class for preparing and retrieving cards from the virtual deck.
 */
/**
 * @author elrowe
 *
 */
public class GameManager {
  /**
   * logging tag
   */
  public static String TAG = "GameManager";

  /**
   * The list of cardIds that we pull from (our "deck" of cards)
   */
  private Deck mDeck;

  // Create a thread for updating the playcount for each card
  private Thread mUpdateThread;
  
  /**
   * The position in the list of card ids (where we are in the "deck")
   */
  private int mCardPosition;

  /**
   * List of team objects
   */
  private List<Team> mTeams;
  private Iterator<Team> mTeamIterator;
  private Team mCurrentTeam;

  /**
   * The maximum number of rounds for this game
   */
  private int mNumRounds;

  /**
   * The index of the round being played
   */
  private int mCurrentRound;

  /**
   * Number of turns to play
   */
  private int mTurnLimit;
  
  /**
   * Number of points to play to
   */
  private int mScoreLimit;
  
  /**
   * The current game's mode
   */
  private GameMode mCurrentGameMode;

  /**
   * Index of the current turn
   */
  private int mCurrentTurn;

  /**
   * The card in play
   */
  private Card mCurrentCard;

  /**
   * The set of cards that have been activated in the latest turn
   */
  private LinkedList<Card> mCurrentCards;

  /**
   * An array indicating scoring for right, wrong, and skip (in that order)
   */
  private int[] mRwsValueRules;

  /**
   * Time for the Timer in milliseconds
   */
  private int mTurnTime;
  
  /**
   * The various game modes that are selected in game setup
   */
  public static enum GameMode{
	  TURNS (0, "Turns"),
	  SCORE (1, "Score"), 
	  FREEPLAY (2, "Free Play");
	  
	  private int index;
	  private String name;
	  
	  GameMode(int index, String name)
	  {
		  this.index = index;
		  this.name = name;
	  }
	  
	  public int getIndex()
	  {
		  return index;		  
	  }
	  
	  public String getName()
	  {
		  return name;
	  }
  }

  /**
   * Standard Constructor
   * 
   * @param context
   *          required for game to instantiate the database
   */
  public GameManager(Context context) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GameManager()");
    }

    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(context);

    mCurrentRound = 0;
    mCurrentTurn = 0;
    mCardPosition = -1;
    mCurrentCards = new LinkedList<Card>();

    mTurnTime = Integer.valueOf(sp.getString(Consts.PREFKEY_TIMER, "60")) * 1000;

    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "Turn time is " + mTurnTime);
    }
    mRwsValueRules = new int[4];

    // Set score values for game
    // Value for correct cards
    mRwsValueRules[0] = Integer.valueOf(sp.getString(Consts.PREFKEY_RIGHT_SCORE, "1"));
    // Value for wrong cards
    mRwsValueRules[1] = Integer.valueOf(sp.getString(Consts.PREFKEY_WRONG_SCORE, "-1"));
    // Set skip value to 0 if skip penalty is not on
    mRwsValueRules[2] = Integer.valueOf(sp.getString(Consts.PREFKEY_SKIP_SCORE, "0"));
    // Value for NOTSET
    mRwsValueRules[3] = 0;
    mDeck = new Deck(context);
  }

  /**
   * Get the card indicated by the cardIdPosition. If we've dealt past the end
   * of the deck, we should prep the deck.
   * 
   * @return the card we want
   */
  public Card getNextCard() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getNextCard()");
    }
    ++mCardPosition;
    if(mCardPosition >= mCurrentCards.size()) {
      mCurrentCard = mDeck.dealCard();
      mCurrentCards.addLast(mCurrentCard);
    }
    else {
      mCurrentCard = mCurrentCards.get(mCardPosition);
    }
    return mCurrentCard;
  }

  /**
   * Return the previous card
   * 
   * @return the previous card in the deck
   */
  public Card getPreviousCard() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getPreviousCard()");
    }

    --mCardPosition;
    if (mCardPosition < 0) {
      mCardPosition = 0;
    }
    mCurrentCard = mCurrentCards.get(mCardPosition);
    return mCurrentCard;
  }

  /**
   * Start the game given a set of team names. This creates both a game and a
   * set of teams in the database
   * 
   * @param teams
   *          a string array of team names
   * @param mode
   * 		  GameMode for this game (play to score, or number of rounds)
   * @param modeInfo
   *          the number of rounds to play, or the points to play to
   */
  public void startGame(List<Team> teams, GameMode mode, int modeInfo) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "StartGame()");
    }
    mTeams = teams;
    Iterator<Team> itr = teams.iterator();
    for (itr = teams.iterator(); itr.hasNext();) {
      itr.next().setScore(0);
    }
    mTeamIterator = teams.iterator();
    mCurrentTeam = mTeamIterator.next();
    mCurrentGameMode = mode;
    switch (mCurrentGameMode)
    {
    	case TURNS:
    		mNumRounds = modeInfo;
    	    mTurnLimit = mTeams.size() * mNumRounds;
    		mScoreLimit = -1;
    		break;
    	case SCORE:
    		mNumRounds = -1;
    	    mTurnLimit = -1;
    		mScoreLimit = modeInfo;
    		break;
    	case FREEPLAY:
    		mNumRounds = -1;
    	    mTurnLimit = -1;
    		mScoreLimit = -1;
    		break;
    }
    mCurrentTurn++;
  }

  /**
   * Starts a new turn incrementing the round and/or team index as necessary.
   * This function also empties the collection of active cards.
   */
  public void nextTurn() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "NextTurn()");
    }
    this.incrementActiveTeamIndex();
    mCurrentCards.clear();
    mCardPosition = -1;
    mCurrentTurn++;
  }
  
  /*
   * Add the results of the current turn into the current team's score.
   */
  public void addTurnScore()
  {
	  int score = mCurrentTeam.getScore() + getTurnScore();
	  mCurrentTeam.setScore(score);
  }
  
  /*
   * Ammend turn score by the result of a given card.  This is used when a card 
   * is reviewed.
   */
  public void ammendCard(int changedCardIndex, int rws)
  {
	  int prevTurnScore = getTurnScore();
      Card curCard = mCurrentCards.get(changedCardIndex);
      curCard.setRws(rws);
      // new score is current score (which included previous turn score) plus
      // the difference
      int newScore = mCurrentTeam.getScore() + (getTurnScore() - prevTurnScore);
	  mCurrentTeam.setScore(newScore);
  }

  public void incrementActiveTeamIndex() {
    if (mTeamIterator.hasNext()) {
      mCurrentTeam = mTeamIterator.next();
    } else {
      mTeamIterator = mTeams.iterator();
      mCurrentTeam = mTeamIterator.next();
      mCurrentRound++;
    }
  }

  /**
   * Write turn and game relevant data to the database.
   */
  public void endGame() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "EndGame()");
    }
    mTeamIterator = mTeams.iterator();

    // clear current cards so that scoreboards don't add turn score in
    mCurrentCards.clear();
  }

  /**
   * Checks on the deck's caches to make sure enough cards have been
   * stored to play a turn.
   */
  public void maintainDeck() {
    Log.d(TAG, "maintainDeck()");
    mDeck.fillCachesIfLow();
  }

  
  /**
   * Call the Deck function that installs all 'starter' decks.  This
   * should only get called on first run.
   */
  public void installStarterPacks() {
    mDeck.installStarterPacks();
  }
  
  
  public void installPack(final Pack pack) {
    // TODO This should probably be in a thread (mInstallThread)
    // Though I ran into problems with the database state 
    try {
      mDeck.installPack(pack);
    } catch (RuntimeException e) {
      Log.e(TAG, "Unable to install pack: " + pack.getName());
      e.printStackTrace();
    }
  }
  
  /**
   * Attempt to remove the pack with _id == packId
   * @param packId the id of the pack to remove
   * @param removeDialog a dialog that is shown to users during removal
   */
  public void uninstallPack(final int packId) {
    // TODO This should probably be in a thread (mInstallThread)
    // Though I ran into problems with the database state
    try {
      mDeck.uninstallPack(packId);
    } catch (RuntimeException e) {
      Log.e(TAG, "Unable to install pack: " +String.valueOf(packId));
      e.printStackTrace();
    }
  }
  
  /**
   * The game manager will have the Deck update the play date for
   * any cards the Deck has marked as "seen".  Runs inside a thread.
   */
  public void updateSeenFields() {
    mUpdateThread = new Thread(new Runnable() {
      public void run() {
        mDeck.updateSeenFields();
        }
      });
    mUpdateThread.start();
  }
  
  /**
   * The game manager will have the Deck update the play date for
   * any cards passed into this method.  This is being used for
   * Turn Summary which will have a list of seen cards to pass in.
   * Runs inside a thread.
   * @param cardsToUpdate - A linked list of cards to update
   */
  public void updateSeenFields(final List<Card> cardsToUpdate) {
    mUpdateThread = new Thread(new Runnable() {
      public void run() {
        mDeck.updateSeenFields(cardsToUpdate);
        }
      });
    mUpdateThread.start();
  }
  
  /**
   * Adds the current card to the active cards
   * 
   * @param rws
   *          the right, wrong, skip status
   */
  public void processCard(int rws) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "ProcessCard(" + rws + ")");
    }
    mCurrentCard.setRws(rws);
  }

  /**
   * Return the card currently in play without moving through the deck
   * 
   * @return the card currently in play
   */
  public Card getCurrentCard() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetCurrentCard()");
    }
    return mCurrentCard;
  }

  /**
   * Get a list of all cards that have been acted on in a given turn.
   * 
   * @return list of all cards from the current turn
   */
  public LinkedList<Card> getCurrentCards() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetCurrentCards()");
    }
    return mCurrentCards;
  }

  /**
   * Queries the game manager to determine if the game should end rather than
   * advancing to another turn based on the current game mode.
   * 
   * @return true if the game should end instead of advancing to another turn
   */
  public boolean shouldGameEnd()
  {
    if( BuzzWordsApplication.DEBUG )
    {
      Log.d( TAG, "shouldGameEnd()" );
    }
    boolean ret = false;
    switch ( mCurrentGameMode )
    {
      case TURNS:
        ret = getNumberOfTurnsRemaining() == 0;
        break;
      case SCORE:
        Iterator<Team> itr = mTeams.iterator();
        boolean isScoreLimitReached = false;
        for( itr = mTeams.iterator(); itr.hasNext(); )
        {
          if( itr.next().getScore() >= mScoreLimit )
          {
            isScoreLimitReached = true;
            break;
          }
        }
        // When score limit is reached, make sure all teams have had
        // equal number of turns.
        if( isScoreLimitReached )
        {
          ret = mCurrentTurn % mTeams.size() == 0; 
        }
        else
        {
          ret = false;
        }
        break;
      case FREEPLAY:
        ret = false;
        break;
    }
    return ret;
  }

  /**
   * Iterate through through all cards for the current turn and return the total
   * score
   * 
   * @return score for the round
   */
  public int getTurnScore() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetTurnScore()");
    }
    int ret = 0;
    for (Iterator<Card> it = mCurrentCards.iterator(); it.hasNext();) {
      Card card = it.next();
      ret += mRwsValueRules[card.getRws()];
    }
    return ret;
  }

  /**
   * Return a list of the currently playing team objects
   * 
   * @return a list of the currently playing team objects
   */
  public List<Team> getTeams() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetTeams()");
    }
    return mTeams;
  }

  /**
   * Return a reference to the team currently playing
   * 
   * @return a reference to the team currently playing
   */
  public Team getActiveTeam() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetActiveTeamIndex()");
    }
    return mCurrentTeam;
  }

  /**
   * Return the number of teams set up by the game manager.
   * 
   * @return integer representing the number of teams ie. the length of
   *         teamIds[]
   */
  public int getNumTeams() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetNumTeams()");
    }
    return mTeams.size();
  }

  /**
   * Return the number of rounds that have fully taken place. We add one since
   * we, like good computer scientists, start counting at zero
   * 
   * @return int representing the number of rounds thus far in a game
   */
  public int getCurrentRound() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetCurrentRound()");
    }
    return mCurrentRound + 1;
  }

  /**
   * Return the maximum number of rounds in this game
   * 
   * @return the maximum number of rounds in this game
   */
  public int getNumRounds() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetNumRounds()");
    }
    return mNumRounds;
  }
  
  /**
   * Returns the point limit for the current game
   * 
   * @return the points a team needs to reach in order to win
   */
  public int getScoreLimit() {
    if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "GetNumRounds()");
      }	  
	  return mScoreLimit;
  }

  /**
   * Returns game limit, regardless of mode
   * 
   * @return the game limit, no matter what the mode. -1 for free play
   */
  public int getGameLimit()
  {
    int limit;
    if( mCurrentGameMode == GameMode.TURNS ) {
      limit = mTurnLimit;
    }
    else if( mCurrentGameMode == GameMode.SCORE ) {
      limit = mScoreLimit;
    }
    else {
      limit = -1;
    }
    return limit;
  }
  
  /**
   * Returns the game mode for the current game
   * 
   * @return the game mode for the current game
   */
  public GameMode getGameMode()
  {
    if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "getGameMode()");
      }
	  return mCurrentGameMode;
  }

  /**
   * Accessor to return the amount of time in each turn.
   * 
   * @return integer representing the number of milliseconds in each turn.
   */
  public int getTurnTime() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetTurnTime()");
    }
    return mTurnTime;
  }

  /**
   * Returns the number of remaining Turns in Turns GameMode.
   * If the GameMode is not Turns, it returns -1.
   * 
   * @returns the number of turns until the turn limit is reached
   */
  private int getNumberOfTurnsRemaining() {
    if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "GetNumberOfTurnsRemaining()");
      }
    
    if(mCurrentGameMode != GameMode.TURNS)
    	return -1;
    
    return mTurnLimit - mCurrentTurn;
  }

  /**
   * Return a Linked List of all Packs that are currently installed.
   * @return
   */
  public LinkedList<Pack> getInstalledPacks() {
    return mDeck.getLocalPacks();
  }
}
