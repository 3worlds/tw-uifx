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
	 * Obtained from logistic 1
	 * 
	 * NB: Ignore ui and experiment
	 */
	private static int baseNodes = 44;
	private static int baseEdges = 54;
	private static int baseDrvs = 1;
	private static int baseCnts = 1;
	private static int baseProps = 68;

	private int nNodes;
	private int nEdges;
	private int nCnts;
	private int nDrvs;
	private int nProps;
	private int nCT;
	private int nGroups;
	private List<TreeGraphDataNode> spaces;
	// private Map<TreeGraphDataNode, List<TreeGraphDataNode>> tmProcs;
	List<TreeGraphDataNode> timersClock;
	List<TreeGraphDataNode> timersEvent;
	List<TreeGraphDataNode> timersScenario;

	TreeGraphDataNode timeline;

	private TreeGraph<TreeGraphDataNode, ALEdge> cfg;

	public DocoGenerator(TreeGraph<TreeGraphDataNode, ALEdge> cfg) {
		this.cfg = cfg;
		// basic metrics
		nNodes = cfg.nNodes();
		nEdges = nNodes - 1;
		for (TreeGraphDataNode n : cfg.nodes()) {
			nProps += n.properties().size();
			for (ALEdge e : n.edges(Direction.OUT)) {
				nEdges++;
				if (e instanceof ALDataEdge)
					nProps += ((ALDataEdge) e).properties().size();
			}
		}
		TreeGraphDataNode dDef = (TreeGraphDataNode) get(cfg.root().getChildren(),
				selectOne(hasTheLabel(N_DATADEFINITION.label())));
		for (TreeGraphDataNode n : cfg.subTree(dDef)) {
			if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_DRIVERS.label()))) != null)
				nDrvs += getDimensions(n);
			else if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_CONSTANTS.label()))) != null)
				nCnts += getDimensions(n);
		}
	}

	@SuppressWarnings("unchecked")
	public void generate() {

		// we should also collect all the fields/tables and their categories
		List<TreeGraphDataNode> systems = (List<TreeGraphDataNode>) get(cfg.root().getChildren(),
				selectOneOrMany(hasTheLabel(N_SYSTEM.label())));
		spaces = new ArrayList<>();
		for (TreeGraphDataNode system : systems) {
			TreeGraphDataNode space = (TreeGraphDataNode) get(system.getChildren(),
					selectZeroOrOne(hasTheLabel(N_SPACE.label())));
			if (space != null)
				spaces.add(space);
		}

		// prep for flowchart
		// deal with only one system
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

		// collect all componentTypes, relationTypes groups and whatever else - arena,

		try {// TODO: refactor all this
			SimplePropertyList p = cfg.root().properties();
			LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
			String datetime = currentDate.format(DateTimeFormatter.ofPattern("d-MMM-uuuu"));
			StringBuilder authors = new StringBuilder();
			// ODD
			// cf: https://odftoolkit.org/simple/document/cookbook/Text%20Document.html
			TextDocument odd = TextDocument.newTextDocument();
			// TODO: Add model name and version to footer. odd.getFooter();
			// TOC?
			StringBuilder title1 = new StringBuilder();
			title1.append("Overview, Design concepts and Details");
			odd.addParagraph(title1.toString()).applyHeading(true, 1);

			StringBuilder title2 = new StringBuilder();
			title2.append(Project.getDisplayName())//
					.append(" (Version: ")//
					.append(p.getPropertyValue(P_MODEL_VERSION.key()))//
					.append(")");
			odd.addParagraph(title2.toString()).applyHeading(true, 1);

			org.odftoolkit.simple.text.list.List lst = odd.addList();
			// authors
			StringTable tblAuthors = (StringTable) p.getPropertyValue(P_MODEL_AUTHORS.key());

			for (int i = 0; i < tblAuthors.size(); i++)
				authors.append(tblAuthors.getWithFlatIndex(i)).append("\n");

			authors.append("Date: ").append(datetime).append("\n");
			odd.addParagraph(authors.toString());

			odd.addParagraph("1. Purpose").applyHeading(true, 2);
			odd.addParagraph((String) p.getPropertyValue(P_MODEL_PRECIS.key()));

			odd.addParagraph("2. Entities, state variables, and scales").applyHeading(true, 2);

			/**
			 * Many things to fill out here from the graph
			 * 
			 * Entities: all componentTypes, relationTypes (interactions) and groups
			 * 
			 * State variables: Categories with drivers and field/table descriptions
			 * 
			 * Global variables: from the arena
			 * 
			 * table sizes
			 * 
			 */

			odd.addParagraph("Agents/individuals").applyHeading(true, 3);
			odd.addParagraph("Spatial units").applyHeading(true, 3);
			// TODO: Only spatial units is being asked for here.
			if (spaces.isEmpty())
				odd.addParagraph("Non-spatial model.");
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
					odd.addParagraph(sb.toString());
				}
			}
			odd.addParagraph("Environment").applyHeading(true, 3);
			odd.addParagraph("Collectives").applyHeading(true, 3);

			odd.addParagraph("3. Process overview and scheduling").applyHeading(true, 2);
			/**
			 * 
			 * flow chart, - insert drawing
			 * 
			 */
			StringBuilder sb = new StringBuilder();
			sb.append("Timeline\n")//
					.append("\tScale: ").append(timeline.properties().getPropertyValue(P_TIMELINE_SCALE.key()))
					.append("\n")//
					.append("\torigin: ").append(timeline.properties().getPropertyValue(P_TIMELINE_TIMEORIGIN.key()))
					.append("\n");

			if (!timersClock.isEmpty()) {
				sb.append("clock timers\n");
				for (TreeGraphDataNode timer : timersClock) {
					sb.append(timer.id()).append("\n");
					sb.append("\tUnits: ").append(timer.properties().getPropertyValue(P_TIMEMODEL_TU.key()))
							.append("\n");
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

			odd.addParagraph(sb.toString());

			String indent = "";
			StringBuilder flowChart = new StringBuilder();
// TODO initialisation
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

			odd.addParagraph(flowChart.toString());
			odd.addParagraph("4. Design concepts").applyHeading(true, 2);

			// initialise function code if simple
			odd.addParagraph("5. Initialisation").applyHeading(true, 2);

			// data files
			odd.addParagraph("6. Input data").applyHeading(true, 2);

			odd.addParagraph("7. Submodels").applyHeading(true, 2);

			odd.addParagraph("References").applyHeading(true, 2);

			StringTable tblRefs = (StringTable) p.getPropertyValue(P_MODEL_CITATIONS.key());
			StringBuilder refs = new StringBuilder();
			for (int i = 0; i < tblRefs.size(); i++)
				refs.append(i + 1).append(". ").append(tblRefs.getWithFlatIndex(i)).append("\n");
			odd.addParagraph(refs.toString());

			odd.appendSection("Some section");
			odd.addPageBreak();

			title1 = new StringBuilder();
			title1.append("Appendix 1: Model specification metrics");
			odd.addParagraph(title1.toString()).applyHeading(true, 1);

			title2 = new StringBuilder();
			title2.append(Project.getDisplayName())//
					.append(" (Version: ")//
					.append(p.getPropertyValue(P_MODEL_VERSION.key()))//
					.append(")");
			odd.addParagraph(title2.toString()).applyHeading(true, 1);

			odd.addParagraph(authors.toString());
			// rows, cols
			Table table = odd.addTable(5, 2);
			// cols, rows!
			table.getCellByPosition(0, 0).setStringValue("#Nodes");
			table.getCellByPosition(1, 0).setStringValue(Integer.toString(nNodes - baseNodes));
			table.getCellByPosition(0, 1).setStringValue("#Edges");
			table.getCellByPosition(1, 1).setStringValue(Integer.toString(nEdges - baseEdges));
			table.getCellByPosition(0, 2).setStringValue("#Constants");
			table.getCellByPosition(1, 2).setStringValue(Integer.toString(nCnts - baseCnts));
			table.getCellByPosition(0, 3).setStringValue("#Drivers");
			table.getCellByPosition(1, 3).setStringValue(Integer.toString(nDrvs - baseDrvs));
			table.getCellByPosition(0, 4).setStringValue("#Properties");
			table.getCellByPosition(1, 4).setStringValue(Integer.toString(nProps - baseProps));

			odd.addParagraph("[Add all other graph analysis measures here.]");

			odd.appendSection("Some other section");
			odd.addPageBreak();

			title1 = new StringBuilder();
			title1.append("Appendix 2: Model specification graph");
			odd.addParagraph(title1.toString()).applyHeading(true, 1);

			title2 = new StringBuilder();
			title2.append(Project.getDisplayName())//
					.append(" (Version: ")//
					.append(p.getPropertyValue(P_MODEL_VERSION.key()))//
					.append(")");
			odd.addParagraph(title2.toString()).applyHeading(true, 1);

			odd.addParagraph("[Add selected graph images here]");

			odd.save(Project.makeFile(cfg.root().id() + ".odt"));

			{
				/**
				 * Suggest:
				 * 
				 * Appendix 1: Model specification metrics
				 * 
				 * Appendix 2: Selected config graph images: 1) xlinks, 2) parent-child with
				 * only relevant nodes shown
				 * 
				 */
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
