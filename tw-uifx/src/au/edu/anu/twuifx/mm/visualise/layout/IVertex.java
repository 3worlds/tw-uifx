package au.edu.anu.twuifx.mm.visualise.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import au.edu.anu.twapps.mm.visualGraph.VisualNode;

/**
 * @author Ian Davies
 *
 * @date 30 Apr 2020
 */
public interface IVertex {
	
	public VisualNode getNode();
	public void setLocation(double x, double y);

	public double getX();

	public double getY();

	public String id();
	
	public void jitter(double f,Random rnd);

	public void normalise(Rectangle2D from, Rectangle2D to);
	
	public void getLayoutBounds(Point2D min, Point2D max);
}
