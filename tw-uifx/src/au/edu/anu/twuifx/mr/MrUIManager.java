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


import java.util.List;

import au.edu.anu.rscs.aot.collections.DynamicList;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Window;

/**
 * @author Ian Davies
 * @date 12 Dec. 2017
 */
/*-
 * This class calls the Widget user code to instantiate the UI components (see: ElementUserInterfacefx)
 * This is for Javafx widgets ONLY
 * NOTE: If no controller widget is supplied, the model can't run
 * 
 * TODO: enact all possible property settings for each of the 10 container classes (if hasProperty("blah"); etc
 */
@Deprecated
public class MrUIManager {
	private Menu userMenu;
	//private Preferences pref;
	private Window parent;
//	private List<ElementUserInterfacefx> widgets;

//	public MrUIManager(TreeGraphDataNode userInterface, HBox toolBar, TabPane topLeft, TabPane topRight, TabPane bottomLeft,
//			TabPane bottomRight, HBox statusBar, Menu widgetMenu, Window window) {
//		this.parent = window;
//		this.pref = pref;
//		this.userMenu = widgetMenu;
//		widgets = new ArrayList<>();
//
//		TreeGraphDataNode toolbarTopNode = (TreeGraphDataNode) get(userInterface.getChildren(),
//				selectZeroOrOne(hasTheLabel(N_TOOLBARTOP.toString())));
//		TreeGraphDataNode toolbarBottomNode = (TreeGraphDataNode) get(userInterface.getChildren(),
//				selectZeroOrOne(hasTheLabel(N_TOOLBARBOTTOM.toString())));
//		if (toolbarTopNode != null)
//			buildBar(toolbarTopNode, toolBar);
//		if (toolbarBottomNode != null)
//			buildBar(toolbarBottomNode, statusBar);
//		buildQuadrant(N_TOPLEFTPANEL.toString(), userInterface, topLeft);
//		buildQuadrant(N_TOPRIGHTPANEL.toString(), userInterface, topRight);
//		buildQuadrant(N_BOTTOMLEFTPANEL.toString(), userInterface, bottomLeft);
//		buildQuadrant(N_BOTTOMRIGHTPANEL.toString(), userInterface, bottomRight);
//
//		// List<TreeGraphDataNode> tabNodes = (List<TreeGraphDataNode>) get(userInterface, children(),
//		// selectZeroOrMany(hasTheLabel("tab")));
//		// for (TreeGraphDataNode tabNode : tabNodes) {
//		// UIQuadrants quad = (UIQuadrants) tabNode.getPropertyValue("quadrant");
//		// switch (quad) {
//		// case TopLeftPanel: {
//		// topLeft.getTabs().add(createTab(tabNode));
//		// break;
//		// }
//		// case TopRightPanel: {
//		// topRight.getTabs().add(createTab(tabNode));
//		// break;
//		// }
//		// case BottomLeftPanel: {
//		// bottomLeft.getTabs().add(createTab(tabNode));
//		// break;
//		// }
//		// default:
//		// bottomRight.getTabs().add(createTab(tabNode));
//		// }
//		// }
//		for (ElementUserInterfacefx widget : widgets)
//			setWidgetMenu(widget);
//	}

//	@SuppressWarnings("unchecked")
	private void buildQuadrant(String label, TreeGraphDataNode uiNode, TabPane tabPane) {
//		List<TreeGraphDataNode> qNodes = (List<TreeGraphDataNode>) get(uiNode.getChildren(), selectZeroOrMany(hasTheLabel(label)));
//		for (TreeGraphDataNode n : qNodes) {
//			tabPane.getTabs().add(createTab(n));
//		}
	}

//	public void loadPreferences() {
//		for (ElementUserInterfacefx widget : widgets)
//			widget.loadPreferences(pref);
//	}

//	private Tab createTab(TreeGraphDataNode tabNode) {
//		Tab tab = new Tab(tabNode.getName());
//		tab.closableProperty().set(false);
//		TabLayoutTypes layout = (TabLayoutTypes) tabNode.getPropertyValue("layout");
//		Pane pane;
//		switch (layout) {
//		case FlowPane:
//			pane = createFlowPaneWidgets(tabNode);
//			break;
//		case BorderPane:
//			pane = createBorderPaneWidgets(tabNode);
//			break;
//		case HBox:
//			pane = createHBoxWidgets(tabNode);
//			break;
//		case VBox:
//			pane = createVBoxWidgets(tabNode);
//			break;
//		case AnchorPane:
//			pane = createAnchorPaneWidgets(tabNode);
//			break;
//		case StackPane:
//			pane = createStackPaneWidgets(tabNode);
//			break;
//		case TabPane:
//			pane = createTabPaneWidgets(tabNode);
//			break;
//		case TilePane:
//			pane = createTilePaneWidgets(tabNode);
//			break;
//		case GridPane:
//			pane = createGridPaneWidgets(tabNode);
//			break;
//		case Accordion:
//			pane = createAccordianWidgets(tabNode);
//			break;
//		default:
//			throw new TwuifxException("'" + layout + "' is not a supported layout");
//		}
//		tab.setContent(pane);
//
//		return tab;
//	}

//	public void savePreferences() {
//		for (ElementUserInterfacefx widget : widgets)
//			widget.savePreferences(pref);
//
//	}

//	private void setWidgetMenu(ElementUserInterfacefx widget) {
//		if (userMenu != null) {
//			Menu widgetMenu = widget.getMenuItems(parent);
//			if (widgetMenu != null)
//				userMenu.getItems().add(widgetMenu);
//		}
//	}

