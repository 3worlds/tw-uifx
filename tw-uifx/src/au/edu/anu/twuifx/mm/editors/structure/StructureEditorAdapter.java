/**************************************************************************
 *  TW-APPS - Applications used by 3Worlds                                *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-APPS contains ModelMaker and ModelRunner, programs used to         *
 *  construct and run 3Worlds configuration graphs. All code herein is    *
 *  independent of UI implementation.                                     *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-APPS (3Worlds applications).                  *
 *                                                                        *
 *  TW-APPS is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-APPS is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-APPS.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
  **************************************************************************/

package au.edu.anu.twuifx.mm.editors.structure;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.rscs.aot.archetype.ArchetypeArchetypeConstants;
import au.edu.anu.rscs.aot.queries.graph.element.ElementLabel;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualGraphFactory;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TWA;
import au.edu.anu.twcore.archetype.TwArchetypeConstants;
import au.edu.anu.twcore.archetype.tw.ChildXorPropertyQuery;
import au.edu.anu.twcore.archetype.tw.EdgeXorPropertyQuery;
import au.edu.anu.twcore.archetype.tw.OutNodeXorQuery;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.root.TwConfigFactory;
import au.edu.anu.twuifx.mm.visualise.IGraphVisualiser;
import fr.cnrs.iees.graph.Edge;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.identity.impl.PairIdentity;
import fr.cnrs.iees.twcore.constants.DataElementType;
import fr.cnrs.iees.twcore.constants.DateTimeType;
import fr.cnrs.iees.twcore.constants.ExperimentDesignType;
import fr.cnrs.iees.twcore.constants.FileType;
import fr.cnrs.iees.twcore.constants.Grouping;
import fr.cnrs.iees.twcore.constants.LifespanType;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.TimeScaleType;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.cnrs.iees.twcore.constants.TwFunctionTypes;
import javafx.util.Pair;

