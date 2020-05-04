package au.edu.anu.twuifx.mm.visualise.layout;

import fr.cnrs.iees.uit.space.Distance;

/**
 * @author Ian Davies
 *
 * @date 2 May 2020
 */
public class Vector {
	private double x;
	private double y;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;

	}

	public Vector add(Vector v) {
		return new Vector(x + v.x, y + v.y);
	}

	public Vector sub(Vector v) {
		return new Vector(x - v.x, y - v.y);
	}

	public Vector mul(double factor) {
		return new Vector(x * factor, y * factor);
	}

	// Difference??
	public Vector rmul(double factor) {
		return new Vector(x * factor, y * factor);
	}

	public Vector truediv(double factor) {
		return new Vector(x / factor, y / factor);
	}

	public Vector normalize() {
		return scale(1.0 / Distance.euclidianDistance(0, 0, x, y));
	}

	public Vector scale(double k) {
		return new Vector(x * k, y * k);
	}

	public double angle() {
		return (Math.atan2(y, x));
	}

}
