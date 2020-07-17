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
package au.edu.anu.twuifx.mm.propertyEditors;

import java.io.File;

import au.edu.anu.rscs.aot.util.Resources;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * 
 * Generic UI for property editor with text (non-editable) on left
 * and and edit button on the right.
 * This is the ui of some property appearing in the 
 * propertySheet in ModelMaker
 * 
 */
/**
 * @author Ian Davies
 * @date 24 Feb. 2018
 */
public class LabelButtonControl extends GridPane {
	private Label label;
	private Button button;

	public LabelButtonControl(String fileName, String packageName) {
		super();
		label = new Label("");
		label.setPadding(new Insets(5, 2, 0, 0));
		label.setMinWidth(100);
		button = new Button("");
		// TODO: This won't work when MM is running from jar.cf MRSplash.java
		File file = Resources.getFile(fileName, packageName);
		Image image = new Image(file.toURI().toString());
		ImageView imageView = new ImageView(image);
		imageView.pickOnBoundsProperty().set(true);
		imageView.preserveRatioProperty().set(true);
		button.graphicProperty().set(imageView);
		this.add(label, 0, 0);
		this.add(button, 1, 0);
		GridPane.setHgrow(label, Priority.ALWAYS);
		GridPane.setHalignment(label, HPos.LEFT);
		GridPane.setValignment(label, VPos.CENTER);
		GridPane.setHalignment(button, HPos.RIGHT);
		GridPane.setValignment(button, VPos.CENTER);
	}

	public StringProperty getTextProperty() {
		return label.textProperty();
	}

	public void setText(String text) {
		label.setText(text);
	}

	public void setOnAction(EventHandler<ActionEvent> action) {
		button.setOnAction(action);
	}

	public void setDiabled(boolean b) {
		button.setDisable(b);
	}

}
