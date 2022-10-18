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

package au.edu.anu.twuifx.mm.propertyEditors.StringTable;

import java.util.Optional;

import org.controlsfx.property.editor.PropertyEditor;

import au.edu.anu.omugi.collections.tables.StringTable;
import au.edu.anu.omugi.collections.tables.Table;
import au.edu.anu.twapps.mm.MMController;
import au.edu.anu.twuifx.mm.propertyEditors.SimpleMMPropertyItem;
import fr.cnrs.iees.graph.ElementAdapter;

/**
 * Property item for {@link StringTable}.
 * 
 * @author Ian Davies - 15 Dec 2019
 */
public class StringTableItem extends SimpleMMPropertyItem {

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
	public StringTableItem(MMController controller, String key, ElementAdapter element, boolean canEdit,
			String category, String description) {
		super(controller, key, element, canEdit, category, description);
	}

	@Override
	public Object getValue() {
		Table table = (Table) super.getValue();
		return table.toSaveableString();
	}

	@Override
	public void setValue(Object value) {
		Table oldTable = (Table) properties.getPropertyValue(key);
		String oldValue = oldTable.toSaveableString();
		String newValue = (String) value;
		// NB Tables do not have an equals() function!
		if (!oldValue.equals(newValue)) {
			StringTable newTable = StringTable.valueOf((String) value);
			onUpdateProperty(newTable);
		}
	}

	@Override
	public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
		return Optional.of(StringTableEditor.class);
	}

}
