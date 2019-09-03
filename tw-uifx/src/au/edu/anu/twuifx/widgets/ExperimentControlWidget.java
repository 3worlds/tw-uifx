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

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twcore.experiment.runtime.ExperimentController;
import au.edu.anu.twcore.ui.runtime.ControlWidget;
import au.edu.anu.twuifx.images.Images;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.Event;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineObserver;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.*;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */
public class ExperimentControlWidget extends ControlWidget {

	private Button btnRunPause;
	private Button btnStep;
	private Button btnReset;
	private List<Button> buttons;
	private ImageView runGraphic;
	private ImageView pauseGraphic;

	// NB initial state is always 'waiting' ('null' causes a crash)
	private String state = waiting.name();

	public ExperimentControlWidget(StateMachineObserver controller) {
		super(controller);
		((ExperimentController) controller).setStatusProcessor(this);
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
		pane.getChildren().addAll(buttons);
		setButtonLogic();
		return pane;
	}

	private Object handleResetPressed() {
		setButtons(true,true,true,null);
		if (state.equals(pausing.name()) | 
			state.equals(stepping.name()) | 
			state.equals(finished.name()))
			controller.sendEvent(reset.event());
		return null;
	}

	private Object handleStepPressed() {
		setButtons(true,true,true,null);
		if (state.equals(pausing.name()) | 
			state.equals(stepping.name()) | 
			state.equals(waiting.name()))
			controller.sendEvent(step.event());
		return null;
	}

	private Object handleRunPausePressed() {
		// TODO see of long step times mean the buttons are not updated in a timely
		// fashion?
		// If so disable all buttons before issuing the event. Safe because we are in
		// the application thread here
		setButtons(true,true,true,null);
		Event event = null;
		if (state.equals(waiting.name()))
			event = run.event();
		else if (state.equals(running.name()))
			event = pause.event();
		else if (state.equals(pausing.name()) | state.equals(stepping.name()))
			event = goOn.event();
		if (event != null)
			controller.sendEvent(event);
		return null;
	}

	@Override
	public void processStatus(Object status) {
		State st = (State) status;
		state = st.getName();

		setButtonLogic();
	}

	private void setButtonLogic() {
		// processStatus is not in the application thread
		Platform.runLater(() -> {
			if (state.equals(waiting.name())) {
				setButtons(false, false, true, runGraphic);
				return;
			}
			if (state.equals(running.name())) {
				setButtons(false, true, true, pauseGraphic);
				return;

			}
			if (state.equals(stepping.name())) {
				setButtons(false, false, false, runGraphic);
				return;
			}
			if (state.equals(finished.name())) {
				setButtons(true, true, false, runGraphic);
				return;
			}
			if (state.equals(pausing.name())) {
				setButtons(false, false, false, runGraphic);
				return;
			}
		});
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void putPreferences() {
	}

	@Override
	public void getPreferences() {
	}

	@Override
	public void setProperties(String id,SimplePropertyList properties) {
	}

	private void setButtons(boolean runPauseDisable, boolean stepDisable, boolean resetDisable, ImageView iv) {
		btnRunPause.setDisable(runPauseDisable);
		btnStep.setDisable(stepDisable);
		btnReset.setDisable(resetDisable);
		if (iv != null)
			btnRunPause.setGraphic(iv);
	}

}
