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

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * @author Ian Davies
 *
 * @date 25 Jan 2020
 */
public class SimplePerformanceNumericWidget extends AbstractDisplayWidget<TimeData, Metadata> implements Widget {
	private WidgetTrackingPolicy<TimeData> policy;
	private Label lblTime;
	private long startTime;

	public SimplePerformanceNumericWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME);
		policy = new SimpleWidgetTrackingPolicy();
	}
	@Override
	public void onMetaDataMessage(Metadata meta) {
		System.out.println("STARTING");
		long now = System.currentTimeMillis();
		startTime=now;
	}

	@Override
	public void onDataMessage(TimeData data) {
		System.out.println("RECORDING");
		long now = System.currentTimeMillis();
		final Long duration = now - startTime;
		Platform.runLater(() -> {
			lblTime.setText(duration.toString());
		});
	}


	@Override
	public void onStatusMessage(State state) {
			if (isSimulatorState(state, finished)) {
			System.out.println("FINISHED");
			long now = System.currentTimeMillis();
			final Long duration = now - startTime ;
			Platform.runLater(() -> {
				lblTime.setText(duration.toString());
			});
		}
	}

	@Override
	public Object getUserInterfaceContainer() {
		HBox content = new HBox();
		content.setAlignment(Pos.BASELINE_LEFT);
		content.setSpacing(5);
		lblTime = new Label("0");
		content.getChildren().addAll(new Label("Duration:"), lblTime, new Label("ms."));
		return content;
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		policy.setProperties(id, properties);
	}

	@Override
	public void putPreferences() {
		policy.putPreferences();
	}

	@Override
	public void getPreferences() {
		policy.getPreferences();
	}

}
