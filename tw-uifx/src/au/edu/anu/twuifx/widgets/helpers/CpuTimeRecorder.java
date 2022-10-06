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

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.goOn;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.pause;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.reset;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.run;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.step;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.pausing;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.running;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.stepping;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;
import static au.edu.anu.twcore.ui.runtime.StatusWidget.isSimulatorState;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.AbstractDataTracker;
import au.edu.anu.twcore.ui.runtime.DataReceiver;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.rendezvous.RVMessage;
import fr.cnrs.iees.rvgrid.rendezvous.RendezvousProcess;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineObserver;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 * Possible ancestor of component of other widgets - not implemented (yet?).
 * 
 * @author Ian Davies - 25 Sep. 2022
 *
 */
public class CpuTimeRecorder extends StateMachineController
		implements StateMachineObserver, DataReceiver<TimeData, Metadata>, WidgetGUI {
	private long startTime;
	private long idleTime;
	private long idleStartTime;
	private Button btnRunPause;
	private Button btnStep;
	private Button btnReset;
	private Label lblRealTime;

	/**
	 * @param observed TODO: not implemented
	 */
	public CpuTimeRecorder(StateMachineEngine<StateMachineController> observed) {
		super(observed);
//		new SimpleWidgetTrackingPolicy();

		// Data messages
		addRendezvous(new RendezvousProcess() {
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type() == AbstractDataTracker.TIME) {
					TimeData data = (TimeData) message.payload();
					onDataMessage(data);
				}
			}
		}, AbstractDataTracker.TIME);
		// Metadata messages
		addRendezvous(new RendezvousProcess() {
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type() == AbstractDataTracker.METADATA) {
					Metadata meta = (Metadata) message.payload();
					onMetaDataMessage(meta);
				}
			}
		}, AbstractDataTracker.METADATA);
	}

	protected void handleResetPressed() {
		nullButtons();
		sendEvent(reset.event());
		Platform.runLater(() -> {
			this.lblRealTime.setText("0");
		});
	}

	protected void handleRunPausePressed() {
		long now = System.currentTimeMillis();
		nullButtons();
		State state = stateMachine().getCurrentState();

		if (isSimulatorState(state, waiting)) {
			startTime = now;
			idleTime = 0;
			sendEvent(run.event());
		} else if (isSimulatorState(state, running)) {
			sendEvent(pause.event());
		} else if (isSimulatorState(state, pausing) || isSimulatorState(state, stepping)) {
			// total idleTime here
			if (idleStartTime > 0)
				idleTime += (now - idleStartTime);
			sendEvent(goOn.event());
		}
	}

	protected void handleStepPressed() {
		long now = System.currentTimeMillis();
		State state = stateMachine().getCurrentState();
		nullButtons();
		if (isSimulatorState(state, waiting)) {
			startTime = now;
			idleTime = 0;
			sendEvent(step.event());
		} else if (isSimulatorState(state, pausing) | isSimulatorState(state, stepping)) {
			if (idleStartTime > 0)
				idleTime += (now - idleStartTime);
			sendEvent(step.event());
		}
	}

	protected final long getDuration(long now) {
		return now - (startTime + idleTime);
	}

	protected final void nullButtons() {
		setButtons(true, true, true, null);
	}

	protected final void setButtons(boolean runPauseDisable, boolean stepDisable, boolean resetDisable, ImageView iv) {
		btnRunPause.setDisable(runPauseDisable);
		btnStep.setDisable(stepDisable);
		btnReset.setDisable(resetDisable);
		if (iv != null)
			btnRunPause.setGraphic(iv);
	}

	@Override
	public void onStatusMessage(State state) {

	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
	}

	@Override
	public Object getUserInterfaceContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
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
	public void onDataMessage(TimeData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// TODO Auto-generated method stub

	}

}
