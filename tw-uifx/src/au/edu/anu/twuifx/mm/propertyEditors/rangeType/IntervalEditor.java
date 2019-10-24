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

import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.ens.biologie.generic.utils.Interval;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

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
			GraphState.setChanged();
			ConfigGraph.validateGraph();
		}
	}

	private String editDateTime(String currentValue) {
		Interval interval = Interval.valueOf(currentValue);
		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setTitle(getProperty().getName());
//		// dlg.initOwner(Dialogs);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane content = new BorderPane();
		dlg.getDialogPane().setContent(content);
		GridPane grid = new GridPane();
		content.setCenter(grid);
		
		TextField tfFrom = new TextField("*");
		String tt ="*";
		String ft = "*";
		Double inf = interval.inf();
		Double sup = interval.sup();
		if (!inf.equals(Double.NEGATIVE_INFINITY))
			ft=inf.toString();
		if (!sup.equals(Double.POSITIVE_INFINITY))
			tt = sup.toString();
		TextField tfTo = new TextField();
		tfFrom.setText(ft);
		tfTo.setText(tt);
		grid.add(new Label("From:"), 0, 0);
		grid.add(tfFrom, 1, 0);
		grid.add(new Label("To:"), 0, 1);
		grid.add(tfTo, 1, 1);
		dlg.setResizable(true);
		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
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
