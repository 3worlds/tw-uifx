package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 30 Mar 2020
 */
public class Frame {
	private static final double w = 0.5;
	private double radius;// distance to all children

	private Frame parentFrame;
	private VisualNode rootNode;
	private int index;// ith child
	private List<Frame> children;
	private boolean rootFrame;

	public Frame(Frame parent, VisualNode rootNode, int index) {
		this.radius = 1.0;
		rootFrame = (parent == null);
		if (!rootFrame)
			this.radius = parent.getRadius() / 2.0;
		this.parentFrame = parent;
		this.rootNode = rootNode;
		this.index = index;
		children = new ArrayList<>();
	}

	protected double getRadius() {
		return radius;
	}

	protected double getAngle() {
		if (rootFrame)
			return 0.0;
		double m = parentFrame.getChildren().size();
		double i = index;
		if (parentFrame.rootFrame)
			return (2.0 * Math.PI * i) / m;
		else {
			      //π − φ /2  + φ i/m + φ /(2m) NB: error in paper.
			return (2.0*Math.PI)-(w/2.0)+(w*i/m)+w/(2.0*m);
		}
	}

	protected Frame getParentFrame() {
		return parentFrame;
	}

	protected VisualNode getRootNode() {
		return rootNode;
	}

	protected int getIndex() {
		return index;
	}

	protected List<Frame> getChildren() {
		return children;
	}

	protected void addChild(Frame child) {
		children.add(child);
	}

	@Override
	public String toString() {
		return rootNode.getDisplayText(false);
	}

	protected boolean hasParent() {
		return parentFrame != null;
	}

}
