package au.edu.anu.twuifx.mm.visualise.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 24 Apr 2020
 */
public class OTVertex {
	private VisualNode _vNode;
	private OTVertex _parent;
	private double _prelim;
	private int _number;
	// To traverse inside and outside contours of the three.
	private OTVertex _thread;
	private OTVertex _ancestor;
	private double _mod;
	private double _shift;
	private double _change;
	private List<OTVertex> _children;
	protected static int maxLevels = 0;
	protected static double[] levels = new double[10];
	private static final double distance = 1.0;

	public OTVertex(OTVertex parent, VisualNode vNode) {
		this._parent = parent;
		this._vNode = vNode;
		this._children = new ArrayList<>();
	}

	/**
	 * Computes a preliminary x-coord. Before that, 'firstWalk' is applied
	 * recursively to all children as well as the function 'apportion'. After
	 * spacing out the children by calling ExecuteShifts, the node is placed at the
	 * midpoint of its outermost children.
	 */
	public void firstWalk(int num, int depth) {
		setNumber(num);
		updateDepths(depth);
		if (isLeaf()) {
			setPrelim(0.0);
			OTVertex leftSibling = prevSibling();
			if (leftSibling != null)
				setPrelim(leftSibling.getPrelim() + distance);
		} else {
			OTVertex leftMost = getFirstChild();
			OTVertex defaultAncestor = leftMost;
			OTVertex child = leftMost;
			for (int i = 0; child != null; ++i, child = child.nextSibling()) {
				child.firstWalk(i, depth + 1);
				defaultAncestor = child.apportion(defaultAncestor);
			}

			executeShifts();

			double midpoint = 0.5 * (leftMost.getPrelim() + getLastChild().getPrelim());

			OTVertex w = prevSibling();
			if (w != null) {
				setPrelim(w.getPrelim() + distance);
				setMod(getPrelim() - midpoint);
			} else {
				setPrelim(midpoint);
			}

		}
	}

	private static void updateDepths(int level) {
		if (levels.length <= level)
			levels = resize(levels, 3 * level / 2);
		levels[level] = Math.max(levels[level], OTVertex.distance);
		maxLevels = Math.max(maxLevels, level);
	}

	private static final double[] resize(double[] a, int size) {
		if (a.length >= size)
			return a;
		double[] b = new double[size];
		System.arraycopy(a, 0, b, 0, a.length);
		return b;
	}

	private OTVertex getFirstChild() {
		return _children.get(0);
	}

	private OTVertex getLastChild() {
		return _children.get(_children.size() - 1);
	}

	private OTVertex prevSibling() {
		OTVertex parent = getParent();
		if (parent != null) {
			int idx = parent.getChildren().indexOf(this);
			idx--;
			if (idx >= 0)
				return parent.getChildren().get(idx);
		}
		return null;
	}

	private OTVertex nextSibling() {
		OTVertex parent = getParent();
		if (parent != null) {
			int idx = parent.getChildren().indexOf(this);
			idx++;
			if (idx < parent.getChildren().size())
				return parent.getChildren().get(idx);
		}
		return null;
	}

	/**
	 * This is the core of the algorithm. Here, a new subtree is combined with the
	 * previous subtrees. 'Threads' are used to traverse the inside and outside
	 * contours of the left and right subtree up to the highest common level. The
	 * vertices (v) used for the traversals are vip,vim, vop and vom, where 'o'
	 * means 'outside' and 'i' means 'inside', 'm' means left subtree and 'p' means
	 * right subtree. For summing the modifiers along th ecoutour, we use respective
	 * variables sip, sim, som and sop. Whenever two nodes of the inside contours
	 * conflict, we compute the left one of the greatest distinct ancestors using
	 * the function "Ancestor" and call "MoveSubtree to shift the subtree and
	 * prepare the shifts of smaller subtrees. Finally, we add a new 'thread' (if
	 * necessary).
	 */
	private OTVertex apportion(OTVertex defaultAncestor) {
		OTVertex w = prevSibling();
		if (w != null) {
			OTVertex vip, vim, vop, vom;
			double sip, sim, sop, som;
			vip = vop = this;
			vim = w;
			vom = vip.getParent().getChildren().get(0);

			sip = vip.getMod();
			sop = vop.getMod();
			sim = vim.getMod();
			som = vom.getMod();

			OTVertex nr = vim.nextRight();
			OTVertex nl = vip.nextLeft();
			while (nr != null && nl != null) {
				vim = nr;
				vip = nl;
				vom = vom.nextLeft();
				vop = vop.nextRight();

				vop.setAncestor(this);
				double vimpl = vim.getPrelim();
				double vippl = vip.getPrelim();
				double shift = (vimpl + sim) - (vippl + sip) + distance;

				shift = (vim.getPrelim() + sim) - (vip.getPrelim() + sip) + distance;
				if (shift > 0) {
					moveSubTree(ancestor(vim, defaultAncestor), shift);
					sip += shift;
					sop += shift;
				}
				sim += vim.getMod();
				sip += vip.getMod();
				som += vom.getMod();
				sop += vop.getMod();

				nr = vim.nextRight();
				nl = vip.nextLeft();
			}
			if (nr != null && vop.nextRight() == null) {
				vop.setThread(nr);
				double m = vop.getMod();
				m += sim - sop;
				vop.setMod(m);
			}
			if (nl != null && vom.nextLeft() == null) {
				vom.setThread(nl);
				double m = vom.getMod();
				m += sip - som;
				vom.setMod(m);
				defaultAncestor = this;
			}
		}
		return defaultAncestor;
	}

