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

import java.awt.Rectangle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

	private static Preferences preferences;

	public static void setPreferences(Preferences pref) {
		preferences = pref;
	}

	private static final String mainFrameName = "mainFrame";
	private static final String mainMaximized = mainFrameName + "_" + "maximized";

	private Rectangle getStageRectangle(Stage stage) {
		return new Rectangle((int) stage.getX(), (int) stage.getY(), (int) stage.getWidth(), (int) stage.getHeight());
	}

	public void savePrefs(Preferences pref, Stage stage) {
		pref.putRectangle(mainFrameName, getStageRectangle(stage));
		pref.putBoolean(mainMaximized, stage.isMaximized());
		pref.putSplitPaneDividers(splitPane1);
		pref.putSplitPaneDividers(splitPane2);
		pref.putSplitPaneDividers(splitPane3);
	}

	public void loadPrefs(Preferences pref, Stage stage) {
		Rectangle r = pref.getRectangle(mainFrameName, getStageRectangle(stage));
		stage.setHeight(r.getHeight());
		stage.setWidth(r.getWidth());
		stage.setX(r.getX());
		stage.setY(r.getY());
		stage.setMaximized(pref.getBoolean(mainMaximized, stage.isMaximized()));

		pref.getSplitPaneDividers(splitPane1);
		pref.getSplitPaneDividers(splitPane2);
		pref.getSplitPaneDividers(splitPane3);
	}

}
