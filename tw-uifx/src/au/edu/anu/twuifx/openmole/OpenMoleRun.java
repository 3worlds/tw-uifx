package au.edu.anu.twuifx.openmole;

/**
 * Note: (https://docs.oracle.com/javase/tutorial/deployment/jar/downman.html)
 * <p>
 * The Class-Path header points to classes or JAR files on the local network,
 * not JAR files within the JAR file or classes accessible over Internet
 * protocols. To load classes in JAR files within a JAR file into the class
 * path, you must write custom code to load those classes. For example, if
 * MyJar.jar contains another JAR file called MyUtils.jar, you cannot use the
 * Class-Path header in MyJar.jar's manifest to load classes in MyUtils.jar into
 * the class path.
 * </p>
 * <p>
 * This is a template of what a generated java class could look like for a
 * particular model with particular treatment nodes. It assumes the relevant utg
 * file is with this file in a jar used by OpenMole.
 * 
 * @author Ian Davies 16 Sept 2022
 *
 */
public class OpenMoleRun {
	/**
	 * @param args TODO: Not implemented
	 */
	public static void main(String[] args) {
		run(0);
	}

	/**
	 * Testing if openmole runs this class in a separate process.
	 */
	public static long testNumber = -1;

	/**
	 * This file largely boilerplate except for:
	 * <li>for the parameters</li>
	 * <li>The imported file name;</li>
	 * <li>updateParameters</li>
	 * 
	 * @param r a constant for exp manipulation
	 */
	public static void run(int r) {
		System.out.println("r: " + r);
		System.out.println("PID: " + ProcessHandle.current().pid());
		if (testNumber != -1L)
			throw new IllegalStateException("TestNumber: " + testNumber);
		testNumber = ProcessHandle.current().pid();
//		Logging.setDefaultLogLevel(Level.parse("SEVERE"));
//		EnumProperties.recordEnums();
//		FXEnumProperties.recordEnums();
//		@SuppressWarnings("unchecked")
//		TreeGraph<TreeGraphDataNode, ALEdge> configGraph = (TreeGraph<TreeGraphDataNode, ALEdge>) GraphImporter
//				.importGraph("Logistic1.utg", OpenMoleRun.class);
//// Uncomment to run from eclipse
//		if (Jars.getRunningJarFilePath(OpenMoleRun.class) == null) {
//			File userJar = new File("/home/ian/3w/project_Logistic1_2022-10-11-01-15-35-503/Logistic1.jar");
//			OmugiClassLoader.setJarClassLoader(userJar);
//		}
//		// else the generated classes must be in the jar. For OM this means building the
//		// model jar with these classes together with this class and the tw<>.jar i.e.
//		// code/<system>/etc
//		TreeNode uiNode = (TreeNode) get(configGraph.root().getChildren(), selectOne(hasTheLabel(N_UI.label())));
//		WidgetNode ctrlHl = getHeadlessController(uiNode);
//
//		for (TreeNode n : ctrlHl.getParent().getChildren()) {
//			InitialisableNode in = (InitialisableNode) n;
//			in.initialise();
//		}
//
//		Kicker ctrl = (Kicker) ctrlHl.getInstance();
//
//		ctrl.start();
//		while (!ctrl.ended())
//			;
	}

//	private static WidgetNode getHeadlessController(TreeNode uiNode) {
//		RunTimeId.setRunTimeId(0);
//		Class<?> smcClass = fr.cnrs.iees.rvgrid.statemachine.StateMachineController.class;
//		TreeNode headlessNode = (TreeNode) get(uiNode.getChildren(),
//				selectZeroOrOne(hasTheLabel(N_UIHEADLESS.label())));
//		if (headlessNode == null)
//			return null;
//		for (TreeNode n : headlessNode.getChildren()) {
//			TreeGraphDataNode widgetNode = (TreeGraphDataNode) n;
//			String kstr = (String) widgetNode.properties().getPropertyValue(TWA.SUBCLASS);
//			try {
//				Class<?> widgetClass = Class.forName(kstr);
//				if (smcClass.isAssignableFrom(widgetClass))
//					return (WidgetNode) widgetNode;
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}

}
