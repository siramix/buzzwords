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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Custom view that represents a team and its score.
 * 
 * @author The Buzzwords Team
 * 
 */
public class TutorialOverlayLayout extends RelativeLayout {

	private LinearLayout mOverlay;
	private LinearLayout mOverlayMidGroup;
	private View mOverlayBGTop;
	private View mOverlayBGBottom;
	private View mOverlayBGMidLeft;
	private View mOverlayBGCenter;
	private View mOverlayBGMidRight;

	private Paint mOverlayPaint;

	private int mHeight;
	private int mWidth;
	
	private int mTargetHeight;
	private int mTargetWidth;
	private int mTargetID;
	private int mTargetX;
	private int mTargetY;

	/**
	 * @param context
	 */
	public TutorialOverlayLayout(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public TutorialOverlayLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public TutorialOverlayLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Initialize all the elements when constructed.
	 */
	private void init() {
	  mTargetID = 0;

    mOverlay = new LinearLayout(getContext());
    mOverlayMidGroup = new LinearLayout(getContext());
    
    mOverlayBGTop = new View(getContext());
    mOverlayBGBottom = new View(getContext());
    mOverlayBGMidLeft = new View(getContext());
    mOverlayBGCenter = new View(getContext());
    mOverlayBGMidRight = new View(getContext());

		mOverlayPaint = new Paint();
		mOverlayPaint.setStyle(Paint.Style.FILL);
		mOverlayPaint.setColor(0xCC0000CC);
		mOverlayPaint.setColor(getResources().getColor(R.color.black));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mHeight = h;
		mWidth = w;
	}
	
	
  @Override
  public void onFinishInflate() {
    super.onFinishInflate();
    
    //mOverlay.setBackgroundColor(getResources().getColor(R.color.black));
    mOverlay.setOrientation(LinearLayout.VERTICAL);
    mOverlay.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT));
    
    // Setup top bar
    mOverlayBGTop.setBackgroundColor(getResources().getColor(R.color.teamB_primary));
    LinearLayout.LayoutParams topbarParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT);
    topbarParams.weight = 1.0f;
    mOverlayBGTop.setLayoutParams(topbarParams);
    
    // Setup center group
    mOverlayMidGroup.setOrientation(LinearLayout.HORIZONTAL);
    LinearLayout.LayoutParams midbarParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT);
    mOverlayMidGroup.setLayoutParams(midbarParams);
    // Setup mid left
    mOverlayBGMidLeft.setBackgroundColor(getResources().getColor(R.color.black));
    LinearLayout.LayoutParams midleftParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT);
    midleftParams.weight = 1.0f;
    mOverlayBGMidLeft.setLayoutParams(midleftParams);
    // Setup center
    LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT);
    centerParams.width = 200;
    centerParams.height = 200;
    mOverlayBGCenter.setLayoutParams(centerParams);
    // Setup mid right
    mOverlayBGMidRight.setBackgroundColor(getResources().getColor(R.color.white));
    LinearLayout.LayoutParams midrightParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT);
    midrightParams.weight = 1.0f;
    mOverlayBGMidRight.setLayoutParams(midrightParams);
    mOverlayMidGroup.addView(mOverlayBGMidLeft);
    mOverlayMidGroup.addView(mOverlayBGCenter);
    mOverlayMidGroup.addView(mOverlayBGMidRight);
    
    // Setup bottom bar
    mOverlayBGBottom.setBackgroundColor(getResources().getColor(R.color.genericBG_trim));
    LinearLayout.LayoutParams bottombarParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT);
    bottombarParams.weight = 1.0f;
    mOverlayBGBottom.setLayoutParams(bottombarParams);

    // Add all the views to the overlay
    mOverlay.addView(mOverlayBGTop);
    mOverlay.addView(mOverlayMidGroup);
    mOverlay.addView(mOverlayBGBottom);
    
    this.addView(mOverlay);
  }
  

/*
 * Get the view to re-render itself with new weighting for each segment based
 * on the previously supplied segment values.
 */
  /*
public void updateSegmentWeights() {

  float fillWeight;
  float remainderWeight;

  // Calculate weights for the two segments
  if (mTotal != 0) {
    mProgressFill.setVisibility(View.VISIBLE);
    float percent = (float)mProgress / mTotal;
    fillWeight = 1 - percent;
    remainderWeight = percent;
  } else {
    mProgressFill.setVisibility(View.GONE);
    fillWeight = 1;
    remainderWeight = 0;
  }

  // Assign the progress percentage as a weight
  LinearLayout.LayoutParams fillParams = (LinearLayout.LayoutParams) mProgressFill
      .getLayoutParams();
  fillParams.weight = fillWeight;
  mProgressFill.setLayoutParams(fillParams);

  LinearLayout.LayoutParams remainingParams = (LinearLayout.LayoutParams) mRemainingFill
      .getLayoutParams();
  remainingParams.weight = remainderWeight;
  mRemainingFill.setLayoutParams(remainingParams);

  // Force the view to redraw itself
  this.invalidate();
}*/

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		//canvas.drawRect(0,0,mWidth,mHeight, mOverlayPaint);
		canvas.drawRect(0,0,100,100, mOverlayPaint);
	}

}
