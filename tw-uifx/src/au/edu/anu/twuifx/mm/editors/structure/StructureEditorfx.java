/**************************************************************************
 *  TW-APPS - Applications used by 3Worlds                                *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-APPS contains ModelMaker and ModelRunner, programs used to         *
 *  construct and run 3Worlds configuration graphs. All code herein is    *
 *  independent of UI implementation.                                     *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-APPS (3Worlds applications).                  *
 *                                                                        *
 *  TW-APPS is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-APPS is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-APPS.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
  **************************************************************************/

package au.edu.anu.twuifx.mm.editors.structure;

import java.util.List;
import org.apache.commons.text.WordUtils;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.mm.visualise.IGraphVisualiser;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

/**
 * Author Ian Davies
 *
 * Date 13 Jan. 2019
 */
// TODO move to tw-uifx
public class StructureEditorfx extends StructureEditorAdapter {

	private ContextMenu cm;
	private IMMController controller;

	public StructureEditorfx(SpecifiableNode n, MouseEvent event, IMMController controller, IGraphVisualiser gv) {
		super(n,gv);
		this.controller = controller;
		cm = new ContextMenu();
		buildgui();
		cm.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
	}

	@Override
	public void buildgui() {
		if (haveSpecification()) {
			Iterable<SimpleDataTreeNode> childSpecs = specifications.getChildSpecificationsOf(editingNodeSpec);
			List<SimpleDataTreeNode> allowedChildSpecs = newChildList(childSpecs);
			List<TreeGraphNode> orphanedChildren = orphanedChildList(childSpecs);
			Iterable<SimpleDataTreeNode> edgeSpecs = specifications.getEdgeSpecificationsOf(editingNodeSpec);
			List<Pair<String, SimpleDataTreeNode>> allowedEdges = newEdgeList(edgeSpecs);

			if (!allowedChildSpecs.isEmpty()) {
				Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW);
				for (SimpleDataTreeNode child : allowedChildSpecs) {
					addOptionNewChild(mu, child);
				}
				// add new children options
			}

			if (!orphanedChildren.isEmpty()) {
				// list new toNode edge options

			}

			if (!allowedEdges.isEmpty()) {
				// addEdgeOptions
			}

			cm.getItems().add(new SeparatorMenuItem());

			if (editingNode.hasChildren()) {
				// add exportTreeOptions
			}
			if (!allowedChildSpecs.isEmpty()) {
				// add import tree options
			}

			// --------------------------------------
			if (editingNode.canDelete() || editingNode.hasChildren())
				if (!(allowedChildSpecs.isEmpty() && orphanedChildren.isEmpty()))
					cm.getItems().add(new SeparatorMenuItem());

		}
		if (editingNode.hasChildren() || editingNode.hasOutEdges()) {
			// delete edges to children || xlinks - maybe problem
		}

		if (editingNode.canDelete()) {
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_DELETE);
			addOptionDeleteThisNode(mi);
		}

		if (editingNode.hasChildren()) {
		}

		if (!editingNode.isLeaf()) {
			cm.getItems().add(new SeparatorMenuItem());
			if (editingNode.isCollapsed()) {
				// add expand option
			} else {
				// add collapse option
			}
		}

	}

	private void addOptionDeleteThisNode(MenuItem mi) {
		mi.setOnAction((e) -> {
			// TODO: This code can be moved to the adapter.
			
			// Expand children or they would be unreachable
			if (editingNode.getSelectedVisualNode().isCollapsedParent())
				gvisualiser.expandTreeFrom(editingNode.getSelectedVisualNode());

			// Remove visual elements
			VisualNode vn = editingNode.getSelectedVisualNode();
			TreeGraphNode cn = editingNode.getConfigNode();
			gvisualiser.removeView(vn);
			vn.disconnect();
			cn.disconnect();
			vn.factory().removeNode(vn);
			cn.factory().removeNode(cn);
			controller.onNodeDeleted();
			GraphState.setChanged(true);
//			model.checkGraph();
//			model.reBuildAllElementsPropertySheet();
//			model.clearNodePropertySheet();
//			GraphState.isChanged(true);

		});

	}

	private void addOptionNewChild(Menu mu, SimpleDataTreeNode childRoot) {
		String childLabel = (String) childRoot.properties().getPropertyValue(aaIsOfClass);
		MenuItem mi = new MenuItem(childLabel);
		mu.getItems().add(mi);
		mi.setOnAction((e) -> {
			// TODO move to adapter?

			// default name is label with 1 appended
			String prompt = childLabel + "1";
			boolean captialize = specifications.nameStartsWithUpperCase(childRoot);
			if (captialize)
				prompt = WordUtils.capitalize(prompt);
			boolean modified = true;
			prompt = editingNode.proposeAnId(childLabel, prompt);
			while (modified) {
				String userName = promptForNewNode(childLabel, prompt);
				if (userName == null)
					return;
				userName = userName.trim();
				if (userName.equals(""))
					userName = prompt;
				if (captialize)
					userName = WordUtils.capitalize(userName);
				String newName = editingNode.proposeAnId(childLabel, userName);
				modified = !newName.equals(userName);
				prompt = newName;
			}

			// make the node
			newChild = editingNode.newChild(childLabel, prompt);
			Iterable<SimpleDataTreeNode> propertySpecs = specifications.getPropertySpecifications(childRoot);

			// build the properties
			controller.onNewNode(newChild);
			GraphState.setChanged(true);
		});
	}

	@Override
	public String promptForNewNode(String label, String promptName) {
		return Dialogs.getText("New " + label + "node", "", "Name:", promptName);
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
