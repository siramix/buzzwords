/**
 * 
 */
package com.buzzwords;

import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView.OnEditorActionListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @author The Buzzwords Team
 *
 */
public class TeamSelectLayout extends RelativeLayout {

  private Context mContext;
  
  /*
   * Elements contained in this Layout
   */
  private LinearLayout mButtons;
  private FrameLayout mFrame;
  private EditText mTeamText;
  private ImageView mEditIcon;
  private View mButtonAddTeam;
  private View mButtonEditTeamName;
  
  private Team mTeam;
  private LinkedList<Team> mTeamList;
  
  private String mPrefKey;
  private SharedPreferences.Editor mPreferences;
  
  private static String TAG = "TeamSelectLayout";
  
  /**
   * @param context
   */
  public TeamSelectLayout(Context context) {
    super(context);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param context
   * @param attrs
   */
  public TeamSelectLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    // TODO Auto-generated constructor stub
    
    mContext = context;
  }

  /**
   * @param context
   * @param attrs
   * @param defStyle
   */
  public TeamSelectLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void onFinishInflate()
  {
    super.onFinishInflate();
    
    // Store off density in order to convert to pixels
    final float DENSITY = this.getResources().getDisplayMetrics().density;
    // Create the views

    // Initialize the group for the frame
    mFrame = new FrameLayout(mContext);
    mFrame.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    int padding = (int) (DENSITY * 1 + 0.5f);
    mFrame.setPadding(0, padding, 0, padding);
    mFrame.setBackgroundColor(R.color.black);
    
    // Initialize EditText foreground in frame
    mTeamText = new EditText(mContext);
    mTeamText.setText("Red team!");
    mTeamText.setBackgroundColor(this.getResources().getColor(R.color.genericBG));
    mTeamText.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    mTeamText.setCursorVisible(false);
    mTeamText.setFocusable(true);
    mTeamText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    mTeamText.setIncludeFontPadding(false);
    mTeamText.setInputType(InputType.TYPE_CLASS_TEXT);
    mTeamText.setPadding( (int)(DENSITY * 15 + 0.5f), 0, 0, 0);
    mTeamText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);
    mTeamText.setOnEditorActionListener(mEditTextListener);
    
    // Intialize EditIcon in frame
    mEditIcon = new ImageView(mContext);
    mEditIcon.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.gamesetup_editicon));
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int)(DENSITY * 25 + 0.5f), (int)(DENSITY * 25 + 0.5f));
    params.gravity = (Gravity.CENTER_VERTICAL | Gravity.RIGHT);
    params.rightMargin = (int)(DENSITY * 5 + 0.5f);
    mEditIcon.setLayoutParams(params);
    
    // Add the views to frame
    mFrame.addView(mTeamText);
    mFrame.addView(mEditIcon);
    

    // Initialize the group for the buttons
    mButtons = new LinearLayout(mContext);
    mButtons.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    mButtons.setOrientation(LinearLayout.HORIZONTAL);
    
    // Initialize Add / Remove team button
    mButtonAddTeam = new View(mContext);
    mButtonAddTeam.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f));
    mButtonAddTeam.setOnClickListener(mAddTeamListener);

    // Initialize EditTeamName button
    mButtonEditTeamName = new View(mContext);
    mButtonEditTeamName.setLayoutParams(new LayoutParams((int)(DENSITY * 40 + 0.5f), LayoutParams.FILL_PARENT));
    mButtonEditTeamName.setOnClickListener(mEditTeamNameListener);
    
    // Add Buttons to button layout
    mButtons.addView(mButtonAddTeam);
    mButtons.addView(mButtonEditTeamName);
    
    
    // Add groups to TeamSelectLayout
    this.addView(mFrame);
    this.addView(mButtons);
  }
  

  /**
   * Set the teams and team list this TeamSelectLayout is associated with
 * @param team The team this Layout represents
 * @param teamList The list of teams this layout affects
 */
public void assignTeam(Team team, LinkedList<Team> teamList, SharedPreferences.Editor prefs, String key)
  {
	  mTeam = team;
	  mTeamList = teamList;
	  mTeamText.setText(team.getName());
	  mPrefKey = key;
	  mPreferences = prefs;
  }

public void setTeamLayoutActiveness(boolean active)
{
	if( active)
	{
	    mTeamText.setBackgroundResource(mTeam.getPrimaryColor());
	    mTeamText.setTextColor(this.getResources().getColor(mTeam.getSecondaryColor()));
	
	    // Show edit team name icon when team is removed
	    mEditIcon.setVisibility(View.VISIBLE);
	    // Enable edit team name button with the icon
	    mButtonEditTeamName.setVisibility(View.VISIBLE);
	}
	else
	{
	    mTeamText.setBackgroundResource(R.color.inactiveButton);
	    mTeamText.setTextColor(this.getResources().getColor(R.color.genericBG));
	
	    // Hide team name edit icon when team is removed
	    mEditIcon.setVisibility(View.INVISIBLE);
	    // Disable edit team name button with the icon
	    mButtonEditTeamName.setVisibility(View.GONE);
	}
}

  /**
   * Watches the button that adds the first team to the list
   */
  private final OnClickListener mAddTeamListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "AddTeamListener onClick()");
      }
      
      SoundManager sm = SoundManager.getInstance(mContext);
      // Attempt to remove the team from the list
      if (mTeamList.remove(mTeam)) {    
        setTeamLayoutActiveness(false);
        // Store off this selection so it is remember between activities
        mPreferences.putBoolean(mPrefKey, false);
        // Play back sound on remove
        sm.playSound(SoundManager.Sound.BACK);
      } else {
        // Add the team to the list
        mTeamList.add(mTeam);  	  
        setTeamLayoutActiveness(true);
        // Store off this selection so it is remember between activities
    	mPreferences.putBoolean(mPrefKey, true);
        // Play confirm sound on add
        sm.playSound(SoundManager.Sound.CONFIRM);
      }
    }
  };
  
  /**
   * Edit Team Name button Listener
   */
  private final OnClickListener mEditTeamNameListener = new OnClickListener() {
    public void onClick(View v) {
      if (BuzzWordsApplication.DEBUG) {
        Log.d(TAG, "mEditTeamName onClick()");
      }
      mTeamText.setCursorVisible(true);
      mTeamText.requestFocus();
      
      mFrame.setBackgroundColor(TeamSelectLayout.this.getResources().getColor(
          R.color.white));

      mButtonAddTeam.setVisibility(View.INVISIBLE);

      // Show the keyboard
      InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(mTeamText, 0);
    }
  };

/*
 * Watches the EditText view for keyboard input and changes rendering on
 * the ui elements accordingly
 */
  private OnEditorActionListener mEditTextListener = new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      if (actionId == EditorInfo.IME_ACTION_DONE) {

        v.setCursorVisible(false);
        v.setBackgroundResource(mTeam.getPrimaryColor());
        mFrame.setBackgroundColor(TeamSelectLayout.this.getResources().getColor(
            R.color.black));

        mButtonAddTeam.setVisibility(View.VISIBLE);
      }
      return false;
    }
  };
}
