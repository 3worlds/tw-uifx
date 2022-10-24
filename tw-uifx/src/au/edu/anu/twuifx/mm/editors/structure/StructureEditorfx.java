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

import java.util.*;

import au.edu.anu.twapps.mm.*;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.aot.archetype.Archetypes;
import au.edu.anu.twapps.mm.GraphVisualiser;
import au.edu.anu.twapps.mm.graphEditor.*;
import au.edu.anu.twapps.mm.undo.Originator;
import au.edu.anu.twapps.mm.layoutGraph.*;
import au.edu.anu.twcore.archetype.TWA;
import au.edu.anu.twcore.graphState.*;
import au.edu.anu.twuifx.mm.visualise.GraphVisualiserfx;
import fr.cnrs.iees.omugi.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.omhtk.utils.*;
import fr.cnrs.iees.twcore.constants.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import static au.edu.anu.qgraph.queries.CoreQueries.*;
import static au.edu.anu.qgraph.queries.base.SequenceQuery.*;

/**
 * Javafx extension of the {@link StructureEditorAdapter}.
 * <p>
 * This class displays a pop-up menu when the user clicks on a configuration
 * graph node. The menu options available are context dependent constrained by
 * the 3Worlds specification archetype.
 * </p>
 * <p>
 * This class has an {@link Originator}, allowing any edits to be undone or
 * redone.
 * 
 * @author Ian Davies - 13 Jan. 2019
 */

public class StructureEditorfx extends StructureEditorAdapter {

	private ContextMenu cm;
	private Originator recorder;

	/**
	 * @param n          The user-selected node for editing.
	 * @param event      The event used to place the pop-up menu at the correct
	 *                   location.
	 * @param controller The ModelMaker controller
	 * @param gv         The graph visualisation system.
	 * @param recorder   Records edits for the undo/redo system.
	 */
	public StructureEditorfx(NodeEditor n, MouseEvent event, MMController controller, GraphVisualiser gv,
			Originator recorder) {
		super(n, gv, controller);
		this.recorder = recorder;
		cm = new ContextMenu();
		buildgui();
		cm.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
//		System.out.println(this.baseSpec.toShortString());
	}

