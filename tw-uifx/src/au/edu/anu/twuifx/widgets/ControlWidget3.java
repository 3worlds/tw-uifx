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

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.*;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static au.edu.anu.twcore.ui.runtime.StatusWidget.isSimulatorState;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.AbstractDataTracker;
import au.edu.anu.twcore.ui.runtime.ControllerAdapter;
import au.edu.anu.twcore.ui.runtime.DataReceiver;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.widgets.helpers.SimpleWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.rendezvous.RVMessage;
import fr.cnrs.iees.rvgrid.rendezvous.RendezvousProcess;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineObserver;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * A {@link StateMachineController} for the {@link StateMachineEngine} that also
 * displays a graph of total execution time.
 * <p>
 * <img src="{@docRoot}/../doc/images/ControlWidget3.png" width="250" alt=
 * "ControlWidget3"/>
 * </p>
 * <p>
 * This widget is intended for purpose of analysis of a single model as the
 * reported execution times are reliable when one instance of a simulator is
 * being used.
 * 
 * @author Ian Davies -29 Jan 2020
 */
public class ControlWidget3 extends ControllerAdapter
		implements StateMachineObserver, DataReceiver<TimeData, Metadata>, WidgetGUI {

	private Button btnRunPause;
	private Button btnStep;
	private Button btnReset;
	private List<Button> buttons;
	private ImageView runGraphic;
	private ImageView pauseGraphic;

	private final WidgetTrackingPolicy<TimeData> policy;

	private Label lblRealTime;
	private Label lblDelta;
	private String scText;

	private long startTime;
	private long idleTime;
	private long idleStartTime;
	private long prevDuration;

	private static Logger log = Logging.getLogger(ControlWidget3.class);

	private int BUFFER_CAPACITY = 100;// pref mm/mr or both
	// drop overlayed points
	private int MIN_PIXEL_DISTANCE = 0;
	private XYChart chart;
	private CircularDoubleErrorDataSet dataSet;

	/**
	 * @param observed  The {@link StateMachineController}.
	 */
	public ControlWidget3(StateMachineEngine<StateMachineController> observed) {
		super(observed);
		log.info("Thread: " + Thread.currentThread().getId());
		policy = new SimpleWidgetTrackingPolicy();

		// RV for data messages
		addRendezvous(new RendezvousProcess() {
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type() == AbstractDataTracker.TIME) {
					TimeData data = (TimeData) message.payload();
					onDataMessage(data);
				}
			}
		}, AbstractDataTracker.TIME);
		// RV for metadata messages
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

		HBox topPane = new HBox();
		topPane.setAlignment(Pos.BASELINE_LEFT);
		topPane.getChildren().addAll(buttons);
		lblRealTime = new Label("0");
		lblDelta = new Label("0");

		topPane.setSpacing(5.0);
		topPane.getChildren().addAll(new Label("CPU:"), new Label("\u0394t:"), lblDelta, new Label("\u03A3t:"),
				lblRealTime, new Label("[ms]"), new Label(scText));

		BorderPane content = new BorderPane();
		content.setTop(topPane);
		final DefaultNumericAxis yAxis1 = new DefaultNumericAxis("\u03A3", "ms");
		final DefaultNumericAxis xAxis1 = new DefaultNumericAxis("Step", "");
		xAxis1.setAutoRangeRounding(true);
		xAxis1.setTimeAxis(false);
		xAxis1.invertAxis(false);
		// These numbers can be very large
		xAxis1.setTickLabelRotation(45);

		yAxis1.setForceZeroInRange(true);
		yAxis1.setAutoRangeRounding(true);

		// can't create a chart without axes
		chart = new XYChart(xAxis1, yAxis1);
		chart.setPadding(new Insets(5, 5, 5, 5));
		chart.legendVisibleProperty().set(true);
		chart.setAnimated(false);
		content.setCenter(chart);
		dataSet = new CircularDoubleErrorDataSet("time", BUFFER_CAPACITY);
