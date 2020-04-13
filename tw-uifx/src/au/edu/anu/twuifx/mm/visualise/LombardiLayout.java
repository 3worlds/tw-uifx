package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.List;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.impl.TreeGraph;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * @author Ian Davies
 *
 * @date 13 Apr 2020
 */
public class LombardiLayout implements ILayout {

	private List<LombardiNode> lnodes;

	public LombardiLayout(TreeGraph<VisualNode, VisualEdge> graph) {
		lnodes = new ArrayList<>();
		// collect all visible nodes
		for (VisualNode v : graph.nodes())
			if (!v.isCollapsed())
				lnodes.add(new LombardiNode(v));

		for (LombardiNode ln : lnodes) {
			// add children as "outNodes"
			VisualNode vn = ln.getNode();
			for (VisualNode cn : vn.getChildren())
				if (!cn.isCollapsed()) {
					LombardiNode cln = vn2ln(cn);
					ln.addToNode(cln);
				}

			List<VisualNode> toNodes = (List<VisualNode>) get(vn.edges(Direction.OUT), edgeListEndNodes());
			for (VisualNode toNode : toNodes)
				if (!toNode.isCollapsed()) {
					LombardiNode toln = vn2ln(toNode);
					ln.addToNode(toln);
				}
		}
	}

	private LombardiNode vn2ln(VisualNode cn) {
		for (LombardiNode ln : lnodes)
			if (ln.getNode().id().equals(cn.id()))
				return ln;
		return null;
	}

	@Override
	public ILayout compute() {
		// TODO Auto-generated method stub
		return null;
	}

}
