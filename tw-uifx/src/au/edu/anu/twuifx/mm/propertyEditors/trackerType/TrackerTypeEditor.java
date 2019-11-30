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
import java.util.Map;
import java.util.Stack;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twcore.data.FieldNode;
import au.edu.anu.twcore.data.Record;
import au.edu.anu.twcore.data.TableNode;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.ui.TrackFieldEdge;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import fr.cnrs.iees.twcore.constants.TrackerType;
import fr.ens.biologie.generic.utils.Duple;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * @author Ian Davies
 *
 * @date 13 Oct 2019
 */
public class TrackerTypeEditor extends AbstractPropertyEditor<String, LabelButtonControl> {

	private LabelButtonControl view;
	//private List<Record> catRecords;
	private Map<DataLabel, Integer> drivers;
	private List<Duple<CheckBox, Spinner<Integer>>> indices;
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
		//catRecords = getRootRecords();
	}

	private Object onAction() {
//		if (catRecords.isEmpty()) {
//			Dialogs.errorAlert(getProperty().getName(), "Property setting",
//					"Not implemented yet.");
//			return null;
//		}
		BorderPane contents = new BorderPane();
		ListView<String> inputList = new ListView<>();
		ListView<String> outputList = new ListView<>();
		inputList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		outputList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		HBox hbox = new HBox();
		contents.setBottom(hbox);
		GridPane grid = new GridPane();
		contents.setCenter(grid);
		grid.add(inputList, 0, 0);
		VBox btnBx = new VBox();
		grid.add(btnBx, 1, 0);
		grid.add(outputList, 2, 0);
		Button btnTo = new Button(">");
		Button btnFrom = new Button("<");
		btnTo.setOnAction(e -> {
			String item = inputList.getSelectionModel().getSelectedItem();
			if (item != null) {
				item += getCurrentIndexStr();
				if (!outputList.getItems().contains(item)) {
					outputList.getItems().add(item);
					if (!item.contains("[")) {
						inputList.getItems().remove(item);
					}

				}
			}
		});
		btnFrom.setOnAction(e -> {
			String item = outputList.getSelectionModel().getSelectedItem();
			if (item != null) {
				outputList.getItems().remove(item);
				if (!item.contains("["))// only move 0d back to list
					if (!inputList.getItems().contains(item)) {
						inputList.getItems().add(item);
					}
			}
		});

		btnBx.getChildren().addAll(btnTo, btnFrom);

		TrackerTypeItem tti = (TrackerTypeItem) getProperty();
		if (tti.getElement() instanceof TrackFieldEdge) {
			// fields and tables don't require a list in input options as there is only one: a table or a field
			// In this case we need to know the total dimensions of the structure.
			TrackFieldEdge edge = (TrackFieldEdge)tti.getElement();		
			FieldNode fieldNode = (FieldNode) edge.endNode();
			TrackerType tt = TrackerType.valueOf((String) tti.getValue());
			// we just add the id of the field/table node to the input list
			// thin inputlist is 
			return null;
		} else 
			return null;
		
		//fillOutputList(tt, outputList);
//		drivers = getDrivers();
		//int maxDim = fillInputList(inputList, outputList);

		//indices = new ArrayList<>();
//		for (int i = 0; i < maxDim; i++) {
//			CheckBox cb = new CheckBox("const.");
//			Spinner<Integer> sp = new Spinner<>();
//			sp.setEditable(true);
//			SpinnerValueFactory<Integer> factory;
//			factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, (int) 0);
//			sp.setValueFactory(factory);
//			sp.setMaxWidth(100);
//			VBox vbox = new VBox();
//			vbox.getChildren().addAll(cb, sp);
//			hbox.getChildren().add(vbox);
//			indices.add(new Duple<CheckBox, Spinner<Integer>>(cb, sp));
//			cb.selectedProperty().addListener((observable, oldValue, newValue) -> {
//				if (newValue)
//					sp.setVisible(true);
//				else
//					sp.setVisible(false);
//			});
//			cb.setSelected(true);
//		}

//		MultipleSelectionModel<String> srcModel = inputList.getSelectionModel();
//		srcModel.selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
//			int dim = getDims(srcModel.getSelectedItem());
//			for (int i = 0; i < indices.size(); i++) {
//				Duple<CheckBox, Spinner<Integer>> idxctrl = indices.get(i);
//				if (dim > i) {
//					idxctrl.getFirst().setVisible(true);
//					idxctrl.getFirst().setSelected(true);
//				} else {
//					idxctrl.getFirst().setVisible(false);
//					idxctrl.getFirst().setSelected(false);
//				}
//			}
//		});
//
//		if (Dialogs.editList(getProperty().getName(), "", "", contents)) {
//			Dimensioner dim = new Dimensioner(outputList.getItems().size());
//			Dimensioner[] dims = { dim };
//			TrackerType result = new TrackerType(dims);
//			for (int i = 0; i < outputList.getItems().size(); i++)
//				result.set(outputList.getItems().get(i), i);
//			setValue(result.toString());
//		}
//		return null;
	}

	private String getCurrentIndexStr() {
		String result = "";
		int dims = 0;
		for (Duple<CheckBox, Spinner<Integer>> ctrls : indices) {
			if (ctrls.getFirst().isVisible()) {
				dims++;
				result += "|";
				if (ctrls.getFirst().isSelected()) {
					String idx = ctrls.getSecond().getValue().toString();
					result += idx;
				}
			}
		}
		if (dims > 0) {
			return result.replaceFirst("\\|", "[") + "]";
		} else
			return result;
	}

	private int fillInputList(ListView<String> inputList, ListView<String> outputList) {
		int maxDim = 0;
		for (Map.Entry<DataLabel, Integer> entry : drivers.entrySet()) {
			int dim = entry.getValue();
			maxDim = Math.max(maxDim, dim);
			String s = entry.getKey().getEnd();
			if (!outputList.getItems().contains(s))
				inputList.getItems().add(entry.getKey().getEnd());
		}
		return maxDim;
	}

	private int getDims(String id) {
		for (Map.Entry<DataLabel, Integer> entry : drivers.entrySet()) {
			if (entry.getKey().getEnd().equals(id))
				return entry.getValue();
		}
		return -1;
	}

	private TreeGraphDataNode findNode(String id) {
		for (TreeGraphDataNode node : ConfigGraph.getGraph().nodes()) {
			if (node.id().equals(id))
				return node;
		}
		return null;
	}

	private void fillOutputList(TrackerType tt, ListView<String> list) {
		for (int i = 0; i < tt.size(); i++) 
				list.getItems().add(tt.getWithFlatIndex(i));
		}

	private Duple<DataLabel, Integer> getEntry(TreeNode node) {
		int dims = 0;
		if (node instanceof TableNode)
			dims += countDims((TableNode) node);
		// duplicate code from here
		Stack<TreeNode> stack = new Stack<>();
		stack.push(node);
		while (node.getParent() != null && !node.getParent().classId().equals(N_DATADEFINITION.label())) {
			node = node.getParent();
			stack.push(node);
			if (node instanceof TableNode)
				dims += countDims((TableNode) node);
		}
		DataLabel dl = new DataLabel();
		while (!stack.isEmpty()) {
			TreeNode p = stack.pop();
			dl.append(p.id());
		}
		return new Duple<>(dl, dims);
	}

	private List<Record> getRootRecords() {
		List<Record> result = new ArrayList<>();
//		TrackerTypeItem item = (TrackerTypeItem) getProperty();
//		TreeGraphDataNode trackerNode = (TreeGraphDataNode) item.getElement();
//		TreeNode process = trackerNode.getParent();
//		if (process != null) {
//			@SuppressWarnings("unchecked")
//			List<TreeGraphDataNode> cats = (List<TreeGraphDataNode>) get(process.edges(Direction.OUT),
//					selectZeroOrMany(hasTheLabel(ConfigurationEdgeLabels.E_APPLIESTO.label())), edgeListEndNodes());
//			for (TreeGraphDataNode cat : cats) {
//				Record record = (Record) get(cat.edges(Direction.OUT),
//						selectZeroOrOne(hasTheLabel(ConfigurationEdgeLabels.E_DRIVERS.label())), endNode());
//				if (record != null)
//					result.add(record);
//			}
//		}
		return result;
	}

