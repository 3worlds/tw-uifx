package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
// opportunity for parameterised class here OTLayout<OTNode>
public class OTLayout implements ILayout{
	private OTNode root;
	private double[] m_depths = new double[10];
	private final double itemHeight = 8.0;
	private int m_maxDepth;

	public OTLayout (VisualNode vRoot) {
		root = new OTNode(null,vRoot);
		buildSpanningTree(root);
		m_maxDepth = 0;
		Arrays.fill(m_depths, 0);
	}

	
	private void buildSpanningTree(OTNode lNode) {
		List<VisualNode> sortList = new ArrayList<>();
		String parentId="";
		if (lNode.hasParent())
			parentId = lNode.getParent().getNode().id();
		for (VisualNode child : lNode.getNode().getChildren()) {
			String childId = child.id();
			if (!child.isCollapsed()&& !childId.equals(parentId))
				sortList.add(child);
		}
		VisualNode vParent = lNode.getNode().getParent();
		if (vParent!=null) 
			if (!vParent.isCollapsed()&& !vParent.id().equals(parentId))
				sortList.add(vParent);
		sortList.sort(new Comparator<VisualNode>() {
			@Override
			public int compare(VisualNode o1, VisualNode o2) {
				return o1.getDisplayText(false).compareTo(o2.getDisplayText(false));
			}
		});
		for (VisualNode child: sortList) {
			OTNode lChild = new OTNode(lNode,child);
			lNode.addChild(lChild);
			buildSpanningTree(lChild);
		}
	}


	@Override
	public ILayout compute() {
		// TODO Auto-generated method stub
		return null;
	}

}
