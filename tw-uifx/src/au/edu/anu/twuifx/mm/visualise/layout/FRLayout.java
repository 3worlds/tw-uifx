/**************************************************************************
 *  TW-APPS - Applications used by 3Worlds                                *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-APPS contains ModelMaker and ModelRunner, programs used to         *
 *  construct and run 3Worlds configuration graphs. All code herein is    *
 *  independent of UI implementation.                                     *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-APPS (3Worlds applications).                  *
 *                                                                        *
 *  TW-APPS is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-APPS is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-APPS.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
  **************************************************************************/

package au.edu.anu.twuifx.mm.visualise.layout;

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
import fr.cnrs.iees.graph.Edge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.ens.biologie.generic.utils.Duple;

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

	private List<FRVertex> vertices;
	private List<Duple<FRVertex, FRVertex>> edges;
	/* vertices excluded from the alg. These are lined up on the RH side. */
	private List<FRVertex> isolated;

	public FRLayout(TreeGraph<VisualNode, VisualEdge> graph, boolean pcShowing, boolean xlShowing, boolean sideline) {
		vertices = new ArrayList<>();
		edges = new ArrayList<>();
		isolated = new ArrayList<>();
		/* make vertices of all visible nodes */
		for (VisualNode v : graph.nodes()) {
			if (!v.isCollapsed() && v.isVisible()) {
				vertices.add(new FRVertex(v));
			}
		}
		/* sort for predictability */
		vertices.sort(new Comparator<FRVertex>() {

			@Override
			public int compare(FRVertex o1, FRVertex o2) {
				return o1.id().compareTo(o2.id());
			}

		});

		/* collect all visible edges */
		for (FRVertex v : vertices) {
			// add parent/children edges
			VisualNode vn = v.getNode();
			if (pcShowing)
				for (VisualNode cn : vn.getChildren())
					if (!cn.isCollapsed() && cn.isVisible()) {
						FRVertex u = Node2Vertex(cn);
						edges.add(new Duple<FRVertex, FRVertex>(v, u));
						v.setHasEdge(true);
						u.setHasEdge(true);
					}

			// add xlink edges
			if (xlShowing) {
				for (Edge e : vn.edges(Direction.OUT)) {
					VisualEdge ve = (VisualEdge) e;
					if (ve.isVisible()) {
						VisualNode endNode = (VisualNode) ve.endNode();
						if (!endNode.isCollapsed() && endNode.isVisible()) {
							FRVertex u = Node2Vertex(endNode);
							edges.add(new Duple<FRVertex, FRVertex>(v, u));
							v.setHasEdge(true);
							u.setHasEdge(true);
						}
					}
				}
			}
		}

		// remove isolated vertices
		if (sideline) {
			for (FRVertex v : vertices)
				if (!v.hasEdges())
					isolated.add(v);

			for (FRVertex v : isolated)
				vertices.remove(v);
		}
	}

	private FRVertex Node2Vertex(VisualNode vn) {
		for (FRVertex v : vertices)
			if (v.getNode().id().equals(vn.id()))
				return v;
		return null;
	}

	@Override
	public ILayout compute(double jitter) {
		final int interations = 600;
		/* ideal spring length */
		final double k = Math.sqrt(1.0 / vertices.size());
		/* initial temperature */
		final double t0 = 0.1;

		double t = t0; // set initial temperature
		for (int i = 0; i < interations; i++) {
			for (int a = 0; a < vertices.size(); a++) {
				FRVertex v = vertices.get(a);
				for (int b = a + 1; b < vertices.size(); b++) {
					FRVertex u = vertices.get(b);
					/* double force = */v.setRepulsionDisplacement(u, k);
				}
			}

			for (Duple<FRVertex, FRVertex> e : edges) {
				/* double force = */ e.getFirst().setAttractionDisplacement(e.getSecond(), k);
			}

			/* double energy = 0; */
			for (FRVertex v : vertices) {
				/* energy += */v.displace(t);
			}

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

	// linear cooling
	public static double cool(double ti, double i, double t0, double m) {
		return Math.max(0.0, ti - t0 * 1.0 / m);
	}

}
