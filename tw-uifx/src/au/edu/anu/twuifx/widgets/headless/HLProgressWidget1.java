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

package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.twuifx.widgets.helpers.SimCloneWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;

/**
 * @author Ian Davies - 7 Sept 2021
 */
public class HLProgressWidget1 extends AbstractDisplayWidget<TimeData, Metadata> implements Widget {
	private int nSenders;
	private final WidgetTimeFormatter timeFormatter;
	private final WidgetTrackingPolicy<TimeData> policy;
	private Metadata msgMetadata;
	private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");


	public HLProgressWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		nSenders = 0;
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		nSenders++;
		if (policy.canProcessMetadataMessage(meta)) {
			msgMetadata = meta;
			timeFormatter.onMetaDataMessage(msgMetadata);
		}
	}

	@Override
	public void onDataMessage(TimeData data) {
//		boolean show = false;
		if (data.time() == 0)
			System.out.println("[" + (data.sender()+1) + "/" + nSenders + "]\tready...");
		else if (data.time() == 1) {
			LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
			String timeTxt = currentDate.format(dtf);
			System.out.println("[" + (data.sender()+1) + "]\trunning...\t" + timeTxt);
		}

	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting) || isSimulatorState(state, finished)) {
			LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
			String timeTxt = currentDate.format(dtf);
			System.out.println(state.getName() + "\t" + timeTxt);
		}
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		timeFormatter.setProperties(id, properties);
		policy.setProperties(id, properties);

	}

}
