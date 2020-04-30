package au.edu.anu.twuifx.mm.visualise.layout;

import java.util.List;

/**
 * @author Ian Davies
 *
 * @date 30 Apr 2020
 */
public interface ITreeVertex<V> {
	public boolean isLeaf();
	public boolean hasParent();
	public V getParent();
	public List<V> getChildren();

}
