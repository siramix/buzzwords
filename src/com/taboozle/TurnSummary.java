package com.taboozle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
/**
 * @author The Taboozle Team
 * This activity class is responsible for summarizing the turn and the hand-off 
 * into the next turn or game end.
 */
public class TurnSummary extends Activity
{

	/**
	  * Watches the button that handles hand-off to the next turn activity.
	  */
	  private OnClickListener NextTurnListener = new OnClickListener() 
	  {
	      public void onClick(View v) 
	      {
	    	  	startActivity(new Intent(Intent.ACTION_RUN, getIntent().getData()));
	      }
	  }; // End NextTurnListener
			
	
/**
* onCreate - initializes the activity to display the results of the turn.
*/
@Override
public void onCreate( Bundle savedInstanceState )
{
	super.onCreate( savedInstanceState );
	// Setup the view
	this.setContentView(R.layout.turnsummary);
	
	ListView list = (ListView) findViewById(R.id.TurnSumCardList);

    TaboozleApplication application = 
        (TaboozleApplication) this.getApplication();
    GameManager game = application.GetGameManager();
	
	LinkedList<Card> cardlist = game.GetCurrentCards();
	ArrayList<HashMap<String, String>> sumrows = new ArrayList<HashMap<String, String>>();
	for( Iterator<Card> it = cardlist.iterator(); it.hasNext(); )
	{
	  Card card = (Card) it.next();
	  HashMap<String, String> map = new HashMap<String, String>();
	  map.put("title", card.getTitle());
	  map.put("rws", Integer.toString(card.getRws()));
	  sumrows.add(map);
	}

	SimpleAdapter mSchedule = new SimpleAdapter(this, sumrows, R.layout.turnsumrow,
	            new String[] {"title", "rws"}, new int[] {R.id.TurnSum_CardTitle, R.id.TurnSum_CardRWS});
	list.setAdapter(mSchedule);

    Button playGameButton = (Button)this.findViewById( R.id.TurnSumNextTurn );
    playGameButton.setOnClickListener( NextTurnListener );
}

}
