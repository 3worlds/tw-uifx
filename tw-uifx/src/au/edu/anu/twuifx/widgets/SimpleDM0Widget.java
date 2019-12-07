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

import au.edu.anu.twcore.data.runtime.DataLabel;
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
import fr.ens.biologie.generic.utils.Statistics;
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
	private TableView<TableData> table;
	private Label lblTime;

	public SimpleDM0Widget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME_SERIES);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		log.info("Thread: " + Thread.currentThread().getId());
	}

	private boolean initialMessage = false;
	private ObservableList<TableData> tableDataList;

	@Override
	public void onMetaDataMessage(Metadata meta) {
		log.info("Thread: " + Thread.currentThread().getId() + " Meta-data: " + meta);
		// clear the table - probably in a ui thread!
		if (!initialMessage) {
			Platform.runLater(() -> {
				tableDataList = FXCollections.observableArrayList();
				tsmeta = (TimeSeriesMetadata) meta.properties().getPropertyValue(TimeSeriesMetadata.TSMETA);
				timeFormatter.onMetaDataMessage(meta);

				for (DataLabel dl : tsmeta.doubleNames())
					tableDataList.add(new TableData(dl.toString()));
				for (DataLabel dl : tsmeta.intNames())
					tableDataList.add(new TableData(dl.toString()));

				table.setItems(tableDataList);
				initialMessage = true;
			});
		}
	}

	@Override
	public void onDataMessage(TimeSeriesData data) {
		log.info("Thread: " + Thread.currentThread().getId() + " data: " + data);
		if (policy.canProcessDataMessage(data)) {
			Platform.runLater(() -> {
				lblTime.setText(timeFormatter.getTimeText(data.time()));
				for (DataLabel dl : tsmeta.doubleNames()) {
					int idx = tsmeta.indexOf(dl);
					Double value = data.getDoubleValues()[idx];
					TableData td = tableDataList.get(idx);
					td.stats.add(value);
					td.setValue(value);
				}
				for (DataLabel dl : tsmeta.intNames()) {
					int idx = tsmeta.indexOf(dl);
					TableData td = tableDataList.get(idx);
					Long value = data.getIntValues()[idx];
					td.stats.add(value);
					td.setValue(value);
				}
				table.refresh();
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

	@SuppressWarnings("unchecked")
	@Override
	public Object getUserInterfaceContainer() {
		table = new TableView<TableData>();
		TableColumn<TableData, String> col1Label = new TableColumn<>("Label");
		col1Label.setCellValueFactory(new PropertyValueFactory<TableData, String>("label"));

		TableColumn<TableData, String> col2Value = new TableColumn<>("Value");
		col2Value.setCellValueFactory(new PropertyValueFactory<TableData, String>("value"));

		TableColumn<TableData, String> col3Min = new TableColumn<>("Min");
		col3Min.setCellValueFactory(new PropertyValueFactory<TableData, String>("min"));

		TableColumn<TableData, String> col4Max = new TableColumn<>("Max");
		col4Max.setCellValueFactory(new PropertyValueFactory<TableData, String>("max"));
		// avg,var,sum
		TableColumn<TableData, String> col5Avg = new TableColumn<>("Avg");
		col5Avg.setCellValueFactory(new PropertyValueFactory<TableData, String>("avg"));

		TableColumn<TableData, String> col6Var = new TableColumn<>("Var");
		col6Var.setCellValueFactory(new PropertyValueFactory<TableData, String>("var"));

		TableColumn<TableData, String> col7Sum = new TableColumn<>("Sum");
		col7Sum.setCellValueFactory(new PropertyValueFactory<TableData, String>("sum"));

		table.getColumns().addAll(col1Label, col2Value, col3Min, col4Max, col5Avg, col6Var, col7Sum);

		VBox content = new VBox();
		content.setSpacing(5);
		content.setPadding(new Insets(10, 0, 0, 10));
		HBox hbox = new HBox();
		lblTime = new Label("uninitialised");
		hbox.getChildren().addAll(new Label("Tracker time: "), lblTime);
		content.getChildren().addAll(table, hbox);
		ScrollPane sp = new ScrollPane();
		sp.setFitToWidth(true);
		sp.setFitToHeight(true);
		sp.setContent(content);
		return sp;
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

	protected static class TableData {
		// These don't really need to be properties as they are not 'listened' to. It's
		// the list that is observed.
		private final SimpleStringProperty labelProperty;
		private final SimpleStringProperty valueProperty;
		private final Statistics stats;

		public TableData(String label) {
			this.labelProperty = new SimpleStringProperty(label);
			this.valueProperty = new SimpleStringProperty();
			this.stats = new Statistics();
		}

		public String getLabel() {
			return labelProperty.get();
		}

		public void setLabel(String label) {
			this.labelProperty.set(label);
		}

		public String getValue() {
			return valueProperty.get();
		}

		public void setValue(Number value) {
			this.valueProperty.set(value.toString());
		}

		public double getMin() {
			return stats.min();
		}

		public double getMax() {
			return  stats.max();
		}

		public double getAvg() {
			return stats.average();
		}

		public double getVar() {
			return stats.variance();
		}

		public double getSum() {
			return stats.sum();
		}

		public Statistics getStatistics() {
			return stats;
		}

	}

}
