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
import java.util.Optional;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twcore.data.runtime.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class TimeSeriesPlotWidget extends  AbstractDisplayWidget<Property, SimplePropertyList> implements Widget {
	private boolean clearOnReset;
	private GridPane gridPane;
	private Map<String, XYChart.Series<Number, Number>> activeSeries;

	private String creatorId;

	protected TimeSeriesPlotWidget(int dataType) {
		super(DataMessageTypes.TIME_SERIES);
		clearOnReset = true;
	}

	@Override
	public void setProperties(String id,SimplePropertyList properties) {
		this.creatorId = id;
	}

	@Override
	public Object getUserInterfaceContainer() {
		gridPane = new GridPane();
		return gridPane;
	}

	@Override
	public Object getMenuContainer() {
		Menu mu = new Menu(creatorId);
		MenuItem miEdit = new MenuItem("Edit...");
		mu.getItems().add(miEdit);
		miEdit.setOnAction(e -> edit());

		MenuItem miExport = new MenuItem("Export data...");
		mu.getItems().add(miExport);
		return mu;
	}
	
	private void edit() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle(creatorId);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHalignment(HPos.RIGHT);
		grid.getColumnConstraints().add(cc);
		dialog.getDialogPane().setContent(grid);
		//dialog.initOwner(parent);

//		ComboBox<PlotWindowLayout> cbxLayout = new ComboBox<>();
//		cbxLayout.getItems().setAll(PlotWindowLayout.values());
//		cbxLayout.getSelectionModel().select(layout);
		grid.add(new Label("Layout:"), 0, 0);
//		grid.add(cbxLayout, 1, 0);

		CheckBox chxClearOnReset = new CheckBox("Clear data on reset");
		grid.add(chxClearOnReset, 1, 1);
		chxClearOnReset.setSelected(clearOnReset);

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
//			PlotWindowLayout newLayout = cbxLayout.getSelectionModel().getSelectedItem();
			clearOnReset = chxClearOnReset.isSelected();
//			if (!newLayout.equals(layout)) {
//				layout = cbxLayout.getSelectionModel().getSelectedItem();
//				if (!activeSeries.isEmpty()) {
//					reconfigure();
//				}
//			}
		}
	}


	private static final String KeyLayout = "layout";
	private static final String KeyClearOnReset = "clearOnReset";
	@Override
	public void putPreferences() {
		Preferences.putBoolean(creatorId + KeyClearOnReset, clearOnReset);
		//Preferences.putString(creatorId + KeyLayout, layout.name());
		
	}

	@Override
	public void getPreferences() {
		clearOnReset = Preferences.getBoolean(creatorId+KeyClearOnReset, true);
		//String layoutName = Preferences.getString(creatorId+KeyLayout, PlotWindowLayout.STACKED.name());
	}

	@Override
	public void onDataMessage(Property data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMetaDataMessage(SimplePropertyList meta) {
		// TODO Auto-generated method stub
		
	}


}
