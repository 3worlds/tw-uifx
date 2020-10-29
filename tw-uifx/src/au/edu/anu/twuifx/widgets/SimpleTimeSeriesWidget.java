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
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.twapps.dialogs.Dialogs;
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
import au.edu.anu.twuifx.widgets.helpers.CircularDoubleErrorDataSetResizable;
//import au.edu.anu.twuifx.widgets.helpers.CircularDoubleErrorDataSetResizable;
import au.edu.anu.twuifx.widgets.helpers.SimpleWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.TableViewer;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/**
 * @author Ian Davies
 *
 * @date 29 Oct 2019
 *
 *       Trial of chart-fx based on the "RollingBufferSample"
 */
public class SimpleTimeSeriesWidget extends AbstractDisplayWidget<Output0DData, Metadata> implements WidgetGUI {
	private String widgetId;

	private int bufferCapacity = 1000;
//	private int bufferCapacity;// pref mm/mr or both
	// drop overlayed points
	private int MIN_PIXEL_DISTANCE = 0;
	// private int N_SAMPLES = 3000;// what is this?
	// private int UPDATE_PERIOD = 40; // [ms] is this required?
	// these should be created in metadata msg or getUserInterfaceContainer ?
	// That is, they must be created after fx application thread is running.
//	private Timer timer;// dont need this
	private XYChart chart;
	private Map<String, CircularDoubleErrorDataSet> dataSetMap;
	private Output0DMetadata tsMeta;
	private Metadata metadata;
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;
	private StatisticalAggregatesSet sas;
	private Collection<String> sampledItems;

	public SimpleTimeSeriesWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM0);
		// needs to be thread-safe because chartfx plugins may be looking at chart
		// data?? Not sure this makes sense but seems to work.
		dataSetMap = new ConcurrentHashMap<>();
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		policy.setProperties(id, properties);
		this.widgetId = id;
	}

	// helper for onMetaDataMessage, cf below.
	private void makeChannels(DataLabel dl) {
		if (sas != null) {
			for (StatisticalAggregates sa : sas.values()) {
				String key = sa.name() + DataLabel.HIERARCHY_DOWN + dl.toString();
//				String key = sa.name() + " " + dl.toString();
				CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSetResizable(key, bufferCapacity);
				dataSetMap.put(key, ds);
			}
		} else if (sampledItems != null) {
			for (String si : sampledItems) {
				String key = si + DataLabel.HIERARCHY_DOWN + dl.toString();
//				String key = si + " " + dl.toString();
				CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSetResizable(key, bufferCapacity);
				dataSetMap.put(key, ds);
			}
		} else {
			String key = dl.toString();
			CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSetResizable(key, bufferCapacity);
			dataSetMap.put(key, ds);
		}
	}

	/*-
	 * 1) METADATA
	 * 2) UI CONSTRUCTION
	 * 3) WAITING*/
	@Override
	public void onMetaDataMessage(Metadata meta) {
		metadata = meta;
		tsMeta = (Output0DMetadata) meta.properties().getPropertyValue(Output0DMetadata.TSMETA);
		timeFormatter.onMetaDataMessage(metadata);

		sas = null;
		if (metadata.properties().hasProperty(P_DATATRACKER_STATISTICS.key()))
			sas = (StatisticalAggregatesSet) metadata.properties().getPropertyValue(P_DATATRACKER_STATISTICS.key());
		if (metadata.properties().hasProperty("sample")) {
			StringTable st = (StringTable) metadata.properties().getPropertyValue("sample");
			if (st != null) {
				sampledItems = new ArrayList<>(st.size());
				for (int i = 0; i < st.size(); i++)
					sampledItems.add(st.getWithFlatIndex(i));
			}
		}

		for (DataLabel dl : tsMeta.doubleNames()) {
			makeChannels(dl);
		}
		// normally with statistics there are no int variables
		for (DataLabel dl : tsMeta.intNames())
			makeChannels(dl);

	}
	/*-
	 * Renderers should be in the chart BEFORE dataSets are added to the renderer
	 * otherwise when data is added to a dataset, invalidation events will not
	 * propagate to the chart to force a repaint.
	 
	 (cf RollingBufferSample) N.B. it's important to set secondary axis on the 2nd
	 renderer before adding the renderer to the chart
	 renderer_dipoleCurrent.getAxes().add(yAxis2);
	//	*/
	// TODO then lets at least add a second yaxis.
	// Maybe we could allow up to 4 axes - two on each side
