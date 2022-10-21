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

// 
package au.edu.anu.twuifx.widgets.helpers;

import au.edu.anu.omhtk.util.IntegerRange;
import au.edu.anu.twcore.data.runtime.*;
import fr.cnrs.iees.omugi.properties.SimplePropertyList;

/**
 * @author Ian Davies - 23 Sep 2019
 */


public class SimCloneWidgetTrackingPolicy implements WidgetTrackingPolicy<TimeData> {
	int nSenders;
	
	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		nSenders = 0;
	}

	@Override
	public boolean canProcessDataMessage(TimeData data) {
		return true;
	}

	@Override
	public boolean canProcessMetadataMessage(Metadata meta) {
		nSenders++;
		return meta.sender()==0;
	}

	@Override
	public IntegerRange getDataMessageRange() {
		return new IntegerRange(0,nSenders);
	}

}
