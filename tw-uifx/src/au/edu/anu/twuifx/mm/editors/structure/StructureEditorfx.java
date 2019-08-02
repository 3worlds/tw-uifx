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
import au.edu.anu.twcore.archetype.tw.CheckSubArchetypeQuery;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.mm.visualise.IGraphVisualiser;
import fr.cnrs.iees.graph.NodeFactory;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.io.parsing.ValidPropertyTypes;
import fr.cnrs.iees.properties.ExtendablePropertyList;
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
		super(n, gv);
		this.controller = controller;
		cm = new ContextMenu();
		buildgui();
		cm.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
	}

	@Override
	public void buildgui() {
		if (haveSpecification()) {
			Iterable<SimpleDataTreeNode> childSpecs = specifications.getChildSpecificationsOf(TWA.getRoot(),
					editingNodeSpec);
			List<SimpleDataTreeNode> allowedChildSpecs = newChildList(childSpecs);
			List<TreeGraphNode> orphanedChildren = orphanedChildList(allowedChildSpecs);
			Iterable<SimpleDataTreeNode> edgeSpecs = specifications.getEdgeSpecificationsOf(editingNodeSpec);
			List<Pair<String, SimpleDataTreeNode>> allowedEdges = newEdgeList(edgeSpecs);

			if (!allowedChildSpecs.isEmpty()) {
				// add new children options
				Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW);
				for (SimpleDataTreeNode child : allowedChildSpecs) {
					addOptionNewChild(mu, child);
				}
			}

			if (!orphanedChildren.isEmpty()) {
				// list roots that can be children

			}

			if (!allowedEdges.isEmpty()) {
				// addEdgeOptions
			}

			cm.getItems().add(new SeparatorMenuItem());

			boolean addSep = editingNode.canDelete();
			if (editingNode.hasChildren()) {
				// add exportTreeOptions
				addSep = true;
			}
			if (!allowedChildSpecs.isEmpty()) {
				// add import tree options
				addSep = true;
			}

			// --------------------------------------
			if (addSep)
				cm.getItems().add(new SeparatorMenuItem());

		}
		if (editingNode.hasOutEdges()) {
			// delete xlinks
		}

		if (editingNode.canDelete()) {
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_DELETE);
			addOptionDeleteThisNode(mi);
		}

		if (editingNode.hasChildren()) {
		}

		if (!editingNode.isLeaf()) {
			cm.getItems().add(new SeparatorMenuItem());
			if (editingNode.getSelectedVisualNode().isCollapsedParent()) {
				addExpandOption(MenuLabels.addMenuItem(cm, MenuLabels.ML_EXPAND));
			} else if (editingNode.hasChildren()) {
				addCollapseOption(MenuLabels.addMenuItem(cm, MenuLabels.ML_COLLAPSE));
			}
		}
	}

	private void addCollapseOption(MenuItem mi) {
		mi.setOnAction((e) -> {
			gvisualiser.collapseTreeFrom(editingNode.getSelectedVisualNode());
			controller.onTreeCollapse();
			GraphState.setChanged(true);
		});
	}

	private void addExpandOption(MenuItem mi) {
		mi.setOnAction((e) -> {
			gvisualiser.expandTreeFrom(editingNode.getSelectedVisualNode());
			controller.onTreeExpand();
			GraphState.setChanged(true);
		});

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
		});

	}

	private void addOptionNewChild(Menu mu, SimpleDataTreeNode childRoot) {
		String childLabel = (String) childRoot.properties().getPropertyValue(aaIsOfClass);
		MenuItem mi = new MenuItem(childLabel);
		mu.getItems().add(mi);
		mi.setOnAction((e) -> {
			// TODO move to adapter?
			// default name is label with 1 appended
			String promptId = childLabel + "1";
			boolean captialize = specifications.nameStartsWithUpperCase(childRoot);
			if (captialize)
				promptId = WordUtils.capitalize(promptId);
			boolean modified = true;
			promptId = editingNode.proposeAnId(childLabel, promptId);
			while (modified) {
				String userName = promptForNewNode(childLabel, promptId);
				if (userName == null)
					return;
				userName = userName.trim();
				if (userName.equals(""))
					userName = promptId;
				if (captialize)
					userName = WordUtils.capitalize(userName);
				String newName = editingNode.proposeAnId(childLabel, userName);
				modified = !newName.equals(userName);
				promptId = newName;
			}
			// make the node
			newChild = editingNode.newChild(childLabel, promptId);
			// prompt for property creation options:
			// look for subclass
			Class subClass = null;
			List<Class> subClasses = specifications.getSubClasses(childRoot);
			if (subClasses.size() > 1) {
				subClass = promptForClass(subClasses, (String) childRoot.properties().getPropertyValue(aaIsOfClass));
				if (subClass == null)
					return;// flaw child exists in the graph if not shown??
				// need to build an extendable property list and provide it at creation time.
			} else if (subClasses.size() == 1) {
				subClass = subClasses.get(0);
			}

			if (subClass != null)
				newChild.addProperty(twaSubclass, subClass.getName());
			Iterable<SimpleDataTreeNode> propertySpecs = specifications.getPropertySpecifications(childRoot, subClass);
			for (SimpleDataTreeNode propertySpec : propertySpecs) {
				String key = (String) propertySpec.properties().getPropertyValue(twaHasName);
				// property choices - others to come.
				if (!subclassProperty(propertySpec)) {
					// we need some default values so we can have classes.
					String type = (String) propertySpec.properties().getPropertyValue(twaType);
					Object defValue = ValidPropertyTypes.getDefaultValue(type);
					System.out.println(defValue.getClass()+": "+defValue);
					newChild.addProperty((String) propertySpec.properties().getPropertyValue(twaHasName));
				}
			}

			// build the properties
			controller.onNewNode(newChild);
			GraphState.setChanged(true);
		});
	}

	private boolean subclassProperty(SimpleDataTreeNode propertySpec) {
		String value = (String) propertySpec.properties().getPropertyValue(twaHasName);
		if (value.equals(twaSubclass))
			return true;
		else
			return false;
	}

	@Override
	public Class promptForClass(List<Class> subClasses, String rootClassSimpleName) {
		String[] list = new String[subClasses.size()];
		for (int i = 0; i < subClasses.size(); i++)
			list[i] = subClasses.get(i).getSimpleName();
		int result = Dialogs.getListChoice(list, "Sub-classes", rootClassSimpleName, "select:");
		if (result != -1)
			return subClasses.get(result);
		else
			return null;
	}

	@Override
	public String promptForNewNode(String label, String promptName) {
		return Dialogs.getText("New '" + label + "' node", "", "Name:", promptName);
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
