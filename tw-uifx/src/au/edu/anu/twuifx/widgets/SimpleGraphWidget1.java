package au.edu.anu.twuifx.widgets;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.RuntimeTreeData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.widgets.helpers.SimpleWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;

public class SimpleGraphWidget1 extends AbstractDisplayWidget<RuntimeTreeData,Metadata> implements WidgetGUI{
	private WidgetTrackingPolicy<TimeData> policy;
	private WidgetTimeFormatter timeFormatter;
	private String widgetId;
	
	public SimpleGraphWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super (statusSender, DataMessageTypes.RTTree);
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
	}

	@Override
	public void onDataMessage(RuntimeTreeData data) {
		if (policy.canProcessDataMessage(data)) {
			Platform.runLater(() -> {
				//drawTree(updateData(data));
			});
		}
		
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			//drawTree(true);
		}
	
	}

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane container = new BorderPane();
		return container;
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putUserPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getUserPreferences() {
		// TODO Auto-generated method stub
		
	}

}
