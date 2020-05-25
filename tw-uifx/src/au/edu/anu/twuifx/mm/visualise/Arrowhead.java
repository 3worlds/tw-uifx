package au.edu.anu.twuifx.mm.visualise;

import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * @author Ian Davies
 *
 * @date 25 May 2020
 */
public class Arrowhead extends Line {
	public Arrowhead(Line line) {
		super();
		Color c = (Color) line.getStroke();
//		setStroke(c.darker());
		setStroke(c);
		setStrokeWidth(2);
		visibleProperty().bind(line.visibleProperty());
		
		DoubleProperty x2 = line.endXProperty();
		DoubleProperty y2 = line.endYProperty();
		DoubleProperty x1 = line.startXProperty();
		DoubleProperty y1 = line.startYProperty();

		startXProperty().bind(x2);
		startYProperty().bind(y2);

		//need to limit to twice the node radius. Must be done by trig
		endXProperty().bind(x2.subtract(x2.subtract(x1).divide(4)));
		endYProperty().bind(y2.subtract(y2.subtract(y1).divide(4)));
		
/**myProperty().bind(Bindings.createDoubleBinding(() ->
    Math.cos(angleProperty.get()) * factorProperty.get(),
    angleProperty, factorProperty));*/

	}

}
