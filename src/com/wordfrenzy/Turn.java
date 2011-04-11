package com.wordfrenzy;

import com.wordfrenzy.R;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.util.Log;

/**
 * This handles a single turn consisting of cards presented to a player for a
 * limited amount of time.
 *
 * @author The WordFrenzy Team
 */
public class Turn extends Activity
{

  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "Turn";

  static final int DIALOG_PAUSED_ID = 0;
  static final int DIALOG_GAMEOVER_ID = 1;
  static final int DIALOG_READY_ID = 2;
  
  static final int TIMERANIM_PAUSE_ID = 0;
  static final int TIMERANIM_RESUME_ID = 1;
  static final int TIMERANIM_START_ID = 2;

  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;

  private View pauseOverlay;
  private ImageButton buzzerButton;
  private ImageButton nextButton;
  private ImageButton skipButton;
  private TextView countdownTxt;
  private TextView timesUpText;
  private LinearLayout pauseTextLayout;
  private ViewFlipper viewFlipper;
  
  private ImageView timerfill;
  
  private TextView cardTitle;
  private LinearLayout cardBadWords;
  private ImageView cardStatus;
  
  RelativeLayout timerGroup;
  RelativeLayout buttonGroup;
  
  private long lastCardTimerState;
  
  /**
   * Tracks the current state of the Turn as a boolean.  Set to true when time has expired and
   * activity is showing the user "Time's up!"
   */
  private boolean turnIsOver = false;
  /**
   * Track when the game has paused.  This will prevents code from executing pointlessly if already
   * paused.
   */
  private boolean isPaused = true;
  
  /**
   * This is a reference to the current game manager
   */
  private GameManager curGameManager;

  /**
   * Boolean to track which views are currently active
   */
  private boolean AIsActive;

  /**
   * vibrator object to vibrate on buzz click
   */
  private Vibrator buzzVibrator;
  
  /**
   * Boolean for representing whether we've gone back or not
   */
  private boolean isBack;
  
  /**
   * Boolean representing whether music is enabled or not.
   * Reduces calls to getprefs
   */
  private boolean musicEnabled;
  
  /**
   * Sound Manager stored as an instance variable to reduce calls to GetSoundManager
   */
  private SoundManager sound;
  
  /**
   * Unique IDs for Options menu
   */
  protected static final int MENU_ENDGAME = 0;
  protected static final int MENU_SCORE = 1;
  protected static final int MENU_RULES = 2;
  
