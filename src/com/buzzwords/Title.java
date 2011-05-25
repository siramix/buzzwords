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

import com.buzzwords.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * This is the activity class that kicks off BuzzWords and displays a nice title
 * with basic menu options
 * 
 * @author Siramix Labs
 */
public class Title extends Activity {
  /**
   * logging tag
   */
  public static String TAG = "Title";

  /**
   * flag used for stopping music OnStop() event.
   */
  private boolean mContinueMusic;

  /**
   * PlayGameListener is used for the start game button. It launches the next
   * activity.
   */
  private OnTouchListener mTouchPlayListener = new OnTouchListener() {

    public boolean onTouch(View v, MotionEvent event) {
      // return out if views are not enabled
      if (!v.isEnabled()) {
        return true;
      }
      // Make this seem like a button OnClick for both the label and the button
      int action = event.getAction();
      if (action == MotionEvent.ACTION_DOWN) {
        highlightDelegateItems(v.getId(), true);
      } else if (action == MotionEvent.ACTION_MOVE) {
        // Check if the move happened in the bounds of this view
        Rect bounds = new Rect();
        v.getHitRect(bounds);
        // If in bounds, we have to re-highlight in case we went out of bounds
        // previously
        if (bounds.contains((int) event.getX(), (int) event.getY())) {
          highlightDelegateItems(v.getId(), true);
        } else {
          highlightDelegateItems(v.getId(), false);
        }
      } else {
        highlightDelegateItems(v.getId(), false);
      }

      return false;
    }
  };

  /**
   * Helper function to highlight a view given its Id
   * 
   * @param id
   */
  private void highlightDelegateItems(int id, boolean on) {
    ImageButton button = (ImageButton) Title.this
        .findViewById(R.id.Title_PlayButton);
    TextView label = (TextView) Title.this.findViewById(R.id.Title_PlayText);
    switch (id) {
    case R.id.Title_PlayDelegate:
      button = (ImageButton) Title.this.findViewById(R.id.Title_PlayButton);
      label = (TextView) Title.this.findViewById(R.id.Title_PlayText);
      if (on) {
        button.setBackgroundResource(R.drawable.title_play_onclick);
        label.setTextColor(Title.this.getResources().getColor(
            R.color.teamB_highlight));
      } else {
        button.setBackgroundResource(R.drawable.title_play);
        label.setTextColor(Title.this.getResources().getColor(
            R.color.teamB_primary));
      }
      break;
    case R.id.Title_BuzzDelegate:
      button = (ImageButton) Title.this.findViewById(R.id.Title_BuzzButton);
      label = (TextView) Title.this.findViewById(R.id.Title_BuzzText);
      if (on) {
        button.setBackgroundResource(R.drawable.title_buzzer_onclick);
        label.setTextColor(Title.this.getResources().getColor(
            R.color.teamC_highlight));
      } else {
        button.setBackgroundResource(R.drawable.title_buzzer);
        label.setTextColor(Title.this.getResources().getColor(
            R.color.teamC_primary));
      }
      break;
    case R.id.Title_SettingsDelegate:
      button = (ImageButton) Title.this.findViewById(R.id.Title_SettingsButton);
      label = (TextView) Title.this.findViewById(R.id.Title_SettingsText);
      if (on) {
        button.setBackgroundResource(R.drawable.title_settings_onclick);
        label.setTextColor(Title.this.getResources().getColor(
            R.color.teamD_highlight));
      } else {
        button.setBackgroundResource(R.drawable.title_settings);
        label.setTextColor(Title.this.getResources().getColor(
            R.color.teamD_primary));
      }
      break;
    case R.id.Title_RulesDelegate:
      button = (ImageButton) Title.this.findViewById(R.id.Title_RulesButton);
      label = (TextView) Title.this.findViewById(R.id.Title_RulesText);
      if (on) {
        button.setBackgroundResource(R.drawable.title_rules_onclick);
        label.setTextColor(Title.this.getResources().getColor(
            R.color.teamA_highlight));
      } else {
        button.setBackgroundResource(R.drawable.title_rules);
        label.setTextColor(Title.this.getResources().getColor(
            R.color.teamA_primary));
      }
      break;
    }
    if (on) {
      label.setTextSize(45);
    } else {
      label.setTextSize(42);
    }
  }

