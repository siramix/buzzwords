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
import com.buzzwords.Consts;
import com.buzzwords.GameManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

/**
 * This is the activity class that kicks off BuzzWords and displays a nice title
 * with basic menu options
 * 
 * @author Siramix Labs
 */
public class TitleActivity extends Activity {
  
  /**
   * logging tag
   */
  public static String TAG = "Title";

  /**
   * flag used for stopping music OnStop() event.
   */
  private boolean mContinueMusic;

  private SharedPreferences mSharedPrefs;

  /**
   * Dialog constant for first Rate Us message
   */
  static final int DIALOG_RATEUS_FIRST = 0;

  /**
   * Dialog constant for second Rate Us message
   */
  static final int DIALOG_RATEUS_SECOND = 1;

  /**
   * Dialog constant for the first time dialog
   */
  static final int DIALOG_FIRST_TIME = 2;

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
    ImageButton button;
    TextView label = (TextView) TitleActivity.this.findViewById(R.id.Title_BuzzText);
    switch (id) {
    case R.id.Title_BuzzDelegate:
      button = (ImageButton) TitleActivity.this.findViewById(R.id.Title_BuzzButton);
      label = (TextView) TitleActivity.this.findViewById(R.id.Title_BuzzText);
      if (on) {
        button.setBackgroundResource(R.drawable.title_buzzer_onclick);
        label.setTextColor(TitleActivity.this.getResources().getColor(
            R.color.teamC_highlight));
      } else {
        button.setBackgroundResource(R.drawable.title_buzzer_normal);
        label.setTextColor(TitleActivity.this.getResources().getColor(
            R.color.teamC_primary));
      }
      break;
    case R.id.Title_SettingsDelegate:
      button = (ImageButton) TitleActivity.this.findViewById(R.id.Title_SettingsButton);
      label = (TextView) TitleActivity.this.findViewById(R.id.Title_SettingsText);
      if (on) {
        button.setBackgroundResource(R.drawable.title_settings_onclick);
        label.setTextColor(TitleActivity.this.getResources().getColor(
            R.color.teamD_highlight));
      } else {
        button.setBackgroundResource(R.drawable.title_settings_normal);
        label.setTextColor(TitleActivity.this.getResources().getColor(
            R.color.teamD_primary));
      }
      break;
    case R.id.Title_RulesDelegate:
      button = (ImageButton) TitleActivity.this.findViewById(R.id.Title_RulesButton);
      label = (TextView) TitleActivity.this.findViewById(R.id.Title_RulesText);
      if (on) {
        button.setBackgroundResource(R.drawable.title_rules_onclick);
        label.setTextColor(TitleActivity.this.getResources().getColor(
            R.color.teamA_highlight));
      } else {
        button.setBackgroundResource(R.drawable.title_rules_normal);
        label.setTextColor(TitleActivity.this.getResources().getColor(
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
      v.setEnabled(false);
      mContinueMusic = true;

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(TitleActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(TitleActivity.this.getApplication().getString(
          R.string.IntentPackPurchase), getIntent().getData()));
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
      v.setEnabled(false);
      mContinueMusic = false;

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(TitleActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

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
      v.setEnabled(false);
      mContinueMusic = true;

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(TitleActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      startActivity(new Intent(TitleActivity.this.getApplication().getString(
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
      v.setEnabled(false);
      mContinueMusic = true;

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(TitleActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

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
      v.setEnabled(false);
      mContinueMusic = false;

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(TitleActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      Uri uri = Uri.parse(getApplication().getString(R.string.URI_fb_buzzwordsapp));
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      startActivity(intent);

    }
  }; // End AboutUsListener

  /**
   * Returns the animation that scales in buttons
   * 
   * @param labelNum
   *          specifies the number this animation corresponds to. Used for start
   *          offsets.
   */
  private AnimationSet stretch(int order) {
    final int SCALE_UP_DURATION = 400;
    final int SCALE_DOWN_DURATION = 200;
    final int SCALE_NORMAL_DURATION = 100;
    final float SCALE_START = 0.0f;
    final float SCALE_MAX = 1.20f;
    final float SCALE_MIN = 0.8f;
    final float SCALE_NORMAL = 1.0f;

    AnimationSet set = new AnimationSet(true);
    set.setInterpolator(new DecelerateInterpolator());

    ScaleAnimation scaleUp = new ScaleAnimation(SCALE_START, SCALE_MAX,
        SCALE_START, SCALE_MAX, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    scaleUp.setDuration(SCALE_UP_DURATION);
    ScaleAnimation scaleDown = new ScaleAnimation(SCALE_MAX, SCALE_MIN,
        SCALE_MAX, SCALE_MIN, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    scaleDown.setDuration(SCALE_DOWN_DURATION);
    scaleDown.setStartOffset(SCALE_UP_DURATION);

    ScaleAnimation scaleNormal = new ScaleAnimation(SCALE_MIN, SCALE_NORMAL,
        SCALE_MIN, SCALE_NORMAL, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    scaleNormal.setDuration(SCALE_NORMAL_DURATION);
    scaleNormal.setStartOffset(SCALE_UP_DURATION + SCALE_DOWN_DURATION);

    set.addAnimation(scaleUp);
    set.addAnimation(scaleDown);
    set.addAnimation(scaleNormal);

    set.setStartOffset(order * 200);

    return set;
  }

  /**
   * Returns the animation that fades in labels.
   * 
   * @param labelNum
   *          specifies the number this animation corresponds to. Used for start
   *          offsets.
   */
  private AlphaAnimation fadeLabels(int labelNum) {
    AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
    alpha.setDuration(500);
    alpha.setStartOffset(labelNum * 200);
    return alpha;
  }

  /*
   * Returns the rotation animation for the starburst
   */
  private RotateAnimation rotateStarburst() {
    RotateAnimation rotate = new RotateAnimation(0, 360,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    rotate.setDuration(60000);
    rotate.setInterpolator(new LinearInterpolator());
    rotate.setRepeatCount(Animation.INFINITE);
    return rotate;
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
    mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    
    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.createMusicPlayer(this.getBaseContext(),
        R.raw.mus_title);

    mp.setLooping(true);

    if (mSharedPrefs.getBoolean(Consts.PREFKEY_MUSIC, true)) {
      mp.start();
    }
    mContinueMusic = false;

    // Setup the Main Title Screen view
    this.setContentView(R.layout.title);
    new InstallerAndAnimator().execute();
    
    // Assign listeners to the delegate buttons
    View playbutton = (View) this.findViewById(R.id.Title_Button_Play);
    playbutton.setOnClickListener(mPlayGameListener);

    View delegate = (View) this.findViewById(R.id.Title_BuzzDelegate);
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
        .findViewById(R.id.Title_FB_BuzzwordsApp);
    aboutusButton.setOnClickListener(mAboutUsListener);

    // Initialize button animations
    View tempButton;
    int[] buttons = {R.id.Title_BuzzButton, R.id.Title_SettingsButton, R.id.Title_RulesButton};
    for(int i = 0; i < buttons.length; i++)
    {
      tempButton = (View) this.findViewById(buttons[i]);
      tempButton.startAnimation(stretch(i));
    }
    
    // Initialize Labels with animation
    TextView tempLabel;
    int[] labels = {R.id.Title_BuzzText, R.id.Title_SettingsText, R.id.Title_RulesText};
    for(int i = 0; i < labels.length; i++)
    {
      tempLabel = (TextView) this.findViewById(labels[i]);
      tempLabel.startAnimation(fadeLabels(i));
    }
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
      if (mp.isPlaying() && sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
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

    // Re-enable things
    this.findViewById(R.id.Title_RulesButton).setEnabled(true);
    this.findViewById(R.id.Title_BuzzButton).setEnabled(true);
    this.findViewById(R.id.Title_FB_BuzzwordsApp).setEnabled(true);
    this.findViewById(R.id.Title_Button_Play).setEnabled(true);
    this.findViewById(R.id.Title_BuzzDelegate).setEnabled(true);
    this.findViewById(R.id.Title_SettingsDelegate).setEnabled(true);
    this.findViewById(R.id.Title_RulesDelegate).setEnabled(true);

    // Resume Title Music
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer();
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    if (sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
      if (!mp.isPlaying()) {
        mp.start();
      }
    }
    // set flag to let onStop handle music
    mContinueMusic = false;
  }

  /** 
   * Run the installation in an Async Task.  This puts the intensive task of installing
   * on a separate thread that once complete will dismiss the progress dialog and start
   * the title screen's animation.  After the first run this task will only be used
   * for animating the title screen.
   */
  private class InstallerAndAnimator extends AsyncTask <Void, Void, Boolean>
  {
      private ProgressDialog dialog;
      private SharedPreferences prefs;
      private boolean initialized;
      
      @Override
      protected void onPreExecute()
      {
        prefs = getPreferences(Context.MODE_PRIVATE);
        initialized = prefs.getBoolean(Consts.PREFKEY_DB_INITIALIZED, false);
        if (!initialized) {
          dialog = ProgressDialog.show(
            TitleActivity.this,
            null,
            getString(R.string.progressDialog_install_text), 
            true);
        }
      }

      @Override
      protected Boolean doInBackground(Void... params)
      {
        if (!initialized) {
          GameManager gm = new GameManager(TitleActivity.this);
          gm.installStarterPacks();
        }
        return true;
        
      }

      @Override
      protected void onPostExecute(Boolean result)
      {
        if (!initialized) {
          SharedPreferences.Editor edit = prefs.edit();
          edit.putBoolean(Consts.PREFKEY_DB_INITIALIZED, true);
          edit.commit();
          dialog.dismiss();
        }
        
        displayTitleDialog();

        // Animate (rotate) the starburst which would have slowed down the install process
        View starburst = (View) findViewById(R.id.Title_Starburst);
        starburst.startAnimation(rotateStarburst());
      }
  }
  
  /**
   * The Title screen should only show one dialog.  We can call this after the
   * installation check completes.  This method ensures that the correct dialog
   * displays for the Title screen depending on things like times played.
   */
  private void displayTitleDialog() {
    // Capture our play count to decide whether to show the Rate Us dialog
    if (mSharedPrefs == null) {
      mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
    }
        
    int playCount = mSharedPrefs.getInt(getResources()
                                      .getString(R.string.PREFKEY_PLAYCOUNT), 0);
    boolean showReminder = mSharedPrefs.getBoolean(getResources()
                                      .getString(R.string.PREFKEY_SHOWREMINDER), false);
    boolean showFirstRun = mSharedPrefs.getBoolean(getResources()
                                      .getString(R.string.PREFKEY_SHOWFIRSTRUN), true);

    // If playCount is 0. Show them the first-time dialog
    if (playCount == 0 && showFirstRun) {
      showDialog(DIALOG_FIRST_TIME);
    }
    
    // If 3 plays have been done and reminder is not muted, show dialog
    if (showReminder) {
      if (playCount < 6) {
        showDialog(DIALOG_RATEUS_FIRST);
      }
      else {
        showDialog(DIALOG_RATEUS_SECOND);
      }
    }
  }
  
  /**
   * Sets the boolean preference for muting the Rate Us dialog to true.
   */
  private void delayRateReminder()
  {
	if (BuzzWordsApplication.DEBUG) {
		Log.d(TAG, "delayRateReminder()");
	}
    // Prepare to edit preference for mute reminder bool
    BuzzWordsApplication application = (BuzzWordsApplication) getApplication();    
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext());
    SharedPreferences.Editor prefEditor = sp.edit();   
    
    prefEditor.putBoolean(this.getResources().getString(R.string.PREFKEY_SHOWREMINDER), false);
    prefEditor.commit();    
  }
  
  /**
   * Sets the boolean preference for muting the Rate Us dialog to true.
   */
  private void muteRateReminder()
  {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "muteRateReminder()");
    }
    // Prepare to edit preference for mute reminder bool
    BuzzWordsApplication application = (BuzzWordsApplication) getApplication();
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext());
    SharedPreferences.Editor prefEditor = sp.edit();
    
    // 7 and false will mean the user has seen the second dialog and muted it 
    prefEditor.putInt(this.getResources().getString(R.string.PREFKEY_PLAYCOUNT),7);
    prefEditor.putBoolean(this.getResources().getString(R.string.PREFKEY_SHOWREMINDER), false);
    
    prefEditor.commit();
  }

  /**
   * Set the pref for the first-open dialog to false
   */
  private void muteFirstRunDialog() {
    // Prepare to edit preference for mute reminder bool
    BuzzWordsApplication application = (BuzzWordsApplication) getApplication();
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext());
    SharedPreferences.Editor prefEditor = sp.edit();

    prefEditor.putBoolean(this.getResources().getString(R.string.PREFKEY_SHOWFIRSTRUN), false);

    prefEditor.commit();
  }

  /**
   * Custom create Dialogs
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    if (BuzzWordsApplication.DEBUG) {
      Log.d(TAG, "onCreateDialog(" + id + ")");
    }
    Dialog dialog = null;
    AlertDialog.Builder builder = null;
    
    switch (id) {
    /**
     * When players have played X times, show a dialog asking them to rate us or put it
     * off until later.  We will provide a 'Never' option as well.
     */
    case DIALOG_RATEUS_FIRST:
      
      builder = new AlertDialog.Builder(this);
      builder
          .setTitle(
              getResources().getString(R.string.rateUsFirstDialog_title))
          .setMessage(     		  
        	  getResources().getString(R.string.rateUsFirstDialog_text))
          .setPositiveButton(getResources().getString(R.string.rateUsDialog_positiveBtn), 
              new DialogInterface.OnClickListener() {            
                public void onClick(DialogInterface dialog, int id) {                                            
                  Intent intent = new Intent(Intent.ACTION_VIEW, BuzzWordsApplication.storeURI_Buzzwords);
                  startActivity(intent);
                  muteRateReminder();
                }
          })
          .setNegativeButton(getResources().getString(R.string.rateUsDialog_neutralBtn),
	              new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                  delayRateReminder();
	                }
	        }
            );
      dialog = builder.create();
      break;
    
    /**
     * The second more urgent dialog shows after 6 plays
     */
    case DIALOG_RATEUS_SECOND:
      builder = new AlertDialog.Builder(this);
      builder
          .setTitle(
              getResources().getString(R.string.rateUsSecondDialog_title))
          .setMessage(
            getResources().getString(R.string.rateUsSecondDialog_text))
          .setPositiveButton(getResources().getString(R.string.rateUsDialog_positiveBtn), 
              new DialogInterface.OnClickListener() {            
                public void onClick(DialogInterface dialog, int id) {                                            
                  Intent intent = new Intent(Intent.ACTION_VIEW, BuzzWordsApplication.storeURI_Buzzwords);
                  startActivity(intent);
                  muteRateReminder();
                }
          }).setNegativeButton(getResources().getString(R.string.rateUsDialog_negativeBtn), 
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    muteRateReminder();
            }
          });
      dialog = builder.create();
      break;
    case DIALOG_FIRST_TIME:
      builder = new AlertDialog.Builder(this);
      builder
          .setTitle(
              getResources().getString(R.string.openingDialog_title))
          .setMessage(
            getResources().getString(R.string.openingDialog_text))
          .setPositiveButton(getResources().getString(R.string.openingDialog_positiveBtn), 
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                  mContinueMusic = true;
                  // play confirm sound
                  SoundManager sm = SoundManager.getInstance(TitleActivity.this.getBaseContext());
                  sm.playSound(SoundManager.Sound.CONFIRM);
                  muteFirstRunDialog();
                  startActivity(new Intent(
                      getApplication().getString(R.string.IntentRules), getIntent()
                          .getData()));
                }
          }).setNegativeButton(getResources().getString(R.string.openingDialog_negativeBtn), 
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    // play confirm sound
                    SoundManager sm = SoundManager.getInstance(TitleActivity.this.getBaseContext());
                    sm.playSound(SoundManager.Sound.CONFIRM);
                    muteFirstRunDialog();
            }
          });
      dialog = builder.create();
      break;
    default:
      dialog = null;
    }
    return dialog;
  }
  
}
