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
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.project.TwPaths;
import fr.cnrs.iees.OmugiClassLoader;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
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
			+ " <Project relative directory> default logging level, class:level.";
	private static Logger log = Logging.getLogger(Main.class);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		if (args.length > 1)
			Logging.setDefaultLogLevel(Level.parse(args[1]));
		else
			Logging.setDefaultLogLevel(Level.OFF);

		List<String> failedClasses = new ArrayList<>();
		List<String> failedLevels = new ArrayList<>();
		/*
		 * deal with loggers that don't require using the OmugiClassLoader to avoid
		 * effecting that class
		 */
		for (int i = 2; i < args.length; i++) {
			String[] pair = args[i].split(":");
			if (pair.length != 2) {
				System.out.println(usage);
				System.exit(-1);
			}
			String klass = pair[0];
			String level = pair[1];
			try {
				Class<?> c = Class.forName(klass, false, Thread.currentThread().getContextClassLoader());
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

		if (args.length < 1) {
			System.out.println(usage);
			System.exit(1);
		}

		File prjDir = new File(TwPaths.TW_ROOT + File.separator + args[0]);
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
					Class<?> c = Class.forName(klass, false, OmugiClassLoader.getJarClassLoader());
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

		TreeGraph<TreeGraphNode, ALEdge> configGraph = (TreeGraph<TreeGraphNode, ALEdge>) FileImporter
				.loadGraphFromFile(Project.makeConfigurationFile());

		// TODO complete the cascading init system ad remove this
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

		if (get(configGraph.root().getChildren(), selectZeroOrOne(hasTheLabel(N_UI.label()))) != null) {
			log.info("Ready to run with user-interface ");
			ModelRunnerfx.launchUI(configGraph);
		} else {
			log.info("Ready to run headless");
		}
	}

}
