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

import java.util.function.Supplier;

import fr.cnrs.iees.io.parsing.ValidPropertyTypes;
import javafx.scene.paint.Color;


/**
 * An enum wrapper for {@link Color}.
 * 
 * @author Ian Davies - 20 July 2022
 */
public enum MissingValueColour implements Supplier<Color> {	
	/**
	 * returns {@link Color#WHITE}
	 */
	WHITE(Color.WHITE),
	/**
	 * returns {@link Color#LIGHTGRAY}
	 */
	LIGHTGRAY(Color.LIGHTGRAY),
	/**
	 * returns {@link Color#GRAY}
	 */
	GRAY(Color.GRAY),
	/**
	 * returns {@link Color#BLACK}
	 */
	BLACK(Color.BLACK),
	/**
	 * returns {@link Color#TRANSPARENT}
	 */
	TRANSPARENT(Color.TRANSPARENT);

	private Color c;

	private MissingValueColour(Color c) {
		this.c = c;
	}

	@Override
	public Color get() {
		return c;
	}

	public static MissingValueColour defaultValue() {
		return TRANSPARENT;
	}
	static {
		ValidPropertyTypes.recordPropertyType(MissingValueColour.class.getSimpleName(),
				MissingValueColour.class.getName(), MissingValueColour.defaultValue());
	}
	
}
