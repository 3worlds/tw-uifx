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
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.twcore.constants.UIContainerOrientation;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

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
		this.controller = controller;
		TreeGraphNode top = (TreeGraphNode) get(uiNode.getChildren(), selectZeroOrOne(hasTheLabel(N_UITOP.label())));
		if (top != null)
			buildBar(controller.getToolBar(), top);

		TreeGraphNode bottom = (TreeGraphNode) get(uiNode.getChildren(),
				selectZeroOrOne(hasTheLabel(N_UIBOTTOM.label())));
		if (bottom != null)
			buildBar(controller.getStatusBar(), bottom);

		List<TreeGraphNode> tabs = (List<TreeGraphNode>) get(uiNode.getChildren(),
				selectZeroOrMany(hasTheLabel(N_UITAB.label())));
		for (TreeGraphNode tab : tabs)
			buildTab(tab);

	}

	private void buildTab(TreeGraphNode tabNode) {
		TabPane tabPane = controller.getTabPane();
		Tab tab = new Tab(tabNode.id());
		tabPane.getTabs().add(tab);
		for (TreeNode c : tabNode.getChildren()) {
			TreeGraphDataNode container = (TreeGraphDataNode) c;
			UIContainerOrientation orientation = (UIContainerOrientation) container.properties()
					.getPropertyValue(P_UICONTAINER_ORIENT.key());

		}
	}

	private void buildBar(HBox container, TreeGraphNode parent) {
		List<WidgetNode> barList = new ArrayList<>();
		Iterable<? extends TreeNode> widgetNodes = parent.getChildren();
		for (TreeNode n : widgetNodes)
			barList.add((WidgetNode) n);
		sortWidgetOrder(barList);
		for (WidgetNode wn : barList) {
			Widget w = wn.getInstance();
			widgets.add(w);
			container.getChildren().add((Node) w.getUserInterfaceContainer());
		}
	}

	private void setMenus() {
		Menu wmenu = controller.getWidgetMenu();
		for (Widget w : widgets) {
			Object o = w.getMenuContainer();
			if (o != null)
				wmenu.getItems().add((MenuItem) o);
		}
	}

	public void getPreferences() {
		for (Widget w : widgets)
			w.getPreferences();
	}

	public void putPreferences() {
		for (Widget w : widgets)
			w.putPreferences();
	}

//------------------------------
	private void sortWidgetOrder(List<WidgetNode> lst) {
		lst.sort(new Comparator<WidgetNode>() {

			@Override
			public int compare(WidgetNode o1, WidgetNode o2) {
				Integer w1 = (Integer) o1.properties().getPropertyValue("order");
				Integer w2 = (Integer) o2.properties().getPropertyValue("order");
				return w1.compareTo(w2);
			}
		});

	}
}
