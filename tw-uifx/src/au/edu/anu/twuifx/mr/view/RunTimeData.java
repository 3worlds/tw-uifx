package au.edu.anu.twuifx.mr.view;

import au.edu.anu.twcore.ecosystem.runtime.system.SystemComponent;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemContainer;
import au.edu.anu.twcore.ecosystem.structure.ComponentType;
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
// NB these trial bit of code assumes the graph has been initialised i.e. these methods are only called from ModelRunner
/**
 * The problem here maybe if there is a dependence between pars and drivers. If
 * a new state is generated, will there be new parameters. If so, modification
 * of initial state files would invalidate parameter files. Is this possible???
 * 
 * If so, drivers will have to be saved to the same file which will confound
 * debugging models.
 * 
 * For me: Parameter files contain properties where nothing can change apart
 * from values can be edited.
 * 
 * Driver files contain ONLY things that change - populations, property values
 * and the state of random number streams. BTW This is violated if rns are hard
 * coded. They must appear in the graph for their state to be saved and
 * reloaded. The test is: if two initial state files are generated for t0 and
 * t500, the state of the model at t1000 should be identical if run from t0 to
 * t1000 for 1000 steps OR from t500 to t1000 for 500 steps.
 */
public class RunTimeData {
	public Object getParameters(TreeGraph<TreeGraphDataNode, ALEdge> graph) {
		/**
		 * Some return type that provides a listing all par properties organised in some
		 * meaningful way. The purpose is to populate a PropertySheetEditor so the user
		 * can modify pars at runTime. These should be a copy so changes don't take
		 * place unless an "Apply" button is pressed?
		 * 
		 * Previously the return type was a graph of some kind. The Object should be
		 * saveable.
		 */
		return null;
	}

	public void putParameters(TreeGraph<TreeGraphDataNode, ALEdge> graph, Object newPars) {
		/**
		 * Update the runtime parameters with data from newPars
		 */

	}

	public Object getState(TreeGraph<TreeGraphDataNode, ALEdge> graph) {
		/**
		 * TODO method of extracting the system state and returning in some saveable
		 * object (a graph??).
		 */
		return null;
	}

	public void putState(Object newDrivers, TreeGraph<TreeGraphDataNode, ALEdge> graph) {
		/**
		 * TODO method of resetting the system state to the data in the given object.
		 */

	}

	public Object getSystemsForGeneration(TreeGraph<TreeGraphDataNode, ALEdge> graph) {
		/**
		 * Purpose: generate drivers and populations from some algorithm for
		 * bootstrapping a model. The result of the operation will be a saveable State
		 * object. cf above.
		 * 
		 * Issues: If this process creates parameters, then they will have to be edited
		 * (cf getParameters())
		 */
		return null;
	}

	// ------------------------------------------------
	// temp code
	@SuppressWarnings("unchecked")
	public static void listStuff(TreeGraph<TreeGraphDataNode, ALEdge> graph) {
		List<TreeGraphDataNode> systems = (List<TreeGraphDataNode>) get(graph.root().getChildren(),
				selectOneOrMany(hasTheLabel(N_SYSTEM.label())));

		for (TreeGraphDataNode system : systems) {
			TreeGraphDataNode structure = (TreeGraphDataNode) get(system.getChildren(),
					selectOne(hasTheLabel(N_STRUCTURE.label())));
			List<ComponentType> componentTypes = (List<ComponentType>) get(structure.getChildren(),
					selectOneOrMany(hasTheLabel(N_COMPONENTTYPE.label())));
			for (ComponentType componentType : componentTypes) {
				System.out.println(componentType.classId() + ":" + componentType.id());
				for (Map<String, SystemContainer> scmap : componentType.containers()) {
					for (SystemContainer sc : scmap.values()) {
						System.out.println(sc.id());
						for (SystemComponent comp : sc.allItems()) {
							System.out.println(comp.classId() + ":" + comp.id());
							ReadOnlyPropertyList props = comp.readOnlyProperties();
							for (String key : props.getKeysAsSet())
								System.out.println(key + ":" + props.propertyToString(key));
							// there is no way of distinguishing between pars and vars at this level
							// we need some other way - how does the data tracker do it?
						}
					}
				}
				;

			}
		}
	}

}
