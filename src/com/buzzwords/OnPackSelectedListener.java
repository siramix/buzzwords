/*****************************************************************************
 *  PhraseCraze is a family friendly word game for mobile phones.
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

/**
 * Interface definition for a callback to be invoked when the PackRow layout
 * detects a pack is added or removed from the deck.
 * 
 * @author Siramix Labs
 * 
 */
public abstract class OnPackSelectedListener {

  /*
   * Empty Constructor
   */
  OnPackSelectedListener() {

  }

  /*
   * Override to handle when a pack is added or removed from the deck
   * 
   * @param Pack pack - the pack that was selected
   * @param boolean isPackSelected - the new status of the pack
   */
  public abstract void onPackSelected(Pack pack, boolean isPackSelected);

}
