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

package au.edu.anu.twuifx.mm.propertyEditors.dateTimeType;

import java.util.Optional;

import org.controlsfx.property.editor.PropertyEditor;

import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twcore.InitialisableNode;
import au.edu.anu.twcore.ecosystem.dynamics.Timeline;
import au.edu.anu.twcore.root.World;
import au.edu.anu.twuifx.mm.propertyEditors.SimpleMMPropertyItem;
import fr.cnrs.iees.graph.ElementAdapter;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import fr.cnrs.iees.twcore.constants.DateTimeType;
import fr.cnrs.iees.twcore.constants.TimeScaleType;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

public class DateTimeItem extends SimpleMMPropertyItem {
	private TimeScaleType timeScale = TimeScaleType.defaultValue();
	private TimeUnits tuMin = TimeUnits.defaultValue();
	private TimeUnits tuMax = TimeUnits.defaultValue();

	public DateTimeItem(IMMController controller, String key, ElementAdapter element, boolean canEdit, String category, String description) {
		super(controller,key, element, canEdit, category, description);
		Timeline timeline = null;
		if (element.classId().equals(N_TIMELINE.label()))
			timeline = (Timeline) element;
		else
			timeline = findSingleTimeLine();
		if (timeline != null) {
			timeScale = (TimeScaleType) timeline.properties().getPropertyValue(P_TIMELINE_SCALE.key());
			tuMin = (TimeUnits) timeline.properties().getPropertyValue(P_TIMELINE_SHORTTU.key());
			tuMax = (TimeUnits) timeline.properties().getPropertyValue(P_TIMELINE_LONGTU.key());
		}
	}

	public TimeScaleType getTimeScaleType() {
		return timeScale;
	}

	public TimeUnits getTUMin() {
		return tuMin;
	}

	public TimeUnits getTUMax() {
		return tuMax;
	}

	private Timeline findSingleTimeLine() {
		TreeGraphNode system = (TreeGraphNode)World.getSystemRoot((InitialisableNode) this.element);
		if (system.equals(this.element))
			return null;
		TreeGraphNode dynamics = (TreeGraphNode) get(system.getChildren(),
				selectZeroOrOne(hasTheLabel(N_DYNAMICS.label())));
		if (dynamics == null)
			return null;
		TreeGraphNode timeline = (TreeGraphNode) get(dynamics.getChildren(),
				selectZeroOrOne(hasTheLabel(N_TIMELINE.label())));
		if (timeline == null)
			return null;
		return (Timeline) timeline;
	}

	@Override
	public Object getValue() {
		return super.getValue().toString();
	}

	@Override
	public void setValue(Object value) {
		DateTimeType oldValue = (DateTimeType) properties.getPropertyValue(key);
		DateTimeType newValue = DateTimeType.valueOf((String) value);
		if (oldValue.getDateTime() != newValue.getDateTime()) {
			onUpdateProperty(newValue);
		}
	}

	@Override
	public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
		return Optional.of(DateTimeTypeEditor.class);
	}

}
