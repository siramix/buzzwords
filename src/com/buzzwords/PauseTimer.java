package com.buzzwords;

import android.os.CountDownTimer;
import android.util.Log;

/* 
 * Adds pause and resume capabilities to CountDownTimer.  Requires implementation of abstract methods
 * for onFinish and onTick.  Assumes 200ms tick time.
 * 
 * @author The BuzzWords Team
 */
public abstract class PauseTimer
{
  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "PauseTimer";
  
  private static final int TICK = 50;
  private boolean timerActive = false;
  private long timeRemaining;
  private CountDownTimer timer;
  
  /*
   * The underlying timer
   */
  private class InternalTimer extends CountDownTimer
  {
  
    public InternalTimer(long millisInFuture, long countDownInterval)
    {
      super(millisInFuture, countDownInterval);
      Log.d ( TAG, "InternalTimer(" + millisInFuture + "," + countDownInterval + ")" );
    }
    
    @Override
    public void onFinish() 
    {
      Log.d( TAG, "onFinish()" );
      PauseTimer.this.onFinish();
      PauseTimer.this.timerActive = false;
    }
    
    @Override
    public void onTick(long millisUntilFinished) 
    {
      //Log.d( TAG, "onTick(" + millisUntilFinished + ")" ); //ticks often, commenting out for cleaner debug output
      PauseTimer.this.timeRemaining = millisUntilFinished;
      PauseTimer.this.onTick();
    }
  }

  /*
   * Create a timer with pause capabilities.  Must be manually started by calling .start()
   */
  public PauseTimer(long timeToCount) 
  {
    Log.d( TAG, "PauseTimer(" + timeToCount + ")" );
    this.timer = new InternalTimer(timeToCount, TICK);
    this.timeRemaining = timeToCount;
  }
     
  /*
   * Called when internal timer finishes
   */
  abstract public void onFinish();
  /*
   * Called when internal timer updates
   */
  abstract public void onTick(); 
  
  /*
   * Start the timer countdown from the initialized time
   */
  public void start()
  {
    Log.d( TAG, "start()" );
    this.timer.start();
    this.timerActive = true;
  }
  
  /*
   * Pause an active timer.  Use resume() to resume.
   */
  public void pause()
  {
    Log.d( TAG, "pause()" );
    if(this.timerActive)
    {
      this.timerActive = false;
      this.timer.cancel();
    }
  }
  
  /*
   * Resume the timer from the time when last paused.
   */  	
  public void resume()
  {
    Log.d( TAG, "resume()" );
    if(!this.timerActive)
    {
      this.timer = new InternalTimer(timeRemaining, TICK);
      this.timer.start();
      this.timerActive = true;
    }
  }

  /*
   * Check if a timer is currently counting down or paused.
   * @return true if timer is counting down (paused).  returns false if paused or already expired
   */   
  public boolean isActive()
  {
    Log.d( TAG, "isActive()" );
    return this.timerActive;
  }
  
  /*
   * Get the time left before this timer expires and calls onFinished()
   * @return long representing milliseconds remaining
   */   
  public long getTimeRemaining()
  {
    //Log.d( TAG, "getTimeRemaining()" ); //ticks often, commenting out for cleaner debug output
    return this.timeRemaining;
  }
}
