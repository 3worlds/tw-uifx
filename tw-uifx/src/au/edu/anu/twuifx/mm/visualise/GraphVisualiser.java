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

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.twapps.mm.GraphState;
import au.edu.anu.twapps.mm.visualGraph.VisualGraph;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twuifx.mm.editors.structure.SpecifiedNode;
import au.edu.anu.twuifx.mm.editors.structure.StructureEditable;
import au.edu.anu.twuifx.mm.editors.structure.StructureEditorfx;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Author Ian Davies
 *
 * Date 28 Jan. 2019
 */
public final class GraphVisualiser implements GraphVisualisablefx {

	private VisualGraph visualGraph;
	private Pane pane;
	private IntegerProperty nodeRadius;
	private BooleanProperty showTreeLine;
	private BooleanProperty showGraphLine;
	private DropShadow ds;
	private ObjectProperty<Font> font;
	private Color hoverColor;

	public GraphVisualiser(VisualGraph visualGraph, Pane pane, IntegerProperty nodeRadius, BooleanProperty showTreeLine,
			BooleanProperty showGraphLine, ObjectProperty<Font> font) {
		this.visualGraph = visualGraph;
		this.pane = pane;
		this.nodeRadius = nodeRadius;
		this.showGraphLine = showGraphLine;
		this.showTreeLine = showTreeLine;
		this.font = font;
		ds = new DropShadow();
		hoverColor = Color.RED;
	}

	@Override
	public void initialiseView() {
		pane.setPrefHeight(pane.getHeight());
		pane.setPrefWidth(pane.getWidth());

		for (VisualNode n : visualGraph.nodes()) {
			createNodeVisualisation(n);
		}
		List<VisualNode> collapsedParents = new ArrayList<>();

		for (VisualNode n : visualGraph.nodes()) {
			createTreeLines(n, showTreeLine);
			createGraphLines(n, showGraphLine);
			if (n.isCollapsedParent())
				collapsedParents.add(n);
		}
		for (VisualNode n : collapsedParents)
			collapseTree(n);

		pane.setPrefHeight(Control.USE_COMPUTED_SIZE);
		pane.setPrefWidth(Control.USE_COMPUTED_SIZE);

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	private StructureEditable gse;
	private VisualNode dragNode;

	private void createNodeVisualisation(VisualNode n) {
		double x = n.getX() * pane.getWidth();
		double y = n.getY() * pane.getHeight();

		Circle c = new Circle(x, y, nodeRadius.get());
		c.radiusProperty().bind(nodeRadius);
		Text text = new Text(n.uniqueId());
		n.setVisualElements(c, text);
		Color nColor = TreeColours.getCategoryColor(n.getCategory());
		c.fillProperty().bind(Bindings.when(c.hoverProperty()).then(hoverColor).otherwise(nColor));
		c.setEffect(ds);
		c.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				dragNode = n;
				e.consume();
			}
		});
		c.setOnMouseDragged(e -> {
			if (dragNode != null) {
				double w = pane.getWidth();
				double h = pane.getHeight();
				double ex = e.getX();
				double ey = e.getY();
				if (x < w && y < h && ex >= 0 && ey >= 0) {
					Circle dc = (Circle) dragNode.getSymbol();
					dc.setCenterX(ex);
					dc.setCenterY(ey);
				}
				e.consume();
			}

		});
		c.setOnMouseReleased(e -> {
			if (dragNode != null) {
				Circle dc = (Circle) dragNode.getSymbol();
				double w = pane.getWidth();
				double h = pane.getHeight();
				double newx = Math.max(0, Math.min(w, dc.getCenterX())) / w;
				double newy = Math.max(0, Math.min(h, dc.getCenterY())) / h;
				double oldx = dragNode.getX();
				double oldy = dragNode.getY();
				int dx = (int) Math.round(50 * Math.abs(oldx - newx));
				int dy = (int) Math.round(50 * Math.abs(oldy - newy));
				if (dx != 0 || dy != 0) {
					dragNode.setPosition(newx, newy);
					GraphState.isChanged(true);
				}
				dragNode = null;
				e.consume();
			}
		});
		c.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				gse = new StructureEditorfx(new SpecifiedNode(n), e);
			} else
				setNodePropertySheet(n);
		});

		text.fontProperty().bind(font);

		// Bind text position relative to circle center
		text.xProperty().bind(c.centerXProperty().add(nodeRadius));
		text.yProperty().bind(c.centerYProperty().add(nodeRadius.divide(2)));
		text.visibleProperty().bind(c.visibleProperty());
		pane.getChildren().addAll(c, text);
		/*
		 * put text behind circle - can't work entirely because other circles will be
		 * behind this text. It would require a second loop.
		 */
		c.toFront();
		text.toBack();
	}

}