	@Override
	public void buildgui() {
		Iterable<SimpleDataTreeNode> childSpecs = specifications.getChildSpecsOf(nodeEditor, baseSpec, subClassSpec);
		List<SimpleDataTreeNode> filteredChildSpecs = filterChildSpecs(childSpecs);
		List<LayoutNode> orphanedChildren = orphanedChildList(filteredChildSpecs);
		Iterable<SimpleDataTreeNode> edgeSpecs = specifications.getEdgeSpecsOf(baseSpec, subClassSpec);
		List<Tuple<String, LayoutNode, SimpleDataTreeNode>> filteredEdgeSpecs = filterEdgeSpecs(edgeSpecs);
		List<SimpleDataTreeNode> optionalNodePropertySpecs = removeNonEditablePropertySpecs(
				specifications.getOptionalProperties(baseSpec, subClassSpec));
		// Get edge specs for currently extant edges and select those that are
		// optional.
		// TODO: No account taken of possible constraints and non-editable properties.
		// Some inconsistency in use of Iterable and List<>
		List<Duple<LayoutEdge, SimpleDataTreeNode>> optionalEdgePropertySpecs = filterOptionalEdgePropertySpecs(
				nodeEditor, edgeSpecs);
		final double duration = GraphVisualiserfx.animateSlow;

		{
			
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_NEW_NODE);
			if (!filteredChildSpecs.isEmpty() && !nodeEditor.isPredefined()) {

				for (SimpleDataTreeNode child : filteredChildSpecs) {
					String childLabel = (String) child.properties().getPropertyValue(Archetypes.IS_OF_CLASS);
					String childId = null;
					String dispName = childLabel;
					boolean reserved = false;
					if (child.properties().hasProperty(Archetypes.HAS_ID)) {
						childId = (String) child.properties().getPropertyValue(Archetypes.HAS_ID);
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
				for (Tuple<String, LayoutNode, SimpleDataTreeNode> p : filteredEdgeSpecs) {
					boolean reserved = ConfigurationReservedEdgeLabels.isPredefined(p.getFirst());
					if (!reserved) {
						MenuItem mi = MenuLabels.addMenuItem(mu,
								p.getFirst() + "->" + p.getSecond().getDisplayText(ElementDisplayText.RoleName));
						if (ConfigurationReservedNodeId.isPredefined(p.getSecond().id()) && ConfigurationReservedNodeId
								.isPredefined(nodeEditor.getName()))
							mi.setDisable(true);
						mi.setOnAction((e) -> {

							onNewEdge(p, duration);

							String desc = MenuLabels.ML_NEW_EDGE.label() + " [" + p.getFirst() + "->"
									+ p.getSecond().configNode().toShortString() + "]";

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
			if (!orphanedChildren.isEmpty() && !nodeEditor.isPredefined()) {
				for (LayoutNode vn : orphanedChildren) {
					MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(ElementDisplayText.RoleName));
					mi.setOnAction((e) -> {

						onReconnectChild(vn);

						String desc = MenuLabels.ML_NEW_CHILD_LINK.label() + " [" + vn.configNode().toShortString()
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
			if (nodeEditor.hasCollapsedChild()) {
				int count = 0;
				SortedMap<String, LayoutNode> sortedNames = new TreeMap<>();
				for (LayoutNode vn : nodeEditor.getChildren())
					sortedNames.put(vn.getDisplayText(ElementDisplayText.RoleName), vn);
				for (Map.Entry<String, LayoutNode> entry : sortedNames.entrySet()) {
					if (entry.getValue().isCollapsed()) {
						count++;
						MenuItem mi = MenuLabels.addMenuItem(mu, entry.getKey());
						mi.setOnAction((e) -> {
							onExpandTree(entry.getValue(), duration);
							String desc = MenuLabels.ML_EXPAND.label() + " [" + entry.getKey() + "]";
							recorder.addState(desc);
						});
					}
				}
				if (count > 1) {
					mu.getItems().add(new SeparatorMenuItem());
					MenuItem mi = MenuLabels.addMenuItem(mu, MenuLabels.ML_ALL.label());
					mi.setOnAction((e) -> {
						onExpandTrees(duration);
						String desc = "Expand " + MenuLabels.ML_ALL.label() + " ["
								+ nodeEditor.toString() + "]";
						recorder.addState(desc);
					});
				}
			} else
				mu.setDisable(true);
		}
		// --
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_COLLAPSE);
			if (nodeEditor.hasUncollapsedChildren()) {
				int count = 0;
				SortedMap<String, LayoutNode> sortedNames = new TreeMap<>();
				for (LayoutNode vn : nodeEditor.getChildren())
					sortedNames.put(vn.getDisplayText(ElementDisplayText.RoleName), vn);
				for (Map.Entry<String, LayoutNode> entry : sortedNames.entrySet()) {
					if (!entry.getValue().isCollapsed()) {
						count++;
						MenuItem mi = MenuLabels.addMenuItem(mu, entry.getKey());
						mi.setOnAction(e -> {
							onCollapseTree(entry.getValue(), duration);
							String desc = MenuLabels.ML_COLLAPSE.label() + " [" + entry.getKey() + "]";
							recorder.addState(desc);
						});
					}
				}
				if (count > 1) {
					mu.getItems().add(new SeparatorMenuItem());
					MenuItem mi = MenuLabels.addMenuItem(mu, MenuLabels.ML_ALL.label());
					mi.setOnAction(e -> {
						onCollapseTrees(duration);
						String desc = "Collapse " + MenuLabels.ML_ALL.label() + " ["
								+ nodeEditor.toString() + "]";
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
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_DELETE_NODE);
			if (!nodeEditor.is3Wroot() && !nodeEditor.isPredefined()) {
				mi.setOnAction((e) -> {

					String desc = MenuLabels.ML_DELETE_NODE.label() + " ["
							+ nodeEditor.toString() + "]";

					onDeleteNode(duration);

					visualiser.setLayoutRoot(controller.getLayoutRoot());

					recorder.addState(desc);
				});
			} else
				mi.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_EDGE);
			if (nodeEditor.hasOutEdges()) {
				for (LayoutEdge edge : nodeEditor.getOutEdges()) {
					LayoutNode vn = (LayoutNode) edge.endNode();

					MenuItem mi = MenuLabels.addMenuItem(mu, edge.getDisplayText(ElementDisplayText.RoleName) + "->"
							+ vn.getDisplayText(ElementDisplayText.RoleName));
					if (ConfigurationReservedNodeId.isPredefined(vn.id())
							&& ConfigurationReservedNodeId.isPredefined(nodeEditor.getName()))
						mi.setDisable(true);
					mi.setOnAction((e) -> {

						onDeleteEdge(edge);

						String desc = MenuLabels.ML_DELETE_EDGE.label() + " ["
								+ nodeEditor.toString() + "]";
						recorder.addState(desc);
					});
				}

			} else
				mu.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_CHILD_EDGE);
			if (nodeEditor.hasChildren() && !nodeEditor.isPredefined()) {
				for (LayoutNode child : nodeEditor.getChildren()) {
					MenuItem mi = MenuLabels.addMenuItem(mu, child.getDisplayText(ElementDisplayText.RoleName));
					if (child.isPredefined())
						mi.setDisable(true);
					mi.setOnAction((e) -> {

						String desc = MenuLabels.ML_DELETE_CHILD_EDGE.label() + " ["
								+ child.configNode().toShortString() + "]";

						onDeleteParentLink(child);

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
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_DELETE_TREE);
			if (nodeEditor.hasChildren() && !nodeEditor.isPredefined()) {
				Iterable<LayoutNode> lst = nodeEditor.getChildren();
				for (LayoutNode vn : lst) {
					MenuItem mi = MenuLabels.addMenuItem(mu, vn.getDisplayText(ElementDisplayText.RoleName));
					if (vn.isPredefined())
						mi.setDisable(true);
					mi.setOnAction((e) -> {
						String desc = MenuLabels.ML_DELETE_TREE.label + " [" + vn.configNode().toShortString() + "]";

						onDeleteTree(vn, duration);

						visualiser.setLayoutRoot(controller.getLayoutRoot());

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
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_OPTIONAL_PROPS);
			if (!nodeEditor.isPredefined() && (!optionalNodePropertySpecs.isEmpty())
					|| !optionalEdgePropertySpecs.isEmpty()) {
				mi.setOnAction((e) -> {

					String desc = MenuLabels.ML_OPTIONAL_PROPS.label() + " ["
							+ nodeEditor.toString() + "]";

					if (onOptionalProperties(optionalNodePropertySpecs, optionalEdgePropertySpecs)) {

						controller.onAddRemoveProperty(nodeEditor.layoutNode());
						GraphStateFactory.setChanged();
						ConfigGraph.verifyGraph();

						recorder.addState(desc);
					}
				});
			} else
				mi.setDisable(true);

		}
		// ---------------------------------------------------------------
		cm.getItems().add(new SeparatorMenuItem());
		// ---------------------------------------------------------------
		{
			MenuItem mi = MenuLabels.addMenuItem(cm, MenuLabels.ML_RENAME_NODE);
			// mi.setDisable(true);

			if (!nodeEditor.is3Wroot() && !nodeEditor.isPredefined()) {
				mi.setOnAction((e) -> {
					String desc = MenuLabels.ML_RENAME_NODE.label() + " ["
							+ nodeEditor.toString() + "]";

					if (onRenameNode()) {

						controller.model().saveAndReload();

						visualiser.setLayoutRoot(controller.getLayoutRoot());

						recorder.addState(desc);

						GraphStateFactory.setChanged();
						ConfigGraph.verifyGraph();
					}
				});
			} else
				mi.setDisable(true);
		}
		// ---
		{
			Menu mu = MenuLabels.addMenu(cm, MenuLabels.ML_RENAME_EDGE);
			if (nodeEditor.hasOutEdges()) {
				for (LayoutEdge edge : nodeEditor.getOutEdges()) {
					LayoutNode vn = (LayoutNode) edge.endNode();
					MenuItem mi = MenuLabels.addMenuItem(mu, edge.getDisplayText(ElementDisplayText.RoleName) + "->"
							+ vn.getDisplayText(ElementDisplayText.RoleName));
//					mi.setDisable(true);
					if (vn.isPredefined() && nodeEditor.isPredefined())
						mi.setDisable(true);
					mi.setOnAction((e) -> {
						String desc = MenuLabels.ML_RENAME_EDGE.label() + " [" + edge.getConfigEdge().toShortString()
								+ "]";

						if (onRenameEdge(edge)) {

							controller.model().saveAndReload();

							recorder.addState(desc);

							GraphStateFactory.setChanged();
							ConfigGraph.verifyGraph();
						}
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
			mu.setDisable(true);// TODO fix importing problem with parent table
			if (!filteredChildSpecs.isEmpty() && !nodeEditor.isPredefined()) {
				for (SimpleDataTreeNode childSpec : filteredChildSpecs) {
					MenuItem mi = MenuLabels.addMenuItem(mu,
							(String) childSpec.properties().getPropertyValue(Archetypes.IS_OF_CLASS));
					mi.setOnAction((e) -> {

						String desc = MenuLabels.ML_IMPORT_TREE.label() + " ["
								+ (String) childSpec.properties().getPropertyValue(Archetypes.IS_OF_CLASS) + "]";

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
			if (nodeEditor.hasChildren() && !nodeEditor.isPredefined()) {
				for (LayoutNode vn : nodeEditor.getChildren()) {
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
	@SuppressWarnings("unchecked")
	private List<Duple<LayoutEdge, SimpleDataTreeNode>> filterOptionalEdgePropertySpecs(NodeEditor nodeEditor,
			Iterable<SimpleDataTreeNode> edgeSpecs) {
		// Task: get edge spec for each out edge present for this node that contains
		// optional properties
		Map<String, LayoutEdge> edgeMap = new LinkedHashMap<>();
		for (LayoutEdge e : nodeEditor.getOutEdges())
			edgeMap.put(e.getConfigEdge().classId(), e);

		List<Duple<LayoutEdge, SimpleDataTreeNode>> result = new ArrayList<>();
		for (SimpleDataTreeNode es : edgeSpecs) {
			String edgeClass = (String) es.properties().getPropertyValue(Archetypes.IS_OF_CLASS);
			if (edgeMap.containsKey(edgeClass)) {
				List<SimpleDataTreeNode> propSpecs = (List<SimpleDataTreeNode>) get(es, children(),
						selectZeroOrMany(hasTheLabel(Archetypes.HAS_PROPERTY)));
				for (SimpleDataTreeNode ps : propSpecs) {
					if (specifications.getMultiplicityOf(ps).getFirst() == 0) {
//						String propName = (String) ps.properties().getPropertyValue(aaHasName);
						// add a duple of ALEdge and property spec
						result.add(new Duple<LayoutEdge, SimpleDataTreeNode>(edgeMap.get(edgeClass), ps));
//						LayoutEdge ve = edgeMap.get(edgeClass);
					}
				}
			}
		}
		return result;
	}

	private List<SimpleDataTreeNode> removeNonEditablePropertySpecs(List<SimpleDataTreeNode> propSpecs) {
		Collection<String> ne = controller.getUnEditablePropertyKeys(nodeEditor.getLabel());
		Iterator<SimpleDataTreeNode> iter = propSpecs.iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next().properties().getPropertyValue(Archetypes.HAS_NAME);
			if (ne.contains(name))
				iter.remove();
		}
		return propSpecs;
	}

	private enum MenuLabels {
		ML_NEW_NODE /*         */("New node"), // spec
		ML_NEW_EDGE/*          */("New edge"), // spec
		ML_NEW_CHILD_LINK/*    */("New child edge"), // spec
		// --------------------------------------------
		ML_IMPORT_TREE/*       */("Import sub-tree"), // spec
		ML_EXPORT_TREE/*       */("Export sub-tree"), // spec
		ML_EXPAND/*            */("Expand sub-tree"), // config
		ML_COLLAPSE/*          */("Collapse sub-tree"), // config
		// --------------------------------------------
		ML_RENAME_NODE /*      */("Rename node"), // config
		ML_RENAME_EDGE/*       */("Rename edge"), // config
		ML_DELETE_EDGE/*       */("Delete edge"), // config
		ML_DELETE_CHILD_EDGE/* */("Delete child edge"), // config
		ML_DELETE_TREE/*-      */("Delete sub-tree"), // config
		ML_DELETE_NODE/*       */("Delete node"), // config
		ML_OPTIONAL_PROPS/*    */("Optional properties..."), // config & spec
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
