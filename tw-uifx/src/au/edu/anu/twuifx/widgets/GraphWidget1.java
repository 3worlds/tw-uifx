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

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.RuntimeGraphData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.system.ArenaComponent;
import au.edu.anu.twcore.ecosystem.runtime.system.CategorizedContainer;
import au.edu.anu.twcore.ecosystem.runtime.system.ComponentContainer;
import au.edu.anu.twcore.ecosystem.runtime.system.EcosystemGraph;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemComponent;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.widgets.helpers.SimpleWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import javafx.scene.layout.BorderPane;

public class GraphWidget1 extends AbstractDisplayWidget<RuntimeGraphData, Metadata> implements WidgetGUI {
	private final WidgetTrackingPolicy<TimeData> policy;
	private final WidgetTimeFormatter timeFormatter;
	public GraphWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.RUNTIMEGRAPH);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		if (policy.canProcessMetadataMessage(meta))
			timeFormatter.onMetaDataMessage(meta);
	}

	@Override
	public void onDataMessage(RuntimeGraphData data) {
//		System.out.println("onDataMessage: " + data);
		// an hierarchical component: as is a group component
		EcosystemGraph eg = data.getEcosystem();
		System.out.println("EcosystemGraph");

		int nNodes = 0;
		if (eg.nodes() != null)
			nNodes = eg.nNodes();

		// NB: eg.nNodes() is not implemented yet
		System.out.println("\tnNodes: " + nNodes);
		System.out.println("\tnEdges: " + eg.nEdges());

		ArenaComponent arena = eg.arena();
		eg.community();

		System.out.println("Arena: " + arena);
		System.out.println("\t" + arena.membership().categoryId());

		if (arena.content() != null) {
			System.out.println("\tArena full id: " + String.join("->", arena.content().fullId()));
			System.out.println("\tArena allItems-------------------");

			// JG 20/8/2020
			// allItems() returns all SystemComponents contained in the container hierarchy
			// as a flat list, so it's not the proper way to see the hierarchy - it's ok to
			// display the graph
			// If you want to display the arena/group/component hierarchy, you should loop
			// recursively
			// A Container has the following fields:
			// 1) container.hierarchicalView() returns its variables and parameters
			// (actually, it's
			// either an ArenaComponent, GroupComponent or LifeCycleComponent (when it
			// exists)
			// 2) container.items() returns the list of the SystemComponents stored in this
			// container
			// 3) container.subContainers() returns the list of the containers one
			// hierarchical level below
			// the current container.
			// so to get the whole hierarchy information you have to loop recursively on
			// subcontainers
			for (SystemComponent sc : arena.content().allItems()) {
				printContainer("\t\t", sc);
			}
		}

		// what now? - where are the groups
		// cf above - follow the container hierarchy
	}

	private static void printContainer(String indent, SystemComponent sc) {
		ComponentContainer container = (ComponentContainer) sc.container();
		if (container != null) {
			container.fullId();
			// where can i find the ephemeral category that should be displayed here.
			System.out.println(indent + String.join("-.", container.fullId()) + "->" + sc.id() + sc);// etc
			for (CategorizedContainer<SystemComponent> subc : container.subContainers()) {
				for (SystemComponent ssc : subc.allItems()) {
					printContainer(indent + "\t", ssc);
				}
			}
		} else
			System.out.println(sc);
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			// drawTree(true);
		}

	}

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane container = new BorderPane();
		return container;
	}

	@Override
	public Object getMenuContainer() {
		return null;
	}

	@Override
	public void putUserPreferences() {

	}

	@Override
	public void getUserPreferences() {
		System.out.println("getUserPreferences");

	}

}
