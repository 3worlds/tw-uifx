package au.edu.anu.twuifx.mr.view;

import au.edu.anu.twcore.data.runtime.TwData;
import au.edu.anu.twcore.ecosystem.dynamics.SimulatorNode;
import au.edu.anu.twcore.ecosystem.runtime.simulator.Simulator;
import au.edu.anu.twcore.ecosystem.runtime.system.CategorizedContainer;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemComponent;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemContainer;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemFactory;
import au.edu.anu.twcore.ecosystem.structure.ComponentType;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.properties.ReadOnlyPropertyList;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;

import java.util.List;
import java.util.Map;

import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;

/**
 * @author Ian Davies
 *
 * @date 6 Jan 2020
 */
// NB these trial bit of code assumes the graph has been initialised i.e. these proposed methods are only called from ModelRunner
/**
 * The problem here maybe if there is a dependence between pars and
 * drivers/state. If a new state is generated, will there be new parameters. If
 * so, modification of initial state files would invalidate parameter files. Is
 * this possible???
 * 
 * If so, drivers will have to be saved to the same file which will confound
 * debugging models.
 * 
 * For me: Parameter files contain properties where nothing can change apart
 * from editing the property values (constrained by meta-data).
 * 
 * Driver files contain ONLY things that change - populations, property values
 * and the state of random number streams. BTW This is violated if rns are hard
 * coded. Therefore, rns must appear in the graph for their state to be saved
 * and reloaded. The test is: if two initial state files are generated for t0
 * and t500, the state of the model at t1000 should be identical if run from t0
 * to t1000 for 1000 steps OR from t500 to t1000 for 500 steps... if you get
 * what i mean!
 */

public class RunTimeData {
	private RunTimeData() {
	};

	/**
	 * Extracts a "parameters" graph from "initialisedConfig". This data is a
	 * **copy** from the initialisedGraph
	 */
	public static TreeGraph<TreeGraphDataNode, ALEdge> getParameters(
			TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		/**
		 * The purpose is to populate a PropertySheetEditor so the user can modify pars
		 * at runTime and use various tools to create large tables. These should be a
		 * copy so changes don't take place unless an "Apply" button is pressed? Apply
		 * would call putParameters()
		 * 
		 * The return graph can be saved.
		 */

		return null;
	}

	/** Overwrite the runTime parameters with data in "newPars" */
	public static void putParameters(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig,
			TreeGraph<TreeGraphDataNode, ALEdge> newPars) {
	}

	/** Checks that "pars" is a valid parameter set for "initialisedConfig" */
	public static boolean validParameters(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig,
			TreeGraph<TreeGraphDataNode, ALEdge> pars) {
		return false;
	}

	/** Extract "systemState" graph from "initialisedConfig" */
	public static TreeGraph<TreeGraphDataNode, ALEdge> getState(
			TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		// The old code had Community.asGraph() function???
		return null;
	}

	/** Overwrite the runTime state with data in "newState" */
	public static void putState(TreeGraph<TreeGraphDataNode, ALEdge> newState,
			TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		clearState(initialisedConfig);

	}

	/** Checks that "state" is a valid data set for "initialisedConfig" */
	public static boolean validState(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig,
			TreeGraph<TreeGraphDataNode, ALEdge> state) {
		return false;
	}

	/**
	 * Sets all state data to appropriate zero values - essentially clears all
	 * populations
	 */
	public static void clearState(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		// must be called before calling "putState";
	}

	/**
	 * This will be tricky! Return whatever is required (System factories?) to
	 * generate a new state for "initialisedConfig"
	 */
	public static Object getSystemsForGeneration(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		/**
		 * Purpose: Return all necessary things so driver data and populations can be
		 * created with some tool for bootstrapping a model. The output of this tool
		 * will be a state graph to be saved.
		 * 
		 * Issues: If this process creates parameters, then everyting is confounded?
		 */
		return null;
	}

	// ------------------------------------------------

//	JG:
//	All parameters are stored in SystemContainers, organised as a tree 
//	(cf class CategorizedContainer), through the CategorizedContainer.parameters() method. 
//	There is a separate tree instance for every Simulator instance, accessible through
//	the Simulator.community() method. The Simulator instances are accessible through 
//	SimulatorNode.getInstance(id), which is in the initialedConfig tree passed
//	as argument here. But you also need to know the id of your current simulator
//	instance (eg the one being currently running in simple cases, or if many
//	simulators are running you probably need to give the user a way to select one)

