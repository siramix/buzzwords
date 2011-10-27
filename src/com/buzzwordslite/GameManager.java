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
package com.buzzwordslite;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.buzzwordslite.R;

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
public class GameManager {
  /**
   * logging tag
   */
  public static String TAG = "GameManager";

  /**
   * The list of cardIds that we pull from (our "deck" of cards)
   */
  private Deck mDeck;

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
  private int mNumTurns;

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
   * An array of resource IDs to each right, wrong, skip sprite
   */
  public final int[] mRwsResourceIds;

  /**
   * Time for the Timer in milliseconds
   */
  private int mTurnTime;

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
    mRwsResourceIds = new int[] { R.drawable.right, R.drawable.wrong,
        R.drawable.skip };

    mTurnTime = Integer.parseInt(sp.getString("turn_timer", "60")) * 1000;

    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "Turn time is " + mTurnTime);
    }
    mRwsValueRules = new int[3];

    // Set score values for game
    mRwsValueRules[0] = 1; // Value for correct cards
    mRwsValueRules[1] = -1; // Value for wrong cards
    mRwsValueRules[2] = 0; // set skip value to 0 if skip penalty is not on

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
      mCurrentCard = mDeck.getCard();
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
   * @param rounds
   *          the number of rounds to play
   */
  public void startGame(List<Team> teams, int rounds) {
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
    mNumRounds = rounds;
    mNumTurns = mTeams.size() * mNumRounds;
    mCurrentTurn++;
    mDeck.prepareForRound();
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
    mDeck.prepareForRound();
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
    mDeck.prepareForRound();
    // clear current cards so that scoreboards don't add turn score in
    mCurrentCards.clear();
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

      // If the card's rws value is not set we default it to SKIP
      if(card.getRws() == Card.NOTSET)
      {
        card.setRws(Card.SKIP);
      }

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
   * Accessor to return the amount of time in each turn.
   * 
   * @return integer representing the number of miliseconds in each turn.
   */
  public int getTurnTime() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "GetTurnTime()");
    }
    return mTurnTime;
  }

  public int getNumberOfTurnsRemaining() {
    return mNumTurns - mCurrentTurn;
  }
}
