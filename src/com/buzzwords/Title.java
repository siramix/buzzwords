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
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * This is the activity class that kicks off BuzzWords
 * 
 * @author The BuzzWords Team
 */
public class Title extends Activity
{
  /**
   * logging tag
   */
  public static String TAG = "Title";
  
  /**
   * flag used for stopping music OnStop() event.
   */
  private boolean musicHandled;

  /**
   * PlayGameListener is used for the start game button.  It launches the next 
   * activity.
   */
   private View.OnTouchListener TouchPlayListener = new View.OnTouchListener() 
   {

      public boolean onTouch(View v, MotionEvent event) {
        // return out if views are not enabled
        if ( !v.isEnabled() )
        {
          return true;
        }
     // Make this seem like a button OnClick for both the label and the button
        int action = event.getAction();
        if ( action == MotionEvent.ACTION_DOWN )
        {
          highlightDelegateItems(v.getId(), true);
        }
        else if ( action == MotionEvent.ACTION_MOVE )
        {
          // Check if the move happened in the bounds of this view
          Rect bounds = new Rect();
          v.getHitRect(bounds);
          // If in bounds, we have to re-highlight in case we went out of bounds previously
          if ( bounds.contains( (int) event.getX(), (int) event.getY()) )
          {
            highlightDelegateItems(v.getId(), true);
          }
          else
          {
            highlightDelegateItems(v.getId(), false);
          }
        }
        else
        {
          highlightDelegateItems(v.getId(), false);
        }
        
        return false;
      }
   };
   
   /**
    * Helper function to highlight a view given its Id
    * @param id
    */
   private void highlightDelegateItems( int id, boolean On )
   {
     ImageButton button = (ImageButton) Title.this.findViewById( R.id.Title_PlayButton );;
     TextView label = (TextView) Title.this.findViewById( R.id.Title_PlayText);;
     switch(id)
     {
     case R.id.Title_PlayDelegate:
       button = (ImageButton) Title.this.findViewById( R.id.Title_PlayButton );
       label = (TextView) Title.this.findViewById( R.id.Title_PlayText);
       if( On)
       {
         button.setBackgroundResource(R.drawable.title_play_onclick);
         label.setTextColor( Title.this.getResources().getColor(R.color.teamB_highlight ));
       }
       else
       {
         button.setBackgroundResource(R.drawable.title_play);
         label.setTextColor( Title.this.getResources().getColor(R.color.teamB_text ));
       }
       break;
     case R.id.Title_BuzzDelegate:
       button = (ImageButton) Title.this.findViewById( R.id.Title_BuzzButton );
       label = (TextView) Title.this.findViewById( R.id.Title_BuzzText);
       if( On)
       {
         button.setBackgroundResource(R.drawable.title_buzzer_onclick);
         label.setTextColor( Title.this.getResources().getColor(R.color.teamC_highlight ));
       }
       else
       {
         button.setBackgroundResource(R.drawable.title_buzzer);
         label.setTextColor( Title.this.getResources().getColor(R.color.teamC_text ));
       }
       break;
     case R.id.Title_SettingsDelegate:
       button = (ImageButton) Title.this.findViewById( R.id.Title_SettingsButton );
       label = (TextView) Title.this.findViewById( R.id.Title_SettingsText);
       if( On)
       {
         button.setBackgroundResource(R.drawable.title_settings_onclick);
         label.setTextColor( Title.this.getResources().getColor(R.color.teamD_highlight ));
       }
       else
       {
         button.setBackgroundResource(R.drawable.title_settings);
         label.setTextColor( Title.this.getResources().getColor(R.color.teamD_text ));
       }
       break;
     case R.id.Title_RulesDelegate:
       button = (ImageButton) Title.this.findViewById( R.id.Title_RulesButton );
       label = (TextView) Title.this.findViewById( R.id.Title_RulesText);
       if( On)
       {
         button.setBackgroundResource(R.drawable.title_rules_onclick);
         label.setTextColor( Title.this.getResources().getColor(R.color.teamA_highlight ));
       }
       else
       {
         button.setBackgroundResource(R.drawable.title_rules);
         label.setTextColor( Title.this.getResources().getColor(R.color.teamA_text ));
       }
       break;
     } 
     if( On)
     {
       label.setTextSize( 45);
     }
     else
     {
       label.setTextSize( 42);
     }
   }

