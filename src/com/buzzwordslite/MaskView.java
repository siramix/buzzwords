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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Mask view creates an overlay that fills the specified area, except for
 * a certain "Target" area. It's used to highlight spots on the screen
 * during the tutorial.
 *
 * @author Siramix Labs
 */
public class MaskView extends View {

	private Paint mOverlayPaint;
	private Paint mTargetStrokePaint;

	private int mTargetHeight;
	private int mTargetWidth;
	private View mTarget;
	private int[] mTargetCoords;

	private static final int X = 0;
	private static final int Y = 1;

	private int mTargetPadding;
	private final int mDefaultPadding = 5;
	private final int mDefaultTargetStroke = 3;

	/**
	 * @param context
	 */
	public MaskView(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public MaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public MaskView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Initialize all the elements when constructed.
	 */
	private void init() {
    mTargetCoords = new int[2];

		mOverlayPaint = new Paint();
		mOverlayPaint.setStyle(Paint.Style.FILL);
		mOverlayPaint.setColor(0xAA000000);

    mTargetStrokePaint = new Paint();
    mTargetStrokePaint.setStyle(Paint.Style.STROKE);
    mTargetStrokePaint.setStrokeWidth(mDefaultTargetStroke);
    mTargetStrokePaint.setColor(0xFFFFFFFF);

    mTargetPadding = 0;
	}

  /**
   * Sets a target View to be masked out by the overlay
   * @param target - the View to highlight
   */
  public void setTarget(View target)
 {
    if (target == null) {
      clearTarget();
    } else {
      mTarget = target;
      mTargetWidth = mTarget.getWidth();
      mTargetHeight = mTarget.getHeight();
      mTarget.getLocationInWindow(mTargetCoords);

      // If the target area is empty, don't pad it
      if (mTargetWidth + mTargetHeight == 0)
        mTargetPadding = 0;
      else
        mTargetPadding = mDefaultPadding;
    }
    invalidate();
  }

  /**
   * Helper function that clears the target from the mask and reinitializes the views.
   */
  private void clearTarget()
  {
    mTarget = null;
    mTargetWidth = 0;
    mTargetHeight = 0;
    mTargetCoords[X] = 0;
    mTargetCoords[Y] = 0;
    mTargetPadding = 0;

    invalidate();
  }

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Calculate all the corners of the four rectangles that outline the target
		float tl = 0;
		float tt = 0;
		float tr = canvas.getWidth();
		float tb = Math.max(mTargetCoords[Y] - mTargetPadding, 0);
		float lr = Math.max(mTargetCoords[X] - mTargetPadding, 0);
		float lb = Math.min(mTargetCoords[Y] + mTargetHeight + mTargetPadding, canvas.getHeight());
		float rl = Math.min(mTargetCoords[X] + mTargetWidth + mTargetPadding, canvas.getWidth());
		float bb = canvas.getHeight();

		// Draw top rectangle
		canvas.drawRect(tl,tt,tr,tb, mOverlayPaint);
		// Draw left rectangle : ll = tl, lt = tb
		canvas.drawRect(tl,tb,lr,lb, mOverlayPaint);
		// Draw right rectangle:  rt = tb, rr = tr, rb = lb
		canvas.drawRect(rl,tb,tr,lb, mOverlayPaint);
		// Draw bottom rectangle: bl = tl, bt = lb, br = tr
		canvas.drawRect(tl,lb,tr,bb, mOverlayPaint);

		canvas.drawRect(lr, tb,rl,lb, mTargetStrokePaint);

	}
}
