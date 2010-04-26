package com.taboozle;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.taboozle.Pack.Cards;

import android.app.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
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
   * Small private class for representing the card in memory as strings 
   */
  private class CardStrings
  {
    public String title;
    public ArrayList<String> badWords;
    
    public CardStrings()
    {
      title = "";
      badWords = new ArrayList<String>();
    }
  }
 
  /**
   * A variable-length list of cards for in-memory storage
   */
  protected ArrayList<CardStrings> Deck;
  
  /**
   * An integer to keep track of where we are
   */
  protected int DeckPosition;
  
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
  	inFromRight.setDuration(2000);
  	inFromRight.setInterpolator(new OvershootInterpolator(1));
  	return inFromRight;
  }

  private Animation OutToLeftAnimation ()
  {
	Animation outToLeft = new TranslateAnimation(
		  	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
		  	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f );
	outToLeft.setDuration(2000);
	outToLeft.setInterpolator(new OvershootInterpolator(1));
  	return outToLeft;
  }  
  
  /**
   * Function for changing the currently viewed card. It does a bit of bounds
   * checking.
   */
  protected void ShowCard()
  {
    if( this.DeckPosition >= this.Deck.size() ||
        this.DeckPosition < 0 )
    {
      this.DeckPosition = 0;
    }

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
    // Disable the ListView to prevent its children from being clickable
    cardWords.setEnabled(false);
    ArrayAdapter<String> cardAdapter = 
      new ArrayAdapter<String>( this, R.layout.word );
    CardStrings curCard = this.Deck.get( this.DeckPosition );
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
        DeckPosition++;
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
        DeckPosition--;
        if( DeckPosition < 0 )
        {
        	DeckPosition = Deck.size()-1;
        }
        ShowCard();
      }
  };
  
  /**
   * onCreate - initializes the activity to display the word you have to cause
   * your team mates to say with the words you cannot say below.
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );

    this.Deck = new ArrayList<CardStrings>();
    this.DeckPosition = 0;
    this.AIsActive = true;


    this.soundPool = new SoundPool( 4, AudioManager.STREAM_MUSIC, 100 );
    this.buzzSoundId = this.soundPool.load( this, R.raw.buzzer, 1 );
    
    // Setup the view
    this.setContentView(R.layout.turn );
    
    ViewFlipper flipper = (ViewFlipper) this.findViewById( R.id.ViewFlipper0 );
    flipper.setInAnimation(InFromRightAnimation());
    flipper.setOutAnimation(OutToLeftAnimation());
    
    // If no data was given in the intent (because we were started
    // as a MAIN activity), then use our default content provider.
    Intent intent = this.getIntent();

    if( intent.getData() == null )
    {
      intent.setData( Cards.CONTENT_URI );
    }
    
    // Add content to our content provider
    ContentResolver resolver = this.getContentResolver();
    
    // Form an array specifying which columns to return.
    String[] projection = new String[] { Cards.TITLE, Cards.BAD_WORDS };
    
    // Query and print the added card record
    Cursor cur = resolver.query( Pack.Cards.CONTENT_URI, projection, null,
                                 null, null );

    // Iterate through cursor transferring from database to memory
    if( cur.moveToFirst() )
    {
      int titleColumn = cur.getColumnIndex( Cards.TITLE );
      int badWordsColumn = cur.getColumnIndex( Cards.BAD_WORDS );
      
      do
      {
        // Get the field values
        String title = cur.getString( titleColumn );
        String badWords = cur.getString( badWordsColumn );

        StringTokenizer tok = new StringTokenizer(badWords);

        CardStrings cWords = new CardStrings();
        cWords.title = title;
        
        while( tok.hasMoreTokens() )
        {
          cWords.badWords.add( tok.nextToken( "," ) );
        }
        
        this.Deck.add( cWords );
      }
      while( cur.moveToNext() );
    }
    
    this.ShowCard();
    
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
