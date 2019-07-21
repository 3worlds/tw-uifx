package au.edu.anu.twuifx.mm.editors.structure;

import java.util.List;

import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twcore.archetype.TwArchetype;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;

public class TwSpecifications implements Specifications{

	@Override
	public boolean complies() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean complies(TreeGraphNode node, TreeNode root) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TreeGraphNode getSpecificationOf(TreeGraphNode configNode) {
		System.out.println("classId(): "+configNode.classId()+"; id(): "+configNode.id());
		System.out.println(TwArchetype.getLabel(configNode.id()));

		return null;
	}

	@Override
	public Iterable<TreeGraphNode> getChildSpecificationsOf(String parentLabel, TreeGraphNode parentSpec,
			String parentClass) {
		System.out.println("GetChildSpecificationof()");
		return null;
	}

	@Override
	public Iterable<TreeGraphNode> getEdgeSpecificationsOf(String parentLabel, TreeGraphNode parentSpec,
			String parentClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<TreeGraphNode> getPropertySpecifications(TreeGraphNode root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntegerRange getMultiplicity(TreeGraphNode root, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntegerRange getMultiplicity(TreeGraphNode spec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean nameStartsWithUpperCase(TreeGraphNode root) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLabel(TreeGraphNode root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getConstraintOptions(TreeGraphNode root, String constraintClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEdgeToNodeLabel(TreeGraphNode edgeSpec) {
		// TODO Auto-generated method stub
		return null;
	}
// end of implementation methods
}
