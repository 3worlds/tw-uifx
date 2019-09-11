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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.rscs.aot.collections.tables.Dimensioner;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.DataMessageTypes;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeSeriesData;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.ymuit.ui.colour.ColourContrast;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

public class TimeSeriesPlotWidget extends AbstractDisplayWidget<TimeSeriesData, Metadata> implements Widget {
	private boolean clearOnReset;
	private GridPane gridPane;
	private Map<String, XYChart.Series<Number, Number>> activeSeries;

	private List<String> seriesNames;
	private List<String> seriesUnits;
	private String timeUnits = "light years"; // for x axis

	private String creatorId;

	private int layout;

	private static int stacked = 1;
	private static int tiled = 0;

	private final Color backGroundColor = Color.WHITE;
	private static final String sep = ":";
	private String[] colours;

	private static Logger log = Logging.getLogger(TimeSeriesPlotWidget.class);

	public TimeSeriesPlotWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME_SERIES);
		clearOnReset = true;
		layout = stacked;
		activeSeries = new HashMap<>();
		seriesNames = new ArrayList<>();
		seriesUnits = new ArrayList<>();
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		log.info("Meta-data received: " + meta);
		// we need a units label for the xaxis (cf: TimeUnit.addreviation())
		timeUnits = (String) meta.properties().getPropertyValue("timeUnits");
		// ordered list of DataLabel
		List<DataLabel> dls = (List<DataLabel>) meta.properties().getPropertyValue("labels");
		seriesNames.clear();
		for (DataLabel dl : dls) {
			String name = "";
			for (int i = 0; i < dl.size(); i++)
				name += sep + dl.get(i);
			seriesNames.add(name.replaceFirst(sep, ""));
		}
		// likewise, we need y axis units
		seriesUnits = (List<String>) meta.properties().getPropertyValue("units");
	}

	@Override
	public void onDataMessage(TimeSeriesData data) {
		log.info("Data received: " + data);
		// I thought we would have an ordered list of data instead of a Map<>
		addData(data.time(), data.values());
	}

	private void addData(long time, Map<DataLabel, Number> numbers) {
		Platform.runLater(() -> {
			for (String key : seriesNames) {
				XYChart.Series<Number, Number> srs = activeSeries.get(key);
				srs.getData().add(new Data<Number, Number>(time, numbers.get(key)));
			}
		});
	}

	private void setColours() {
		colours = ColourContrast.allContrastingColourNames(backGroundColor, seriesNames.size() + 5);
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			Platform.runLater(() -> {

				for (XYChart.Series srs : activeSeries.values())
					srs.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: lightgray;-fx-stroke-width: 1px;");

				activeSeries.clear();

				if (clearOnReset) {
					for (Node n : gridPane.getChildren()) {
						LineChart<Number, Number> chart = (LineChart<Number, Number>) n;
						chart.getData().clear();

					}
				}
				// TODO but now we must createNewSerues
			});

		}
	}

	@Override
	public Object getUserInterfaceContainer() {
		seriesNames.add("clip:clop");
		seriesNames.add("hip:hop");
		seriesNames.add("go:slow");
		seriesUnits.add("m/s");
		seriesUnits.add("ha.");
		seriesUnits.add("mph");
		setColours();

		gridPane = new GridPane();
		if (layout == stacked) {
			layoutStacked();
		} else
			layoutTiled();
		return gridPane;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.creatorId = id;
	}

	private static final String KeyLayout = "_layout";
	private static final String KeyClearOnReset = "_clearOnReset";

	@Override
	public void putPreferences() {
		Preferences.putBoolean(creatorId + KeyClearOnReset, clearOnReset);
		Preferences.putInt(creatorId + KeyLayout, layout);
	}

	@Override
	public void getPreferences() {
		clearOnReset = Preferences.getBoolean(creatorId + KeyClearOnReset, true);
		layout = Preferences.getInt(creatorId + KeyLayout, stacked);
	}

	private void layoutStacked() {
		int row = 0;
		for (int i = 0; i < seriesNames.size(); i++) {
			addChart(row, 0, seriesNames.get(i), colours[row], getXAxisTile());
			row++;
		}
	}

	private String getXAxisTile() {
		return "time (" + timeUnits + ")";
	}

	private void layoutTiled() {
		int nSeries = seriesNames.size() - 1;
		int nCols = (int) Math.sqrt(nSeries) + 1;
		int nRows = 1;
		while (nRows * nCols < nSeries)
			nRows++;
		int row = 0;
		int col = 0;
		int count = 0;
		for (String name : seriesNames) {
			if (!name.equals("time")) {
				// Need units of the x axis (time units)
				addChart(row, col, name, colours[count], getXAxisTile());
				col++;
				count++;
				if (col > nCols) {
					col = 0;
					row++;
				}
			}
		}
	}

	private void createNewSeries() {
		ObservableList<Node> charts = gridPane.getChildren();
		int count = 0;
		gridPane.getChildren();
//		for (String name : reader.data().keySet()) {
//			if (!name.equals("time")) {
//				Node c = charts.get(count);
//				LineChart<Number, Number> chart = (LineChart<Number, Number>) c;
//				installNewSeries(chart, name, colourNames[count]);
//				count++;
//			}
//		}
	}

	private void reconfigureStacked(List<Node> charts) {
		int row = 0;
		for (Node n : charts) {
			gridPane.add(n, 0, row);
			row++;
		}
	}

	private void reconfigure() {
		List<Node> charts = new ArrayList<>();
		for (Node c : gridPane.getChildren())
			charts.add(c);
		gridPane.getChildren().clear();
		if (layout == stacked)
			reconfigureStacked(charts);
		else
			reconfigureTiled(charts);
	}

	private void reconfigureTiled(List<Node> charts) {
		int nSeries = charts.size();
		int nCols = (int) Math.sqrt(nSeries) + 1;
		int nRows = 1;
		while (nRows * nCols < nSeries)
			nRows++;
		int row = 0;
		int col = 0;
		for (Node n : charts) {
			gridPane.add(n, col, row);
			col++;
			if (col > nCols) {
				col = 0;
				row++;
			}
		}
	}

	private void addChart(int r, int c, String name, String colour, String xAxisLabel) {
		// for now there is 1:1 with chart and series.
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel(xAxisLabel);
		yAxis.setLabel(getYAxisLabel(name));
		// create the chart
		LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

		/**
		 * clearing series does not work if either lineChart.setCreateSymbols(false) or
		 * lineChart.setAnimated(true)! Known bug.
		 * https://bugs.openjdk.java.net/browse/JDK-8150264
		 * 
		 * We don't want symbols so must switch off animation.
		 */
		lineChart.setCreateSymbols(false);
		lineChart.setAnimated(false);

		// No use for legend
		lineChart.legendVisibleProperty().set(false);
		setFontSize(lineChart, 10);
		setChartTitle(lineChart, name);

		installNewSeries(lineChart, name, colour);

		ColumnConstraints cc = new ColumnConstraints();
		cc.setHalignment(HPos.RIGHT);
		cc.setHgrow(Priority.SOMETIMES);
		RowConstraints rc = new RowConstraints();
		rc.setValignment(VPos.TOP);
		rc.setVgrow(Priority.SOMETIMES);// may get errors here if more than one chart
		gridPane.getColumnConstraints().add(cc);
		gridPane.getRowConstraints().add(rc);

		gridPane.add(lineChart, c, r);
	}

	private String getYAxisLabel(String name) {
		String units = seriesUnits.get(seriesNames.indexOf(name));
		String[] fields = name.split(sep);
		return fields[fields.length - 1] + " (" + units + ")";
	}

	private void setFontSize(LineChart<Number, Number> chart, int fs) {
		chart.setStyle("-fx-font-size: " + fs + "px;");
	}

	private void setChartTitle(LineChart<Number, Number> lineChart, String name) {
		String chartTitle = name;
		// TODO who knows?
		lineChart.setTitle(chartTitle);
	}

	private void installNewSeries(LineChart<Number, Number> lineChart, String name, String colour) {
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
		series.setName(name);
		lineChart.getData().add(series);
		setLineColour(series, colour);
		activeSeries.put(name, series);
	}

	private void setLineColour(XYChart.Series<Number, Number> series, String colourName) {
		String css = "-fx-stroke: " + colourName + "; -fx-stroke-width: 1px;";
		series.getNode().lookup(".chart-series-line").setStyle(css);
	}

	private enum LayoutOptions {
		Stacked, Tiled
	}

	private void edit() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle(creatorId);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHalignment(HPos.RIGHT);
		grid.getColumnConstraints().add(cc);
		dialog.getDialogPane().setContent(grid);
		// dialog.initOwner(parent);

		ComboBox<LayoutOptions> cbxLayout = new ComboBox<>();
		cbxLayout.getItems().setAll(LayoutOptions.values());
		cbxLayout.getSelectionModel().select(layout);
		grid.add(new Label("Layout:"), 0, 0);
		grid.add(cbxLayout, 1, 0);

		CheckBox chxClearOnReset = new CheckBox("Clear data on reset");
		grid.add(chxClearOnReset, 1, 1);
		chxClearOnReset.setSelected(clearOnReset);

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			clearOnReset = chxClearOnReset.isSelected();
			int newLayout = tiled;
			LayoutOptions newLayoutOption = cbxLayout.getSelectionModel().getSelectedItem();
			if (newLayoutOption.equals(LayoutOptions.Stacked))
				newLayout = stacked;
			if (newLayout != layout) {
				layout = newLayout;
				if (!activeSeries.isEmpty()) {
					reconfigure();
				}
			}
		}
	}

	@Override
	public Object getMenuContainer() {
		Menu mu = new Menu(creatorId);
		MenuItem miEdit = new MenuItem("Edit...");
		mu.getItems().add(miEdit);
		miEdit.setOnAction(e -> edit());

		MenuItem miExport = new MenuItem("Export data...");
		miExport.setOnAction(e -> doExportData());
		mu.getItems().add(miExport);
		return mu;
	}

	private Object doExportData() {
		// TODO Auto-generated method stub
		return null;
	}

}
