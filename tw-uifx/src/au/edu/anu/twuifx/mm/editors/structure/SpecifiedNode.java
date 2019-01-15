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

import au.edu.anu.rscs.aot.graph.AotNode;
import au.edu.anu.rscs.aot.util.IntegerRange;
import fr.cnrs.iees.twcore.constants.Configuration;
import javafx.util.Pair;

public class SpecifiedNode implements SpecifiableNode, Configuration {
	private AotNode visualNode;

	public SpecifiedNode(AotNode visualNode) {
		this.visualNode = visualNode;
	}

	@Override
	public List<AotNode> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AotNode getConfigNode() {
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
	public List<AotNode> graphRoots() {
		List<AotNode> result = new ArrayList<>();
		for (AotNode root : visualNode.treeNodeFactory().roots())
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

	// This can go in omghtk in some form;
	@Override
	public String getUniqueName(String label, String name) {
		AotNode n = getConfigNode();
		Iterable<AotNode> nodes = n.nodeFactory().findNodesByReference(label+AotNode.LABEL_NAME_SEPARATOR+name);
		if (!nodes.iterator().hasNext())
				return name;
		else {
			Pair<String,Integer> nameInstance = parseName(name);
			int count = nameInstance.getValue()+1;
			name = nameInstance.getKey()+count;
			return getUniqueName(label,name);
		}
	}
	private static Pair<String,Integer> parseName(String name){
		int idx = getCountStartIndex(name);
		// all numbers or no numbers
		//no numbers
		if (idx<0)
			return new Pair<>(name,0);
		// all numbers
		if (idx==0)
			return new Pair<String, Integer>(name+"_", 0);
		// ends with some numbers
		String key = name.substring(0, idx);
		String sCount = name.substring(idx, name.length());
		int count = Integer.parseInt(sCount);
		return new Pair<>(key, count);
	}
	
	private static int getCountStartIndex(String name) {
		int result = -1;
		for (int i = name.length() - 1; i >= 0; i--) {
			String s = name.substring(i, i + 1);
			try {
				Integer x = Integer.parseInt(s);
				result = i;
			} catch (NumberFormatException e) {
				return result;
			}
		}
		return result;

	}

	@Override
	public AotNode newChild(AotNode specs, String label, String name) {
		AotNode configParent = getConfigNode();
		AotNode configChild = configParent.nodeFactory().makeTreeNode(configParent);
		configChild.setLabel(label);
		configChild.setName(name);
		
		AotNode  childVisualNode = visualNode.nodeFactory().makeTreeNode(visualNode);
		// TODO link the two
		
		return childVisualNode;
	}


}
