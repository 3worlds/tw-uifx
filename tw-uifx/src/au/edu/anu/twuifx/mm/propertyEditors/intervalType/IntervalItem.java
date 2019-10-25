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

package au.edu.anu.twuifx.mm.propertyEditors.intervalType;

import java.util.Optional;

import org.controlsfx.property.editor.PropertyEditor;

import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twuifx.mm.propertyEditors.SimplePropertyItem;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.ens.biologie.generic.utils.Interval;

/**
 * @author Ian Davies
 *
 * @date 24 Oct 2019
 */
public class IntervalItem extends SimplePropertyItem {

	public IntervalItem(IMMController controller, String key, TreeGraphDataNode n, boolean canEdit, String category, String description) {
		super(controller,key, n, canEdit, category, description);
	}



	@Override
	public Object getValue() {
		return node.properties().getPropertyValue(key).toString();
	}

	@Override
	public void setValue(Object value) {
		Interval oldValue = (Interval) node.properties().getPropertyValue(key);
		Interval newValue = Interval.valueOf((String) value);
		if (!oldValue.equals(newValue)) {
			onUpdateProperty(newValue);
		}
	}

	@Override
	public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
		return Optional.of(IntervalEditor.class);
	}

}
