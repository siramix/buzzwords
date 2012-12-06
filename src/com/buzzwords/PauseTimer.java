/*****************************************************************************
 *  Buzzwords is a family friendly word game for mobile phones.
 *  Copyright (C) 2011 Siramix Team
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.buzzwords;

import android.os.CountDownTimer;

/**
 * Adds pause and resume capabilities to CountDownTimer. Requires implementation
 * of abstract methods for onFinish and onTick. Assumes 200ms tick time.
 * 
 * @author Siramix Labs
 */
public abstract class PauseTimer {
  /**
   * Static string used to refer to this class, in debug output for example.
   */
  private static final String TAG = "PauseTimer";

  /**
   * Tick frequencey of the timer
   */
  private static final int TICK = 200;
  private boolean mTimerActive = false;
  private long mTimeRemaining;
  private CountDownTimer mTimer;

  /*
   * The underlying timer
   */
  private class InternalTimer extends CountDownTimer {

    public InternalTimer(long millisInFuture, long countDownInterval) {
      super(millisInFuture, countDownInterval);
    }

    @Override
    public void onFinish() {
      PauseTimer.this.onFinish();
      PauseTimer.this.mTimerActive = false;
    }

    @Override
    public void onTick(long millisUntilFinished) {
      PauseTimer.this.mTimeRemaining = millisUntilFinished;
      PauseTimer.this.onTick();
    }
  }

  /**
   * Create a timer with pause capabilities. Must be manually started by calling
   * .start()
   */
  public PauseTimer(long timeToCount) {
    this.mTimer = new InternalTimer(timeToCount, TICK);
    this.mTimeRemaining = timeToCount;
  }

  /**
   * Called when internal timer finishes
   */
  abstract public void onFinish();

  /**
   * Called when internal timer updates
   */
  abstract public void onTick();

  /**
   * Start the timer countdown from the initialized time
   */
  public void start() {
    this.mTimer.start();
    this.mTimerActive = true;
  }

  /**
   * Pause an active timer. Use resume() to resume.
   */
  public void pause() {
    SafeLog.d(TAG, "pause()");
    if (this.mTimerActive) {
      this.mTimerActive = false;
      this.mTimer.cancel();
    }
  }

  /**
   * Resume the timer from the time when last paused.
   */
  public void resume() {
    SafeLog.d(TAG, "resume()");
    if (!this.mTimerActive) {
      this.mTimer = new InternalTimer(mTimeRemaining, TICK);
      this.mTimer.start();
      this.mTimerActive = true;
    }
  }

  /**
   * Check if a timer is currently counting down or paused.
   * 
   * @return true if timer is counting down (paused). returns false if paused or
   * already expired
   */
  public boolean isActive() {
    return this.mTimerActive;
  }

  /**
   * Get the time left before this timer expires and calls onFinished()
   * 
   * @return long representing milliseconds remaining
   */
  public long getTimeRemaining() {
    return this.mTimeRemaining;
  }
}
