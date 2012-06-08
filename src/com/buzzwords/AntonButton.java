package com.buzzwords;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class AntonButton extends Button {

  public AntonButton(Context context) {
    super(context);
    setCustomFont();
  }

  public AntonButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    setCustomFont();
  }

  public AntonButton(Context context, AttributeSet attrs, int defStyle) {
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
          .getAssets(), "fonts/Anton.ttf");
      setTypeface(typeface);
    }
    
    // Set buttons to always use upper case
    this.setText(this.getText().toString().toUpperCase());
  }
}
