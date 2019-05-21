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
import au.edu.anu.twapps.mm.visualGraph.VisualGraph;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.identity.impl.PairIdentity;
import fr.cnrs.iees.twcore.constants.Configuration;
import javafx.util.Pair;

public class SpecifiedNode implements SpecifiableNode, Configuration {
	private VisualNode visualNode;

	public SpecifiedNode(VisualNode visualNode) {
		this.visualNode = visualNode;

	}

	@Override
	public List<VisualNode> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeGraphDataNode getConfigNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canDelete() {
		return getLabel().equals(N_ROOT);
	}

	@Override
	public boolean inRange(IntegerRange range, String childLabel) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLabel() {
		return visualNode.getLabel();
	}

	@Override
	public List<VisualNode> graphRoots() {
		List<VisualNode> result = new ArrayList<>();
		//TODO maybe TYPECAST CRASH here??
		VisualGraph vg = (VisualGraph) visualNode.factory();
		for (VisualNode root : vg.roots())
			result.add(root);
		return result;
	}

	@Override
	public boolean haschildren() {
		// TODO Auto-generated method stub
		return false;
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
	public VisualNode newChild(TreeNode specs, String label, String name) {
		TreeGraphDataNode configParent = getConfigNode();

		TreeGraphDataNode configChild = (TreeGraphDataNode)	configParent
				.factory().makeNode(label + PairIdentity.LABEL_NAME_STR_SEPARATOR + name);
		configChild.connectParent(configParent);

		VisualNode childVisualNode =  (VisualNode) visualNode.factory().makeNode(
				label + PairIdentity.LABEL_NAME_STR_SEPARATOR + name);
		childVisualNode.connectParent(visualNode);
		childVisualNode.setConfigNode(configChild);

		return childVisualNode;
	}

}
