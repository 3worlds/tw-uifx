package au.edu.anu.twuifx.mm.visualise.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import au.edu.anu.omhtk.rng.Pcg32;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;

public class RT2Layout implements ILayout {

	private RT2Vertex root;
	

	public RT2Layout(VisualNode vRoot) {
		root = new RT2Vertex(null, vRoot);
		buildSpanningTree(root);
	}

	private void buildSpanningTree(RT2Vertex v) {
		List<VisualNode> sortList = new ArrayList<>();
		String parentId = "";
		if (v.hasParent())
			parentId = v.getParent().getNode().id();
		for (VisualNode vChild : v.getNode().getChildren()) {
			String childId = vChild.id();
			if (!vChild.isCollapsed() && !childId.equals(parentId))
				sortList.add(vChild);
		}
		VisualNode parentNode = v.getNode().getParent();
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
			RT2Vertex w = new RT2Vertex(v, sortList.get(idx));
			v.getChildren().add(w);
			buildSpanningTree(w);
		}
	}

	@Override
	public ILayout compute(double jitter) {
		List<RT2Vertex> leaves = new ArrayList<>();
		root.collectLeaves(leaves);
		double angle = 0;
		double inc = 360.0/leaves.size();
		for (RT2Vertex leaf:leaves) {
			leaf.setAngle(angle);
			angle+=inc;
		}
		root.setPosition();
		
		if (jitter > 0) {
			Random rnd = new Pcg32();
			root.jitter(jitter, rnd);
		}
		
		Point2D min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point2D max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		root.getLayoutBounds(min, max);
		root.normalise(ILayout.getBoundingFrame(min, max), ILayout.getFittingFrame());

		return this;
	}

}
