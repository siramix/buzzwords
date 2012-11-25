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
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This handles reviewing a card to adjust its right-wrong-skip state. When
 * complete it sends its results back to TurnSummary by finishing.
 * 
 * @author Siramix Labs
 */
public class CardReviewActivity extends Activity {

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
  private AntonButton mNoScoreButton;

  /**
   * Holder for the index of the card in the TurnSummary caller of this activity
   */
  private int mCardIndex;

  /**
   * Integers for displaying buzzwords according to preferences
   */
  private int mNumBuzzwords;
  private final int MAX_BUZZWORDS = 5;
  /**
   * Listener for correct button
   */
  private final OnClickListener mCorrectClickListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "CorrectClickListener OnClick()");
      }
      v.setEnabled(false);
      setCardState(Card.RIGHT);
      SoundManager sm = SoundManager.getInstance(CardReviewActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.RIGHT);
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
      v.setEnabled(false);
      setCardState(Card.WRONG);
      SoundManager sm = SoundManager.getInstance(CardReviewActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.WRONG);
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
      v.setEnabled(false);
      setCardState(Card.SKIP);
      SoundManager sm = SoundManager.getInstance(CardReviewActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.SKIP);
      goBackToTurnSummary(Card.SKIP);
    }
  };

  /**
   * Listener for skip button
   */
  private final OnClickListener mNoScoreClickListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "NoScoreClickListener OnClick()");
      }
      v.setEnabled(false);
      setCardState(Card.NOTSET);
      SoundManager sm = SoundManager.getInstance(CardReviewActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.BACK);
      goBackToTurnSummary(Card.NOTSET);
    }
  };

  /**
   * Set the references to the elements from the layout file
   */
  private void setupViewReferences() {

    mTitleView = (TextView) this.findViewById(R.id.CardReview_CardTitle);

    int numToHide = MAX_BUZZWORDS- mNumBuzzwords;
    
    mBadWordViews = new TextView[5];
    mBadWordViews[0] = (TextView) this
        .findViewById(R.id.CardReview_Card_BadWord0);
    mBadWordViews[1] = (TextView) this
        .findViewById(R.id.CardReview_Card_BadWord1);
    mBadWordViews[2] = (TextView) this
        .findViewById(R.id.CardReview_Card_BadWord2);
    mBadWordViews[3] = (TextView) this
        .findViewById(R.id.CardReview_Card_BadWord3);
    mBadWordViews[4] = (TextView) this
        .findViewById(R.id.CardReview_Card_BadWord4);

    for(int i=1; i<=numToHide; ++i) {
      mBadWordViews[MAX_BUZZWORDS-i].setVisibility(View.GONE);
    }
    
    mCorrectButton = (ImageButton) this
        .findViewById(R.id.CardReview_ButtonCorrect);
    mWrongButton = (ImageButton) this.findViewById(R.id.CardReview_ButtonWrong);
    mSkipButton = (ImageButton) this.findViewById(R.id.CardReview_ButtonSkip);
    mNoScoreButton = (AntonButton) this.findViewById(R.id.CardReview_ButtonNoScore);
  }
  
  /**
   * Setup any visual properties of the UI
   */
  private void setupUIProperties() {
    // Set font on title
    Typeface font = Typeface.createFromAsset(getAssets(),
        "fonts/FrancoisOne.ttf");
    TextView text = (TextView) this.findViewById(R.id.CardReview_CardTitle);
    text.setTypeface(font);
    
    // Set font on badwords
    LinearLayout badWords = (LinearLayout) findViewById(R.id.CardReview_Card_BadWords); 
    for(int i = 0; i < badWords.getChildCount(); i++)
    {
      text = (TextView) badWords.getChildAt(i);
      text.setTypeface(font);
    }
    

    // Force the dialog to fill the screen
    LayoutParams params = getWindow().getAttributes();
    params.height = LayoutParams.FILL_PARENT;
    params.width = LayoutParams.FILL_PARENT;
    // Remove status bar because for some reason it comes back
    params.flags |= LayoutParams.FLAG_FULLSCREEN;
    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    
  }
  
  /**
   * Setup the button OnClickListeners
   */
  private void setupListeners() {
    mCorrectButton.setOnClickListener(mCorrectClickListener);
    mWrongButton.setOnClickListener(mWrongClickListener);
    mSkipButton.setOnClickListener(mSkipClickListener);
    mNoScoreButton.setOnClickListener(mNoScoreClickListener);
  }

  /**
   * Reset the background selectors of the right-wrong-skip image buttons on the
   * card. Then, set the selector of the passed state.
   * 
   * @param state
   *          - indicates the requested "lit" button
   */
  private void setCardState(int state) {

    mCorrectButton
        .setBackgroundResource(R.drawable.button_review_right_selector);
    mWrongButton
        .setBackgroundResource(R.drawable.button_review_wrong_selector);
    mSkipButton.setBackgroundResource(R.drawable.button_review_skip_selector);

    switch (state) {
    case Card.RIGHT:
      mCorrectButton.setBackgroundResource(R.drawable.button_right_selector);
      break;
    case Card.WRONG:
      mWrongButton.setBackgroundResource(R.drawable.button_wrong_selector);
      break;
    case Card.SKIP:
      mSkipButton.setBackgroundResource(R.drawable.button_skip_selector);
      break;
    default:
      break;
    }
  }

  /**
   * Display the specified card in the dialog
   * 
   * @param card
   *          - the card to be displayed
   */
  private void displayCard(Card card) {
    mTitleView.setText(card.getTitle());
    for (int i = 0; i < card.getBadWords().size(); ++i) {
      mBadWordViews[i].setText(card.getBadWords().get(i));
    }
    this.setCardState(card.getRws());
    
    // When not set, hide "clear" button (status is already cleared)
    if (card.getRws() == Card.NOTSET)
    {
      mNoScoreButton.setVisibility(View.GONE);
    }
  }

  /**
   * Finish the current activity and return the new card state and index to the
   * calling activity.
   * 
   * @param state
   *          - the right-wrong-skip state of the card
   */
  private void goBackToTurnSummary(int state) {
    Intent curIntent = new Intent();
    curIntent.putExtra(getString(R.string.cardIndexBundleKey), mCardIndex);
    curIntent.putExtra(getString(R.string.cardStateBundleKey), state);
    this.setResult(Activity.RESULT_OK, curIntent);
    this.finish();
  }

  /**
   * Create the activity and display the card bundled in the intent.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(getBaseContext());
    
    mNumBuzzwords = Integer.valueOf(sp.getString(Consts.PREFKEY_NUM_BUZZWORDS, "5"));

    this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

    this.setContentView(R.layout.cardreview);

    this.setupViewReferences();
    
    this.setupUIProperties();
    
    this.setupListeners();

    Intent curIntent = this.getIntent();
    Bundle cardBundle = curIntent.getExtras();
    mCardIndex = cardBundle.getInt(getString(R.string.cardIndexBundleKey));
    Card curCard = (Card) cardBundle
        .getSerializable(getString(R.string.cardBundleKey));

    this.displayCard(curCard);
  }

}
