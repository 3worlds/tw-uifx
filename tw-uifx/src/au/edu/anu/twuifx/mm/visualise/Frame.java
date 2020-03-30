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
	private double radius;// distance to all children
	
	private Frame parentFrame;
	private VisualNode rootNode;
	private int index;// ith child
	private List<Frame> children;

	public Frame(Frame parent, VisualNode rootNode, int index) {
		this.radius = 1.0;
		if (parent != null)
			this.radius = parent.getRadius() / 2.0;
		this.parentFrame = parent;
		this.rootNode = rootNode;
		this.index = index;
		children = new ArrayList<>();
	}

	protected double getRadius() {
		return radius;
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

}
