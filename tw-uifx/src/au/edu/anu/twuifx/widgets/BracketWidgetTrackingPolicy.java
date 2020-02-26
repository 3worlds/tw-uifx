/**************************************************************************
 *  TW-CORE - 3Worlds Core classes and methods                            *
 *                                                                        *
 *  Copyright 2018: Shayne Flint, Jacques Gignoux & Ian D. Davies         *
 *       shayne.flint@anu.edu.au                                          * 
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-CORE is a library of the principle components required by 3W       *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-CORE (3Worlds Core).                          *
 *                                                                        *
 *  TW-CORE is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-CORE is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-CORE.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
 *                                                                        *
 **************************************************************************/

package au.edu.anu.twuifx.widgets;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import au.edu.anu.twcore.data.runtime.TimeData;
import fr.cnrs.iees.properties.SimplePropertyList;
// not sure about this wip
/**
 * @author Ian Davies
 *
 * @date 23 Sep 2019
 */
/*
 * The policy of simple widgets is:
 * 
 * 1) to follow just one sender. That's it! The chosen sender is
 * a sub-archetype property. These widgets will therefore ignore data from
 * other senders.
 * 
 * Each widget should indicate the sender int on the ui.
 * 
 * 
 */

public  class BracketWidgetTrackingPolicy implements WidgetTrackingPolicy<TimeData>{
	private Map<Integer, Long> simTimes = new HashMap<>();
	@Override
	public void setProperties(String id, SimplePropertyList properties) {
				
	}


	@Override
	public boolean canProcessDataMessage(TimeData data) {
		simTimes.put(data.sender(), data.time());
		return true;
	}

	@Override
	public int sender() {
		return 1;
		// stupid flaw
	}

	@Override
	public Collection<Integer> senders() {
		return simTimes.keySet();
	}


}
