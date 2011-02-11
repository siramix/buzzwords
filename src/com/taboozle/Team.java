/**
 * 
 */
package com.taboozle;

import java.util.Comparator;


/**
 * @author cpatrick
 *
 */
public enum Team
{
  TEAMA( "Blue", R.color.teamA_BG, R.color.teamA_CardText, 
         R.color.teamA_secondary, R.color.teamA_text, R.drawable.bg_bluegradient ),
  TEAMB( "Green", R.color.teamB_BG, R.color.teamB_CardText, 
         R.color.teamB_secondary, R.color.teamB_text, R.drawable.bg_greengradient ),
  TEAMC( "Red", R.color.teamC_BG, R.color.teamC_CardText, 
         R.color.teamC_secondary, R.color.teamC_text, R.drawable.bg_redgradient ),
  TEAMD( "Yellow", R.color.teamD_BG, R.color.teamD_CardText, 
         R.color.teamD_secondary, R.color.teamD_text, R.drawable.bg_yellowgradient );
  
  
  private final String name;
  private final int bg;
  private final int cardText;
  private final int secondary;
  private final int text;
  private final int gradient;


  private int score;
  
  private Team( String name, int bg, int cardText, int secondary, int text, int gradient)
  {
    this.name = name;
    this.bg = bg;
    this.cardText = cardText;
    this.secondary = secondary;
    this.text = text;
    this.gradient = gradient;
    this.setScore( 0 );
  }

  
  /**
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  
  /**
   * @return the bg
   */
  public int getBg()
  {
    return bg;
  }

  
  /**
   * @return the cardText
   */
  public int getCardText()
  {
    return cardText;
  }

  
  /**
   * @return the secondary
   */
  public int getSecondary()
  {
    return secondary;
  }

  
  /**
   * @return the text
   */
  public int getText()
  {
    return text;
  }
  
  /**
   * @return the gradient
   */
  public int getGradient()
  {
    return gradient;
  }


  public void setScore( int score )
  {
    this.score = score;
  }


  public int getScore()
  {
    return score;
  }
  
  class ScoreComparator implements Comparator<Team>
  {
    
    public int compare(Team team1, Team team2)
    {
 
        int s1 = team1.score;
        int s2 = team2.score;
       
        if(s1 > s2)
        {
          return 1;
        }
        else if(s1 < s2)
        {
          return -1;
        }
        else
        {
          return 0;
        }
    }
  }
  
}
