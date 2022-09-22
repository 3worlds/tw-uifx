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
import java.util.List;
import au.edu.anu.omhtk.Language;
import au.edu.anu.omhtk.preferences.IPreferences;
import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.util.Resources;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mr.IMRController;
import au.edu.anu.twapps.mr.IMRModel;
import au.edu.anu.twapps.mr.MRModel;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.dialogs.ExperimentDetailsDlg;
import au.edu.anu.twuifx.dialogs.ISParametersDlg;
import au.edu.anu.twuifx.images.Images;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.twcore.generators.odd.DocoGenerator;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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

import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

/**
 * @author Ian Davies - 18 Jan. 2018
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

	// private IRunTimeParameterizer dashboard;

	/**
	 * Getter for the controller's TabPane
	 * @return the tabPane
	 */
	public TabPane getTabPane() {
		return tabPane;
	}

	/**
	 * Getter for the controller's tool bar, implemented as an HBox
	 * @return the tool bar HBox container
	 */
	public HBox getToolBar() {
		return toolBar;
	}

	/**
	 * Getter for the controller's status bar, implemented as an HBox
	 * @return the status bar HBox container
	 */
	public HBox getStatusBar() {
		return statusBar;
	}

	/**
	 * Getter for the controller's widget menu
	 * @return the widget menu
	 */
	public Menu getWidgetMenu() {
		return menuWidgets;
	}

	@FXML
	public void initialize() {
		model = new MRModel();
		statusBar.setSpacing(5);
		statusBar.setPadding(new Insets(1, 1, 1, 1));
		// statusBar.setStyle("-fx-background-color: lightgray");
		toolBar.setSpacing(5);
		// toolBar.setStyle("-fx-background-color: lightgray");
	}

	@FXML
	void onAboutModelRunner(ActionEvent event) {
		Dialog<ButtonType> dlg = new Dialog<>();
		dlg.initOwner((Window) Dialogs.owner());
		dlg.setTitle("About ModelRunner");
		ButtonType done = new ButtonType("Close", ButtonData.OK_DONE);
		HBox content = new HBox();
		VBox rightContent = new VBox();
		VBox leftContent = new VBox();
		ImageView imageView = new ImageView(new Image(Images.class.getResourceAsStream("3worlds-5.jpg")));
		imageView.preserveRatioProperty().set(true);
		TextArea attribution = new TextArea();
		attribution.setWrapText(true);
		attribution.setPrefWidth(330);
		attribution.setEditable(false);
		ScrollPane attrScroller = new ScrollPane(attribution);

		SimplePropertyList rootProps = model.getGraph().root().properties();
		StringTable t;
		attribution.appendText(P_MODEL_PRECIS.key() + ":\n" + rootProps.getPropertyValue(P_MODEL_PRECIS.key()) + "\n");

		attribution.appendText("\n" + P_MODEL_AUTHORS.key() + ":\n");
		t = (StringTable) rootProps.getPropertyValue(P_MODEL_AUTHORS.key());
		for (int i = 0; i < t.size(); i++)
			attribution.appendText("\t" + t.getWithFlatIndex(i) + "\n");

		attribution.appendText("\n" + P_MODEL_CITATIONS.key() + ":\n");
		t = (StringTable) rootProps.getPropertyValue(P_MODEL_CITATIONS.key());
		for (int i = 0; i < t.size(); i++)
			attribution.appendText("\t" + t.getWithFlatIndex(i) + "\n");

		attribution.appendText("\n" + P_MODEL_CONTACTS.key() + ":\n");
		t = (StringTable) rootProps.getPropertyValue(P_MODEL_CONTACTS.key());
		for (int i = 0; i < t.size(); i++)
			attribution.appendText("\t" + t.getWithFlatIndex(i) + "\n");

		attribution.appendText(
				"\n" + P_MODEL_VERSION.key() + ": " + rootProps.getPropertyValue(P_MODEL_VERSION.key()) + "\n");

		attribution.appendText("\n" + P_MODEL_BUILTBY.key() + ": " + rootProps.getPropertyValue(P_MODEL_BUILTBY.key()));

		leftContent.getChildren().addAll(imageView, new Label("Three Worlds - M. C. Escher (1955)"), attrScroller);
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

		content.getChildren().addAll(leftContent, rightContent);
		TextArea textArea = new TextArea();
		List<String> lines;
		if (Language.French())
			lines = Resources.getTextResource("aboutMRFR.txt", getClass());
		else
			lines = Resources.getTextResource("aboutMREN.txt", getClass());

		for (String line:lines){
			textArea.appendText(line);
			textArea.appendText("\n");
		}

		textArea.setWrapText(true);
		textArea.setPrefHeight(400);
		textArea.setEditable(false);
		TextFlow authors = new TextFlow();
		authors.setTextAlignment(TextAlignment.CENTER);
		authors.getChildren().add(new Text("Authors: J. Gignoux, I. Davies and S. Flint"));
		rightContent.getChildren().addAll(textFlow, textArea, new Label(), authors);

		dlg.getDialogPane().setContent(content);
		dlg.getDialogPane().getButtonTypes().addAll(done);
		// dlg.setResizable(true);
		textArea.selectPositionCaret(0);
		textArea.deselect();
		attribution.selectPositionCaret(0);
		attribution.deselect();
		dlg.showAndWait();

	}

	@FXML
	void onODDGen(ActionEvent event) {
		Cursor oldCursor = tabPane.getScene().getCursor();
		tabPane.getScene().setCursor(Cursor.WAIT);
		DocoGenerator gen = new DocoGenerator(model.getGraph());
		Task<Void> oddTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				gen.generate();
				return null;
			}

		};
		oddTask.setOnSucceeded(e -> {
			tabPane.getScene().setCursor(oldCursor);
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Documentation generator");
			alert.setResizable(true);
			alert.setHeaderText("Generation completed");
			String content = "Directory:\n" + gen.getArtifactFiles()[0].getParent() + "\n\nFiles:";
			File[] list = gen.getArtifactFiles();
			for (int i = 0;i<list.length;i++)
				content+="\n - "+list[i].getName();
			alert.setContentText(content);
			alert.showAndWait();
		});
		oddTask.setOnFailed(e->{
			tabPane.getScene().setCursor(oldCursor);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Documentation generator");
			alert.setResizable(true);
			alert.setHeaderText("Generation failed");
			alert.setContentText(e.getEventType().toString());
			alert.showAndWait();
		});
		new Thread(oddTask).start();
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
		RunTimeData.resetModelState(model.getGraph());
	}

	@FXML
	void onISSaveAs(ActionEvent event) {
		String[] exts = new String[2];
		exts[0] = "Initial state (*.isf)";
		exts[1] = ".isf";
		File file = Dialogs.promptForSaveFile(Project.makeFile(Project.RUNTIME), "Save state as", exts);
		if (file != null) {
			System.out.println(file);
			model.doISSaveAs(file);
		}
	}

	@FXML
	void onISSelect(ActionEvent event) {
		int idx = Dialogs.selectFile(model.getISFiles(), model.getISSelection());
		model.setISSelection(idx);
	}

	@FXML
	void onParEdit(ActionEvent event) {
		new ISParametersDlg(model.getGraph());
	}

	@FXML
	void onParOpen(ActionEvent event) {
		String[] exts = new String[2];
		exts[0] = "Model parameters (*.mpf)";
		exts[1] = ".mpf";
		File file = Dialogs.promptForOpenFile(Project.makeFile(Project.RUNTIME), "Open parameters", exts);
		System.out.println(file);
	}

	@FXML
	void onParSave(ActionEvent event) {
		String[] exts = new String[2];
		exts[0] = "Model parameters (*.mpf)";
		exts[1] = ".mpf";
		File file = Dialogs.promptForSaveFile(Project.makeFile(Project.RUNTIME), "Save parameters", exts);
		System.out.println(file);
	}

	@FXML
	void onExperimentDetails(ActionEvent event) {

		new ExperimentDetailsDlg(model.getGraph());

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
			IPreferences prefs = Preferences.getImplementation();
			prefs.putDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
			prefs.putBoolean(mainMaximized, stage.isMaximized());
			int idx = tabPane.getSelectionModel().getSelectedIndex();
			prefs.putInt(tabIndex, idx);
			model.putPreferences();
			prefs.flush();
		}
	}

	public void getPreferences() {
		IPreferences prefs = Preferences.getImplementation();
		double[] r = prefs.getDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(),
				stage.getHeight());
		stage.setX(r[0]);
		stage.setY(r[1]);
		stage.setWidth(r[2]);
		stage.setHeight(r[3]);
		stage.setMaximized(prefs.getBoolean(mainMaximized, stage.isMaximized()));
		int idx = prefs.getInt(tabIndex, 0);
		tabPane.getSelectionModel().select(idx);
		model.getPreferences();
	}

}
