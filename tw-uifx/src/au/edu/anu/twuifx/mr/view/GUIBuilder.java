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
package au.edu.anu.twuifx.mr.view;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twcore.ui.UIContainer;
import au.edu.anu.twcore.ui.UITab;
import au.edu.anu.twcore.ui.WidgetNode;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.utils.UiHelpers;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.twcore.constants.UIContainerOrientation;
import fr.ens.biologie.generic.utils.Duple;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
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
public class GUIBuilder {
	private List<WidgetGUI> guiWidgets;
	private List<Widget> hlWidgets;
	private List<SplitPane> splitPanes;
	private MrController controller;

	@SuppressWarnings("unchecked")
	public GUIBuilder(TreeGraphNode uiNode, MrController controller) {
		guiWidgets = new ArrayList<>();
		hlWidgets = new ArrayList<>();
		splitPanes = new ArrayList<>();
		this.controller = controller;
		// top bar
		TreeGraphNode topNode = (TreeGraphNode) get(uiNode.getChildren(),
				selectZeroOrOne(hasTheLabel(N_UITOP.label())));
		if (topNode != null)
			buildBar(controller.getToolBar(), topNode);

		// bottom bar
		TreeGraphNode bottomNode = (TreeGraphNode) get(uiNode.getChildren(),
				selectZeroOrOne(hasTheLabel(N_UIBOTTOM.label())));
		if (bottomNode != null)
			buildBar(controller.getStatusBar(), bottomNode);

		// center content
		List<TreeGraphNode> tabNodes = (List<TreeGraphNode>) get(uiNode.getChildren(),
				selectZeroOrMany(hasTheLabel(N_UITAB.label())));
		for (TreeGraphNode n : tabNodes) {
			UITab tabNode = (UITab) n;
			UIContainerOrientation orientation = (UIContainerOrientation) tabNode.properties()
					.getPropertyValue(P_UICONTAINER_ORIENT.key());
			buildContent(n.getChildren(), orientation, getTabContainer(n.id()));
		}

		setMenus();

		TreeGraphNode headless = (TreeGraphNode) get(uiNode.getChildren(),
				selectZeroOrOne(hasTheLabel(N_UIHEADLESS.label())));
		if (headless != null)
			for (TreeNode n : headless.getChildren()) {
				WidgetNode widgetNode = (WidgetNode) n;
				hlWidgets.add(widgetNode.getInstance());
			}

	}

	private Duple<BorderPane, BorderPane> makeSplitPane(UIContainerOrientation orientation, BorderPane parentPane,
			String id) {
		SplitPane splitPane = new SplitPane();
		splitPane.setId(id);
		splitPanes.add(splitPane);
		if (orientation == UIContainerOrientation.horizontal)
			splitPane.setOrientation(Orientation.HORIZONTAL);
		else
			splitPane.setOrientation(Orientation.VERTICAL);
		BorderPane first = new BorderPane();
		BorderPane second = new BorderPane();
		splitPane.getItems().add(first);
		splitPane.getItems().add(second);
		parentPane.setCenter(splitPane);
		return new Duple<BorderPane, BorderPane>(first, second);
	}

