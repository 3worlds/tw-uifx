package au.edu.anu.twuifx.mm.visualise.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.ens.biologie.generic.utils.Duple;

/**
 * @author Ian Davies
 *
 * @date 30 Mar 2020
 */
public class RT1Layout implements ILayout {
	
	private RT1Node root;

	/**
	 * Build a spanning tree based upon the given root. The visual node is wrapped
	 * in PCTNodeWrapper which the has necessary fields for the layout algorithm and
	 * can redefine the direction of parent/child relations.
	 */
	public RT1Layout(VisualNode vRoot) {
		root = new RT1Node(null, vRoot, 0);
		buildSpanningTree(root);
		root.setRadius(1.0);

	}

	private void buildSpanningTree(RT1Node lNode) {
		List<VisualNode> sortList = new ArrayList<>();
		String parentId = "";
		if (lNode.hasParent())
			parentId = lNode.getPctParent().getNode().id();
		for (VisualNode vChild : lNode.getNode().getChildren()) {
			String childId = vChild.id();
			if (!vChild.isCollapsed() && !childId.equals(parentId))
				sortList.add(vChild);
		}
		VisualNode parentNode = lNode.getNode().getParent();
		if (parentNode != null)
			if (!parentNode.isCollapsed() && !parentNode.id().equals(parentId))
				sortList.add(parentNode);
		
		sortList.sort(new Comparator<VisualNode>() {
			@Override
			public int compare(VisualNode o1, VisualNode o2) {
				return o1.getDisplayText(false).compareTo(o2.getDisplayText(false));
			}
		});
		for (int idx = 0; idx < sortList.size(); idx++) {
			RT1Node lChild = new RT1Node(lNode, sortList.get(idx), idx);
			lNode.addChild(lChild);
			buildSpanningTree(lChild);
		}
	}

	@Override
	public ILayout compute() {
		int depth = 0;
		double angle =0;
        // recursive call to create the Cartesian coordinates from local system polar coords.
		toCartesian(root, depth, angle);
 
		// scale into the unit space
		Point2D min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point2D max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		root.getLayoutBounds(min, max);
		root.normalise(min, max, new Point2D.Double(0.0, 0.0), new Point2D.Double(1.0, 1.0));
		return this;
	}

	private void toCartesian(RT1Node lNode, int depth, double angleSum) {
		if (!lNode.hasParent()) {// root
			lNode.setXY(0, 0);
			for (RT1Node cw : lNode.getChildren())
				toCartesian(cw, depth + 1, cw.getAngle());
		} else {
			double deg = Math.toDegrees(angleSum);
			double distance = lNode.getPctParent().getRadius();
			Duple<Double, Double> p = RT1Node.polarToCartesian(angleSum, distance);
			double px = lNode.getPctParent().getX();
			double py = lNode.getPctParent().getY();
			double cx = p.getFirst();
			double cy = p.getSecond();
			lNode.setXY(px + cx, py + cy);
			for (RT1Node cf : lNode.getChildren()) // children
				toCartesian(cf, depth + 1, angleSum + cf.getAngle());

		}


	}

}
