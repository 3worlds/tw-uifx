package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.ens.biologie.generic.utils.Duple;

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * @author Ian Davies
 *
 * @date 13 Apr 2020
 */

/**
 * A force-directed layout similar to Fruchterman and Reingold but using force
 * equations from Lombardi layout approach. This is NOT the full Lombardi style.
 * Tangential and rotational forces are not used:
 * 
 * Chernobelskiy, R., Cunningham, K.I., Goodrich, M.T., Kobourov, S.G. and
 * Trott, L., 2011, September. Force-directed Lombardi-style graph drawing. In
 * International Symposium on Graph Drawing (pp. 320-331). Springer, Berlin,
 * Heidelberg.
 */

public class FRLayout implements ILayout {

	private List<FRNode> lNodes;
	private List<Duple<FRNode, FRNode>> lEdges;
	private int interations = 100;
	private double initTemp = 0.1;

	public FRLayout(TreeGraph<VisualNode, VisualEdge> graph, boolean usePCEdges,
			boolean useXEdges) {
	
		lNodes = new ArrayList<>();
		lEdges = new ArrayList<>();
		// collect all visible nodes
		for (VisualNode v : graph.nodes()) {
			if (!v.isCollapsed()) {
				lNodes.add(new FRNode(v));
			}
		}
		lNodes.sort(new Comparator<FRNode>() {

			@Override
			public int compare(FRNode o1, FRNode o2) {
				return o1.id().compareTo(o2.id());
			}

		});

		// set edges
		for (FRNode ln : lNodes) {
			// add parent/children edges
			VisualNode vn = ln.getNode();
			if (usePCEdges)
				for (VisualNode cn : vn.getChildren())
					if (!cn.isCollapsed()) {
						FRNode cln = vn2ln(cn);
						lEdges.add(new Duple<FRNode, FRNode>(ln, cln));
					}

			// add xlink edges
			if (useXEdges) {
				@SuppressWarnings("unchecked")
				List<VisualNode> toNodes = (List<VisualNode>) get(vn.edges(Direction.OUT), edgeListEndNodes());
				for (VisualNode toNode : toNodes)
					if (!toNode.isCollapsed()) {
						FRNode toln = vn2ln(toNode);
						lEdges.add(new Duple<FRNode, FRNode>(ln, toln));
					}
			}
		}
	}

	private FRNode vn2ln(VisualNode cn) {
		for (FRNode ln : lNodes)
			if (ln.getNode().id().equals(cn.id()))
				return ln;
		return null;
	}

	@Override
	public ILayout compute() {
		// ideal spring length
		final double k = Math.sqrt(1.0 / lNodes.size());
		double t = initTemp; // temperature;
		for (int i = 0; i < interations; i++) {

			for (FRNode v : lNodes) {
				v.clearDisplacement();
				for (FRNode u : lNodes)
					if (!v.equals(u))
						v.setRepulsionDisplacement(u, k);
			}

			for (Duple<FRNode, FRNode> e : lEdges) {
				e.getFirst().setAttractionDisplacement(e.getSecond(), k);
				e.getSecond().setAttractionDisplacement(e.getFirst(), k);
			}

			for (FRNode v : lNodes)
				v.displace(t);

			/**
			 * NOTE: with no edges (repulsive forces only) the layout, like the universe,
			 * expands for ever. However, this is too slow to reach large numbers. This
			 * increase does not accumulate over repeated application of the alg because the
			 * result is rescaled into a unit space at the end.
			 */

			// lower the temperature
			t = cool(t, i, initTemp, interations);

		}

		return null;
	}

	// linear cooling
	private double cool(double ct, double i, double t0, double max) {
		return Math.max(0.0, ct - t0 * 1.0 / max);
	}

}
