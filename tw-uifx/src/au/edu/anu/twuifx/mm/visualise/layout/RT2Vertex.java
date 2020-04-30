package au.edu.anu.twuifx.mm.visualise.layout;

import java.util.List;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 26 Apr 2020
 */
public class RT2Vertex extends TreeVertexAdapter {

	private int _depth;
	private double _angle;

	public RT2Vertex(RT2Vertex parent, VisualNode vNode) {
		super(parent, vNode);
		this._depth = 0;
		if (hasParent())
			_depth = ((RT2Vertex) getParent()).getDepth() + 1;
	}

	public int getDepth() {
		return _depth;
	}

	public void setAngle(double angle) {
		_angle = angle;
	}

	public double getAngle() {
		if (!hasParent())
			return 0.0;
		if (isLeaf())
			return _angle;
		else {
			RT2Vertex left = getLefthand();
			RT2Vertex right = getRightHand();
			double la = left.getAngle();
			double ra = right.getAngle();
			return la + (ra - la) / 2.0;
		}
	}

	public void collectLeaves(List<RT2Vertex> leaves) {
		if (isLeaf())
			leaves.add(this);
		for (TreeVertexAdapter c : getChildren())
			((RT2Vertex) c).collectLeaves(leaves);
	}

	private void updatePosition(double angle) {
		double x = _depth * Math.cos(Math.toRadians(angle));
		double y = _depth * Math.sin(Math.toRadians(angle));
		setLocation(x, y);
	}

	public void setPosition() {
		updatePosition(getAngle());
		for (TreeVertexAdapter c : getChildren())
			((RT2Vertex) c).setPosition();
	}

	private RT2Vertex getLefthand() {
		if (isLeaf())
			return this;
		return ((RT2Vertex) getChildren().get(0)).getLefthand();
	}

	private RT2Vertex getRightHand() {
		if (isLeaf())
			return this;
		return ((RT2Vertex) getChildren().get(getChildren().size() - 1)).getRightHand();
	}

}
