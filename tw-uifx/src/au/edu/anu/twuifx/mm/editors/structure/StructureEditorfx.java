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

package au.edu.anu.twuifx.mm.editors.structure;

import java.util.List;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.graphEditor.IGraphVisualiser;
import au.edu.anu.twapps.mm.graphEditor.StructureEditorAdapter;
import au.edu.anu.twapps.mm.graphEditor.VisualNodeEditable;
import au.edu.anu.twapps.mm.layout.LayoutType;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TWA;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.twcore.constants.ConfigurationReservedEdgeLabels;
import fr.cnrs.iees.twcore.constants.ConfigurationReservedNodeId;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import fr.ens.biologie.generic.utils.Tuple;

/**
 * Author Ian Davies
 *
 * Date 13 Jan. 2019
 */

public class StructureEditorfx extends StructureEditorAdapter {

	private ContextMenu cm;

	public StructureEditorfx(VisualNodeEditable n, MouseEvent event, IMMController controller, IGraphVisualiser gv) {
		super(n, gv, controller);
		cm = new ContextMenu();
		buildgui();
		cm.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
	}

	@Override
	public void buildgui() {
		Iterable<SimpleDataTreeNode> childSpecs = specifications.getChildSpecsOf(editableNode, baseSpec, subClassSpec,
				TWA.getRoot());
		List<SimpleDataTreeNode> filteredChildSpecs = filterChildSpecs(childSpecs);
		List<VisualNode> orphanedChildren = orphanedChildList(filteredChildSpecs);
		Iterable<SimpleDataTreeNode> edgeSpecs = specifications.getEdgeSpecsOf(baseSpec, subClassSpec);
		List<Tuple<String, VisualNode, SimpleDataTreeNode>> filteredEdgeSpecs = filterEdgeSpecs(edgeSpecs);

		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW_NODE);
			if (!filteredChildSpecs.isEmpty() && !editableNode.isPredefined()) {

				for (SimpleDataTreeNode child : filteredChildSpecs) {
					String childLabel = (String) child.properties().getPropertyValue(aaIsOfClass);
					String childId = null;
					String dispName = childLabel;
					boolean reserved = false;
					if (child.properties().hasProperty(aaHasId)) {
						childId = (String) child.properties().getPropertyValue(aaHasId);
						reserved = ConfigurationReservedNodeId.isPredefined(childId);
						dispName += ":" + childId;
					}
					final String chldId = childId;
					if (!reserved) {
						MenuItem mi = MenuLabels.addMenuItem(mu, dispName);
						mi.setOnAction((e) -> {
							onNewChild(childLabel, chldId, child);
						});
					}
				}
			} else
				mu.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW_EDGE);
			if (!filteredEdgeSpecs.isEmpty() && !editableNode.isPredefined()) {
				for (Tuple<String, VisualNode, SimpleDataTreeNode> p : filteredEdgeSpecs) {
					boolean reserved = ConfigurationReservedEdgeLabels.isPredefined(p.getFirst());
					if (!reserved) {
						MenuItem mi = MenuLabels.addMenuItem(mu,
								p.getFirst() + "->" + p.getSecond().getDisplayText(false));
						mi.setOnAction((e) -> {
							onNewEdge(p);
						});
					}
				}
			} else
				mu.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW_CHILD_LINK);
			if (!orphanedChildren.isEmpty() && !editableNode.isPredefined()) {
				for (VisualNode vn : orphanedChildren) {
					MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(false));
					mi.setOnAction((e) -> {
						onReconnectChild(vn);
					});
				}
			} else
				mu.setDisable(true);
		}

		cm.getItems().add(new SeparatorMenuItem());
		// --
		{
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_DELETE_NODE);
			if (!editableNode.isRoot() && !editableNode.isPredefined()) {
				mi.setOnAction((e) -> {
					onDeleteNode();
				});
			} else
				mi.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_EDGE);
			if (editableNode.hasOutEdges() && !editableNode.isPredefined()) {
				for (VisualEdge edge : editableNode.getOutEdges()) {
					VisualNode vn = (VisualNode) edge.endNode();

					MenuItem mi = MenuLabels.addMenuItem(mu,
							edge.getDisplayText(false) + "->" + vn.getDisplayText(false));
					mi.setOnAction((e) -> {
						onDeleteEdge(edge);
					});
				}

			} else
				mu.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_CHILD);
			if (editableNode.hasChildren() && !editableNode.isPredefined()) {
				for (VisualNode child : editableNode.getSelectedVisualNode().getChildren()) {
					MenuItem mi = MenuLabels.addMenuItem(mu, child.getDisplayText(false));
					mi.setOnAction((e) -> {
						onDeleteParentLink(child);
					});
				}
			} else
				mu.setDisable(true);
		}
		// --
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_TREE);
			if (editableNode.hasChildren() && !editableNode.isPredefined()) {
				Iterable<VisualNode> lst = editableNode.getSelectedVisualNode().getChildren();
				for (VisualNode vn : lst) {

					MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(false));
					mi.setOnAction((e) -> {
						onDeleteTree(vn);
					});
				}
			} else
				mu.setDisable(true);
		}

		cm.getItems().add(new SeparatorMenuItem());
		// ---
		{
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_RENAME_NODE);

			if (!editableNode.isRoot() && !editableNode.isPredefined()) {
				mi.setOnAction((e) -> {
					onRenameNode();
				});
			} else
				mi.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_RENAME_EDGE);
			if (editableNode.hasOutEdges() && !editableNode.isPredefined()) {
				for (VisualEdge edge : editableNode.getOutEdges()) {
					VisualNode vn = (VisualNode) edge.endNode();
					if (!vn.isPredefined() && !editableNode.isPredefined()) {
						MenuItem mi = MenuLabels.addMenuItem(mu,
								edge.getDisplayText(false) + "->" + vn.getDisplayText(false));
						mi.setOnAction((e) -> {
							onRenameEdge(edge);
						});
					}
				}
			} else
				mu.setDisable(true);
		}

		cm.getItems().add(new SeparatorMenuItem());

		// ----------
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_IMPORT_TREE);
			if (!filteredChildSpecs.isEmpty() && !editableNode.isPredefined()) {
				for (SimpleDataTreeNode childSpec : filteredChildSpecs) {
					MenuItem mi = MenuLabels.addMenuItem(mu,
							(String) childSpec.properties().getPropertyValue(aaIsOfClass));
					mi.setOnAction((e) -> {
						onImportTree(childSpec);
					});

				}
			} else
				mu.setDisable(true);
		}
