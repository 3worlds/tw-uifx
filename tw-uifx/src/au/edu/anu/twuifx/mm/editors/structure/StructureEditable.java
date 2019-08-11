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

import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.ens.biologie.generic.utils.Duple;

/**
 * Author Ian Davies
 *
 * Date 11 Jan. 2019
 */
public interface StructureEditable {

	/*
	 * Filters a list of possible children depending on current state of the
	 * configuration
	 */
	public List<SimpleDataTreeNode> filterChildSpecs(Iterable<SimpleDataTreeNode> childNodeSpecs);

	/*
	 * Filters a list of edge labels and eligible node pairs to be connected from a
	 * list of all possible edge specifications
	 */
	public List<Duple<String, VisualNode>> filterEdgeSpecs(Iterable<SimpleDataTreeNode> edgeSpecs);

	public List<VisualNode> orphanedChildList(Iterable<SimpleDataTreeNode> childSpecs);

	public void onConnectToOrphanedChild(VisualNode child);

	/* create child of node currently being edited base on childSpec */
	public void onNewChild(String childLabel, SimpleDataTreeNode childBaseSpec);

	/* add edge call String,from node currently being editied to a visualNode */
	public void onNewEdge(Duple<String, VisualNode> duple);

	/* delete the node currently being edited */
	public void onDeleteNode();

	/* collapse tree from the node currently being edited */
	public void onCollapseTree();

	/* expand tree from the node currently being edited */
	public void onExpandTree();
	
	public void onSetChild(VisualNode childNode);

	public void buildgui();

}
