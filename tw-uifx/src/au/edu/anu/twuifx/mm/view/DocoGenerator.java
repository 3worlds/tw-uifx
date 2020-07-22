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
package au.edu.anu.twuifx.mm.view;

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.get;
import static fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;

import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.twcore.ecosystem.runtime.timer.ClockTimer;
import au.edu.anu.twcore.ecosystem.runtime.timer.EventTimer;
import au.edu.anu.twcore.project.Project;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import static au.edu.anu.twcore.archetype.TwArchetypeConstants.*;
import org.odftoolkit.simple.style.StyleTypeDefinitions;

/**
 * @author Ian Davies
 *
 * @date 13 Jul 2020
 */

/**
 * It may be that auto generation of this document is too slow to have in the
 * check, compile deploy steps. If so, we can generate it from a menu choice.
 */
public class DocoGenerator {
	/**
	 * Obtained minimal config
	 */
	private static int baseNodes = 26;
	private static int baseEdges = 6;
	private static int baseDrvs = 0;
	private static int baseCnts = 0;
	private static int baseDecs = 0;
	private static int baseProps = 27;

	private int nNodes;
	private int nEdges;
	private int nCnts;
	private int nDrvs;
	private int nDecs;
	private int nProps;
	private int nCT;
	private int nGroups;
	private List<TreeGraphDataNode> spaces;
	private String authors;

	List<TreeGraphDataNode> timersClock;
	List<TreeGraphDataNode> timersEvent;
	List<TreeGraphDataNode> timersScenario;

	TreeGraphDataNode timeline;

	private TreeGraph<TreeGraphDataNode, ALEdge> cfg;

	private static final int level1 = 1;
	private static final int level2 = 2;
	private static final int level3 = 3;

	// NB: Ignore ui. experiment and snippets
	private static List<String> allowedNodes = new ArrayList<>();
	static {
		allowedNodes.add(N_DIMENSIONER.label());
		allowedNodes.add(N_TABLE.label());
		allowedNodes.add(N_RECORD.label());
		allowedNodes.add(N_FIELD.label());
		allowedNodes.add(N_RNG.label());
		allowedNodes.add(N_SYSTEM.label());
		allowedNodes.add(N_DYNAMICS.label());
		allowedNodes.add(N_TIMELINE.label());
		allowedNodes.add(N_TIMER.label());
		allowedNodes.add(N_PROCESS.label());
		allowedNodes.add(N_FUNCTION.label());
		allowedNodes.add(N_LIFECYCLE.label());
		allowedNodes.add(N_RECRUIT.label());
		allowedNodes.add(N_PRODUCE.label());
		allowedNodes.add(N_INITFUNCTION.label());
		allowedNodes.add(N_GROUP.label());
		allowedNodes.add(N_COMPONENT.label());
		allowedNodes.add(N_STRUCTURE.label());
		allowedNodes.add(N_CATEGORYSET.label());
		allowedNodes.add(N_CATEGORY.label());
		allowedNodes.add(N_COMPONENTTYPE.label());
		allowedNodes.add(N_RELATIONTYPE.label());
		allowedNodes.add(N_SPACE.label());
		allowedNodes.add(N_PREDEFINED.label());
	}

