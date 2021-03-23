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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.widgets.helpers.SimCloneWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.DefaultDataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 23 Mar. 2021
 * 
 *       Series (time by Sender) for each simulator
 */
public class TimeWidget2 extends AbstractDisplayWidget<TimeData, Metadata> implements WidgetGUI {
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;
	private final Map<Integer, DoubleDataSet> senderDataSet;
	private XYChart chart;
	private Metadata metadata;
	private List<TimeData> initialData;

	public TimeWidget2(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		senderDataSet = new ConcurrentHashMap<>();
		initialData = new ArrayList<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		timeFormatter.setProperties(id, properties);
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		if (policy.canProcessMetadataMessage(meta)) {
			metadata = meta;
			timeFormatter.onMetaDataMessage(metadata);
		}
	}
	@Override
	public Object getUserInterfaceContainer() {

		getUserPreferences();

		final BorderPane content = new BorderPane();

		final DefaultNumericAxis xAxis = new DefaultNumericAxis("Time", timeFormatter.getSmallest().abbreviation());
		final DefaultNumericAxis yAxis = new DefaultNumericAxis("Sim", "#");
		xAxis.setTickLabelRotation(45);
		chart = new XYChart(xAxis, yAxis);
		chart.setTitle("Stop when: " + metadata.properties().getPropertyValue("StoppingDesc"));
		chart.setLegendVisible(false);
		ErrorDataSetRenderer rndr = new ErrorDataSetRenderer();
		// setup renderer
		rndr.setErrorType(ErrorStyle.NONE);// Default ErrorStyle.ERRORCOMBO
		DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) rndr.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(1);
		chart.getRenderers().add(rndr);
		content.setCenter(chart);

		return content;
	}

	@Override
	public void onDataMessage(TimeData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial))
				initialData.add(data);
			else {
				processDataMessage(data);
			}
		}
	}

	private void processDataMessage(TimeData data) {
		if (senderDataSet.get(data.sender()) == null) {
			DoubleDataSet dds = new DefaultDataSet(Integer.toString(data.sender()));
			senderDataSet.put(data.sender(), dds);
			chart.getDatasets().add(dds);
		}
		final DoubleDataSet ds = senderDataSet.get(data.sender());
		final long time = data.time();
		final int sender = data.sender();
		ds.add(time, sender);
//		Platform.runLater(() -> {
////			System.out.println(time+", "+sender);
//			ds.add(time, sender);
//		});

	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			for (Map.Entry<Integer, DoubleDataSet> entry : senderDataSet.entrySet())
				entry.getValue().clearData();
			for (TimeData data : initialData) {
				processDataMessage(data);
			}
		} else if (isSimulatorState(state, finished)) {
			Platform.runLater(() -> {
				chart.getAxes().forEach((axis) -> {
					axis.forceRedraw();
				});
			});
		}
	}


	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void putUserPreferences() {
	}

	@Override
	public void getUserPreferences() {
	}

}
