package au.edu.anu.twuifx.widgets;

import au.edu.anu.twcore.ui.runtime.HeadlessWidget;
import fr.cnrs.iees.properties.SimplePropertyList;
//not sure about this wip

public interface WidgetPolicy<T> extends HeadlessWidget{
	
	public boolean canProcessDataMessage(T data);
	
	public int sender();
	
}
