package au.edu.anu.twuifx.mm.visualise.layout;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.uit.space.Distance;
import fr.ens.biologie.generic.utils.Duple;

/**
 * @author Ian Davies
 *
 * @date 30 Mar 2020
 */
public class RT1Vertex extends TreeVertexAdapter {
	private double radius;// distance to all children
	private int index;// ith child

	public RT1Vertex(RT1Vertex parent, VisualNode node, int index) {
		super(parent, node);
		this.index = index;
	}


	protected double getRadius() {
		return radius;
	}

	protected void setRadius(double value) {
		this.radius = value;
		System.out.println(toString()+"\t"+(radius));
		// determine next radius
		double nextRadius = 0;
		int n = getChildren().size();
		if (n == 0)// leaf
			return;
		else if (n == 1)// children have no siblings.
			nextRadius = radius / 2.0;
		else {// find mid point between nearest children
			double diff = Double.POSITIVE_INFINITY;
			int idx = 0;
			for (int i = 1; i < n; i++) {
				double aDiff = Math.abs(((RT1Vertex) getChildren().get(i - 1)).getAngle()
						- ((RT1Vertex) getChildren().get(i)).getAngle());
				if (aDiff < diff) {
					diff = aDiff;
					idx = i;
				}
			}

			double theta1 = ((RT1Vertex) getChildren().get(idx)).getAngle();
			double theta2 = theta1 + diff / 2.0;
			Duple<Double, Double> p1 = polarToCartesian(theta1, radius);
			Duple<Double, Double> p2 = polarToCartesian(theta2, radius);
			double distance = Distance.euclidianDistance(p1.getFirst(), p1.getSecond(), p2.getFirst(), p2.getSecond());
			nextRadius = distance;
		}

		// update recursively;
		for (TreeVertexAdapter cf : getChildren())
			((RT1Vertex) cf).setRadius(nextRadius);
	}

	private static final double w = Math.PI; // = 45 deg for two children

	protected double getAngle() {
		if (!hasParent())
			return 0.0;
		double m = getParent().getChildren().size();
		double i = index;
		if (!getParent().hasParent())
			return (2.0 * Math.PI * i) / m;
		else {
			// π − φ /2 + φ i/m + φ /(2m) NB: error in paper : remove π
			return ((w * i) / m) + w / (2.0 * m) - (w / 2.0);
		}
	}


	protected int getIndex() {
		return index;
	}


	public static Duple<Double, Double> polarToCartesian(double radiant, double magnitude) {
		double x = magnitude * Math.cos(radiant);
		double y = magnitude * Math.sin(radiant);
		return new Duple<Double, Double>(x, y);
	}


}
