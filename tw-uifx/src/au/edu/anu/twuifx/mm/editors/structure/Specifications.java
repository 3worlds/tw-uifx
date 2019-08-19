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
import java.util.Set;

import au.edu.anu.rscs.aot.queries.Query;
import au.edu.anu.rscs.aot.util.IntegerRange;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.ens.biologie.generic.utils.Duple;

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
 * This interface is the contract between the archetype and what a builder (e.g
 * MM) requires
 * 
 * 
 */
// Develop in this library but move to tw-core later - saves time!!
public interface Specifications {

	// checking should be a static method of TWA?
	/* True if the archetype is a valid archetype */
//	public boolean complies();

	/*
	 * runs all checks against the given node . Nodes without an spec can't be
	 * checked. I'm avoiding complies(AotGraph graph) at the moment
	 */
//	public boolean complies(TreeGraphNode node, SimpleDataTreeNode root);

	/*
	 * get specification of a given node from the configuration graph. If null, it
	 * can't be checked.
	 */
	public SimpleDataTreeNode getSpecsOf(TreeGraphNode configurationNode, String createdBy, TreeNode root, Set<String> discoveredFiles);

	public SimpleDataTreeNode getSubSpecsOf(SimpleDataTreeNode baseSpecs, Class<? extends TreeGraphNode> subClass);

	/*
	 * Specifications of all potential children of a parent with this label and
	 * optional subClass.
	 */
	public Iterable<SimpleDataTreeNode> getChildSpecsOf(SimpleDataTreeNode baseSpec,
			SimpleDataTreeNode subSpec, TreeNode root);

	/* edge specifications nodes of a node with this label and class */
	public Iterable<SimpleDataTreeNode> getEdgeSpecsOf(SimpleDataTreeNode baseSpec, SimpleDataTreeNode subSpec);

	/* property specs of the given node spec (root) */
	public Iterable<SimpleDataTreeNode> getPropertySpecsOf(SimpleDataTreeNode baseSpec,
			SimpleDataTreeNode subSpec);


	public IntegerRange getMultiplicityOf(SimpleDataTreeNode spec);

	/* True if node name must begin with upper case letter */
	public boolean nameStartsWithUpperCase(SimpleDataTreeNode root);


	public List<Class<? extends TreeNode>> getSubClassesOf(SimpleDataTreeNode spec);
	
	public List<SimpleDataTreeNode> getQueries(SimpleDataTreeNode spec, Class<? extends Query>... queryClass);

	public List<String[]> getQueryStringTables(SimpleDataTreeNode spec, Class<? extends Query> queryClass);


	/* returns false of no option chosen: expects user input*/
	@SuppressWarnings("unchecked")
	public boolean filterPropertyStringTableOptions(Iterable<SimpleDataTreeNode> propertySpecs, SimpleDataTreeNode baseSpec,
			SimpleDataTreeNode subSpec, String childId,Class<? extends Query>... queryClasses);

	public List<Duple<String, String>> getNodeLabelDuples(List<SimpleDataTreeNode> queries);


}
