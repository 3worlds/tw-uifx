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
import au.edu.anu.twcore.archetype.tw.ChildXorPropertyQuery;
import au.edu.anu.twcore.archetype.tw.NodeAtLeastOneChildLabelOfQuery;
import au.edu.anu.twcore.archetype.tw.PropertyXorQuery;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.mm.visualise.IGraphVisualiser;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.identity.impl.PairIdentity;
import fr.cnrs.iees.io.parsing.ValidPropertyTypes;
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
			Iterable<SimpleDataTreeNode> childSpecs = specifications.getChildSpecificationsOf(baseSpec, subClassSpec,
					TWA.getRoot());
			List<SimpleDataTreeNode> allowedChildSpecs = newChildList(childSpecs);
			List<TreeGraphNode> orphanedChildren = orphanedChildList(allowedChildSpecs);
			Iterable<SimpleDataTreeNode> edgeSpecs = specifications.getEdgeSpecificationsOf(baseSpec);
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

	private void addOptionNewChild(Menu mu, SimpleDataTreeNode childBaseSpec) {
		String childLabel = (String) childBaseSpec.properties().getPropertyValue(aaIsOfClass);
		MenuItem mi = new MenuItem(childLabel);
		mu.getItems().add(mi);
		mi.setOnAction((e) -> {
			// TODO move to adapter?
			// default name is label with 1 appended
			String promptId = childLabel + "1";
			boolean captialize = specifications.nameStartsWithUpperCase(childBaseSpec);
			if (captialize)
				promptId = WordUtils.capitalize(promptId);
			boolean modified = true;
			promptId = editingNode.proposeAnId(childLabel, promptId);
			while (modified) {
				String userName = promptForNewNode(childLabel, promptId);
				if (userName == null)
					return;// cancel
				userName = userName.trim();
				if (userName.equals(""))
					userName = promptId;
				if (captialize)
					userName = WordUtils.capitalize(userName);
				String newName = editingNode.proposeAnId(childLabel, userName);
				modified = !newName.equals(userName);
				promptId = newName;
			}
			// prompt for property creation options:
			// TODO One dialog for all options.
			// look for subclass
			String childClassName = (String) childBaseSpec.properties().getPropertyValue(aaIsOfClass);
			Class subClass = null;
			List<Class> subClasses = specifications.getSubClasses(childBaseSpec);
			if (subClasses.size() > 1) {
				subClass = promptForClass(subClasses, childClassName);
				if (subClass == null)
					return;// cancel
			} else if (subClasses.size() == 1) {
				subClass = subClasses.get(0);
			}
			SimpleDataTreeNode childSubSpec = specifications.getSubSpecsOf(childBaseSpec, subClass);
			// ChildXorPropertyQuery (e.g. Record,dataElementType)
			List<String[]> entriesChildXorProperty = specifications.getQueryStringTables(childBaseSpec,
					ChildXorPropertyQuery.class);
			entriesChildXorProperty
					.addAll(specifications.getQueryStringTables(childSubSpec, ChildXorPropertyQuery.class));
			List<String> selectedKeysChildXorProperty = null;
			if (!entriesChildXorProperty.isEmpty()) {
				selectedKeysChildXorProperty = promptForTableEntryChoice(
						childClassName + PairIdentity.LABEL_NAME_SEPARATOR + promptId, "Property choices", "",
						entriesChildXorProperty);
				if (selectedKeysChildXorProperty == null)
					return;
			}

			List<String[]> entriesPropertiesXor = specifications.getQueryStringTables(childBaseSpec,
					PropertyXorQuery.class);
			entriesPropertiesXor.addAll(specifications.getQueryStringTables(childSubSpec, PropertyXorQuery.class));
			List<String> selectedKeysPropertyXor = null;
			if (!entriesPropertiesXor.isEmpty()) {
				selectedKeysPropertyXor = promptForTableEntryChoice(
						childClassName + PairIdentity.LABEL_NAME_SEPARATOR + promptId, "Property choices", "",
						entriesPropertiesXor);
				if (selectedKeysPropertyXor == null)
					return;
			}
			// make the node
			newChild = editingNode.newChild(childLabel, promptId);

			Iterable<SimpleDataTreeNode> propertySpecs = specifications.getPropertySpecifications(childBaseSpec,
					childSubSpec);
			// we could remove the pspec based on the above choices before
			// build the properties
			for (SimpleDataTreeNode propertySpec : propertySpecs) {
				String key = (String) propertySpec.properties().getPropertyValue(twaHasName);
				// property choices - others to come.
				if (!subclassProperty(propertySpec)) {
					boolean addProperty = true;
					String optionalKey = getSelectedEntry(key, selectedKeysChildXorProperty, entriesChildXorProperty);
					if (optionalKey != null && !optionalKey.equals(key))
						addProperty = false;
					if (addProperty) {
						optionalKey = getSelectedEntry(key, selectedKeysPropertyXor, entriesPropertiesXor);
						if (optionalKey != null && !optionalKey.equals(key))
							addProperty = false;
					}
					if (addProperty) {
						String type = (String) propertySpec.properties().getPropertyValue(twaType);
						Object defValue = ValidPropertyTypes.getDefaultValue(type);
						System.out.println(key + "; " + defValue.getClass() + ": " + defValue);
						newChild.addProperty(key, defValue);
					}
				} else
					newChild.addProperty(twaSubclass, subClass.getName());
			}

			controller.onNewNode(newChild);
			GraphState.setChanged(true);
		});
	}

	private static String getSelectedEntry(String key, List<String> selectedKeys, List<String[]> entries) {
		if (selectedKeys == null)
			return null;
		for (int i = 0; i < selectedKeys.size(); i++) {
			String sel = selectedKeys.get(i);
			String[] entry = entries.get(i);
			for (int j = 0; j < entry.length; j++) {
				if (entry[j].equals(sel))
					return sel;
			}
		}
		return null;
	}

	private List<String> promptForTableEntryChoice(String title, String header, String content,
			List<String[]> entries) {
		return Dialogs.getRadioButtonChoices(title, header, content, entries);
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
