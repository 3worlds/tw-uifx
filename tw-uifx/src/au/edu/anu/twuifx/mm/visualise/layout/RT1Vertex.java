package au.edu.anu.twuifx.mm.visualise.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.uit.space.Distance;
import fr.ens.biologie.generic.utils.Duple;
import au.edu.anu.twapps.mm.layout.ILayout;

/**
 * @author Ian Davies
 *
 * @date 30 Mar 2020
 */
public class RT1Vertex {
	private double radius;// distance to all children

	private RT1Vertex _parent;
	private VisualNode _vNode;
	private int index;// ith child
	private List<RT1Vertex> _children;
	//private boolean isPctRoot;

	public RT1Vertex(RT1Vertex parent, VisualNode node, int index) {
		//isPctRoot = (parent == null);
		this._parent = parent;
		this._vNode = node;
		this.index = index;
		_children = new ArrayList<>();
	}

	protected void setXY(double x, double y) {
		_vNode.setX(x);
		_vNode.setY(y);
	}

	protected double getX() {
		return _vNode.getX();
	}

	protected double getY() {
		return _vNode.getY();
	}

	protected double getRadius() {
		return radius;
	}

	protected void setRadius(double value) {
		this.radius = value;
		// determine next radius
		double nextRadius = 0;
		int n = _children.size();
		if (n==0)// leaf
			return;
		else if (n == 1)// children have no siblings.
			nextRadius = radius / 2.0;
		else {// find mid point between nearest children
			double diff = Double.POSITIVE_INFINITY;
			int idx = 0;
			for (int i = 1; i < n; i++) {
				double aDiff = Math.abs(_children.get(i - 1).getAngle() - _children.get(i).getAngle());
				if (aDiff < diff) {
					diff = aDiff;
					idx = i;
				}
			}

			double theta1 = _children.get(idx).getAngle();
			double theta2 = theta1 + diff / 2.0;
			Duple<Double, Double> p1 = polarToCartesian(theta1, radius);
			Duple<Double, Double> p2 = polarToCartesian(theta2, radius);
			double distance = Distance.euclidianDistance(p1.getFirst(), p1.getSecond(), p2.getFirst(), p2.getSecond());
			nextRadius = distance;
		}

		// update recursively;
		for (RT1Vertex cf : _children)
			cf.setRadius(nextRadius);
	}

	private static final double w = Math.PI; // = 45 deg for two children

	protected double getAngle() {
		if (!hasParent())
			return 0.0;
		double m = _parent.getChildren().size();
		double i = index;
		if (!getParent().hasParent())
			return (2.0 * Math.PI * i) / m;
		else {
			// π − φ /2 + φ i/m + φ /(2m) NB: error in paper : remove π
			return ((w * i) / m) + w / (2.0 * m) - (w / 2.0);
		}
	}

	protected RT1Vertex getParent() {
		return _parent;
	}

	protected VisualNode getvNode() {
		return _vNode;
	}

	protected int getIndex() {
		return index;
	}

	protected List<RT1Vertex> getChildren() {
		return _children;
	}


	@Override
	public String toString() {
		return _vNode.getDisplayText(false);
	}

	protected boolean hasParent() {
		return _parent != null;
	}

	public static Duple<Double, Double> polarToCartesian(double radiant, double magnitude) {
		double x = magnitude * Math.cos(radiant);
		double y = magnitude * Math.sin(radiant);
		return new Duple<Double, Double>(x, y);
	}

	public void getLayoutBounds(Point2D min, Point2D max) {
		min.setLocation(Math.min(min.getX(), getX()), Math.min(min.getY(), getY()));
		max.setLocation(Math.max(max.getX(), getX()), Math.max(max.getY(), getY()));
		for (RT1Vertex child : getChildren())
			child.getLayoutBounds(min, max);

	}

	public void normalise(Point2D fromMin, Point2D fromMax, Point2D toMin, Point2D toMax) {
		double x = ILayout.rescale(getX(), fromMin.getX(), fromMax.getX(), toMin.getX(), toMax.getX());
		double y = ILayout.rescale(getY(), fromMin.getY(), fromMax.getY(), toMin.getY(), toMax.getY());
		setXY(x, y);
		for (RT1Vertex child : getChildren())
			child.normalise(fromMin, fromMax, toMin, toMax);

	}

}
