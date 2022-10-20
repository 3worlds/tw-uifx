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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import au.edu.anu.omhtk.preferences.IPreferences;
import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.dialogs.DialogsFactory;
import au.edu.anu.twcore.data.runtime.IndexedDataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.OutputXYData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.AbstractDataTracker;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.widgets.helpers.MultiSenderTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.marker.DefaultMarker;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.TableViewer;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.DefaultDataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
 * 
 * A {@link WidgetGUI} that displays a scatter plot for simulators sending {@link OutputXYData} messages.
 * <p>
  * <img src="{@docRoot}/../doc/images/ScatterPlotWidget1.png" width="400" alt=
 * "ScatterPlotWidget1"/>
 * </p>
 
 * @author Ian Davies - 16 Mar. 2021
 */
public class ScatterPlotWidget1 extends AbstractDisplayWidget<OutputXYData, Metadata> implements WidgetGUI {
	private String widgetId;
	private final WidgetTimeFormatter timeFormatter;
	private final WidgetTrackingPolicy<TimeData> policy;
	private final Map<Integer, DoubleDataSet> senderDataSet;
	// private static Logger log = Logging.getLogger(SimpleXYPlotWidget.class);
	private XYChart chart;
	private Metadata msgMetadata;
	private BorderPane content;
	private String xName;
	private String yName;
	private String xUnits;
	private String yUnits;

	/**
	 * @param statusSender  statusSender The {@link StatusWidget}.
	 */
	public ScatterPlotWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, AbstractDataTracker.XY);
		timeFormatter = new WidgetTimeFormatter();
		policy = new MultiSenderTrackingPolicy();
		senderDataSet = new ConcurrentHashMap<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.widgetId = id;
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		if (policy.canProcessMetadataMessage(meta)) {
			msgMetadata = meta;
		}
	}

