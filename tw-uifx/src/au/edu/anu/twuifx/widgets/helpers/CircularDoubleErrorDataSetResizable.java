package au.edu.anu.twuifx.widgets.helpers;

import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import de.gsi.dataset.utils.CircularBuffer;
import de.gsi.dataset.utils.DoubleCircularBuffer;

public class CircularDoubleErrorDataSetResizable extends CircularDoubleErrorDataSet {

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
		for (int i=0;i<Math.min(newSize, xValues.available());i++) {
			newxValues.put(xValues.get(i));
			newyValues.put(yValues.get(i));
			newyErrorsPos.put(yErrorsPos.get(i));
			newyErrorsNeg.put(yErrorsNeg.get(i));
		}
		for (int i=0;i<Math.min(newSize, newdataTag.available());i++) {
			newdataTag.put(dataTag.get(i));
			newdataStyles.put(dataStyles.get(i));
		}
		xValues = newxValues;
		yValues = newyValues;
		yErrorsPos = newyErrorsPos;
		yErrorsNeg = newyErrorsNeg;
		dataTag = newdataTag;
		dataStyles = newdataStyles;
	}

}
