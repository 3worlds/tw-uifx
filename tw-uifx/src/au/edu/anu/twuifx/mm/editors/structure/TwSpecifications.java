package au.edu.anu.twuifx.mm.editors.structure;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twcore.archetype.tw.CheckSubArchetypeQuery;
import au.edu.anu.twcore.archetype.tw.ChildXorPropertyQuery;
import au.edu.anu.twcore.archetype.tw.IsInValueSetQuery;
import au.edu.anu.twcore.archetype.tw.NameStartsWithUpperCaseQuery;
import au.edu.anu.twuifx.exceptions.TwuifxException;
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
	public SimpleDataTreeNode getSpecsOf(TreeGraphNode configNode, String createdBy, TreeNode root) {
		for (TreeNode child : root.getChildren()) {
			if (isOfClass((SimpleDataTreeNode) child, TWA.getLabel(configNode.id()))) {
				if (createdBy == null)
					return (SimpleDataTreeNode) child;
				if (parentTableContains((SimpleDataTreeNode) child, createdBy))
					return (SimpleDataTreeNode) child;
			}
			// search sa.
			List<SimpleDataTreeNode> saConstraints = (List<SimpleDataTreeNode>) get(child.getChildren(),
					selectZeroOrMany(hasProperty(twaClassName, CheckSubArchetypeQuery.class.getName())));
			for (SimpleDataTreeNode constraint : saConstraints) {
				List<String> pars = getConstraintTable(constraint);
				Tree<?> tree = (Tree<?>) GraphImporter.importGraph(pars.get(2), CheckSubArchetypeQuery.class);
				SimpleDataTreeNode result = getSpecsOf(configNode, createdBy, tree.root());
				if (result != null)
					return result;
			}
		}
		return null;
	}

	@Override
	public SimpleDataTreeNode getSubSpecsOf(SimpleDataTreeNode baseSpecs, Class<? extends TreeGraphNode> subClass) {
		if (subClass != null) {
			Tree<?> subClassTree = getSubArchetype(baseSpecs, subClass);
			return (SimpleDataTreeNode) get(subClassTree.root().getChildren(),
					selectOne(hasProperty(aaIsOfClass, (String) baseSpecs.properties().getPropertyValue(aaIsOfClass))));
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<SimpleDataTreeNode> getChildSpecificationsOf(SimpleDataTreeNode parentSpec,
			SimpleDataTreeNode parentSubClass, TreeNode root) {
		String parentLabel = (String) parentSpec.properties().getPropertyValue(aaIsOfClass);
		List<SimpleDataTreeNode> children = (List<SimpleDataTreeNode>) get(root.getChildren(),
				selectZeroOrMany(hasProperty(aaHasParent)));
		// could have a query here for finding a parent in a parent Stringtable
		List<SimpleDataTreeNode> result = new ArrayList<>();
		addChildrenTo(result, parentLabel, children);
		if (parentSubClass != null) {
			// look for children in the subclass spec
			children = (List<SimpleDataTreeNode>) get(parentSubClass.getChildren(),
					selectZeroOrMany(hasProperty(aaHasParent)));
			addChildrenTo(result, parentLabel, children);
		}
		return result;
	}

	private void addChildrenTo(List<SimpleDataTreeNode> result, String parentLabel, List<SimpleDataTreeNode> children) {
		for (SimpleDataTreeNode child : children) {
			StringTable t = (StringTable) child.properties().getPropertyValue(aaHasParent);
			if (t.contains(parentLabel + PairIdentity.LABEL_NAME_SEPARATOR))
				result.add(child);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<SimpleDataTreeNode> getPropertySpecifications(SimpleDataTreeNode spec, SimpleDataTreeNode subSpec) {
		List<SimpleDataTreeNode> results = (List<SimpleDataTreeNode>) get(spec.getChildren(),
				selectZeroOrMany(hasTheLabel(aaHasProperty)));
		if (subSpec != null) {
			List<SimpleDataTreeNode> subList = (List<SimpleDataTreeNode>) get(subSpec.getChildren(),
					selectZeroOrMany(hasTheLabel(aaHasProperty)));
			results.addAll(subList);
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<SimpleDataTreeNode> getEdgeSpecificationsOf(SimpleDataTreeNode nodeSpec) {
		return (Iterable<SimpleDataTreeNode>) get(nodeSpec.getChildren(), selectZeroOrMany(hasTheLabel(aaHasEdge)));
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

	@Override
	public List<Class> getSubClasses(SimpleDataTreeNode spec) {
		List<Class> result = new ArrayList<>();
		SimpleDataTreeNode propertySpec = (SimpleDataTreeNode) get(spec.getChildren(),
				selectZeroOrOne(hasProperty(twaHasName, twaSubclass)));
		if (propertySpec != null) {
			SimpleDataTreeNode constraint = (SimpleDataTreeNode) get(propertySpec.getChildren(),
					selectOne(hasProperty(twaClassName, IsInValueSetQuery.class.getName())));
			StringTable classes = (StringTable) constraint.properties().getPropertyValue(twaValues);
			for (int i = 0; i < classes.size(); i++)
				try {
					result.add(Class.forName(classes.getWithFlatIndex(i)));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String[]> getQueryStringTables(SimpleDataTreeNode spec, Class<ChildXorPropertyQuery> klass) {
		List<String[]> result = new ArrayList<>();
		if (spec == null)
			return result;
		List<SimpleDataTreeNode> constraints = (List<SimpleDataTreeNode>) get(spec.getChildren(),
				selectZeroOrMany(hasProperty(twaClassName, klass.getName())));
		for (SimpleDataTreeNode constraint : constraints) {
			List<String> entries = getConstraintTable(constraint);
			if (!entries.isEmpty()) {
				String[] ss = new String[entries.size()];
				for (int i = 0; i < ss.length; i++)
					ss[i] = entries.get(i);
				result.add(ss);
			}
		}
		return result;
	}

	// -----------------------end of implementation methods-----------------------
	private List<String> getConstraintTable(SimpleDataTreeNode constraint) {
		List<String> result = new ArrayList<>();
		if (constraint == null)
			return result;
		for (String key : constraint.properties().getKeysAsArray()) {
			if (constraint.properties().getPropertyValue(key) instanceof StringTable) {
				StringTable t = (StringTable) constraint.properties().getPropertyValue(key);
				for (int i = 0; i < t.size(); i++)
					result.add(t.getWithFlatIndex(i));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Tree<?> getSubArchetype(SimpleDataTreeNode spec, Class subClass) {
		List<SimpleDataTreeNode> constraints = (List<SimpleDataTreeNode>) get(spec.getChildren(),
				selectOneOrMany(hasProperty(twaClassName, CheckSubArchetypeQuery.class.getName())));
		for (SimpleDataTreeNode constraint : constraints) {
			StringTable pars = (StringTable) constraint.properties().getPropertyValue(twaParameters);
			if (pars.getWithFlatIndex(1).equals(subClass.getName())) {
				return (Tree<?>) GraphImporter.importGraph(pars.get(2), CheckSubArchetypeQuery.class);
			}
		}
		throw new TwuifxException("Sub archetype graph not found for " + subClass.getName());
	}

	private SimpleDataTreeNode getConstraint(SimpleDataTreeNode spec, String constraintClass) {
		return (SimpleDataTreeNode) get(spec.getChildren(),
				selectZeroOrOne(andQuery(hasTheLabel(aaMustSatisfyQuery), hasProperty(twaClassName, constraintClass))));
	}

	private static boolean parentTableContains(SimpleDataTreeNode node, String createdBy) {
		StringTable st = (StringTable) node.properties().getPropertyValue(aaHasParent);
		return st.contains(createdBy + PairIdentity.LABEL_NAME_STR_SEPARATOR);
	}

	@SuppressWarnings("unchecked")
	private List<SimpleDataTreeNode> getConstraints(SimpleDataTreeNode spec, String constraintClass) {
		return (List<SimpleDataTreeNode>) get(spec.getChildren(), selectZeroOrMany(
				andQuery(hasTheLabel(aaMustSatisfyQuery), hasProperty(twaClassName, constraintClass))));
	}

	private boolean isOfClass(SimpleDataTreeNode child, String label) {
		String ioc = (String) child.properties().getPropertyValue(aaIsOfClass);
		return ioc.equals(label);
	}

}
