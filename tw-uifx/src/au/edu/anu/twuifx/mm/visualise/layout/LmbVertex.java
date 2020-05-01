package au.edu.anu.twuifx.mm.visualise.layout;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import fr.cnrs.iees.uit.space.Distance;

public class LmbVertex extends FRVertex{
	private List<LmbVertex> _neighbours;
	private double _rotation;

	public LmbVertex(VisualNode node) {
		super(node);
		_neighbours = new ArrayList<>();
	}

	public void addNeighbour(LmbVertex n) {
		_neighbours.add(n);
		
	}
	@Override
	public boolean hasEdges() {
		return (!_neighbours.isEmpty());
	}

	public void setRotationalForce(LmbVertex other, double b) {
		// Rotate to tangents have the same reciprocal angle
		
	}
	public void setTangentialDisplacement(LmbVertex other, double a) {
		// TODO Auto-generated method stub
		
	}


	

}
