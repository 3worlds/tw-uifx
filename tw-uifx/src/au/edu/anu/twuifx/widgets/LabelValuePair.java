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
package au.edu.anu.twuifx.widgets;


import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twcore.data.runtime.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;

/**
 * @author Ian Davies
 *
 * @date 3 Sep 2019
 */
// listens to what??
public class LabelValuePair 
		extends AbstractDisplayWidget<Property,SimplePropertyList>
		implements Widget {
	
	public LabelValuePair() {
		super(DataMessageTypes.VALUE_PAIR);
	}

	private Label label;
	private Label value;

	@Override
	public Object getUserInterfaceContainer() {
		HBox content = new HBox();
		label = new Label("crap label");
		value = new Label("crap value");
		content.setPadding(new Insets(5, 1, 1, 2));
		content.setSpacing(5);
		content.getChildren().addAll(label, value);
		return content;
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void putPreferences() {
	}

	@Override
	public void getPreferences() {
	}
	@Override
	public void setProperties(String id,SimplePropertyList properties) {
	}

	@Override
	public void onDataMessage(Property data) {
		// TODO Auto-generated method stub
		// when plugged to a simulator through an edge with label "trackTime"
		// this method will receive a pair with label="time" and value=<long>, the current time
		// of the attached simulator (dont forget there could be many simulators)
		data.getKey(); // = "time"
		data.getValue(); // = the current time as a long
	}

	@Override
	public void onMetaDataMessage(SimplePropertyList meta) {
		// TODO Auto-generated method stub
		
	}


}