  /**
   * PlayGameListener plays an animation on the view that will result in
   * launching GameSetup
   */
  private OnClickListener mPlayGameListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "PlayGameListener OnClick()");
      }
      mContinueMusic = true;

      // play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) Title.this
          .getApplication();
      SoundManager sound = application.getSoundManager();
      sound.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(Title.this.getApplication().getString(
          R.string.IntentGameSetup), getIntent().getData()));
    }
  };

  /**
   * BuzzerListener plays an animation on the view that will result in launching
   * Buzz Mode
   */
  private OnClickListener mBuzzerListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "PlayGameListener OnClick()");
      }
      mContinueMusic = false;

      // play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) Title.this
          .getApplication();
      SoundManager sound = application.getSoundManager();
      sound.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(getApplication()
          .getString(R.string.IntentBuzzer), getIntent().getData()));
    }
  };

  /**
   * Listener to determine when the settings button is clicked. Includes an
   * onClick function that starts the settingsActivity.
   */
  private OnClickListener mSettingsListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "SettingsListener OnClick()");
      }
      mContinueMusic = true;

      // play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) Title.this
          .getApplication();
      SoundManager sound = application.getSoundManager();
      sound.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(Title.this.getApplication().getString(
          R.string.IntentSettings), getIntent().getData()));
    }
  }; // End SettingsListener

  /**
   * Listener to determine when the Rules button is clicked on the title screen.
   * Includes an onClick method that will start the Rule activity.
   */
  private OnClickListener mRulesListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "RulesListener OnClick()");
      }
      mContinueMusic = true;

      // play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) Title.this
          .getApplication();
      SoundManager sound = application.getSoundManager();
      sound.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(
          getApplication().getString(R.string.IntentRules), getIntent()
              .getData()));

    }
  }; // End RulesListener

  /**
   * Listener to determine when the About Us button is clicked on the title
   * screen. Includes an onClick method that will start the Rule activity.
   */
  private OnClickListener mAboutUsListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "AboutUsListener OnClick()");
      }
      mContinueMusic = false;

      // play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) Title.this
          .getApplication();
      SoundManager sound = application.getSoundManager();
      sound.playSound(SoundManager.Sound.CONFIRM);

      Uri uri = Uri.parse("http://www.siramix.com/");
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      startActivity(intent);

    }
  }; // End AboutUsListener

  /**
   * Returns the animation that brings in the buttons screen
   * 
   * @return The animation that brings in the buttons screen
   */
  private Animation translateButtons(int buttonNum) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "TranslateButtons()");
    }

    final int MOVETIME = 600;

    // Slide in from off-screen
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
        Animation.ABSOLUTE, (-600f), Animation.RELATIVE_TO_PARENT, 0.0f);
    slideIn.setDuration(MOVETIME + (200 * buttonNum));
    slideIn.setInterpolator(new DecelerateInterpolator());
    return slideIn;
  }

  /**
   * Returns the animation that brings in labels screen
   * 
   * @return The animation that brings in labels screen
   */
  private AnimationSet translateLabels(int labelNum) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "TranslateLabels()");
    }

    final int MOVETIME = 800;
    AnimationSet set = new AnimationSet(true);

    // Define the translate animation
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT, (-1.0f * labelNum),
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
        (0.0f), Animation.RELATIVE_TO_PARENT, 0.0f);
    slideIn.setDuration(MOVETIME);
    slideIn.setInterpolator(new DecelerateInterpolator());
    slideIn.setStartOffset(300 * (labelNum + 1));

    // Create entire sequence
    set.addAnimation(slideIn);
    return set;
  }

  /**
   * Initializes a welcome screen that starts the game.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreate()");
    }

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.createMusicPlayer(this.getBaseContext(),
        R.raw.mus_title);
    application.createSoundManager(this.getBaseContext());

    mp.setLooping(true);
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    if (sp.getBoolean("music_enabled", true)) {
      mp.start();
    }

    mContinueMusic = false;

    // Setup the Main Title Screen view
    this.setContentView(R.layout.title);

    // Assign listeners to the delegate buttons
    View delegate = (View) this.findViewById(R.id.Title_PlayDelegate);
    delegate.setOnTouchListener(mTouchPlayListener);
    delegate.setOnClickListener(mPlayGameListener);

    delegate = (View) this.findViewById(R.id.Title_BuzzDelegate);
    delegate.setOnTouchListener(mTouchPlayListener);
    delegate.setOnClickListener(mBuzzerListener);

    delegate = (View) this.findViewById(R.id.Title_SettingsDelegate);
    delegate.setOnTouchListener(mTouchPlayListener);
    delegate.setOnClickListener(mSettingsListener);

    delegate = (View) this.findViewById(R.id.Title_RulesDelegate);
    delegate.setOnTouchListener(mTouchPlayListener);
    delegate.setOnClickListener(mRulesListener);

    ImageButton rulesButton = (ImageButton) this
        .findViewById(R.id.Title_RulesButton);
    rulesButton.setOnClickListener(mRulesListener);

    ImageButton buzzerButton = (ImageButton) this
        .findViewById(R.id.Title_BuzzButton);
    buzzerButton.setOnClickListener(mBuzzerListener);

    ImageButton aboutusButton = (ImageButton) this
        .findViewById(R.id.Title_AboutUs);
    aboutusButton.setOnClickListener(mAboutUsListener);

    View button = (View) this.findViewById(R.id.Title_PlayButton);
    button.startAnimation(this.translateButtons(4));
    button = (View) this.findViewById(R.id.Title_BuzzButton);
    button.startAnimation(this.translateButtons(3));
    button = (View) this.findViewById(R.id.Title_SettingsButton);
    button.startAnimation(this.translateButtons(2));
    button = (View) this.findViewById(R.id.Title_RulesButton);
    button.startAnimation(this.translateButtons(1));

    // set font
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");

    TextView label = (TextView) this.findViewById(R.id.Title_PlayText);
    label.startAnimation(this.translateLabels(4));
    label.setTypeface(antonFont);
    label = (TextView) this.findViewById(R.id.Title_BuzzText);
    label.startAnimation(this.translateLabels(3));
    label.setTypeface(antonFont);
    label = (TextView) this.findViewById(R.id.Title_SettingsText);
    label.startAnimation(this.translateLabels(2));
    label.setTypeface(antonFont);
    label = (TextView) this.findViewById(R.id.Title_RulesText);
    label.startAnimation(this.translateLabels(1));
    label.setTypeface(antonFont);

  }

  /**
   * Override onPause to prevent activity specific processes from running while
   * app is in background
   */
  @Override
  public void onPause() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onPause()");
    }
    super.onPause();
    if (!mContinueMusic) {
      BuzzWordsApplication application = (BuzzWordsApplication) this
          .getApplication();
      MediaPlayer mp = application.getMusicPlayer();
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
          .getBaseContext());
      if (mp.isPlaying() && sp.getBoolean("music_enabled", true)) {
        mp.pause();
      }
    }
  }

  /**
   * Override OnResume to resume activity specific processes
   */
  @Override
  public void onResume() {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onResume()");
    }
    super.onResume();

    // Resume Title Music
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer();
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    if (sp.getBoolean("music_enabled", true)) {
      if (!mp.isPlaying()) {
        mp.start();
      }
    }
    // set flag to let onStop handle music
    mContinueMusic = false;
  }

}
