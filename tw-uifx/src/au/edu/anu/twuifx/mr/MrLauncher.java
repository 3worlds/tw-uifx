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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import au.edu.anu.omhtk.jars.Jars;
import au.edu.anu.rscs.aot.graph.AotGraph;
import au.edu.anu.twcore.jars.SimulatorJar;
import au.edu.anu.twcore.project.ProjectPaths;

/**
 * <p>
 * The simulation runner - launched as a jar (normal mode) or standalone (for
 * debugging only).
 * </p>
 * 
 * <p>
 * The jar containing this class must be created by a call to
 * {@linkplain fr.ens.biologie.threeWorlds.build.jar.SimulatorJar} .saveJar().
 * It must contain:
 * </p>
 * <ol>
 * <li>the model configuration graph file (.dsl or .twg) in the root of the jar;
 * </li>
 * <li>all the aot and 3worlds classes and resource files (ie auxiliary and
 * default dsls, css files etc);</li>
 * <li>a manifest file with the following entries:
 * <ul>
 * <li><code>Main-Class: fr.ens.biologie.threeWorlds.core.ModelRunner</code></li>
 * <li><code>Class-Path: <em>UserModel</em>.jar ../tw-dep.jar</code></li>
 * </ul>
 * </li>
 * </ol>
 * <p>
 * Plus, the user-defined model file <code><em>UserModel</em>.jar</code> must
 * sit in the same directory as <code>simulator.jar</code>
 * </p>
 * 
 * @author Jacques Gignoux - 4 December 2017
 *
 */
public class MrLauncher implements ProjectPaths {

	private static File jarFile = null;
	// private static boolean runningFromJAR = false;

	/**
	 * <p>
	 * Testing if this code is running from a jar - hack found <a
	 * href=https://stackoverflow.com/questions/482560/can-you-tell-on-runtime-
	 * if-youre-running-java-from-within-a-jar> there</a>. The test is based on the
	 * existence of the manifest.
	 * </p>
	 */

	private static void detectRunningEnvironment() {
		String path = Jars.getRunningJarFilePath(MrLauncher.class);
		boolean found = path != null;
		if (found) {
			File file = new File(path);
			path = path.replace(file.getName(), SimulatorJar.SimJarName);// !!!
			jarFile = new File(path);
		} else
			jarFile = null;
	}

	public static String jarFilePath() {
		if (jarFile != null)
			return jarFile.getAbsolutePath();
		return null;
	}

	//
	public static boolean runningFromJAR() {
		return jarFile != null;
	}

	/**
	 * Gets the configuration graph from the JAR this class is running from (normal
	 * case)
	 * 
	 * @param filename
	 * @param dir
	 * 
	 * @return the raw configuration graph
	 * @throws IOException
	 */
//	private static AotGraph getGraphFromJar(String filename) {
//		log.debug("Running from JAR '" + jarFilePath() + "'");
//		log.debug("Resource name: " + filename);
//		try {
//			int result = compareFiles(filename, Project.getProjectFile());
//			if (result != 0)
//				log.warning("File on disk differs from file in jar at byte: " + result);
//			else
//				log.debug("File on disk is identical to file in jar.");
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
//
//		if (ins == null)
//			throw new TwuifxException("Unable to load resource '" + filename + "' using loader "
//					+ Thread.currentThread().getContextClassLoader().toString());
//
//		DslImporter importer = null;
//		try {
//			importer = new DslImporter(new AotReader(ins));
//		} catch (Exception e) {
//			log.error("The configuration graph could not be read: check its syntax");
//			e.printStackTrace();
//		}
//		AotGraph config = new AotGraph(importer);
//		return config;
//	}

