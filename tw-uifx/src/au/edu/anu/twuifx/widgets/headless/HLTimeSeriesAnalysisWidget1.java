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
package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import fr.cnrs.iees.omugi.collections.tables.StringTable;
import fr.cnrs.iees.omugi.graph.property.Property;
import au.edu.anu.twcore.data.runtime.*;
import au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates;
import au.edu.anu.twcore.ecosystem.runtime.tracking.AbstractDataTracker;
import au.edu.anu.twcore.experiment.ExpFactor;
import au.edu.anu.twcore.experiment.runtime.*;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.ui.runtime.*;
import au.edu.anu.twuifx.widgets.helpers.*;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.*;
import fr.cnrs.iees.twcore.constants.*;
import fr.cnrs.iees.omhtk.utils.Statistics;

/**
 * 
 * A headless widget to save time series data to file.
 * <p>
 * It writes:
 * <li>Raw time series</li>
 * <li>Average of replicates time series</li>
 * <li>Performs ANOVA analysis for and sensitivity analysis depending on
 * {@link EddReadable#getType()}</li>
 * </p>
 * <p>
 * All files are placed in uniquely named directories base on the WidgetNode's
 * id in the model configuration file.
 * 
 * @author Ian Davies -22 Feb 2020
 */

public class HLTimeSeriesAnalysisWidget1 extends AbstractDisplayWidget<Output0DData, Metadata> implements Widget {
	final private WidgetTimeFormatter timeFormatter;
	final private WidgetTrackingPolicy<TimeData> policy;
	private String widgetId;
	private Output0DMetadata tsMetadata;
	private Metadata msgMetadata;
	private StatisticalAggregatesSet sas;
	private Collection<String> sampledItems;
	private int nLines;
	private EddReadable edd;
	private boolean isMinZero;

	final private Map<Integer, TreeMap<String, List<Double>>> simulatorDataSetMap;

