package au.edu.anu.twuifx.mr.view;

import au.edu.anu.twcore.data.runtime.TwData;
import au.edu.anu.twcore.ecosystem.runtime.Categorized;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemComponent;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemContainer;
import au.edu.anu.twcore.ecosystem.structure.Category;
import au.edu.anu.twcore.ecosystem.structure.ComponentType;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.identity.Identity;
import fr.cnrs.iees.properties.ReadOnlyPropertyList;

import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;

public class RunTimeData {
	public static void listStuff(TreeGraph<TreeGraphDataNode, ALEdge> graph) {
		List<TreeGraphDataNode> systems = (List<TreeGraphDataNode>) get(graph.root().getChildren(),
				selectOneOrMany(hasTheLabel(N_SYSTEM.label())));

		for (TreeGraphDataNode system : systems) {
			TreeGraphDataNode structure = (TreeGraphDataNode) get(system.getChildren(), selectOne(hasTheLabel(N_STRUCTURE.label())));
			List<ComponentType> componentTypes = (List<ComponentType>) get(structure.getChildren(),
					selectOneOrMany(hasTheLabel(N_COMPONENTTYPE.label())));
			for (ComponentType componentType : componentTypes) {
				System.out.println(componentType.classId()+":"+componentType.id());
				for (Map<String, SystemContainer> scmap:componentType.containers()) {
					for (SystemContainer sc:scmap.values()){
						System.out.println(sc.id());
						for (SystemComponent comp:sc.allItems()) {
							System.out.println(comp.classId()+":"+comp.id());
							ReadOnlyPropertyList props = comp.readOnlyProperties();
							for (String key:props.getKeysAsSet())
								System.out.println(key+":"+props.propertyToString(key));
						}
					}
				};
				

			}
		}
		// 1) Get all systems
		// 2) get all structures
		// 3) get all componentTypes
		// 4) get all containers
		// 5) lifecycle stuff??
		// 6) factories??
		/*-
		 * 	public SystemContainer(Categorized<SystemComponent> cats, 
				String proposedId, 
				SystemContainer parent,
				TwData parameters,
				TwData variables) {
			super(cats,proposedId,parent,parameters,variables);
		}
		CategorizedContainer<T extends Identity>
		 */
	}

}
