/**
 * 
 */
package com.buzzwords;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @author The Buzzwords Team
 *
 */
public class TeamSelectLayout extends RelativeLayout {

  private Context mContext;
  
  private LinearLayout mButtons;
  private FrameLayout mFrame;
  private EditText mTeamText;
  private ImageView mEditIcon;
  private View mButtonAddTeam;
  private View mButtonEditTeamName;
  
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
    /*
    // Store off density in order to convert to pixels
    final float DENSITY = this.getResources().getDisplayMetrics().density;
    // Create the views

    // Initialize the group for the buttons
    mButtons = new LinearLayout(mContext);
    LinearLayout.LayoutParams linearparams = (android.widget.LinearLayout.LayoutParams) mButtons.getLayoutParams();
    linearparams.height = LinearLayout.LayoutParams.FILL_PARENT;
    linearparams.width = LinearLayout.LayoutParams.FILL_PARENT;
    mButtons.setLayoutParams(linearparams);
    
    // Initialize the group for the frame
    mFrame = new FrameLayout(mContext);
    FrameLayout.LayoutParams frameparams =
      (android.widget.FrameLayout.LayoutParams) mFrame.getLayoutParams();
    frameparams.height = LinearLayout.LayoutParams.FILL_PARENT;
    frameparams.width = LinearLayout.LayoutParams.FILL_PARENT;
    mFrame.setLayoutParams(frameparams);
    int padding = (int) (DENSITY * 1 + 0.5f);
    mFrame.setPadding(0, padding, padding, 0);
    mFrame.setBackgroundColor(R.color.black);
    */
    
    mTeamText = new EditText(mContext);
    mTeamText.setText("Red team!");
    mTeamText.setBackgroundColor(this.getResources().getColor(R.color.white));
    mTeamText.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    this.addView(mTeamText);
    
    //mEditIcon = new ImageView(mContext);
    //mButtonAddTeam = new View(mContext);
    //mButtonEditTeamName = new View(mContext);
    
  }
}
