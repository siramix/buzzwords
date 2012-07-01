package com.buzzwords;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.buzzwords.R;

import android.util.Log;

/**
 * The Card class is a simple container class for storing data associated with
 * cards
 * 
 * @author Siramix Labs
 */
public class Card implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -5094548104192852941L;

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "Card";

  /**
   * R-W-S Constants
   */
  public static final int RIGHT = 0;
  public static final int WRONG = 1;
  public static final int SKIP = 2;
  public static final int NOTSET = 3;

  /**
   * The db id of the card
   */
  private int mId;

  /**
   * The right,wrong,skip, not set {0,1,2,3} state of the card
   */
  private int mRws;

  /**
   * The title of the card, the word to be guessed
   */
  private String mTitle;

  /**
   * An array list of the words you cannot say
   */
  private ArrayList<String> mBadWords;

  /**
   * The Pack to which the card belongs
   */
  private Pack mPack;
  
  /**
   * Function for breaking a string into an array list of strings based on the
   * presence of commas. The bad words are stored in the database as a comma
   * separated list for each card.
   * 
   * @param commaSeparated
   *          - a comma separated string
   * @return an array list of the substrings
   */
  public static ArrayList<String> bustString(String commaSeparated) {
    ArrayList<String> ret = new ArrayList<String>();
    StringTokenizer tok = new StringTokenizer(commaSeparated);

    while (tok.hasMoreTokens()) {
      ret.add(tok.nextToken(",").toUpperCase());
    }

    return ret;
  }
  
  /**
   * Get the resource ID for this card's right wrong skip icon Mid-turn (when
   * user hits back). These IDs must differ from those on Turn Result Screen.
   */
  public static int getCardMarkDrawableId(int cardRWS) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "getCardMarkDrawableId()");
    }
    switch (cardRWS) {
    case RIGHT:
      return R.drawable.stamp_right;
    case WRONG:
      return R.drawable.stamp_wrong;
    case SKIP:
      return R.drawable.stamp_skip;
    case NOTSET:
      //TODO Edward fill this in with the right drawable for NotSet
      return 3;
    default:
      return -1;
    }
  }

  /**
   * Default constructor
   */
  public Card() {
    this.init(NOTSET, NOTSET, "", new ArrayList<String>(), new Pack());
  }

  /**
   * Copy Constructor
   */
  public Card(Card rhs) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "Card( Card )");
    }
    ArrayList<String> bws = new ArrayList<String>(rhs.getBadWords());
    this.init(rhs.getId(), rhs.getRws(), rhs.getTitle(), bws, rhs.getPack());
  }

  /**
   * Standard constructor accepting all members as their native types
   * 
   * @param id
   * @param rws
   * @param title
   * @param badWords
   */
  public Card(int id, int rws, String title, ArrayList<String> badWords, Pack pack) {
    this.init(id, rws, title, badWords, pack);
  }

  /**
   * Shortcut constructor for comma-separated bad word entry
   * 
   * @param id
   * @param title
   * @param badWords
   * @param pack Can be set to null if unset
   */
  public Card(int id, String title, String badWords, Pack pack) {
    this.init(id, NOTSET, title, Card.bustString(badWords), pack);
  }

  /**
   * Equals function for comparison
   */
  @Override
  public boolean equals(Object compareObj) {
    if (this == compareObj) {
      return true;
    }

    if (compareObj == null) {
      return false;
    }

    if (!(compareObj instanceof Card)) {
      return false;
    }
    Card rhs = (Card) compareObj;
    return mBadWords.equals(rhs.getBadWords()) && mRws == rhs.getRws()
        && mTitle.equals(rhs.getTitle());
  }

  /**
   * Function for initializing card state
   */
  private void init(int id, int rws, String title, ArrayList<String> badWords, Pack pack) {
    mId = id;
    mRws = rws;
    mTitle = title;
    mBadWords = badWords;
    mPack = pack;
  }

  /**
   * Get the right/wrong/skip state as an integer
   * 
   * @return
   */
  public int getRws() {
    return mRws;
  }

  /**
   * Set the right/wrong/skip state as an integer
   * 
   * @param rws
   */
  public void setRws(int rws) {
    Log.d(TAG, "setRws(" + String.valueOf(rws) + ")");
    mRws = rws;
  }

  /**
   * Get a reference to the title string
   * 
   * @return
   */
  public String getTitle() {
    return mTitle;
  }

  /**
   * Set the title as a string
   * 
   * @param title
   */
  public void setTitle(String title) {
    Log.d(TAG, "setTitle()");
    mTitle = title;
  }

  /**
   * Get the bad words as an ArrayList reference
   * 
   * @return an array list of bad words
   */
  public ArrayList<String> getBadWords() {
    return mBadWords;
  }
  
  /**
   * Get the array list of bad words as a comma separated
   * string of words
   * @return a comma separated string of badwords
   */
  public String getBadWordsString() {
    String badwordString = mBadWords.toString();
    return badwordString.substring(1, badwordString.length()-1);
  }

  /**
   * Set the bad words as an array list of strings
   * 
   * @param badWords
   */
  public void setBadWords(ArrayList<String> badWords) {
    Log.d(TAG, "setBadWords(ArrayList<String>)");
    mBadWords = badWords;
  }

  /**
   * Override setter for a comma-separated string
   * 
   * @param commaSeparated
   */
  public void setBadWords(String commaSeparated) {
    Log.d(TAG, "setBadWords(String)");
    mBadWords = Card.bustString(commaSeparated);
  }

  /**
   * Get the resource ID for this card's right wrong skip icon
   */
  public int getRowEndDrawableId() {
    switch (mRws) {
    case RIGHT:
      return R.drawable.turnsum_right;
    case WRONG:
      return R.drawable.turnsum_wrong;
    case SKIP:
      return R.drawable.turnsum_skip;
    case NOTSET:
      return R.drawable.turnsum_notset;
    default:
      return 0;
    }
  }

  /**
   * Sets a card's id (from DB)
   * 
   * @param id
   */
  public void setId(int id) {
    mId = id;
  }

  /**
   * @return the id (DB)
   */
  public int getId() {
    return mId;
  }

  /**
   * @return The pack to which the card belongs
   */
  public Pack getPack() {
    return mPack;
  }
}