	/**
	 * @param statusSender The {@link StatusWidget}.
	 */
	public HLTimeSeriesAnalysisWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, AbstractDataTracker.DIM0);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		simulatorDataSetMap = new ConcurrentHashMap<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		policy.setProperties(id, properties);
		widgetId = id;
		edd = (ExperimentDesignDetails) properties.getPropertyValue(P_EXP_DETAILS.key());
		nLines = 1;
		if (properties.hasProperty(P_HLWIDGET_NLINES.key())) {
			nLines = (Integer) properties.getPropertyValue(P_HLWIDGET_NLINES.key());
		}
		isMinZero = false;
		if (properties.hasProperty(P_HLWIDGET_ZERO_MIN.key()))
			isMinZero = (Boolean) properties.getPropertyValue(P_HLWIDGET_ZERO_MIN.key());

	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
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
		// WidgetUtils.makeChannels(dl,sender,sas,sampledItems,dataSetMap);

		Map<String, List<Double>> dataSetMap = simulatorDataSetMap.get(sender);

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
			for (Map.Entry<Integer, TreeMap<String, List<Double>>> entry : simulatorDataSetMap.entrySet()) {
				TreeMap<String, List<Double>> tm = entry.getValue();
				for (Map.Entry<String, List<Double>> tmEntry : tm.entrySet()) {
					tmEntry.getValue().clear();
				}
			}

		} else if (isSimulatorState(state, finished)) {
			writeData();
		} else if (isSimulatorState(state, SimulatorStates.quitting)) {
			/**
			 * TODO: Check if this comment is still the case. when debugging a with a
			 * headless controller, enable the line below because if setting a breakpoint in
			 * writeData(), the program will exit during debugging
			 * 
			 * writeData();
			 * 
			 * we need a flag to indicate if we are running headless.
			 */
		}
	}

	@Override
	public void onDataMessage(Output0DData data) {
		if (policy.canProcessDataMessage(data))
			processDataMessage(data);
	}

	private void processDataMessage(Output0DData data) {
		int sender = data.sender();
		TreeMap<String, List<Double>> dataSetMap = simulatorDataSetMap.get(sender);
		if (dataSetMap == null) {
			dataSetMap = new TreeMap<>();
			simulatorDataSetMap.put(sender, dataSetMap);
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

	private String getHeader() {
		String result = "";
		String sep = "\t";

		if (edd.getTreatments().isEmpty()) {
			for (Map.Entry<Integer, TreeMap<String, List<Double>>> simEntry : simulatorDataSetMap.entrySet()) {
//				TreeMap<String, List<Double>> simData = simEntry.getValue();
				result += sep + simEntry.getKey() + ":Value";
//				for (Map.Entry<String, List<Double>> simTimeSeries : simData.entrySet()) {
//					String seriesKey = simTimeSeries.getKey();
//					result += sep + seriesKey;
//				}
			}

		} else
			for (int i = 0; i < edd.getReplicateCount(); i++) {
				for (List<Property> props : edd.getTreatments()) {
					String colHeader = i + ":";
					for (Property p : props) {
						ExpFactor factor = edd.getFactors().get(p.getKey());
						colHeader += "_" + factor.getName() + "[" + factor.getValueName(p) + "]";
//						
//						colHeader += "_" + p.getKey() + "[" + p.getValue() + "]";
					}
					colHeader = colHeader.replaceFirst("_", "");
					result += "\t" + colHeader;
				}
			}
		return result.replaceFirst("\t", "");

	}

	private String getAveragesHeader() {
		String result = "";
		String sep = "\t";
		for (List<Property> props : edd.getTreatments()) {
			String colHeader = "";
			for (Property p : props) {
				ExpFactor factor = edd.getFactors().get(p.getKey());
				colHeader += "_" + factor.getName() + "[" + factor.getValueName(p) + "]";
			}
			colHeader = colHeader.replaceFirst("_", "");
			result += sep + colHeader;
		}

		return result.replaceFirst(sep, "");
	}

	private void writeData() {
		// Unique file names to prevent file overwrites
		String widgetDirName = WidgetUtils.getUniqueExperimentSubdirectoryName(edd.getExpDir(), widgetId);

		SaveProjectDesign(widgetDirName);

		int max = 0;
		Map<String, List<List<Double>>> seriesMap = new HashMap<>();
		// Messy for SingleRun exp design types.
		// The header is unique for each series but not if not SingleRun.
		String header = getHeader();

		// Split into separate data series
		// one entry for each simulator
		for (Map.Entry<Integer, TreeMap<String, List<Double>>> simEntry : simulatorDataSetMap.entrySet()) {
			TreeMap<String, List<Double>> simData = simEntry.getValue();
			for (Map.Entry<String, List<Double>> simTimeSeries : simData.entrySet()) {
				String seriesKey = simTimeSeries.getKey().split(":")[1];
				List<List<Double>> lstLst = seriesMap.get(seriesKey);
				if (lstLst == null) {
					lstLst = new ArrayList<>();
					seriesMap.put(seriesKey, lstLst);
				}
				List<Double> valueList = simTimeSeries.getValue();
				lstLst.add(valueList);
				max = Math.max(max, valueList.size());
			}
		}

		for (Map.Entry<String, List<List<Double>>> series : seriesMap.entrySet()) {
			String seriesName = series.getKey();
			List<List<Double>> seriesData = series.getValue();
			int lastNonZeroTime = processSeries(widgetDirName, header, seriesName, seriesData, max);
			if (edd.getType() != null)
				switch (edd.getType()) {
				case singleRun: {
					// nothing to do except write the data series and record the exp data as above
					break;
				}
				case sensitivityAnalysis: {
					processSA(widgetDirName, seriesName, seriesData, lastNonZeroTime);
					break;
				}
				case crossFactorial: {
					processANOVA(widgetDirName, seriesName, seriesData, lastNonZeroTime);
					break;
				}
				}

		}

	}

	private void processANOVA(String widgetDirName, String name, List<List<Double>> data, int lastNonZeroTime) {
		// name = StringUtils.cap(name);
		List<Double> sample = new ArrayList<>();
		for (int c = 0; c < data.size(); c++) {
			List<Double> col = data.get(c);
			double sum = 0;
			for (int i = 0; i < nLines; i++)
				sum += col.get(lastNonZeroTime - 1);
			sample.add(sum / (double) nLines);
		}

		List<ExpFactor> orderedFactors = new ArrayList<>();
		String header = "";
		List<Property> tmpProps = edd.getTreatments().get(0);
		for (Property p : tmpProps) {
			ExpFactor factor = edd.getFactors().get(p.getKey());
			header += factor.getName() + "\t";
			orderedFactors.add(factor);
		}

		header += "RV";
		List<String> fileLines = new ArrayList<>();
		fileLines.add(header);

		for (int i = 0; i < sample.size(); i++) {
			List<Property> ps = edd.getTreatments().get(i % edd.getTreatments().size());
			Double v = sample.get(i);
			String line = "";
			for (Property p : ps) {
				ExpFactor factor = edd.getFactors().get(p.getKey());
				String s = factor.getValueName(p);
				line += s + "\t";
			}
			line += v.toString();
			fileLines.add(line);
		}

		File anovaInputFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName,
				name + "_AnovaInput.csv");

		try {
			Files.write(anovaInputFile.toPath(), fileLines, StandardCharsets.UTF_8);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String anovaResultsName = name + "_anovaResults.csv";
		List<String> anovaLines = WidgetUtils.generateANOVAScript(anovaInputFile, edd.getFactors(), orderedFactors,
				name, anovaResultsName);
		File anovaFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "_anova.R");
		WidgetUtils.saveAndExecuteScript(anovaFile, anovaLines);

		try {
			File results = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, anovaResultsName);
			fileLines = Files.readAllLines(results.toPath(), StandardCharsets.UTF_8);
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
				StringBuilder sb = new StringBuilder().append(terms.get(i)).append(sep).append(df.get(i)).append(sep)
						.append(ssq.get(i)).append(sep).append(relSsq.get(i));
				fileLines.add(sb.toString());
			}
			fileLines.add("Explained\t" + dfresiduals + "\t" + totalExplained.toString());

			File rssqFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "_RelSumSq.csv");
			Files.write(rssqFile.toPath(), fileLines, StandardCharsets.UTF_8);

			List<String> rssqPlotLines = WidgetUtils.generateRVEPlotScript(rssqFile, name);
			WidgetUtils.saveAndExecuteScript(
					Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "RVE.R"), rssqPlotLines);
			List<String> trendsBarplotLines = WidgetUtils.generateBarPlotScript(
					Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "_avg.csv"),
					edd.getFactors(), name, isMinZero);
