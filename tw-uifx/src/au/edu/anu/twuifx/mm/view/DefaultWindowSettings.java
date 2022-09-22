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
package au.edu.anu.twuifx.mm.view;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class DefaultWindowSettings {
	private static Rectangle2D screenBounds = Screen.getPrimary().getBounds();
	private static double w = screenBounds.getWidth() * 0.8;
	private static double h = screenBounds.getHeight() * 0.8;
	private static double x = screenBounds.getWidth() * 0.1;
	private static double y = screenBounds.getHeight() * 0.1;
	private static double splitter1 = 0.2;
	private static double splitter2 = 0.5;
	private static String defaultMMName = "3Worlds ModelMaker";

	public static double getWidth() {
		return w;
	}

	public static double getHeight() {
		return h;
	}

	public static double getX() {
		return x;
	}

	public static double getY() {
		return y;
	}

	public static double splitter1() {
		return splitter1;
	}

	public static double splitter2() {
		return splitter2;
	}

	public static String defaultName() {
		return defaultMMName;
	}

}
