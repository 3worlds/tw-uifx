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
import fr.cnrs.iees.uit.space.Distance;
import fr.ens.biologie.generic.utils.Duple;

/**
 * @author Ian Davies
 *
 * @date 30 Mar 2020
 */
public class RT1Vertex extends TreeVertexAdapter {
	private double radius;// distance to all children

	public RT1Vertex(TreeVertexAdapter parent, VisualNode node) {
		super(parent, node);
	}

	protected double getRadius() {
		return radius;
	}

	protected void setRadius(double value) {
		this.radius = value;
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
		double i = getIndex();
		if (!getParent().hasParent())
			return (2.0 * Math.PI * i) / m;
		else {
			// π − φ /2 + φ i/m + φ /(2m) NB: error in paper : remove π
			return ((w * i) / m) + w / (2.0 * m) - (w / 2.0);
		}
	}

	protected int getIndex() {
		if (hasParent())
			return getParent().getChildren().indexOf(this);
		return 0;
	}

	private static Duple<Double, Double> polarToCartesian(double radiant, double magnitude) {
		double x = magnitude * Math.cos(radiant);
		double y = magnitude * Math.sin(radiant);
		return new Duple<Double, Double>(x, y);
	}

	/** Recursively translate relative polar coords to absolute Cartesian */
	public void locate(int depth, double angleSum) {
		if (!hasParent()) {
			setLocation(0, 0);
			for (TreeVertexAdapter c : getChildren()) {
				RT1Vertex child = (RT1Vertex) c;
				child.locate(depth + 1, child.getAngle());
			}
		} else {
			double distance = ((RT1Vertex) getParent()).getRadius();
			Duple<Double, Double> p = RT1Vertex.polarToCartesian(angleSum, distance);
			double px = getParent().getX();
			double py = getParent().getY();		
			double cx = p.getFirst();
			double cy = p.getSecond();
			setLocation(px + cx, py + cy);
			for (TreeVertexAdapter c : getChildren()) {
				RT1Vertex child = (RT1Vertex) c;
				child.locate(depth + 1, angleSum + child.getAngle());
			}

		}
	}

}
