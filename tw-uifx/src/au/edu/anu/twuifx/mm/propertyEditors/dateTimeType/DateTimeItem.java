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

import java.util.List;
import java.util.Optional;

import org.controlsfx.property.editor.PropertyEditor;

import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twcore.ecosystem.dynamics.TimeLine;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.mm.propertyEditors.SimplePropertyItem;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import fr.cnrs.iees.twcore.constants.DateTimeType;
import fr.cnrs.iees.twcore.constants.TimeScaleType;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

public class DateTimeItem extends SimplePropertyItem {
	private TimeScaleType timeScale = TimeScaleType.defaultValue();
	private TimeUnits tuMin = TimeUnits.defaultValue();
	private TimeUnits tuMax = TimeUnits.defaultValue();

	public DateTimeItem(String key, TreeGraphDataNode n, boolean canEdit, String category, String description) {
		super(key, n, canEdit, category, description);
		TimeLine timeline = null;
		if (n.classId().equals(N_TIMELINE.label()))
			timeline = (TimeLine) n;
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

	@SuppressWarnings("unchecked")
	private TimeLine findSingleTimeLine() {
		// At the moment, this is only possible if there exists ONE timeline connected
		// to the graph tw root;
		TreeGraph<TreeGraphDataNode, ALEdge> graph = ConfigGraph.getGraph();
		TreeGraphNode twroot = null;
		for (TreeGraphNode root : graph.roots())
			if (root.classId().equals(N_ROOT.label()))
				twroot = root;
		List<TreeGraphNode> systems = (List<TreeGraphNode>) get(twroot.getChildren(),
				selectZeroOrMany(hasTheLabel(N_SYSTEM.label())));
		if (systems.isEmpty() || systems.size() > 1)
			return null;
		TreeGraphNode system = systems.get(0);
		TreeGraphNode dynamics = (TreeGraphNode) get(system.getChildren(),
				selectZeroOrOne(hasTheLabel(N_DYNAMICS.label())));
		if (dynamics == null)
			return null;
		TreeGraphNode timeline = (TreeGraphNode) get(dynamics.getChildren(),
				selectZeroOrOne(hasTheLabel(N_TIMELINE.label())));
		if (timeline == null)
			return null;
		return (TimeLine) timeline;
	}

	@Override
	public Object getValue() {
		return node.properties().getPropertyValue(key).toString();
	}

	@Override
	public void setValue(Object value) {
		// TODO This will be wrong - check later
		DateTimeType oldValue = (DateTimeType) node.properties().getPropertyValue(key);
		DateTimeType newValue = DateTimeType.valueOf((String) value);
		if (oldValue.getDateTime() != newValue.getDateTime()) {
//			node.addProperty(key, newValue);
			node.properties().setProperty(key, newValue);
			GraphState.setChanged();
			ConfigGraph.validateGraph();
		}
	}

	@Override
	public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
		return Optional.of(DateTimeTypeEditor.class);
	}

}
