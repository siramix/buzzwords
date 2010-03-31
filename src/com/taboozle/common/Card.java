/**
 * 
 */
package com.taboozle.common;

import java.util.ArrayList;

/**
 * Card class for storing words and the words you should not and cannot say.
 * @author The Taboozle Team
 *
 */
public class Card {

  /**
   * Data members
   */
  protected String Name;
  protected ArrayList<String> BadWords;

  /**
   * Card default constructor
   */
  public Card() {
    this("",0);
  }

  /** 
   * Card standard constructor
   */
  public Card( String name, int numBadWords ) {
    this.Name = name;
    this.BadWords = new ArrayList<String>(numBadWords);
  }

  /**
   * GetName access function
   * @return the name of the card
   */
  public String GetName()
  {
    return this.Name;
  }

  /**
   * GetBadWords access function
   * @return a string array of the words the player is not allowed to say
   */
  public String[] GetBadWords()
  {
    String[] str = new String[this.BadWords.size()];
    for(int i = 0; i < this.BadWords.size(); i++)
    {
      str[i] = this.BadWords.get(i);
    }
    return str;
  }

  /**
   * AddBadWords function to add a string array to the words you cannot say
   * stored within the card.
   * @param badWords - the words you wish to add to the forbidden words
   */
  public void AddBadWords(String[] badWords)
  {
    for(int i = 0; i < badWords.length; i++)
    {
      this.BadWords.add(badWords[i]);
    }
  }
}
