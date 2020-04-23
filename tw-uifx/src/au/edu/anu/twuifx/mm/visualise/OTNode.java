package au.edu.anu.twuifx.mm.visualise;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;

public class OTNode {
	private VisualNode vNode;
	private OTNode parent;
	private double prelim;
	private int number;
	private OTNode thread;
	private OTNode ancestor;
	private double mod;
	private double shift;
	private double change;
	private List<OTNode> children;
	protected static int m_maxDepth = 0;
	protected static double[] m_depths = new double[10];
	private static final double itemHeight = 8.0;

	public OTNode(OTNode parent, VisualNode vNode) {
		this.parent = parent;
		this.vNode = vNode;
		this.children = new ArrayList<>();
	}

	private static void dump(OTNode n, String msg) {
		System.out.println(n.getvNode().getDisplayText(false) + "\t" + msg);
	}

	public void firstWalk(int num, int depth) {
//		dump(this,"firstWalk");
		setNumber(num);
		setPrelim(0.0);
		setThread(null);
		setAncestor(null);
		setMod(0.0);
		setShift(0.0);
		setChange(0.0);

		updateDepths(depth);
		if (getChildren().isEmpty()) {// leaf
			OTNode l = prevSibling();
			if (l == null)
				setPrelim(0.0);
			else {
//				dump(l,"case1");
				l.setPrelim(l.getPrelim() + itemHeight);
			}
		} else {
			OTNode leftMost = getFirstChild();
			OTNode rightMost = getLastChild();
			OTNode defaultAncestor = leftMost;
			OTNode c = leftMost;
			for (int i = 0; c != null; ++i, c = c.nextSibling()) {
				c.firstWalk(i, depth + 1);
				defaultAncestor = c.apportion(defaultAncestor);
			}

			executeShifts();
			double pl = leftMost.getPrelim();
			double pr = rightMost.getPrelim();

			double midpoint = 0.5 * (pl + pr);

			OTNode left = prevSibling();
			if (left != null) {
				double nprelim = left.getPrelim() + itemHeight;
				setPrelim(nprelim);
				setMod(nprelim - midpoint);
			} else {
				setPrelim(midpoint);
			}

		}
	}

	private static void updateDepths(int depth) {
		if (m_depths.length <= depth)
			m_depths = resize(m_depths, 3 * depth / 2);
		m_depths[depth] = Math.max(m_depths[depth], OTNode.itemHeight);
		m_maxDepth = Math.max(m_maxDepth, depth);
	}

	private static final double[] resize(double[] a, int size) {
		if (a.length >= size)
			return a;
		double[] b = new double[size];
		System.arraycopy(a, 0, b, 0, a.length);
		return b;
	}

	private OTNode getFirstChild() {
		return children.get(0);
	}

	private OTNode getLastChild() {
		return children.get(children.size() - 1);
	}

	private OTNode prevSibling() {
		OTNode parent = getParent();
		if (parent != null) {
			int idx = parent.getChildren().indexOf(this);
			idx--;
			if (idx >= 0)
				return parent.getChildren().get(idx);
		}
		return null;
	}

	private OTNode nextSibling() {
		OTNode parent = getParent();
		if (parent != null) {
			int idx = parent.getChildren().indexOf(this);
			idx++;
			if (idx < parent.getChildren().size())
				return parent.getChildren().get(idx);
		}
		return null;
	}

