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

import static fr.cnrs.iees.io.parsing.TextGrammar.DIM_BLOCK_DELIMITERS;
import static fr.cnrs.iees.io.parsing.TextGrammar.DIM_ITEM_SEPARATOR;
import static fr.cnrs.iees.io.parsing.TextGrammar.TABLE_BLOCK_DELIMITERS;
import static fr.cnrs.iees.io.parsing.TextGrammar.TABLE_ITEM_SEPARATOR;

import java.util.Optional;

import org.controlsfx.property.editor.PropertyEditor;

import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.collections.tables.Table;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twuifx.mm.propertyEditors.SimplePropertyItem;
import fr.cnrs.iees.graph.ElementAdapter;

/**
 * @author Ian Davies
 *
 * @date 15 Dec 2019
 */
public class StringTableItem extends SimplePropertyItem {
	protected char[][] bdel = new char[2][2];
	protected char[] isep = new char[2];

	public StringTableItem(IMMController controller, String key, ElementAdapter element, boolean canEdit, String category, String description) {
		super(controller,key, element, canEdit, category, description);
		bdel[Table.DIMix] = DIM_BLOCK_DELIMITERS;
		bdel[Table.TABLEix] = TABLE_BLOCK_DELIMITERS;
		isep[Table.DIMix] = DIM_ITEM_SEPARATOR;
		isep[Table.TABLEix] = TABLE_ITEM_SEPARATOR;
	}

	@Override
	public Object getValue() {
		return super.getValue().toString();
	}

	@Override
	public void setValue(Object value) {
		String newValue = (String)value;
		StringTable oldTable = (StringTable) getElementProperties().getPropertyValue(key);
		String oldValue = oldTable.toString();
		// NB Tables do not have an equals() function!
		if (!oldValue.equals(newValue)){
			StringTable newTable = StringTable.valueOf(newValue,bdel,isep);
			onUpdateProperty(newTable);
		}
	}

	@Override
	public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
		return Optional.of(StringTableEditor.class);
	}

}
