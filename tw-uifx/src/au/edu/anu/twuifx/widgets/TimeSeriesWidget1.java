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
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import au.edu.anu.omhtk.preferences.IPreferences;
import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.util.StringUtils;
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
import au.edu.anu.twuifx.widgets.helpers.MultiSenderTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.EditAxis;
import de.gsi.chart.plugins.TableViewer;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.ui.geometry.Side;// This is a flaw since two of these values don't apply - messy
import de.gsi.dataset.DataSet;
// Nicer would be to use a converter routine
import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.DateTimeType;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
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
import javafx.scene.layout.HBox;
import javafx.stage.Window;

/**
 * Displays 1..* time series lines in a circular buffer: one set for each
 * selected simulator. Plotting is done with {@link XYChart}.
 * <p>
 * The model configuration can define any number of y axes.
 * 
 * @author Ian Davies - 10 Dec. 2020
 * 
 */
public class TimeSeriesWidget1 extends AbstractDisplayWidget<Output0DData, Metadata> implements WidgetGUI {
	private String widgetId;

	private int bufferSize;
	private int maxAxes;
	// drop overlayed points
	private int MIN_PIXEL_DISTANCE = 1;// check this in case it causes data to be lost
	private XYChart chart;
	private final List<DefaultNumericAxis> yAxes;
	private final Map<Integer, TreeMap<String, CircularDoubleErrorDataSet>> senderDataSetMap;
	private Output0DMetadata tsMetadata;
	private Metadata msgMetadata;
	private final WidgetTimeFormatter timeFormatter;
	private final WidgetTrackingPolicy<TimeData> policy;
	private StatisticalAggregatesSet sas;
	private Collection<String> sampledItems;

	private Label trackerTime;

