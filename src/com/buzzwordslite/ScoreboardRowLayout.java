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
package com.buzzwordslite;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;

/**
 * Custom view that represents a team and its score.
 * 
 * @author The Buzzwords Team
 * 
 */
public class ScoreboardRowLayout extends RelativeLayout {

  private Context mContext;

  private FrameLayout mFrame;
  private View mBackground;

  private LinearLayout mContents;
  private TextView mStanding;
  private TextView mTeamText;

  private TextView mScore;

  // Track if this team select is active or inactive
  private boolean mIsTeamActive;

  private Team mTeam;

  /**
   * @param context
   */
  public ScoreboardRowLayout(Context context) {
    super(context);
    mContext = context;
  }

  /**
   * @param context
   * @param attrs
   */
  public ScoreboardRowLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  /**
   * @param context
   * @param attrs
   * @param defStyle
   */
  public ScoreboardRowLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();

    this.setFocusable(true);

    // Store off density in order to convert to pixels
    final float DENSITY = this.getResources().getDisplayMetrics().density;

    // Create the views

    // Initialize the group for the frame
    mFrame = new FrameLayout(mContext);
    mFrame.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.WRAP_CONTENT));
    int padding = (int) (DENSITY * 1 + 0.5f);
    mFrame.setPadding(0, padding, 0, padding);
    mFrame.setBackgroundColor(R.color.black);

    // Background for the layout
    mBackground = new View(mContext);
    mBackground.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT));
    mBackground.setBackgroundColor(this.getResources().getColor(
        R.color.gameend_blankrow));

    // Initialize Layout that stores the contents (standing, text, score)
    mContents = new LinearLayout(mContext);
    mContents.setOrientation(LinearLayout.HORIZONTAL);

    // Initialize team standing (1st, 2nd, 3rd, etc.) -- initially gone
    mStanding = new TextView(mContext);
    mStanding.setIncludeFontPadding(false);
    mStanding.setLayoutParams(new LayoutParams((int) (DENSITY * 50 + 0.5f),
        LayoutParams.FILL_PARENT));
    mStanding.setText("0th");
    mStanding.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    mStanding.setPadding((int) (DENSITY * 5 + 0.5f), 0, 0, 0);
    mStanding.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
    mStanding.setTextColor(this.getResources().getColor(R.color.white));
    mStanding.setVisibility(View.GONE);

    // Initialize TeamName
    mTeamText = new TextView(mContext);

    mTeamText.setText("Null Teambalamoramofamo");
    mTeamText.setIncludeFontPadding(false);
    LinearLayout.LayoutParams teamTextParams = new LinearLayout.LayoutParams(
        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    teamTextParams.weight = 1.0f;
    mTeamText.setLayoutParams(teamTextParams);
    mTeamText.setPadding((int) (DENSITY * 10 + 0.5f), 0, 0, 0);
    mTeamText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    mTeamText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
    mTeamText.setEllipsize(TruncateAt.END);
    mTeamText.setHorizontallyScrolling(true);

    // Initialize Score view
    mScore = new TextView(mContext);
    mScore.setBackgroundDrawable(getResources().getDrawable(
        R.drawable.gameend_row_end_blank));
    mScore.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.FILL_PARENT));
    mScore.setText("0");
    mScore.setIncludeFontPadding(false);
    mScore.setPadding(0, 0, (int) (DENSITY * 5 + 0.5f), 0);
    mScore.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
    mScore.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
    // Set font - Wrap in isInEditMode so as not to break previewer
    /*
     * This creates tons of padding / centering issues.... gotta figure this out
     * later if(!this.isInEditMode()) { Typeface antonFont =
     * Typeface.createFromAsset(mContext.getAssets(), "fonts/Anton.ttf");
     * mScore.setTypeface(antonFont); }
     */

    // Add views to the contents layout
    mContents.addView(mStanding);
    mContents.addView(mTeamText);
    mContents.addView(mScore);

    // Add the views to frame
    mFrame.addView(mBackground);
    mFrame.addView(mContents);

    // Add groups to TeamSelectLayout
    this.addView(mFrame);
  }

  /**
   * Set the team this TeamSelectLayout is associated with
   * 
   * @param team
   *          The team this Layout represents
   */
  public void setTeam(Team team) {
    mTeam = team;
    mTeamText.setText(team.getName());
    mTeamText.setTextColor(this.getResources().getColor(
        mTeam.getSecondaryColor()));
    mScore.setText(Integer.toString(mTeam.getScore()));
    mScore.setTextColor(this.getResources().getColor(mTeam.getPrimaryColor()));
  }

  /**
   * Set the team this TeamSelectLayout is associated with
   * 
   * @param standing
   *          String to display as this team's standing (1st, 2nd, etc)
   */
  public void setStanding(String standing) {
    mStanding.setText(standing);
    mStanding.setVisibility(View.VISIBLE);
  }

  /*
   * Get the team assigned to this TeamSelectLayout
   */
  public Team getTeam() {
    return mTeam;
  }

  /*
   * Set the view to display as active or inactive (bright or dim, for example)
   */
  public void setActiveness(boolean active) {
    mIsTeamActive = active;
    if (mIsTeamActive && mTeam != null) {
      this.setVisibility(View.VISIBLE);

      mBackground.setBackgroundResource(mTeam.getPrimaryColor());
      mScore.setBackgroundDrawable(getResources().getDrawable(
          mTeam.getGameEndPiece()));
      // Show scores and team names
      mTeamText.setVisibility(View.VISIBLE);
      mScore.setVisibility(View.VISIBLE);
    } else {
      this.setVisibility(View.GONE);

      mBackground.setBackgroundColor(getResources().getColor(
          R.color.gameend_blankrow));
      mScore.setBackgroundDrawable(getResources().getDrawable(
          R.drawable.gameend_row_end_blank));
      mTeamText.setVisibility(View.INVISIBLE);
      mScore.setVisibility(View.INVISIBLE);
    }
  }
}
