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

import au.edu.anu.rscs.aot.queries.graph.element.ElementLabel;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.tw.EdgeXorPropertyQuery;
import au.edu.anu.twcore.archetype.tw.OutNodeXorQuery;
import au.edu.anu.twuifx.mm.visualise.IGraphVisualiser;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
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

	// protected Class<? extends TreeGraphNode> subClass;

	static private final DataElementType det = DataElementType.defaultValue();
	static private final ExperimentDesignType edt = ExperimentDesignType.defaultValue();
	static private final Grouping g = Grouping.defaultValue();
	static private final LifespanType lst = LifespanType.defaultValue();
	// SnippetLocation sl= SnippetLocation.defaultValue();
	static private final StatisticalAggregates sa = StatisticalAggregates.defaultValue();
	// TabLayoutTypes tlt = TabLayoutTypes.defaultValue();
	static private final TimeScaleType tst = TimeScaleType.defaultValue();
	static private final TimeUnits tu = TimeUnits.defaultValue();
	static private final TwFunctionTypes twft = TwFunctionTypes.defaultValue();
	// UIContainers uic =UIContainers.defaultValue();
	static private final FileType ft = FileType.defaultValue();
	static private final DateTimeType dtt = DateTimeType.defaultValue();

	public StructureEditorAdapter(SpecifiableNode clickedNode, IGraphVisualiser gv) {
		super();
		this.specifications = new TwSpecifications();
		this.newChild = null;
		this.editingNode = clickedNode;
		this.baseSpec = specifications.getSpecsOf(editingNode.getConfigNode(), editingNode.createdBy(),
				TWA.getRoot());
		this.subClassSpec = specifications.getSubSpecsOf(baseSpec, editingNode.getSubClass());
		this.gvisualiser = gv;
		System.out.println("Config: " + editingNode.getConfigNode().id() + ", Specified by: " + baseSpec.id()
				+ " + " + subClassSpec);
	}

	@Override
	public List<SimpleDataTreeNode> newChildList(Iterable<SimpleDataTreeNode> childSpecs) {
		List<SimpleDataTreeNode> result = new ArrayList<SimpleDataTreeNode>();
		for (SimpleDataTreeNode childNodeSpec : childSpecs) {
			String childLabel = (String) childNodeSpec.properties().getPropertyValue(aaIsOfClass);
			IntegerRange range = specifications.getMultiplicity(childNodeSpec);
			if (editingNode.moreChildrenAllowed(range, childLabel))
				result.add(childNodeSpec);
		}
		return result;
	}

//	@Override
	public List<Pair<String, SimpleDataTreeNode>> newEdgeList(Iterable<SimpleDataTreeNode> edgeSpecs) {
		List<Pair<String, SimpleDataTreeNode>> result = new ArrayList<>();
		List<String> edgePropXorOptions = specifications.getConstraintOptions(baseSpec,
				EdgeXorPropertyQuery.class.getName());
		List<String> nodeNodeXorOptions = specifications.getConstraintOptions(baseSpec,
				OutNodeXorQuery.class.getName());

		for (SimpleDataTreeNode edgeSpec : edgeSpecs) {
			String nodeLabel = specifications.getEdgeToNodeLabel(edgeSpec);
			List<String> edgeLabelOptions = specifications.getConstraintOptions(edgeSpec, ElementLabel.class.getName());
			// we now need the node list of the graph!
			// easy: graph.nodes() (as an Iterable<Node>)
		}
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

}
