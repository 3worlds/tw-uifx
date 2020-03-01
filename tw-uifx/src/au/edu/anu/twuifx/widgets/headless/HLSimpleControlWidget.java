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

import java.util.logging.Logger;

import au.edu.anu.twcore.ecosystem.runtime.simulator.RunTimeId;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.ui.runtime.Kicker;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.Event;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Logging;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.*;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */
public class HLSimpleControlWidget extends StateMachineController implements Widget, Kicker {

	private static Logger log = Logging.getLogger(HLSimpleControlWidget.class);
	private long startTime;

	public HLSimpleControlWidget(StateMachineEngine<StateMachineController> observed) {
		super(observed);
		// should we be setting the initial state here or something???
		log.fine("Current state: " + stateMachine().getCurrentState());
	}
	@Override
	public boolean start() {
		startTime = System.currentTimeMillis();
		log.fine("Current state: " + stateMachine().getCurrentState());
		sendEvent(run.event());
		return true;

	}

	@Override
	public void onStatusMessage(State state) {
		log.fine("Thread: " + Thread.currentThread().getId() + " State: " + state);
		String stateName = state.getName();
		// close down all threads so app can close cleanly.
		if (stateName.equals(finished.name())) {
			sendEvent(quit.event());
			long endTime = System.currentTimeMillis();
			System.out.println("Simulation finished. [Instance: "+RunTimeId.runTimeId()+"; Duration: "+(endTime-startTime)+" ms]");
		}
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// how would this know and respond to the sender id
	}


}
