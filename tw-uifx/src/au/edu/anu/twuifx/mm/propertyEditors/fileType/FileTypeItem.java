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
package au.edu.anu.twuifx.mm.propertyEditors.fileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.controlsfx.property.editor.PropertyEditor;

import au.edu.anu.rscs.aot.graph.AotNode;
import au.edu.anu.twapps.mm.GraphState;
import au.edu.anu.twcore.specificationCheck.Checkable;
import au.edu.anu.twuifx.mm.propertyEditors.SimplePropertyItem;
import fr.cnrs.iees.twcore.constants.FileType;
import javafx.stage.FileChooser;

/**
 * Author Ian davies
 *
 * Date Jan 28, 2019
 */
// TODO Problem how to avoid polluting tw-apps or tw-core with FileChooser which
// is fx stage specific

public class FileTypeItem extends SimplePropertyItem {

	private List<FileChooser.ExtensionFilter> exts;

	private FileType fileType;

	public FileTypeItem(String key, AotNode n, boolean canEdit, String category, String description,
			Checkable checker) {
		super(key, n, canEdit, category, description, checker);
		fileType= (FileType) node.getPropertyValue(key);
		exts= new ArrayList<>();
		exts.add(new FileChooser.ExtensionFilter("All files", "*.*"));
	}

	public void setExtensions(List<FileChooser.ExtensionFilter> exts) {
		this.exts = exts;
	}

	public List<FileChooser.ExtensionFilter> getExtensions() {
		return exts;
	}

	@Override
	public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
		return Optional.of(FileTypeEditor.class);
	}

	@Override
	public Object getValue() {
		return fileType.getRelativePath();
	}

	@Override
	public void setValue(Object newValue) {
		Object oldValue = getValue();
		if (!oldValue.toString().equals(newValue.toString())) {
			fileType.setRelativePath((String) newValue);
			GraphState.setChanged(true);
			checker.validateGraph();
		}
	}

//	@Override
//	public Optional<ObservableValue<? extends Object>> getObservableValue() {
//		return Optional.empty();
//	}

}