//				for (String s:trendsBarplotLines)
//					System.out.println(s);
			WidgetUtils.saveAndExecuteScript(
					Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "_barplots.R"),
					trendsBarplotLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> boxPlotLines = WidgetUtils.generateBoxPlotScript(anovaInputFile, edd.getFactors(), orderedFactors,
				name);
		File boxChartFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "_boxplots.R");
		WidgetUtils.saveAndExecuteScript(boxChartFile, boxPlotLines);

	}

	private void processSA(String widgetDirName, String name, List<List<Double>> data, int lastNonZeroTime) {
		List<String> fileLines = new ArrayList<>();
		int nBars = edd.getTreatments().size();
		// how many indep vars? do we care here?
		Statistics[] stats = new Statistics[nBars];
		for (int bar = 0; bar < stats.length; bar++)
			stats[bar] = new Statistics();

		for (int c = 0; c < data.size(); c++) {
			int bar = c % nBars;
			List<Double> col = data.get(c);
			double sum = 0;
			for (int i = 0; i < nLines; i++) {
				sum += col.get(lastNonZeroTime - i);
			}
			stats[bar].add(sum / (double) nLines);
		}
		File statsFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "_SA.csv");
		fileLines.clear();
		fileLines.add("Property\tAverage\tVar\tStdD\tN\tTime");
		String sep = "\t";
		for (int bar = 0; bar < stats.length; bar++) {
			Property p = edd.getTreatments().get(bar).get(0);
			ExpFactor f = edd.getFactors().get(p.getKey());
			String s1 = f.getName() + "(" + f.getValueName(p) + ")";
			String s2 = Double.toString(stats[bar].average());
			String s3 = Double.toString(stats[bar].variance());
			String s4 = Double.toString(Math.sqrt(stats[bar].variance()));
			String s5 = Integer.toString(stats[bar].n());
			String s6 = Integer.toString(lastNonZeroTime + 1);
			fileLines.add(s1 + sep + s2 + sep + s3 + sep + s4 + sep + s5 + sep + s6);
		}
		try {
			Files.write(statsFile.toPath(), fileLines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// not appropriate here until we consider avg.csv column headings
//		List<String> trendsBarplotLines = WidgetUtils.generateTrendsBarPlotScript(
//				Project.makeFile(ProjectPaths.RUNTIME, edd.getExpDir(), widgetDirName, name + "_avg.csv"),
//				edd.getFactors(), name, isMinZero);
//		WidgetUtils.saveAndExecuteScript(
//				Project.makeFile(ProjectPaths.RUNTIME, edd.getExpDir(), widgetDirName, name + "_MeansPlot.R"),
//				trendsBarplotLines);

	}

	private void SaveProjectDesign(String widgetDirName) {
		File designFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, "Design.csv");
		WidgetUtils.SaveExperimentDesignDetails(edd, designFile);
	}

	private int processSeries(String widgetDirName, String header, String name, List<List<Double>> data, int max) {
		String sep = "\t";
		int lastNonZeroTime = Integer.MAX_VALUE;
		File seriesFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + ".csv");
		seriesFile.getParentFile().mkdirs();
		List<String> fileLines = new ArrayList<>();
		fileLines.add(header);
		for (int row = 0; row < max; row++) {
			String line = "";
			for (int i = 0; i < data.size(); i++) {
				String s = "";
				List<Double> col = data.get(i);
				if (row < col.size()) {
					Double d = col.get(row);
					if (d <= 0.0)
						lastNonZeroTime = Math.min(lastNonZeroTime, row);
					s = d.toString();
				}
				line += sep + s;
			}
			fileLines.add(line.replaceFirst(sep, ""));
		}
		try {
			Files.write(seriesFile.toPath(), fileLines, StandardCharsets.UTF_8);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (lastNonZeroTime == Integer.MAX_VALUE)
			lastNonZeroTime = max - 1;

		File averageFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "_avg.csv");
		File varianceFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "_var.csv");
		List<String> fileLinesAvg = new ArrayList<>();
		List<String> fileLinesVar = new ArrayList<>();
		String headerAvg = getAveragesHeader();
		fileLinesAvg.add(headerAvg);
		fileLinesVar.add(headerAvg);
		int nCols = data.size() / edd.getReplicateCount();
		for (int row = 0; row < max; row++) {
			String lineAgv = "";
			String lineVar = "";
			for (int i = 0; i < nCols; i++) {
				Statistics stat = new Statistics();
				for (int r = 0; r < edd.getReplicateCount(); r++) {
					int col = r * nCols + i;
					stat.add(data.get(col).get(row));
				}
				String a = Double.toString(stat.average());
				String v = Double.toString(stat.variance());
				lineAgv += sep + a;
				lineVar += sep + v;
			}
			fileLinesAvg.add(lineAgv.replaceFirst(sep, ""));
			fileLinesVar.add(lineVar.replaceFirst(sep, ""));
		}
		try {
			Files.write(averageFile.toPath(), fileLinesAvg, StandardCharsets.UTF_8);
			Files.write(varianceFile.toPath(), fileLinesVar, StandardCharsets.UTF_8);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		List<String> seriesScript = WidgetUtils.generateSeriesScript(edd.getType(), seriesFile, name,
				edd.getReplicateCount(), false);
		File rFile = Project.makeFile(Project.RUNTIME, edd.getExpDir(), widgetDirName, name + "_Series.R");
		WidgetUtils.saveAndExecuteScript(rFile, seriesScript);

		return lastNonZeroTime;

	}

}
