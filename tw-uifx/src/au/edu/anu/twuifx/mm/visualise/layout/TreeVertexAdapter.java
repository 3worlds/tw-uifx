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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 30 Apr 2020
 */
public abstract class TreeVertexAdapter extends VertexAdapter implements ITreeVertex<TreeVertexAdapter> {
	private List<TreeVertexAdapter> _children;
	private TreeVertexAdapter _parent;

	public TreeVertexAdapter(TreeVertexAdapter parent, VisualNode vNode) {
		super(vNode);
		this._parent = parent;
		this._children = new ArrayList<>();
	}
	public boolean nodeHasEdges() {
		return getNode().edges().iterator().hasNext();
	}

	@Override
	public boolean isChildless() {
		return _children.isEmpty();
	}

	@Override
	public boolean hasParent() {
		return _parent != null;
	}

	@Override
	public List<TreeVertexAdapter> getChildren() {
		return _children;
	}

	@Override
	public TreeVertexAdapter getParent() {
		return _parent;
	}

	@Override
	public void jitter(double f, Random rnd) {
		super.jitter(f, rnd);
		for (TreeVertexAdapter c : getChildren())
			c.jitter(f, rnd);
	}

	@Override
	public void getLayoutBounds(Point2D min, Point2D max) {
		super.getLayoutBounds(min, max);
		for (TreeVertexAdapter c : getChildren())
			c.getLayoutBounds(min, max);
	}

	@Override
	public void normalise(Rectangle2D from, Rectangle2D to) {
		super.normalise(from, to);
		for (IVertex c : getChildren())
			c.normalise(from, to);
	}
	
	public static void buildSpanningTree(TreeVertexAdapter vertex,ITreeVertexFactory factory) {
		List<VisualNode> sortList = new ArrayList<>();
		String parentId = "";
		if (vertex.hasParent())
			parentId = vertex.getParent().getNode().id();
		for (VisualNode nChild : vertex.getNode().getChildren()) {
			String childId = nChild.id();
			if (!nChild.isCollapsed() && !childId.equals(parentId)&& nChild.isVisible())
				sortList.add(nChild);
		}
		VisualNode nParent = vertex.getNode().getParent();
		if (nParent != null)
			if (!nParent.isCollapsed() && !nParent.id().equals(parentId)&& nParent.isVisible())
				sortList.add(nParent);

		sortList.sort(new Comparator<VisualNode>() {
			@Override
			public int compare(VisualNode o1, VisualNode o2) {
				return o1.getDisplayText().compareTo(o2.getDisplayText());
			}
		});
		for (VisualNode nChild : sortList) {
			TreeVertexAdapter vChild = factory.makeVertex(vertex, nChild);
			vertex.getChildren().add(vChild);
			buildSpanningTree(vChild,factory);
		}
	}
	public void getIsolated(List<TreeVertexAdapter> lstIsolated, boolean pcShowing, boolean xlShowing) {
		if (!pcShowing)
			if (!xlShowing)
				lstIsolated.add(this);
			else if (!nodeHasEdges())
				lstIsolated.add(this);
		for (TreeVertexAdapter c:getChildren()) {
			c.getIsolated(lstIsolated,pcShowing,xlShowing);
		}
	}


}
