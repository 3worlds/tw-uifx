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

	private int[] collectDims(TreeNode leaf) {
		List<DimNode> dimList = new ArrayList<>();
		collectDims(leaf, dimList);
		int[] result = new int[dimList.size()];
		// reverse the order - TODO check
		for (int i = dimList.size() - 1; i >= 0; i--) {
			DimNode dm = dimList.get(i);
			int size = (Integer) dm.properties().getPropertyValue(P_DIMENSIONER_SIZE.key());
			result[result.length - i - 1] = size;
		}
		return result;
	}

	private void collectDims(TreeNode node, List<DimNode> result) {
		if (node instanceof FieldNode) {
			collectDims(node.getParent(), result);

		} else if (node instanceof TableNode) {
			TableNode table = (TableNode) node;
			@SuppressWarnings("unchecked")
			List<DimNode> dims = (List<DimNode>) get(table.edges(Direction.OUT), edgeListEndNodes(),
					selectZeroOrMany(hasTheLabel(N_DIMENSIONER.label())));
			// sort within table order
			// TODO this will be wrong for multi-table trees as deepest tables should be
			// first (ie. closest to DataDef node)
			dims.sort(new Comparator<DimNode>() {

				@Override
				public int compare(DimNode d1, DimNode d2) {
					int o1 = (Integer) d1.properties().getPropertyValue(P_DIMENSIONER_SIZE.key());
					int o2 = (Integer) d2.properties().getPropertyValue(P_DIMENSIONER_SIZE.key());
					if (o1 > o2)
						return -1;
					else if (o2 > o1)
						return 1;
					else
						return 0;
//					return Integer.compare(o1, o2);
				}
			});
			result.addAll(dims);
			collectDims(node.getParent(), result);
		} else if (node instanceof Record) {
			if (!node.getParent().classId().equals(N_DATADEFINITION.label())) {
				collectDims(node.getParent(), result);
			}
		}

	}

	private Object editTrackPopulation(TrackPopulationEdge trackerEdge) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object editTrackTable(TrackTableEdge trackerEdge) {
		int[] sizes = collectDims((TreeNode) trackerEdge.endNode());
		if (nameMismatch(sizes))
			updateEntry(sizes);

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
		String txt = "[";
		for (int s : sizes) {
			txt += "0.." + (s - 1) + "|";
		}
		txt = txt.substring(0, txt.length() - 1) + "]";
		grid.add(new Label(
				"Indexing for " + trackerEdge.endNode().classId() + ":" + trackerEdge.endNode().id() + " (Inclusive)"),
				0, 0);

		grid.add(new Label(txt), 0, 1);
		TextField txfInput = new TextField(currentTT.getWithFlatIndex(0));
		grid.add(txfInput, 0, 2);
		Button btnValidate = new Button("Validate");
		grid.add(btnValidate, 1, 2);
		Label lblError = new Label("");
		grid.add(lblError, 0, 3);

		btnValidate.setOnAction((e) -> {
			try {
				IndexString.stringToIndex(txfInput.getText(), sizes);
				lblError.setText("");
			} catch (Exception excpt){
				lblError.setText(excpt.getMessage());
			}
		});

		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			try {
				IndexString.stringToIndex(txfInput.getText(), sizes);
			} catch (Exception exctp) {
				Dialogs.errorAlert(trackerEdge.classId() + ":" + trackerEdge.id(), "Format error", exctp.getMessage());
				return null;
			}
			String entry = txfInput.getText();
			String value = "([1]"+entry+")";	
			setValue(value);
		}
		return null;
	}

	private Object editTrackField(TrackFieldEdge trackEdge) {
		int[] sizes = collectDims((TreeNode) trackEdge.endNode());
		if (nameMismatch(sizes))
			updateEntry(sizes);

		if (sizes.length <= 0)
			return null;
		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setTitle(trackerEdge.classId() + ":" + trackerEdge.id());
		dlg.initOwner((Window) Dialogs.owner());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane content = new BorderPane();
		GridPane grid = new GridPane();
		content.setCenter(grid);
		dlg.getDialogPane().setContent(content);
		String txt = "[";
		for (int s : sizes) {
			txt += "0.." + (s - 1) + "|";
		}
		txt = txt.substring(0, txt.length() - 1) + "]";
		grid.add(new Label(
				"Indexing for " + trackerEdge.endNode().classId() + ":" + trackerEdge.endNode().id() + " (Inclusive)"),
				0, 0);

		grid.add(new Label(txt), 0, 1);
		grid.add(new TextField(currentTT.getWithFlatIndex(0)), 0, 2);
		grid.add(new Button("Validate"), 1, 2);

		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {

		}
		return null;
	}

	private boolean nameMismatch(int[] sizes) {
		if (sizes.length <= 0)
			return (!"".equals(currentTT.getWithFlatIndex(0)));
		else {
			String indexStr = currentTT.getWithFlatIndex(0);
			try {
				IndexString.stringToIndex(indexStr, sizes);
				return false;
			} catch (Exception e) {
				return true;
			}
		}
	}

	private void updateEntry(int[] sizes) {
		String newEntry = "";
		if (sizes.length > 0) {
			// [||||... etc]
			newEntry = "[";
			for (int i : sizes)
				newEntry += "|";
			newEntry = newEntry.substring(0, newEntry.length() - 1);
			newEntry += "]";
		}
		currentTT.setWithFlatIndex(newEntry, 0);
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

}