	private List<TreeGraphDataNode> sort(DynamicList<TreeGraphDataNode> widgetNodes) {
		widgetNodes.sort((first, second) -> {
			Integer i1 = (Integer) first.properties().getPropertyValue("order");
			Integer i2 = (Integer) second.properties().getPropertyValue("order");
			return i1.compareTo(i2);
		});
		return widgetNodes;
	}

//	private void addToolTip(Node nodefx, String tip) {
//		if (nodefx.getProperties().get("toolTip") == null)
//			Tooltip.install(nodefx, new Tooltip(tip));
//	}

//	private void buildBar(TreeGraphDataNode parentNode, HBox bar) {
//		for (TreeGraphDataNode widgetNode : sort(parentNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			bar.getChildren().add(nodefx);
//			widgets.add(widget);
//		}
//
//	}

//	private Pane createAccordianWidgets(TreeGraphDataNode tabNode) {
//		Accordion accordion = new Accordion();
//		BorderPane pane = new BorderPane();
//		pane.setCenter(accordion);
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			TitledPane tp = new TitledPane();
//			tp.setText(widgetNode.getName());
//			tp.setContent(nodefx);
//			accordion.getPanes().add(tp);
//			widgets.add(widget);
//		}
//		return pane;
//	}

//	private Pane createGridPaneWidgets(TreeGraphDataNode tabNode) {
//		GridPane pane = new GridPane();
//		pane.setHgap(5);
//		pane.setVgap(5);
//		int nWidgets = tabNode.getChildren().size();
//		int nRows = (int) Math.sqrt(nWidgets);
//		int nCols = 1;
//		while ((nCols * nRows) < nWidgets)
//			nCols++;
//		int col = 0;
//		int row = 0;
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			pane.add(nodefx, col, row);
//			col++;
//			if (col >= nCols) {
//				row++;
//				col = 0;
//			}
//			widgets.add(widget);
//		}
//		return pane;
//	}

//	private Pane createTilePaneWidgets(TreeGraphDataNode tabNode) {
//		// This does not work for some reason
//		// All widgets are added but only the first appears
//		// Do a test to see if it works as content of a tab.
//		TilePane tilePane = new TilePane();
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			tilePane.getChildren().add(nodefx);
//			widgets.add(widget);
//		}
//		// pane.setCenter(tilePane);
//		return tilePane;
//	}

//	private Pane createTabPaneWidgets(TreeGraphDataNode tabNode) {
//		BorderPane pane = new BorderPane();
//		TabPane tabPane = new TabPane();
//		pane.setCenter(tabPane);
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			Tab tab = new Tab(widgetNode.getName());
//			tabPane.getTabs().add(tab);
//			tab.setContent(nodefx);
//			tab.setClosable(false);
//			widgets.add(widget);
//		}
//		return pane;
//	}

//	private Pane createStackPaneWidgets(TreeGraphDataNode tabNode) {
//		StackPane pane = new StackPane();
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			pane.getChildren().add(nodefx);
//			widgets.add(widget);
//		}
//
//		return null;
//	}
//
//	private Pane createAnchorPaneWidgets(TreeGraphDataNode tabNode) {
//		AnchorPane pane = new AnchorPane();
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			pane.getChildren().add(nodefx);
//			widgets.add(widget);
//
//		}
//
//		return pane;
//	}
//
//	private Pane createVBoxWidgets(TreeGraphDataNode tabNode) {
//		VBox pane = new VBox();
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			pane.getChildren().add(nodefx);
//			widgets.add(widget);
//		}
//		return pane;
//	}
//
//	private Pane createHBoxWidgets(TreeGraphDataNode tabNode) {
//		HBox pane = new HBox();
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			pane.getChildren().add(nodefx);
//			widgets.add(widget);
//		}
//		return pane;
//	}
//
//	private Pane createBorderPaneWidgets(TreeGraphDataNode tabNode) {
//		BorderPane pane = new BorderPane();
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			if (pane.getCenter() == null)
//				pane.setCenter(nodefx);
//			else if (pane.getTop() == null)
//				pane.setTop(nodefx);
//			else if (pane.getBottom() == null)
//				pane.setBottom(nodefx);
//			else if (pane.getLeft() == null)
//				pane.setLeft(nodefx);
//			else if (pane.getRight() == null)
//				pane.setRight(nodefx);
//			else
//				// issue a warning that the BorderPane is full or prevent this circumstance
//				// arising
//				;
//
//			widgets.add(widget);
//
//		}
//
//		return pane;
//	}
//
//	private Pane createFlowPaneWidgets(TreeGraphDataNode tabNode) {
//		FlowPane pane = new FlowPane();
//		pane.hgapProperty().set((Integer) tabNode.getPropertyValue(FlowLayoutParameters.hGap.toString(), 1));
//		pane.vgapProperty().set((Integer) tabNode.getPropertyValue(FlowLayoutParameters.vGap.toString(), 1));
//		// pane.setAlignment(Pos.valueOf((String)
//		// tabNode.getPropertyValue(FlowLayoutParameters.align.toString())));
//		for (TreeGraphDataNode widgetNode : sort(tabNode.getChildren())) {
//			ElementUserInterfacefx widget = (ElementUserInterfacefx) widgetNode;
//			Node nodefx = widget.getUserInterface();
//			addToolTip(nodefx, widgetNode.displayName());
//			pane.getChildren().add(nodefx);
//			widgets.add(widget);
//		}
//		return pane;
//	}

}
