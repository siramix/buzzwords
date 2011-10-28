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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
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
  private static final int DATABASE_VERSION = 2;
  private static final int CACHE_SIZE = 50;
  private static final String CARD_TABLE_CREATE = "CREATE TABLE "
      + CARD_TABLE_NAME + "( " + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
      + "title TEXT, " + "badwords TEXT );";
  private static final String CACHE_TABLE_CREATE = "CREATE TABLE "
      + CACHE_TABLE_NAME + "( " + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
      + "val TEXT );";

  private static final String[] CARD_COLUMNS = { "id", "title", "badwords" };
  private static final String[] CACHE_COLUMNS = { "id", "val" };
  private LinkedList<Card> mCache;
  private int mSeed;
  private int mPosition;
  private ArrayList<Integer> mOrder;
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
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(context);
    Random r = new Random();
    mSeed = sp.getInt("deck_seed", r.nextInt());
    r = new Random(mSeed);
    Editor editor = sp.edit();
    editor.putInt("deck_seed", mSeed);
    editor.commit();
    mPosition = sp.getInt("deck_position", 0);
    int sizeOfDeck = mDatabaseOpenHelper.countCards();
    mOrder = new ArrayList<Integer>(sizeOfDeck);
    for (int i = 0; i < sizeOfDeck; ++i) {
      mOrder.add(i);
    }
    Collections.shuffle(mOrder, r);
    mCache = mDatabaseOpenHelper.loadCache();
    mDatabaseOpenHelper.close();
  }

  /**
   * Prepare for a round by caching the cards necessary for that round (get the
   * number of cards back up to DECK_SIZE). Refilling the cache
   */
  public void prepareForRound() {
    mDatabaseOpenHelper = new DeckOpenHelper(mContext);
    
    // Find out the indices we need to fill the cache
    int lack = CACHE_SIZE - mCache.size();
    String ids = "";
    for (int i = 0; i < lack; ++i) {
      if (mPosition >= mOrder.size()) {
        mPosition = 0;
      }

      ids += mOrder.get(mPosition++);
      if (i < (lack - 1)) {
        ids += ",";
      }

    }

    // Update our position in the application prefs
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(mContext);
    Editor editor = sp.edit();
    editor.putInt("deck_position", mPosition);
    editor.commit();

    // Fill the cache from the deck (DB)
    mCache.addAll(mDatabaseOpenHelper.getCards(ids));
    Collections.shuffle(mCache);
    mDatabaseOpenHelper.saveCache(mCache);
    mDatabaseOpenHelper.close();
  }

  /**
   * Get the card from the top of the cache
   * 
   * @return a card reference
   */
  public Card getCard() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getCard()");
    }
    if (mCache.isEmpty()) {
      this.prepareForRound();
      return mCache.removeFirst();
    } else {
      return mCache.removeFirst();
    }
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
      db.execSQL(CARD_TABLE_CREATE);
      db.execSQL(CACHE_TABLE_CREATE);
      loadWords(db);
    }

    /**
     * Count the cards in the deck quickly
     * 
     * @return the number of cards in the deck
     */
    public int countCards() {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "countCards()");
      }
      mDatabase = getWritableDatabase();
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, CARD_TABLE_NAME);
      return ret;
    }

    /**
     * Count the number of cache entries (This should NEVER be > 1)
     * 
     * @return the number of cache entries
     */
    public int countCaches() {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "countCaches()");
      }
      mDatabase = getWritableDatabase();
      int ret = (int) DatabaseUtils.queryNumEntries(mDatabase, CACHE_TABLE_NAME);
      return ret;
    }

    /**
     * Load the words from the XML file using only one SQLite database
     * 
     * @param db
     *          from the installing context
     */
    private void loadWords(SQLiteDatabase db) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "Loading words...");
      }

      mDatabase = db;

      InputStream starterXML = mHelperContext.getResources().openRawResource(
          R.raw.starter);
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
          .newInstance();

      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "Building DocBuilderFactory for card pack parsing from "
            + R.class.toString());
      }
      try {
        mDatabase.beginTransaction();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(starterXML);
        NodeList cardNodes = doc.getElementsByTagName("card");
        for (int i = 0; i < cardNodes.getLength(); i++) {
          NodeList titleWhiteAndBads = cardNodes.item(i).getChildNodes();
          Node titleNode = null;
          Node badsNode = null;
          for (int j = 0; j < titleWhiteAndBads.getLength(); j++) {
            String candidateName = titleWhiteAndBads.item(j).getNodeName();
            if (candidateName.equals("title")) {
              titleNode = titleWhiteAndBads.item(j);
            } else if (candidateName.equals("bad-words")) {
              badsNode = titleWhiteAndBads.item(j);
            } else {
              continue; // We found some #text
            }
          }
          String title = titleNode.getFirstChild().getNodeValue();
          String badWords = "";
          NodeList bads = badsNode.getChildNodes();
          for (int j = 0; j < bads.getLength(); j++) {
            String candidateName = bads.item(j).getNodeName();
            if (candidateName.equals("word")) {
              badWords += bads.item(j).getFirstChild().getNodeValue();
              if (j < (bads.getLength() - 1)) {
                badWords += ",";
              }
            }
          }
          this.addWord(i, title, badWords, mDatabase);
        }
        mDatabase.setTransactionSuccessful();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        mDatabase.endTransaction();
      }

      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "DONE loading words.");
      }
    }

    /**
     * Add a word to the deck (DB)
     * 
     * @return rowId or -1 if failed
     */
    public long addWord(int id, String title, String badWords, SQLiteDatabase db) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "addWord()");
      }
      ContentValues initialValues = new ContentValues();
      initialValues.put("id", id);
      initialValues.put("title", title);
      initialValues.put("badwords", badWords);
      return db.insert(CARD_TABLE_NAME, null, initialValues);
    }

    /**
     * Get the cards corresponding to a comma-separated list of indices
     * 
     * @param args
     *          indices separated by commas
     * @return a reference to a linked list of cards corresponding to the ids
     */
    public LinkedList<Card> getCards(String args) {
      mDatabase = getWritableDatabase();
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "getCards()");
      }
      Cursor res = mDatabase.query(CARD_TABLE_NAME, CARD_COLUMNS, "id in ("
          + args + ")", null, null, null, null);
      res.moveToFirst();
      LinkedList<Card> ret = new LinkedList<Card>();
      while (!res.isAfterLast()) {
        if (BuzzWordsApplication.DEBUG) {
          Log.d(TAG, res.getString(1));
        }
        ret.add(new Card(res.getInt(0), res.getString(1), res.getString(2)));
        res.moveToNext();
      }
      res.close();
      return ret;
    }

    /**
     * Saves the cache in the database
     * 
     * @param cache
     *          Linked list of cards to insert (by DB index)
     */
    public void saveCache(LinkedList<Card> cache) {
      mDatabase = getWritableDatabase();
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "saveCache()");
      }
      String cacheString = "";
      for (Iterator<Card> itr = cache.iterator(); itr.hasNext();) {
        cacheString += itr.next().getId();
        if (itr.hasNext()) {
          cacheString += ",";
        }
      }
      
      ContentValues values = new ContentValues();
      values.put("id", 0);
      values.put("val", cacheString);
      if (this.countCaches() >= 1) {
        mDatabase.update(CACHE_TABLE_NAME, values, "", null);
      } else {
        mDatabase.insert(CACHE_TABLE_NAME, null, values);
      }
    }

    /**
     * Load a cache from the database into cards in memory
     * @return the linked list of cards loaded into memory
     */
    public LinkedList<Card> loadCache() {
      mDatabase = getWritableDatabase();
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "loadCache()");
      }
      Cursor res = mDatabase.query(CACHE_TABLE_NAME, CACHE_COLUMNS, "id in (0)", null,
          null, null, null);
      LinkedList<Card> ret;
      if (res.getCount() == 0) {
        ret = new LinkedList<Card>();
      } else {
        res.moveToFirst();
        ret = getCards(res.getString(1));
      }
      res.close();
      return ret;

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
