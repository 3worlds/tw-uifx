package au.edu.anu.twuifx.mm.visualise;

import java.util.Collection;
import java.util.Set;

import au.edu.anu.rscs.aot.graph.property.PropertyKeys;
import fr.cnrs.iees.graph.NodeFactory;
import fr.cnrs.iees.graph.impl.SimpleNodeImpl;
import fr.cnrs.iees.properties.PropertyListSetters;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.properties.impl.SharedPropertyListImpl;
import fr.cnrs.iees.tree.TreeNode;
import fr.cnrs.iees.tree.TreeNodeFactory;
import fr.ens.biologie.generic.Labelled;
import fr.ens.biologie.generic.Named;
import fr.ens.biologie.generic.NamedAndLabelled;

public class VisualNode extends SimpleNodeImpl implements TreeNode,SimplePropertyList,NamedAndLabelled{
	private String name;
	private String label;
	private SharedPropertyListImpl properties;
	protected VisualNode(String label,String instanceId, PropertyKeys keys,NodeFactory factory) {
		super(instanceId, factory);
		this.name=instanceId;
		this.label = label;
		this.properties = new SharedPropertyListImpl(keys);
	}


	@Override
	public PropertyListSetters setProperty(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getKeysAsSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPropertyValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasProperty(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasName(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean sameName(Named arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Named setName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasLabel(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean sameLabel(Labelled arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Labelled setLabel(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimplePropertyList clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChild(TreeNode arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterable<TreeNode> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeNode getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int nChildren() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setChildren(TreeNode... arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setChildren(Iterable<TreeNode> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setChildren(Collection<TreeNode> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParent(TreeNode arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TreeNodeFactory treeNodeFactory() {
		// TODO Auto-generated method stub
		return null;
	}

}
