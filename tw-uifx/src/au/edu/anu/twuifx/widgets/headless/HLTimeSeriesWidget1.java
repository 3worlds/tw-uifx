package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_DATATRACKER_STATISTICS;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMELINE_SHORTTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_NTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_TU;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.Output0DData;
import au.edu.anu.twcore.data.runtime.Output0DMetadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.simulator.RunTimeId;
import au.edu.anu.twcore.ecosystem.runtime.timer.TimeUtil;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.widgets.helpers.RangeWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.SimCloneWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.ens.biologie.generic.utils.Logging;

/**
 * @author Ian Davies
 *
 * @date 22 Feb 2020
 */

public class HLTimeSeriesWidget1 extends AbstractDisplayWidget<Output0DData, Metadata> implements Widget {
	final private WidgetTimeFormatter timeFormatter;
	final private WidgetTrackingPolicy<TimeData> policy;
	private String widgetId;
	private PrintWriter writer;
	private Output0DMetadata tsMetadata;
	private Metadata msgMetadata;
	private StatisticalAggregatesSet sas;
	private Collection<String> sampledItems;

	final private Map<Integer, TreeMap<String, List<Double>>> senderDataSetMap;

	public HLTimeSeriesWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM0);
		timeFormatter = new WidgetTimeFormatter();
		policy = new RangeWidgetTrackingPolicy();
		senderDataSetMap = new ConcurrentHashMap<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		policy.setProperties(id, properties);
		widgetId = id;
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// Policy must ensure we're not looking at another simulator
		if (policy.canProcessMetadataMessage(meta)) {
			msgMetadata = meta;
			tsMetadata = (Output0DMetadata) msgMetadata.properties().getPropertyValue(Output0DMetadata.TSMETA);
			timeFormatter.onMetaDataMessage(msgMetadata);
			sas = null;
			if (msgMetadata.properties().hasProperty(P_DATATRACKER_STATISTICS.key()))
				sas = (StatisticalAggregatesSet) msgMetadata.properties()
						.getPropertyValue(P_DATATRACKER_STATISTICS.key());
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
				throw new TwuifxException("No numeric items have been defined for '" + widgetId + "'.");
			int nModifiers = 0;
			if (sas != null)
				nModifiers += sas.values().size();
			if (sampledItems != null)
				nModifiers += sampledItems.size();

			for (int sender = policy.getDataMessageRange().getFirst(); sender <= policy.getDataMessageRange()
					.getLast(); sender++) {
				senderDataSetMap.put(sender, new TreeMap<String, List<Double>>());
				for (DataLabel dl : tsMetadata.doubleNames())
					makeChannels(dl, sender);
				for (DataLabel dl : tsMetadata.intNames())
					makeChannels(dl, sender);

			}

		}
	}

	private void makeChannels(DataLabel dl, int sender) {

		Map<String, List<Double>> dataSetMap = senderDataSetMap.get(sender);

		if (sas != null) {
			for (StatisticalAggregates sa : sas.values()) {
				String key = sa.name() + DataLabel.HIERARCHY_DOWN + dl.toString();
				List<Double> ds = new ArrayList<>();
				dataSetMap.put(key, ds);
			}
		} else if (sampledItems != null) {
			for (String si : sampledItems) {
				String key = si + DataLabel.HIERARCHY_DOWN + dl.toString();
				List<Double> ds = new ArrayList<>();
				dataSetMap.put(key, ds);
			}
		} else {
			String key = sender + ":" + dl.toLazyString();
			List<Double> ds = new ArrayList<>();
			dataSetMap.put(key, ds);
		}
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			// possible in interactive mode
			for (Map.Entry<Integer, TreeMap<String, List<Double>>> entry : senderDataSetMap.entrySet()) {
				TreeMap<String, List<Double>> tm = entry.getValue();
				for (Map.Entry<String, List<Double>> tmEntry : tm.entrySet()) {
					tmEntry.getValue().clear();
				}
			}

		} else if (isSimulatorState(state, finished)) {
			writeData();

			// write data and clear

//			writer.close();
		}
	}

	private void writeData() {
		String fileName = widgetId + "[" + RunTimeId.runTimeId() + "].txt";
		File outFile = Project.makeFile(ProjectPaths.RUNTIME, "output", fileName);
		outFile.getParentFile().mkdirs();
		final TimeUnits timeUnit = (TimeUnits) msgMetadata.properties().getPropertyValue(P_TIMELINE_SHORTTU.key());
		final int nTimeUnits = (Integer) msgMetadata.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
		final String timeUnitName = TimeUtil.timeUnitAbbrev(timeUnit, nTimeUnits);
		String header = TimeUtil.timeUnitName(timeUnit, nTimeUnits);
		List<List<Double>> cols = new ArrayList<>();
		int max = 0;
		for (Map.Entry<Integer, TreeMap<String, List<Double>>> entry : senderDataSetMap.entrySet()) {
			TreeMap<String, List<Double>> tm = entry.getValue();
			for (Map.Entry<String, List<Double>> tmEntry : tm.entrySet()) {
				header += "\t" + tmEntry.getKey();
				max = Math.max(max, tmEntry.getValue().size());
				cols.add(tmEntry.getValue());
			}
		}

		try {
			writer = new PrintWriter(outFile);
			writer.write(header+"\n");
			for (int line = 0; line < max; line++) { 
				Integer step = line+1;
				writer.write(step.toString());
				for (int i = 0; i < cols.size(); i++) {
					String s = "";
					List<Double> col = cols.get(i);
					if (line<col.size())
						s = col.get(line).toString();
					writer.write("\t" + s);
				}
				writer.write("\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onDataMessage(Output0DData data) {
		if (policy.canProcessDataMessage(data))
			processDataMessage(data);

	}

	private void processDataMessage(Output0DData data) {
		Map<String, List<Double>> dataSetMap = senderDataSetMap.get(data.sender());
		int sender = data.sender();

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
			List<Double> ds = dataSetMap.get(key);
			double y = data.getDoubleValues()[tsMetadata.indexOf(dl)];
			if (ds != null)
				ds.add(y);
		}

		for (DataLabel dl : tsMetadata.intNames()) {
			String key;
			if (itemId != null)
				key = sender + ":" + itemId + DataLabel.HIERARCHY_DOWN + dl.toLazyString();
			else
				key = sender + ":" + dl.toLazyString();
			List<Double> ds = dataSetMap.get(key);
			double y = data.getIntValues()[tsMetadata.indexOf(dl)];
			if (ds != null)
				ds.add(y);
		}

	}

}