	// temp code
	/*-
	 * --> I agree with this. Parameters should be kept separate from initial
	drivers. That's done in the graph (hence the parameterValues and
	variableValues nodes), but I didnt enforce that those files should be
	separate. It would probably make sense to separate them in different
	files at step 3 of our data loading system. The only point of having
	separate files is when you have one or more huge consistent sets of
	variables/parameters that you want to reuse in different projects. For
	small projects, p & v can be kept together.
	 * */
	/**
	 * Not for me. The main purpose here is for a model of a given structure i.e
	 * this project only, to allow for the same parameter value with different
	 * variable values or the same variables values with different parameters
	 * values. Does this mean parameters and variables are confounded by the current
	 * state of the simulation?
	 */

	@SuppressWarnings("unchecked")
	public static void testingRuntimeGraphStuff(TreeGraph<TreeGraphDataNode, ALEdge> configGraph) {
		List<TreeGraphDataNode> systems = (List<TreeGraphDataNode>) get(configGraph.root().getChildren(),
				selectOneOrMany(hasTheLabel(N_SYSTEM.label())));

		for (TreeGraphDataNode system : systems) {
			SimulatorNode simNode = (SimulatorNode) get(system.getChildren(),
					selectOne(hasTheLabel(N_DYNAMICS.label())));
			TreeNode st = (TreeNode) get(system.getChildren(), selectOne(hasTheLabel(N_STRUCTURE.label())));
			List<ComponentType> cts = (List<ComponentType>) get(st.getChildren(),
					selectOneOrMany(hasTheLabel(N_COMPONENTTYPE.label())));
			for (ComponentType ct : cts) {
				ct.categoryId();
				for (SystemFactory sf : ct.getFactories().values()) {
					System.out.println(ct.categoryId() + ":" + ct.id() + "->" + sf);
				}
			}
			/**
			 * There no simulators because they're cleared at line 208 of SimulatorNode
			 */
			for (Simulator sim : simNode.getSimulators()) {
				/**
				 * NB ive commented out line 208 of SimulatorNode to get this far
				 */
				SystemContainer community = sim.community();
				TwData pars = community.parameters();
				TwData vars = community.variables();
				// null if no category has "parameters" edge. ie. parameterClass property value
				// will be empty
				if (pars != null) {
					System.out.println("PARS");
					for (String key : pars.getKeysAsArray())
						System.out.println(simNode.id() + ":" + sim.id() + ":" + community.id() + ":" + key + ":"
								+ pars.getPropertyValue(key));
				} else {
					System.out.println(simNode.id() + ":" + sim.id() + ":" + community.id() + ":" + " has no parameters");
				}
				if (vars != null) {
					System.out.println("VARS");
					for (String key : vars.getKeysAsArray())
						System.out.println(simNode.id() + ":" + sim.id() + ":" + community.id() + ":" + key + ":"
								+ vars.getPropertyValue(key));

				} else {
					System.out.println(simNode.id() + ":" + sim.id() + ":" + community.id() + ":" + " has no variables");
				}

				System.out.println("STATE");
				for (SystemComponent sc : community.allItems()) {
					System.out.println(sc);
//					//sc.properties();
				}
				System.out.println("INITIAL STATE");
				for (SystemComponent sc : community.getInitialItems()) {
					System.out.println(sc);
				}
				System.out.println("SUBCOMMUNITIES");
				for (CategorizedContainer<SystemComponent> cc:community.subContainers()) {
					cc.subContainers();
					cc.allItems();
					cc.getInitialItems();
					
				};


				// rather: look at initialItems
			}
//			TreeGraphDataNode structure = (TreeGraphDataNode) get(system.getChildren(),
//					selectOne(hasTheLabel(N_STRUCTURE.label())));
//			List<ComponentType> componentTypes = (List<ComponentType>) get(structure.getChildren(),
//					selectOneOrMany(hasTheLabel(N_COMPONENTTYPE.label())));
//			for (ComponentType componentType : componentTypes) {
//				System.out.println(componentType.classId() + ":" + componentType.id());
//				for (Map<String, SystemContainer> scmap : componentType.containers()) {
//					for (SystemContainer sc : scmap.values()) {
//						System.out.println(sc.id());
//						for (SystemComponent comp : sc.allItems()) {
//							System.out.println(comp.classId() + ":" + comp.id());
//							ReadOnlyPropertyList props = comp.readOnlyProperties();
//							for (String key : props.getKeysAsSet())
//								System.out.println(key + ":" + props.propertyToString(key));
//							// there is no way of distinguishing between pars and vars at this level
//							// we need some other way - how does the data tracker do it?
//							
//						}
//					}
//				}
//				;
//
//			}
		}
	}

}
