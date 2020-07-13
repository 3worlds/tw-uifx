package au.edu.anu.twuifx.mm.view;

import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
import java.util.List;

import au.edu.anu.twcore.project.Project;

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * @author Ian Davies
 *
 * @date 11 Jul 2020
 */
public class ConfigInfo {

	// NB: assumes the graph is a valid configuration!!
	/**
	 * Logistic 1.
	 * 
	 * Nodes: 45
	 * 
	 * Edges: 55
	 * 
	 * Drivers: 1
	 * 
	 * Constants: 1
	 * 
	 * The number of editable properties may be some measure of node/edge
	 * complexity?
	 * 
	 */

	private int nNodes;
	private int nEdges;
	private int nCnts;
	private int nDrvs;
	private int nProps;
	private int nCT;
	
	private TreeGraph<TreeGraphDataNode, ALEdge> cfg;

	private static int baseNodes = 45;
	private static int baseEdges = 55;
	private static int baseDrvs = 1;
	private static int baseCnts = 1;
	private static int baseProps = 68;

	public ConfigInfo(TreeGraph<TreeGraphDataNode, ALEdge> cfg) {
		this.cfg=cfg;
		this.nNodes = cfg.nNodes();
		this.nEdges = nNodes - 1;
		for (TreeGraphDataNode n : cfg.nodes()) {
			nProps += n.properties().size();
			for (ALEdge e : n.edges(Direction.OUT)) {
				this.nEdges++;
				if (e instanceof ALDataEdge)
					nProps+= ((ALDataEdge)e).properties().size();
				}
		}
		TreeGraphDataNode dDef = (TreeGraphDataNode) get(cfg.root().getChildren(),
				selectOne(hasTheLabel(N_DATADEFINITION.label())));
		for (TreeGraphDataNode n : cfg.subTree(dDef)) {
			if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_DRIVERS.label()))) != null)
				nDrvs += getDimensions(n);
			else if (get(n.edges(Direction.IN), selectZeroOrOne(hasTheLabel(E_CONSTANTS.label()))) != null)
				nCnts += getDimensions(n);
		}
	}

	// this must have been done somewhere already!
	private static int getDimensions(TreeNode rec) {
		int res = 0;
		for (TreeNode n : rec.getChildren()) {
			if (n.classId().equals(N_FIELD.label()))
				res++;
			if (n.classId().equals(N_TABLE.label())) {
				res += getTableDims(n);
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	private static int getTableDims(TreeNode n) {
		List<TreeGraphDataNode> dims = (List<TreeGraphDataNode>) get(n.edges(Direction.OUT), edgeListEndNodes(),
				selectOneOrMany(hasTheLabel(N_DIMENSIONER.label())));
		int result = 1;
		for (TreeGraphDataNode dim : dims) {
			int s = (Integer) dim.properties().getPropertyValue(P_DIMENSIONER_SIZE.key());
			result *= s;
		}
		if (n.hasChildren())
			result += getDimensions(n.getChildren().iterator().next());
		return result;
	}

	public String metricsToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#Nodes:\t").append(nNodes - baseNodes).append("\n");
		sb.append("#Edges:\t").append(nEdges - baseEdges).append("\n");
		sb.append("#Const:\t").append(nCnts - baseCnts).append("\n");
		sb.append("#Drvs:\t").append(nDrvs - baseDrvs).append("\n");
		sb.append("#Props:\t").append(nProps - baseProps).append("\n");		
		return sb.toString();
	}
	
	// some kind of viewable odt document
	public String ODDToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("====*3Worlds*: ");
		sb.append(Project.getDisplayName()).append("\n");
		// version
	// authors
		// contacts
		sb.append("Overview, Design concepts and Details\n");
		sb.append("Purpose\n");
		sb.append(cfg.root().properties().getPropertyValue(P_MODEL_PRECIS.key())+"\n");
		
		
		// citations
		return sb.toString();
	}
	
	// some kind of odg document
	public String flowChartToString() {
		StringBuilder sb = new StringBuilder();
		// build hierarchical thingo
		
		return sb.toString();
	}

}
