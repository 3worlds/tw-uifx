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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.widgets.helpers.SimpleWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */

public class SimpleTimeWidget extends AbstractDisplayWidget<TimeData, Metadata> implements WidgetGUI {
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;
	private Label lblTime;
	private String scText;
	private static Logger log = Logging.getLogger(SimpleTimeWidget.class);
	private final List<TimeData> initialData;

	public SimpleTimeWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		log.info("Thread id: " + Thread.currentThread().getId());
		initialData = new ArrayList<>();
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		log.info("Thread id: " + Thread.currentThread().getId());
		timeFormatter.onMetaDataMessage(meta);
		scText = "Stop when: " + meta.properties().getPropertyValue("StoppingDesc");
	}

	@Override
	public void onDataMessage(TimeData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial))
				initialData.add(data);
			else
				processDataMessage(data);
		}
	}

	private void processDataMessage(TimeData data) {
		Platform.runLater(() -> {
			log.info("Thread id: " + Thread.currentThread().getId());
			lblTime.setText(formatOutput(data.sender(),data.time()));
		});
	}

	@Override
	public Object getUserInterfaceContainer() {
		log.info("Thread id: " + Thread.currentThread().getId());
		HBox content = new HBox();
		content.setAlignment(Pos.BASELINE_LEFT);
		// content.setPadding(new Insets(4, 1, 1, 2));
		lblTime = new Label("");
		content.getChildren().addAll(new Label("Simulator time: "), lblTime, new Label(scText));

		getUserPreferences();

		return content;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		log.info("Thread id: " + Thread.currentThread().getId());
		timeFormatter.setProperties(id, properties);
		policy.setProperties(id, properties);
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void putUserPreferences() {
		// timeFormatter.putPreferences();
		// policy.putPreferences();
	}

	@Override
	public void getUserPreferences() {
//		timeFormatter.getPreferences();
//		policy.getPreferences();
	}

	@Override
	public void onStatusMessage(State state) {
		log.info("Thread id: " + Thread.currentThread().getId() + " State: " + state);
		if (isSimulatorState(state, waiting)) {
			for (TimeData data:initialData)
				processDataMessage(data);
		}
	}


	private String formatOutput(int sender, long time) {
		return "[#" + sender + "] " + timeFormatter.getTimeText(time);
	}

}