	/**
	 * @param statusSender The {@link StatusWidget}.
	 */
	public TimeSeriesWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM0);
		// needs to be thread-safe because chartfx plugins may be looking at chart
		// data?? No sure this makes sense but seems to work.
		senderDataSetMap = new ConcurrentHashMap<>();
		timeFormatter = new WidgetTimeFormatter();
		policy = new MultiSenderTrackingPolicy();
		yAxes = new ArrayList<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		/* 1) Called first immediately after construction */
		policy.setProperties(id, properties);
		this.widgetId = id;
		this.maxAxes = 1;
		if (properties.hasProperty(P_WIDGET_MAXAXES.key()))
			this.maxAxes = (Integer) properties.getPropertyValue(P_WIDGET_MAXAXES.key());
		this.bufferSize = 1000;
		if (properties.hasProperty(P_WIDGET_BUFFERSIZE.key()))
			this.bufferSize = (Integer) properties.getPropertyValue(P_WIDGET_BUFFERSIZE.key());
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		/* 2) called second after construction */
		if (policy.canProcessMetadataMessage(meta)) {
			msgMetadata = meta;
			tsMetadata = (Output0DMetadata) meta.properties().getPropertyValue(Output0DMetadata.TSMETA);
		}
	}

	@Override
	public Object getUserInterfaceContainer() {
		/* 3) called third after metadata */

		// get the prefs before building the ui
		getPreferences();

		timeFormatter.onMetaDataMessage(msgMetadata);
//		TimeScaleType tst = (TimeScaleType) msgMetadata.properties().getPropertyValue(P_TIMELINE_SCALE.key());
		final TimeUnits timeUnit = (TimeUnits) msgMetadata.properties().getPropertyValue(P_TIMELINE_SHORTTU.key());

		int nTimeUnits = 1;
		if (msgMetadata.properties().hasProperty(P_TIMEMODEL_NTU.key()))
			nTimeUnits = (Integer) msgMetadata.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
		final String timeUnitName = TimeUtil.timeUnitAbbrev(timeUnit, nTimeUnits);

		final BorderPane content = new BorderPane();
		HBox topContent = new HBox();
		content.setTop(topContent);
		trackerTime = new Label();
		topContent.getChildren().addAll(new Label("Tracker time: "), trackerTime);

		sas = null;
		if (msgMetadata.properties().hasProperty(P_DATATRACKER_STATISTICS.key()))
			sas = (StatisticalAggregatesSet) msgMetadata.properties().getPropertyValue(P_DATATRACKER_STATISTICS.key());
		if (msgMetadata.properties().hasProperty("sample")) {
			StringTable st = (StringTable) msgMetadata.properties().getPropertyValue("sample");
			if (st != null) {
				sampledItems = new ArrayList<>(st.size());
				for (int i = 0; i < st.size(); i++) {
					sampledItems.add(st.getWithFlatIndex(i));
				}
			}
		}

		int nItems = tsMetadata.doubleNames().size() + tsMetadata.intNames().size();
		if (nItems == 0)
			throw new IllegalArgumentException("No numeric items have been defined for '" + widgetId + "'.");
		int nModifiers = 0;
		if (sas != null)
			nModifiers += sas.values().size();
		if (sampledItems != null)
			nModifiers += sampledItems.size();
		// TODO: Look out this is probably wrong
		int nAxes;
		if (nModifiers > 0)
			nAxes = Math.min(nItems * nModifiers, maxAxes);
		else
			nAxes = Math.min(nItems, maxAxes);

		for (int sender = policy.getDataMessageRange().getFirst(); sender <= policy.getDataMessageRange()
				.getLast(); sender++) {
			senderDataSetMap.put(sender, new TreeMap<String, CircularDoubleErrorDataSet>());
			for (DataLabel dl : tsMetadata.doubleNames())
				makeChannels(dl, sender);
			for (DataLabel dl : tsMetadata.intNames())
				makeChannels(dl, sender);
		}

		// for adding to the chart later.
		final List<ErrorDataSetRenderer> renderers = new ArrayList<>();

		for (int i = 0; i < nAxes; i++) {
			DefaultNumericAxis newAxis = new DefaultNumericAxis("", "");
			newAxis.setAnimated(false);
			if (yAxes.size() % 2 == 0)
				newAxis.setSide(Side.LEFT);
			else
				newAxis.setSide(Side.RIGHT);
			yAxes.add(newAxis);
			// newAxis.setAutoRangePadding(0.1);
		}

		// we need to sort this using padIndexedDidgets somehow
		int count = 0;
		for (Map.Entry<Integer, TreeMap<String, CircularDoubleErrorDataSet>> entry : senderDataSetMap.entrySet()) {
			TreeMap<String, CircularDoubleErrorDataSet> dsm = entry.getValue();
			for (String key : dsm.navigableKeySet()) {
				int index = count % nAxes;
				DefaultNumericAxis axis = yAxes.get(index);
				ErrorDataSetRenderer newRenderer = new ErrorDataSetRenderer();
				initErrorDataSetRenderer(newRenderer);
				newRenderer.getAxes().add(axis);
				newRenderer.getDatasets().add(dsm.get(key));
				renderers.add(newRenderer);
				if (count >= maxLegendItems)
					newRenderer.setShowInLegend(false);
				count++;

				if (axis.getName().isBlank())
					axis.setName(key);
				else {
					String currentName = axis.getName();
					if (currentName.contains(StringUtils.ELLIPSIS))
						currentName = currentName.substring(0, currentName.indexOf(StringUtils.ELLIPSIS));
					String newName = currentName + StringUtils.ELLIPSIS + key;
					axis.setName(newName);
				}
			}
		}

		final DefaultNumericAxis xAxis = new DefaultNumericAxis("time: ", timeUnitName);
		xAxis.setAutoRangeRounding(false);
//		xAxis.setAutoRangePadding(0.1);
		xAxis.setTickLabelRotation(45);
		xAxis.invertAxis(false);
		xAxis.setTimeAxis(false);

		chart = new XYChart(xAxis, yAxes.get(0));
		// NB: This is the padding around the whole control - not between child nodes
		chart.setPadding(new Insets(1, 10, 5, 5));
		chart.setLegendSide(legendSide);
		chart.setLegendVisible(legendVisible);
		chart.setAnimated(false);// probably expensive if true
		chart.getRenderers().addAll(renderers);
		chart.getTitleLegendPane(legendSide).setPadding(new Insets(10, 10, 10, 10));
//		chart.getTitleLegendPane(legendSide).setStyle("-fx-background-color: transparent");

		chart.getPlugins().add(new Zoomer());
		// senderDataSetMap.get(0).values();
		int nSims = senderDataSetMap.size();
		int nSeriesPerSim = senderDataSetMap.get(policy.getDataMessageRange().getFirst()).size();
		int nSeries = nSims * nSeriesPerSim;

		if (nSeries <= 100)
			chart.getPlugins().add(new TableViewer());
//		causes  concurrent modification error at times.
		chart.getPlugins().add(new DataPointTooltip());
//		not sure how this works?
//		chart.getPlugins().add(new Panner());
//		using this is a very confusing and perhaps buggy ui
		chart.getPlugins().add(new EditAxis());
		chart.setTitle("[#" + policy.toString() + "]" + widgetId);

		content.setCenter(chart);
		content.setRight(new Label(" "));

		return content;
	}

	@Override
	public void onStatusMessage(State state) {
//		4) Called 4th after UI construction - this is only in the UI thread the first time it's called
		if (isSimulatorState(state, waiting)) {

			Platform.runLater(() -> {
				DateTimeType dtt = (DateTimeType) msgMetadata.properties()
						.getPropertyValue(P_TIMELINE_TIMEORIGIN.key());
				trackerTime.setText(timeFormatter.getTimeText(dtt.getDateTime()));
			});

			for (Renderer r : chart.getRenderers())
				for (DataSet d : r.getDatasets()) {
					CircularDoubleErrorDataSet cdds = (CircularDoubleErrorDataSet) d;
					cdds.reset();
				}

		} else if (isSimulatorState(state, finished)) {
			// It seems this is the critical thing to do to see the axes correctly.
			Platform.runLater(() -> {
				chart.getAxes().forEach((axis) -> {
					axis.forceRedraw();
				});
			});
		}
	}

	private void processDataMessage(Output0DData data) {
		final Map<String, CircularDoubleErrorDataSet> dataSetMap = senderDataSetMap.get(data.sender());
		final int sender = data.sender();

		Platform.runLater(() -> {

			trackerTime.setText(timeFormatter.getTimeText(data.time()));

			CircularDoubleErrorDataSet dontTouch = dataSetMap.values().iterator().next();

			for (CircularDoubleErrorDataSet ds : dataSetMap.values())
				if (!ds.equals(dontTouch))
					ds.autoNotification().getAndSet(false);

			final double x = data.time();

			String itemId = null;
			if (sas != null)
				itemId = data.itemLabel().getEnd();
			else if (sampledItems != null)
				itemId = data.itemLabel().toLazyString();

			for (DataLabel dl : tsMetadata.doubleNames()) {
				String key;
				if (itemId != null)
					key = sender + ":" + itemId + DataLabel.HIERARCHY_DOWN + dl.toLazyString();
				else
					key = sender + ":" + dl.toLazyString();
				CircularDoubleErrorDataSet ds = dataSetMap.get(key);
				final double y = data.getDoubleValues()[tsMetadata.indexOf(dl)];
				final double ey = 0;
				if (ds != null)
					ds.add(x, y, ey, ey);
			}

			for (DataLabel dl : tsMetadata.intNames()) {
				String key;
				if (itemId != null)
					key = sender + ":" + itemId + DataLabel.HIERARCHY_DOWN + dl.toLazyString();
				else
					key = sender + ":" + dl.toLazyString();
				CircularDoubleErrorDataSet ds = dataSetMap.get(key);
				final double y = data.getIntValues()[tsMetadata.indexOf(dl)];
				final double ey = 0;
				if (ds != null)
					ds.add(x, y, ey, ey);
			}

			for (CircularDoubleErrorDataSet ds : dataSetMap.values())
				if (!ds.equals(dontTouch))
					ds.autoNotification().getAndSet(true);

			if (((DefaultNumericAxis) chart.getYAxis()).isAutoRangeRounding())
				chart.getYAxis().forceRedraw();
		});
	}

	@Override
	public void onDataMessage(Output0DData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial))
				throw new IllegalArgumentException("Handling initial data not implemented for this widget.");
			else
				processDataMessage(data);
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
		int row = 0;
		CheckBox chbxLegendVisible = new CheckBox("");
		addGridControl("Legend visible", row++, chbxLegendVisible, content);
		chbxLegendVisible.setSelected(legendVisible);

		ComboBox<Side> cmbSide = new ComboBox<>();
		cmbSide.getItems().addAll(Side.values());// better to convert to javafx.geometry.Side.
		cmbSide.getSelectionModel().select(chart.getLegendSide());
		addGridControl("Legend position", row++, cmbSide, content);

		Spinner<Integer> spMaxLegendItems = new Spinner<>();
		spMaxLegendItems.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, maxLegendItems));
		spMaxLegendItems.setMaxWidth(100);
		spMaxLegendItems.setEditable(true);
		addGridControl("Max legend items", row++, spMaxLegendItems, content);

		dialog.getDialogPane().setContent(content);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			maxLegendItems = spMaxLegendItems.getValue();
			for (int i = 0; i < chart.getRenderers().size(); i++) {
				Renderer r = chart.getRenderers().get(i);
				if (i > maxLegendItems)
					r.setShowInLegend(false);
				else
					r.setShowInLegend(true);
			}
			legendSide = cmbSide.getValue();
			chart.setLegendSide(legendSide);
			legendVisible = chbxLegendVisible.isSelected();
			chart.setLegendVisible(legendVisible);
			chart.getTitleLegendPane(legendSide).setPadding(new Insets(10, 10, 10, 10));
