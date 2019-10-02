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

import java.util.Map;
import java.util.logging.Logger;

import au.edu.anu.twcore.data.runtime.DataMessageTypes;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */

public class SimpleTimeBracketWidget extends AbstractDisplayWidget<TimeData, Metadata> implements Widget {

	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;

	private Label lblTimeSlowest;
	private Label lblTimeFastest;

	private Map<String, Long> simTimes;
	private static Logger log = Logging.getLogger(SimpleTimeBracketWidget.class);

	public SimpleTimeBracketWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME);
		timeFormatter = new WidgetTimeFormatter();
		policy = new BracketWidgetTrackingPolicy();

		log.info("Thread id: " + Thread.currentThread().getId());
	}

	// why all this? Dont we just need the Timer's Timeunits and multiple?
	@Override
	public void onMetaDataMessage(Metadata meta) {
		log.info("Thread id: " + Thread.currentThread().getId());
		timeFormatter.onMetaDataMessage(meta);
	}

	@Override
	public void onDataMessage(TimeData data) {
		if (policy.canProcessDataMessage(data)) {
			Platform.runLater(() -> {
				processOnDataMessage(data);
			});
		}
	}

	private void processOnDataMessage(TimeData data) {
		log.info("Thread id: " + Thread.currentThread().getId());
//		simTimes.put(data.label().getEnd(), (Long) data.value());
//		Duple<Long, Long> times = getTimes();
//		updateControls(getLabelText(times.getFirst()), getLabelText(times.getSecond()));
	}

//	private Duple<Long, Long> getTimes() {
//		Long min = Long.MAX_VALUE;
//		Long max = Long.MIN_VALUE;
//		for (Long time : simTimes.values()) {
//			min = Math.min(min, time);
//			max = Math.max(max, time);
//		}
//		return new Duple<Long, Long>(min, max);
//	}


//	private void updateControls(String slowest, String fastest) {
//		lblTimeSlowest.setText(slowest);
//		if (slowest.equals(fastest))
//			lblTimeFastest.setText("");
//		else
//			lblTimeFastest.setText(fastest);
//		log.info("Thread id: " + Thread.currentThread().getId());
//	}
//
	@Override
	public Object getUserInterfaceContainer() {
		log.info("Thread id: " + Thread.currentThread().getId());
		HBox content = new HBox();
		// top, right, bottom, and left padding
		content.setPadding(new Insets(5, 1, 1, 2));
		content.setSpacing(5);
		lblTimeSlowest = new Label();
		lblTimeFastest = new Label();
		content.getChildren().addAll(lblTimeSlowest, lblTimeFastest);
//		updateControls(getLabelText(startTime), getLabelText(startTime));
		return content;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		log.info("Thread id: " + Thread.currentThread().getId());
	}

	@Override
	public Object getMenuContainer() {
		log.info("Thread id: " + Thread.currentThread().getId());
		return null;
	}

	@Override
	public void putPreferences() {
		log.info("Thread id: " + Thread.currentThread().getId());
	}

	@Override
	public void getPreferences() {
		log.info("Thread id: " + Thread.currentThread().getId());
	}

	@Override
	public void onStatusMessage(State state) {
		log.info("Thread id: " + Thread.currentThread().getId() + " State: " + state);
		if (isSimulatorState(state, waiting)) {
			log.info("Waiting: Thread id: " + Thread.currentThread().getId());
			simTimes.clear();
//			updateControls(getLabelText(startTime), getLabelText(startTime));
		}

	}
}
