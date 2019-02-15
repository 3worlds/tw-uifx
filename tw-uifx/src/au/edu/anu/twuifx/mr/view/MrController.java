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

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.utils.UiHelpers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Ian Davies
 * @date 18 Jan. 2018
 */
public class MrController {
	@FXML
	private CheckMenuItem miDashboard;

	@FXML
	private Menu menuWidgets;

	@FXML
	private TabPane tptl;

	@FXML
	private TabPane tptr;

	@FXML
	private TabPane tpbl;

	@FXML
	private TabPane tpbr;

	@FXML
	private HBox toolBar;

	@FXML
	private HBox statusBar;

	@FXML
	private SplitPane splitPane1;

	@FXML
	private SplitPane splitPane2;

	@FXML
	private SplitPane splitPane3;

	public TabPane getTopLeft() {
		return tptl;
	}

	public HBox getToolBar() {
		return toolBar;
	}

	public HBox getStatusBar() {
		return statusBar;
	}

	public TabPane getTopRight() {
		return tptr;
	}

	public TabPane getBottomLeft() {
		return tpbl;
	}

	public TabPane getBottomRight() {
		return tpbr;
	}

	public Menu getWidgetMenu() {
		return menuWidgets;
	}

	@FXML
	void handleDashboard(ActionEvent event) {

	}

	@FXML
	public void initialize() {
		statusBar.setSpacing(5);
		statusBar.setPadding(new Insets(1, 1, 1, 1));
		statusBar.setStyle("-fx-background-color: lightgray");
		toolBar.setSpacing(5);
		toolBar.setStyle("-fx-background-color: lightgray");
	}

	private Stage stage;
	private static final String mainFrameName = "mainFrame";
	private static final String mainMaximized = mainFrameName + "_" + "maximized";


	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void putPreferences() {
		if (Project.isOpen()) {
			Preferences.initialise(Project.makeRuntimePreferencesFile());
			Preferences.putDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
			Preferences.putBoolean(mainMaximized, stage.isMaximized());
			Preferences.putDouble(splitPane1.idProperty().get(), splitPane1.getDividerPositions()[0]);
			Preferences.putDouble(splitPane2.idProperty().get(), splitPane2.getDividerPositions()[0]);
			Preferences.putDouble(splitPane3.idProperty().get(), splitPane3.getDividerPositions()[0]);
			Preferences.flush();
		}
	}

	public void getPreferences() {
		Preferences.initialise(Project.makeProjectPreferencesFile());
		double[] r = Preferences.getDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
		stage.setX(r[0]);
		stage.setY(r[1]);
		stage.setWidth(r[2]);
		stage.setHeight(r[3]);
		stage.setMaximized(Preferences.getBoolean(mainMaximized, stage.isMaximized()));
		splitPane1.setDividerPositions(UiHelpers.getSplitPanePositions(splitPane1));
		splitPane2.setDividerPositions(UiHelpers.getSplitPanePositions(splitPane2));
		splitPane3.setDividerPositions(UiHelpers.getSplitPanePositions(splitPane3));
	}

}
