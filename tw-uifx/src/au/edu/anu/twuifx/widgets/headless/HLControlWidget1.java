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

import au.edu.anu.twcore.ecosystem.runtime.simulator.RunTimeId;
import au.edu.anu.twcore.ui.runtime.Kicker;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.*;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static au.edu.anu.twcore.ui.runtime.StatusWidget.*;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 * 
 *       This controller has no GUI. It sends a run event on start(), a quit
 *       event on Finished and writes the instance id and simulation time to
 *       stdout.
 */
public class HLControlWidget1 extends StateMachineController implements Widget, Kicker {

//	private static Logger log = Logging.getLogger(HLSimpleControlWidget.class);
	private long startTime;
	private boolean ended;

	public HLControlWidget1(StateMachineEngine<StateMachineController> observed) {
		super(observed);
	}

	@Override
	public boolean start() {
		ended = false;
		sendEvent(initialise.event());
		startTime = System.currentTimeMillis();
//		log.info("Start at "+startTime);
		sendEvent(run.event());
		return true;
	}

	@Override
	public synchronized boolean ended() {
		return ended;
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, finished)) {
			sendEvent(quit.event());
			long endTime = System.currentTimeMillis();
			System.out.println("Experiment [done; Instance: " + RunTimeId.runTimeId() + "; Duration: "
					+ (endTime - startTime) + " ms]");
			ended = true;
		}
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
	}

}