	/**
	 * This function is used to traverse the left contour of a subtree. It returns
	 * the successor of this vertex on this contour. This successor is either given
	 * by the leftmost child of this vertex or by its thread. The function returns
	 * null if and only if this vertex is on the highest evel of its subtree.
	 */
	private OTVertex nextLeft() {
		if (!_children.isEmpty())
			return _children.get(0);
		return getThread();
	}

	/** Works analogously to nextLeft() */
	private OTVertex nextRight() {
		if (!_children.isEmpty())
			return _children.get(_children.size() - 1);
		else
			return getThread();
	}

	/**
	 * Returns the left one of the greatest distinct ancestors of this vertex and
	 * its neighbour.
	 */
	private OTVertex ancestor(OTVertex insideLeftSubtree, OTVertex defaultAncestor) {
		// inside left subtree
		OTVertex parent = getParent();
		OTVertex ancst = insideLeftSubtree.getAncestor();
		if (ancst != null)
			if (ancst.getParent() != null) {
				OTVertex ancstParent = ancst.getParent();
				if (ancstParent.equals(parent))
					return ancst;
			}
		return defaultAncestor;
	}

	/**
	 * Shifts the current subtree rooted at "this". This is done by increasing
	 * prelim() and mod() by shift. All other shifts, applied to the smaller
	 * subtrees between wm and 'this' are performed later by ExecuteSHifts. To
	 * prepare for this, we adjust wm.change(), wm.shift() and this.change().
	 */
	private void moveSubTree(OTVertex wm, double shift) {
		double subTrees = getNumber() - wm.getNumber();

		double wpChange = getChange();
		wpChange -= shift / subTrees;
		setChange(wpChange);

		double wpShift = getShift();
		wpShift += shift;
		setShift(wpShift);

		double wmChange = wm.getChange();
		wmChange += shift / subTrees;
		wm.setChange(wmChange);

		double wpPrelim = getPrelim();
		wpPrelim += shift;
		setPrelim(wpPrelim);

		double wpMod = getMod();
		wpMod += shift;
		setMod(wpMod);
	}

	/**
	 * This function only needs one traversal of the children of this vertex to
	 * execute all shifts computed and memorized in MoveSubtree().
	 */
	private void executeShifts() {
		double shft = 0, chng = 0;
		for (OTVertex c = getLastChild(); c != null; c = c.prevSibling()) {
			double cprelim = c.getPrelim();
			cprelim += shft;
			c.setPrelim(cprelim);

			double dmod = c.getMod();
			dmod += shft;
			c.setMod(dmod);

			chng += c.getChange();
			shft += c.getShift() + chng;
		}
	}

	/** Computes all real x-coords by summing the modifiers recursively. */
	public void secondWalk(OTVertex p, double m, int depth) {
		double y = getPrelim() + m;
		double x = OTVertex.levels[depth];
		getvNode().setX(x);
		getvNode().setY(y);
		depth += 1;
		if (!isLeaf()) {
			for (OTVertex child = getFirstChild(); child != null; child = child.nextSibling()) {
				child.secondWalk(this, m + getMod(), depth);
			}
		}
	}

	public VisualNode getvNode() {
		return _vNode;
	}

	public void setvNode(VisualNode vNode) {
		this._vNode = vNode;
	}

	public OTVertex getParent() {
		return _parent;
	}

	public void setParent(OTVertex parent) {
		this._parent = parent;
	}

	public double getPrelim() {
		return _prelim;
	}

	public void setPrelim(double prelim) {
		this._prelim = prelim;
	}

	public int getNumber() {
		return _number;
	}

	public void setNumber(int number) {
		this._number = number;
	}

	public OTVertex getThread() {
		return _thread;
	}

	public void setThread(OTVertex thread) {
		this._thread = thread;
	}

	public OTVertex getAncestor() {
		return _ancestor;
	}

	public void setAncestor(OTVertex ancestor) {
		this._ancestor = ancestor;
	}

	public double getMod() {
		return _mod;
	}

	public void setMod(double mod) {
		this._mod = mod;
	}

	public double getShift() {
		return _shift;
	}

	public void setShift(double shift) {
		this._shift = shift;
	}

	public double getChange() {
		return _change;
	}

	public void setChange(double change) {
		this._change = change;
	}

	public List<OTVertex> getChildren() {
		return _children;
	}

	public boolean hasParent() {
		return _parent != null;
	}

	private boolean isLeaf() {
		return _children.isEmpty();
	}

	@Override
	public String toString() {
		return getvNode().getDisplayText(false);
	}

	public void getLayoutBounds(Point2D min, Point2D max) {
		double x = getvNode().getX();
		double y = getvNode().getY();

		min.setLocation(Math.min(x, min.getX()), Math.min(y, min.getY()));
		max.setLocation(Math.max(x, max.getX()), Math.max(y, max.getY()));
		for (OTVertex child : getChildren())
			child.getLayoutBounds(min, max);
	}

	public void normalise(Point2D fromMin, Point2D fromMax, Point2D toMin, Point2D toMax) {
		double x = getvNode().getX();
		double y = getvNode().getY();
		x = ILayout.rescale(x, fromMin.getX(), fromMax.getX(), toMin.getX(), toMax.getX());
		y = ILayout.rescale(y, fromMin.getY(), fromMax.getY(), toMin.getY(), toMax.getY());
		getvNode().setX(x);
		getvNode().setY(y);
		for (OTVertex child : _children) {
			child.normalise(fromMin, fromMax, toMin, toMax);
		}
	}
}
