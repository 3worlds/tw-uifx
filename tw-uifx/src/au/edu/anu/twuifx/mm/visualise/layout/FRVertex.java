package au.edu.anu.twuifx.mm.visualise.layout;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.uit.space.Distance;

/**
 * @author Ian Davies
 *
 * @date 13 Apr 2020
 */

public class FRVertex extends VertexAdapter{
	//private VisualNode node;
	private double dispX; // total displacement of x and y
	private double dispY;
	//private String id;
	private boolean hasEdges;

	public FRVertex(VisualNode node) {
		super (node);
//		this.node = node;
//		this.id = node.getDisplayText(false);
		this.hasEdges = false;
	}

	/**
	 * Calculate the displacement from the repulsion force between this node and
	 * another.
	 * 
	 * @param other
	 * @param k     spacing constant (ideal spring length)
	 */
	public void setRepulsionDisplacement(FRVertex other, double k) {
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
	public void setAttractionDisplacement(FRVertex other, double k) {
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
		setLocation(nx, ny);
		return Distance.euclidianDistance(0, 0, dx, dy);
	}


	protected double getXDisp() {
		return dispX;
	}

	protected double getYDisp() {
		return dispY;
	}


	public void clearDisplacement() {
		dispX = 0;
		dispY = 0;
	}


	private double dx(FRVertex other) {
		return other.getX() - getX();
	}

	private double dy(FRVertex other) {
		return other.getY() - getY();
	}


	public void hasEdge() {
		hasEdges = true;
	}

	public boolean hasEdges() {
		return hasEdges;
	}

	private static double close = 0.0000001;

	public double fRepulsion(FRVertex other, double k) {
		double d = Distance.euclidianDistance(getX(), getY(), other.getX(), other.getY());
		if (d < close) {
			System.out.println("R Distance zero");
			d = Math.max(close, d);
		}
		return fRepulsion(k, d);
	}

	public double fAttraction(FRVertex other, double k) {
		double d = Distance.euclidianDistance(getX(), getY(), other.getX(), other.getY());
		if (d < close) {
			System.out.println("A Distance zero");
			d = Math.max(close, d);
		}
		return fAttract(k, d);
	}

	/**
	 * @param k ideal spring length
	 * @param d distance between any two nodes
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
