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

package au.edu.anu.twuifx.mr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.javafx.application.LauncherImpl;

import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.mr.view.MrController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Author Ian Davies
 *
 * Date 18 Dec. 2018
 */
public class ModelRunnerfx extends Application {
	private static Object uiNode;
	private static Object config;
	private TwUIManager uiManager;
	private MrController controller;
	private Stage stage;

	public static void launchUI(Object config1, String[] args) {
		config = config1;
		//uiNode = config.findNode(N_UI.toString() + ":");
		LauncherImpl.launchApplication(ModelRunnerfx.class, MRSplash.class, args);

	}

	public static String getInitNodeName() {
		return initNodeName;
	}

	private static String initNodeName;

	@Override
	public void init() throws Exception {
		// we need the ordered list of nodes to initialise
		//AotList<AotNode> list = config.getInitialiser().getInitialisationList();
		int i = 1;
//		double size = list.size();
//		for (AotNode n : list) {
//			try {
//				initNodeName = n.displayName();
//				double progress = i / size;
//				LauncherImpl.notifyPreloader(this, new Preloader.ProgressNotification(progress));
//				n.initialise();
//				i++;
//			} catch (Exception e) {
//				throw new TwuifxException("Initialisation failed for node: " + getInitNodeName() + ". ", e);
//			}
//		}
	}

	private static final long maxSplashDelay = 2000;
	@Override
	public void start(Stage stage) throws Exception {
		// we could build the scene in an init() method.??
		this.stage = stage;
		stage.setWidth(800);
		stage.setHeight(600);
		String title = Project.getDisplayName();
		stage.titleProperty().set(title);
		// Create a runTime dir within this project and create a preferences file if not
		// already present
		File prefFile = Project.makeFile(ProjectPaths.RUNTIME, "preferences.dsl");
		prefFile.getParentFile().mkdirs();
//		pref = new Preferences("RunTimePreferences", prefFile, log);
//		MrController.setPreferences(pref);
		setUserAgentStylesheet(STYLESHEET_CASPIAN);
		// GraphState.setTitleProperty(stage.titleProperty(), null);
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ModelRunnerfx.class.getResource("view/Mr.fxml"));
		Parent root = (Parent) loader.load();
		Scene scene = new Scene(root);
		stage.setScene(scene);

//		Dialogs.setParent(root.getScene().getWindow());
		controller = loader.getController();
		uiManager = new TwUIManager(uiNode, controller.getToolBar(), controller.getTopLeft(), controller.getTopRight(),
				controller.getBottomLeft(), controller.getBottomRight(), controller.getStatusBar(),
				controller.getWidgetMenu(), stage.getScene().getWindow());

		stage.show();
		stage.toBack();

		Platform.runLater(() -> {
			uiManager.loadPreferences();
//			controller.loadPrefs(pref, stage);
			// Hide the splash window
			// 
			long endTime = System.currentTimeMillis();
			long timeElapsed = endTime - MRSplash.startTime;
			long delay = maxSplashDelay - timeElapsed;
			if (delay > 0) {
				Timer timer = new Timer();
				TimerTask task = new TimerTask() {
					public void run() {
						Platform.runLater(() -> {
								MRSplash.hideStage();
								stage.toFront();
						});
					}
				};
				timer.schedule(task, delay);
			} else {
				MRSplash.hideStage();
				stage.toFront();
			}			
		});
	}

	@Override
	public void stop() {
//		controller.savePrefs(pref, stage);
		uiManager.savePreferences();
//		pref.flush();
	}

}
