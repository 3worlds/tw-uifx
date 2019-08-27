package au.edu.anu.twuifx.mr;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import au.edu.anu.rscs.aot.init.InitialiseMessage;
import au.edu.anu.rscs.aot.init.Initialiser;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.TwPaths;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.io.FileImporter;
import fr.ens.biologie.generic.Initialisable;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * TODO: at the moment this is a dummy application Main class for testing the
 * jar packing in TwSystem. Of course it will have to change when ModelRunner is
 * functional
 * 
 * @author Jacques Gignoux - 14 août 2019
 *
 */
// This should now replace the stupid MrLauncher i wrote.
public class Main {

	private Main() {
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage:");
			System.out.println(Main.class.getName() + " <Project directory>.");
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
		File userJar = Project.makeFile(Project.getProjectName() + ".jar");
		if (!userJar.exists()) {
			System.out.println("User generated classes not found: [" + userJar + "]");
			System.exit(1);

		}
// needs to install this in the thread class loader?
		loadUserClasses(userJar);
		@SuppressWarnings("unchecked")
		TreeGraph<TreeGraphDataNode, ALEdge> configGraph = (TreeGraph<TreeGraphDataNode, ALEdge>) FileImporter
				.loadGraphFromFile(Project.makeConfigurationFile());
//		SimulationSession session = new SimulationSession(configGraph);
		// generated classes are not accessible here?
		List<Initialisable> initList = new LinkedList<>();
		for (TreeGraphDataNode n : configGraph.nodes())
			initList.add((Initialisable) n);
		Initialiser initer = new Initialiser(initList);
		initer.initialise();
		if (initer.errorList() != null)
			for (InitialiseMessage msg : initer.errorList()) {
				System.out.println("FAILED: " + msg.getTarget() + msg.getException().getMessage());
			}
		else {
			TreeGraphDataNode uiNode = (TreeGraphDataNode) get(configGraph.root().getChildren(),
					selectZeroOrOne(hasTheLabel(N_UI.label())));
			if (uiNode != null) {
				System.out.println("Ready to launch UI");
			} else {
				System.out.println("Ready to run without UI?");
			}
		}
	}

	private static void loadUserClasses(File userJar) {
		// This typecast is no longer possible cf:
		// https://blog.codefx.org/java/java-11-migration-guide/
		/*-
		 * URL path[] = { ... }; 
		 * ClassLoader parent = ClassLoader.getPlatformClassLoader(); 
		 * URLClassLoader loader = new URLClassLoader(path, parent);
		 */
		ClassLoader parent = ClassLoader.getPlatformClassLoader();
//		URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		URL userUrl;

		try {
			userUrl = userJar.toURI().toURL();
			URL path[] = {userUrl};
			URLClassLoader loader = new URLClassLoader(path, parent);
			URL[] currentUrls = loader.getURLs();
			boolean found = false;
			for (URL currentUrl : currentUrls) {
				if (userUrl.sameFile(currentUrl)) {
					found = true;
					break;
				}
			}
			if (!found) {
				Class[] parameters = new Class[] { URL.class };
				Method method;
				method = URLClassLoader.class.getDeclaredMethod("addURL", parameters);
				method.setAccessible(true);
				method.invoke(loader, userUrl);
			}

		} catch (Exception e) {
//			log.error("could not load user-defined classes");
			e.printStackTrace();
		}

	}

}
