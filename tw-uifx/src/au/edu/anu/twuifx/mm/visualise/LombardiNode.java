package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 13 Apr 2020
 */
public class LombardiNode {
	private VisualNode node;
	private List<LombardiNode> toNodes;

	public LombardiNode(VisualNode node) {
		this.node = node;
		toNodes = new ArrayList<>();
	}

	protected VisualNode getNode() {
		return node;
	}

	@Override
	public boolean equals(Object other) {
		LombardiNode lother = (LombardiNode) other;
		return node.id().equals(lother.getNode().id());
	}

	public void addToNode(LombardiNode cln) {
		toNodes.add(cln);
	}

	public static double fRepel(double k, double d) {
		return (k * k) / (d * d * d);
	}

	public static double fAttract(double k, double d) {
		return (d - k) / d;
	}

	public static double fTangential(double a, double d) {
		return a * d;
	}

	public static double fRotational(double b, double theta) {
		return b * theta;
	}

}
