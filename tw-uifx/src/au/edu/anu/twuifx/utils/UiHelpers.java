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

import au.edu.anu.omhtk.preferences.IPreferences;
import au.edu.anu.omhtk.preferences.Preferences;
import impl.org.controlsfx.skin.PropertySheetSkin;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.BorderPane;

/**
 * Static helper methods for various javafx controls.
 * 
 * @author Ian Davies - 11 Dec 2019
 */
public class UiHelpers {
	public static double[] getSplitPanePositions(double def, String key) {
		IPreferences prefs = Preferences.getImplementation();
		double pos;
		double[] positions = new double[1];
		pos = prefs.getDouble(key, def);
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
		if (accordion == null)
			return;
		int size = accordion.getPanes().size();
		if (size < 1)
			return;
		if (idx < 0)
			return;
		if (idx < size)
			accordion.setExpandedPane(accordion.getPanes().get(idx));
	}

	private static Accordion getAccordion(PropertySheet sheet) {
		PropertySheetSkin skin = (PropertySheetSkin) sheet.getSkin();
		BorderPane content = (BorderPane) skin.getChildren().get(0);
		ScrollPane sp = (ScrollPane) content.getCenter();
		return (Accordion) sp.getContent();
	}

	/**
	 * Allow to zoom/scale any node with pivot at scene (x,y) coordinates.
	 * 
	 * usage;
	 * 
	 * myView.setOnScroll(event -> UiHelpers.zoom(myView, event)); // mouse scroll
	 * wheel zoom
	 * 
	 * myView.setOnZoom(event -> UiHelpers.zoom(myView, event)); // pinch to zoom
	 * 
	 * https://stackoverflow.com/questions/27356577/scale-at-pivot-point-in-an-already-scaled-node
	 * 
	 * @param node
	 * @param delta
	 * @param x
	 * @param y
	 */
	private static void zoom(Node node, double factor, double x, double y) {
		double oldScale = node.getScaleX();
		double scale = oldScale * factor;
		if (scale < 0.05)
			scale = 0.05;
		if (scale > 50)
			scale = 50;
		node.setScaleX(scale);
		node.setScaleY(scale);

		double f = (scale / oldScale) - 1;
		Bounds bounds = node.localToScene(node.getBoundsInLocal());
		double dx = (x - (bounds.getWidth() / 2 + bounds.getMinX()));
		double dy = (y - (bounds.getHeight() / 2 + bounds.getMinY()));

		node.setTranslateX(node.getTranslateX() - f * dx);
		node.setTranslateY(node.getTranslateY() - f * dy);
	}

	public static void zoom(Node node, ScrollEvent event) {
		if (event.isControlDown()) {
			zoom(node, Math.pow(1.01, event.getDeltaY()), event.getSceneX(), event.getSceneY());
			event.consume();
		}
	}

	public static void zoom(Node node, ZoomEvent event) {
		zoom(node, event.getZoomFactor(), event.getSceneX(), event.getSceneY());
	}
}
