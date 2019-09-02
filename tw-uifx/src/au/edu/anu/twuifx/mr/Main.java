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
import java.util.LinkedList;
import java.util.List;

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
//		if (Jars.getRunningJarFilePath(Main.class) == null) {
//			File userJar = Project.makeFile(Project.getProjectUserName() + ".jar");
//			if (!userJar.exists()) {
//				System.out.println("User generated classes not found: [" + userJar + "]");
//				System.exit(1);
//			}
//			OmugiClassLoader.setJarClassLoader(userJar);
//		} else {
//			// Nothing to do. We are running from a jar so all classes are here.
//		}
		
		// OK try this regardless
		File userJar = Project.makeFile(Project.getProjectUserName() + ".jar");
		if (!userJar.exists()) {
			System.out.println("User generated classes not found: [" + userJar + "]");
			System.exit(1);
		}
		OmugiClassLoader.setJarClassLoader(userJar);

		TreeGraph<TreeGraphNode, ALEdge> configGraph = (TreeGraph<TreeGraphNode, ALEdge>) FileImporter
				.loadGraphFromFile(Project.makeConfigurationFile());
		// this initialises the graph
//		 Humm... I imagine the experiement deployer will be responsible for node init
//		Therefore, creating the widget ui should not be a part of init()
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
		TreeGraphNode uiNode = (TreeGraphNode) get(configGraph.root().getChildren(),
			selectZeroOrOne(hasTheLabel(N_UI.label())));
		if (uiNode != null) {
			// ok now we can start building the ui
			System.out.println("Ready to launch UI");
			ModelRunnerfx.launchUI(configGraph, args);
		} else {
			System.out.println("Ready to run without UI?");
		}
	}

}
