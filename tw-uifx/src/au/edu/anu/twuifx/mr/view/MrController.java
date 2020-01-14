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

package au.edu.anu.twuifx.mr.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mr.IMRController;
import au.edu.anu.twapps.mr.IMRModel;
import au.edu.anu.twapps.mr.MRModel;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twuifx.images.Images;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * @author Ian Davies
 * @date 18 Jan. 2018
 */
public class MrController implements IMRController {
	@FXML
	private CheckMenuItem miParDashboard;

	@FXML
	private Menu menuWidgets;
	@FXML
	private MenuItem miISGenerate;

	@FXML
	private MenuItem miISSaveAs;

	@FXML
	private MenuItem miISSelect;

	@FXML
	private MenuItem miISReload;

	@FXML
	private MenuItem miISClear;

	@FXML
	private TabPane tabPane;

	private IMRModel model;

	@FXML
	private HBox toolBar;

	@FXML
	private HBox statusBar;

	private IRunTimeParameterizer dashboard;

	public TabPane getTabPane() {
		return tabPane;
	}

	public HBox getToolBar() {
		return toolBar;
	}

	public HBox getStatusBar() {
		return statusBar;
	}

	public Menu getWidgetMenu() {
		return menuWidgets;
	}

	@FXML
	void onAboutModelRunner(ActionEvent event) {
		Dialog<ButtonType> dlg = new Dialog<>();
		dlg.initOwner((Window) Dialogs.owner());
		dlg.setTitle("About ModelMaker");
		ButtonType done = new ButtonType("Close", ButtonData.OK_DONE);
		HBox content = new HBox();
		VBox leftContent = new VBox();
		VBox rightContent = new VBox();
		ImageView imageView = new ImageView(new Image(Images.class.getResourceAsStream("3worlds-5.jpg")));
		imageView.preserveRatioProperty().set(true);
		rightContent.getChildren().addAll(imageView, new Label("Three Worlds - M. C. Escher (1955)"));
		TextFlow textFlow = new TextFlow();
		textFlow.setPrefWidth(400);
		textFlow.setTextAlignment(TextAlignment.CENTER);
		textFlow.setLineSpacing(10);
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(4);
		dropShadow.setOffsetY(6);
		dropShadow.setHeight(5);
		Text text_1 = new Text("ModelRunner\n");
		text_1.setEffect(dropShadow);
		text_1.setFont(Font.font("Helvetica", 30));

		Text text_2 = new Text("\"Though the organisms may claim our primary interest, " + //
				"when we are trying to think fundamentally, we cannot separate them from their special environment, " + //
				"with which they form one physical system\"");

		text_2.setFont(Font.font("Helvetica", FontPosture.ITALIC, 12));

		Text text_3 = new Text(" A. G. Tanlsey (1935).\n");
		text_3.setFont(Font.font("Helvetica", 12));

		textFlow.getChildren().addAll(text_1, text_2, text_3);

		content.getChildren().addAll(rightContent, leftContent);
		TextArea textArea = new TextArea();
		Scanner sc = new Scanner(MrController.class.getResourceAsStream("aboutMR.txt"));

		while (sc.hasNext()) {
			textArea.appendText(sc.nextLine());
			textArea.appendText("\n");
		}
		sc.close();

		textArea.setWrapText(true);
		textArea.setPrefHeight(400);
		textArea.setEditable(false);
		TextFlow authors = new TextFlow();
		authors.setTextAlignment(TextAlignment.CENTER);
		authors.getChildren().add(new Text("Authors: J. Gignoux, I. Davies and S. Flint"));
		leftContent.getChildren().addAll(textFlow, textArea, new Label(), authors);

		dlg.getDialogPane().setContent(content);
		dlg.getDialogPane().getButtonTypes().addAll(done);
		// dlg.setResizable(true);
		textArea.selectPositionCaret(0);
		textArea.deselect();
		dlg.showAndWait();

	}

	@FXML
	void onCurrentConfiguration(ActionEvent event) {
		RunTimeData.dumpGraphState(model.getGraph());
	}

	@FXML
	void onISClear(ActionEvent event) {
		RunTimeData.clearModelState(model.getGraph());
	}

	@FXML
	void onISGenerate(ActionEvent event) {
		model.doISGenerate();
	}

	@FXML
	void onISReload(ActionEvent event) {
		model.doISReload();
	}

	@FXML
	void onISSaveAs(ActionEvent event) {
		List<ExtensionFilter> extensions = new ArrayList<>();
		extensions.add(new ExtensionFilter("Initial state (*.isf)", ".isf"));
		File file = Dialogs.saveISFile(Project.makeFile(ProjectPaths.RUNTIME), "Save state as");
		if (file != null) {
			System.out.println(file);
			model.doISSaveAs(file);
		}
	}

	@FXML
	void onISSelect(ActionEvent event) {
		int idx = Dialogs.editISFiles(model.getISFiles(), model.getISSelection());
		model.setISSelection(idx);
	}

	@FXML
	void onParDashboard(ActionEvent event) {
		if (dashboard == null)
			dashboard = new ParameterWindow(model.getGraph(), stage, miParDashboard.selectedProperty());
		dashboard.show(miParDashboard.isSelected());
	}

	@FXML
	public void initialize() {
		model = new MRModel(this);
		statusBar.setSpacing(5);
		statusBar.setPadding(new Insets(1, 1, 1, 1));
		statusBar.setStyle("-fx-background-color: lightgray");
		toolBar.setSpacing(5);
		toolBar.setStyle("-fx-background-color: lightgray");
	}

	private Stage stage;
	private static final String mainFrameName = "mainFrame";
	private static final String mainMaximized = mainFrameName + "_" + "maximized";
	private static final String tabIndex = "tabIndex";

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public IMRModel getModel() {
		return model;
	}

	public void putPreferences() {
		if (Project.isOpen()) {
			Preferences.putDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
			Preferences.putBoolean(mainMaximized, stage.isMaximized());
			int idx = tabPane.getSelectionModel().getSelectedIndex();
			Preferences.putInt(tabIndex, idx);
			model.putPreferences();
			Preferences.flush();
		}
	}

	public void getPreferences() {
		double[] r = Preferences.getDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(),
				stage.getHeight());
		stage.setX(r[0]);
		stage.setY(r[1]);
		stage.setWidth(r[2]);
		stage.setHeight(r[3]);
		stage.setMaximized(Preferences.getBoolean(mainMaximized, stage.isMaximized()));
		int idx = Preferences.getInt(tabIndex, 0);
		tabPane.getSelectionModel().select(idx);
		model.getPreferences();
	}

}
