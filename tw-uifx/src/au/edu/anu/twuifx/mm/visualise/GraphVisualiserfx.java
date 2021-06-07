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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import fr.cnrs.iees.uit.space.Distance;
//import org.apache.commons.math.util.MathUtils;

import au.edu.anu.rscs.aot.queries.base.SequenceQuery;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.Originator;
import au.edu.anu.twapps.mm.graphEditor.IGraphVisualiser;
import au.edu.anu.twapps.mm.graphEditor.VisualNodeEditor;
import au.edu.anu.twapps.mm.layout.ILayout;
import au.edu.anu.twapps.mm.layout.LayoutType;
import au.edu.anu.twapps.mm.visualGraph.ElementDisplayText;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.mm.editors.structure.StructureEditorfx;
import au.edu.anu.twuifx.mm.visualise.layout.FRLayout;
import au.edu.anu.twuifx.mm.visualise.layout.OTLayout;
import au.edu.anu.twuifx.mm.visualise.layout.RT1Layout;
import au.edu.anu.twuifx.mm.visualise.layout.RT2Layout;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.Edge;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.twcore.constants.ConfigurationReservedNodeId;
import fr.ens.biologie.generic.utils.Duple;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;

/**
 * Author Ian Davies
 *
 * Date 28 Jan. 2019
 */
public final class GraphVisualiserfx implements IGraphVisualiser {
	public static final Double animateSlow = 1000.0;
	public static final Double animateFast = 1.0;

	private TreeGraph<VisualNode, VisualEdge> visualGraph;
	private final Pane pane;
	private final DoubleProperty nodeRadius;
	private final DoubleProperty lineWidth;
	private final BooleanProperty parentLineVisibleProperty;
	private final BooleanProperty edgeLineVisibleProperty;

	// private final BooleanProperty sideLineProperty;
	private final DropShadow dropShadow;
	private final ColorAdjust colorAdjust;
	private final ObjectProperty<Font> font;
	private final Color hoverColor;
	private final Color treeEdgeColor;
	private final Color graphEdgeColor;
	private final static Interpolator interpolator = Interpolator.EASE_BOTH;
	private final IMMController controller;
	private final Originator recorder;
	private final ReadOnlyObjectProperty<ElementDisplayText> nodeSelect;
	private final ReadOnlyObjectProperty<ElementDisplayText> edgeSelect;
	private final ReadOnlyObjectProperty<Integer> pathLength;

	public GraphVisualiserfx(TreeGraph<VisualNode, VisualEdge> visualGraph, //
			Pane pane, //
			DoubleProperty nodeRadius, //
			DoubleProperty lineWidth, //
			BooleanProperty showTreeLine, //
			BooleanProperty showGraphLine, //
			ReadOnlyObjectProperty<ElementDisplayText> nodeSelect, //
			ReadOnlyObjectProperty<ElementDisplayText> edgeSelect, //
			BooleanProperty sideline, //
//			BooleanProperty neighMode, 
			ReadOnlyObjectProperty<Integer> pathLength, //
			ObjectProperty<Font> font, //
			IMMController controller, //
			Originator recorder) {
		this.visualGraph = visualGraph;
		this.pane = pane;
		this.nodeRadius = nodeRadius;
		this.lineWidth = lineWidth;
		this.edgeLineVisibleProperty = showGraphLine;
		this.parentLineVisibleProperty = showTreeLine;

		// this.sideLineProperty = sideline;
		this.font = font;
		this.controller = controller;
		this.recorder = recorder;
		this.edgeSelect = edgeSelect;
		this.nodeSelect = nodeSelect;
//		this.neighMode = neighMode;
		this.pathLength = pathLength;

		dropShadow = new DropShadow();
		colorAdjust = new ColorAdjust();
		colorAdjust.setBrightness(0.8);

		hoverColor = Color.RED;
		treeEdgeColor = Color.MEDIUMSEAGREEN;
		graphEdgeColor = Color.INDIANRED;

		this.nodeSelect.addListener(e_ -> {
			for (VisualNode n : visualGraph.nodes()) {
				Text txt = (Text) n.getText();
				txt.setText(n.getDisplayText(nodeSelect.get()));
			}
		});
		this.edgeSelect.addListener(e -> {
			for (VisualNode n : visualGraph.nodes()) {
				for (Edge edge : n.edges(Direction.OUT)) {
					VisualEdge ve = (VisualEdge) edge;
					Text txt = (Text) ve.getText();
					txt.setText(ve.getDisplayText(edgeSelect.get()));
				}
			}
		});

	}

