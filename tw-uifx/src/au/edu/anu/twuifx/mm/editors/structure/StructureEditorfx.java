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
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TWA;
import au.edu.anu.twuifx.mm.visualise.IGraphVisualiser;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import fr.ens.biologie.generic.utils.Duple;

/**
 * Author Ian Davies
 *
 * Date 13 Jan. 2019
 */

public class StructureEditorfx extends StructureEditorAdapter {

	private ContextMenu cm;

	public StructureEditorfx(SpecifiableNode n, MouseEvent event, IMMController controller, IGraphVisualiser gv) {
		super(n, gv, controller);
		cm = new ContextMenu();
		buildgui();
		cm.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
	}

	@Override
	public void buildgui() {
		if (haveSpecification()) {
			Iterable<SimpleDataTreeNode> childSpecs = specifications.getChildSpecificationsOf(baseSpec, subClassSpec,
					TWA.getRoot());
			List<SimpleDataTreeNode> filteredChildSpecs = filterChildSpecs(childSpecs);
			List<VisualNode> orphanedChildren = orphanedChildList(filteredChildSpecs);
			Iterable<SimpleDataTreeNode> edgeSpecs = specifications.getEdgeSpecificationsOf(baseSpec, subClassSpec);
			List<Duple<String, VisualNode>> filteredEdgeSpecs = filterEdgeSpecs(edgeSpecs);

			if (!filteredChildSpecs.isEmpty()) {
				Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW);
				for (SimpleDataTreeNode child : filteredChildSpecs) {
					String childLabel = (String) child.properties().getPropertyValue(aaIsOfClass);
					MenuItem mi = new MenuItem(childLabel);
					mu.getItems().add(mi);
					mi.setOnAction((e) -> {
						onNewChild(childLabel, child);
					});
				}
			}
			if (!orphanedChildren.isEmpty()) {
				Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_CONNECT_TO_CHILD);
				for (VisualNode vn:orphanedChildren) {
					MenuItem mi = new MenuItem(vn.id());
					mu.getItems().add(mi);
					mi.setOnAction((e)->{
						onAddChild(vn);
					});
				}
				

			}

			if (!filteredEdgeSpecs.isEmpty()) {
				Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_CONNECT_TO);
				for (Duple<String, VisualNode> p : filteredEdgeSpecs) {
					MenuItem mi = new MenuItem(p.getFirst() + "->" + p.getSecond().id());
					mu.getItems().add(mi);
					mi.setOnAction((e) -> {
						onNewEdge(p);
					});

				}
			}

			cm.getItems().add(new SeparatorMenuItem());

			boolean addSep = editingNode.canDelete();
			if (editingNode.hasChildren()) {
				// add exportTreeOptions
				addSep = true;
			}
			if (!filteredChildSpecs.isEmpty()) {
				// add import tree options
				addSep = true;
			}

			// --------------------------------------
			if (addSep)
				cm.getItems().add(new SeparatorMenuItem());

		}
		if (editingNode.hasOutEdges())

		{
			// delete xlinks
		}

		if (editingNode.canDelete()) {
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_DELETE);
			mi.setOnAction((e) -> {
				onDeleteNode();
			});
		}

		if (editingNode.hasChildren()) {
		}

		if (!editingNode.isLeaf()) {
			cm.getItems().add(new SeparatorMenuItem());
			if (editingNode.getSelectedVisualNode().isCollapsedParent()) {
				MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_EXPAND);
				mi.setOnAction((e) -> {
					onExpandTree();
				});

			} else if (editingNode.hasChildren()) {
				MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_COLLAPSE);
				mi.setOnAction((e) -> {
					onCollapseTree();
				});
			}
		}
	}

	private enum MenuLabels {
		ML_NEW /*-             */("New"), //
		ML_CONNECT_TO/*-       */("Connect to"), //
		ML_CONNECT_TO_CHILD/*- */("Connect to child"), //
		ML_DISCONNECT_FROM/*-  */("Disconnect from"), //
		ML_DELETE/*-           */("Delete"), //
		ML_IMPORT_TREE/*-      */("Import tree"), //
		ML_DELETE_TREE/*-      */("Delete tree"), //
		ML_EXPORT_TREE/*-      */("Export tree"), //
		ML_SELECT_PARAMETERS/* */("Select parameters"), //
		ML_SELECT_DRIVERS/*    */("Select drivers"), //
		ML_SELECT_DECORATORS/* */("Select decorators"), //
		ML_EXPAND/*            */("Expand"), //
		ML_COLLAPSE/*          */("Collapse"),//
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
			cm.getItems().add(result);
			return result;
		}

		public static Menu addMenu(Menu mu, MenuLabels ml) {
			Menu result = new Menu(ml.label());
			mu.getItems().add(result);
			return result;
		}

		public static MenuItem addMenuItem(ContextMenu cm, MenuLabels ml) {
			MenuItem result = new MenuItem(ml.label());
			cm.getItems().add(result);
			return result;
		}
	}

	
}
