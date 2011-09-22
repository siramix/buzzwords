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

/**
 * Listener that can be set on TeamSelectLayouts to fire off an event
 * when a team is added or removed via a TeamSelectLayout.
 * 
 *  @author Siramix Labs
 * 
 */
public abstract class OnTeamAddedListener {

	/* 
	 * Empty Constructor
	 */
	OnTeamAddedListener()
	{
		
	}
	
	/*
	 * Override to handle when a team is added or removed
	 * from a TeamSelectLayout
	 * 
	 * @param t 
	 * 			team that was added
	 * @param teamAdded
	 * 			true if the team was added, false if it was removed
	 */
	public abstract void onTeamAdded(Team t, boolean isTeamAdded);
	
}
