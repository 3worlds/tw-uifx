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
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import au.edu.anu.twcore.data.runtime.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;



/**
 * @author Ian Davies
 *
 * @date 11 Sep 2019
 */
public class SimpleTimeSeriesWidget extends AbstractDisplayWidget<SimplePropertyList, SimplePropertyList> implements Widget {

	public SimpleTimeSeriesWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.VALUE_PAIR);
		simData = new HashMap<>();
	}

	private String name = "uninitialised name";
	private String units = "?";
	private String timeUnits = "?";
	private Number initialValue = (Double)0.0;
	private Number value = initialValue;
	private Map<Integer, Long> simData;

	private static Logger log = Logging.getLogger(SimpleTimeSeriesWidget.class);


	@Override
	public void onMetaDataMessage(SimplePropertyList meta) {
		log.info("Meta-data received " + meta);
		name = (String) meta.getPropertyValue("name");
		initialValue = (Number) meta.getPropertyValue("value");
		value = initialValue;
		updateLabel();
	}

	@Override
	public void onDataMessage(SimplePropertyList data) {
		// of the attached simulator (dont forget there could be many simulators)
		log.info("Data received " + data);
		int simId = (Integer)data.getPropertyValue("simId");
		value = (Number) data.getPropertyValue("value");
		long time =(Long)data.getPropertyValue("time");
		updateLabel();
	}
	@Override
	public void onStatusMessage(State state) {
		log.info("Status msg received:" + state);
		if (isSimulatorState(state, waiting)) {
			log.info("Resetting initial value: " + initialValue + "," + state);
			value = initialValue;
			updateLabel();
		}

	}

	@Override
	public Object getUserInterfaceContainer() {
		log.info("Prepared user interface");
		HBox content = new HBox();
//		lblName = new Label(name);
//		lblValue = new Label();
		content.setPadding(new Insets(5, 1, 1, 2));
		content.setSpacing(5);
//		content.getChildren().addAll(lblName, lblValue);
		updateLabel0();
		log.info("User interface built");
		return content;
	}

	private void updateLabel0() {
		log.info(value.toString());
//		lblValue.setText(value.toString());
	}

	private void updateLabel() {
		Platform.runLater(() -> {
			updateLabel0();
		});
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

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
	}

}