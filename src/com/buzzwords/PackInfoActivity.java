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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * This handles changing the points scored for each team in a round
 * 
 * @author Siramix Labs
 */
public class PackInfoActivity extends Activity {

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "PackInfoActivity";

  /**
   * flag used for stopping music OnStop() event.
   */
  private boolean mContinueMusic = false;
  
  /*
   * References to button views
   */
  private Button mButtonCancel;
  private Button mButtonAccept;
  
  /**
   * References to elements that render pack data
   */
  private PackPurchaseRowLayout mPackTitle;
  private TextView mPackDescription;
  private TextView mPackIsOwnedText;
  private ProgressBarView mProgressBar;
  private AntonTextView mCardsInPack;
  
  /*
   * Reference to the pack this activity is displaying
   */
  private Pack mPack;
  private boolean mIsPackSelected;
  private boolean mIsPackRowOdd;
  private boolean mIsPackPurchased;
  private int mPurchaseType;

  /**
   * Set the references to the elements from the layout file
   */
  private void setupViewReferences() {
    mButtonCancel = (Button) this
        .findViewById(R.id.PackInfo_Buttons_Cancel);
    mButtonAccept = (Button) this
        .findViewById(R.id.PackInfo_Buttons_Accept);
    mPackDescription = (TextView) this.findViewById(R.id.PackInfo_Description);
    mPackTitle = (PackPurchaseRowLayout) this.findViewById(R.id.PackInfo_TitlePackRow);
    mPackIsOwnedText = (TextView) this.findViewById(R.id.PackInfo_AlreadyOwnedText);
    mCardsInPack = (AntonTextView) this.findViewById(R.id.PackInfo_CardInPack);
    mProgressBar = (ProgressBarView) this.findViewById(R.id.PackInfo_ProgressBarView);
  }

  /**
   * Watches the button that handles returning to previous activity with no
   * changes
   */
  private final OnClickListener mCancelListener = new OnClickListener() {
    public void onClick(View v) {
      // play back sound
      SoundManager sm = SoundManager.getInstance(PackInfoActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.BACK);
      
      mContinueMusic = true;
      
      finish();
    }
  };

  /**
   * Watches the button that handles pack purchase
   */
  private final OnClickListener mAcceptListener = new OnClickListener() {
    public void onClick(View v) {
      Intent outIntent = new Intent();
      outIntent.putExtra(getString(R.string.packBundleKey), mPack);
      // Set result
      PackInfoActivity.this.setResult((Integer) v.getTag(), outIntent);
      
      // play confirm sound
      SoundManager sm = SoundManager.getInstance(PackInfoActivity.this.getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);
      
      mContinueMusic = true;
      
      finish();
    }
  };

  /**
   * Create the activity and display the card bundled in the intent.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.packinfo);
    Intent inIntent = getIntent();
    
    // Set references to passed-in data
    mPack = (Pack) inIntent.getExtras().get(
        getApplication().getString(R.string.packBundleKey));
    mIsPackSelected = inIntent.getBooleanExtra(
        getApplication().getString(R.string.packInfoIsPackSelectedBundleKey),
        false);
    mIsPackRowOdd = inIntent.getBooleanExtra(
        getApplication().getString(R.string.packInfoIsPackRowOddBundleKey),
        false);
    mIsPackPurchased = mPack.isInstalled();
    mPurchaseType = mPack.getPurchaseType();

    setupViewReferences();
    
    setupPackDataViews();
    
    setupButtons();
  }
  
  /*
   * Populate the elements with the Pack's Data
   */
  private void setupPackDataViews()
  {
    mPackTitle.setPack(mPack, mIsPackSelected, mIsPackRowOdd);
    mPackTitle.setRowClickable(false);
    mPackDescription.setText(mPack.getDescription());
  }

  /*
   * Sets the progress on the ProgressBar and shows it. Hides the Cards In Pack.
   */
  private void setProgressVisible(boolean isVisible) {
    if (isVisible) {
      mCardsInPack.setVisibility(View.GONE);
      mProgressBar.setVisibility(View.VISIBLE);
      mProgressBar.setProgress(mPack.getNumCardsSeen(), mPack.getSize());
    } else {
      mCardsInPack.setVisibility(View.VISIBLE);
      mProgressBar.setVisibility(View.GONE);
      mCardsInPack.setText(
          getResources().getString(R.string.packInfo_cardsinpack,
          Integer.toString(mPack.getServerSize())));
    }
  }
 
  /* 
   * Setup the view and buttons based on the purchasability of the pack
   */
  private void setupButtons()
  {
    
    mButtonAccept.setOnClickListener(mAcceptListener);
    
    if(!mIsPackPurchased)
    {
      // Display the buttons for a pack that has been unpurchased
      mButtonCancel.setVisibility(View.VISIBLE);
      mButtonCancel.setOnClickListener(mCancelListener);
      mPackIsOwnedText.setVisibility(View.GONE);
      
      mButtonAccept.setText(this.getResources().
          getString(PackPurchaseConsts.PURCHASE_LABEL_IDS[mPurchaseType]));
      mButtonAccept.setTag(PackPurchaseConsts.PURCHASE_RESULT_CODES[mPurchaseType]);
      // Do not show progress for unpurchased packs
      setProgressVisible(false);
    }
    else
    {
      // Display the buttons for a pack that is owned by the user
      setProgressVisible(true);
      mButtonCancel.setVisibility(View.GONE);
      mPackIsOwnedText.setVisibility(View.VISIBLE);
      mButtonAccept.setText(this.getResources().getString(R.string.packInfo_confirm_nobuy));
      mButtonAccept.setOnClickListener(mAcceptListener);
      // Set the result to return for the button
      mButtonAccept.setTag(RESULT_CANCELED);
    }
  }

  /**
   * Override back button to carry music on back to the previous activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled()) {
      mContinueMusic = true; // Flag to keep music playing
    }

    return super.onKeyUp(keyCode, event);
  }
  
  /**
   * Override onPause for music continuation
   */
  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, "onPause()");

    if (!mContinueMusic) {
      BuzzWordsApplication application = (BuzzWordsApplication) this
          .getApplication();
      MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
          .getBaseContext());
      if (mp.isPlaying() && sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
        application.cleanUpMusicPlayer();
      }
    }
  }

  /**
   * Override OnResume to resume activity specific processes
   */
  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume()");

    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    if (!mp.isPlaying() && sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
      mp.start();
    }

    mContinueMusic = false;
  }
}
