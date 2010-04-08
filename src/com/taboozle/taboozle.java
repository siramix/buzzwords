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

/**
 * This is the activity class that kicks off Taboozle
 * @author The Taboozle Team
 */
public class taboozle extends Activity {

  private static final String TAG = "taboozle";
  
  /**
   * onCreate - initializes the activity to display the word you have to cause
   * your team mates to say with the words you cannot say below.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // If no data was given in the intent (because we were started          
    // as a MAIN activity), then use our default content provider.          
    Intent intent = this.getIntent();                                            
    if (intent.getData() == null) {                                         
        intent.setData(Cards.CONTENT_URI);                                  
    }
    
    this.setContentView(R.layout.main);
    ListView cardView = (ListView) this.findViewById(R.id.CardView);
    ArrayAdapter<CharSequence> cardAdapter = ArrayAdapter.createFromResource(
        this, R.array.testCard, R.layout.word);
    cardView.setAdapter(cardAdapter);
    
    ContentResolver resolver = this.getContentResolver();
    ContentValues values = new ContentValues();
    values.put(Cards.TITLE, "Hello");
    values.put(Cards.BAD_WORDS,"HI,GREETING,HOLA,SAY,YOU");
    resolver.insert(Cards.CONTENT_URI, values);
    
    //Form an array specifying which columns to return.
    String[] projection = new String[] {
      Cards.TITLE,
      Cards.BAD_WORDS };
    Cursor cur = resolver.query(Pack.Cards.CONTENT_URI, projection, null, null, 
      null);
    cur.moveToFirst();
    Log.e(TAG,cur.getString(0));
    Log.e(TAG,cur.getString(1));
  }

}