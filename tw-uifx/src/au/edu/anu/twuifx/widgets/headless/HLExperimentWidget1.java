/**************************************************************************
 *  TW-CORE - 3Worlds Core classes and methods                            *
 *                                                                        *
 *  Copyright 2018: Shayne Flint, Jacques Gignoux & Ian D. Davies         *
 *       shayne.flint@anu.edu.au                                          *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            *
 *                                                                        *
 *  TW-CORE is a library of the principle components required by 3W       *
 *                                                                        *
 **************************************************************************
 *  This file is part of TW-CORE (3Worlds Core).                          *
 *                                                                        *
 *  TW-CORE is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-CORE is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-CORE.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
 *                                                                        *
 **************************************************************************/
package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.Output0DData;
import au.edu.anu.twcore.data.runtime.Output0DMetadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.experiment.ExpFactor;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.widgets.helpers.SimCloneWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import fr.cnrs.iees.identity.impl.LocalScope;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.ExperimentDesignType;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;
import fr.ens.biologie.generic.utils.Statistics;

/**
 * @author Ian Davies -22 Feb 2020
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
	private Map<String,ExpFactor> factors;
	private Map<String, Object> baseline;
	private ExperimentDesignType edt;
	private int nReps;
	private int nLines;
	private String outputDir;
	private String precis;

	final private Map<Integer, TreeMap<String, List<Double>>> simulatorDataSetMap;

	public HLExperimentWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM0);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		simulatorDataSetMap = new ConcurrentHashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		policy.setProperties(id, properties);
		widgetId = id;
		if (properties.hasProperty(P_DESIGN_TYPE.key())) {
			edt = (ExperimentDesignType) properties.getPropertyValue(P_DESIGN_TYPE.key());
			treatmentList = (List<List<Property>>) properties.getPropertyValue("TreatmentList");
			factors = (Map<String,ExpFactor>) properties.getPropertyValue("Factors");
			baseline = (Map<String, Object>) properties.getPropertyValue("Baseline");
			nReps = (Integer) properties.getPropertyValue(P_EXP_NREPLICATES.key());
		}
		nLines = 1;
		if (properties.hasProperty(P_HLWIDGET_NLINES.key())) {
			nLines = (Integer) properties.getPropertyValue(P_HLWIDGET_NLINES.key());
		}
		outputDir = (String) properties.getPropertyValue(P_EXP_DIR.key());
		precis = (String) properties.getPropertyValue(P_EXP_PRECIS.key());
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
			 * TODO: Check if this comment is still the case.
			 * when debugging a headless sim, enable the line below because if setting a
			 * breakpoint in writeData(), the program will exit during debugging
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

		if (treatmentList.isEmpty()) {
			for (Map.Entry<Integer, TreeMap<String, List<Double>>> simEntry : simulatorDataSetMap.entrySet()) {
				TreeMap<String, List<Double>> simData = simEntry.getValue();
				for (Map.Entry<String, List<Double>> simTimeSeries : simData.entrySet()) {
					String seriesKey = simTimeSeries.getKey();
					result += sep + seriesKey;
				}
			}

		} else
			for (int i = 0; i < nReps; i++) {
				for (List<Property> props : treatmentList) {
					String colHeader = i + ":";
					for (Property p : props) {
						ExpFactor factor = factors.get(p.getKey());
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
		for (List<Property> props : treatmentList) {
			String colHeader = "";
			for (Property p : props) {
				ExpFactor factor = factors.get(p.getKey());
//				colHeader += "_" + p.getKey() + "[" + p.getValue() + "]";
				colHeader += "_" + factor.getName() + "[" + factor.getValueName(p) + "]";
			}
			colHeader = colHeader.replaceFirst("_", "");
			result += sep + colHeader;
		}

		return result.replaceFirst(sep, "");
	}

	private void writeData() {
		// use a local scope to ensure unique file names and so prevent file overwrites
		LocalScope scope = new LocalScope("Files");
		File dir = Project.makeFile(ProjectPaths.RUNTIME, outputDir);
		dir.mkdirs();
		for (String fileName : dir.list()) {
			int dotIndex = fileName.lastIndexOf('.');
			fileName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
			scope.newId(true, fileName);
		}
		String widgetDirName = scope.newId(false, widgetId + "0").id();

		SaveProjectDesign(widgetDirName);

		int max = 0;
		Map<String, List<List<Double>>> seriesMap = new HashMap<>();
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
			if (edt != null)
				switch (edt) {
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
		// TODO make abbrev of factor levels
		List<Double> sample = new ArrayList<>();
		for (int c = 0; c < data.size(); c++) {
			List<Double> col = data.get(c);
			double sum = 0;
			for (int i = 0; i < nLines; i++)
				sum += col.get(lastNonZeroTime - 1);
			sample.add(sum / (double) nLines);
		}

		// We need just the property for header
//		int factors = treatmentList.get(0).size();
//		String h = "";
//		for (int i = 0; i < factors; i++)
//			h += "F" + i + "\t";
//		h += "RV";
		
		String h = "";
		for (Map.Entry<String, ExpFactor> entry : factors.entrySet()) {
			h+= entry.getValue().getName()+"\t";
		}
		h +="RV";

		File anovaInputFile = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDirName, name + "_AnovaInput.csv");
		List<String> fileLines = new ArrayList<>();
		fileLines.add(h);
		for (int i = 0; i < sample.size(); i++) {
			List<Property> ps = treatmentList.get(i % treatmentList.size());
			Double v = sample.get(i);
			String line = "";
			for (Property p : ps) {
				ExpFactor factor = factors.get(p.getKey());
				String s = factor.getValueName(p);
				//String s = p.getValue().toString();
//				line += p.getKey() + s + "\t";
				line += s + "\t";
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
		String anovaResultsName = name + "_anovaResults.csv";
/*-
setwd("/home/ian/.3w/project_LMDExp_2022-01-03-22-40-14-976/local/runTime/NOG-K/popWriter0")
data = read.table("population_AnovaInput.csv",sep="	",header = TRUE,dec=".")
F0 = data$F0
F1 = data$F1
F2 = data$F2
F3 = data$F3
RV = data$RV
mdl = lm(RV~F0*F1*F2*F3)
ava = anova (mdl)
write.table(ava,"population_anovaResults.csv", sep = "	")
svg(paste("population_trends.svg"),width = 5.5, height = 5.5)
oldpar <- par(mfrow = c(2,2))
plot(RV~F0, main = outputDir)
plot(RV~F1, main = outputDir)
plot(RV~F2, main = outputDir)
plot(RV~F3, main = outputDir)
dev.off()
 */
		fileLines.clear();
		fileLines.add("setwd(\"" + anovaInputFile.getParent() + "\")");
		fileLines.add("data = read.table(\"" + anovaInputFile.getName() + "\",sep=\"\t\",header = TRUE,dec=\".\")");
