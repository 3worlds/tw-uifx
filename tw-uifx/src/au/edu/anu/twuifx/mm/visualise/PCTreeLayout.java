package au.edu.anu.twuifx.mm.visualise;

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
public class PCTreeLayout implements ILayout {
	// TODO this won't work for animation! We need an current frame.
	private PCTNodeWrapper rootWrapper;

	/**
	 * Build a spanning tree based upon the given root. The visual node is wrapped
	 * in PCTNodeWrapper which the has necessary fields for the layout algorithm and
	 * can redefine the direction of parent/child relations.
	 */
	public PCTreeLayout(VisualNode root) {
		rootWrapper = new PCTNodeWrapper(null, root, 0);
		buildSpanningTree(rootWrapper);
		rootWrapper.setRadius(1.0);

	}

	private void buildSpanningTree(PCTNodeWrapper root) {
		List<VisualNode> sortList = new ArrayList<>();
		String parentFrameId = "";
		if (root.hasParent())
			parentFrameId = root.getParentFrame().getNode().id();
		for (VisualNode child : root.getNode().getChildren()) {
			String childNodeId = child.id();
			if (!child.isCollapsed() && !childNodeId.equals(parentFrameId))
				sortList.add(child);
		}
		VisualNode parentNode = root.getNode().getParent();
		if (parentNode != null)
			if (!parentNode.isCollapsed() && !parentNode.id().equals(parentFrameId))
				sortList.add(parentNode);
		sortList.sort(new Comparator<VisualNode>() {
			@Override
			public int compare(VisualNode o1, VisualNode o2) {
				return o1.getDisplayText(false).compareTo(o2.getDisplayText(false));
			}
		});
		for (int idx = 0; idx < sortList.size(); idx++) {
			PCTNodeWrapper childFrame = new PCTNodeWrapper(root, sortList.get(idx), idx);
			root.addChild(childFrame);
			buildSpanningTree(childFrame);
		}

	}

	@Override
	public ILayout compute() {
		int depth = 0;
        // recursive call to create the Cartesian coordinates from local system polar coords.
		toCartesian(rootWrapper, depth, 0);
 
		// scale into the unit space
		Point2D min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point2D max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		rootWrapper.getLayoutBounds(min, max);
		rootWrapper.normalise(min, max, new Point2D.Double(0.0, 0.0), new Point2D.Double(1.0, 1.0));
		return this;
	}

	private void toCartesian(PCTNodeWrapper pw, int depth, double angleSum) {
		if (!pw.hasParent()) {// root
			pw.setXY(0, 0);
			for (PCTNodeWrapper cw : pw.getChildren())
				toCartesian(cw, depth + 1, cw.getAngle());
		} else {
			double deg = Math.toDegrees(angleSum);
			double distance = pw.getParentFrame().getRadius();
			Duple<Double, Double> p = PCTNodeWrapper.polarToCartesian(angleSum, distance);
			double px = pw.getParentFrame().getX();
			double py = pw.getParentFrame().getY();
			double cx = p.getFirst();
			double cy = p.getSecond();
			pw.setXY(px + cx, py + cy);
			for (PCTNodeWrapper cf : pw.getChildren()) // children
				toCartesian(cf, depth + 1, angleSum + cf.getAngle());

		}


	}

}
