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

import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.ymuit.ui.colour.ColourContrast;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.cnrs.iees.twcore.constants.TrackerType;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.data.runtime.TimeSeriesData;
import au.edu.anu.twcore.data.runtime.TimeSeriesMetadata;
import au.edu.anu.twcore.ecosystem.runtime.timer.TimeUtil;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 11 Sep 2019
 */

/*
 * One chart for all series. Bad luck if the y axis ranges are very different.
 * Best used for a single series with repeat (replicate?) simulations
 */
public class SimpleTimeSeriesWidget extends AbstractDisplayWidget<TimeSeriesData, Metadata> implements Widget {

	private static Logger log = Logging.getLogger(SimpleTimeSeriesWidget.class);

	private LineChart<Number, Number> chart;
	/* lookup table to associate var name with series */
	private Map<String, XYChart.Series<Number, Number>> activeSeries;
	/* a set of colours, chosen by the uit for their contrast in RGB space */
	private String[] colours;
	/* clear or keep data showing from previous run(s) */
	private boolean clearOnReset;
	private int maxColours = 20;
	private String widgetId;

	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;

	private TimeSeriesMetadata tsmeta = null;
	private BlockingQueue<TimeSeriesData> buffer;

	public SimpleTimeSeriesWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME_SERIES);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		buffer = new LinkedBlockingQueue<TimeSeriesData>(/*1024*/);
		log.info("Thread: " + Thread.currentThread().getId());		
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// this is now effectively a reset method.
		log.info("Thread: " + Thread.currentThread().getId()+" Meta-data: " + meta);
