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

import org.apache.commons.math.util.MathUtils;

import au.edu.anu.rscs.aot.queries.base.SequenceQuery;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.mm.editors.structure.VisualNodeEditor;
import au.edu.anu.twuifx.mm.editors.structure.StructureEditorfx;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.Edge;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.TreeGraph;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Author Ian Davies
 *
 * Date 28 Jan. 2019
 */
public final class GraphVisualiserfx implements IGraphVisualiser {

	private final TreeGraph<VisualNode, VisualEdge> visualGraph;
	private final Pane pane;
	private final IntegerProperty nodeRadius;
	private final BooleanProperty showTreeLine;
	private final BooleanProperty showGraphLine;
	private final DropShadow ds;
	private final ObjectProperty<Font> font;
	// these could all be properties and therefore modifiable from the GUI
	private final Color hoverColor;
	private final Color treeEdgeColor;
	private final Color graphEdgeColor;
	private static final Double animateDuration = 500.0;
	private final IMMController controller;
	
	private boolean edgeClassOnly = false;
	private boolean nodeClassOnly = false;

	public GraphVisualiserfx(TreeGraph<VisualNode, VisualEdge> visualGraph, //
			Pane pane, //
			IntegerProperty nodeRadius, //
			BooleanProperty showTreeLine, //
			BooleanProperty showGraphLine, //
			ObjectProperty<Font> font, //
			IMMController controller) {
		this.visualGraph = visualGraph;

		this.pane = pane;
		this.nodeRadius = nodeRadius;
		this.showGraphLine = showGraphLine;
		this.showTreeLine = showTreeLine;
		this.font = font;
		this.controller = controller;
		ds = new DropShadow();
		hoverColor = Color.RED;
		treeEdgeColor = Color.GREEN;
		graphEdgeColor = Color.GRAY;

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
	public TreeGraph<VisualNode, VisualEdge> getVisualGraph() {
		return visualGraph;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	// private StructureEditable gse;
	private VisualNode dragNode;

	private void createNodeVisualisation(VisualNode n) {
		double x = n.getX() * pane.getWidth();
		double y = n.getY() * pane.getHeight();

		Circle c = new Circle(x, y, nodeRadius.get());
		c.radiusProperty().bind(nodeRadius);
		Text text = new Text(n.getDisplayText(nodeClassOnly));
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
					GraphState.setChanged();
				}
				dragNode = null;
				e.consume();
			}
		});
		c.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				new StructureEditorfx(new VisualNodeEditor(n, visualGraph), e, controller, this);
			} else
				controller.onNodeSelected(n);

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

	@Override
	public void onNewNode(VisualNode node) {
		createNodeVisualisation(node);
		createTreeLines(node, showTreeLine);
		// gse.
		// create all the visual stuff
		// if (placing) {
		// Platform.runLater(() -> {
		// AotNode n = popupEditor.locate(event, pane.getWidth(), pane.getHeight());
		// VisualNode.insertCircle(n, controller.childLinksProperty(),
		// controller.xLinksProperty(), pane, this);
		// // add parent edge. There must be one in this circumstance
		// AotEdge inEdge = (AotEdge) get(n.getEdges(Direction.IN),
		// selectOne(hasTheLabel(Trees.CHILD_LABEL)));
		// VisualNode.createChildLine(inEdge, controller.childLinksProperty(), pane);
		// popupEditor = null;
		// placing = false;
		// pane.setCursor(Cursor.DEFAULT);
		// reBuildAllElementsPropertySheet();
		// checkGraph();
		// });
		// }
		//

	}

	private void createTreeLines(VisualNode child, BooleanProperty show) {
		// each node has a line connected to its parent
		VisualNode parent = child.getParent();
		if (parent != null) {
			Circle parentCircle = (Circle) parent.getSymbol();
			Circle childCircle = (Circle) child.getSymbol();
			Line line = new Line();
			child.setParentLine(line);

			// bindings
			// TODO can we have a colour property?
			// line.strokeProperty().bind(treeEdgeColourProperty());
			line.setStroke(treeEdgeColor);
			line.startXProperty().bind(parentCircle.centerXProperty());
			line.startYProperty().bind(parentCircle.centerYProperty());

			line.endXProperty().bind(childCircle.centerXProperty());
			line.endYProperty().bind(childCircle.centerYProperty());

			line.visibleProperty().bind(show);

			pane.getChildren().add(line);
			line.toBack();

		}
	}

	// TODO used for editing??? later
	@SuppressWarnings("unchecked")
	private void createGraphLines(VisualNode n, BooleanProperty show) {
		Iterable<VisualEdge> edges = (Iterable<VisualEdge>) SequenceQuery.get(n.edges(Direction.OUT));
		for (VisualEdge edge : edges) {
			createGraphLine(edge, show);
		}
	}

	private void createGraphLine(VisualEdge edge, BooleanProperty show) {
		VisualNode startNode = (VisualNode) edge.startNode();
		VisualNode endNode = (VisualNode) edge.endNode();
//		@SuppressWarnings("unchecked")
//		Iterable<VisualEdge> edges = (Iterable<VisualEdge>) SequenceQuery.get(startNode.edges(Direction.OUT),
//				selectZeroOrMany(notQuery(hasTheLabel(getEdgeLabel(edge)))));
//		/*
//		 * edge labels of identical lines will obscure each other. Therefore we have to
//		 * pull some tricks to concatenate all the info in one text object and set the
//		 * others to ""
//		 */
//
//		String newLabel = "";
//		for (VisualEdge e : edges) {
//			if (e.endNode().id().equals(endNode.id())) {
//				newLabel += getEdgeLabel(e) + "/";
//				Text text = (Text) e.getText();
//				if (text != null)
//					text.setText("");
//			}
//		}
//		newLabel += getEdgeLabel(edge);
		String newLabel = edge.getDisplayText(edgeClassOnly);

		Circle fromCircle = (Circle) startNode.getSymbol();
		Circle toCircle = (Circle) endNode.getSymbol();
		Line line = new Line();
		Text text = new Text(newLabel);
		text.fontProperty().bind(font);
		;
		edge.setVisualElements(line, text);
		// TODO use property here
		line.setStroke(graphEdgeColor);

		// Bindings
		line.startXProperty().bind(fromCircle.centerXProperty());
		line.startYProperty().bind(fromCircle.centerYProperty());

		line.endXProperty().bind(toCircle.centerXProperty());
		line.endYProperty().bind(toCircle.centerYProperty());

		line.visibleProperty().bind(show);

		text.xProperty().bind(fromCircle.centerXProperty().add(toCircle.centerXProperty()).divide(2.0));
		text.yProperty().bind(fromCircle.centerYProperty().add(toCircle.centerYProperty()).divide(2.0));
		text.visibleProperty().bind(line.visibleProperty());

		// hide text when positions make text too cramped
		text.xProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				textPropertyChange(line, text, startNode, endNode);

			}
		});
		text.yProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				textPropertyChange(line, text, startNode, endNode);

			}
		});
		pane.getChildren().addAll(line, text);
		line.toBack();

	}

	protected void textPropertyChange(Line line, Text text, VisualNode startNode, VisualNode endNode) {
		boolean collapsed = startNode.isCollapsed() || endNode.isCollapsed();
		double[] p1 = { line.getStartX(), line.getStartY() };
		double[] p2 = { line.getEndX(), line.getEndY() };
		double distance = MathUtils.distance(p1, p2);
		// or dy small (horizontal) and dx shorter than label??
		if ((distance < (8 * nodeRadius.get())) | collapsed) {
			text.visibleProperty().unbind();
			text.setVisible(false);
		} else {
			text.visibleProperty().bind(line.visibleProperty());
		}
	}

	private static void collapseTree(VisualNode parent) {
		Circle circle = (Circle) parent.getSymbol();
		DoubleProperty xp = circle.centerXProperty();
		DoubleProperty yp = circle.centerYProperty();
		for (TreeNode child : parent.getChildren())
			collapse(child, xp, yp);
	}

	private void expandTree(VisualNode parent) {
		double w = pane.getWidth();
		double h = pane.getHeight();
		for (TreeNode child : parent.getChildren()) {
			expand(child, w, h);
		}
	}

	private static void expand(TreeNode node, double w, double h) {
		setExpandBindings((VisualNode) node, w, h);
		for (TreeNode child : node.getChildren())
			expand(child, w, h);
	}

	private static void setExpandBindings(VisualNode node, double w, double h) {
		Circle circle = (Circle) node.getSymbol();
		double x = node.getX();
		double y = node.getY();
		x = w * x;
		y = h * y;
		circle.centerXProperty().unbind();
		circle.centerYProperty().unbind();
		KeyValue endX = new KeyValue(circle.centerXProperty(), x, Interpolator.LINEAR);
		KeyValue endY = new KeyValue(circle.centerYProperty(), y, Interpolator.LINEAR);
		KeyFrame keyFrame = new KeyFrame(Duration.millis(animateDuration), endX, endY);
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(keyFrame);
		node.setCollapse(false);
		circle.setVisible(true);
		timeline.play();
	}

	private static void collapse(TreeNode node, DoubleProperty xp, DoubleProperty yp) {
		VisualNode vn = (VisualNode) node;
		setCollapseBindings(vn, xp, yp);
		vn.setCollapse(true);
		for (TreeNode child : node.getChildren())
			collapse(child, xp, yp);
	}

	private static void setCollapseBindings(VisualNode node, DoubleProperty xp, DoubleProperty yp) {
		Circle circle = (Circle) node.getSymbol();
		// Some subtrees may already be collapsed
		if (node.isCollapsed()) {
			circle.centerXProperty().unbind();
			circle.centerYProperty().unbind();
		}
		KeyValue endX = new KeyValue(circle.centerXProperty(), xp.getValue(), Interpolator.LINEAR);
		KeyValue endY = new KeyValue(circle.centerYProperty(), yp.getValue(), Interpolator.LINEAR);
		KeyFrame keyFrame = new KeyFrame(Duration.millis(animateDuration), endX, endY);
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(keyFrame);
		timeline.setOnFinished((e) -> {
			circle.setVisible(false);
			circle.centerXProperty().bind(xp);
			circle.centerYProperty().bind(yp);
		});
		timeline.play();
	}

	@Override
	public void collapseTreeFrom(VisualNode node) {
		collapseTree(node);

	}

	@Override
	public void expandTreeFrom(VisualNode node) {
		expandTree(node);

	}

	@Override
	public void removeView(VisualNode visualNode) {
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) visualNode.getSymbol());
		sceneNodes.add((Node) visualNode.getText());
		sceneNodes.add((Node) visualNode.getParentLine());
		for (VisualNode child : visualNode.getChildren()) {
			sceneNodes.add((Node) child.getParentLine());
			child.removeParentLine();
		}
		for (Edge e : visualNode.edges()) {
			VisualEdge ve = (VisualEdge) e;
			sceneNodes.add((Node) ve.getText());
			sceneNodes.add((Node) ve.getSymbol());
		}
		pane.getChildren().removeAll(sceneNodes);
	}

	@Override
	public void onNewEdge(VisualEdge edge) {
		createGraphLine(edge, showGraphLine);

	}

	// Is this ever used???
	@Override
	public void removeView(VisualEdge edge) {
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) edge.getText());
		sceneNodes.add((Node) edge.getSymbol());
		pane.getChildren().removeAll(sceneNodes);

	}

	@Override
	public void onNewParent(VisualNode child) {
		createTreeLines(child, showTreeLine);

	}

	@Override
	public void doLayout() {
		pane.setPrefHeight(pane.getHeight());
		pane.setPrefWidth(pane.getWidth());
		Layout layout = new TreeLayout(visualGraph);
		layout.compute();

		for (VisualNode node : visualGraph.nodes()) {
			Circle c = (Circle) node.getSymbol();
			if (!c.centerXProperty().isBound()) {
				c.centerXProperty().set(node.getX() * pane.getWidth());
				c.centerYProperty().set(node.getY() * pane.getHeight());
			}
		}
		pane.setPrefHeight(Control.USE_COMPUTED_SIZE);
		pane.setPrefWidth(Control.USE_COMPUTED_SIZE);
		GraphState.setChanged();
	}

	@Override
	public void onRemoveParentLink(VisualNode vnChild) {
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) vnChild.getParentLine());
		vnChild.removeParentLine();	
		pane.getChildren().removeAll(sceneNodes);
	}
}
