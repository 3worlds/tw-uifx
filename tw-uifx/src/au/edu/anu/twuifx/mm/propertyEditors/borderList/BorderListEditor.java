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
import fr.ens.biologie.generic.utils.Interval;
import javafx.beans.value.ObservableValue;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

public class BorderListEditor extends AbstractPropertyEditor<String, LabelButtonControl> {

	private Canvas canvas;
	private ComboBox<BorderType> cmbLeft;
	private ComboBox<BorderType> cmbRight;
	private ComboBox<BorderType> cmbBottom;
	private ComboBox<BorderType> cmbTop;
	private TreeGraphDataNode spaceNode;

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
		canvas = new Canvas();
		canvas.setHeight(150);
		canvas.setWidth(150);
		content.setCenter(canvas);

		cmbTop = new ComboBox<>();
		BorderPane.setAlignment(cmbTop, Pos.CENTER);
		BorderPane.setMargin(cmbTop, new Insets(2, 2, 2, 2));
		cmbTop.getItems().addAll(BorderType.values());
		content.setTop(cmbTop);

		cmbBottom = new ComboBox<>();
		BorderPane.setAlignment(cmbBottom, Pos.CENTER);
		BorderPane.setMargin(cmbBottom, new Insets(2, 2, 2, 2));
		cmbBottom.getItems().addAll(BorderType.values());
		content.setBottom(cmbBottom);

		cmbLeft = new ComboBox<>();
		BorderPane.setAlignment(cmbLeft, Pos.CENTER);
		BorderPane.setMargin(cmbLeft, new Insets(2, 2, 2, 2));
		cmbLeft.getItems().addAll(BorderType.values());
		content.setLeft(cmbLeft);

		cmbRight = new ComboBox<>();
		BorderPane.setAlignment(cmbRight, Pos.CENTER);
		BorderPane.setMargin(cmbRight, new Insets(2, 2, 2, 2));
		cmbRight.getItems().addAll(BorderType.values());
		content.setRight(cmbRight);

		spaceNode = (TreeGraphDataNode) item.getElement();
		BorderListType currentBLT = (BorderListType) spaceNode.properties().getPropertyValue(P_SPACE_BORDERTYPE.key());

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
		
		drawCanvas();
		
		cmbTop.getSelectionModel().selectedItemProperty().addListener((e) -> {
			drawCanvas();
		});
		cmbBottom.getSelectionModel().selectedItemProperty().addListener((e) -> {
			drawCanvas();
		});
		cmbLeft.getSelectionModel().selectedItemProperty().addListener((e) -> {
			drawCanvas();
		});
		cmbRight.getSelectionModel().selectedItemProperty().addListener((e) -> {
			drawCanvas();
		});

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

	private void drawCanvas() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.setStroke(Color.WHITE);
		Interval xLimits = (Interval) spaceNode.properties().getPropertyValue(P_SPACE_XLIM.key());
		Interval yLimits = (Interval) spaceNode.properties().getPropertyValue(P_SPACE_YLIM.key());
		Bounds bounds = new BoundingBox(xLimits.inf(), yLimits.inf(), xLimits.sup() - xLimits.inf(),
				yLimits.sup() - yLimits.inf());
		double maxDim = Math.max(bounds.getWidth(), bounds.getHeight());
		double maxSize = 150;
		double scale = maxSize / maxDim;
		double w = scale * bounds.getWidth();
		double h = scale * bounds.getHeight();
		canvas.setWidth(w);
		canvas.setHeight(h);
		gc.fillRect(0, 0, w, h);

		drawBorder(gc, cmbTop.getSelectionModel().getSelectedItem(),    0,   1,   w,   1);
		drawBorder(gc, cmbBottom.getSelectionModel().getSelectedItem(), 0,   h-1, w,   h-1);
		drawBorder(gc, cmbLeft.getSelectionModel().getSelectedItem(),   1,   0,   1,   h);
		drawBorder(gc, cmbRight.getSelectionModel().getSelectedItem(),  w-1, 0,   w-1, h);

	}

	private void drawBorder(GraphicsContext gc, BorderType bt, double x1, double y1, double x2, double y2) {
		switch (bt) {
		case wrap: {
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(5);
			gc.setLineWidth(1.0);
			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
		case reflection: {
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(0);
			gc.setLineWidth(4.0);
			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
		case sticky: {
			gc.setStroke(Color.GREY);
			gc.setLineDashes(0);
			gc.setLineWidth(4.0);
			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
		case oblivion: {
//			gc.setStroke(Color.WHITE);
//			gc.setLineDashes(0);
//			gc.setLineWidth(1.0);
//			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
		default: {
			// infinite
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(0);
			gc.setLineWidth(2.0);
			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
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
