package au.edu.anu.twuifx.widgets;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMELINE_SHORTTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_NTU;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.timer.TimeUtil;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.widgets.helpers.SimCloneWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProgressWidget3 extends AbstractDisplayWidget<TimeData, Metadata> implements WidgetGUI {
	private final WidgetTimeFormatter timeFormatter;
	private final WidgetTrackingPolicy<TimeData> policy;
	private int nSenders;
	private XYChart chart;
	private Metadata metadata;
	private final Map<Integer, Long> currentSenderTimes;
	private final Map<Integer, DoubleDataSet> senderDataSetMap;
	private final long refreshRate;// ms
	private Label lblTime;
	private ErrorDataSetRenderer renderer;

	public ProgressWidget3(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		nSenders = 0;
		currentSenderTimes = new ConcurrentHashMap<>();
		senderDataSetMap = new ConcurrentHashMap<>();
		refreshRate = 250;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		timeFormatter.setProperties(id, properties);
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		nSenders++;
		if (policy.canProcessMetadataMessage(meta)) {
			metadata = meta;
		}
	}

	@Override
	public Object getUserInterfaceContainer() {
		getUserPreferences();
		timeFormatter.onMetaDataMessage(metadata);
		lblTime = new Label("");
		VBox vBox = new VBox();
		HBox line1 = new HBox(5);
		HBox line2 = new HBox(5);
		HBox line3 = new HBox(5);
		HBox line4 = new HBox(5);

		line1.getChildren().addAll(new Label("Sims:"), new Label(Integer.toString(nSenders)));
		line2.getChildren().addAll(new Label("Cores:"),
				new Label(Integer.toString(Runtime.getRuntime().availableProcessors())));
		line3.getChildren().addAll(new Label("Stop:"),
				new Label((String) metadata.properties().getPropertyValue("StoppingDesc")));
		line4.getChildren().addAll(new Label("Time:"), lblTime);

		BorderPane content = new BorderPane();
		vBox.getChildren().addAll(line1, line2, line3, line4);
		content.setTop(vBox);

		DefaultNumericAxis xAxis = new DefaultNumericAxis("Time: ", timeFormatter.getSmallest().abbreviation());
		DefaultNumericAxis yAxis = new DefaultNumericAxis("Simulator");
		chart = new XYChart(xAxis, yAxis);
		chart.setPadding(new Insets(0,10,0,0));
		

		chart.setLegendVisible(false);
		renderer = new ErrorDataSetRenderer();
		initErrorDataSetRenderer(renderer);
		chart.getRenderers().add(renderer);

		yAxis.setAutoGrowRanging(false);
		yAxis.setForceZeroInRange(true);				
		yAxis.setMax(nSenders+1);
		yAxis.setMaxMajorTickLabelCount(2);// no idea why this should be the case!

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			private long lastTime = Long.MAX_VALUE;

			@Override
			public void run() {
				long time = Math.round(getMeanTime());
				if (time != lastTime) {
					lastTime = time;
					String text = formatOutput(nSenders, time);
					Platform.runLater(() -> {
						lblTime.setText(text);
					});
				}
			}
		}, 0, refreshRate);

		content.setCenter(chart);
		return content;
	}

	@Override
	public void onDataMessage(TimeData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial)) {
			} else {
				processDataMessage(data);
			}
		}
	}

	private void processDataMessage(TimeData data) {
		int sender = data.sender();
		long time = data.time();
		currentSenderTimes.put(sender, time);
		
		Platform.runLater(() -> {
			DoubleDataSet ds = senderDataSetMap.get(sender);
			if (ds==null) {
				ds = new DoubleDataSet(Integer.toString(sender));
				senderDataSetMap.put(sender, ds);
				renderer.getDatasets().add(ds);
			}		
			ds.add(time, sender+1);
		});
		
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			currentSenderTimes.clear();
			for (DataSet ds : renderer.getDatasets()) {
				DoubleDataSet dds = (DoubleDataSet) ds;
				dds.clearData();
			}
		}

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

	private static void initErrorDataSetRenderer(final ErrorDataSetRenderer r) {
		r.setErrorType(ErrorStyle.NONE);
		r.setDashSize(0);
		r.setPointReduction(true);
		r.setDrawMarker(false);
		r.setDrawBars(false);
		final DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) r.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(1);
	}

	private double getMeanTime() {
		double sum = 0;
		double n = currentSenderTimes.size();
		for (Entry<Integer, Long> entry : currentSenderTimes.entrySet())
			sum += entry.getValue();
		if (n > 0)
			return sum / n;
		return timeFormatter.getInitialTime();
	}

	private String formatOutput(int n, long time) {
		return "[#1-" + n + "] " + timeFormatter.getTimeText(time);
	}

}
