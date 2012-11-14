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

import java.io.Serializable;
import java.util.Comparator;

import android.util.Log;

import com.buzzwords.R;

/**
 * Team is a data representation of a Team in buzzwords. It mainly consists of
 * getters and setters, and contains the ids of various team specific attributes
 * like colors.
 * 
 * @author BuzzWords team
 * 
 */
public enum Team implements Serializable {
  TEAMA(R.string.teamnameA, R.color.teamA_primary,
      R.color.teamA_secondary, R.color.teamA_complement, R.drawable.bg_bluegradient, 
      "teamA_enabled"), TEAMB(R.string.teamnameB,
      R.color.teamB_primary, R.color.teamB_secondary,  R.color.teamB_complement,
      R.drawable.bg_greengradient, "teamB_enabled"), TEAMC(
      R.string.teamnameC, R.color.teamC_primary,  R.color.teamC_secondary,
      R.color.teamC_complement, R.drawable.bg_redgradient, "teamC_enabled"), TEAMD(
      R.string.teamnameD, R.color.teamD_primary, R.color.teamD_secondary,
      R.color.teamD_complement, R.drawable.bg_yellowgradient, "teamD_enabled");

  // Team name
  private String mName;
  // Team default name as resource ID
  private final int mDefaultName;
  
  // Team colors
  private final int mPrimary;
  private final int mSecondary;
  private final int mComplement;
  private final int mGradient;
  // The team's running score
  private int mScore;
  // The team's preference key
  private final String mPrefKey;

  /*
   * Store a version ID for this serialized class
   */
  static final long serialVersionUID = -5094548104192852942L;
  
  
  /*
   * Construct a Team
   */
  private Team(int name, int primary, int secondary, int complement, int gradient, String key) {
    mDefaultName = name;
    mComplement = complement;
    mSecondary = secondary;
    mPrimary = primary;
    mGradient = gradient;
    mPrefKey = key;
    this.setScore(0);
  }

  /**
   * Returns the Team's name as a String
   * 
   * @return the name
   */
  public String getName() {
	if (BuzzWordsApplication.DEBUG) {
	   Log.d("Team:", "getName(): " + mName);
	}
    return mName;
  }
  
  /**
   * Returns the Team's original name as a String Resource ID
   * 
   * @return the default name
   */
  public int getDefaultName() {
    return mDefaultName;
  }

  /**
   * Returns the id of the Team's secondary color
   * 
   * @return the secondary color
   */
  public int getSecondaryColor() {
    return mSecondary;
  }

  /**
   * Returns the id of the Team's primary color
   * 
   * @return the primary color
   */
  public int getPrimaryColor() {
    return mPrimary;
  }
  
  /**
   * Returns the id of the Team's complement color
   * 
   * @return the background color
   */
  public int getComplementaryColor() {
    return mComplement;
  }


  /**
   * Returns the id of the Team's gradient resource
   * 
   * @return the gradient
   */
  public int getGradient() {
    return mGradient;
  }
  
  /**
   * Returns the Team's preference key
   * 
   * @return the preference key
   */
  public String getPreferenceKey() {
    return mPrefKey;
  }


  /**
   * Set the Team's running score total
   * 
   * @param score
   */
  public void setScore(int score) {
    this.mScore = score;
  }
  

  /**
   * Set the Team's new name.  Original name can still be accessed with
   * getDefaultName()
   */
  public void setName(String name) {
    mName = name;
  }


  /**
   * Returns the Team's running score
   * 
   * @return score
   */
  public int getScore() {
    return mScore;
  }

  /**
   * Basic Comparator for the Team enum. Handles comparison between Team objects
   * 
   */
  class ScoreComparator implements Comparator<Team> {

    /**
     * Compare two teams and return which is greater Return 1 if team1 is
     * greater, -1 if team2 is greater, and 0 if even
     */
    public int compare(Team team1, Team team2) {

      int s1 = team1.mScore;
      int s2 = team2.mScore;

      if (s1 > s2) {
        return 1;
      } else if (s1 < s2) {
        return -1;
      } else {
        return 0;
      }
    }
  }

}
