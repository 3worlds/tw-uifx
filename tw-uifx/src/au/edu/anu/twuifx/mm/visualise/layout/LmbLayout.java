package au.edu.anu.twuifx.mm.visualise.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import au.edu.anu.omhtk.rng.Pcg32;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.Edge;
import fr.cnrs.iees.graph.impl.TreeGraph;

/**
 * @author Ian Davies
 *
 * @date 25 May 2020
 */

// NOTE : WIP
public class LmbLayout implements ILayout {
	private List<LmbVertex> vertices;
	private List<LmbEdge> edges;
	private List<LmbVertex> isolated;
	// from / to
	private Map<LmbVertex, Map<LmbVertex, LmbEdge>> adjMat;

	public LmbLayout(TreeGraph<VisualNode, VisualEdge> graph, boolean pcShowing, boolean xlShowing, boolean sideline) {
		vertices = new ArrayList<>();
		edges = new ArrayList<>();
		adjMat = new HashMap<>();
		isolated = new ArrayList<>();
		/* make vertices of all visible nodes */
		for (VisualNode v : graph.nodes()) {
			if (!v.isCollapsed() && v.isVisible()) {
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
			if (pcShowing)
				for (VisualNode cn : vn.getChildren())
					if (!cn.isCollapsed() && cn.isVisible()) {
						LmbVertex u = (LmbVertex) Node2Vertex(cn);
						LmbEdge e = new LmbEdge(v, u);
						edges.add(e);
						Map<LmbVertex, LmbEdge> adjMap1 = addAdjMat(e, v, u);
						v.addNeighbour(adjMap1);
						Map<LmbVertex, LmbEdge> adjMap2 = addAdjMat(e, u, v);
						u.addNeighbour(adjMap2);
					}

			// add xlink edges
			if (xlShowing) {
				for (Edge e : vn.edges(Direction.OUT)) {
					VisualEdge ve = (VisualEdge) e;
					if (ve.isVisible()) {
						VisualNode endNode = (VisualNode) ve.endNode();
						if (!endNode.isCollapsed() && endNode.isVisible()) {
							LmbVertex u = (LmbVertex) Node2Vertex(endNode);
							LmbEdge le = new LmbEdge(v, u);
							edges.add(le);
							Map<LmbVertex, LmbEdge> adjMap1 = addAdjMat(le, v, u);
							v.addNeighbour(adjMap1);
							Map<LmbVertex, LmbEdge> adjMap2 = addAdjMat(le, u, v);
							u.addNeighbour(adjMap2);
						}
					}
				}
			}
		}

		for (LmbVertex v : vertices)
			v.init();

		for (LmbEdge e : edges)
			e.init();
		// remove isolated vertices
		if (sideline) {
			for (LmbVertex v : vertices)
				if (v.degree() == 0)
					isolated.add(v);

			for (LmbVertex v : isolated)
				vertices.remove(v);
		}

	}

	private Map<LmbVertex, LmbEdge> addAdjMat(LmbEdge e, LmbVertex n1, LmbVertex n2) {
		Map<LmbVertex, LmbEdge> result = adjMat.get(n1);
		if (result == null) {
			result = new HashMap<>();
			adjMat.put(n1, result);
		}
		result.put(n2, e);
		return result;
	}

	private IVertex Node2Vertex(VisualNode vn) {
		for (IVertex v : vertices)
			if (v.getNode().id().equals(vn.id()))
				return v;
		return null;
	}

	@Override
	public ILayout compute(double jitter) {
		/*
		 * max difference between two tangent diffs, for arc to be possible, in radians
		 */
		// double maxDiff = 0.1; used in setting control points at the end.
		/*
		 * rotational force constant, for how opp tangents affect my rotation (want my
		 * tan to match opp)
		 */
		double rfKopp = 0.5;
		/*
		 * rotational force constant, for how my tangents affect my rotation (want my
		 * tan close to my edge)
		 */
		double rfKadj = 0.1;
		/* tangential force constant */
		double tangentialK = 0.9;
		/* reserve a percent of iterations at the end just for my forces */
		double finalRound = 0.03;
		/* percentage of iterations to shuffle on */
		double shufflePercent = 0.4;
		/* #max number of shuffle attempts */
		int shuffleSamples = 20;

		final int interations = 600;
		/* ideal spring length */
		final double k = Math.sqrt(1.0 / vertices.size());
		/* initial temperature */
		final double t0 = 0.1;
		// #iteration mod to shuffle on
		int shuffleEvery = (int) Math.round(1.0 / shufflePercent);
		int maxDeterministicShuffle = 1;
		while (factorial(maxDeterministicShuffle + 1) < shuffleSamples)
			maxDeterministicShuffle += 1;
		double t = t0; // set initial temperature
		// Repulsion
//		boolean done = false;
		for (int i = 0; i < interations; i++) {
			for (int a = 0; a < vertices.size(); a++) {
				LmbVertex v = vertices.get(a);
				for (int b = a + 1; b < vertices.size(); b++) {
					LmbVertex u = vertices.get(b);
					/* double force = */v.setRepulsionDisplacement(u, k);
//					if (!done)System.out.println(v.id()+"<-->"+u.id());
				}
			}

			// Attraction
			for (LmbEdge e : edges) {
				/* double force = */e.setAttractionDisplacement(k);
//				if (!done)System.out.println(e.getP().id()+"-><-"+e.getQ().id());
			}
//			done = true;
			// shuffle edge order
			if (i % shuffleEvery == 0)
				for (LmbVertex v : vertices)
					v.shuffleTans(rfKopp, maxDeterministicShuffle, shuffleSamples);

			// Rotation
			for (LmbVertex v : vertices) {
				/* double force = */ v.rotationalDisplacement(rfKopp, rfKadj);
			}
			// Tangent forces
			for (LmbVertex v : vertices)
				v.trangentialDisplacement(tangentialK);

			for (LmbVertex v : vertices)
				v.updateAngle(t);

			// double energy = 0;
			for (LmbVertex v : vertices) {
				/* energy += */ v.displace(t);
			}

			t = FRLayout.cool(t, i, t0, interations);
		}

		for (int i = 0; i < (finalRound * interations); i++)
			finalStep();

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

	private boolean finalStep() {
		boolean legit = true;
		for (LmbEdge e : edges) {
			if (!e.finalStep())
				legit = false;
		}
		return legit;
	}

	private static int factorial(int num) {
		int result = 1;
		for (int i = 1; i < num; i++)
			result *= (i + 1);
		return result;
	}

//	private static double cool(double ti, double i, double t0, double m) {
//		return Math.max(0.0, ti - t0 * 1.0 / m);
//	}
	public static void main(String[] args) {
	}
}
