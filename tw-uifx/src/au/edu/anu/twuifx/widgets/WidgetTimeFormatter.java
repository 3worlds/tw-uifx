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

import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.ecosystem.runtime.timer.TimeUtil;
import au.edu.anu.twcore.ui.runtime.HeadlessWidget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.twcore.constants.TimeScaleType;
import fr.cnrs.iees.twcore.constants.TimeUnits;

/**
 * @author Ian Davies
 *
 * @date 19 Sep 2019
 */

/*
 * Many (most) widgets need to display the time or received data. This
 * associative class can be added to a widget to handle all that.
 * 
 * NOTE: time is NOT the time from the simulator. It is the time from the time
 * model that is driving the dataTracker and hence the time the associated data
 * was sent.
 */
public class WidgetTimeFormatter implements HeadlessWidget {
	private TimeUnits smallest;
	private TimeUnits largest;
	private TimeScaleType timeScale;
	private List<TimeUnits> units;
	private Long startTime;

	/*
	 * So why do we have the smallest/largest rather than the timeModel's TimeUnit.
	 * Well - I did this long ago for a reason but the reason is not really clear to
	 * me now. We will see - but i think its to factor large time values (a week of
	 * seconds) into something understandable e.g 1 day, 2 hours, 4 minutes and 34
	 * seconds. i.e. the formatter would (could) show "d:1, h:2, m:4, s:34" instead
	 * of 93874 sec
	 * 
	 * So the user can choose and this choice might be a setting from the arch or
	 * user settable here. Probably not here because a change in the config will
	 * mean the preference is not longer valid. So the user could set the largest to
	 * hours to show as "h:26, m:4, s:34" etc
	 * 
	 * Ok so "largest" is the largest (allowed) value the user wants to see.
	 * 
	 * NOTE every timeUnit has an abbreviation for this purpose. To be short, units
	 * such as month and year are m and y but if they not not Gregorian months they
	 * have a tick mark to highlight this. Some of these abbrev are wrong bimonth
	 * should be bmo
	 */
	public String getTimeText(Long time) {
		if (timeScale.equals(TimeScaleType.GREGORIAN)) {
			LocalDateTime presentDate = TimeUtil.longToDate(time, smallest);
			return presentDate.format(TimeUtil.getGregorianFormat(smallest));
		} else {
			return (TimeUtil.formatExactTimeScales(time, units));
		}
	}

	public void onMetaDataMessage(Metadata meta) {
		smallest = (TimeUnits) meta.properties().getPropertyValue(P_TIMELINE_SHORTTU.key());
		largest = (TimeUnits) meta.properties().getPropertyValue(P_TIMELINE_LONGTU.key());
		timeScale = (TimeScaleType) meta.properties().getPropertyValue(P_TIMELINE_SCALE.key());
		startTime = (Long) meta.properties().getPropertyValue(P_TIMELINE_TIMEORIGIN.key());
		units = new ArrayList<>();
		Set<TimeUnits> allowable = TimeScaleType.validTimeUnits(timeScale);
		for (TimeUnits allowed : allowable)
			if (allowed.compareTo(largest) <= 0 && allowed.compareTo(smallest) >= 0)
				units.add(allowed);

		units.sort((first, second) -> {
			return second.compareTo(first);
		});

	}

	public long getInitialTime() {
		return startTime;
	}

	@Override
	public void putPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// TODO Auto-generated method stub

	};

}