//		peek(meta);
		Platform.runLater(() -> {
			clearPreviousResults();
			tsmeta = (TimeSeriesMetadata) meta.properties().getPropertyValue(TimeSeriesMetadata.TSMETA);
			List<String> ynames = new ArrayList<>();
			for (DataLabel dl : tsmeta.doubleNames()) {
				String key = dl.toString();
				ynames.add(key);
				String colour = getColour(activeSeries.size() + 1);
				addSeries(key, colour);
			}
			for (DataLabel dl : tsmeta.intNames()) {
				String key = dl.toString();
				ynames.add(key);
				String colour = getColour(activeSeries.size() + 1);
				addSeries(key, colour);
			}
			TimeUnits tu = (TimeUnits) meta.properties().getPropertyValue(P_TIMEMODEL_TU.key());
			int nTu = (Integer) meta.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
			String xAxisName = TimeUtil.timeUnitName(tu, nTu);
			chart.getXAxis().setLabel(TimeUtil.timeUnitName(tu, nTu));
			String yAxisName = String.join(",", ynames);
			chart.getYAxis().setLabel(yAxisName);
			String chartTitle = "(" + yAxisName + ")/(" + xAxisName + ")";
			chart.setTitle(chartTitle);
			timeFormatter.onMetaDataMessage(meta);
		});
	}

	private void clearPreviousResults() {
		if (clearOnReset)
			chart.getData().clear();
		else
			for (XYChart.Series<Number, Number> series : activeSeries.values())
				setLineColour(series, "lightgrey");
		activeSeries.clear();
	}

	@Override
	public void onDataMessage(TimeSeriesData data) {
		log.info("Thread: " + Thread.currentThread().getId()+" DATA: "+data);

		if (policy.canProcessDataMessage(data)) {
			try {
				Thread.yield();
				buffer.put(data);
				Platform.runLater(() -> {
					processBuffer();
				});

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private synchronized void processBuffer(){
//		System.out.println("Buffer size: " + buffer.size());
//		while (!buffer.isEmpty())
//			processDataMessage(buffer.take());
		for (TimeSeriesData ts : buffer) {
			processDataMessage(ts);
		}
		buffer.clear();
//		while (!buffer.isEmpty()) {
//			//System.out.println("Buffer size: " + buffer.size());
//			TimeSeriesData d;
//			try {
//				d = buffer.take();
//				processDataMessage(d);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
	}

	private void processDataMessage(final TimeSeriesData data) {
//		 log.info("Processing data " + data);
		int sender = data.sender();
		double x = data.time();
		for (DataLabel dl : tsmeta.doubleNames()) {
			double y = data.getDoubleValues()[tsmeta.indexOf(dl)];
			String key = dl.getEnd();
//			System.out.println(key+"="+y);
			XYChart.Series<Number, Number> series = activeSeries.get(key);
			series.getData().add(new Data<Number, Number>(x, y));
		}
		for (DataLabel dl : tsmeta.intNames()) {
			long y = data.getIntValues()[tsmeta.indexOf(dl)];
			String key = dl.getEnd();
//			System.out.println(key+"="+y);
		XYChart.Series<Number, Number> series = activeSeries.get(key);
			series.getData().add(new Data<Number, Number>(x, y));
		}
	}

	private String getColour(int i) {
		return colours[i % colours.length];
	}

	@Override
	public void onStatusMessage(State state) {
		 log.info(state.toString());
	/*
		 * this msg arrives AFTER onMetaDataMsg. Thus the work of that method will
		 * override this so there is no longer any point to this method... but there may
		 * be one day.
		 */
	}

	@Override
	public Object getUserInterfaceContainer() {
		log.info("Thread: " + Thread.currentThread().getId());

		colours = ColourContrast.allContrastingColourNames(Color.LIGHTGRAY, maxColours);
		activeSeries = new HashMap<>();
		log.info("Prepared user interface");
		BorderPane content = new BorderPane();
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("?");
		yAxis.setLabel("?");
		chart = new LineChart<>(xAxis, yAxis);
		chart.setCreateSymbols(false);
		chart.setAnimated(false);
		chart.legendVisibleProperty().set(false);
		setFontSize(10);
		chart.setTitle("?");
		content.setCenter(chart);
		return content;
	}

	private XYChart.Series<Number, Number> addSeries(String name, String colour) {
		XYChart.Series<Number, Number> result = new XYChart.Series<Number, Number>();
		result.setName(name);
		chart.getData().add(result);
		setLineColour(result, colour);
		activeSeries.put(name, result);
//		if (activeSeries.size() == 1) {
//			
//			chart.getYAxis().setLabel(name);
//			chart.setTitle(name);
//		}
		return result;
	}

	private void setFontSize(int fs) {
		chart.setStyle("-fx-font-size: " + fs + "px;");
	}

	private void setLineColour(XYChart.Series<Number, Number> series, String colourName) {
		String css = "-fx-stroke: " + colourName + "; -fx-stroke-width: 1px;";
		series.getNode().lookup(".chart-series-line").setStyle(css);
	}

	@Override
	public Object getMenuContainer() {
		Menu mu = new Menu(widgetId);
		MenuItem miEdit = new MenuItem("Edit...");
		mu.getItems().add(miEdit);
		miEdit.setOnAction(e -> edit());
		return mu;
	}

	private static final String KeyClearOnReset = "_clearOnReset";

	@Override
	public void putPreferences() {
		log.info("Thread: " + Thread.currentThread().getId());
		Preferences.putBoolean(widgetId + KeyClearOnReset, clearOnReset);
		timeFormatter.putPreferences();
		policy.putPreferences();

	}

	@Override
	public void getPreferences() {
		log.info("Thread: " + Thread.currentThread().getId());
		clearOnReset = Preferences.getBoolean(widgetId + KeyClearOnReset, true);
		timeFormatter.getPreferences();
		policy.getPreferences();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		log.info("Thread: " + Thread.currentThread().getId());
		this.widgetId = id;
		timeFormatter.setProperties(id, properties);
		policy.setProperties(id, properties);
		maxColours = 20;

	}

	private void edit() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle(widgetId);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		// dialog.initOwner(parent);
		CheckBox chxClearOnReset = new CheckBox("Clear data on reset");
		dialog.getDialogPane().setContent(chxClearOnReset);
		chxClearOnReset.setSelected(clearOnReset);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			clearOnReset = chxClearOnReset.isSelected();
		}
	}

	private void peek(Metadata meta) {
		TimeSeriesMetadata ts_meta = (TimeSeriesMetadata) meta.properties().getPropertyValue(TimeSeriesMetadata.TSMETA);
		for (String key : meta.properties().getKeysAsSet()) {
			System.out.println(key + ": " + meta.properties().getPropertyClassName(key) + ": "
					+ meta.properties().getPropertyValue(key));
		}
		for (DataLabel dl : ts_meta.doubleNames()) {
			System.out.println("doubleNames: " + dl);
		}
		for (DataLabel dl : ts_meta.intNames()) {
			System.out.println("intNames: " + dl);
		}

		System.out.println("nDouble: " + ts_meta.nDouble());
		System.out.println("nInt: " + ts_meta.nInt());
	}

}
