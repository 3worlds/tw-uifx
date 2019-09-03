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

import java.util.List;

import au.edu.anu.twcore.ui.runtime.AbstractWidget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.twcore.constants.TimeScaleType;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */
public class TimeDisplayWidget extends AbstractWidget{
	private TimeUnits smallest;
	private Label lblTime;
	private TimeScaleType timeScale;
	private List<TimeUnits> units;
	private long startTime;

//	PayloadReader reader = metadata.payloadReader();
//
//	startTime = reader.readLong();
//	timeScale = TimeScaleType.valueOf(reader.readString());
//	TimeUnits largest = TimeUnits.valueOf(reader.readString());
//	smallest = TimeUnits.valueOf(reader.readString());
//	SortedSet<TimeUnits> allowable = TimeScaleType.validTimeUnits(timeScale);
//	units = new ArrayList<>();
//	for (TimeUnits allowed : allowable) {
//		if (allowed.compareTo(largest) <= 0 && allowed.compareTo(smallest) >= 0) {
//			units.add(allowed);
//		}
//	}
//	// ensure sorted largest to smallest
//	units.sort((first, second) -> {
//		return second.compareTo(first);
//	});

	@Override
	public void setProperties(String id,SimplePropertyList properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getUserInterfaceContainer() {
		HBox content = new HBox();
		// top, right, bottom, and left padding
		content.setPadding(new Insets(5, 1, 1, 2));
		content.setSpacing(5);
		//content.getChildren().addAll(tdm.getTimeLabel());
		return content;

	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void putPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPreferences() {
		// TODO Auto-generated method stub
		
	}


}