	@Override
	public void initialiseView(double duration) {
//		animateDuration = animateDurationFast;
		pane.setPrefHeight(pane.getHeight());
		pane.setPrefWidth(pane.getWidth());

		for (VisualNode n : visualGraph.nodes()) {
			createNodeVisualisation(n);
		}
		List<VisualNode> collapsedParents = new ArrayList<>();

		for (VisualNode n : visualGraph.nodes()) {
			createParentLines(n, parentLineVisibleProperty);
			createGraphLines(n, edgeLineVisibleProperty);
			if (!n.isCollapsed() && n.hasCollaspedChild())
				collapsedParents.add(n);
		}
		resetZorder(pane.getChildren());

		for (VisualNode parent : collapsedParents)
			for (VisualNode child : parent.getChildren()) {
				if (child.isCollapsed())
					collapseTree(child, false, duration);
			}
		Set<VisualNode> visibleNodes = new HashSet<>();

		for (VisualNode n : visualGraph.nodes())
			if (n.isVisible())
				visibleNodes.add(n);

		updateGraphVisibility(visualGraph, visibleNodes, parentLineVisibleProperty, edgeLineVisibleProperty);

		setLayoutNode(null);
	}

	private void resetZorder(ObservableList<Node> nodeList) {
		List<Node> lstText = new ArrayList<>();
		List<Node> lstCircle = new ArrayList<>();
		List<Node> lstLines = new ArrayList<>();
		List<Node> lstArrows = new ArrayList<>();
		for (Node n : nodeList) {
			if (n instanceof Text)
				lstText.add(n);
			else if (n instanceof Circle)
				lstCircle.add(n);
			else if (n instanceof Arrowhead)
				lstArrows.add(n);
			else if (n instanceof Line)
				lstLines.add(n);
		}
		for (Node n : lstLines)
			n.toFront();
		for (Node n : lstArrows)
			n.toFront();
		for (Node n : lstText)
			n.toFront();
		for (Node n : lstCircle)
			n.toFront();
	}

	@Override
	public TreeGraph<VisualNode, VisualEdge> getVisualGraph() {
		return visualGraph;
	}

	@Override
	public void close() {
		pane.getChildren().clear();

	}

	private VisualNode dragNode;

	private boolean dimmingOn;

