package au.edu.anu.twuifx.mm.visualise;
/*
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
  
  Originally from the prefuse library.
 */

import java.util.ArrayList;

/**
 * <p>TreeLayout that computes a tidy layout of a node-link tree
 * diagram. This algorithm lays out a rooted tree such that each
 * depth level of the tree is on a shared line. The orientation of the
 * tree can be set such that the tree goes left-to-right (default),
 * right-to-left, top-to-bottom, or bottom-to-top.</p>
 * 
 * <p>The algorithm used is that of Christoph Buchheim, Michael J�nger,
 * and Sebastian Leipert from their research paper
 * <a href="http://citeseer.ist.psu.edu/buchheim02improving.html">
 * Improving Walker's Algorithm to Run in Linear Time</a>, Graph Drawing 2002.
 * This algorithm corrects performance issues in Walker's algorithm, which
 * generalizes Reingold and Tilford's method for tidy drawings of trees to
 * support trees with an arbitrary number of children at any given node.</p>
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import au.edu.anu.rscs.aot.graph.property.PropertyKeys;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.archetype.TWA;
import au.edu.anu.twcore.exceptions.TwcoreException;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.properties.impl.SharedPropertyListImpl;
import fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels;

/**
 * 
 * Adapted for ThreeWorlds by Ian Davies
 *
 */
public class TreeLayout implements Layout {
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
	private Map<String, List<VisualNode>> sortedChildMap;
	private double xmx;
	private double ymx;
	private double xmn;
	private double ymn;
	private double dx;
	private double dy;

	public TreeLayout(TreeGraph<VisualNode, VisualEdge> visualGraph) {
		propertyMap = new HashMap<>();
		sortedChildMap = new HashMap<>();
		for (VisualNode vroot : visualGraph.roots()) {
			if (TWA.getLabel(root.id()).equals(ConfigurationNodeLabels.N_ROOT.label()))
				root = vroot;
		}
		addProperties(root);
		m_maxDepth = 0;
		Arrays.fill(m_depths, 0);
	}

	private void addProperties(VisualNode parent) {
		SharedPropertyListImpl p = new SharedPropertyListImpl(keys);
		p.setProperty(X, parent.getX());
		p.setProperty(Y, parent.getY());
		propertyMap.put(parent.id(), p);

		List<VisualNode> childList = new ArrayList<>();
		for (VisualNode child : parent.getChildren())
			childList.add(child);
		Collections.sort(childList, new Comparator<VisualNode>() {
			@Override
			public int compare(VisualNode n1, VisualNode n2) {
				return n1.id().compareTo(n2.id());
			}
		});
		sortedChildMap.put(parent.id(), childList);
		for (VisualNode child : parent.getChildren())
			addProperties(child);
	}

	@Override
	public void compute() {

		firstWalk(root, 0, 1);

		determineDepths();

		xmx = Double.NEGATIVE_INFINITY;
		ymx = xmx;
		xmn = Double.POSITIVE_INFINITY;
		ymn = xmn;
		secondWalk(root, null, -(Double) properties(root).getPropertyValue(Prelim), 0);

		dx = xmx - xmn;
		dy = ymx - ymn;
		normalise(root);
	}

	private void normalise(VisualNode n) {
		Random rnd = new Random();
		SharedPropertyListImpl prop = properties(n);

		double x = (Double) prop.getPropertyValue(X);
		double y = (Double) prop.getPropertyValue(Y);
		if (dx > 0)
			x = ((x - xmn) / dx) * 0.9 + 0.03;
		else
			x = 0.5;
		if (dy > 0)
			y = ((y - ymn) / dy) * 0.9;// + 0.03;
		else
			y = 0.5;
		double jitter = rnd.nextDouble() * 0.01;
		if (rnd.nextBoolean())
			x += jitter;
		else
			x -= jitter;

		prop.setProperty(X, x);
		prop.setProperty(Y, y);
		for (VisualNode child : n.getChildren())
			normalise(child);
	}

	private void determineDepths() {
		for (int i = 1; i < m_maxDepth; ++i)
			m_depths[i] += m_depths[i - 1];
	}

	private VisualNode prevSibling(VisualNode sibling) {
		VisualNode parent = sibling.getParent();
		if (parent != null) {
			List<VisualNode> siblings = children(parent);
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
			List<VisualNode> siblings = children(parent);
			int idx = siblings.indexOf(sibling);
			idx++;
			if (idx < siblings.size())
				return siblings.get(idx);
		}
		return null;
	}

	private double spacing() {
		return itemHeight;
	}

