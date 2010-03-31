package com.taboozle;

import com.taboozle.common.Card;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.*;

public class taboozle extends ListActivity {
    /** Called when the activity is first created. */
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