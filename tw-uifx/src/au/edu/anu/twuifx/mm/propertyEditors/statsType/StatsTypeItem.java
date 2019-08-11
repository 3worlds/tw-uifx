/**************************************************************************
 *  TW-UIFX - ThreeWorlds User-Interface fx                               *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-UIFX contains the Javafx interface for ModelMaker and ModelRunner. *
 *  This is to separate concerns of UI implementation and the code for    *
 *  these java programs.                                                  *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-UIFX (ThreeWorlds User-Interface fx).         *
 *                                                                        *
 *  TW-UIFX is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-UIFX is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-UIFX.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>.                  *
 *                                                                        *
 **************************************************************************/

package au.edu.anu.twuifx.mm.propertyEditors.statsType;

import java.util.Optional;

import org.controlsfx.property.editor.PropertyEditor;

import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.specificationCheck.Checkable;
import au.edu.anu.twuifx.mm.propertyEditors.SimplePropertyItem;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;

/**
 * Author Ian Davies
 *
 * Date 14 Feb. 2019
 */
public class StatsTypeItem extends SimplePropertyItem{

	public StatsTypeItem(String key, TreeGraphDataNode n, boolean canEdit, String category, String description,
			Checkable checker) {
		super(key, n, canEdit, category, description, checker);
	}
	
	@Override
	public void setValue(Object newString) {
		StatisticalAggregatesSet oldValue = (StatisticalAggregatesSet) node.properties().getPropertyValue(key);
		String oldString = oldValue.toString();
		if (!oldString.equals(newString)) {
			StatisticalAggregatesSet newValue = StatisticalAggregatesSet.valueOf((String) newString);
//			node.addProperty(key, newValue);
			node.properties().setProperty(key, newValue);
			GraphState.setChanged();
			checker.validateGraph();
		}
	}
	@Override
	public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
		return Optional.of(StatsTypeEditor.class);
	}


}
