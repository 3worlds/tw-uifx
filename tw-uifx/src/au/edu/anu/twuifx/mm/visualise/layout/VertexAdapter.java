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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 30 Apr 2020
 */
public class VertexAdapter implements IVertex {
	private VisualNode _vNode;

	public VertexAdapter(VisualNode vNode) {
		this._vNode = vNode;
	}

	@Override
	public VisualNode getNode() {
		return _vNode;
	}

	@Override
	public void setLocation(double x, double y) {
		_vNode.setPosition(x, y);
	}

	@Override
	public double getX() {
		return _vNode.getX();
	}

	@Override
	public double getY() {
		return _vNode.getY();
	}

	@Override
	public String id() {
		return _vNode.getDisplayText(false);
	}

	@Override
	public String toString() {
		return "[" + getX() + "," + getY() + "]" + id();
	}

	@Override
	public boolean equals(Object other) {
		IVertex lother = (IVertex) other;
		return id().equals(lother.id());
	}

	@Override
	public void normalise(Rectangle2D from, Rectangle2D to) {
		double x = ILayout.rescale(getX(), from.getMinX(), from.getMaxX(), to.getMinX(), to.getMaxX());
		double y = ILayout.rescale(getY(), from.getMinY(), from.getMaxY(), to.getMinY(), to.getMaxY());
		setLocation(x, y);
	}

	@Override
	public void getLayoutBounds(Point2D min, Point2D max) {
		min.setLocation(Math.min(min.getX(), getX()), Math.min(min.getY(), getY()));
		max.setLocation(Math.max(max.getX(), getX()), Math.max(max.getY(), getY()));
	}

	@Override
	public void jitter(double f,Random rnd) {
		double x = ILayout.jitter(getX(),f,rnd);
		double y = ILayout.jitter(getY(),f,rnd);
		setLocation(x,y);
	}

}
