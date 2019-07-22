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
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import javafx.util.Pair;

public abstract class StructureEditorAdapter implements StructureEditable, TwArchetypeConstants {
	/* what we need to know from the archetype graph*/
	protected Specifications specifications;
	/* what we need to know from the visualNode that has been selected for editing*/
	protected SpecifiableNode editingNode;
	/* new node created by this editor. May be null because the op is not necessarily node creation */
	protected VisualNode newChild;
	/* specificatons of this editingNode*/
	protected TreeNode editingNodeSpec;

	public StructureEditorAdapter(SpecifiableNode clickedNode) {
		super();
		this.specifications = new TwSpecifications();
		this.newChild = null;
		this.editingNode = clickedNode;
		this.editingNodeSpec = specifications.getSpecificationOf( editingNode.getConfigNode());
	}


	@Override
	public List<TreeNode> newChildList(Iterable<TreeNode> childSpecs) {
		List<TreeNode> result = new ArrayList<TreeNode>();
		for (TreeNode childNodeSpec : childSpecs) {
			IntegerRange range = specifications.getMultiplicity(childNodeSpec, twaName);
			String childLabel = specifications.getLabel(childNodeSpec);
			if (!editingNode.inRange(range, childLabel))
				result.add(childNodeSpec);
		}
		return result;
	}

	@Override
	public List<Pair<String, TreeNode>> newEdgeList(Iterable<TreeNode> edgeSpecs) {
		List<Pair<String, TreeNode>> result = new ArrayList<>();
		List<String> edgePropXorOptions = specifications.getConstraintOptions(editingNodeSpec, twaConstraintEdgePropXor);
		List<String> nodeNodeXorOptions = specifications.getConstraintOptions(editingNodeSpec, twaConstraintNodeNodeXor);

		for (TreeNode edgeSpec : edgeSpecs) {
			String nodeLabel = specifications.getEdgeToNodeLabel(edgeSpec);
			List<String> edgeLabelOptions = specifications.getConstraintOptions(edgeSpec, twaConstraintElementLabel);
			// we now need the node list of the graph!
			// easy: graph.nodes() (as an Iterable<Node>)
		}
		return result;
	}

	public List<TreeGraphNode> orphanedChildList(Iterable<TreeNode> childSpecs) {
		List<TreeGraphNode> result = new ArrayList<>();

		return result;
	}

	protected boolean haveSpecification() {
		return editingNodeSpec != null;
	}

}
