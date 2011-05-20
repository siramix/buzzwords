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
 * Contains logic to return specific words from the dictionary, and
 * load the dictionary table when it needs to be created.
 */
public class Deck {

	private static final String TAG = "Deck";

  private static final String DATABASE_NAME = "cards";
  private static final int DATABASE_VERSION = 1;
  private static final int DECK_SIZE = 50;
  private static final String CARD_TABLE_CREATE = "CREATE TABLE cards( " + 
    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
    "title TEXT, " +
    "badwords TEXT );";
  private static final String CACHE_TABLE_CREATE = "CREATE TABLE cache( " + 
  "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
  "val TEXT );";
  
  private static final String[] CARD_COLUMNS = {"id","title","badwords"};
  private static final String[] CACHE_COLUMNS = {"id","val"};
  private LinkedList<Card> mDeck;
  private int mSeed;
  private int mPosition;
  private ArrayList<Integer> mOrder;
  private Context mContext;
    	

  private DeckOpenHelper mDatabaseOpenHelper;

    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    public Deck(Context context) {
        mContext = context;
        mDatabaseOpenHelper = new DeckOpenHelper(context);
        mDeck = new LinkedList<Card>();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Random r = new Random();
        mSeed = sp.getInt( "deck_seed", r.nextInt() );
        r = new Random(mSeed);
        Editor editor = sp.edit();
        editor.putInt( "deck_seed", mSeed );
        editor.commit();
        mPosition = sp.getInt( "deck_position", 0 );        
        mOrder = new ArrayList<Integer>(mDatabaseOpenHelper.countCards());
        for(int i = 0; i < mDatabaseOpenHelper.countCards(); ++i )
        {
          mOrder.add(i);
        }
        Collections.shuffle(mOrder, r);
        mDeck = mDatabaseOpenHelper.loadCache();
        //mDatabaseOpenHelper.close();
    }
    
    public void prepareForRound()
    {
      mDatabaseOpenHelper = new DeckOpenHelper(mContext);
      int lack = DECK_SIZE - mDeck.size();
      String ids = "";
      for(int i = 0; i < lack; ++i )
      {
        if( mPosition >= mOrder.size() )
        {
          mPosition = 0;
        }
        
        if( (lack-1) == i )
        {
          ids += mOrder.get(mPosition++); 
        }
        else
        {
          ids += mOrder.get(mPosition++) + ","; 
        }
      }
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
      Editor editor = sp.edit();
      editor.putInt( "deck_position", mPosition );
      editor.commit();
      mDeck.addAll(mDatabaseOpenHelper.getCards(ids));
      Collections.shuffle(mDeck);
      mDatabaseOpenHelper.saveCache(mDeck);
      mDatabaseOpenHelper.close();
    }
   
    
    public Card getCard()
    {
      Log.d(TAG,"getCard()");
      if(mDeck.isEmpty())
      {
        this.prepareForRound();
        return mDeck.removeFirst();
      }
      else {
      return mDeck.removeFirst();
      }
    }


