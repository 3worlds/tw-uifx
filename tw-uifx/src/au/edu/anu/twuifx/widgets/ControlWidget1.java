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
package au.edu.anu.twuifx.widgets;

import java.util.*;

import au.edu.anu.twcore.ui.runtime.*;
import au.edu.anu.twuifx.images.Images;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.*;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static au.edu.anu.twcore.ui.runtime.StatusWidget.*;

/**
 * A Simple {@link StateMachineController} for the {@link StateMachineEngine} to
 * Start, Pause/Continue and Stop a simulation experiment.
 * <p>
 * <img src="{@docRoot}/../doc/images/ControlWidget1.png" width="100" alt=
 * "ControlWidget1"/>
 *
 * @author Ian Davies - 2 Sep 2019
 */
public class ControlWidget1 extends ControllerAdapter implements WidgetGUI {
	private Button btnRunPause;
	private Button btnStep;
	private Button btnReset;
	private List<Button> buttons;
	private ImageView runGraphic;
	private ImageView pauseGraphic;

	/**
	 * @param observed The {@link StateMachineController}.
	 */
	public ControlWidget1(StateMachineEngine<StateMachineController> observed) {
		super(observed);
	}

	@Override
	public Object getUserInterfaceContainer() {
		runGraphic = new ImageView(new Image(Images.class.getResourceAsStream("Play16.gif")));
		pauseGraphic = new ImageView(new Image(Images.class.getResourceAsStream("Pause16.gif")));
		btnRunPause = new Button("", runGraphic);
		btnRunPause.setTooltip(new Tooltip("Run/Pause simulation"));
		btnStep = new Button("", new ImageView(new Image(Images.class.getResourceAsStream("StepForward16.gif"))));
		btnStep.setTooltip(new Tooltip("Step forward one timer event"));
		btnReset = new Button("", new ImageView(new Image(Images.class.getResourceAsStream("Stop16.gif"))));
		btnReset.setTooltip(new Tooltip("Reset to start"));
		btnReset.setDisable(true);

		buttons = new ArrayList<>();
		buttons.add(btnRunPause);
		buttons.add(btnStep);
		buttons.add(btnReset);

		btnRunPause.setOnAction(e -> handleRunPausePressed());
		btnStep.setOnAction(e -> handleStepPressed());
		btnReset.setOnAction(e -> handleResetPressed());

		HBox pane = new HBox();
		pane.setAlignment(Pos.BASELINE_LEFT);
		pane.getChildren().addAll(buttons);

		pane.setSpacing(5.0);

		nullButtons();

		getPreferences();

		return pane;
	}

	private void handleResetPressed() {
		nullButtons();
		sendEventThreaded(reset.event());
	}

	private void handleStepPressed() {
		nullButtons();
		sendEventThreaded(step.event());
	}

	private void handleRunPausePressed() {
		nullButtons();
		State state = stateMachine().getCurrentState();
		Event event = null;
		if (isSimulatorState(state, waiting))
			event = run.event();
		else if (isSimulatorState(state, running))
			event = pause.event();
		else if (isSimulatorState(state, pausing) || isSimulatorState(state, stepping))
			event = goOn.event();
		if (event != null)
			sendEventThreaded(event);
	}

	@Override
	public void onStatusMessage(State state) {
		setButtonLogic(state);
	}

	private void setButtonLogic(State state) {
		if (isSimulatorState(state, waiting)) {
			setButtons(false, false, true, runGraphic);
			return;
		} else if (isSimulatorState(state, running)) {
			setButtons(false, true, true, pauseGraphic);
			return;
		} else if (isSimulatorState(state, stepping)) {
			setButtons(false, false, false, runGraphic);
			return;
		} else if (state.getName().equals(finished.name())) {
			setButtons(true, true, false, runGraphic);
			return;
		} else if (isSimulatorState(state, pausing)) {
			setButtons(false, false, false, runGraphic);
			return;
		}
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}


	@Override
	public void setProperties(String id, SimplePropertyList properties) {
	}

	private void nullButtons() {
		setButtons(true, true, true, null);
	}

	private void setButtons(boolean runPauseDisable, boolean stepDisable, boolean resetDisable, ImageView iv) {
		Platform.runLater(() -> {
			btnRunPause.setDisable(runPauseDisable);
			btnStep.setDisable(stepDisable);
			btnReset.setDisable(resetDisable);
			if (iv != null)
				btnRunPause.setGraphic(iv);
		});
	}

	@Override
	public void putPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPreferences() {
		// TODO Auto-generated method stub
		
	}
}