//			chart.getTitleLegendPane(legendSide).setStyle("-fx-background-color: transparent;");
		}
	}

	// move to helper
	private static void addGridControl(String name, int row, Node ctrl, GridPane grid) {
		Label lbl = new Label(name);
		grid.add(lbl, 0, row);
		grid.add(ctrl, 1, row);
		GridPane.setHalignment(lbl, HPos.RIGHT);
		GridPane.setHalignment(ctrl, HPos.LEFT);
		GridPane.setValignment(ctrl, VPos.CENTER);
	}

	private static final String keyLegendSide = "legendSide";
	private static final String keyLegendVisible = "legendVisible";
	private static final String keyMaxLegendItems = "maxLegendItems";

	private boolean legendVisible;
	private Side legendSide;
	private int maxLegendItems;

	@Override
	public void putPreferences() {
		IPreferences prefs = Preferences.getImplementation();

		prefs.putBoolean(widgetId + keyLegendVisible, legendVisible);
		prefs.putEnum(widgetId + keyLegendSide, legendSide);
		prefs.putInt(widgetId + keyMaxLegendItems, maxLegendItems);
	}

	@Override
	public void getPreferences() {
		IPreferences prefs = Preferences.getImplementation();

		legendVisible = prefs.getBoolean(widgetId + keyLegendVisible, true);
		legendSide = (Side) prefs.getEnum(widgetId + keyLegendSide, Side.BOTTOM);
		maxLegendItems = prefs.getInt(widgetId + keyMaxLegendItems, 8);
	}

	// helper to initialise a Renderer
	private void initErrorDataSetRenderer(final ErrorDataSetRenderer r) {
		r.setErrorType(ErrorStyle.NONE);
		r.setDashSize(0);
		r.setPointReduction(true);
		r.setDrawMarker(false);
		r.setDrawBars(false);
		final DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) r.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(MIN_PIXEL_DISTANCE);
	}

	// helper new sender
	private void makeChannels(DataLabel dl, int sender) {

		Map<String, CircularDoubleErrorDataSet> dataSetMap = senderDataSetMap.get(sender);

		if (sas != null) {
			for (StatisticalAggregates sa : sas.values()) {
				String key = sender + ":" + sa.name() + DataLabel.HIERARCHY_DOWN + dl.toLazyString();
				CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSet(key, bufferSize);
				dataSetMap.put(key, ds);
			}
		} else if (sampledItems != null) {
			for (String si : sampledItems) {
				String key = sender + ":" + si + DataLabel.HIERARCHY_DOWN + dl.toLazyString();
				CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSet(key, bufferSize);
				dataSetMap.put(key, ds);
			}
		} else {
			String key = sender + ":" + dl.toLazyString();
			CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSet(key, bufferSize);
			dataSetMap.put(key, ds);
		}
	}
}