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

package au.edu.anu.twuifx.mm.propertyEditors.boxType;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.uit.space.Box;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Ian Davies
 *
 * @date 25 Sep 2020
 */
public class BoxItemEditor extends AbstractPropertyEditor<String, LabelButtonControl> {
	public BoxItemEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public BoxItemEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		this.getEditor().setOnAction(e -> onAction());
	}

	private List<TextField> origins;
	private List<TextField> widths;

	private void onAction() {
		origins = new ArrayList<>();
		widths = new ArrayList<>();
		BoxItem item = (BoxItem) getProperty();
		Box currentBox = (Box) item.getElementProperties().getPropertyValue(P_SPACE_OBSWINDOW.key());
		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setResizable(true);
		dlg.setTitle(item.getElement().toShortString() + "#" + P_SPACE_OBSWINDOW.key());
		dlg.initOwner((Window) Dialogs.owner());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		GridPane content = new GridPane();
		dlg.getDialogPane().setContent(content);
		content.setVgap(2);
		content.setHgap(2);
		content.add(new Label("origin"), 0, 0);
		content.add(new Label("width"), 2, 0);
		char[] dname = { 'x', 'y', 'z' };
		for (int i = 0; i < currentBox.dim(); i++) {
			Double origin = currentBox.lowerBound(i);
			Double width = currentBox.upperBound(i) - origin;
			TextField tf;

			tf = new TextField(origin.toString());
			tf.setTextFormatter(new TextFormatter<>(
					change -> (change.getControlNewText().matches(Dialogs.vsReal) ? change : null)));
			origins.add(tf);
			tf = new TextField(width.toString());
			tf.setTextFormatter(new TextFormatter<>(
					change -> (change.getControlNewText().matches(Dialogs.vsReal) ? change : null)));
		
			widths.add(tf);
		
			content.add(origins.get(i), 0, i + 1);
			content.add(widths.get(i), 2, i + 1);
			content.add(new Label("dimension(" + dname[i] + ")"), 1, i + 1);
		}

		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {

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
