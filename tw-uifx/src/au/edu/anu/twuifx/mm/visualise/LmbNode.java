package au.edu.anu.twuifx.mm.visualise;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import au.edu.anu.omhtk.rng.Pcg32;
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

	protected String id() {
		return id;
	}

	protected double getXDisp() {
		return easting;
	}

	protected double getYDisp() {
		return northing;
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

	public void reset() {
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

	private static String sep = "\t";

	@Override
	public String toString() {
		return "[" + getX() + "," + getY() + "]" + id();
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
		double mag = fAttraction(other.getX(), other.getY(), k) ;
		double dx = dx(other);
		double dy = dy(other);
		double a = Math.atan2(dy, dx);
		double x = mag * Math.cos(a);
		double y = mag * Math.sin(a);
		easting += x;
		northing += y;
	}

	private static Rectangle2D f = new Rectangle.Double(0, 0, 1.0, 1.0);

	public double displace(double t) {
		double direction = Math.atan2(northing, easting);
		double distance = Math.sqrt((easting * easting) + (northing * northing));
		distance = Math.min(t, distance);
		double dx = distance * Math.cos(direction);
		double dy = distance * Math.sin(direction);
		double nx = dx + getX();
		double ny = dy + getY();
		setPosition(nx, ny);
		return Distance.euclidianDistance(0, 0, dx, dy);
	}

//	public double force() {
//		return Distance.euclidianDistance(0, 0, easting, northing);
//	}

	private static double close = 0.0000001;

	public double fRepulsion(LmbNode other, double k) {
		double d = Distance.euclidianDistance(getX(), getY(), other.getX(), other.getY());
		if (d < close) {
			System.out.println("R Distance zero");
			d = Math.max(close, d);
		}
		return fRepulsion(k, d);
	}

	public double fAttraction(double ox, double oy, double k) {
		double d = Distance.euclidianDistance(getX(), getY(), ox, oy);
		if (d < close) {
			System.out.println("A Distance zero");
			d = Math.max(close, d);
		}
		return fAttract(k, d);
	}

	protected double getUpdatedDistance(LmbNode other) {
		return Distance.euclidianDistance(getX() + getXDisp(), getY() + getYDisp(), other.getX() + other.getXDisp(),
				other.getY() + other.getYDisp());
	}

	
	// private static double c4 = 0.1;
	/**
	 * @param k constant
	 * @param d distance between any two nodes
	 * @return repulsion force
	 */
	public static double fRepulsion(double k, double d) {
		return (k * k) / (d * d * d);
		// return c3 / (d * d);
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

}
