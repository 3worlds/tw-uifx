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
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.graph.NodeFactory;
import fr.cnrs.iees.graph.TreeNode;
//import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.identity.Identity;
import fr.cnrs.iees.identity.impl.PairIdentity;
import fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels;
//import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
//import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

public class SpecifiedNode implements SpecifiableNode,ArchetypeArchetypeConstants {
	private VisualNode selectedVisualNode;
	private TreeGraph<VisualNode, VisualEdge> visualGraph ;

	public SpecifiedNode(VisualNode visualNode, TreeGraph<VisualNode, VisualEdge> visualGraph) {
		this.selectedVisualNode = visualNode;
		this.visualGraph=visualGraph;
	}

	@Override
	public boolean hasChildren() {
		return selectedVisualNode.getChildren().iterator().hasNext();
	}

	@Override
	public String getClassValue() {
		// TODO Auto-generated method stub
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean moreChildrenAllowed(IntegerRange range, String childLabel) {
		// we need a query hasTheName startsWith
//		List<SimpleDataTreeNode> lstx = (List<SimpleDataTreeNode>) get(selectedVisualNode.getChildren(),
//				selectZeroOrMany(id(),startsWith(childLabel+PairIdentity.LABEL_NAME_STR_SEPARATOR)));
		//hasTheName startsWith
		List<TreeNode> lst = new ArrayList<>();
		for (TreeNode child:selectedVisualNode.getChildren()) {
			String label = TWA.getLabel(child.id());
			if (label.equals(childLabel))
				lst.add(child);
		}
		return range.inRange(lst.size()+1);
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCollapsed() {
		// TODO Auto-generated method stub
		return false;
	}


	public static VisualNode newChild(VisualNode parent,String label, String name) {
		String proposedId = label + PairIdentity.LABEL_NAME_STR_SEPARATOR + name;
		TreeGraphNode configParent = parent.getConfigNode();
		NodeFactory cf =  configParent.factory();
		TreeGraphDataNode configChild=(TreeGraphDataNode) cf.makeNode(cf.nodeClass(label), proposedId);
		configChild.connectParent(configParent);

		NodeFactory vf = parent.factory();
		VisualNode childVisualNode = (VisualNode) vf.makeNode(proposedId);
		childVisualNode.connectParent(parent);
		childVisualNode.setCreatedBy(TWA.getLabel(parent.id()));
		childVisualNode.setConfigNode(configChild);
		childVisualNode.setCategory();
		return childVisualNode;
		
	}
	
	@Override
	public VisualNode newChild(String label, String name) {
		return newChild(selectedVisualNode,label,name);
	}

	@Override
	public String proposeAnId(String label, String proposedName) {
		Identity id = selectedVisualNode.scope().newId(false,label,PairIdentity.LABEL_NAME_STR_SEPARATOR,proposedName);
		return TWA.getName(id.id());
	}


//	@Override
//	public String getUniqueName(String label, String proposedName) {
//		Identity id = selectedVisualNode.scope().newId(true,label,PairIdentity.LABEL_NAME_STR_SEPARATOR,proposedName);
//		return TWA.getName(id.id());
//	}

	@Override
	public String createdBy() {
		return selectedVisualNode.getCreatedBy();
	}


}
