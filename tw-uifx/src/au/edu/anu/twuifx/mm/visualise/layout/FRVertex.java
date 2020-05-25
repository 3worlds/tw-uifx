/**************************************************************************
 *  TW-APPS - Applications used by 3Worlds                                *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-APPS contains ModelMaker and ModelRunner, programs used to         *
 *  construct and run 3Worlds configuration graphs. All code herein is    *
 *  independent of UI implementation.                                     *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-APPS (3Worlds applications).                  *
 *                                                                        *
 *  TW-APPS is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-APPS is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-APPS.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
  **************************************************************************/

package au.edu.anu.twuifx.mm.visualise.layout;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 13 Apr 2020
 */

public class FRVertex extends VertexAdapter {
	/* total displacement of x and y */
	protected double fx;
	protected double fy;
	/* vertices without edges are excluded from the algorithm */
	private boolean hasEdges;

	public FRVertex(VisualNode node) {
		super(node);
	}

	/**
	 * Calculate the displacement from the repulsion force between this node and
	 * another.
	 * 
	 * @param other
	 * @param k     spacing constant (ideal spring length)
	 */
	public double setRepulsionDisplacement(FRVertex other, double k) {
		return repApply(this, other, k);
	}

	/**
	 * Calculate the displacement from the attraction force between this node and
	 * another connected by a common edge.
	 * 
	 * @param other
	 * @param k     spacing constant (ideal spring length)
	 */
	public double setAttractionDisplacement(FRVertex other, double k) {
		return attrApply(this, other, k);
	}

	/**
	 * Update the node position with the displacement limited by temperature
	 * 
	 * @param temperature temperature
	 * @return energy
	 */
	public double displace(double temperature) {
		double force = Math.sqrt(fx * fx + fy * fy);
		if (force < temperature) {
			setLocation(getX() + fx, getY() + fy);
		} else {
			double fact = temperature / force;
			double dx = fx * fact;
			double dy = fy * fact;
			setLocation(getX() + dx, getY() + dy);
		}
		fx = 0;
		fy = 0;
		return force;
	}

	public void setHasEdge(boolean b) {
		hasEdges = b;
	}

	public boolean hasEdges() {
		return hasEdges;
	}

	private static double repApply(FRVertex p, FRVertex q, double k) {
		double dx = q.getX() - p.getX();
		double dy = q.getY() - p.getY();
		double dist2 = dx * dx + dy * dy;
		while (dist2 == 0) {
			dx = 5 - Math.round(Math.random() * 10);
			dy = 5 - Math.round(Math.random() * 10);
			dist2 = dx * dx + dy * dy;
		}
		double force = fRepulsion(k, Math.sqrt(dist2));
		q.fx += dx * force;
		q.fy += dy * force;
		p.fx -= dx * force;
		p.fy -= dy * force;
		return force;

	}

	public static double attrApply(FRVertex p, FRVertex q, double k) {
		double dx = q.getX() - p.getX();
		double dy = q.getY() - p.getY();
		double dist2 = dx * dx + dy * dy;
		while (dist2 == 0) {
			dx = 5 - Math.round(Math.random() * 10);
			dy = 5 - Math.round(Math.random() * 10);
			dist2 = dx * dx + dy * dy;
		}
		double force = fAttract(k, Math.sqrt(dist2));
		q.fx -= dx * force;
		q.fy -= dy * force;
		p.fx += dx * force;
		p.fy += dy * force;
		return force;
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
