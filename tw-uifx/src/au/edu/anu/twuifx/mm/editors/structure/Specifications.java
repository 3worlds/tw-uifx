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
 * TODO: All this needs careful documenting
 */

public interface Specifications {

	/*
	 * get specification of a given node from the configuration graph. If null, it
	 * won't be checked.
	 */
	public SimpleDataTreeNode getSpecsOf(String cClassId, String createdBy, TreeNode root, Set<String> discoveredFiles);

	/*Returns the sub archetype spec of the given class of this baseSpec. Often will return null*/
	public SimpleDataTreeNode getSubSpecsOf(SimpleDataTreeNode baseSpecs, Class<? extends TreeNode> subClass);

	/*
	 * Specifications of all potential children of a parent with this label and
	 * optional subClass.
	 */
	public Iterable<SimpleDataTreeNode> getChildSpecsOf(SimpleDataTreeNode baseSpec, SimpleDataTreeNode subSpec,
			TreeNode root);

	/* edge specifications nodes of a node with this label and class */
	public Iterable<SimpleDataTreeNode> getEdgeSpecsOf(SimpleDataTreeNode baseSpec, SimpleDataTreeNode subSpec);

	/*
	 * Returns all property specs of the given spec, both the base class spec and
	 * its optional sub class spec (can be null)
	 */
	public Iterable<SimpleDataTreeNode> getPropertySpecsOf(SimpleDataTreeNode baseSpec, SimpleDataTreeNode subSpec);

	/* Returns the integerRange class of the spec's multiplicity property */
	public IntegerRange getMultiplicityOf(SimpleDataTreeNode spec);

	/* True if node name must begin with upper case letter - not sure if should be here?*/
	public boolean nameStartsWithUpperCase(SimpleDataTreeNode spec);

	public List<Class<? extends TreeNode>> getSubClassesOf(SimpleDataTreeNode spec);

	/*
	 * returns all query specs of the given query classes for the given parent spec
	 */
	public List<SimpleDataTreeNode> getQueries(SimpleDataTreeNode parentSpec, Class<? extends Query>... queryClass);

	/* check the use of this. It should use the above function */
	public List<String[]> getQueryStringTables(SimpleDataTreeNode spec, Class<? extends Query> queryClass);

	/* returns false if no option chosen: expects user input */
	@SuppressWarnings("unchecked")
	public boolean filterPropertyStringTableOptions(Iterable<SimpleDataTreeNode> propertySpecs,
			SimpleDataTreeNode baseSpec, SimpleDataTreeNode subSpec, String childId,
			Class<? extends Query>... queryClasses);

	/*Returns a list of Duple<NodeLabel1,NodeLabel2> optons for the given query list*/
	public List<Duple<String, String>> getNodeLabelDuples(List<SimpleDataTreeNode> queries);

}
