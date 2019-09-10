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

import static au.edu.anu.rscs.aot.queries.CoreQueries.hasTheLabel;
import static au.edu.anu.rscs.aot.queries.CoreQueries.selectZeroOrOne;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.get;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.N_UI;

import java.util.Timer;
import java.util.TimerTask;

import com.sun.javafx.application.LauncherImpl;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.dialogs.Dialogsfx;
import au.edu.anu.twuifx.mr.view.MrController;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.twcore.constants.EnumProperties;
import javafx.application.Application;
import javafx.application.Platform;
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
	private static TreeGraphNode uiNode;
	private static TreeGraph<TreeGraphNode, ALEdge> config;
	private UIDeployer uiDeployer;
	private MrController controller;
	private Stage stage;

	public static void launchUI(TreeGraph<TreeGraphNode, ALEdge> config1) {
		config = config1;
		uiNode = (TreeGraphNode) get(config.root().getChildren(), selectZeroOrOne(hasTheLabel(N_UI.label())));
		String[] args = new String[0];
		LauncherImpl.launchApplication(ModelRunnerfx.class, MrSplash.class, args);

	}

	public static String getInitNodeName() {
		return initNodeName;
	}

	private static String initNodeName;

	@Override
	public void init() throws Exception {
		// we need the ordered list of nodes to initialise
		// AotList<TreeGraphDataNode> list =
		// config.getInitialiser().getInitialisationList();
		int i = 1;
//		double size = list.size();
//		for (TreeGraphDataNode n : list) {
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
	public void start(Stage primaryStage) throws Exception {
		// we could build the scene in an init() method.??
		EnumProperties.recordEnums();
		this.stage = primaryStage;
		stage.setWidth(800);
		stage.setHeight(600);
		String title = Project.getDisplayName();
		stage.titleProperty().set(title);
		setUserAgentStylesheet(STYLESHEET_CASPIAN);
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ModelRunnerfx.class.getResource("view/Mr.fxml"));
		Parent root = (Parent) loader.load();
		Scene scene = new Scene(root);
		stage.setScene(scene);

		Dialogs.initialise(new Dialogsfx(root.getScene().getWindow()));
		GraphState.initialise(null);

		controller = loader.getController();
		controller.setStage(stage);
		scene.getWindow().setOnCloseRequest((e) -> {
			stop();
		});
		Preferences.initialise(Project.makeRuntimePreferencesFile());
		uiDeployer = new UIDeployer(uiNode, controller);
		stage.show();
		stage.toBack();
		Platform.runLater(() -> {

			controller.getPreferences();
			uiDeployer.getPreferences();
			// hide splash after set delay
			long endTime = System.currentTimeMillis();
			long timeElapsed = endTime - MrSplash.startTime;
			long delay = maxSplashDelay - timeElapsed;
			if (delay > 0) {
				Timer timer = new Timer();
				TimerTask task = new TimerTask() {
					public void run() {
						Platform.runLater(() -> {
							MrSplash.hideStage();
							stage.toFront();
						});
					}
				};
				timer.schedule(task, delay);
			} else {
				MrSplash.hideStage();
				stage.toFront();
			}
		});
	}

	@Override
	public void stop() {
		controller.putPreferences();
		uiDeployer.putPreferences();
		Preferences.flush();
		Platform.exit();
		System.exit(0);
	}

}
