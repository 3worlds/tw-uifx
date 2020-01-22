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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * @author Ian Davies
 *
 * @date 2 Jan 2020
 */
public class ISSelectionDlg {
	private List<File> fileList;
	private int newIndex;
	private int oldIndex;
	private Dialog<ButtonType> dlg;
	private ButtonType ok;
	private Button btnUp;
	private Button btnDn;
	private Button btnAdd;
	private Button btnDel;
	private Button btnClear;
	private Button btnSet;
	private Label lblISFileName;
	private ListView<File> listView;

	public ISSelectionDlg(List<File> fileList, int index) {
		this.fileList = fileList;
		this.newIndex = index;
		this.oldIndex = index;
		dlg = new Dialog<ButtonType>();
		dlg.setTitle("Manage initial state files");
		dlg.initOwner((Window) Dialogs.owner());
		ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane content = new BorderPane();
		dlg.getDialogPane().setContent(content);
		listView = new ListView<>();
		listView.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {

			@Override
			public ListCell<File> call(ListView<File> list) {
				return new FileFormatCell();
			}
		});
		listView.setPrefWidth(400);
		content.setCenter(listView);
		listView.getItems().addAll(fileList);

		VBox rightContent = new VBox(10);
		rightContent.setPrefWidth(40);
		content.setRight(rightContent);
		BorderPane.setMargin(rightContent, new Insets(50, 5, 5, 5));
		btnUp = new Button("↑");
		btnDn = new Button("↓");
		btnAdd = new Button("+");
		btnDel = new Button("x");
		btnClear = new Button("Clear");
		btnSet = new Button("Set");
		btnUp.setDisable(true);
		btnDn.setDisable(true);
		btnDel.setDisable(true);
		btnSet.setDisable(true);
		btnClear.setDisable(true);
		btnUp.setMinWidth(rightContent.getPrefWidth());
		btnDn.setMinWidth(rightContent.getPrefWidth());
		btnAdd.setMinWidth(rightContent.getPrefWidth());
		btnDel.setMinWidth(rightContent.getPrefWidth());
		rightContent.getChildren().addAll(btnUp, btnDn, btnAdd, btnDel);
		VBox btmContent = new VBox(10);
		HBox cs = new HBox(5);
		btmContent.getChildren().add(cs);
		Label lbl = new Label("Current selection: ");
		lblISFileName = new Label();
		if (index != -1)
			setNameText(fileList.get(index));
		cs.getChildren().addAll(lbl, lblISFileName);
		btnClear.setPrefWidth(100);
		btnSet.setPrefWidth(100);
		btmContent.getChildren().addAll(btnClear, btnSet);
		content.setBottom(btmContent);
		dlg.setResizable(true);

		listView.getSelectionModel().selectedIndexProperty().addListener(c -> {
			setButtons();
		});
		btnDel.setOnAction(e -> onDelete());
		btnSet.setOnAction(e -> onSet());
		btnClear.setOnAction(e -> onClear());
		btnUp.setOnAction(e -> onUp());
		btnDn.setOnAction(e -> onDn());
		btnAdd.setOnAction(e -> onAdd());

	}

	private void onAdd() {
		String[] exts = new String[2];
		exts[0] = "Initial state (*.isf)";
		exts[1] = ".isf";
		File file = Dialogs.promptForOpenFile(Project.makeFile(ProjectPaths.RUNTIME), "Add initial state file", exts);
		// TODO open and validate the file before listing
		if (file != null)
			if (!listView.getItems().contains(file)) {
				listView.getItems().add(file);
				setButtons();
			}
	}

	private void onDn() {
		int idx = listView.getSelectionModel().getSelectedIndex();
		Collections.rotate(listView.getItems().subList(idx, idx + 2), -1);
		listView.getSelectionModel().select(idx + 1);
		if (newIndex == idx)// ??
			newIndex++;
		setButtons();
	}

	private void onUp() {
		int idx = listView.getSelectionModel().getSelectedIndex();
		Collections.rotate(listView.getItems().subList(idx - 1, idx + 1), -1);
		listView.getSelectionModel().select(idx - 1);
		if (newIndex == idx)// ??
			newIndex--;
		setButtons();
	}

	private void onClear() {
		newIndex = -1;
		setButtons();
	}

	private void onDelete() {
		int idx = listView.getSelectionModel().getSelectedIndex();
		if (idx == newIndex)
			newIndex = -1;
		else if (newIndex > idx)
			newIndex--;
		listView.getItems().remove(idx);
		setButtons();
	}

	private void setNameText(File file) {
		lblISFileName.setText(newIndex + ": " + file.getName());
	}

	private void onSet() {
		int idx = listView.getSelectionModel().getSelectedIndex();
		newIndex = idx;
		File file = listView.getSelectionModel().getSelectedItem();
		setNameText(file);
		setButtons();
	}

	private void setButtons() {
		int idx = listView.getSelectionModel().getSelectedIndex();

		if (idx <= 0)
			btnUp.setDisable(true);
		else
			btnUp.setDisable(false);
		if (idx == (listView.getItems().size() - 1))
			btnDn.setDisable(true);
		else
			btnDn.setDisable(false);
		if (idx < 0) {
			btnDel.setDisable(true);
			btnSet.setDisable(true);
		} else {
			btnDel.setDisable(false);
			btnSet.setDisable(false);
		}
		if (newIndex == -1) {
			btnClear.setDisable(true);
			lblISFileName.setText("");
		} else {
			btnClear.setDisable(false);
			setNameText(listView.getItems().get(newIndex));
		}
		if (idx >= 0)
			if (!listView.getItems().get(idx).exists())
				btnSet.setDisable(true);
	}

	public int getResult() {
		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			fileList.clear();
			fileList.addAll(listView.getItems());
			return newIndex;
		}
		return oldIndex;

	}

}