	@SuppressWarnings("unchecked")
	public DocoGenerator(TreeGraph<TreeGraphDataNode, ALEdge> cfg) {
		this.cfg = cfg;
		// basic metrics
		for (TreeGraphDataNode n : cfg.nodes()) {
			if (allowedNodes.contains(n.classId())) {
				nNodes++;
				nProps += n.properties().size();
				for (ALEdge e : n.edges(Direction.OUT)) {
					if (allowedNodes.contains(e.endNode().classId())) {
						nEdges++;
						if (e instanceof ALDataEdge)
							nProps += ((ALDataEdge) e).properties().size();
					}
				}
			}
		}

		TreeGraphDataNode dDef = (TreeGraphDataNode) get(cfg.root().getChildren(),
				selectOne(hasTheLabel(N_DATADEFINITION.label())));
		for (TreeGraphDataNode n : cfg.subTree(dDef)) {
			if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_DRIVERS.label()))) != null)
				nDrvs += getDimensions(n);
			else if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_CONSTANTS.label()))) != null)
				nCnts += getDimensions(n);
			else if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_DECORATORS.label()))) != null)
				nDecs += getDimensions(n);
		}
		List<TreeGraphDataNode> systems = (List<TreeGraphDataNode>) get(cfg.root().getChildren(),
				selectOneOrMany(hasTheLabel(N_SYSTEM.label())));

		for (TreeGraphDataNode system : systems) {
			TreeGraphDataNode struct = (TreeGraphDataNode) get(system.getChildren(),
					selectZeroOrOne(hasTheLabel(N_STRUCTURE.label())));
			if (struct != null)
				spaces = (List<TreeGraphDataNode>) get(struct.getChildren(),
						selectZeroOrMany(hasTheLabel(N_SPACE.label())));

		}
		timeline = (TreeGraphDataNode) get(systems.get(0).getChildren(), //
				selectOne(hasTheLabel(N_DYNAMICS.label())), //
				children(), //
				selectOne(hasTheLabel(N_TIMELINE.label())));
		timersClock = new ArrayList<>();
		timersEvent = new ArrayList<>();
		timersScenario = new ArrayList<>();
		for (TreeGraphDataNode timer : (List<TreeGraphDataNode>) get(timeline.getChildren(),
				selectOneOrMany(hasTheLabel(N_TIMER.label())))) {
			if (timer.properties().getPropertyValue(twaSubclass).equals(ClockTimer.class.getName()))
				timersClock.add(timer);
			else if (timer.properties().getPropertyValue(twaSubclass).equals(EventTimer.class.getName()))
				timersEvent.add(timer);
			else
				timersScenario.add(timer);
		}

		timersClock.sort(new Comparator<TreeGraphDataNode>() {

			@Override
			public int compare(TreeGraphDataNode t1, TreeGraphDataNode t2) {
				TimeUnits tu1 = (TimeUnits) t1.properties().getPropertyValue(P_TIMEMODEL_TU.key());
				TimeUnits tu2 = (TimeUnits) t2.properties().getPropertyValue(P_TIMEMODEL_TU.key());
				return tu1.compareTo(tu2);
			}
		});
		StringTable tblAuthors = (StringTable) cfg.root().properties().getPropertyValue(P_MODEL_AUTHORS.key());
		StringBuilder authorssb = new StringBuilder();
		LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
		String datetime = currentDate.format(DateTimeFormatter.ofPattern("d-MMM-uuuu"));

		for (int i = 0; i < tblAuthors.size(); i++)
			authorssb.append(tblAuthors.getWithFlatIndex(i)).append("\n");

		authorssb.append("Date: ").append(datetime).append("\n");
		authors = authorssb.toString();

	}

	@SuppressWarnings("unchecked")
	public void generate() {

		try {
			// cf: https://odftoolkit.org/simple/document/cookbook/Text%20Document.html
			TextDocument document = TextDocument.newTextDocument();
			setHeading(document, level1);

			writePurpose(document, level2);

			writeEVS(document, level2);
			writeAgentsIndividuals(document, level3);
			writeSpatialUnits(document, level3);
			writeEnvironment(document, level3);
			writeCollectives(document, level3);
			writeProcessScheduling(document, level2);

			writeDesignConcepts(document, level2);
			writeEmergenceConcepts(document, level3);
			writeAdaptationConcepts(document, level3);
			writeObjectivesConcepts(document, level3);
			writeLearningConcepts(document, level3);
			writePredictionConcepts(document, level3);
			writeSensingConcepts(document, level3);
			writeInteractionConcepts(document, level3);
			writeStochasticityConcepts(document, level3);
			writeCollectivesConcepts(document, level3);
			writeObservationConcepts(document, level3);

			writeInitialisation(document, level2);

			writeInputData(document, level2);

			writeSubmodels(document, level2);

			writeReferences(document, level2);

			document.appendSection("end of ODD");
			document.addPageBreak();

			setAppendixTitle(document, "Appendix 1: Model specification metrics", level1);

			writeMetrics(document);

			document.appendSection("end of Appendix 1");
			document.addPageBreak();

			setAppendixTitle(document, "Appendix 2: Model specification graph", level1);

			document.addParagraph("[Add selected graph images here]");

			document.save(Project.makeFile(cfg.root().id() + ".odt"));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void writeMetrics(TextDocument doc) {
		Table table = doc.addTable(7, 2);
		int configSize = (nNodes - baseNodes) + (nEdges - baseEdges) + (nProps - baseProps);
		// cols, rows!
		table.getCellByPosition(0, 0).setStringValue("1 #Nodes");
		table.getCellByPosition(1, 0).setStringValue(Integer.toString(nNodes - baseNodes));
		table.getCellByPosition(0, 1).setStringValue("2 #Edges");
		table.getCellByPosition(1, 1).setStringValue(Integer.toString(nEdges - baseEdges));
		table.getCellByPosition(0, 2).setStringValue("3 #Constants");
		table.getCellByPosition(1, 2).setStringValue(Integer.toString(nCnts - baseCnts));
		table.getCellByPosition(0, 3).setStringValue("4 #Drivers");
		table.getCellByPosition(1, 3).setStringValue(Integer.toString(nDrvs - baseDrvs));
		table.getCellByPosition(0, 4).setStringValue("5 #Decorators");
		table.getCellByPosition(1, 4).setStringValue(Integer.toString(nDecs - baseDecs));
		table.getCellByPosition(0, 5).setStringValue("6 #Properties");
		table.getCellByPosition(1, 5).setStringValue(Integer.toString(nProps - baseProps));
		table.getCellByPosition(0, 6).setStringValue("7 configuration size (1+2+6)");
		table.getCellByPosition(1, 6).setStringValue(Integer.toString(configSize));

		// doc.addParagraph("[Add all other graph analysis measures here.]");

	}

	private void setAppendixTitle(TextDocument doc, String string, int level) {
		StringBuilder title1 = new StringBuilder();
		title1.append("Appendix 1: Model specification metrics");
		doc.addParagraph(title1.toString()).applyHeading(true, level);

		StringBuilder title2 = new StringBuilder();
		title2.append(Project.getDisplayName())//
				.append(" (Version: ")//
				.append(cfg.root().properties().getPropertyValue(P_MODEL_VERSION.key()))//
				.append(")");
		doc.addParagraph(title2.toString()).applyHeading(true, level);

		doc.addParagraph(authors);
		// rows, cols

	}

	private void writeReferences(TextDocument doc, int level) {
		doc.addParagraph("References").applyHeading(true, level);

		StringTable tblRefs = (StringTable) cfg.root().properties().getPropertyValue(P_MODEL_CITATIONS.key());
		StringBuilder refs = new StringBuilder();
		for (int i = 0; i < tblRefs.size(); i++)
			refs.append(i + 1).append(". ").append(tblRefs.getWithFlatIndex(i)).append("\n");
		doc.addParagraph(refs.toString());
	}

	private void writeSubmodels(TextDocument doc, int level) {
		doc.addParagraph("Submodels").applyHeading(true, level);

	}

	private void writeInputData(TextDocument doc, int level) {
		doc.addParagraph("Input data").applyHeading(true, level);
	}

	private void writeInitialisation(TextDocument doc, int level) {
		doc.addParagraph("Initialisation").applyHeading(true, level);
	}

	private void writeDesignConcepts(TextDocument doc, int level) {
		doc.addParagraph("Design concepts").applyHeading(true, level);
	}

	private void writeEmergenceConcepts(TextDocument doc, int level) {
		doc.addParagraph("Emergence").applyHeading(true, level);
	}

	private void writeAdaptationConcepts(TextDocument doc, int level) {
		doc.addParagraph("Adaptation").applyHeading(true, level);
	}

	private void writeObjectivesConcepts(TextDocument doc, int level) {
		doc.addParagraph("Objectives").applyHeading(true, level);
	}

	private void writeLearningConcepts(TextDocument doc, int level) {
		doc.addParagraph("Learning").applyHeading(true, level);
	}

	private void writePredictionConcepts(TextDocument doc, int level) {
		doc.addParagraph("Prediction").applyHeading(true, level);
	}

	private void writeSensingConcepts(TextDocument doc, int level) {
		doc.addParagraph("Sensing").applyHeading(true, level);
	}

	private void writeInteractionConcepts(TextDocument doc, int level) {
		doc.addParagraph("Interaction").applyHeading(true, level);
	}

	private void writeStochasticityConcepts(TextDocument doc, int level) {
		doc.addParagraph("Stochasticity").applyHeading(true, level);
	}

	private void writeCollectivesConcepts(TextDocument doc, int level) {
		doc.addParagraph("Collectives").applyHeading(true, level);
	}

	private void writeObservationConcepts(TextDocument doc, int level) {
		doc.addParagraph("Observation").applyHeading(true, level);
	}

	@SuppressWarnings("unchecked")
	private void writeProcessScheduling(TextDocument doc, int level) {
		doc.addParagraph("Process overview and scheduling").applyHeading(true, level);
		/**
		 * 
		 * flow chart, - insert drawing
		 * 
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("Timeline\n")//
				.append("\tScale: ").append(timeline.properties().getPropertyValue(P_TIMELINE_SCALE.key())).append("\n")//
				.append("\torigin: ").append(timeline.properties().getPropertyValue(P_TIMELINE_TIMEORIGIN.key()))
				.append("\n");

		if (!timersClock.isEmpty()) {
			sb.append("clock timers\n");
			for (TreeGraphDataNode timer : timersClock) {
				sb.append(timer.id()).append("\n");
				sb.append("\tUnits: ").append(timer.properties().getPropertyValue(P_TIMEMODEL_TU.key())).append("\n");
				sb.append("\tnumber of units: ").append(timer.properties().getPropertyValue(P_TIMEMODEL_NTU.key()))
						.append("\n");
			}
		}
		if (!timersEvent.isEmpty()) {
			sb.append("event timers\n");
			for (TreeGraphDataNode timer : timersEvent) {
				sb.append(timer.id()).append("\n");
				for (TreeGraphDataNode feeder : (List<TreeGraphDataNode>) get(timer.edges(Direction.OUT),
						selectOneOrMany(hasTheLabel(E_FEDBY.label())), edgeListEndNodes())) {
					sb.append("\tFed by: ").append(feeder.id()).append("\n");
				}
			}
		}

		doc.addParagraph(sb.toString());

		String indent = "";
		StringBuilder flowChart = new StringBuilder();
//TODO initialisation
		for (int i = 0; i < timersClock.size(); i++) {
			TreeGraphDataNode timer = timersClock.get(i);
			SimplePropertyList tp = timer.properties();
			int nTU = (Integer) tp.getPropertyValue(P_TIMEMODEL_NTU.key());
			TimeUnits tu = (TimeUnits) tp.getPropertyValue(P_TIMEMODEL_TU.key());

			flowChart.append(indent).append("for each ").append(tu.name().toLowerCase());
			if (nTU > 1)
				flowChart.append("(x").append(nTU).append(")");
			flowChart.append("\n");
			List<TreeGraphDataNode> procs = (List<TreeGraphDataNode>) get(timer.getChildren(), //
					selectOneOrMany(hasTheLabel(N_PROCESS.label())));
			// arrange by dependsOn
			indent += "\t";
			for (TreeGraphDataNode proc : procs) {
				List<TreeGraphDataNode> funcs = (List<TreeGraphDataNode>) get(proc.getChildren(),
						selectZeroOrMany(hasTheLabel(N_FUNCTION.label())));
				// TODO consequences
				for (TreeGraphDataNode func : funcs) {
					flowChart.append(indent)//
							.append(proc.id())//
							.append(".")//
							.append(func.id())//
							.append(".")//
							.append(func.properties().getPropertyValue(P_FUNCTIONTYPE.key()))//
							.append("(...)\n");
				}
			}
		}

		doc.addParagraph(flowChart.toString());

	}

	private void writeCollectives(TextDocument doc, int level) {
		doc.addParagraph("Collectives").applyHeading(true, level);

	}

	private void writeEnvironment(TextDocument doc, int level) {
		doc.addParagraph("Environment").applyHeading(true, level);

	}

	private void writeSpatialUnits(TextDocument doc, int level) {
		doc.addParagraph("Spatial units").applyHeading(true, level);
		// TODO: Only spatial units is being asked for here.
		if (spaces == null)
			doc.addParagraph("Non-spatial model.");
		else {
			for (TreeGraphDataNode space : spaces) {
				StringBuilder sb = new StringBuilder();
				sb.append(space.toShortString()).append("\n");
				for (String key : space.properties().getKeysAsSet()) {
					sb.append("\t")//
							.append(key)//
							.append(": ")//
							.append(space.properties().getPropertyValue(key))//
							.append("\n");
				}
				// TODO: list processes using this space.
				doc.addParagraph(sb.toString());
			}
		}

	}

	private void writeAgentsIndividuals(TextDocument doc, int level) {
		doc.addParagraph("Agents/individuals").applyHeading(true, level);

	}

	private void writeEVS(TextDocument doc, int level) {
		doc.addParagraph("Entities, state variables, and scales").applyHeading(true, level);
	}

	private void writePurpose(TextDocument doc, int level) {
		doc.addParagraph("Purpose").applyHeading(true, level);
		doc.addParagraph((String) cfg.root().properties().getPropertyValue(P_MODEL_PRECIS.key()));
		Paragraph para1 = doc.addParagraph(
				"\n[Explanation: Every model has to start from a clear question, problem, or hypothesis. " + //
						"Therefore, ODD starts with a concise summary of the overall objective(s) for which the model was developed. "
						+ //
						"Do not describe anything about how the model works here, only what it is to be used for. " + //
						"We encourage authors to use this paragraph independently of any presentation of the purpose in the introduction of their article, "
						+ //
						"since the ODD protocol should be complete and understandable by itself and not only in connection with the whole publication "
						+ //
						"(as it is also the case for figures, tables and their legends). " + //
						"If one of the purposes of a model is to expand from basic principles to richer representation of real-world scenarios, "
						+ //
						"this should be stated explicitly.]");
		Font font = para1.getFont();

		font.setFontStyle(StyleTypeDefinitions.FontStyle.ITALIC);

		para1.setFont(font);
	}

	private void setHeading(TextDocument doc, int level) {
		SimplePropertyList p = cfg.root().properties();
		StringBuilder title1 = new StringBuilder();
		title1.append("Overview, Design concepts and Details");
		doc.addParagraph(title1.toString()).applyHeading(true, level);

		StringBuilder title2 = new StringBuilder();
		title2.append(Project.getDisplayName())//
				.append(" (Version: ")//
				.append(p.getPropertyValue(P_MODEL_VERSION.key()))//
				.append(")");
		doc.addParagraph(title2.toString()).applyHeading(true, level);

		doc.addParagraph(authors);
	}

	// this must have been done somewhere already!
	private static int getDimensions(TreeNode rec) {
		int res = 0;
		for (TreeNode n : rec.getChildren()) {
			if (n.classId().equals(N_FIELD.label()))
				res++;
			if (n.classId().equals(N_TABLE.label())) {
				res += getTableDims(n);
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	private static int getTableDims(TreeNode n) {
		List<TreeGraphDataNode> dims = (List<TreeGraphDataNode>) get(n.edges(Direction.OUT), edgeListEndNodes(),
				selectOneOrMany(hasTheLabel(N_DIMENSIONER.label())));
		int result = 1;
		for (TreeGraphDataNode dim : dims) {
			int s = (Integer) dim.properties().getPropertyValue(P_DIMENSIONER_SIZE.key());
			result *= s;
		}
		if (n.hasChildren())
			result += getDimensions(n.getChildren().iterator().next());
		return result;
	}

}
