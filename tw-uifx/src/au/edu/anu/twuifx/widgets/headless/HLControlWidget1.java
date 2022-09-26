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

import au.edu.anu.twcore.ecosystem.runtime.simulator.RunTimeId;
import au.edu.anu.twcore.ui.runtime.Kicker;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.*;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static au.edu.anu.twcore.ui.runtime.StatusWidget.*;

// TODO: Move to twcore but note: 
// - not all headless widgets are free of javafx (Color!!!). 
// - Also library models would need to be update!
/**
 * 
 * This controller has no GUI. It sends a run event on start(), a quit event on
 * Finished and writes the instance id and simulation time to stdout.
 * 
 * @author Ian Davies -2 Sep 2019
 */
public class HLControlWidget1 extends StateMachineController implements Widget, Kicker {

//	private static Logger log = Logging.getLogger(HLSimpleControlWidget.class);
	private long startTime;
	private boolean ended;

	/**
	 * @param observed The {@link StatusWidget}.
	 */
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
			// Other widgets will not get the finished message so must also be prepared to
			// tidy-up in quitting state
			sendEvent(quit.event());

			// Does not include tidy-up (quitting state) time taken by other widgets as that
			// occurs in another thread.
			System.out.println("Experiment [done; Instance: " + RunTimeId.runTimeId() + "; Duration: "
					+ (System.currentTimeMillis() - startTime) + " ms]");

			ended = true;
		}
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
	}

}
