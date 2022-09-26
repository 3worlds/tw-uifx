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

import au.edu.anu.twcore.data.runtime.TwData;
import javafx.beans.value.ObservableValue;

/**
 * Author Ian Davies
 *
 * Date 14 Feb. 2019
 */
public class SimpleMRPropertyItem implements Item {
	private String containerId;
	private String description;
	private String key;
	private TwData data;

	/**
	 * @param data TODO: Not implemented
	 * @param key TODO: Not implemented
	 * @param containerId TODO: Not implemented
	 * @param description TODO: Not implemented
	 */
	public SimpleMRPropertyItem(TwData data, String key, String containerId, String description) {
		this.data = data;
		this.key = key;
		this.containerId = containerId;
		this.description = description;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public Class<?> getType() {
		return data.getPropertyClass(key);
	}

	@Override
	public String getCategory() {
		return containerId;
	}

	@Override
	public String getName() {
		return key;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Object getValue() {
		return data.getPropertyValue(key);
	}

	@Override
	public void setValue(Object newValue) {
		//Object oldValue = getValue();
		data.writeEnable();
		data.setProperty(key, newValue);
		data.writeDisable();
	}

	@Override
	public Optional<ObservableValue<? extends Object>> getObservableValue() {
		return Optional.empty();
	}

}
