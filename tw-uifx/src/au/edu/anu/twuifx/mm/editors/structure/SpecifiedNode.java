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

import java.util.List;

import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.identity.impl.PairIdentity;
import fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels;
import javafx.util.Pair;// remove fx dependency here
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

public class SpecifiedNode implements SpecifiableNode {
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
	public boolean inRange(IntegerRange range, String childLabel) {
		// TODO check again when we have children
		List<SimpleDataTreeNode> lst = (List<SimpleDataTreeNode>) get(selectedVisualNode.getChildren(),
				selectZeroOrMany(hasTheLabel(childLabel)));
		int c = lst.size();
		return range.inRange(c);
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

	@Override
	public String getUniqueName(String label, String name) {
//		// the id system has now been taken over by Identity system - we cant get one without it been added so we are lost here.
//		// must do duplicate code I think!!!
//		TreeGraphDataNode n = getConfigNode();
//		Iterable<TreeGraphDataNode> nodes = n.nodeFactory()
//				.findNodesByReference(label + PairIdentity.LABEL_NAME_SEPARATOR + name);
//		if (!nodes.iterator().hasNext())
//			return name;
//		else {
//			Pair<String, Integer> nameInstance = parseName(name);
//			int count = nameInstance.getValue() + 1;
//			name = nameInstance.getKey() + count;
//			return getUniqueName(label, name);
//		}
		throw new TwuifxException("getUniqueName not yet implemented!!!");
	}

	@Deprecated
	private static Pair<String, Integer> parseName(String name) {
		int idx = getCountStartIndex(name);
		// all numbers or no numbers
		// no numbers
		if (idx < 0)
			return new Pair<>(name, 0);
		// all numbers
		if (idx == 0)
			return new Pair<String, Integer>(name + "_", 0);
		// ends with some numbers
		String key = name.substring(0, idx);
		String sCount = name.substring(idx, name.length());
		int count = Integer.parseInt(sCount);
		return new Pair<>(key, count);
	}

	@Deprecated
	private static int getCountStartIndex(String name) {
		int result = -1;
		for (int i = name.length() - 1; i >= 0; i--) {
			String s = name.substring(i, i + 1);
			try {
				Integer.parseInt(s);
				result = i;
			} catch (NumberFormatException e) {
				return result;
			}
		}
		return result;

	}

	@Override
	public VisualNode newChild(SimpleDataTreeNode specs, String label, String name) {
		TreeGraphNode configParent = getConfigNode();

		TreeGraphDataNode configChild = (TreeGraphDataNode) configParent.factory()
				.makeNode(label + PairIdentity.LABEL_NAME_STR_SEPARATOR + name);
		configChild.connectParent(configParent);

		VisualNode childVisualNode = (VisualNode) selectedVisualNode.factory()
				.makeNode(label + PairIdentity.LABEL_NAME_STR_SEPARATOR + name);
		childVisualNode.connectParent(selectedVisualNode);
		childVisualNode.setConfigNode(configChild);

		return childVisualNode;
	}

	@Override
	public String proposeId() {
		selectedVisualNode.scope().newId(proposedId)
		return null;
	}


}
