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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.util.MathUtils;

import au.edu.anu.rscs.aot.queries.base.SequenceQuery;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.graphEditor.IGraphVisualiser;
import au.edu.anu.twapps.mm.graphEditor.VisualNodeEditor;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.layout.TreeLayout;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.mm.editors.structure.StructureEditorfx;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.Edge;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
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
			if (!n.isCollapsed() && n.hasCollaspedChild())
				collapsedParents.add(n);
		}
		for (VisualNode parent : collapsedParents)
			for (VisualNode child : parent.getChildren()) {
				if (child.isCollapsed())
					collapseTree(child);
			}
		resetZorder();

	}

	private void resetZorder() {
		List<Node> lstText = new ArrayList<>();
		List<Node> lstCircle = new ArrayList<>();
		List<Node> lstLines = new ArrayList<>();
		for (Node n : pane.getChildren()) {
			if (n instanceof Text)
				lstText.add(n);
			else if (n instanceof Circle)
				lstCircle.add(n);
			else if (n instanceof Line)
				lstLines.add(n);
		}
		for (Node n : lstText)
			n.toBack();
		for (Node n : lstLines)
			n.toBack();
		for (Node n : lstCircle)
			n.toFront();

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
		Color nColor = TreeColours.getCategoryColor(n.getCategory(),n.cClassId());
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
//				double nx = dragNode.getX() * w;
//				double ny = dragNode.getY() * h;
				if (ex < w && ey < h && ex >= 0 && ey >= 0) {
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
		text.yProperty().bind(c.centerYProperty());
		text.visibleProperty().bind(c.visibleProperty());
		pane.getChildren().addAll(c, text);

	}

	@Override
	public void onNewNode(VisualNode node) {
		createNodeVisualisation(node);
		createTreeLines(node, showTreeLine);
		resetZorder();
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
		String newLabel = edge.getDisplayText(edgeClassOnly);

		Circle fromCircle = (Circle) startNode.getSymbol();
		Circle toCircle = (Circle) endNode.getSymbol();
		Line line = new Line();
		Text text = new Text(newLabel);
		text.fontProperty().bind(font);
		edge.setVisualElements(line, text);
		// TODO use property here
		line.setStroke(graphEdgeColor);

		// Bindings
		line.startXProperty().bind(fromCircle.centerXProperty());
		line.startYProperty().bind(fromCircle.centerYProperty());

		line.endXProperty().bind(toCircle.centerXProperty());
		line.endYProperty().bind(toCircle.centerYProperty());

		line.visibleProperty().bind(show);

		// bind text position to the mid-point of the line
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

		arrangeLineText(startNode, endNode);
		pane.getChildren().addAll(line, text);

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

	private static void collapseTree(VisualNode childRoot) {
		VisualNode parent = childRoot.getParent();
		Circle circle = (Circle) parent.getSymbol();
		DoubleProperty xp = circle.centerXProperty();
		DoubleProperty yp = circle.centerYProperty();
		collapse(childRoot, xp, yp);
	}

	private void expandTree(VisualNode childRoot) {
		double w = pane.getWidth();
		double h = pane.getHeight();
		expand(childRoot, w, h);
	}

	private static void expand(TreeNode parent, double w, double h) {
		setExpandBindings((VisualNode) parent, w, h);
		for (TreeNode child : parent.getChildren())
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

	private static void collapse(TreeNode parent, DoubleProperty xp, DoubleProperty yp) {
		VisualNode vParent = (VisualNode) parent;
		setCollapseBindings(vParent, xp, yp);
		vParent.setCollapse(true);
		for (TreeNode child : parent.getChildren())
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
	public void collapseTreeFrom(VisualNode childRoot) {
		collapseTree(childRoot);
	}

	@Override
	public void expandTreeFrom(VisualNode childRoot) {
		expandTree(childRoot);
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
		resetZorder();
	}

	@Override
	public void removeView(VisualEdge edge) {
		removeFromBinding(edge);
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) edge.getText());
		sceneNodes.add((Node) edge.getSymbol());
		pane.getChildren().removeAll(sceneNodes);
	}

	private void removeFromBinding(VisualEdge edge) {
		List<VisualEdge> edges = getReplicateEdges((VisualNode) edge.startNode(), (VisualNode) edge.endNode());
		if (edges.size() == 1)
			return;
		edges.remove(edge);
		stackEdges(edges);
	}

	private List<VisualEdge> getReplicateEdges(VisualNode startNode, VisualNode endNode) {
		List<VisualEdge> result = new ArrayList<>();
		for (ALEdge e : startNode.edges(Direction.OUT)) {
			VisualEdge ve = (VisualEdge) e;
			if (ve.endNode().id().equals(endNode.id()) && ve.hasText())
				result.add((VisualEdge) e);
		}
		for (ALEdge e : endNode.edges(Direction.OUT)) {
			VisualEdge ve = (VisualEdge) e;
			if (ve.endNode().id().equals(startNode.id()) && ve.hasText())
				result.add((VisualEdge) e);
		}
		return result;
	}

	private void arrangeLineText(VisualNode startNode, VisualNode endNode) {
		List<VisualEdge> edges = getReplicateEdges(startNode, endNode);
		if (edges.size() <= 1)
			// nothing to do
			return;
		stackEdges(edges);
	}

	private void stackEdges(List<VisualEdge> edges) {
		edges.sort(new Comparator<VisualEdge>() {

			@Override
			public int compare(VisualEdge e1, VisualEdge e2) {
				Text t1 = (Text) e1.getText();
				Text t2 = (Text) e2.getText();
				return t1.getText().compareTo(t2.getText());
			}
		});

		// Set the first edge text as per normal i.e mid point of line.
		// Then stack all the remaining texts below it.

		VisualEdge firstEdge = edges.get(0);
		VisualNode startNode = (VisualNode) firstEdge.startNode();
		VisualNode endNode = (VisualNode) firstEdge.endNode();
		Circle fromCircle = (Circle) startNode.getSymbol();
		Circle toCircle = (Circle) endNode.getSymbol();

		Text anchorText = (Text) firstEdge.getText();
		anchorText.xProperty().unbind();
		anchorText.yProperty().unbind();
		anchorText.xProperty().bind(fromCircle.centerXProperty().add(toCircle.centerXProperty()).divide(2.0));
		anchorText.yProperty().bind(fromCircle.centerYProperty().add(toCircle.centerYProperty()).divide(2.0));
		for (int i = 1; i < edges.size(); i++) {
			Text lastText = (Text) edges.get(i - 1).getText();
			Text nextText = (Text) edges.get(i).getText();
			nextText.xProperty().unbind();
			nextText.yProperty().unbind();
			nextText.xProperty().bind(lastText.xProperty());
			nextText.yProperty().bind(lastText.yProperty().add(nextText.boundsInLocalProperty().get().getHeight()));
//			System.out.println(nextText.getText());
		}

	}

	@Override
	public void onNewParent(VisualNode child) {
		createTreeLines(child, showTreeLine);
		resetZorder();
	}

	@Override
	public void doLayout(double jitterFraction) {
		ILayout layout = new TreeLayout(visualGraph).compute();

		Random rnd = new Random();
		double w = pane.getWidth();
		double h = pane.getHeight();
		double nrx = nodeRadius.get() / h;
		double nry = nodeRadius.get() / h;

		Point2D min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point2D max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		for (VisualNode node : visualGraph.nodes())
			if (!node.isCollapsed()) {
				node.setX(jitter(node.getX(), jitterFraction, rnd));
				node.setY(jitter(node.getY(), jitterFraction, rnd));
				Text text = (Text) node.getText();
				double th = Math.max(nrx, text.getLayoutBounds().getHeight() / h);
				double tw = text.getLayoutBounds().getWidth() / w + nrx;
				double top = node.getY() - th;
				double bottom = node.getY() + nry;
				double left = node.getX() - nrx;
				double right = node.getX() + nrx + tw;
				min.setLocation(Math.min(left, min.getX()), Math.min(top, min.getY()));
				max.setLocation(Math.max(right, max.getX()), Math.max(bottom, max.getY()));
			}

		for (VisualNode node : visualGraph.nodes())
			if (!node.isCollapsed()) {
				double x = node.getX();
				// I don't know why the labels don't line up on the rh side?
				double x1 = layout.rescale(x, min.getX(), max.getX(), 0.00, 1.0 - 0.05);
				double y = node.getY();
				double y1 = layout.rescale(y, min.getY(), max.getY(), 0.00, 1.0);
				node.setX(x1);
				node.setY(y1);
				Circle c = (Circle) node.getSymbol();
				c.centerXProperty().set(node.getX() * pane.getWidth());
				c.centerYProperty().set(node.getY() * pane.getHeight());
			}

		GraphState.setChanged();
	}

	private static double jitter(double value, double jitterFraction, Random rnd) {
		double delta = rnd.nextDouble() * jitterFraction;
		if (rnd.nextBoolean())
			return value + delta;
		else
			return value - delta;
	}

	@Override
	public void onRemoveParentLink(VisualNode vnChild) {
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) vnChild.getParentLine());
		vnChild.removeParentLine();
		pane.getChildren().removeAll(sceneNodes);
	}

	@Override
	public void onNodeRenamed(VisualNode vNode) {
		Text text = (Text) vNode.getText();
		text.setText(vNode.getDisplayText(false));
	}

	@Override
	public void onEdgeRenamed(VisualEdge vEdge) {
		Text text = (Text) vEdge.getText();
		text.setText(vEdge.getDisplayText(false));
	}
}