//	hints:
//	xAxis1.setAnimated(false);// default = false;
//	xAxis1.setAutoRangeRounding(false);// default = false;
//	xAxis1.setTimeAxis(false);// default = false
//	xAxis1.invertAxis(false);// default = false
//	xAxis1.setForceZeroInRange(false);// default = false
//	rndr.setDrawMarker(true);// Default true
	@Override
	public Object getUserInterfaceContainer() {
		/**
		 * 3) called third after metadata.
		 */
		// Get the prefs before building the ui
		getPreferences();

		timeFormatter.onMetaDataMessage(msgMetadata);
// Know nothing about sampledItem at this stage!?

		for (String key : msgMetadata.properties().getKeysAsSet()) {
			if (key.contains(P_FIELD_LABEL.key())) {
				IndexedDataLabel idxdl = (IndexedDataLabel) msgMetadata.properties().getPropertyValue(key);
				if (xName == null) {
					xName = idxdl.getEnd();
				} else
					yName = idxdl.getEnd();
			}
		}
		for (String key : msgMetadata.properties().getKeysAsSet()) {
			if (key.contains(xName + ".")) {
				if (key.contains(P_FIELD_UNITS.key())) {
					xUnits = (String) msgMetadata.properties().getPropertyValue(key);
				}

			} else if (key.contains(yName + ".")) {
				if (key.contains(P_FIELD_UNITS.key())) {
					yUnits = (String) msgMetadata.properties().getPropertyValue(key);
				}

			}
		}

		content = new BorderPane();
		// a.k.a. makeChannels
		for (int sender = policy.getDataMessageRange().getFirst(); sender <= policy.getDataMessageRange()
				.getLast(); sender++) {
			DoubleDataSet ds;
			if (!swapAxes)
				ds = new DefaultDataSet(xName + ":" + yName);
			else
				ds = new DefaultDataSet(yName + ":" + xName);

			senderDataSet.put(sender, ds);
		}

		final DefaultNumericAxis xAxis1 = new DefaultNumericAxis(xName, xUnits);
		final DefaultNumericAxis yAxis1 = new DefaultNumericAxis(yName, yUnits);

		xAxis1.setTickLabelRotation(45);

		// create one renderer
		ErrorDataSetRenderer rndr = new ErrorDataSetRenderer();
		// setup renderer
		rndr.setErrorType(ErrorStyle.NONE);// Default ErrorStyle.ERRORCOMBO
		rndr.setPolyLineStyle(LineStyle.NONE);// Default LineStyle.NORMAL
		rndr.setMarkerSize(symbolSize);// Default = 1.5
		rndr.setMarker(symbol);// Default DefaultMarker.RECTANGLE
		rndr.setPointReduction(false);// Default: true;
		rndr.setAssumeSortedData(false);// Default: true !! important since DS is likely unsorted
		DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) rndr.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(1);

		chart = new XYChart(xAxis1, yAxis1);
		chart.setPadding(new Insets(5, 5, 5, 5));

		for (Map.Entry<Integer, DoubleDataSet> entry : senderDataSet.entrySet())
			rndr.getDatasets().add(entry.getValue());

		rndr.getAxes().addAll(xAxis1, yAxis1);

		chart.getRenderers().add(rndr);
		chart.legendVisibleProperty().set(false);
		chart.setAnimated(false);
		content.setRight(new Label(""));
		chart.getPlugins().add(new Zoomer());
		if (senderDataSet.size() <= 100)
			chart.getPlugins().add(new TableViewer());
		chart.getPlugins().add(new DataPointTooltip());

		content.setCenter(chart);

		if (swapAxes) {
			chart.getXAxis().setName(yName);
			chart.getXAxis().setUnit(yUnits);
			chart.getYAxis().setName(xName);
			chart.getYAxis().setUnit(xUnits);
		}

		return content;
	}

	private void processDataMessage(OutputXYData data) {
		Platform.runLater(() -> {
			DoubleDataSet ds = senderDataSet.get(data.sender());
			if (swapAxes)
				ds.add(data.getY(), data.getX());
			else
				ds.add(data.getX(), data.getY());
		});

	}

	@Override
	public void onDataMessage(OutputXYData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial))
				throw new IllegalArgumentException("Handling initial data not implemented for this widget.");
			else
				processDataMessage(data);
		}
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) { // use this concurrent map rather than the chart list of dataSets
			for (Map.Entry<Integer, DoubleDataSet> entry : senderDataSet.entrySet())
				entry.getValue().clearData();
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
		dialog.initOwner((Window) DialogsFactory.owner());
		GridPane content = new GridPane();
		content.setVgap(5);
		content.setHgap(3);
		Label lbl = new Label("Symbol size");
		Spinner<Integer> spCapacity = new Spinner<>();
		spCapacity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, symbolSize));
		//
		spCapacity.setMaxWidth(100);
		spCapacity.setEditable(true);

		ComboBox<DefaultMarker> cmbMarker = new ComboBox<>();
		cmbMarker.getItems().addAll(DefaultMarker.values());
		cmbMarker.getSelectionModel().select(symbol);
		content.add(lbl, 0, 0);
		content.add(spCapacity, 1, 0);
		content.add(new Label("Symbol"), 0, 1);
		content.add(cmbMarker, 1, 1);
		content.add(new Label("Swap axes"), 0, 2);
		CheckBox cbsa = new CheckBox("");
		cbsa.setSelected(swapAxes);
		content.add(cbsa, 1, 2);

		dialog.getDialogPane().setContent(content);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			symbolSize = spCapacity.getValue();
			symbol = cmbMarker.getValue();

			for (Renderer renderer : chart.getRenderers()) {
				ErrorDataSetRenderer r = (ErrorDataSetRenderer) renderer;
				r.setMarkerSize(symbolSize);
				r.setMarker(symbol);
			}
			if (swapAxes != cbsa.isSelected()) {
				swapAxes = cbsa.isSelected();
				// swap names and units
				String xName = chart.getXAxis().getName();
				String xUnits = chart.getXAxis().getUnit();
				String yName = chart.getYAxis().getName();
				String yUnits = chart.getYAxis().getUnit();
				chart.getXAxis().setName(yName);
				chart.getXAxis().setUnit(yUnits);
				chart.getYAxis().setName(xName);
				chart.getYAxis().setUnit(xUnits);

				// swap data
				for (Map.Entry<Integer, DoubleDataSet> entry : senderDataSet.entrySet()) {
					DoubleDataSet ds = entry.getValue();
					ds.setName(chart.getXAxis().getName() + ":" + chart.getYAxis().getName());
					double[] xv = ds.getXValues();
					double[] yv = ds.getYValues();
					ds.clearData();
					for (int i = 0; i < xv.length; i++)
						ds.add(yv[i], xv[i]);

				}
			}
			chart.requestLayout();
		}
	}

	private static final String keySymbolSize = "symbolSize";
	private static final String keySymbol = "symbol";
	private static final String keySwapAxes = "swapAxes";

	private int symbolSize;
	private DefaultMarker symbol;
	private boolean swapAxes;

	@Override
	public void putPreferences() {
		IPreferences prefs = Preferences.getImplementation();
		prefs.putInt(widgetId + keySymbolSize, symbolSize);
		prefs.putEnum(widgetId + keySymbol, symbol);
		prefs.putBoolean(keySwapAxes, swapAxes);
	}

	@Override
	public void getPreferences() {
		IPreferences prefs = Preferences.getImplementation();
		symbolSize = prefs.getInt(widgetId + keySymbolSize, 2);
		symbol = (DefaultMarker) prefs.getEnum(widgetId + keySymbol, DefaultMarker.DIAMOND);
		swapAxes = prefs.getBoolean(keySwapAxes, false);
	}

}
