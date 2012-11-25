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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.buzzwords.Card;
import com.buzzwords.Pack;
import com.buzzwords.CardJSONIterator;
import com.buzzwords.Consts;
import com.buzzwords.Deck;
import com.buzzwords.PackColumns;
import com.buzzwords.PackParser;
import com.buzzwords.CardColumns;
import com.buzzwords.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

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
public class Deck {

  private static final String TAG = "Deck";
  private static final String DATABASE_NAME = "buzzwords";  
  private static final int DATABASE_VERSION = 3;
  
  protected static final int CACHE_MAXSIZE = 100;
  protected static final int CACHE_TURNSIZE = 20;
  
  private static final int PACK_CURRENT = -1;
  private static final int PACK_NOT_PRESENT = -2;

  // This is the sum of all Cards in selected Packs
  private int mTotalPlayableCards;
  
  // A list of Cards in memory. Will be filled when <= ~ a turn of cards.
  private LinkedList<Card> mCache;
  
  // Tracks seen cards. Should be wiped every time the cache is refreshed.  
  private LinkedList<Card> mDiscardPile;
  
  // List of all packs selected for the Deck
  private LinkedList<Pack> mSelectedPacks;

  private Pack mStarterPack;
  
  private Context mContext;
  
  private DeckOpenHelper mDatabaseOpenHelper;
  
  /**
   * Constructor
   * 
   * @param context
   *          The Context within which to work, used to create the DB
   */
  public Deck(Context context) {
    mContext = context;
    mDatabaseOpenHelper = new DeckOpenHelper(context);
    mCache = new LinkedList<Card>();
    mDiscardPile = new LinkedList<Card>();
    mSelectedPacks = new LinkedList<Pack>();
    mStarterPack = new Pack(1, "starterPack", "freepacks/starterPack.json", "packs/icons/packicon_classic1.png",
        "Description of pack1", 125, PackPurchaseConsts.PACKTYPE_FREE, 0, true);
  }

  /**
   * Get a card from the top of the Cache queue.  Once
   * this reaches the bottom of the deck, we should top off the Deck which
   * will in turn trigger a pull from packs to refill the cache.
   * @return a card reference
   */
  public Card dealCard() {
    Card ret;
    // This shouldn't happen unless a lot of cards are played in one turn (CACHE_TURNSIZE)
    if (mCache.isEmpty()) {
      Log.i(TAG, "Filling entire cache mid-turn. This is expensive.");
      // We must mark seen now so that we won't re-pull these same cards during fillCache
      updateSeenFields();
      this.fillCache();
    }
    ret = mCache.removeFirst();
    Log.i(TAG, " Dealing ::" + ret.getTitle() + " Pack: " + ret.getPack().getName());
    mDiscardPile.add(ret);
    return ret;
  }
  
  /**
   * If there aren't enough cards in the cache to play one more turn,
   * clear it and fill it back up. This should be called during downtime
   * since it could be a costly database pull.
   */
  public void fillCacheIfLow() {
    Log.d(TAG, "fillCacheIfLow()");
    if (mCache.size() < CACHE_TURNSIZE) {
      Log.d(TAG, "...Cache size was low (" + mCache.size() + "), filling...");
      mCache.clear();
      fillCache();
      Log.d(TAG, "...filled. Cache size is now " + mCache.size());
    }
  }
  
  /**
   * Updates the playdate and times_seen for only the seen cards.  This
   * should be called when we pause the game or a turn ends.
   */
  public void updateSeenFields() {
    mDatabaseOpenHelper.updateSeenFields(mDiscardPile);
    mDiscardPile.clear();
  }

  
  /**
   * Retrieve a Linked List of all Packs that a user has installed in their database.
   * @return Linked List of all local Packs
   */
  public LinkedList<Pack> getLocalPacks() {
    Log.d(TAG, "getLocalPacks()");
    LinkedList<Pack> localPacks = mDatabaseOpenHelper.getAllPacksFromDB();
    for (Pack pack : localPacks) {
      pack.setSize(mDatabaseOpenHelper.countCards(pack));
      pack.setNumCardsSeen(mDatabaseOpenHelper.countNumSeen(pack));
    }
    return localPacks;
  }
  
