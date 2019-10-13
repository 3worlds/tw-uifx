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
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels;
import fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.TrackerType;
import fr.ens.biologie.generic.utils.Duple;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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

	public TrackerTypeEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	public TrackerTypeEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.imagePackage));
		view = this.getEditor();
		view.setOnAction(e -> onAction());
	}

	private Object onAction() {
		Duple<List<Field>, List<TableNode>> items = getDrivers();
		ListSelectionView<String> listViewFields = new ListSelectionView<>();
		ListSelectionView<String> listViewTables = new ListSelectionView<>();
		ObservableList<String> srcFields = listViewFields.getSourceItems();
		ObservableList<String> trgFields = listViewFields.getTargetItems();
		TrackerTypeItem tti = (TrackerTypeItem) this.getProperty();
		TrackerType tt = TrackerType.valueOf((String) tti.getValue());
		for (int i = 0; i < tt.size(); i++) {
			String s = tt.getWithFlatIndex(i);
			srcFields.add(s);
		}
		for (Field field : items.getFirst()) {
			String s = field.id();
			if (!srcFields.contains(s))
				trgFields.add(s);
		}
		if (Dialogs.editList(getProperty().getName(), "", "", listViewFields)) {
		}
		return null;
	}

	private Duple<List<Field>, List<TableNode>> getDrivers() {
		List<Field> fieldList = new ArrayList<>();
		List<TableNode> tableList = new ArrayList<>();
		Duple<List<Field>, List<TableNode>> result = new Duple<List<Field>, List<TableNode>>(fieldList, tableList);
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
					recurseRecord(fieldList, tableList, record);
				}
			}
		}
		return result;
	}

	private void recurseRecord(List<Field> fieldList, List<TableNode> tableList, TreeNode record) {
		@SuppressWarnings("unchecked")
		List<Field> fields = (List<Field>) get(record.getChildren(),
				selectZeroOrMany(hasTheLabel(ConfigurationNodeLabels.N_FIELD.label())));
		fieldList.addAll(fields);
		@SuppressWarnings("unchecked")
		List<TableNode> tables = (List<TableNode>) get(record.getChildren(),
				selectZeroOrMany(hasTheLabel(ConfigurationNodeLabels.N_TABLE.label())));
		for (TableNode table : tables) {
			if (!table.hasChildren())
				tableList.add(table);
			else // well there can only be one but why not
				for (TreeNode r : table.getChildren())
					recurseRecord(fieldList, tableList, r);
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
