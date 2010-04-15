package com.taboozle;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.taboozle.common.Pack;
import com.taboozle.common.Pack.Cards;

import android.app.*;
import android.content.ContentResolver;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This is the activity class that kicks off Taboozle
 * 
 * @author The Taboozle Team
 */
public class taboozle extends Activity
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
  protected ArrayList<CardStrings> cardStrings;
  
  /**
   * An integer to keep track of where we are
   */
  protected int cardPosition;
  
  /**
   * OnClickListener for the buzzer button
   */
  private OnClickListener BuzzListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
        MediaPlayer mp = MediaPlayer.create( v.getContext(), R.raw.buzzer );
        mp.start();
      }
  };
  
  /**
   * constant for the logging tag
   */
  //private static final String TAG = "taboozle";

  /**
   * Function for changing the currently viewed card. It does a bit of bounds
   * checking.
   */
  protected void showCard()
  {
    if( this.cardPosition >= this.cardStrings.size() ||
        this.cardPosition < 0 )
    {
      this.cardPosition = 0;
    }
    TextView cardTitle = (TextView) this.findViewById( R.id.CardTitle );
    ListView cardWords = (ListView) this.findViewById( R.id.CardWords );
    // Disable the ListView to prevent its children from being clickable
    cardWords.setEnabled(false);
    ArrayAdapter<String> cardAdapter = 
      new ArrayAdapter<String>( this, R.layout.word );
    CardStrings curCard = this.cardStrings.get( this.cardPosition );
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
        cardPosition++;
        showCard();
      }
  };
  
  private OnClickListener SkipListener = new OnClickListener() 
  {
      public void onClick(View v) 
      {
        cardPosition--;
        if( cardPosition < 0 )
        {
          cardPosition = cardStrings.size()-1;
        }
        showCard();
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

    this.cardStrings = new ArrayList<CardStrings>();
    this.cardPosition = 0;
      
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
    
    // Setup the view
    this.setContentView( R.layout.main );

    // Iterate through cursor transfering from database to memory
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
        
        this.cardStrings.add( cWords );
      }
      while( cur.moveToNext() );
    }
    
    this.showCard();
    
    ImageButton buzzerButton = (ImageButton)this.findViewById( R.id.BuzzerButton );
    buzzerButton.setOnClickListener( BuzzListener );
    
    ImageButton nextButton = (ImageButton)this.findViewById( R.id.CorrectButton );
    nextButton.setOnClickListener( CorrectListener );
    
    Button skipButton = (Button)this.findViewById( R.id.SkipButton );
    skipButton.setOnClickListener( SkipListener );
  }
}
