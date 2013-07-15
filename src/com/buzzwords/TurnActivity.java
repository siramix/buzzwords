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

import java.util.ArrayList;

import com.buzzwords.R;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * This handles a single turn consisting of cards presented to a player for a
 * limited amount of time.
 * 
 * @author Siramix Labs
 */
public class TurnActivity extends Activity {

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "Turn";

  static final int DIALOG_PAUSED_ID = 0;
  static final int DIALOG_GAMEOVER_ID = 1;
  static final int DIALOG_READY_ID = 2;
  static final int DIALOG_ENDTURN_ID = 3;

  static final int TIMERANIM_PAUSE_ID = 0;
  static final int TIMERANIM_RESUME_ID = 1;
  static final int TIMERANIM_START_ID = 2;

  // Gesture thresholds expressed in dp
  private static final int SWIPE_MIN_DISTANCE_DP = 80;
  private static final int SWIPE_THRESHOLD_VELOCITY_DP = 150;
  // Convert DP thresholds to pixels for this phone (add .5f to round to nearest
  // pixel)
  private int mGestureThreshold;
  private int mGestureVelocityThreshold;

  private View mPauseOverlay;
  private ImageButton mBuzzerButton;
  private ImageButton mNextButton;
  private ImageButton mSkipButton;
  private Button mMenuButton;
  private TextView mCountdownText;
  private TextView mTimesUpText;
  private LinearLayout mPauseTextLayout;
  private ViewFlipper mViewFlipper;

  private ImageView mTimerfill;

  private TextView mCardTitle;
  private LinearLayout mCardBadWords;
  private ImageView mCardStatus;

  private RelativeLayout mMenuBar;
  private RelativeLayout mTimerGroup;
  private RelativeLayout mButtonGroup;
  
  private TutorialLayout mTutorialLayout;

  
  public GameManager getGameManager() {
    if(mGameManager == null) {
      BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
      mGameManager = application.getGameManager();
    }
    return mGameManager;
  }

  /**
   * Tracks the current state of the Turn as a boolean. Set to true when time
   * has expired and activity is showing the user "Time's up!"
   */
  private boolean mTurnTimeIsUp = false;
  
  /**
   * Track whether or not the turn is still waiting on the Team Ready
   */
  private boolean mIsWaitingForTeamReady = false;

  /**
   * Track when the game has paused. This prevents code from executing
   * pointlessly if already paused.
   */
  private boolean mIsPaused = true;
  
  /**
   * Track when the game is in the tutorial state.
   */
  private boolean mIsInTutorial = false;
  
  /**
   * Track which part of the tutorial the user is in.
   */
  private TutorialPage mTutorialPage;

  /**
   * Enum gives a name to each tutorial page
   */
  private enum TutorialPage {TITLE, BADWORDS, RIGHT, WRONG, SKIP, TIME, END, NOADVANCE };
  
  /**
   * Flag tells onPause event when leaving to the next activity
   */
  private boolean mExitingToNextActivity = false;

  /**
   * This is a reference to the current game manager
   */
  private GameManager mGameManager;

  /**
   * Boolean to track which views are currently active
   */
  private boolean mAIsActive;

  /**
   * Boolean for representing whether we've gone back or not
   */
  private boolean mIsBack;

  /**
   * Boolean representing whether music is enabled or not. Reduces calls to
   * getprefs
   */
  private boolean mMusicEnabled;

  /**
   * Boolean representing whether gestures are enabled or not. Reduces calls to
   * getprefs
   */
  private boolean mGesturesEnabled;

  /**
   * Boolean representing whether skip is enabled or not. Reduces calls to
   * getprefs
   */
  private boolean mSkipEnabled;

  /**
   * Boolean representing whether the countdown ticking has already been
   * started.
   */
  private boolean mIsTicking = false;

