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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import au.edu.anu.omhtk.jars.Jars;
import au.edu.anu.rscs.aot.init.InitialiseMessage;
import au.edu.anu.rscs.aot.init.Initialiser;
import au.edu.anu.twcore.archetype.TwArchetypeConstants;
import au.edu.anu.twcore.ecosystem.dynamics.SimulatorNode;
import au.edu.anu.twcore.ecosystem.runtime.simulator.RunTimeId;
import au.edu.anu.twcore.ecosystem.runtime.simulator.Simulator;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.project.TwPaths;
import au.edu.anu.twcore.ui.WidgetNode;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.OmugiClassLoader;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.io.FileImporter;
import fr.ens.biologie.generic.Initialisable;
import fr.ens.biologie.generic.utils.Logging;
import java.util.logging.Logger;

import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * @author Ian Davies
 *
 * @date 30 Aug 2019
 */
// This replaces MrLauncher
public class Main {

	private Main() {
	}

	private static String usage = "Usage:\n" + Main.class.getName()
			+ ": id, <Project relative directory> default logging level, class:level.";
	private static Logger log = Logging.getLogger(Main.class);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// must have an id and a project
		if (args.length < 2) {
			System.out.println(usage);
			System.exit(-1);
		}
		try {
			int id = Integer.parseInt(args[0]);
			RunTimeId.setRunTimeId(id);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println(usage);
			System.exit(-1);
			;
		}

		if (args.length > 2)
			Logging.setDefaultLogLevel(Level.parse(args[2]));
		else
			Logging.setDefaultLogLevel(Level.OFF);

		List<String> failedClasses = new ArrayList<>();
		List<String> failedLevels = new ArrayList<>();
		/*
		 * deal with loggers that don't require using the OmugiClassLoader to avoid
		 * effecting that class
		 */
		for (int i = 3; i < args.length; i++) {
			String[] pair = args[i].split(":");
			if (pair.length != 2) {
				System.out.println(usage);
				System.exit(-1);
			}
			String klass = pair[0];
			String level = pair[1];
			try {
				Class<?> c = Class.forName(klass, true, OmugiClassLoader.getAppClassLoader());
				Level lvl = Level.parse(level);
				// ensures the logger is in the list
				Logger log = Logging.getLogger(c);
				log.setLevel(lvl);
			} catch (ClassNotFoundException e) {
				// store failed attempts for a second attempt later
				failedClasses.add(klass);
				failedLevels.add(level);
			}

		}

//		if (args.length < 1) {
//			System.out.println(usage);
//			System.exit(1);
//		}

		File prjDir = new File(TwPaths.TW_ROOT + File.separator + args[1]);
		if (!prjDir.exists()) {
			System.out.println("Project not found: [" + prjDir + "]");
			System.exit(1);
		}

		if (Project.isOpen()) {
			System.out.println("Project is already open: [" + prjDir + "]");
			System.exit(1);
		}

		Project.open(prjDir);
		// Make the runtime dir, otherwise saving prefs will crash
		Project.makeFile(ProjectPaths.RUNTIME).mkdirs();

		// If we are not running from a jar then load the generated classes
		if (Jars.getRunningJarFilePath(Main.class) == null) {
			log.info("ModelRunner is NOT running from JAR");
			File userJar = Project.makeFile(Project.getProjectUserName() + ".jar");
			if (!userJar.exists()) {
				System.out.println("User generated classes not found: [" + userJar + "]");
				System.exit(1);
			}
			// enable the url class loader
			OmugiClassLoader.setJarClassLoader(userJar);
			// Now try the failed classes again.
			for (int i = 0; i < failedClasses.size(); i++) {
				String klass = failedClasses.get(i);
				Level lvl = Level.parse(failedLevels.get(i));
				try {
					Class<?> c = Class.forName(klass, true, OmugiClassLoader.getJarClassLoader());
					// ensures the logger is in the list
					Logger log = Logging.getLogger(c);
					log.setLevel(lvl);
				} catch (ClassNotFoundException e) {
					log.severe("Unable to set logger for " + klass);
				}

			}
		} else
			log.info("ModelRunner is running from JAR");
		// Nothing to do. We are running from a jar so all classes are here.