    /**
     * This creates/opens the database.
     */
    private static class DeckOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        DeckOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CARD_TABLE_CREATE);
            db.execSQL(CACHE_TABLE_CREATE);
            loadDeck(db);
        }
        
        public int countCards()
        {
          mDatabase = getWritableDatabase();
          int ret = (int)DatabaseUtils.queryNumEntries(mDatabase, "cards");
          return ret;
        }
        
        private int countCaches()
        {
          mDatabase = getWritableDatabase();
          int ret = (int)DatabaseUtils.queryNumEntries(mDatabase, "cache");
          return ret;
        }

        /**
         * Starts a thread to load the database table with words
         */
        private void loadDeck(SQLiteDatabase db) {
          mDatabase = db;
          new Thread(new Runnable() {
            public void run() {
              try {
                loadWords();
              } catch(IOException e) {
                 throw new RuntimeException(e);
              }
            }
          }).start();          
        }

        private void loadWords() throws IOException {
            Log.d(TAG, "Loading words...");
            
            InputStream starterXML =
                mHelperContext.getResources().openRawResource(R.raw.starter);
              DocumentBuilderFactory docBuilderFactory =
                DocumentBuilderFactory.newInstance();
              
              Log.d( TAG, "Building DocBuilderFactory for card pack parsing from " + R.class.toString() );
              try
              {
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(starterXML);
                NodeList cardNodes = doc.getElementsByTagName( "card" );
                for(int i = 0; i < cardNodes.getLength(); i++)
                {
                  NodeList titleWhiteAndBads = cardNodes.item( i ).getChildNodes();
                  Node titleNode = null;
                  Node badsNode = null;
                  for( int j = 0; j < titleWhiteAndBads.getLength(); j++ )
                  {
                    String candidateName = titleWhiteAndBads.item( j ).getNodeName();
                    if( candidateName.equals( "title" ) )
                    {
                      titleNode = titleWhiteAndBads.item( j );
                    }
                    else if( candidateName.equals( "bad-words" ) )
                    {
                      badsNode = titleWhiteAndBads.item( j );
                    }
                    else
                    {
                      continue; // We found some #text
                    }
                  }
                  String title = titleNode.getFirstChild().getNodeValue();
                  String badWords = "";
                  NodeList bads = badsNode.getChildNodes();
                  for( int j = 0; j < bads.getLength(); j++ )
                  {
                    String candidateName = bads.item( j ).getNodeName();
                    if( candidateName.equals( "word" ) )
                    {
                      badWords += bads.item( j ).getFirstChild().getNodeValue() + ",";
                    }
                  }
                  // hack because I have a comma at the end
                  badWords = badWords.substring( 0, badWords.length() - 1 );
                  this.addWord(i, title, badWords, mDatabase);
                  
                }
              }
              catch( ParserConfigurationException e )
              {
                e.printStackTrace();
              }
              catch( SAXException e )
              {
                e.printStackTrace();
              }
              catch( IOException e )
              {
                e.printStackTrace();
              }
            
            Log.d(TAG, "DONE loading words.");
        }

        /**
         * Add a word to the dictionary.
         * @return rowId or -1 if failed
         */
        public long addWord(int id, String title, String badWords, SQLiteDatabase db) {
            Log.d(TAG,"addWord()");
            ContentValues initialValues = new ContentValues();
            initialValues.put("id", id);
            initialValues.put("title", title);
            initialValues.put("badwords", badWords);
            return db.insert("cards", null, initialValues);
        }
        
        public LinkedList<Card> getCards(String args )
        {
          mDatabase = getWritableDatabase();
          Log.d(TAG,"getCards()");
          Log.d(TAG,args);
          Cursor res = mDatabase.query("cards", CARD_COLUMNS, "id in ("+args+")", null, null, null, null);
          res.moveToFirst();
          LinkedList<Card> ret = new LinkedList<Card>();
          while(!res.isAfterLast())
          {
            Log.d(TAG,res.getString(1));
            ret.add( new Card(res.getInt(0),res.getString(1), res.getString(2)) );
            res.moveToNext();
          }
          res.close();
          return ret;
        }
        
        public long saveCache( LinkedList<Card> cache)
        {
          mDatabase = getWritableDatabase();
          Log.d(TAG,"saveCache()");
          String cacheString = "";
          for( Iterator<Card> itr = cache.iterator(); itr.hasNext(); )
          {
            cacheString += itr.next().getId() + ",";
          }
          cacheString = cacheString.substring(0, cacheString.length()-1);
          ContentValues values = new ContentValues();
          values.put("id", 0);
          values.put("val", cacheString);
          long ret;
          if( this.countCaches() >= 1)
          {
            ret = mDatabase.update("cache", values,"",null);
          }
          else
          {            
            ret = mDatabase.insert("cache", null, values);
          }
          return ret;
          
        }
        
        public LinkedList<Card> loadCache()
        {
          mDatabase = getWritableDatabase();
          Log.d(TAG,"loadCache()");
          Cursor res = mDatabase.query("cache",CACHE_COLUMNS, "id in (0)", null, null, null, null);
          LinkedList<Card> ret;
          if( res.getCount() == 0 )
          {
            ret = new LinkedList<Card>();
          }
          else
          {
            res.moveToFirst();
            ret = getCards(res.getString(1));
          }
          res.close();
          return ret;
            
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS cards;");
            db.execSQL("DROP TABLE IF EXISTS cache;");
            onCreate(db);
        }
        
        @Override
        public void close() {
          super.close();
          if(mDatabase != null) {
            mDatabase.close();
          }
        }
    }

}
