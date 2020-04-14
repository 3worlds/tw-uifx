package au.edu.anu.twuifx.mm.visualise;

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
public class PCTNode {
	// private static final double w = 0.5;
	private double radius;// distance to all children

	private PCTNode pctParent;
	private VisualNode node;
	private int index;// ith child
	private List<PCTNode> children;
	private boolean isPctRoot;

	public PCTNode(PCTNode pctParent, VisualNode node, int index) {
		isPctRoot = (pctParent == null);
		this.pctParent = pctParent;
		this.node = node;
		this.index = index;
		children = new ArrayList<>();
	}

	protected void setXY(double x, double y) {
		node.setX(x);
		node.setY(y);
	}

	protected double getX() {
		return node.getX();
	}

	protected double getY() {
		return node.getY();
	}

	protected double getRadius() {
		return radius;
	}

	protected void setRadius(double value) {
		this.radius = value;
		// determine next radius
		double nextRadius = 0;
		int n = children.size();
		if (children.isEmpty())// leaf
			return;
		else if (children.size() == 1)// children have no siblings.
			nextRadius = radius / 2.0;
		else {// find mid point between nearest children
			double diff = Double.POSITIVE_INFINITY;
			int idx = 0;
			for (int i = 1; i < children.size(); i++) {
				double aDiff = Math.abs(children.get(i - 1).getAngle() - children.get(i).getAngle());
				if (aDiff < diff) {
					diff = aDiff;
					idx = i;
				}
			}

			double theta1 = children.get(idx).getAngle();
			double theta2 = theta1 + diff / 2.0;
			Duple<Double, Double> p1 = polarToCartesian(theta1, radius);
			Duple<Double, Double> p2 = polarToCartesian(theta2, radius);
			double distance = Distance.euclidianDistance(p1.getFirst(), p1.getSecond(), p2.getFirst(), p2.getSecond());
			nextRadius = distance;
		}

		// update recursively;
		for (PCTNode cf : children)
			cf.setRadius(nextRadius);
	}

	private static final double w = Math.PI; // = 45 deg for two children

	protected double getAngle() {
		if (isPctRoot)
			return 0.0;
		double m = pctParent.getChildren().size();
		double i = index;
		if (pctParent.isPctRoot)
			return (2.0 * Math.PI * i) / m;
		else {
			// π − φ /2 + φ i/m + φ /(2m) NB: error in paper - π should be 2π ?

			return /* 2 * Math.PI + */ ((w * i) / m) + w / (2.0 * m) - (w / 2.0);
		}
	}

	protected PCTNode getPctParent() {
		return pctParent;
	}

	protected VisualNode getNode() {
		return node;
	}

	protected int getIndex() {
		return index;
	}

	protected List<PCTNode> getChildren() {
		return children;
	}

	protected void addChild(PCTNode child) {
		children.add(child);
	}

	@Override
	public String toString() {
		return node.getDisplayText(false);
	}

	protected boolean hasParent() {
		return pctParent != null;
	}

	public static Duple<Double, Double> polarToCartesian(double radiant, double magnitude) {
		double x = magnitude * Math.cos(radiant);
		double y = magnitude * Math.sin(radiant);
		return new Duple<Double, Double>(x, y);
	}

	public void getLayoutBounds(Point2D min, Point2D max) {
		min.setLocation(Math.min(min.getX(), getX()), Math.min(min.getY(), getY()));
		max.setLocation(Math.max(max.getX(), getX()), Math.max(max.getY(), getY()));
		for (PCTNode child : getChildren())
			child.getLayoutBounds(min, max);

	}

	public void normalise(Point2D fromMin, Point2D fromMax, Point2D toMin, Point2D toMax) {
		double x = ILayout.rescale(getX(), fromMin.getX(), fromMax.getX(), toMin.getX(), toMax.getX());
		double y = ILayout.rescale(getY(), fromMin.getY(), fromMax.getY(), toMin.getY(), toMax.getY());
		setXY(x, y);
		for (PCTNode child : getChildren())
			child.normalise(fromMin, fromMax, toMin, toMax);

	}

}
