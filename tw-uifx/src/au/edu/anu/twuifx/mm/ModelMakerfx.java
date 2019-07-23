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

package au.edu.anu.twuifx.mm;

import java.io.File;

import java.io.IOException;

import javax.tools.ToolProvider;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.project.TwPaths;
import au.edu.anu.twuifx.dialogs.Dialogsfx;
import au.edu.anu.twuifx.graphState.GraphStatefx;
import au.edu.anu.twuifx.mm.view.MmController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Author Ian Davies
 *
 * Date 10 Dec. 2018
 */
public class ModelMakerfx extends Application implements ProjectPaths, TwPaths {
	private Stage mainStage;
	private Parent root;
	private MmController controller;

	private void createMainWindow() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ModelMakerfx.class.getResource("view/Mm.fxml"));
		root = (Parent) loader.load();
		controller = loader.getController();
		Scene scene = new Scene(root);
		mainStage.setScene(scene);
		controller.setStage(mainStage);
		scene.getWindow().setOnCloseRequest((e) -> {
			if (!controller.canClose()) {
				e.consume();
			} else {
				Platform.exit();
				System.exit(0);
			}
		});
	}

	private void checkResources() {
		File file = new File(TW_ROOT + File.separator + TW_DEP_JAR);
		if (!file.exists())
			Dialogs.warnAlert("Resource Error", "Required Java dependency jar not found",
					"Use TwSetup to create " + file.getAbsolutePath());

		boolean haveCompiler = !(ToolProvider.getSystemJavaCompiler() == null);
		if (!haveCompiler)
			Dialogs.warnAlert("Resource Error", "Java compiler not found",
					"Check you have the Java Development Kit installed");
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		/**
		 * setUserAgentStylesheet(STYLESHEET_CASPIAN);
		 * 
		 * Not used because this stylesheet elicits a warning from search bar of
		 * propertySheet.
		 */
		mainStage = primaryStage;
		mainStage.setTitle("3Worlds Model Maker");
		createMainWindow();
		Dialogs.initialise(new Dialogsfx(root.getScene().getWindow()));
		GraphState.initialise(new GraphStatefx(mainStage.titleProperty(),controller.getUserProjectPathProperty()));
		//checkResources();
		setDefaultFrameSize();
		mainStage.show();
	}

	private void setDefaultFrameSize() {
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		double w = screenBounds.getWidth() / 1.5;
		// golden mean?
		double h = w / 1.618;
		double x = (screenBounds.getWidth() - w) / 2;
		double y = (screenBounds.getHeight() - h) / 3;
		mainStage.setWidth(w);
		mainStage.setHeight(h);
		mainStage.setX(x);
		mainStage.setY(y);
	}

	@Override
	public void stop() {
		controller.putPreferences();
		// Without this, threads remain when debugging.
		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
		//System.out.println("OK");
	}

}
