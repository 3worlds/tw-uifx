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
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import au.edu.anu.omhtk.jars.Jars;
import au.edu.anu.rscs.aot.init.InitialiseMessage;
import au.edu.anu.rscs.aot.init.Initialiser;
import au.edu.anu.twcore.InitialisableNode;
import au.edu.anu.twcore.archetype.TwArchetypeConstants;
import au.edu.anu.twcore.ecosystem.runtime.simulator.RunTimeId;
import au.edu.anu.twcore.experiment.Design;
import au.edu.anu.twcore.experiment.Experiment;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.project.TwPaths;
import au.edu.anu.twcore.ui.WidgetNode;
import au.edu.anu.twcore.ui.runtime.Kicker;
import fr.cnrs.iees.OmugiClassLoader;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.io.FileImporter;
import fr.cnrs.iees.twcore.constants.ExperimentDesignType;
import fr.cnrs.iees.twcore.generators.odd.DocoGenerator;
import fr.ens.biologie.generic.Initialisable;
import fr.ens.biologie.generic.utils.Logging;
import java.util.logging.Logger;

import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * @author Ian Davies -30 Aug 2019
 */
// This replaces MrLauncher
public class MRmain {

	private MRmain() {
	}

	private static String usage = "Usage:\n" + MRmain.class.getName()
			+ " [instance id] [<Project relative directory>] [default log level]  [class to log:level...]";
	private static Logger log = Logging.getLogger(MRmain.class);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		// great trick - allows myProject.jar to be run without args if run from the prj
		// dir.
		if (args.length == 0) {
			try {
				if (Project.isValidProjectFile(new File(".").getCanonicalFile())) {
					args = new String[3];
					args[0] = "0";
					args[1] = new File(".").getCanonicalFile().getName();
					args[2] = "OFF";
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// must have an id and a project
		if (args.length < 2) {
			System.err.println(Arrays.deepToString(args));
			System.err.println(usage);
			System.exit(1);
		}
		try {
			int id = Integer.parseInt(args[0]);
			RunTimeId.setRunTimeId(id);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.err.println(Arrays.deepToString(args));
			System.err.println(usage);
			System.exit(1);
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
				System.err.println(Arrays.deepToString(args));
				System.err.println(usage);
				System.exit(1);
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

		File prjDir = new File(TwPaths.TW_ROOT + File.separator + args[1]);
		if (!prjDir.exists()) {
			System.err.println("Project not found: [" + prjDir + "]");
			System.err.println(Arrays.deepToString(args));
			System.exit(1);
		}

		if (Project.isOpen()) {
			System.err.println("Project is already open: [" + prjDir + "]");
			System.err.println(Arrays.deepToString(args));
			System.exit(1);
		}

		Project.open(prjDir);
		// Make the runtime dir, otherwise saving prefs will crash
		Project.makeFile(ProjectPaths.RUNTIME).mkdirs();

		// If we are not running from a jar then load the generated classes
		if (Jars.getRunningJarFilePath(MRmain.class) == null) {
			log.info("ModelRunner is NOT running from JAR");
			File userJar = Project.makeFile(Project.getProjectUserName() + ".jar");
			if (!userJar.exists()) {
				System.err.println("User generated classes not found: [" + userJar + "]");
				System.err.println(Arrays.deepToString(args));
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
					System.err.println("Unable to set logger: Class not found [" + klass + "]");
					System.err.println(Arrays.deepToString(args));
					System.err.println(usage);
					System.exit(1);
				}
			}
		} else
			log.info("ModelRunner is running from JAR");
		// Nothing to do. We are running from a jar so all classes are here.

		TreeGraph<TreeGraphDataNode, ALEdge> configGraph = (TreeGraph<TreeGraphDataNode, ALEdge>) FileImporter
				.loadGraphFromFile(Project.makeConfigurationFile());

		// initGraph(configGraph);

		TreeNode uiNode = (TreeNode) get(configGraph.root().getChildren(), selectOne(hasTheLabel(N_UI.label())));
		WidgetNode ctrlHl = getHeadlessController(uiNode);
		boolean hasGUI = hasGUI(uiNode);
		if (hasGUI) {
			log.info("Ready to run with GUI ");
			ModelRunnerfx.launchUI(configGraph);
			if (ctrlHl != null) {// TODO test this option.
				// Here we have some GUI widgets but a headless controller. Is this useful??
				// NB There is a query to ensure there exists one and only one controller for
				// the graph.
				/**
				 * If ctrlHl were cast to a StateMachineController we could instead just do
				 * ctrl.sendEvent(run.event()); However, by doing this we miss recording the
				 * start time. The ctrl records the endTime when onStatusMessage (state) is
				 * "finished". This is really the only purpose of a kicker here! Perhaps, at
				 * some other time, we could wrap this in a cmd line interactive shell that
				 * allows typing cmds. run/stop / pause etc
				 */

				Kicker ctrl = (Kicker) ctrlHl.getInstance();
				ctrl.start();
			}
		} else {
			log.info("Ready to run headless");
			/**
			 * Initialise the non-gui widgets. Note: With a GUI, all widgets are initialised
			 * by the GUIBuilder. Perhaps that should be removed and added above so its
			 * clearer?
			 */

			/**
			 * If ctrlHl were cast to a StateMachineController we could instead just do
			 * ctrl.sendEvent(run.event()); However, by doing this we miss recording the
			 * start time. The ctrl records the endTime when onStatusMessage (state) is
			 * "finished". This is really the only purpose of a kicker here! Perhaps, at
			 * some other time, we could wrap this in a cmd line interactive shell that
			 * allows typing cmds. run/stop / pause etc
			 */
			Experiment exp = (Experiment) get(configGraph.root().getChildren(),
					selectOne(hasTheLabel(N_EXPERIMENT.label())));

			int nSim = 1;
			if (exp.properties().hasProperty(P_EXP_NREPLICATES.key()))
				nSim = (Integer) exp.properties().getPropertyValue(P_EXP_NREPLICATES.key());
			Design dsgn = (Design) get(exp.getChildren(), selectOne(hasTheLabel(N_DESIGN.label())));

			ExperimentDesignType edt = null;

			if (dsgn.properties().hasProperty(P_DESIGN_TYPE.key())) {
				edt = (ExperimentDesignType) dsgn.properties().getPropertyValue(P_DESIGN_TYPE.key());
			}

			DateTimeFormatter fm = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss:SSS");
			LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
			String date = currentDate.format(fm);

			System.out.println("Running... [Project: " + Project.getDisplayName() + "; Date: " + date + "]");
			int nTreatments = 1;
			if (edt.equals(ExperimentDesignType.crossFactorial) || edt.equals(ExperimentDesignType.sensitivityAnalysis))
				nTreatments = exp.getExperimentDesignDetails().treatments().size();
			System.out.println("Initialising... [Simulators: " + (nSim * nTreatments) + "]");
			for (TreeNode n : ctrlHl.getParent().getChildren()) {
				InitialisableNode in = (InitialisableNode) n;
				in.initialise();
			}

			Kicker ctrl = (Kicker) ctrlHl.getInstance();
			System.out.println("Initialising [done]");

			String desc = exp.toShortString();
			if (edt!=null)
				desc +="("+edt.name()+")";
			System.out.println("Starting... [" + desc + "]");
			ctrl.start();
			// Loop the main thread until controller receives finished msg
			while (!ctrl.ended())
				;

			System.out.println("Running [done]");
			// Generate doco - there is no other opportunity so it's done here without
			// asking.
			System.out.println("Writing documentation...");
			DocoGenerator gen = new DocoGenerator(configGraph);
			gen.generate();
			System.out.println("Writing [done]");
			System.out.println("Finished");

//			System.out.println("--- Main thread exit ---");
		}
	} // end main()

	/* Find a headless controller if it exists. */
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

	/* Check if there is any kind of GUI. */
	private static boolean hasGUI(TreeNode uiNode) {
		// Queries ensure these nodes, if present, have children
		for (TreeNode n : uiNode.getChildren()) {
			if (n.classId().equals(N_UITAB.label()) || n.classId().equals(N_UITOP.label())
					|| n.classId().equals(N_UIBOTTOM.label())) {
				return true;
			}
		}
		return false;
	}

	protected static void initGraph(TreeGraph<TreeGraphDataNode, ALEdge> g) {
		List<Initialisable> initList = new LinkedList<>();
		for (TreeNode n : g.nodes())
			initList.add((Initialisable) n);
		Initialiser initer = new Initialiser(initList);
		initer.initialise();
		if (initer.errorList() != null) {
			for (InitialiseMessage msg : initer.errorList())
				System.err.println("FAILED: " + msg.getTarget() + msg.getException().getMessage());
			System.exit(1);
		}

	}

}
