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
import au.edu.anu.twapps.graphviz.GraphVisualisationConstants;
import javafx.util.Pair;

public  abstract class StructureEditorAdapter
		implements StructureEditable, GraphVisualisationConstants, ArchetypeConstants {
	protected Specifications specifications;
	protected SpecifiableNode targetNode;
	protected AotNode newChild;
	protected AotNode nodeSpec;

	public StructureEditorAdapter(SpecifiableNode targetNode) {
		super();
		this.newChild = null;
		this.targetNode = targetNode;
		this.nodeSpec = specifications.getSpecificationOf(targetNode.getConfigNode());
	}

	@Override
	public AotNode SetNodeLocation(double x, double y, double w, double h) {
		// rescale user's x,y into unit space
		newChild.setProperty(gvX, x / w);
		newChild.setProperty(gvY, y / h);
		return newChild;
	}

	@Override
	public boolean hasNewChild() {
		return newChild != null;
	}

	@Override
	public List<AotNode> newChildList(Iterable< AotNode> childSpecs) {
		List<AotNode> result = new ArrayList<AotNode>();
		for (AotNode childNodeSpec : childSpecs) {
			IntegerRange range = specifications.getMultiplicity(childNodeSpec, atName);
			String childLabel = specifications.getLabel(childNodeSpec);
			if (!targetNode.inRange(range, childLabel))
				result.add(childNodeSpec);
		}
		return result;
	}

	@Override
	public List<Pair<String, AotNode>> newEdgeList(Iterable<AotNode> edgeSpecs) {
		List<Pair<String, AotNode>> result = new ArrayList<>();
		List<String> edgePropXorOptions = specifications.getConstraintOptions(nodeSpec, atConstraintEdgePropXor);
		List<String> nodeNodeXorOptions = specifications.getConstraintOptions(nodeSpec, atConstraintNodeNodeXor);

		for (AotNode edgeSpec : edgeSpecs) {
			String nodeLabel = specifications.getEdgeToNodeLabel(edgeSpec);
			List<String> edgeLabelOptions = specifications.getConstraintOptions(edgeSpec, atConstraintElementLabel);
			// we now need the node list of the graph!
		}
		return result;
	}
	public List<AotNode> orphanedChildList(Iterable<AotNode> childSpecs) {
		List<AotNode> result = new ArrayList<>();
		
		return result;
	}
	protected boolean haveSpecification() {
		return nodeSpec != null;
	}

	


}
