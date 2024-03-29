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

import java.io.*;
import java.nio.file.*;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.*;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;

/**
 * Property editor for {@link FileTypeItem}. 
 * <p>
 * The path is relative to the project
 * directory.
 * 
 * @author Ian Davies - 14 Feb. 2019
 */
public class FileTypeEditor extends AbstractPropertyEditor<String, LabelButtonControl> {

	private LabelButtonControl view;

	/**
	 * @param property The {@link FileTypeItem}.
	 * @param control The {@link LabelButtonControl}
	 */
	public FileTypeEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	/**
	 * @param property  The {@link FileTypeItem}.
	 */
	public FileTypeEditor(Item property) {
		this(property, new LabelButtonControl("Open16.gif", Images.class.getPackageName()));
		view = this.getEditor();
		view.setOnAction(e -> onAction());
	}

	@Override
	public void setValue(String value) {
		getEditor().setText(value);
	}

	@Override
	protected ObservableValue<String> getObservableValue() {
		return getEditor().getTextProperty();
	}

	// This all needs refactoring
	private void onAction() {
		File root = Project.makeFile("");
		FileTypeItem fileTypeItem = (FileTypeItem) getProperty();
		File file = DialogService.getImplementation().getOpenFile(root, "Select file", fileTypeItem.getExtensions());
		String relativePath = null;
		if (file != null) {
			if (file.getAbsolutePath().contains(root.getAbsolutePath()))
				relativePath = makeProjectRelative(file.getAbsolutePath());
			else if (DialogService.getImplementation().confirmation("Import file", "", "Import " + file.getName() + " to the current project?")) {
				File newFile = importFile(file);
				relativePath = makeProjectRelative(newFile.getAbsolutePath());
			}
		} else {
			if (DialogService.getImplementation().confirmation("Query", "Clear property value?", "")) {
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

	private static String makeProjectRelative(String absolutePath) {
		String root = Project.makeFile("").getAbsolutePath();
		if (absolutePath.contains(root))
			absolutePath = absolutePath.replace(root, "");
		if (absolutePath.startsWith(File.separator))
			absolutePath = absolutePath.replaceFirst("\\\\", "");
		absolutePath = absolutePath.replaceAll("\\\\", "/");
		return absolutePath;
	}

}
