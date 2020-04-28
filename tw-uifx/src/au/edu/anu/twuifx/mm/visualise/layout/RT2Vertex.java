package au.edu.anu.twuifx.mm.visualise.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 26 Apr 2020
 */
public class RT2Vertex {

	private RT2Vertex _parent;
	private VisualNode _vNode;
	private List<RT2Vertex> _children;
	private int _depth;
	private double _angle;

	public RT2Vertex(RT2Vertex parent, VisualNode vNode) {
		this._parent = parent;
		this._vNode = vNode;
		this._depth = 0;
		if (hasParent())
			_depth = getParent().getDepth() + 1;
		_children = new ArrayList<>();
	}

	public boolean hasParent() {
		return _parent != null;
	}

	public RT2Vertex getParent() {
		return _parent;
	}

	public VisualNode getvNode() {
		return _vNode;
	}

	public List<RT2Vertex> getChildren() {
		return _children;
	}

	public int getDepth() {
		return _depth;
	}

	public boolean isLeaf() {
		return _children.isEmpty();
	}

	@Override
	public String toString() {
		return _vNode.getDisplayText(false);
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
		for (RT2Vertex c : getChildren())
			c.collectLeaves(leaves);
	}

	private void updatePosition(double angle) {
		double x = _depth * Math.cos(Math.toRadians(angle));
		double y = _depth * Math.sin(Math.toRadians(angle));
		_vNode.setX(x);
		_vNode.setY(y);
	}

	public void setPosition() {
		updatePosition(getAngle());
		for (RT2Vertex c : _children)
			c.setPosition();
	}

	private RT2Vertex getLefthand() {
		if (isLeaf())
			return this;
		return _children.get(0).getLefthand();
	}

	private RT2Vertex getRightHand() {
		if (isLeaf())
			return this;
		return _children.get(_children.size() - 1).getRightHand();
	}
	public void getLayoutBounds(Point2D min, Point2D max) {
		min.setLocation(Math.min(min.getX(), _vNode.getX()), Math.min(min.getY(),  _vNode.getY()));
		max.setLocation(Math.max(max.getX(), _vNode.getX()), Math.max(max.getY(),  _vNode.getY()));
		for (RT2Vertex child : getChildren())
			child.getLayoutBounds(min, max);

	}

	public void normalise(Point2D fromMin, Point2D fromMax, Point2D toMin, Point2D toMax) {
		double x = ILayout.rescale( _vNode.getX(), fromMin.getX(), fromMax.getX(), toMin.getX(), toMax.getX());
		double y = ILayout.rescale( _vNode.getY(), fromMin.getY(), fromMax.getY(), toMin.getY(), toMax.getY());
		 _vNode.setX(x);
		 _vNode.setY(y);
		for (RT2Vertex child : getChildren())
			child.normalise(fromMin, fromMax, toMin, toMax);

	}

}
