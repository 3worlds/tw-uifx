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

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels.*;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.rscs.aot.archetype.ArchetypeArchetypeConstants;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TWA;
import au.edu.anu.twcore.archetype.TwArchetypeConstants;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.NodeFactory;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.ALNode;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.identity.Identity;
import fr.cnrs.iees.identity.impl.PairIdentity;
import fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels;

public class SpecifiedNode implements //
		SpecifiableNode, //
		ArchetypeArchetypeConstants, //
		TwArchetypeConstants {
	private VisualNode selectedVisualNode;
	private TreeGraph<VisualNode, VisualEdge> visualGraph;

	public SpecifiedNode(VisualNode visualNode, TreeGraph<VisualNode, VisualEdge> visualGraph) {
		this.selectedVisualNode = visualNode;
		this.visualGraph = visualGraph;
	}

	@Override
	public boolean hasChildren() {
		return !isLeaf();
	}

	@Override
	public String getClassValue() {
		// TODO Auto-generated method stub ???
		return null;
	}

	@Override
	public TreeGraphNode getConfigNode() {
		return selectedVisualNode.getConfigNode();
	}

	@Override
	public boolean canDelete() {
		return !getLabel().equals(ConfigurationNodeLabels.N_ROOT.label());
	}

	@Override
	public boolean moreChildrenAllowed(IntegerRange range, String childLabel) {
		List<TreeNode> lst = new ArrayList<>();
		for (TreeNode child : selectedVisualNode.getChildren()) {
			String label = TWA.getLabel(child.id());
			if (label.equals(childLabel))
				lst.add(child);
		}
		return range.inRange(lst.size() + 1);
	}

	@Override
	public String getLabel() {
		return selectedVisualNode.getLabel();
	}

	@Override
	public Iterable<VisualNode> graphRoots() {
		return visualGraph.roots();
	}

	@Override
	public boolean hasOutEdges() {
		return selectedVisualNode.edges().iterator().hasNext();
	}

	@Override
	public boolean isLeaf() {
		return selectedVisualNode.isLeaf();
	}

	@Override
	public boolean isCollapsed() {
		return selectedVisualNode.isCollapsed();
	}

	public static VisualNode newChild(VisualNode parent, String label, String name) {
		String proposedId = label + PairIdentity.LABEL_NAME_STR_SEPARATOR + name;

		TreeGraphNode configParent = parent.getConfigNode();
		NodeFactory cf = configParent.factory();
		TreeGraphDataNode configChild = (TreeGraphDataNode) cf.makeNode(cf.nodeClass(label), proposedId);
		configChild.connectParent(configParent);

		VisualNode childVisualNode = (VisualNode) parent.factory().makeNode(proposedId);
		childVisualNode.connectParent(parent);
		childVisualNode.setCreatedBy(TWA.getLabel(parent.id()));
		childVisualNode.setConfigNode(configChild);
		childVisualNode.setCategory();
		return childVisualNode;

	}

	@Override
	public VisualNode newChild(String label, String name) {
		return newChild(selectedVisualNode, label, name);
	}

	@Override
	public String proposeAnId(String label, String proposedName) {
		Identity id = selectedVisualNode.scope().newId(false, label, PairIdentity.LABEL_NAME_STR_SEPARATOR,
				proposedName);
		return TWA.getName(id.id());
	}

	@Override
	public String createdBy() {
		return selectedVisualNode.getCreatedBy();
	}

	@Override
	public VisualNode getSelectedVisualNode() {
		return selectedVisualNode;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends TreeGraphNode> getSubClass() {
		if (selectedVisualNode.configHasProperty(twaSubclass)) {
			String result = (String) selectedVisualNode.configGetPropertyValue(twaSubclass);
			try {
				return (Class<? extends TreeGraphNode>) Class.forName(result);
			} catch (ClassNotFoundException e) {
				throw new TwuifxException("Subclass not found in the system: "+result);

			}
		} else
			return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<VisualEdge> getOutEdges() {
		return (Iterable<VisualEdge>) selectedVisualNode.edges(Direction.OUT);
	}

	@Override
	public boolean hasOutEdgeTo(VisualNode vEnd, String edgeLabel) {
		TreeGraphNode start = selectedVisualNode.getConfigNode();
		TreeGraphNode end = vEnd.getConfigNode();
		for (ALEdge edge : start.edges(Direction.OUT)) {
			ALNode endNode = edge.endNode();
			if (endNode.id().equals(end.id()))
				if (TWA.getLabel(edge.id()).equals(edgeLabel))
					return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<VisualNode> getOutNodes() {
		return (Iterable<VisualNode>) get(selectedVisualNode.edges(Direction.OUT),
				selectZeroOrMany(),
				edgeListEndNodes()); 
	}

}
