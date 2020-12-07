package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_DATATRACKER_STATISTICS;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_NTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_TU;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
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
import au.edu.anu.twuifx.widgets.helpers.SimCloneWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
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

public class HLSimpleTimeSeriesWidget extends AbstractDisplayWidget<Output0DData, Metadata> implements Widget {
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;
	private File outFile;
	private String widgetId;
	private PrintWriter writer;
	private Output0DMetadata tsMeta;
	private Metadata metadata;
	private StatisticalAggregatesSet sas;
	private Collection<String> sampledItems;

	private static Logger log = Logging.getLogger(HLSimpleTimeSeriesWidget.class);
	private static String sep = "\t";

	public HLSimpleTimeSeriesWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM0);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		log.info(this.toString());
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
			log.info(meta.toString());
			metadata = meta;
			tsMeta = (Output0DMetadata) meta.properties().getPropertyValue(Output0DMetadata.TSMETA);

			// if file open close it. It will be overwritten.
			if (writer != null) {
				log.info("Closing file!");
				writer.close();
			}
			// we have to assume a project dir exists even in openmole.
			String fileName = widgetId + "[" + RunTimeId.runTimeId() + "][" + meta.sender() + "].txt";
			outFile = Project.makeFile(ProjectPaths.RUNTIME, "output", fileName);
			outFile.getParentFile().mkdirs();
			try {
				log.info("Opening file!");
				timeFormatter.onMetaDataMessage(meta);
				writer = new PrintWriter(outFile);
				TimeUnits tu = (TimeUnits) meta.properties().getPropertyValue(P_TIMEMODEL_TU.key());
				int nTu = (Integer) meta.properties().getPropertyValue(P_TIMEMODEL_NTU.key());

				String header = TimeUtil.timeUnitName(tu, nTu);
				sas = null;
				if (metadata.properties().hasProperty(P_DATATRACKER_STATISTICS.key()))
					sas = (StatisticalAggregatesSet) metadata.properties()
							.getPropertyValue(P_DATATRACKER_STATISTICS.key());
				if (metadata.properties().hasProperty("sample")) {
					StringTable st = (StringTable) metadata.properties().getPropertyValue("sample");
					if (st != null) {
						sampledItems = new ArrayList<>(st.size());
						for (int i = 0; i < st.size(); i++)
							sampledItems.add(st.getWithFlatIndex(i));
					}
				}

				for (DataLabel dl : tsMeta.doubleNames()) {
					header += makeChannels(dl);

				}

				for (DataLabel dl : tsMeta.intNames())
					header += makeChannels(dl);

				writer.write(header + "\n");

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private String makeChannels(DataLabel dl) {
		String header = "";
		if (sas != null) {
			for (StatisticalAggregates sa : sas.values()) {
				String key = sa.name() + DataLabel.HIERARCHY_DOWN + dl.toString();
				header += sep+key;
			}
		} else if (sampledItems != null) {
			for (String si : sampledItems) {
				String key = si + DataLabel.HIERARCHY_DOWN + dl.toString();
				header += sep+key;
			}
		} else {
			header += sep+dl.toString();
		}
		return header;
	}

	@Override
	public void onDataMessage(Output0DData data) {
		if (policy.canProcessDataMessage(data)) {
			log.info(data.toString());
			String line = Long.toString(data.time());

			// TODO: Unchecked
			for (DataLabel dl : tsMeta.doubleNames()) {
				double y = data.getDoubleValues()[tsMeta.indexOf(dl)];
				line += (sep + Double.toString(y));
			}
			for (DataLabel dl : tsMeta.intNames()) {
				Long y = data.getIntValues()[tsMeta.indexOf(dl)];
				line += (sep + Long.toString(y));
			}
			writer.write(line + "\n");
		}
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, finished) || isSimulatorState(state, waiting)) {
			log.info("closing file!");
			writer.close();
		}
	}

}