public abstract class StructureEditorAdapter
		implements StructureEditable, TwArchetypeConstants, ArchetypeArchetypeConstants {
	// forces call to registerType in ValidPropertyTypes
//	static private final DataElementType det = DataElementType.defaultValue();
//	static private final ExperimentDesignType edt = ExperimentDesignType.defaultValue();
//	static private final Grouping g = Grouping.defaultValue();
//	static private final LifespanType lst = LifespanType.defaultValue();
//	// SnippetLocation sl= SnippetLocation.defaultValue();
//	static private final StatisticalAggregates sa = StatisticalAggregates.defaultValue();
//	// TabLayoutTypes tlt = TabLayoutTypes.defaultValue();
//	static private final TimeScaleType tst = TimeScaleType.defaultValue();
//	static private final TimeUnits tu = TimeUnits.defaultValue();
//	static private final TwFunctionTypes twft = TwFunctionTypes.defaultValue();
//	// UIContainers uic =UIContainers.defaultValue();
//	static private final FileType ft = FileType.defaultValue();
//	static private final DateTimeType dtt = DateTimeType.defaultValue();

	/* what we need to know from the archetype graph */
	protected Specifications specifications;
	/*
	 * what we need to know from the visualNode that has been selected for editing
	 */
	protected SpecifiableNode editingNode;
	/*
	 * new node created by this editor. May be null because the op is not
	 * necessarily node creation
	 */
	protected VisualNode newChild;
	/* specificatons of this editingNode */
	protected SimpleDataTreeNode baseSpec;

	protected SimpleDataTreeNode subClassSpec;

	protected IGraphVisualiser gvisualiser;

	public StructureEditorAdapter(SpecifiableNode clickedNode, IGraphVisualiser gv) {
		super();
		this.specifications = new TwSpecifications();
		this.newChild = null;
		this.editingNode = clickedNode;
		this.baseSpec = specifications.getSpecsOf(editingNode.getConfigNode(), editingNode.createdBy(), TWA.getRoot());
		this.subClassSpec = specifications.getSubSpecsOf(baseSpec, editingNode.getSubClass());
		this.gvisualiser = gv;
		if (subClassSpec != null)
			System.out.println("Config: " + editingNode.getConfigNode().id() + ", Specified by: " + baseSpec.id()
					+ " + " + subClassSpec.id());
		else
			System.out.println("Config: " + editingNode.getConfigNode().id() + ", Specified by: " + baseSpec.id());
	}

	@Override
	public List<SimpleDataTreeNode> filterChildSpecs(Iterable<SimpleDataTreeNode> childSpecs) {
		List<SimpleDataTreeNode> result = new ArrayList<SimpleDataTreeNode>();
		List<String[]> tables = specifications.getQueryStringTables(baseSpec, ChildXorPropertyQuery.class);
		tables.addAll(specifications.getQueryStringTables(subClassSpec, ChildXorPropertyQuery.class));
		for (SimpleDataTreeNode childSpec : childSpecs) {
			String childLabel = (String) childSpec.properties().getPropertyValue(aaIsOfClass);
			IntegerRange range = specifications.getMultiplicity(childSpec);
			if (editingNode.moreChildrenAllowed(range, childLabel)) {
				if (!tables.isEmpty()) {
					if (allowedChild(childLabel, tables))
						result.add(childSpec);
				} else
					result.add(childSpec);
			}
		}
		return result;
	}

	private boolean allowedChild(String childLabel, List<String[]> tables) {
		VisualNode vn = editingNode.getSelectedVisualNode();
		for (String[] ss : tables) {
			if (ss[0].equals(childLabel)) {
				if (vn.configHasProperty(ss[1]))
					return false;
			}
		}
		return true;
	};

	private List<VisualNode> findNodesLabelled(String label) {
		List<VisualNode> result = new ArrayList<>();
		TreeGraph<VisualNode, VisualEdge> vg = gvisualiser.getVisualGraph();
		for (VisualNode vn : vg.nodes()) {
			if (vn.getConfigNode().classId().equals(label))
				result.add(vn);
		}
		return result;
	}

	// @Override
	public List<Pair<String, VisualNode>> filterEdgeSpecs(Iterable<SimpleDataTreeNode> edgeSpecs) {
		// 1) Do the constraints allow this edge to exist?
		// 2) does multiplicity allow for this edge?
		// 3) do we have available end nodes?
		// Test cases:
		// 1) Table: dimensioner 1..*
		List<Pair<String, VisualNode>> result = new ArrayList<>();
		for (SimpleDataTreeNode edgeSpec : edgeSpecs) {
			String toNodeRef = (String) edgeSpec.properties().getPropertyValue(aaToNode);
			String edgeLabel = (String) edgeSpec.properties().getPropertyValue(aaIsOfClass);
			List<VisualNode> en = findNodesLabelled(toNodeRef.replace(PairIdentity.LABEL_NAME_STR_SEPARATOR, ""));
			Pair<String, VisualNode> p = new Pair<String, VisualNode>(edgeLabel, en.get(0));
			result.add(p);
//			for (VisualNode n : en)
//				System.out.println(n);
		}

//		List<String> edgePropXorOptions = specifications.getConstraintOptions(baseSpec,
//				EdgeXorPropertyQuery.class.getName());
//		List<String> nodeNodeXorOptions = specifications.getConstraintOptions(baseSpec,
//				OutNodeXorQuery.class.getName());
//
//		for (SimpleDataTreeNode edgeSpec : edgeSpecs) {
//			String nodeLabel = specifications.getEdgeToNodeLabel(edgeSpec);
//			List<String> edgeLabelOptions = specifications.getConstraintOptions(edgeSpec, ElementLabel.class.getName());
//			// we now need the node list of the graph!
//			// easy: graph.nodes() (as an Iterable<Node>)
//		}
		return result;
	}

	public List<TreeGraphNode> orphanedChildList(Iterable<SimpleDataTreeNode> childSpecs) {
		List<TreeGraphNode> result = new ArrayList<>();
		for (TreeGraphNode root : editingNode.graphRoots()) {
			String rootLabel = TWA.getLabel(root.id());
			for (SimpleDataTreeNode childSpec : childSpecs) {
				String specLabel = (String) childSpec.properties().getPropertyValue(aaIsOfClass);
				if (rootLabel.equals(specLabel))
					result.add(root);
			}
		}
		return result;
	}

	protected boolean haveSpecification() {
		return baseSpec != null;
	}

	private static VisualEdge createVisualEdge(String edgeClassName, VisualNode vStart, VisualNode vEnd) {
		TreeGraphDataNode cStart = (TreeGraphDataNode) vStart.getConfigNode();
		TreeGraphDataNode cEnd = (TreeGraphDataNode) vEnd.getConfigNode();
		TwConfigFactory cf = (TwConfigFactory) cStart.factory();
		ALEdge ce = (ALEdge) cf.makeEdge(cf.edgeClass(edgeClassName), cStart, cEnd, edgeClassName);
		VisualGraphFactory vf = (VisualGraphFactory) vStart.factory();
		VisualEdge result = vf.makeEdge(vStart, vEnd, edgeClassName);
		result.setConfigEdge(ce);
		return result;
	}

	protected void connectTo(Pair<String, VisualNode> p) {
		VisualEdge ve =createVisualEdge(p.getKey(),editingNode.getSelectedVisualNode(),p.getValue());
		gvisualiser.onNewEdge(ve);
		GraphState.setChanged(true);
	}

}
