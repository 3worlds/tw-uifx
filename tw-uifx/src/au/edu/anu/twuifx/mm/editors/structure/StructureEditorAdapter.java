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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.text.WordUtils;

import au.edu.anu.rscs.aot.archetype.ArchetypeArchetypeConstants;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualGraphFactory;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TWA;
import au.edu.anu.twcore.archetype.TwArchetypeConstants;
import au.edu.anu.twcore.archetype.tw.ChildXorPropertyQuery;
import au.edu.anu.twcore.archetype.tw.PropertyXorQuery;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.root.ExpungeableFactory;
import au.edu.anu.twcore.root.TwConfigFactory;
import au.edu.anu.twuifx.mm.visualise.IGraphVisualiser;
import fr.cnrs.iees.graph.Element;
import fr.cnrs.iees.graph.Node;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.identity.impl.PairIdentity;
import fr.cnrs.iees.io.parsing.ValidPropertyTypes;
import fr.ens.biologie.generic.utils.Duple;

public abstract class StructureEditorAdapter
		implements StructureEditable, TwArchetypeConstants, ArchetypeArchetypeConstants {
	// TODO logging

	/* what we need to know from the archetype graph */
	protected Specifications specifications;
	/*
	 * what we need to know from the visualNode that has been selected for editing
	 */
	protected SpecifiableNode editingNode;
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

	public StructureEditorAdapter(SpecifiableNode selectedNode, IGraphVisualiser gv, IMMController controller) {
		super();
		this.specifications = new TwSpecifications();
		this.controller = controller;
		this.newChild = null;
		this.editingNode = selectedNode;
		Set<String> discoveredFile = new HashSet<>();
		this.baseSpec = specifications.getSpecsOf(editingNode.getConfigNode(), editingNode.createdBy(), TWA.getRoot(),
				discoveredFile);

		this.subClassSpec = specifications.getSubSpecsOf(baseSpec, editingNode.getSubClass());
		this.gvisualiser = gv;
	}

	@Override
	public List<SimpleDataTreeNode> filterChildSpecs(Iterable<SimpleDataTreeNode> childSpecs) {
		List<SimpleDataTreeNode> result = new ArrayList<SimpleDataTreeNode>();
		List<String[]> tables = specifications.getQueryStringTables(baseSpec, ChildXorPropertyQuery.class);
		tables.addAll(specifications.getQueryStringTables(subClassSpec, ChildXorPropertyQuery.class));
		for (SimpleDataTreeNode childSpec : childSpecs) {
			String childLabel = (String) childSpec.properties().getPropertyValue(aaIsOfClass);
			IntegerRange range = specifications.getMultiplicity(childSpec);
			if (editingNode.moreChildrenAllowed(range, childLabel)) {
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
		VisualNode vn = editingNode.getSelectedVisualNode();
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
			if (vn.getConfigNode().classId().equals(label))
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
			List<VisualNode> en = findNodesLabelled(toNodeRef.replace(PairIdentity.LABEL_NAME_STR_SEPARATOR, ""));
			if (!en.isEmpty()) {
				Duple<String, VisualNode> p = new Duple<String, VisualNode>(edgeLabel, en.get(0));
				result.add(p);
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

	public List<VisualNode> orphanedChildList(Iterable<SimpleDataTreeNode> childSpecs) {
		List<VisualNode> result = new ArrayList<>();
		for (VisualNode root : editingNode.graphRoots()) {
			String rootLabel = TWA.getLabel(root.id());
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

	private static VisualEdge createVisualEdge(String edgeClassName, VisualNode vStart, VisualNode vEnd) {
		TreeGraphDataNode cStart = (TreeGraphDataNode) vStart.getConfigNode();
		TreeGraphDataNode cEnd = (TreeGraphDataNode) vEnd.getConfigNode();
		TwConfigFactory cf = (TwConfigFactory) cStart.factory();
		String proposedId = edgeClassName + PairIdentity.LABEL_NAME_STR_SEPARATOR + edgeClassName + "1";
		ALEdge ce = (ALEdge) cf.makeEdge(cf.edgeClass(edgeClassName), cStart, cEnd, proposedId);
		VisualGraphFactory vf = (VisualGraphFactory) vStart.factory();
		VisualEdge result = vf.makeEdge(vStart, vEnd, proposedId);
		result.setConfigEdge(ce);
		return result;
	}

	private void connectTo(Duple<String, VisualNode> p) {
		VisualEdge ve = createVisualEdge(p.getFirst(), editingNode.getSelectedVisualNode(), p.getSecond());
		gvisualiser.onNewEdge(ve);
	}

	private String promptForNewNode(String label, String promptName) {
		return Dialogs.getText("New '" + label + "' node", "", "Name:", promptName);
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

	@SuppressWarnings("unchecked")
	@Override
	public void onNewChild(String childLabel, SimpleDataTreeNode childBaseSpec) {
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
				return; // implicit cancel
			// userName = promptId;
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
		List<Class<? extends TreeNode>> subClasses = specifications.getSubClasses(childBaseSpec);
		if (subClasses.size() > 1) {
			subClass = promptForClass(subClasses, childClassName);
			if (subClass == null)
				return;// cancel
		} else if (subClasses.size() == 1) {
			subClass = subClasses.get(0);
		}
		SimpleDataTreeNode childSubSpec = specifications.getSubSpecsOf(childBaseSpec, subClass);
		// unfiltered propertySpecs
		Iterable<SimpleDataTreeNode> propertySpecs = specifications.getPropertySpecifications(childBaseSpec,
				childSubSpec);
		if (!specifications.filterPropertySpecs(propertySpecs, childBaseSpec, childSubSpec,
				childClassName + PairIdentity.LABEL_NAME_SEPARATOR + promptId, ChildXorPropertyQuery.class,
				PropertyXorQuery.class))
			return;// cancel

		// make the node
		newChild = editingNode.newChild(childLabel, promptId);

		for (SimpleDataTreeNode propertySpec : propertySpecs) {
			String key = (String) propertySpec.properties().getPropertyValue(aaHasName);
			if (key.equals(twaSubclass))
				newChild.addProperty(twaSubclass, subClass.getName());
			else {
				String type = (String) propertySpec.properties().getPropertyValue(aaType);
				Object defValue = ValidPropertyTypes.getDefaultValue(type);
				newChild.addProperty(key, defValue);
			}
		}

		controller.onNewNode(newChild);

		GraphState.setChanged();
		ConfigGraph.validateGraph();
	}

	@Override
	public void onNewEdge(Duple<String, VisualNode> duple) {
		if (editingNode.isCollapsed())
			gvisualiser.expandTreeFrom(editingNode.getSelectedVisualNode());
		connectTo(duple);
		ConfigGraph.validateGraph();
		GraphState.setChanged();
	}

	private void deleteNode(VisualNode vNode) {
		if (vNode.isCollapsedParent())
			gvisualiser.expandTreeFrom(vNode);
		TreeGraphNode cNode = vNode.getConfigNode();
		// Remove visual elements before disconnecting
		gvisualiser.removeView(vNode);
		ExpungeableFactory vf = (ExpungeableFactory) vNode.factory();
		ExpungeableFactory cf = (ExpungeableFactory) cNode.factory();
		vf.expungeNode(vNode);
		cf.expungeNode(cNode);
		vNode.disconnect();
		cNode.disconnect();
	}

	@Override
	public void onDeleteNode() {
		deleteNode(editingNode.getSelectedVisualNode());
		controller.onNodeDeleted();
		GraphState.setChanged();
		ConfigGraph.validateGraph();
	}

	@Override
	public void onCollapseTree() {
		gvisualiser.collapseTreeFrom(editingNode.getSelectedVisualNode());
		controller.onTreeCollapse();
		GraphState.setChanged();
	}

	@Override
	public void onExpandTree() {
		gvisualiser.expandTreeFrom(editingNode.getSelectedVisualNode());
		controller.onTreeExpand();
		GraphState.setChanged();
	}

	@Override
	public void onReconnectChild(VisualNode vnChild) {
		VisualNode vnParent = editingNode.getSelectedVisualNode();
		TreeGraphNode cnChild = vnChild.getConfigNode();
		TreeGraphNode cnParent = editingNode.getConfigNode();
		cnParent.connectChild(cnChild);
		vnParent.connectChild(vnChild);
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
		GraphState.setChanged();
		ConfigGraph.validateGraph();
	}

	private void deleteEdge(VisualEdge vEdge) {
		ALEdge cEdge = vEdge.getConfigEdge();
		// Remove visual elements before disconnecting
		gvisualiser.removeView(vEdge);
		ExpungeableFactory vf = (ExpungeableFactory) vEdge.factory();
		ExpungeableFactory cf = (ExpungeableFactory) cEdge.factory();
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
	public void onExportTree(VisualNode vn) {
	}

	@Override
	public void onImportTree(SimpleDataTreeNode childSpec) {
		GraphState.setChanged();
		ConfigGraph.validateGraph();
	}

	@Override
	public void onDeleteParentLink(VisualNode vChild) {
		VisualNode vParent = editingNode.getSelectedVisualNode();
		TreeGraphNode cChild = vChild.getConfigNode();
		TreeGraphNode cParent = editingNode.getConfigNode();
		gvisualiser.onRemoveParent(vChild);
		vParent.disconnectFrom(vChild);
		cParent.disconnectFrom(cChild);
		GraphState.setChanged();
		ConfigGraph.validateGraph();
	}

}