	private static void loadUserClasses(AotGraph config) {
//		File userJar = Project.makeFile(UserProjectJar.USERPROJECTJAR);
//		URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//		URL userUrl;
//		try {
//			userUrl = userJar.toURI().toURL();
//			URL[] currentUrls = classLoader.getURLs();
//			boolean found = false;
//			for (URL currentUrl : currentUrls) {
//				if (userUrl.sameFile(currentUrl)) {
//					found = true;
//					break;
//				}
//			}
//			if (!found) {
//				Class[] parameters = new Class[] { URL.class };
//				Method method;
//				method = URLClassLoader.class.getDeclaredMethod("addURL", parameters);
//				method.setAccessible(true);
//				method.invoke(classLoader, userUrl);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}

	/**
	 * Gets the configuration graph from a project name passed as argument (only if
	 * not running from a JAR - debugging case). Also loads user-defined classes
	 * (not needed when running from a jar since this is automatic)
	 * 
	 * @param projectDir
	 *            the name of the project to look for (relative name)
	 * @return the raw configuration graph
	 */
//	private static AotGraph getGraphFromDir() {
//		File file = Project.getProjectFile();
////		log.debug("Running from project " + file.getAbsolutePath());
//		InputStream ins = null;
//		try {
//			ins = new FileInputStream(file);
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}
//		DslImporter importer = null;
//		try {
//			importer = new DslImporter(new AotReader(ins));
//		} catch (Exception e) {
//			log.error("The configuration graph could not be read: check its syntax");
//			e.printStackTrace();
//		}
//		AotGraph config = new AotGraph(importer);
//		return config;
//	}

	/**
	 * gets a resource (from file or from jar entry) in the currently running
	 * project
	 * 
	 * @param name
	 *            the resource to find (as a file name, usually)
	 * @return an InputStream (from jar or file content)
	 * @throws IOException
	 */

