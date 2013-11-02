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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
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
  private RelativeLayout mChatBorder;
  private RelativeLayout mChatBox;

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
    final int chatBoxBorderWidth = 4;
    Drawable characterDrawable = getResources().getDrawable(
        R.drawable.tutorial_buzz);
    Drawable chatArrowDrawable = getResources().getDrawable(
        R.drawable.tutorial_chat_arrow);

    mText = "This is just some test text to seee what the view will actually do.";

    // Initialize the chat text
    mTextView = new FrancoisOneTextView(context);
    mTextView.setBackgroundColor(getResources().getColor(R.color.white));
    mTextView.setTextSize(22);
    mTextView.setTextColor(getResources().getColor(R.color.black));
    mTextView.setId(++id);
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    mTextView.setText(mText);
    mTextView.setPadding(25, 10, 25, 10);
    mTextView.setLayoutParams(params);

    // Initialize the border for the chat text
    mChatBorder = new RelativeLayout(context);
    mChatBorder.setBackgroundColor(getResources().getColor(R.color.black));
    RelativeLayout.LayoutParams borderParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    mChatBorder.setPadding(chatBoxBorderWidth, chatBoxBorderWidth,
        chatBoxBorderWidth, chatBoxBorderWidth);
    mChatBorder.setLayoutParams(borderParams);
    mChatBorder.setId(++id);
    mChatBorder.addView(mTextView);

    // Initialize the chat arrow
    mChatArrow = new ImageView(context);
    mChatArrow.setImageDrawable(chatArrowDrawable);
    RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    arrowParams.addRule(RelativeLayout.BELOW, mChatBorder.getId());
    arrowParams.addRule(RelativeLayout.ALIGN_RIGHT, mChatBorder.getId());
    arrowParams.topMargin = -chatBoxBorderWidth;
    arrowParams.rightMargin = characterDrawable.getMinimumWidth();
    mChatArrow.setLayoutParams(arrowParams);
    mChatArrow.setId(++id);

    // Initialize the chat box layout, which stores the arrow and box
    mChatBox = new RelativeLayout(context);
    RelativeLayout.LayoutParams chatBoxParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    chatBoxParams.setMargins(10, 10, 10, 10);
    mChatBox.setLayoutParams(chatBoxParams);
    mChatBox.addView(mChatBorder);
    mChatBox.addView(mChatArrow);
    mChatBox.setId(++id);

    // Initialize "Buzz", our mascot
    mCharacter = new ImageView(context);
    mCharacter.setImageDrawable(characterDrawable);
    RelativeLayout.LayoutParams buzzParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    buzzParams.addRule(RelativeLayout.ALIGN_RIGHT, mChatBox.getId());
    buzzParams.addRule(RelativeLayout.ALIGN_BOTTOM, mChatBox.getId());
    buzzParams.bottomMargin = -15;
    buzzParams.rightMargin = 10;
    mCharacter.setLayoutParams(buzzParams);
    mCharacter.setId(++id);

    // Add the views to the layout
    this.addView(mChatBox);
    this.addView(mCharacter);

    mChatBox.setVisibility(View.INVISIBLE);
  }

  /**
   * Set the text for the chat bubble and shows it if necessary.
   *
   * @param text
   */
  public void setText(String text) {
    mText = text;
    mTextView.setText(mText);

    invalidate();

    // Always animate up chat bubble every time text changes
    showChatBubble();
  }

  /**
   * Hide the chat box, but not Buzz himself
   */
  public void hideChat() {
    mChatBox.startAnimation(scaleDownChatAnimation());

    // play chat disappear sound
    SoundManager sm = SoundManager.getInstance(this.getContext());
    sm.playSound(SoundManager.Sound.CHAT_OUT);
  }

  /**
   * Handles playing animation and setting visibility when the chat bubble
   * should be shown.
   */
  private void showChatBubble() {
    mChatBox.startAnimation(scaleUpChatAnimation());

    // play chat pop in sound
    SoundManager sm = SoundManager.getInstance(this.getContext());
    sm.playSound(SoundManager.Sound.CHAT_IN);

  }

  /**
   * Returns the animation that scales in the chat box
   */
  private ScaleAnimation scaleUpChatAnimation() {
    final int SCALE_DURATION = 400;
    final float SCALE_START = 0.0f;
    final float SCALE_END = 1.0f;
    final int X = 0;
    final int Y = 1;
    final float[] PIVOTS = { 0.8f, 1.0f };

    ScaleAnimation scaleUp = new ScaleAnimation(SCALE_START, SCALE_END,
        SCALE_START, SCALE_END, Animation.RELATIVE_TO_SELF, PIVOTS[X],
        Animation.RELATIVE_TO_SELF, PIVOTS[Y]);
    scaleUp.setDuration(SCALE_DURATION);
    scaleUp.setInterpolator(new OvershootInterpolator(1.0f));
    scaleUp.setFillAfter(true);
    return scaleUp;
  }

  /**
   * Returns the animation that scales down the chat box
   */
  private ScaleAnimation scaleDownChatAnimation() {
    final int SCALE_DURATION = 200;
    final float SCALE_START = 1.0f;
    final float SCALE_END = 0.0f;
    final int X = 0;
    final int Y = 1;
    final float[] PIVOTS = { 0.8f, 0.9f };

    ScaleAnimation scaleDown = new ScaleAnimation(SCALE_START, SCALE_END,
        SCALE_START, SCALE_END, Animation.RELATIVE_TO_SELF, PIVOTS[X],
        Animation.RELATIVE_TO_SELF, PIVOTS[Y]);

    scaleDown.setDuration(SCALE_DURATION);
    scaleDown.setFillAfter(true);
    return scaleDown;
  }

}
