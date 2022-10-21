/**************************************************************************
 *  TW-UIFX - ThreeWorlds User-Interface fx                               *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            *
 *                                                                        *
 *  TW-UIFX contains the Javafx interface for ModelMaker and ModelRunner. *
 *  This is to separate concerns of UI implementation and the code for    *
 *  these java programs.                                                  *
 *                                                                        *
 **************************************************************************
 *  This file is part of TW-UIFX (ThreeWorlds User-Interface fx).         *
 *                                                                        *
 *  TW-UIFX is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-UIFX is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-UIFX.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>.                  *
 *                                                                        *
 **************************************************************************/
package au.edu.anu.twuifx.widgets.helpers;

import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
import java.time.LocalDateTime;
import java.util.*;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.ecosystem.runtime.timer.TimeUtil;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;
import fr.cnrs.iees.twcore.constants.*;

/**
 * A widget used to display the time stamp of data received. It is intended that
 * this widget be an associative class of widgets this feature.
 * <p>
 * This widget assumes properties from a simulator's time scale node are present
 * in the meta-data.
 * 
 * @author Ian Davies - 19 Sep 2019
 */

public class WidgetTimeFormatter implements Widget {
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
	 * seconds - whatever the TimeScaleType allows i.e. the formatter would (could)
	 * show "d:1, h:2, min:4, s:34" instead of 93874 sec
	 * 
	 * So the user can choose and this choice might be a setting from the arch or
	 * user settable here. Probably not here because a change in the config will
	 * mean the preference is not longer valid. So the user could set the largest to
	 * hours to show as "h:26, m:4, s:34" etc. Anyway, this widget could have
	 * setting which was to factor or not. If factor then pass the largest allowed
	 * by the TimeScaleType, otherwise pass the smallest? Not sure yet.
	 * 
	 * Ok so "largest" is the largest (allowed) value the user wants to see.
	 * 
	 * NOTE every timeUnit has an abbreviation for this purpose. To be short, units
	 * such as month and year are m and y but if they not Gregorian months they have
	 * a tick mark to highlight this.
	 */
	/**
	 * Translate the simulator time to text using methods from {@link TimeUtil}.
	 * 
	 * @param time Simulator time.
	 * @return Simulator time in text format.
	 */
	public String getTimeText(Long time) {
		if (timeScale.equals(TimeScaleType.GREGORIAN)) {
			LocalDateTime presentDate = TimeUtil.longToDate(time, smallest);
			return presentDate.format(TimeUtil.getGregorianFormat(smallest));
		} else {
			return TimeUtil.formatExactTimeScales(time, units);
		}
	}

	/**
	 * Set class memebers based to timer properties.
	 * 
	 * @param meta The simulators meta-data.
	 */
	public void onMetaDataMessage(Metadata meta) {
		smallest = (TimeUnits) meta.properties().getPropertyValue(P_TIMELINE_SHORTTU.key());
		largest = (TimeUnits) meta.properties().getPropertyValue(P_TIMELINE_LONGTU.key());
		timeScale = (TimeScaleType) meta.properties().getPropertyValue(P_TIMELINE_SCALE.key());
		DateTimeType dtt = (DateTimeType) meta.properties().getPropertyValue(P_TIMELINE_TIMEORIGIN.key());
		startTime = dtt.getDateTime();
		units = new ArrayList<>(timeScale.validTimeUnits(smallest, largest));
	}

	/**
	 * Getter for the initial simulator starting time.
	 * 
	 * @return simulator starting time.
	 */
	public long getInitialTime() {
		return startTime;
	}

	/**
	 * Getter for the smallest unit of time measurement.
	 * 
	 * @return the smallest time unit.
	 */
	public TimeUnits getSmallest() {
		return smallest;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// TODO Seems a wrong design to have this. Does this class need to extned
		// widget?? Some refactoring of interfaces may be needed.

	};

}
