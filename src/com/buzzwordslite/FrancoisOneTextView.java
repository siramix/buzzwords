package com.buzzwordslite;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class FrancoisOneTextView extends TextView {

  public FrancoisOneTextView(Context context) {
    super(context);
    setCustomFont();
  }

  public FrancoisOneTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setCustomFont();
  }

  public FrancoisOneTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setCustomFont();
  }

  /*
   * Helper function to set the custom typeface of this view.
   */
  private void setCustomFont()
  {
    // Wrap in isEditMode so that xml previewer doesn't break.
    if (!this.isInEditMode()) {
      Typeface typeface = Typeface.createFromAsset(this.getContext()
          .getAssets(), "fonts/FrancoisOne.ttf");
      setTypeface(typeface);
    }
  }
}
