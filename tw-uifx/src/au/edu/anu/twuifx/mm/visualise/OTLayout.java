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

	public OTLayout (VisualNode vRoot) {
		root = new OTNode(null,vRoot);
		buildSpanningTree(root);
		}

	
	private void buildSpanningTree(OTNode lNode) {
		List<VisualNode> sortList = new ArrayList<>();
		String parentId="";
		if (lNode.hasParent())
			parentId = lNode.getParent().getvNode().id();
		for (VisualNode child : lNode.getvNode().getChildren()) {
			String childId = child.id();
			if (!child.isCollapsed()&& !childId.equals(parentId))
				sortList.add(child);
		}
		VisualNode vParent = lNode.getvNode().getParent();
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
			lNode.getChildren().add(lChild);
			buildSpanningTree(lChild);
		}
	}


	@Override
	public ILayout compute() {
		OTNode.m_maxDepth=0;
		Arrays.fill(OTNode.m_depths, 0);
		
		root.firstWalk(0,1);
		
		determineDepths();
		
		root.secondWalk(null,-root.getPrelim(),0);
		
		return this;
	}
	
	private static void determineDepths() {
		for (int i = 1; i < OTNode.m_maxDepth; ++i)
			OTNode.m_depths[i] +=OTNode. m_depths[i - 1];
	}

}
