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
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_MODEL_BUILTBY;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_WIDGET_SUBCLASS;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.*;
import java.util.Timer;
import java.util.TimerTask;

import au.edu.anu.omhtk.Language;
import au.edu.anu.omhtk.preferences.PrefImpl;
import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.dialogs.DialogsFactory;
import au.edu.anu.twcore.graphState.*;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.ui.WidgetNode;
import au.edu.anu.twuifx.dialogs.Dialogsfx;
import au.edu.anu.twuifx.mm.view.DefaultWindowSettings;
import au.edu.anu.twuifx.mr.view.GUIBuilder;
import au.edu.anu.twuifx.mr.view.MRControllerfx;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.twcore.constants.EnumProperties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 3Worlds application to run, analyse and document simulations, whose
 * configuration graph conforms with the 3Worlds specification archetype.
 * 
 * @author Ian Davies- 18 Dec. 2018
 */
public class ModelRunnerfx extends Application {
	private static TreeGraphNode uiNode;
	private static TreeGraph<TreeGraphDataNode, ALEdge> config;
	private GUIBuilder uiDeployer;
	private MRControllerfx controller;
	private Stage stage;

	/**
	 * This method is called if the configuration has a GUI. A splash screen is
	 * shown while loading.
	 * 
	 * @param config1 The project configuration graph.
	 */
	public static void launchUI(TreeGraph<TreeGraphDataNode, ALEdge> config1) {
		config = config1;
		uiNode = (TreeGraphNode) get(config.root().getChildren(), selectZeroOrOne(hasTheLabel(N_UI.label())));
		String[] args = new String[0];
		MrSplash.builtBy = (String) config.root().properties().getPropertyValue(P_MODEL_BUILTBY.key());
		System.setProperty("javafx.preloader", MrSplash.class.getCanonicalName());
		launch(args);
	}

	/**
	 * This system is not currently used. It is intended to display the name of each
	 * node as it is initialised.
	 * 
	 * @return currently initializing node.
	 */
	public static String getInitNodeName() {
		return initNodeName;
	}

	private static String initNodeName;

	@Override
	public void init() throws Exception {
		// we need the ordered list of nodes to initialise
		// AotList<TreeGraphDataNode> list =
		// config.getInitialiser().getInitialisationList();
//		int i = 1;
//		double size = list.size();
//		for (TreeGraphDataNode n : list) {
//			try {
//				initNodeName = n.displayName();
//				double progress = i / size;
//				LauncherImpl.notifyPreloader(this, new Preloader.ProgressNotification(progress));
//				n.initialise();
//				i++;
//			} catch (Exception e) {
//				throw new Something("Initialisation failed for node: " + getInitNodeName() + ". ", e);
//			}
//		}
	}

	private static final long maxSplashDelay = 2000;

	@Override
	public void start(Stage primaryStage) throws Exception {
		// we could build the scene in an init() method.??
		EnumProperties.recordEnums();
		this.stage = primaryStage;
		stage.setWidth(DefaultWindowSettings.getWidth());
		stage.setHeight(DefaultWindowSettings.getHeight());
		stage.setX(DefaultWindowSettings.getX());
		stage.setY(DefaultWindowSettings.getY());
		String title = Project.getDisplayName();
		stage.titleProperty().set(title);
		setUserAgentStylesheet(STYLESHEET_CASPIAN);
		FXMLLoader loader = new FXMLLoader();
		if (Language.French())
			loader.setLocation(ModelRunnerfx.class.getResource("view/MrFR.fxml"));
		else
			loader.setLocation(ModelRunnerfx.class.getResource("view/MrEN.fxml"));

		Parent root = (Parent) loader.load();
		Scene scene = new Scene(root);
		stage.setScene(scene);

		DialogsFactory.setImplementation(new Dialogsfx(root.getScene().getWindow()));
		GraphStateFactory.setImplementation(new SimpleGraphStateImpl());

		controller = loader.getController();
		controller.setStage(stage);
		controller.getModel().setGraph(config);

		scene.getWindow().setOnCloseRequest((e) -> {
			stop();
		});
		Preferences.setImplementation(new PrefImpl(Project.makeRuntimePreferencesFile()));
		//
		// Everything is initialized here through cascading.
		uiDeployer = new GUIBuilder(uiNode, controller);
		// Get instance of controller (it is already initialized) and send the
		WidgetNode ctrlNode = getControllerNode(uiNode);
		StateMachineController smc = (StateMachineController) ctrlNode.getInstance();
		// This event is sent from here now rather than from the deployer
		// initialization.
		smc.sendEvent(initialise.event());

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

	private static WidgetNode getControllerNode(TreeGraphNode uiNode) {
		for (TreeNode tn : uiNode.subTree()) {
			if (tn instanceof WidgetNode) { // WidgetNodes are not related to SMC - only the instance is.
				WidgetNode wn = (WidgetNode) tn;
				String klass = (String) wn.properties().getPropertyValue(P_WIDGET_SUBCLASS.key());
				try {
					Class<?> widgetClass = Class.forName(klass);
					if (StateMachineController.class.isAssignableFrom(widgetClass))
						return wn;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		throw new NullPointerException("No controller found in configuration.");
	}

	@Override
	public void stop() {
		controller.putPreferences();
		uiDeployer.putPreferences();
		Preferences.getImplementation().flush();
		Platform.exit();
		System.exit(0);
	}

}
