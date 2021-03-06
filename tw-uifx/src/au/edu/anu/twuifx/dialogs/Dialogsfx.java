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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.dialogs.IDialogs;
import au.edu.anu.twapps.dialogs.YesNoCancel;
import au.edu.anu.twcore.project.TwPaths;
import fr.cnrs.iees.io.GraphFileFormats;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
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
	private Window owner;

	/**
	 * @param owner the application window - dialog parent
	 */
	public Dialogsfx(Window owner) {
		this.owner = owner;
	}

	private void alert(AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	@Override
	public void errorAlert(String title, String header, String content) {
		alert(AlertType.ERROR, title, header, content);
	}

	@Override
	public void infoAlert(String title, String header, String content) {
		alert(AlertType.INFORMATION, title, header, content);
	}

	@Override
	public void warnAlert(String title, String header, String content) {
		alert(AlertType.WARNING, title, header, content);
	}

	@Override
	public File selectDirectory(String title, String currentPath) {
		DirectoryChooser dc = new DirectoryChooser();
		if (!currentPath.equals(""))
			dc.setInitialDirectory(new File(currentPath));
		else
			dc.setInitialDirectory(new File(TwPaths.USER_ROOT));
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
	public String getText(String title, String header, String content, String prompt, String validFormat) {
		TextInputDialog dialog = new TextInputDialog(prompt);
		dialog.initOwner(owner);
		dialog.setTitle(title);
		dialog.setHeaderText(header);
		dialog.setContentText(content);
		if (validFormat != null) {
			TextField tf = dialog.getEditor();
			tf.setTextFormatter(
					new TextFormatter<>(change -> (change.getControlNewText().matches(validFormat) ? change : null)));
		}
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent())
			return result.get();
		return null;
	}

	@Override
	public File getExternalProjectFile() {
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(TwPaths.USER_ROOT));
		String[] extList = GraphFileFormats.TGOMUGI.extensions();
		for (String ext : extList)
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext + " (*" + ext + ")", "*" + ext));
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

	@SuppressWarnings("unchecked")
	@Override
	public File getOpenFile(File directory, String title, Object extensions) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(directory);
		fileChooser.getExtensionFilters().addAll((List<ExtensionFilter>) extensions);
		if (!fileChooser.getExtensionFilters().isEmpty())
			fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
		return fileChooser.showOpenDialog(owner);
	}

	@Override
	public boolean editList(String title, String header, String content, Object element) {

		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle(title);
		dialog.setHeaderText(header);
		dialog.setContentText(content);
		dialog.initOwner(owner);
		if (element instanceof Control) {
			BorderPane pane = new BorderPane();
			dialog.getDialogPane().setContent(pane);
			pane.setCenter((Control) element);
		} else {
			dialog.getDialogPane().setContent((Node) element);
		}

		dialog.setResizable(true);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		Optional<ButtonType> result = dialog.showAndWait();
		return result.get().equals(ok);
	}

	@Override
	public int getListChoice(String[] list, String title, String header, String content) {
		ChoiceDialog<String> dlg = new ChoiceDialog<>(list[0], list);
		dlg.initOwner(owner);
		dlg.setTitle(title);
		dlg.setHeaderText(header);
		dlg.setContentText(content);
		Optional<String> result = dlg.showAndWait();
		if (result.isPresent()) {
			String s = result.get();
			for (int i = 0; i < list.length; i++)
				if (list[i].equals(s))
					return i;
		}
		return -1;
	}

	@Override
	public List<String> getRadioButtonChoices(String title, String header, String content, List<String[]> entries) {
		Dialog<ButtonType> dlg = new Dialog<>();
		dlg.initOwner(owner);
		dlg.setTitle(title);
		dlg.setHeaderText(header);
		dlg.setContentText(content);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane pane = new BorderPane();
		ScrollPane sp = new ScrollPane();
		VBox vb = new VBox();
		pane.setCenter(sp);
		sp.setContent(vb);
		dlg.getDialogPane().setContent(pane);
		List<ToggleGroup> tgs = new ArrayList<>();
		for (String[] ss : entries) {
			ToggleGroup tg = new ToggleGroup();
			tgs.add(tg);
			boolean firstSelected = false;
			for (String s : ss) {
				RadioButton rb = new RadioButton(s);
				rb.setToggleGroup(tg);
				if (!firstSelected) {
					rb.setSelected(true);
					firstSelected = true;
				}
				vb.getChildren().add(rb);
			}
		}
		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			List<String> selection = new ArrayList<>();
			for (ToggleGroup tg : tgs) {
				RadioButton rb = (RadioButton) tg.getSelectedToggle();
				selection.add(rb.getText());
			}
			return selection;
		}
		;
		return null;
	}

	@Override
	public Object owner() {
		return owner;
	}

	@Override
	public File exportFile(String title, String promptDir, String promptFileName) {
		FileChooser fc = new FileChooser();
		fc.setTitle(title);
		ExtensionFilter filter = new FileChooser.ExtensionFilter("Tree graph files (*.utg)", "*.utg");
		fc.getExtensionFilters().add(filter);
		fc.setInitialDirectory(new File(promptDir));
		fc.setInitialFileName(promptFileName);
		fc.setSelectedExtensionFilter(filter);
		return fc.showSaveDialog(owner);
	}

	private File getFile(File directory, String title, boolean doOpen, String[]... exts) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(directory);
		for (String[] ext : exts)
			fileChooser.getExtensionFilters().add(new ExtensionFilter(ext[0], ext[1]));
		fileChooser.setInitialFileName("*" + exts[0][1]);// TODO this should be passed in.
		File result;
		if (doOpen)
			result = fileChooser.showOpenDialog(owner);
		else
			result = fileChooser.showSaveDialog(owner);
		if (result == null)
			return null;
		for (String[] ext : exts)
			if (result.getName().endsWith(ext[1]))
				return result;
		Dialogs.errorAlert("File name error", "", result.getName() + " does not have a valid file-extension.");
		return null;
	}

	@Override
	public File promptForOpenFile(File directory, String title, String[]... exts) {
		return getFile(directory, title, true, exts);
	}

	@Override
	public File promptForSaveFile(File directory, String title, String[]... exts) {
		return getFile(directory, title, false, exts);
	}

	@Override
	public int editISFiles(List<File> files, int idx) {
		ISSelectionDlg dlg = new ISSelectionDlg(files, idx);
		return dlg.getResult();
	}

	@Override
	public List<String> getCBSelections(String title,String header,List<String> items, List<Boolean> selected) {
		Dialog<ButtonType> dlg = new Dialog<>();
		dlg.setTitle(title);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		dlg.initOwner((Window) Dialogs.owner());
		GridPane content = new GridPane();
		content.setVgap(15);
		content.setHgap(10);
		dlg.getDialogPane().setContent(content);
		List<CheckBox> chkbxs = new ArrayList<>();
		content.add(new Label(header), 0, 0);
		for (int i = 0; i < items.size(); i++) {
			CheckBox cx = new CheckBox(items.get(i));
			chkbxs.add(cx);
			cx.setSelected(selected.get(i));
		}

		chkbxs.sort(new Comparator<CheckBox>() {

			@Override
			public int compare(CheckBox cb1, CheckBox cb2) {
				return cb1.getText().compareTo(cb2.getText());
			}
		});

		int row = 1;
		for (CheckBox cx : chkbxs) {
			content.add(cx, 0, row++);
		}

		Optional<ButtonType> btn = dlg.showAndWait();
		List<String> result = new ArrayList<>();
		if (btn.get().equals(ok)) {
			for (CheckBox cx : chkbxs)
				if (cx.isSelected())
					result.add(cx.getText());
			return result;
		} else {
			for (int i = 0; i < items.size(); i++)
				if (selected.get(i))
					result.add(items.get(i));

			return result;
		}
	}

}
