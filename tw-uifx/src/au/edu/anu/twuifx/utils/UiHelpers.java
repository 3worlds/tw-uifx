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
 *  along with UIT.  If not, see <https://www.gnu.org/licenses/gpl.html>. *
 *                                                                        *
 **************************************************************************/
package au.edu.anu.twuifx.utils;

import org.controlsfx.control.PropertySheet;

import au.edu.anu.omhtk.preferences.Preferences;
import impl.org.controlsfx.skin.PropertySheetSkin;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

/**
 * @author Ian Davies
 *
 * @date 11 Dec 2019
 */
public class UiHelpers {
	public static double[] getSplitPanePositions(double def, String key) {
		double pos;
		double[] positions = new double[1];
		pos = Preferences.getDouble(key, def);
		positions[0] = pos;
		return positions;
	}

	public static int getExpandedPaneIndex(PropertySheet sheet) {
		if (!sheet.getMode().equals(PropertySheet.Mode.CATEGORY))
			return 0;
		Accordion accordion = getAccordion(sheet);
		return accordion.getPanes().indexOf(accordion.getExpandedPane());
	}

	public static void setExpandedPane(PropertySheet sheet, int idx) {
		if (!sheet.getMode().equals(PropertySheet.Mode.CATEGORY))
			return;
		Accordion accordion = getAccordion(sheet);
		accordion.setExpandedPane(accordion.getPanes().get(idx));
	}

	private static Accordion getAccordion(PropertySheet sheet) {
		PropertySheetSkin skin = (PropertySheetSkin) sheet.getSkin();
		BorderPane content = (BorderPane) skin.getChildren().get(0);
		ScrollPane sp = (ScrollPane) content.getCenter();
		return (Accordion) sp.getContent();
	}

}
