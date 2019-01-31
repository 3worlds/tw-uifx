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
import au.edu.anu.rscs.aot.graph.AotNode;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import javafx.util.Pair;

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
	public List<AotNode> newChildList(Iterable<AotNode> childNodeSpecs);

	/*
	 * Filters a list of edge labels and eligible node pairs to be connected from a
	 * list of all possible edge specifications
	 */
	public List<Pair<String, AotNode>> newEdgeList(Iterable<AotNode> edgeSpecs);

	public List<AotNode> orphanedChildList(Iterable<AotNode> childSpecs);
	
	public String promptForNewNode(String label,String promptName);

	public void buildgui();

}