//		} else {

	/*-
	 * 1) METADATA
	 * 2) UI CONSTRUCTION
	 * 3) WAITING*/

	@Override
	public Object getUserInterfaceContainer() {
//		System.out.println("UI CONSTRUCTION");

		BorderPane content = new BorderPane();

		chart = new XYChart(new DefaultNumericAxis("time", "?"), new DefaultNumericAxis("", ""));
		chart.setAnimated(false);
		chart.getPlugins().add(new Zoomer());
		chart.getPlugins().add(new TableViewer());
//		DataPointTooltip() seems to cause concurrent modification error at times.
//		chart.getPlugins().add(new DataPointTooltip());
//		chart.getPlugins().add(new Panner());
//		chart.getPlugins().add(new EditAxis());
		chart.setTitle(widgetId);
		content.setCenter(chart);
		content.setRight(new Label(" "));
		String ylabel = dataSetMap.keySet().iterator().next();
		chart.getYAxis().setName(ylabel);// this requires the javafx application thread

		dataSetMap.entrySet().forEach(entry -> {
			ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
			initErrorDataSetRenderer(renderer);
			chart.getRenderers().add(renderer);
			renderer.getDatasets().add(entry.getValue());
		});

		TimeUnits tu = (TimeUnits) metadata.properties().getPropertyValue(P_TIMEMODEL_TU.key());
		int nTu = (Integer) metadata.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
		chart.getXAxis().setUnit(TimeUtil.timeUnitName(tu, nTu));
		if (dataSetMap.size() > 1)
			chart.setLegendVisible(true);
		else
			chart.setLegendVisible(false);

//		DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
//		System.out.println("initial X [" + chart.getXAxis().getMin() + "," + chart.getXAxis().getMax() + "]");
//		System.out.println("initial Y [" + chart.getYAxis().getMin() + "," + chart.getYAxis().getMax() + "]");

		DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
		xAxis.setTickLabelRotation(45);
//		 Check if data has bounds??
//		System.out.println(metadata.properties().getPropertyValue(dl.toString()+"."+P_FIELD_INTERVAL.key()));

		getUserPreferences();

		return content;
	}

	/*-
	 * 1) METADATA
	 * 2) UI CONSTRUCTION
	 * 3) WAITING*/
	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
//			System.out.println("WAITING");
			// synchronized (this) {// thread-safe because a backlog of onDataMessage may be
			// being processed
			// Platform.runLater(() -> {
//			System.out.println("Before clear X [" + chart.getXAxis().getMin() + "," + chart.getXAxis().getMax() + "]");
//			System.out.println("Before clear Y [" + chart.getYAxis().getMin() + "," + chart.getYAxis().getMax() + "]");

			for (Map.Entry<String, CircularDoubleErrorDataSet> entry : dataSetMap.entrySet()) {
				entry.getValue().reset();
			}
//			Platform.runLater(() -> {
//				chart.getXAxis().set(Double.NaN, Double.NaN);
//				chart.getYAxis().set(Double.NaN, Double.NaN);
				
