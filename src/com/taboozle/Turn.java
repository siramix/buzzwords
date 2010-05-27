package com.taboozle;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * This handles a single turn consisting of cards presented to a player for a 
 * limited amount of time.
 * 
 * @author The Taboozle Team
 */
public class Turn extends Activity
{

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
   * id of the buzz within android's sound-pool framework
   */
  private int buzzSoundId;
  
  /**
   * id of the buzz's stream within android's sound-pool framework 
   */
  private int buzzStreamId;
  
  /**
   * Unique IDs for Options menu
   */
  protected static final int MENU_ENDGAME = 0;
  protected static final int MENU_SCORE = 1;
  protected static final int MENU_RULES = 2;  
 
  /**
   *  Creates the menu items for the options menu 
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {	  
      menu.add(0, MENU_ENDGAME, 0, "End Game");
      menu.add(0, MENU_SCORE, 0, "Score");
      menu.add(0, MENU_RULES, 0, "Rules");
      
      return true;
  } 
  
  /**
   * Listener for the buzzer that plays on touch-down and stops playing on
   * touch-up.
   */
  private OnTouchListener BuzzListener = new OnTouchListener() 
  {
      public boolean onTouch(View v, MotionEvent event) 
      {
        AudioManager mgr = 
          (AudioManager) v.getContext().getSystemService( Context.AUDIO_SERVICE );
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC); 
        float volume = streamVolumeCurrent / streamVolumeMax;
        boolean ret;
        switch( event.getAction() )
        {
          case MotionEvent.ACTION_DOWN:
            buzzStreamId = soundPool.play( buzzSoundId, volume, volume, 1, -1, 1.0f );
            ret = true;
            break;
          case MotionEvent.ACTION_UP:
            soundPool.stop( buzzStreamId );
            ret = true;
            break;
          default:
            ret = false;              
        }

        return ret;
      } 
      
  }; // End BuzzListener
  
  /**
   * Listener for the 'Correct' button. It deals with the flip to the next 
   * card.
   */
  private OnClickListener CorrectListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
        AIsActive = !AIsActive;
        ViewFlipper flipper = (ViewFlipper) findViewById( R.id.ViewFlipper0 );
        flipper.showNext();
        curGameManager.ProcessCard( 0 );
        ShowCard();
      }  
  }; // End CorrectListener
  
  /**
   * Listener for the 'Skip' button. This deals with moving to the next card
   * via the ViewFlipper, but denotes that the card was skipped;
   */
  private OnClickListener SkipListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
        AIsActive = !AIsActive;
        ViewFlipper flipper = (ViewFlipper) findViewById( R.id.ViewFlipper0 );
        flipper.showNext();
        curGameManager.ProcessCard( 2 );
        ShowCard();
      }
  }; // End SkipListener
 
  /**
   * CountdownTimer - This initializes a timer during every turn that runs a
   * method when it completes as well as during update intervals.
  */ 
  private class TurnTimer extends CountDownTimer
  {
    
    public TurnTimer(long millisInFuture, long countDownInterval) 
    {
      super(millisInFuture, countDownInterval);
    }
    
    @Override
      public void onFinish() {
         OnTurnEnd();
      }
    
    @Override
      public void onTick(long millisUntilFinished) 
      {
     TextView countdownTxt = (TextView) findViewById( R.id.Timer );
     countdownTxt.setText( ":" + Long.toString(( millisUntilFinished / 1000 ) + 1 ));
      }
  }; // End TurnTimer
  
  /**
   * @return The animation that brings cards into view from the right of the
   * screen
   */
  private Animation InFromRightAnimation ()
  {
	Animation inFromRight = new TranslateAnimation(
		  	Animation.RELATIVE_TO_PARENT,  1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
		  	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f );
  	inFromRight.setDuration(500);
  	return inFromRight;
  }

  /**
   * @return The animation that tosses the cards from the view out into the
   * ether at the left of the screen
   */
  private Animation OutToLeftAnimation ()
  {
	Animation outToLeft = new TranslateAnimation(
		  	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
		  	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f );
	outToLeft.setDuration(500);
  	return outToLeft;
  }  
  
  /**
   * Function for changing the currently viewed card. It does a bit of bounds
   * checking.
   */
  protected void ShowCard()
  {
    int curTitle;
    int curWords;
    if( this.AIsActive )
    {
    	curTitle = R.id.CardTitleA;
    	curWords = R.id.CardWordsA;
    }
    else
    {
    	curTitle = R.id.CardTitleB;
    	curWords = R.id.CardWordsB;
    }
    
    TextView cardTitle = (TextView) this.findViewById( curTitle );
    ListView cardWords = (ListView) this.findViewById( curWords );
    // Disable the ListView to prevent its children from being click-able
    cardWords.setEnabled(false);
    ArrayAdapter<String> cardAdapter = 
      new ArrayAdapter<String>( this, R.layout.word );
    Card curCard = this.curGameManager.GetNextCard();
    cardTitle.setText( curCard.getTitle() );
    for( int i = 0; i < curCard.getBadWords().size(); i++ )
    {
      cardAdapter.add( curCard.getBadWords().get( i ) );
    }
    cardWords.setAdapter( cardAdapter );
  }
  
  

  /**
   * Hands off the intent to the next turn summary activity.
   */
  protected void OnTurnEnd( )
  {
	  //Stop the sound if someone had the buzzer held down
	  soundPool.stop( buzzStreamId );
	  Intent newintent = new Intent( this, TurnSummary.class);
	  startActivity(newintent);
  }

  /**
   * onCreate - initializes the activity to display the word you have to cause
   * your team mates to say with the words you cannot say below.
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );

    this.AIsActive = true;

    this.soundPool = new SoundPool( 4, AudioManager.STREAM_MUSIC, 100 );
    this.buzzSoundId = this.soundPool.load( this, R.raw.buzzer, 1 );

    TaboozleApplication application = 
      (TaboozleApplication) this.getApplication();
    this.curGameManager = application.GetGameManager();
    this.curGameManager.NextTurn();
    
    // Setup the view
    this.setContentView(R.layout.turn );
    
    ViewFlipper flipper = (ViewFlipper) this.findViewById( R.id.ViewFlipper0 );
    flipper.setInAnimation(InFromRightAnimation());
    flipper.setOutAnimation(OutToLeftAnimation());
    
    this.ShowCard();
    
    TurnTimer counter = new TurnTimer( 60000, 200);
    counter.start();
    
    ImageButton buzzerButton = (ImageButton)this.findViewById( R.id.BuzzerButtonA );
    buzzerButton.setOnTouchListener( BuzzListener );
    buzzerButton = (ImageButton)this.findViewById( R.id.BuzzerButtonB );
    buzzerButton.setOnTouchListener( BuzzListener );
    
    ImageButton nextButton = (ImageButton)this.findViewById( R.id.CorrectButtonA );
    nextButton.setOnClickListener( CorrectListener );
    nextButton = (ImageButton)this.findViewById( R.id.CorrectButtonB );
    nextButton.setOnClickListener( CorrectListener );
    
    Button skipButton = (Button)this.findViewById( R.id.SkipButtonA );
    skipButton.setOnClickListener( SkipListener );
    skipButton = (Button)this.findViewById( R.id.SkipButtonB );
    skipButton.setOnClickListener( SkipListener );
  }
}
