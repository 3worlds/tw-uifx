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
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.DataMessageTypes;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeSeriesData;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 11 Sep 2019
 */

/*
 * One chart for all series. Bad luck if the y axis ranges are very different.
 * Best used for a single series with repeat (replicate?) simulatons
 */
public class SimpleTimeSeriesWidget extends AbstractDisplayWidget<TimeSeriesData, Metadata> implements Widget {

	private static Logger log = Logging.getLogger(SimpleTimeSeriesWidget.class);

	private LineChart<Number, Number> chart;
	private Map<String, XYChart.Series<Number, Number>> activeSeries;
	private String[] colours;
	// clear or keep data showing from previous run(s)
	private boolean clearOnReset;
	private int maxColours = 20; // can be set from archetype
	private String widgetId;

	public SimpleTimeSeriesWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME_SERIES);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		log.info("Meta-data: " + meta);
	}

	@Override
	public void onDataMessage(TimeSeriesData data) {
		log.info("Data: " + data);
		Platform.runLater(() -> {
			processDataMessage(data);
		});
	}

	private void processDataMessage(TimeSeriesData data) {
		log.info("Processing data " + data);
		long time = data.time();
		int sender = data.sender();
		Map<DataLabel, Number> values = data.values();
		for (Map.Entry<DataLabel, Number> entry : values.entrySet()) {
			String key = entry.getKey().getEnd() + "(" + sender + ")";
			XYChart.Series<Number, Number> series = activeSeries.get(key);
			if (series == null)
				series = addSeries(key, getColour(activeSeries.size() + 1));
			series.getData().add(new Data<Number, Number>(time, entry.getValue()));
		}
	}

	private String getColour(int i) {
		return colours[i % colours.length];
	}

	@Override
	public void onStatusMessage(State state) {
		log.info("Status msg received:" + state);
		if (isSimulatorState(state, waiting)) {
			Platform.runLater(() -> {
				processWaitState();
			});
		}
	}

	private void processWaitState() {
		if (clearOnReset)
			chart.getData().clear();
		else
			for (XYChart.Series<Number, Number> series : activeSeries.values())
				setLineColour(series, "lightgray");
		activeSeries.clear();
	}

	@Override
	public Object getUserInterfaceContainer() {
		colours = ColourContrast.allContrastingColourNames(Color.LIGHTGRAY, maxColours);
		activeSeries = new HashMap<>();
		log.info("Prepared user interface");
		BorderPane content = new BorderPane();
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("time (units?)");
		yAxis.setLabel("what should be here?");
		chart = new LineChart<>(xAxis, yAxis);
		chart.setCreateSymbols(false);
		chart.setAnimated(false);
		chart.legendVisibleProperty().set(true);
		setFontSize(10);
		chart.setTitle("What should be here?");
		content.setCenter(chart);
		return content;
	}

	private XYChart.Series<Number, Number> addSeries(String name, String colour) {
		XYChart.Series<Number, Number> result = new XYChart.Series<Number, Number>();
		result.setName(name);
		chart.getData().add(result);
		setLineColour(result, colour);
		activeSeries.put(name, result);
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
		Preferences.putBoolean(widgetId + KeyClearOnReset, clearOnReset);
	}

	@Override
	public void getPreferences() {
		clearOnReset = Preferences.getBoolean(widgetId + KeyClearOnReset, true);
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.widgetId = id;
		if (properties.hasProperty("maxColours"))
			maxColours = (Integer) properties.getPropertyValue("maxColours");
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

}
