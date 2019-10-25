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

package au.edu.anu.twuifx.mm.propertyEditors.rangeType;

import java.util.Optional;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.ens.biologie.generic.utils.Interval;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

public class IntervalEditor extends AbstractPropertyEditor<String, LabelButtonControl> {
	private LabelButtonControl view;
	private IntervalItem dtItem;

	public IntervalEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
		// TODO Auto-generated constructor stub
	}

	public IntervalEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		view = this.getEditor();
		dtItem = (IntervalItem) this.getProperty();
		// we need to find the timeline to create the meta-data for time editing
		view.setOnAction(e -> onAction());
	}

	private void onAction() {
		String oldString = (String) dtItem.getValue();
		String newString = oldString;
		newString = editDateTime(oldString);
		if (!newString.equals(oldString)) {
			setValue(newString);
			// GraphState.setChanged();
			// ConfigGraph.validateGraph();
		}
	}

	private String editDateTime(String currentValue) {
		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setTitle(getProperty().getName());
		dlg.initOwner((Window) Dialogs.owner());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane content = new BorderPane();
		dlg.getDialogPane().setContent(content);
		GridPane grid = new GridPane();
		content.setCenter(grid);

		Interval interval = Interval.valueOf(currentValue);
		String txtLow = "";
		String txtHigh = "";
		Double low = interval.inf();
		Double high = interval.sup();
		if (!low.equals(Double.NEGATIVE_INFINITY))
			txtLow = low.toString();
		if (!high.equals(Double.POSITIVE_INFINITY))
			txtHigh = high.toString();
		TextField tfLow = new TextField("");
		TextField tfHigh = new TextField();
		CheckBox cbLow = new CheckBox("Inclusive");
		CheckBox cbHigh = new CheckBox("Inclusive");
		CheckBox cbLowInf = new CheckBox("Infinite");
		CheckBox cbHighInf = new CheckBox("Infinite");
		tfLow.setText(txtLow);
		tfHigh.setText(txtHigh);
		grid.add(new Label("Low:"), 0, 0);
		grid.add(tfLow, 1, 0);
		grid.add(cbLow, 2, 0);
		grid.add(cbLowInf, 3, 0);
		grid.add(new Label("High:"), 0, 1);
		grid.add(tfHigh, 1, 1);
		grid.add(cbHigh, 2, 1);
		grid.add(cbHighInf, 3, 1);
		GridPane.setMargin(tfLow, new Insets(0, 5, 0, 0));
		GridPane.setMargin(cbLow, new Insets(0, 5, 0, 0));
		GridPane.setMargin(tfHigh, new Insets(0, 5, 0, 0));
		GridPane.setMargin(cbHigh, new Insets(0, 5, 0, 0));
		GridPane.setMargin(cbLowInf, new Insets(0, 5, 0, 0));
		GridPane.setMargin(cbHighInf, new Insets(0, 5, 0, 0));
		dlg.setResizable(true);
		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			txtLow = tfLow.getText();
			txtHigh = tfHigh.getText();

			// Interval interval = new Interval();
			return currentValue;
		} else
			return currentValue;
	}

	@Override
	public void setValue(String value) {
		getEditor().setText(value);
	}

	@Override
	protected ObservableValue<String> getObservableValue() {
		return getEditor().getTextProperty();
	}

}
