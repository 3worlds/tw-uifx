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

import java.io.IOException;
import java.net.URL;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.Rollover;
import fr.cnrs.iees.twcore.constants.EnumProperties;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.project.TwPaths;
import au.edu.anu.twuifx.dialogs.Dialogsfx;
import au.edu.anu.twuifx.graphState.GraphStatefx;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.view.DefaultWindowSettings;
import au.edu.anu.twuifx.mm.view.MmController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
//		URL URLView = ModelMakerfx.class.getResource("view/Mmfr.fxml");
		URL URLView = ModelMakerfx.class.getResource("view/Mm.fxml");
		loader.setLocation(URLView);
		root = (Parent) loader.load();
		controller = loader.getController();
		Scene scene = new Scene(root);
		mainStage.setScene(scene);
		controller.setStage(mainStage);
		mainStage.getIcons().add(new Image(Images.class.getResourceAsStream("MmIcon16.png")));
		scene.getWindow().setOnCloseRequest((e) -> {
			if (!controller.canClose()) {
				e.consume();
			} else {
				Rollover.finalise();
				stop();
			}
		});
	}

//	private boolean checkArchetype = false;


	@Override
	public void start(Stage primaryStage) throws Exception {
		/**
		 * setUserAgentStylesheet(STYLESHEET_CASPIAN);
		 * 
		 * Not used because this stylesheet elicits a warning from search bar of
		 * propertySheet.
		 */
		EnumProperties.recordEnums();
//		ValidPropertyTypes.listTypes(); // uncomment this if you want to make sure all property types are here
		mainStage = primaryStage;
		mainStage.setTitle(DefaultWindowSettings.defaultName());
		createMainWindow();
		Dialogs.initialise(new Dialogsfx(root.getScene().getWindow()));
		GraphState.initialise(new GraphStatefx(mainStage.titleProperty(), controller.getUserProjectPathProperty()));
		GraphState.addListener(controller);
		//setDefaultFrameSize();
		mainStage.show();
		//Logging.setLogLevel(Level.INFO, MMModel.class);

	}

//	private void setDefaultFrameSize() {
//		mainStage.setWidth(DefaultWindowSettings.getWidth());
//		mainStage.setHeight(DefaultWindowSettings.getHeight());
//		mainStage.setX(DefaultWindowSettings.getX());
//		mainStage.setY(DefaultWindowSettings.getY());
//	}

	@Override
	public void stop() {
		controller.putPreferences();
		Platform.exit();
		System.exit(0);
	}

	public static void main(String[] args) {
		System.out.println("Before 'launch(args)'");
		launch(args);
		// System.out.println("OK");
	}

}
