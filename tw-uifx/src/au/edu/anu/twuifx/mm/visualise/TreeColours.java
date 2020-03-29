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
package au.edu.anu.twuifx.mm.visualise;

import java.util.HashMap;
import java.util.Map;

import fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels;
import javafx.scene.paint.Color;

public class TreeColours {
	/*http://www.color-blindness.com/coblis-color-blindness-simulator/*/
	private static Map<String,Color> nodeColours= new HashMap<>();
	static {
		nodeColours.put(ConfigurationNodeLabels.N_DYNAMICS.label(), Color.LIME);
		nodeColours.put(ConfigurationNodeLabels.N_STRUCTURE.label(), Color.GREEN);
		nodeColours.put(ConfigurationNodeLabels.N_DATADEFINITION.label(), Color.LIGHTGREY);
		nodeColours.put(ConfigurationNodeLabels.N_EXPERIMENT.label(), Color.GOLDENROD);
		nodeColours.put(ConfigurationNodeLabels.N_UI.label(), Color.WHITE);
	}
	public static Color getCategoryColor(String category, String label){
		if (!nodeColours.containsKey(category))
			if (label.equals(ConfigurationNodeLabels.N_SYSTEM.label()))
				return Color.TEAL;
			else
				return Color.BLACK;
		return nodeColours.get(category);
	}

}
