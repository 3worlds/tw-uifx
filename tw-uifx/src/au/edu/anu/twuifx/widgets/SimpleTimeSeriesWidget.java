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

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import au.edu.anu.twuifx.widgets.helpers.SimpleWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.TableViewer;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.ui.geometry.Side;
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

	private int bufferCapacity;
	private int maxAxes;
	// drop overlayed points
	private int MIN_PIXEL_DISTANCE = 1;// check this in case it causes data to be lost
	// private int UPDATE_PERIOD = 40; // check on this - this is their max drawing
	// rate
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
		// data?? No sure this makes sense but seems to work.
		dataSetMap = new ConcurrentHashMap<>();
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
//		1) Called first immediately after construction
		policy.setProperties(id, properties);
		this.widgetId = id;
		this.maxAxes = 1;
		if (properties.hasProperty(P_WIDGET_MAXAXES.key()))
			this.maxAxes = (Integer) properties.getPropertyValue(P_WIDGET_MAXAXES.key());
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
//		2) called second after construction
		metadata = meta;
		tsMeta = (Output0DMetadata) metadata.properties().getPropertyValue(Output0DMetadata.TSMETA);
		// do everything in getUserInterfaceContainer() below
	}

	@Override
	public Object getUserInterfaceContainer() {
//		3) called third after metadata
//		get the prefs before building the ui
		getUserPreferences();

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

		for (DataLabel dl : tsMeta.doubleNames())
			makeChannels(dl);
		// normally with statistics there are no int variables
		for (DataLabel dl : tsMeta.intNames())
			makeChannels(dl);
		timeFormatter.onMetaDataMessage(metadata);
		final TimeUnits timeUnit = (TimeUnits) metadata.properties().getPropertyValue(P_TIMEMODEL_TU.key());
		final int nTimeUnits = (Integer) metadata.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
		final String timeUnitName = TimeUtil.timeUnitName(timeUnit, nTimeUnits);

		final BorderPane content = new BorderPane();
		final DefaultNumericAxis xAxis = new DefaultNumericAxis("time", timeUnitName);
		xAxis.setAutoRangeRounding(false);
		xAxis.setTickLabelRotation(45);
		xAxis.invertAxis(false);
		xAxis.setTimeAxis(false);

		final List<DefaultNumericAxis> yAxes = new ArrayList<>();
		final List<ErrorDataSetRenderer> renderers = new ArrayList<>();

//		System.out.println(metadata.properties());
		for (Entry<String, CircularDoubleErrorDataSet> entry : dataSetMap.entrySet()) {
			if (yAxes.size() < maxAxes) {
				String yAxisUnits = "";// where do we get these - they don't apply to statistics
				DefaultNumericAxis newAxis = new DefaultNumericAxis(entry.getKey(), yAxisUnits);
				//newAxis.setAutoRangeRounding(true);
				newAxis.setAnimated(false);
				//assign axis side BEFORE adding renderer to the chart
				if (yAxes.size() % 2 == 0)
					newAxis.setSide(Side.LEFT);
				else
					newAxis.setSide(Side.RIGHT);
				yAxes.add(newAxis);
				ErrorDataSetRenderer newRenderer = new ErrorDataSetRenderer();
				initErrorDataSetRenderer(newRenderer);
				// add axis before adding dataset
				newRenderer.getAxes().add(newAxis);
				newRenderer.getDatasets().add(entry.getValue());
				renderers.add(newRenderer);

			} else { // add remaining data sets to the last axis
				ErrorDataSetRenderer renderer = renderers.get(maxAxes - 1);
				renderer.getDatasets().add(entry.getValue());
				// update the axis name
				DefaultNumericAxis yAxis = yAxes.get(maxAxes - 1);
				// Concatenate first and last names
				String currentName = yAxis.getName();
				if (currentName.contains("..."))
					currentName = currentName.substring(0, currentName.indexOf("..."));
				String newName = currentName + "..." + entry.getKey();
				yAxis.setName(newName);
			}
		}

		chart = new XYChart(xAxis, yAxes.get(0));
		if (dataSetMap.size() != 1)
			chart.setLegendVisible(true);
		else
			chart.setLegendVisible(false);

		chart.setAnimated(false);// probably expensive if true
		chart.getRenderers().addAll(renderers);

		chart.getPlugins().add(new Zoomer());
		chart.getPlugins().add(new TableViewer());
//		causes  concurrent modification error at times.
		chart.getPlugins().add(new DataPointTooltip());
//		not sure how this works?
//		chart.getPlugins().add(new Panner());
//		using this is a very confusing and perhaps buggy ui
//		chart.getPlugins().add(new EditAxis());
		chart.setTitle(widgetId);

		content.setCenter(chart);
		content.setRight(new Label(" "));

		return content;
	}

	@Override
	public void onStatusMessage(State state) {
//		System.out.println("State: " + state + "\t" + Thread.currentThread().getId());
//		4) Called 4th after UI construction - this is only in the UI thread the first time it's called
		if (isSimulatorState(state, waiting)) {
//			TODO may have problems with slow sims here - writes still occurring?
			for (Map.Entry<String, CircularDoubleErrorDataSet> entry : dataSetMap.entrySet()) {
				CircularDoubleErrorDataSet cbds = (CircularDoubleErrorDataSet) entry.getValue();
				cbds.reset();
			}

		} else if (isSimulatorState(state, finished)) {
			// It seems this is the critical thing to do to see the yaxis correctly.
			Platform.runLater(() -> {
				chart.getYAxis().forceRedraw();
			});
		}
	}

	@Override
	public void onDataMessage(final Output0DData data) {
		if (policy.canProcessDataMessage(data)) {
			// not in ui thread.
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
				ds.add(x, y, ey, ey);
			}

			for (CircularDoubleErrorDataSet ds : dataSetMap.values())
				if (!ds.equals(dontTouch))
					ds.autoNotification().getAndSet(true);
			if (((DefaultNumericAxis) chart.getYAxis()).isAutoRangeRounding())
				Platform.runLater(() -> {
					chart.getYAxis().forceRedraw();
				});

		}
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
						ds.reset();
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

	// helper to initialise a Renderer
	private void initErrorDataSetRenderer(final ErrorDataSetRenderer r) {
		r.setErrorType(ErrorStyle.NONE);
		r.setDashSize(0);
		r.setPointReduction(true);
		r.setDrawMarker(false);
		final DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) r.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(MIN_PIXEL_DISTANCE);
	}

	// helper for onMetaDataMessage, cf below.
	private void makeChannels(DataLabel dl) {
		if (sas != null) {
			for (StatisticalAggregates sa : sas.values()) {
				String key = sa.name() + DataLabel.HIERARCHY_DOWN + dl.toString();
				CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSetResizable(key, bufferCapacity);
				dataSetMap.put(key, ds);
			}
		} else if (sampledItems != null) {
			for (String si : sampledItems) {
				String key = si + DataLabel.HIERARCHY_DOWN + dl.toString();
				CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSetResizable(key, bufferCapacity);
				dataSetMap.put(key, ds);
			}
		} else {
			String key = dl.toString();
			CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSetResizable(key, bufferCapacity);
			dataSetMap.put(key, ds);
		}
	}

}