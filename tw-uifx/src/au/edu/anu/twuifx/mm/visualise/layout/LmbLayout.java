package au.edu.anu.twuifx.mm.visualise.layout;

import static au.edu.anu.rscs.aot.queries.CoreQueries.edgeListEndNodes;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.get;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import au.edu.anu.omhtk.rng.Pcg32;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.ens.biologie.generic.utils.Duple;

public class LmbLayout implements ILayout{
	private List<LmbVertex> vertices;
	private List<Duple<LmbVertex, LmbVertex>> edges;
	private List<LmbVertex> isolated;
	private int interations = 600;	
	
	public LmbLayout(TreeGraph<VisualNode, VisualEdge> graph, boolean usePCEdges, boolean useXEdges) {
		vertices = new ArrayList<>();
		edges = new ArrayList<>();
		isolated = new ArrayList<>();
		/* make vertices of all visible nodes */
		for (VisualNode v : graph.nodes()) {
			if (!v.isCollapsed()) {
				vertices.add(new LmbVertex(v));
			}
		}
		/* sort for predictability */
		vertices.sort(new Comparator<IVertex>() {

			@Override
			public int compare(IVertex v1, IVertex v2) {
				return v1.id().compareTo(v2.id());
			}

		});

		/* collect all visible edges */
		for (LmbVertex v : vertices) {
			// add parent/children edges
			VisualNode vn = v.getNode();
			if (usePCEdges)
				for (VisualNode cn : vn.getChildren())
					if (!cn.isCollapsed()) {
						LmbVertex u =  (LmbVertex) Node2Vertex(cn);
						edges.add(new Duple<LmbVertex, LmbVertex>(v, u));
						v.addNeighbour(u);
						u.addNeighbour(v);
					}

			// add xlink edges
			if (useXEdges) {
				@SuppressWarnings("unchecked")
				List<VisualNode> toNodes = (List<VisualNode>) get(vn.edges(Direction.OUT), edgeListEndNodes());
				for (VisualNode toNode : toNodes)
					if (!toNode.isCollapsed()) {
						LmbVertex u = (LmbVertex) Node2Vertex(toNode);
						edges.add(new Duple<LmbVertex, LmbVertex>(v, u));
						v.addNeighbour(u);
						u.addNeighbour(v);
					}
			}
		}
		
		// remove isolated vertices
		for (LmbVertex v : vertices)
			if (!v.hasEdges())
				isolated.add(v);

		for (LmbVertex v : isolated)
			vertices.remove(v);


	}
	private IVertex Node2Vertex(VisualNode vn) {
		for (IVertex v : vertices)
			if (v.getNode().id().equals(vn.id()))
				return v;
		return null;
	}

	@Override
	public ILayout compute(double jitter) {
		final double k = Math.sqrt(1.0 / vertices.size());
		final double t0 = 0.1;
		final double a = 0.9;
		final double b = 0.5;

		double t = t0; // temperature;
		for (int i = 0; i < interations; i++) {

			for (LmbVertex v : vertices) {
				v.clearDisplacement();
				for (LmbVertex u : vertices)
					if (!v.equals(u)) 
						v.setRepulsionDisplacement(u, k);
			}

			for (Duple<LmbVertex, LmbVertex> e : edges) {
				e.getFirst().setAttractionDisplacement(e.getSecond(), k);
				e.getSecond().setAttractionDisplacement(e.getFirst(), k);
				e.getFirst().setRotationalForce(e.getSecond(),b);
				e.getSecond().setRotationalForce(e.getFirst(),b);
				e.getFirst().setTangentialDisplacement(e.getSecond(),a);
				e.getSecond().setTangentialDisplacement(e.getFirst(),a);
			}

			double energy = 0;
			for (LmbVertex v : vertices)
				energy += v.displace(t);

			// lower the temperature
			t = cool(t, i, t0, interations);
	}
		if (jitter > 0.0) {
			Random rnd = new Pcg32();
			for (IVertex v : vertices)
				v.jitter(jitter, rnd);
		}

		Point2D min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point2D max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		for (IVertex v : vertices)
			v.getLayoutBounds(min, max);

		for (IVertex v : vertices)
			v.normalise(ILayout.getBoundingFrame(min, max), ILayout.getFittingFrame());

		// Arrange isolated nodes down the RHS
		for (int i = 0; i < isolated.size(); i++) {
			IVertex v = isolated.get(i);
			v.setLocation(1.07, (double) i / (double) isolated.size());
		}
		return this;
}
	private double cool(double ti, double i, double t0, double m) {
		return Math.max(0.0, ti - t0 * 1.0 / m);
	}
}
