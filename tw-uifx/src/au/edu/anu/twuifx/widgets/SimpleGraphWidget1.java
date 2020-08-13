package au.edu.anu.twuifx.widgets;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.RuntimeGraphData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.Categorized;
import au.edu.anu.twcore.ecosystem.runtime.system.ArenaComponent;
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

		if (policy.canProcessDataMessage(data)) {
			Platform.runLater(() -> {
				ArenaComponent arena = data.getEcosystem().arena();
				// System.out.println(arena.toShortString());// TreeGraphDataNode
				ComponentContainer content = arena.content();
				//HierarchicalComponent hc = content.hierarchicalView(); // this is the arenaComponent
				
		
				System.out.println("C: "+content);
				for (SystemComponent sc: content.allItems()) {
					System.out.println(sc.id());
				};
				for (Category cat: content.itemCategorized().categories()) {
					System.out.println(cat.toShortString());
				} 


				// drawTree(updateData(data));
			});
		}

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
