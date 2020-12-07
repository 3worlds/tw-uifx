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

package au.edu.anu.twuifx.mm.propertyEditors.borderList;

import java.util.Optional;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.twcore.constants.BorderListType;
import fr.cnrs.iees.twcore.constants.BorderType;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

/**
 * @author Ian Davies
 *
 * @date 25 Sep 2020
 */
public class BorderListEditor extends AbstractPropertyEditor<String, LabelButtonControl> {

	public BorderListEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public BorderListEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		this.getEditor().setOnAction(e -> onAction());
	}

	@SuppressWarnings("unchecked")
	private void onAction() {
		BorderListItem item = (BorderListItem) getProperty();
		TreeGraphDataNode spaceNode = (TreeGraphDataNode) item.getElement();
		BorderListType currentBLT = (BorderListType) spaceNode.properties().getPropertyValue(P_SPACE_BORDERTYPE.key());

		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setResizable(true);
		dlg.setTitle(item.getElement().toShortString() + "#" + P_SPACE_BORDERTYPE.key());
		dlg.initOwner((Window) Dialogs.owner());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

		GridPane content = new GridPane();
		content.setVgap(2);
		content.setHgap(2);

		content.add(new Label("lower bound"), 0, 0);
		content.add(new Label("upper bound"), 2, 0);
		int nDims = currentBLT.size() / 2;
		char[] dname = { 'x', 'y', 'z' };
		ComboBox<BorderType>[] cmbLower = new ComboBox[nDims];
		ComboBox<BorderType>[] cmbUpper = new ComboBox[nDims];
		for (int i = 0; i < nDims; i++) {
			cmbLower[i] = new ComboBox<>();
			cmbUpper[i] = new ComboBox<>();
			content.add(cmbLower[i], 0, i + 1);
			content.add(cmbUpper[i], 2, i + 1);
			Label lbl = new Label("dimension(" + dname[i] + ")");
			content.add(lbl, 1, i + 1);
			GridPane.setHalignment(lbl, HPos.CENTER);
			GridPane.setHalignment(cmbLower[i], HPos.RIGHT);
			GridPane.setHalignment(cmbUpper[i], HPos.LEFT);
			String lower = currentBLT.getWithFlatIndex(i * 2);
			String upper = currentBLT.getWithFlatIndex(i * 2 + 1);
			cmbLower[i].getItems().addAll(BorderType.values());
			cmbUpper[i].getItems().addAll(BorderType.values());
			cmbLower[i].getSelectionModel().select(BorderType.valueOf(lower));
			cmbUpper[i].getSelectionModel().select(BorderType.valueOf(upper));
		}

		dlg.getDialogPane().setContent(content);

		Optional<ButtonType> result = dlg.showAndWait();
		String entry = "";
		if (result.get().equals(ok)) {
			for (int i = 0; i < nDims; i++) {
				entry += "," + cmbLower[i].getSelectionModel().getSelectedItem().name();
				entry += "," + cmbUpper[i].getSelectionModel().getSelectedItem().name();
			}
			entry = entry.replaceFirst(",", "");
			String value = "([" + nDims * 2 + "]" + entry + ")";

			//NB Leave it to the queries to validate these settings.
			setValue(value);

		}

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
