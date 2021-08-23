package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_DATATRACKER_STATISTICS;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_DESIGN_TYPE;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_EXP_NREPLICATES;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMELINE_SHORTTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_NTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_TU;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.graph.property.Property;
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
import fr.cnrs.iees.twcore.constants.ExperimentDesignType;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.ens.biologie.generic.utils.Logging;
import fr.ens.biologie.generic.utils.Statistics;

/**
 * @author Ian Davies
 *
 * @date 22 Feb 2020
 */

public class HLExperimentWidget1 extends AbstractDisplayWidget<Output0DData, Metadata> implements Widget {
	final private WidgetTimeFormatter timeFormatter;
	final private WidgetTrackingPolicy<TimeData> policy;
	private String widgetId;
	private Output0DMetadata tsMetadata;
	private Metadata msgMetadata;
	private StatisticalAggregatesSet sas;
	private Collection<String> sampledItems;
	private List<List<Property>> treatmentList;
	private ExperimentDesignType edt;
	private int nReps;
	private int nSenders;

	final private Map<Integer, TreeMap<String, List<Double>>> senderDataSetMap;

	public HLExperimentWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM0);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		senderDataSetMap = new ConcurrentHashMap<>();
		nSenders = 0;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		policy.setProperties(id, properties);
		widgetId = id;
		if (properties.hasProperty(P_DESIGN_TYPE.key())) {
			edt = (ExperimentDesignType) properties.getPropertyValue(P_DESIGN_TYPE.key());
			treatmentList = (List<List<Property>>) properties.getPropertyValue("TreatmentList");
			nReps = (Integer) properties.getPropertyValue(P_EXP_NREPLICATES.key());
		}
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		nSenders++;
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

	@Override
	public void onDataMessage(Output0DData data) {
		if (policy.canProcessDataMessage(data))
			processDataMessage(data);
	}

	private void processDataMessage(Output0DData data) {
		int sender = data.sender();
		TreeMap<String, List<Double>> dataSetMap = senderDataSetMap.get(sender);
		if (dataSetMap==null) {
			dataSetMap = new TreeMap<>();
			senderDataSetMap.put(sender, dataSetMap);
			for (DataLabel dl : tsMetadata.doubleNames())
				makeChannels(dl, sender);
			for (DataLabel dl : tsMetadata.intNames())
				makeChannels(dl, sender);
		}
		
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

	private void writeData() {
		final TimeUnits timeUnit = (TimeUnits) msgMetadata.properties().getPropertyValue(P_TIMELINE_SHORTTU.key());
		final int nTimeUnits = (Integer) msgMetadata.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
		final String timeUnitName = TimeUtil.timeUnitAbbrev(timeUnit, nTimeUnits);
		String header = TimeUtil.timeUnitName(timeUnit, nTimeUnits);
		List<List<Double>> cols = new ArrayList<>();
		int max = 0;
		for (Map.Entry<Integer, TreeMap<String, List<Double>>> entry : senderDataSetMap.entrySet()) {
			TreeMap<String, List<Double>> tm = entry.getValue();
			for (Map.Entry<String, List<Double>> tmEntry : tm.entrySet()) {
				List<Double> valueList = tmEntry.getValue();
				// problem: unused cols will be zero
				if (!valueList.isEmpty()) {
					cols.add(tmEntry.getValue());
					header += "\t" + tmEntry.getKey();
					max = Math.max(max, tmEntry.getValue().size());
				}
			}
		}

		int lastNonZeroTime = Integer.MAX_VALUE;
		int longestSeries = 0;
		String fileName = widgetId + "_" + RunTimeId.runTimeId() + ".csv";
		File outFile = Project.makeFile(ProjectPaths.RUNTIME, "output", fileName);
		outFile.getParentFile().mkdirs();
		List<String> fileLines = new ArrayList<>();
		fileLines.add(header);
		for (int lineNo = 0; lineNo < max; lineNo++) {
			Integer step = lineNo + 1;
			String line = step.toString();
			for (int i = 0; i < cols.size(); i++) {
				String s = "";
				List<Double> col = cols.get(i);
				if (lineNo < col.size()) {
					Double d = col.get(lineNo);
					if (d <= 0.0)
						lastNonZeroTime = Math.min(lastNonZeroTime, lineNo);
					s = d.toString();
				}
				line += "\t" + s;
			}
			fileLines.add(line);
		}
		try {
			Files.write(outFile.toPath(), fileLines, StandardCharsets.UTF_8);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (lastNonZeroTime == Integer.MAX_VALUE)
			lastNonZeroTime = max - 1;
		if (edt != null)
			switch (edt) {
			case sensitivityAnalysis: {
				int nBars = treatmentList.size();
				// how many indep vars? do we care here?
				Statistics[] stats = new Statistics[nBars];
				for (int bar = 0; bar < stats.length; bar++)
					stats[bar] = new Statistics();

				for (int c = 0; c < cols.size(); c++) {
					int bar = c % nBars;
					List<Double> col = cols.get(c);
					Double iv = col.get(lastNonZeroTime);
					stats[bar].add(iv);
				}
				String statsName = widgetId + "SA_" + RunTimeId.runTimeId() + ".csv";
				File statsFile = Project.makeFile(ProjectPaths.RUNTIME, "output", statsName);
				fileLines.clear();
				fileLines.add("Property\tAverage\tVar\tStdD\tN\tTime");
				String sep = "\t";
				for (int bar = 0; bar < stats.length; bar++) {
					Property p = treatmentList.get(bar).get(0);
					String s1 = p.getKey() + "(" + p.getValue() + ")";
					String s2 = Double.toString(stats[bar].average());
					String s3 = Double.toString(stats[bar].variance());
					String s4 = Double.toString(Math.sqrt(stats[bar].variance()));
					String s5 = Integer.toString(stats[bar].n());
					String s6 = Integer.toString(lastNonZeroTime);
					fileLines.add(s1 + sep + s2 + sep + s3 + sep + s4 + sep + s5 + sep + s6);
				}
				try {
					Files.write(statsFile.toPath(), fileLines, StandardCharsets.UTF_8);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			default: {
				// TODO make abbrev of factor levels
				List<Double> sample = new ArrayList<>();
				for (int c = 0; c < cols.size(); c++) {
					List<Double> col = cols.get(c);
					sample.add(col.get(lastNonZeroTime));
				}

				int factors = treatmentList.get(0).size();
				String h = "";
				for (int i = 0; i < factors; i++)
					h += "F" + i + "\t";
				// TODO only one response variable allowed at the moment
				h += "RV";
				String anovaInputName = widgetId + "AnovaInput_" + RunTimeId.runTimeId() + ".csv";
				File anovaInputFile = Project.makeFile(ProjectPaths.RUNTIME, "output", anovaInputName);
				fileLines.clear();

				fileLines.add(h);
				for (int i = 0; i < sample.size(); i++) {
					List<Property> ps = treatmentList.get(i % treatmentList.size());
					Double v = sample.get(i);
					String line = "";
					for (Property p : ps) {
						String s = p.getValue().toString();
						line += p.getKey() + s + "\t";
					}
					line += v.toString();
					fileLines.add(line);
				}
				try {
					Files.write(anovaInputFile.toPath(), fileLines, StandardCharsets.UTF_8);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				fileLines.clear();
				fileLines.add("setwd(\"" + anovaInputFile.getParent() + "\")");
				fileLines.add(
						"data = read.table(\"" + anovaInputFile.getName() + "\",sep=\"\t\",header = TRUE,dec=\".\")");
				for (int i = 0; i < factors; i++)
					fileLines.add("F" + i + " = data$F" + i);
				fileLines.add("RV = data$RV");
				String args = "RV~";
				for (int f = 0; f < factors; f++)
					args += "*F" + f;
				args = args.replaceFirst("\\*", "");
				fileLines.add("mdl = lm(" + args + ")");
				fileLines.add("ava = anova (mdl)");
				fileLines.add("write.table(ava,\"anovaResults.csv\", sep = \"\t\")");
				String rsciptName = "anova.R";
				File rscriptFile = Project.makeFile(ProjectPaths.RUNTIME, "output", rsciptName);
				try {
					Files.write(rscriptFile.toPath(), fileLines, StandardCharsets.UTF_8);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// exec R with rscriptFile
				List<String> commands = new ArrayList<>();
				commands.add("Rscript");
				commands.add(rscriptFile.getAbsolutePath());
				ProcessBuilder b = new ProcessBuilder(commands);
				b.directory(new File(rscriptFile.getParent()));
				b.inheritIO();
				try {
					try {
						b.start().waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					File results = Project.makeFile(ProjectPaths.RUNTIME, "output", "anovaResults.csv");
					fileLines = Files.readAllLines(results.toPath());
					String line = "Terms\t" + fileLines.get(0);
					fileLines.set(0, line);
					// Terms "Df" "Sum Sq" "Mean Sq" "F value" "Pr(>F)"
					List<String> terms = new ArrayList<>();
					List<Integer> df = new ArrayList<>();
					List<Double> ssq = new ArrayList<>();
					double totalSsq = 0;
					for (int l = 1; l < fileLines.size() - 1; l++) {
						String[] parts = fileLines.get(l).split("\t");
						terms.add(parts[0]);
						df.add(Integer.parseInt(parts[1]));
						double d = Double.parseDouble(parts[2]);
						totalSsq += d;
						ssq.add(d);
					}
					double residuals = Double.parseDouble(fileLines.get(fileLines.size() - 1).split("\t")[2]);
					int dfresiduals = Integer.parseInt(fileLines.get(fileLines.size() - 1).split("\t")[1]);
					totalSsq += residuals;
					// make relative
					List<Double> relSsq = new ArrayList<>();
					Double totalExplained = 0.0;
					for (Double d : ssq) {
						double e = d / totalSsq;
						relSsq.add(e);
						totalExplained += e;
					}
					fileLines.clear();
					String sep = "\t";
					fileLines.add("Terms\tdf\tSum sq\tRel sum sq");
					for (int i = 0; i < relSsq.size(); i++) {
						StringBuilder sb = new StringBuilder().append(terms.get(i)).append(sep).append(df.get(i))
								.append(sep).append(ssq.get(i)).append(sep).append(relSsq.get(i));
						fileLines.add(sb.toString());
					}
					fileLines.add("Explained\t" + dfresiduals + "\t" + totalExplained.toString());
					fileLines.add("");
					fileLines.add("Legend");

					for (int i = 0; i < factors; i++) {
						String key = treatmentList.get(0).get(i).getKey();
						String sym = "F" + i;
						fileLines.add(new StringBuilder().append(sym).append(sep).append(key).toString());
					}

					File rssqFile = Project.makeFile(ProjectPaths.RUNTIME, "output", "RelativeSumSq.csv");
					Files.write(rssqFile.toPath(), fileLines, StandardCharsets.UTF_8);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			}

	}

}
