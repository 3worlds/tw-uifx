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

package au.edu.anu.twuifx.mm.propertyEditors;

import java.util.Optional;

import org.controlsfx.control.PropertySheet.Item;

import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
//import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twcore.graphState.GraphState;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import javafx.beans.value.ObservableValue;

/**
 * Author Ian Davies
 *
 * Date 14 Feb. 2019
 */
public class SimplePropertyItem implements Item {
	protected TreeGraphDataNode node;
	protected String key;
	protected boolean isEditable;
	protected String category;
	private String description;
	protected IMMController controller;

	public SimplePropertyItem(IMMController controller, String key, TreeGraphDataNode n, boolean canEdit, String category, String description) {
		this.node = n;
		this.key = key;
		this.isEditable = canEdit;
		this.category = category;
		this.description=description;
		this.controller = controller;
	}

	@Override
	public boolean isEditable() {
		return isEditable;
	}

	@Override
	public Class<?> getType() {
		return node.properties().getPropertyClass(key);
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getName() {
		return node.classId()+":"+node.id() + "#" + key;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Object getValue() {
		return node.properties().getPropertyValue(key);
	}

	@Override
	public void setValue(Object newValue) {
		Object oldValue = getValue();
		if (!(oldValue.toString().compareTo(newValue.toString()) == 0)) {
			node.properties().setProperty(key, newValue);
			GraphState.setChanged();
			ConfigGraph.validateGraph();
			controller.onItemEdit(this);
		}
	}

	@Override
	public Optional<ObservableValue<? extends Object>> getObservableValue() {
		return Optional.empty();
	}
}
