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

package au.edu.anu.twuifx.mm.propertyEditors.statsType;

import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.*;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;
import impl.org.controlsfx.skin.ListSelectionViewSkin;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.layout.Pane;

/**
 * Property editor for {@link StatsTypeItem}.
 * 
 * @author Ian Davies - 14 Feb. 2019
 */
public class StatsTypeEditor extends AbstractPropertyEditor<String, LabelButtonControl> {

	private LabelButtonControl view;

	/**
	 * @param property The {@link StatsTypeItem}.
	 * @param control The {@link LabelButtonControl}.
	 */
	public StatsTypeEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	/**
	 * @param property The {@link StatsTypeItem}.
	 */
	public StatsTypeEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif",  Images.class.getPackageName()));
		view = this.getEditor();
		view.setOnAction(e -> onAction());
	}

	private Object onAction() {
		StatsTypeItem statsItem = (StatsTypeItem) getProperty();
		String oldString = (String) statsItem.getValue();
		String newString = editTable(oldString);
		if (!newString.equals(oldString)) {
			setValue(newString);
		}

		return null;
	}

	private String editTable(String oldString) {
		StatisticalAggregatesSet sas = StatisticalAggregatesSet.valueOf(oldString);
		ListSelectionView<StatisticalAggregates> listView = new ListSelectionView<>();
		ObservableList<StatisticalAggregates> src = listView.getSourceItems();
		ObservableList<StatisticalAggregates> trg = listView.getTargetItems();
		for (StatisticalAggregates sa : sas.values()) {
			trg.add(sa);
		}
		for (StatisticalAggregates sa : StatisticalAggregates.values()) {
			if (!sas.values().contains(sa))
				src.add(sa);
		}
		if (DialogService.getImplementation().editList(getProperty().getName(), "", "", listView)) {
			/*
			 * Using this dirty trick:
			 * 
			 * https://groups.google.com/forum/#!topic/controlsfx-dev/u556GrRwUSw
			 */
			@SuppressWarnings("unchecked")
			ListSelectionViewSkin<StatisticalAggregates> skin = (ListSelectionViewSkin<StatisticalAggregates>) listView
					.getSkin();
			MultipleSelectionModel<StatisticalAggregates> trgModel = skin.getTargetListView().getSelectionModel();
			trgModel.selectAll();

			if (!trgModel.isEmpty()) {
				sas.values().clear();
				for (StatisticalAggregates sa : trgModel.getSelectedItems()) {
					sas.values().add(sa);
				}
				return sas.toString();
			}
		}
		return oldString;
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
