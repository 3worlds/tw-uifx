package au.edu.anu.twuifx.mr;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import au.edu.anu.omhtk.jars.Jars;
import au.edu.anu.rscs.aot.init.InitialiseMessage;
import au.edu.anu.rscs.aot.init.Initialiser;
import au.edu.anu.twcore.ecosystem.runtime.TwFunction;
import au.edu.anu.twcore.ecosystem.runtime.biology.ChangeStateFunction;
import au.edu.anu.twcore.ecosystem.runtime.biology.TwFunctionAdapter;
import au.edu.anu.twcore.project.Project;
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
		File userJar = Project.makeFile(Project.getProjectUserName() + ".jar");
		if (!userJar.exists()) {
			System.out.println("User generated classes not found: [" + userJar + "]");
			System.exit(1);

		}

		if (Jars.getRunningJarFilePath(Main.class) == null) {
			loadUserClasses(userJar);
		} else
			OmugiClassLoader.setClassLoader(Thread.currentThread().getContextClassLoader());
//			    System.out.println("Classloader of ArrayList:"
//			        + FunctionNode.class.getClassLoader());
		@SuppressWarnings("unchecked")
		TreeGraph<TreeGraphNode, ALEdge> configGraph = (TreeGraph<TreeGraphNode, ALEdge>) FileImporter
				.loadGraphFromFile(Project.makeConfigurationFile());
		// TwConfigFactory labels are lost somewhere???
//		SimulationSession session = new SimulationSession(configGraph);
		// generated classes are not accessible here?
		List<Initialisable> initList = new LinkedList<>();
		for (TreeGraphNode n : configGraph.nodes())
//			if (n instanceof Initialisable)
			initList.add((Initialisable) n);
		Initialiser initer = new Initialiser(initList);
		initer.initialise();
		if (initer.errorList() != null)
			for (InitialiseMessage msg : initer.errorList()) {
				System.out.println("FAILED: " + msg.getTarget() + msg.getException().getMessage());
			}
		else {
			TreeGraphNode uiNode = (TreeGraphNode) get(configGraph.root().getChildren(),
					selectZeroOrOne(hasTheLabel(N_UI.label())));
			if (uiNode != null) {
				System.out.println("Ready to launch UI");
			} else {
				System.out.println("Ready to run without UI?");
			}
		}
	}

	private static void loadUserClasses(File userJar) {
		URL userUrl;
		try {
			userUrl = userJar.toURI().toURL();
			URL path[] = { userUrl };
//			ClassLoader parent = ClassLoader.getPlatformClassLoader();
			ClassLoader parent = Thread.currentThread().getContextClassLoader();
			URLClassLoader child = new URLClassLoader(path, parent);
			OmugiClassLoader.setClassLoader(child);
			// test code
			try {
				Class<? extends TwFunction> functionClass = (Class<? extends TwFunction>) Class
						.forName("system1.Function1", true, child);
				System.out.println(functionClass);
				// ok to here
				Constructor<? extends TwFunction> nodeConstructor = functionClass.getDeclaredConstructor();
				System.out.println(nodeConstructor);
				Object function = nodeConstructor.newInstance();
				System.out.println(function.getClass());
				System.out.println(function.getClass().getSuperclass());
				System.out.println(function.getClass().getSuperclass().getSuperclass());
				Class<?>[] intfs = function.getClass().getSuperclass().getSuperclass().getInterfaces();
				System.out.println(intfs[0].getName());
				System.out.println("function class loader: "+function.getClass().getClassLoader());				
				TwFunction f = new MyChangeStateFunction();
				System.out.println("f class loader: "+f.getClass().getClassLoader());
		/*
				 * fail here. class system1.Function1 cannot be cast to class
				 * au.edu.anu.twcore.ecosystem.runtime.TwFunction (system1.Function1 is in
				 * unnamed module of loader java.net.URLClassLoader @17c68925;
				 * au.edu.anu.twcore.ecosystem.runtime.TwFunction is in unnamed module of loader
				 * 'app')
				 * 
				 * SOMETHING TO DO WITH Modules and layers crap!!!
				 */
				f = (TwFunction) function;
			} catch (Exception e) {
				e.printStackTrace();
			}
			//

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String pathSeparator = System
//			    .getProperty("path.separator");
//			String[] classPathEntries = System
//			    .getProperty("java.class.path")
//			    .split(pathSeparator);
//			for (String s: classPathEntries) {
//				System.out.println(s);
//			}

	}

}