  /**
   * Swipe left for skip, right for back, up for right, and down for wrong.
   */
  private SimpleOnGestureListener mSwipeListener = new SimpleOnGestureListener() {

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        float velocityY) {
      // Do not let them do swipes while paused or time's up!
      if (mIsPaused || mTurnTimeIsUp) {
        return false;
      }

      // Detect swipes in order of least to most harmful to a game -- ie if
      // a "correct" swipe is confused as a "skip" its not as bad as a skip
      // being
      // interpreted as a correct or wrong
      if (mGesturesEnabled) {
        if (mSkipEnabled && e1.getX() - e2.getX() > mGestureThreshold
            && Math.abs(velocityX) > mGestureVelocityThreshold) {
          TurnActivity.this.doSkip();
          return true;
        } else if (e2.getX() - e1.getX() > mGestureThreshold
            && Math.abs(velocityX) > mGestureVelocityThreshold) {
          TurnActivity.this.doBack();
          return true;
        } else if (e1.getY() - e2.getY() > mGestureThreshold
            && Math.abs(velocityY) > mGestureVelocityThreshold) {
          TurnActivity.this.doCorrect();
          return true;
        } else if (e2.getY() - e1.getY() > mGestureThreshold
            && Math.abs(velocityY) > mGestureVelocityThreshold) {
          TurnActivity.this.doWrong();
          return true;
        }
      }

      return false;
    }
  };

  private GestureDetector mSwipeDetector;

  private OnTouchListener mGestureListener;

  private PauseTimer mCounter;
  private PauseTimer mResultsDelay;

  /**
   * Setup the turn timer and return a reference to it.
   * Also set up the fill bar associated with the timer.
   * 
   * @return a reference to the turn timer
   */
  public PauseTimer createTurnTimer(long time) {
    
    mTimerfill.startAnimation(timerPauseAnimation(time));
    long shownTime = (long) Math.ceil((double) time / 1000);
    mCountdownText.setText(Long.toString(shownTime));
    
    // Initialize the turn timer
    return new PauseTimer(time) {

      @Override
      public void onFinish() {
        TurnActivity.this.onTimeExpired();
        mCountdownText.setText("0");
        mTurnTimeIsUp= true;
      }

      @Override
      public void onTick() {
        // Update our text each second
        long time = mCounter.getTimeRemaining();
        long shownTime = (long) Math.ceil((double) time / 1000);
        mCountdownText.setText(Long.toString(shownTime));

        // When music is not enabled, use the ticking sound
        if (!mMusicEnabled && !mIsTicking) {
          if (shownTime == 10) {
            SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

            // Only play ticking when sound effects are enabled
            if (sp.getBoolean(Consts.PREFKEY_SFX, true)) {
              mIsTicking = true;
              BuzzWordsApplication application = (BuzzWordsApplication) TurnActivity.this
                  .getApplication();
              MediaPlayer mp = application.getMusicPlayer(TurnActivity.this
                  .getBaseContext());
              mp.start();
            }
          }
        }
      }

    };

  }

  /**
   * Starts the turn timer and the animations that go along with that
   */
  private void startTurnTimer() {
    mCounter.start();
    mTimerfill.startAnimation(timerCountdownAnimation(mCounter.getTimeRemaining()));
  }

  /**
   * Stops the turn timer and the animations that go along with it
   */
  private void stopTurnTimer() {
    Log.d(TAG, "stopTimer()");
    Log.d(TAG, Long.toString(mCounter.getTimeRemaining()));

    if (!mTurnTimeIsUp && mCounter.isActive()) {
      mCounter.pause();
      mTimerfill
          .startAnimation(timerPauseAnimation(mCounter.getTimeRemaining()));
    }
  }

  /**
   * Resumes the turn timer and the animations that go along with that
   */
  private void resumeTurnTimer() {
    Log.d(TAG, "resumeTimer()");
    Log.d(TAG, Long.toString(mCounter.getTimeRemaining()));

    if (!mTurnTimeIsUp && !mCounter.isActive()) {
      mCounter.resume();
      mTimerfill
          .startAnimation(timerCountdownAnimation(mCounter.getTimeRemaining()));
    }
  }

  /**
   * Creates the menu items for the options menu
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, R.string.menu_EndTurn, 0, R.string.menu_EndTurn_Title);
    menu.add(0, R.string.menu_EndGame, 0, R.string.menu_EndGame_Title);
    menu.add(0, R.string.menu_Rules, 0, R.string.menu_Rules_Title);
    return true;
  }

  /**
   * Handle various menu clicks
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    // Handle item selection
    switch (item.getItemId()) {
    case R.string.menu_EndTurn:
      // Play confirmation sound
      sm.playSound(SoundManager.Sound.CONFIRM);
      this.showDialog(DIALOG_ENDTURN_ID);
      return true;
    case R.string.menu_EndGame:
      // Play confirmation sound
      sm.playSound(SoundManager.Sound.CONFIRM);
      this.showDialog(DIALOG_GAMEOVER_ID);
      return true;
    case R.string.menu_Rules:
      // Play confirmation sound
      sm.playSound(SoundManager.Sound.CONFIRM);
      startActivity(new Intent(getString(R.string.IntentRules), getIntent()
          .getData()));
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Listener for click on the timer to pause
   */
  private final OnClickListener mTimerClickListener = new OnClickListener() {
    public void onClick(View v) {
      TurnActivity.this.pauseGame();
    }
  };

  /**
   * Listener for advancing the tutorial
   */
  private final OnClickListener mTutorialClickListener = new OnClickListener() {
    public void onClick(View v) {
      if (mTutorialPage != TutorialPage.NOADVANCE) {
        advanceTutorial();
      }
    }
  };
  
  /**
   * Listener to watch when the Tutorial is finished animating
   */
  private final TutorialListener mTutorialListener = new TutorialListener() {

    @Override
    public void onTutorialEnded() {
      TurnActivity.this.showDialog(DIALOG_READY_ID);
    }
    
  };

  /**
   * Listener for menu button
   */
  private final OnClickListener mMenuButtonListener = new OnClickListener() {
    public void onClick(View v) {
      if (!mIsPaused) {
        TurnActivity.this.pauseGame();
      }

      TurnActivity.this.openOptionsMenu();
    }
  };

  /**
   * Listener for the 'Correct' button. It deals with the flip to the next card.
   */
  private final OnClickListener mCorrectListener = new OnClickListener() {
    public void onClick(View v) {
      TurnActivity.this.doCorrect();
    }
  }; // End CorrectListener

  /**
   * Listener for the 'Wrong' button. It deals with the flip to the next card.
   */
  private final OnClickListener mWrongListener = new OnClickListener() {
    public void onClick(View v) {
      TurnActivity.this.doWrong();
    }
  }; // End WrongListener

  /**
   * Listener for the 'Skip' button. This deals with moving to the next card via
   * the ViewFlipper, but denotes that the card was skipped;
   */
  private final OnClickListener mSkipListener = new OnClickListener() {
    public void onClick(View v) {
      TurnActivity.this.doSkip();
    }
  }; // End SkipListener

  /**
   * Listener for the pause overlay. It resumes the game.
   */
  private final OnClickListener mPauseListener = new OnClickListener() {
    public void onClick(View v) {
      // If music is disabled, just resume the game immediately (don't wait for
      // music to seek unless it's begun)
      if (mTurnTimeIsUp || (!mMusicEnabled && !mIsTicking)) {
        // Turn is over when timer reaches 0. At that point, we should just not
        // resume music
        TurnActivity.this.resumeGame();
      } else if (mMusicEnabled || mIsTicking) {
        // Resume must wait for music to seek back to the correct elapsed time
        BuzzWordsApplication application = (BuzzWordsApplication) TurnActivity.this
            .getApplication();
        MediaPlayer mp = application.getMusicPlayer(TurnActivity.this
            .getBaseContext());
        int elapsedtime;
        if (mMusicEnabled) {
          elapsedtime = getGameManager().getTurnTime()
              - (int) mCounter.getTimeRemaining();
        } else {
          elapsedtime = 10000 - (int) mCounter.getTimeRemaining();
        }
        // Return to the elapsed time
        mp.seekTo(elapsedtime);
        mp.setOnSeekCompleteListener(new TurnMusicListener());
      }

      // Hide overlays here so that they can't report multiple OnClick'ed while
      // music seeks
      mPauseOverlay.setVisibility(View.INVISIBLE);
      mPauseTextLayout.setVisibility(View.INVISIBLE);
    }
  }; // End CorrectListener

  /**
   * Returns an animation that brings cards into view from the right of the
   * screen
   * 
   * @return Animation that brings cards into view from the right of the screen
   */
  private Animation inFromRightAnimation() {
    Animation inFromRight = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    inFromRight.setDuration(500);
    return inFromRight;
  }

  /**
   * Returns an animation that tosses the cards from the view out to the left
   * 
   * @return Animation that tosses the cards from the view out to the left
   */
  private Animation outToLeftAnimation() {
    Animation outToLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
        0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    outToLeft.setDuration(500);
    return outToLeft;
  }

  /**
   * Returns an animation that moves the card back in from the left (on back
   * state)
   * 
   * @return Animation that moves the card back in from the left
   */
  private Animation backInFromLeftAnimation() {
    Animation outToLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
        -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    outToLeft.setDuration(500);
    return outToLeft;
  }

  /**
   * Returns an animation that moves the card out to the right (on back state)
   * 
   * @return Animation that moves the card out to the right
   */
  private Animation backOutToRightAnimation() {
    Animation inFromRight = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 1.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    inFromRight.setDuration(500);
    return inFromRight;
  }

  /**
   * Returns an animation that slides the timer off the screen
   * 
   * @return Animation that slides the timer off screen
   */
  private Animation showTimerAnim(boolean show) {
    float y0 = 0.0f;
    float y1 = -1.0f;
    Animation slideUp;
    if (show) {
      slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, y1,
          Animation.RELATIVE_TO_SELF, y0);

      slideUp.setInterpolator(new DecelerateInterpolator());
    } else {
      slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, y0,
          Animation.RELATIVE_TO_SELF, y1);

      slideUp.setInterpolator(new AccelerateInterpolator());
    }
    slideUp.setDuration(250);
    // make the element maintain its orientation even after the animation
    // finishes.
    slideUp.setFillAfter(true);
    return slideUp;
  }

  /**
   * Returns an animation that slides the buttons on or off the screen
   * 
   * @return Animation that slides the buttons on or off screen
   */
  private Animation showButtonsAnim(boolean show) {
    float y0 = 0.0f;
    float y1 = 1.0f;
    Animation slideUp;
    if (show) {
      slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, y1,
          Animation.RELATIVE_TO_SELF, y0);

      slideUp.setInterpolator(new DecelerateInterpolator());
    } else {
      slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
          Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, y0,
          Animation.RELATIVE_TO_SELF, y1);

      slideUp.setInterpolator(new AccelerateInterpolator());
    }
    slideUp.setDuration(250);

    // make the element maintain its orientation even after the animation
    // finishes.
    slideUp.setFillAfter(true);
    return slideUp;
  }

  /**
   * Animation method for the timer bar that takes the time remaining
   * and sets the bar to scale from the existing percent to 0 over the time. 
   * 
   * @return The animation that scales the timer as the time depletes
   */
  private Animation timerCountdownAnimation(float time) {
    float percentTimeLeft = ((float) time / mGameManager
        .getTurnTime());

    ScaleAnimation scaleTimer = new ScaleAnimation(percentTimeLeft, 0.0f, 1.0f,
        1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
        1.0f);

    int duration = (int) mCounter.getTimeRemaining();

    scaleTimer.setDuration(duration);
    scaleTimer.setInterpolator(new LinearInterpolator());
    return scaleTimer;
  }

  /**
   * Pause animation allows the timer to pause at a certain percentage scale
   * @param time represented by the paused timer
   * @return
   */
  private Animation timerPauseAnimation(long time) {
    float percentTimeLeft = ((float) time / mGameManager
        .getTurnTime());

    ScaleAnimation scaleTimer = new ScaleAnimation(percentTimeLeft, percentTimeLeft, 1.0f,
        1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
        1.0f);

    scaleTimer.setFillAfter(true);
    scaleTimer.setInterpolator(new LinearInterpolator());
    return scaleTimer;
  }

  /**
   * Works with GameManager to perform the back end processing of a correct
   * card. For consistency this method was created to match the skip
   * architecture. Also for consistency the sound for correct cards will be
   * handled in this method.
   */
  protected void doCorrect() {
    ViewFlipper flipper = (ViewFlipper) findViewById(R.id.Turn_ViewFlipper);

    mAIsActive = !mAIsActive;
    flipper.showNext();
    mGameManager.processCard(Card.RIGHT);

    // Mark the card with an icon
    setCardStatusView(Card.RIGHT);

    // Only play sound once card has been processed so we don't confuse the user
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    sm.playSound(SoundManager.Sound.RIGHT);

    // Show the next card
    showNextCard();
  }

  /**
   * Works with GameManager to perform the back end processing of a card wrong.
   * Also handles playing of a durative incorrect sound, as opposed to the
   * buzzer.
   */
  protected void doWrong() {
    mAIsActive = !mAIsActive;
    ViewFlipper flipper = (ViewFlipper) findViewById(R.id.Turn_ViewFlipper);
    flipper.showNext();

    mGameManager.processCard(Card.WRONG);

    // Mark the card with an icon
    setCardStatusView(Card.WRONG);

    // Only play sound once card has been processed so we don't confuse the user
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    sm.playSound(SoundManager.Sound.WRONG);

    // Show the next card
    showNextCard();
  }

  /**
   * Works with GameManager to perform the back end processing of a card skip.
   * Also handles the sound for skipping so that all forms of skips (swipes or
   * button clicks) play the sound.
   */
  protected void doSkip() {
    mAIsActive = !mAIsActive;
    mViewFlipper.showNext();
    mGameManager.processCard(Card.SKIP);

    // Mark the card with an icon for SKIP
    setCardStatusView(Card.SKIP);

    // Only play sound once card has been processed so we don't confuse the user
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    sm.playSound(SoundManager.Sound.SKIP);

    // Show the next card
    showNextCard();
  }

  /**
   * Sets the card status view to represent the specified RWS status
   * @param rws (from Card class)
   */
  protected void setCardStatusView(int rws)
  {
    switch(rws){
    case Card.RIGHT:
      mCardStatus.setBackgroundResource(Card.getCardMarkDrawableId(Card.RIGHT));
      mCardStatus.setVisibility(View.VISIBLE);
      break;
    case Card.WRONG:
      mCardStatus.setBackgroundResource(Card.getCardMarkDrawableId(Card.WRONG));
      mCardStatus.setVisibility(View.VISIBLE);
      break;
    case Card.SKIP:
      mCardStatus.setBackgroundResource(Card.getCardMarkDrawableId(Card.SKIP));
      mCardStatus.setVisibility(View.VISIBLE);
      break;
    default:
      mCardStatus.setVisibility(View.INVISIBLE);
      break;
    }
  }
  
  /**
   * Handle when a back button is pressed (we only let the user go back one card
   * at this time.
   */
  protected void doBack() {
    if (mIsBack) {
      return;
    }

    mIsBack = true;

    mAIsActive = !mAIsActive;

    // Reassign view to animate backwards
    mViewFlipper.setInAnimation(backInFromLeftAnimation());
    mViewFlipper.setOutAnimation(backOutToRightAnimation());

    mViewFlipper.showNext();

    this.assignActiveCardViewReferences();
    mGameManager.processCard(Card.NOTSET);
    mGameManager.getPreviousCard();
    showCurrentCard();

    // Restore animations for future actions
    mViewFlipper.setInAnimation(inFromRightAnimation());
    mViewFlipper.setOutAnimation(outToLeftAnimation());

    // Play back sound
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    sm.playSound(SoundManager.Sound.BACK);
  }

  /**
   * Updates references to reflect the A or B status of the current card
   */
  protected void assignActiveCardViewReferences() {
    int curTitle;
    int curWords;
    int curStatus;
    if (mAIsActive) {
      curTitle = R.id.Turn_CardTitleA;
      curWords = R.id.Turn_CardA_BadWords;
      curStatus = R.id.Turn_StatusImageA;
    } else {
      curTitle = R.id.Turn_CardTitleB;
      curWords = R.id.Turn_CardB_BadWords;
      curStatus = R.id.Turn_StatusImageB;
    }

    mCardTitle = (TextView) this.findViewById(curTitle);
    mCardBadWords = (LinearLayout) this.findViewById(curWords);
    mCardStatus = (ImageView) this.findViewById(curStatus);

  }

  /**
   * Function for changing the currently viewed card. It does a bit of bounds
   * checking.
   */
  protected void showNextCard() {
    
    mGameManager.getNextCard(this.getBaseContext());
    mIsBack = false;
    showCurrentCard();
    
  }
  
  /**
   * Update the views for the current card to reflect the current card
   */
  protected void showCurrentCard() {
    this.assignActiveCardViewReferences();
    
    Card curCard = this.getGameManager().getCurrentCard();
    mCardTitle.setText(curCard.getTitle());
    // Update the badwords
    this.setBadWords(mCardBadWords, curCard, mGameManager.getActiveTeam());
    
    // Show the card status if it has one
    setCardStatusView(curCard.getRws());
  }

  /**
   * Sets the text in a layout to match a supplied StringArray of badWords.
   * 
   * @param wordLayout
   *          LinearLayout of textViews
   * @param curCard
   *          Card object to set badWords from
   */
  private void setBadWords(LinearLayout wordLayout, Card curCard, Team curTeam) {
    TextView text;
    int color = this.getResources().getColor(curTeam.getSecondaryColor());
    ArrayList<String> badwords = curCard.getBadWords();

    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(getBaseContext());

    int numBuzzwords = Integer.valueOf(sp.getString(
        Consts.PREFKEY_NUM_BUZZWORDS, "5"));
    int maxBuzzwords = wordLayout.getChildCount();

    for (int i = 0; i < maxBuzzwords; ++i) {
      text = (TextView) wordLayout.getChildAt(i);
      text.setText(badwords.get(i));
      text.setTextColor(color);
    }

    int numToHide = maxBuzzwords - numBuzzwords;
    for (int i = 1; i <= numToHide; ++i) {
      wordLayout.getChildAt(maxBuzzwords - i).setVisibility(View.GONE);
    }
  }
  
  /**
   * Clears the card text views, used when the tutorial needs to wipe it clean.
   */
  private void clearCardViews()
  {
    this.assignActiveCardViewReferences();
    
    mCardTitle.setText("");
    for (int i = 0; i < mCardBadWords.getChildCount(); ++i) {
      ((TextView) mCardBadWords.getChildAt(i)).setText("");
    }
  }
  
  /**
   * OnTimeExpired defines what happens when the player's turn timer runs out
   */
  protected void onTimeExpired() {
    mResultsDelay = new PauseTimer(1500) {

      @Override
      public void onFinish() {
        TurnActivity.this.onDelayTimerExpired();
      }

      @Override
      public void onTick() {
        // Do nothing on tick
      }
    };
    mResultsDelay.start();

    // Hide timer bar and time
    mTimerfill.setVisibility(View.INVISIBLE);
    mMenuBar.startAnimation(this.showTimerAnim(false));
    // Hide buttons

    mButtonGroup.startAnimation(this.showButtonsAnim(false));

    // Only play gong if music is off
    if (!mMusicEnabled) {
      SoundManager sm = SoundManager.getInstance(this.getBaseContext());
      sm.playSound(SoundManager.Sound.GONG);
    }

    TextView timer = (TextView) this.findViewById(R.id.Turn_Timer);
    timer.setVisibility(View.INVISIBLE);

    // Hide card
    this.assignActiveCardViewReferences();
    mViewFlipper.setVisibility(View.INVISIBLE);

    // Turn off buttons
    mBuzzerButton.setEnabled(false);
    mSkipButton.setEnabled(false);
    mNextButton.setEnabled(false);
    mMenuButton.setEnabled(false);
    mTimerGroup.setEnabled(false);

    TextView timesUpView = (TextView) this.findViewById(R.id.Turn_TimesUp);
    timesUpView.setVisibility(View.VISIBLE);
  }
  
  /**
   * Method contains everything that should happen when the
   * second timer goes off, the one that delays before TurnSummary.
   */
  private void onDelayTimerExpired()
  {
    endTurnAndAdvance();
  }

  /**
   * Hands off the intent to the next turn summary activity.
   */
  protected void endTurnAndAdvance() {
    wrapupTurn();
    mExitingToNextActivity = true;
    startActivity(new Intent(getString(R.string.IntentTurnSummary), getIntent()
        .getData()));
  }
  
  /**
   * Wrapup turn handles score and cleanup of variables that is necessary
   * just before the Turn activity ends
   */
  protected void wrapupTurn()
  {
    // Must force flag time up in case they ended through menu
    mTurnTimeIsUp = true;
    BuzzWordsApplication application = (BuzzWordsApplication) TurnActivity.this
        .getApplication();
    
    // Clean up Music resources
    application.cleanUpMusicPlayer();
    
    GameManager gm = this.getGameManager();
    gm.addTurnScore();
  }

  /**
   * Get references to all of the UI elements that we need to work with after
   * the activity creation
   */
  protected void setupViewReferences() {
    mPauseOverlay = (View) this.findViewById(R.id.Turn_PauseOverlay);
    mCountdownText = (TextView) findViewById(R.id.Turn_Timer);
    mViewFlipper = (ViewFlipper) this.findViewById(R.id.Turn_ViewFlipper);
    mTimesUpText = (TextView) this.findViewById(R.id.Turn_TimesUp);

    mBuzzerButton = (ImageButton) this.findViewById(R.id.Turn_ButtonWrong);
    mNextButton = (ImageButton) this.findViewById(R.id.Turn_ButtonCorrect);
    mSkipButton = (ImageButton) this.findViewById(R.id.Turn_ButtonSkip);
    mMenuButton = (Button) this.findViewById(R.id.Turn_TimerMenuButton);

    mTimerfill = (ImageView) this.findViewById(R.id.Turn_TimerFill);
    mPauseTextLayout = (LinearLayout) this
        .findViewById(R.id.Turn_PauseTextGroup);

    mMenuBar = (RelativeLayout) this.findViewById(R.id.Turn_HighBar);
    mTimerGroup = (RelativeLayout) this.findViewById(R.id.Turn_TimerBar);
    mButtonGroup = (RelativeLayout) this.findViewById(R.id.Turn_LowBar);
    
    mTutorialLayout = (TutorialLayout) this.findViewById(R.id.Turn_TutorialLayout);
  }

  /**
   * Set the initial properties for UI elements
   */
  protected void setupUIProperties() {
    mPauseOverlay.setVisibility(View.INVISIBLE);

    mViewFlipper.setInAnimation(inFromRightAnimation());
    mViewFlipper.setOutAnimation(outToLeftAnimation());

    // Set visibility and control of Skip Button
    if (mSkipEnabled) {
      mSkipButton.setVisibility(View.VISIBLE);
    } else {
      mSkipButton.setVisibility(View.INVISIBLE);
    }

    // Change views to appropriate team color
    ImageView barFill = (ImageView) this.findViewById(R.id.Turn_TimerFill);
    // Color timer fill
    Team curTeam = this.getGameManager().getActiveTeam();
    barFill.setImageResource(curTeam.getPrimaryColor());

    // Set Turn Timer frame color
    Drawable timerFrameBG = getResources().getDrawable(
        R.drawable.gameend_row_end_white);
    ImageView timerFrame = (ImageView) this.findViewById(R.id.Turn_TimerFrame);
    timerFrameBG.setColorFilter(
        getResources().getColor(R.color.genericBG_trim), Mode.MULTIPLY);
    timerFrame.setBackgroundDrawable(timerFrameBG);

    // Set background gradient
    this.findViewById(R.id.Turn_Root).setBackgroundResource(
        curTeam.getGradient());
  }
  
  /**
   * Assign listeners to all the elements in TurnActivity
   */
  private void setupClickListeners()
  {
    mPauseOverlay.setOnClickListener(mPauseListener);

    mTimerGroup.setOnClickListener(mTimerClickListener);

    mBuzzerButton.setOnClickListener(mWrongListener);
    mNextButton.setOnClickListener(mCorrectListener);
    mSkipButton.setOnClickListener(mSkipListener);

    // assign click to menu button
    mMenuButton.setOnClickListener(mMenuButtonListener);

    // Listen for all gestures
    mSwipeDetector = new GestureDetector(mSwipeListener);
    mGestureListener = new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        if (mSwipeDetector.onTouchEvent(event)) {
          return true;
        }
        return true; // prevents highlighting badwords by consuming even if
        // not detected as a swipe
      }
    };

    // Setup the "card" views to allow for skip gesture to be performed on top
    this.findViewById(R.id.Turn_CardTitleA)
        .setOnTouchListener(mGestureListener);
    this.findViewById(R.id.Turn_CardA_BadWords).setOnTouchListener(
        mGestureListener);
    this.findViewById(R.id.Turn_CardTitleB)
        .setOnTouchListener(mGestureListener);
    this.findViewById(R.id.Turn_CardB_BadWords).setOnTouchListener(
        mGestureListener);
    this.findViewById(R.id.Turn_Root).setOnTouchListener(mGestureListener);
    this.findViewById(R.id.Turn_ViewFlipper).setOnTouchListener(
        mGestureListener);
    this.findViewById(R.id.Turn_CardLayoutA).setOnTouchListener(
        mGestureListener);
    this.findViewById(R.id.Turn_CardLayoutB).setOnTouchListener(
        mGestureListener);

  }

  /**
   * Setup the turn timer and return a reference to it
   * 
   * @return a reference to the turn timer
   */
  public PauseTimer setupTurnTimer(long time) {

    // Initialize the turn timer
    return new PauseTimer(time) {

      @Override
      public void onFinish() {
        TurnActivity.this.onTimeExpired();
        mCountdownText.setText("0");
        mTurnTimeIsUp = true;
      }

      @Override
      public void onTick() {
        // Update our text each second
        long remainingTime = (mCounter.getTimeRemaining() / 1000) + 1;
        mCountdownText.setText(Long.toString(remainingTime));

        // When music is not enabled, use the ticking sound
        if (!mMusicEnabled && !mIsTicking) {
          if (remainingTime == 10) {
            SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

            // Only play ticking when sound effects are enabled
            if (sp.getBoolean(Consts.PREFKEY_SFX, true)) {
              mIsTicking = true;
              BuzzWordsApplication application = (BuzzWordsApplication) TurnActivity.this
                  .getApplication();
              MediaPlayer mp = application.getMusicPlayer(TurnActivity.this
                  .getBaseContext());
              mp.start();
            }
          }
        }
      }

    };

  }

  /**
   * Initializes the activity to display the word you have to cause your team
   * mates to say with the words you cannot say below.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Setup the view
    this.setContentView(R.layout.turn);

    final float scale = TurnActivity.this.getResources().getDisplayMetrics().density;
    mGestureThreshold = (int) (SWIPE_MIN_DISTANCE_DP * scale + 0.5f);
    mGestureVelocityThreshold = (int) (SWIPE_THRESHOLD_VELOCITY_DP * scale + 0.5f);

    // Capture our preference variable for music, skip, and gestures once
    SharedPreferences sp = PreferenceManager
        .getDefaultSharedPreferences(getBaseContext());

    mMusicEnabled = sp.getBoolean(Consts.PREFKEY_MUSIC, true);

    // Set local variable for skip preference to reduce calls to get
    mSkipEnabled = sp.getBoolean(Consts.PREFKEY_SKIP, true);

    // Set local variable for allowing gesture preference to reduce get calls
    mGesturesEnabled = sp.getBoolean(Consts.PREFKEY_GESTURES, true);

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    mGameManager = this.getGameManager();

    this.setupViewReferences();
    this.setupUIProperties();

    SharedPreferences turnStatePrefs = this.getSharedPreferences(
        Consts.PREFFILE_TURN_STATE, Context.MODE_PRIVATE);
    boolean isAppRestored = turnStatePrefs
        .contains(Consts.PREFKEY_IS_TURN_IN_START_DIALOG);

    // Restore State of Turn based on preferences
    // Set which card is active
    mAIsActive = true;
    mIsBack = true;
    boolean isTurnInProgress = turnStatePrefs.getBoolean(
        Consts.PREFKEY_IS_TURN_IN_PROGRESS, false);
    mIsWaitingForTeamReady = turnStatePrefs.getBoolean(
        Consts.PREFKEY_IS_TURN_IN_START_DIALOG, true);

    if (mIsWaitingForTeamReady) {
      // Dialog is automatically restored by OS, so only show it if it is not a
      // restore.
      if (!isAppRestored) {
        this.showDialog(DIALOG_READY_ID);
      }
      mCounter = createTurnTimer(mGameManager.getTurnTime());
    } else if (isTurnInProgress) {
      mAIsActive = turnStatePrefs.getBoolean(Consts.PREFKEY_A_IS_ACTIVE, true);
      if (!mAIsActive) {
        mViewFlipper.showNext();
      }
      mIsBack = turnStatePrefs.getBoolean(Consts.PREFKEY_IS_BACK, true);
      mIsTicking = turnStatePrefs.getBoolean(Consts.PREFKEY_IS_TICKING, false);
      long curTime = turnStatePrefs.getLong(Consts.PREFKEY_TURN_TIME_REMAINING,
          mGameManager.getTurnTime());
      mCounter = createTurnTimer(curTime);
      this.showCurrentCard();
      this.pauseGame();
    } else {
      // Spoof time expired for ResultsDelay - no need in delaying if
      // starting from a finished turn
      onDelayTimerExpired();
    }
    
  }
  /**
   * Helper function ensures we use the same view when setting KeepScreenOn
   * 
   * @param sleep
   */
  private void setSleepAllowed(boolean sleep) {
    View view = this.findViewById(R.id.Turn_MasterLayout);
    view.setKeepScreenOn(!sleep);
  }

  /**
   * Make sure to pause the game if the activity loses focus (enters the
   * "paused" state)
   */
  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, "onPause()");

    if (!mTurnTimeIsUp && !mIsWaitingForTeamReady) {
      this.pauseIfNotPaused();
    }
    
    // Pause the delay timer when backgrounding. This fixes an issue
    // when you background the app during TimesUp. Turn Pauses and
    // serializes, but the activity advances while backgrounded,
    // meaning onPause does not get called again and the Preference
    // clear is bypassed
    if (mTurnTimeIsUp && !mExitingToNextActivity) {
      mResultsDelay.pause();
    }
    
    mGameManager.updateSeenFields(this.getBaseContext());
    
    SharedPreferences turnStatePrefs =
        this.getSharedPreferences(Consts.PREFFILE_TURN_STATE, Context.MODE_PRIVATE);
    SharedPreferences.Editor turnStatePrefsEditor = turnStatePrefs.edit();

    if (mExitingToNextActivity) {
      // When exiting to TurnSummary we clear the results so that next time
      // through we don't restore to this state
      turnStatePrefsEditor.clear();
    } else {
      mGameManager.saveState(this.getBaseContext());
      turnStatePrefsEditor.putBoolean(Consts.PREFKEY_IS_TURN_IN_START_DIALOG,
          mIsWaitingForTeamReady);
      turnStatePrefsEditor.putBoolean(Consts.PREFKEY_IS_TURN_IN_PROGRESS,
          !mTurnTimeIsUp);
      turnStatePrefsEditor.putBoolean(Consts.PREFKEY_A_IS_ACTIVE, mAIsActive);
      turnStatePrefsEditor.putBoolean(Consts.PREFKEY_IS_BACK, mIsBack);
      turnStatePrefsEditor.putBoolean(Consts.PREFKEY_IS_TICKING, mIsTicking);
      turnStatePrefsEditor.putLong(Consts.PREFKEY_TURN_TIME_REMAINING,
          mCounter.getTimeRemaining());
    }
    turnStatePrefsEditor.commit();
  }
  
  /**
   * Restores anything that was saved off in onPause
   */
  @Override
  public void onResume() {
    super.onResume();

    if(mResultsDelay != null)
    {
      mResultsDelay.resume();
    }
  }

  /**
   * Pauses the game only if it is not already paused
   */
  public void pauseIfNotPaused() {
    if(!mIsPaused && !mIsWaitingForTeamReady) {
      this.pauseGame();
    }
  }
  
  /**
   * Display a dialog when the search is requested
   */
  @Override
  public boolean onSearchRequested() {
    Log.d(TAG, "onSearchRequested()");
    return false;
  }

  /**
   * Create game over and ready dialogs using builders
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    Dialog dialog = null;
    AlertDialog.Builder builder = null;

    switch (id) {
    case DIALOG_ENDTURN_ID:
      builder = new AlertDialog.Builder(this);
      builder.setMessage(this.getString(R.string.menu_EndTurn_Text))
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound
              SoundManager sm = SoundManager.getInstance(TurnActivity.this
                  .getBaseContext());
              sm.playSound(SoundManager.Sound.CONFIRM);
              endTurnAndAdvance();
            }
          }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound
              SoundManager sm = SoundManager.getInstance(TurnActivity.this
                  .getBaseContext());
              sm.playSound(SoundManager.Sound.CONFIRM);

              dialog.cancel();
            }
          });
      dialog = builder.create();
      break;
    case DIALOG_GAMEOVER_ID:
      builder = new AlertDialog.Builder(this);
      builder.setMessage(this.getString(R.string.menu_EndGame_Text))
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound
              SoundManager sm = SoundManager.getInstance(TurnActivity.this
                  .getBaseContext());
              sm.playSound(SoundManager.Sound.CONFIRM);

              wrapupTurn();
              getGameManager().endGame();
              
              mExitingToNextActivity = true;
              startActivity(new Intent(getString(R.string.IntentEndGame),
                  getIntent().getData()));
            }
          }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              // Play confirmation sound
              SoundManager sm = SoundManager.getInstance(TurnActivity.this
                  .getBaseContext());
              sm.playSound(SoundManager.Sound.CONFIRM);

              dialog.cancel();
            }
          });
      dialog = builder.create();
      break;
    case DIALOG_READY_ID:

      // Play team ready sound
      SoundManager sm = SoundManager.getInstance(TurnActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.TEAMREADY);

      String curTeam = mGameManager.getActiveTeam().getName();

      // Setup the dialog
      builder = new AlertDialog.Builder(this);
      builder.setMessage("Ready " + curTeam + "?").setCancelable(false)
          .setPositiveButton("Start!", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              mIsWaitingForTeamReady = false;
              startGame();
            }

          });
      
      // Add on the tutorial button if set in the preferences
      SharedPreferences sp = PreferenceManager
          .getDefaultSharedPreferences(getBaseContext());
      boolean showTutorial = sp.getBoolean(
          Consts.PREFKEY_SHOWTUTORIAL_TURN, true);
      if (showTutorial) {
        builder.setNegativeButton("No, How do I play?",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startTutorial();
              }

            });
      }

      // Create the dialog
      dialog = builder.create();

      // We add an onDismiss listener to handle the case in which a user
      // attempts to search on the ready dialog
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

        public void onDismiss(DialogInterface dialog) {
          // If it's paused but they are not in tutorial mode, re-show Ready
          if (mIsPaused && !mIsInTutorial) {
            showDialog(DIALOG_READY_ID);
          }
        }

      });
      break;
    default:
      dialog = null;
    }
    return dialog;

  }

  /**
   * Class tracks the seek time on the music realignment that happens on every
   * resume.
   */
  private class TurnMusicListener implements OnSeekCompleteListener {
    public void onSeekComplete(MediaPlayer mp) {
      // Resume the game on seek complete
      TurnActivity.this.resumeGame();

      // Resume the music
      if (mMusicEnabled || (!mMusicEnabled && mIsTicking)) {
        mp.start();
      }
    }
  }

  /**
   * Resume the game by showing/enabling the proper view elements and resuming
   * the turn timer.
   */
  protected void resumeGame() {
    Log.d(TAG, "resumeGameTurn()");

    mIsPaused = false;

    setSleepAllowed(false);

    if (!mTurnTimeIsUp) {
      this.resumeTurnTimer();

      this.assignActiveCardViewReferences();

      // Play resume sound
      SoundManager sm = SoundManager.getInstance(this.getBaseContext());
      sm.playSound(SoundManager.Sound.BACK);

      mViewFlipper.setVisibility(View.VISIBLE);

      mBuzzerButton.setEnabled(true);
      mSkipButton.setEnabled(true);
      mNextButton.setEnabled(true);

      // Allow timer to pause the game again
      mTimerGroup.setEnabled(true);

      mButtonGroup.startAnimation(this.showButtonsAnim(true));
    } else {
      mResultsDelay.resume();

      // Show TimesUp text when resuming after time has expired
      mTimesUpText.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Perform the logic of hiding card elements and stopping the turn timer when
   * we pause the game through whatever pausing method.
   */
  protected void pauseGame() {
    Log.d(TAG, "pauseGame()");

    // Allow phone to sleep while paused
    setSleepAllowed(true);

    mIsPaused = true;
    mPauseOverlay.setVisibility(View.VISIBLE);

    mPauseTextLayout = (LinearLayout) this
        .findViewById(R.id.Turn_PauseTextGroup);
    mPauseTextLayout.setVisibility(View.VISIBLE);

    // Stop music
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer(TurnActivity.this
        .getBaseContext());
    if (mp.isPlaying()) {
      mp.pause();
    }

    // Play ready sound since it indicates a wait.
    // This is the menu method that is called on every menu push
    SoundManager sm = SoundManager.getInstance(this.getBaseContext());
    sm.playSound(SoundManager.Sound.TEAMREADY);

    // Don't let timer unpause the game. This was just making it 
    // really easy to double tap and pause unpause on accident
    mTimerGroup.setEnabled(false);

    if (!mTurnTimeIsUp) {
      this.stopTurnTimer();

      this.assignActiveCardViewReferences();

      mViewFlipper.setVisibility(View.INVISIBLE);
      mBuzzerButton.setEnabled(false);
      mSkipButton.setEnabled(false);
      mNextButton.setEnabled(false);

      mButtonGroup.startAnimation(this.showButtonsAnim(false));
    } else {
      mResultsDelay.pause();

      // Hide TimesUp text when pause after time has expired
      mTimesUpText.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * Helper function to tell all game elements to start
   */
  private void startGame() {
    mIsPaused = false;
    
    // Assign listeners to views at start game to prevent illegal
    // clicks before game starts (tutorial).
    setupClickListeners();
    
    TurnActivity.this.showNextCard();
    mIsBack = true;
    TurnActivity.this.startTurnTimer();

    setSleepAllowed(false);

    // Play back sound to differentiate from normal clicks
    SoundManager sm = SoundManager.getInstance(TurnActivity.this
        .getBaseContext());
    sm.playSound(SoundManager.Sound.BACK);

    // Start the turn music
    BuzzWordsApplication application = (BuzzWordsApplication) TurnActivity.this
        .getApplication();
    GameManager gm = this.getGameManager();

    int musicId = R.raw.mus_countdown;
    // If music is enabled, select the appropriate track
    if (mMusicEnabled) {
      switch (gm.getTurnTime()) {
      case 30000:
        musicId = R.raw.mus_round_30;
        break;
      case 60000:
        musicId = R.raw.mus_round_60;
        break;
      case 90000:
        musicId = R.raw.mus_round_90;
        break;
      }
    }

    MediaPlayer mp = application.createMusicPlayer(
        TurnActivity.this.getBaseContext(), musicId);
    // If music is not enabled, it will start the countdown track at
    // 10 seconds
    if (mMusicEnabled) {
      mp.start();
    }
  }
  

  /**
   * Begins the tutorial by initializing views and references, and showing the layout
   */
  private void startTutorial() {
    
    mIsInTutorial = true;
    
    // Set the listener to get a callback when the view is done
    mTutorialLayout.setTutorialListener(mTutorialListener);
    
    // Set the first page of the tutorial
    mTutorialPage = TutorialPage.TITLE;
    
    // Assign references to match the shown tutorial card
    this.assignActiveCardViewReferences();
    
    // Create a temporary card that shows tutorial entries.
    Card tutorialCard = new Card(-1, "Puppy", "Dog,Bark,Litter,Baby,Train", null);
    mCardTitle.setText(tutorialCard.getTitle());
    this.setBadWords(mCardBadWords, tutorialCard, mGameManager.getActiveTeam());
    
    // Advance the tutorial to the first page
    advanceTutorial();
    
    // Assign a listener to catch click events on the Tutorial
    mTutorialLayout.setClickListener(mTutorialClickListener);
  }
  
  /**
   * Advance the tutorial and the content to the next stage
   */
  private void advanceTutorial()
  {
    // Sets the content and the next tutorial page for the given tutorial page
    switch(mTutorialPage)
    {
    case TITLE:
      mTutorialLayout.setContent(mCardTitle, getResources().getString(R.string.tutorial_turn_guess), TutorialLayout.CENTER);
      mTutorialPage = TutorialPage.BADWORDS;
      break;
    case BADWORDS:
      mTutorialLayout.setContent(mCardBadWords, getResources().getString(R.string.tutorial_turn_buzzwords),
          TutorialLayout.TOP);
      mTutorialPage = TutorialPage.WRONG;
      break;
    case RIGHT:
      mTutorialLayout.setContent(mNextButton, getResources().getString(R.string.tutorial_turn_right),
          TutorialLayout.CENTER);
      if (mSkipEnabled) {
        mTutorialPage = TutorialPage.SKIP;
      } else {
        mTutorialPage = TutorialPage.TIME;
      }
      break;
    case WRONG:
      mTutorialLayout.setContent(mBuzzerButton, getResources().getString(R.string.tutorial_turn_wrong),
          TutorialLayout.CENTER);
      mTutorialPage = TutorialPage.RIGHT;
      break;
    case SKIP:
      mTutorialLayout.setContent(mSkipButton, getResources().getString(R.string.tutorial_turn_skip),
          TutorialLayout.CENTER);
      mTutorialPage = TutorialPage.TIME;
      break;
    case TIME:
      mTutorialLayout.setContent(mTimerGroup, getResources().getString(R.string.tutorial_turn_time),
          TutorialLayout.CENTER);
      mTutorialPage = TutorialPage.END;
      break;
    case END:
      // Set TutorialPage to a value that will prevent tutorial advancement
      mTutorialPage = TutorialPage.NOADVANCE;
      stopTutorial();
      break;
    case NOADVANCE:
      break;
    default:
      stopTutorial();
      break;
    }
  }
  
  /**
   * Performs all actions to stop the tutorial and return to game ready.
   */
  private void stopTutorial()
  {
    mIsInTutorial = false;
    mTutorialLayout.hide();
    this.clearCardViews();
  }


  /**
   * We make sure to pause the game when the menu is opened
   */
  @Override
  public boolean onMenuOpened(int featureId, Menu menu) {
    pauseIfNotPaused();
    return true;
  }

  /**
   * Handler for key down events. This will start tracking the back button event
   * so we can properly catch it and move back between cards instead of
   * activities
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Handle the back button
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      event.startTracking();
      return true;
    }
    // Consume the search button
    else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
      return false;
    }
    return super.onKeyDown(keyCode, event);
  }

  /**
   * Handle the back button such that we go back between cards instead of to the
   * previous activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    // Back button should go to the previous card
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled()) {
      if (!(mIsPaused || mTurnTimeIsUp)) {
        this.doBack();
      }
      else if (mIsInTutorial)
      {
        // Stop the tutorial if they press Back during it
        stopTutorial();
      }
      return true;
    }

    return super.onKeyUp(keyCode, event);
  }

  /**
   * Consume touch events to prevent anything other than the swipe detector from
   * operating
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mSwipeDetector != null && mSwipeDetector.onTouchEvent(event))
      return true;
    else
      return false;
  }
}
