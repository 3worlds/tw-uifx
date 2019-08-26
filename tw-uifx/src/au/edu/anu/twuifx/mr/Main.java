package au.edu.anu.twuifx.mr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import au.edu.anu.omhtk.jars.Jars;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.project.TwPaths;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.io.FileImporter;

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

	private static File jarFile = null;

	public static void main(String[] args) {
		// command line must have two arguments:
		// 1 the project directory name (relative)
		// 2 the project config file name within this directory
		// if wrong number of arguments, exit
		if (args.length != 1) {
			System.out.println("Usage:");
			System.out.println("ModelRunner <Project directory>.");
			System.exit(1);
		}

		// open the project and get the dsl file
		// fail if file does not exist
		if (!Project.isOpen())
			openProject(args[0]);
		File projectFile = Project.getProjectFile();
		if (projectFile == null)
			throw new TwuifxException("Project file '" + args[1] + "' not found in '" + args[0] + "'");
		if (!projectFile.getName().equals(args[1]))
			System.out.println(
					"WARNING: ModelRunner arg[1] '" + args[1] + "' differs from '" + projectFile.getName() + "'");
//		// check if running from jar or from eclipse
		detectRunningEnvironment();
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
		// (TreeGraph<TreeGraphDataNode, ALEdge>)
		// FileImporter.loadGraphFromFile(Project.makeConfigurationFile())
		TreeGraph<TreeGraphDataNode, ALEdge> config = null;
		if (runningFromJAR())
			config = getGraphFromJar(args[1]);
		else {
			config = getGraphFromDir();
			loadUserClasses(config);
		}
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

	private static void detectRunningEnvironment() {
		String path = Jars.getRunningJarFilePath(Main.class);
		boolean found = path != null;
		if (found) {
			File file = new File(path);
			// path = path.replace(file.getName(), SimulatorJar.SimJarName);// !!!
			jarFile = new File(path);
		} else
			jarFile = null;
	}

	private static void openProject(String dirname) {
		File dirPath = new File(TwPaths.TW_ROOT + File.separator + dirname);
		Project.open(dirPath);
	}

	private static boolean runningFromJAR() {
		return jarFile != null;
	}

	private static TreeGraph<TreeGraphDataNode, ALEdge> getGraphFromJar(String filename) {
		try {
			int result = compareFiles(filename, Project.getProjectFile());
			if (result != 0)
				System.out.println("File on disk differs from file in jar at byte: " + result);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

		if (ins == null)
			throw new TwuifxException("Unable to load resource '" + filename + "' using loader "
					+ Thread.currentThread().getContextClassLoader().toString());

		TreeGraph<TreeGraphDataNode, ALEdge> result = (TreeGraph<TreeGraphDataNode, ALEdge>) FileImporter
				.loadGraphFromFile(ins);
		return result;
	}

}
