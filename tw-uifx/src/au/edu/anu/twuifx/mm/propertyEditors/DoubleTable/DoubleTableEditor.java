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

package au.edu.anu.twuifx.mm.propertyEditors.DoubleTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.rscs.aot.collections.tables.Dimensioner;
import au.edu.anu.rscs.aot.collections.tables.DoubleTable;
import au.edu.anu.rscs.aot.collections.tables.Table;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
/**
 * @author Ian Davies-15 Dec 2019
 */

//TODO table editors need to be brought together in a proper hierarchy
public class DoubleTableEditor extends AbstractPropertyEditor<String, LabelButtonControl> {
	private LabelButtonControl view;
	private DoubleTableItem dtItem;
	public DoubleTableEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public DoubleTableEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		view = this.getEditor();
		dtItem = (DoubleTableItem) this.getProperty();
		view.setOnAction(e -> onAction());
	}

	private void onAction() {
		DoubleTable currentTable = DoubleTable.valueOf((String)dtItem.getValue());
		Table newTable = editTable(currentTable);
		setValue(newTable.toSaveableString());
	}

	private DoubleTable editTable(DoubleTable currentValue) {
		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setTitle(getProperty().getName());
		dlg.initOwner((Window) Dialogs.owner());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane pane = new BorderPane();
		TextArea textArea = new TextArea();
		pane.setCenter(textArea);
		dlg.getDialogPane().setContent(pane);
		dlg.setResizable(true);
		String s = "";
		for (int i = 0; i < currentValue.size(); i++) {
			s += Double.toString(currentValue.getWithFlatIndex(i));
			if (i < currentValue.size() - 1)
				s += "\n";
		}
		textArea.setText(s);
		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			s = textArea.getText();
			List<String> entries = new ArrayList<>();
			String[] parts = s.split("\\n");
			for (String p : parts) {
				p = p.trim();
				if (p.length() > 0)
					entries.add(p);
			}
			if (entries.isEmpty())
				entries.add("");
			DoubleTable newValue = new DoubleTable(new Dimensioner(entries.size()));
			//NB 1 dim editor only
			for (int i = 0; i < entries.size(); i++)
				newValue.setWithFlatIndex(Double.parseDouble(entries.get(i)), i);
			return newValue;
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
