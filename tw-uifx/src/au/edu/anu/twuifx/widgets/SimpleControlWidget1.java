package au.edu.anu.twuifx.widgets;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorEvents.*;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineController;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineObserver;
import fr.ens.biologie.generic.utils.Logging;
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
public class SimpleControlWidget1 extends StateMachineController
		implements StateMachineObserver, DataReceiver<TimeData, Metadata>, Widget {

	private Button btnRunPause;
	private Button btnStep;
	private Button btnReset;
	private List<Button> buttons;
	private ImageView runGraphic;
	private ImageView pauseGraphic;

	private WidgetTrackingPolicy<TimeData> policy;
	// NB initial state is always 'waiting' ('null' causes a crash)
	private String state = waiting.name();

	private Label lblRealTime;
	private long startTime;
	private long idleTime;
	private long idleStartTime;

	private static Logger log = Logging.getLogger(SimpleControlWidget1.class);

	public SimpleControlWidget1(StateMachineEngine<StateMachineController> observed) {
		super(observed);
		log.info("Thread: " + Thread.currentThread().getId());
		policy = new SimpleWidgetTrackingPolicy();

		// RV for data messages
		addRendezvous(new RendezvousProcess() {
			@SuppressWarnings("unchecked")
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type() == DataMessageTypes.TIME) {
					TimeData data = (TimeData) message.payload();
					onDataMessage(data);
				}
			}
		}, DataMessageTypes.TIME);
		// RV for metadata messages
		addRendezvous(new RendezvousProcess() {
			@SuppressWarnings("unchecked")
			@Override
			public void execute(RVMessage message) {
				if (message.getMessageHeader().type() == DataMessageTypes.METADATA) {
					Metadata meta = (Metadata) message.payload();
					onMetaDataMessage(meta);
				}
			}
		}, DataMessageTypes.METADATA);

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
		pane.getChildren().addAll(new Label("Duration:"), lblRealTime, new Label("ms."));

		setButtonLogic();
		return pane;
	}

	private Object handleResetPressed() {
		log.info("Thread: " + Thread.currentThread().getId());

		// Always begin by disabling in case the next operation takes a long time
		// log.info("handleResetPressed Thread: " + Thread.currentThread().getId());
		setButtons(true, true, true, null);
		if (state.equals(pausing.name()) | state.equals(stepping.name()) | state.equals(finished.name())) {
			Platform.runLater(() -> {
				this.lblRealTime.setText("0");
			});
			sendEvent(reset.event());
		}
		return null;
	}

	private Object handleStepPressed() {
		long now = System.currentTimeMillis();
		if (state.equals(waiting.name())) {
			startTime = now;
			idleTime = 0;
		}
		log.info("Thread: " + Thread.currentThread().getId());
		setButtons(true, true, true, null);
		if (state.equals(pausing.name()) | state.equals(stepping.name()) | state.equals(waiting.name())) {
			if (idleStartTime > 0)
				idleTime += (now - idleStartTime);
			sendEvent(step.event());
		}
		return null;
	}

	private Object handleRunPausePressed() {
		long now = System.currentTimeMillis();
		log.info("Thread: " + Thread.currentThread().getId());
		setButtons(true, true, true, null);
		Event event = null;
		if (state.equals(waiting.name())) {
			startTime = now;
			idleTime = 0;
			event = run.event();
		} else if (state.equals(running.name())) {
			event = pause.event();
		} else if (state.equals(pausing.name()) | state.equals(stepping.name())) {
			// total idleTime here
			if (idleStartTime > 0)
				idleTime += (now - idleStartTime);
			event = goOn.event();
		}
		if (event != null)
			sendEvent(event);
		return null;
	}

	private String getDuration(long now) {
		long duration = now - (startTime + idleTime);
		String result = Long.toString(duration);
		return result;
	}

	@Override
	public void onStatusMessage(State newState) {
		log.info("Thread: " + Thread.currentThread().getId() + " State: " + newState);
		final long now = System.currentTimeMillis();
		state = newState.getName();
		if (state.equals(finished.name())) {
			final String strDuration = getDuration(now);
			Platform.runLater(() -> {
				lblRealTime.setText(strDuration);
			});
		}
		if (state.equals(stepping.name())) {
			idleStartTime = now;
		}
		if (state.equals(pausing.name())) {
			idleStartTime = now;
		}
		if (state.equals(waiting.name())) {
			idleTime = 0;
			idleStartTime = 0;
			Platform.runLater(() -> {
				lblRealTime.setText("0");
			});
		}
		setButtonLogic();
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
		if (policy.canProcessDataMessage(data)) {
			final String strDur = getDuration(System.currentTimeMillis());
			Platform.runLater(() -> {
				lblRealTime.setText(strDur);
			});
		}
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// TODO Auto-generated method stub

	}

}
