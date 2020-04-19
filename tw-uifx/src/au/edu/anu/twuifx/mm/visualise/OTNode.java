package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;

public class OTNode {
	private VisualNode vNode;
	private OTNode otnParent;
	private double prelim;
	private int number;
	private OTNode thread;
	private OTNode ancestor;
	private double mod;
	private double shift;
	private double change;
	private List<OTNode> children;

	public OTNode(OTNode otnParent, VisualNode vNode) {
		this.otnParent = otnParent;
		this.vNode = vNode;
		children = new ArrayList<>();
	}

	public double getX() {
		return vNode.getX();
	}

	public double getY() {
		return vNode.getY();
	}

	public boolean hasParent() {
		return otnParent != null;
	}

	public OTNode getParent() {
		return otnParent;
	}

	public VisualNode getNode() {
		return vNode;
	}

	public void addChild(OTNode child) {
		children.add(child);

	}
}
