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

import au.edu.anu.twcore.archetype.tw.IsInValueSetQuery;
import fr.cnrs.iees.graph.Tree;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.io.GraphImporter;
import fr.cnrs.iees.identity.impl.PairIdentity;

/**
 * @author Ian Davies
 *
 * @date 20 Jul 2019
 */
// Global singleton instance of the 3Warchetype: thread safe and lazy load
public class TWA {
	private static Tree<? extends TreeNode> instance;

	private TWA() {
	};

	@SuppressWarnings("unchecked")
	public static synchronized Tree<? extends TreeNode> getInstance() {
		if (instance == null) {
			instance = (Tree<? extends TreeNode>) GraphImporter.importGraph("3wArchetype.ugt", IsInValueSetQuery.class);
		}
		return instance;
	}
	
	// Static helper methods??
	public static String getLabel(String id) {
		return id.split(PairIdentity.LABEL_NAME_STR_SEPARATOR)[0];
	}

	public static String getName(String id) {
		return id.split(PairIdentity.LABEL_NAME_STR_SEPARATOR)[1];
	}

}