//--
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_EXPORT_TREE);
			if (editableNode.hasChildren() && !editableNode.isPredefined()) {
				for (VisualNode vn : editableNode.getSelectedVisualNode().getChildren()) {
					if (!vn.isPredefined()) {
						MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(false));
						mi.setOnAction((e) -> {
							onExportTree(vn);
						});
					}
				}
			} else
				mu.setDisable(true);
		}
		cm.getItems().add(new SeparatorMenuItem());
		// --
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_EXPAND);
			if (editableNode.getSelectedVisualNode().hasCollaspedChild()) {
				int count = 0;
				for (VisualNode vn : editableNode.getSelectedVisualNode().getChildren()) {
					if (vn.isCollapsed()) {
						count++;
						MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(false));
						mi.setOnAction(e -> onExpandTree(vn));
					}
				}
				if (count > 1) {
					mu.getItems().add(new SeparatorMenuItem());
					MenuItem mi = MenuLabels.addMenuItem(mu, MenuLabels.ML_ALL.label());
					mi.setOnAction(e -> onExpandTrees());
				}
			} else
				mu.setDisable(true);
		}
		// --
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_COLLAPSE);
			if (editableNode.getSelectedVisualNode().hasUncollapsedChildren()) {
				int count = 0;
				for (VisualNode vn : editableNode.getSelectedVisualNode().getChildren()) {
					if (!vn.isCollapsed()) {
						count++;
						MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(false));
						mi.setOnAction(e -> onCollapseTree(vn));
					}
				}
				if (count > 1) {
					mu.getItems().add(new SeparatorMenuItem());
					MenuItem mi = MenuLabels.addMenuItem(mu, MenuLabels.ML_ALL.label());
					mi.setOnAction(e -> onCollapseTrees());
				}
			} else
				mu.setDisable(true);
		}
		// --

		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_LAYOUT);
			for (LayoutType lt : LayoutType.values()) {
				MenuItem mi = new MenuItem(lt.name());
				mu.getItems().add(mi);
				mi.setOnAction((e) -> {
					String s = ((MenuItem) e.getSource()).getText();
					LayoutType t = LayoutType.valueOf(s);
					controller.doFocusedLayout(editableNode.getSelectedVisualNode(), t);
				});
			}
		}
		// --
		{
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_LOCALGRAPH);
			mi.setOnAction((e) -> {
				String title = MenuLabels.ML_LOCALGRAPH.label();
				String header = "Show graph surrounding '" + editableNode.getSelectedVisualNode().getDisplayText(false)
						+ "'.";
				String content = "Path length: ";
				String defaultValue = "1";
				String result = Dialogs.getText(title, header, content, defaultValue, Dialogs.vsNumeric);
				if (result != null) {
					int depth = Integer.parseInt(result);
					gvisualiser.showLocalGraph(editableNode.getSelectedVisualNode(), depth);
				}

			});
		}

	}

	private enum MenuLabels {
		ML_NEW_NODE /*         */("New node"), // spec
		ML_NEW_EDGE/*          */("New edge"), // spec
		ML_NEW_CHILD_LINK/*    */("New child edge"), // spec
		// --------------------------------------------
		ML_IMPORT_TREE/*       */("Import tree"), // spec
		ML_EXPORT_TREE/*       */("Export tree"), // spec
		ML_EXPAND/*            */("Expand"), // config
		ML_COLLAPSE/*          */("Collapse"), // config
		// --------------------------------------------
		ML_RENAME_NODE /*      */("Rename node"), // config
		ML_RENAME_EDGE/*       */("Rename edge"), // config
		ML_DELETE_EDGE/*       */("Delete edge"), // config
		ML_DELETE_CHILD/*      */("Delete child edge"), // config
		ML_DELETE_TREE/*-      */("Delete tree"), // config
		ML_DELETE_NODE/*       */("Delete node"), // config
		// --------------------------------------------
		ML_ALL/*               */("All"), //
		ML_LAYOUT/*            */("Apply layout"), //
		ML_LOCALGRAPH/*        */("Show neighbourhood"),;

		private final String label;

		private MenuLabels(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}

		public static Menu addMenu(ContextMenu cm, MenuLabels ml) {
			Menu result = new Menu(ml.label());
			result.setMnemonicParsing(false);
			cm.getItems().add(result);
			return result;
		}

		public static MenuItem addMenuItem(Menu mu, String label) {
			MenuItem result = new MenuItem(label);
			result.setMnemonicParsing(false);
			mu.getItems().add(result);
			return result;
		}

		public static MenuItem addMenuItem(ContextMenu cm, MenuLabels ml) {
			MenuItem result = new MenuItem(ml.label());
			result.setMnemonicParsing(false);
			cm.getItems().add(result);
			return result;
		}
	}

}
