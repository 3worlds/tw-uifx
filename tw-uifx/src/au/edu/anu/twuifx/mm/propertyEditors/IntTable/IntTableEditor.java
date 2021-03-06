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
package au.edu.anu.twuifx.mm.propertyEditors.IntTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.rscs.aot.collections.tables.Dimensioner;
import au.edu.anu.rscs.aot.collections.tables.DoubleTable;
import au.edu.anu.rscs.aot.collections.tables.IntTable;
import au.edu.anu.rscs.aot.collections.tables.Table;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

/**
 * @author Ian Davies
 *
 * @date 11 Nov 2021
 */
public class IntTableEditor extends AbstractPropertyEditor<String, LabelButtonControl> {

	private LabelButtonControl view;
	private IntTableItem itItem;

	public IntTableEditor(Item property, LabelButtonControl control) {
		super(property, control);
	}

	public IntTableEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		itItem = (IntTableItem) this.getProperty();
		view = this.getEditor();
		view.setOnAction(e -> onAction());
	}

	private void onAction() {
		IntTable currentTable = IntTable.valueOf((String) itItem.getValue());
		Table newTable = editTable(currentTable);
		setValue(newTable.toSaveableString());
	}

	private IntTable editTable(IntTable currentValue) {
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
		dlg.setResizable(true);
		String s = "";
		for (int i = 0; i < currentValue.size(); i++) {
			s += Integer.toString(currentValue.getWithFlatIndex(i));
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
			IntTable newValue = new IntTable(new Dimensioner(entries.size()));
			//NB 1 dim editor only
			for (int i = 0; i < entries.size(); i++)
				newValue.setWithFlatIndex(Integer.parseInt(entries.get(i)), i);
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
