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
package com.buzzwordslite;

import com.buzzwordslite.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {

  private AudioManager mAudioManager;
  private SoundPool mSoundPool;
  private float mVolume;

  public static enum Sound {
    RIGHT, WRONG, SKIP, TEAMREADY, WIN, BACK, CONFIRM, GONG, BUZZ,
  };

  /**
   * Array for storing system sound ids for the sounds loaded into the pool
   */
  private int[] mSoundIds;

  /**
   * Default consructor
   * 
   * @param baseContext
   *          the context in which to initialize all of the system services
   *          needed
   */
  public SoundManager(Context baseContext) {
    mAudioManager = (AudioManager) baseContext
        .getSystemService(Context.AUDIO_SERVICE);
    float streamVolumeCurrent = mAudioManager
        .getStreamVolume(AudioManager.STREAM_MUSIC);
    float streamVolumeMax = mAudioManager
        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    mVolume = streamVolumeCurrent / streamVolumeMax;

    mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);

    // Load all sounds on creation of Sound Manager. Could pull this into a
    // separate function
    // if we don't want these paired.
    mSoundIds = new int[Sound.values().length];
    mSoundIds[Sound.RIGHT.ordinal()] = mSoundPool.load(baseContext,
        R.raw.fx_right, 1);
    mSoundIds[Sound.WRONG.ordinal()] = mSoundPool.load(baseContext,
        R.raw.fx_wrong, 1);
    mSoundIds[Sound.SKIP.ordinal()] = mSoundPool.load(baseContext,
        R.raw.fx_skip, 1);
    mSoundIds[Sound.TEAMREADY.ordinal()] = mSoundPool.load(baseContext,
        R.raw.fx_teamready, 1);
    mSoundIds[Sound.WIN.ordinal()] = mSoundPool.load(baseContext, R.raw.fx_win,
        1);
    mSoundIds[Sound.BACK.ordinal()] = mSoundPool.load(baseContext,
        R.raw.fx_back, 1);
    mSoundIds[Sound.CONFIRM.ordinal()] = mSoundPool.load(baseContext,
        R.raw.fx_confirm, 1);
    mSoundIds[Sound.GONG.ordinal()] = mSoundPool.load(baseContext,
        R.raw.fx_countdown_gong, 1);
    mSoundIds[Sound.BUZZ.ordinal()] = mSoundPool.load(baseContext,
        R.raw.fx_buzzer, 1);
  }

  /**
   * Play a sound once
   * 
   * @param fxIndex
   *          the sound to be played (once)
   * @return the id of the sound in the sound pool
   */
  public int playSound(Sound fxIndex) {
    return mSoundPool.play(mSoundIds[fxIndex.ordinal()], mVolume, mVolume, 1,
        0, 1.0f);
  }

  /**
   * Plays a sound looped
   * 
   * @param fxIndex
   *          the sound to be played FOREVER
   * @return the id of the sound in the sound pool
   */
  public int playLoop(Sound fxIndex) {
    return mSoundPool.play(mSoundIds[fxIndex.ordinal()], mVolume, mVolume, 1,
        -1, 1.0f);
  }

  /**
   * Stop sound playing with the id listed
   * 
   * @param soundId
   */
  public void stopSound(int soundId) {
    mSoundPool.stop(soundId);
  }
}
