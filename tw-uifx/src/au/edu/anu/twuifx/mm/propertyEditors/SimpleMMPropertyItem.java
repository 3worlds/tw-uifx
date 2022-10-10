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

import java.util.Objects;
import java.util.Optional;
import org.controlsfx.control.PropertySheet.Item;

import au.edu.anu.twapps.mm.*;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twcore.graphState.GraphState;
import fr.cnrs.iees.graph.ElementAdapter;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.properties.SimplePropertyList;
import javafx.beans.value.ObservableValue;

/**
 * An adaptor for {@link Item} for any element in a ModelMaker configuration
 * file whose class is handled natively by Controlsfx (e.g All numbers, Strings
 * and Enums).
 * <p>
 * Note: For String properties, an update event is fired for every key stroke.
 * This is a problem because each update event causes a re-compile of the
 * configuration file. This means typing fast will over-load the thread.
 * Therefore, large strings such as Descriptions, should be handled by purpose
 * built property editor.
 * </p>
 * <p>
 * Currently, for String fields, update events are only enacted when focus
 * leaves the field.
 * 
 * @author Ian Davies - 14 Feb. 2019
 *
 */
public class SimpleMMPropertyItem implements Item {
	protected ElementAdapter element;
	protected String key;
	protected boolean isEditable;
	protected String category;
	protected SimplePropertyList properties;
	private String description;
	private MMController controller;

	/**
	 * 
	 * @param controller  ModelMaker controller, used to coordinated updates across
	 *                    two property sheets.
	 * @param key         The unique key of the property in the element's property
	 *                    list.
	 * @param element     The element (Node or Edge) containing the property list.
	 * @param canEdit     True if editing of this property is allowed, false
	 *                    otherwise.
	 * @param category    The sub-tree to which this element belongs. This is used
	 *                    in the property sheet to categorized items.
	 * @param description Not implemented. Intended as help info for the property.
	 */
	public SimpleMMPropertyItem(MMController controller, String key, ElementAdapter element, boolean canEdit,
			String category, String description) {
		this.element = element;
		this.key = key;
		this.isEditable = canEdit;
		this.category = category;
		this.description = description;
		this.controller = controller;
		properties = Objects.requireNonNull(getElementProperties());
	}

	@Override
	public boolean isEditable() {
		return isEditable;
	}

	@Override
	public Class<?> getType() {
		return properties.getPropertyClass(key);
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getName() {
		return element.id() + "#" + key;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Object getValue() {
		return properties.getPropertyValue(key);
	}

	@Override
	public void setValue(Object newValue) {
		Object oldValue = getValue();
//		log.info("Before compare: " + getName() + "| OLD:NEW\t[" + oldValue + "," + newValue + "]");
		if (!(oldValue.toString().compareTo(newValue.toString()) == 0)) {
//			log.info("After compare: " + getName() + "| OLD:NEW\t[" + oldValue + "," + newValue + "]");
			onUpdateProperty(newValue);
		}
	}

	@Override
	public Optional<ObservableValue<? extends Object>> getObservableValue() {
		return Optional.empty();
	}

	protected void onUpdateProperty(Object value) {
		properties.setProperty(key, value);
		controller.onItemEdit(this);
		GraphState.setChanged();
		if (!(value instanceof String))
			ConfigGraph.verifyGraph();
	}

	private SimplePropertyList getElementProperties() {
		if (element instanceof TreeGraphDataNode) {
			TreeGraphDataNode node = (TreeGraphDataNode) element;
			return node.properties();
		} else if (element instanceof ALDataEdge) {
			ALDataEdge edge = (ALDataEdge) element;
			return edge.properties();
		}
		return null;

	}
}
