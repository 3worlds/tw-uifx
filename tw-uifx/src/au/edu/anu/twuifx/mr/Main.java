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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import au.edu.anu.omhtk.jars.Jars;
import au.edu.anu.rscs.aot.init.InitialiseMessage;
import au.edu.anu.rscs.aot.init.Initialiser;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.project.TwPaths;
import fr.cnrs.iees.OmugiClassLoader;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.io.FileImporter;
import fr.ens.biologie.generic.Initialisable;
import fr.ens.biologie.generic.utils.Logging;

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

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO: use command line args to set the log level
		// possible values:
		// INFO = debug, WARNING, SEVERE=errors, OFF
		Logging.setDefaultLogLevel(Level.OFF);
		
		if (args.length < 1) {
			System.out.println("Usage:");
			System.out.println(Main.class.getName() + " <Project relative directory>.");
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

		// If we are not running from a jar the load the generated classes
		if (Jars.getRunningJarFilePath(Main.class) == null) {
			System.out.println("ModelRunner is NOT running from JAR");
			File userJar = Project.makeFile(Project.getProjectUserName() + ".jar");
			if (!userJar.exists()) {
				System.out.println("User generated classes not found: [" + userJar + "]");
				System.exit(1);
			}
			OmugiClassLoader.setJarClassLoader(userJar);
		} else {
			System.out.println("ModelRunner is running from JAR");
			// Nothing to do. We are running from a jar so all classes are here.
		}

		// OK try this regardless
		File userJar = Project.makeFile(Project.getProjectUserName() + ".jar");
		if (!userJar.exists()) {
			System.out.println("User generated classes not found: [" + userJar + "]");
			System.exit(1);
		}
//		OmugiClassLoader.setJarClassLoader(userJar);

		TreeGraph<TreeGraphNode, ALEdge> configGraph = (TreeGraph<TreeGraphNode, ALEdge>) FileImporter
				.loadGraphFromFile(Project.makeConfigurationFile());

		// TODO move this to the splash screen routine
		List<Initialisable> initList = new LinkedList<>();
		for (TreeGraphNode n : configGraph.nodes())
			initList.add((Initialisable) n);
		Initialiser initer = new Initialiser(initList);
		initer.initialise();
		if (initer.errorList() != null) {
			for (InitialiseMessage msg : initer.errorList())
				System.out.println("FAILED: " + msg.getTarget() + msg.getException().getMessage());
			System.exit(1);
		}

		int depth = 0;
		parentFirstList = new HashSet<>();
		initialiseTree(configGraph.root(), depth);

		if (get(configGraph.root().getChildren(), selectZeroOrOne(hasTheLabel(N_UI.label()))) != null) {
			ModelRunnerfx.launchUI(configGraph, args);
		} else {
			System.out.println("Ready to run without UI?");
		}
	}

	private static Set<TreeNode> parentFirstList;


	private static void onParentInitialised(TreeNode child, int depth) {
		Initialisable initNode = (Initialisable) child;
		System.out.println("Parent \tDepth: " + depth + "\tRank: " + initNode.initRank() + "\t" + child.classId() + ":"
				+ child.id());

	}

	private static void onChildrenInitialised(TreeNode parent, int depth) {
		Initialisable initNode = (Initialisable) parent;
		System.out.println("Child \tDepth: " + depth + "\tRank: " + initNode.initRank() + "\t" + parent.classId() + ":"
				+ parent.id());

	}

	// This method means onParentInitialised can't depend on children being ready.
	// If we do two passes we can
	private static void initialiseTree(TreeNode parent, int depth) {
		if (!parentFirstList.contains(parent)) {
			parentFirstList.add(parent);
			onParentInitialised(parent, depth);
		}
		for (TreeNode child : parent.getChildren())
			initialiseTree(child, depth + 1);
		onChildrenInitialised(parent, depth);
	}
}
