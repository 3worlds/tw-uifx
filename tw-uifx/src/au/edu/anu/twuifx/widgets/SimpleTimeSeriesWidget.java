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

import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_NTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_TU;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.data.runtime.Output0DData;
import au.edu.anu.twcore.data.runtime.Output0DMetadata;
import au.edu.anu.twcore.ecosystem.runtime.timer.TimeUtil;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * @author Ian Davies
 *
 * @date 29 Oct 2019
 * 
 *       Trial of chart-fx based on the "RollingBufferSample"
 */
public class SimpleTimeSeriesWidget extends AbstractDisplayWidget<Output0DData, Metadata> implements WidgetGUI {

	private int BUFFER_CAPACITY = 100;// pref mm/mr or both
	// drop overlayed points
	private int MIN_PIXEL_DISTANCE = 0;
	// private int N_SAMPLES = 3000;// what is this?
	// private int UPDATE_PERIOD = 40; // [ms] is this required?
	// these should be created in metadata msg or getUserInterfaceContainer ?
	// That is, they must be created after fx application thread is running.
//	private Timer timer;// dont need this
	private XYChart chart;
	private Map<String, CircularDoubleErrorDataSet> dataSetMap;
	private Output0DMetadata tsmeta;
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;
	private static Logger log = Logging.getLogger(SimpleTimeSeriesWidget.class);

	public SimpleTimeSeriesWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME_SERIES);
		dataSetMap = new HashMap<>();
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		log.info("Thread: " + Thread.currentThread().getId());
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		policy.setProperties(id, properties);
	}

	@Override
	public void putPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getPreferences() {
		// TODO Auto-generated method stub
		// on application thread AFTER getUserInterfaceContainer

	}

	@Override
	public void onDataMessage(final Output0DData data) {
		log.info("Thread: " + Thread.currentThread().getId() + " data: " + data);
		if (policy.canProcessDataMessage(data)) {

			/*
			 * TODO Still some problems with how we manage this.
			 * 
			 */

			Platform.runLater(() -> {
				CircularDoubleErrorDataSet dontTouch = dataSetMap.values().iterator().next();

				for (CircularDoubleErrorDataSet ds : dataSetMap.values())
					if (!ds.equals(dontTouch))
						ds.setAutoNotifaction(false);

				final double x = data.time();
				for (DataLabel dl : tsmeta.doubleNames()) {
					CircularDoubleErrorDataSet ds = dataSetMap.get(dl.toString());
					final double y = data.getDoubleValues()[tsmeta.indexOf(dl)];
					final double ey = 1;
					ds.add(x, y, ey, ey);
				}
				for (DataLabel dl : tsmeta.intNames()) {
					CircularDoubleErrorDataSet ds = dataSetMap.get(dl.toString());
					final double y = data.getIntValues()[tsmeta.indexOf(dl)];
					final double ey = 1;
					ds.add(x, y, ey, ey);
				}

				for (CircularDoubleErrorDataSet ds : dataSetMap.values())
					if (!ds.equals(dontTouch))
						ds.setAutoNotifaction(true);

			});
		}
	}

	private boolean initialMessage = false;

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// clear data from last run - here we could use the lovely 'history' method
		// supplied with chartfx - TODO
		log.info("Thread: " + Thread.currentThread().getId() + " Meta-data: " + meta);
		dataSetMap.entrySet().forEach(entry -> {
			entry.getValue().reset();
		});

		// this occurs EVERY reset so take care not to recreate datasets and renderers
		// etc
		if (!initialMessage) {
			Platform.runLater(() -> {
				tsmeta = (Output0DMetadata) meta.properties().getPropertyValue(Output0DMetadata.TSMETA);
				for (DataLabel dl : tsmeta.doubleNames()) {
					String key = dl.toString();
//				log.info("Tracking: " + key);
					CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSet(key, BUFFER_CAPACITY);
					dataSetMap.put(key, ds);
				}

				for (DataLabel dl : tsmeta.intNames()) {
					String key = dl.toString();
//				log.info("Tracking: " + key);
					CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSet(key, BUFFER_CAPACITY);
					dataSetMap.put(key, ds);
				}
				/*
				 * Renderers should be in the chart BEFORE dataSets are added to the renderer
				 * otherwise when data is added to a dataset, invalidation events will not
				 * propagate to the chart to force a repaint.
				 */
				// (cf RollingBufferSample) N.B. it's important to set secondary axis on the 2nd
				// renderer before adding the renderer to the chart
				// renderer_dipoleCurrent.getAxes().add(yAxis2);
				if (dataSetMap.size() > 1) {
					// TODO then lets at least add a second yaxis.
					// Maybe we could allow up to 4 axes - two on each side
				} else {
					String ylabel = dataSetMap.keySet().iterator().next();
					chart.getYAxis().setLabel(ylabel);
				}

				dataSetMap.entrySet().forEach(entry -> {
					ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
					initErrorDataSetRenderer(renderer);
					chart.getRenderers().add(renderer);
					renderer.getDatasets().add(entry.getValue());
				});
				timeFormatter.onMetaDataMessage(meta);
				TimeUnits tu = (TimeUnits) meta.properties().getPropertyValue(P_TIMEMODEL_TU.key());
				int nTu = (Integer) meta.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
				chart.getXAxis().setUnit(TimeUtil.timeUnitName(tu, nTu));
				// depending on the TU we could decide on xAxis.setMinorTickCount(some even
				// division of the period)
				initialMessage = true;
			});
		}
	}

	private void initErrorDataSetRenderer(final ErrorDataSetRenderer r) {
		r.setErrorType(ErrorStyle.NONE);
		r.setDashSize(MIN_PIXEL_DISTANCE);
		r.setPointReduction(true);
		r.setDrawMarker(false);
		DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) r.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(MIN_PIXEL_DISTANCE);
	}

	@Override
	public void onStatusMessage(State state) {
		// TODO Auto-generated method stub
		// SimulatorStatus.Final;
		// if final push the last data from buffer to chart
	}

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane content = new BorderPane();
		final DefaultNumericAxis yAxis1 = new DefaultNumericAxis("", "");
		final DefaultNumericAxis xAxis1 = new DefaultNumericAxis("time", "?");
		xAxis1.setAutoRangeRounding(true);
		xAxis1.setTimeAxis(false);
		xAxis1.invertAxis(false);
		// These numbers can be very large
		xAxis1.setTickLabelRotation(45);
		// for gregorian we may need something else here
//		xAxis1.setTimeAxis(true);

		yAxis1.setForceZeroInRange(true);
		yAxis1.setAutoRangeRounding(true);

		// can't create a chart without axes
		chart = new XYChart(xAxis1, yAxis1);
		chart.legendVisibleProperty().set(true);
		chart.setAnimated(false);
		content.setCenter(chart);
		content.setRight(new Label(" "));

		return content;
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
	}

}