  /**
   * Take a Pack object and pull in cards from the server into the database. 
   * @param pack
   * @throws RuntimeException 
   * @throws IOException
   * @throws URISyntaxException
   */
  public void installPack(Pack pack) throws RuntimeException {
    Log.d(TAG, "INSTALLING PACK: \n" + pack.getName());
    // If pack is out of date, delete the icon
    if (mDatabaseOpenHelper.packInstalled(pack.getId(), pack.getVersion()) == pack.getId()) {
      PackIconUtils.deleteIcon(pack.getIconName(), mContext);
    }
    mDatabaseOpenHelper.installOrUpdatePackFromServer(pack);
  }
  
  /**
   * Install all of the packs that the app comes with.  This ultimately
   * will be just one pack.
   * @throws RuntimeException 
   */
  public synchronized void installStarterPacks() throws RuntimeException {
    Log.d(TAG, "INSTALLING STARTER PACKS");
    mDatabaseOpenHelper.installStarterPacks(mStarterPack);
  }
  
  /** 
   * Delete the pack and phrases associated with a given Pack Id.  Will first
   * check that the pack exists before attempting to perform any deletions.
   * @param packId to remove
   */
  public synchronized void uninstallPack(int packId) {
    Log.d(TAG, "REMOVING PACK: " + packId);
    Pack pack = getPackFromDB(packId); 
    if (pack != null) {
      PackIconUtils.deleteIcon(pack.getIconName(), mContext);
      mDatabaseOpenHelper.uninstallPack(String.valueOf(packId));
    }
    else {
      Log.d(TAG, "PackId " + String.valueOf(packId) + " not found in database.");
    }
  }
  
  /**
   * Shuffle all cards in the database by setting the date played to a year
   * between 1000 and 1999.  Also set the play count to 0.
   */
  public void shuffleAllPacks() {
    mDatabaseOpenHelper.shuffleAllPacks();
  }
  
  /**
   * Helper method to retrieve the pack from the database given a pack Id.
   * This allows us to do things to packs using pack attributes like pack icon
   * name which is used before deletion of a pack.
   * @param packId to get
   * @return Pack data from db, instantiated as a Pack
   */
  public Pack getPackFromDB(int packId) {
    return mDatabaseOpenHelper.getPackFromDB(String.valueOf(packId));
  }
  
  /**
   * Returns whether or not a pack is installed in the database
   * @param packId to check for installation status
   * @return true if installed - false if not
   */
  public boolean isPackInstalled(int packId) {
    Log.d(TAG, "isPackInstalled(" + String.valueOf(packId) + ")");
    if (mDatabaseOpenHelper.packInstalled(packId, 0)  == PACK_NOT_PRESENT ) {
      return false;
    }
    return true;
  }
  