   /**
   * PlayGameListener plays an animation on the view that will result in launching GameSetup
   */
   private OnClickListener PlayGameListener = new OnClickListener() 
   {
       public void onClick(View v) 
       {
         Log.d( TAG, "PlayGameListener OnClick()" );
         musicHandled = true;      
         
         // play confirm sound
         BuzzWordsApplication application = (BuzzWordsApplication) Title.this.getApplication();
         SoundManager sound = application.GetSoundManager();
         sound.PlaySound( SoundManager.SOUND_CONFIRM );
         
         startActivity(new Intent(Title.this.getApplication().getString( R.string.IntentGameSetup),
                                  getIntent().getData()));
       }
   };

   /**
   * BuzzerListener plays an animation on the view that will result in launching Buzz Mode
   */
   private OnClickListener BuzzerListener = new OnClickListener() 
   {
       public void onClick(View v) 
       {
         Log.d( TAG, "PlayGameListener OnClick()" );
         musicHandled = false;
         
         // play confirm sound
         BuzzWordsApplication application = (BuzzWordsApplication) Title.this.getApplication();
         SoundManager sound = application.GetSoundManager();
         sound.PlaySound( SoundManager.SOUND_CONFIRM );
         
         startActivity(new Intent(getApplication().getString( R.string.IntentBuzzer ), 
             getIntent().getData()));
       }
   };
   
  /**
   * Listener to determine when the settings button is clicked.  Includes an onClick function
   * that starts the settingsActivity.
   */
  private OnClickListener SettingsListener = new OnClickListener()
  {
  	public void onClick(View v) 
  	{
      Log.d( TAG, "SettingsListener OnClick()" );
      musicHandled = true;
      
      // play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) Title.this.getApplication();
      SoundManager sound = application.GetSoundManager();
      sound.PlaySound( SoundManager.SOUND_CONFIRM );
      
  	  startActivity(new Intent(Title.this.getApplication().getString( R.string.IntentSettings ), 
  	        getIntent().getData()));
  	}
  }; //End SettingsListener
  
  /**
   * Listener to determine when the Rules button is clicked on the title screen.  Includes
   * an onClick method that will start the Rule activity.
   */
  private OnClickListener RulesListener = new OnClickListener() 
  {
    public void onClick(View v) 
    {
      Log.d( TAG, "RulesListener OnClick()" );
      musicHandled = true;
      
      // play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) Title.this.getApplication();
      SoundManager sound = application.GetSoundManager();
      sound.PlaySound( SoundManager.SOUND_CONFIRM );
      
      startActivity(new Intent(getApplication().getString( R.string.IntentRules ), 
          getIntent().getData()));
      
    }
  }; // End RulesListener

  /**
   * Listener to determine when the About Us button is clicked on the title screen.  Includes
   * an onClick method that will start the Rule activity.
   */
  private OnClickListener AboutUsListener = new OnClickListener() 
  {
    public void onClick(View v) 
    {
      Log.d( TAG, "AboutUsListener OnClick()" );
      musicHandled = false;
      
      // play confirm sound
      BuzzWordsApplication application = (BuzzWordsApplication) Title.this.getApplication();
      SoundManager sound = application.GetSoundManager();
      sound.PlaySound( SoundManager.SOUND_CONFIRM );
      
      Uri uri = Uri.parse("http://www.rockandrowe.com/");  
      Intent intent=new Intent(Intent.ACTION_VIEW,uri);
      startActivity(intent);

    }
  }; // End AboutUsListener
  
  /**
   * @return The animation that brings in the buttons
   * screen
   */
  private Animation TranslateButtons(int buttonNum)
  {
    Log.d( TAG, "TranslateButtons()" );
    
    final int MOVETIME = 600;
    
    // Slide in from off-screen
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
        Animation.ABSOLUTE,  ( -600f ), Animation.RELATIVE_TO_PARENT,   0.0f );
    slideIn.setDuration(MOVETIME + ( 200 * buttonNum) );
    slideIn.setInterpolator(new DecelerateInterpolator());
    return slideIn;
  }

  /**
   * @return The animation that brings in labels
   * screen
   */
  private AnimationSet TranslateLabels(int labelNum)
  {
    Log.d( TAG, "TranslateLabels()" );

    final int MOVETIME = 800;
    AnimationSet set = new AnimationSet(true);

    // Define the translate animation
    TranslateAnimation slideIn = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  ( -1.0f * labelNum ), Animation.RELATIVE_TO_PARENT,  0.0f,
        Animation.RELATIVE_TO_PARENT,  ( 0.0f ), Animation.RELATIVE_TO_PARENT,   0.0f );
    slideIn.setDuration( MOVETIME );
    slideIn.setInterpolator(new DecelerateInterpolator());
    slideIn.setStartOffset( 300 * (labelNum+1) );
  
    // Create entire sequence
    set.addAnimation(slideIn);
    return set;
  }

