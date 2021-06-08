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

package au.edu.anu.twuifx.mm.propertyEditors.trackerType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.rscs.aot.collections.tables.Dimensioner;
import au.edu.anu.rscs.aot.collections.tables.IndexString;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.data.Record;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
import fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels;
import fr.cnrs.iees.twcore.constants.TrackerType;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * @author Ian Davies
 *
 * @date 13 Oct 2019
 */
public class TrackerTypeEditor extends AbstractPropertyEditor<String, LabelButtonControl> {

	// private LabelButtonControl view;
	private List<Record> catRecords;
	private ALDataEdge trackerEdge;
	private TrackerType currentTT;

	public TrackerTypeEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public TrackerTypeEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		this.getEditor().setOnAction(e -> onAction());
	}

	private void onAction() {
		TrackerTypeItem item = (TrackerTypeItem) getProperty();
		trackerEdge = (ALDataEdge) item.getElement();
		catRecords = getRootRecords();
		currentTT = (TrackerType) trackerEdge.properties().getPropertyValue(P_TRACKEDGE_INDEX.key());
		if (catRecords.isEmpty()) {
			Dialogs.errorAlert(getProperty().getName(), "Property setting",
					"Not able to edit this property until associated category drivers are defined.");
			return;
		}
		edit();
	}

	private List<Record> getRootRecords() {
		List<Record> result = new ArrayList<>();
		TreeNode trackerNode = (TreeNode) trackerEdge.startNode();
		TreeNode process = trackerNode.getParent();
		if (process != null) {
			@SuppressWarnings("unchecked")
			List<TreeGraphDataNode> categories = (List<TreeGraphDataNode>) get(process.edges(Direction.OUT),
					selectZeroOrMany(hasTheLabel(ConfigurationEdgeLabels.E_APPLIESTO.label())), edgeListEndNodes());
			for (TreeGraphDataNode category : categories) {
				Record record = (Record) get(category.edges(Direction.OUT),
						selectZeroOrOne(orQuery(hasTheLabel(ConfigurationEdgeLabels.E_DRIVERS.label()),
								hasTheLabel(ConfigurationEdgeLabels.E_DECORATORS.label()))),
						endNode());
				if (record != null)
					result.add(record);

			}
		}
		return result;
	}

	// ---------------------------------------------------

	@Override
	public void setValue(String value) {
		getEditor().setText(value);
	}

	@Override
	protected ObservableValue<String> getObservableValue() {
		return getEditor().getTextProperty();
	}

	private void edit() {
		int[][] sizes = Record.collectDims((TreeNode) trackerEdge.endNode());
		if (sizes.length <= 0) {
			// nothing to do
			return;
		}

		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setResizable(true);
		dlg.setTitle(trackerEdge.toShortString() + "#" + P_TRACKEDGE_INDEX.key());
		dlg.initOwner((Window) Dialogs.owner());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane content = new BorderPane();
		GridPane grid = new GridPane();
		content.setCenter(grid);
		dlg.getDialogPane().setContent(content);

		String hintText = "";
		for (int i = 0; i < sizes.length; i++) {
			String txt = "[";
			for (int s : sizes[i]) {
				txt += "0:" + (s - 1) + "|";
			}
			txt = txt.substring(0, txt.length() - 1) + "],";
			hintText += txt;
		}
		hintText = hintText.substring(0, hintText.length() - 1);

		grid.add(new Label("Indexing for " + trackerEdge.endNode().classId() + ":" + trackerEdge.endNode().id()
				+ " (range inclusive)"), 0, 0);

		grid.add(new Label(hintText), 0, 1);
		String s = "";
		for (int i = 0; i < currentTT.size(); i++)
			s += currentTT.getWithFlatIndex(i) + ",";
		s = s.substring(0, s.length() - 1);
		TextField txfInput = new TextField(s);
		grid.add(txfInput, 0, 2);
		Button btnValidate = new Button("Validate");
		grid.add(btnValidate, 1, 2);
		Label lblError = new Label("");
		grid.add(lblError, 0, 3);
		btnValidate.setOnAction((e) -> {
			String result = validateEntry(txfInput.getText(), sizes);
			lblError.setText(result);
			if ("".equals(result))
				dlg.getDialogPane().lookupButton(ok).setDisable(false);
			else
				dlg.getDialogPane().lookupButton(ok).setDisable(true);
		});

		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			String entry = txfInput.getText();
			String value = "([" + sizes.length + "]" + entry + ")";
			setValue(value);
		}
	}

	private static String illegalChars = "[abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMOPQRSTUVWXYZ(*&^$#@!?><~+=)/_]";
	// why not legal chars?

	private String validateEntry(String input, int[][] sizes) {		
		String test = input.replaceAll(illegalChars, "");
		if (!test.equals(input))
			return input + " contains illegal characters.";
		try {
			IndexString.stringToIndex(test, sizes[0]);
		} catch (Exception excpt) {
			return excpt.getMessage();
		}
		return "";

	}
}