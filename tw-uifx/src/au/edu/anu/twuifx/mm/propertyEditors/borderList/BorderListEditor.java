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
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

public class BorderListEditor extends AbstractPropertyEditor<String, LabelButtonControl> {

	public BorderListEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public BorderListEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		this.getEditor().setOnAction(e -> onAction());
	}

	private void onAction() {
		BorderListItem item = (BorderListItem) getProperty();
		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setResizable(true);
		dlg.setTitle(item.getElement().toShortString() + "#" + P_SPACE_BORDERTYPE.key());
		dlg.initOwner((Window) Dialogs.owner());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane content = new BorderPane();
		Rectangle r = new Rectangle(0, 0, 150, 150);
		r.setFill(Color.WHITE);
		content.setCenter(r);

		ComboBox<BorderType> cmbTop = new ComboBox<>();
		BorderPane.setAlignment(cmbTop, Pos.CENTER);
		cmbTop.getItems().addAll(BorderType.values());
		content.setTop(cmbTop);

		ComboBox<BorderType> cmbBottom = new ComboBox<>();
		BorderPane.setAlignment(cmbBottom, Pos.CENTER);
		cmbBottom.getItems().addAll(BorderType.values());
		content.setBottom(cmbBottom);

		ComboBox<BorderType> cmbLeft = new ComboBox<>();
		BorderPane.setAlignment(cmbLeft, Pos.CENTER);
		cmbLeft.getItems().addAll(BorderType.values());
		content.setLeft(cmbLeft);

		ComboBox<BorderType> cmbRight = new ComboBox<>();
		BorderPane.setAlignment(cmbRight, Pos.CENTER);
		cmbRight.getItems().addAll(BorderType.values());
		content.setRight(cmbRight);

		TreeGraphDataNode node = (TreeGraphDataNode) item.getElement();
		BorderListType currentBLT = (BorderListType) node.properties().getPropertyValue(P_SPACE_BORDERTYPE.key());

		// first dim is x : therefore LRBT
		String sLeft = currentBLT.getWithFlatIndex(0);
		String sRight = currentBLT.getWithFlatIndex(1);
		// second dim is y
		String sBottom = currentBLT.getWithFlatIndex(2);
		String sTop = currentBLT.getWithFlatIndex(3);
		cmbTop.getSelectionModel().select(BorderType.valueOf(sTop));
		cmbBottom.getSelectionModel().select(BorderType.valueOf(sBottom));
		cmbRight.getSelectionModel().select(BorderType.valueOf(sRight));
		cmbLeft.getSelectionModel().select(BorderType.valueOf(sLeft));

		dlg.getDialogPane().setContent(content);

		Optional<ButtonType> result = dlg.showAndWait();
		String entry = "";
		if (result.get().equals(ok)) {
			BorderType left = cmbLeft.getSelectionModel().getSelectedItem();
			entry += left.name() + ",";
			BorderType right = cmbRight.getSelectionModel().getSelectedItem();
			entry += right.name() + ",";
			BorderType bottom = cmbBottom.getSelectionModel().getSelectedItem();
			entry += bottom.name() + ",";
			BorderType top = cmbTop.getSelectionModel().getSelectedItem();
			entry += top.name();
			String value = "([4]" + entry + ")";
			int i = BorderListType.getUnpairedWrapIndex(BorderListType.valueOf(value));
			if (i >= 0)
				Dialogs.errorAlert(item.getElement().toShortString() + "#" + P_SPACE_BORDERTYPE.key(),
						"Wrap-around missmatch", "Wrap-around in dimension " + i + " is unpaired.");
			else
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
