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

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * This handles reviewing a card to adjust its right-wrong-skip state.
 * 
 * @author Siramix Labs
 */
public class CardReview extends Activity {
  
  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "CardReview";
  
  /**
   * Text elements
   */
  private TextView mTitleView; 
  private TextView[] mBadWordViews;
  
  /**
   * Image buttons
   */
  private ImageButton mCorrectButton;
  private ImageButton mWrongButton;
  private ImageButton mSkipButton;
  
  /**
   * Holder for the index of the card in the TurnSummary caller of this activity
   */
  private int mCardIndex;
  
  /**
   * Sound manager reference for playing right-wrong-skip sounds
   */
  private SoundManager mSoundManager;

  /**
   * Listener for correct button
   */
  private final OnClickListener mCorrectClickListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "CorrectClickListener OnClick()");
      }
      setCardState(Card.RIGHT);
      mSoundManager.playSound(SoundManager.Sound.RIGHT);
      goBackToTurnSummary(Card.RIGHT);
    }
  };
  
  /**
   * Listener for wrong button
   */
  private final OnClickListener mWrongClickListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "WrongClickListener OnClick()");
      }
      setCardState(Card.WRONG);
      mSoundManager.playSound(SoundManager.Sound.WRONG);
      goBackToTurnSummary(Card.WRONG);
    }
  };
  
  /**
   * Listener for skip button
   */
  private final OnClickListener mSkipClickListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "SkipClickListener OnClick()");
      }
      setCardState(Card.SKIP);
      mSoundManager.playSound(SoundManager.Sound.SKIP);
      goBackToTurnSummary(Card.SKIP);
    }
  };
  
  /**
   * Set the references to the elements from the layout file
   */
  private void setupViewReferences() {
    
    mTitleView = (TextView) this.findViewById(R.id.CardReview_CardTitle);
    
    mBadWordViews = new TextView[5];
    mBadWordViews[0] = (TextView) this.findViewById(R.id.CardReview_Card_BadWord0);
    mBadWordViews[1] = (TextView) this.findViewById(R.id.CardReview_Card_BadWord1);
    mBadWordViews[2] = (TextView) this.findViewById(R.id.CardReview_Card_BadWord2);
    mBadWordViews[3] = (TextView) this.findViewById(R.id.CardReview_Card_BadWord3);
    mBadWordViews[4] = (TextView) this.findViewById(R.id.CardReview_Card_BadWord4);
    
    mCorrectButton = (ImageButton) this.findViewById(R.id.CardReview_ButtonCorrect);
    mWrongButton = (ImageButton) this.findViewById(R.id.CardReview_ButtonWrong);
    mSkipButton = (ImageButton) this.findViewById(R.id.CardReview_ButtonSkip);
    
  }
  
  private void setupListeners() {
    mCorrectButton.setOnClickListener(mCorrectClickListener);
    mWrongButton.setOnClickListener(mWrongClickListener);
    mSkipButton.setOnClickListener(mSkipClickListener);
  }
  
  private void setupSoundManager() {
    BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
    mSoundManager = application.getSoundManager();
  }
  
  private void setCardState(int state) {
    
    mCorrectButton.setBackgroundResource(R.drawable.controls_review_right_selector);
    mWrongButton.setBackgroundResource(R.drawable.controls_review_wrong_selector);
    mSkipButton.setBackgroundResource(R.drawable.controls_review_skip_selector);
    
    switch(state) {
    case Card.RIGHT:
      mCorrectButton.setBackgroundResource(R.drawable.controls_right_selector);
      break;
    case Card.WRONG:
      mWrongButton.setBackgroundResource(R.drawable.controls_wrong_selector);
      break;
    case Card.SKIP:
      mSkipButton.setBackgroundResource(R.drawable.controls_skip_selector);
      break;
    default:
      break;
    }
  }
  
  private void displayCard(Card card) {
    mTitleView.setText(card.getTitle());
    for(int i = 0; i < card.getBadWords().size(); ++i) {
      mBadWordViews[i].setText(card.getBadWords().get(i));
    }
    this.setCardState(card.getRws());
  }
  
  private void goBackToTurnSummary(int state)
  {
    Intent curIntent = new Intent();
    curIntent.putExtra(getString(R.string.cardIndexBundleKey), mCardIndex);
    curIntent.putExtra(getString(R.string.cardStateBundleKey), state );
    this.setResult(Activity.RESULT_OK, curIntent);
    this.finish();
  }
  
  /**
   * Initializes the activity to display the word you have to cause your team
   * mates to say with the words you cannot say below.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }
    
    this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    
    this.setContentView(R.layout.cardreview);
    
    this.setupViewReferences();
    
    this.setupSoundManager();
    
    this.setupListeners();
    
    Intent curIntent = this.getIntent();
    Bundle cardBundle = curIntent.getExtras();
    mCardIndex = cardBundle.getInt(getString(R.string.cardIndexBundleKey));
    Card curCard = (Card) cardBundle.getSerializable(getString(R.string.cardBundleKey));
    
    this.displayCard(curCard);
  }
  
}