//	private Map<DataLabel, Integer> getDrivers() {
//		Map<DataLabel, Integer> result = new HashMap<>();
//		for (Record record : catRecords) {
//			recurseRecord(record, result);
//		}
//		return result;
//	}

	private void recurseRecord(Record record, Map<DataLabel, Integer> items) {
		@SuppressWarnings("unchecked")

		List<FieldNode> fields = (List<FieldNode>) get(record.getChildren(), selectZeroOrMany(hasTheLabel(N_FIELD.label())));
		for (FieldNode field : fields) {
			Duple<DataLabel, Integer> entry = getEntry(field);
			items.put(entry.getFirst(), entry.getSecond());
		}
		@SuppressWarnings("unchecked")
		List<TableNode> tables = (List<TableNode>) get(record.getChildren(),
				selectZeroOrMany(hasTheLabel(N_TABLE.label())));
		for (TableNode table : tables) {
			if (!table.hasChildren()) {
				Duple<DataLabel, Integer> entry = getEntry(table);
				items.put(entry.getFirst(), entry.getSecond());
			} else
				// there can only be one but...
				for (TreeNode child : table.getChildren()) {
					recurseRecord((Record) child, items);
				}
		}
	}

	private int countDims(TableNode n) {
		int count = 0;
		for (ALEdge edge : n.edges(Direction.OUT))
			count++;
		return count;
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
