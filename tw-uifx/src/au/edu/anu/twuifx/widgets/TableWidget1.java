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
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.data.runtime.Output0DData;
import au.edu.anu.twcore.data.runtime.Output0DMetadata;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.widgets.helpers.RangeWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;
import fr.ens.biologie.generic.utils.Logging;
//import fr.ens.biologie.generic.utils.Statistics;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_DATATRACKER_STATISTICS;

/**
 * @author Ian Davies
 *
 * @date 6 Dec 2019
 */
/**
 * Displays a table of data from all simulator.id() from [lower.. lower+range]
 */
public class TableWidget1 extends AbstractDisplayWidget<Output0DData, Metadata> implements WidgetGUI {
	private final WidgetTimeFormatter timeFormatter;
	private final WidgetTrackingPolicy<TimeData> policy;
	private static Logger log = Logging.getLogger(TableWidget1.class);
	private TableView<WidgetTableData> table;
//	private Label lblTime;
	private StatisticalAggregatesSet sas;
	private Collection<String> sampledItems;
	private Output0DMetadata metadataTS;
	private Metadata msgMetadata;
	private String widgetId;
	private final ObservableList<WidgetTableData> tableDataList;
	private Map<Integer, TreeMap<String, WidgetTableData>> senderDataSetMap;

	public TableWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM0);
		timeFormatter = new WidgetTimeFormatter();
		policy = new RangeWidgetTrackingPolicy();
		log.info("Thread: " + Thread.currentThread().getId());
		tableDataList = FXCollections.observableArrayList();
		senderDataSetMap = new ConcurrentHashMap<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
