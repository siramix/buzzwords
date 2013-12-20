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
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Siramix Labs
 *
 */
public class DeckOpenHelper extends SQLiteOpenHelper {

  private SQLiteDatabase mDatabase;
  private static DeckOpenHelper mInstance = null;

  private static String TAG = "DeckOpenHelper";
  
  private static final String RAND_DATE_STR = "datetime(abs(random())%1000000000, 'unixepoch')";

  /**
   * Method for getting the singleton DeckOpenHelper
   * @param context the context with which to potentially construct a new singleton
   * @return the DeckOpenHelper
   */
  public static DeckOpenHelper getInstance(Context context) {
    if(mInstance == null) {
      mInstance = new DeckOpenHelper(context);
    }
    return mInstance;
  }
  
  /**
   * Default Constructor from superclass
   * 
   * @param context
   */
  DeckOpenHelper(Context context) {
    super(context, Consts.DATABASE_NAME, null, Consts.DATABASE_VERSION);
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
            packQuery.getInt(5), packQuery.getInt(6), true, "");
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
    mDatabase = getReadableDatabase();
    
    String[] id = new String[] {packId};
    
    Cursor packQuery = mDatabase.query(PackColumns.TABLE_NAME,PackColumns.COLUMNS, 
        PackColumns._ID + "=?", id, null, null, null);
    
    Pack pack = null;
    if (packQuery.moveToFirst()) {
      pack = new Pack(packQuery.getInt(0), packQuery.getString(1), packQuery.getString(2),
                      packQuery.getString(3), packQuery.getString(4), -1, 
                      packQuery.getInt(5), packQuery.getInt(6), true, "");
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
   * @param context the context from which to deal with resources
   * @throws RuntimeException 
   */
  public synchronized void installPackFromResource(Pack pack, int resId,
      Context context) throws RuntimeException {
    Log.d(TAG, "Installing pack from resource (" + pack.getName() + ")");

    BufferedReader packJSON = new BufferedReader(new InputStreamReader(
        context.getResources().openRawResource(resId)));
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
  public synchronized void installLatestPackFromServer(Pack serverPack) throws RuntimeException {
    Log.d(TAG, "installPackFromServer(" + serverPack.getName() + ")");
    int packId = packInstalled(serverPack.getId(), serverPack.getVersion());

    CardJSONIterator cardItr;
    
    // Don't add a pack if it's already there
    if (packId == Consts.PACK_CURRENT) {
      Log.d(TAG, "No update required, pack " + serverPack.getName() + " current.");
      return;
    }
    if(packId == Consts.PACK_NOT_PRESENT) { 
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
   * Delete the pack and cards associated with a given Pack Id.  Will first
   * check that the pack exists before attempting to perform any deletions.
   * @param packId to remove
   */
  public synchronized void uninstallPack(String packId) {
    Log.d(TAG, "uninstallPack(" + packId + ")");
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
    // Get a random year to make sure installations have different play orders.
    long rangebegin = Timestamp.valueOf("1970-01-01 00:00:00").getTime();
    long rangeend = Timestamp.valueOf("1999-01-01 00:00:00").getTime();
    long diff = rangeend - rangebegin + 1;
    Timestamp rand = new Timestamp(rangebegin + (long)(Math.random() * diff));
    ContentValues initialValues = new ContentValues();
    initialValues.put(CardColumns._ID, card.getId());
    initialValues.put(CardColumns.TITLE, card.getTitle());
    initialValues.put(CardColumns.BADWORDS, card.getBadWordsString());
    initialValues.put(CardColumns.PLAY_DATE, rand.toString());
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
  public synchronized int packInstalled(int packId, int packVersion) {
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
        return Consts.PACK_CURRENT;
      }
    } else {
      res.close();
      mDatabase.close();
      
      return Consts.PACK_NOT_PRESENT;
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
  public synchronized LinkedList<Card> pullFromPack(Pack pack) {
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
          null, null, null, CardColumns.PLAY_DATE + " ASC, " + CardColumns.TIMES_SEEN + " ASC", args[1]);
    res.moveToFirst();
    
    // The number of cards to return from any given pack will use the following formula:
    // (WEIGHT OF PACK) * CACHE-SIZE

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
    Log.d(TAG, "Upgrading database from version " + oldVersion + " to "
        + newVersion + ", which should update the ");
    // Call any migrations or scripts that need to run on update.
    DeleteLitePack(db);
    FixTimestamps(db);
  }
  
  /*
   * Fix the timestamps to be all 1970+ (unixepoch). Dates were being sorted as strings which
   * meant that 2013 was being marked as older than 21 (year 21) for example.
   */
  private void FixTimestamps(SQLiteDatabase db) {
    db.execSQL("UPDATE " + CardColumns.TABLE_NAME +
        " SET " + CardColumns.PLAY_DATE + " = " + RAND_DATE_STR +
        " WHERE times_seen = 0;");
  }

  /*
   * Remove the Lite pack from installs which had the lite pack in their pack list.
   */
  private void DeleteLitePack(SQLiteDatabase db) {
    db.execSQL("DELETE FROM " + PackColumns.TABLE_NAME +
        " WHERE " + PackColumns._ID + " = 0;");
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

}
