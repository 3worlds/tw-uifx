package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	public void firstWalk(int num, int depth) {
		setNumber(num);
		setPrelim(0.0);
		setThread(null);
		setAncestor(null);
		setMod(0.0);
		setShift(0.0);
		setChange(0.0);
		updateDepths(depth);
		if (!getChildren().isEmpty()) {// leaf
			OTNode l = prevSibling();
			if (l == null)
				setPrelim(0.0);
			else
				setPrelim(getPrelim() + itemHeight);

		} else {
			OTNode leftMost = children.get(0);
			OTNode rightMost = children.get(children.size() - 1);
			OTNode defaultAncestor = leftMost;
			for (int i = 0; i < children.size(); i++) {
				OTNode child = children.get(i);
				child.firstWalk(i, depth + 1);
				defaultAncestor = child.apportion(defaultAncestor);
			}
			
			executeShifts();
			double pl = leftMost.getPrelim();
			double pr = rightMost.getPrelim();
			double midpoint = 0.5*(pl+pr);
			OTNode left = prevSibling();
			if (left!=null) {
//				double nprelim = (Double) properties(left).getPropertyValue(Prelim) + spacing();
				double nprelim = left.getPrelim() + itemHeight;
//				prop.setProperty(Prelim, nprelim);
				setPrelim(nprelim);
				setMod(nprelim - midpoint);		
			} else {
				setPrelim(midpoint);
			}

		}

	}

	private void executeShifts() {
		double shift = 0, change = 0;
		for (OTNode c = getLastChild(); c!=null; c= c.prevSibling()) {
//			double cprelim = (Double) props.getPropertyValue(Prelim);
			double cprelim = c.getPrelim();
			cprelim += shift;
//		   props.setProperty(Prelim, cprelim);
			c.setPrelim(cprelim);

//			double dmod = (double) props.getPropertyValue(Mod);
			double dmod = c.getMod();
			dmod += shift;
//			properties(c).setProperty(Mod, dmod);
			c.setMod(dmod);

//			change += (Double) props.getPropertyValue(Change);
			change += c.getChange();
			shift += c.getShift() + change;

		}
		
	}

	private OTNode getLastChild() {
		return children.get(children.size()-1);
	}

	private OTNode apportion(OTNode a) {
		OTNode w = prevSibling();
		if (w != null) {
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
				vop.setAncestor(this);
				double shift = (vim.getPrelim() + sim) - (vip.getPrelim() + sip) + itemHeight;
				if (shift > 0) {
					OTNode newa = ancestor(vim, a);
					moveSubTree(newa, shift);
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
			if (nr != null && vop.nextRight()== null) {
				vop.setThread(nl);
				double m = vop.getMod();
				m+= sim-sop;
				vop.setMod(m);
			}
			if (nl != null && vom.nextLeft()==null) {
				vom.setThread(nl);
				double m = vom.getMod();
				m+= sip - som;
				vom.setMod(m);
				a = this;
			}

		}
		return a;
	}

	private void moveSubTree(OTNode wm, double shft) {
		int wpNumber = getNumber();
		int wmNumber = wm.getNumber();
		double subTrees = wpNumber - wmNumber;

		double wpChange = getChange();
		wpChange -= shft / subTrees;
		// properties(wp).setProperty(Change, wpChange);
		setChange(wpChange);

//		double wpShift = (Double) properties(wp).getPropertyValue(Shift);
		double wpShift = getShift();
		wpShift += shft;
		// properties(wp).setProperty(Shift, wpShift);
		setShift(wpShift);

		// double wmChange = (Double) properties(wm).getPropertyValue(Change);
		double wmChange = wm.getChange();
		wmChange += shft / subTrees;
		// properties(wm).setProperty(Change, wmChange);
		wm.setChange(wmChange);

//		double wpPrelim = (Double) properties(wp).getPropertyValue(Prelim);
		double wpPrelim = getPrelim();
		wpPrelim += shft;
//		properties(wp).setProperty(Prelim, wpPrelim);
		setPrelim(wpPrelim);

//		double wpMod = (Double) properties(wp).getPropertyValue(Mod);
		double wpMod = getMod();
		wpMod += shft;
		// properties(wp).setProperty(Mod, wpMod);
		setMod(wpMod);
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

	private OTNode nextLeft() {
		if (!children.isEmpty())
			return children.get(0);
		return getThread();
	}

	private OTNode nextRight() {
		if (!children.isEmpty()) {
			return children.get(children.size() - 1);
		} else
			return getThread();
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

	public void secondWalk(OTNode p, double m, int depth) {
		double y = getPrelim()+m;
		double x = OTNode.m_depths[depth];
		getvNode().setX(x);
		getvNode().setY(y);
		depth+=1;
		if (!children.isEmpty()) {
			for (OTNode c= getFirstChild();c!=null; c= c.nextSibling()) {
				c.secondWalk(this,m+getMod(),depth);
			}
		}	
	}

	private OTNode nextSibling() {
		OTNode parent = getParent();
		if (parent!=null) {
			int idx = parent.getChildren().indexOf(this);
			idx++;
			if (idx<parent.getChildren().size())
				return parent.getChildren().get(idx);
		}
		return null;
	}

	private OTNode getFirstChild() {
		return children.get(0);
	}

}
