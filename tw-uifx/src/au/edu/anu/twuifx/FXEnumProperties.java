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
package au.edu.anu.twuifx;

import au.edu.anu.twuifx.widgets.*;
import au.edu.anu.ymuit.ui.colour.PaletteTypes;
import fr.cnrs.iees.omugi.io.parsing.ValidPropertyTypes;
import fr.cnrs.iees.twcore.constants.EnumProperties;

/**
 * Trigger static methods of enums in fx dependent projects. The purpose of this
 * class is primarily so other fx projects don't require a dependence on the
 * project containing {@link EnumProperties#recordEnums()}.
 * 
 * @author Ian Davies - 21 July 2022
 */
public class FXEnumProperties {
	private FXEnumProperties() {
	};

	// TODO: There may be a better way (Services??)
	/**
	 * These references trigger the static block initialization of all these classes
	 * (unknown to twcore), which then record their details in
	 * {@link ValidPropertyTypes}. This static method must be called early in
	 * application setup.
	 * <p>
	 */
	public static void recordEnums() {
		PaletteTypes.defaultValue();
		IsMissingValue.defaultValue();
		MissingValueColour.defaultValue();
	}

}
