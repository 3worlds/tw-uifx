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
package au.edu.anu.twuifx.mr.view;

import au.edu.anu.twcore.ecosystem.runtime.system.ComponentContainer;
import au.edu.anu.twcore.ecosystem.runtime.system.OldComponentContainer;
import fr.cnrs.iees.omugi.graph.impl.ALEdge;
import fr.cnrs.iees.omugi.graph.impl.TreeGraph;
import fr.cnrs.iees.omugi.graph.impl.TreeGraphDataNode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ian Davies - 6 Jan 2020
 */
// NB these trial bits of code assume the graph has been initialised i.e. these proposed methods are only called from ModelRunner
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
	 * Extracts a "parameters" graph from "initialisedConfig". This data is a _copy_
	 * from the initialisedGraph
	 * 
	 * @param initialisedConfig TODO: Not implemented
	 * @return TODO: Not implemented
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

	/**
	 * Overwrite the runTime parameters with data in "newPars"
	 * 
	 * @param initialisedConfig TODO: Not implemented
	 * @param newPars           TODO: Not implemented
	 */
	public static void putModelParameters(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig,
			TreeGraph<TreeGraphDataNode, ALEdge> newPars) {
	}

	/**
	 * Checks that "pars" is a valid parameter set for "initialisedConfig"
	 * 
	 * @param initialisedConfig TODO: Not implemented
	 * @param pars              TODO: Not implemented
	 * @return TODO: Not implemented
	 */
	public static boolean validModelParameters(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig,
			TreeGraph<TreeGraphDataNode, ALEdge> pars) {
		return false;
	}

	/**
	 * Extract "systemState" graph from "initialisedConfig"
	 * 
	 * @param initialisedConfig TODO: Not implemented
	 * @return TODO: Not implemented
	 */
	public static TreeGraph<TreeGraphDataNode, ALEdge> getModelState(
			TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		// The old code had Community.asGraph() function???
		return null;
	}

	/**
	 * Overwrite the runTime state with data in "newState"
	 * 
	 * @param newState          TODO: Not implemented
	 * @param initialisedConfig TODO: Not implemented
	 */
	public static void putModelState(TreeGraph<TreeGraphDataNode, ALEdge> newState,
			TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		clearModelState(initialisedConfig);

	}

	/**
	 * Checks that "state" is a valid data set for "initialisedConfig"
	 * 
	 * @param initialisedConfig TODO: Not implemented
	 * @param state             TODO: Not implemented
	 * @return TODO: Not implemented
	 */
	public static boolean validModelState(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig,
			TreeGraph<TreeGraphDataNode, ALEdge> state) {
		return false;
	}

	/**
	 * Sets all state data to appropriate zero values - essentially clears all
	 * populations
	 * 
	 * @param initialisedConfig TODO: Not implemented
	 */
	public static void clearModelState(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		for (OldComponentContainer community : communities(initialisedConfig))
			community.clearState();
	}

	/**
	 * @param initialisedConfig TODO: Not implemented
	 */
	public static void resetModelState(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		for (OldComponentContainer community : communities(initialisedConfig))
			community.reset();
	}

	private static List<OldComponentContainer> communities(TreeGraph<TreeGraphDataNode, ALEdge> initialisedConfig) {
		List<OldComponentContainer> result = new ArrayList<>();
//		List<TreeGraphDataNode> systems = (List<TreeGraphDataNode>) get(initialisedConfig.root().getChildren(),
//				selectOneOrMany(hasTheLabel(N_SYSTEM.label())));
//		for (TreeGraphDataNode system : systems) {
//			SimulatorNode simNode = (SimulatorNode) get(system.getChildren(),
//					selectOne(hasTheLabel(N_DYNAMICS.label())));
////			for (Simulator sim : simNode.getSimulators())
////				result.add(sim.community());
//		}
		return result;
	}

	/**
	 * This will be tricky! Return whatever is required (System factories?) to
	 * generate a new state for "initialisedConfig"
	 * 
	 * @param initialisedConfig TODO: Not implemented
	 * @return TODO: Not implemented
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
	 * @param configGraph TODO: Not implemented
	 */
	public static void dumpGraphState(TreeGraph<TreeGraphDataNode, ALEdge> configGraph) {
//		List<TreeGraphDataNode> systems = (List<TreeGraphDataNode>) get(configGraph.root().getChildren(),
//				selectOneOrMany(hasTheLabel(N_SYSTEM.label())));
//		for (TreeGraphDataNode system : systems) {
//			SimulatorNode simNode = (SimulatorNode) get(system.getChildren(),
//					selectOne(hasTheLabel(N_DYNAMICS.label())));
//			TreeNode st = (TreeNode) get(system.getChildren(), selectZeroOrOne(hasTheLabel(N_STRUCTURE.label())));
//			List<ComponentType> cts = (List<ComponentType>) get(st.getChildren(),
//					selectOneOrMany(hasTheLabel(N_COMPONENTTYPE.label())));
//			for (ComponentType ct : cts) {
//				ct.categoryId();
//				for (SystemFactory sf : ct.getFactories().values()) {
//					System.out.println("FACTORY: " + system.id() + ":" + ct.categoryId() + ":" + ct.id());
//					// NB side-effects: increments instance count MAX 2^31-1 LONG : 2^63-1
//					// Does NOT add to the factory graph - there is none
//					System.out.println("example instance: " + sf.newInstance());
//				}
//			}
//			for (Simulator sim : simNode.getSimulators()) {
//				ComponentContainer community = sim.community();
//
//				int count = 0;
//				System.out.println("STATE");
//				for (SystemComponent sc : community.allItems()) {
//					if (count <= 10)
//						System.out.println(sc);
//					count++;
//				}
//				if (count > 10)
//					System.out.println("... and " + (count - 10) + " other(s).");
//
//				System.out.println("CONTAINERS");
//				printContainer(community);
//
//			}
//		}
//		System.out.println("----------------- END --------------------------");
	}

//	private static void printContainer(ComponentContainer container) {
//		System.out.println("ID: " + container.id());
////		System.out.println("VARS:" + container.variables());
//		System.out.println("PARS:" + container.parameters());
//		if (container.parameters()!=null) {
//			TwData pars = container.parameters().clone();
//			for (String key:pars.getKeysAsSet())
//				System.out.println(key+":"+pars.getPropertyValue(key));
//		}
//
//		for (SystemComponent component : container.getInitialItems()) {
//			System.out.println("INIT: " + component);
//			SimplePropertyList props =  component.properties().clone();
//			for (String key:props.getKeysAsSet())
//				System.out.println(key+":"+props.getPropertyValue(key));
//
//		}
//		for (CategorizedContainer<SystemComponent> childContainer : container.subContainers()) {
//			printContainer((ComponentContainer) childContainer);
//		}
//
//	}

}
