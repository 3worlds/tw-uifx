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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.rscs.aot.collections.tables.Dimensioner;
import au.edu.anu.rscs.aot.collections.tables.IndexString;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.data.DimNode;
import au.edu.anu.twcore.data.FieldNode;
import au.edu.anu.twcore.data.Record;
import au.edu.anu.twcore.data.TableNode;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.ui.TrackFieldEdge;
import au.edu.anu.twcore.ui.TrackPopulationEdge;
import au.edu.anu.twcore.ui.TrackTableEdge;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
import fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels;
import fr.cnrs.iees.twcore.constants.TrackerType;
import fr.ens.biologie.generic.utils.Duple;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
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

	private LabelButtonControl view;
	private List<Record> catRecords;
	private Map<DataLabel, Integer> drivers;
	private List<Duple<CheckBox, Spinner<Integer>>> indices;
	private ALDataEdge trackerEdge;
	private TrackerType currentTT;
	/*- element is either a 
	 * 1) TrackField (edge classId E_TRACKFIELD must have endNode classId N_FIELD)
	 * 2) TrackTable (edge classId E_TRACKTABLE must have endNode N_TABLE - must have DataElementType property)
	 * 3) TrackPopulation (edge classId E_TRACKPOP. Must have endNode : N_INITIALSTATE, N_GROUP, N_COMPONENT (componententTYpe must be permanent)
	 * wait and see for 3). 
	 */

	public TrackerTypeEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public TrackerTypeEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		view = this.getEditor();
		view.setOnAction(e -> onAction());
	}

	private Object onAction() {
		TrackerTypeItem item = (TrackerTypeItem) getProperty();
		trackerEdge = (ALDataEdge) item.getElement();
		catRecords = getRootRecords();

		currentTT = (TrackerType) trackerEdge.properties().getPropertyValue(P_TRACKEDGE_INDEX.key());

		if (catRecords.isEmpty()) {
			Dialogs.errorAlert(getProperty().getName(), "Property setting",
					"Not able to edit this property until associated category drivers are defined.");
			return null;
		}
		if (trackerEdge.classId().equals(E_TRACKFIELD.label()))
			return editTrackField((TrackFieldEdge) trackerEdge);
		else if (trackerEdge.classId().equals(E_TRACKTABLE.label()))
			return editTrackTable((TrackTableEdge) trackerEdge);
		else
			return editTrackPopulation((TrackPopulationEdge) trackerEdge);
	}

	private int[][] collectDims(TreeNode parent) {
//		List<DimNode> dimList = new ArrayList<>();
		List<int[]> dimList = new ArrayList<>();
		while (parent != null) {
			if (parent instanceof TableNode) {
				// presumably, these are now sorted
				Dimensioner[] dims = ((TableNode) parent).dimensioners();
				int[] dd = new int[dims.length];
				dimList.add(dd);
				for (int j = 0; j < dd.length; j++)
					dd[j] = dims[j].getLength();
			}
			parent = parent.getParent();
		}

		int[][] result = new int[dimList.size()][];
		// reverse the order - TODO check
		for (int i = dimList.size() - 1; i >= 0; i--) {
			int[] dd = dimList.get(i);
			result[result.length - i - 1] = dd;
		}
		return result;
	}

	private Object editTrackPopulation(TrackPopulationEdge trackerEdge) {
		Dialogs.errorAlert(trackerEdge.id(), "Not yet implemented", "");
		return null;
	}

	private Object editTrackTable(TrackTableEdge trackerEdge) {
		runDlg();
		return null;
	}

	private Object editTrackField(TrackFieldEdge trackEdge) {
		runDlg();
		return null;
	}

	private boolean nameMismatch(int[][] sizes) {
		if (sizes.length <= 0)
			return (!"".equals(currentTT.getWithFlatIndex(0)));
		else {
			try {
				for (int i = 0; i < currentTT.size(); i++) {
					String indexStr = currentTT.getWithFlatIndex(0);
					IndexString.stringToIndex(indexStr, sizes[i]);
				}
				return false;
			} catch (Exception e) {
				return true;
			}
		}
	}

	private void updateEntry(int[][] sizes) {
		// need a new TrackerType
		Dimensioner[] dims = { new Dimensioner(sizes.length) };
		TrackerType newTT = new TrackerType(dims);
		for (int i = 0; i < sizes.length; i++) {
			String newEntry = "[";
			for (int j = 0; j < sizes[i].length; j++)
				newEntry += "|";
			newEntry = newEntry.substring(0, newEntry.length() - 1);
			newEntry += "]";
			newTT.setWithFlatIndex(newEntry, i);
		}
		currentTT = newTT;
		setValue(currentTT.toString());
		GraphState.setChanged();
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
						selectZeroOrOne(hasTheLabel(ConfigurationEdgeLabels.E_DRIVERS.label())), endNode());
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

	private void runDlg() {
		// needs to be int[][] i.e. tables by indexes
		int[][] sizes = collectDims((TreeNode) trackerEdge.endNode());
		if (nameMismatch(sizes))
			updateEntry(sizes);
		if (sizes.length <= 0) {
			// nothing to do
			return;
		}

		// Ok put up a textfield to take and validate the user string
		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setResizable(true);
		dlg.setTitle(trackerEdge.classId() + ":" + trackerEdge.id());
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
				txt += "0;" + (s - 1) + "|";
			}
			txt = txt.substring(0, txt.length() - 1) + "],";
			hintText += txt;
		}
		hintText = hintText.substring(0, hintText.length() - 1);

		grid.add(new Label(
				"Indexing for " + trackerEdge.endNode().classId() + ":" + trackerEdge.endNode().id() + " (Inclusive)"),
				0, 0);

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
		});

		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			String vstr = validateEntry(txfInput.getText(), sizes);
			if (!"".equals(vstr))
				return;
			String entry = txfInput.getText();
			String value = "(["+sizes.length+"]" + entry + ")";
			setValue(value);
		}
	}

	private String validateEntry(String input, int[][] sizes) {
		String[] parts = input.split(",");
		if (parts.length != sizes.length) {
			return parts.length + " terms entered but " + sizes.length + " required.";
		}
		for (int i = 0; i < parts.length; i++) {
			try {
				IndexString.stringToIndex(parts[i], sizes[i]);
			} catch (Exception excpt) {
				return excpt.getMessage();
			}
		}
		return "";

	}
}