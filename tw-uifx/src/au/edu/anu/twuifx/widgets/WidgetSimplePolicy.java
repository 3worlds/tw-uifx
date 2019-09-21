package au.edu.anu.twuifx.widgets;

import au.edu.anu.twcore.data.runtime.OutputData;
import fr.cnrs.iees.properties.SimplePropertyList;
// not sure about this wip
public class WidgetSimplePolicy implements WidgetPolicy<OutputData>{
	private int sender;

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		sender = (Integer) properties.getPropertyValue("sender");		
	}

	@Override
	public void putPreferences() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void getPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canProcessDataMessage(OutputData data) {
		return sender==data.sender();
	}

	@Override
	public int sender() {
		return sender;
	}


}
