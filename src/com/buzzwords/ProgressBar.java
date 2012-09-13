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
public class ProgressBar extends LinearLayout {

  private Context mContext;

  /*
   * Elements contained in this Layout
   */
  private LinearLayout mBarLayout;
  private FrameLayout mProgressFill;
  private FrameLayout mRemainingFill;
  private TextView mTitle;
  
  private float mProgress;
  private int mTotal;

  /**
   * @param context
   */
  public ProgressBar(Context context) {
    super(context);
    initializeMembers(context);
  }

  /**
   * @param context
   * @param attrs
   */
  public ProgressBar(Context context, AttributeSet attrs) {
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
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();

    // Setup initial parameters of the main layout
    this.setOrientation(LinearLayout.VERTICAL);
    this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT));

    // Setup the Title
    mTitle.setGravity(Gravity.CENTER);
    mTitle.setText("Cards Played");
    mTitle.setTextSize(20);
    mTitle.setTextColor(this.getResources().getColor(R.color.white));
    if (!this.isInEditMode()) {
      Typeface francoisFont = Typeface.createFromAsset(mContext.getAssets(),
          "fonts/FrancoisOne.ttf");
      mTitle.setTypeface(francoisFont);
    }
    
    // Setup the Bar layout. This is for the group of bar pieces and labels
    mBarLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT));
    mBarLayout.setOrientation(LinearLayout.HORIZONTAL);

    // Setup each segment in the bar layout
    setupSegment(mProgressFill, this.getResources().getColor(R.color.teamA_primary));
    setupSegment(mRemainingFill, this.getResources().getColor(R.color.teamD_primary));
    
    float DEFAULT_PROGRESS = 0.75f;
    int DEFAULT_TOTAL = 1000;
    setTotal(DEFAULT_TOTAL);
    setProgress(DEFAULT_PROGRESS);
    
    mBarLayout.addView(mProgressFill);
    mBarLayout.addView(mRemainingFill);
    
    // Construct the entire element
    this.addView(mTitle);
    this.addView(mBarLayout);
  }
 
  private void setupSegment(FrameLayout segment, int color) {

      final float DENSITY = this.getResources().getDisplayMetrics().density;

      int defaultPadding = (int) (DENSITY * 2 + 0.5f);
    
      // Setup each segment in the bar layout
      segment.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      segment.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
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
    updateSegmentWeights();
  }

  public void setTotal(int total)
  {
    mTotal = total;
    updateSegmentWeights();
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