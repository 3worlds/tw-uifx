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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import au.edu.anu.twcore.data.runtime.DataMessageTypes;
import au.edu.anu.twcore.data.runtime.LabelValuePairData;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.ecosystem.runtime.timer.TimeUtil;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.TimeScaleType;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.ens.biologie.generic.utils.Duple;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */

public class TimeDisplayWidget 
		extends AbstractDisplayWidget<LabelValuePairData,Metadata> 
		implements Widget {

	private boolean metadataReceived = false;
	private TimeUnits smallest;
	private TimeUnits largest;
	private TimeScaleType timeScale;
	private List<TimeUnits> units;
	private Long startTime;

	private Label lblTimeSlowest;
	private Label lblTimeFastest;

	private Map<String, Long> simTimes;
	private static Logger log = Logging.getLogger(TimeDisplayWidget.class);
	/*
	 * TODO: The key of the data msg should be SimulatorNode.id():<instance count>
	 * e.g. myDynamics(3439). This way, in a swarm time display, we can provide
	 * meaningful labels to the data series.
	 * 
	 * Also all widgets need to be able to respond to a reset status from the
	 * simulator so they can clear their fields. But this can't happen before
	 * receiving the meta-data msg
	 * 
	 */

	public TimeDisplayWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME);
		simTimes = new HashMap<>();
		log.info("Constructor");
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		log.info("meta-data for TimeDisplayWidget: " + meta.toString());
		smallest = (TimeUnits) meta.properties().getPropertyValue(P_TIMELINE_SHORTTU.key());
		largest = (TimeUnits) meta.properties().getPropertyValue(P_TIMELINE_LONGTU.key());
		timeScale = (TimeScaleType) meta.properties().getPropertyValue(P_TIMELINE_SCALE.key());
		startTime = (Long) meta.properties().getPropertyValue(P_TIMELINE_TIMEORIGIN.key());
		units = new ArrayList<>();
		Set<TimeUnits> allowable = TimeScaleType.validTimeUnits(timeScale);
		for (TimeUnits allowed : allowable)
			if (allowed.compareTo(largest) <= 0 && allowed.compareTo(smallest) >= 0)
				units.add(allowed);

		units.sort((first, second) -> {
			return second.compareTo(first);
		});

		metadataReceived = true;
		log.info("metadata received");
	}

	@Override
	public void onDataMessage(LabelValuePairData data) {
		log.info("data msg received");
		if (metadataReceived) {
			simTimes.put(data.label().getEnd(), (Long)data.value());
			Duple<Long, Long> times = getTimes();
			updateControls(times.getFirst(), times.getSecond());
		} else
			log.severe("Missed data. Data msg received before widget has received meta-data. " + data.toString());
	}

	private Duple<Long, Long> getTimes() {
		Long min = Long.MAX_VALUE;
		Long max = Long.MIN_VALUE;
		for (Long time : simTimes.values()) {
			min = Math.min(min, time);
			max = Math.max(max, time);
		}
		return new Duple<Long, Long>(min, max);
	}

	private String getLabelText(Long time) {
		if (timeScale.equals(TimeScaleType.GREGORIAN)) {
			LocalDateTime presentDate = TimeUtil.longToDate(time, smallest);
			return presentDate.format(TimeUtil.getGregorianFormat(smallest));
		} else {
			return (TimeUtil.formatExactTimeScales(time, units));
		}
	}

	private void updateControls(Long slowest, Long fastest) {
		String ss = getLabelText(slowest);
		String sf = getLabelText(fastest);
		Platform.runLater(() -> {
			updateControls0(ss, sf);
		});
	}

	private void updateControls0(String slowest, String fastest) {
		lblTimeSlowest.setText(slowest);
		if (slowest.equals(fastest))
			lblTimeFastest.setText("");
		else
			lblTimeFastest.setText(fastest);
		log.info("Updating ui labels");
	}

	@Override
	public Object getUserInterfaceContainer() {
		/*
		 * This is only called in application thread so it is only here that you can
		 * construct fx stuff
		 */
		log.info("getUserInterfaceContainer");
		HBox content = new HBox();
		// top, right, bottom, and left padding
		content.setPadding(new Insets(5, 1, 1, 2));
		content.setSpacing(5);
		lblTimeSlowest = new Label();
		lblTimeFastest = new Label();
		content.getChildren().addAll(lblTimeSlowest, lblTimeFastest);
		updateControls0(getLabelText(startTime), getLabelText(startTime));
		return content;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		log.info("setProperties");
	}

	@Override
	public Object getMenuContainer() {
		log.info("getMenuContainer");
		return null;
	}

	@Override
	public void putPreferences() {
		log.info("putPreferences");
	}

	@Override
	public void getPreferences() {
		log.info("getPreferences");
	}

	@Override
	public void onStatusMessage(State state) {
		log.info("Status message received: " + state);
		if (state.equals(waiting.state())) {
			simTimes.clear();
			updateControls(startTime, startTime);
		}

	}
}
