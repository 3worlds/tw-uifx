package au.edu.anu.twuifx.mm.editors.structure;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twcore.archetype.tw.CheckSubArchetypeQuery;
import au.edu.anu.twcore.archetype.tw.IsInValueSetQuery;
import au.edu.anu.twcore.archetype.tw.NameStartsWithUpperCaseQuery;
import fr.cnrs.iees.graph.Tree;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.graph.io.GraphImporter;
import fr.cnrs.iees.identity.impl.PairIdentity;

import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

public class TwSpecifications implements //
		Specifications, //
		ArchetypeArchetypeConstants, //
		TwArchetypeConstants {

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

	@SuppressWarnings("unchecked")
	@Override
	public SimpleDataTreeNode getSpecificationOf(TreeNode root,String createdBy, TreeGraphNode configNode) {	
		//String className = configNode.getClass().getName();
		for (TreeNode child : root.getChildren()) {
			if (isOfClass((SimpleDataTreeNode) child,TWA.getLabel(configNode.id()))) {
				if (createdBy==null)
					return (SimpleDataTreeNode) child;
				if (parentTableContains((SimpleDataTreeNode) child,createdBy))
					return (SimpleDataTreeNode) child;
			}
			// search sa.
			// TODO check for TimeModel look for Sto
			List<SimpleDataTreeNode> saConstraints = (List<SimpleDataTreeNode>) get(child.getChildren(),
					selectZeroOrMany(hasProperty(twaClassName, CheckSubArchetypeQuery.class.getName())));
			for (SimpleDataTreeNode constraint: saConstraints) {
				List<String> pars = getConstraintTable(constraint);
				Tree<?> tree = (Tree<?>)GraphImporter.importGraph(pars.get(2),CheckSubArchetypeQuery.class);
				SimpleDataTreeNode result = getSpecificationOf(tree.root(),createdBy,configNode);
				if (result!=null)
					return result;
			}
		}

//		List<SimpleDataTreeNode> lst = (List<SimpleDataTreeNode>) get(aroot.getChildren(),
//				selectOneOrMany(hasProperty(aaIsOfClass, TWA.getLabel(configNode.id()))));
//		if (lst.size() == 1)
//			return lst.get(0);
//
//		for (SimpleDataTreeNode node : lst) {
//			if (parentTableContains(node, createdBy))
//				return node;
//		}
		return null;
	}

	private boolean isOfClass(SimpleDataTreeNode child, String label) {
		String ioc = (String) child.properties().getPropertyValue(aaIsOfClass);
		return ioc.equals(label);
	}

	@Override
	public String getSubClass(String configClass, SimpleDataTreeNode spec) {
		SimpleDataTreeNode propertySpec = (SimpleDataTreeNode) get(spec.getChildren(),
				selectZeroOrOne(hasProperty(twaHasName, twaSubClass)));
		if (propertySpec != null) {
			SimpleDataTreeNode constraint = (SimpleDataTreeNode) get(propertySpec.getChildren(),
					selectOne(hasProperty(twaClassName, IsInValueSetQuery.class.getName())));
			StringTable classes = (StringTable) constraint.properties().getPropertyValue(twaValues);
			if (classes.contains(configClass))
				return configClass;
		}
		return null;
	}

	private static boolean parentTableContains(SimpleDataTreeNode node, String createdBy) {
		StringTable st = (StringTable) node.properties().getPropertyValue(aaHasParent);
		return st.contains(createdBy + PairIdentity.LABEL_NAME_STR_SEPARATOR);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<SimpleDataTreeNode> getChildSpecificationsOf(TreeNode root,SimpleDataTreeNode parentSpec) {
		String parentLabel = (String) parentSpec.properties().getPropertyValue(aaIsOfClass);
		List<SimpleDataTreeNode> children = (List<SimpleDataTreeNode>) get(root.getChildren(),
				selectZeroOrMany(hasProperty(aaHasParent)));
		List<SimpleDataTreeNode> result = new ArrayList<>();
		for (SimpleDataTreeNode child : children) {
			StringTable t = (StringTable) child.properties().getPropertyValue(aaHasParent);
			if (t.contains(parentLabel + PairIdentity.LABEL_NAME_SEPARATOR))
				result.add(child);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<SimpleDataTreeNode> getEdgeSpecificationsOf(SimpleDataTreeNode nodeSpec) {
		return (Iterable<SimpleDataTreeNode>) get(nodeSpec.getChildren(), selectZeroOrMany(hasTheLabel(aaHasEdge)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<SimpleDataTreeNode> getPropertySpecifications(SimpleDataTreeNode spec) {
		return (Iterable<SimpleDataTreeNode>) get(spec.getChildren(), selectZeroOrMany(hasTheLabel(aaHasProperty)));
	}

	@Override
	public IntegerRange getMultiplicity(SimpleDataTreeNode spec, String key) {
		// TODO Auto-generated method stub - do we need this???
		return null;
	}

	@Override
	public IntegerRange getMultiplicity(SimpleDataTreeNode spec) {
		return (IntegerRange) spec.properties().getPropertyValue(aaMultiplicity);
	}

	@Override
	public boolean nameStartsWithUpperCase(SimpleDataTreeNode spec) {
		return getConstraint(spec, NameStartsWithUpperCaseQuery.class.getName()) != null;
	}

	@Override
	public String getLabel(TreeNode spec) {
		System.out.println(spec.id());
		return null;
	}

	private List<String> getConstraintTable(SimpleDataTreeNode constraint) {
		List<String> result = new ArrayList<>();
		if (constraint != null) {
			for (String key : constraint.properties().getKeysAsArray()) {
				if (constraint.properties().getPropertyValue(key) instanceof StringTable) {
					StringTable t = (StringTable) constraint.properties().getPropertyValue(key);
					for (int i = 0; i < t.size(); i++)
						result.add(t.getWithFlatIndex(i));
				}
			}
		}
		return result;
	}

	private SimpleDataTreeNode getConstraint(SimpleDataTreeNode spec, String constraintClass) {
		return (SimpleDataTreeNode) get(spec.getChildren(),
				selectZeroOrOne(andQuery(hasTheLabel(aaMustSatisfyQuery), hasProperty(twaClassName, constraintClass))));
	}

	@Override
	public List<String> getConstraintOptions(SimpleDataTreeNode spec, String constraintClass) {
		SimpleDataTreeNode constraint = getConstraint(spec, constraintClass);
		return getConstraintTable(constraint);
	}

	@Override
	public String getEdgeToNodeLabel(SimpleDataTreeNode edgeSpec) {
		String result = (String) edgeSpec.properties().getPropertyValue(twaToNode);
		result = result.replace(PairIdentity.LABEL_NAME_STR_SEPARATOR, "");
		return result;
	}
// end of implementation methods

}