//		1) Called first immediately after construction
		policy.setProperties(id, properties);
		this.widgetId = id;
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
//		2) called second after construction
		if (policy.canProcessMetadataMessage(meta)) {
			msgMetadata = meta;
			metadataTS = (Output0DMetadata) meta.properties().getPropertyValue(Output0DMetadata.TSMETA);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getUserInterfaceContainer() {
//		3) called third after metadata
//		get the prefs, if any, before building the ui
		getUserPreferences();

		// use a helper
		sas = null;
		if (msgMetadata.properties().hasProperty(P_DATATRACKER_STATISTICS.key()))
			sas = (StatisticalAggregatesSet) msgMetadata.properties().getPropertyValue(P_DATATRACKER_STATISTICS.key());
		if (msgMetadata.properties().hasProperty("sample")) {
			StringTable st = (StringTable) msgMetadata.properties().getPropertyValue("sample");
			if (st != null) {
				sampledItems = new ArrayList<>(st.size());
				for (int i = 0; i < st.size(); i++)
					sampledItems.add(st.getWithFlatIndex(i));
			}
		}

		for (int sender = policy.getDataMessageRange().getFirst(); sender <= policy.getDataMessageRange()
				.getLast(); sender++) {
			senderDataSetMap.put(sender, new TreeMap<String, WidgetTableData>());

			for (DataLabel dl : metadataTS.doubleNames())
				makeChannels(dl, sender);
			for (DataLabel dl : metadataTS.intNames())
				makeChannels(dl, sender);
		}
		timeFormatter.onMetaDataMessage(msgMetadata);

		table = new TableView<WidgetTableData>();
		TableColumn<WidgetTableData, String> col1Label = new TableColumn<>("Name");
		col1Label.setCellValueFactory(new PropertyValueFactory<WidgetTableData, String>("label"));

		TableColumn<WidgetTableData, String> col2Value = new TableColumn<>("Value");
		col2Value.setCellValueFactory(new PropertyValueFactory<WidgetTableData, String>("value"));

		TableColumn<WidgetTableData, String> col3Time = new TableColumn<>("Time");
		col3Time.setCellValueFactory(new PropertyValueFactory<WidgetTableData, String>("time"));

		table.getColumns().addAll(col1Label, col2Value, col3Time);

		col1Label.prefWidthProperty().bind(table.widthProperty().multiply(1.0 / 3.0));
		col2Value.prefWidthProperty().bind(table.widthProperty().multiply(1.0 / 3.0));
		col3Time.prefWidthProperty().bind(table.widthProperty().multiply(1.0 / 3.0));

		BorderPane content = new BorderPane();
		Label lname = new Label(widgetId);
		content.setTop(lname);
		BorderPane.setAlignment(lname, Pos.CENTER);
		content.setCenter(table);

		ScrollPane sp = new ScrollPane();
		sp.setFitToWidth(true);
		sp.setFitToHeight(true);
		sp.setContent(content);

		table.setItems(tableDataList);
		table.refresh();
		return content;
	}

	@Override
	public void onStatusMessage(State state) {
//		4) Called 4th after UI construction - this is only in the UI thread the first time it's called
		if (isSimulatorState(state, waiting))
			Platform.runLater(() -> {
//				lblTime.setText(timeFormatter.getTimeText(timeFormatter.getInitialTime()));
				// initialvalue (if we had one)
				for (WidgetTableData td : tableDataList) {
					td.setValue(0);
					td.setTime(timeFormatter.getTimeText(timeFormatter.getInitialTime()));
				}

				table.refresh();

			});
	}

	private void processDataMessage(Output0DData data) {
		final Map<String, WidgetTableData> dataSetMap = senderDataSetMap.get(data.sender());
		final int sender = data.sender();
		Platform.runLater(() -> {
			String itemId = null;
			if (sas != null)
				itemId = data.itemLabel().getEnd();
			else if (sampledItems != null)
				itemId = data.itemLabel().toString();

			for (DataLabel dl : metadataTS.doubleNames()) {
				String key = getKey(sender, dl, itemId);
				WidgetTableData td = dataSetMap.get(key);
				final double value = data.getDoubleValues()[metadataTS.indexOf(dl)];
				td.setValue(value);
				td.setTime(timeFormatter.getTimeText(data.time()));
			}
			for (DataLabel dl : metadataTS.intNames()) {
				String key = getKey(sender, dl, itemId);
				WidgetTableData td = dataSetMap.get(key);
				final long value = data.getIntValues()[metadataTS.indexOf(dl)];
				td.setValue(value);
				td.setTime(timeFormatter.getTimeText(data.time()));
			}
			table.refresh();
//			lblTime.setText(timeFormatter.getTimeText(data.time()));
		});
	}

	@Override
	public void onDataMessage(Output0DData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial))
				throw new TwuifxException("Handling initial data not implemented for this widget.");
			else
				processDataMessage(data);
		}
	}

	// TODO move to a helper class
	private String getKey(int sender, DataLabel dl, String itemId) {
		String result;
		if (itemId != null)
			result = sender + ":" + itemId + DataLabel.HIERARCHY_DOWN + dl.toString();
		else
			result = sender + ":" + dl.toString();
		return result;
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void putUserPreferences() {
	}

	@Override
	public void getUserPreferences() {
	}

	protected static class WidgetTableData {
		// These don't really need to be properties as they are not 'listened' to. It's
		// the list that is observed.
		private final SimpleStringProperty labelProperty;
		private final SimpleStringProperty valueProperty;
		private final SimpleStringProperty timeProperty;

		public WidgetTableData(String label) {
			this.labelProperty = new SimpleStringProperty(label);
			this.valueProperty = new SimpleStringProperty();
			this.timeProperty = new SimpleStringProperty();
//			this.stats = new Statistics();
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

		public String getTime() {
			return timeProperty.get();
		}

		public void setTime(String timeText) {
			this.timeProperty.set(timeText);
		}

	}

	private void makeChannels(DataLabel dl, int sender) {
		Map<String, WidgetTableData> dataSetMap = senderDataSetMap.get(sender);
		if (sas != null) {
			for (StatisticalAggregates sa : sas.values()) {
				String key = sender + ":" + sa.name() + DataLabel.HIERARCHY_DOWN + dl.toString();
				WidgetTableData wtd = new WidgetTableData(key);
				tableDataList.add(wtd);
				dataSetMap.put(key, wtd);
			}
		} else if (sampledItems != null) {
			for (String si : sampledItems) {
				String key = sender + ":" + si + DataLabel.HIERARCHY_DOWN + dl.toString();
				WidgetTableData wtd = new WidgetTableData(key);
				tableDataList.add(wtd);
				dataSetMap.put(key, wtd);
			}
		} else {
			String key = sender + ":" + dl.toString();
			WidgetTableData wtd = new WidgetTableData(key);
			tableDataList.add(wtd);
			dataSetMap.put(key, wtd);
		}

	}

}
