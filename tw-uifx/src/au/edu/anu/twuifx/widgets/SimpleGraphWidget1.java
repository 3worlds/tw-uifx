package au.edu.anu.twuifx.widgets;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.RuntimeGraphData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.Categorized;
import au.edu.anu.twcore.ecosystem.runtime.system.ArenaComponent;
import au.edu.anu.twcore.ecosystem.runtime.system.CategorizedContainer;
import au.edu.anu.twcore.ecosystem.runtime.system.ComponentContainer;
import au.edu.anu.twcore.ecosystem.runtime.system.EcosystemGraph;
import au.edu.anu.twcore.ecosystem.runtime.system.HierarchicalComponent;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemComponent;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemRelation;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ecosystem.structure.Category;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.widgets.helpers.SimpleWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import fr.cnrs.iees.graph.Edge;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;

public class SimpleGraphWidget1 extends AbstractDisplayWidget<RuntimeGraphData, Metadata> implements WidgetGUI {
	private WidgetTrackingPolicy<TimeData> policy;
	private WidgetTimeFormatter timeFormatter;
	private String widgetId;

	public SimpleGraphWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.RUNTIMEGRAPH);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.widgetId = id;
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		timeFormatter.onMetaDataMessage(meta);
		System.out.println("onMetaDataMessage: " + meta);
	}

	@Override
	public void onDataMessage(RuntimeGraphData data) {
		System.out.println("onDataMessage: " + data);
		// an hierarchical component: as is a group component
		EcosystemGraph eg = data.getEcosystem();
		System.out.println("EcosystemGraph");

		int nNodes = 0;
		if (eg.nodes() != null)
			for (SystemComponent sc : eg.nodes()) 
				nNodes++;
			
		System.out.println("\tnNodes: " + nNodes);
		System.out.println("\tnEdges: " + eg.nEdges());

		ArenaComponent arena = eg.arena();

		System.out.println("Arena: " + arena);
		System.out.println("\t" + arena.membership().categoryId());

		if (arena.content() != null) {
			System.out.println("Arena content allItems");
			for (SystemComponent sc : arena.content().allItems()) {
				ComponentContainer container = sc.container();// TODO organise recursively
				if (container != null) {
					// System.out.println("\t" + container.containerCategorized().categoryId()); //
					// where can i find the ephemeral category that should be displayed here.
					System.out.println("\t" + container.id() + "->" + sc);// etc
					for (CategorizedContainer<SystemComponent> subc : container.subContainers()) {
						for (SystemComponent ssc : subc.allItems()) {
							System.out.println(
									"\t\t" + container.parentContainer().id() + "->" + container.id() + "->" + sc);
						}
					}
				} else
					System.out.println(sc);
			}
		}

		// what now - where are groups
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			// drawTree(true);
		}

	}

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane container = new BorderPane();
		return container;
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void putUserPreferences() {

	}

	@Override
	public void getUserPreferences() {
		System.out.println("getUserPreferences");

	}

}
