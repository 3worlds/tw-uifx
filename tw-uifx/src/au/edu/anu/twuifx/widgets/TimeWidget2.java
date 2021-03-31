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

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.widgets.helpers.SimCloneWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import de.gsi.chart.XYChart;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.AbstractHistogram.HistogramOuterBounds;
import de.gsi.dataset.spi.Histogram;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

import java.util.Arrays;

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
	private int nSenders;
	private XYChart chart;
	private Metadata metadata;
	private Histogram histDataSet;
	private static int MAX_BINS = 2000;
	private Double[] binWeights;

	public TimeWidget2(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		nSenders = 0;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		timeFormatter.setProperties(id, properties);
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
//      Count senders here before policy as only one meta data msg is processed		
		nSenders++;
		if (policy.canProcessMetadataMessage(meta)) {
			metadata = meta;
			timeFormatter.onMetaDataMessage(metadata);
		}
	}

	@Override
	public Object getUserInterfaceContainer() {

		getUserPreferences();

		BorderPane content = new BorderPane();
		content.setTop(new Label("Simulators: " + nSenders + "; Cores: " + Runtime.getRuntime().availableProcessors()
				+ "; Stop: when " + metadata.properties().getPropertyValue("StoppingDesc")));

		chart = new XYChart();
		chart.setLegendVisible(false);
		ErrorDataSetRenderer rndr = new ErrorDataSetRenderer();
		rndr.setDrawBars(true);
		rndr.setDrawMarker(false);
		rndr.setPolyLineStyle(LineStyle.NONE);
		rndr.setBarWidthPercentage(100);
		rndr.setErrorType(ErrorStyle.NONE);

		chart.getRenderers().add(rndr);
		chart.getXAxis().setName("Simulator");
		chart.getXAxis().setUnit("Id");
		chart.getYAxis().setName("Time");
		chart.getYAxis().setUnit(timeFormatter.getSmallest().abbreviation());
		content.setCenter(chart);

		return content;
	}

	@Override
	public void onDataMessage(TimeData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial)) {
			} else {
				processDataMessage(data);
			}
		}
	}

	private void processDataMessage(TimeData data) {
		int bin = histDataSet.findBin(DataSet.DIM_X, data.sender()+0.0001);
//		System.out.println(data.sender() + " -> " + bin);
		histDataSet.addBinContent(bin, binWeights[bin]);

	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			Platform.runLater(() -> {
				// defacto initialisation point
				double minX = 0.0;
				double maxX = nSenders-0.5;
				int nBins = Math.min(MAX_BINS, nSenders);
				histDataSet = new Histogram("", nBins, minX, maxX, HistogramOuterBounds.BINS_ALIGNED_WITH_BOUNDARY);
				int[] spb = new int[histDataSet.getBinCount(DataSet.DIM_X)];// sims per bin
				for (int i = 0; i < nSenders; i++) {
					int bin = histDataSet.findBin(DataSet.DIM_X, i);
					spb[bin]++;
				}
				binWeights = new Double[spb.length];
				for (int i = 0; i < spb.length; i++) {
					if (spb[i] > 0)
						binWeights[i] = 1.0 / (double) spb[i];
					else
						binWeights[i] = 1.0;// over/under flow
				}
//				System.out.println(Arrays.deepToString(binWeights));
				chart.getDatasets().setAll(histDataSet);
			});

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
