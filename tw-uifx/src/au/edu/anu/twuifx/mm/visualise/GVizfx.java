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

import au.edu.anu.twapps.graphviz.GVisable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.text.Font;

/**
 * Author Ian Davies
 *
 * Date 12 Dec. 2018
 */

// We could do the old impl trick here or better just implement an interface. Must not expose fx!

public class GVizfx implements GVisable{
	private static int fontSize;
	private static Font font;
	private static IntegerProperty nodeRadiusProperty = new SimpleIntegerProperty(0);

	public static void setFontSize(int size) {
		font = Font.font("Verdana", size);
		fontSize = size;
	}

	public static void setNodeRadius(int size) {
		nodeRadiusProperty.set(size);
	}

	public static int getNodeRadius() {
		return nodeRadiusProperty.get();
	}

	// Exposes fx!
	public static Font getFont() {
		return font;
	}

	public static int getFontSize() {
		return fontSize;
	}

}
