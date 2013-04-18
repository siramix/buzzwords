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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.buzzwords.Card;
import com.buzzwords.Pack;
import com.buzzwords.Consts;
import com.buzzwords.Deck;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The Deck represents the stack of all cards in the game. We interact with a
 * SQLite database that stores the cards and intelligently caches them at the
 * beginning of the game and replenishes the cache at the beginning of each
 * turn. Each instance of Buzzwords contains the same database; however the
 * sorted order is determined using a pseudo-random variable. This variable is
 * determined on the first run of the game using the Java Random class and has a
 * something to do with cpuid, system time, etc. The variable, called mSeed, is
 * used to shuffle the deck with a generating function such that the sort can be
 * repeated on every subsequent load of the game. Thus, we can store only on the
 * seed and an offset to preserve the sorted order of the players' deck. We also
 * save mCache to prevent any unnecessary card loss between sessions.
 * 
 * @author Siramix Labs
 */
public class Deck implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -1730611844942143988L;
  private static final String TAG = "Deck";

  // This is the sum of all Cards in selected Packs
  private int mTotalPlayableCards;
  
  // A list of Cards in memory. Will be filled when <= ~ a turn of cards.
  private LinkedList<Card> mCache;
  
  // Tracks seen cards. Should be wiped every time the cache is refreshed.  
  private LinkedList<Card> mDiscardPile;
  
  // List of all packs selected for the Deck
  private LinkedList<Pack> mSelectedPacks;

  private Pack mStarterPack;
  //TODO Swap this code in when cutting a release build
  //private Pack mStarterPack2;
  
  /**
   * Constructor
   * 
   * @param context
   *          The Context within which to work, used to create the DB
   */
  public Deck(Context context) {

    mCache = new LinkedList<Card>();
    mDiscardPile = new LinkedList<Card>();
    mSelectedPacks = new LinkedList<Pack>();

    // Lite version code
    mStarterPack = new Pack(0, "Lite Pack", "packs/lite_pack.json", "packs/icons/packicon_classic1.png",
        "A sample pack of 125 Buzzwords.", 125, PackPurchaseConsts.PACKTYPE_FREE, 0, true, "");
    /*
    // TODO Swap this code in when cutting release builds of full version.
    // Full version code
    mStarterPack = new Pack(1, "Buzzwords I", "packs/buzzwords_i.json", "packs/icons/packicon_classic1.png",
        "The first of two pre-installed packs that include a mix of words from every catagory.", 
        500, PackPurchaseConsts.PACKTYPE_FREE, 0, true, "");
    mStarterPack2 = new Pack(2, "Buzzwords II", "packs/buzzwords_ii.json", "packs/icons/packicon_classic2.png",
        "The second of two pre-installed packs that include a mix of words from every catagory.", 
        500, PackPurchaseConsts.PACKTYPE_FREE, 0, true, "");
    */
  }

  public void saveState(Context context) {
    try {
      //use buffering
      OutputStream file = context.openFileOutput(Consts.DECK_TEMP_FILE, Context.MODE_PRIVATE);
      OutputStream buffer = new BufferedOutputStream(file);
      ObjectOutput output = new ObjectOutputStream(buffer);
      try {
        output.writeObject(this);
      }
      finally{
        output.close();
      }
    }  
    catch(IOException ex){
      SafeLog.d(TAG, "IOException while saving Deck state.");
    }
  }
  
  public static Deck restoreState(Context context) {
    Deck savedDeck = null;
    try {
      //use buffering
      InputStream file = context.openFileInput(Consts.DECK_TEMP_FILE);
      InputStream buffer = new BufferedInputStream( file );
      ObjectInput input = new ObjectInputStream ( buffer );
      try {
        savedDeck = (Deck) input.readObject();
      }
      finally{
        input.close();
      }
    }
    catch(ClassNotFoundException ex) {
      SafeLog.d(TAG, "ClassNotFoundException while restoring deck.");
    }
    catch(IOException ex) {
      SafeLog.d(TAG, "IOException while restoring deck.");
    }
    return savedDeck;
  }

  /**
   * Get a card from the top of the Cache queue.  Once
   * this reaches the bottom of the deck, we should top off the Deck which
   * will in turn trigger a pull from packs to refill the cache.
   * @param context the context for the db helper
   * @return a card reference
   */
  public Card dealCard(Context context) {
    Card ret;
    // This shouldn't happen unless a lot of cards are played in one turn (CACHE_TURNSIZE)
    if (mCache.isEmpty()) {
      SafeLog.d(TAG, "Filling entire cache mid-turn. This is expensive.");
      // We must mark seen now so that we won't re-pull these same cards during fillCache
      updateSeenFields(context);
      this.fillCache(context);
    }
    ret = mCache.removeFirst();
    mDiscardPile.add(ret);
    return ret;
  }
  
  /**
   * If there aren't enough cards in the cache to play one more turn,
   * clear it and fill it back up. This should be called during downtime
   * since it could be a costly database pull.
   * @param context the context for the db helper
   */
  public void fillCacheIfLow(Context context) {
    if (mCache.size() < Consts.CACHE_TURNSIZE) {
      SafeLog.d(TAG, "Cache size was low (" + mCache.size() + "), filling...");
      mCache.clear();
      fillCache(context);
      SafeLog.d(TAG, "filled. Cache size is now " + mCache.size());
    }
  }
  
  /**
   * Updates the playdate and times_seen for only the seen cards.  This
   * should be called when we pause the game or a turn ends.
   * @param context the context for the db helper
   */
  public void updateSeenFields(Context context) {
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    helper.updateSeenFields(mDiscardPile);
    mDiscardPile.clear();
  }

  
  /**
   * Retrieve a Linked List of all Packs that a user has installed in their database.
   * @param the context for the db helper
   * @return Linked List of all local Packs
   */
  public LinkedList<Pack> getLocalPacks(Context context) {
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    LinkedList<Pack> localPacks = helper.getAllPacksFromDB();
    for (Pack pack : localPacks) {
      pack.setSize(helper.countCards(pack));
      pack.setNumCardsSeen(helper.countNumSeen(pack));
    }
    return localPacks;
  }
  
  /**
   * Take a Pack object and pull in cards from the server into the database. 
   * @param pack
   * @throws RuntimeException 
   */
  public void installLatestPack(Pack pack, Context context) throws RuntimeException {
    // If pack is out of date, delete the icon and get the new
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    int packState = helper.packInstalled(pack.getId(), pack.getVersion());
    if (packState == pack.getId()) {
      PackIconUtils.deleteIcon(pack.getIconName(), context);
    } else if (packState == Consts.PACK_NOT_PRESENT) {
      setPackSelectionPref(pack.getId(), true, context);
    }
    
    helper.installLatestPackFromServer(pack);
  }
  
  /**
   * Install all of the packs that the app comes with.  This ultimately
   * will be just one pack.
   * @param context the context in which to set the pack selection preference
   * @throws RuntimeException 
   */
  public synchronized void installStarterPacks(Context context) throws RuntimeException {
    // Lite code
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    helper.installStarterPacks(mStarterPack, context);
    setPackSelectionPref(mStarterPack.getId(), true, context);
    
    /* TODO Swap this code in when cutting a release build
    // Full version code
    mDatabaseOpenHelper.installStarterPacks(mStarterPack);
    mDatabaseOpenHelper.installStarterPacks(mStarterPack2);
    setPackSelectionPref(mStarterPack.getId(), true);
    setPackSelectionPref(mStarterPack2.getId(), true);
    */
  }
  
  /** 
   * Delete the pack and phrases associated with a given Pack Id.  Will first
   * check that the pack exists before attempting to perform any deletions.
   * @param packId to remove
   */
  public synchronized void uninstallPack(int packId, Context context) {
    SafeLog.d(TAG, "REMOVING PACK: " + packId);
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    Pack pack = helper.getPackFromDB(String.valueOf(packId)); 
    if (pack != null) {
      PackIconUtils.deleteIcon(pack.getIconName(), context);
      helper.uninstallPack(String.valueOf(packId));
      setPackSelectionPref(packId, false, context);
    }
    else {
      SafeLog.d(TAG, "PackId " + String.valueOf(packId) + " not found in database.");
    }
  }
  
  /**
   * Shuffle all cards in the database by setting the date played to a year
   * between 1000 and 1999.  Also set the play count to 0.
   * @param context the context for the db helper
   */
  public void shuffleAllPacks(Context context) {
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    helper.shuffleAllPacks();
  }
  
  /**
   * Helper method to retrieve the pack from the database given a pack Id.
   * This allows us to do things to packs using pack attributes like pack icon
   * name which is used before deletion of a pack.
   * @param packId to get
   * @param the context for the db helper
   * @return Pack data from db, instantiated as a Pack
   */
  public Pack getPackFromDB(int packId, Context context) {
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    return helper.getPackFromDB(String.valueOf(packId));
  }
  
  /**
   * Returns whether or not a pack is installed in the database
   * @param packId to check for installation status
   * @param context the context for the db helper
   * @return true if installed - false if not
   */
  public boolean isPackInstalled(int packId, Context context) {
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    if (helper.packInstalled(packId, 0)  == Consts.PACK_NOT_PRESENT ) {
      return false;
    }
    return true;
  }
  
  /**
   * Take the packs from the server and compare version numbers against installed
   * pack versions.  Return a list of pack ids that need to be udpated.
   * @param packs from Server
   * @param context the context for the db helper
   * @return true if any pack needs to be updated, false otherwise
   */
  public boolean packsRequireUpdate(LinkedList<Pack> serverPacks, Context context) {
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    for (Pack serverPack : serverPacks) {
      int packStatus = helper.packInstalled(serverPack.getId(), serverPack.getVersion());
      if (packStatus != Consts.PACK_CURRENT && packStatus != Consts.PACK_NOT_PRESENT) {
        SafeLog.d(TAG, "Pack requires update: " + serverPack.getName());
        return true;
      }
    }
    return false;
  }

  /**
   * Instantiate the list of Selected Packs and then modify any
   * member variables of Pack that can be determined at time of
   * Deck creation.  This includes number of playable phrases
   * and pack weights.  ONLY call this method after packs have been
   * selected.
   * @param context the context from which to deal with pack preferences
   */
  public void setPackData(Context context) {
    instantiateSelectedPacks(context);
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    mTotalPlayableCards = helper.countCards(mSelectedPacks);
    setPackWeights();
    calculatePackDistribution();
  }

  /**
   * Change the pack preference for the passed in pack to either on or off.
   * @param id of pack to remove (this is the preference key)
   * @param onOff true of false (true to select)
   * @param context the context from which the shared preferences come
   */
  public void setPackSelectionPref(int id, Boolean onOff, Context context) {
    // Store the pack's boolean in the preferences file for pack preferences
    SharedPreferences.Editor packPrefsEdit = context.getSharedPreferences(
        Consts.PREFFILE_PACK_SELECTIONS, Context.MODE_PRIVATE).edit();
    packPrefsEdit.putBoolean(String.valueOf(id), onOff);
    packPrefsEdit.commit();
  }
  
  /**
   * Prepare for a game by caching the cards necessary for the entire game.  
   * Ideally we should only do this in between games to get a good cross-section
   * of the selected packs.
   * @param context the context for the db helper
   */
  private void fillCache(Context context) {
    // Fill our cache up with cards from all selected packs (using sorting algorithm)
    // Separate seen and unseen cards for handling the end of deck 
    // (when unseen must take priority)
    LinkedList<Card> unseenCards = new LinkedList<Card>();
    LinkedList<Card> seenCards = new LinkedList<Card>();
    // Pull cards from each pack into our cache, choosing unseen ones first.
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    for (int i=0; i<mSelectedPacks.size(); ++i) {
      LinkedList<Card> pulledCards = helper.pullFromPack(mSelectedPacks.get(i));
      for (Card card : pulledCards) {
        if (card.hasBeenSeenMoreThanOthers()) {
          seenCards.add(card);
        }
        else {
          unseenCards.add(card);
        }
      }
    }
    
    // Now shuffle, allowing unseen priority
    Collections.shuffle(seenCards);
    Collections.shuffle(unseenCards);
    mCache.addAll(unseenCards);
    mCache.addAll(seenCards);
    helper.close();
  }
  
  /**
   * Parse our Pack Selection Preferences to find active packs.  For
   * each of these packs, instantiate a Pack as part of our mSelectedPacks 
   * in the Deck object.
   * @param context the context from which to load the prefs
   */
  private void instantiateSelectedPacks(Context context) {
    SharedPreferences packPrefs = context.getSharedPreferences(
            Consts.PREFFILE_PACK_SELECTIONS, Context.MODE_PRIVATE);
    Map<String, ?> packSelections = new HashMap<String, Boolean>();
    packSelections = packPrefs.getAll();
    DeckOpenHelper helper = DeckOpenHelper.getInstance(context);
    for (String packId : packSelections.keySet()) {
      if (packPrefs.getBoolean(packId, false) == true) {
        Pack newPack = helper.getPackFromDB(packId);
        LinkedList<Pack> packsToCount = new LinkedList<Pack>();
        packsToCount.add(newPack);
        if (newPack != null) {
          newPack.setSize(helper.countCards(packsToCount));
          mSelectedPacks.add(newPack);
        } else {
          // Pack doesn't exist in the database, so let's unselect it
          setPackSelectionPref(Integer.valueOf(packId), false, context);
          SafeLog.w(TAG, "Preference set for pack " + String.valueOf(packId) + 
                     " which does not exist in the database. " +
                     "This may be due to changing users which will wipe purchased packs.");
        }
      }
    }
  }

  /**
   * Set the weight of each pack relative to the total number of
   * playable cards selected by the user.
   */
  private void setPackWeights() {
    for (Pack curPack : mSelectedPacks) {
      curPack.setWeight((float) curPack.getSize() / (float) mTotalPlayableCards);
    }
  }
  
  /**
   * Determine based on the size of the Cache how many
   * cards should be pulled from each deck.  After determining
   * the portion each Pack should pull, allocate the remaining
   * cards randomly to the teams.
   */
  private void calculatePackDistribution() {
    // Divide up evenly the size of the cache
    int allocated = 0;
    for (Pack curPack : mSelectedPacks) {
      int numToPull = (int) Math.floor(Consts.CACHE_MAXSIZE * curPack.getWeight());
      curPack.setNumToPullNext(numToPull);
      allocated += numToPull;
      SafeLog.d(TAG, curPack.toString());
    }
    
    // Allocate randomly the remaining cache to fill
    Random randomizer = new Random();
    int remainder = Consts.CACHE_MAXSIZE - allocated;
    for (int i=0; i<remainder; ++i) {
      int packIndex = randomizer.nextInt(mSelectedPacks.size());
      Pack pack = mSelectedPacks.get(packIndex);
      pack.setNumToPullNext(pack.getNumToPullNext()+1);
    }
  }
  
  /**
   * Return the starter pack that comes with every installation
   * @return Pack object for the starting pack
   */
  public Pack getStarterPack() {
    return mStarterPack;
  }

}
