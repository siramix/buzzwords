package com.taboozle;

import android.os.CountDownTimer;
import android.util.Log;

public abstract class PauseTimer
{
  private static final String TAG = "PauseTimer";
  private static final int TICK = 200;
  private boolean timerOn = false;
  private long timeRemaining;
  private CountDownTimer timer;
     
  private class HiddenTimer extends CountDownTimer
  {
  
    public HiddenTimer(long millisInFuture, long countDownInterval)
    {
      super(millisInFuture, countDownInterval);
      Log.d( TAG, "PauseTimer(" + millisInFuture + ", " + countDownInterval + ")" );
    }
    
    @Override
    public void onFinish() 
    {
      PauseTimer.this.onFinish();
    }
    
    @Override
    public void onTick(long millisUntilFinished) 
    {
      Log.d( TAG, "onTick(" + millisUntilFinished + ")");
      PauseTimer.this.timeRemaining = millisUntilFinished;
      PauseTimer.this.onTick();
    }
  }
    
  public PauseTimer(long timeToCount) 
  {
    this.timer = new HiddenTimer(timeToCount, TICK);
    this.timeRemaining = timeToCount;
  }
     
  abstract public void onFinish();
  abstract public void onTick();
  
  public void start()
  {
    this.timerOn = true;
    this.timer.start();
  }
  public void pause()
  {
    this.timerOn = false;
    this.timer.cancel();
  }
  	
  public void resume()
  {
    if(!this.timerOn)
    {
      this.timer = new HiddenTimer(timeRemaining, TICK);
      this.timer.start();
    }
  }
    
  public boolean isPaused()
  {
    return this.timerOn;
  }
  
  public long getTimeRemaining()
  {
    return this.timeRemaining;
  }
}
