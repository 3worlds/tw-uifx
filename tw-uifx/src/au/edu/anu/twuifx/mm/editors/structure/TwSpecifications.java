package au.edu.anu.twuifx.mm.editors.structure;

import java.util.List;

import au.edu.anu.rscs.aot.queries.CoreQueries;
import au.edu.anu.rscs.aot.queries.base.SequenceQuery;
import au.edu.anu.rscs.aot.util.IntegerRange;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;

public class TwSpecifications implements //
		Specifications, //
		ArchetypeArchetypeConstants//
{
	private static final TreeNode aroot = TWA.getInstance().root();

	@Override
	public boolean complies() {
		// TODO Auto-generated method stub ??
		return false;
	}

	@Override
	public boolean complies(TreeGraphNode configNode, TreeNode spec) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TreeNode getSpecificationOf(TreeGraphNode configNode) {
		TreeNode result = (TreeNode)SequenceQuery.get(aroot.getChildren(),
				selectOne(CoreQueries.hasProperty(aaIsOfClass, TWA.getLabel(configNode.id()))));
		return result;
	}

	@Override
	public Iterable<TreeNode> getChildSpecificationsOf(TreeNode parentSpec) {
		SimpleDataTreeNode ps = (SimpleDataTreeNode) parentSpec;
		
		String className = (String) ps.properties().getPropertyValue(aaIsOfClass);
		// find all specs which list this isOfClass in hasParentTables
		System.out.println("GetChildSpecificationof()");
		return null;
	}

	@Override
	public Iterable<TreeNode> getEdgeSpecificationsOf(String parentLabel, TreeNode parentSpec, String parentClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<TreeNode> getPropertySpecifications(TreeNode spec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntegerRange getMultiplicity(TreeNode spec, String key) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public IntegerRange getMultiplicity(TreeNode spec) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public boolean nameStartsWithUpperCase(TreeNode spec) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLabel(TreeNode root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getConstraintOptions(TreeNode spec, String constraintClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEdgeToNodeLabel(TreeNode edgeSpec) {
		// TODO Auto-generated method stub
		return null;
	}
// end of implementation methods
}
