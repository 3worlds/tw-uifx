package au.edu.anu.twuifx.widgets;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.util.Optional;
import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.IndexedDataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.OutputXYData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.widgets.helpers.SimpleWidgetTrackingPolicy;
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
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
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
//private static final DefaultMarker[] symbols = { //
//DefaultMarker.RECTANGLE, //
//DefaultMarker.DIAMOND, //
//DefaultMarker.CIRCLE, //
//DefaultMarker.CROSS, //
//DefaultMarker.RECTANGLE2, //
//DefaultMarker.DIAMOND2, //
//DefaultMarker.CIRCLE2, //
//};

public class SimpleXYPlotWidget extends AbstractDisplayWidget<OutputXYData, Metadata> implements WidgetGUI {
	private String widgetId;
	private final WidgetTimeFormatter timeFormatter;
	private final WidgetTrackingPolicy<TimeData> policy;
	// private static Logger log = Logging.getLogger(SimpleXYPlotWidget.class);
	private XYChart chart;
	private DoubleDataSet dataSet;
	private int symbolSize;
	private DefaultMarker symbol;
	private Metadata msgMetadata;
	private String xName;
	private String yName;
	private String xUnits;
	private String yUnits;

	public SimpleXYPlotWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.XY);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
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

	@Override
	public Object getUserInterfaceContainer() {
		/**
		 * 3) called third after metadata.
		 */
		// Get the prefs before building the ui
		getUserPreferences();

		for (String key : msgMetadata.properties().getKeysAsSet()) {
			if (key.contains(P_FIELD_LABEL.key())) {
				IndexedDataLabel idxdl = (IndexedDataLabel) msgMetadata.properties().getPropertyValue(key);
				if (xName == null) {
					xName = idxdl.getEnd();
				} else
					yName = idxdl.getEnd();
			}
		}
		timeFormatter.onMetaDataMessage(msgMetadata);
		for (String key : msgMetadata.properties().getKeysAsSet()) {
			if (key.contains(xName + ".")) {
				if (key.contains(P_FIELD_UNITS.key())) {
					xUnits =  (String) msgMetadata.properties().getPropertyValue(key);
				}

			} else if (key.contains(yName + ".")) {
				if (key.contains(P_FIELD_UNITS.key())) {
					yUnits =  (String) msgMetadata.properties().getPropertyValue(key);
				}

			}
		}
		BorderPane content = new BorderPane();
		// aka makeChannels
		dataSet = new DefaultDataSet(xName+":"+yName);
		
		final DefaultNumericAxis xAxis1 = new DefaultNumericAxis(xName, xUnits);
		final DefaultNumericAxis yAxis1 = new DefaultNumericAxis(yName, yUnits);

//		xAxis1.setAnimated(false);// default = false;
//		yAxis1.setAnimated(false);
//		
//		xAxis1.setAutoRangeRounding(false);// default = false;
//		yAxis1.setAutoRangeRounding(false);
//
//		xAxis1.setTimeAxis(false);// default = false
//		yAxis1.setTimeAxis(false);
//
//		xAxis1.invertAxis(false);// default = false
//		yAxis1.invertAxis(false);
//
//		xAxis1.setForceZeroInRange(false);// default = false
//		yAxis1.setForceZeroInRange(false);

		xAxis1.setTickLabelRotation(45);

		// create one renderer
		ErrorDataSetRenderer rndr = new ErrorDataSetRenderer();
		// setup renderer
//		System.out.println(rndr.drawMarkerProperty().get());
		rndr.setErrorType(ErrorStyle.NONE);// Default ErrorStyle.ERRORCOMBO
		rndr.setPolyLineStyle(LineStyle.NONE);// Default LineStyle.NORMAL
		rndr.setMarkerSize(symbolSize);// Default = 1.5
		rndr.setMarker(symbol);// Default DefaultMarker.RECTANGLE
		rndr.setPointReduction(false);// Default: true;
//		rndr.setDrawMarker(true);// Default true
		rndr.setAssumeSortedData(false);// Default: true !! important since DS is likely unsorted
		DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) rndr.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(0);

		chart = new XYChart(xAxis1, yAxis1);

		rndr.getDatasets().add(dataSet);

		rndr.getAxes().addAll(xAxis1,yAxis1);

		chart.getRenderers().add(rndr);
		
		chart.legendVisibleProperty().set(false);
		chart.setAnimated(false);
		content.setCenter(chart);
		content.setRight(new Label(""));
		chart.getPlugins().add(new Zoomer());
		chart.getPlugins().add(new TableViewer());
		chart.getPlugins().add(new DataPointTooltip());

		return content;
	}

	private void processDataMessage(OutputXYData data) {
		Platform.runLater(() -> {
//			System.out.println(data.time()+"\t"+data.getX()+"\t"+data.getY());
			dataSet.add(data.getX(),data.getY());		
		});

	}

	@Override
	public void onDataMessage(OutputXYData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial))
				throw new TwuifxException("Handling initial data not implemented for this widget.");
			else
				processDataMessage(data);
		}
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
//			System.out.println("CLEAR DATA");
			dataSet.clearData();
		} else if (isSimulatorState(state, finished)) {
			Platform.runLater(() -> {
//				System.out.println("--AXIS REDRAW--");
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
		dialog.initOwner((Window) Dialogs.owner());
		GridPane content = new GridPane();
		content.setVgap(5);
		content.setHgap(3);
		Label lbl = new Label("Symbol size");
		Spinner<Integer> spCapacity = new Spinner<>();
		spCapacity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, symbolSize));
		spCapacity.setMaxWidth(100);
		spCapacity.setEditable(true);
		content.add(lbl, 0, 0);
		content.add(spCapacity, 1, 0);
		dialog.getDialogPane().setContent(content);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			int v = spCapacity.getValue();
			if (v != symbolSize) {
				symbolSize = v;
//				for (Renderer renderer : chart.getRenderers()) {
//					ErrorDataSetRenderer r = (ErrorDataSetRenderer) renderer;
//					r.setMarkerSize(symbolSize);
//				}
//				chart.requestLayout();
			}
		}
	}

	private static final String keySymbolSize = "symbolSize";
	private static final String keySymbol = "symbol";

	@Override
	public void putUserPreferences() {
		Preferences.putInt(widgetId + keySymbolSize, symbolSize);
		Preferences.putEnum(widgetId + keySymbol, symbol);
	}

	@Override
	public void getUserPreferences() {
		symbolSize = Preferences.getInt(widgetId + keySymbolSize, 2);
		symbol = (DefaultMarker) Preferences.getEnum(widgetId + keySymbol, DefaultMarker.DIAMOND);
	}

}
