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
 * Custom view that pairs a mask with a chat bubble and handles showing,
 * hiding, and advancing tutorial text.
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

  public final static int BOTTOM = 0;
  public final static int CENTER = 1;
  public final static int TOP = 2;
  
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

    // Initialize the mask
    mMask = new MaskView(context);
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
      LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    mMask.setLayoutParams(params);
  
    // Initialize the chat bubble
    mChat = new ChatBubbleLayout(context);
    RelativeLayout.LayoutParams chatParams = new RelativeLayout.LayoutParams(
        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    mChat.setPadding(0, 10, 0, 10);
    mChat.setLayoutParams(chatParams);
  
    // Assign IDs to our elements
    int id = 0;
    mMask.setId(++id);
    mChat.setId(++id);

    // Add the views to the layout
    this.addView(mMask);
    this.addView(mChat);

    // Hide the view, to be shown programatically by Activities that use this Layout
    this.setVisibility(View.INVISIBLE);
  }
  
  /**
   * Show the tutorial overlay, highlighting the specified view and displaying the text as spoken
   * by Buzz.
   * @param View target - The view to highlight using the MaskView
   * @param String text - The text for Buzz to speak
   * @param int chatLocation - the location for the chat bubble, as defined by TutorialLayout
   */
  public void show(View target, String text, int chatLocation) {
    
    this.setVisibility(View.VISIBLE);
    
    mCurrentTarget = target;
    mCurrentText = text;
    mCurrentChatLocation = chatLocation;

    mMask.setAnimation(fadeInAnimation());
    
    mChat.setVisibility(View.INVISIBLE);
    
    //mChat.setText(mCurrentText);
    
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
  }
  
  /*
   * Returns an animation to play when shown
   */
  private AlphaAnimation fadeInAnimation()
  {
    AlphaAnimation fadeIn = new AlphaAnimation(0,1);
    fadeIn.setDuration(1000);
    fadeIn.setFillAfter(true);
    AnimationListener listener = new AnimationListener() 
    {
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
   * Returns the slide in animation for the chat element
   */
  private TranslateAnimation slideInChatAnimation()
  {
    TranslateAnimation slideIn = new TranslateAnimation(0, 0, 1200, 0);
    slideIn.setFillBefore(true);
    slideIn.setDuration(600);
    slideIn.setInterpolator(new OvershootInterpolator(0.6f));
    AnimationListener listener = new AnimationListener() 
    {
      public void onAnimationEnd(Animation animation) {
        mChat.setText(mCurrentText);
        mMask.setTarget(mCurrentTarget);
      }

      public void onAnimationRepeat(Animation animation) {
      }

      public void onAnimationStart(Animation animation) {
      }
    };
    slideIn.setAnimationListener(listener);
    return slideIn;
  }

}
