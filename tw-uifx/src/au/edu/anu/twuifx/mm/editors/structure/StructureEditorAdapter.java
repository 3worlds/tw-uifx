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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.text.WordUtils;

import au.edu.anu.rscs.aot.archetype.ArchetypeArchetypeConstants;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TWA;
import au.edu.anu.twcore.archetype.TwArchetypeConstants;
import au.edu.anu.twcore.archetype.tw.ChildXorPropertyQuery;
import au.edu.anu.twcore.archetype.tw.ExclusiveCategoryQuery;
import au.edu.anu.twcore.archetype.tw.OutNodeXorQuery;
import au.edu.anu.twcore.archetype.tw.PropertyXorQuery;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.root.EditableFactory;
import au.edu.anu.twcore.userProject.UserProjectLink;
import au.edu.anu.twuifx.mm.visualise.IGraphVisualiser;
import fr.cnrs.iees.graph.Node;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.identity.impl.PairIdentity;
import fr.cnrs.iees.io.parsing.ValidPropertyTypes;
import static fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
//import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
import fr.ens.biologie.generic.utils.Duple;
import fr.ens.biologie.generic.utils.Logging;

public abstract class StructureEditorAdapter
		implements StructureEditable, TwArchetypeConstants, ArchetypeArchetypeConstants {
	private static Logger log = Logging.getLogger(StructureEditorAdapter.class);

	/* what we need to know from the archetype graph */
	protected Specifications specifications;
	/*
	 * what we need to know from the visualNode that has been selected for editing
	 */
	protected VisualNodeEditable editableNode;
	/*
	 * new node created by this editor. May be null because the op is not
	 * necessarily node creation.
	 */
	protected VisualNode newChild;

	/* specifications of this editingNode */
	protected SimpleDataTreeNode baseSpec;

	/* specifications of subclass of this editingNode if it has one */
	protected SimpleDataTreeNode subClassSpec;

	protected IGraphVisualiser gvisualiser;

	protected IMMController controller;

	public StructureEditorAdapter(VisualNodeEditable selectedNode, IGraphVisualiser gv, IMMController controller) {
		super();
		this.specifications = new TwSpecifications();
		this.controller = controller;
		this.newChild = null;
		this.editableNode = selectedNode;
		Set<String> discoveredFile = new HashSet<>();
		this.baseSpec = specifications.getSpecsOf(editableNode.cClassId(), editableNode.createdBy(), TWA.getRoot(),
				discoveredFile);

		this.subClassSpec = specifications.getSubSpecsOf(baseSpec, editableNode.getSubClass());
		this.gvisualiser = gv;
		log.info("BaseSpec: " + baseSpec);
		log.info("SubSpec: " + subClassSpec);
	}

	@Override
	public List<SimpleDataTreeNode> filterChildSpecs(Iterable<SimpleDataTreeNode> childSpecs) {
		// childXorPropertyQuerySpec
		List<SimpleDataTreeNode> result = new ArrayList<SimpleDataTreeNode>();
		List<String[]> tables = specifications.getQueryStringTables(baseSpec, ChildXorPropertyQuery.class);
		tables.addAll(specifications.getQueryStringTables(subClassSpec, ChildXorPropertyQuery.class));
		for (SimpleDataTreeNode childSpec : childSpecs) {
			String childLabel = (String) childSpec.properties().getPropertyValue(aaIsOfClass);
			IntegerRange range = specifications.getMultiplicityOf(childSpec);
			if (editableNode.moreChildrenAllowed(range, childLabel)) {
				if (!tables.isEmpty()) {
					if (allowedChild(childLabel, tables))
						result.add(childSpec);
				} else
					result.add(childSpec);
			}
		}
		Collections.sort(result, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o1.id().compareToIgnoreCase(o2.id());
			}
		});
		return result;
	}

	private boolean allowedChild(String childLabel, List<String[]> tables) {
		VisualNode vn = editableNode.getSelectedVisualNode();
		for (String[] ss : tables) {
			if (ss[0].equals(childLabel)) {
				if (vn.configHasProperty(ss[1]))
					return false;
			}
		}
		return true;
	};

	private List<VisualNode> findNodesLabelled(String label) {
		List<VisualNode> result = new ArrayList<>();
		TreeGraph<VisualNode, VisualEdge> vg = gvisualiser.getVisualGraph();
		for (VisualNode vn : vg.nodes()) {
			if (vn.cClassId().equals(label))
				result.add(vn);
		}
		return result;
	}

	// @Override
	public List<Duple<String, VisualNode>> filterEdgeSpecs(Iterable<SimpleDataTreeNode> edgeSpecs) {
		// 1) Do the constraints allow this edge to exist?
		// 2) does multiplicity allow for this edge?
		// 3) do we have available end nodes?
		// Test cases:
		// 1) Table: dimensioner 1..*
		List<Duple<String, VisualNode>> result = new ArrayList<>();
		for (SimpleDataTreeNode edgeSpec : edgeSpecs) {
			String toNodeRef = (String) edgeSpec.properties().getPropertyValue(aaToNode);
			String edgeLabel = (String) edgeSpec.properties().getPropertyValue(aaIsOfClass);
			List<VisualNode> endNodes = findNodesLabelled(toNodeRef.replace(":", ""));
			for (VisualNode endNode : endNodes) {
				if (!editableNode.getSelectedVisualNode().id().equals(endNode.id()))
					if (!editableNode.hasOutEdgeTo(endNode, edgeLabel)) {
						if (satisfyExclusiveCategoryQuery(edgeSpec, endNode, edgeLabel))
							if (satisfyOutNodeXorQuery(edgeSpec, endNode, edgeLabel))
								result.add(new Duple<String, VisualNode>(edgeLabel, endNode));
					}
			}
		}
		Collections.sort(result, new Comparator<Duple<String, VisualNode>>() {
			@Override
			public int compare(Duple<String, VisualNode> o1, Duple<String, VisualNode> o2) {
				String s1 = o1.getFirst() + o1.getSecond();
				String s2 = o2.getFirst() + o2.getSecond();
				return s1.compareToIgnoreCase(s2);
			}
		});

		return result;
	}

	@SuppressWarnings("unchecked")
	private boolean satisfyOutNodeXorQuery(SimpleDataTreeNode edgeSpec, VisualNode proposedEndNode, String edgeLabel) {
		List<SimpleDataTreeNode> queries = specifications.getQueries((SimpleDataTreeNode) edgeSpec.getParent(),
				OutNodeXorQuery.class);
		if (queries.isEmpty())
			return true;
		List<Duple<String, String>> entries = specifications.getNodeLabelDuples(queries);

		// can have either of the entires
		if (!editableNode.hasOutEdges())
			return true;

		// if there is an out node which is not of the same label as proposedEndNode
		// then return false
		String currentChoice = getCurrentXORChoice(entries);
		// no outNodes with label in the set of duples
		if (currentChoice == null)
			return true;
		// Can't change to other choice
		if (!currentChoice.equals(proposedEndNode.cClassId())) {
			log.info("Fail");
			return false;
		}
		return true;
	}

	private String getCurrentXORChoice(List<Duple<String, String>> entries) {
		for (VisualNode outNode : editableNode.getOutNodes()) {
			String outLabel = outNode.cClassId();
			for (Duple<String, String> duple : entries) {
				if (duple.getFirst().equals(outLabel) || duple.getSecond().equals(outLabel))
					return outLabel;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private boolean satisfyExclusiveCategoryQuery(SimpleDataTreeNode edgeSpec, VisualNode proposedEndNode,
			String edgeLabel) {
		if (specifications.getQueries((SimpleDataTreeNode) edgeSpec.getParent(), ExclusiveCategoryQuery.class)
				.isEmpty())
			return true;
		VisualNode proposedCatSet = proposedEndNode.getParent();
		for (VisualEdge edge : editableNode.getOutEdges()) {
			if (edge.getConfigEdge().classId().equals(E_BELONGSTO.label())) {
				VisualNode myCat = (VisualNode) edge.endNode();
				VisualNode myCatSet = myCat.getParent();
				if (proposedCatSet.id().equals(myCatSet.id())) {
					log.info("Fail");
					return false;
				}
			}
		}
		return true;
	}

	public List<VisualNode> orphanedChildList(Iterable<SimpleDataTreeNode> childSpecs) {
		List<VisualNode> result = new ArrayList<>();
		for (VisualNode root : editableNode.graphRoots()) {
			String rootLabel = root.cClassId();
			for (SimpleDataTreeNode childSpec : childSpecs) {
				String specLabel = (String) childSpec.properties().getPropertyValue(aaIsOfClass);
				if (rootLabel.equals(specLabel))
					result.add(root);
			}
		}
		Collections.sort(result, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o1.id().compareToIgnoreCase(o2.id());
			}
		});
		return result;
	}

	protected boolean haveSpecification() {
		return baseSpec != null;
	}

	private void connectTo(Duple<String, VisualNode> p) {
		VisualEdge vEdge = editableNode.newEdge(p.getFirst(), p.getSecond());
		gvisualiser.onNewEdge(vEdge);
	}

	private String promptForNewNode(String label, String promptName) {
		return Dialogs.getText("'" + label + "' node name.", "", "Name:", promptName);
	}

	private Class<? extends TreeNode> promptForClass(List<Class<? extends TreeNode>> subClasses,
			String rootClassSimpleName) {
		String[] list = new String[subClasses.size()];
		for (int i = 0; i < subClasses.size(); i++)
			list[i] = subClasses.get(i).getSimpleName();
		int result = Dialogs.getListChoice(list, "Sub-classes", rootClassSimpleName, "select:");
		if (result != -1)
			return subClasses.get(result);
		else
			return null;
	}

	private String getNewName(String childLabel, SimpleDataTreeNode childBaseSpec) {
		// default name is label with 1 appended

		String promptId = childLabel.replaceAll("[aeiou]", "") + "1";
		boolean captialize = specifications.nameStartsWithUpperCase(childBaseSpec);
		if (captialize)
			promptId = WordUtils.capitalize(promptId);
		boolean modified = true;
		promptId = editableNode.proposeAnId(promptId);
		while (modified) {
			String userName = promptForNewNode(childLabel, promptId);
			if (userName == null)
				return null;// cancel
			userName = userName.trim();
			if (userName.equals(""))
				return null; // implicit cancel
			// userName = promptId;
			if (captialize)
				userName = WordUtils.capitalize(userName);
			String newName = editableNode.proposeAnId(userName);
			modified = !newName.equals(userName);
			promptId = newName;
		}
		return promptId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onNewChild(String childLabel, SimpleDataTreeNode childBaseSpec) {
		String promptId = getNewName(childLabel, childBaseSpec);
		if (promptId == null)
			return;
		String childClassName = (String) childBaseSpec.properties().getPropertyValue(aaIsOfClass);
		Class<? extends TreeNode> subClass = null;
		List<Class<? extends TreeNode>> subClasses = specifications.getSubClassesOf(childBaseSpec);
		if (subClasses.size() > 1) {
			subClass = promptForClass(subClasses, childClassName);
			if (subClass == null)
				return;// cancel
		} else if (subClasses.size() == 1) {
			subClass = subClasses.get(0);
		}
		SimpleDataTreeNode childSubSpec = specifications.getSubSpecsOf(childBaseSpec, subClass);
		// unfiltered propertySpecs
		Iterable<SimpleDataTreeNode> propertySpecs = specifications.getPropertySpecsOf(childBaseSpec, childSubSpec);
		if (!specifications.filterPropertyStringTableOptions(propertySpecs, childBaseSpec, childSubSpec,
				childClassName + PairIdentity.LABEL_NAME_SEPARATOR + promptId, ChildXorPropertyQuery.class,
				PropertyXorQuery.class))
			return;// cancel

		// make the node
		newChild = editableNode.newChild(childLabel, promptId);
		newChild.setCollapse(false);
		newChild.setCategory();

		for (SimpleDataTreeNode propertySpec : propertySpecs) {
			String key = (String) propertySpec.properties().getPropertyValue(aaHasName);
//			System.out.println(key);
			if (key.equals(twaSubclass))
				newChild.addProperty(twaSubclass, subClass.getName());
			else {
				String type = (String) propertySpec.properties().getPropertyValue(aaType);
				Object defValue = ValidPropertyTypes.getDefaultValue(type);
				newChild.addProperty(key, defValue);
			}
		}
		/*
		 * hasNode recordFieldSpec isOfClass = String("field")
		 * 
		 * hasProperty
		 * 
		 * hasName = String("type") type = String("DataElementType")
		 * 
		 * StringTable(([2]"type","3wSA-RealFieldMetadata.ugt"))
		 * 
		 * value = fr.cnrs.iees.twcore.constants.DataElementType(Double)
		 * 
		 * --------------------------
		 * 
		 * hasNode tableSpec isOfClass = String("table") *
		 * 
		 * hasProperty dataElementTypeSpec
		 * 
		 * hasName = String("dataElementType")
		 * 
		 * type = String("DataElementType")
		 * 
		 * StringTable(([2]"dataElementType","3wSA-RealTableMetadata.ugt"))
		 * 
		 * value = fr.cnrs.iees.twcore.constants.DataElementType(Double)
		 *
		 */
		/*
		 * 
		 * TODO How to handle non-subclass SAs. These SAs, unlike subclass SAs, are in
		 * the root of the archetype (why i don't know) i.e. they are already
		 * instantiated in the main arch and therefore they should not be opened and
		 * loaded into the MAP of SAs in TWA. So, what to do?
		 * 
		 * 1) check if base spec has SA query with first par not equal to 'subclass'
		 * 
		 * 2) See if node instance has a property key equal to par.get(0). Should be an
		 * exception if this is not the case.
		 * 
		 * 3) Find which query is relevant to the actual property class in the node
		 * instance (i.e all other properties should exist at this time) by looking at
		 * the 'value' property.
		 * 
		 * 4) select all hasNodes of this isOfClass excepting this baseClassSpec. Here
		 * we come unstuck because we don't know how the map the value entry to a
		 * hasNode in the archetype. The only way to do this is ignore what's in the main
		 * arch and load the SA again. Oh well, I shuoldn't have prevented non-subclass
		 * SAs from loading into the TWA SA lookup. So I ignore the entries of this
		 * 'isOfClass' (bug in waiting) and load the sa and add the properties ot the
		 * newly created node.
		 * 
		 * 
		 * 
		 * 
		 */

//		if (!newChild.configHasProperty(twaSubclass)) {
//			
//			
//		}
		// Not sure yet if this is a good idea. Perhaps the user should decide the order
		// prompted by a query
//
//		if (newChild.cClassId().equals(N_DIMENSIONER.label())) {
//			setDefaultDimRank(newChild);
//		}

		controller.onNewNode(newChild);
	}
// If we do this its better done by examining the appropriate query
//	private void setDefaultDimRank(VisualNode newChild2) {
//		int mxRank = 0;
//		for (TreeGraphNode n : ConfigGraph.getGraph().nodes()) {
//			if (n.classId().equals(N_DIMENSIONER.label())) {
//				TreeGraphDataNode dn = (TreeGraphDataNode) n;
//				int r = (Integer) dn.properties().getPropertyValue(P_DIMENSIONER_RANK.key());
//				mxRank = Math.max(r, mxRank);
//			}
//			newChild.getConfigNode().properties().setProperty(P_DIMENSIONER_RANK.key(), mxRank + 1);
//		}
//
//	}

	@Override
	public void onNewEdge(Duple<String, VisualNode> duple) {
		if (editableNode.isCollapsed())
			gvisualiser.expandTreeFrom(editableNode.getSelectedVisualNode());
		connectTo(duple);
		ConfigGraph.validateGraph();
		GraphState.setChanged();
	}

	private void deleteNode(VisualNode vNode) {
		// don't leave nodes hidden
		if (vNode.hasCollaspedChild())
			gvisualiser.expandTreeFrom(vNode);
		// remove from view while still intact
		gvisualiser.removeView(vNode);
		// this and its config from graphs and disconnect
		vNode.remove();
	}

	@Override
	public void onDeleteNode() {
		deleteNode(editableNode.getSelectedVisualNode());
		controller.onNodeDeleted();
		GraphState.setChanged();
		ConfigGraph.validateGraph();
	}

	@Override
	public void onRenameNode() {
		String userName = getNewName(editableNode.cClassId(), baseSpec);
		if (userName != null) {
			renameNode(userName, editableNode.getSelectedVisualNode());
			gvisualiser.onNodeRenamed(editableNode.getSelectedVisualNode());
			controller.onNodeRenamed();
			GraphState.setChanged();
			ConfigGraph.validateGraph();
		}
	}

	private void renameNode(String uniqueId, VisualNode vNode) {
		TreeGraphDataNode cNode = vNode.getConfigNode();
		// warn of linked project directory name change
		if (UserProjectLink.haveUserProject()) {
			if (cNode.classId().equals(N_SYSTEM.label())) {
				String remoteProject = UserProjectLink.projectRoot().getName();
				Dialogs.warnAlert("Linked project '" + remoteProject + "'",
						"Renaming directory '" + cNode.id() + "' to '" + uniqueId + "' in project '" + remoteProject
								+ "'",
						"Update relevant source code in\n'" + uniqueId + "' with code from '" + cNode.id()
								+ "'\nbefore attempting to rename this node again.\n");

			}
			if (cNode.classId().equals(N_RECORD.label()) || cNode.classId().equals(N_INITIALISER.label())) {
				String remoteProject = UserProjectLink.projectRoot().getName();
				Dialogs.warnAlert("Linked project '" + remoteProject + "'",
						"Renaming code file from  '" + cNode.id() + "' to '" + uniqueId + "' in project '"
								+ UserProjectLink.projectRoot().getName() + "'",
						"'" + cNode.id() + "' is now redundant and can be removed from project '" + remoteProject
								+ "'.");
			}
		}
		cNode.rename(cNode.id(), uniqueId);
		vNode.rename(vNode.id(), uniqueId);
	}

	@Override
	public void onCollapseTree(VisualNode childRoot) {
		gvisualiser.collapseTreeFrom(childRoot);
		controller.onTreeCollapse();
		GraphState.setChanged();
	}

	@Override
	public void onCollapseTrees() {
		for (VisualNode child : editableNode.getSelectedVisualNode().getChildren()) {
			if (!child.isCollapsed())
				gvisualiser.collapseTreeFrom(child);
		}
		controller.onTreeCollapse();
		GraphState.setChanged();
	}

	@Override
	public void onExpandTree(VisualNode childRoot) {
		gvisualiser.expandTreeFrom(childRoot);
		controller.onTreeExpand();
		GraphState.setChanged();
	}

	@Override
	public void onExpandTrees() {
		for (VisualNode child : editableNode.getSelectedVisualNode().getChildren()) {
			if (child.isCollapsed())
				gvisualiser.expandTreeFrom(child);
		}
		controller.onTreeExpand();
		GraphState.setChanged();
	}

	@Override
	public void onReconnectChild(VisualNode vnChild) {
		editableNode.reconnectChild(vnChild);
		gvisualiser.onNewParent(vnChild);
		ConfigGraph.validateGraph();
		GraphState.setChanged();
	}

	private void deleteTree(VisualNode root) {
		// avoid concurrent modification
		List<VisualNode> list = new LinkedList<>();
		for (VisualNode child : root.getChildren())
			list.add(child);

		for (VisualNode child : list)
			deleteTree(child);
		deleteNode(root);
	}

	@Override
	public void onDeleteTree(VisualNode root) {
		deleteTree(root);
		controller.onNodeDeleted();
		GraphState.setChanged();
		ConfigGraph.validateGraph();
	}

	private void deleteEdge(VisualEdge vEdge) {
		ALEdge cEdge = vEdge.getConfigEdge();
		// Remove visual elements before disconnecting
		gvisualiser.removeView(vEdge);
		// Remove ids before disconnecting;
		EditableFactory vf = (EditableFactory) vEdge.factory();
		EditableFactory cf = (EditableFactory) cEdge.factory();
		vf.expungeEdge(vEdge);
		cf.expungeEdge(cEdge);
		vEdge.disconnect();
		cEdge.disconnect();
	}

	@Override
	public void onDeleteEdge(VisualEdge edge) {
		deleteEdge(edge);
		GraphState.setChanged();
		ConfigGraph.validateGraph();
	}

	@Override
	public void onExportTree(VisualNode root) {
		Dialogs.infoAlert("Export Tree", "Not implemented yet", "");
	}

	@Override
	public void onImportTree(SimpleDataTreeNode childSpec) {
		if (treeImport(editableNode.getSelectedVisualNode(), childSpec)) {
			GraphState.setChanged();
			ConfigGraph.validateGraph();
		}
	}

	private boolean treeImport(VisualNode parent, SimpleDataTreeNode childSpec) {
		Dialogs.infoAlert("Import Tree", "Not implemented yet", "");
		return false;
	}

	@Override
	public void onDeleteParentLink(VisualNode vChild) {
		// messy: onParentChanged never expect edge deletion
		VisualNode vParent = editableNode.getSelectedVisualNode();
		TreeGraphNode cChild = vChild.getConfigNode();
		TreeGraphNode cParent = editableNode.getConfigNode();
		gvisualiser.onRemoveParentLink(vChild);
		vParent.disconnectFrom(vChild);
		cParent.disconnectFrom(cChild);
		gvisualiser.getVisualGraph().onParentChanged();
		ConfigGraph.onParentChanged();
		GraphState.setChanged();
		ConfigGraph.validateGraph();
	}

}
