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
public class HLSimpleControlWidget extends StateMachineController implements Widget,Kicker{

//	private Button btnRunPause;
//	private Button btnStep;
//	private Button btnReset;
//	private List<Button> buttons;
//	private ImageView runGraphic;
//	private ImageView pauseGraphic;


	private static Logger log = Logging.getLogger(HLSimpleControlWidget.class);

	// NB initial state is always 'waiting' ('null' causes a crash)
	private String state = waiting.name();

	public HLSimpleControlWidget(StateMachineEngine<StateMachineController> observed) {
		super(observed);
		log.info("Thread: " + Thread.currentThread().getId());
	}

//	@Override
//	public Object getUserInterfaceContainer() {
//		// log.info("Thread: " + Thread.currentThread().getId());
//		runGraphic = new ImageView(new Image(Images.class.getResourceAsStream("Play16.gif")));
//		pauseGraphic = new ImageView(new Image(Images.class.getResourceAsStream("Pause16.gif")));
//		btnRunPause = new Button("", runGraphic);
//		btnRunPause.setTooltip(new Tooltip("Run/Pause simulation"));
//		btnStep = new Button("", new ImageView(new Image(Images.class.getResourceAsStream("StepForward16.gif"))));
//		btnStep.setTooltip(new Tooltip("Step forward one timer event"));
//		btnReset = new Button("", new ImageView(new Image(Images.class.getResourceAsStream("Stop16.gif"))));
//		btnReset.setTooltip(new Tooltip("Reset to start"));
//		btnReset.setDisable(true);
//
//		buttons = new ArrayList<>();
//		buttons.add(btnRunPause);
//		buttons.add(btnStep);
//		buttons.add(btnReset);
//
//		btnRunPause.setOnAction(e -> handleRunPausePressed());
//		btnStep.setOnAction(e -> handleStepPressed());
//		btnReset.setOnAction(e -> handleResetPressed());
//
//		HBox pane = new HBox();
//		pane.setAlignment(Pos.BASELINE_LEFT);
//		pane.getChildren().addAll(buttons);
//			
//		pane.setSpacing(5.0);
//
//		setButtonLogic();
//		return pane;
//	}

	private boolean handleResetPressed() {
		// Always begin by disabling in case the next operation takes a long time
		// log.info("handleResetPressed Thread: " + Thread.currentThread().getId());
//		setButtons(true, true, true, null);
		if (state.equals(pausing.name()) | state.equals(stepping.name()) | state.equals(finished.name())) {
			sendEvent(reset.event());
			return true;
		}
		return false;
	}

	private  boolean handleStepPressed() {
		// log.info("handleStepPressed Thread: " + Thread.currentThread().getId());
//		setButtons(true, true, true, null);
		if (state.equals(pausing.name()) | state.equals(stepping.name()) | state.equals(waiting.name())) {
			sendEvent(step.event());
			return true;
		}
		return false;
	}

	private  boolean handleRunPausePressed() {
		// log.info("handleRunPausePressed Thread: " + Thread.currentThread().getId());
//		setButtons(true, true, true, null);
		Event event = null;
		if (state.equals(waiting.name()))
			event = run.event();
		else if (state.equals(running.name()))
			event = pause.event();
		else if (state.equals(pausing.name()) | state.equals(stepping.name()))
			event = goOn.event();
		if (event != null) {
			sendEvent(event);
			return true;
		}
		return false;
	}

	private long startTime;
	@Override
	public void onStatusMessage(State newState) {
		log.info("Thread: " + Thread.currentThread().getId() + " State: " + newState);
		state = newState.getName();
//		setButtonLogic();
	}

//	private void setButtonLogic() {
//		// ensure waiting for app thread i.e. only needed when 'running'
//		Platform.runLater(() -> {
//			// log.info("setButtonLogic: State: "+ state+", Thread: " +
//			// Thread.currentThread().getId());
//			if (state.equals(waiting.name())) {
//				setButtons(false, false, true, runGraphic);
//				return;
//			}
//			if (state.equals(running.name())) {
//				setButtons(false, true, true, pauseGraphic);
//				return;
//
//			}
//			if (state.equals(stepping.name())) {
//				setButtons(false, false, false, runGraphic);
//				return;
//			}
//			if (state.equals(finished.name())) {
//				setButtons(true, true, false, runGraphic);
//				return;
//			}
//			if (state.equals(pausing.name())) {
//				setButtons(false, false, false, runGraphic);
//				return;
//			}
//		});
//	}

//	@Override
//	public Object getMenuContainer() {
//		return null;
//	}

	@Override
	public void putPreferences() {
	}

	@Override
	public void getPreferences() {
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// how would this know and respond to the sender id
	}

	@Override
	public boolean start() {
		return handleRunPausePressed();
	}

//	private void setButtons(boolean runPauseDisable, boolean stepDisable, boolean resetDisable, ImageView iv) {
//		btnRunPause.setDisable(runPauseDisable);
//		btnStep.setDisable(stepDisable);
//		btnReset.setDisable(resetDisable);
//		if (iv != null)
//			btnRunPause.setGraphic(iv);
//	}

}
