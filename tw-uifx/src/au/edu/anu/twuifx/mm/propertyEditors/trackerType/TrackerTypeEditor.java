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

import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.data.Field;
import au.edu.anu.twcore.data.Record;
import au.edu.anu.twcore.data.TableNode;
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
import javafx.scene.layout.Pane;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * @author Ian Davies
 *
 * @date 13 Oct 2019
 */
public class TrackerTypeEditor extends AbstractPropertyEditor<String, LabelButtonControl> {
	private enum DataType {
		DT_FIELD, DT_TABLE, DT_STRUCT;
	}

	private LabelButtonControl view;

	public TrackerTypeEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public TrackerTypeEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		view = this.getEditor();
		view.setOnAction(e -> onAction());
	}

	private ListSelectionView<String> buildTab(TabPane tabPane, String title) {
		Tab tab = new Tab(title);
		tabPane.getTabs().add(tab);
		ListSelectionView<String> list = new ListSelectionView<>();
		BorderPane p = new BorderPane();
		tab.setContent(p);
		p.setCenter(list);
		return list;
	}

	private DataType getDataType(String s) {
		// we don't yet know how we will parse this
		// field = does not contain[]
		// table does contain []
		// a structure? Perhaps should begin with a dot and must have [];
		if (!s.contains(SaveableAsText.SQUARE_BRACKETS + ""))
			return DataType.DT_FIELD;
		if (s.contains("."))
			return DataType.DT_STRUCT;
		return DataType.DT_TABLE;
	}

	private Object onAction() {
		Tuple<List<Field>, List<TableNode>, List<TreeGraphDataNode>> items = getDrivers();

		TabPane tabPane = new TabPane();
		ListSelectionView<String> listViewFields = buildTab(tabPane, "Fields");
		ObservableList<String> srcFields = listViewFields.getSourceItems();
		ObservableList<String> trgFields = listViewFields.getTargetItems();

		ListSelectionView<String> listViewTables = buildTab(tabPane, "Tables");
		ObservableList<String> srcTables = listViewTables.getSourceItems();
		ObservableList<String> trgTables = listViewTables.getTargetItems();

		ListSelectionView<String> listViewStruct = buildTab(tabPane, "Structures");
		ObservableList<String> srcStruct = listViewStruct.getSourceItems();
		ObservableList<String> trgStruct = listViewStruct.getTargetItems();

		TrackerTypeItem tti = (TrackerTypeItem) this.getProperty();
		TrackerType tt = TrackerType.valueOf((String) tti.getValue());

		for (int i = 0; i < tt.size(); i++) {
			String s = tt.getWithFlatIndex(i);
			switch (getDataType(s)) {
			case DT_FIELD: {
				trgFields.add(s);
				break;
			}
			case DT_TABLE: {
				trgTables.add(s);// no way of knowing the indexing
				break;
			}
			case DT_STRUCT: {
				trgStruct.add(s);// no way of knowing the indexing
				break;
			}
			default: {
				throw new TwuifxException("Unrecognized tracking entry: '" + s + "'.");
			}
			}
		}
		// WIP!
		// simple fields of the root record
		for (Field field : items.getFirst()) {
			String s = field.id();
			if (!trgFields.contains(s))
				srcFields.add(s);
		}
		// simple tables of the root record
		// we will need some index selection ui
		for (TableNode table : items.getSecond()) {
			String s = table.id();
			srcTables.add(s);
		}
		// recursive structures of tables and records
		// we will need some index selection ui
		for (TreeGraphDataNode struc : items.getThird()) {
			// for the moment i just add a '.' in front to indicate its nature.
			String s = "." + struc.id();
			srcStruct.add(s);
		}

		if (Dialogs.editList(getProperty().getName(), "", "", tabPane)) {
			// build a string from the items in the lists.

		}
		return null;
	}

	private Tuple<List<Field>, List<TableNode>, List<TreeGraphDataNode>> getDrivers() {
		List<Field> fieldList = new ArrayList<>();
		List<TableNode> tableList = new ArrayList<>();
		List<TreeGraphDataNode> structList = new ArrayList<>();
		Tuple<List<Field>, List<TableNode>, List<TreeGraphDataNode>> result = new Tuple<List<Field>, List<TableNode>, List<TreeGraphDataNode>>(
				fieldList, tableList, structList);
		TrackerTypeItem item = (TrackerTypeItem) getProperty();
		TreeGraphDataNode process = (TreeGraphDataNode) item.getNode().getParent();
		if (process != null) {
			@SuppressWarnings("unchecked")
			List<TreeGraphDataNode> cats = (List<TreeGraphDataNode>) get(process.edges(Direction.OUT),
					selectZeroOrMany(hasTheLabel(ConfigurationEdgeLabels.E_APPLIESTO.label())), edgeListEndNodes());
			for (TreeGraphDataNode cat : cats) {
				Record record = (Record) get(cat.edges(Direction.OUT),
						selectZeroOrOne(hasTheLabel(ConfigurationEdgeLabels.E_DRIVERS.label())), endNode());
				if (record != null) {
					recurseRecord(0, fieldList, tableList, structList, record);
				}
			}
		}
		return result;
	}

	private void recurseRecord(int depth, List<Field> fieldList, List<TableNode> tableList,
			List<TreeGraphDataNode> structList, TreeGraphDataNode record) {
		@SuppressWarnings("unchecked")
		List<Field> fields = (List<Field>) get(record.getChildren(),
				selectZeroOrMany(hasTheLabel(ConfigurationNodeLabels.N_FIELD.label())));
		if (depth == 0)
			fieldList.addAll(fields);
		else
			structList.add(record);// TODO we don't want to add this if it ONLY has a table
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
