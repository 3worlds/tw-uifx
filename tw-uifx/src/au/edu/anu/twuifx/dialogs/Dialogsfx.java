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

package au.edu.anu.twuifx.dialogs;

import java.io.File;
import java.util.List;
import java.util.Optional;

import au.edu.anu.twapps.dialogs.IDialogs;
import au.edu.anu.twapps.dialogs.YesNoCancel;
import au.edu.anu.twcore.project.TWPaths;
import fr.cnrs.iees.io.GraphFileFormats;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * Author Ian Davies
 *
 * Date 12 Dec. 2018
 */
public class Dialogsfx implements IDialogs {
	private static Window owner;

	/**
	 * @param owner set the owner of these dialogs
	 */
	public Dialogsfx(Window owner) {
		Dialogsfx.owner = owner;
	}

	@Override
	public void errorAlert(String title, String header, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	@Override
	public File selectDirectory(String title, String currentPath) {
		DirectoryChooser dc = new DirectoryChooser();
		if (!currentPath.equals(""))
			dc.setInitialDirectory(new File(currentPath));
		else
			dc.setInitialDirectory(new File(TWPaths.USER_ROOT));
		dc.setTitle(title);
		return dc.showDialog(owner);
	}

	@Override
	public YesNoCancel yesNoCancel(String title, String header, String content) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		ButtonType btnYes = new ButtonType("Yes");
		ButtonType btnNo = new ButtonType("No");
		ButtonType btnCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(btnYes, btnNo, btnCancel);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == btnYes)
			return YesNoCancel.yes;
		if (result.get() == btnNo)
			return YesNoCancel.no;
		return YesNoCancel.cancel;
	}

	@Override
	public String getText(String title, String header, String content, String prompt) {
		TextInputDialog dialog = new TextInputDialog(prompt);
		dialog.initOwner(owner);
		dialog.setTitle(title);
		dialog.setHeaderText(header);
		dialog.setContentText(content);
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent())
			return result.get();
		return null;
	}

	@Override
	public File getExternalProjectFile() {
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(TWPaths.USER_ROOT));
		String[] extList = GraphFileFormats.TWG.extensions();
		for (String ext : extList)
			fc.getExtensionFilters().add(
					new FileChooser.ExtensionFilter(GraphFileFormats.TWG.toString() + " (*" + ext + ")", "*" + ext));
		fc.setSelectedExtensionFilter(fc.getExtensionFilters().get(0));
		File file = fc.showOpenDialog(owner);
		return file;
	}

	@Override
	public boolean confirmation(String title, String header, String content) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		Optional<ButtonType> result = alert.showAndWait();
		return (result.get() == ButtonType.OK);
	}

	@Override
	public File getOpenFile(File directory, String title, List<ExtensionFilter> extensions) {
		FileChooser fc = new FileChooser();
		fc.setTitle(title);
		fc.setInitialDirectory(directory);
		fc.getExtensionFilters().addAll(extensions);
		if (!fc.getExtensionFilters().isEmpty())
			fc.setSelectedExtensionFilter(fc.getExtensionFilters().get(0));
		return fc.showOpenDialog(owner);
	}

}
