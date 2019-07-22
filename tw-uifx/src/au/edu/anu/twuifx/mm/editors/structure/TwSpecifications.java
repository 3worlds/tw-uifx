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
	public boolean complies(TreeGraphNode configNode, SimpleDataTreeNode spec) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SimpleDataTreeNode getSpecificationOf(TreeGraphNode configNode) {
		return  (SimpleDataTreeNode)SequenceQuery.get(aroot.getChildren(),
				selectOne(CoreQueries.hasProperty(aaIsOfClass, TWA.getLabel(configNode.id()))));
	}

	@Override
	public Iterable<SimpleDataTreeNode> getChildSpecificationsOf(SimpleDataTreeNode parentSpec) {		
		String className = (String) parentSpec.properties().getPropertyValue(aaIsOfClass);
		System.out.println("Children of label: "+className);
		// find all specs which list this isOfClass in hasParentTables

		return null;
	}

	@Override
	public Iterable<SimpleDataTreeNode> getEdgeSpecificationsOf(String parentLabel, SimpleDataTreeNode parentSpec, String parentClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<SimpleDataTreeNode> getPropertySpecifications(SimpleDataTreeNode spec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntegerRange getMultiplicity(SimpleDataTreeNode spec, String key) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public IntegerRange getMultiplicity(TreeNode spec) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public boolean nameStartsWithUpperCase(SimpleDataTreeNode spec) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLabel(TreeNode root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getConstraintOptions(SimpleDataTreeNode spec, String constraintClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEdgeToNodeLabel(SimpleDataTreeNode edgeSpec) {
		// TODO Auto-generated method stub
		return null;
	}
// end of implementation methods
}
