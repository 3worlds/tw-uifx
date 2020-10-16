package au.edu.anu.twuifx.widgets;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_NTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_TU;

import java.util.Optional;
import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.OutputXYData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
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
import de.gsi.dataset.spi.DoubleDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
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

public class SimpleXYPlotWidget extends AbstractDisplayWidget<OutputXYData, Metadata> implements WidgetGUI {
	private String widgetId;
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;
	//private static Logger log = Logging.getLogger(SimpleXYPlotWidget.class);
	private XYChart chart;
	private DoubleDataSet ds;
//	private static final DefaultMarker[] symbols = { //
//			DefaultMarker.RECTANGLE, //
//			DefaultMarker.DIAMOND, //
//			DefaultMarker.CIRCLE, //
//			DefaultMarker.CROSS, //
//			DefaultMarker.RECTANGLE2, //
//			DefaultMarker.DIAMOND2, //
//			DefaultMarker.CIRCLE2, //
//	};
	private int symbolSize;
	private DefaultMarker symbol;

	public SimpleXYPlotWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.XY);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		// dataSetMap = new HashMap<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.widgetId = id;
		policy.setProperties(id, properties);

	}

	/*- TIPS
	 * ((Region) currentChart.getYAxis()).lookup(".axis-label").setStyle("-fx-text-fill: green;");
	 * currentChart.getYAxis().setLabel("Current");
	 * currentChart.getYAxis().setSide(Side.RIGHT);
	 * currentChart.getDatasets().addAll(createSeries());
	*/
	private static int bufferSize = 1000;

	@Override
	public void onMetaDataMessage(Metadata meta) {
		Platform.runLater(() -> {
//			for (String key : meta.properties().getKeysAsSet()) {
//				System.out.println(key+"\t"+meta.properties().getProperty(key));
//			}
			DataLabel dlx = (DataLabel) meta.properties().getPropertyValue("x.hlabel");
			DataLabel dly = (DataLabel) meta.properties().getPropertyValue("xnew.hlabel");
			ds = new DoubleDataSet(dlx.toString() + "|" + dly.toString());
			ds.getStyle();
			ErrorDataSetRenderer rndr = new ErrorDataSetRenderer();
			rndr.setErrorType(ErrorStyle.NONE);
			rndr.setPolyLineStyle(LineStyle.NONE);
			rndr.setMarkerSize(symbolSize);
			rndr.setMarker(symbol);
			rndr.setPointReduction(true);
			rndr.setDrawMarker(true);
			DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) rndr.getRendererDataReducer();
			reductionAlgorithm.setMinPointPixelDistance(0);

			chart.getRenderers().setAll(rndr);
			chart.getDatasets().addAll(ds);
			String axisUnit = "units";
			chart.getXAxis().set(dlx.toString(), axisUnit);
		
			chart.getYAxis().set(dly.toString());
			chart.getXAxis().setUnit((String) meta.properties().getPropertyValue("x.units"));
			chart.getYAxis().setUnit((String) meta.properties().getPropertyValue("y.units"));
			chart.setLegendVisible(false);
			
			timeFormatter.onMetaDataMessage(meta);
			TimeUnits tu = (TimeUnits) meta.properties().getPropertyValue(P_TIMEMODEL_TU.key());
			int nTu = (Integer) meta.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
			// show this somewhere
		});

	}

	@Override
	public void onDataMessage(OutputXYData data) {

		if (policy.canProcessDataMessage(data)) {
//			Platform.runLater(() -> {
				ds.add(data.getX(), data.getY());
//			});
		}
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			if (ds != null)// called before onMetaDataMessage()
				ds.clearData();
		}
	}

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane content = new BorderPane();
		final DefaultNumericAxis xAxis1 = new DefaultNumericAxis("", "x label");
		// xAxis1.setUnitScaling(MetricPrefix.);
		final DefaultNumericAxis yAxis1 = new DefaultNumericAxis("", "?");
//		xAxis1.setAutoRangeRounding(true);
//		yAxis1.setAutoRangeRounding(true);

		xAxis1.setTimeAxis(false);
		yAxis1.setTimeAxis(false);

		xAxis1.invertAxis(false);
		yAxis1.invertAxis(false);

//		xAxis1.setForceZeroInRange(true);
//		yAxis1.setForceZeroInRange(true);

		xAxis1.setTickLabelRotation(45);

		xAxis1.setUnit("x unit?");
		yAxis1.setUnit("y unit?");

		xAxis1.set("x label");
		yAxis1.set("y label");

		chart = new XYChart(xAxis1, yAxis1);
		chart.legendVisibleProperty().set(true);
		chart.setAnimated(false);
		content.setCenter(chart);
		content.setRight(new Label(" "));
		chart.getPlugins().add(new Zoomer());
//		chart.getPlugins().add(new Panner());// confusing!
//		chart.getPlugins().add(new EditAxis());// crashes
		chart.getPlugins().add(new TableViewer());
		chart.getPlugins().add(new DataPointTooltip());
		//chart.getPlugins().add(new ParameterMeasurements());// crashes
		getUserPreferences();

		return content;
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
				for (Renderer renderer : chart.getRenderers()) {
					ErrorDataSetRenderer r = (ErrorDataSetRenderer) renderer;
					r.setMarkerSize(symbolSize);
				}
				chart.requestLayout();
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
