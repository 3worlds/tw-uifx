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

package au.edu.anu.twuifx.mm.propertyEditors.integerRangeType;

import java.util.Optional;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import au.edu.anu.rscs.aot.util.IntegerRange;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

public class IntegerRangeEditor extends AbstractPropertyEditor<String, LabelButtonControl> {
	private LabelButtonControl view;
	private IntegerRangeItem dtItem;

	public IntegerRangeEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public IntegerRangeEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif",  Images.class.getPackageName()));
		view = this.getEditor();
		dtItem = (IntegerRangeItem) this.getProperty();
		view.setOnAction(e -> onAction());
	}

	private void onAction() {
		String oldString = (String) dtItem.getValue();
		String newString = oldString;
		newString = editInterval(oldString);
		if (!newString.equals(oldString)) {
			setValue(newString);
		}
	}

	private String editInterval(String currentValue) {
		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setTitle(getProperty().getName());
		dlg.initOwner((Window) Dialogs.owner());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane content = new BorderPane();
		dlg.getDialogPane().setContent(content);
		GridPane grid = new GridPane();
		content.setCenter(grid);

		IntegerRange range = IntegerRange.valueOf(currentValue);
		int low = range.getFirst();
		int high = range.getLast();

		TextField tfLow = new TextField();
		TextField tfHigh = new TextField();
		tfLow.textProperty().addListener((observable, oldValue, newValue) -> {
		    if (newValue.matches("-?\\d*")) return;
		    tfLow.setText(newValue.replaceAll("[^\\d]", ""));
		});
		tfHigh.textProperty().addListener((observable, oldValue, newValue) -> {
		    if (newValue.matches("-?\\d*")) return;
		    tfHigh.setText(newValue.replaceAll("[^\\d]", ""));
		});
		CheckBox cbLowMin = new CheckBox("MIN INTEGER");
		CheckBox cbHighMax = new CheckBox("*");
		cbLowMin.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				tfLow.setVisible(false);
			} else {
				tfLow.setVisible(true);
				try {
					Double.parseDouble(tfLow.getText());
				} catch (NumberFormatException e) {
					tfLow.setText("0");
				}
			}
		});
		cbHighMax.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				tfHigh.setVisible(false);
			} else {
				tfHigh.setVisible(true);
				try {
					Double.parseDouble(tfHigh.getText());
				} catch (NumberFormatException e) {
					tfHigh.setText("1");
				}
			}
		});

		if (range.getFirst() == Integer.MIN_VALUE)
			cbLowMin.setSelected(true);
		else {
			cbLowMin.setSelected(false);
			tfLow.setText(String.valueOf(range.getFirst()));
		}
		if (range.getLast() == Integer.MAX_VALUE)
			cbHighMax.setSelected(true);
		else {
			cbHighMax.setSelected(false);
			tfHigh.setText(String.valueOf(range.getLast()));
		}
		grid.add(cbLowMin, 0, 0);
		grid.add(tfLow, 1, 0);
		grid.add(new Label(".."), 2, 0);
		grid.add(tfHigh, 3, 0);
		grid.add(cbHighMax, 4, 0);
		dlg.setResizable(true);
		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			low = Integer.MIN_VALUE;
			high = Integer.MAX_VALUE; 
			if (!cbLowMin.isSelected())
				low = Integer.parseInt(tfLow.getText());
			if (!cbHighMax.isSelected())
				high = Integer.parseInt(tfHigh.getText());
			range = new IntegerRange(low,high);
			return range.toString();	
		}
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
