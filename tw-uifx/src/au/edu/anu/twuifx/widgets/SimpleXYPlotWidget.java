package au.edu.anu.twuifx.widgets;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_NTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_TU;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.spi.DoubleDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

public class SimpleXYPlotWidget extends AbstractDisplayWidget<OutputXYData, Metadata> implements WidgetGUI {
	private String widgetId;
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;
	private Map<String, DoubleDataSet> dataSetMap;
	private static Logger log = Logging.getLogger(SimpleXYPlotWidget.class);
	private XYChart chart;

	public SimpleXYPlotWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.XY);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		dataSetMap = new HashMap<>();

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
			// loop on some meta data type to create these
			String name = "Some unique name";
			DoubleDataSet ds = new DoubleDataSet(name, bufferSize);
			dataSetMap.put(name, ds);
			// end loop
			dataSetMap.entrySet().forEach(entry -> {
				ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
				renderer.setErrorType(ErrorStyle.NONE);
				renderer.setPolyLineStyle(LineStyle.NONE);
				renderer.setMarkerSize(4);
//				renderer.setMarker(DefaultMarker.DIAMOND);// filled
//				renderer.setMarker(DefaultMarker.DIAMOND2);// no fill
				renderer.setMarker(DefaultMarker.CROSS);
//				renderer.setMarker(DefaultMarker.CIRCLE);// filled
//				renderer.setMarker(DefaultMarker.CIRCLE2);// no fill
//				renderer.setMarker(DefaultMarker.RECTANGLE);// filled
//				renderer.setMarker(DefaultMarker.RECTANGLE2);// no fill


				chart.getRenderers().setAll(renderer);
				chart.getDatasets().addAll(ds);

				// ------------------ dummy data
				for (int i = 0; i < 100; i++)
					ds.add(Math.random(), Math.random());
				//-----------------------------

			});
			timeFormatter.onMetaDataMessage(meta);
			TimeUnits tu = (TimeUnits) meta.properties().getPropertyValue(P_TIMEMODEL_TU.key());
			int nTu = (Integer) meta.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
			// show this somewhere

		});

	}

	@Override
	public void onDataMessage(OutputXYData data) {

		// JG: debug
		System.out.println("Data message received: x="+data.getX()+" y="+data.getY());

		if (policy.canProcessDataMessage(data)) {
			Platform.runLater(() -> {
				/*- loop on x,y data pairs{
				   String name = name for this pair
				   DoubleDataSet ds = dataSetMap.get(name);
				   double/int get x value
				   double/int get y value
				   ds.add(x,y);
					}*/

			});
		}
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			dataSetMap.entrySet().forEach(entry -> {
				entry.getValue().clearData();
			});
		}
	}

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane content = new BorderPane();
		final DefaultNumericAxis xAxis1 = new DefaultNumericAxis("", "x label");
		//xAxis1.setUnitScaling(MetricPrefix.);
		final DefaultNumericAxis yAxis1 = new DefaultNumericAxis("", "?");
		xAxis1.setAutoRangeRounding(true);
		yAxis1.setAutoRangeRounding(true);

		xAxis1.setTimeAxis(false);
		yAxis1.setTimeAxis(false);

		xAxis1.invertAxis(false);
		yAxis1.invertAxis(false);

		xAxis1.setForceZeroInRange(true);
		yAxis1.setForceZeroInRange(true);

		xAxis1.setTickLabelRotation(45);

		xAxis1.setUnit("x unit?");
		yAxis1.setUnit("y unit?");

		xAxis1.setLabel("x label");
		yAxis1.setLabel("y label");

		chart = new XYChart(xAxis1, yAxis1);
		chart.legendVisibleProperty().set(true);
		chart.setAnimated(false);
		content.setCenter(chart);
		content.setRight(new Label(" "));


		getUserPreferences();

		return content;
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putUserPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getUserPreferences() {
		// TODO Auto-generated method stub

	}

}
