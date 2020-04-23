
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
/**------------------------------------------------------------------------
 * 
  Copyright (c) 2004-2007 Regents of the University of California.
  All rights reserved.
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:
  1. Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
  3.  Neither the name of the University nor the names of its contributors
  may be used to endorse or promote products derived from this software
  without specific prior written permission.
  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  SUCH DAMAGE.
------------------------------------------------------------------------- */

package au.edu.anu.twuifx.mm.visualise;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import au.edu.anu.rscs.aot.graph.property.PropertyKeys;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.exceptions.TwcoreException;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.properties.impl.SharedPropertyListImpl;
import fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels;

/**
 * 
 * Adapted for ThreeWorlds by Ian Davies
 *
 */

/**
 * @author Ian Davies
 *
 * @date 13 Aug 2019
 */
/**
 * <p>
 * TreeLayout that computes a tidy layout of a node-link tree diagram. This
 * algorithm lays out a rooted tree such that each depth level of the tree is on
 * a shared line. The orientation of the tree can be set such that the tree goes
 * left-to-right (default), right-to-left, top-to-bottom, or bottom-to-top.
 * </p>
 * 
 * <p>
 * The algorithm used is that of Christoph Buchheim, Michael J�nger, and
 * Sebastian Leipert from their research paper
 * <a href="http://citeseer.ist.psu.edu/buchheim02improving.html"> Improving
 * Walker's Algorithm to Run in Linear Time</a>, Graph Drawing 2002. This
 * algorithm corrects performance issues in Walker's algorithm, which
 * generalizes Reingold and Tilford's method for tidy drawings of trees to
 * support trees with an arbitrary number of children at any given node.
 * </p>
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class OTLayoutOld implements ILayout {
	private static final String Prelim = "prelim";
	private static final String Number = "number";
	private static final String Thread = "thread";
	private static final String Mod = "mod";
	private static final String Ancestor = "ancestor";
	private static final String Shift = "shift";
	private static final String Change = "change";
	private static final String X = "x";
	private static final String Y = "y";
	private static PropertyKeys keys = new PropertyKeys(X, Y, Change, Shift, Ancestor, Mod, Thread, Number, Prelim);
	private double[] m_depths = new double[10];
	private int m_maxDepth;
	private final double itemHeight = 8.0;

	private VisualNode root;
	private Map<String, SharedPropertyListImpl> propertyMap;
	//
	private Map<String, List<VisualNode>> sortedChildMap;

	/*
	 * TreeLayout operator is only applied to expanded trees from the 3w root. The
	 * position of detached or collapsed trees are left unchanged.
	 * 
	 * Properties are neither added or deleted from the visual node's property list.
	 * Instead a lookup table is built of properties required by the layout
	 * operator.
	 * 
	 * Child nodes are sorted in alpha order for visual consistency.
	 */
	public OTLayoutOld(TreeGraph<VisualNode, VisualEdge> visualGraph) {
		propertyMap = new HashMap<>();

		sortedChildMap = new HashMap<>();
		for (VisualNode vnRoot : visualGraph.roots()) {
			if (vnRoot.cClassId().equals(ConfigurationNodeLabels.N_ROOT.label()))
				root = vnRoot;
		}
		addProperties(root);
		m_maxDepth = 0;
		Arrays.fill(m_depths, 0);
	}

	private void addProperties(VisualNode parent) {
		SharedPropertyListImpl p = new SharedPropertyListImpl(keys);
		propertyMap.put(parent.id(), p);

		List<VisualNode> childList = new ArrayList<>();
		for (VisualNode child : parent.getChildren()) {
			if (!child.isCollapsed())
				childList.add(child);
		}
		Collections.sort(childList, new Comparator<VisualNode>() {
			@Override
			public int compare(VisualNode n1, VisualNode n2) {
				String s1 = n1.cClassId() + ":" + n1.id();
				String s2 = n2.cClassId() + ":" + n2.id();
				return s1.compareTo(s2);
			}
		});
		sortedChildMap.put(parent.id(), childList);
		for (VisualNode child : parent.getChildren()) {
			if (!child.isCollapsed())
				addProperties(child);
		}
	}

	@Override
	public ILayout compute() {

		firstWalk(root, 0, 1);

		determineDepths();

		secondWalk(root, null, -(Double) properties(root).getPropertyValue(Prelim), 0);

		Point2D min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point2D max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

		getLayoutBounds(min, max);

		normalise(root, min, max, new Point2D.Double(0.0, 0.0), new Point2D.Double(1.0, 1.0));

		return this;
	}

	private static void dump(VisualNode n, String msg) {
		System.out.println(n.getDisplayText(false) + "\t" + msg);
	}

	private void firstWalk(VisualNode n, int num, int depth) {
		// dump(n,"firstWalk");
		SharedPropertyListImpl thisProps = properties(n);
		thisProps.setProperty(Number, num);
		thisProps.setProperty(Prelim, 0.0);
		thisProps.setProperty(Thread, null);
		thisProps.setProperty(Ancestor, null);
		thisProps.setProperty(Mod, 0.0);
		thisProps.setProperty(Shift, 0.0);
		thisProps.setProperty(Change, 0.0);

		updateDepths(depth, n);
		if (!hasAccessibleChildren(n)) {
			VisualNode l = prevSibling(n);
			if (l == null) {
				thisProps.setProperty(Prelim, 0.0);
			} else {
//				dump(l,"case1");
				SharedPropertyListImpl lprop = propertyMap.get(l.id());
				double lp = (double) lprop.getPropertyValue(Prelim);
				thisProps.setProperty(Prelim, lp + itemHeight);
			}

		} else {
			VisualNode leftMost = getFirstChild(n);
			VisualNode rightMost = getLastChild(n);
			VisualNode defaultAncestor = leftMost;
			VisualNode c = leftMost;
			for (int i = 0; c != null; ++i, c = nextSibling(c)) {
				firstWalk(c, i, depth + 1);
				defaultAncestor = apportion(c, defaultAncestor);
			}

			executeShifts(n);
			SharedPropertyListImpl lmProp = properties(leftMost);
			SharedPropertyListImpl rmProp = properties(rightMost);
			double pl = (Double) lmProp.getPropertyValue(Prelim);
			double pr = (Double) rmProp.getPropertyValue(Prelim);

			double midpoint = 0.5 * (pl + pr);

			VisualNode left = prevSibling(n);
			if (left != null) {
				double nprelim = (Double) properties(left).getPropertyValue(Prelim) + itemHeight;
				thisProps.setProperty(Prelim, nprelim);
				thisProps.setProperty(Mod, nprelim - midpoint);
			} else {
				thisProps.setProperty(Prelim, midpoint);
			}
		}
	}

	private VisualNode getFirstChild(VisualNode n) {
		if (n == null)
			throw new TwcoreException("Asking for children of null node");
		List<VisualNode> children = accessibleChildren(n);
		if (children.isEmpty())
			throw new TwcoreException("Expecting children but none found: " + n);
		return children.get(0);
	}

	private VisualNode getLastChild(VisualNode n) {
		List<VisualNode> children = accessibleChildren(n);
		return children.get(children.size() - 1);
	}

	private void updateDepths(int depth, VisualNode n) {
		if (m_depths.length <= depth)
			m_depths = resize(m_depths, 3 * depth / 2);
		m_depths[depth] = Math.max(m_depths[depth], itemHeight);
		m_maxDepth = Math.max(m_maxDepth, depth);
	}

	private static final double[] resize(double[] a, int size) {
		if (a.length >= size)
			return a;
		double[] b = new double[size];
		System.arraycopy(a, 0, b, 0, a.length);
		return b;
	}

	private VisualNode prevSibling(VisualNode sibling) {
		VisualNode parent = sibling.getParent();
		if (parent != null) {
			List<VisualNode> siblings = accessibleChildren(parent);
			int idx = siblings.indexOf(sibling);
			idx--;
			if (idx >= 0)
				return siblings.get(idx);
		}
		return null;
	}

	private VisualNode nextSibling(VisualNode sibling) {
		VisualNode parent = sibling.getParent();
		if (parent != null) {
			List<VisualNode> siblings = accessibleChildren(parent);
			int idx = siblings.indexOf(sibling);
			idx++;
			if (idx < siblings.size())
				return siblings.get(idx);
		}
		return null;
	}

	private VisualNode apportion(VisualNode v, VisualNode a) {
		// v is THIS
		VisualNode w = prevSibling(v);
		if (w != null) {
//			dump(v,"apportion");
//			dump(w,"apportion");

			VisualNode vip, vim, vop, vom;
			double sip, sim, sop, som;
			vip = vop = v;
			vim = w;
			vom = accessibleChildren(vip.getParent()).get(0);

			sip = (Double) propertyMap.get(vip.id()).getPropertyValue(Mod);
			sop = (Double) propertyMap.get(vop.id()).getPropertyValue(Mod);
			sim = (Double) propertyMap.get(vim.id()).getPropertyValue(Mod);
			som = (Double) propertyMap.get(vom.id()).getPropertyValue(Mod);

			VisualNode nr = nextRight(vim);
			VisualNode nl = nextLeft(vip);
			while (nr != null && nl != null) {
				vim = nr;
				vip = nl;
				vom = nextLeft(vom);
				vop = nextRight(vop);
//				dump(vim, "vim");
//				dump(vip, "vip");
//				dump(vom, "vom");
//				dump(vop, "vop");
				properties(vop).setProperty(Ancestor, v);
				double shift = ((Double) properties(vim).getPropertyValue(Prelim) + sim)
						- ((Double) properties(vip).getPropertyValue(Prelim) + sip) + itemHeight;
				if (shift > 0) {
//					dump(v, "Shift " + shift);
					moveSubtree(ancestor(vim, v, a), v, shift);
					sip += shift;
					sop += shift;
				}
				sim += (Double) properties(vim).getPropertyValue(Mod);
				sip += (Double) properties(vip).getPropertyValue(Mod);
				som += (Double) properties(vom).getPropertyValue(Mod);
				sop += (Double) properties(vop).getPropertyValue(Mod);

				nr = nextRight(vim);
				nl = nextLeft(vip);
			}
			if (nr != null && nextRight(vop) == null) {
				properties(vop).setProperty(Thread, nr);
				double m = (double) properties(vop).getPropertyValue(Mod);
				m += sim - sop;
				properties(vop).setProperty(Mod, m);
			}
			if (nl != null && nextLeft(vom) == null) {
				properties(vom).setProperty(Thread, nl);
				double m = (double) properties(vom).getPropertyValue(Mod);
				m += sip - som;
				properties(vom).setProperty(Mod, m);
				a = v;
			}
		}
		return a;
	}

	private VisualNode nextLeft(VisualNode n) {
		List<VisualNode> children = accessibleChildren(n);
		if (!children.isEmpty())
			return children.get(0);
		else
			return (VisualNode) properties(n).getPropertyValue(Thread);
	}

	private VisualNode nextRight(VisualNode n) {
		List<VisualNode> children = accessibleChildren(n);
		if (!children.isEmpty())
			return children.get(children.size() - 1);
		else
			return (VisualNode) properties(n).getPropertyValue(Thread);

	}

	private VisualNode ancestor(VisualNode vim, VisualNode v, VisualNode a) {
		VisualNode p = v.getParent();
		VisualNode vimAncestor = (VisualNode) properties(vim).getPropertyValue(Ancestor);
		if (vimAncestor != null)
			if (vimAncestor.getParent() != null) {
				VisualNode vimParent = vimAncestor.getParent();
				if (vimParent.equals(p))
					return vimAncestor;
			}
		return a;
	}

	private void moveSubtree(VisualNode wm, VisualNode wp, double shift) {
		// wp is this
		// wm is the new ancestor
		int wpNumber = (Integer) properties(wp).getPropertyValue(Number);
		int wmNumber = (Integer) properties(wm).getPropertyValue(Number);
		double subtrees = wpNumber - wmNumber;

		double wpChange = (Double) properties(wp).getPropertyValue(Change);
		wpChange -= shift / subtrees;
		properties(wp).setProperty(Change, wpChange);

		double wpShift = (Double) properties(wp).getPropertyValue(Shift);
		wpShift += shift;
		properties(wp).setProperty(Shift, wpShift);

		double wmChange = (Double) properties(wm).getPropertyValue(Change);
		wmChange += shift / subtrees;
		properties(wm).setProperty(Change, wmChange);

		double wpPrelim = (Double) properties(wp).getPropertyValue(Prelim);
		wpPrelim += shift;
		properties(wp).setProperty(Prelim, wpPrelim);

		double wpMod = (Double) properties(wp).getPropertyValue(Mod);
		wpMod += shift;
		properties(wp).setProperty(Mod, wpMod);
	}

	private void executeShifts(VisualNode n) {
		double shift = 0, change = 0;
		for (VisualNode c = getLastChild(n); c != null; c = prevSibling(c)) {
			SharedPropertyListImpl cProps = properties(c);

			double cprelim = (Double) cProps.getPropertyValue(Prelim);
			cprelim += shift;
			cProps.setProperty(Prelim, cprelim);

			double dmod = (double) cProps.getPropertyValue(Mod);
			dmod += shift;
			properties(c).setProperty(Mod, dmod);
			change += (Double) cProps.getPropertyValue(Change);
			shift += (Double) cProps.getPropertyValue(Shift) + change;
		}
	}

	private void secondWalk(VisualNode n, VisualNode p, double m, int depth) {
//		dump(n,"secondWalk");
		SharedPropertyListImpl thisProps = properties(n);
		double y = (Double) thisProps.getPropertyValue(Prelim) + m;
		double x = m_depths[depth];
		thisProps.setProperty(Y, y);
		thisProps.setProperty(X, x);
		depth += 1;
		if (!accessibleChildren(n).isEmpty())
			for (VisualNode c = getFirstChild(n); c != null; c = nextSibling(c)) {
				secondWalk(c, n, m + (Double) thisProps.getPropertyValue(Mod), depth);
			}
	}

	private SharedPropertyListImpl properties(VisualNode node) {
		return propertyMap.get(node.id());
	}

	private List<VisualNode> accessibleChildren(VisualNode parent) {
		return sortedChildMap.get(parent.id());
	}

	private boolean hasAccessibleChildren(VisualNode parent) {
		return !accessibleChildren(parent).isEmpty();
	}

	private void getLayoutBounds(Point2D min, Point2D max) {
		for (SharedPropertyListImpl pl : propertyMap.values()) {
			double x = (Double) pl.getPropertyValue(X);
			double y = (Double) pl.getPropertyValue(Y);
			min.setLocation(Math.min(x, min.getX()), Math.min(y, min.getY()));
			max.setLocation(Math.max(x, max.getX()), Math.max(y, max.getY()));
		}
	}

	private void normalise(VisualNode parent, Point2D fromMin, Point2D fromMax, Point2D toMin, Point2D toMax) {
		SharedPropertyListImpl prop = properties(parent);
		double x = (Double) prop.getPropertyValue(X);
		double y = (Double) prop.getPropertyValue(Y);
		x = ILayout.rescale(x, fromMin.getX(), fromMax.getX(), toMin.getX(), toMax.getX());
		y = ILayout.rescale(y, fromMin.getY(), fromMax.getY(), toMin.getY(), toMax.getY());
		prop.setProperty(X, x);
		prop.setProperty(Y, y);
		parent.setX(x);
		parent.setY(y);
		for (VisualNode child : parent.getChildren())
			if (!child.isCollapsed())
				normalise(child, fromMin, fromMax, toMin, toMax);
	}

	private void determineDepths() {
		for (int i = 1; i < m_maxDepth; ++i)
			m_depths[i] += m_depths[i - 1];
	}

}
