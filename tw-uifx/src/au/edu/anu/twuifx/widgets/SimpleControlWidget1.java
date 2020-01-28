package au.edu.anu.twuifx.widgets;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.*;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.DataReceiver;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.twuifx.images.Images;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.rendezvous.RVMessage;
import fr.cnrs.iees.rvgrid.rendezvous.RendezvousProcess;
import fr.cnrs.iees.rvgrid.statemachine.Event;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineObserver;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * @author Ian Davies
 *
 * @date 29 Jan 2020
 */
public class SimpleControlWidget1  extends StateMachineController implements StateMachineObserver,DataReceiver<TimeData, Metadata>,Widget{
	private Button btnRunPause;
	private Button btnStep;
	private Button btnReset;
	private List<Button> buttons;
	private ImageView runGraphic;
	private ImageView pauseGraphic;
	private Label lblRealTime;
	private long startTime;
	private WidgetTrackingPolicy<TimeData> policy;

	// NB initial state is always 'waiting' ('null' causes a crash)
	private String state = waiting.name();

	public SimpleControlWidget1(StateMachineEngine<StateMachineController> observed) {
		super(observed);
		policy = new SimpleWidgetTrackingPolicy();

		// RV for data messages
		addRendezvous(new RendezvousProcess() {
			@SuppressWarnings("unchecked")
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type()==DataMessageTypes.TIME) {
					TimeData data = (TimeData) message.payload();
					onDataMessage(data);
				}
			}
		},DataMessageTypes.TIME);
		// RV for metadata messages
		addRendezvous(new RendezvousProcess() {
			@SuppressWarnings("unchecked")
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type()==DataMessageTypes.METADATA) {
					Metadata meta = (Metadata) message.payload();
					onMetaDataMessage(meta);
				}
			}
		},DataMessageTypes.METADATA);

	}

	

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void putPreferences() {
		policy.putPreferences();
	}

	@Override
	public void getPreferences() {
		policy.getPreferences();
	}

	@Override
	public Object getUserInterfaceContainer() {
		// log.info("Thread: " + Thread.currentThread().getId());
		runGraphic = new ImageView(new Image(Images.class.getResourceAsStream("Play16.gif")));
		pauseGraphic = new ImageView(new Image(Images.class.getResourceAsStream("Pause16.gif")));
		btnRunPause = new Button("", runGraphic);
		btnRunPause.setTooltip(new Tooltip("Run/Pause simulation"));
		btnStep = new Button("", new ImageView(new Image(Images.class.getResourceAsStream("StepForward16.gif"))));
		btnStep.setTooltip(new Tooltip("Step forward one timer event"));
		btnReset = new Button("", new ImageView(new Image(Images.class.getResourceAsStream("Stop16.gif"))));
		btnReset.setTooltip(new Tooltip("Reset to start"));
		btnReset.setDisable(true);

		buttons = new ArrayList<>();
		buttons.add(btnRunPause);
		buttons.add(btnStep);
		buttons.add(btnReset);

		btnRunPause.setOnAction(e -> handleRunPausePressed());
		btnStep.setOnAction(e -> handleStepPressed());
		btnReset.setOnAction(e -> handleResetPressed());

		HBox pane = new HBox();
		pane.setAlignment(Pos.BASELINE_LEFT);
		pane.getChildren().addAll(buttons);
		lblRealTime = new Label("0");
	
		pane.setSpacing(5.0);
		pane.getChildren().addAll(new Label("Duration:"),lblRealTime,new Label("[ms]"));

		setButtonLogic();
		return pane;
	}
	private Object handleResetPressed() {
		// Always begin by disabling in case the next operation takes a long time
		// log.info("handleResetPressed Thread: " + Thread.currentThread().getId());
		setButtons(true, true, true, null);
		if (state.equals(pausing.name()) | state.equals(stepping.name()) | state.equals(finished.name()))
			sendEvent(reset.event());
		return null;
	}

	private Object handleStepPressed() {
		// log.info("handleStepPressed Thread: " + Thread.currentThread().getId());
		setButtons(true, true, true, null);
		if (state.equals(pausing.name()) | state.equals(stepping.name()) | state.equals(waiting.name()))
			sendEvent(step.event());
		return null;
	}

	private Object handleRunPausePressed() {
		// log.info("handleRunPausePressed Thread: " + Thread.currentThread().getId());
		setButtons(true, true, true, null);
		Event event = null;
		if (state.equals(waiting.name()))
			event = run.event();
		else if (state.equals(running.name()))
			event = pause.event();
		else if (state.equals(pausing.name()) | state.equals(stepping.name()))
			event = goOn.event();
		if (event != null)
			sendEvent(event);
		return null;
	}
	private void setButtonLogic() {
		// ensure waiting for app thread i.e. only needed when 'running'
		Platform.runLater(() -> {
			// log.info("setButtonLogic: State: "+ state+", Thread: " +
			// Thread.currentThread().getId());
			if (state.equals(waiting.name())) {
				setButtons(false, false, true, runGraphic);
				return;
			}
			if (state.equals(running.name())) {
				setButtons(false, true, true, pauseGraphic);
				return;

			}
			if (state.equals(stepping.name())) {
				setButtons(false, false, false, runGraphic);
				return;
			}
			if (state.equals(finished.name())) {
				setButtons(true, true, false, runGraphic);
				return;
			}
			if (state.equals(pausing.name())) {
				setButtons(false, false, false, runGraphic);
				return;
			}
		});
	}
	private void setButtons(boolean runPauseDisable, boolean stepDisable, boolean resetDisable, ImageView iv) {
		btnRunPause.setDisable(runPauseDisable);
		btnStep.setDisable(stepDisable);
		btnReset.setDisable(resetDisable);
		if (iv != null)
			btnRunPause.setGraphic(iv);
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void onDataMessage(TimeData data) {
		System.out.println("RECORDING");
		long now = System.currentTimeMillis();
		final Long duration = now - startTime;
		Platform.runLater(() -> {
			lblRealTime.setText(duration.toString());
		});

	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// TODO Auto-generated method stub
		
	}

}
