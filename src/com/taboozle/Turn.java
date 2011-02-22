package com.taboozle;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.util.Log;

/**
 * This handles a single turn consisting of cards presented to a player for a
 * limited amount of time.
 *
 * @author The Taboozle Team
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

  private ImageView pauseOverlay;
  private ImageButton buzzerButton;
  private ImageButton nextButton;
  private ImageButton skipButton;
  private TextView countdownTxt;
  private ViewFlipper viewFlipper;
  
  private ImageView timerfill;
  
  private TextView cardTitle;
  private ListView cardWords;
  private ImageView cardStatus;

  private long lastCardTimerState;
  
  /**
   * Tracks the current state of the Turn as a boolean.  Set to true when time has expired and
   * activity is showing the user "Time's up!"
   */
  private boolean turnIsOver = false;
  
  /**
   * This is a reference to the current game manager
   */
  private GameManager curGameManager;

  /**
   * Boolean to track which views are currently active
   */
  private boolean AIsActive;

  /**
   * Sound pool for playing the buzz sound on a loop.
   */
  private SoundPool soundPool;

  /**
   * id of the buzz's stream within android's sound-pool framework
   */
  private int buzzStreamId;

  /**
   * id of the correct sound within the sound-pool framework
   */
  private int rightSoundId;

  /**
   * id of the wrong sound within the sound-pool framework
   */
  private int wrongSoundId;
  
  /**
   * id of the swipe sound within the sound-pool framework
   */
  private int swipeSoundId;
  
  /**
   * vibrator object to vibrate on buzz click
   */
  private Vibrator buzzVibrator;
  
  /**
   * Boolean for representing whether we've gone back or not
   */
  private boolean isBack;

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
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
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
  
  private GestureDetector swipeDetector;

  View.OnTouchListener gestureListener;

  private PauseTimer counter;
  private PauseTimer resultsDelay;

  private void startTimer()
  {
    Log.d( TAG, "startTimer()" );

    long time = this.curGameManager.GetTurnTime();
    this.counter = new PauseTimer(time)
    {
      @Override
      public void onFinish() 
      {
        Turn.this.OnTimeExpired();
        Turn.this.countdownTxt.setText( ":0" );
        Turn.this.turnIsOver = true;
      }

      @Override
      public void onTick()
      {
        Turn.this.countdownTxt.setText( ":" + Long.toString(( counter.getTimeRemaining() / 1000 ) + 1 ));
      }
    };
    this.lastCardTimerState = time;
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
    menu.add(0, R.string.menu_Score, 0, "Score");
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
      case R.string.menu_Score:
        //quit();
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
   * Listener for the pause overlay. It unpauses the the game.
   */
  private final OnClickListener PauseListener = new OnClickListener()
  {
      public void onClick(View v)
      {
        Log.d( TAG, "PauseListener OnClick()" );
        Turn.this.resumeGame();
        Turn.this.closeOptionsMenu();
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
    AudioManager mgr =
      (AudioManager) this.getBaseContext().getSystemService( Context.AUDIO_SERVICE );
    float streamVolumeCurrent = mgr.getStreamVolume( AudioManager.STREAM_MUSIC );
    float streamVolumeMax = mgr.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
    float volume = streamVolumeCurrent / streamVolumeMax;
  
    ViewFlipper flipper = (ViewFlipper) findViewById( R.id.ViewFlipper0 );
    
    AIsActive = !AIsActive;   
    flipper.showNext();
    this.setCardTime();
    curGameManager.ProcessCard( GameData.RIGHT );

    //Only play sound once card has been processed so we don't confuse the user
    soundPool.play( rightSoundId, volume, volume, 1, 0, 1.0f );
    
    ShowCard();    
  }
  
  /**
   * Works with GameManager to perform the back end processing of a card wrong.  Also
   * handles playing of a durative incorrect sound, as opposed to the buzzer.
   */
  protected void doWrong()
  {
    Log.d( TAG, "doWrong()");
    AudioManager mgr =
      (AudioManager) this.getBaseContext().getSystemService( Context.AUDIO_SERVICE );
    float streamVolumeCurrent = mgr.getStreamVolume( AudioManager.STREAM_MUSIC );
    float streamVolumeMax = mgr.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
    float volume = streamVolumeCurrent / streamVolumeMax;
    
    AIsActive = !AIsActive;
    ViewFlipper flipper = (ViewFlipper) findViewById( R.id.ViewFlipper0 );
    flipper.showNext();
    
    Turn.this.setCardTime();
    curGameManager.ProcessCard( GameData.WRONG );
    ShowCard();

    //Only play sound once card has been processed so we don't confuse the user
    buzzStreamId = soundPool.play( wrongSoundId, volume, volume, 1, 0, 1.0f );
  }

  /**
   * Works with GameManager to perform the back end processing of a card skip.  Also
   * handles the sound for skipping so that all forms of skips (swipes or button clicks)
   * play the sound.
   */
  protected void doSkip()
  {
    Log.d( TAG, "doSkip()");
    AudioManager mgr =
      (AudioManager) this.getBaseContext().getSystemService( Context.AUDIO_SERVICE );
    float streamVolumeCurrent = mgr.getStreamVolume( AudioManager.STREAM_MUSIC );
    float streamVolumeMax = mgr.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
    float volume = streamVolumeCurrent / streamVolumeMax;
 
    AIsActive = !AIsActive;
    this.viewFlipper.showNext();
    this.setCardTime();
    this.curGameManager.ProcessCard( GameData.SKIP );

    //Only play sound once card has been processed so we don't confuse the user
    soundPool.play( swipeSoundId, volume, volume, 1, 0, 1.0f );
    
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

    //this.viewFlipper.setInAnimation(OutToLeftAnimation());    //Reverse animations temporarily
    this.viewFlipper.setInAnimation(BackInFromLeftAnimation());
    this.viewFlipper.setOutAnimation(BackOutToRightAnimation());
    
    this.viewFlipper.showNext();
    
    this.setActiveCard();
    ArrayAdapter<String> cardAdapter =
      new ArrayAdapter<String>( this, R.layout.word );
    Card curCard = this.curGameManager.GetPreviousCard();
    this.cardTitle.setText( curCard.getTitle() );
    for( int i = 0; i < curCard.getBadWords().size(); i++ )
    {
      cardAdapter.add( curCard.getBadWords().get( i ) );
    }
    this.cardWords.setAdapter( cardAdapter );
    this.cardStatus.setBackgroundResource( curCard.getDrawableIdForBack() );
    this.isBack = true;
    
    this.viewFlipper.setInAnimation(InFromRightAnimation());  //Reset animations
    this.viewFlipper.setOutAnimation(OutToLeftAnimation());
  }
  
  protected void setActiveCard()
  {
    Log.d( TAG, "setActiveCard()");
    int curTitle;
    int curWords;
    int curStatus;
    if( this.AIsActive )
    {
      curTitle = R.id.CardTitleA;
      curWords = R.id.CardWordsA;
      curStatus = R.id.StatusImageA;
    }
    else
    {
      curTitle = R.id.CardTitleB;
      curWords = R.id.CardWordsB;
      curStatus = R.id.StatusImageB;
    }

    this.cardTitle = (TextView) this.findViewById( curTitle );
    this.cardWords = (ListView) this.findViewById( curWords );
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

    ArrayAdapter<String> cardAdapter =
    new ArrayAdapter<String>( this, R.layout.word );
    Card curCard = this.curGameManager.GetNextCard();
    this.cardTitle.setText( curCard.getTitle() );
    for( int i = 0; i < curCard.getBadWords().size(); i++ )
    {
      cardAdapter.add( curCard.getBadWords().get( i ) );
    }
    this.cardWords.setAdapter( cardAdapter );
    this.cardStatus.setBackgroundResource( curCard.getDrawableId() );
    this.isBack = false;
  }
  
  /**
   * OnTimeExpired defines what happens when the player's turn timer runs out
   */
  protected void OnTimeExpired( )
  {
    Log.d( TAG, "onTimeExpired()" );
    resultsDelay = new PauseTimer(1000)
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
    
    TextView timer = (TextView) this.findViewById( R.id.Timer );
    timer.setVisibility( View.INVISIBLE );
    
    // Hide card and disable buttons.
    this.setActiveCard();
    
    this.viewFlipper.setVisibility( View.INVISIBLE );
    
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
	  //Stop the sound if someone had the buzzer held down
	  this.soundPool.stop( buzzStreamId );
	  this.buzzVibrator.cancel();
	  Intent newintent = new Intent( this, TurnSummary.class);
	  startActivity(newintent);
  }

  protected void setupViewReferences()
  {
    Log.d( TAG, "setupViewReferences()");
    this.soundPool = new SoundPool( 4, AudioManager.STREAM_MUSIC, 100 );
    //this.buzzSoundId = this.soundPool.load( this, R.raw.buzzer, 1 );
    this.buzzVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    this.rightSoundId = this.soundPool.load( this, R.raw.wine_clink, 1);
    this.swipeSoundId = this.soundPool.load( this, R.raw.swipe, 1);
    this.wrongSoundId = this.soundPool.load( this, R.raw.wrong, 1);

    TaboozleApplication application =
      (TaboozleApplication) this.getApplication();
    this.curGameManager = application.GetGameManager();

    this.pauseOverlay = (ImageView) this.findViewById( R.id.PauseImageView );
    this.countdownTxt = (TextView) findViewById( R.id.Timer );
    this.viewFlipper = (ViewFlipper) this.findViewById( R.id.ViewFlipper0 );
    
    this.buzzerButton = (ImageButton) this.findViewById( R.id.ButtonWrong );
    this.nextButton = (ImageButton) this.findViewById( R.id.ButtonCorrect );
    this.skipButton = (ImageButton) this.findViewById( R.id.ButtonSkip );
    
    this.timerfill = (ImageView) this.findViewById(R.id.TurnTimerFill);
  }
  
  protected void setupUIProperties()
  {
    Log.d( TAG, "setupUIProperties()");
    this.pauseOverlay.setVisibility( View.INVISIBLE );

    this.pauseOverlay.setOnClickListener( PauseListener );

    this.countdownTxt.setOnClickListener( this.TimerClickListener );
    
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
    }
    else
    {
      this.skipButton.setVisibility(View.INVISIBLE);
    }
    this.swipeDetector = new GestureDetector(swipeListener);
    this.gestureListener = new View.OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
          if (swipeDetector.onTouchEvent(event)) {
              return true;
          }
          return true; //prevents highlighting badwords by consuming even if not detected as a swipe
      }
    };
    
    //Setup the "card" views to allow for skip gesture to be performed on top
    this.findViewById( R.id.CardTitleA ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardWordsA ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardTitleB ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardWordsB ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.MultiCardLayout ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.ViewFlipper0 ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardLayoutA ).setOnTouchListener( this.gestureListener );
    this.findViewById( R.id.CardLayoutB ).setOnTouchListener( this.gestureListener );
    
    //Change views to appropriate team color
    ImageView barFill = (ImageView) this.findViewById( R.id.TurnTimerFill );
    
    Team curTeam = this.curGameManager.GetActiveTeam();
    barFill.setImageResource( curTeam.getBg() );
    this.findViewById( R.id.MultiCardLayout ).setBackgroundResource( curTeam.getGradient() );
    
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
    
    // set which card is active
    this.AIsActive = true;
    this.isBack = true;

    // Setup the view
    this.setContentView(R.layout.turn );
    
    this.setupViewReferences();
    
    this.setupUIProperties();
    
    this.showDialog( DIALOG_READY_ID );

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
    this.pauseGame();
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
             .setTitle("Confirm End Game")
             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                 TaboozleApplication application = (TaboozleApplication) Turn.this.getApplication();
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
      String curTeam = this.curGameManager.GetActiveTeam().getName();
      builder = new AlertDialog.Builder(this);
      builder.setTitle( "Ready " + curTeam + "?" )
             .setCancelable(false)
             .setPositiveButton("START!", new DialogInterface.OnClickListener() {
              
              public void onClick(DialogInterface dialog, int which) {
                Turn.this.ShowCard();
                Turn.this.isBack = true;
                Turn.this.startTimer();                
              }
            })
            
             .setOnCancelListener(new DialogInterface.OnCancelListener() {				
             public void onCancel(DialogInterface dialog) {
             // Handles cancelling the dialog (which shouldn't be possible anymore)
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
  
  protected void resumeGame()
  {
    this.pauseOverlay.setVisibility( View.INVISIBLE );
    
    if(!this.turnIsOver)
    {
      this.resumeTurnTimer();
  
      this.setActiveCard();

      this.viewFlipper.setVisibility( View.VISIBLE );
      this.buzzerButton.setEnabled( true );
      this.skipButton.setEnabled( true );
      this.nextButton.setEnabled( true );
    }
    else
    {
      resultsDelay.resume();
    }
  }

  protected void pauseGame()
  {
    this.pauseOverlay.setVisibility( View.VISIBLE );
    
    if(!this.turnIsOver)
    {    
      this.stopTurnTimer();
  
      this.setActiveCard();
      
      this.viewFlipper.setVisibility( View.INVISIBLE );
      this.buzzerButton.setEnabled( false );
      this.skipButton.setEnabled( false );
      this.nextButton.setEnabled( false );
    }
    else
    {
      resultsDelay.pause();
    }
  }

  @Override
  public boolean onMenuOpened(int featureId, Menu menu)
  {
    this.pauseGame();
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

    // Make back do nothing on key-up instead of climb the action stack
    if( keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled() )
      {
      this.doBack();
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
