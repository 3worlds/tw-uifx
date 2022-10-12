/**************************************************************************
 *  TW-UIFX - ThreeWorlds User-Interface fx                               *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-UIFX contains the Javafx interface for ModelMaker and ModelRunner. *
 *  This is to separate concerns of UI implementation and the code for    *
 *  these java programs.                                                  *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-UIFX (ThreeWorlds User-Interface fx).         *
 *                                                                        *
 *  TW-UIFX is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-UIFX is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-UIFX.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>.                  *
 *                                                                        *
 **************************************************************************/
package au.edu.anu.twuifx.mm.visualise;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.Line;

/**
 * A line with a simple arrowhead (thickened line end).
 * 
 * @author Ian Davies - 25 May 2020
 */
public class Arrowhead extends Line {
	/**
	 * @param line The line to which the arrowhead belongs.
	 * @param r ModelMaker's node radius property.
	 * @param lineWidth ModelMaker's line width property. 
	 */
	public Arrowhead(Line line, DoubleProperty r, DoubleProperty lineWidth) {
		super();
		// arrow same colour as line
		setStroke(line.getStroke());
		// make thicker
		this.strokeWidthProperty().bind(lineWidth.multiply(4));
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

	}

}