	private OTNode apportion(OTNode a) {
		OTNode w = prevSibling();
		if (w != null) {
//			dump(this, "apportion");
//			dump(w, "apportion");
			OTNode vip, vim, vop, vom;
			double sip, sim, sop, som;
			vip = vop = this;
			vim = w;
			vom = vip.getParent().getChildren().get(0);

			sip = vip.getMod();
			sop = vop.getMod();
			sim = vim.getMod();
			som = vom.getMod();

			OTNode nr = vim.nextRight();
			OTNode nl = vip.nextLeft();
			while (nr != null && nl != null) {
				vim = nr;
				vip = nl;
				vom = vom.nextLeft();
				vop = vop.nextRight();
//				dump(vim, "vim");
//				dump(vip, "vip");
//				dump(vom, "vom");
//				dump(vop, "vop");

				vop.setAncestor(this);
				double shift = (vim.getPrelim() + sim) - (vip.getPrelim() + sip) + itemHeight;
				if (shift > 0) {
					dump(this, "Shift " + shift);
					moveSubTree(ancestor(vim, a), shift);
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
				a = this;
			}
		}
		return a;
	}

	private OTNode nextLeft() {
		if (!children.isEmpty())
			return children.get(0);
		return getThread();
	}

	private OTNode nextRight() {
		if (!children.isEmpty())
			return children.get(children.size() - 1);
		else
			return getThread();
	}

	private OTNode ancestor(OTNode vim, OTNode a) {
		OTNode p = getParent();
		OTNode vimAncestor = vim.getAncestor();
		if (vimAncestor != null)
			if (vimAncestor.getParent() != null) {
				OTNode vimParent = vimAncestor.getParent();
				if (vimParent.equals(p))
					return vimAncestor;
			}
		return a;
	}

	private void moveSubTree(OTNode wm, double shft) {
		// wp is THIS
		// wm is the new ancestor
		int wpNumber = getNumber();
		int wmNumber = wm.getNumber();
		double subTrees = wpNumber - wmNumber;

		double wpChange = getChange();
		wpChange -= shft / subTrees;
		setChange(wpChange);

		double wpShift = getShift();
		wpShift += shft;
		setShift(wpShift);

		double wmChange = wm.getChange();
		wmChange += shft / subTrees;
		wm.setChange(wmChange);

		double wpPrelim = getPrelim();
		wpPrelim += shft;
		setPrelim(wpPrelim);

		double wpMod = getMod();
		wpMod += shft;
		setMod(wpMod);
	}

	private void executeShifts() {
		double shft = 0, chng = 0;
		for (OTNode c = getLastChild(); c != null; c = c.prevSibling()) {
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

	public void secondWalk(OTNode p, double m, int depth) {
//		dump(this,"secondWalk");
		double y = getPrelim() + m;
		double x = OTNode.m_depths[depth];
		getvNode().setX(x);
		getvNode().setY(y);
		depth += 1;
		if (!children.isEmpty()) {
			for (OTNode c = getFirstChild(); c != null; c = c.nextSibling()) {
				c.secondWalk(this, m + getMod(), depth);
			}
		}
	}

	public VisualNode getvNode() {
		return vNode;
	}

	public void setvNode(VisualNode vNode) {
		this.vNode = vNode;
	}

	public OTNode getParent() {
		return parent;
	}

	public void setParent(OTNode parent) {
		this.parent = parent;
	}

	public double getPrelim() {
		return prelim;
	}

	public void setPrelim(double prelim) {
		this.prelim = prelim;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public OTNode getThread() {
		return thread;
	}

	public void setThread(OTNode thread) {
		this.thread = thread;
	}

	public OTNode getAncestor() {
		return ancestor;
	}

	public void setAncestor(OTNode ancestor) {
		this.ancestor = ancestor;
	}

	public double getMod() {
		return mod;
	}

	public void setMod(double mod) {
		this.mod = mod;
	}

	public double getShift() {
		return shift;
	}

	public void setShift(double shift) {
		this.shift = shift;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}

	public List<OTNode> getChildren() {
		return children;
	}

	public boolean hasParent() {
		return parent != null;
	}

	@Override
	public String toString() {
		return getvNode().getDisplayText(false);
	}

	public void getLayoutBounds(Point2D min, Point2D max) {
		double x = getvNode().getX();
		double y = getvNode().getY();
//		System.out.println(x+"\t"+y);

		min.setLocation(Math.min(x, min.getX()), Math.min(y, min.getY()));
		max.setLocation(Math.max(x, max.getX()), Math.max(y, max.getY()));
		for (OTNode child : getChildren())
			child.getLayoutBounds(min, max);
	}

	public void normalise(Point2D fromMin, Point2D fromMax, Point2D toMin, Point2D toMax) {
		double x = getvNode().getX();
		double y = getvNode().getY();
		x = ILayout.rescale(x, fromMin.getX(), fromMax.getX(), toMin.getX(), toMax.getX());
		y = ILayout.rescale(y, fromMin.getY(), fromMax.getY(), toMin.getY(), toMax.getY());
		getvNode().setX(x);
		getvNode().setY(y);
		for (OTNode child : children) {
			child.normalise(fromMin, fromMax, toMin, toMax);
		}
	}
}
