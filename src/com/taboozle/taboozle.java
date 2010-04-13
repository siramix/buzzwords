package com.taboozle;

import com.taboozle.common.Pack;
import com.taboozle.common.Pack.Cards;

import android.app.*;
import android.content.ContentResolver;
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

    // Add content to our content provider
    ContentResolver resolver = this.getContentResolver();

    // Form an array specifying which columns to return.
    String[] projection = new String[] { Cards.TITLE, Cards.BAD_WORDS };

    // Query and print the added card record
    Cursor cur = resolver.query( Pack.Cards.CONTENT_URI, projection, null,
                                 null, null );
        
    // Run the query
    if( cur.moveToFirst() )
    {
      int titleColumn = cur.getColumnIndex( Cards.TITLE );
      int badWordsColumn = cur.getColumnIndex( Cards.BAD_WORDS );

      do
      {
        // Get the field values
        String title = cur.getString( titleColumn );
        String badWords = cur.getString( badWordsColumn );

        // Do something with the values.
        Log.d( TAG, title );
        Log.d( TAG, badWords );
        
        // Setup the main content view and add a card to it from the strings xml
        this.setContentView( R.layout.main );
        TextView cardTitle = (TextView) this.findViewById( R.id.CardTitle );
        cardTitle.setText( title );
        ListView cardWords = (ListView) this.findViewById( R.id.CardWords );
        ArrayAdapter<CharSequence> cardAdapter = 
          ArrayAdapter.createFromResource( this, R.array.testWords, R.layout.word );
        cardWords.setAdapter( cardAdapter );
      }
      while( cur.moveToNext() );
    }
  }
}
