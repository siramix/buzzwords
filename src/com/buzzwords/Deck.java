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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.buzzwords.Card;
import com.buzzwords.Pack;
import com.buzzwords.Deck.DeckOpenHelper;
import com.buzzwords.CardJSONIterator;
import com.buzzwords.Consts;
import com.buzzwords.Deck;
//import com.buzzwords.PackClient;
import com.buzzwords.PackColumns;
import com.buzzwords.PackParser;
import com.buzzwords.CardColumns;
import com.buzzwords.BuzzWordsApplication;
import com.buzzwords.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
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
  private static final String CARD_TABLE_NAME = "cards";
  private static final String CACHE_TABLE_NAME = "cache";
  
  private static final int DATABASE_VERSION = 3;
  
  protected static final int BACK_CACHE_MAXSIZE = 200;
  protected static final int FRONT_CACHE_MAXSIZE = 20;
  
  private static final int PACK_CURRENT = -1;
  private static final int PACK_NOT_PRESENT = -2;
  
  // This is the sum of all Cards in selected Packs
  private int mTotalPlayableCards;
  
  // After taking the top 1/DIVSOR Cards from a pack, throw back a percentage of them 
  private static final int THROW_BACK_PERCENTAGE = 0;
  
  // A list of back Cards used for refreshing the deck.  Will be filled after it reaches 0.
  private LinkedList<Card> mBackCache;
  
  // Front Cache will be topped off every turn
  private LinkedList<Card> mFrontCache;
  
  // Should be wiped every time the mbackCache is cleared
  private LinkedList<Card> mSeenCards;
  
  // List of all packs selected for the Deck
  private LinkedList<Pack> mSelectedPacks;
  
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
    mBackCache = new LinkedList<Card>();
    mFrontCache = new LinkedList<Card>();
    mSeenCards = new LinkedList<Card>();
    mSelectedPacks = new LinkedList<Pack>();
    setPackData();
  }

  /**
   * Get a card from the top of the Front Cache queue.  Once
   * this reaches the bottom of the deck, we should top off the Deck which
   * will in turn trigger a pull from packs to refill the cache.
   * @return a card reference
   */
  public Card dealCard() {
    Card ret;
    if (mFrontCache.isEmpty()) {
      this.topOffFrontCache();
    }
    ret = mFrontCache.removeFirst();
    Log.i(TAG, " Dealing ::" + ret.getTitle() + " Pack: ???");
    mSeenCards.add(ret);
    return ret;
  }
  
  /**
   * If there aren't enough cards in the back cache to fill the 
   * front-facing cache at least once, fill up both caches.  This
   * should be called during downtime since it could be a costly
   * database pull.
   */
  public void fillCachesIfLow() {
    Log.d(TAG, "fillCachesIfLow()");
    topOffFrontCache();
    if (mBackCache.size() < FRONT_CACHE_MAXSIZE) {
      Log.d(TAG, "...Back Cache size was low (" + mBackCache.size() + "), filling...");
      mBackCache.clear();
      fillBackCache();
      Log.d(TAG, "...filled. Back Cache size is now " + mBackCache.size());
    }
  }
  
  /**
   * Updates the playdate for any cards seen mid-turn to today's date.
   * The list of seen cards will be cleared when the front cache is re-filled.
   * cache.
   */
  public void updatePlayDate() {
    this.updatePlayDate(mSeenCards);
  }

  /**
   * Updates the playdate for any cards passed in.  This
   * list of cards will be cleared 
   * @param cardsToUpdate - a Linked List of cards that will have their playdate
   *                        updated to today's date.
   */
  public void updatePlayDate(LinkedList<Card> cardsToUpdate) {
    DeckOpenHelper helper = new Deck.DeckOpenHelper(
        mContext);      
    helper.updatePlayDate(cardsToUpdate);
    helper.close();
  }

  
  /**
   * Retrieve a Linked List of all Packs that a user has installed in their database.
   * @return Linked List of all local Packs
   */
  public LinkedList<Pack> getLocalPacks() {
    return mDatabaseOpenHelper.getAllPacksFromDB();
  }
  
  /**
   * Take a Pack object and pull in cards from the server into the database. 
   * @param pack
   * @throws IOException
   * @throws URISyntaxException
   */
  public synchronized void installPack(Pack pack) throws RuntimeException {
    Log.d(TAG, "INSTALLING PACK: \n" + pack.toString());
    try {
      mDatabaseOpenHelper.installPackFromServer(pack);
    } catch (IOException e) {
      RuntimeException userException = new RuntimeException(e);
      throw userException;
    } catch (URISyntaxException e) {
      RuntimeException userException = new RuntimeException(e);
      throw userException;
    }
  }
  
  /**
   * Install all of the packs that the app comes with.  This ultimately
   * will be just one pack.
   */
  public synchronized void installStarterPacks() {
    Log.d(TAG, "INSTALLING STARTER PACKS");
    mDatabaseOpenHelper.installStarterPacks();
  }
  
  /** 
   * Delete the pack and phrases associated with a given Pack Id.  Will first
   * check that the pack exists before attempting to perform any deletions.
   * @param packId to remove
   */
  public synchronized void uninstallPack(int packId) {
    Log.d(TAG, "REMOVING PACK: " + packId);
    String packIdStr = String.valueOf(packId);
    if (mDatabaseOpenHelper.getPackFromDB(packIdStr) != null) {
        mDatabaseOpenHelper.uninstallPack(packIdStr);
    }
    else {
      Log.d(TAG, "PackId " + String.valueOf(packId) + " not found in database.");
    }
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
   * @param payPacks All the server's pay packs
   * @param freePacks All the server's free packs
   * @return LinkedList of packs that need to be updated, 
   *            null if no packs need updating
   */
  public LinkedList<Pack> getPacksForUpdate(LinkedList<Pack> payPacks, LinkedList<Pack> freePacks) {
    Log.d(TAG, "Checking local pack status...");
    LinkedList<Pack> localPacks = mDatabaseOpenHelper.getAllPacksFromDB();
    LinkedList<Pack> allPacks = new LinkedList<Pack>();
    allPacks.addAll(payPacks);
    allPacks.addAll(freePacks);
    
    HashMap<Integer, Pack> allPacksMap= new HashMap<Integer, Pack>();
    for (Pack pack : allPacks) {
      allPacksMap.put(pack.getId(), pack);
    }
    
    LinkedList<Pack> packsToUpdate = new LinkedList<Pack>();
    for (Pack pack : localPacks) {
      int curId = pack.getId();
      int oldVsn = pack.getVersion();
      int newVsn = allPacksMap.get(curId).getVersion();
      Log.d(TAG, "pack: " + pack.getName() + " oldvsn: " + oldVsn + " newvsn: " + newVsn);
      
      if (oldVsn < newVsn) {
        packsToUpdate.add(allPacksMap.get(curId));
      }
    }
    return packsToUpdate;
  }
  
  private void topOffFrontCache() {
    Log.d(TAG, "topOffFrontCache()");
    int lack = FRONT_CACHE_MAXSIZE - mFrontCache.size();
    Log.d(TAG, "*** Front Cache Size: " + mFrontCache.size());
    Log.d(TAG, "*** Lack: " + String.valueOf(lack));    
    Log.d(TAG, "*** Current Back Cache Size: " + String.valueOf(mBackCache.size()));
    
    for (int i=0; i<lack; ++i) {
      mFrontCache.add(popBackCache());
    }
    printDeck();
  }

  /**
   * Get the card from the top of the cache
   * 
   * @return a card reference
   */
  private Card popBackCache() {
    Card ret;
    // If we reach this scenario it means a lot of cards were looked at during a turn
    // Otherwise it should be filled by a GameManager.maintainDeck call
    if (mBackCache.isEmpty()) {
      mDatabaseOpenHelper.updatePlayDate(mSeenCards);
      mSeenCards.clear();
      this.fillBackCache();
    }
    ret = mBackCache.removeFirst();
    Log.d(TAG, " Popped " + ret.getTitle() + " from cache.");
    return ret;
  }
  

  /**
   * Instantiate the list of Selected Packs and then modify any
   * member variables of Pack that can be determined at time of
   * Deck creation.  This includes number of playable phrases
   * and pack weights.
   */
  private void setPackData() {
    instantiateSelectedPacks();
    mTotalPlayableCards = 0;
    for (Pack pack : mSelectedPacks) {
      mTotalPlayableCards += pack.getSize();
    }
    
    setPackWeights();
  }
  
  /**
   * Prepare for a game by caching the cards necessary for the entire game.  
   * Ideally we should only do this in between games. 
   */
  private void fillBackCache() {
    Log.d(TAG, "fillBackCache()");
    printDeck();
    Log.i(TAG, "filling back cache...");
    mDatabaseOpenHelper = new DeckOpenHelper(mContext);
    
    // 1. Allocate lack to all selected packs
    Log.d(TAG, "1. Allocate lack ");
    int lack = Deck.BACK_CACHE_MAXSIZE - mBackCache.size();
    allocateCardsToPull(lack);
    
    // 2. Fill our cache up with cards from all selected packs (using sorting algorithm)
    Log.d(TAG, "2. Pull Calculations ");
    LinkedList<Card> activeCards = new LinkedList<Card>();
    activeCards.addAll(mFrontCache);
    activeCards.addAll(mBackCache);
    for (int i=0; i<mSelectedPacks.size(); ++i) {
      mBackCache.addAll(mDatabaseOpenHelper.pullFromPack(mSelectedPacks.get(i), activeCards));
    }
    
    // 3. Now shuffle
    Collections.shuffle(mBackCache);
    
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
          Log.e(TAG, "Preference set for a pack that does not exist in the database.");
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
      Log.d(TAG, curPack.toString());
    }
  }
  
  /**
   * Determine based on a lack in the Back Cache how many
   * cards should be pulled from each deck.  After determining
   * the portion each Pack should pull, allocate the remaining
   * cards randomly to the teams.
   * @param lack the number of cards the Back Cache is short by
   */
  private void allocateCardsToPull(int lack) {
    Log.d(TAG, "allocateCardsToPull()");
    Log.d(TAG, "** lack: " + lack);
    
    // Divide up evenly the lack
    int allocated = 0;
    Log.d(TAG, "SELECTED PACKS LENGTH: " + mSelectedPacks.size());
    for (Pack curPack : mSelectedPacks) {
      int numToPull = (int) Math.floor(lack * curPack.getWeight());
      curPack.setNumToPullNext(numToPull);
      allocated += numToPull;
      Log.d(TAG, curPack.toString());
    }
    
    // Allocate randomly any of the lack that remains
    Random randomizer = new Random();
    int remainder = lack - allocated;
    Log.d(TAG, "Assigning remainder of " + remainder + " cards.");
    for (int i=0; i<remainder; ++i) {
      int packIndex = randomizer.nextInt(mSelectedPacks.size()-1);
      Pack pack = mSelectedPacks.get(packIndex);
      pack.setNumToPullNext(pack.getNumToPullNext()+1);
      Log.d(TAG, "..added a remainder to " + pack.getName());
    }
  }
  
  /**
   * Debugging function.  Can be removed later.
   */
  public void printDeck() {
    Log.d(TAG, "printDeck...");
    Log.d(TAG, "========================");
    Log.d(TAG, "FRONT CACHE: ");
    Log.d(TAG, "Front Cache Size is " + mFrontCache.size());
    for (int i=0; i<mFrontCache.size(); ++i) {
      Log.d(TAG, "..." + mFrontCache.get(i).getTitle());
    }
    Log.d(TAG, "END FRONT CACHE");
    Log.d(TAG, "------------------------");
    Log.d(TAG, "BACK CACHE: ");
    Log.d(TAG, "Back Cache Size is " + mBackCache.size());
    for (int i=0; i<mBackCache.size(); ++i) {
      Log.d(TAG, "..." + mBackCache.get(i).getTitle());
    }
    Log.d(TAG, "END BACK CACHE");
    Log.d(TAG, "========================");
  }
  
  
  /**
   * This class creates/opens the database and provides helper functions for
   * batch CRUD operations
   */
  public static class DeckOpenHelper extends SQLiteOpenHelper {

    private final Context mHelperContext;
    private SQLiteDatabase mDatabase;
    private Pack starterPack = new Pack(1, "starterPack", "freepacks/starterPack.json", 
        "Description of pack1", "first install", R.drawable.starter_icon, 125, 0, true);
    
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
     */
    public void installStarterPacks() {
      Log.d(TAG, "installStarterPacks()");
      mDatabase = getWritableDatabase();

      installPackFromResource(mDatabase, starterPack, R.raw.starter);
      
      mDatabase.close();
    }
    
    /**
     * Count all cards in the deck quickly
     * 
     * @return the number of cards in the deck
     */
    public int countAllCards() {
      mDatabase = getReadableDatabase();
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, CardColumns.TABLE_NAME);
      mDatabase.close();
      return ret;
    }
    
    /**
     * Returns an integer count of all phrases associated with the passed in pack names
     * @param packFileNames The filenames of all packs to be counted
     * @return -1 if no phrases found, otherwise the number of phrases found
     */
    public int countCards(LinkedList<Pack> packs) {
      Log.d(TAG, "countCards(LinkedList<String>)");
      mDatabase = getWritableDatabase();
      
      String[] args = new String[2];
      
      args[0] = buildPackIdString(packs);
      args[1] = buildDifficultyString();
      
      //TODO For code review, we are counting size every time we instantiatePacks.
      // Perhaps it would be better to make sure Packs.size stays current?
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
    public int countPacks() {
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
    public LinkedList<Pack> getAllPacksFromDB() {
      Log.d(TAG, "getAllPacksFromDB()");
      mDatabase = getReadableDatabase();
      
      Cursor packQuery = mDatabase.query(PackColumns.TABLE_NAME, PackColumns.COLUMNS,
          null, null, null, null, null);
      
      Pack pack = null;
      LinkedList<Pack> ret = new LinkedList<Pack>();
      if (packQuery.moveToFirst()) {
        while (!packQuery.isAfterLast()) {
          pack = new Pack(packQuery.getInt(0), packQuery.getString(1), packQuery.getString(2),
              packQuery.getString(3), null, R.drawable.starter_icon, packQuery.getInt(4), packQuery.getInt(5), true);
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
    public Pack getPackFromDB(String packId) {
      Log.d(TAG, "getPackFromDB(" + String.valueOf(packId) + ")");
      mDatabase = getReadableDatabase();
      
      String[] id = new String[] {packId};
      
      Cursor packQuery = mDatabase.query(PackColumns.TABLE_NAME,PackColumns.COLUMNS, 
          PackColumns._ID + "=?", id, null, null, null);
      
      Pack pack = null;
      if (packQuery.moveToFirst()) {
        pack = new Pack(packQuery.getInt(0), packQuery.getString(1), packQuery.getString(2),
                        packQuery.getString(3), null, R.drawable.starter_icon, packQuery.getInt(4), packQuery.getInt(5), true);
      }
      packQuery.close();
      mDatabase.close();
      return pack;
    }

    /**
     * Load the words from the JSON file using only one SQLite database. This
     * function loads the words from a json file that is stored as a resource in the project
     * 
     * @param db from the installing context
     * @param packName the name of the file to digest
     * @param resId the resource of the pack file to digest
     */
    public void installPackFromResource(SQLiteDatabase db, Pack pack, int resId) {
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
        Log.e(TAG,"Problem installing pack " + pack.getName() + " from resource.");
        e.printStackTrace();
      }
      CardJSONIterator cardItr = PackParser.parseCards(packBuilder);
      
      installPack(db, pack, cardItr);
      
      Log.d(TAG, "DONE loading words.");
    }


    public void installPackFromServer(Pack serverPack) throws IOException, URISyntaxException {
      Log.d(TAG, "installPackFromServer(" + serverPack.getName() + ")");
      mDatabase = getWritableDatabase();
      // Don't add a pack if it's already there
      int packId = packInstalled(serverPack.getId(), serverPack.getVersion());
      if (packId == PACK_CURRENT) {
        return;
      }
      if(packId == PACK_NOT_PRESENT) { 
        //CardJSONIterator cardItr = PackClient.getInstance().getCardsForPack(serverPack);
        //installPack(mDatabase, serverPack, cardItr);
      }
      else {
        //CardJSONIterator cardItr = PackClient.getInstance().getCardsForPack(serverPack);
        //installPack(mDatabase, serverPack, cardItr);
      }
      Log.d(TAG, "DONE loading words.");
      mDatabase.close();
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
    private synchronized void installPack(SQLiteDatabase db, Pack pack, CardJSONIterator cardItr) {
      Log.d(TAG, "installPack: " + pack.getName() + "v" + String.valueOf(pack.getVersion()));
 
      // Add the pack and all cards in a single transaction.
      db.beginTransaction();
      try {
        // Clear our pack in case it exists (transactions can be nested)
        uninstallPack(String.valueOf(pack.getId()));
        
        Card curCard = null;
        while(cardItr.hasNext()) {
          curCard = cardItr.next();
          upsertCard(curCard, pack.getId(), db);
        }
        upsertPack(pack, db);
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
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
      mDatabase.beginTransaction();
      try {
        mDatabase.delete(CardColumns.TABLE_NAME, CardColumns.PACK_ID + "=?", whereArgs);
        mDatabase.delete(PackColumns.TABLE_NAME, PackColumns._ID + "=?", whereArgs);
        mDatabase.setTransactionSuccessful();
      } finally {
        mDatabase.endTransaction();
      }
    }
    
    /**
     * Replaces existing card if it exists, otherwise inserts the card in the Cards table.
     * 
     * @return rowId or -1 if failed
     */
    public synchronized static long upsertCard(Card card, int packId, SQLiteDatabase db) {
      Log.d(TAG, "upsertCard(" + card.getTitle() + ")");
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
      ContentValues initialValues = new ContentValues();
      initialValues.put(CardColumns._ID, card.getId());
      initialValues.put(CardColumns.TITLE, card.getTitle());
      initialValues.put(CardColumns.BADWORDS, card.getBadWordsString());
      initialValues.put(CardColumns.PLAY_DATE, 0);
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
      packValues.put(PackColumns.DESCRIPTION, pack.getDescription());
      packValues.put(PackColumns.SIZE, pack.getSize());
      packValues.put(PackColumns.VERSION, pack.getVersion());
      return db.replace(PackColumns.TABLE_NAME, null, packValues);
    }

    /**
     * Queries the Packs table and returns the packId if the pack requires updating, 
     * otherwise returns either PACK_CURRENT or PACK_NOT_PRESENT.
     * @param packName 
     * @param packVersion Latest version of the pack
     * @param db
     * @return
     */
    private int packInstalled(int packId, int packVersion) {
      mDatabase = getReadableDatabase();
      String[] packIds= {String.valueOf(packId)};
      Cursor res = mDatabase.query(PackColumns.TABLE_NAME, PackColumns.COLUMNS,
          PackColumns._ID + " = (?)", packIds, null, null, null);
      if (res.getCount() >= 1) {
        res.moveToFirst();
        int oldVersion = res.getInt(3);
        int oldId = res.getInt(0);
        if (packVersion > oldVersion) {
          return oldId;
        } else {
          return PACK_CURRENT;
        }
      } else {
        return PACK_NOT_PRESENT;
      }
    }

    /**
     * Delete a pack from the database
     * @param packId Pack id to delete
     * @param db
     */
    public static void clearPack(int packId, SQLiteDatabase db) {
      String[] whereArgs = new String[] { String.valueOf(packId) };
      db.delete(PackColumns.TABLE_NAME, PackColumns._ID + " = ?", whereArgs);
      db.delete(CardColumns.TABLE_NAME, CardColumns.PACK_ID + " = ?", whereArgs);
    }

    /**
     * Update play_date for all passed in card ids to current time
     * @param ids
     *          comma delimited set of card ids to increment, ex. "1, 2, 4, 10"
     * @return
     */
    public void updatePlayDate(LinkedList<Card> cardList) {
      mDatabase = getWritableDatabase();
      if (Consts.DEBUG) {
        Log.d(TAG, "updatePlaydate()");
      }
      
      String ids = buildCardIdString(cardList);
      
      //TODO for code review:  For some reason the database.update command was interpreting the
      // playcount+1 as a string and inserting that string instead of actually incrementing
      // If its all the same to everyone, I went ahead and hardcoded this query to work around this
      // There is also a known issue with this...if a card is seen twice in a single round
      // it will only get updated once.  To me the issue of seeing a card twice is the real issue
      // and it's not worth fixing the count for that severe case.
//      ContentValues newValues = new ContentValues();
//      newValues.put("playcount", "playcount+1");
//      mDatabase.update(PHRASE_TABLE_NAME, newValues, "id in (" + args + ")", null);
      mDatabase.execSQL("UPDATE " + CardColumns.TABLE_NAME
                      + " SET " + CardColumns.PLAY_DATE + " = datetime('now')"
                      + " WHERE " + CardColumns._ID + " in(" + ids + ");");
      mDatabase.close();
    }

    /**
     * Generates and returns a LinkedList of Cards from the database for a specific pack.  First,
     * we request all the cards from the db sorted by date.  Then we calculate how many of the 
     * Cards should be returned based on the pack's weight relative to the total number of selected
     * cards.  Then, just to shake things up we take a few extra, and remove an equal number at random
     * @param pack The pack from which to pull cards
     * @param cardsToExclude a Linked List of Cards that should not be included in the cache
     * @return
     */
    public LinkedList<Card> pullFromPack(Pack pack, LinkedList<Card> cardsToExclude) {
      Log.d(TAG, "pullFromPack(" + pack.getName() + ")");
      mDatabase = getWritableDatabase();
      
      LinkedList<Card> returnCards = new LinkedList<Card>();
      int packid = pack.getId();
      int targetNum = pack.getNumToPullNext();
      int surplusNum = (int) Math.floor(
                (float) targetNum * ((float) Deck.THROW_BACK_PERCENTAGE / 100.00));
      
      // Build our arguments for SQL
      String[] args = new String[3];
      args[0] = String.valueOf(packid);
      args[1] = buildCardIdString(cardsToExclude);
      args[2] = buildDifficultyString();
      
      // Get the playable cards from pack, sorted by playdate
      Cursor res = mDatabase.query(CardColumns.TABLE_NAME, CardColumns.COLUMNS,
            CardColumns.PACK_ID + " = " + args[0] + " AND " +
            CardColumns._ID + " NOT IN (" + args[1] + ")",
            null, null, null, CardColumns.PLAY_DATE + " asc");
      res.moveToFirst();
      
      // The number of cards to return from any given pack will use the following formula:
      // (WEIGHT OF PACK) * lack + SURPLUS --> Then we randomly take out X cards where X = SURPLUS
      
      Log.d(TAG, "** Deck.BackCacheSize: " + Deck.BACK_CACHE_MAXSIZE);
      Log.d(TAG, "** Pack.numToPullNext: " + targetNum);
      Log.d(TAG, "** this.surplusnum: " + surplusNum);
      Log.d(TAG, "** this.numPlayable (excludes active): " + res.getCount());
      Log.d(TAG, "** Pack.size: " + pack.getSize());
      Log.d(TAG, "** ADDING PHRASES");

      // Add cards to what will be the Cache, including a surplus 
      while (!res.isAfterLast() && res.getPosition() < (targetNum + surplusNum)) {
        returnCards.add(new Card(res.getInt(0), res.getString(1), res.getString(2)));
        res.moveToNext();
      }
      Log.d(TAG, "**" + returnCards.size() + " phrases added.");
      
      // Throw out x surplus cards at random
      Random r = new Random();
      int removeCount = 0;
      int randIndex = 0;
      Log.d(TAG, "** REMOVING PHRASES");
      while (removeCount < surplusNum) {
        randIndex = r.nextInt(returnCards.size()-1);
        Log.d(TAG, "**removing: " + returnCards.get(randIndex).getTitle());
        returnCards.remove(randIndex);
        removeCount++;
      }
      Log.d(TAG, "**" + removeCount + " phrases removed.");
      
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
    private String buildCardIdString(LinkedList<Card> cardList) {
      Log.d(TAG, "buildCardIdString(cardList)");
      String[] ids = new String[cardList.size()];
      
      for (int i=0; i< cardList.size(); ++i) {
        ids[i] = String.valueOf(cardList.get(i).getId());
      }
      return TextUtils.join(",", ids);
    }
    
    /**
     * Helper class to convert a linked list of packs to 
     * a comma-delimited String of Pack Ids.
     * @param packList a list of Packs
     * @return a comma-delimited string of Ids ex. 1,20,22
     */
    private String buildPackIdString(LinkedList<Pack> packList) {
      Log.d(TAG, "buildPackIdString(packList)");
      String[] ids = new String[packList.size()];
      
      for (int i=0; i<packList.size(); ++i) {
        ids[i] = String.valueOf(packList.get(i).getId());
      }
      return TextUtils.join(",", ids);
    }
    
    /**
     * Helper method to build a comma-delimited string of the enabled
     * difficulties
     * @return Comma-delimited string of difficulties for db args
     */
    private String buildDifficultyString() {
      Log.d(TAG, "buildDifficultyString()");
      SharedPreferences prefs = PreferenceManager
          .getDefaultSharedPreferences(mHelperContext);
      
      Boolean easy = prefs.getBoolean("easy_phrases", true);
      Boolean medium = prefs.getBoolean("medium_phrases", true);
      Boolean hard = prefs.getBoolean("hard_phrases", true);
      
      String ret = "";
      if (easy && medium && hard) {
        ret = "0,1,2";
      } else if (easy && hard) {
        ret = "0,2";
      } else if (easy && medium) {
        ret = "0,1";
      } else if (medium && hard) {
        ret = "1,2";
      } else if (easy) {
        ret = "0";
      } else if (medium) {
        ret = "1";
      } else if (hard) {
        ret = "2";
      }
      return ret;
    }
    
    //TODO Let's reconsider if this is the best thing to do after we figure out 
    // how marketplace updates will affect each phone's database
     /**
     * For now, onUpgrade destroys the old database and runs create again.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
          + newVersion + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS cards;");
      db.execSQL("DROP TABLE IF EXISTS cache;");
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
