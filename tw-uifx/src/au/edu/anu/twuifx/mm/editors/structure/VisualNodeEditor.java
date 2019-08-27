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
import java.util.ArrayList;
import java.util.List;

import au.edu.anu.rscs.aot.archetype.ArchetypeArchetypeConstants;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TwArchetypeConstants;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.ALNode;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.identity.Identity;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;

public class VisualNodeEditor implements //
		VisualNodeEditable, //
		ArchetypeArchetypeConstants, //
		TwArchetypeConstants {
	private VisualNode visualNode;
	private TreeGraph<VisualNode, VisualEdge> visualGraph;

	public VisualNodeEditor(VisualNode visualNode, TreeGraph<VisualNode, VisualEdge> visualGraph) {
		this.visualNode = visualNode;
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
		return visualNode.getConfigNode();
	}

	@Override
	public boolean canDelete() {
		return !visualNode.cClassId().equals(N_ROOT.label());
	}

	@Override
	public boolean moreChildrenAllowed(IntegerRange range, String childLabel) {
		List<VisualNode> lst = new ArrayList<>();
		for (VisualNode child : visualNode.getChildren()) {
			String label =child.cClassId();
			if (label.equals(childLabel))
				lst.add(child);
		}
		return range.inRange(lst.size() + 1);
	}


	@Override
	public Iterable<VisualNode> graphRoots() {
		return visualGraph.roots();
	}

	@Override
	public boolean hasOutEdges() {
		return visualNode.edges(Direction.OUT).iterator().hasNext();
	}

	@Override
	public boolean isLeaf() {
		return visualNode.isLeaf();
	}

	@Override
	public boolean isCollapsed() {
		return visualNode.isCollapsed();
	}


	@Override
	public VisualNode newChild(String label, String proposedId) {
		return visualNode.newChild(label, proposedId);
	}

	@Override
	public String proposeAnId(String proposedName) {
		Identity id = visualNode.scope().newId(false, proposedName);
		return id.id();
	}

	@Override
	public String createdBy() {
		return visualNode.getCreatedBy();
	}

	@Override
	public VisualNode getSelectedVisualNode() {
		return visualNode;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends TreeGraphNode> getSubClass() {
		if (visualNode.configHasProperty(twaSubclass)) {
			String result = (String) visualNode.configGetPropertyValue(twaSubclass);
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
		return (Iterable<VisualEdge>) visualNode.edges(Direction.OUT);
	}

	@Override
	public boolean hasOutEdgeTo(VisualNode vEnd, String edgeLabel) {
		TreeGraphNode cStart = visualNode.getConfigNode();
		TreeGraphNode cEnd = vEnd.getConfigNode();
		for (ALEdge cEdge : cStart.edges(Direction.OUT)) {
			ALNode cEndNode = cEdge.endNode();
			if (cEndNode.id().equals(cEnd.id()))
				if ((cEdge.classId()).equals(edgeLabel))
					return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<VisualNode> getOutNodes() {
		return (Iterable<VisualNode>) get(visualNode.edges(Direction.OUT),
				selectZeroOrMany(),
				edgeListEndNodes()); 
	}

	@Override
	public String cClassId() {
		return visualNode.cClassId();
	}

	@Override
	public VisualEdge newEdge(String label, VisualNode vEnd) {
		return visualNode.newEdge(label,vEnd);
	}

	@Override
	public void reconnectChild(VisualNode vnChild) {
		visualNode.reconnectChild(vnChild);
		
	}

}
