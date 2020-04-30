package au.edu.anu.twuifx.mm.visualise.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 30 Apr 2020
 */
public class TreeVertexAdapter extends VertexAdapter implements ITreeVertex<TreeVertexAdapter> {
	private List<TreeVertexAdapter> _children;
	private TreeVertexAdapter _parent;

	public TreeVertexAdapter(TreeVertexAdapter parent, VisualNode vNode) {
		super(vNode);
		this._parent = parent;
		this._children = new ArrayList<>();
	}

	@Override
	public boolean isLeaf() {
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

}
