/**
 * 
 */
package com.wordfrenzy;

import java.util.Comparator;

import com.wordfrenzy.R;


/**
 * @author cpatrick
 *
 */
public enum Team
{
  TEAMA( "Blue", R.color.teamA_BG, R.color.teamA_CardText, 
         R.color.teamA_secondary, R.color.teamA_text, R.drawable.bg_bluegradient,
         R.drawable.gameend_row_end_blue),
  TEAMB( "Green", R.color.teamB_BG, R.color.teamB_CardText, 
         R.color.teamB_secondary, R.color.teamB_text, R.drawable.bg_greengradient,
         R.drawable.gameend_row_end_green),
  TEAMC( "Red", R.color.teamC_BG, R.color.teamC_CardText, 
         R.color.teamC_secondary, R.color.teamC_text, R.drawable.bg_redgradient,
         R.drawable.gameend_row_end_red),
  TEAMD( "Yellow", R.color.teamD_BG, R.color.teamD_CardText, 
         R.color.teamD_secondary, R.color.teamD_text, R.drawable.bg_yellowgradient,
         R.drawable.gameend_row_end_yellow);
  
  
  private final String name;
  private final int bg;
  private final int cardText;
  private final int secondary;
  private final int text;
  private final int gradient;
  private final int gameend_bg;


  private int score;
  
  private Team( String name, int bg, int cardText, int secondary, int text, int gradient, int gameend_bg)
  {
    this.name = name;
    this.bg = bg;
    this.cardText = cardText;
    this.secondary = secondary;
    this.text = text;
    this.gradient = gradient;
    this.gameend_bg = gameend_bg;
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
   * @return the background color
   */
  public int getBg()
  {
    return bg;
  }

  
  /**
   * @return the cardText color
   */
  public int getCardText()
  {
    return cardText;
  }

  
  /**
   * @return the secondary color
   */
  public int getSecondaryColor()
  {
    return secondary;
  }

  
  /**
   * @return the primary color
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

  /**
   * @return the end piece for a game end row
   */
  public int getGameEndPiece()
  {
    return gameend_bg;
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
