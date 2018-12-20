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

import au.edu.anu.twapps.graphviz.GraphVisualisable;
import fr.cnrs.iees.graph.Graph;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.text.Font;

/**
 * Author Ian Davies
 *
 * Date 12 Dec. 2018
 */

// We could do the old impl trick here or better just implement an interface.
// Must not expose fx!

public class GVizfx implements GraphVisualisable {
	private static int fontSize;
	private static Font font;
	private static IntegerProperty nodeRadiusProperty = new SimpleIntegerProperty(0);

	public static final String kX = "$x";
	public static final String kY = "$y";

	public static void setFontSize(int size) {
		font = Font.font("Verdana", size);
		fontSize = size;
	}

	public static void setNodeRadius(int size) {
		nodeRadiusProperty.set(size);
	}

	public static int getNodeRadius() {
		return nodeRadiusProperty.get();
	}

	// Exposes fx!
	public static Font getFont() {
		return font;
	}

	public static int getFontSize() {
		return fontSize;
	}

	@Override
	public Graph<?, ?> initialiseLayOut(Graph<?, ?> layoutGraph) {
		// Node vn = layoutGraph.nodes().iterator().next();
		// vn.setProperty(kX, 0.1);
		// vn.setProperty(kY, 0.5);
		return null;
	}

	@Override
	public void createVisualElements(Graph<?, ?> layoutGraph) {
		// List<AotNode> collapseParents = new ArrayList<AotNode>();

		// BooleanProperty showChildLines = controller.childLinksProperty();
		// BooleanProperty showXLinkLines = controller.xLinksProperty();
		// for (AotNode n : layoutGraph.nodes())
		// VisualNode.createNodeVisualisation(n, pane, this);
		// for (AotNode n : layoutGraph.nodes()) {
		// VisualNode.createChildOutLines(n, showChildLines, pane);
		// VisualNode.createXLinkOutLines(n, showXLinkLines, pane);
		// if (VisualNode.isCollapseParent(n))
		// collapseParents.add(n);
		// }
		// for (AotNode n : collapseParents)
		// VisualNode.collapseTree(n);
	}

	@Override
	public void linkGraphs(Graph<?, ?> currentGraph, Graph<?, ?> layoutGraph) {
		// TODO Auto-generated method stub
//		for (AotNode n : vg.nodes()) {
//			VisualNode vn = (VisualNode) n;
//			String id = VisualNode.getGuestNodeId(n);
//			AotNode guestNode = findNode(cg, id);
//			if (guestNode == null)
//				throw new AotException("Unable to find guest node for visual node: " + id);
//			vn.LinkNode(guestNode);
//		}

		
	}

}
