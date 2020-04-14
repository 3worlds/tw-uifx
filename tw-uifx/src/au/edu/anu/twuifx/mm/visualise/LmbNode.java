package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.uit.space.Distance;
import fr.ens.biologie.generic.utils.Duple;

/**
 * @author Ian Davies
 *
 * @date 13 Apr 2020
 */
public class LmbNode {
	private VisualNode node;
	private List<LmbNode> toNodes;// may not need these??
	private double easting; // displacement
	private double northing;
	private String id;

	public LmbNode(VisualNode node) {
		this.node = node;
		toNodes = new ArrayList<>();
		this.id = node.id();
	}

	protected VisualNode getNode() {
		return node;
	}

	@Override
	public boolean equals(Object other) {
		LmbNode lother = (LmbNode) other;
		return id.equals(lother.id);
	}

	public void addToNode(LmbNode cln) {
		toNodes.add(cln);
	}

	public void clearDisp() {
		easting = 0;
		northing = 0;
	}

	public double getX() {
		return node.getX();
	}

	public double getY() {
		return node.getY();
	}

	public void setPosition(double x, double y) {
		node.setPosition(x, y);
	}

	public double distanceFrom(LmbNode other) {
		return Distance.euclidianDistance(getX(), getY(), other.getX(), other.getY());
	}

	private double dx(LmbNode other) {
		return other.getX() - getX();
	}

	private double dy(LmbNode other) {
		return other.getY() - getY();
	}

	public void repulsionDisplacement(LmbNode other, double k) {
		double mag = fRepulsion(other, k);
		double dx = dx(other);
		double dy = dy(other);
		double a = Math.atan2(dy, dx);
		double x = mag * Math.cos(a);
		double y = mag * Math.sin(a);
		easting -= x;
		northing -= y;
	}

	public void attractionDisplacement(LmbNode other, double k) {
		double mag = fAttraction(other, k);
		double dx = dx(other);
		double dy = dy(other);
		double a = Math.atan2(dy, dx);
		double x = mag * Math.cos(a);
		double y = mag * Math.sin(a);
		easting += x;
		northing += y;
	}

	public double fRepulsion(LmbNode other, double k) {
		double d = Distance.euclidianDistance(getX(), getY(), other.getX(), other.getY());
		return fRepulsion(k, d);
	}

	public double fAttraction(LmbNode other, double k) {
		double d = Distance.euclidianDistance(getX(), getY(), other.getX(), other.getY());
		return fAttract(k, d);
	}

	/**
	 * @param k constant
	 * @param d distance between any two nodes
	 * @return repulsion force
	 */
	public static double fRepulsion(double k, double d) {
		return (k * k) / (d * d * d);
	}

	/**
	 * @param k constant
	 * @param d distance between two vertices with a common edge (adjacent)
	 * @return attraction force
	 */
	public static double fAttract(double k, double d) {
		return (d - k) / d;
	}

	/**
	 * @param a constant
	 * @param d ??? not sure if this is angular distance
	 * @return ???
	 */
	public static double fTangential(double a, double d) {
		return a * d;
	}

	/**
	 * @param b     constant
	 * @param theta angle between current and ideal
	 * @return rotational force on vertex
	 */
	public static double fRotational(double b, double theta) {
		return b * theta;
	}

	public void limitDisplacement(double t) {
		double x = getX();
		double y = getY();
		easting = easting *t;
		northing = northing *t;
		double ny = easting+getY();
		double nx = northing+getX();
		
		// wip...
		
		easting = Math.min(1.0,Math.max(x+easting, 0.0));
		northing = Math.min(1.0,Math.max(y+northing, 0.0));
	}

	public void updatePosition() {
		// TODO Auto-generated method stub
		
	}

}
