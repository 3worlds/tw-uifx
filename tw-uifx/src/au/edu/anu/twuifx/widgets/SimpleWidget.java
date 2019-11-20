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

import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.logging.Logger;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.ObjectData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 3 Sep 2019
 */

/*
 * The policy of simple widgets is:
 * 
 * 1) to follow just one sender (currentSender). That's it! The chosen sender is
 * a sub-archetype property. Therefore, these widgets will ignore data from
 * other senders.
 * 
 * Each widget should indicate the sender int on the ui.
 * 
 * 2) To receive a time value from the time model driving the dataTracker, or in
 * the case of SimpleTimeWidget, a Simulator instance.
 * 
 */
public class SimpleWidget extends AbstractDisplayWidget<ObjectData, Metadata> implements Widget {
	private static Logger log = Logging.getLogger(SimpleWidget.class);

	private Label lblOutput;

	//private Object initialValue;
	private WidgetTimeFormatter timeFormatter;
	// private int sender;
	private String name;
	private WidgetTrackingPolicy<TimeData> policy;

	public SimpleWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.VALUE_PAIR);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
	}

	@Override
	public void onDataMessage(ObjectData data) {
		log.info("Data received " + data);
		if (policy.canProcessDataMessage(data))
			Platform.runLater(() -> {
				processOnDataMessage(data);
			});
	}

	private void processOnDataMessage(ObjectData data) {
		log.info("Data processing " + data);
		lblOutput.setText(getOutputString(data.sender(), timeFormatter.getTimeText(data.time()), name, data.text()));
	}

	private String getOutputString(int sender, String time, String name, String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("Sender: ").append(sender).append(" Time: ").append(time).append(" ").append(name).append("=")
				.append(text);
		return sb.toString();
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		log.info("Meta-data received " + meta);
		timeFormatter.onMetaDataMessage(meta);
//		DataLabel  dl = (DataLabel) meta.properties().getPropertyValue("name");
//		name = dl.toString();
//		initialValue = meta.properties().getPropertyValue("value");
		lblOutput.setText("uninitialised until data msg received");
	}

	@Override
	public void onStatusMessage(State state) {
		log.info("Status msg received:" + state);
		if (isSimulatorState(state, waiting)) {
			processWaitState();
		}
	}

	private void processWaitState() {
		log.info("Would like to reset to initial value but we don't know what it is!");
//		lblOutput.setText(getOutputString(currentSender, timeFormatter.getTimeText(timeFormatter.getInitialTime()),
//				name, initialValue.toString()));
	}

	@Override
	public Object getUserInterfaceContainer() {
		log.info("Prepared user interface");
		HBox content = new HBox();
		lblOutput = new Label("uninitialized");
		content.setPadding(new Insets(5, 1, 1, 2));
		content.setSpacing(5);
		content.getChildren().addAll(lblOutput);
		log.info("User interface built");
		return content;
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void putPreferences() {
		timeFormatter.putPreferences();
	}

	@Override
	public void getPreferences() {
		timeFormatter.getPreferences();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		timeFormatter.setProperties(id, properties);
		policy.setProperties(id, properties);
	}

}
