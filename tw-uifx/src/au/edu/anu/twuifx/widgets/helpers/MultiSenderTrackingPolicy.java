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
// 
package au.edu.anu.twuifx.widgets.helpers;

import au.edu.anu.omhtk.util.IntegerRange;
import au.edu.anu.twcore.data.runtime.*;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.util.*;

/**
 * A policy allowing multiple simulators to be tracked as set by an optional
 * property in this widgets configuration. The reason for this policy is that
 * many widget UI are not designed to scale to very large number of simulators.
 * 
 * @author Ian Davies - 9 Dec. 2020
 */
public class MultiSenderTrackingPolicy implements WidgetTrackingPolicy<TimeData> {

	private final List<Integer> allowedSimIds;
	private int n;

	/**
	 * Default constructor to maintain a list of allowed simulator ids.
	 */
	public MultiSenderTrackingPolicy() {
		allowedSimIds = new ArrayList<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		n = 1;
		if (properties.hasProperty(P_WIDGET_NSIMS.key()))
			n = (int) properties.getPropertyValue(P_WIDGET_NSIMS.key());
	}

	@Override
	public boolean canProcessDataMessage(TimeData data) {
		return allowedSimIds.contains(data.sender());
	}

	// TODO: There can be gaps in the range!!
	@Override
	public boolean canProcessMetadataMessage(Metadata meta) {
		if (allowedSimIds.size() < n) {
			allowedSimIds.add(meta.sender());
			allowedSimIds.sort((i1, i2) -> i1.compareTo(i2));
			return true;
		}
		allowedSimIds.sort((i1, i2) -> i1.compareTo(i2));
		return false;
	}

	@Override
	public String toString() {
		return allowedSimIds.toString();
	}

	@Override
	public IntegerRange getDataMessageRange() {
		return new IntegerRange(allowedSimIds.get(0), allowedSimIds.get(allowedSimIds.size() - 1));
	}

}