//		chart.getYAxis().setLabel("duration");
//		chart.getYAxis().setUnit("ms");
		/*
		 * Renderers should be in the chart BEFORE dataSets are added to the renderer
		 * otherwise when data is added to a dataset, invalidation events will not
		 * propagate to the chart to force a repaint.
		 */

		ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
		// initRenderer
		renderer.setErrorType(ErrorStyle.NONE);
		renderer.setDashSize(MIN_PIXEL_DISTANCE);
		renderer.setPointReduction(true);
		renderer.setDrawMarker(false);
		DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) renderer.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(MIN_PIXEL_DISTANCE);

		chart.getRenderers().add(renderer);
		renderer.getDatasets().add(dataSet);

		nullButtons();

		getPreferences();

		return content;
	}

	private void handleResetPressed() {
		nullButtons();
		sendEvent(reset.event());
		Platform.runLater(() -> {
			this.lblRealTime.setText("0");
		});
	}

	private void handleStepPressed() {
		long now = System.currentTimeMillis();
		State state = stateMachine().getCurrentState();
		nullButtons();
		if (isSimulatorState(state, waiting)) {
			startTime = now;
			idleTime = 0;
			sendEvent(step.event());
		} else if (isSimulatorState(state, pausing) || isSimulatorState(state, stepping)) {
			if (idleStartTime > 0)
				idleTime += (now - idleStartTime);
			sendEvent(step.event());
		}
	}

	private void handleRunPausePressed() {
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

	private long getDuration(long now) {
		return now - (startTime + idleTime);
	}

	@Override
	public void onStatusMessage(State state) {
		final long now = System.currentTimeMillis();
		if (isSimulatorState(state, finished)) {
			long duration = getDuration(now);
			final String strDuration = Long.toString(duration);
			long delta = duration - prevDuration;
			final String strDelta = Long.toString(delta);
			prevDuration = duration;

			Platform.runLater(() -> {
				lblRealTime.setText(strDuration);
				lblDelta.setText(strDelta);
			});
		}
		if (isSimulatorState(state, stepping)) {
			idleStartTime = now;
		}
		if (isSimulatorState(state, pausing)) {
			idleStartTime = now;
		}
		if (isSimulatorState(state, waiting)) {
			idleTime = 0;
			idleStartTime = 0;
			prevDuration = 0;
			startTime = 0;
			dataSet.reset();
			Platform.runLater(() -> {
				lblRealTime.setText("0");
				lblDelta.setText("0");
			});
		}
		setButtonLogic(state);
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
	}

	@Override
	public void putPreferences() {
	}

	@Override
	public void getPreferences() {
	}

	private void setButtonLogic(State state) {

		Platform.runLater(() -> {
			log.info("setButtonLogic: " + state);
			if (isSimulatorState(state, waiting)) {
				setButtons(false, false, true, runGraphic);
				return;
			} else if (isSimulatorState(state, running)) {
				setButtons(false, true, true, pauseGraphic);
				return;
			} else if (isSimulatorState(state, stepping)) {
				setButtons(false, false, false, runGraphic);
				return;
			} else if (isSimulatorState(state, finished)) {
				setButtons(true, true, false, runGraphic);
				return;
			} else if (isSimulatorState(state, pausing)) {
				setButtons(false, false, false, runGraphic);
				return;
			}
		});
	}

	private void nullButtons() {
		setButtons(true, true, true, null);
	}

	private void setButtons(boolean runPauseDisable, boolean stepDisable, boolean resetDisable, ImageView iv) {
		btnRunPause.setDisable(runPauseDisable);
		btnStep.setDisable(stepDisable);
		btnReset.setDisable(resetDisable);
		if (iv != null)
			btnRunPause.setGraphic(iv);
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void onDataMessage(TimeData data) {
		if (policy.canProcessDataMessage(data)) {
			final long duration = getDuration(System.currentTimeMillis());
			final String strDur = Long.toString(duration);
			long delta = duration - prevDuration;
			final String strDelta = Long.toString(delta);
			prevDuration = duration;
			long x = data.time();
			Platform.runLater(() -> {
				lblRealTime.setText(strDur);
				lblDelta.setText(strDelta);
				dataSet.add(x, duration, 1, 1);
			});
		}
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		if (policy.canProcessMetadataMessage(meta))
			scText = "Stop when: " + meta.properties().getPropertyValue("StoppingDesc");

	}

}
