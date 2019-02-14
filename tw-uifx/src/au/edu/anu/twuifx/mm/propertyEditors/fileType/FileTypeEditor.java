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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;

public class FileTypeEditor extends AbstractPropertyEditor<String, Pane> {

	private static LabelButtonControl view = new LabelButtonControl("Open16.gif", Images.imagePackage);

	public FileTypeEditor(FileTypeItem property, Pane control) {
		super(property, control);
	}

	public FileTypeEditor(FileTypeItem property) {
		this(property, view);
		view.setAction(e -> onButtonClicked());
	}

	@Override
	public void setValue(String value) {
		LabelButtonControl control = (LabelButtonControl) getEditor();
		control.setText(value);
	}

	@Override
	protected ObservableValue<String> getObservableValue() {
		LabelButtonControl control = (LabelButtonControl) getEditor();
		return control.getTextProperty();
	}

	// This all needs refactoring
	private void onButtonClicked() {
		File root = Project.makeFile("");
		FileTypeItem fileTypeItem = (FileTypeItem) getProperty();
		File file = Dialogs.getOpenFile(root, "Select file", fileTypeItem.getExtensions());
		String relativePath = null;
		if (file != null) {
			if (file.getAbsolutePath().contains(root.getAbsolutePath()))
				relativePath = makeProjectRelative(file.getAbsolutePath());
			else if (Dialogs.confirmation("Import file", "", "Import " + file.getName() + " to the current project?")) {
				File newFile = importFile(file);
				relativePath = makeProjectRelative(newFile.getAbsolutePath());
			}
		} else {
			if (Dialogs.confirmation("Query", "Clear property value?", "")) {
				relativePath = "";
			}
		}
		if (relativePath != null)
			setValue(relativePath);
	}

	private File importFile(File inFile) {
		File result = new File(Project.makeFile("").getAbsolutePath() + File.separator + inFile.getName());
		try {
			Files.copy(inFile.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String makeProjectRelative(String s) {
		String root = Project.makeFile("").getAbsolutePath();
		if (s.contains(root))
			s = s.replace(root, "");
		if (s.startsWith(File.separator))
			s = s.replaceFirst("\\\\", "");
		s = s.replaceAll("\\\\", "/");
		return s;
	}

}
