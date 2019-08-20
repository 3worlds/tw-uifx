/**************************************************************************
 *  TW-CORE - 3Worlds Core classes and methods                            *
 *                                                                        *
 *  Copyright 2018: Shayne Flint, Jacques Gignoux & Ian D. Davies         *
 *       shayne.flint@anu.edu.au                                          * 
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-CORE is a library of the principle components required by 3W       *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-CORE (3Worlds Core).                          *
 *                                                                        *
 *  TW-CORE is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-CORE is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-CORE.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
 *                                                                        *
 **************************************************************************/

package au.edu.anu.twuifx.mm.editors.structure;

import java.util.List;

import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;

// just experimenting with what services mm requires of an aotnode.

// impl will have a VisualNode which hosts the configuration node
public interface VisualNodeEditable {
	public boolean hasChildren();

	/* return the class value or null from the hosted config node */
	public String getClassValue();

	/* get the configuration node under-pinning this */
	public TreeGraphNode getConfigNode();

	/*
	 * normally true unless this is the configuration root (3worlds:<projectName>)
	 */
	public boolean canDelete();

	/* true if the this node can have more children of this label */
	public boolean moreChildrenAllowed(IntegerRange range, String childLabel);

	public Iterable<VisualNode> graphRoots();

	public boolean hasOutEdges();
	
	public Iterable<VisualEdge> getOutEdges();

	public boolean isLeaf();

	public boolean isCollapsed();

	public VisualNode newChild(String label, String name);

	public String proposeAnId(String proposedName);
	
	public String createdBy();

	public VisualNode getSelectedVisualNode();

	public Class<? extends TreeGraphNode> getSubClass();

	public boolean hasOutEdgeTo(VisualNode endNode, String edgeLabel);

	public Iterable<VisualNode> getOutNodes();

	public String cClassId();

	
	//public void addProperty(String key, Object defaultValue);

}
