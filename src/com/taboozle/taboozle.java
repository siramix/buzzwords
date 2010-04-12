package com.taboozle;

import com.taboozle.common.Pack;
import com.taboozle.common.Pack.Cards;

import android.app.*;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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
   * constant for the logging tag
   */
  private static final String TAG = "taboozle";

  /**
   * onCreate - initializes the activity to display the word you have to cause
   * your team mates to say with the words you cannot say below.
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );

    // If no data was given in the intent (because we were started
    // as a MAIN activity), then use our default content provider.
    Intent intent = this.getIntent();
    if( intent.getData() == null )
    {
      intent.setData( Cards.CONTENT_URI );
    }

    // Setup the main content view and add a card to it from the strings xml
    this.setContentView( R.layout.main );
    TextView cardTitle = (TextView) this.findViewById( R.id.CardTitle );
    cardTitle.setText( this.getText(R.string.testTitle) );
    ListView cardWords = (ListView) this.findViewById( R.id.CardWords );
    ArrayAdapter<CharSequence> cardAdapter = ArrayAdapter
                                                         .createFromResource(
                                                                              this,
                                                                              R.array.testWords,
                                                                              R.layout.word );
    cardWords.setAdapter( cardAdapter );

    // Add content to our content provider
    ContentResolver resolver = this.getContentResolver();
    ContentValues values = new ContentValues();
    values.put( Cards.TITLE, "Hello" );
    values.put( Cards.BAD_WORDS, "HI,GREETING,HOLA,SAY,YOU" );
    resolver.insert( Cards.CONTENT_URI, values );

    // Form an array specifying which columns to return.
    String[] projection = new String[] { Cards.TITLE, Cards.BAD_WORDS };

    // Query and print the added card record
    Cursor cur = resolver.query( Pack.Cards.CONTENT_URI, projection, null,
                                 null, null );
    cur.moveToFirst();
    Log.e( TAG, cur.getString( 0 ) );
    Log.e( TAG, cur.getString( 1 ) );
  }

}
