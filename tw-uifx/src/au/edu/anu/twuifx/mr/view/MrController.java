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
import java.util.Optional;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mr.IMRController;
import au.edu.anu.twapps.mr.IMRModel;
import au.edu.anu.twapps.mr.MRModel;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
	private CheckMenuItem miCurrentSetup;

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

	public TabPane getTabPane() {
		return tabPane;
	}

	@FXML
	private HBox toolBar;

	@FXML
	private HBox statusBar;

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
	void onCurrentSetup(ActionEvent event) {

	}

	@FXML
	void onISClear(ActionEvent event) {
		model.doISClear();
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
		if (file!=null) {
			model.doISSaveAs(file);
			System.out.println(file);
		}
	}

	@FXML
	void onISSelect(ActionEvent event) {	 
		int idx =Dialogs.editISFiles(model.getISFiles(), model.getISSelection());
		model.setISSelection(idx);
	}

	@FXML
	void onParDashboard(ActionEvent event) {
		if (dashboard==null)
			dashboard = new Dashboard(model.getGraph(),stage,miParDashboard.selectedProperty());
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

	private Dashboard dashboard;

}