	private void buildContent(Iterable<? extends TreeNode> children, UIContainerOrientation parentOrientation,
			BorderPane parentBorderPane) {

		List<WidgetNode> widgetNodes = new ArrayList<>();
		List<UIContainer> containerNodes = new ArrayList<>();
		for (TreeNode c : children) {
			if (c.classId().equals(N_UIWIDGET.label()))
				widgetNodes.add((WidgetNode) c);
			if (c.classId().equals(N_UICONTAINER.label()))
				containerNodes.add((UIContainer) c);
		}
		if (widgetNodes.size() == 1 && containerNodes.size() == 1) {
			WidgetNode wn = widgetNodes.get(0);
			UIContainer cn = containerNodes.get(0);
			Duple<BorderPane, BorderPane> contents = makeSplitPane(parentOrientation, parentBorderPane,
					wn.getParent().id());

			int wnp = (Integer) wn.properties().getPropertyValue(P_UIORDER.key());
			int cnp = (Integer) cn.properties().getPropertyValue(P_UIORDER.key());
			WidgetGUI w = (WidgetGUI) wn.getInstance();
			guiWidgets.add(w);
			if (wnp <= cnp) {
				contents.getFirst().setCenter((Node) w.getUserInterfaceContainer());
				buildContent(cn.getChildren(),
						(UIContainerOrientation) cn.properties().getPropertyValue(P_UICONTAINER_ORIENT.key()),
						contents.getSecond());
			} else {
				contents.getSecond().setCenter((Node) w.getUserInterfaceContainer());
				buildContent(cn.getChildren(),
						(UIContainerOrientation) cn.properties().getPropertyValue(P_UICONTAINER_ORIENT.key()),
						contents.getFirst());
			}

		} else if (widgetNodes.size() == 1) {
			WidgetGUI w = (WidgetGUI) widgetNodes.get(0).getInstance();
			guiWidgets.add(w);
			parentBorderPane.setCenter((Node) w.getUserInterfaceContainer());
		} else if (widgetNodes.size() == 2) {
			WidgetNode wn1 = widgetNodes.get(0);
			WidgetNode wn2 = widgetNodes.get(1);
			Duple<BorderPane, BorderPane> contents = makeSplitPane(parentOrientation, parentBorderPane,
					wn1.getParent().id());

			int w1Pos = (Integer) wn1.properties().getPropertyValue(P_UIORDER.key());
			int w2Pos = (Integer) wn2.properties().getPropertyValue(P_UIORDER.key());
			WidgetGUI w1 = (WidgetGUI) wn1.getInstance();
			WidgetGUI w2 = (WidgetGUI) wn2.getInstance();
			guiWidgets.add(w1);
			guiWidgets.add(w2);
			if (w1Pos <= w2Pos) {
				contents.getFirst().setCenter((Node) w1.getUserInterfaceContainer());
				contents.getSecond().setCenter((Node) w2.getUserInterfaceContainer());
			} else {
				contents.getFirst().setCenter((Node) w2.getUserInterfaceContainer());
				contents.getSecond().setCenter((Node) w1.getUserInterfaceContainer());
			}
		} else if (containerNodes.size() == 2) {
			// not strictly necessary but might give options for how the nested splitters
			// move their contents
			UIContainer cn1 = containerNodes.get(0);
			UIContainer cn2 = containerNodes.get(1);

			Duple<BorderPane, BorderPane> contents = makeSplitPane(parentOrientation, parentBorderPane,
					cn1.getParent().id());
			int c1Pos = (Integer) cn1.properties().getPropertyValue(P_UIORDER.key());
			int c2Pos = (Integer) cn2.properties().getPropertyValue(P_UIORDER.key());
			if (c1Pos <= c2Pos) {
				buildContent(cn1.getChildren(),
						(UIContainerOrientation) cn1.properties().getPropertyValue(P_UICONTAINER_ORIENT.key()),
						contents.getFirst());
				buildContent(cn2.getChildren(),
						(UIContainerOrientation) cn2.properties().getPropertyValue(P_UICONTAINER_ORIENT.key()),
						contents.getSecond());
			} else {
				buildContent(cn1.getChildren(),
						(UIContainerOrientation) cn1.properties().getPropertyValue(P_UICONTAINER_ORIENT.key()),
						contents.getSecond());
				buildContent(cn2.getChildren(),
						(UIContainerOrientation) cn2.properties().getPropertyValue(P_UICONTAINER_ORIENT.key()),
						contents.getFirst());
			}
		} else if (containerNodes.size() == 1) {
			// ignore this and use parent orientation and borderPane
			UIContainer cn = containerNodes.get(0);
			buildContent(cn.getChildren(), parentOrientation, parentBorderPane);
		}
	}

	private BorderPane getTabContainer(String name) {
		TabPane tabPane = controller.getTabPane();
		Tab tab = new Tab(name);
		BorderPane result = new BorderPane();
		tab.setContent(result);
		tabPane.getTabs().add(tab);
		return result;
	}

	private void buildBar(HBox container, TreeGraphNode parent) {
		List<WidgetNode> barList = new ArrayList<>();
		Iterable<? extends TreeNode> widgetNodes = parent.getChildren();
		for (TreeNode n : widgetNodes)
			barList.add((WidgetNode) n);
		sortWidgetOrder(barList);
		for (WidgetNode wn : barList) {
			WidgetGUI w = (WidgetGUI) wn.getInstance();
			guiWidgets.add(w);
			container.getChildren().add((Node) w.getUserInterfaceContainer());
		}
	}

	private void setMenus() {
		Menu wmenu = controller.getWidgetMenu();
		for (WidgetGUI w : guiWidgets) {
			Object o = w.getMenuContainer();
			if (o != null)
				wmenu.getItems().add((MenuItem) o);
		}
	}

	private static final String splitter = "splitter_";

	public void getPreferences() {
		for (WidgetGUI w : guiWidgets)
			w.getUserPreferences();
		// maybe needs to be delayed!
		for (SplitPane s : splitPanes) {
			String key = splitter + s.getId();
			double[] pos = UiHelpers.getSplitPanePositions(0.5, key);
			s.setDividerPositions(pos);
		}
//		for (Widget w : hlWidgets) {
//			w.getPreferences();
//		}
	}

	public void putPreferences() {
		for (WidgetGUI w : guiWidgets)
			w.putUserPreferences();
		for (SplitPane s : splitPanes) {
			String key = splitter + s.getId();
			Preferences.putDouble(key, s.getDividerPositions()[0]);
		}
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
