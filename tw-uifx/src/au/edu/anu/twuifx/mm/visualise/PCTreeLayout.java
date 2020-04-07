package au.edu.anu.twuifx.mm.visualise;

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
	private Frame rootFrame;

	public PCTreeLayout(VisualNode root) {
		/**
		 * Build a tree based upon the give root. NB this ignores the underlying
		 * treegraph of the config graph. That is, a config parent can be a child of a
		 * Frame root. To avoid confusion, I've call the root node a Frame. Frames have
		 * children.
		 */

		rootFrame = new Frame(null, root, 0);
		buildTree(rootFrame);

		String indent = " ";
		// dump(rootFrame, indent);

	}

	private static Duple<Double, Double> polarToCartesian(double radiant, double magnitude) {
		double x = magnitude * Math.cos(radiant);
		double y = magnitude * Math.sin(radiant);
		return new Duple<Double, Double>(x, y);
	}

	private void buildTree(Frame root) {
		List<VisualNode> sortList = new ArrayList<>();
		String parentFrameId = "";
		if (root.hasParent())
			parentFrameId = root.getParentFrame().getRootNode().id();
		for (VisualNode child : root.getRootNode().getChildren()) {
			String childNodeId = child.id();
			if (!child.isCollapsed() && !childNodeId.equals(parentFrameId))
				sortList.add(child);
		}
		VisualNode parentNode = root.getRootNode().getParent();
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
			Frame childFrame = new Frame(root, sortList.get(idx), idx);
			root.addChild(childFrame);
			buildTree(childFrame);
		}

	}

//	private void dump(Frame f, String indent) {
//		System.out.println("vector: [" + f.getAngle()+","+f.getRadius() +"]"+ indent + f.getRootNode().getDisplayText(false));
//		for (Frame cf : f.getChildren())
//			dump(cf, indent + "\t");
//
//	}

	@Override
	public ILayout compute() {
		int depth = 0;
		System.out.println("depth\tname\tAbsDeg\tAbsDist\tx\ty\tRelDeg");
		dumpAbsPosition(rootFrame, depth, 0, 0);
		return this;
	}

	private void dumpAbsPosition(Frame f, int depth, double angle, double distance) {
		Duple<Double, Double> p = polarToCartesian(angle, distance);
		System.out.println(depth + "\t" + f.getRootNode().getDisplayText(false) + "\t" + Math.toDegrees(angle) + "\t"
				+ distance + "\t" + p.getFirst() + "\t" + p.getSecond() + "\t" + Math.toDegrees(f.getAngle()));
		for (Frame c : f.getChildren()) {
			dumpAbsPosition(c, depth + 1, c.getAngle() + angle, f.getRadius() + distance);
		}

	}

}
