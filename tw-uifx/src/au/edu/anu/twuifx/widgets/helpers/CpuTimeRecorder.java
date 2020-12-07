package au.edu.anu.twuifx.widgets.helpers;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.goOn;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.pause;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.reset;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.run;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.step;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.pausing;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.running;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.stepping;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;
import static au.edu.anu.twcore.ui.runtime.StatusWidget.isSimulatorState;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.DataReceiver;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.rendezvous.RVMessage;
import fr.cnrs.iees.rvgrid.rendezvous.RendezvousProcess;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineObserver;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class CpuTimeRecorder  extends StateMachineController
implements StateMachineObserver, DataReceiver<TimeData, Metadata>, WidgetGUI {
	private long startTime;
	private long idleTime;
	private long idleStartTime;
	private Button btnRunPause;
	private Button btnStep;
	private Button btnReset;
	private Label lblRealTime;
	public CpuTimeRecorder(StateMachineEngine<StateMachineController> observed) {
		super(observed);
//		new SimpleWidgetTrackingPolicy();

		// Data messages
		addRendezvous(new RendezvousProcess() {
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type() == DataMessageTypes.TIME) {
					TimeData data = (TimeData) message.payload();
					onDataMessage(data);
				}
			}
		}, DataMessageTypes.TIME);
		// Metadata messages
		addRendezvous(new RendezvousProcess() {
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type() == DataMessageTypes.METADATA) {
					Metadata meta = (Metadata) message.payload();
					onMetaDataMessage(meta);
				}
			}
		}, DataMessageTypes.METADATA);
	}
	protected void handleResetPressed() {
		nullButtons();
		sendEvent(reset.event());
		Platform.runLater(() -> {
			this.lblRealTime.setText("0");
		});
	}

	protected void handleRunPausePressed() {
		long now = System.currentTimeMillis();
		nullButtons();
		State state = stateMachine().getCurrentState();

		if (isSimulatorState(state, waiting)) {
			startTime = now;
			idleTime = 0;
			sendEvent(run.event());
		} else if (isSimulatorState(state, running)) {
			sendEvent(pause.event());
		} else if (isSimulatorState(state, pausing) || isSimulatorState(state, stepping)) {
			// total idleTime here
			if (idleStartTime > 0)
				idleTime += (now - idleStartTime);
			sendEvent(goOn.event());
		}
	}
	
	protected void handleStepPressed() {
		long now = System.currentTimeMillis();
		State state = stateMachine().getCurrentState();
		nullButtons();
		if (isSimulatorState(state, waiting)) {
			startTime = now;
			idleTime = 0;
			sendEvent(step.event());
		} else if (isSimulatorState(state, pausing) | isSimulatorState(state, stepping)) {
			if (idleStartTime > 0)
				idleTime += (now - idleStartTime);
			sendEvent(step.event());
		}
	}
	protected final long getDuration(long now) {
		return now - (startTime + idleTime);
	}

	protected final void nullButtons() {
		setButtons(true, true, true, null);
	}

	protected final void setButtons(boolean runPauseDisable, boolean stepDisable, boolean resetDisable, ImageView iv) {
		btnRunPause.setDisable(runPauseDisable);
		btnStep.setDisable(stepDisable);
		btnReset.setDisable(resetDisable);
		if (iv != null)
			btnRunPause.setGraphic(iv);
	}

	@Override
	public void onStatusMessage(State state) {
		
	}
	@Override
	public void setProperties(String id, SimplePropertyList properties) {
	}

	@Override
	public Object getUserInterfaceContainer() {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public void onDataMessage(TimeData data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// TODO Auto-generated method stub
		
	}

}
