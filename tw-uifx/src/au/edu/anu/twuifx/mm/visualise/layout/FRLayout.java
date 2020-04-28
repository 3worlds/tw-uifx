package au.edu.anu.twuifx.mm.visualise.layout;

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

	private List<FRVertex> lNodes;
	private List<Duple<FRVertex, FRVertex>> lEdges;
	private int interations = 100;
	private double initTemp = 0.1;

	/** It would be nice to move nodes without edges out of the way? */
	public FRLayout(TreeGraph<VisualNode, VisualEdge> graph, boolean usePCEdges, boolean useXEdges) {
		lNodes = new ArrayList<>();
		lEdges = new ArrayList<>();
		// collect all visible nodes
		for (VisualNode v : graph.nodes()) {
			if (!v.isCollapsed()) {
				lNodes.add(new FRVertex(v));
			}
		}
		lNodes.sort(new Comparator<FRVertex>() {

			@Override
			public int compare(FRVertex o1, FRVertex o2) {
				return o1.id().compareTo(o2.id());
			}

		});

		// set edges
		for (FRVertex ln : lNodes) {
			// add parent/children edges
			VisualNode vn = ln.getNode();
			if (usePCEdges)
				for (VisualNode cn : vn.getChildren())
					if (!cn.isCollapsed()) {
						FRVertex cln = vn2ln(cn);
						lEdges.add(new Duple<FRVertex, FRVertex>(ln, cln));
						ln.hasEdge();
						cln.hasEdge();
					}

			// add xlink edges
			if (useXEdges) {
				@SuppressWarnings("unchecked")
				List<VisualNode> toNodes = (List<VisualNode>) get(vn.edges(Direction.OUT), edgeListEndNodes());
				for (VisualNode toNode : toNodes)
					if (!toNode.isCollapsed()) {
						FRVertex toln = vn2ln(toNode);
						lEdges.add(new Duple<FRVertex, FRVertex>(ln, toln));
						ln.hasEdge();
						toln.hasEdge();
					}
			}
		}
		List<FRVertex> isolatedVertices = new ArrayList<>();
		for (FRVertex v:lNodes) {
			if (!v.hasEdges())
				isolatedVertices.add(v);
		}
		for (FRVertex v:isolatedVertices) {
			lNodes.remove(v);
		}
		double inc = 360.0/isolatedVertices.size();
		double angle = 0;
		double d = Math.PI;
		for (FRVertex v:isolatedVertices) {
			double x = d*Math.cos(Math.toRadians(angle));
			double y = d*Math.sin(Math.toRadians(angle));
			v.setPosition(x, y);
			angle+=inc;		
		}
		
	}

	private FRVertex vn2ln(VisualNode cn) {
		for (FRVertex ln : lNodes)
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

			for (FRVertex v : lNodes) {
				v.clearDisplacement();
				for (FRVertex u : lNodes)
					if (!v.equals(u))
						v.setRepulsionDisplacement(u, k);
			}

			for (Duple<FRVertex, FRVertex> e : lEdges) {
				e.getFirst().setAttractionDisplacement(e.getSecond(), k);
				e.getSecond().setAttractionDisplacement(e.getFirst(), k);
			}

			for (FRVertex v : lNodes)
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