  /**
   * Swipe Stuff
   */
  private SimpleOnGestureListener swipeListener = new SimpleOnGestureListener() {

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
    {
      // Do not let them do swipes while paused or time's up!
      if ( Turn.this.isPaused || Turn.this.turnIsOver)
      {
        return false;
      }
      if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) 
      {
        Turn.this.doSkip();
        return true;
      }
      else if(e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) 
      {
        Turn.this.doBack();
        return true;
      }
      else
      {
        return false;
      }
     
    }    
  };
  
  /**
   * Swipe Stuff
   */
  private SimpleOnGestureListener onlybackswipeListener = new SimpleOnGestureListener() {

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
    {
      // Do not let them do swipes while paused or time's up!
      if ( Turn.this.isPaused || Turn.this.turnIsOver)
      {
        return false;
      }
      if(e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) 
      {
        Turn.this.doBack();
        return true;
      }
      else
      {
        return false;
      }
     }    
  };
  
  private GestureDetector swipeDetector;

  View.OnTouchListener gestureListener;

  private PauseTimer counter;
  private PauseTimer resultsDelay;

  private void startTimer()
  {
    Log.d( TAG, "startTimer()" );

    this.counter.start();
    this.timerfill.startAnimation(TimerAnimation(Turn.TIMERANIM_START_ID));
  }

  private void stopTurnTimer()
  {
    Log.d( TAG, "stopTimer()" );
    Log.d( TAG, Long.toString( this.counter.getTimeRemaining() ) );
    if(!this.turnIsOver && this.counter.isActive())
    {
      Log.d( TAG, "Do the Pause." );
      this.counter.pause();
      this.timerfill.startAnimation(TimerAnimation(Turn.TIMERANIM_PAUSE_ID));
    }
  }

  private void resumeTurnTimer()
  {
    Log.d( TAG, "resumeTimer()" );
    Log.d( TAG, Long.toString( this.counter.getTimeRemaining() ) );
    if(!this.turnIsOver && !this.counter.isActive())
    {
      Log.d( TAG, "Do the Resume." );
      this.counter.resume();
      this.timerfill.startAnimation(TimerAnimation(Turn.TIMERANIM_RESUME_ID));
    }
  }

  /**
   *  Creates the menu items for the options menu
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    Log.d( TAG, "onCreateOptionsMenu()" );
    menu.add(0, R.string.menu_EndGame, 0, "End Game");
    menu.add(0, R.string.menu_Rules, 0, "Rules");

    return true;
  }

  /**
   * Handle various menu clicks
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    Log.d( TAG, "onOptionsItemSelected()" );
    // Handle item selection
    switch (item.getItemId())
    {
      case R.string.menu_EndGame:
        this.showDialog( DIALOG_GAMEOVER_ID );
        return true;
      case R.string.menu_Rules:
        startActivity(new Intent(getApplication().getString( R.string.IntentRules ),
            getIntent().getData()));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Listener for click on the timer to pause
   */
  private final OnClickListener TimerClickListener = new OnClickListener()
  {
    public void onClick( View v)
    {
      Log.d( TAG, "TimerClickListener OnClick()" );
      Turn.this.pauseGame();
    }
  };

  /**
   * Listener for the 'Correct' button. It deals with the flip to the next
   * card.
   */
  private final OnClickListener CorrectListener = new OnClickListener()
  {
    public void onClick(View v)
    {
      Log.d( TAG, "CorrectListener OnClick()" );
      
      Turn.this.doCorrect();
    }
  }; // End CorrectListener
  
  /**
   * Listener for the 'Correct' button. It deals with the flip to the next
   * card.
   */
  private final OnClickListener WrongListener = new OnClickListener()
  {
    public void onClick(View v)
    {
      Log.d( TAG, "WrongListener OnClick()" );
      
      Turn.this.doWrong();
    }
  }; // End WrongListener
  
  /**
   * Listener for the 'Skip' button. This deals with moving to the next card
   * via the ViewFlipper, but denotes that the card was skipped;
   */
  private final OnClickListener SkipListener = new OnClickListener()
  {
    public void onClick(View v)
    {
      Log.d( TAG, "SkipListener OnClick()" );
      
      Turn.this.doSkip();
    }
  }; // End SkipListener
  
  /**
   * Listener for the pause overlay. It unpauses the game.
   */
  private final OnClickListener PauseListener = new OnClickListener()
  {
      public void onClick(View v)
      {
        Log.d( TAG, "PauseListener OnClick()" );

        // If music is disabled, just resume the game immediately (don't wait for music to seek) 
        if ( Turn.this.turnIsOver || !musicEnabled )
        {
          // Turn is over when timer reaches 0.  At that point, we should just not resume music
          Turn.this.resumeGame();
        }
        else
        {
          // Resume must wait for music to seek back to the correct elapsed time
          WordFrenzyApplication application = (WordFrenzyApplication) Turn.this.getApplication();
          MediaPlayer mp = application.GetMusicPlayer();
          // Return to the elapsed time
          int elapsedtime = Turn.this.curGameManager.GetTurnTime() - (int) Turn.this.counter.getTimeRemaining();
          mp.seekTo( elapsedtime );
          mp.setOnSeekCompleteListener( new TurnMusicListener());
        }
        
        // Hide overlays here so that they can't report multiple OnClick'ed while music seeks
        Turn.this.pauseOverlay.setVisibility( View.INVISIBLE );
        Turn.this.pauseTextLayout.setVisibility( View.INVISIBLE);
      }
  }; // End CorrectListener
  
  /**
   * @return The animation that brings cards into view from the right of the
   * screen
   */
  private Animation InFromRightAnimation ()
  {
    Log.d( TAG, "InFromRightAnimation()" );
    Animation inFromRight = new TranslateAnimation(
		  	Animation.RELATIVE_TO_PARENT,  1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
		  	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f );
  	inFromRight.setDuration(500);
  	return inFromRight;
  }

  /**
   * @return The animation that tosses the cards from the view out into the
   * either at the left of the screen
   */
  private Animation OutToLeftAnimation ()
  {
    Log.d( TAG, "OutToLeftAnimation()" );
    Animation outToLeft = new TranslateAnimation(
		  	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
		  	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f );
    outToLeft.setDuration(500);
  	return outToLeft;
  }
  
  private Animation BackInFromLeftAnimation ()
  {
    Log.d( TAG, "BackInFromLeftAnimation()" );
    Animation outToLeft = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f );
    outToLeft.setDuration(500);
    return outToLeft;
  }
  
  private Animation BackOutToRightAnimation ()
  {
    Log.d( TAG, "BackOutToRightAnimation()" );
    Animation inFromRight = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  1.0f,
        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f );
    inFromRight.setDuration(500);
    return inFromRight;
  }
  
  /**
   * @return Animation that slides the timer off screen
   */
  private Animation ShowTimerAnim (boolean show)
  {
    float y0 = 0.0f;
    float y1 = -1.0f;
    Animation slideUp;
    if( show )
    {
      slideUp = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,  0.0f,
          Animation.RELATIVE_TO_SELF,  y1, Animation.RELATIVE_TO_SELF,   y0 );

      slideUp.setInterpolator( new DecelerateInterpolator());
    }
    else
    {
      slideUp = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,  0.0f,
          Animation.RELATIVE_TO_SELF,  y0, Animation.RELATIVE_TO_SELF,   y1 );
      
      slideUp.setInterpolator( new AccelerateInterpolator());
    }
    slideUp.setDuration( 250 );
    // make the element maintain its orientation even after the animation finishes.
    slideUp.setFillAfter(true);
    return slideUp;
  }

  /**
   * @return Animation that slides the buttons on or off screen
   */
  private Animation ShowButtonsAnim (boolean show)
  {
    float y0 = 0.0f;
    float y1 = 1.0f;
    Animation slideUp;
    if( show )
    {
      slideUp = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,  0.0f,
          Animation.RELATIVE_TO_SELF,  y1, Animation.RELATIVE_TO_SELF,   y0 );
      
      slideUp.setInterpolator( new DecelerateInterpolator());
    }
    else
    {
      slideUp = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF,  0.0f, Animation.RELATIVE_TO_SELF,  0.0f,
          Animation.RELATIVE_TO_SELF,  y0, Animation.RELATIVE_TO_SELF,   y1 );

      slideUp.setInterpolator( new AccelerateInterpolator());
    }
    slideUp.setDuration( 250 );
    
    // make the element maintain its orientation even after the animation finishes.
    slideUp.setFillAfter(true);
    return slideUp;
  }
  
  /**
   * Animation method for the timer bar that takes an integer to determine
   * whether it is starting, resuming, or stopping animation.
   * 
   * @param Accepts an integer value of 0 for Pause, 1 for Resume, and 2 for Start
   * 
   * @return The animation that scales the timer as the time depletes
   */
  private Animation TimerAnimation (int timerCommand)
  {
    Log.d( TAG, "TimerAnimation()");
    
    float percentTimeLeft = ((float) this.counter.getTimeRemaining() / this.curGameManager.GetTurnTime());
    int duration = this.curGameManager.GetTurnTime();
    
    if (timerCommand == Turn.TIMERANIM_RESUME_ID)
    {
    	duration = (int) this.counter.getTimeRemaining();
    }
    else if (timerCommand == Turn.TIMERANIM_PAUSE_ID)
    {
    	duration = Integer.MAX_VALUE;
    }
    
    ScaleAnimation scaleTimer = new ScaleAnimation(percentTimeLeft, 0.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF,
        1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
    
    scaleTimer.setDuration(duration);
    scaleTimer.setInterpolator(new LinearInterpolator());
  	return scaleTimer;
  }

  /**
   * Works with GameManager to perform the back end processing of a correct card.
   * For consistency this method was created to match the skip architecture.  Also for
   * consistency the sound for correct cards will be handled in this method.
   */
  protected void doCorrect() 
  {
    Log.d( TAG, "doCorrect()"); 
  
    ViewFlipper flipper = (ViewFlipper) findViewById( R.id.ViewFlipper0 );
    
    AIsActive = !AIsActive;   
    flipper.showNext();
    this.setCardTime();
    curGameManager.ProcessCard( Card.RIGHT );

    // Only play sound once card has been processed so we don't confuse the user
    sound.PlaySound( SoundManager.SOUND_RIGHT );  

    // Mark the card with an icon
    this.cardStatus.setBackgroundResource( Card.getCardMarkDrawableId(Card.RIGHT) );
    this.cardStatus.setVisibility( View.VISIBLE );
    
    // Show the next card
    ShowCard();    
  }
  
  /**
   * Works with GameManager to perform the back end processing of a card wrong.  Also
   * handles playing of a durative incorrect sound, as opposed to the buzzer.
   */
  protected void doWrong()
  {
    Log.d( TAG, "doWrong()");

    AIsActive = !AIsActive;
    ViewFlipper flipper = (ViewFlipper) findViewById( R.id.ViewFlipper0 );
    flipper.showNext();
    
    Turn.this.setCardTime();
    curGameManager.ProcessCard( Card.WRONG );

    // Mark the card with an icon
    this.cardStatus.setBackgroundResource( Card.getCardMarkDrawableId(Card.WRONG) );    
    this.cardStatus.setVisibility( View.VISIBLE );
    
    //Only play sound once card has been processed so we don't confuse the user
    sound.PlaySound( SoundManager.SOUND_WRONG );
    
    // Show the next card
    ShowCard();
  }

  /**
   * Works with GameManager to perform the back end processing of a card skip.  Also
   * handles the sound for skipping so that all forms of skips (swipes or button clicks)
   * play the sound.
   */
  protected void doSkip()
  {
    Log.d( TAG, "doSkip()");

 
    AIsActive = !AIsActive;
    this.viewFlipper.showNext();
    this.setCardTime();
    this.curGameManager.ProcessCard( Card.SKIP );

    // Mark the card with an icon for SKIP
    this.cardStatus.setBackgroundResource( Card.getCardMarkDrawableId(Card.SKIP) );
    this.cardStatus.setVisibility( View.VISIBLE );
    
    //Only play sound once card has been processed so we don't confuse the user
    sound.PlaySound( SoundManager.SOUND_SKIP );
    
    // Show the next card
    ShowCard();    
  }
  
  protected void doBack()
  {
    Log.d( TAG, "doBack()");
    if( this.isBack )
    {
      return;
    }
    
    this.AIsActive = !this.AIsActive;

    this.viewFlipper.setInAnimation(BackInFromLeftAnimation());
    this.viewFlipper.setOutAnimation(BackOutToRightAnimation());
    
    this.viewFlipper.showNext();
    
    this.setActiveCard();
    Card curCard = this.curGameManager.getPreviousCard();
    this.cardTitle.setText( curCard.getTitle() );
    // Update bad words
    this.SetBadWords( this.cardBadWords, curCard, this.curGameManager.GetActiveTeam());
    this.isBack = true;
    
    this.viewFlipper.setInAnimation(InFromRightAnimation());  //Reset animations
    this.viewFlipper.setOutAnimation(OutToLeftAnimation());
    
    // Play back sound
    WordFrenzyApplication app = (WordFrenzyApplication) this.getApplication();
    SoundManager snd = app.GetSoundManager();
    snd.PlaySound(SoundManager.SOUND_BACK);
  }
  
  /**
   * Updates references to reflect the A or B status of the current card
   */
  protected void setActiveCard()
  {
    Log.d( TAG, "setActiveCard()");
    int curTitle;
    int curWords;
    int curStatus;
    if( this.AIsActive )
    {
      curTitle = R.id.CardTitleA;
      curWords = R.id.CardA_BadWords;
      curStatus = R.id.StatusImageA;
    }
    else
    {
      curTitle = R.id.CardTitleB;
      curWords = R.id.CardB_BadWords;
      curStatus = R.id.StatusImageB;
    }

    this.cardTitle = (TextView) this.findViewById( curTitle );
    this.cardBadWords = (LinearLayout) this.findViewById( curWords );
    this.cardStatus = (ImageView) this.findViewById( curStatus );
    
  }

  /**
   * Function for changing the currently viewed card. It does a bit of bounds
   * checking.
   */
  protected void ShowCard()
  {
    Log.d( TAG, "ShowCard()" );
    
    this.setActiveCard();

    Card curCard = this.curGameManager.getNextCard();
    this.cardTitle.setText( curCard.getTitle() );
    // Update the badwords
    this.SetBadWords( this.cardBadWords, curCard, this.curGameManager.GetActiveTeam());
    // Hide the card status until marked
    this.cardStatus.setVisibility( View.INVISIBLE );
    this.isBack = false;
  }
  
  /**
   * Sets the text in a layout to match a supplied StringArray of badWords.
   * @param wordLayout - LinearLayout of textViews
   * @param curCard - Card object to set badWords from
   */
  private void SetBadWords( LinearLayout wordLayout, Card curCard, Team curTeam)
  {
    for( int i = 0; i < wordLayout.getChildCount(); ++i)
    {
      TextView text = (TextView) wordLayout.getChildAt(i);
      text.setText( curCard.getBadWords().get( i ));
      text.setTextColor( this.getResources().getColor(curTeam.getSecondaryColor()) );
    }
  }
  
  /**
   * OnTimeExpired defines what happens when the player's turn timer runs out
   */
  protected void OnTimeExpired( )
  {
    Log.d( TAG, "onTimeExpired()" );
    resultsDelay = new PauseTimer(1500)
    {
      @Override
      public void onFinish() 
      {
        Turn.this.OnTurnEnd();
      }

      @Override
      public void onTick() 
      {
        //Do nothing on tick
      }
    };
    resultsDelay.start();
    
    // Hide timer bar and time
    ImageView timerFill = (ImageView) this.findViewById(R.id.TurnTimerFill);
    timerFill.setVisibility( View.INVISIBLE );
    RelativeLayout timerGroup = (RelativeLayout) this.findViewById(R.id.Turn_TimerBar);
    timerGroup.startAnimation( this.ShowTimerAnim( false));
    // Hide buttons
    RelativeLayout buttonGroup = (RelativeLayout) this.findViewById(R.id.Turn_LowBar);
    buttonGroup.startAnimation( this.ShowButtonsAnim( false));
    
    
    TextView timer = (TextView) this.findViewById( R.id.Timer );
    timer.setVisibility( View.INVISIBLE );
    
    // Mark the current card as a skip so it can be amended later
    this.setCardTime();
    this.curGameManager.ProcessCard( Card.SKIP );
    
    
    // Hide card
    this.setActiveCard();
    this.viewFlipper.setVisibility( View.INVISIBLE );
    
    // turn off buttons
    this.buzzerButton.setEnabled( false );
    this.skipButton.setEnabled( false );
    this.nextButton.setEnabled( false );
    
    TextView timesUpView = (TextView) this.findViewById(R.id.TurnTimesUp);
    timesUpView.setVisibility( View.VISIBLE);
  }
  
  /**
   * Hands off the intent to the next turn summary activity.
   */
  protected void OnTurnEnd( )
  {
    Log.d( TAG, "onTurnEnd()" );
	  this.buzzVibrator.cancel();
	  Intent newintent = new Intent( this, TurnSummary.class);
	  startActivity(newintent);
  }

  protected void setupViewReferences()
  {
    Log.d( TAG, "setupViewReferences()");
    this.buzzVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

    WordFrenzyApplication application =
      (WordFrenzyApplication) this.getApplication();
    this.curGameManager = application.GetGameManager();

    this.pauseOverlay = (View) this.findViewById( R.id.PauseImageView );
    this.countdownTxt = (TextView) findViewById( R.id.Timer );
    this.viewFlipper = (ViewFlipper) this.findViewById( R.id.ViewFlipper0 );
    this.timesUpText = (TextView) this.findViewById(R.id.TurnTimesUp);
    
    this.buzzerButton = (ImageButton) this.findViewById( R.id.ButtonWrong );
    this.nextButton = (ImageButton) this.findViewById( R.id.ButtonCorrect );
    this.skipButton = (ImageButton) this.findViewById( R.id.ButtonSkip );
    
    this.timerfill = (ImageView) this.findViewById(R.id.TurnTimerFill);
    this.pauseTextLayout = (LinearLayout) this.findViewById( R.id.Turn_PauseTextGroup);
    
    this.timerGroup = (RelativeLayout) this.findViewById(R.id.Turn_TimerBar);
    this.buttonGroup = (RelativeLayout) this.findViewById(R.id.Turn_LowBar);
  }
  
  protected void setupUIProperties()
  {
    Log.d( TAG, "setupUIProperties()");
    this.pauseOverlay.setVisibility( View.INVISIBLE );
    this.pauseOverlay.setOnClickListener( PauseListener );

    this.timerGroup.setOnClickListener( this.TimerClickListener );
    
    this.viewFlipper.setInAnimation(InFromRightAnimation());
    this.viewFlipper.setOutAnimation(OutToLeftAnimation());
    
    //this.buzzerButton.setOnTouchListener( BuzzListener );
    this.buzzerButton.setOnClickListener( WrongListener );
    this.nextButton.setOnClickListener( CorrectListener );
    
    //Only show skipButton and set listener if preference is enabled
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
    if (sp.getBoolean("allow_skip", true))
    {
      this.skipButton.setOnClickListener( SkipListener );
      this.skipButton.setVisibility(View.VISIBLE);
      
      //Only listen for backs and swipes (swipeListener) if skip pref is on
      this.swipeDetector = new GestureDetector(swipeListener);
      this.gestureListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (swipeDetector.onTouchEvent(event)) {
                return true;
            }
            return true; //prevents highlighting badwords by consuming even if not detected as a swipe
        }
      };
    }
    else
    {
      this.skipButton.setVisibility(View.INVISIBLE);
      
      //If skip pref is off, set up gestureListener to only listen to back swipes
      this.swipeDetector = new GestureDetector(onlybackswipeListener);
      this.gestureListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (swipeDetector.onTouchEvent(event)) {
                return true;
            }
            return true; //prevents highlighting badwords by consuming even if not detected as a swipe
        }
     };
    }
   
    //Setup the "card" views to allow for skip gesture to be performed on top
    this.findViewById( R.id.CardTitleA ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardA_BadWords ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardTitleB ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardB_BadWords ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.Turn_Root ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.ViewFlipper0 ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardLayoutA ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardLayoutB ).setOnTouchListener( this.gestureListener );
    
    //Change views to appropriate team color
    ImageView barFill = (ImageView) this.findViewById( R.id.TurnTimerFill );
    
    Team curTeam = this.curGameManager.GetActiveTeam();
    barFill.setImageResource( curTeam.getBg() );
    this.findViewById( R.id.Turn_Root ).setBackgroundResource( curTeam.getGradient() );
    
  }
  
  /**
   * onCreate - initializes the activity to display the word you have to cause
   * your team mates to say with the words you cannot say below.
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.d( TAG, "onCreate()" );
    
    // Save sound manager as a local variable
    WordFrenzyApplication application = (WordFrenzyApplication) Turn.this.getApplication();
    sound = application.GetSoundManager();
    
    // set which card is active
    this.AIsActive = true;
    this.isBack = true;

    // Setup the view
    this.setContentView(R.layout.turn );
    
    this.setupViewReferences();
    
    this.setupUIProperties();
    
    this.showDialog( DIALOG_READY_ID );
    
    // Capture our preference variable for music once
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Turn.this.getBaseContext());
    if( sp.getBoolean("music_enabled", true))
      this.musicEnabled = true;
    else
      this.musicEnabled = false;
    
    // Initialize the turn timer
    long time = this.curGameManager.GetTurnTime();
    this.counter = new PauseTimer(time)
    {
      private int btwncount = 5;
      
      
      @Override
      public void onFinish() 
      {
        Turn.this.OnTimeExpired();
        Turn.this.countdownTxt.setText( "0" );
        Turn.this.turnIsOver = true;
      }

      @Override
      public void onTick()
      {            
        Log.d( TAG , Long.toString( counter.getTimeRemaining() ) );
        Turn.this.countdownTxt.setText( Long.toString(( counter.getTimeRemaining() / 1000 ) + 1 ));
        if ( !musicEnabled )
        {
        if ( counter.getTimeRemaining() <= 15000 )
        {
          if ( btwncount == 5 )
          {
            Log.d( TAG , "^^^^^1" );
            btwncount = 0;
            sound.PlaySound( SoundManager.SOUND_TICK );
          }

          if ( counter.getTimeRemaining() <= 10000 )
          {
            if ( btwncount == 2 )
            {     
              Log.d( TAG , "^^^^^0.5" );
              sound.PlaySound( SoundManager.SOUND_TICK );
            }            
          }
          btwncount++;
        }
        } 
      }
    };
    this.lastCardTimerState = time;
  }

  /**
   *
   */
  @Override
  public void onRestart()
  {
    super.onRestart();
    Log.d( TAG, "onRestart()" );
  }

  /**
   *
   */
  @Override
  public void onStart()
  {
    super.onStart();
    Log.d( TAG, "onStart()" );
  }

  /**
   *
   */
  @Override
  public void onResume()
  {
    super.onResume();
    Log.d( TAG, "onResume()" );
  }

  /**
   *
   */
  @Override
  public void onPause()
  {
    super.onPause();
    Log.d( TAG, "onPause()" );
  }

  /**
   *
   */
  @Override
  public void onStop()
  {
    super.onStop();
    Log.d( TAG, "onStop()" );
    if(!this.isPaused)
    {
      this.pauseGame();
    }
  }

  /**
   *
   */
  @Override
  public void onDestroy()
  {
    super.onDestroy();
    Log.d( TAG, "onDestroy()" );
  }

  /**
   *
   */
  @Override
  protected Dialog onCreateDialog(int id)
  {
    Log.d( TAG, "onCreateDialog(" + id + ")" );
    Dialog dialog = null;
    AlertDialog.Builder builder = null;
    
    switch(id) {
    case DIALOG_GAMEOVER_ID:
      builder = new AlertDialog.Builder(this);
      builder.setMessage( "Are you sure you want to end the current game?" )
             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                 WordFrenzyApplication application = (WordFrenzyApplication) Turn.this.getApplication();
                 GameManager gm = application.GetGameManager();
                 gm.EndGame();
                 startActivity(new Intent(Intent.ACTION_CALL, getIntent().getData()));
                 }
               })
             .setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
                 }
               });       
      dialog = builder.create();
      break;
    case DIALOG_READY_ID:
      // Play team ready sound
      sound.PlaySound( SoundManager.SOUND_TEAMREADY );
      
      String curTeam = this.curGameManager.GetActiveTeam().getName();
      builder = new AlertDialog.Builder(this);
      builder.setMessage( "Ready " + curTeam + "?" )
             .setCancelable(false)
             .setPositiveButton("START!", new DialogInterface.OnClickListener() {
              
              public void onClick(DialogInterface dialog, int which) {
                Turn.this.ShowCard();
                Turn.this.isBack = true;
                Turn.this.isPaused = false;
                Turn.this.startTimer();
                
                // Start the turn music
                WordFrenzyApplication application = (WordFrenzyApplication) Turn.this.getApplication();
                GameManager gm = application.GetGameManager();
                int musicId = R.raw.mus_round_60;
                switch ( gm.GetTurnTime())
                {
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
                
                MediaPlayer mp = application.CreateMusicPlayer( Turn.this.getBaseContext(), musicId );
                if( musicEnabled )
                {
                  mp.start();
                }
              }
            })
            
             .setOnCancelListener(new DialogInterface.OnCancelListener() {				
             public void onCancel(DialogInterface dialog) {
             // Handles canceling the dialog (which shouldn't be possible anymore)
	                 Turn.this.ShowCard();
	                 Turn.this.isBack = true;
	                 Turn.this.startTimer();	                 					
               }
             });      
      dialog = builder.create();
      break;
    default:
        dialog = null;
    }
    return dialog;

  }
  
  /**
   *  Class tracks the seek time on the music realignment that happens on every resume.
   */
  private class TurnMusicListener implements OnSeekCompleteListener
  {

    @Override
    public void onSeekComplete(MediaPlayer mp) {
      // Resume the game on seek complete
      Turn.this.resumeGame();

      // Resume the music
      if( musicEnabled )
      {
        mp.start();
      }
    }
  }
  
  protected void resumeGame()
  {
    Log.d( TAG, "resumeGameTurn()" );
    this.isPaused = false;
    
    if(!this.turnIsOver)
    {
      this.resumeTurnTimer();
  
      this.setActiveCard();

      this.viewFlipper.setVisibility( View.VISIBLE );
      this.buzzerButton.setEnabled( true );
      this.skipButton.setEnabled( true );
      this.nextButton.setEnabled( true );

      
      this.timerGroup.startAnimation( this.ShowTimerAnim( true ));
      this.buttonGroup.startAnimation( this.ShowButtonsAnim( true ));
    }
    else
    {
      resultsDelay.resume();
      
      // Show TimesUp text when resuming after time has expired
      this.timesUpText.setVisibility( View.VISIBLE );
    }
  }

  protected void pauseGame()
  {
    Log.d( TAG, "pauseGameTurn()" );
    this.isPaused = true;
    this.pauseOverlay.setVisibility( View.VISIBLE );
    
    this.pauseTextLayout = (LinearLayout) this.findViewById( R.id.Turn_PauseTextGroup);
    pauseTextLayout.setVisibility( View.VISIBLE);
    
    // Stop music
    WordFrenzyApplication application = (WordFrenzyApplication) this.getApplication();
    MediaPlayer mp = application.GetMusicPlayer();
    mp.pause();
    
    if(!this.turnIsOver)
    {    
      this.stopTurnTimer();
  
      this.setActiveCard();
      
      this.viewFlipper.setVisibility( View.INVISIBLE );
      this.buzzerButton.setEnabled( false );
      this.skipButton.setEnabled( false );
      this.nextButton.setEnabled( false );

      this.timerGroup.startAnimation( this.ShowTimerAnim( false ));
      this.buttonGroup.startAnimation( this.ShowButtonsAnim( false ));
    }
    else
    {
      resultsDelay.pause();
      
      // Hide TimesUp text when pause after time has expired
      this.timesUpText.setVisibility( View.INVISIBLE );
    }
  }

  @Override
  public boolean onMenuOpened(int featureId, Menu menu)
  {
    if( !this.isPaused )
    {
      this.pauseGame();
    }
    return true;
  }
  
  public void setCardTime()
  {
    this.curGameManager.GetCurrentCard().setTime( (int)(this.lastCardTimerState - this.counter.getTimeRemaining()) );
    this.lastCardTimerState = this.counter.getTimeRemaining();
  }

  /**
   * Handler for key down events
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    Log.d( TAG, "onKeyDown()" );

    // Handle the back button
    if( keyCode == KeyEvent.KEYCODE_BACK
        && event.getRepeatCount() == 0 )
      {
        event.startTracking();
        return true;
      }

    return super.onKeyDown(keyCode, event);
  }

  /**
   * Handler for key up events
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event)
  {
    Log.d( TAG, "onKeyUp()" );

    // Back button should go to the previous card
    if( keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled() )
      {
      if ( !this.isPaused )
      {
        this.doBack();
      }
      return true;
      }

    return super.onKeyUp(keyCode, event);
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
  if (this.swipeDetector.onTouchEvent(event))
  return true;
  else
  return false;
  }

}