	private static int compareFiles(String name, File diskFile) throws IOException {
		BufferedInputStream input1 = null;
		BufferedInputStream input2 = null;
		try {
			input1 = new BufferedInputStream(new FileInputStream(diskFile));
			input2 = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(name));
			int ch1 = input1.read();
			int i = 1;
			while (-1 != ch1) {
				int ch2 = input2.read();
				if (ch1 != ch2) {
					input1.close();
					input2.close();
					return i;
				}
				ch1 = input1.read();
				i++;
			}

			int ch2 = input2.read();
			if (ch2 == -1) {
				input1.close();
				input2.close();
				return 0;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (input1 != null)
			input1.close();
		if (input2 != null)
			input2.close();
		return 0;

	}

	public static InputStream getProjectResource(String name) {
//		if (runningFromJAR()) {
//			name = name.replace("\\", Jars.separator);
//			if (name.startsWith(Jars.separator))
//				name = name.replaceFirst(Jars.separator, "");
//			InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
//			if (ins == null) {
//				// try project relative path
//				String prjDir = Project.getProjectDirectory() + File.separator;
//				prjDir = prjDir.replace("\\", Jars.separator);
//				if (prjDir.startsWith(Jars.separator))
//					prjDir = prjDir.replaceFirst(Jars.separator, "");
//				name = name.replace(prjDir, "");
//				ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
//			}
//			return ins;
//		} else {
//			File result = null;
//			File file = new File(name);
//			if (file.exists())
//				result = file;
//
//			// user home, ~/
//			file = new File(System.getProperty("user.home") + File.separator + name);
//			if (file.exists())
//				result = file;
//			// user current working directory, eg ~/<workspace>/ in eclipse
//			file = new File(System.getProperty("user.dir") + File.separator + name);
//			if (file.exists())
//				result = file;
//			// Why not first try getResourceAsStream(name). It does not return an exception.
//			InputStream ips = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
//			if (ips != null)
//				return ips;
//			// CAUTION: this is dirty, i dont like it...
//			// from eclipse, src/threeWorlds directory
//			file = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "threeWorlds"
//					+ File.separator + name);
//			if (file.exists())
//				result = file;
//			if (result != null)
//				try {
//					return new FileInputStream(result);
//				} catch (FileNotFoundException e) {
//					log.error("Could not open file " + result.getName());
//					e.printStackTrace();
//				}
//		}
		return null;
	}

	/**
	 * 3Worlds experiment runner. Will launch the UI with the user-defined controls
	 * and displays.
	 * 
	 * @param args
	 *            0..1 project directory name within the local .3w repository (eg
	 *            project_bidon_70F3950209EA-000001601ED1C085-0000)
	 */
	private static void openProject(String dirname) {
//		File dirPath = new File(
//				ProjectPaths.USER_ROOT + File.separator + ProjectPaths.TW_ROOT + File.separator + dirname);
//		Project.open(dirPath);
	}

	public static void main(String[] args) {

//		// command line must have two arguments:
//		// 1 the project directory name (relative)
//		// 2 the project dsl file name within this directory
//		// if wrong number of arguments, exit
//		if (args.length < 2) {
//			System.out.println("Usage:");
//			System.out.println(" ModelRunner <Project directory> <filename>.");
//			System.exit(1);
//		}
//
//		// open the project and get the dsl file
//		// fail if file does not exist
//		if (!Project.isOpen())
//			openProject(args[0]);
//		File projectFile = Project.getProjectFile();
//		if (projectFile == null)
//			throw new AotException("Project file '" + args[1] + "' not found in '" + args[0] + "'");
//		if (!projectFile.getName().equals(args[1]))
//			System.out.println(
//					"WARNING: ModelRunner arg[1] '" + args[1] + "' differs from '" + projectFile.getName() + "'");
//
//		// check if running from jar or from eclipse
//		detectRunningEnvironment();
//
//		// setup logging
//		// fail if logging configuration not found (in jar or in project directory tree)
//		String logConfigName = "fr/ens/biologie/threeWorlds/resources/defaults/SimulatorLogger.dsl";
//		InputStream loggingConfig = getProjectResource(logConfigName);
//		if (loggingConfig == null)
//			if (runningFromJAR())
//				throw new AotException(logConfigName + " not found in " + jarFilePath());
//			else
//				throw new AotException(logConfigName + " not found in the system");
//		LoggerFactory.loadConfig(loggingConfig);
//		log = LoggerFactory.getLogger(MrLauncher.class, "3Worlds");
//		log.enable();
//
//		// finally start interesting things
//		log.debug("Running 3Worlds... ");
//
//		// load the configuration graph from the configuration file or jar entry
//		AotGraph config = null;
//		if (runningFromJAR())
//			config = getGraphFromJar(args[1]);
//		else {
//			config = getGraphFromDir();
//			loadUserClasses(config);
//		}
//		config.resolveReferences();
//		// important! otherwise castnodes() misses the simulator !
//		Utilities.setDefaultProperties(config.nodes());
//		config.castNodes();
//		if ((config.findNode(N_UI.toString() + ":")!=null)){
//			log.debug("Starting the 3Worlds simulator with a graphical user interface");
//			ModelLauncher.launchUI(config, args);
//		} else {
//			log.debug("Starting the 3Worlds simulator without a graphical user interface");
//			config.initialise();
//			// somehow send a start message to simulator here??
//			// Maybe its as simple as having a flag in the statemachine to
//			// self start
//			// without requiring a msg
//		}
//
//		log.debug("...Done.");
	}

	// private static void checkResources() {
	// // Test resource loading
	// ClassChecker chk = new ClassChecker();
	// chk.findResource("fr/ens/biologie/threeWorlds/ui/modelMakerfx/view/images/Ellipsis16.gif");
	// /*
	// * NOTE: if not running from jar then we don't expect the data to be found by
	// * this method
	// */
	// if (runningFromJAR()) {
	// chk.findResource("test_model_9.dsl");
	// chk.findResource("JGSeedDispersalModel.ods");
	// }
	// chk.findClass("au.edu.anu.rscs.aot.graph.AotNode");
	// chk.findClass("fr.ens.biologie.threeWorlds.core.ecology.simulator.TimerModelSimulator");
	// // from tw-Dep.jar
	// chk.findClass("impl.org.controlsfx.tools.rectangle.Rectangles2D");
	// chk.findClass("com.jgraph.layout.JGraphCompoundLayout");
	// // // from model jar
	// chk.findClass("simpleSpatialPopulation.code.JGSeedDispersalModel");
	// chk.findClass("simpleSpatialPopulation.code.Bidon");
	//
	// chk.showURL("simpleSpatialPopulation.jar");
	// chk.showURL("tw-dep.jar");
	// chk.listLoadedClasses();
	//
	// }
}
