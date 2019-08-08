package au.edu.anu.twuifx.mm.editors.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.edu.anu.rscs.aot.archetype.ArchetypeArchetypeConstants;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.queries.Query;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.archetype.TWA;
import au.edu.anu.twcore.archetype.TwArchetypeConstants;
import au.edu.anu.twcore.archetype.tw.CheckSubArchetypeQuery;
import au.edu.anu.twcore.archetype.tw.IsInValueSetQuery;
import au.edu.anu.twcore.archetype.tw.NameStartsWithUpperCaseQuery;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import fr.cnrs.iees.graph.Tree;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.SimpleDataTreeNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
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
		// TODO Auto-generated method stub ??
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
					selectZeroOrMany(hasProperty(aaClassName, CheckSubArchetypeQuery.class.getName())));
			for (SimpleDataTreeNode constraint : saConstraints) {
				List<String> pars = getConstraintTable(constraint);
				Tree<?> tree = (Tree<?>) TWA.getSubArchetype(pars.get(2));
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
			SimpleDataTreeNode parentSubSpec, TreeNode root) {
		String parentLabel = (String) parentSpec.properties().getPropertyValue(aaIsOfClass);
		List<SimpleDataTreeNode> children = (List<SimpleDataTreeNode>) get(root.getChildren(),
				selectZeroOrMany(hasProperty(aaHasParent)));
		// could have a query here for finding a parent in a parent Stringtable
		List<SimpleDataTreeNode> result = new ArrayList<>();
		addChildrenTo(result, parentLabel, children);
		if (parentSubSpec != null) {
			// look for children in the subclass tree root
			children = (List<SimpleDataTreeNode>) get(parentSubSpec.getParent().getChildren(),
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
	public Iterable<SimpleDataTreeNode> getEdgeSpecificationsOf(SimpleDataTreeNode baseSpec,
			SimpleDataTreeNode subSpec) {
		List<SimpleDataTreeNode> result = (List<SimpleDataTreeNode>) get(baseSpec.getChildren(),
				selectZeroOrMany(hasTheLabel(aaHasEdge)));
		if (subSpec != null)
			result.addAll(
					(List<SimpleDataTreeNode>) get(subSpec.getChildren(), selectZeroOrMany(hasTheLabel(aaHasEdge))));
		return result;
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
		String result = (String) edgeSpec.properties().getPropertyValue(aaToNode);
		result = result.replace(PairIdentity.LABEL_NAME_STR_SEPARATOR, "");
		return result;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public List<Class<? extends TreeNode>> getSubClasses(SimpleDataTreeNode spec) {
		List<Class<? extends TreeNode>> result = new ArrayList<>();
		SimpleDataTreeNode propertySpec = (SimpleDataTreeNode) get(spec.getChildren(),
				selectZeroOrOne(hasProperty(aaHasName, twaSubclass)));
		if (propertySpec != null) {
			SimpleDataTreeNode constraint = (SimpleDataTreeNode) get(propertySpec.getChildren(),
					selectOne(hasProperty(aaClassName, IsInValueSetQuery.class.getName())));
			StringTable classes = (StringTable) constraint.properties().getPropertyValue(twaValues);
			for (int i = 0; i < classes.size(); i++)
				try {
					result.add((Class<? extends TreeNode>) Class.forName(classes.getWithFlatIndex(i)));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String[]> getQueryStringTables(SimpleDataTreeNode spec, Class<? extends Query> queryClass) {
		List<String[]> result = new ArrayList<>();
		if (spec == null)
			return result;
		List<SimpleDataTreeNode> constraints = (List<SimpleDataTreeNode>) get(spec.getChildren(),
				selectZeroOrMany(hasProperty(aaClassName, queryClass.getName())));
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

	@Override
	public boolean filterPropertySpecs(Iterable<SimpleDataTreeNode> propertySpecs, SimpleDataTreeNode baseSpec,
			SimpleDataTreeNode subSpec, String childId, Class<? extends Query>... queryClasses) {
		List<String[]> entries = new ArrayList<>();
		for (Class<? extends Query> qclass : queryClasses) {
			entries.addAll(getQueryStringTables(baseSpec, qclass));
			entries.addAll(getQueryStringTables(subSpec, qclass));
		}
		if (!entries.isEmpty()) {
			List<String> selectedKeys = Dialogs.getRadioButtonChoices(childId, "PropertyChoices", "", entries);
			if (selectedKeys == null)
				return false;
			Iterator<SimpleDataTreeNode> iter = propertySpecs.iterator();
			while (iter.hasNext()) {
				SimpleDataTreeNode ps = iter.next();
				String key = (String) ps.properties().getPropertyValue(aaHasName);
				String optionalKey = getSelectedEntry(key, selectedKeys, entries);
				if (optionalKey != null && !optionalKey.equals(key))
					iter.remove();
			}
		}
		return true;
	}

	private static String getSelectedEntry(String key, List<String> selectedKeys, List<String[]> entries) {
		if (selectedKeys == null)
			return null;
		for (int i = 0; i < selectedKeys.size(); i++) {
			String sel = selectedKeys.get(i);
			String[] entry = entries.get(i);
			for (int j = 0; j < entry.length; j++) {
				if (entry[j].equals(sel))
					return sel;
			}
		}
		return null;
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
	protected Tree<? extends TreeNode> getSubArchetype(SimpleDataTreeNode spec, Class<? extends TreeNode> subClass) {
		List<SimpleDataTreeNode> constraints = (List<SimpleDataTreeNode>) get(spec.getChildren(),
				selectOneOrMany(hasProperty(aaClassName, CheckSubArchetypeQuery.class.getName())));
		for (SimpleDataTreeNode constraint : constraints) {
			StringTable pars = (StringTable) constraint.properties().getPropertyValue(twaParameters);
			if (pars.getWithFlatIndex(1).equals(subClass.getName())) {
				return TWA.getSubArchetype(pars.get(2));
			}
		}
		throw new TwuifxException("Sub archetype graph not found for " + subClass.getName());
	}

	private SimpleDataTreeNode getConstraint(SimpleDataTreeNode spec, String constraintClass) {
		return (SimpleDataTreeNode) get(spec.getChildren(),
				selectZeroOrOne(andQuery(hasTheLabel(aaMustSatisfyQuery), hasProperty(aaClassName, constraintClass))));
	}

	private static boolean parentTableContains(SimpleDataTreeNode node, String createdBy) {
		StringTable st = (StringTable) node.properties().getPropertyValue(aaHasParent);
		return st.contains(createdBy + PairIdentity.LABEL_NAME_STR_SEPARATOR);
	}

	@SuppressWarnings("unchecked")
	private List<SimpleDataTreeNode> getConstraints(SimpleDataTreeNode spec, String constraintClass) {
		return (List<SimpleDataTreeNode>) get(spec.getChildren(),
				selectZeroOrMany(andQuery(hasTheLabel(aaMustSatisfyQuery), hasProperty(aaClassName, constraintClass))));
	}

	private boolean isOfClass(SimpleDataTreeNode child, String label) {
		String ioc = (String) child.properties().getPropertyValue(aaIsOfClass);
		return ioc.equals(label);
	}

}