	private void createNodeVisualisation(VisualNode n) {
		double x = n.getX() * pane.getWidth();
		double y = n.getY() * pane.getHeight();

		Circle c = new Circle(x, y, nodeRadius.get());
		c.radiusProperty().bind(nodeRadius);
		Text text = new Text(n.getDisplayText(nodeSelect.get()));
		n.setVisualElements(c, text);
		Color nColor = TreeColours.getCategoryColor(n.getCategory(), n.cClassId());

		c.fillProperty().bind(Bindings.when(c.hoverProperty()).then(hoverColor).otherwise(nColor));
//		c.setStroke(Color.BLACK);
		c.setOnMouseEntered(e -> {
			if (e.isShiftDown()) {
				dimmingOn = true;
				onHighlightLocalGraph(n, pathLength.getValue());
			} else if (dimmingOn) {
				onHighlightAll();
				dimmingOn = false;
			}
		});

		c.setOnMouseExited(e -> {
//			if (dimmingOn) {
//				onHighlightAll();
//				dimmingOn = false;
//			}
		});

		c.setEffect(dropShadow);
		c.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown()) {
				dragNode = n;
//				e.consume();
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
			if (e.getButton() == MouseButton.SECONDARY && !e.isControlDown()) {
				new StructureEditorfx(new VisualNodeEditor(n, visualGraph), e, controller, this, recorder);
				e.consume();
			} else if (e.getButton() == MouseButton.SECONDARY && e.isControlDown()) {
				setLayoutNode(n);
//				e.consume();
			} else {
				controller.onNodeSelected(n);
//				e.consume();
			}
		});

		text.fontProperty().bind(font);

		// Bind text position relative to circle center
		text.xProperty().bind(c.centerXProperty().add(nodeRadius));
		text.yProperty().bind(c.centerYProperty());
		text.visibleProperty().bind(c.visibleProperty());
		pane.getChildren().addAll(c, text);

	}

	@Override
	public void setLayoutNode(VisualNode newRoot) {
		if (newRoot == null)
			newRoot = getTWRoot();
		if (!newRoot.isVisible() || newRoot.isCollapsed())
			newRoot = getTWRoot();
		if (!visualGraph.nodes().contains(newRoot))
			newRoot = getTWRoot();
		VisualNode oldRoot = controller.setLayoutRoot(newRoot);
		if (oldRoot != null)
			((Circle) oldRoot.getSymbol()).setStroke(null);
		if (newRoot.cClassId().equals(N_ROOT.label()))
			((Circle) newRoot.getSymbol()).setStroke(Color.WHITE);
		else
			((Circle) newRoot.getSymbol()).setStroke(Color.BLACK);
	}

	@Override
	public void onNewNode(VisualNode node) {
		createNodeVisualisation(node);
		createParentLines(node, parentLineVisibleProperty);
		resetZorder(pane.getChildren());
	}

	private void createParentLines(VisualNode child, BooleanProperty show) {
		// each node has a line connected to its parent
		VisualNode parent = child.getParent();
		if (parent != null) {
			Circle parentCircle = (Circle) parent.getSymbol();
			Circle childCircle = (Circle) child.getSymbol();
			Line line = new Line();
			line.strokeWidthProperty().bind(lineWidth);

			line.setStroke(treeEdgeColor);
			line.startXProperty().bind(parentCircle.centerXProperty());
			line.startYProperty().bind(parentCircle.centerYProperty());

			line.endXProperty().bind(childCircle.centerXProperty());
			line.endYProperty().bind(childCircle.centerYProperty());

			line.visibleProperty().bind(show);
			Arrowhead arrow = new Arrowhead(line, nodeRadius, lineWidth);

			child.setParentLine(line, arrow);

			pane.getChildren().add(line);
			pane.getChildren().add(arrow);

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

		Circle fromCircle = (Circle) startNode.getSymbol();
		Circle toCircle = (Circle) endNode.getSymbol();
		Line line = new Line();
		line.strokeWidthProperty().bind(lineWidth);
		Text text = new Text(edge.getDisplayText(edgeSelect.get()));
		text.fontProperty().bind(font);
		// TODO use property here
		line.setStroke(graphEdgeColor);

		// Bindings
		line.startXProperty().bind(fromCircle.centerXProperty());
		line.startYProperty().bind(fromCircle.centerYProperty());

		line.endXProperty().bind(toCircle.centerXProperty());
		line.endYProperty().bind(toCircle.centerYProperty());

		line.visibleProperty().bind(show);

		Arrowhead arrow = new Arrowhead(line, nodeRadius, lineWidth);

		edge.setVisualElements(line, arrow, text);

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
		pane.getChildren().addAll(line, arrow, text);

	}

	protected void textPropertyChange(Line line, Text text, VisualNode startNode, VisualNode endNode) {
		boolean collapsed = startNode.isCollapsed() || endNode.isCollapsed();
//		double[] p1 = { line.getStartX(), line.getStartY() };
//		double[] p2 = { line.getEndX(), line.getEndY() };
		double distance = Distance.euclidianDistance(line.getStartX(), line.getStartY(), line.getEndX(),
				line.getEndY());
		// or dy small (horizontal) and dx shorter than label??
		if ((distance < (4 * nodeRadius.get())) | collapsed) {
			text.visibleProperty().unbind();
			text.setVisible(false);
		} else {
			text.visibleProperty().bind(line.visibleProperty());
		}
	}

	private static void setExpandBindings(VisualNode node, double w, double h,
			BooleanProperty parentLineVisibleProperty, List<Animation> timelines, double duration) {
		Circle circle = (Circle) node.getSymbol();
		double x = node.getX();
		double y = node.getY();
		x = w * x;
		y = h * y;
		circle.centerXProperty().unbind();
		circle.centerYProperty().unbind();
		node.setCollapse(false);
		circle.setVisible(true);
		node.setVisible(true);
		if (node.getParent() != null && node.getParent().isVisible()) {
			Shape s = (Shape) node.getParentLine().getFirst();
			if (!s.visibleProperty().isBound())
				s.visibleProperty().bind(parentLineVisibleProperty);
		}

		animateTo(circle, x, y, timelines, duration);
	}

	private static void collapse(TreeNode parent, DoubleProperty xp, DoubleProperty yp, List<Animation> timelines,
			double duration) {
		VisualNode vParent = (VisualNode) parent;
		setCollapseBindings(vParent, xp, yp, timelines, duration);
		vParent.setCollapse(true);
		for (TreeNode child : parent.getChildren())
			collapse(child, xp, yp, timelines, duration);
	}

	private static void setCollapseBindings(VisualNode node, DoubleProperty xp, DoubleProperty yp,
			List<Animation> timelines, double duration) {
		Circle circle = (Circle) node.getSymbol();
		// Some subtrees may already be collapsed
		if (node.isCollapsed()) {
			circle.centerXProperty().unbind();
			circle.centerYProperty().unbind();
		}

		KeyValue endX = new KeyValue(circle.centerXProperty(), xp.getValue(), Interpolator.EASE_BOTH);
		KeyValue endY = new KeyValue(circle.centerYProperty(), yp.getValue(), Interpolator.EASE_BOTH);
		KeyFrame keyFrame = new KeyFrame(Duration.millis(duration), endX, endY);
		Timeline timeline = new Timeline();
		timelines.add(timeline);
		timeline.getKeyFrames().add(keyFrame);

		timeline.setOnFinished((e) -> {
			circle.setVisible(false);
			node.setVisible(false);
			circle.centerXProperty().bind(xp);
			circle.centerYProperty().bind(yp);
		});
		// timeline.play();
	}

	@Override
	public void collapseTreeFrom(VisualNode childRoot, boolean record, double duration) {
		collapseTree(childRoot, record, duration);
	}

	private void collapseTree(VisualNode childRoot, boolean record, double duration) {
		List<Animation> timelines = new ArrayList<>();
		VisualNode parent = childRoot.getParent();
		Circle circle = (Circle) parent.getSymbol();
		DoubleProperty xp = circle.centerXProperty();
		DoubleProperty yp = circle.centerYProperty();
		// Recursively, collapse the tree to the position of the parent of the tree.
		collapse(childRoot, xp, yp, timelines, duration);

		ParallelTransition pt = new ParallelTransition();
		pt.getChildren().addAll(timelines);
		pt.setOnFinished(e -> {
			// Hide every edge between this tree and other non-collapsed nodes
			hideEdges(childRoot);
			setLayoutNode(controller.getLayoutRoot());

			if (record) {
				// Don't do this so "record" can be removed sometime
				// Problem stemming from CollapseAll
//				String desc = "Collapse [" + childRoot.getConfigNode().toShortString() + "]";
//				recorder.addState(desc);
//				GraphState.setChanged();
			}
		});
		pt.play();
	}

	private static void hideEdges(VisualNode vNode) {
		// May not word due to threading.
		for (Edge e : vNode.edges()) {
			VisualNode sn = (VisualNode) e.startNode();
			VisualNode en = (VisualNode) e.endNode();
			if (sn.isCollapsed() || en.isCollapsed()) {
				VisualEdge ve = (VisualEdge) e;
				Line line = (Line) ve.getSymbol().getFirst();
				if (line.visibleProperty().isBound()) {
					line.visibleProperty().unbind();
					line.setVisible(false);
				}
			}
		}
		for (VisualNode vChild : vNode.getChildren())
			hideEdges(vChild);
	}

	@Override
	public void expandTreeFrom(VisualNode childRoot, boolean record, double duration) {
		expandTree(childRoot, record, duration);
	}

	private void expandTree(VisualNode childRoot, boolean record, double duration) {
		List<Animation> timelines = new ArrayList<>();
		double w = pane.getWidth();
		double h = pane.getHeight();
//		double duration = animateDuration;
		// Recursively unbind nodes from the position of the sub-tree's parent.
		expand(childRoot, w, h, parentLineVisibleProperty, timelines, duration);

		ParallelTransition pt = new ParallelTransition();
		pt.getChildren().addAll(timelines);
		pt.setOnFinished(e -> {
			// rebind edges to the showGraphLine property.
			showEdges(childRoot, edgeLineVisibleProperty);
			if (record) {
				// Don't save collapsed cmds - there are too many. Therefore, remove "record"
				// arg sometime.
				// Problem stemming from CollapseAll
//				String desc = "Expand [" + childRoot.getConfigNode().toShortString() + "]";
//				recorder.addState(desc);
//				GraphState.setChanged();
			}
		});
		pt.play();
	}

	private static void showEdges(VisualNode vNode, BooleanProperty show) {
		for (Edge e : vNode.edges()) {
			VisualNode sn = (VisualNode) e.startNode();
			VisualNode en = (VisualNode) e.endNode();
			if (!sn.isCollapsed() && !en.isCollapsed())
				if (sn.isVisible() && en.isVisible()) {
					VisualEdge ve = (VisualEdge) e;
					ve.setVisible(true);
					Line line = (Line) ve.getSymbol().getFirst();
					if (!line.visibleProperty().isBound()) {
						line.visibleProperty().bind(show);
					}
				}
		}
		for (VisualNode vChild : vNode.getChildren())
			showEdges(vChild, show);
	}

	private static void expand(TreeNode parent, double w, double h, BooleanProperty parentLineVisibleProperty,
			List<Animation> timelines, double duration) {
		setExpandBindings((VisualNode) parent, w, h, parentLineVisibleProperty, timelines, duration);
		for (TreeNode child : parent.getChildren())
			expand(child, w, h, parentLineVisibleProperty, timelines, duration);
	}

	@Override
	public void removeView(VisualNode visualNode) {
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) visualNode.getSymbol());
		sceneNodes.add((Node) visualNode.getText());
		sceneNodes.add((Node) visualNode.getParentLine().getFirst());
		sceneNodes.add((Node) visualNode.getParentLine().getSecond());
		for (VisualNode child : visualNode.getChildren()) {
			sceneNodes.add((Node) child.getParentLine().getFirst());
			sceneNodes.add((Node) child.getParentLine().getSecond());
			child.removeParentLine();
		}
		for (Edge e : visualNode.edges()) {
			VisualEdge ve = (VisualEdge) e;
			sceneNodes.add((Node) ve.getText());
			sceneNodes.add((Node) ve.getSymbol().getFirst());
			sceneNodes.add((Node) ve.getSymbol().getSecond());
		}
		pane.getChildren().removeAll(sceneNodes);
	}

	@Override
	public void onNewEdge(VisualEdge edge, double duration) {
		createGraphLine(edge, edgeLineVisibleProperty);
		VisualNode vn = (VisualNode) edge.endNode();
		if (vn.isCollapsed()) {
			// animate fadeout otherwise it looks like nothing happened
			Duple<Object, Object> line = edge.getSymbol();
			Line body = (Line) line.getFirst();
			Line arrow = (Line) line.getSecond();
			if (body.visibleProperty().isBound()) {
				body.visibleProperty().unbind();
			}
			FadeTransition bodyFade = new FadeTransition();
			bodyFade.setDuration(Duration.millis(duration));
			bodyFade.setFromValue(10);
			bodyFade.setToValue(0.1);
			bodyFade.setNode(body);

			FadeTransition arrowFade = new FadeTransition();
			arrowFade.setDuration(Duration.millis(duration));
			arrowFade.setFromValue(10);
			arrowFade.setToValue(0.1);
			arrowFade.setNode(arrow);

			ParallelTransition pt = new ParallelTransition();

			pt.getChildren().addAll(bodyFade, arrowFade);
			pt.setOnFinished(e -> {
				body.setVisible(false);// arrow vis is bound to this
				edge.setVisible(false);
				body.setOpacity(10);
				arrow.setOpacity(10);
			});
			pt.play();

		}
		resetZorder(pane.getChildren());
	}

	@Override
	public void removeView(VisualEdge edge) {
		removeFromBinding(edge);
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) edge.getText());
		sceneNodes.add((Node) edge.getSymbol().getFirst());
		sceneNodes.add((Node) edge.getSymbol().getSecond());
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
		}

	}

	@Override
	public void onNewParent(VisualNode child) {
		createParentLines(child, parentLineVisibleProperty);
		resetZorder(pane.getChildren());
	}

	@Override
	public void onRemoveParentLink(VisualNode vnChild) {
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) vnChild.getParentLine().getFirst());
		sceneNodes.add((Node) vnChild.getParentLine().getSecond());
		vnChild.removeParentLine();
		pane.getChildren().removeAll(sceneNodes);
	}

	@Override
	public void onNodeRenamed(VisualNode vNode) {
		Text text = (Text) vNode.getText();
		text.setText(vNode.getDisplayText(nodeSelect.get()));
	}

	@Override
	public void onEdgeRenamed(VisualEdge vEdge) {
		Text text = (Text) vEdge.getText();
		text.setText(vEdge.getDisplayText(edgeSelect.get()));
	}

	private VisualNode getTWRoot() {
		for (VisualNode n : visualGraph.nodes()) {
			if (n.cClassId().equals(fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.N_ROOT.label()))
				return n;
		}
		return null;
	}

	@Override
	public void doLayout(VisualNode root, double jitterFraction, LayoutType layoutType, boolean pcShowing,
			boolean xlShowing, boolean sideline, double duration) {
		/**
		 * Can we figure out a way to show just the local neighbourhood to some path
		 * length?
		 * 
		 * use a radial layout and avoid circular paths
		 * 
		 * We want to show all children and the parent AND cross edges but no others
		 * This is easy enough. But the problem is how to hide a specific list of edges.
		 */

		if (root == null)
			root = getTWRoot();

		ILayout layout;
		switch (layoutType) {
		case OrderedTree: {
			layout = new OTLayout(root, pcShowing, xlShowing, sideline);
			break;
		}
		case RadialTree1: {
			layout = new RT1Layout(root, pcShowing, xlShowing, sideline);
			break;
		}
		case RadialTree2: {
			layout = new RT2Layout(root, pcShowing, xlShowing, sideline);
			break;
		}
		case SpringGraph: {
			layout = new FRLayout(visualGraph, pcShowing, xlShowing, sideline);
			break;
		}
//		case LombardiGraph: {
//			layout = new LmbLayout(visualGraph, pcShowing, xlShowing, sideline);
//			break;
//		}
		default: {
			throw new TwuifxException("Unknown layout type '" + layoutType + "',");
		}
		}

		layout.compute(jitterFraction);

		// always scaled within unit space

		// rescale to provide border for a little node radius
		Timeline timeline = new Timeline();

		for (VisualNode node : visualGraph.nodes())
			if (!node.isCollapsed()) {
				double x = ILayout.rescale(node.getX(), 0, 1, 0.05, 0.95);
				double y = ILayout.rescale(node.getY(), 0, 1, 0.05, 0.95);
				node.setPosition(x, y);
				Circle c = (Circle) node.getSymbol();
				KeyFrame f = getKeyFrame(c, node.getX() * pane.getWidth(), node.getY() * pane.getHeight(), duration);
				timeline.getKeyFrames().add(f);
			}
		timeline.play();

		GraphState.setChanged();
	}

	private static KeyFrame getKeyFrame(Circle c, double x, double y, double duration) {
		KeyValue endX = new KeyValue(c.centerXProperty(), x, interpolator);
		KeyValue endY = new KeyValue(c.centerYProperty(), y, interpolator);
		return new KeyFrame(Duration.millis(duration), endX, endY);
	}

	private static void animateTo(Circle c, double x, double y, List<Animation> timelines, double duration) {
		KeyValue endX = new KeyValue(c.centerXProperty(), x, interpolator);
		KeyValue endY = new KeyValue(c.centerYProperty(), y, interpolator);
		KeyFrame keyFrame = new KeyFrame(Duration.millis(duration), endX, endY);
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(keyFrame);
		timelines.add(timeline);
		// timeline.play();
	}

	@Override
	public void collapsePredef() {
		for (VisualNode root : visualGraph.roots()) {
			if (root.cClassId().equals(N_ROOT.label())) {
				VisualNode predef = (VisualNode) get(root.getChildren(),
						selectOne(hasTheName(ConfigurationReservedNodeId.categories.id())));
				collapseTree(predef, false, 1.0);
			}
		}
	}

	@Override
	public void onHighlightAll() {
		Set<VisualNode> highlightNodes = new HashSet<>();
		for (VisualNode n : visualGraph.nodes())
//			if (!n.isCollapsed())
			highlightNodes.add(n);
		updateElementColour(highlightNodes);
	}

	@Override
	public void onShowLocalGraph(VisualNode root, int pathLength) {
		Set<VisualNode> visibleNodes = new HashSet<>();
		traversal(parentLineVisibleProperty.getValue(), edgeLineVisibleProperty.getValue(), root, 0, pathLength, visibleNodes);
		visibleNodes.add(root);
		updateGraphVisibility(visualGraph, visibleNodes, parentLineVisibleProperty, edgeLineVisibleProperty);
	}

	@Override
	public void onShowAll() {
		Set<VisualNode> visibleNodes = new HashSet<>();
		for (VisualNode n : visualGraph.nodes())
			if (!n.isCollapsed())
				visibleNodes.add(n);
		updateGraphVisibility(visualGraph, visibleNodes, parentLineVisibleProperty, edgeLineVisibleProperty);
	}

	@Override
	public void onHighlightLocalGraph(VisualNode root, int pathLength) {
		Set<VisualNode> focusNodes = new HashSet<>();
		traversal(parentLineVisibleProperty.getValue(), edgeLineVisibleProperty.getValue(), root, 0, pathLength,
				focusNodes);
		focusNodes.add(root);
		updateElementColour(focusNodes);
	}

	private void updateElementColour(Set<VisualNode> focusNodes) {
		for (VisualNode n : visualGraph.nodes())
			dimNode(n);

		ObservableList<Node> obs = FXCollections.observableArrayList();
		for (VisualNode n : focusNodes)
			unDimNode(n, focusNodes, obs);

		resetZorder(obs);

	}

	@SuppressWarnings("unchecked")
	private void unDimNode(VisualNode n, Set<VisualNode> focusNodes, ObservableList<Node> obs) {
		Shape c = (Shape) n.getSymbol();
		if (c != null) {// will be null during node creation
			c.setEffect(dropShadow);
			obs.add(c);
			Text t = (Text) n.getText();
			obs.add(t);
			t.setEffect(null);
			if (n.getParent() != null && (focusNodes.contains(n.getParent()))) {
				Duple<Object, Object> dpl = n.getParentLine();
				((Line) dpl.getFirst()).setEffect(null);
				((Line) dpl.getSecond()).setEffect(null);
				obs.add((Line) dpl.getFirst());
				obs.add((Line) dpl.getSecond());
			}
			for (VisualEdge e : (Iterable<VisualEdge>) get(n.edges(Direction.OUT))) {
				if (focusNodes.contains(e.endNode())) {
					Duple<Object, Object> dpl = e.getSymbol();
					((Shape) dpl.getFirst()).setEffect(null);
					((Shape) dpl.getSecond()).setEffect(null);
					((Text) e.getText()).setEffect(null);
					obs.add((Line) dpl.getFirst());
					obs.add((Line) dpl.getSecond());
					obs.add((Text) e.getText());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void dimNode(VisualNode n) {
		Shape c = (Shape) n.getSymbol();
		if (c == null)
			return;// may occur when creating a node!
		c.setEffect(colorAdjust);
		Text t = (Text) n.getText();
		t.setEffect(colorAdjust);
		if (n.getParent() != null) {
			((Line) n.getParentLine().getFirst()).setEffect(colorAdjust);
			((Line) n.getParentLine().getSecond()).setEffect(colorAdjust);
		}
		for (VisualEdge e : (Iterable<VisualEdge>) get(n.edges(Direction.OUT))) {
			((Shape) e.getSymbol().getFirst()).setEffect(colorAdjust);
			((Shape) e.getSymbol().getSecond()).setEffect(colorAdjust);
			((Text) e.getText()).setEffect(colorAdjust);
		}
	}

	@SuppressWarnings("unchecked")
	private static void showNode(VisualNode n, BooleanProperty parentVisibleProperty,
			BooleanProperty edgeVisibleProperty, Set<VisualNode> visibleNodes) {
		Shape s = (Shape) n.getSymbol();
		s.setVisible(true);
		n.setVisible(true);
		// no binding for node visibility
		if (n.getParent() != null) {
			if (visibleNodes.contains(n.getParent())) {
				s = (Shape) n.getParentLine().getFirst();
				s.visibleProperty().bind(parentVisibleProperty);
				// TODO check what happens to bindings on collapse /
				// expand
			}
		}
		for (VisualEdge e : (Iterable<VisualEdge>) get(n.edges(Direction.OUT))) {
			if (visibleNodes.contains(e.endNode())) {
				s = (Shape) e.getSymbol().getFirst();
				s.visibleProperty().bind(edgeVisibleProperty);
				e.setVisible(s.isVisible());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void hideNode(VisualNode n) {
		hideShape((Shape) n.getSymbol());
		n.setVisible(false);
		if (n.getParent() != null) {
			hideShape((Shape) n.getParentLine().getFirst());
		}
		for (VisualEdge e : (Iterable<VisualEdge>) get(n.edges(Direction.OUT))) {
			hideShape((Shape) e.getSymbol().getFirst());
			e.setVisible(false);
		}
	}

	private static void updateGraphVisibility(TreeGraph<VisualNode, VisualEdge> g, Set<VisualNode> visibleNodes,
			BooleanProperty parentLineVisibleProperty, BooleanProperty edgeLineVisibleProperty) {
		for (VisualNode n : g.nodes())
			hideNode(n);
		for (VisualNode n : visibleNodes) {
			showNode(n, parentLineVisibleProperty, edgeLineVisibleProperty, visibleNodes);
		}

	}

	private static void hideShape(Shape s) {
		if (s.visibleProperty().isBound())
			s.visibleProperty().unbind();
		s.setVisible(false);
	}

	@SuppressWarnings("unchecked")
	private static void traversal(boolean treeVisible, boolean edgesVisible, VisualNode root, int depth, int pathLength,
			Set<VisualNode> nnNodes) {
		if (depth < pathLength) {
			Set<VisualNode> nn = new HashSet<>();

			if (treeVisible) {
				VisualNode parent = root.getParent();
				if (parent != null && !nnNodes.contains(parent))
					nn.add(root.getParent());

				for (VisualNode n : root.getChildren())
					if (!n.isCollapsed())
						if (!nnNodes.contains(n))
							nn.add(n);
			}
			
			if (edgesVisible) {
				Collection<VisualEdge> outEdges = (Collection<VisualEdge>) get(root.edges(Direction.OUT));
				for (VisualEdge e : outEdges) {
					VisualNode endNode = (VisualNode) e.endNode();
					if (!endNode.isCollapsed())
						if (!nnNodes.contains(endNode))
							nn.add(endNode);
				}

				Collection<VisualEdge> inEdges = (Collection<VisualEdge>) get(root.edges(Direction.IN));
				for (VisualEdge e : inEdges) {
					VisualNode startNode = (VisualNode) e.startNode();
					if (!startNode.isCollapsed())
						if (!nnNodes.contains(startNode))
							nn.add(startNode);
				}
			}
			nnNodes.addAll(nn);

			for (VisualNode n : nn)
				traversal(treeVisible, edgesVisible,n, depth + 1, pathLength, nnNodes);

		}
	}

	@Override
	public void onRollback(TreeGraph<VisualNode, VisualEdge> layoutGraph) {
		final double duration = animateFast;
		this.visualGraph = layoutGraph;
		pane.getChildren().clear();
		this.initialiseView(duration);
	}

}
