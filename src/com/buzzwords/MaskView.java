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
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view that represents a team and its score.
 * 
 * @author The Buzzwords Team
 * 
 */
public class MaskView extends View {

	private Paint mOverlayPaint;

	private int mTargetHeight;
	private int mTargetWidth;
	private View mTarget;
	private int[] mTargetCoords;
	
	private static final int X = 0;
	private static final int Y = 1;
	
	private int mTargetPadding;
	private final int mDefaultPadding = 10;

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
		
		mTargetPadding = 0;
	}
  
  public void setTarget(View target)
  {
    mTarget = target;
    mTargetWidth = mTarget.getWidth();
    mTargetHeight = mTarget.getHeight();
    mTarget.getLocationInWindow(mTargetCoords);
    
    if (mTargetWidth + mTargetHeight == 0)
      mTargetPadding = 0;
    else
      mTargetPadding = mDefaultPadding;
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

	}

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    setMeasuredDimension(width, height);
  }

}