/**
* onCreate - initializes a welcome screen that starts the game.
*/
@Override
public void onCreate( Bundle savedInstanceState )
{
	super.onCreate( savedInstanceState );
  Log.d( TAG, "onCreate()" );               
  
  //Force volume controls to affect Media volume
  setVolumeControlStream(AudioManager.STREAM_MUSIC);
  
  BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
  MediaPlayer mp = application.CreateMusicPlayer(this.getBaseContext(), R.raw.mus_title);
  application.CreateSoundManager( this.getBaseContext() );
  
  mp.setLooping(true);
  SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
  if( sp.getBoolean("music_enabled", true))
  {
    mp.start();
  }
  
  musicHandled = false;
  
	// Setup the Main Title Screen view
	this.setContentView(R.layout.title );

  // Assign listeners to the delegate buttons
  View delegate = (View) this.findViewById( R.id.Title_PlayDelegate);
  delegate.setOnTouchListener( TouchPlayListener );
  delegate.setOnClickListener( PlayGameListener);
  
  delegate = (View) this.findViewById( R.id.Title_BuzzDelegate );
  delegate.setOnTouchListener( TouchPlayListener );
  delegate.setOnClickListener( BuzzerListener);

  delegate = (View) this.findViewById( R.id.Title_SettingsDelegate );
  delegate.setOnTouchListener( TouchPlayListener );
  delegate.setOnClickListener( SettingsListener);
  
  delegate = (View) this.findViewById( R.id.Title_RulesDelegate );
  delegate.setOnTouchListener( TouchPlayListener );
  delegate.setOnClickListener( RulesListener);
  
  
  ImageButton rulesButton = (ImageButton) this.findViewById( R.id.Title_RulesButton );
  rulesButton.setOnClickListener( RulesListener );
  
  ImageButton buzzerButton = (ImageButton) this.findViewById( R.id.Title_BuzzButton );
  buzzerButton.setOnClickListener( BuzzerListener );
  
  ImageButton aboutusButton = (ImageButton) this.findViewById( R.id.Title_AboutUs );
  aboutusButton.setOnClickListener( AboutUsListener );
  
  View button = (View) this.findViewById( R.id.Title_PlayButton);
  button.startAnimation(this.TranslateButtons(4));
  button = (View) this.findViewById( R.id.Title_BuzzButton);
  button.startAnimation(this.TranslateButtons(3));
  button = (View) this.findViewById( R.id.Title_SettingsButton);
  button.startAnimation(this.TranslateButtons(2));
  button = (View) this.findViewById( R.id.Title_RulesButton);
  button.startAnimation(this.TranslateButtons(1));
  
  // set font
  Typeface antonFont = Typeface.createFromAsset(getAssets(), "fonts/Anton.ttf");

  TextView label = (TextView) this.findViewById( R.id.Title_PlayText);
  label.startAnimation(this.TranslateLabels(4));
  label.setTypeface(antonFont);
  label = (TextView) this.findViewById( R.id.Title_BuzzText);
  label.startAnimation(this.TranslateLabels(3));
  label.setTypeface(antonFont);
  label = (TextView) this.findViewById( R.id.Title_SettingsText);
  label.startAnimation(this.TranslateLabels(2));
  label.setTypeface(antonFont);
  label = (TextView) this.findViewById( R.id.Title_RulesText);
  label.startAnimation(this.TranslateLabels(1));
  label.setTypeface(antonFont);
  
}

/**
 * Override onPause to prevent activity specific processes from running while app is in background
 */
@Override
public void onPause()
{
   Log.d( TAG, "onPause()" );   
   super.onPause();
   if( !musicHandled )
   {
     BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
     MediaPlayer mp = application.GetMusicPlayer();
     mp.pause();
   }
}

/**
 * Override OnResume to resume activity specific processes
 */
@Override
public void onResume()
{
   Log.d( TAG, "onResume()" );   
   super.onResume();
   
   // Resume Title Music
   BuzzWordsApplication application = (BuzzWordsApplication) this.getApplication();
   MediaPlayer mp = application.GetMusicPlayer();
   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
   if( sp.getBoolean("music_enabled", true))
   {
     mp.start();
   }
   // set flag to let onStop handle music
   musicHandled = false;
}

}
