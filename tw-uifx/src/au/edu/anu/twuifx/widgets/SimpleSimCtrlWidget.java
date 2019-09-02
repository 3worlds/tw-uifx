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
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineObserver;
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
// JG - NB this widget should be called ExperimentControlWidget because it doesnt control the
// simulators directly - this is done by the Experiment Deployer.
public class SimpleSimCtrlWidget extends ControlWidget{
	
	private Button btnRunPause;
	private Button btnStep;
	private Button btnReset;
	private List<Button> buttons;
	private ImageView runGraphic;
	private ImageView pauseGraphic;
	
	// NB initial state is always 'waiting' ('null' causes a crash)
	private String state = waiting.name();

	public SimpleSimCtrlWidget(StateMachineObserver controller) {
		super(controller);
		((ExperimentController)controller).setStatusProcessor(this);
	}

	@Override
	public void setProperties(SimplePropertyList properties) {
		// TODO Auto-generated method stub
		
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
		return pane;
	}

	private Object handleResetPressed() {
		System.out.println("RESET PRESSED");
		// issue command and manage button logic (??)
		controller.sendEvent(reset.event());
		return null;
	}

	private Object handleStepPressed() {
		System.out.println("STEP PRESSED");
		// issue command and manage button logic (??)
		controller.sendEvent(step.event());
		return null;
	}

	private Object handleRunPausePressed() {
		System.out.println("RUN/PAUSE PRESSED");
		if (state.equals(waiting.name())) {
			controller.sendEvent(run.event());
			btnRunPause.setGraphic(pauseGraphic);
		}
		else if (state.equals(running.name())) {
			controller.sendEvent(pause.event());
			btnRunPause.setGraphic(runGraphic);
		}
		else if (state.equals(pausing.name()) | state.equals(stepping.name())) {
			controller.sendEvent(goOn.event());
			btnRunPause.setGraphic(pauseGraphic);
		}
		// toggle these graphics
//		btnRunPause.setGraphic(runGraphic);
//		btnRunPause.setGraphic(pauseGraphic);
		// possibly nice to have run/pause/contine(running)
		// issue command and manage button logic (??)
		return null;
	}

	@Override
	public Object getMenuContainer() {
		//No options here
		return null;
	}

	@Override
	public void reset() {
		// reset simulation(s)? I think this is circular - do nothing
		
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
	public void processStatus(Object status) {
		State st = (State) status;
		state = st.getName();
	}


}