//				chart.getXAxis().setMax(0.0);
//				chart.getYAxis().setMin(0.0);
//				chart.getYAxis().setMax(0.0);
//				System.out.println("After clear X [" + chart.getXAxis().getMin() + "," + chart.getXAxis().getMax() + "]");
//				System.out.println("After clear Y [" + chart.getYAxis().getMin() + "," + chart.getYAxis().getMax() + "]");
//			});

			// }
		}
	}

	@Override
	public void onDataMessage(final Output0DData data) {
		if (policy.canProcessDataMessage(data)) {
			// needs to be thread-safe because waiting state clears these data
			// synchronized (this) {
//			Platform.runLater(() -> {
				CircularDoubleErrorDataSet dontTouch = dataSetMap.values().iterator().next();

				for (CircularDoubleErrorDataSet ds : dataSetMap.values())
					if (!ds.equals(dontTouch))
						ds.autoNotification().getAndSet(false);

				final double x = data.time();

				String itemId = null;
				if (sas != null)
					itemId = data.itemLabel().getEnd();
				else if (sampledItems != null)
					itemId = data.itemLabel().toString();

				for (DataLabel dl : tsMeta.doubleNames()) {
					String key;
					if (itemId != null)
						key = itemId + DataLabel.HIERARCHY_DOWN + dl.toString();
					else
						key = dl.toString();
					CircularDoubleErrorDataSet ds = dataSetMap.get(key);
					final double y = data.getDoubleValues()[tsMeta.indexOf(dl)];
					final double ey = 1;
//					System.out.println(key + "[" + x + "," + y + "]");
					ds.add(x, y, ey, ey);
				}

				for (DataLabel dl : tsMeta.intNames()) {
					String key;
					if (itemId != null)
						key = itemId + DataLabel.HIERARCHY_DOWN + dl.toString();
					else
						key = dl.toString();
					CircularDoubleErrorDataSet ds = dataSetMap.get(key);
					final double y = data.getIntValues()[tsMeta.indexOf(dl)];
					final double ey = 1;
//					System.out.println(key + "[" + x + "," + y + "]");
					ds.add(x, y, ey, ey);
				}

				for (CircularDoubleErrorDataSet ds : dataSetMap.values())
					if (!ds.equals(dontTouch))
						ds.autoNotification().getAndSet(true);

//			});
			// }

		}
	}

	private void initErrorDataSetRenderer(final ErrorDataSetRenderer r) {
		r.setErrorType(ErrorStyle.NONE);
		r.setDashSize(0);
		r.setPointReduction(true);
		r.setDrawMarker(false);
		DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) r.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(MIN_PIXEL_DISTANCE);
	}

	@Override
	public Object getMenuContainer() {
		Menu mu = new Menu(widgetId);
		MenuItem miEdit = new MenuItem("Edit...");
		mu.getItems().add(miEdit);
		miEdit.setOnAction(e -> edit());
		return mu;
	}

	private void edit() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle(widgetId);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		dialog.initOwner((Window) Dialogs.owner());
		GridPane content = new GridPane();
		content.setVgap(5);
		content.setHgap(3);
		Label lbl = new Label("Buffer capacity");
		Spinner<Integer> spCapacity = new Spinner<>();
		spCapacity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 10000, bufferCapacity));
		spCapacity.setMaxWidth(100);
		spCapacity.setEditable(true);
		content.add(lbl, 0, 0);
		content.add(spCapacity, 1, 0);
		dialog.getDialogPane().setContent(content);
		Optional<ButtonType> result = dialog.showAndWait();
		// Map<String, CircularDoubleErrorDataSet>
		if (result.get().equals(ok)) {
			int v = spCapacity.getValue();
			if (v != bufferCapacity) {
				synchronized (this) {
					bufferCapacity = v;
					for (Map.Entry<String, CircularDoubleErrorDataSet> e : dataSetMap.entrySet()) {
						CircularDoubleErrorDataSetResizable ds = (CircularDoubleErrorDataSetResizable) e.getValue();
						ds.resizeBuffer(bufferCapacity);
					}
				}
			}
		}
	}

	private static final String keyBuffer = "bufferCapacity";

	@Override
	public void putUserPreferences() {
		Preferences.putInt(widgetId + keyBuffer, bufferCapacity);
	}

	@Override
	public void getUserPreferences() {
		bufferCapacity = Preferences.getInt(widgetId + keyBuffer, 1000);
	}

}