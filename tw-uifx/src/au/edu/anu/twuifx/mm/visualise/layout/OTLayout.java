package au.edu.anu.twuifx.mm.visualise.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import au.edu.anu.omhtk.rng.Pcg32;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
/**
 * <p>
 * OTLayout computes a tidy layout of a node-link tree diagram. This
 * algorithm lays out a rooted tree such that each depth level of the tree is on
 * a shared vertical line. 
 * </p>
 * 
 * <p>
 * "Improving Walker's Algorithm to Run in Linear Time"
 * 
 * http://dirk.jivas.de/papers/buchheim02improving.pdf
 */

/**
 * @author Ian Davies
 *
 * @date 24 Apr 2020
 */
public class OTLayout implements ILayout{
	private OTVertex root;

	public OTLayout (VisualNode vRoot) {
		root = new OTVertex(null,vRoot);
		buildSpanningTree(root);
		}

	
	private void buildSpanningTree(OTVertex lNode) {
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
			OTVertex lChild = new OTVertex(lNode,child);
			lNode.getChildren().add(lChild);
			buildSpanningTree(lChild);
		}
	}


	@Override
	public ILayout compute(double jitter) {
		OTVertex.maxLevels=0;
		Arrays.fill(OTVertex.levels, 0);
		
		root.firstWalk(0,1);
		
		determineDepths();
		
		root.secondWalk(null,-root.getPrelim(),0);
		
		if (jitter>0) {
			Random rnd = new Pcg32();
			root.jitter(jitter,rnd);
		}
		
		Point2D min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point2D max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		root.getLayoutBounds(min,max);
		root.normalise(ILayout.getBoundingFrame(min, max),ILayout.getFittingFrame());
		
		return this;
	}
	
	private static void determineDepths() {
		for (int i = 1; i < OTVertex.maxLevels; ++i)
			OTVertex.levels[i] +=OTVertex.levels[i - 1];
	}

}
