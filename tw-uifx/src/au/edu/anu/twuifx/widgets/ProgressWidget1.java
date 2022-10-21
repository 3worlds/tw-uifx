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
package au.edu.anu.twuifx.widgets;

import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import au.edu.anu.twcore.data.runtime.*;
import au.edu.anu.twcore.ecosystem.runtime.simulator.Simulator;
import au.edu.anu.twcore.ecosystem.runtime.*;
import au.edu.anu.twcore.ecosystem.runtime.tracking.AbstractDataTracker;
import au.edu.anu.twcore.ui.runtime.*;
import au.edu.anu.twuifx.widgets.helpers.*;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.*;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

//Tested to 1,000,000 simulators.
/**
 * 
 * A {@link WidgetGUI} to display the average time of all currently running
 * simulators.
 * <p>
 * 
 * <img src="{@docRoot}/../doc/images/ProgressWidget1.png" width="400" alt=
 * "ProgressWidget1"/>
 * </p>
 * <p>
 * This widget also displays the:
 * <li>number of {@link Simulator}s;</li>
 * <li>{@link StoppingCondition} - if used.</li>
 * </p>
 * <img src="{@docRoot}/../doc/images/ProgressWidget1.png" width="400" alt=
 * "ProgressWidget1"/>
 * </p>
 * <p>
 * It rendezvous with messages of type {@link DataMessageTypes#TIME} containing
 * {@link TimeData} at a rate depending on the sending {@link Simulator}.
 * </p>
 * <p>
 * The current time of every simulator is recorded in a collection. A java timer
 * determines how often an average value from this collection is calculated
 * (controlled by the widget refresh rate - default 250 ms). If the average time
 * has changed, the display is updated.
 * 
 * @author Ian Davies - 5 Dec. 2020
 * 
 */
public class ProgressWidget1 extends AbstractDisplayWidget<TimeData, Metadata> implements WidgetGUI {
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;
	private Label lblTime;
	private String scText;
	private final List<TimeData> initialData;
	private final Map<Integer, Long> currentSenderTimes;
	private long refreshRate;// ms
	private Timer timer;

	/**
	 * @param statusSender The {@link StatusWidget}.
	 */
	public ProgressWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, AbstractDataTracker.TIME);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		initialData = new ArrayList<>();
		currentSenderTimes = new ConcurrentHashMap<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		timeFormatter.setProperties(id, properties);
		policy.setProperties(id, properties);
		refreshRate = 250;
		if (properties.hasProperty(P_WIDGET_REFRESHRATE.key()))
			refreshRate = (Long) properties.getPropertyValue(P_WIDGET_REFRESHRATE.key());
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		if (policy.canProcessMetadataMessage(meta)) {
			timeFormatter.onMetaDataMessage(meta);
			scText = "Stop when: " + meta.properties().getPropertyValue("StoppingDesc");
			timeFormatter.getInitialTime();
		}
	}

	@Override
	public void onDataMessage(TimeData data) {
		// Lazy init to avoid a time out while many simulators are loading
		if (timer == null) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				private long lastTime = Long.MAX_VALUE;

				@Override
				public void run() {
					long time = Math.round(getMeanTime());
					if (time != lastTime) {
						lastTime = time;
						String text = formatOutput(currentSenderTimes.size(), time);
						Platform.runLater(() -> {
							lblTime.setText(text);
						});
					}
				}
			}, 0, refreshRate);

		}

		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial))
				initialData.add(data);
			else
				processDataMessage(data);
		}
	}

	private void processDataMessage(TimeData data) {
		currentSenderTimes.put(data.sender(), data.time());
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {

			currentSenderTimes.clear();

			for (TimeData data : initialData)
				processDataMessage(data);

			initialData.clear();
		}
	}

	@Override
	public Object getUserInterfaceContainer() {
		getPreferences();//?

		HBox content = new HBox(5);
		content.setAlignment(Pos.BASELINE_LEFT);
		lblTime = new Label("");
		content.getChildren().addAll(new Label("Simulator time (mean): "), lblTime, new Label(scText));
		return content;
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void putPreferences() {
	}

	@Override
	public void getPreferences() {
	}

	private double getMeanTime() {
		double sum = 0;
		double n = currentSenderTimes.size();
		for (Entry<Integer, Long> entry : currentSenderTimes.entrySet())
			sum += entry.getValue();
		if (n > 0)
			return sum / n;
		return timeFormatter.getInitialTime();
	}

	private String formatOutput(int n, long time) {
		return "[#1-" + n + "] " + timeFormatter.getTimeText(time);
	}

}