//		for (int i = 0; i < factors.size(); i++)
//			fileLines.add("F" + i + " = data$F" + i);
		for (Map.Entry<String, ExpFactor> entry : factors.entrySet()) 
			fileLines.add(entry.getValue().getName() + " = data$" + entry.getValue().getName());
		fileLines.add(name + " = data$RV");
		String args = name+"~";
		for (Map.Entry<String, ExpFactor> entry : factors.entrySet()) 
			args += "*"+entry.getValue().getName();
//		args += "*F" + f;
		args = args.replaceFirst("\\*", "");
		fileLines.add("mdl = lm(" + args + ")");
		fileLines.add("ava = anova (mdl)");
		fileLines.add("write.table(ava,\"" + anovaResultsName + "\", sep = \"\t\")");

		File rscriptFile = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDirName, name + "_anova.R");
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
		boolean rPresent = true;
		try {
			try {
				b.start().waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				rPresent = false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			rPresent = false;
		}

		if (rPresent) {
		try {
			File results = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDirName, anovaResultsName);
			fileLines = Files.readAllLines(results.toPath(),StandardCharsets.UTF_8);
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

			File rssqFile = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDirName, name + "_RelSumSq.csv");
			Files.write(rssqFile.toPath(), fileLines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} else {
			throw new TwuifxException("ANOVA not computed because Rscript was not found on the system.");
		}
	}

	private void processSA(String widgetDirName, String name, List<List<Double>> data, int lastNonZeroTime) {
		List<String> fileLines = new ArrayList<>();
		int nBars = treatmentList.size();
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
		File statsFile = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDirName, name + "_SA.csv");
		fileLines.clear();
		fileLines.add("Property\tAverage\tVar\tStdD\tN\tTime");
		String sep = "\t";
		for (int bar = 0; bar < stats.length; bar++) {
			Property p = treatmentList.get(bar).get(0);
			ExpFactor f = factors.get(p.getKey());
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
	}

	private void SaveProjectDesign(String widgetDirName) {
		List<String> fileLines = new ArrayList<>();
		File designFile = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDirName, "Design.csv");
		designFile.getParentFile().mkdirs();
		fileLines.clear();
		fileLines.add("Label\tValue");
		fileLines.add("Precis\t"+precis);
		for (Map.Entry<String, Object> pair : baseline.entrySet()) {
			fileLines.add(pair.getKey() + "\t" + pair.getValue());
		}
		if (treatmentList != null && !treatmentList.isEmpty()) {
			fileLines.add("\nSimulator\tSetting(s)");
			for (int i = 0; i < treatmentList.size(); i++) {
				List<Property> list = treatmentList.get(i);
				fileLines.add(i + "\t" + list.toString());
			}
		}
		try {
			Files.write(designFile.toPath(), fileLines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private int processSeries(String widgetDirName, String header, String name, List<List<Double>> data, int max) {
		String sep = "\t";
		int lastNonZeroTime = Integer.MAX_VALUE;
		File seriesFile = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDirName, name + ".csv");
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

		File averageFile = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDirName, name + "_avg.csv");
		File varianceFile = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDirName, name + "_var.csv");
		List<String> fileLinesAvg = new ArrayList<>();
		List<String> fileLinesVar = new ArrayList<>();
		String headerAvg = getAveragesHeader();
		fileLinesAvg.add(headerAvg);
		fileLinesVar.add(headerAvg);
		int nCols = data.size() / nReps;
		for (int row = 0; row < max; row++) {
			String lineAgv = "";
			String lineVar = "";
			for (int i = 0; i < nCols; i++) {
				Statistics stat = new Statistics();
				for (int r = 0; r < nReps; r++) {
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

		return lastNonZeroTime;

	}

}
