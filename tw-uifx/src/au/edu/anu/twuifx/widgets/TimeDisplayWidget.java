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
package au.edu.anu.twuifx.widgets;

import java.util.List;
import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twcore.data.runtime.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.twcore.constants.TimeScaleType;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */
public class TimeDisplayWidget extends AbstractDisplayWidget<Property,SimplePropertyList> implements Widget {
	
	public TimeDisplayWidget() {
		super(DataMessageTypes.TIME);
	}

	private boolean metadataReceived = false;
	private TimeUnits smallest;
	private Label lblTime;
	private TimeScaleType timeScale;
	private List<TimeUnits> units;
	private long startTime;

	/*
	 * Need the meta-data listed below to interpret the time value as anything other
	 * than a great big number. The values 'could' be just properties of this that
	 * the user can change and have saved and loaded in preferences. But this would
	 * be annoying as you would have to do this every time you first deploy.
	 * 
	 * Question is when?
	 * 
	 * 1) The meta-data comes from the TimeLine and so assumes all sending sims are
	 * using this same timeline. Is this enforced somewhere?
	 * YES (cf archetype): at the moment there is 1 timeLine per Simulator (=dynamics node) 
	 * and 1 simulator per ecosystem BUT there may be more than one ecosystem per 3w root node
	 * 
	 * 2) This data shouldn't change after initialisation (i.e.not at reset) so
	 * should only be sent once per deployment.
	 * 
	 * Could it be done within initialise and the data passed to each instance or
	 * does this risk conflating the purpose of initialisation order? Well
	 * initialisation does not create the instance.
	 * 
	 * WHAT about another solution: add into the abstract DisplayWidget ancestor
	 * the possibility to send metadata (as a propertylist) through a particular message type
	 * then (1) it's up to the widget to interpret the metadata and (2) it's also up to the widget
	 * to know what to do in the case where data arrive before the metadata or when the
	 * metadata change (ie when a second metadata message is sent).
	 * This would fix the problem for this widget, and possibly for all other ones where
	 * the same question will certainly arise.
	 * 
	 */

	/*- 
	 * startTime = reader.readLong(); 
	 * timeScale =TimeScaleType.valueOf(reader.readString()); 
	 * TimeUnits largest =TimeUnits.valueOf(reader.readString()); 
	 * smallest = TimeUnits.valueOf(reader.readString()); 
	 * 
	*/

	// Also see below

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getUserInterfaceContainer() {
		HBox content = new HBox();
		// top, right, bottom, and left padding
		content.setPadding(new Insets(5, 1, 1, 2));
		content.setSpacing(5);
		// content.getChildren().addAll(tdm.getTimeLabel());
		return content;

	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
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
	public void onDataMessage(Property data) {
		// when plugged to a simulator through an edge with label "trackTime"
		// this method will receive a pair with label="time" and value=<long>, the
		// current time
		// of the attached simulator (dont forget there could be many simulators)
		/*
		 * Ok so we could have a sim swarm. Can we assume that all Simulator are
		 * instances of the same SimulatorNode?? If so the "key" could be a hashcode
		 * supplied by the sim instance instead of "time" since we know its the time
		 * (because its a TimeDisplayWidget silly!). All sim instances would be using
		 * the same timeline so the meta-data for display is always the same. Am i right
		 * so far? If so there are various display options:
		 * 
		 * If one sim just show its time as usual.
		 * 
		 * If >1 show the times of the fastest and slowest of still running sims. (Need
		 * to listen for a Finish state?)
		 * 
		 * Show a graph of straight lines, one for each sim (this is really another
		 * widget (SwarmProgressWidget).
		 * 
		 * Show a time density distribution etc etc - another widget
		 */
		/*- e.g.
		* |1-----------------
		* |2--------
		* |3--------------------
		* |4------
		* |etc...
		* |______________________________
		* Time scale in appropriate units
		*/
		/*- or
		* |
		* |
		* |     ^
		* |    /  \_
		* |   /     \
		* |__/_______\_____________________
		* Time scale in appropriate units
		* and many others
		* 
		* That's GREAT!
		* One of the still unresolved issues with sending data is the identification of the
		* data sent - here we just send a label and a value, and we were naively thinking
		* that label = variable name. But what about component name, species, stage, etc etc.
		* I think one solution is to construct a hierarchical label, eg in our case here
		* "sim1>time" or "sim2>time" etc. If we dont want a complex string parser to deal with 
		* those labels, then we can define the label as a String list (eg "sim1","time" here),
		* so it's easier to get the information back (and you dont get into problems if you
		* use the separator character in the Strings). It's easy for the Simulator to build
		* the hierarchical label list, since all its objects have a uniqueID and are stored
		* in nested data structures, and it's easy for widgets to analyse this label list
		* 
		*/
		
		// TODO: replace this with useful code
		if (metadataReceived)
			System.out.println("Simulator "+data.getKey()+" time = "+
					data.getValue()+" "+smallest.abbreviation());
	}

	@Override
	public void onMetaDataMessage(SimplePropertyList meta) {
		smallest = (TimeUnits) meta.getPropertyValue(P_TIMELINE_SHORTTU.key());
//		lblTime = new Label("t ("+smallest.abbreviation()+")");
		timeScale = (TimeScaleType) meta.getPropertyValue(P_TIMELINE_SCALE.key());
//		private List<TimeUnits> units;
		startTime = (Long) meta.getPropertyValue(P_TIMELINE_TIMEORIGIN.key());
		// what to do with this one ? compute all valid unit within timescale ?
		meta.getPropertyValue(P_TIMELINE_LONGTU.key()); // longest time unit
		
		// TODO: replace this by useful code
		System.out.println("metadata for TimeDisplayWidget: "+meta.toString());	
		metadataReceived = true;
	}

}
