package au.edu.anu.twuifx.mm.visualise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import au.edu.anu.omhtk.rng.Pcg32;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.ens.biologie.generic.utils.Duple;

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.N_ROOT;

/**
 * @author Ian Davies
 *
 * @date 13 Apr 2020
 */
public class LmbLayout implements ILayout {

	private List<LmbNode> lNodes;
	private List<Duple<LmbNode,LmbNode>> lEdges;
	private Random rnd ;

	public LmbLayout(TreeGraph<VisualNode, VisualEdge> graph) {
		lNodes = new ArrayList<>();
		lEdges = new ArrayList<>();
		rnd =  new Pcg32();
		// collect all visible nodes
		VisualNode twRoot = null;
		for (VisualNode v : graph.nodes()) {
			if (v.getConfigNode().classId().equals(N_ROOT.label()))
				twRoot = v;
			if (!v.isCollapsed())
				lNodes.add(new LmbNode(v));
		}
		// set links
		for (LmbNode ln : lNodes) {
			// add children as "toNodes"
			VisualNode vn = ln.getNode();
			for (VisualNode cn : vn.getChildren())
				if (!cn.isCollapsed()) {
					LmbNode cln = vn2ln(cn);
					//ln.addToNode(cln);
					lEdges.add(new Duple<LmbNode,LmbNode>(ln,cln));
					
				}

			// add out edge end nodes as "toNodes"
			List<VisualNode> toNodes = (List<VisualNode>) get(vn.edges(Direction.OUT), edgeListEndNodes());
			for (VisualNode toNode : toNodes)
				if (!toNode.isCollapsed()) {
					LmbNode toln = vn2ln(toNode);
					//ln.addToNode(toln);
					lEdges.add(new Duple<LmbNode,LmbNode>(ln,toln));
				}
		}
		/**
		 * Initialise to some standard starting state to make this process effectively
		 * deterministic.
		 */
//		System.out.println("N\t"+lNodes.size()+"\tE\t"+lEdges.size());
//		ILayout radialLayout = new PCTreeLayout(twRoot);
//		radialLayout.compute();
	}

	private LmbNode vn2ln(VisualNode cn) {
		for (LmbNode ln : lNodes)
			if (ln.getNode().id().equals(cn.id()))
				return ln;
		return null;
	}

	private int interations = 400;
	private double initTemp = 0.1;
	

	@Override
	public ILayout compute() {
		// ideal spring length
		final double k = Math.sqrt(1.0/lNodes.size());
		double t=initTemp; // temperature;
		for (int i = 0; i < interations; i++) {
			// repulsion between all nodes : could be optimized?
			for (LmbNode v : lNodes) {
				
				for (LmbNode u : lNodes)
					if (!v.equals(u)) {
						v.repulsionDisplacement(u, k);
					}
			}
			for (Duple<LmbNode,LmbNode> e: lEdges) {
				e.getFirst().attractionDisplacement(e.getSecond(),k);
				e.getSecond().attractionDisplacement(e.getFirst(), k);
			}
			//limit max disp to temperature and frame bounds;
			for (LmbNode v: lNodes) 
				v.displace(t,rnd);
			// lower the temperature
			t = cool(t,i,initTemp,interations);
			
		}

		return null;
	}
	private double cool(double ct, double i, double t0,double max) {
		return Math.max(0.0, ct-t0*1.0/max);
	}

}
