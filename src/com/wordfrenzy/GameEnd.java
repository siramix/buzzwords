package com.wordfrenzy;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wordfrenzy.R;
import com.wordfrenzy.WordFrenzyApplication;

/**
 * The GameEnd class is the final screen of the application, called
 * when either the number of turns is up, the time is up, the end game
 * button is clicked, or any other number of ways to end a game.
 *
 * @author WordFrenzy team
 *
 */
public class GameEnd extends Activity implements TextToSpeech.OnInitListener
{

  /**
   * logging tag
   */
  public static final String TAG = "GameEnd";
  
  /**
   * code for checking Text to Speech capabilities
   */
  public static final int MY_DATA_CHECK_CODE = 1234;

  /**
   * Text to speech object to speak the team name
   */
  private TextToSpeech mTts;
 
  /**
   * Winning text to be said when TTS object is called
   */
  private String winningtext = "Tie game";
  
  /**
   * This is a reference to the current game manager
   */
  private GameManager curGameManager;
 
  /**
   * Resources to be retrieved throughout GameEnd display 
   */
  private Resources res;

  /**
   * Animates all views in GameEnd screen in an interesting sequence.
   */
  private void AnimateGameEnd(int numteams)
  {
    // Animate GameOver by sliding it down and fading to some semi-transparency value
    AnimationSet animGameOver = new AnimationSet(false);
    
    TranslateAnimation transGameOver = new TranslateAnimation( 
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, 
        Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    transGameOver.setStartOffset( 500 );
    transGameOver.setDuration( 500 );
    transGameOver.setInterpolator( new AccelerateInterpolator());
    
    // Wait to Fade in Game Over - for drama.
    AlphaAnimation fadeInGameOver = new AlphaAnimation( 0.0f, 1.0f);
    fadeInGameOver.setStartOffset( 500 );
    fadeInGameOver.setDuration( 0 );
    AlphaAnimation fadeOutGameOver = new AlphaAnimation(1.0f, 0.25f);
    fadeOutGameOver.setStartOffset( 500 );
    fadeOutGameOver.setDuration( 1000 );

    animGameOver.addAnimation(transGameOver);
    animGameOver.addAnimation(fadeInGameOver);
    animGameOver.addAnimation(fadeOutGameOver);
    animGameOver.setFillAfter(true);
    animGameOver.setAnimationListener( this.gameOverListener );
    
    RelativeLayout gameOverGroup = (RelativeLayout) this.findViewById( R.id.GameEnd_GameOverGroup);
    gameOverGroup.startAnimation( animGameOver );
    
    
    // Slide in header as GameOver comes to a stop
    TranslateAnimation transHeader = new TranslateAnimation( 
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, 
        Animation.RELATIVE_TO_PARENT, -0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
    transHeader.setStartOffset(500);
    transHeader.setDuration(500);
    transHeader.setInterpolator( new AccelerateInterpolator());
    transHeader.setFillBefore(true);
    
    RelativeLayout header = (RelativeLayout) this.findViewById( R.id.EndGame_HeaderGroup);
    header.startAnimation( transHeader );
    
    // Translate scoreboard with header
    LinearLayout scoreboard = (LinearLayout) this.findViewById(R.id.EndGame_FinalStandings);
    scoreboard.startAnimation( transHeader);
    
    // Animate buttons to fade in as scoreboard translates
    AlphaAnimation fadeInButtons = new AlphaAnimation(0.0f, 1.0f);
    fadeInButtons.setStartOffset( 1000 );
    fadeInButtons.setDuration( 500 );
    fadeInButtons.setAnimationListener( this.buttonFadeListener);
    Button tempButton = (Button) this.findViewById( R.id.EndGameMainMenu);
    tempButton.startAnimation( fadeInButtons );
    tempButton = (Button) this.findViewById( R.id.EndGameRematch);
    tempButton.startAnimation( fadeInButtons );

    
    // Slide in panels one at a time ( could do this in some sort of loop... )
    final int PANEL_DELAY = 250;
    TranslateAnimation transPanel4 = new TranslateAnimation( 
        Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, 
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    long offset = 1500;
    transPanel4.setStartOffset( offset );
    transPanel4.setDuration(250);
    transPanel4.setFillBefore(true);
    transPanel4.setInterpolator(new DecelerateInterpolator());
    RelativeLayout panel4 = (RelativeLayout) this.findViewById( R.id.GameEnd_Scores_4);
    panel4.startAnimation( transPanel4 );
    
    TranslateAnimation transPanel3 = new TranslateAnimation( 
        Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, 
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    // set offset based on previous element
    if (numteams >= 3)
    {
      offset += (transPanel4.getDuration() + PANEL_DELAY );
    }
    else
    {
      // Slide in with panel3 if this is an invalid team
      offset = transPanel4.getStartOffset();
    }
    transPanel3.setStartOffset( offset );
    transPanel3.setDuration(250);
    transPanel3.setFillBefore(true);
    transPanel3.setInterpolator(new DecelerateInterpolator());
    RelativeLayout panel3 = (RelativeLayout) this.findViewById( R.id.GameEnd_Scores_3);
    panel3.startAnimation( transPanel3 );
    
    TranslateAnimation transPanel2 = new TranslateAnimation( 
        Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, 
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    offset = transPanel3.getStartOffset() + transPanel3.getDuration() + PANEL_DELAY;
    transPanel2.setStartOffset( offset );
    transPanel2.setDuration(250);
    transPanel2.setFillBefore(true);
    transPanel2.setInterpolator(new DecelerateInterpolator());
    RelativeLayout panel2 = (RelativeLayout) this.findViewById( R.id.GameEnd_Scores_2);
    panel2.startAnimation( transPanel2 );
    
    TranslateAnimation transPanel1 = new TranslateAnimation( 
        Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, 
        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
    offset = transPanel2.getStartOffset() + transPanel2.getDuration() + PANEL_DELAY;
    transPanel1.setStartOffset( offset );
    // Suspense on final animation
    transPanel1.setDuration(250);
    transPanel1.setFillBefore(true);
    transPanel1.setInterpolator(new DecelerateInterpolator());

    RelativeLayout panel1 = (RelativeLayout) this.findViewById( R.id.GameEnd_Scores_1);
    panel1.startAnimation( transPanel1 );
    
    
    // Show winner only at end
    AlphaAnimation fadeInWinner = new AlphaAnimation(0.0f, 1.0f);
    offset = transPanel1.getStartOffset() + transPanel1.getDuration() ;
    fadeInWinner.setStartOffset( offset );
    fadeInWinner.setDuration( 200 );
    fadeInWinner.setFillBefore( true );
    
    TextView winner = (TextView) this.findViewById( R.id.GameEnd_WinnerText);
    winner.startAnimation(fadeInWinner);
        
  }
  
  /**
   * Setup the sounds to go with the animation
   */
  private final AnimationListener gameOverListener = new AnimationListener()
  {
    public void onAnimationEnd( Animation animation )
    {
    }

    public void onAnimationRepeat( Animation animation )
    { 
    }

    public void onAnimationStart( Animation animation )
    {
      // Play win sound
      WordFrenzyApplication app = (WordFrenzyApplication) GameEnd.this.getApplication();
      SoundManager sound = app.GetSoundManager();
      sound.PlaySound( SoundManager.SOUND_WIN );     
    }
  };

  
  /**
   * Make buttons usable once faded in
   */
  private final AnimationListener buttonFadeListener = new AnimationListener()
  {
    public void onAnimationEnd( Animation animation )
    {     
      // Fire off an intent to check if a TTS engine is installed, it will 
      // simultaneously speak the words outloud onInit
      Intent checkIntent = new Intent();
      checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
      startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
      
      // Make buttons usable
      Button mainmenuButton = (Button) GameEnd.this.findViewById( R.id.EndGameMainMenu );
      Button rematchButton = (Button) GameEnd.this.findViewById( R.id.EndGameRematch );
      mainmenuButton.setClickable(true);
      rematchButton.setClickable(true);
    }

    public void onAnimationRepeat( Animation animation )
    { 
    }

    public void onAnimationStart( Animation animation )
    {
    }
  };
  
  /**
   * Listener for the 'Main Menu' button. Sends user back to the main screen on click.
   */
  private final OnClickListener MainMenuListener = new OnClickListener()
  {
    public void onClick(View v)
    {
      Log.d( TAG, "MainMenuListener onClick()" );
      
      WordFrenzyApplication application = (WordFrenzyApplication) GameEnd.this.getApplication();
      
      // Play confirm sound
      SoundManager sound = application.GetSoundManager();
      sound.PlaySound( SoundManager.SOUND_CONFIRM );
      
      Intent clearStackIntent = new Intent(getApplication().getString( R.string.IntentTitle ), getIntent().getData());
      clearStackIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
      startActivity( clearStackIntent );
    }
  }; // End MainMenuListener

  /**
   * Listener for the 'Rematch' button. Starts a new game with same team names.
   */
  private final OnClickListener RematchListener = new OnClickListener()
  {
      public void onClick(View v)
      {
        Log.d( TAG, "RematchListener onClick()" );
        
        WordFrenzyApplication application =
          (WordFrenzyApplication) GameEnd.this.getApplication();
        
        GameManager curgm = application.GetGameManager();
        GameManager newgm = new GameManager(GameEnd.this);
        newgm.StartGame( curgm.GetTeams(), curgm.GetNumRounds() );
        application.SetGameManager( newgm );
        
        Intent clearStackIntent = new Intent(getApplication().getString( R.string.IntentTurn ), getIntent().getData());
        clearStackIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( clearStackIntent );    
      }
  }; // End MainMenuListener
  
    /**
     * GameEnd on create handles all logic, including calls to query the db, populate
     * views, and display them.
     */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
      Log.d( TAG, "onCreate()" );
  		super.onCreate(savedInstanceState);
  		this.setContentView( R.layout.gameend );
  	    this.res = this.getResources();
  
  		WordFrenzyApplication application =
  			(WordFrenzyApplication) this.getApplication();
  		curGameManager = application.GetGameManager();		

		
  	    List<Team> teams = curGameManager.GetTeams();
  	    
        // Sort the list by scores to determine the winner(s)
        Collections.sort( teams, (Team.TEAMA).new ScoreComparator() );
        
        // Assign rankings to the teams (in worst to best, to match team list)
        int[] rankings = new int[teams.size()];
        int rank = 0;
        rankings[teams.size()-1] = rank;
        for( int i = teams.size()-2 ; i >= 0; --i)
        {
          // Continue to count up, regardless of ties.
          // This gives results such as 1st, 1st, 3rd, 4th instead of 1st, 1st, 2nd, 3rd
          rank++;
          // Stomp rank of tying team with that of the team they tied with (the higher rank)
          if (teams.get(i).getScore() == teams.get(i+1).getScore())
          {
            rankings[i] = rankings[i+1];
          }
          else
          {
            rankings[i] = rank;
          }
        }
        // If the two highest ranks are the same, we have a tie on 1st place
        boolean tieGame = (rankings[rankings.length-1] == rankings[rankings.length-2]);

  	    // Ids for Scoreboard list rows (one per team).
        final int[] TEAM_SCORE_GROUPS = new int[]{ R.id.GameEnd_Scores_1, R.id.GameEnd_Scores_2,
            R.id.GameEnd_Scores_3, R.id.GameEnd_Scores_4};  	    
  	    
  	    // Ids for score placement text views.  These should only be changed in the event of ties
        final int[] TEAM_PLACE_IDS = new int[]{ R.id.GameEnd_Scores_1_place, R.id.GameEnd_Scores_2_place,
            R.id.GameEnd_Scores_3_place, R.id.GameEnd_Scores_4_place};  	    
  	    
        // Ids for team names
        final int[] TEAM_NAME_IDS = new int[]{ R.id.GameEnd_Scores_1_name, R.id.GameEnd_Scores_2_name,
            R.id.GameEnd_Scores_3_name, R.id.GameEnd_Scores_4_name};
  	    
        // Ids for score values
  		final int[] TEAM_SCORE_IDS = new int[]{ R.id.GameEnd_Scores_1_score, R.id.GameEnd_Scores_2_score,
  												R.id.GameEnd_Scores_3_score, R.id.GameEnd_Scores_4_score};
  		
        // Ids for scoreboard backgrounds
        final int[] TEAM_SCORE_BGS = new int[]{ R.id.GameEnd_Scores_1_BG, R.id.GameEnd_Scores_2_BG,
                                                R.id.GameEnd_Scores_3_BG, R.id.GameEnd_Scores_4_BG};
        
        // Ids for scoreboard background end elements
        final int[] TEAM_SCORE_ENDS = new int[]{ R.id.GameEnd_Scores_1_end, R.id.GameEnd_Scores_2_end,
                                                R.id.GameEnd_Scores_3_end, R.id.GameEnd_Scores_4_end};
        
        final String[] RANKS = new String[]{"1st", "2nd", "3rd", "4th"};
        
  		// Setup score displays.  Iterate through all team groups, setting scores for teams that played
  		// and disabling the group for teams that did not play
  		for (int i = 0; i < TEAM_SCORE_GROUPS.length; i++)
  		{
  		  if(i >= teams.size())
  		  {
  		    // Gray out rows for teams that didn't play
            View bg = (View) findViewById( TEAM_SCORE_BGS[i]);
            bg.setBackgroundResource( R.color.gameend_blankrow);
            // Hide place, Hide Name, Hide Score
            TextView text = (TextView) findViewById( TEAM_PLACE_IDS[i]);
            text.setVisibility(View.INVISIBLE);
            text = (TextView) findViewById( TEAM_NAME_IDS[i]);
            text.setVisibility(View.INVISIBLE);
            text = (TextView) findViewById( TEAM_SCORE_IDS[i]);
            text.setVisibility(View.INVISIBLE);
            // Set background of end piece to gray
            Drawable d = getResources().getDrawable(R.drawable.gameend_row_end_blank);
            ImageView end = (ImageView) findViewById(TEAM_SCORE_ENDS[i]);
            end.setImageDrawable(d);
  		  }
  		  else
  		  {
            // team list is sorted lowest score to highest, so we want to add them highest first.
            int teamIndex = ( ( teams.size() - 1 ) - i );
  		    
  		    // Set ranking
  		    TextView text = (TextView) findViewById( TEAM_PLACE_IDS[i]);
  		    //text.setTextColor( res.getColor( teams.get( teamIndex ).getText() ));
  		    text.setText( RANKS[rankings[teamIndex]] );
  		    // Set team name and color
  		    text = (TextView) findViewById( TEAM_NAME_IDS[i]);
            text.setTextColor( res.getColor( teams.get( teamIndex ).getSecondaryColor() ));
            text.setText(teams.get(teamIndex).getName());
            // Set team score and color
            text = (TextView) findViewById( TEAM_SCORE_IDS[i]);
            text.setTextColor( res.getColor( teams.get( teamIndex ).getText() ));
            text.setText(Integer.toString(teams.get(teamIndex).getScore()));
            // Set background color
            View bg = (View) findViewById( TEAM_SCORE_BGS[i]);
            bg.setBackgroundResource( teams.get( teamIndex).getText());
            // Set row end background color
            ImageView end = (ImageView) findViewById(TEAM_SCORE_ENDS[i]);
            Drawable d = getResources().getDrawable( teams.get( teamIndex).getGameEndPiece() );
            end.setImageDrawable(d);
            
  		  }
  		}

  		TextView text = (TextView) findViewById( R.id.GameEnd_WinnerText);
        if( tieGame)
        {
          // Set text to Tie game!
          text.setTextColor( res.getColor( R.color.white ));
          text.setText( "Tie Game!");          
        }
        else
        {
          // Set text to Team X Wins!
          int winnerIndex = teams.size() - 1;
          text.setTextColor( res.getColor( teams.get( winnerIndex ).getText() ));
          text.setText( teams.get( winnerIndex ).getName() + " Wins!");
          this.winningtext = teams.get( winnerIndex ).getName() + " Wins!";
        }
        // set font
        Typeface antonFont = Typeface.createFromAsset(getAssets(), "fonts/Anton.ttf");
        text.setTypeface( antonFont );
        
        // Set font on GameOver text
        text = (TextView) findViewById( R.id.GameEnd_GameOver_Game);
        text.setTypeface( antonFont );
        text = (TextView) findViewById( R.id.GameEnd_GameOver_Over);
        text.setTypeface( antonFont );
        
        // Set onclick listeners for game end buttons
        Button mainMenuButton = (Button)this.findViewById( R.id.EndGameMainMenu );
        Button rematchButton = (Button)this.findViewById( R.id.EndGameRematch );
        mainMenuButton.setOnClickListener( MainMenuListener );
        rematchButton.setOnClickListener( RematchListener );
        // buttons start disabled and get enabled once faded in
        mainMenuButton.setClickable( false );
        rematchButton.setClickable( false );

        
        // Animate the whole thing
        AnimateGameEnd(teams.size());

    }
    
    /**
    * Method handles stopping of any outstanding timers during closing of GameEnd
    */
   @Override
   public void onStop()
   {
     super.onStop();
     Log.d( TAG, "onStop()" );
   }

   /**
    * Method handles stopping of any outstanding timers during closing of GameEnd
    */
   @Override
   public void onDestroy()
   {
     super.onDestroy();
     Log.d( TAG, "onDestroy()" );
     // Don't forget to shutdown!
     if (mTts != null)
     {
         mTts.stop();
         mTts.shutdown();
     }
   }    
    
    /**
     * Handler for key up events
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
      // Make back do nothing on key-up instead of climb the action stack
      if( keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
          && !event.isCanceled() )
        {
        return true;
        }

      return super.onKeyUp(keyCode, event);
    }
    
    /**
     * Executed when a new TTS is instantiated. 
     * This is where the winner is announced.
     * @param i
     */
    public void onInit(int i)
    {
      if ( mTts.isLanguageAvailable(Locale.getDefault()) == TextToSpeech.LANG_AVAILABLE || 
           mTts.isLanguageAvailable(Locale.getDefault()) == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
           mTts.isLanguageAvailable(Locale.getDefault()) == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE )
      {
        mTts.setLanguage(Locale.getDefault());
        mTts.speak(this.winningtext, TextToSpeech.QUEUE_FLUSH, null);
      }
    }
    
    /**
     * This is the callback from the TTS engine check, if a TTS is installed we
     * create a new TTS instance (which in turn calls onInit), if not then we will
     * create an intent to go off and install a TTS engine
     * @param requestCode int Request code returned from the check for TTS engine.
     * @param resultCode int Result code returned from the check for TTS engine.
     * @param data Intent Intent returned from the TTS check.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == MY_DATA_CHECK_CODE)
        {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                // success, create the TTS instance
                mTts = new TextToSpeech(this, this);                
            }
            // we don't want to worry about installing if their phone doesn't support TTS
            else 
            {
              // Android docs included installation intent here.  I don't think we
              // should bother for this one line.  Just don't play the text if their phone
              // won't be able to.
            }
        }
    }
    
}