		TreeGraph<TreeGraphDataNode, ALEdge> configGraph = (TreeGraph<TreeGraphDataNode, ALEdge>) FileImporter
				.loadGraphFromFile(Project.makeConfigurationFile());

		// TODO complete the cascading init system ad remove this
		// Keep here rather than the splash screen in case we really do want to do a
		// headless launch
		// TimeModel line 96 timeLine.initialise()
//		List<Initialisable> initList = new LinkedList<>();
//
//		log.info("Preparing initialisation");
//		for (TreeGraphNode n : configGraph.nodes())
//			initList.add((Initialisable) n);
//		Initialiser initer = new Initialiser(initList);
//		initer.initialise();
//		if (initer.errorList() != null) {
//			for (InitialiseMessage msg : initer.errorList())
//				System.out.println("FAILED: " + msg.getTarget() + msg.getException().getMessage());
//			System.exit(1);
//		}

		// Trying to assume its possible to have a headless ctrl and yet have GUI
		// widgets
		TreeNode uiNode = (TreeNode) get(configGraph.root().getChildren(), selectOne(hasTheLabel(N_UI.label())));
		WidgetNode ctrlHl = getHeadlessController(uiNode);
		boolean hasGUI = hasGUI(uiNode);
		if (hasGUI) {
			// if (get(configGraph.root().getChildren(),
			// selectZeroOrOne(hasTheLabel(N_UI.label()))) != null) {
			log.info("Ready to run with user-interface ");
			ModelRunnerfx.launchUI(configGraph);
			if (ctrlHl != null) {
				Widget ctrl = ctrlHl.getInstance();
				// we need a ctrl interface here!
				//ctrl.start();
			}
		} else { // TODO!!!
			log.info("Ready to run headless");
			List<Initialisable> initList = new LinkedList<>();

			log.info("Preparing initialisation");
			for (TreeGraphNode n : configGraph.nodes())
				initList.add((Initialisable) n);
			Initialiser initer = new Initialiser(initList);
			initer.initialise();
			if (initer.errorList() != null) {
				for (InitialiseMessage msg : initer.errorList())
					System.out.println("FAILED: " + msg.getTarget() + msg.getException().getMessage());
				System.exit(1);
			}
			
			Widget ctrl = ctrlHl.getInstance();
			//ctrl.start();

		}
	}

	private static WidgetNode getHeadlessController(TreeNode uiNode) {
		Class<?> smcClass = fr.cnrs.iees.rvgrid.statemachine.StateMachineController.class;
		TreeNode headlessNode = (TreeNode) get(uiNode.getChildren(),
				selectZeroOrOne(hasTheLabel(N_UIHEADLESS.label())));
		if (headlessNode == null)
			return null;
		for (TreeNode n : headlessNode.getChildren()) {
			TreeGraphDataNode widgetNode = (TreeGraphDataNode) n;
			String kstr = (String) widgetNode.properties().getPropertyValue(TwArchetypeConstants.twaSubclass);
			try {
				Class<?> widgetClass = Class.forName(kstr);
				if (smcClass.isAssignableFrom(widgetClass))
					return (WidgetNode) widgetNode;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/*- 
	 * 1) do we have GUIs. if so, cannot run as headless
	 * 2) If headless run, don't forget to initialise the widgets
	*/
	private static boolean hasGUI(TreeNode uiNode) {
		for (TreeNode n : uiNode.getChildren()) {
			if (n.classId().equals(N_UITAB.label()) || n.classId().equals(N_UITOP.label())
					|| n.classId().equals(N_UIBOTTOM.label())) {
				return true;
			}
		}
		return false;
	}

}
