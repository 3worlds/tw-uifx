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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.DataReceiver;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.images.Images;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.rendezvous.RVMessage;
import fr.cnrs.iees.rvgrid.rendezvous.RendezvousProcess;
import fr.cnrs.iees.rvgrid.statemachine.Event;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineObserver;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * @author Ian Davies
 *
 * @date 29 Jan 2020
 * 
 *       Plots run time output.
 */
public class SimpleControlWidget2 extends StateMachineController
		implements StateMachineObserver, DataReceiver<TimeData, Metadata>, WidgetGUI {

	private Button btnRunPause;
	private Button btnStep;
	private Button btnReset;
	private List<Button> buttons;
	private ImageView runGraphic;
	private ImageView pauseGraphic;

	private WidgetTrackingPolicy<TimeData> policy;
	// NB initial state is always 'waiting' ('null' causes a crash)
	private String state = waiting.name();

	private Label lblRealTime;
	private Label lblDelta;
	private long startTime;
	private long idleTime;
	private long idleStartTime;
	private long prevDuration;

	private static Logger log = Logging.getLogger(SimpleControlWidget2.class);

	private int BUFFER_CAPACITY = 100;// pref mm/mr or both
	// drop overlayed points
	private int MIN_PIXEL_DISTANCE = 0;
	private XYChart chart;
	private CircularDoubleErrorDataSet dataSet;

	public SimpleControlWidget2(StateMachineEngine<StateMachineController> observed) {
		super(observed);
		log.info("Thread: " + Thread.currentThread().getId());
		policy = new SimpleWidgetTrackingPolicy();

		// RV for data messages
		addRendezvous(new RendezvousProcess() {
			@SuppressWarnings("unchecked")
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type() == DataMessageTypes.TIME) {
					TimeData data = (TimeData) message.payload();
					onDataMessage(data);
				}
			}
		}, DataMessageTypes.TIME);
		// RV for metadata messages
		addRendezvous(new RendezvousProcess() {
			@SuppressWarnings("unchecked")
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type() == DataMessageTypes.METADATA) {
					Metadata meta = (Metadata) message.payload();
					onMetaDataMessage(meta);
				}
			}
		}, DataMessageTypes.METADATA);

	}

	@Override
	public Object getUserInterfaceContainer() {
		// log.info("Thread: " + Thread.currentThread().getId());
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
				lblRealTime, new Label("[ms]"));

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
		setButtonLogic();
		return content;
	}

	private Object handleResetPressed() {
		log.info("Thread: " + Thread.currentThread().getId());

		// Always begin by disabling in case the next operation takes a long time
		// log.info("handleResetPressed Thread: " + Thread.currentThread().getId());
		setButtons(true, true, true, null);
		if (state.equals(pausing.name()) | state.equals(stepping.name()) | state.equals(finished.name())) {
			Platform.runLater(() -> {
				this.lblRealTime.setText("0");
			});
			sendEvent(reset.event());
		}
		return null;
	}

	private Object handleStepPressed() {
		long now = System.currentTimeMillis();
		if (state.equals(waiting.name())) {
			startTime = now;
			idleTime = 0;
		}
		log.info("Thread: " + Thread.currentThread().getId());
		setButtons(true, true, true, null);
		if (state.equals(pausing.name()) | state.equals(stepping.name()) | state.equals(waiting.name())) {
			if (idleStartTime > 0)
				idleTime += (now - idleStartTime);
			sendEvent(step.event());
		}
		return null;
	}

	private Object handleRunPausePressed() {
		long now = System.currentTimeMillis();
		log.info("Thread: " + Thread.currentThread().getId());
		setButtons(true, true, true, null);
		Event event = null;
		if (state.equals(waiting.name())) {
			startTime = now;
			idleTime = 0;
			event = run.event();
		} else if (state.equals(running.name())) {
			event = pause.event();
		} else if (state.equals(pausing.name()) | state.equals(stepping.name())) {
			// total idleTime here
			if (idleStartTime > 0)
				idleTime += (now - idleStartTime);
			event = goOn.event();
		}
		if (event != null)
			sendEvent(event);
		return null;
	}

	private long getDuration(long now) {
		return now - (startTime + idleTime);
	}

	@Override
	public void onStatusMessage(State newState) {
		log.info("Thread: " + Thread.currentThread().getId() + " State: " + newState);
		final long now = System.currentTimeMillis();
		state = newState.getName();
		if (state.equals(finished.name())) {
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
		if (state.equals(stepping.name())) {
			idleStartTime = now;
		}
		if (state.equals(pausing.name())) {
			idleStartTime = now;
		}
		if (state.equals(waiting.name())) {
			idleTime = 0;
			idleStartTime = 0;
			prevDuration = 0;
			startTime=0;
			Platform.runLater(() -> {
				lblRealTime.setText("0");
				lblDelta.setText("0");
			});
		}
		setButtonLogic();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putUserPreferences() {
	}

	@Override
	public void getUserPreferences() {
	}

	private void setButtonLogic() {
		// ensure waiting for app thread i.e. only needed when 'running'
		Platform.runLater(() -> {
			// log.info("setButtonLogic: State: "+ state+", Thread: " +
			// Thread.currentThread().getId());
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

	private void setButtons(boolean runPauseDisable, boolean stepDisable, boolean resetDisable, ImageView iv) {
		btnRunPause.setDisable(runPauseDisable);
		btnStep.setDisable(stepDisable);
		btnReset.setDisable(resetDisable);
		if (iv != null)
			btnRunPause.setGraphic(iv);
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub

	}

}
