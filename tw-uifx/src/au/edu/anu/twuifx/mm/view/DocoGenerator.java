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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.table.Column;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;

import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twcore.ecosystem.runtime.timer.ClockTimer;
import au.edu.anu.twcore.ecosystem.runtime.timer.EventTimer;
import au.edu.anu.twcore.ecosystem.runtime.timer.ScenarioTimer;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.mm.propertyEditors.trackerType.TrackerTypeEditor;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.twcore.constants.DataElementType;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.cnrs.iees.twcore.constants.TwFunctionTypes;
import fr.ens.biologie.generic.utils.Interval;

import static au.edu.anu.twcore.archetype.TwArchetypeConstants.*;
import org.odftoolkit.simple.style.StyleTypeDefinitions;

/**
 * @author Ian Davies
 *
 * @date 13 Jul 2020
 */

/**
 * ODD generator
 * 
 * THis can go in tw-core when finished. Running from jar may crash if left in
 * tw-uifx since its not included in this library.
 */
public class DocoGenerator {
	/**
	 * Minimal config baseline
	 */
	private static int baseNodes = 26;
	private static int baseEdges = 6;
	private static int baseDrvs = 0;
	private static int baseCnts = 0;
	private static int baseDecs = 0;
	private static int baseProps = 27;
	private static int baseCT = 1; // the system

	private int nNodes;
	private int nEdges;
	private int nCnts;
	private int nDrvs;
	private int nDecs;
	private int nProps;
	private int nGroups;
	private String authors;
	private String version;

	private static String sep = "\t";

	private List<TreeGraphDataNode> timersClock;
	private List<TreeGraphDataNode> timersEvent;
	private List<TreeGraphDataNode> timersScenario;
	private Map<TreeNode, String> timerDesc;
	private Map<TreeNode, String> funcDesc;
	private Map<TreeNode, String> scDesc;
	private TreeGraphDataNode timeline;
	private TreeGraphDataNode system;
	private TreeGraphDataNode struct;
	private TreeGraphDataNode dDef;
	private List<TreeGraphDataNode> compTypes; // including the system
	private List<TreeGraphDataNode> relTypes;
	private List<TreeGraphDataNode> spaceTypes;
	private List<TreeGraphDataNode> scTypes;
	private List<TreeGraphDataNode> initTypes;

	private TreeGraph<TreeGraphDataNode, ALEdge> cfg;

	private static final int level1 = 1;
	private static final int level2 = 2;
	private static final int level3 = 3;

//	private int tableNumber;

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

	private long startTime;

	@SuppressWarnings("unchecked")
	public DocoGenerator(TreeGraph<TreeGraphDataNode, ALEdge> cfg) {
		startTime = System.currentTimeMillis();
		this.cfg = cfg;
		timerDesc = new HashMap<>();
		funcDesc = new HashMap<>();
		scDesc = new HashMap<>();
		compTypes = new ArrayList<>();
		relTypes = new ArrayList<>();
		spaceTypes = new ArrayList<>();
		scTypes = new ArrayList<>();
		initTypes = new ArrayList<>();
//		tableNumber = 0;
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

			if (n.classId().equals(N_DATADEFINITION.label()))
				dDef = n;
			else if (n.classId().equals(N_SYSTEM.label())) {
				system = n;
				compTypes.add(n);
			} else if (n.classId().equals(N_FUNCTION.label()) || n.classId().equals(N_INITFUNCTION.label())) {
				TwFunctionTypes ft = (TwFunctionTypes) n.properties().getPropertyValue(P_FUNCTIONTYPE.key());
				funcDesc.put(n, n.id() + " " + formatClassifier(ft.name() + "(...)"));
				if (n.classId().equals(N_INITFUNCTION.label())) {
					initTypes.add(n);
				}
			} else if (n.classId().equals(N_COMPONENTTYPE.label())) {
				compTypes.add(n);
			} else if (n.classId().equals(N_RELATIONTYPE.label())) {
				relTypes.add(n);
			} else if (n.classId().equals(N_SPACE.label())) {
				spaceTypes.add(n);
			} else if (n.classId().equals(N_STRUCTURE.label())) {
				struct = n;
			} else if (n.classId().equals(N_TIMELINE.label())) {
				timeline = n;
			} else if (n.classId().equals(N_STOPPINGCONDITION.label())) {
				String sn = getSimpleName((String) n.properties().getPropertyValue(twaSubclass));
				scDesc.put(n, n.id() + " " + formatClassifier(sn));
			}

		}

