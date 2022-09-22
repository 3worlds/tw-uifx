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

package au.edu.anu.twuifx.mm.propertyEditors.trackerType;

import java.util.Optional;

import org.controlsfx.property.editor.PropertyEditor;

import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twuifx.mm.propertyEditors.SimpleMMPropertyItem;
import fr.cnrs.iees.graph.ElementAdapter;
//import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.twcore.constants.TrackerType;

/**
 * @author Ian Davies - 12 Oct 2019
 */
public class TrackerTypeItem extends SimpleMMPropertyItem {

	public TrackerTypeItem(IMMController controller, String key, ElementAdapter element, boolean canEdit, String category,
			String description) {
		super(controller, key, element, canEdit, category, description);
	}

	@Override
	public void setValue(Object newValue) {
		Object oldValue = getValue();
		if (!oldValue.toString().equals(newValue.toString())) {
			TrackerType tt = TrackerType.valueOf((String) newValue);
			onUpdateProperty(tt);
		}
	}
	public ElementAdapter getElement() {
		return element;
	}

	@Override
	public Object getValue() {
		TrackerType tt = (TrackerType) properties.getPropertyValue(key);
		return tt.toString();
	}

	@Override
	public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
		return Optional.of(TrackerTypeEditor.class);
	}

}