	private VisualNode getFirstChild(VisualNode n) {
		if (n == null)
			throw new TwcoreException("Asking for children of null node");
		List<VisualNode> children = children(n);
		if (children.isEmpty())
			throw new TwcoreException("Expecting children but none found ");
		return children.get(0);
	}

	private VisualNode getLastChild(VisualNode n) {
		List<VisualNode> children = children(n);
		return children.get(children.size() - 1);
	}

	private void firstWalk(VisualNode n, int num, int depth) {
		SharedPropertyListImpl prop = properties(n);
		prop.setProperty(Number, num);
		prop.setProperty(Prelim, 0.0);
		prop.setProperty(Thread, null);
		prop.setProperty(Ancestor, null);
		prop.setProperty(Mod, 0.0);
		prop.setProperty(Shift, 0.0);
		prop.setProperty(Change, 0.0);

		updateDepths(depth, n);
		if (!n.hasChildren()) {
			VisualNode l = prevSibling(n);
			if (l == null) {
				prop.setProperty(Prelim, 0.0);
			} else {
				SharedPropertyListImpl lprop = propertyMap.get(l.id());
				double lp = (double) lprop.getPropertyValue(Prelim);
				prop.setProperty(Prelim, lp + spacing());
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
				double nprelim = (Double) properties(left).getPropertyValue(Prelim) + spacing();
				prop.setProperty(Prelim, nprelim);
				prop.setProperty(Mod, nprelim - midpoint);
			} else {
				prop.setProperty(Prelim, midpoint);
			}
		}
	}

	private VisualNode apportion(VisualNode v, VisualNode a) {
		VisualNode w = prevSibling(v);
		if (w != null) {
			VisualNode vip, vim, vop, vom;
			double sip, sim, sop, som;
			vip = vop = v;
			vim = w;
			vom = children(vip.getParent()).get(0);

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
				properties(vop).setProperty(Ancestor, v);
				double shift = ((Double) properties(vim).getPropertyValue(Prelim) + sim)
						- ((Double) properties(vip).getPropertyValue(Prelim) + sip) + spacing();
				if (shift > 0) {
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
		List<VisualNode> children = children(n);
		if (!children.isEmpty())
			return children.get(0);
		else
			return (VisualNode) properties(n).getPropertyValue(Thread);
	}

	private VisualNode nextRight(VisualNode n) {
		List<VisualNode> children = children(n);
		if (!children.isEmpty())
			return children.get(children.size() - 1);
		else
			return (VisualNode) properties(n).getPropertyValue(Thread);

	}

	private void moveSubtree(VisualNode wm, VisualNode wp, double shift) {
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

	private void executeShifts(VisualNode n) {
		double shift = 0, change = 0;
		for (VisualNode c = getLastChild(n); c != null; c = prevSibling(c)) {
			SharedPropertyListImpl props = properties(c);

			double cprelim = (Double) props.getPropertyValue(Prelim);
			cprelim += shift;
			props.setProperty(Prelim, cprelim);

			double dmod = (double) props.getPropertyValue(Mod);
			dmod += shift;
			properties(c).setProperty(Mod, dmod);

			change += (Double) props.getPropertyValue(Change);
			shift += (Double) props.getPropertyValue(Shift) + change;

		}

	}
    private  static final double[] resize(double[] a, int size) {
        if ( a.length >= size ) return a;
        double[] b = new double[size];
        System.arraycopy(a, 0, b, 0, a.length);
        return b;
    }

	private void updateDepths(int depth, VisualNode n) {
		if (m_depths.length <= depth)
			m_depths = resize(m_depths, 3 * depth / 2);
		m_depths[depth] = Math.max(m_depths[depth], itemHeight);
		m_maxDepth = Math.max(m_maxDepth, depth);
	}

	private void secondWalk(VisualNode n, VisualNode p, double m, int depth) {
		SharedPropertyListImpl nprops = properties(n); 
		double y = (Double) nprops.getPropertyValue(Prelim) + m;
		double x = m_depths[depth];
		nprops.setProperty(Y, y);
		nprops.setProperty(X, x);
		xmx = Math.max(xmx, x);
		ymx = Math.max(ymx, y);
		xmn = Math.min(xmn, x);
		ymn = Math.min(ymn, y);
		depth += 1;
		if (!children(n).isEmpty())
			for (VisualNode c = getFirstChild(n); c != null; c = nextSibling(c)) {
				secondWalk(c, n, m + (Double) nprops.getPropertyValue(Mod), depth);
			}
	}

	private SharedPropertyListImpl properties(VisualNode node) {
		return propertyMap.get(node.id());
	}

	private List<VisualNode> children(VisualNode node) {
		return sortedChildMap.get(node.id());
	}

}