		// Count drivers, constants and decorators
		for (TreeGraphDataNode n : cfg.subTree(dDef)) {
			if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_DRIVERS.label()))) != null)
				nDrvs += getDimensions(n);
			else if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_CONSTANTS.label()))) != null)
				nCnts += getDimensions(n);
			else if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_DECORATORS.label()))) != null)
				nDecs += getDimensions(n);
		}

		timersClock = new ArrayList<>();
		timersEvent = new ArrayList<>();
		timersScenario = new ArrayList<>();
		for (TreeGraphDataNode timer : (List<TreeGraphDataNode>) get(timeline.getChildren(),
				selectOneOrMany(hasTheLabel(N_TIMER.label())))) {
			if (timer.properties().getPropertyValue(twaSubclass).equals(ClockTimer.class.getName())) {
				timersClock.add(timer);
				timerDesc.put(timer, timer.id() + " " + formatClassifier(ClockTimer.class.getSimpleName()));
			} else if (timer.properties().getPropertyValue(twaSubclass).equals(EventTimer.class.getName())) {
				timersEvent.add(timer);
				timerDesc.put(timer, timer.id() + " " + formatClassifier(EventTimer.class.getSimpleName()));
			} else {
				timersScenario.add(timer);
				timerDesc.put(timer, timer.id() + " " + formatClassifier(ScenarioTimer.class.getSimpleName()));
			}
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
		version = (String) cfg.root().properties().getPropertyValue(P_MODEL_VERSION.key());
		version = version.trim();
	}

	public void generate() {

		try {
			// cf: https://odftoolkit.org/simple/document/cookbook/Text%20Document.html
			TextDocument document = TextDocument.newTextDocument();
			writeTitle(document, "Overview, Design concepts and Details", level1);
			// setHeading(document, level1);

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

			// ----- end ODD

			document.appendSection("end of ODD");
			document.addPageBreak();

			writeTitle(document, "Appendix 1: Model specification metrics", level1);
			writeAppendix1(document);

			// ----- end Appendix 1

			document.appendSection("end of Appendix 1");
			document.addPageBreak();

			writeTitle(document, "Appendix 2: Model specification graph", level1);

			document.addParagraph("[Add selected graph images here]");

			// ----- end Appendix 2

			// try and format all tables
			for (Table t : document.getTableList()) {
				/**
				 * Doesn't work . Also it's really a table property because when set for one col
				 * it's set for all.
				 */
				Iterator<Column> ci = t.getColumnIterator();
				while (ci.hasNext())
					ci.next().setUseOptimalWidth(true);
			}

			document.save(Project.makeFile(cfg.root().id() + ".odt"));

			// free resources
			document.close();

			long endTime = System.currentTimeMillis();
			System.out.println("DOC GENERATION TIME: " + (endTime - startTime));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void writeTitle(TextDocument doc, String heading, int level) {
		StringBuilder sb = new StringBuilder();
		sb.append(heading);
		doc.addParagraph(sb.toString()).applyHeading(true, level);

		sb = new StringBuilder();
		sb.append(Project.getDisplayName());
		if (!version.isBlank())
			sb.append(" (Version: ").append(version).append(")");

		doc.addParagraph(sb.toString()).applyHeading(true, level);

		doc.addParagraph(authors);

	}

	private void writePurpose(TextDocument doc, int level) {
		doc.addParagraph("Purpose").applyHeading(true, level);
		String precis = (String) cfg.root().properties().getPropertyValue(P_MODEL_PRECIS.key());
		if (precis.trim().isBlank()) {
			Paragraph para1 = doc.addParagraph(""//
					+ "[Explanation: Every model has to start from a clear question, problem, or hypothesis. "//
					+ "Therefore, ODD starts with a concise summary of the overall objective(s) for which the model was developed. "//
					+ "Do not describe anything about how the model works here, only what it is to be used for. "//
					+ "We encourage authors to use this paragraph independently of any presentation of the purpose in the introduction of their article, "//
					+ "since the ODD protocol should be complete and understandable by itself and not only in connection with the whole publication "//
					+ "(as it is also the case for figures, tables and their legends). "//
					+ "If one of the purposes of a model is to expand from basic principles to richer representation of real-world scenarios, "//
					+ "this should be stated explicitly.]");

			Font font = para1.getFont();
			font.setFontStyle(StyleTypeDefinitions.FontStyle.ITALIC);
			para1.setFont(font);
		} else
			doc.addParagraph((String) cfg.root().properties().getPropertyValue(P_MODEL_PRECIS.key()));
	}

	/**
	 * Questions: What kinds of entities are in the model? By what state variables,
	 * or attributes, are these entities characterized? What are the temporal and
	 * spatial resolutions and extents of the model?
	 */
	private void writeEVS(TextDocument doc, int level) {
		List<String> entries;
		doc.addParagraph("Entities, state variables, and scales").applyHeading(true, level);

		entries = getEntityTypesRolesAndDriversTableEntries();
		doc.addParagraph("Entity roles and state variables");
		writeTable(doc, entries, "Entity type", "Role", "Drivers", "Description", "Dimensions", "Type", "Units",
				"Range");

		if (!spaceTypes.isEmpty()) {
			entries = getEntitySpatialReferenceTableEntries();
			doc.addParagraph("Entity components and spatial representation");
			writeTable(doc, entries, "Entity", "Component", "Space");
		}

		entries = getEntityTemporalReferenceTableEntries();
		doc.addParagraph("Entities and temporal representation");
		writeTable(doc, entries, "Entity", "Timer");

		entries = getCategoryRelationsTableEntries();
		if (!entries.isEmpty()) {
			doc.addParagraph("Entity relations");
			writeTable(doc, entries, "Relation", "Entity interaction");
		}

	}

	private void writeAgentsIndividuals(TextDocument doc, int level) {
//		doc.addParagraph("Agents/individuals").applyHeading(true, level);

	}

	private void writeSpatialUnits(TextDocument doc, int level) {
		doc.addParagraph("Spatial units").applyHeading(true, level);

		List<String> entries = getSpatialUnitsTableEntries();
		if (!entries.isEmpty()) {
			doc.addParagraph("Spatial details");
			writeTable(doc, entries, "Type", "Details");
		} else
			doc.addParagraph("Non-spatial model.");
	}

	private void writeEnvironment(TextDocument doc, int level) {
//		doc.addParagraph("Environment").applyHeading(true, level);
	}

	private void writeCollectives(TextDocument doc, int level) {
//		doc.addParagraph("Collectives").applyHeading(true, level);
	}

	/**
	 * Questions: Who (i.e., what entity) does what, and in what order? When are
	 * state variables updated? How is time modeled, as discrete steps or as a
	 * continuum over which both continuous processes and discrete events can occur?
	 * Except for very simple schedules, one should use pseudo-code to describe the
	 * schedule in every detail, so that the model can be re-implemented from this
	 * code. Ideally, the pseudo-code corresponds fully to the actual code used in
	 * the program implementing the ABM.
	 */
	@SuppressWarnings("unchecked")
	private void writeProcessScheduling(TextDocument doc, int level) {
		doc.addParagraph("Process overview and scheduling").applyHeading(true, level);
		List<String> entries;
		// Entity, timer and functions
		entries = getEntityTimedFunctionsTableEntries();
		doc.addParagraph("Entity functions");
		writeTable(doc, entries, "Entity", "Timer", "Functions");

		entries = getTimeTableEntries();
		doc.addParagraph("Timeline and timer properties");
		writeTable(doc, entries, "Name", "Details");

		entries = getStoppingConditionTableEntries();
		if (!entries.isEmpty()) {
			doc.addParagraph("Stopping conditions");
			writeTable(doc, entries, "Name", "Details");
		}

		entries = getEntityInitialiseTableEntries();
		if (!entries.isEmpty()) {
			doc.addParagraph("Initialisation");
			writeTable(doc, entries, "Entity", "Initialiser");
		}

		// TODO wip
		String indent = "";
		StringBuilder flowChart = new StringBuilder();
		// initialisation
		// NB system is in the compTypes list
		for (TreeGraphDataNode ct : compTypes) {
			TreeGraphDataNode init = (TreeGraphDataNode) get(ct.getChildren(),
					selectZeroOrOne(hasTheLabel(N_INITFUNCTION.label())));
			if (init != null)
				flowChart.append(ct.id()).append(".").append(init.id()).append("\n");
		}

		// clock timers
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

	private void writeDesignConcepts(TextDocument doc, int level) {
		// doc.addParagraph("Design concepts").applyHeading(true, level);
	}

	private void writeEmergenceConcepts(TextDocument doc, int level) {
		// doc.addParagraph("Emergence").applyHeading(true, level);
	}

	private void writeAdaptationConcepts(TextDocument doc, int level) {
//		doc.addParagraph("Adaptation").applyHeading(true, level);
	}

	private void writeObjectivesConcepts(TextDocument doc, int level) {
//		doc.addParagraph("Objectives").applyHeading(true, level);
	}

	private void writeLearningConcepts(TextDocument doc, int level) {
//		doc.addParagraph("Learning").applyHeading(true, level);
	}

	private void writePredictionConcepts(TextDocument doc, int level) {
//		doc.addParagraph("Prediction").applyHeading(true, level);
	}

	private void writeSensingConcepts(TextDocument doc, int level) {
//		doc.addParagraph("Sensing").applyHeading(true, level);
	}

	private void writeInteractionConcepts(TextDocument doc, int level) {
//		doc.addParagraph("Interaction").applyHeading(true, level);
	}

	private void writeStochasticityConcepts(TextDocument doc, int level) {
//		doc.addParagraph("Stochasticity").applyHeading(true, level);
	}

	private void writeCollectivesConcepts(TextDocument doc, int level) {
//		doc.addParagraph("Collectives").applyHeading(true, level);
	}

	private void writeObservationConcepts(TextDocument doc, int level) {
//		doc.addParagraph("Observation").applyHeading(true, level);
	}

	private void writeInitialisation(TextDocument doc, int level) {
//		doc.addParagraph("Initialisation").applyHeading(true, level);
	}

	private void writeInputData(TextDocument doc, int level) {
//		doc.addParagraph("Input data").applyHeading(true, level);
	}

	private void writeSubmodels(TextDocument doc, int level) {
//		doc.addParagraph("Submodels").applyHeading(true, level);

	}

	private void writeReferences(TextDocument doc, int level) {
		doc.addParagraph("References").applyHeading(true, level);
		int counter = 0;

		StringTable tblRefs = (StringTable) cfg.root().properties().getPropertyValue(P_MODEL_CITATIONS.key());
		StringBuilder refs = new StringBuilder();
		for (int i = 0; i < tblRefs.size(); i++) {
			String entry = tblRefs.getWithFlatIndex(i).trim();
			if (!entry.isBlank()) {
				refs.append(++counter).append(". ").append(entry).append("\n");
			}
		}

		doc.addParagraph(refs.toString());
	}

//------------ end of ODD ------------------

//------------- appendix 1 -----------------
	private void writeAppendix1(TextDocument doc) {
		List<String> entries = getMetricsTableEntries();
		doc.addParagraph("Configuration graph metrics");
		writeTable(doc, entries, "Metric", "Value*");
		doc.addParagraph("*obtained after subtracting the values of a minimal configuration.");
	}
// ------------- end appendix 1 -----------------

	@SuppressWarnings("unchecked")
	private List<String> getEntityTimedFunctionsTableEntries() {
		List<String> entries = new ArrayList<>();
		for (TreeGraphDataNode ct : compTypes) {
			String c1 = ct.id();
			List<TreeGraphDataNode> cats = (List<TreeGraphDataNode>) get(ct.edges(Direction.OUT),
					selectZeroOrMany(hasTheLabel(E_BELONGSTO.label())), edgeListEndNodes());
			for (TreeGraphDataNode cat : cats) {
				List<TreeGraphDataNode> procs = (List<TreeGraphDataNode>) get(cat.edges(Direction.IN),
						selectZeroOrMany(hasTheLabel(E_APPLIESTO.label())), edgeListStartNodes());
				if (!procs.isEmpty()) {
					TreeGraphDataNode timer = (TreeGraphDataNode) procs.get(0).getParent();
					String c2 = timerDesc.get(timer);
					for (TreeGraphDataNode proc : procs) {
						List<TreeGraphDataNode> funcs = (List<TreeGraphDataNode>) get(proc.getChildren(),
								selectZeroOrMany(hasTheLabel(N_FUNCTION.label())));
						for (TreeGraphDataNode func : funcs) {
							String c3 = funcDesc.get(func);
							entries.add(new StringBuilder().append(c1).append(sep).append(c2).append(sep).append(c3)
									.toString());
							c1 = "";
							c2 = c1;
						}
					}
				}
			}
		}
		// get all relationTypes and their functions
		TreeGraphDataNode struc = (TreeGraphDataNode) get(system.getChildren(),
				selectZeroOrOne(hasTheLabel(N_STRUCTURE.label())));
		if (struc != null) {
			List<TreeGraphDataNode> rts = (List<TreeGraphDataNode>) get(struc.getChildren(),
					selectZeroOrMany(hasTheLabel(N_RELATIONTYPE.label())));
			for (TreeGraphDataNode rt : rts) {
				String c1 = rt.toShortString();
				List<TreeGraphDataNode> procs = (List<TreeGraphDataNode>) get(rt.edges(Direction.IN),
						selectOneOrMany(hasTheLabel(E_APPLIESTO.label())), edgeListStartNodes());
				for (TreeGraphDataNode proc : procs) {
					TreeNode timer = proc.getParent();
					String c2 = timerDesc.get(timer);
					List<TreeGraphDataNode> funcs = (List<TreeGraphDataNode>) get(proc.getChildren(),
							selectZeroOrMany(hasTheLabel(N_FUNCTION.label())));
					for (TreeGraphDataNode func : funcs) {
						String c3 = funcDesc.get(func);
						entries.add(new StringBuilder().append(c1).append(sep).append(c2).append(sep).append(c3)
								.toString());
						c1 = "";
						c2 = c1;
					}
				}
			}
		}

		return entries;
	}

	@SuppressWarnings("unchecked")
	private List<String> getTimeTableEntries() {
		/**
		 * Its not very readable to just dump all the property names and values so this
		 * can't be generalised and is vulnerable to changes to the archetype.
		 */
		List<String> entries = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		// we only need the scale and origin in units of shortest time unit
		sb.append(timeline.toShortString());
		sb.append(sep).append(P_TIMELINE_SCALE.key()).append("=")
				.append(timeline.properties().getPropertyValue(P_TIMELINE_SCALE.key()));
		entries.add(sb.toString());
		sb = new StringBuilder();
		sb.append(sep).append(P_TIMELINE_TIMEORIGIN.key())//
				.append("=")//
				.append(timeline.properties().getPropertyValue(P_TIMELINE_TIMEORIGIN.key()))//
				.append(" (units: ").append(timeline.properties().getPropertyValue(P_TIMELINE_SHORTTU.key()))
				.append(")");
		entries.add(sb.toString());

		// clocks: need only TimeUnits and nTimeUnits - actually what is dt for???
		for (TreeGraphDataNode timer : timersClock) {
			sb = new StringBuilder();
			sb.append(timer.toShortString());
			sb.append(sep).append("Type=").append(ClockTimer.class.getSimpleName());
			entries.add(sb.toString());
			sb = new StringBuilder();

			sb.append(sep).append(P_TIMEMODEL_TU.key()).append("=")
					.append(timer.properties().getPropertyValue(P_TIMEMODEL_TU.key()));
			entries.add(sb.toString());
			sb = new StringBuilder();

			sb.append(sep).append(P_TIMEMODEL_NTU.key()).append("=")
					.append(timer.properties().getPropertyValue(P_TIMEMODEL_NTU.key()));
			entries.add(sb.toString());
		}
		for (TreeGraphDataNode timer : timersEvent) {
			sb = new StringBuilder();
			sb.append(timer.toShortString());
			sb.append(sep).append("Type=").append(EventTimer.class.getSimpleName());
			entries.add(sb.toString());
			sb = new StringBuilder();
			for (TreeGraphDataNode feeder : (List<TreeGraphDataNode>) get(timer.edges(Direction.OUT),
					selectOneOrMany(hasTheLabel(E_FEDBY.label())), edgeListEndNodes())) {
				sb.append(sep).append("Fed by: ").append(feeder.toShortString());
				entries.add(sb.toString());
				sb = new StringBuilder();
			}
		}
		for (TreeGraphDataNode timer : timersScenario) {
			sb = new StringBuilder();
			sb.append(timer.toShortString());
			sb.append(sep).append("Type=").append(ScenarioTimer.class.getSimpleName());;
			entries.add(sb.toString());
			/**
			 * TODO: Properties yet to be defined. Probably just a file name and time unit.
			 */

		}
		return entries;
	}

	private List<String> getStoppingConditionTableEntries() {
		List<String> entries = new ArrayList<>();
		for (TreeGraphDataNode sc : scTypes) {
			String c1 = scDesc.get(sc);
			for (String key : sc.properties().getKeysAsArray()) {
				Object value = sc.properties().getPropertyValue(key);
				if (!key.equals(twaSubclass)) {
					entries.add(new StringBuilder().append(c1).append(sep).append(key).append("=").append(value)
							.toString());
					c1 = "";
				}
			}
		}
		return entries;

	}

	private List<String> getEntityInitialiseTableEntries() {
		List<String> entries = new ArrayList<>();
		// NB system is in the compTypes list

		for (TreeGraphDataNode ct : compTypes) {
			TreeGraphDataNode init = (TreeGraphDataNode) get(ct.getChildren(),
					selectZeroOrOne(hasTheLabel(N_INITFUNCTION.label())));
			if (init != null) {
				entries.add(new StringBuilder().append(ct.id()).append(sep).append(funcDesc.get(init)).toString());
			}

		}

		return entries;
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

	private List<String> getEntityTypesRolesAndDriversTableEntries() {
		List<String> entries = new ArrayList<>();
		for (TreeGraphDataNode ct : compTypes) {
			String c1 = ct.id();
			for (ALEdge e : ct.edges(Direction.OUT)) {
				TreeGraphDataNode cat = (TreeGraphDataNode) e.endNode();
				String set = cat.getParent().id().replace("*", "");
				String classifier = cat.id().replace("*", "");
				String c2 = set + " " + formatClassifier(classifier);
				ALEdge drv = (ALEdge) get(cat.edges(Direction.OUT), selectZeroOrOne(hasTheLabel(E_DRIVERS.label())));
				if (drv == null) {
					String c3 = "none";
					StringBuilder sb = new StringBuilder();
					sb.append(c1).append(sep).append(c2).append(sep).append(c3);
					c1 = "";
					c2 = c1;
					entries.add(sb.toString());
				} else {
					Map<String, List<String>> drivers = getDriverDetails((TreeNode) drv.endNode());
					for (Map.Entry<String, List<String>> entry : drivers.entrySet()) {
						String c3 = entry.getKey();
						List<String> details = entry.getValue();
						StringBuilder sb = new StringBuilder();
						sb.append(c1).append(sep).append(c2).append(sep).append(c3);
						for (String s : details)
							sb.append(sep).append(s);

						entries.add(sb.toString());
						c1 = "";
						c2 = c1;
					}
				}
			}
		}
		return entries;
	}

	@SuppressWarnings("unchecked")
	private List<String> getEntityTemporalReferenceTableEntries() {
		List<String> entries = new ArrayList<>();
		for (TreeGraphDataNode ct : compTypes) {
			String c1 = ct.id();
			List<TreeGraphDataNode> cats = (List<TreeGraphDataNode>) get(ct.edges(Direction.OUT),
					selectZeroOrMany(hasTheLabel(E_BELONGSTO.label())), edgeListEndNodes());
			for (TreeGraphDataNode cat : cats) {
				List<TreeGraphDataNode> procs = (List<TreeGraphDataNode>) get(cat.edges(Direction.IN),
						selectZeroOrMany(hasTheLabel(E_APPLIESTO.label())), edgeListStartNodes());
				if (!procs.isEmpty()) {
					TreeGraphDataNode timer = (TreeGraphDataNode) procs.get(0).getParent();
					String c2 = timerDesc.get(timer);
					entries.add(new StringBuilder().append(c1).append(sep).append(c2).toString());
					c1 = "";
				}
			}
		}
		return entries;
	}

	@SuppressWarnings("unchecked")
	private List<String> getCategoryRelationsTableEntries() {
		List<String> entries = new ArrayList<>();

		for (TreeGraphDataNode rt : relTypes) {
			// TODO This is flawed! check how we pair to and from.
			String c1 = rt.id();
			List<TreeGraphDataNode> toCats = (List<TreeGraphDataNode>) get(rt.edges(Direction.OUT),
					selectOneOrMany(hasTheLabel(E_TOCATEGORY.label())), edgeListEndNodes());
			List<TreeGraphDataNode> fromCats = (List<TreeGraphDataNode>) get(rt.edges(Direction.OUT),
					selectOneOrMany(hasTheLabel(E_FROMCATEGORY.label())), edgeListEndNodes());
			for (int i = 0; i < toCats.size(); i++) {
				TreeGraphDataNode toCat = toCats.get(i);
				TreeGraphDataNode fromCat = fromCats.get(i);// ???
				TreeGraphDataNode ctTo = (TreeGraphDataNode) get(toCat.edges(Direction.IN),
						selectOneOrMany(hasTheLabel(E_BELONGSTO.label())), edgeListStartNodes(),
						selectOne(hasTheLabel(N_COMPONENTTYPE.label())));
				TreeGraphDataNode ctFrom = (TreeGraphDataNode) get(fromCat.edges(Direction.IN),
						selectOneOrMany(hasTheLabel(E_BELONGSTO.label())), edgeListStartNodes(),
						selectOne(hasTheLabel(N_COMPONENTTYPE.label())));
				String c2a = ctFrom.id();
				String c2b = ctTo.id();
				entries.add(new StringBuilder().append(c1).append(sep).append(c2a).append(" effects ").append(c2b)
						.toString());

			}
		}

		return entries;
	}

	@SuppressWarnings("unchecked")
	private List<String> getEntitySpatialReferenceTableEntries() {
		List<String> entries = new ArrayList<>();
		for (TreeGraphDataNode ct : compTypes) {
			String c1 = ct.id();
			List<TreeGraphDataNode> cmps = (List<TreeGraphDataNode>) get(ct.getChildren(),
					selectZeroOrMany(hasTheLabel(N_COMPONENT.label())));
			if (cmps.isEmpty()) {
				String c2 = "n.a";
				String c3 = "non-spatial";
				StringBuilder sb = new StringBuilder();
				sb.append(c1).append(sep).append(c2).append(sep).append(c3);
				entries.add(sb.toString());
				c1 = "";
				c2 = "";
			} else {
				for (TreeGraphDataNode cmp : cmps) {
					TreeGraphDataNode space = (TreeGraphDataNode) get(cmp.edges(Direction.OUT), edgeListEndNodes(),
							selectZeroOrOne(hasTheLabel(N_SPACE.label())));
					String c2 = cmp.id();
					String c3 = "non-spatial";
					if (space != null) {
						c3 = space.id();
					}
					StringBuilder sb = new StringBuilder();
					sb.append(c1).append(sep).append(c2).append(sep).append(c3);
					entries.add(sb.toString());
					c1 = "";
					c2 = "";
				}
			}
		}
		return entries;
	}

	private List<String> getSpatialUnitsTableEntries() {
		List<String> entries = new ArrayList<>();
		for (TreeGraphDataNode space : spaceTypes) {
			String c1 = space.toShortString();
			for (String key : space.properties().getKeysAsSet()) {
				StringBuilder sb = new StringBuilder();
				sb.append(key)//
						.append(" = ")//
						.append(space.properties().getPropertyValue(key));
				String c2 = sb.toString();
				entries.add(new StringBuilder().append(c1).append(sep).append(c2).toString());
				c1 = "";
			}
		}
		return entries;
	}

	private List<String> getMetricsTableEntries() {
		List<String> entries = new ArrayList<>();
		int configSize = (nNodes - baseNodes) + (nEdges - baseEdges) + (nProps - baseProps);
		entries.add(new StringBuilder().append("1 #Nodes").append(sep).append((nNodes - baseNodes)).toString());
		entries.add(new StringBuilder().append("2 #Edges").append(sep).append((nEdges - baseEdges)).toString());
		entries.add(new StringBuilder().append("3 #Properties").append(sep).append((nProps - baseProps)).toString());
		entries.add(
				new StringBuilder().append("4 configuration size (1+2+3)").append(sep).append((configSize)).toString());
		entries.add(new StringBuilder().append("5 #Drivers").append(sep).append((nDrvs - baseDrvs)).toString());
		entries.add(new StringBuilder().append("6 #Constants").append(sep).append((nCnts - baseCnts)).toString());
		entries.add(new StringBuilder().append("7 #Decorators").append(sep).append((nDecs - baseDecs)).toString());
		entries.add(new StringBuilder().append("8 #ComponentTypes").append(sep).append((compTypes.size() - baseCT))
				.toString());
		entries.add(new StringBuilder().append("9 #RelationTypes").append(sep).append((relTypes.size())).toString());
		return entries;
	}

	private static Map<String, List<String>> getDriverDetails(TreeNode record) {
		List<TreeNode> items = new ArrayList<>();
		listItems(record, items);
		Map<String, List<String>> result = new HashMap<>();
		for (TreeNode item : items) {
			TreeGraphDataNode n = (TreeGraphDataNode) item;
			String kDesc = P_TABLE_DESCRIPTION.key();
			String kUnits = P_TABLE_UNITS.key();
			String kType = P_DATAELEMENTTYPE.key();
			String kRange = P_TABLE_RANGE.key();
			String kInterval = P_TABLE_INTERVAL.key();
			if (item.classId().equals(N_FIELD.label())) {
				kDesc = P_FIELD_DESCRIPTION.key();
				kUnits = P_FIELD_UNITS.key();
				kType = P_FIELD_TYPE.key();
				kRange = P_FIELD_RANGE.key();
				kInterval = P_FIELD_INTERVAL.key();
			}
			String desc = (String) n.properties().getPropertyValue(kDesc);
			desc = desc.trim();
			if (desc.isBlank())
				desc = "n.a.";

			int[][] sizes = TrackerTypeEditor.collectDims(item);
			String dims = "scalar";
			String txt = "[";
			for (int i = 0; i < sizes.length; i++)
				for (int s : sizes[i])
					txt += s + ",";
			if (txt.length() > 1)
				dims = txt.substring(0, txt.length() - 1) + "]";

			DataElementType det = (DataElementType) n.properties().getPropertyValue(kType);
			String type = det.name();

			String units = "n.a";
			if (n.properties().hasProperty(kUnits)) {
				units = (String) n.properties().getPropertyValue(kUnits);
				units = units.trim();
				if (units.isBlank())
					units = "n.a.";
			}

			String range = "n.a";
			if (n.properties().hasProperty(kRange)) {
				IntegerRange ir = (IntegerRange) n.properties().getPropertyValue(kRange);
				range = ir.toString();
			} else if (n.properties().hasProperty(kInterval)) {
				Interval ir = (Interval) n.properties().getPropertyValue(kInterval);
				range = ir.toString();
			}
			List<String> details = new ArrayList<>();
			details.add(desc);
			details.add(dims);
			details.add(type);
			details.add(units);
			details.add(range);
			result.put(n.id(), details);
		}
		return result;
	}

	private static void listItems(TreeNode record, List<TreeNode> items) {
		for (TreeNode child : record.getChildren()) {
			if (child.classId().equals(N_FIELD.label())) {
				items.add(child);
			} else if (child.classId().equals(N_TABLE.label())) {
				if (child.getChildren().iterator().hasNext()) {
					listItems(child.getChildren().iterator().next(), items);
				} else
					items.add(child);
			}
		}
	}

	private static void writeTable(TextDocument doc, List<String> entries, String... headers) {
		Table table = doc.addTable(entries.size() + 1, headers.length);

		// col,row
		for (int i = 0; i < headers.length; i++)
			table.getCellByPosition(i, 0).setStringValue(headers[i]);

		for (int i = 0; i < entries.size(); i++) {
			String[] parts = entries.get(i).split(sep);
			for (int j = 0; j < parts.length; j++)
				table.getCellByPosition(j, i + 1).setStringValue(parts[j]);
		}

		doc.addParagraph(null);
	}

	private static String getSimpleName(String classStr) {
		try {
			Class<?> klass = (Class<?>) Class.forName(classStr);
			return klass.getSimpleName();
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private static String formatClassifier(String type) {
		return new StringBuilder().append(" {").append(type).append("}").toString();
	}
}
