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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twcore.data.Field;
import au.edu.anu.twcore.data.Record;
import au.edu.anu.twcore.data.TableNode;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels;
import fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.TrackerType;
import fr.ens.biologie.generic.SaveableAsText;
import fr.ens.biologie.generic.utils.Duple;
import fr.ens.biologie.generic.utils.Tuple;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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

	public TrackerTypeEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public TrackerTypeEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		view = this.getEditor();
		view.setOnAction(e -> onAction());
		catRecords = getRootRecords();
	}

	private Object onAction() {
		if (catRecords.isEmpty()) {
			Dialogs.errorAlert(getProperty().getName(), "Property setting",
					"This property cannot be edited until the\n process drivers are defined.");
			return null;
		}
		BorderPane contents = new BorderPane();
		ListSelectionView<String> listViewData = new ListSelectionView<>();
		ObservableList<String> srcData = listViewData.getSourceItems();
		ObservableList<String> trgData = listViewData.getTargetItems();
		contents.setCenter(listViewData);
		HBox hbox = new HBox();
		contents.setBottom(hbox);

		Map<DataLabel, Integer> items = getDrivers();
		TrackerTypeItem tti = (TrackerTypeItem) this.getProperty();
		TrackerType tt = TrackerType.valueOf((String) tti.getValue());
		fillSrcData(tt, srcData);

		items.entrySet().forEach(entry -> {
			String s = entry.getKey().getEnd();
			if (!trgData.contains(s))
				srcData.add(s);
		});

		if (Dialogs.editList(getProperty().getName(), "", "", contents)) {
			// build a string from the items in the lists.

		}
		return null;
	}

	private TreeGraphDataNode findNode(String id) {
		for (TreeGraphDataNode node: ConfigGraph.getGraph().nodes()) {
			if (node.id().equals(id))
				return node;
		}
		return null;
	}
	private void fillSrcData(TrackerType tt, ObservableList<String> srcData) {
		for (int i = 0; i < tt.size(); i++) {
			String s = tt.getWithFlatIndex(i);
			DataLabel dl = new DataLabel();
			TreeGraphDataNode node = findNode(s);
			//wip
			
//			dl.append(rootRecord.id());
//			buildDataLabel(rootRecord, dl, s);
		}
	}

	private void buildDataLabel(Record record, DataLabel dl, String s) {
		// TODO Auto-generated method stub

	}

	private List<Record> getRootRecords() {
		List<Record> result = new ArrayList<>();
		TrackerTypeItem item = (TrackerTypeItem) getProperty();
		TreeGraphDataNode process = (TreeGraphDataNode) item.getNode().getParent();
		if (process != null) {
			@SuppressWarnings("unchecked")
			List<TreeGraphDataNode> cats = (List<TreeGraphDataNode>) get(process.edges(Direction.OUT),
					selectZeroOrMany(hasTheLabel(ConfigurationEdgeLabels.E_APPLIESTO.label())), edgeListEndNodes());
			for (TreeGraphDataNode cat : cats) {
				Record record = (Record) get(cat.edges(Direction.OUT),
						selectZeroOrOne(hasTheLabel(ConfigurationEdgeLabels.E_DRIVERS.label())), endNode());
				if (record != null)
					result.add(record);
			}
		}
		return result;
	}

	private Map<DataLabel, Integer> getDrivers() {
		// list all fields and tables with their total dimensions.
		List<Field> fieldList = new ArrayList<>();
		List<TableNode> tableList = new ArrayList<>();
		List<TreeGraphDataNode> structList = new ArrayList<>();
		Map<DataLabel, Integer> result = new HashMap<>();
		TrackerTypeItem item = (TrackerTypeItem) getProperty();
		//recurseRecord(0, fieldList, tableList, structList, rootRecord);
		return result;
	}

	private void recurseRecord(int depth, List<Field> fieldList, List<TableNode> tableList,
			List<TreeGraphDataNode> structList, TreeGraphDataNode record) {
		@SuppressWarnings("unchecked")

		List<Field> fields = (List<Field>) get(record.getChildren(),
				selectZeroOrMany(hasTheLabel(ConfigurationNodeLabels.N_FIELD.label())));
		if (depth == 0)
			fieldList.addAll(fields);
//		else
//			structList.add(record);// TODO we don't want to add this if it ONLY has a table
		@SuppressWarnings("unchecked")
		List<TableNode> tables = (List<TableNode>) get(record.getChildren(),
				selectZeroOrMany(hasTheLabel(ConfigurationNodeLabels.N_TABLE.label())));
		for (TableNode table : tables) {
			if (!table.hasChildren())
				if (depth == 0)
					tableList.add(table);
				else
					structList.add(table);
			else // well there can only be one but why not
				for (TreeNode childRecord : table.getChildren())
					recurseRecord(depth + 1, fieldList, tableList, structList, (TreeGraphDataNode) childRecord);
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
