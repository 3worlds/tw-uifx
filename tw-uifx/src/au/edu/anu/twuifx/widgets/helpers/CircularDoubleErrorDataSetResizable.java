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
package au.edu.anu.twuifx.widgets.helpers;

import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import de.gsi.dataset.utils.CircularBuffer;
import de.gsi.dataset.utils.DoubleCircularBuffer;

public class CircularDoubleErrorDataSetResizable extends CircularDoubleErrorDataSet {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3245592112380606133L;

	//@Deprecated
	public CircularDoubleErrorDataSetResizable(String name, int initalSize) {
		super(name, initalSize);
	}

	public void resizeBuffer(int newSize) {
		DoubleCircularBuffer newxValues = new DoubleCircularBuffer(newSize);
		DoubleCircularBuffer newyValues = new DoubleCircularBuffer(newSize);
		DoubleCircularBuffer newyErrorsPos = new DoubleCircularBuffer(newSize);
		DoubleCircularBuffer newyErrorsNeg = new DoubleCircularBuffer(newSize);
		CircularBuffer<String> newdataTag = new CircularBuffer<>(newSize);
		CircularBuffer<String> newdataStyles = new CircularBuffer<>(newSize);
//		for (int i=0;i<Math.min(newSize, xValues.available());i++) {
//			newxValues.put(xValues.get(i));
//			newyValues.put(yValues.get(i));
//			newyErrorsPos.put(yErrorsPos.get(i));
//			newyErrorsNeg.put(yErrorsNeg.get(i));
//		}
//		for (int i=0;i<Math.min(newSize, newdataTag.available());i++) {
//			newdataTag.put(dataLabels.get(i));
//			newdataStyles.put(dataStyles.get(i));
//		}
		xValues = newxValues;
		yValues = newyValues;
		yErrorsPos = newyErrorsPos;
		yErrorsNeg = newyErrorsNeg;
		dataLabels = newdataTag;
		dataStyles = newdataStyles;
	}

}
