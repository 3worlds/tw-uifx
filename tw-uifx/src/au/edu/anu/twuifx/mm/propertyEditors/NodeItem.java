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

import au.edu.anu.rscs.aot.graph.AotNode;
import au.edu.anu.twapps.mm.GraphState;
import au.edu.anu.twapps.mm.Modelable;
import au.edu.anu.twcore.specificationCheck.Checkable;
import javafx.beans.value.ObservableValue;

public class NodeItem implements Item {
	protected AotNode n;
	protected String key;
	protected boolean canEdit;
	protected String category;
	protected Checkable checker;

	public NodeItem(String key, AotNode n, boolean canEdit, String category, Checkable checker) {
		this.n = n;
		this.key = key;
		this.canEdit = canEdit;
		this.category = category;
		this.checker = checker;
	}

	public AotNode getNode() {
		return n;
	}

	@Override
	public boolean isEditable() {
		return canEdit;
	}

	@Override
	public Class<?> getType() {
		return n.getPropertyClass(key);
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getName() {
		return n.uniqueId()+"#"+key;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public Object getValue() {
		return n.getPropertyValue(key);
	}

	@Override
	public void setValue(Object newValue) {
		Object oldValue = getValue();
//		System.out.println(key + "["+oldValue+","+newValue+"]");
		if (!(oldValue.toString().compareTo(newValue.toString())==0)){
			n.addProperty(key, newValue);
			checker.validateGraph();
			GraphState.isChanged(true);
		}
	}

	@Override
	public Optional<ObservableValue<? extends Object>> getObservableValue() {
		return Optional.empty();
	}

}
