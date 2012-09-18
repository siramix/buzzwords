/*****************************************************************************
 *  PhraseCraze is a family friendly word game for mobile phones.
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

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.LinearLayout;

/**
 * Custom view that represents one value as a progress bar of a total.
 * 
 * @author The PhraseCraze Team
 * 
 */
public class ProgressBarView extends LinearLayout {

  private Context mContext;

  /*
   * Elements contained in this Layout
   */
  private LinearLayout mBarLayout;
  private FrameLayout mProgressFill;
  private FrameLayout mRemainingFill;
  private TextView mTitle;
  private TextView mFraction;
  
  private float mProgress;
  private int mTotal;

  /**
   * @param context
   */
  public ProgressBarView(Context context) {
    super(context);
    initializeMembers(context);
  }

  /**
   * @param context
   * @param attrs
   */
  public ProgressBarView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initializeMembers(context);
  }

  // Initialize the member variables
  private void initializeMembers(Context context) {
    mContext = context;
    mBarLayout = new LinearLayout(mContext);
    mProgressFill = new FrameLayout(mContext);
    mRemainingFill = new FrameLayout(mContext);
    mTitle = new TextView(mContext);
    mFraction = new TextView(mContext);
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();
    
    final float DENSITY = this.getResources().getDisplayMetrics().density;

    // Setup initial parameters of the main layout
    this.setOrientation(LinearLayout.VERTICAL);
    this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT));

    // Setup the Title
    mTitle.setGravity(Gravity.LEFT);
    mTitle.setText("Cards Played");
    mTitle.setTextSize(20);
    mTitle.setTextColor(this.getResources().getColor(R.color.white));
    mTitle.setPadding((int) (DENSITY * 5 + 0.5f), 0, 0, 0);
    if (!this.isInEditMode()) {
      Typeface antonFont = Typeface.createFromAsset(mContext.getAssets(),
          "fonts/Anton.ttf");
      mTitle.setTypeface(antonFont);
    }
    
    // Setup the Fraction
    mFraction.setGravity(Gravity.RIGHT);
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
    mFraction.setLayoutParams(params);
    mFraction.setTextSize(18);
    if (!this.isInEditMode()) {
      Typeface antonFont = Typeface.createFromAsset(mContext.getAssets(),
          "fonts/Anton.ttf");
      mFraction.setTypeface(antonFont);
    }
    
    
    // Setup the Bar layout. This is for the group of bar pieces and labels
    mBarLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT));
    mBarLayout.setOrientation(LinearLayout.HORIZONTAL);

    // Setup each segment in the bar layout
    int borderPadding = (int) (DENSITY * 2 + 0.5f);
    setupSegment(mProgressFill, this.getResources().getColor(R.color.teamB_primary), 0, borderPadding);
    setupSegment(mRemainingFill, this.getResources().getColor(R.color.genericBG_trim), borderPadding, borderPadding);
    
    // Set stub values
    float DEFAULT_PROGRESS = 0.75f;
    int DEFAULT_TOTAL = 1000;
    setTotal(DEFAULT_TOTAL);
    setProgress(DEFAULT_PROGRESS);
    
    // Add the fraction to the appropriate frame
    mFraction.setPadding(0, 0, (int) (DENSITY * 5 + 0.5f), 0);
    mRemainingFill.addView(mFraction);
    
    mBarLayout.addView(mProgressFill);
    mBarLayout.addView(mRemainingFill);
    
    // Construct the entire element
    this.addView(mTitle);
    this.addView(mBarLayout);
  }
 
  private void setupSegment(FrameLayout segment, int color, int innerPadding, int outerPadding) {

      int paddingLTRB[] = {outerPadding, outerPadding, innerPadding, outerPadding};
    
      // Setup each segment in the bar layout
      segment.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      segment.setPadding(paddingLTRB[0], paddingLTRB[1], paddingLTRB[2], paddingLTRB[3]);
      segment.setBackgroundColor(this.getResources().getColor(R.color.black));

      // Setup the colored section of the bar
      View foreground = new View(mContext);
      foreground.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      foreground.setBackgroundColor(color);
      
      segment.addView(foreground);
  }
  
  /*
   * Set the title of this percentage bar
   */
  public void setTitle(String title) {
    mTitle.setText(title);
  }

  /**
   * Set all the data that this segment of the ComboPercentageBar can represent.
   * 
   * @param percent
   *          the amount of progress to render as a percent of the total
   */
  public void setProgress(float percent) {
    mProgress = percent;
    updateFraction();
    updateSegmentWeights();
  }

  public void setTotal(int total)
  {
    mTotal = total;
    updateFraction();
    updateSegmentWeights();
  }
  /*
   * Updates the fraction that displays the progress numbers
   */
  private void updateFraction()
  {
    mFraction.setText((int)(mProgress*mTotal) + "/" + mTotal);
  }

  /*
   * Get the view to re-render itself with new weighting for each segment based
   * on the previously supplied segment values.
   */
  public void updateSegmentWeights() {
    
    // Assign the progress percentage as a weight
    LinearLayout.LayoutParams fillParams = (LinearLayout.LayoutParams) mProgressFill
        .getLayoutParams();
    fillParams.weight = (1 - mProgress);
    mProgressFill.setLayoutParams(fillParams); 
    
    LinearLayout.LayoutParams remainingParams = (LinearLayout.LayoutParams) mRemainingFill
        .getLayoutParams();
    remainingParams.weight = mProgress;
    mRemainingFill.setLayoutParams(remainingParams); 

    // Force the view to redraw itself
    this.invalidate();
  }
}