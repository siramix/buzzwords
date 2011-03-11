/**
 * 
 */
package com.taboozle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Taboozle Team
 */
public class Award
{
  
  /**
   * All of the awards
   */
  public static ArrayList<Award> awards;
  
  /**
   * ID of the award
   */
  public int id;
  
  /**
   * Priority of the award (greater is better)
   */
  public int priority;
  
  /**
   * Name of the award
   */
  public String name;
  
  /**
   * Explanation of the award
   */
  public String explanation;
  
  /**
   * Drawable icon ID for display on Game End
   */
  public int iconID;

  /**
   * Default Constructor 
   */
  public Award()
  {
    this.id = -1;
    this.name = "";
    this.explanation = "";
    this.priority = 3;
    this.iconID = R.drawable.award_cosmo;
  }
  
  /**
   * Standard Constructor
   */
  public Award( int id, String name, String explanation, int priority, int iconID )
  {
    this.id = id;
    this.name = name;
    this.explanation = explanation;
    this.priority = priority;
    this.iconID = iconID;
  }
  
  /**
   * Standard getter for Id
   * @return id
   */
  public int getId() {
    return this.id;
  }

  /**
   * Standard getter for Priority
   * @return priority
   */
  public int getPriority() {
    return this.priority;
  }

  /**
   * Standard getter for Name
   * @return Name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Standard getter for Explanation
   * @return explanation
   */
  public String getExplanation() {
    return this.explanation;
  }
  
  /**
   * Standard getter for the icon resource ID
   */
  public int getIconID() {
    return this.iconID;
  }
  
  /**
   * Comparison method added for testing Awards.
   */
  @Override
  public boolean equals(Object compareObj)
  {
    if(this == compareObj)
    {
      return true;
    }
   
    if(compareObj == null)
    {
      return false;
    }
   
    if(!(compareObj instanceof Award))
    {
      return false;
    }
    Award rhs = (Award) compareObj;
    return this.id == rhs.getId() && this.name.equals( rhs.getName() ) && 
      this.explanation.equals( rhs.getExplanation() ) &&
      this.priority == rhs.getPriority();    
  }  

  class PriorityComparator implements Comparator<Award>
  {
    
    public int compare(Award award1, Award award2)
    {
 
        int p1 = award1.priority;        
        int p2 = award2.priority;
       
        if(p1 > p2)
        {
          return 1;
        }
        else if(p1 < p2)
        {
          return -1;
        }
        else
        {
          return 0;
        }
    }
}
  
  static
  {
    awards = new ArrayList<Award>();
    awards.add( new Award( 0,"You Got it, Dude","Most correct overall", 1, R.drawable.award_blank ) );
    awards.add( new Award( 1,"Total Screwups","Most incorrect overall", 1, R.drawable.award_screwups ) );
    awards.add( new Award( 2,"Sultans of Swipe","Most skips overall", 1, R.drawable.award_sultan ) );
    awards.add( new Award( 3,"Fast and Furious","Highest scoring round", 1, R.drawable.award_blank ) );
    awards.add( new Award( 4,"2 Fast, 2 Furious","Most correct in round without winning", 2, R.drawable.award_blank ) );
    awards.add( new Award( 5,"Slackers","Most skips in a round", 1, R.drawable.award_slackers ) );
    awards.add( new Award( 6,"Foot in Mouth","Most incorrect in a round", 1, R.drawable.award_blank ) );
    awards.add( new Award( 7,"Paralyzed by Fear","Only skipped cards for an entire round", 2, R.drawable.award_paralyzed ) );
    awards.add( new Award( 8,"Dead Weight","Negative scoring round", 2, R.drawable.award_deadweight ) );
    awards.add( new Award( 9,"Pointless","Zero point round", 2, R.drawable.award_pointless ) );
    awards.add( new Award( 10,"Out to Lunch","Take no action in a round", 2, R.drawable.award_outtolunch ) );
    awards.add( new Award( 11,"Quickdraw","Very fast correct", 3, R.drawable.award_quickdraw ) );
    awards.add( new Award( 12,"Glass Half Empty", "Very fast skip", 1, R.drawable.award_blank ) );
    awards.add( new Award( 13,"Over Before it Started","Buzzed very quickly", 3, R.drawable.award_blank ) );
    awards.add( new Award( 14,"Took You Long Enough!","Very slow correct", 3, R.drawable.award_blank ) );
    awards.add( new Award( 15,"...Did I do that?","Buzzed after a long time", 3, R.drawable.award_blank ) );
    awards.add( new Award( 16,"Worst Guessers","Skipped after a long time", 3, R.drawable.award_blank ) );
    awards.add( new Award( 17,"He's on Fire!","Correct streak", 2, R.drawable.award_blank ) );
    awards.add( new Award( 18,"Iced","Wrong streak", 2, R.drawable.award_blank ) );
    awards.add( new Award( 19,"Skip Sandwich DX","Skip streak", 2, R.drawable.award_skipsandwich ) );
    //awards.add( new Award( 20,"Comeback Kings","Come back from an X point deficit to win", 3 ) );
    awards.add( new Award( 21,"Dirty Cheaters","Double the score of second place team", 3, R.drawable.award_blank ) );
    awards.add( new Award( 22,"Dominated","Lose with a score half as large as next team", 3, R.drawable.award_blank ) );
    awards.add( new Award( 23,"Slowpokes","Fewest cards seen", 1, R.drawable.award_slowpokes ) );
    awards.add( new Award( 24,"Cosmpolitan","Most cards seen", 1, R.drawable.award_cosmo ) );
    awards.add( new Award( 25,"1st Place","", 0, R.drawable.award_first ) );
    awards.add( new Award( 26,"2nd Place","", 0, R.drawable.award_second ) );
    awards.add( new Award( 27,"3rd Place","", 0, R.drawable.award_third ) );
    awards.add( new Award( 28,"4th Place","", 0, R.drawable.award_fourth ) );
    Collections.sort(awards, (new Award()).new PriorityComparator());
  }
  
}
