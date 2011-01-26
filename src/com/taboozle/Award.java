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
   * Default Constructor 
   */
  public Award()
  {
    this.id = -1;
    this.name = "";
    this.explanation = "";
    this.priority = 3;
  }
  
  /**
   * Standard Constructor
   */
  public Award( int id, String name, String explanation, int priority )
  {
    this.id = id;
    this.name = name;
    this.explanation = explanation;
    this.priority = priority;
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
    awards.add( new Award( 0,"You Got it, Dude","Most Right", 1 ) );
    awards.add( new Award( 1,"Total Screwups","Most Incorrect", 1 ) );
    awards.add( new Award( 2,"Sultans of Swipe","Most Skips", 1 ) );
    awards.add( new Award( 3,"Fast and Furious","Highest Scoring Round", 1 ) );
    awards.add( new Award( 4,"2 Fast, 2 Furious","Most Correct in Round + not Highest Scoring", 2 ) );
    awards.add( new Award( 5,"Slackers","Most Skipped in a Round", 1 ) );
    awards.add( new Award( 6,"Foot in Mouth","Most Incorrect in a Round", 1 ) );
    awards.add( new Award( 7,"Paralyzed by Fear","Only skipped cards", 2 ) );
    awards.add( new Award( 8,"Dead Weight","Negative Points", 2 ) );
    awards.add( new Award( 9,"Pointless","Zero Points", 2 ) );
    awards.add( new Award( 10,"Out to Lunch","Complete no actions", 2 ) );
    awards.add( new Award( 11,"Quickdraw","Fastest Correct under 5s", 3 ) );
    awards.add( new Award( 12,"Glass Half Empty", "Fastest Skip under 5s", 3 ) );
    awards.add( new Award( 13,"Over Before it Started","Fastest Buzz under 5s", 3 ) );
    awards.add( new Award( 14,"Took You Long Enough!","Slowest Correct", 3 ) );
    awards.add( new Award( 15,"...Did I do that?","Slowest Buzz", 3 ) );
    awards.add( new Award( 16,"Worst Guessers","Slowest Skip", 3 ) );
    awards.add( new Award( 17,"He's on Fire!","Correct Streak", 2 ) );
    awards.add( new Award( 18,"Iced","Wrong Streak", 2 ) );
    awards.add( new Award( 19,"Skip Sandwich DX","Skip Streak", 2 ) );
    //awards.add( new Award( 20,"Comeback Kings","Come back from an X point deficit to win", 3 ) );
    //awards.add( new Award( 21,"Dirty Cheaters","Win by a spread equal to or greater than 2nd place score", 3 ) );
    awards.add( new Award( 22,"Dominated","Be last and lose to next lowest player by half their score", 3 ) );
    awards.add( new Award( 23,"Slowpokes","Fewest cards seen", 1 ) );
    awards.add( new Award( 24,"Cosmpolitan","Most cards seen", 1 ) );
    awards.add( new Award( 25,"1st Place","", 0 ) );
    awards.add( new Award( 26,"2nd Place","", 0 ) );
    awards.add( new Award( 27,"3rd Place","", 0 ) );
    awards.add( new Award( 28,"4th Place","", 0 ) );
    Collections.sort(awards, (new Award()).new PriorityComparator());
  }
  
}
