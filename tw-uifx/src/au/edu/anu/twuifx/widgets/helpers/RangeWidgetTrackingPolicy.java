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

// 
package au.edu.anu.twuifx.widgets.helpers;

import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import fr.cnrs.iees.properties.SimplePropertyList;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

/**
 * @author Ian Davies
 *
 * @date 9 Dec. 2020
 */
public class RangeWidgetTrackingPolicy implements WidgetTrackingPolicy<TimeData> {

	private IntegerRange range;

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// must be a +ve range
		int l = 0;
		if (properties.hasProperty(P_WIDGET_FIRSTSENDER.key()))
			l = (int) properties.getPropertyValue(P_WIDGET_FIRSTSENDER.key());
		int n = 1;
		if (properties.hasProperty(P_WIDGET_NSENDERS.key()))
			n = (int) properties.getPropertyValue(P_WIDGET_NSENDERS.key());
		range = new IntegerRange(l, l + (n - 1));
	}

	@Override
	public boolean canProcessDataMessage(TimeData data) {
		return range.inRange(data.sender());
	}

	@Override
	public boolean canProcessMetadataMessage(Metadata meta) {
		return range.getFirst() == meta.sender();
	}

	@Override
	public String toString() {
		if (range.getLast() - range.getFirst() == 0)
			return Integer.toString(range.getFirst());
		else
			return Integer.toString(range.getFirst()) + "-" + Integer.toString(range.getLast());
	}

	@Override
	public IntegerRange getDataMessageRange() {
		return range;
	}

}
