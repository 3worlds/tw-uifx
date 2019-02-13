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
		button = new Button("");
		//TODO: This won't work when MM is running from jar.cf MRSplash.java
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

	public void setAction(EventHandler<ActionEvent> action) {
		button.setOnAction(action);
	}
	public void setDiabled(boolean b) {
		button.setDisable(b);
	}

}
