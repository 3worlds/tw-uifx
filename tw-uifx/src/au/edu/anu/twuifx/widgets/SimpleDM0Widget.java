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

import java.util.logging.Logger;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.data.runtime.TimeSeriesData;
import au.edu.anu.twcore.data.runtime.TimeSeriesMetadata;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;

/**
 * @author Ian Davies
 *
 * @date 6 Dec 2019
 */
/**
 * Displays a table of data - largely for debugging. This replaces label/value
 * pair widget
 */
public class SimpleDM0Widget extends AbstractDisplayWidget<TimeSeriesData, Metadata> implements Widget {
	private TimeSeriesMetadata tsmeta;
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;
	private static Logger log = Logging.getLogger(SimpleDM0Widget.class);
	private TableView table;
	private Label lblTime;

	public SimpleDM0Widget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME_SERIES);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		log.info("Thread: " + Thread.currentThread().getId());
	}

	@Override
	public void onDataMessage(TimeSeriesData data) {
		log.info("Thread: " + Thread.currentThread().getId() + " data: " + data);
		if (policy.canProcessDataMessage(data)) {
			Platform.runLater(() -> {
				lblTime.setText(timeFormatter.getTimeText(data.time()));
			});
		}
	}

	private boolean initialMessage = false;

	private ObservableList<LabelValue> data;
	@Override
	public void onMetaDataMessage(Metadata meta) {
		log.info("Thread: " + Thread.currentThread().getId() + " Meta-data: " + meta);
		// clear the table - probably in a ui thread!
		if (!initialMessage) {
			tsmeta = (TimeSeriesMetadata) meta.properties().getPropertyValue(TimeSeriesMetadata.TSMETA);
			Platform.runLater(() -> {
				timeFormatter.onMetaDataMessage(meta);
				data =FXCollections.observableArrayList(
			            new LabelValue("x", "0"),
			            new LabelValue("y", "0")
				        );
				table.setItems(data);
				initialMessage = true;
			});
		}
	}

	@Override
	public void onStatusMessage(State state) {
		log.info("Thread: " + Thread.currentThread().getId() + " State: " + state);
		if (isSimulatorState(state, waiting))
			Platform.runLater(() -> {
				lblTime.setText(timeFormatter.getTimeText(timeFormatter.getInitialTime()));
			});
	}

	@Override
	public Object getUserInterfaceContainer() {
		table = new TableView();
		TableColumn col1 = new TableColumn("Name");
		col1.setCellValueFactory(new PropertyValueFactory<LabelValue, String>("label"));
		TableColumn col2 = new TableColumn("Value");
		col2.setCellValueFactory(new PropertyValueFactory<LabelValue, String>("value"));		
		table.getColumns().addAll(col1, col2);
		VBox content = new VBox();
		content.setSpacing(5);
		content.setPadding(new Insets(10, 0, 0, 10));
		HBox hbox = new HBox();
		lblTime = new Label("HEY - NO DATA IS BEING SENT!!!");
		hbox.getChildren().addAll(new Label("Tracker time: "),lblTime);
		content.getChildren().addAll(table, hbox);
		ScrollPane sp = new ScrollPane();
		sp.setFitToWidth(true);
		sp.setFitToHeight(true);
		sp.setContent(content);
		return content;
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
	}

	@Override
	public void putPreferences() {
	}

	@Override
	public void getPreferences() {
	}

	public static class LabelValue {
		private final SimpleStringProperty label;
		private final SimpleStringProperty value;

		public LabelValue(String label, String value) {
			this.label = new SimpleStringProperty(label);
			this.value = new SimpleStringProperty(value);
		}

		public String getLabel() {
			return label.get();
		}

		public void setLabel(String label) {
			this.label.set(label);
		}

		public String getValue() {
			return value.get();
		}

		public void setValue(String value) {
			this.value.set(value);
		}
	}

}
