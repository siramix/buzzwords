package com.taboozle;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.taboozle.common.Card;

/**
 * This is the activity class that kicks off Taboozle
 * @author The Taboozle Team
 */
public class taboozle extends ListActivity {

  /**
   * onCreate - initializes the activity to display the word you have to cause
   * your team mates to say with the words you cannot say below.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Card card = new Card("perfidious",5);
    String[] badWords = {"trust","betray","gossip","liar","backstab"};
    card.AddBadWords(badWords);
    TextView v = new TextView(this);
    v.setText(card.GetName());
    v.setTextSize(50);
    v.setGravity(android.view.Gravity.CENTER);
    this.getListView().addHeaderView(v);
    this.getListView().setHeaderDividersEnabled(true);
    this.setListAdapter(new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1,card.GetBadWords()));
    this.getListView().setClickable(false);
  }

}