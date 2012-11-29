/*****************************************************************************
 *  PhraseCraze is a family friendly word game for mobile phones.
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

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;

/**
 * Custom view that represents a pack.
 * 
 * @author The Buzzwords Team
 * 
 */
public class PackPurchaseRowLayout extends FrameLayout {

  protected static final String TAG = "PackPurchaseRowLayout";

  private Context mContext;

  // View members
  private RelativeLayout mContents;
  private ImageView mIcon;
  private TextView mTitle;
  private TextView mPrice;
  private ImageView mRowEndBG;
  private ImageButton mInfoButton;

  // Data members
  private Pack mPack;
  private boolean mIsPackEnabled;
  private boolean mIsRowOdd;
  private boolean mIsPackPurchased;

  /*
   * Listeners for click events on this row
   */
  private OnPackSelectedListener mPackSelectedListener;
  private OnPackInfoRequestedListener mPackInfoListener;
  // Allow users to disable the Selection listener
  private boolean mIsRowClickable;

  /**
   * @param context
   */
  public PackPurchaseRowLayout(Context context) {
    super(context);
    initializeMembers(context);
  }

  /**
   * @param context
   * @param attrs
   */
  public PackPurchaseRowLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initializeMembers(context);
  }

  /**
   * @param context
   * @param attrs
   * @param defStyle
   */
  public PackPurchaseRowLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initializeMembers(context);
  }

  private void initializeMembers(Context context) {
    int id = 0;
    mContext = context;
    mContents = new RelativeLayout(mContext);
    mIcon = new ImageView(mContext);
    mIcon.setId(++id);
    mTitle = new TextView(mContext);
    mTitle.setId(++id);
    mPrice = new TextView(mContext);
    mPrice.setId(++id);
    mRowEndBG = new ImageView(mContext);
    mRowEndBG.setId(++id);
    mInfoButton = new ImageButton(mContext);
    mInfoButton.setId(++id);
    mIsRowClickable = true;
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();

    // Store off density in order to convert to pixels
    final float DENSITY = this.getResources().getDisplayMetrics().density;

    // Create the views

    // Initialize the group for the frame
    this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.WRAP_CONTENT));
    int padding = (int) (DENSITY * 1 + 0.5f);
    this.setPadding(0, padding, 0, padding);
    this.setBackgroundColor(getResources().getColor(R.color.black));

    // Initialize Layout that stores the contents
    mContents.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.WRAP_CONTENT));
    mContents.setBackgroundColor(this.getResources().getColor(
        R.color.gameend_blankrow));

    // Add a placeholder for the icon
    mIcon.setImageDrawable(this.getResources().getDrawable(
        R.drawable.placholder_pack_icon));
    mIcon.setVisibility(View.INVISIBLE);
    RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
    iconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    mIcon.setLayoutParams(iconParams);
    int iconPadding = (int) (DENSITY * 8 + 0.5f);
    mIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);

    // Initialize Pack Title
    mTitle.setText("Generic Pack Title");
    RelativeLayout.LayoutParams titleTextParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    titleTextParams.addRule(RelativeLayout.RIGHT_OF, mIcon.getId());
    titleTextParams.addRule(RelativeLayout.CENTER_VERTICAL);
    mTitle.setLayoutParams(titleTextParams);
    mTitle.setPadding((int) (DENSITY * 5 + 0.5f), 0, 0, 0);
    mTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
    mTitle.setWidth((int) (DENSITY * 190 + 0.5f));
    mTitle.setEllipsize(TruncateAt.END);
    mTitle.setHorizontallyScrolling(true);
    mTitle.setTextColor(this.getResources().getColor(R.color.text_default));

    // Initialize End Group and add contents
    mRowEndBG.setImageResource(R.drawable.turnsum_row_end_white);
    RelativeLayout.LayoutParams rowEndParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    rowEndParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    mRowEndBG.setLayoutParams(rowEndParams);
    mRowEndBG
    .setColorFilter(this.getResources()
        .getColor(R.color.genericBG_trim), Mode.MULTIPLY);

    // Initialize Price
    mPrice.setText("");
    RelativeLayout.LayoutParams priceParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    priceParams.addRule(RelativeLayout.ALIGN_RIGHT, mRowEndBG.getId());
    priceParams.addRule(RelativeLayout.CENTER_VERTICAL);
    priceParams.rightMargin = (int) (DENSITY * 6 + 0.5f);
    mPrice.setLayoutParams(priceParams);
    mPrice.setIncludeFontPadding(false);
    mPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
    mPrice.setTextColor(this.getResources().getColor(R.color.text_default));

    // Initialize Price
    RelativeLayout.LayoutParams infoButtonParams = new RelativeLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    infoButtonParams.addRule(RelativeLayout.ALIGN_RIGHT, mRowEndBG.getId());
    infoButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
    infoButtonParams.rightMargin = (int) (DENSITY * 8 + 0.5f);
    mInfoButton.setLayoutParams(infoButtonParams); // Initialize End Group and
                                                   // add contents
    mInfoButton.setBackgroundResource(R.drawable.button_info);

    // Set fonts - Wrap in isInEditMode so as not to break previewer
    if (!this.isInEditMode()) {
      Typeface antonFont = Typeface.createFromAsset(mContext.getAssets(),
          "fonts/Anton.ttf");
      mPrice.setTypeface(antonFont);
      mTitle.setTypeface(antonFont);
    }

    // Add views to the contents layout
    mContents.addView(mIcon);
    mContents.addView(mTitle);
    mContents.addView(mRowEndBG);
    mContents.addView(mPrice);
    mContents.addView(mInfoButton);

    // Add the views to frame
    this.addView(mContents);
    
    // Disable clicking sounds on the view.
    mContents.setSoundEffectsEnabled(false);

    // Add single pixel bar of lightened color to give depth
    View lightBar = new View(mContext);
    lightBar.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        (int) (DENSITY * 1 + 0.5f)));
    lightBar.setBackgroundColor(getResources().getColor(R.color.white));
    AlphaAnimation alpha = new AlphaAnimation(0.2f, 0.2f);
    alpha.setFillAfter(true);
    lightBar.startAnimation(alpha);
    this.addView(lightBar);

    // Enable clicking on the row by default
    setRowClickable(true);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int bgColor;

    if (mIsRowClickable && mIsPackPurchased) {
      // Render a purchased pack that is not in PackInfo
      if (mIsPackEnabled) {
        // Purchased pack is chosen
        setViewAttributes(
            getResources().getColor(R.color.packPurchaseSelected), 0,
            getResources().getColor(R.color.white),
            R.drawable.button_info_blue, false);
      } else {
        // Purchased pack is not chosen
        setViewAttributes(
            getResources().getColor(R.color.packPurchaseUnSelected),
            getResources().getColor(R.color.genericBG_trimDark), getResources()
                .getColor(R.color.genericBG_trim), R.drawable.button_info,
            false);
      }
    } else {
      // Render a generic PackInfo row or a Purchaseable row
      if (mIsPackPurchased) {
        bgColor = R.color.packPurchaseSelected;
      }
      else if (mIsRowOdd) {
        bgColor = R.color.genericBG_trim;
      } else {
        bgColor = R.color.genericBG_trimDark;
      }
      setViewAttributes(getResources().getColor(bgColor), 0, getResources()
          .getColor(R.color.white), 0, !mIsPackPurchased);
    }
  }

  /**
   * Sets the visuals of the view according to the specified parameters.
   * @param backgroundColor The color resource for the background.
   * @param iconFilterColor The color resource to multiply with the icon. Specify 0 to remove the filter.
   * @param titleTextColor The color resource for the Title text.
   * @param infoIconDrawableId The DrawableId for the info Icon. Specify 0 to hide the info icon.
   * @param isPriceVisible False to hide the price and price background. True otherwise.
   */
  private void setViewAttributes(int backgroundColor, int iconFilterColor, int titleTextColor, int infoIconDrawableId, boolean isPriceVisible)
  {
    // Set background
    mContents.setBackgroundColor(backgroundColor);
    
    // Set icon color
    if (iconFilterColor != 0) {
      mIcon.setColorFilter(iconFilterColor, Mode.MULTIPLY);
    } else {
      mIcon.setColorFilter(null);
    }
    
    // Set Title text color
    mTitle.setTextColor(titleTextColor);

    // Set info button drawable
    mInfoButton.setBackgroundResource(infoIconDrawableId); 
    
    // Set price visibility
    int priceVisibility = isPriceVisible ? View.VISIBLE : View.INVISIBLE;
    mPrice.setVisibility(priceVisibility);
    mRowEndBG.setVisibility(priceVisibility);
    
    // Set info button visibility
    int infoButtonVisibility = infoIconDrawableId != 0 ? View.VISIBLE : View.INVISIBLE;   
    mInfoButton.setVisibility(infoButtonVisibility);
  }

  /**
   * Set the pack this Layout is associated with
   * 
   * @param pack
   *          The pack this Layout represents
   * @param isSelected
   *          Specify whether the pack is selected for the game
   * @throws IOException 
   */
  public void setPack(Pack pack, Boolean isSelected, Boolean isRowOdd) {
    // Setup new members
    mPack = pack;
    mIsPackEnabled = isSelected;
    mIsRowOdd = isRowOdd;
    mIsPackPurchased = mPack.isInstalled();

    // Set new pack attributes
    mTitle.setText(mPack.getName());
    retrieveAndSetPackIcon(pack);

    mPrice.setText(pack.getPrice());
    
    // Assign click listeners based on the pack's purchase state
    if (mIsPackPurchased) {
      mContents.setOnClickListener(mSelectPackListener);
      mInfoButton.setOnClickListener(mPackInfoRequestedListener);
    } else {
      mContents.setOnClickListener(mPackInfoRequestedListener);
      mInfoButton.setOnClickListener(null);
    }
    

    invalidate();
  }

  /**
   * Set the status of a layout while keeping pack the same
   * 
   * @param isSelected
   *          Specify whether the pack is selected for the game
   */
  public void setPackStatus(Boolean isSelected) {
    mIsPackEnabled = isSelected;
    invalidate();
  }

  /*
   * Assign a listener to receive the OnPackSelected callback
   * 
   * @param listener Listener to receive the callback
   */
  public void setOnPackSelectedListener(OnPackSelectedListener listener) {
    mPackSelectedListener = listener;
  }

  /*
   * Assign a listener to receive the OnPackInfoRequested callback
   * 
   * @param listener Listener to receive the callback
   */
  public void setOnPackInfoRequestedListener(
      OnPackInfoRequestedListener listener) {
    mPackInfoListener = listener;
  }

  /**
   * First searches for the pack icon in resources, then checks if it is cached,
   * then if neither is doable, looks online for the pack icon and caches it. If
   * none of these work, it will leave it set as the default icon.
   * @param pack that needs an icon
   * @return the icon drawable
   */
  private void retrieveAndSetPackIcon(Pack pack) {
    // First check cache, then hit server
    if (PackIconUtils.isPackIconCached(pack.getIconName(), mContext)) {
      setPackIcon(PackIconUtils.getCachedIcon(pack.getIconName(), mContext));
    } else {
      PackClient.fetchIconOnThread(pack, this, mContext);
    }
  }

  /**
   * Sets the pack icon for the pack row and shows it (default is invisible)
   * @param icon - image to display for the icon view
   */
  public void setPackIcon(Bitmap icon) {
    if (icon != null) {
      mIcon.setImageBitmap(icon);
      mIcon.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Get the pack that is associated with this layout
   */
  public Pack getPack() {
    return mPack;
  }

  /**
   * Get whether or not this row is considered "odd"
   */
  public Boolean isRowOdd() {
    return mIsRowOdd;
  }

  /*
   * Set this row as unselectable, when the invoking classes don't need
   * selection events
   */
  public void setRowClickable(boolean isClickable) {
    mIsRowClickable = isClickable;
    mContents.setClickable(mIsRowClickable);
    mInfoButton.setClickable(mIsRowClickable);
    invalidate();
  }

  /**
   * Watches the group that selects and deselects the pack on click
   */
  private final OnClickListener mSelectPackListener = new OnClickListener() {
    public void onClick(View v) {
      Boolean newSelectionStatus = !mIsPackEnabled;
      setPackStatus(newSelectionStatus);

      // Send event to any listeners
      if (mPackSelectedListener != null) {
        mPackSelectedListener.onPackSelected(mPack, newSelectionStatus);
      }
    }
  };

  /**
   * Watches the group that shows pack Info
   */
  private final OnClickListener mPackInfoRequestedListener = new OnClickListener() {
    public void onClick(View v) {
      // Send event to any listeners
      if (mPackInfoListener != null) {
        mPackInfoListener.onPackInfoRequested(mPack);
      }
    }
  };

}
