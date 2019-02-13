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

import java.io.File;

import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

public class FileTypeEditor extends AbstractPropertyEditor<String,Pane>{
	
	private static LabelButtonControl view = new LabelButtonControl("Open16.gif", Images.imagePackage);

	//private FileTypeItem fileTypeItem;
	public FileTypeEditor(FileTypeItem property, Pane control) {
		super(property, control);
	}
	public FileTypeEditor(FileTypeItem property) {
		this(property,view);
		view.setAction(e->onButtonClicked());
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ObservableValue<String> getObservableValue() {
		// TODO Auto-generated method stub
		return null;
	}
	private void onButtonClicked() {
		File root = Project.makeFile("");
		FileChooser fc = new FileChooser();
		fc.setTitle("Select file");
		fc.setInitialDirectory(root);
		FileTypeItem fileTypeItem = (FileTypeItem)getProperty();
		fc.getExtensionFilters().addAll(fileTypeItem.getExtensions());
		if (!fc.getExtensionFilters().isEmpty())
			fc.setSelectedExtensionFilter(fc.getExtensionFilters().get(0));
		File file = fc.showOpenDialog(Dialogs.getParentWindow());
	}

//	protected LabelButtonControl getLBEditor() {
//		return (LabelButtonControl) getEditor();
//	}


}
