package au.edu.anu.twuifx.mm.visualise;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.uit.space.Distance;

/**
 * @author Ian Davies
 *
 * @date 13 Apr 2020
 */

public class FRNode {
	private VisualNode node;
	private double dispX; // total displacement of x and y
	private double dispY;
	private String id;

	public FRNode(VisualNode node) {
		this.node = node;
		this.id = node.getDisplayText(false);
	}

	/**
	 * Calculate the displacement from the repulsion force between this node and
	 * another.
	 * 
	 * @param other
	 * @param k     spacing constant (ideal spring length)
	 */
	public void setRepulsionDisplacement(FRNode other, double k) {
		double force = fRepulsion(other, k);
		double direction = Math.atan2(dy(other), dx(other));
		double x = force * Math.cos(direction);
		double y = force * Math.sin(direction);
		dispX -= x;
		dispY -= y;

	}

	/**
	 * Calculate the displacement from the attraction force between this node and
	 * another connected by a common edge.
	 * 
	 * @param other
	 * @param k     spacing constant (ideal spring length)
	 */
	public void setAttractionDisplacement(FRNode other, double k) {
		double force = fAttraction(other, k);
		double direction = Math.atan2(dy(other), dx(other));
		double x = force * Math.cos(direction);
		double y = force * Math.sin(direction);
		dispX += x;
		dispY += y;
	}

	/**
	 * Update the node position with the displacement limited by temperature
	 * 
	 * @param t temperature
	 * @return energy
	 */
	public double displace(double t) {
		double direction = Math.atan2(dispY, dispX);
		double distance = Math.sqrt((dispX * dispX) + (dispY * dispY));
		distance = Math.min(t, distance);
		double dx = distance * Math.cos(direction);
		double dy = distance * Math.sin(direction);
		double nx = dx + getX();
		double ny = dy + getY();
		setPosition(nx, ny);
		return Distance.euclidianDistance(0, 0, dx, dy);
	}

	protected String id() {
		return id;
	}

	protected double getXDisp() {
		return dispX;
	}

	protected double getYDisp() {
		return dispY;
	}

	protected VisualNode getNode() {
		return node;
	}

	@Override
	public boolean equals(Object other) {
		FRNode lother = (FRNode) other;
		return id.equals(lother.id);
	}

	public void clearDisplacement() {
		dispX = 0;
		dispY = 0;
	}

	private double getX() {
		return node.getX();
	}

	private double getY() {
		return node.getY();
	}

	private void setPosition(double x, double y) {
		node.setPosition(x, y);
	}

	private double dx(FRNode other) {
		return other.getX() - getX();
	}

	private double dy(FRNode other) {
		return other.getY() - getY();
	}

	@Override
	public String toString() {
		return "[" + getX() + "," + getY() + "]" + id();
	}

	private static double close = 0.0000001;

	public double fRepulsion(FRNode other, double k) {
		double d = Distance.euclidianDistance(getX(), getY(), other.getX(), other.getY());
		if (d < close) {
			System.out.println("R Distance zero");
			d = Math.max(close, d);
		}
		return fRepulsion(k, d);
	}

	public double fAttraction(FRNode other, double k) {
		double d = Distance.euclidianDistance(getX(), getY(), other.getX(), other.getY());
		if (d < close) {
			System.out.println("A Distance zero");
			d = Math.max(close, d);
		}
		return fAttract(k, d);
	}

	/**
	 * @param k ideal spring length
	 * @param d  distance between any two nodes
	 * @return repulsion force
	 */
	public static double fRepulsion(double k, double d) {
		return (k * k) / (d * d * d);
	}

	/**
	 * @param k ideal spring length
	 * @param d distance between two vertices with a common edge (adjacent)
	 * @return attraction force
	 */
	public static double fAttract(double k, double d) {
		return (d - k) / d;

	}

}
