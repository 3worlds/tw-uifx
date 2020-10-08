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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.Originator;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twapps.mm.graphEditor.IGraphVisualiser;
import au.edu.anu.twapps.mm.graphEditor.StructureEditorAdapter;
import au.edu.anu.twapps.mm.graphEditor.VisualNodeEditable;
import au.edu.anu.twapps.mm.layout.LayoutType;
import au.edu.anu.twapps.mm.visualGraph.ElementDisplayText;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TWA;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.mm.visualise.GraphVisualiserfx;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.io.parsing.ValidPropertyTypes;
import fr.cnrs.iees.properties.ExtendablePropertyList;
import fr.cnrs.iees.twcore.constants.ConfigurationReservedEdgeLabels;
import fr.cnrs.iees.twcore.constants.ConfigurationReservedNodeId;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import fr.ens.biologie.generic.utils.Tuple;

/**
 * Author Ian Davies
 *
 * Date 13 Jan. 2019
 */

public class StructureEditorfx extends StructureEditorAdapter {

	private ContextMenu cm;
	private Originator recorder;

	public StructureEditorfx(VisualNodeEditable n, MouseEvent event, IMMController controller, IGraphVisualiser gv,
			Originator recorder) {
		super(n, gv, controller);
		this.recorder = recorder;
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
		List<SimpleDataTreeNode> optionalPropertySpecs = filterOptionalPropertySpecs(
				specifications.getOptionalProperties(baseSpec, subClassSpec));
		final double duration = GraphVisualiserfx.animateSlow;

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

							/*-
							 * NB: Graph is not valid until the new node is placed on screen in the controller!
							 * Rollover is done then.
							 */
						});
					}
				}
			} else
				mu.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW_EDGE);
			if (!filteredEdgeSpecs.isEmpty()) {
				for (Tuple<String, VisualNode, SimpleDataTreeNode> p : filteredEdgeSpecs) {
					boolean reserved = ConfigurationReservedEdgeLabels.isPredefined(p.getFirst());
					if (!reserved) {
						MenuItem mi = MenuLabels.addMenuItem(mu,
								p.getFirst() + "->" + p.getSecond().getDisplayText(ElementDisplayText.RoleName));
						if (ConfigurationReservedNodeId.isPredefined(p.getSecond().id())
								&& ConfigurationReservedNodeId.isPredefined(editableNode.getConfigNode().id()))
							mi.setDisable(true);
						mi.setOnAction((e) -> {

							onNewEdge(p, duration);

							String desc = MenuLabels.ML_NEW_EDGE.label() + " [" + p.getFirst() + "->"
									+ p.getSecond().getConfigNode().toShortString() + "]";

							recorder.addState(desc);
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
					MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(ElementDisplayText.RoleName));
					mi.setOnAction((e) -> {

						onReconnectChild(vn);

						String desc = MenuLabels.ML_NEW_CHILD_LINK.label() + " [" + vn.getConfigNode().toShortString()
								+ "]";

						recorder.addState(desc);
					});
				}
			} else
				mu.setDisable(true);
		}
		// ---------------------------------------------------------------
		cm.getItems().add(new SeparatorMenuItem());
		// ---------------------------------------------------------------

		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_EXPAND);
			if (editableNode.getSelectedVisualNode().hasCollaspedChild()) {
				int count = 0;
				for (VisualNode vn : editableNode.getSelectedVisualNode().getChildren()) {
					if (vn.isCollapsed()) {
						count++;
						MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(ElementDisplayText.RoleName));
						mi.setOnAction((e) -> {
							onExpandTree(vn, duration);
						});
					}
				}
				if (count > 1) {
					mu.getItems().add(new SeparatorMenuItem());
					MenuItem mi = MenuLabels.addMenuItem(mu, MenuLabels.ML_ALL.label());
					mi.setOnAction((e) -> {
//						Rollover.saveState(MenuLabels.ML_EXPAND.label() + " All ", ConfigGraph.getGraph(),
//								gvisualiser.getVisualGraph());
						onExpandTrees(duration);
					});
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
						MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(ElementDisplayText.RoleName));
						mi.setOnAction(e -> onCollapseTree(vn, duration));
					}
				}
				if (count > 1) {
					mu.getItems().add(new SeparatorMenuItem());
					MenuItem mi = MenuLabels.addMenuItem(mu, MenuLabels.ML_ALL.label());
					mi.setOnAction(e -> onCollapseTrees(duration));
//					Rollover.saveState(MenuLabels.ML_COLLAPSE.label() + " All ", ConfigGraph.getGraph(),
//							gvisualiser.getVisualGraph());
				}
			} else
				mu.setDisable(true);
		}
		// --

		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_APPLYLAYOUT);
			for (LayoutType lt : LayoutType.values()) {
				MenuItem mi = new MenuItem(lt.name());
				mu.getItems().add(mi);
				if (lt.equals(controller.getCurrentLayout()))
					mi.setText("*" + mi.getText());
				mi.setUserData(lt);
				mi.setOnAction((e) -> {
					LayoutType layout = (LayoutType) ((MenuItem) e.getSource()).getUserData();
					controller.doFocusedLayout(editableNode.getSelectedVisualNode(), layout, duration);

//					String desc = MenuLabels.ML_APPLYLAYOUT.label + " [" + layout.name() + "]";
//
//					recorder.addState(desc);

				});
			}
		}
		// --
		{
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_SHOWLOCALGRAPH);
			mi.setOnAction((e) -> {
				String title = MenuLabels.ML_SHOWLOCALGRAPH.label();
				String header = "Show graph surrounding '"
						+ editableNode.getSelectedVisualNode().getDisplayText(ElementDisplayText.RoleName) + "'.";
				String content = "Path length: ";
				String defaultValue = "1";
				String result = Dialogs.getText(title, header, content, defaultValue, Dialogs.vsInteger);
				if (result != null) {
					int depth = Integer.parseInt(result);
					gvisualiser.showLocalGraph(editableNode.getSelectedVisualNode(), depth);

//					String desc = MenuLabels.ML_SHOWLOCALGRAPH.label() + " ["
//							+ editableNode.getConfigNode().toShortString() + "(" + depth + ")]";
//					recorder.addState(desc);
				}

			});
		}
		// ---------------------------------------------------------------
		cm.getItems().add(new SeparatorMenuItem());
		// ---------------------------------------------------------------
		{
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_DELETE_NODE);
			if (!editableNode.isRoot() && !editableNode.isPredefined()) {
				mi.setOnAction((e) -> {

					String desc = MenuLabels.ML_DELETE_NODE.label() + " ["
							+ editableNode.getConfigNode().toShortString() + "]";

					onDeleteNode(duration);

					recorder.addState(desc);
				});
			} else
				mi.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_EDGE);
			if (editableNode.hasOutEdges()) {
				for (VisualEdge edge : editableNode.getOutEdges()) {
					VisualNode vn = (VisualNode) edge.endNode();

					MenuItem mi = MenuLabels.addMenuItem(mu, edge.getDisplayText(ElementDisplayText.RoleName) + "->"
							+ vn.getDisplayText(ElementDisplayText.RoleName));
					if (ConfigurationReservedNodeId.isPredefined(vn.id())
							&& ConfigurationReservedNodeId.isPredefined(editableNode.getConfigNode().id()))
						mi.setDisable(true);
					mi.setOnAction((e) -> {

						onDeleteEdge(edge);

						String desc = MenuLabels.ML_DELETE_EDGE.label() + " ["
								+ editableNode.getConfigNode().toShortString() + "]";
						recorder.addState(desc);
					});
				}

			} else
				mu.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_CHILD_EDGE);
			if (editableNode.hasChildren() && !editableNode.isPredefined()) {
				for (VisualNode child : editableNode.getSelectedVisualNode().getChildren()) {
					MenuItem mi = MenuLabels.addMenuItem(mu, child.getDisplayText(ElementDisplayText.RoleName));
					if (child.isPredefined())
						mi.setDisable(true);
					mi.setOnAction((e) -> {

						String desc = MenuLabels.ML_DELETE_CHILD_EDGE.label() + " ["
								+ child.getConfigNode().toShortString() + "]";

						onDeleteParentLink(child);

						recorder.addState(desc);
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
					MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(ElementDisplayText.RoleName));
					if (vn.isPredefined())
						mi.setDisable(true);
					mi.setOnAction((e) -> {
						String desc = MenuLabels.ML_DELETE_TREE.label + " [" + vn.getConfigNode().toShortString() + "]";

						onDeleteTree(vn, duration);

						recorder.addState(desc);
					});
				}
			} else
				mu.setDisable(true);
		}
		{
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_OPTIONAL_PROPS);
			if (!editableNode.isPredefined() && !optionalPropertySpecs.isEmpty()) {
				mi.setOnAction((e) -> {

					String desc = MenuLabels.ML_OPTIONAL_PROPS.label() + " ["
							+ editableNode.getConfigNode().toShortString() + "]";

					if (onOptionalProperties(optionalPropertySpecs))

						recorder.addState(desc);
				});
			} else
				mi.setDisable(true);

		}
		// ---------------------------------------------------------------
		cm.getItems().add(new SeparatorMenuItem());
		// ---------------------------------------------------------------
		{
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_RENAME_NODE);

			if (!editableNode.isRoot() && !editableNode.isPredefined()) {
				mi.setOnAction((e) -> {
					String desc = MenuLabels.ML_RENAME_NODE.label() + " ["
							+ editableNode.getConfigNode().toShortString() + "]";

					onRenameNode();

					recorder.addState(desc);
				});
			} else
				mi.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_RENAME_EDGE);
			if (editableNode.hasOutEdges()) {
				for (VisualEdge edge : editableNode.getOutEdges()) {
					VisualNode vn = (VisualNode) edge.endNode();
					MenuItem mi = MenuLabels.addMenuItem(mu, edge.getDisplayText(ElementDisplayText.RoleName) + "->"
							+ vn.getDisplayText(ElementDisplayText.RoleName));
					if (vn.isPredefined() && editableNode.isPredefined())
						mi.setDisable(true);
					mi.setOnAction((e) -> {
						String desc = MenuLabels.ML_RENAME_EDGE.label() + " [" + edge.getConfigEdge().toShortString()
								+ "]";

						onRenameEdge(edge);

						recorder.addState(desc);
					});
				}
//				}
			} else
				mu.setDisable(true);
		}
		// ---------------------------------------------------------------
		cm.getItems().add(new SeparatorMenuItem());
		// ---------------------------------------------------------------
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_IMPORT_TREE);
			if (!filteredChildSpecs.isEmpty() && !editableNode.isPredefined()) {
				for (SimpleDataTreeNode childSpec : filteredChildSpecs) {
					MenuItem mi = MenuLabels.addMenuItem(mu,
							(String) childSpec.properties().getPropertyValue(aaIsOfClass));
					mi.setOnAction((e) -> {

						String desc = MenuLabels.ML_IMPORT_TREE.label() + " ["
								+ (String) childSpec.properties().getPropertyValue(aaIsOfClass) + "]";

						onImportTree(childSpec, duration);

						recorder.addState(desc);
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
					MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(ElementDisplayText.RoleName));
					if (vn.isPredefined())
						mi.setDisable(true);
					mi.setOnAction((e) -> {
						onExportTree(vn);
					});

				}
			} else
				mu.setDisable(true);
		}

	}

	// TODO Move to adaptor
	private List<SimpleDataTreeNode> filterOptionalPropertySpecs(List<SimpleDataTreeNode> propSpecs) {
		Collection<String> ne = controller.getUnEditablePropertyKeys(editableNode.cClassId());
		Iterator<SimpleDataTreeNode> iter = propSpecs.iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next().properties().getPropertyValue(aaHasName);
			if (ne.contains(name))
				iter.remove();
		}
		return propSpecs;
	}

	// TODO Move to adaptor
	private boolean onOptionalProperties(List<SimpleDataTreeNode> propertySpecs) {
		List<String> items = new ArrayList<>();
		List<Boolean> selected = new ArrayList<>();
		TreeGraphDataNode cn = (TreeGraphDataNode) editableNode.getConfigNode();
		for (SimpleDataTreeNode p : propertySpecs) {
			String name = (String) p.properties().getPropertyValue(aaHasName);
			items.add(name);
			if (cn.properties().hasProperty(name))
				selected.add(true);
			else
				selected.add(false);
		}
		List<String> selectedItems = getCBSelections(items, selected);
		boolean change = false;
		List<String> additions = new ArrayList<>();
		List<String> deletions = new ArrayList<>();

		Set<String> currentKeys = cn.properties().getKeysAsSet();
		for (String key : currentKeys)
			if (items.contains(key))
				if (!selectedItems.contains(key))
					deletions.add(key);
		for (String key : selectedItems)
			if (!currentKeys.contains(key))
				additions.add(key);

		ExtendablePropertyList props = (ExtendablePropertyList) cn.properties();
		for (String key : deletions) {
			props.removeProperty(key);
		}
		for (String key : additions) {
			// find the spec
			SimpleDataTreeNode pSpec = getPropertySpec(key, propertySpecs);
			String type = (String) pSpec.properties().getPropertyValue(aaType);
			Object defValue = ValidPropertyTypes.getDefaultValue(type);
			props.addProperty(key, defValue);
		}

		if (!deletions.isEmpty() || !additions.isEmpty()) {
			controller.onAddRemoveProperty(editableNode.getSelectedVisualNode());
			GraphState.setChanged();
			ConfigGraph.validateGraph();
			return true;
		}
		return false;
	}

	private SimpleDataTreeNode getPropertySpec(String key, List<SimpleDataTreeNode> propertySpecs) {
		for (SimpleDataTreeNode p : propertySpecs) {
			String name = (String) p.properties().getPropertyValue(aaHasName);
			if (name.equals(key))
				return p;
		}
		return null;
	}

	private List<String> getCBSelections(List<String> items, List<Boolean> selected) {
		Dialog<ButtonType> dlg = new Dialog<>();
		dlg.setTitle(editableNode.getConfigNode().toShortString());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		dlg.initOwner((Window) Dialogs.owner());
		GridPane content = new GridPane();
		content.setVgap(15);
		content.setHgap(10);
		dlg.getDialogPane().setContent(content);
		List<CheckBox> chkbxs = new ArrayList<>();
		content.add(new Label("Optional properties"), 0, 0);
		for (int i = 0; i < items.size(); i++) {
			CheckBox cx = new CheckBox(items.get(i));
			chkbxs.add(cx);
			cx.setSelected(selected.get(i));
		}

		chkbxs.sort(new Comparator<CheckBox>() {

			@Override
			public int compare(CheckBox cb1, CheckBox cb2) {
				return cb1.getText().compareTo(cb2.getText());
			}
		});

		int row = 1;
		for (CheckBox cx : chkbxs) {
			content.add(cx, 0, row++);
		}

		Optional<ButtonType> btn = dlg.showAndWait();
		List<String> result = new ArrayList<>();
		if (btn.get().equals(ok)) {
			for (CheckBox cx : chkbxs)
				if (cx.isSelected())
					result.add(cx.getText());
			return result;
		} else {
			for (int i = 0; i < items.size(); i++)
				if (selected.get(i))
					result.add(items.get(i));

			return result;
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
		ML_DELETE_CHILD_EDGE/* */("Delete child edge"), // config
		ML_DELETE_TREE/*-      */("Delete tree"), // config
		ML_DELETE_NODE/*       */("Delete node"), // config
		ML_OPTIONAL_PROPS/*    */("Optional properties..."),
		// --------------------------------------------
		ML_ALL/*               */("All"), //
		ML_APPLYLAYOUT/*       */("Apply layout"), //
		ML_SHOWLOCALGRAPH/*    */("Show neighbourhood..."),;

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
