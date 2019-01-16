package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.edu.anu.rscs.aot.AotException;
import au.edu.anu.rscs.aot.collections.QuickListOfLists;
import au.edu.anu.rscs.aot.graph.AotEdge;
import au.edu.anu.rscs.aot.graph.AotNode;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.Edge;
import fr.cnrs.iees.graph.EdgeFactory;
import fr.cnrs.iees.graph.Graph;
import fr.cnrs.iees.graph.Node;
import fr.cnrs.iees.graph.NodeFactory;
import fr.cnrs.iees.properties.ReadOnlyPropertyList;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.tree.Tree;
import fr.cnrs.iees.tree.TreeNode;
import fr.cnrs.iees.tree.TreeNodeFactory;

public class VisualGraph
		implements Tree<VisualNode>, Graph<VisualNode, VisualEdge>, NodeFactory, EdgeFactory, TreeNodeFactory {

	private Set<VisualNode> nodes;
	private VisualNode root;

	public VisualGraph(Iterable<VisualNode> list) {
		super();
		nodes = new HashSet<VisualNode>();
		root = null;
		for (VisualNode n : list)
			nodes.add(n);
		if (list.iterator().hasNext())
			root = list.iterator().next();
	}

	protected VisualGraph() {
		this(new ArrayList<VisualNode>());
	}

	public VisualGraph(VisualNode root) {
		this(new ArrayList<VisualNode>());
		this.root = root;
		insertOnlyChildren(root, nodes);
	}

	@Override
	public Iterable<VisualNode> leaves() {
		List<VisualNode> result = new ArrayList<>(nodes.size());
		for (VisualNode n : nodes)
			if (n.isLeaf())
				result.add(n);
		return result;
	}

	private void insertOnlyChildren(TreeNode parent, Collection<VisualNode> list) {
		for (TreeNode child : parent.getChildren()) {
			list.add((VisualNode) child);
			insertOnlyChildren(child, list);
		}
	}

	@Override
	public Iterable<VisualNode> nodes() {
		return nodes;
	}

	@Override
	public int size() {
		return nodes.size();
	}

	@Override
	public boolean contains(VisualNode n) {
		return nodes.contains(n);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<VisualEdge> edges() {
		QuickListOfLists<VisualEdge> edges = new QuickListOfLists<>();
		for (VisualNode n : nodes)
			edges.addList((Iterable<VisualEdge>) n.getEdges(Direction.OUT));
		return edges;
	}

	@Override
	public Iterable<VisualNode> roots() {
		List<VisualNode> result = new ArrayList<>(nodes.size());
		for (VisualNode n : nodes)
			if (n.getParent() == null)
				result.add(n);
		return result;
	}

	@Override
	public Iterable<VisualNode> findNodesByReference(String reference) {
		List<VisualNode> found = new ArrayList<>(nodes.size()); // this may be a bad idea for big graphs
		for (VisualNode n : nodes)
			if (Tree.matchesReference(n, reference))
				found.add(n);
		return found;
	}

	@Override
	public int maxDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int minDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public VisualNode root() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tree<VisualNode> subTree(VisualNode parent) {
		return new VisualGraph(parent);
	}

	@Override
	public TreeNode makeTreeNode(TreeNode arg0, String arg1, String arg2, SimplePropertyList arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Edge makeEdge(Node arg0, Node arg1, String arg2, String arg3, ReadOnlyPropertyList arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node makeNode(String arg0, String arg1, ReadOnlyPropertyList arg2) {
		// TODO Auto-generated method stub
		return null;
	}

}
