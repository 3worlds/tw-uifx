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

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;
import au.edu.anu.twcore.ecosystem.runtime.simulator.Simulator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import au.edu.anu.twcore.data.runtime.*;
import au.edu.anu.twcore.ecosystem.runtime.tracking.AbstractDataTracker;
import au.edu.anu.twcore.ui.runtime.*;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import au.edu.anu.twuifx.widgets.helpers.*;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.*;
import au.edu.anu.twcore.ecosystem.runtime.*;

// TODO This widget needs more testing. Buttons become all disabled at times.
/**
 * A {@link WidgetGUI} to display progress of multiple simulators in a vertical
 * bar chart.
 * <p>
 * This widget also displays the:
 * <li>number of {@link Simulator}s;</li>
 * <li>number of computer cores available;</li>
 * <li>{@link StoppingCondition} - if one is in use;</li>
 * <li>mean time of all simulators;</li>
 * <li>start time of the run;</li>
 * <li>end time of the run;</li>
 * </p>
 * <p>
 * <img src="{@docRoot}/../doc/images/ProgressWidget3.png" width="400" alt=
 * "ProgressWidget3"/>
 * </p>
 * <p>
 * This widget rendezvous with messages of type {@link AbstractDataTracker#TIME}
 * containing {@link TimeData} at a rate depending on the sending
 * {@link Simulator}.
 * 
 * @author Ian Davies - 7 Sept 2021
 */
public class ProgressWidget3 extends AbstractDisplayWidget<TimeData, Metadata> implements WidgetGUI {
	private final WidgetTimeFormatter timeFormatter;
	private final WidgetTrackingPolicy<TimeData> policy;
	private int nSenders;
	private XYChart chart;
	private Metadata metadata;
	private final Map<Integer, Long> currentSenderTimes;
	private final Map<Integer, DoubleDataSet> senderDataSetMap;
	private final long refreshRate;// ms
	private Label lblTime;
	private Label lblStart;
	private Label lblEnd;
	private ErrorDataSetRenderer renderer;
	private Timer timer;
	private long startTime;

	private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");

	/**
	 * @param statusSender The {@link StatusWidget}
	 */
	public ProgressWidget3(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, AbstractDataTracker.TIME);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		nSenders = 0;
		currentSenderTimes = new ConcurrentHashMap<>();
		senderDataSetMap = new ConcurrentHashMap<>();
		refreshRate = 1000;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		timeFormatter.setProperties(id, properties);
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		nSenders++;
		if (policy.canProcessMetadataMessage(meta)) {
			metadata = meta;
		}
	}

	@Override
	public Object getUserInterfaceContainer() {

		getPreferences();//?

		timeFormatter.onMetaDataMessage(metadata);
		lblTime = new Label("");
		VBox vBox = new VBox();
		HBox line1 = new HBox(5);
		HBox line2 = new HBox(5);
		HBox line3 = new HBox(5);
		HBox line4 = new HBox(5);
		HBox line5 = new HBox(5);
		HBox line6 = new HBox(5);

		line1.getChildren().addAll(new Label("Sims:"), new Label(Integer.toString(nSenders)));
		line2.getChildren().addAll(new Label("Cores:"),
				new Label(Integer.toString(Runtime.getRuntime().availableProcessors())));
		line3.getChildren().addAll(new Label("Stop:"),
				new Label((String) metadata.properties().getPropertyValue("StoppingDesc")));
		line4.getChildren().addAll(new Label("Time:"), lblTime);

		lblStart = new Label("");
		line5.getChildren().addAll(new Label("Start:"), lblStart);

		lblEnd = new Label("");

		line6.getChildren().addAll(new Label("≈End: "), lblEnd);

		BorderPane content = new BorderPane();
		vBox.getChildren().addAll(line1, line2, line3, line4, line5, line6);
		content.setTop(vBox);

		DefaultNumericAxis xAxis = new DefaultNumericAxis("Time: ", timeFormatter.getSmallest().abbreviation());
		DefaultNumericAxis yAxis = new DefaultNumericAxis("Simulator");
		chart = new XYChart(xAxis, yAxis);
		chart.setPadding(new Insets(1, 10, 2, 1));

		chart.setLegendVisible(false);
		renderer = new ErrorDataSetRenderer();
		initErrorDataSetRenderer(renderer);
		chart.getRenderers().add(renderer);
		for (int sender = 0; sender < nSenders; sender++) {
			DoubleDataSet ds = new DoubleDataSet(Integer.toString(sender));
			senderDataSetMap.put(sender, ds);
			renderer.getDatasets().add(ds);
		}

		yAxis.setAutoGrowRanging(false);
		yAxis.setForceZeroInRange(true);
		yAxis.setMax(nSenders + 1);
		// yAxis.setMaxMajorTickLabelCount(Math.max(1,nSenders/10));

		content.setCenter(chart);
		return content;
	}

	@Override
	public void onDataMessage(TimeData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial)) {
			} else {
				// Lazy init to avoid a time out while many simulators are loading
				if (timer == null) {
					startTime = System.currentTimeMillis();
					timer = new Timer();
					timer.scheduleAtFixedRate(new TimerTask() {
						private long lastTime = Long.MAX_VALUE;

						@Override
						public void run() {
							long time = Math.round(getMeanTime());
							if (time != lastTime) {
								lastTime = time;
								String text = formatOutput(currentSenderTimes.size(), time);
								Platform.runLater(() -> {
									lblTime.setText(text);
									lblStart.setText(sdf.format(new Date(startTime)));
									if (!currentSenderTimes.isEmpty()) {
										long currentTime = System.currentTimeMillis();
										double meanTimePerSim = (currentTime - startTime) / currentSenderTimes.size();
										double simsRemaining = nSenders - currentSenderTimes.size();
										long timeRemaining = (long) (simsRemaining * meanTimePerSim);
										long endTime = currentTime + timeRemaining;
										lblEnd.setText(sdf.format(new Date(endTime)));
									}
								});
							}
						}
					}, 0, refreshRate);
				}

				processDataMessage(data);
			}
		}
	}

	private void processDataMessage(TimeData data) {
		int sender = data.sender();
		long time = data.time();
		currentSenderTimes.put(sender, time);

		Platform.runLater(() -> {
			DoubleDataSet ds = senderDataSetMap.get(sender);
			ds.add(time, sender + 1);
		});

	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			currentSenderTimes.clear();
			int sender = 0;
			for (DataSet ds : renderer.getDatasets()) {
				DoubleDataSet dds = (DoubleDataSet) ds;
				dds.clearData();
				dds.add(0, sender + 1);
				currentSenderTimes.put(sender, 0L);
				sender++;
			}
		}

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

	private static void initErrorDataSetRenderer(final ErrorDataSetRenderer r) {
		r.setErrorType(ErrorStyle.NONE);
		r.setDashSize(0);
		r.setPointReduction(true);
		r.setDrawMarker(false);
		r.setDrawBars(false);
		final DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) r.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(1);
	}

	private double getMeanTime() {
		double sum = 0;
		double n = currentSenderTimes.size();
		for (Entry<Integer, Long> entry : currentSenderTimes.entrySet())
			sum += entry.getValue();
		if (n > 0)
			return sum / n;
		return timeFormatter.getInitialTime();
	}

	private String formatOutput(int n, long time) {
		return "[#1-" + n + "] " + timeFormatter.getTimeText(time);
	}

}
