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

package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import au.edu.anu.twcore.data.runtime.*;
import au.edu.anu.twcore.ecosystem.runtime.tracking.AbstractDataTracker;
import au.edu.anu.twcore.ui.runtime.*;
import au.edu.anu.twuifx.widgets.helpers.*;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.*;

/**
 * A headless class to report simulation progress to stdout. ,P. It reports the
 * time a simulator ended the first time step and the time all simulators have
 * finished.
 * 
 * @author Ian Davies - 7 Sept 2021
 */
public class HLProgressWidget1 extends AbstractDisplayWidget<TimeData, Metadata> implements Widget {
	private int nSenders;
	private final WidgetTimeFormatter timeFormatter;
	private final WidgetTrackingPolicy<TimeData> policy;
	private Metadata msgMetadata;
	private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

	/**
	 * @param statusSender The {@link StatusWidget}.
	 */
	public HLProgressWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, AbstractDataTracker.TIME);
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
			System.out.println("[" + (data.sender() + 1) + "/" + nSenders + "]\tready...");
		else if (data.time() == 1) {
			LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
			String timeTxt = currentDate.format(dtf);
			System.out.println("[" + (data.sender() + 1) + "]\trunning...\t" + timeTxt);
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