  /**
   * Take the packs from the server and compare version numbers against installed
   * pack versions.  Return a list of pack ids that need to be udpated.
   * @param packs from Server
   * @return true if any pack needs to be updated, false otherwise
   */
  public boolean packsRequireUpdate(LinkedList<Pack> serverPacks) {
    Log.d(TAG, "Checking local pack status...");
    
    for (Pack serverPack : serverPacks) {
      int packStatus = mDatabaseOpenHelper.packInstalled(serverPack.getId(), serverPack.getVersion());
      if (packStatus != PACK_CURRENT && packStatus != PACK_NOT_PRESENT) {
        Log.d(TAG, "Pack requires update: " + serverPack.getName());
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
   */
  public void setPackData() {
    Log.d(TAG, "setPackData()");
    instantiateSelectedPacks();
    mTotalPlayableCards = mDatabaseOpenHelper.countCards(mSelectedPacks);
    setPackWeights();
    calculatePackDistribution();
  }
  
  /**
   * Prepare for a game by caching the cards necessary for the entire game.  
   * Ideally we should only do this in between games to get a good cross-section
   * of the selected packs.
   */
  private void fillCache() {
    Log.d(TAG, "fillCache()");
    printDeck();
    
    // Fill our cache up with cards from all selected packs (using sorting algorithm)
    // Separate seen and unseen cards for handling the end of deck 
    // (when unseen must take priority)
    Log.d(TAG, "Pull Calculations ");
    LinkedList<Card> unseenCards = new LinkedList<Card>();
    LinkedList<Card> seenCards = new LinkedList<Card>();
    // Pull cards from each pack into our cache, choosing unseen ones first.
    for (int i=0; i<mSelectedPacks.size(); ++i) {
      LinkedList<Card> pulledCards = mDatabaseOpenHelper.pullFromPack(mSelectedPacks.get(i));
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
    mDatabaseOpenHelper.close();
    Log.i(TAG, "filled.");
    printDeck();
  }
  
  /**
   * Parse our Pack Selection Preferences to find active packs.  For
   * each of these packs, instantiate a Pack as part of our mSelectedPacks 
   * in the Deck object.
   */
  private void instantiateSelectedPacks() {
    Log.d(TAG, "instantiateSelectedPacks()");
    SharedPreferences packPrefs = mContext.getSharedPreferences(
            Consts.PREFFILE_PACK_SELECTIONS, Context.MODE_PRIVATE);
    Map<String, ?> packSelections = new HashMap<String, Boolean>();
    packSelections = packPrefs.getAll();
    
    for (String packId : packSelections.keySet()) {
      if (packPrefs.getBoolean(packId, false) == true) {
        Pack newPack = mDatabaseOpenHelper.getPackFromDB(packId);
        LinkedList<Pack> packsToCount = new LinkedList<Pack>();
        packsToCount.add(newPack);
        if (newPack != null) {
          newPack.setSize(mDatabaseOpenHelper.countCards(packsToCount));
          mSelectedPacks.add(newPack);
        } else {
          Log.w(TAG, "Preference set for pack " + String.valueOf(packId) + 
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
    Log.d(TAG, "setPackWeights()");
    Log.d(TAG, "** mTotalPlayableCards: " + mTotalPlayableCards);

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
    Log.d(TAG, "calculatePackDistribution()");
    
    // Divide up evenly the size of the cache
    int allocated = 0;
    Log.d(TAG, "SELECTED PACKS LENGTH: " + mSelectedPacks.size());
    for (Pack curPack : mSelectedPacks) {
      int numToPull = (int) Math.floor(CACHE_MAXSIZE * curPack.getWeight());
      curPack.setNumToPullNext(numToPull);
      allocated += numToPull;
      Log.d(TAG, curPack.toString());
    }
    
    // Allocate randomly the remaining cache to fill
    Random randomizer = new Random();
    int remainder = CACHE_MAXSIZE - allocated;
    Log.d(TAG, "Assigning remainder of " + remainder + " cards.");
    for (int i=0; i<remainder; ++i) {
      int packIndex = randomizer.nextInt(mSelectedPacks.size());
      Pack pack = mSelectedPacks.get(packIndex);
      pack.setNumToPullNext(pack.getNumToPullNext()+1);
      Log.d(TAG, "..added a remainder to " + pack.getName());
    }
  }
  
  /**
   * Return the starter pack that comes with every installation
   * @return Pack object for the starting pack
   */
  public Pack getStarterPack() {
    return mStarterPack;
  }
  
  /**
   * Debugging function.  Can be removed later.
   */
  public void printDeck() {
    Log.d(TAG, "printDeck...");
    Log.d(TAG, "========================");
    Log.d(TAG, "CACHE: ");
    Log.d(TAG, "Size is " + mCache.size());
    for (int i=0; i<mCache.size(); ++i) {
      Log.d(TAG, "..." + mCache.get(i).getTitle());
    }
    Log.d(TAG, "END CACHE");
    Log.d(TAG, "------------------------");
  }

  /**
   * Helper method to throw a user exception, logging an error
   * message and the stacktrace before throwing the exception.
   * @param e error to print stacktrace for
   * @param msg to log
   * @throws RuntimeException
   */
  static private void throwUserException(Exception e, String msg) throws RuntimeException {
    Log.e(TAG, msg);
    e.printStackTrace();
    RuntimeException userException = new RuntimeException(e);
    throw userException;
  }
  
  
  /**
   * This class creates/opens the database and provides helper functions for
   * batch CRUD operations
   */
  public static class DeckOpenHelper extends SQLiteOpenHelper {

    private final Context mHelperContext;
    private SQLiteDatabase mDatabase;

    /**
     * Default Constructor from superclass
     * 
     * @param context
     */
    DeckOpenHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
      mHelperContext = context;
    }

    /**
     * Create the tables and populate from the XML file
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(PackColumns.TABLE_CREATE);
      db.execSQL(CardColumns.TABLE_CREATE);
    }

    /**
     * Install all the packs that come with the app into the database.
     * Since the pack
     * @throws RuntimeException 
     */
    public synchronized void installStarterPacks(Pack starterPack) throws RuntimeException {
      Log.d(TAG, "installStarterPacks()");
      installPackFromResource(starterPack, R.raw.starter);
    }
    
    /**
     * Count all cards in the deck quickly
     * 
     * @return the number of cards in the deck
     */
    public synchronized int countAllCards() {
      mDatabase = getReadableDatabase();
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, CardColumns.TABLE_NAME);
      mDatabase.close();
      return ret;
    }

    /**
     * Returns an integer count of all phrases associated with the passed in pack
     * @param pack The pack to count
     * @return -1 if no phrases found, otherwise the number of phrases found
     */
    public synchronized int countCards(Pack pack) {
      Log.d(TAG, "countCards(" + pack.getName() + ")");
      mDatabase = getWritableDatabase();
      
      String[] args = new String[2];
      
      args[0] = String.valueOf(pack.getId());
      
      Cursor countQuery = mDatabase.rawQuery("SELECT * " + 
          " FROM " + CardColumns.TABLE_NAME + 
          " WHERE " + CardColumns.PACK_ID + " IN (" + args[0] + ")", null);
      int count = countQuery.getCount();
      
      countQuery.close();
      mDatabase.close();
      return count;
    }

    /**
     * Returns an integer count of all phrases associated with the passed in pack names
     * @param packs A list of packs whose cards will be counted
     * @return -1 if no cards counted, otherwise the number of cards counted
     */
    public synchronized int countCards(LinkedList<Pack> packs) {
      Log.d(TAG, "countCards(LinkedList<Pack>)");
      mDatabase = getWritableDatabase();
      
      String[] args = new String[2];
      
      args[0] = buildPackIdString(packs);
      
      Cursor countQuery = mDatabase.rawQuery("SELECT * " + 
          " FROM " + CardColumns.TABLE_NAME + 
          " WHERE " + CardColumns.PACK_ID + " IN (" + args[0] + ")", null);
      int count = countQuery.getCount();
      
      countQuery.close();
      mDatabase.close();
      return count;
    }

    
    /**
     * Count the number of packs which will likely be needed for setting up views
     * 
     * @return the number of packs
     */
    public synchronized int countPacks() {
      Log.d(TAG, "countPacks()");
      mDatabase = getReadableDatabase();
      
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, PackColumns.TABLE_NAME);

      mDatabase.close();
      return ret;
    }

    /**
     * Return a linked list of instantiated Packs that exist in the Pack db table.
     * @return LinkedList of Packs that use has already installed
     */
    public synchronized LinkedList<Pack> getAllPacksFromDB() {
      Log.d(TAG, "getAllPacksFromDB()");
      mDatabase = getReadableDatabase();
      
      Cursor packQuery = mDatabase.query(PackColumns.TABLE_NAME, PackColumns.COLUMNS,
          null, null, null, null, null);
      
      Pack pack = null;
      LinkedList<Pack> ret = new LinkedList<Pack>();
      if (packQuery.moveToFirst()) {
        
        while (!packQuery.isAfterLast()) {
          pack = new Pack(packQuery.getInt(0), packQuery.getString(1), packQuery.getString(2),
              packQuery.getString(3), packQuery.getString(4), -1, 
              packQuery.getInt(5), packQuery.getInt(6), true);
          ret.add(pack);
          packQuery.moveToNext();
        }
      }
      
      packQuery.close();
      mDatabase.close();
      return ret;
    }
    
    /**
     * Return a Pack instantiated using the entry in the Pack database.
     * @param packId of the pack you wish to instantiate
     * @return Instantiated Pack object if exists, null otherwise 
     */
    public synchronized Pack getPackFromDB(String packId) {
      Log.d(TAG, "getPackFromDB(" + String.valueOf(packId) + ")");
      mDatabase = getReadableDatabase();
      
      String[] id = new String[] {packId};
      
      Cursor packQuery = mDatabase.query(PackColumns.TABLE_NAME,PackColumns.COLUMNS, 
          PackColumns._ID + "=?", id, null, null, null);
      
      Pack pack = null;
      if (packQuery.moveToFirst()) {
        pack = new Pack(packQuery.getInt(0), packQuery.getString(1), packQuery.getString(2),
                        packQuery.getString(3), packQuery.getString(4), -1, 
                        packQuery.getInt(5), packQuery.getInt(6), true);
      }
      packQuery.close();
      mDatabase.close();
      return pack;
    }

    /**
     * Count the number of cards in a given pack that have been seen at least once.
     * @param pack the pack to count
     * @return
     */
    public synchronized int countNumSeen(Pack pack) {
      Log.d(TAG, "countNumSeen(" + pack.getName() + ")");
      mDatabase = getReadableDatabase();
      
      String id = String.valueOf(pack.getId());
      Cursor cardCursor = mDatabase.query(CardColumns.TABLE_NAME, CardColumns.COLUMNS, 
          CardColumns.PACK_ID + " = " + id + " and " + CardColumns.TIMES_SEEN +  " >= 1", 
          null, null, null, null);
      
      int seenCardCount = cardCursor.getCount();
      
      cardCursor.close();
      mDatabase.close();
      return seenCardCount;
    }
    
    /**
     * Determine the number of complete playthroughs by returning the smallest
     * times_seen value for all cards in the passed in pack.
     * @param pack for which to find num playthroughs
     * @return number of playthroughs
     */
    public synchronized int calcNumPlaythroughs(Pack pack) {
      Log.d(TAG, "setNumPlaythroughs(" + String.valueOf(pack.getId()) + ")");
      mDatabase = getReadableDatabase();
      
      String[] id = new String[] {String.valueOf(pack.getId())};
      
      Cursor cursor = mDatabase.rawQuery(
            "SELECT MIN(" + CardColumns.TIMES_SEEN + ")"
          + " FROM " + CardColumns.TABLE_NAME 
          + " WHERE " + CardColumns.PACK_ID + " = ?", id);
      
      cursor.moveToFirst();
      int playThroughs = cursor.getInt(0);
      cursor.close();
      mDatabase.close();
      return playThroughs;
    }
    
    /**
     * Load the words from the JSON file using only one SQLite database. This
     * function loads the words from a json file that is stored as a resource in the project
     * 
     * @param packName the name of the file to digest
     * @param resId the resource of the pack file to digest
     * @throws RuntimeException 
     */
    public synchronized void installPackFromResource(Pack pack, int resId) throws RuntimeException {
      Log.d(TAG, "Installing pack from resource " + String.valueOf(resId));

      BufferedReader packJSON = new BufferedReader(new InputStreamReader(
          mHelperContext.getResources().openRawResource(resId)));
      StringBuilder packBuilder = new StringBuilder();
      String line = null;
      try {
        while((line = packJSON.readLine()) != null) {
          packBuilder.append(line).append("\n");
        }
      } catch (IOException e) {
        throwUserException(e, "IOException installing pack " + pack.getName() + " from resource.");
      }
      CardJSONIterator cardItr = PackParser.parseCards(packBuilder);
      
      installPack(pack, cardItr);
      
      Log.d(TAG, "DONE loading words.");
    }

    /**
     * Take a Pack and determine whether it needs to be updated, installed, or ignored.
     * If Pack needs to be installed, request the cards to install from the server and 
     * perform installation.
     * 
     * @param serverPack Pack of cards to install
     * @throws IOException
     * @throws URISyntaxException
     * @return true if sync successful (up to date/installed) false for failure to install
     * @throws RuntimeException 
     */
    public synchronized void installOrUpdatePackFromServer(Pack serverPack) throws RuntimeException {
      Log.d(TAG, "installPackFromServer(" + serverPack.getName() + ")");
      int packId = packInstalled(serverPack.getId(), serverPack.getVersion());

      CardJSONIterator cardItr;
      
      // Don't add a pack if it's already there
      if (packId == PACK_CURRENT) {
        Log.d(TAG, "No update required, pack " + serverPack.getName() + " current.");
        return;
      }
      if(packId == PACK_NOT_PRESENT) { 
        // I BELIEVE that we could get a database lock issues when an exception is thrown here.
        // To prevent this, close the db on exception. 
        try {
          cardItr = PackClient.getInstance().getCardsForPack(serverPack);
          installPack(serverPack, cardItr);
        } catch (IOException e) {
          throwUserException(e, "Encountered IOException installing pack from server.");
        } catch (URISyntaxException e) {
          throwUserException(e, "Encountered URISyntaxException installing pack from server.");
        }
      } else {
        // Close the db on exception to prevent closing db issues.
        try {
          cardItr = PackClient.getInstance().getCardsForPack(serverPack);
          installPack(serverPack, cardItr);
        } catch (IOException e) {
          throwUserException(e, "Encountered IOException installing pack from server.");
        } catch (URISyntaxException e) {
          throwUserException(e, "Encountered URISyntaxException installing pack from server.");
        }
      }
      Log.d(TAG, "DONE loading words.");
    }
    
    /**
     * Replaces or inserts a new row into the Packs table and then replaces or inserts
     * rows into the Cards table for each card in that pack.
     * 
     * @param db
     * @param packName
     * @param packVersion
     * @param cardItr
     */
    private synchronized void installPack(Pack pack, CardJSONIterator cardItr) {
      Log.d(TAG, "installPack: " + pack.getName() + "v" + String.valueOf(pack.getVersion()));
      
      mDatabase = getWritableDatabase();
      // Add the pack and all cards in a single transaction.
      try {
        mDatabase.beginTransaction();
        Card curCard = null;
        while(cardItr.hasNext()) {
          curCard = cardItr.next();
          upsertCard(curCard, pack.getId(), mDatabase);
        }
        upsertPack(pack, mDatabase);
        mDatabase.setTransactionSuccessful();
      } finally {
        mDatabase.endTransaction();
      }
      if (mDatabase.isOpen()) {
        mDatabase.close();
      }
    }
    
    /** 
     * Delete the pack and cards associated with a given Pack Id.  Will first
     * check that the pack exists before attempting to perform any deletions.
     * @param packId to remove
     */
    private synchronized void uninstallPack(String packId) {
      Log.d(TAG, "removePack: " + String.valueOf(packId));
      mDatabase = getWritableDatabase();

      String[] whereArgs = new String[] { packId };
      // Add the pack and all cards in a single transaction.
      try {
        mDatabase.beginTransaction();
        mDatabase.delete(CardColumns.TABLE_NAME, CardColumns.PACK_ID + "=?", whereArgs);
        mDatabase.delete(PackColumns.TABLE_NAME, PackColumns._ID + "=?", whereArgs);
        mDatabase.setTransactionSuccessful();
      } finally {
        mDatabase.endTransaction();
      }
      if (mDatabase.isOpen()) {
        mDatabase.close();
      }
    }
    
    /**
     * Replaces existing card if it exists, otherwise inserts the card in the Cards table.
     * 
     * @return rowId or -1 if failed
     */
    public synchronized static long upsertCard(Card card, int packId, SQLiteDatabase db) {
      long ret;
      String[] whereArgs = new String[] { String.valueOf(card.getId()) };
      Cursor cursor = db.query(CardColumns.TABLE_NAME, CardColumns.COLUMNS,
          CardColumns._ID + "= ?", whereArgs, null, null, null);
      if (cursor.getCount() == 1) {
        ret = updateCard(card, packId, db);
      } else {
        ret = insertCard(card, packId, db);
      }
      cursor.close();
      return ret;
    }
    
    /**
     * Insert a new card into the cards table
     * @param card The card to insert
     * @param packId The pack to which the card belongs
     * @param db The database to insert the card into
     * @return The new id of the row where the card was inserted, -1 if error
     */
    private synchronized static long insertCard(Card card, int packId, SQLiteDatabase db) {
      Log.d(TAG, "insertCard(" + card.getTitle() + ")");
      Random randomizer = new Random();
      // Get a random year to make sure installations have different play orders.
      final int randYear = randomizer.nextInt(1970);
      ContentValues initialValues = new ContentValues();
      initialValues.put(CardColumns._ID, card.getId());
      initialValues.put(CardColumns.TITLE, card.getTitle());
      initialValues.put(CardColumns.BADWORDS, card.getBadWordsString());
      initialValues.put(CardColumns.PLAY_DATE, randYear + "-01-01 00:00:00");
      initialValues.put(CardColumns.TIMES_SEEN, 0);
      initialValues.put(CardColumns.PACK_ID, packId);
      return db.insert(CardColumns.TABLE_NAME, null, initialValues);
    }
    
    /**
     * Update an existing card in the cards table
     * @param phrase The card to update
     * @param packId The pack to which the card belongs
     * @param db The database where the card exists
     * @return The number of rows affected
     */
    private synchronized static long updateCard(Card card, int packId, SQLiteDatabase db) {
      Log.d(TAG, "updateCard(" + card.getTitle() + ")");
      String[] whereArgs = new String[] { String.valueOf(card.getId()) };
      ContentValues initialValues = new ContentValues();
      initialValues.put(CardColumns.TITLE, card.getTitle());
      initialValues.put(CardColumns.BADWORDS, card.getBadWordsString());
      initialValues.put(CardColumns.PACK_ID, packId);
      return db.update(CardColumns.TABLE_NAME, initialValues, 
                       CardColumns._ID + " = ?", whereArgs);
    }
    
    /**
     * Either insert a new pack into the Pack table of a given database or replace
     * one that already exist in the table.
     * @param pack The pack object to insert into db
     * @param db The db in which to insert the new pack
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public synchronized static long upsertPack(Pack pack, SQLiteDatabase db) {
      Log.d(TAG, "upsertPack(" + pack.getName() + ")");
      
      ContentValues packValues = new ContentValues();
      packValues.put(PackColumns._ID, pack.getId());
      packValues.put(PackColumns.NAME, pack.getName());
      packValues.put(PackColumns.PATH, pack.getPath());
      packValues.put(PackColumns.ICON_PATH, pack.getIconPath());
      packValues.put(PackColumns.DESCRIPTION, pack.getDescription());
      packValues.put(PackColumns.PURCHASE_TYPE, pack.getPurchaseType());
      packValues.put(PackColumns.VERSION, pack.getVersion());
      return db.replace(PackColumns.TABLE_NAME, null, packValues);
    }

    /**
     * Update play_date and times_seen for all passed in card ids to current time
     * @param ids
     *          comma delimited set of card ids to increment, ex. "1, 2, 4, 10"
     * @return
     */
    public synchronized void updateSeenFields(List<Card> cardList) {
      mDatabase = getWritableDatabase();
      String ids = buildCardIdString(cardList);
      mDatabase.beginTransaction();
      try {
        mDatabase.execSQL("UPDATE " + CardColumns.TABLE_NAME
             + " SET " + CardColumns.PLAY_DATE + " = datetime('now'), "
                       + CardColumns.TIMES_SEEN + " = " + CardColumns.TIMES_SEEN + " + 1"
             + " WHERE " + CardColumns._ID + " in(" + ids + ");");
        mDatabase.setTransactionSuccessful();
      } finally {
        mDatabase.endTransaction();
      }
      if (mDatabase.isOpen()) {
        mDatabase.close();
      }
    }
    
    /**
     * Sets all cards in database to a random year between 1970 and 2001.  Also sets
     * the times seen to 0 for every card.  The biggest priority is that
     * this ensures users get a totally reordered deck.
     */
    public synchronized void shuffleAllPacks() {
      final String RAND_DATE_STR = "datetime(abs(random())%1000000000, 'unixepoch')";
      mDatabase = getWritableDatabase();
      mDatabase.beginTransaction();
      try {
        mDatabase.execSQL("UPDATE " + CardColumns.TABLE_NAME
            + " SET " + CardColumns.PLAY_DATE + " = " + RAND_DATE_STR + ","
                      + CardColumns.TIMES_SEEN + " = 0;");
        mDatabase.setTransactionSuccessful();
      } finally {
        mDatabase.endTransaction();
      }
      if (mDatabase.isOpen()) {
        mDatabase.close();
      }
    }

    /**
     * Queries the Packs table and returns the packId if the pack requires updating, 
     * otherwise returns either PACK_CURRENT or PACK_NOT_PRESENT.
     * @param packName 
     * @param packVersion Latest version of the pack
     * @param db
     * @return
     */
    private synchronized int packInstalled(int packId, int packVersion) {
      mDatabase = getReadableDatabase();
      String[] packIds= {String.valueOf(packId)};
      Cursor res = mDatabase.query(PackColumns.TABLE_NAME, PackColumns.COLUMNS,
          PackColumns._ID + " = (?)", packIds, null, null, null);
      if (res.getCount() >= 1) {
        res.moveToFirst();
        int oldVersion = res.getInt(6);
        int oldId = res.getInt(0);
        res.close();
        mDatabase.close();
        if (packVersion > oldVersion) {
          return oldId;
        } else {
          return PACK_CURRENT;
        }
      } else {
        res.close();
        mDatabase.close();
        
        return PACK_NOT_PRESENT;
      }
    }

    
    /**
     * Generates and returns a LinkedList of Cards from the database for a specific pack. First,
     * we request all the cards from the db sorted by date. Then we calculate how many of the 
     * Cards should be returned based on the pack's weight relative to the total number of selected
     * cards.
     * @param pack The pack from which to pull cards
     * @return
     */
    private synchronized LinkedList<Card> pullFromPack(Pack pack) {
      Log.d(TAG, "pullFromPack(" + pack.getName() + ")");
      // Do this first since it needs it's own db interaction
      int numPlayThroughs = calcNumPlaythroughs(pack);
      mDatabase = getWritableDatabase();
      
      LinkedList<Card> returnCards = new LinkedList<Card>();
      int packid = pack.getId();
      int targetNum = pack.getNumToPullNext();
      
      // Build our arguments for SQL
      String[] args = new String[] {String.valueOf(packid), String.valueOf(targetNum)};
      
      // Get the playable cards from pack, sorted by playdate
      Cursor res = mDatabase.query(CardColumns.TABLE_NAME, CardColumns.COLUMNS,
            CardColumns.PACK_ID + " = " + args[0],
            null, null, null, CardColumns.PLAY_DATE + " asc, " + CardColumns.TIMES_SEEN + " asc", args[1]);
      res.moveToFirst();
      
      // The number of cards to return from any given pack will use the following formula:
      // (WEIGHT OF PACK) * CACHE-SIZE
      Log.d(TAG, "** this.numInPack: " + res.getCount());
      Log.d(TAG, "** Pack.numToPull: " + targetNum);
      Log.d(TAG, "** Pack.weight: " + pack.getWeight());
      Log.d(TAG, "** Pack.size: " + pack.getSize());

      // Add cards to what will be the Cache, setting cards seen more than others within
      // the pack to true.
      while (!res.isAfterLast()) {
        Card card = new Card(res.getInt(0), res.getString(1), res.getString(2), pack);
        if (res.getInt(4) > numPlayThroughs) {
          card.setSeenMoreThanOthers(true);
        }
        returnCards.add(card);
        res.moveToNext();
      }
      Log.d(TAG, "**" + returnCards.size() + " phrases added.");
      
      res.close();
      mDatabase.close();
      return returnCards;
    }
    
    /**
     * Helper class to convert a linked list of cards to a 
     * comma-delimited String of Card Ids.
     * @param cardList a list of Cards
     * @return a comma-delimited string of Ids ex. 1,20,22
     */
    private String buildCardIdString(List<Card> cardList) {
      String[] ids = new String[cardList.size()];
      
      for (int i=0; i< cardList.size(); ++i) {
        ids[i] = String.valueOf(cardList.get(i).getId());
      }
      return TextUtils.join(",", ids);
    }
    
    /**
     * Helper class to convert a linked list of packs to 
     * 
     * a comma-delimited String of Pack Ids.
     * @param packList a list of Packs
     * @return a comma-delimited string of Ids ex. 1,20,22
     */
    private String buildPackIdString(List<Pack> packList) {
      String[] ids = new String[packList.size()];
      
      for (int i=0; i<packList.size(); ++i) {
        ids[i] = String.valueOf(packList.get(i).getId());
      }
      return TextUtils.join(",", ids);
    }

     /**
     * For now, onUpgrade destroys the old database and runs create again.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
          + newVersion + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS cards;");
      db.execSQL("DROP TABLE IF EXISTS cache;");
      db.execSQL("CREATE TABLE IF NOT EXISTS " + CardColumns.TABLE_NAME + ";");
      db.execSQL("CREATE TABLE IF NOT EXISTS " + PackColumns.TABLE_NAME + ";");
      onCreate(db);
    }

    /**
     * Make sure we close the database when we close the helper
     */
    @Override
    public void close() {
      super.close();
      if (mDatabase != null) {
        mDatabase.close();
      }
    }
  }

}
