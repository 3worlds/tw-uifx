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
package au.edu.anu.twuifx.mr;


import au.edu.anu.twcore.ui.WidgetNode;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.twuifx.mr.view.MrController;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;


/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */
public class UIDeployer {
	private List<Widget> widgets;
	private MrController controller;

	public UIDeployer(TreeGraphNode uiNode, MrController controller) {
		widgets = new ArrayList<>();
		this.controller= controller;
		HBox toolBar = controller.getToolBar();
		List<WidgetNode> toolBarList = new ArrayList<>();
		TreeGraphNode top = (TreeGraphNode) get(uiNode.getChildren(),selectZeroOrOne(hasTheLabel(N_UITOP.label())));
		if (top!=null) {
			Iterable<? extends TreeNode> widgetNodes = top.getChildren();
			for (TreeNode n:widgetNodes) 
				toolBarList.add((WidgetNode)n);
			sortWidgetOrder(toolBarList);
			for (WidgetNode wn:toolBarList) {
				Widget w = wn.getInstance();
				widgets.add(w);
				toolBar.getChildren().add((Node) w.getUserInterfaceContainer());
			}
		}
	}

	private void setMenus() {
		Menu wmenu = controller.getWidgetMenu();
		for (Widget w:widgets) {
			Object o = w.getMenuContainer();
			if (o!=null) 
				wmenu.getItems().add((MenuItem)o);
		}
	}
	public void getPreferences() {
		for (Widget w:widgets)
			w.getPreferences();
	}

	public void putPreferences() {
		for (Widget w:widgets)
			w.putPreferences();		
	}
//------------------------------
	private void sortWidgetOrder(List<WidgetNode> lst) {
		lst.sort(new Comparator<WidgetNode>() {

			@Override
			public int compare(WidgetNode o1, WidgetNode o2) {
				Integer w1 = (Integer)o1.properties().getPropertyValue("order");
				Integer w2 = (Integer)o2.properties().getPropertyValue("order");
				return w1.compareTo(w2);
			}});
		
	}
}
