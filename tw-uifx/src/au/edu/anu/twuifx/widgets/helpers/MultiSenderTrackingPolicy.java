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

import au.edu.anu.omhtk.util.IntegerRange;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.util.ArrayList;
import java.util.List;

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
