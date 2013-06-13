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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Custom view that is used to display text to the players, as spoken by
 * Buzzword's mascot, Buzz.
 * 
 * @author Siramix Labs
 * 
 */
public class ChatBubbleLayout extends RelativeLayout {

  /*
   * Elements contained in this Layout
   */
  private FrancoisOneTextView mTextView;
  private ImageView mChatArrow;
  private ImageView mCharacter;
  private RelativeLayout mBorder;

  private String mText;

  /**
   * @param context
   */
  public ChatBubbleLayout(Context context) {
    super(context);
    init(context);
  }

  /**
   * @param context
   * @param attrs
   */
  public ChatBubbleLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);

  }

  /**
   * @param context
   * @param attrs
   * @param defStyle
   */
  public ChatBubbleLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  /**
   * Initialize all the elements when constructed, including custom attributes.
   */
  private void init(Context context, AttributeSet attrs) {
    init(context);

    TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
        R.styleable.ChatBubbleLayout, 0, 0);

    try {
      mText = a.getString(R.styleable.ChatBubbleLayout_text);
    } finally {
      a.recycle();
    }
  }

  /**
   * Initialize all the elements when constructed.
   */
  private void init(Context context) {
    int id = 0;
    mText = "";
    
    mTextView = new FrancoisOneTextView(context);
    mTextView.setBackgroundColor(getResources().getColor(R.color.white));
    mTextView.setTextSize(22);
    mTextView.setTextColor(getResources().getColor(R.color.black));
    mTextView.setId(++id);
    
    mChatArrow = new ImageView(context);
    mChatArrow.setImageDrawable(getResources().getDrawable(
        R.drawable.tutorial_chat_arrow));
    mChatArrow.setId(++id);
    
    mCharacter = new ImageView(context);
    mCharacter.setImageDrawable(getResources().getDrawable(
        R.drawable.tutorial_buzz));
    mCharacter.setId(++id);
    
    mBorder = new RelativeLayout(context);
    mBorder.setBackgroundColor(getResources().getColor(R.color.black));
    mBorder.setId(++id);
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();


    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    mTextView.setText(mText);
    mTextView.setPadding(25, 10, 25, 10);
    mTextView.setLayoutParams(params);

    RelativeLayout.LayoutParams borderParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    borderParams.setMargins(20, 10, 20, 10);
    mBorder.setPadding(4, 4, 4, 4);
    mBorder.setLayoutParams(borderParams);
    mBorder.addView(mTextView);
    
    this.addView(mBorder);

    RelativeLayout.LayoutParams buzzParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    buzzParams.addRule(RelativeLayout.ALIGN_RIGHT, mBorder.getId());
    buzzParams.addRule(RelativeLayout.BELOW, mBorder.getId());
    buzzParams.topMargin = -30;
    mCharacter.setLayoutParams(buzzParams);
    this.addView(mCharacter);

    RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    arrowParams.addRule(RelativeLayout.ALIGN_TOP, mCharacter.getId());
    arrowParams.addRule(RelativeLayout.LEFT_OF, mCharacter.getId());
    arrowParams.topMargin = 16;
    mChatArrow.setLayoutParams(arrowParams);
    this.addView(mChatArrow);

  }

}
