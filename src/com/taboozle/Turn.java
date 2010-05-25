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

  private Game curGame;
  
  /**
   * Boolean to track which views are currently active
   */
  protected boolean AIsActive;
  
  /**
   * Sound pool for playing the buzz sound on a loop.
   */
  protected SoundPool soundPool;
  protected int buzzSoundId;
  protected int buzzStreamId;
  
  /**
   * Unique IDs for Options menu
   */
  protected static final int MENU_ENDGAME = 0;
  protected static final int MENU_SCORE = 1;
  protected static final int MENU_RULES = 2;  
 
  /**
   *  Creates the menu items 
   */
  public boolean onCreateOptionsMenu(Menu menu) 
  {	  
      menu.add(0, MENU_ENDGAME, 0, "End Game");
      menu.add(0, MENU_SCORE, 0, "Score");
      menu.add(0, MENU_RULES, 0, "Rules");
      
      return true;
  } 
  
  /**
   * OnClickListener for the buzzer button
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
        boolean ret = false;
        if( event.getAction() == MotionEvent.ACTION_DOWN )
        {
          buzzStreamId = soundPool.play( buzzSoundId, volume, volume, 1, -1, 1.0f );
          ret = true;
        }
        else if( event.getAction() == MotionEvent.ACTION_UP)
        {
          soundPool.stop( buzzStreamId );
          ret = true;
        }
        else
        {
          ret = false;
        }
        return ret;
      }
  };
  
  /**
   * constant for the logging tag
   */
  //private static final String TAG = "taboozle";

  private Animation InFromRightAnimation ()
  {
	Animation inFromRight = new TranslateAnimation(
		  	Animation.RELATIVE_TO_PARENT,  1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
		  	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f );
  	inFromRight.setDuration(500);
  	return inFromRight;
  }

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
    int curTitle = 0;
    int curWords = 0;
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
    Card curCard = this.curGame.getNextCard();
    cardTitle.setText( curCard.title );
    for( int i = 0; i < curCard.badWords.size(); i++ )
    {
      cardAdapter.add( curCard.badWords.get( i ) );
    }
    cardWords.setAdapter( cardAdapter );
  }
  
  private OnClickListener CorrectListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
        AIsActive = !AIsActive;
        ViewFlipper flipper = (ViewFlipper) findViewById( R.id.ViewFlipper0 );
        flipper.showNext();
        ShowCard();
      }
  };
  
  private OnClickListener SkipListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
        AIsActive = !AIsActive;
        ViewFlipper flipper = (ViewFlipper) findViewById( R.id.ViewFlipper0 );
        flipper.showNext();
        ShowCard();
      }
  };
 
  /**
   * CountdownTimer - This initializes a timer during every turn that runs a
   * method when it completes as well as during update intervals.
  */ 
  public class TurnTimer extends CountDownTimer
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
  };

  /**
   * OnTurnEnd - Hands off the intent to the next turn summary activity.
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

    this.curGame = new Game( this );
    
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
