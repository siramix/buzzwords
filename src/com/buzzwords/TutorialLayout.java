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
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

/**
 * Custom view that pairs a mask with a chat bubble and handles showing, hiding,
 * and advancing tutorial text.
 * 
 * @author Siramix Labs
 * 
 */
public class TutorialLayout extends RelativeLayout {

  /*
   * Elements contained in this Layout
   */
  private MaskView mMask;
  private ChatBubbleLayout mChat;

  private View mCurrentTarget;
  private String mCurrentText;
  private int mCurrentChatLocation;

  private FrancoisOneTextView mTapText;

  public final static int BOTTOM = 0;
  public final static int CENTER = 1;
  public final static int TOP = 2;

  private OnClickListener mClickListener;

  /**
   * @param context
   */
  public TutorialLayout(Context context) {
    super(context);
    init(context);
  }

  /**
   * @param context
   * @param attrs
   */
  public TutorialLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);

  }

  /**
   * @param context
   * @param attrs
   * @param defStyle
   */
  public TutorialLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  /**
   * Initialize all the elements when constructed.
   */
  private void init(Context context) {

    mMask = new MaskView(context);
    mChat = new ChatBubbleLayout(context);
    mTapText = new FrancoisOneTextView(context);
    // Assign IDs to our elements
    int id = 0;
    mMask.setId(++id);
    mChat.setId(++id);
    mTapText.setId(++id);

    // Initialize the mask
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    mMask.setLayoutParams(params);

    // Initialize the chat bubble
    RelativeLayout.LayoutParams chatParams = new RelativeLayout.LayoutParams(
        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    mChat.setPadding(0, 10, 0, 10);
    mChat.setLayoutParams(chatParams);

    mTapText.setText("* Tap *");
    RelativeLayout.LayoutParams tapParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    tapParams.addRule(RelativeLayout.ALIGN_BOTTOM, mChat.getId());
    tapParams.bottomMargin = 20;
    tapParams.leftMargin = 30;
    mTapText.setTextSize(22);
    mTapText.setLayoutParams(tapParams);
    mTapText.setVisibility(View.INVISIBLE);

    // Add the views to the layout
    this.addView(mMask);
    this.addView(mChat);
    this.addView(mTapText);

    // Initialize the view unclickable to allow click events underneath it
    this.setClickable(false);

    // Hide the view, to be shown programatically by Activities that use this
    // Layout
    this.setVisibility(View.GONE);
  }

  /**
   * Show the tutorial overlay
   */
  private void show() {

    // Set the view clickable. This blocks all clickable events on views the
    // layout covers
    this.setClickable(true);

    // Show the view so that it returns from GONE
    this.setVisibility(View.VISIBLE);

    this.setEnabled(false);

    mChat.setVisibility(View.INVISIBLE);
    mMask.setAnimation(fadeInMaskAnimation());
  }

  /**
   * Set the content of the tutorial, highlighting the specified view and
   * displaying the text as spoken by Buzz.
   * 
   * @param View
   *          target - The view to highlight using the MaskView
   * @param String
   *          text - The text for Buzz to speak
   * @param int chatLocation - the location for the chat bubble, as defined by
   *        TutorialLayout
   */
  public void setContent(View target, String text, int chatLocation) {
    // Setup the data fields
    mCurrentTarget = target;
    mCurrentText = text;
    mCurrentChatLocation = chatLocation;

    // Put the chat box in the desired location
    RelativeLayout.LayoutParams chatParams = (RelativeLayout.LayoutParams) mChat
        .getLayoutParams();
    int rule = RelativeLayout.ALIGN_PARENT_BOTTOM;
    switch (mCurrentChatLocation) {
    case BOTTOM:
      rule = RelativeLayout.ALIGN_PARENT_BOTTOM;
      break;
    case CENTER:
      rule = RelativeLayout.CENTER_IN_PARENT;
      break;
    case TOP:
      rule = RelativeLayout.ALIGN_PARENT_TOP;
      break;
    }
    chatParams.addRule(rule);

    // Show the view if it's hidden, or else just refresh the views to represent
    // the data
    if (this.getVisibility() == View.GONE) {
      show();
    } else {
      refreshViews();
    }
  }

  /**
   * Set the content of the tutorial, highlighting the specified view and
   * displaying the text as spoken by Buzz.
   * 
   * @param text
   * @param chatLocation
   */
  public void setContent(String text, int chatLocation) {
    setContent(null, text, chatLocation);
  }

  /**
   * Hides the tutorial page and all its children via animation
   */
  public void hide() {
    // Have Buzz stop speaking
    mChat.hideChat();
    // Slide out Buzz
    mChat.setAnimation(slideOutChatAnimation());
    // Fade out the mask
    mMask.setAnimation(fadeOutMaskAnimation());
    // Fade the tap text out
    mTapText.setAnimation(fadeOutTapAnimation());

    // Allow them to click views under the tutorial while fading
    this.setClickable(false);
  }

  /**
   * Sets a listener to get callbacks when the tutorial is clicked.
   */
  public void setListener(OnClickListener listener) {
    mClickListener = listener;
    this.setOnClickListener(mClickListener);
  }

  /**
   * Refresh the content in the views represented by the TutorialLayout. This
   * causes the chat bubble to pop up if it adds text, and sets the target area.
   */
  private void refreshViews() {
    mChat.setText(mCurrentText);
    mMask.setTarget(mCurrentTarget);

    // Show *Tap* text over some time.
    mTapText.setAnimation(fadeInTapAnimation());
  }

  /*
   * Returns an animation to play when shown
   */
  private AlphaAnimation fadeInMaskAnimation() {
    AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
    fadeIn.setDuration(1000);
    fadeIn.setFillAfter(true);
    AnimationListener listener = new AnimationListener() {
      public void onAnimationEnd(Animation animation) {
        mChat.setAnimation(slideInChatAnimation());
        mChat.setVisibility(View.VISIBLE);
      }

      public void onAnimationRepeat(Animation animation) {
      }

      public void onAnimationStart(Animation animation) {
      }
    };
    fadeIn.setAnimationListener(listener);
    return fadeIn;
  }

  /*
   * Returns the animation that fades out the Mask
   */
  private AlphaAnimation fadeOutMaskAnimation() {
    AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
    fadeOut.setDuration(1000);
    fadeOut.setFillAfter(true);
    AnimationListener listener = new AnimationListener() {
      public void onAnimationEnd(Animation animation) {
        // Hide the whole layout after fading
        TutorialLayout.this.setVisibility(View.GONE);
      }

      public void onAnimationRepeat(Animation animation) {
      }

      public void onAnimationStart(Animation animation) {
      }
    };
    fadeOut.setAnimationListener(listener);
    return fadeOut;
  }

  /*
   * Returns the animation that fades in the Tap Text, enabling click events
   */
  private AlphaAnimation fadeInTapAnimation() {
    AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
    fadeIn.setStartOffset(800);
    fadeIn.setDuration(200);
    fadeIn.setFillAfter(true);
    AnimationListener listener = new AnimationListener() {
      public void onAnimationEnd(Animation animation) {
        // Tap text is faded in, allow clicks
        TutorialLayout.this.setEnabled(true);
      }

      public void onAnimationRepeat(Animation animation) {
      }

      public void onAnimationStart(Animation animation) {
        // While fading in the Tap text, don't let them click the view
        TutorialLayout.this.setEnabled(false);
      }
    };
    fadeIn.setAnimationListener(listener);
    return fadeIn;
  }

  /*
   * Returns the animation that fades out the Tap text
   */
  private AlphaAnimation fadeOutTapAnimation() {
    AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
    fadeOut.setDuration(100);
    fadeOut.setFillAfter(true);
    return fadeOut;
  }

  /*
   * Returns the slide in animation for the chat element
   */
  private TranslateAnimation slideInChatAnimation() {
    TranslateAnimation slideIn = new TranslateAnimation(0, 0, 1200, 0);
    slideIn.setFillBefore(true);
    slideIn.setDuration(600);
    slideIn.setInterpolator(new OvershootInterpolator(0.6f));
    AnimationListener listener = new AnimationListener() {
      public void onAnimationEnd(Animation animation) {
        // Refresh the content after character has slid in
        refreshViews();
      }

      public void onAnimationRepeat(Animation animation) {
      }

      public void onAnimationStart(Animation animation) {
      }
    };
    slideIn.setAnimationListener(listener);
    return slideIn;
  }

  /*
   * Returns the slide out animation for the chat element
   */
  private TranslateAnimation slideOutChatAnimation() {
    TranslateAnimation slideOut = new TranslateAnimation(0, 0, 0, 1200);
    slideOut.setFillAfter(true);
    // This offset is a bit hacky. It's waiting for the chat to hide.
    slideOut.setStartOffset(300);
    slideOut.setDuration(600);
    return slideOut;
  }

}
