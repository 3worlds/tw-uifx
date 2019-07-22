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
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;

/**
 * Author Ian Davies
 *
 * Date 10 Jan. 2019
 */

/*
 * These are the basic public methods required of an archetype implementation.
 * 
 * NOTE: There are two uses: CHECKING compliance and BUILDING a configuration
 * file. Queries are not executed when BUILDING.
 * 
 * This interface is the contract between the archetype and what a builder (e.g MM) requires
 * 
 * 
 */
// Develop in this library but move to tw-core later - saves time!!
public interface Specifications {

	/* True if the archetype is a valid archetype */
	public boolean complies();

	/*
	 * runs all checks against the given node . Nodes without an spec can't be
	 * checked. I'm avoiding complies(AotGraph graph) at the moment
	 */
	public boolean complies(TreeGraphNode node, TreeNode root);

	/*
	 * get specification of a given node from the configuration graph. If null, it
	 * can't be checked.
	 */
	public TreeNode getSpecificationOf(TreeGraphNode configurationNode);

	/*
	 * Specifications of all potential children of a parent with this label and
	 * class.
	 */
	public Iterable<TreeNode> getChildSpecificationsOf(TreeNode parentSpec);

	/* edge specification nodes of a node with this label and class */
	public Iterable<TreeNode> getEdgeSpecificationsOf(String parentLabel, TreeNode parentSpec, String parentClass);

	/* property specs of the given node spec (root) */
	public Iterable<TreeNode> getPropertySpecifications(TreeNode rootSpec);

	/* Get multiplicity of a property specification */
	public IntegerRange getMultiplicity(TreeNode rootSpec, String key);

	/* whats this for*/
	//public IntegerRange getMultiplicity(TreeNode spec);

	/* True if node name must begin with upper case letter */
	public boolean nameStartsWithUpperCase(TreeNode root);

	/* returns just the label for nodes of this specification */
	public String getLabel(TreeNode root);

	/* Items in the object table of these constraints. Preserve the order */
	public List<String> getConstraintOptions(TreeNode rootSpec, String constraintClass);

	public String getEdgeToNodeLabel(TreeNode edgeSpec);

	// TODO more to come...

}
