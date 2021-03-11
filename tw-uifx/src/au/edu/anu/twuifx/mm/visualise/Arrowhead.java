package au.edu.anu.twuifx.mm.visualise;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.Line;

/**
 * @author Ian Davies
 *
 * @date 25 May 2020
 */
public class Arrowhead extends Line {
	public Arrowhead(Line line, DoubleProperty r,DoubleProperty lineWidth) {
		super();
		// arrow same colour as line
		setStroke(line.getStroke());
		// make thicker
		this.strokeWidthProperty().bind(lineWidth.multiply(4));
//		setStrokeWidth(4);
		// same visibility
		visibleProperty().bind(line.visibleProperty());

		DoubleProperty x2 = line.endXProperty();
		DoubleProperty y2 = line.endYProperty();
		DoubleProperty x1 = line.startXProperty();
		DoubleProperty y1 = line.startYProperty();

		// bind arrow head to line end
		startXProperty().bind(x2);
		startYProperty().bind(y2);

		// bind arrow tail to distance between nodes less 1.5 node radius
//		https://docs.oracle.com/javafx/2/binding/jfxpub-binding.htm
		DoubleBinding xbnd = new DoubleBinding() {
			{
				super.bind(x1, y1, x2, y2, r);
			}

			@Override
			protected double computeValue() {
				return x1.get() + Math.max(0,
						(Math.sqrt((y2.get() - y1.get()) * (y2.get() - y1.get())
								+ (x2.get() - x1.get()) * (x2.get() - x1.get())) - r.multiply(1.5).get()))
						* Math.cos(Math.atan2(y2.get() - y1.get(), x2.get() - x1.get()));
			}

		};
		endXProperty().bind(xbnd);

		DoubleBinding ybnd = new DoubleBinding() {
			{
				super.bind(x1, y1, x2, y2, r);
			}

			@Override
			protected double computeValue() {
				return y1.get() + Math.max(0,
						(Math.sqrt((y2.get() - y1.get()) * (y2.get() - y1.get())
								+ (x2.get() - x1.get()) * (x2.get() - x1.get())) - r.multiply(1.5).get()))
						* Math.sin(Math.atan2(y2.get() - y1.get(), x2.get() - x1.get()));
			}

		};

		endYProperty().bind(ybnd);

//		old code
//		endXProperty().bind(x2.subtract(x2.subtract(x1).divide(4)));
//		endYProperty().bind(y2.subtract(y2.subtract(y1).divide(4)));

	}

	public static void main(String[] args) {
		double x1 = 1;
		double y1 = 1;
		double x2 = 3;
		double y2 = 3;
		double r = Math.sqrt(2);
		double x = x1 + (Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1)) - r)
				* Math.cos(Math.atan2(y2 - y1, x2 - x1));
		// NB limit to > 0 cf above
		double y = y1 + (Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1)) - r)
				* Math.sin(Math.atan2(y2 - y1, x2 - x1));
		System.out.println(x + "," + y);

	}
}
