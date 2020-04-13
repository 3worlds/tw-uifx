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
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.graphEditor.IGraphVisualiser;
import au.edu.anu.twapps.mm.graphEditor.StructureEditorAdapter;
import au.edu.anu.twapps.mm.graphEditor.VisualNodeEditable;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TWA;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
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
		// if (haveSpecification()) {
		Iterable<SimpleDataTreeNode> childSpecs = specifications.getChildSpecsOf(editableNode,baseSpec, subClassSpec, TWA.getRoot());
		List<SimpleDataTreeNode> filteredChildSpecs = filterChildSpecs(childSpecs);
		List<VisualNode> orphanedChildren = orphanedChildList(filteredChildSpecs);
		Iterable<SimpleDataTreeNode> edgeSpecs = specifications.getEdgeSpecsOf(baseSpec, subClassSpec);
		List<Tuple<String, VisualNode, SimpleDataTreeNode>> filteredEdgeSpecs = filterEdgeSpecs(edgeSpecs);
		boolean section1Entries = !filteredChildSpecs.isEmpty() || !filteredEdgeSpecs.isEmpty()
				|| !orphanedChildren.isEmpty();
		boolean section2Entries = editableNode.hasChildren() || !filteredChildSpecs.isEmpty();
		boolean section3Entries = editableNode.hasChildren() || editableNode.canDelete() || editableNode.canRename();

		if (!filteredChildSpecs.isEmpty()) {
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW_NODE);
			for (SimpleDataTreeNode child : filteredChildSpecs) {
				String childLabel = (String) child.properties().getPropertyValue(aaIsOfClass);
				MenuItem mi = MenuLabels.addMenuItem(mu, childLabel);
				mi.setOnAction((e) -> {
					onNewChild(childLabel, child);
				});
			}
		}
		if (!filteredEdgeSpecs.isEmpty()) {
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW_EDGE);
			for (Tuple<String, VisualNode, SimpleDataTreeNode> p : filteredEdgeSpecs) {
				MenuItem mi = MenuLabels.addMenuItem(mu, p.getFirst() + "->" + p.getSecond().getDisplayText(false));
				mi.setOnAction((e) -> {
					onNewEdge(p);
				});

			}
		}
		if (!orphanedChildren.isEmpty()) {
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW_CHILD_LINK);
			for (VisualNode vn : orphanedChildren) {
				MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(false));
				mi.setOnAction((e) -> {
					onReconnectChild(vn);
				});
			}

		}

		if (section1Entries && (section2Entries || section3Entries))
			cm.getItems().add(new SeparatorMenuItem());

		if (!filteredChildSpecs.isEmpty()) {
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_IMPORT_TREE);
			for (SimpleDataTreeNode childSpec : filteredChildSpecs) {
				MenuItem mi = MenuLabels.addMenuItem(mu, (String) childSpec.properties().getPropertyValue(aaIsOfClass));
				mi.setOnAction((e) -> {
					onImportTree(childSpec);
				});

			}
		}

		if (editableNode.hasChildren()) {
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_EXPORT_TREE);
			for (VisualNode vn : editableNode.getSelectedVisualNode().getChildren()) {
				MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(false));
				mi.setOnAction((e) -> {
					onExportTree(vn);
				});
			}
		}
		if (editableNode.hasChildren()) {
			if (editableNode.getSelectedVisualNode().hasCollaspedChild()) {
				int count = 0;
				Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_EXPAND);
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
			}
			if (editableNode.getSelectedVisualNode().hasUncollapsedChildren()) {
				Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_COLLAPSE);
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

			}

		}

		if (section2Entries && section3Entries)
			cm.getItems().add(new SeparatorMenuItem());

		if (editableNode.canDelete()) {
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_DELETE_NODE);
			mi.setOnAction((e) -> {
				onDeleteNode();
			});
		}

		if (editableNode.canRename()) {
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_RENAME_NODE);
			mi.setOnAction((e) -> {
				onRenameNode();
			});
		}

		if (editableNode.hasOutEdges()) {
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_RENAME_EDGE);
			for (VisualEdge edge : editableNode.getOutEdges()) {
				VisualNode vn = (VisualNode) edge.endNode();
				MenuItem mi = MenuLabels.addMenuItem(mu, edge.getDisplayText(false) + "->" + vn.getDisplayText(false));
				mi.setOnAction((e) -> {
					onRenameEdge(edge);
				});

			}
		}

		if (editableNode.hasOutEdges()) {
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_EDGE);
			for (VisualEdge edge : editableNode.getOutEdges()) {
				VisualNode vn = (VisualNode) edge.endNode();
				MenuItem mi = MenuLabels.addMenuItem(mu, edge.getDisplayText(false) + "->" + vn.getDisplayText(false));
				mi.setOnAction((e) -> {
					onDeleteEdge(edge);
				});
			}
		}

		if (editableNode.hasChildren()) {
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_CHILD);
			for (VisualNode child : editableNode.getSelectedVisualNode().getChildren()) {
				MenuItem mi = MenuLabels.addMenuItem(mu, child.getDisplayText(false));
				mi.setOnAction((e) -> {
					onDeleteParentLink(child);
				});
			}

		}

		if (editableNode.hasChildren()) {
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_TREE);
			Iterable<VisualNode> lst = editableNode.getSelectedVisualNode().getChildren();
			for (VisualNode vn : lst) {
				MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(false));
				mi.setOnAction((e) -> {
					onDeleteTree(vn);
				});
			}
		}
		MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_LAYOUT);
		mi.setOnAction((e) -> {
			gvisualiser.doFocusedLayout(editableNode.getSelectedVisualNode());
		});
	}

	private enum MenuLabels {
		ML_NEW_NODE /*         */("New node"), // spec
		ML_NEW_EDGE/*          */("New edge"), // spec
		ML_NEW_CHILD_LINK/*    */("New child link"), // spec
		// --------------------------------------------
		ML_IMPORT_TREE/*       */("Import tree"), // spec
		ML_EXPORT_TREE/*       */("Export tree"), // spec
		ML_EXPAND/*            */("Expand"), // config
		ML_COLLAPSE/*          */("Collapse"), // config
		// --------------------------------------------
		ML_RENAME_NODE /*      */("Rename node"), // config
		ML_RENAME_EDGE/*       */("Rename edge"), // config
		ML_DELETE_EDGE/*       */("Delete edge"), // config
		ML_DELETE_CHILD/*      */("Delete child link"), // config
		ML_DELETE_TREE/*-      */("Delete tree"), // config
		ML_DELETE_NODE/*       */("Delete node"), // config
		// --------------------------------------------
		ML_ALL/*               */("All"),//
		ML_LAYOUT/*            */("Focus radial layout"),
		;

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
