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

package au.edu.anu.twuifx.mm.propertyEditors.integerRangeType;

import java.util.Optional;

import org.controlsfx.property.editor.PropertyEditor;

import au.edu.anu.twapps.mm.MMController;
import au.edu.anu.twuifx.mm.propertyEditors.SimpleMMPropertyItem;
import fr.cnrs.iees.omugi.graph.ElementAdapter;
import au.edu.anu.omhtk.util.IntegerRange;

/**
 * Property item for {@link IntegerRange}.
 * 
 * @author Ian Davies - 24 Oct 2019
 */
public class IntegerRangeItem extends SimpleMMPropertyItem {

	/**
	 * 
	 * @param controller  ModelMaker controller, used to coordinated updates across
	 *                    two property sheets.
	 * @param key         The unique key of the property in the element's property
	 *                    list.
	 * @param element     The element (Node or Edge) containing the property list.
	 * @param canEdit     True if editing of this property is allowed, false
	 *                    otherwise.
	 * @param category    The sub-tree to which this element belongs. This is used
	 *                    in the property sheet to categorized items.
	 * @param description Not implemented. Intended as help info for the property.
	 */
	public IntegerRangeItem(MMController controller, String key, ElementAdapter element, boolean canEdit, String category, String description) {
		super(controller,key, element, canEdit, category, description);
	}

	@Override
	public Object getValue() {
		return super.getValue().toString();
	}

	@Override
	public void setValue(Object value) {
		IntegerRange oldValue = (IntegerRange) properties.getPropertyValue(key);
		IntegerRange newValue = IntegerRange.valueOf(value.toString());
		if (!oldValue.equals(newValue)) {
			onUpdateProperty(newValue);
		}
	}

	@Override
	public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
		return Optional.of(IntegerRangeEditor.class);
	}

}
