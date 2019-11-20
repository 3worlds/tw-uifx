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

package au.edu.anu.twuifx.mm.propertyEditors.intervalType;

import java.util.Optional;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.ens.biologie.generic.utils.Interval;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
		newString = editInterval(oldString);
		if (!newString.equals(oldString)) {
			setValue(newString);
			// GraphState.setChanged();
			// ConfigGraph.validateGraph();
		}
	}

	private String editInterval(String currentValue) {
		// TODO put change listeners on infinite check boxes to hide relevant open cb
		// and other infinite check box
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
		Double high = interval.sup();// ?? wrong
		if (!low.equals(Double.NEGATIVE_INFINITY))
			txtLow = low.toString();
		if (!high.equals(Double.POSITIVE_INFINITY))
			txtHigh = high.toString();
		TextField tfLow = new TextField("");
		TextField tfHigh = new TextField();
		CheckBox cbLowOpen = new CheckBox("]");
		CheckBox cbHighOpen = new CheckBox("[");
		CheckBox cbPosInf = new CheckBox("+∞");
		CheckBox cbNegInf = new CheckBox("-∞");
		cbNegInf.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				//cbPosInf.setVisible(false);
				cbLowOpen.setVisible(false);
				tfLow.setVisible(false);
			} else {
				//cbPosInf.setVisible(true);
				cbLowOpen.setVisible(true);
				tfLow.setVisible(true);
				try {
					Double.parseDouble(tfLow.getText());
				} catch (NumberFormatException e) {
					tfLow.setText("0.0");
				}
			}
		});
		cbPosInf.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				//cbNegInf.setVisible(false);
				cbHighOpen.setVisible(false);
				tfHigh.setVisible(false);
			} else {
				//cbNegInf.setVisible(true);
				cbHighOpen.setVisible(true);
				tfHigh.setVisible(true);
				try {
					Double.parseDouble(tfHigh.getText());
				} catch (NumberFormatException e) {
					tfHigh.setText("0.0");
				}
			}
		});

		if (interval.halfOpenInf())
			cbLowOpen.setSelected(true);
		if (interval.halfOpenSup())
			cbHighOpen.setSelected(true);
		if (low.equals(Double.NEGATIVE_INFINITY))
			cbNegInf.setSelected(true);
		if (high.equals(Double.POSITIVE_INFINITY))
			cbPosInf.setSelected(true);
		tfLow.setText(txtLow);
		tfHigh.setText(txtHigh);
		grid.add(cbPosInf, 0, 0);
		grid.add(cbLowOpen, 1, 0);
		grid.add(tfLow, 2, 0);
		// grid.add(new Label("Low:"), 0, 0);
		// grid.add(new Label("High:"), 0, 1);
		grid.add(tfHigh, 3, 0);
		grid.add(cbHighOpen, 4, 0);
		grid.add(cbNegInf, 5, 0);
		GridPane.setMargin(tfLow, new Insets(0, 5, 0, 0));
		GridPane.setMargin(cbLowOpen, new Insets(0, 5, 0, 0));
		GridPane.setMargin(tfHigh, new Insets(0, 5, 0, 0));
		GridPane.setMargin(cbHighOpen, new Insets(0, 5, 0, 0));
		GridPane.setMargin(cbPosInf, new Insets(0, 5, 0, 0));
		GridPane.setMargin(cbNegInf, new Insets(0, 5, 0, 0));
		dlg.setResizable(true);
		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			boolean halfOpenInf = cbLowOpen.isSelected();
			boolean halfOpenSup = cbHighOpen.isSelected();
			boolean posInf = cbPosInf.isSelected();
			boolean negInf = cbNegInf.isSelected();
			try {
				low = Double.parseDouble(tfLow.getText());
			} catch (NumberFormatException e) {
				low = Double.NEGATIVE_INFINITY;
			}
			try {
				high = Double.parseDouble(tfHigh.getText());
			} catch (NumberFormatException e) {
				high = Double.POSITIVE_INFINITY;
			}
			if (posInf)
				high = Double.POSITIVE_INFINITY;
			if (negInf)
				low = Double.NEGATIVE_INFINITY;

			// Can't be both
			if (posInf && negInf) {
				Dialogs.errorAlert("Interval error", "", "Both bounds cannot be infinite");
				return currentValue;
			}

			// low must be lower than high.
			if (low > high) {
				Double tmp = high;
				high = low;
				low = tmp;
			}
			if (!halfOpenInf && !halfOpenSup && !negInf && !posInf) {
				/* Simple closed [x,y] */
				interval = Interval.closed(low, high);
			} else if (halfOpenInf && halfOpenSup && !negInf && !posInf) {
				/* open ]x,y[ */
				interval = Interval.open(low, high);
			} else if (halfOpenInf && !halfOpenSup && !negInf && !posInf) {
				/* open inf ]x,y] */
				interval = Interval.halfOpenInf(low, high);
			} else if (!halfOpenInf && halfOpenSup && !negInf && !posInf) {
				/* open sup [x,y[ */
				interval = Interval.halfOpenSup(low, high);
			} else if (!halfOpenSup && negInf) {
				/* "]-∞,high]" */
				interval = Interval.toNegInf(high);
			} else if (!halfOpenInf && !negInf && posInf) {
				/* [low,+∞[ */
				interval = Interval.toPosInf(low);
			} else if (halfOpenSup && negInf) {
				/* ]-∞,high[ */
				interval = Interval.openToNegInf(high);
			} else if (halfOpenInf && posInf) {
				/* ]low,+∞[ */
				interval = Interval.openToPosInf(low);
			} else
				return currentValue;
			return interval.toString();
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
