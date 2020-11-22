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

package au.edu.anu.twuifx.widgets.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import fr.cnrs.iees.properties.SimplePropertyList;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
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

public  class SimpleWidgetTrackingPolicy implements WidgetTrackingPolicy<TimeData>{
	private Integer sender;

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		sender = (Integer) properties.getPropertyValue(P_WIDGET_SENDER.key());		
	}


	@Override
	public boolean canProcessDataMessage(TimeData data) {
		return sender==data.sender();
	}
	@Override
	public boolean canProcessMetadataMessage(Metadata meta) {
		return sender==meta.sender();
	}


	@Override
	public List<Integer> senders() {
		return Collections.unmodifiableList(new ArrayList<Integer>(sender));
	}



}
