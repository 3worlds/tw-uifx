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

import java.util.*;
import fr.cnrs.iees.uit.space.Distance;

import au.edu.anu.rscs.aot.queries.base.SequenceQuery;
import au.edu.anu.twapps.mm.*;
import au.edu.anu.twapps.mm.graphEditor.VisualNodeEditor;
import au.edu.anu.twapps.mm.layout.*;
import au.edu.anu.twapps.mm.undo.Originator;
import au.edu.anu.twapps.mm.layoutGraph.*;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twuifx.mm.editors.structure.StructureEditorfx;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.Edge;
import fr.cnrs.iees.graph.TreeNode;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.twcore.constants.ConfigurationReservedNodeId;
import fr.ens.biologie.generic.utils.Duple;
import javafx.animation.*;
import javafx.beans.property.*;
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
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.*;

/**
 * Javafx implementation of {@link IGraphVisualiser}
 * 
 * @author Ian Davies -28 Jan. 2019
 */
public final class GraphVisualiserfx implements GraphVisualiser {
	/**
	 * Speed of configuration graph animation in msec.
	 */
	public static final Double animateSlow = 1000.0;
	/**
	 * Speed of configuration graph animation in msec when particular tasks are
	 * effectively instantaneous (i.e. when hiding the pre-defined sub-tree at start
	 * up).
	 */
	public static final Double animateFast = 1.0;

	private TreeGraph<LayoutNode, LayoutEdge> layoutGraph;
	private final Pane pane;
	private final DoubleProperty nodeRadius;
	private final DoubleProperty lineWidth;
	private final BooleanProperty isParentLineVisible;
	private final BooleanProperty isCrossLinkLineVisible;
	private final BooleanProperty animate;

	private final DropShadow dropShadow;
	private final ColorAdjust colorAdjust;
	private final ObjectProperty<Font> font;
	private final Color hoverColor;
	private final Color treeEdgeColor;
	private final Color graphEdgeColor;
	private final static Interpolator interpolator = Interpolator.EASE_BOTH;
	private final MMController controller;
	private final Originator recorder;
	private final ReadOnlyObjectProperty<ElementDisplayText> nodeSelect;
	private final ReadOnlyObjectProperty<ElementDisplayText> edgeSelect;
	private final ReadOnlyObjectProperty<Integer> pathLength;

	/**
	 * @param visualGraph            The layout graph.
	 * @param pane                   The zoomable drawing pane.
	 * @param animate                Flag to select animation when applying graph
	 *                               layout operations.
	 * @param nodeRadius             The system wide node radius.
	 * @param lineWidth              The system wide line width.
	 * @param isParentLineVisible    Flag to show/hide parent - child lines.
	 * @param isCrossLinkLineVisible Flag to show/hide node cross-link lines.
	 * @param nodeTextOption         Node text display options.
	 * @param edgeTextOption         Edge text display options.
	 * @param sideline               Flag to place nodes with no showing edges to
	 *                               one side of the display.
	 * @param pathLength             The length of the path to traverse when
	 *                               displaying sub-graphs/
	 * @param font                   The font used for all node and edge text.
	 * @param controller             The view controller interface.
	 * @param recorder               The {@link Originator} to record edits in the
	 *                               undo/redo system.
	 */
	public GraphVisualiserfx(TreeGraph<LayoutNode, LayoutEdge> layoutGraph, //
			Pane pane, //
			BooleanProperty animate, //
			DoubleProperty nodeRadius, //
			DoubleProperty lineWidth, //
			BooleanProperty isParentLineVisible, //
			BooleanProperty isCrossLinkLineVisible, //
			ReadOnlyObjectProperty<ElementDisplayText> nodeTextOption, //
			ReadOnlyObjectProperty<ElementDisplayText> edgeTextOption, //
			BooleanProperty sideline, //
			ReadOnlyObjectProperty<Integer> pathLength, //
			ObjectProperty<Font> font, //
			MMController controller, //
			Originator recorder) {
		this.layoutGraph = layoutGraph;
		this.pane = pane;
		this.nodeRadius = nodeRadius;
		this.lineWidth = lineWidth;
		this.isCrossLinkLineVisible = isCrossLinkLineVisible;
		this.isParentLineVisible = isParentLineVisible;
		this.animate = animate;

		this.font = font;
		this.controller = controller;
		this.recorder = recorder;
		this.edgeSelect = edgeTextOption;
		this.nodeSelect = nodeTextOption;
		this.pathLength = pathLength;

		dropShadow = new DropShadow();
		colorAdjust = new ColorAdjust();
		colorAdjust.setBrightness(0.8);

		hoverColor = Color.RED;
		treeEdgeColor = Color.MEDIUMSEAGREEN;
		graphEdgeColor = Color.INDIANRED;
		setTextListeners();
	}

	private void setTextListeners() {
		this.nodeSelect.addListener(e_ -> {
			for (LayoutNode n : layoutGraph.nodes()) {
				Text txt = (Text) n.getText();
				txt.setText(n.getDisplayText(nodeSelect.get()));
			}
		});
		this.edgeSelect.addListener(e -> {
			for (LayoutNode n : layoutGraph.nodes()) {
				for (Edge edge : n.edges(Direction.OUT)) {
					LayoutEdge ve = (LayoutEdge) edge;
					Text txt = (Text) ve.getText();
					txt.setText(ve.getDisplayText(edgeSelect.get()));
				}
			}
		});

	}

	@Override
	public final void initialiseView(double duration) {
		pane.setPrefHeight(pane.getHeight());
		pane.setPrefWidth(pane.getWidth());

		for (LayoutNode n : layoutGraph.nodes()) {
			createNodeVisualisation(n);
		}
		List<LayoutNode> collapsedParents = new ArrayList<>();

		for (LayoutNode n : layoutGraph.nodes()) {
			createParentLines(n, isParentLineVisible);
			createGraphLines(n, isCrossLinkLineVisible);
			if (!n.isCollapsed() && n.hasCollaspedChild())
				collapsedParents.add(n);
		}
		resetZorder(pane.getChildren());

		for (LayoutNode parent : collapsedParents)
			for (LayoutNode child : parent.getChildren()) {
				if (child.isCollapsed())
					collapseTree(child, duration);
			}
		Set<LayoutNode> visibleNodes = new HashSet<>();

		for (LayoutNode n : layoutGraph.nodes())
			if (n.isVisible())
				visibleNodes.add(n);

		updateGraphVisibility(layoutGraph, visibleNodes, isParentLineVisible, isCrossLinkLineVisible);

		setLayoutRoot(null);
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
	public final TreeGraph<LayoutNode, LayoutEdge> getLayoutGraph() {
		return layoutGraph;
	}

	@Override
	public final void close() {
		pane.getChildren().clear();

	}

	private LayoutNode dragNode;

	private boolean dimmingOn;

	private void createNodeVisualisation(LayoutNode n) {
		double x = n.getX() * pane.getWidth();
		double y = n.getY() * pane.getHeight();

		Circle c = new Circle(x, y, nodeRadius.get());

		c.radiusProperty().bind(nodeRadius);
		Text text = new Text(n.getDisplayText(nodeSelect.get()));
		n.setVisualElements(c, text);
		Color nColor = TreeColours.getCategoryColor(n.getCategory());
		c.setFill(nColor);
		c.setEffect(dropShadow);

		c.hoverProperty().addListener((o, ov, nv) -> {
			if (nv)
				c.setFill(hoverColor);
			else
				c.setFill(nColor);
		});

		c.setOnMouseEntered(e -> {
			if (e.isShiftDown()) {
				dimmingOn = true;
				onHighlightLocalGraph(n, pathLength.getValue());
			} else if (dimmingOn) {
				onHighlightAll();
				dimmingOn = false;
			}
		});

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
				if (ex < w && ey < h && ex >= 0 && ey >= 0) {
					Circle dc = (Circle) dragNode.getSymbol();
					dc.setCenterX(ex);
					dc.setCenterY(ey);
					e.consume();
				}
				// e.consume();
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
					String desc = "Move [" + dragNode.configNode().toShortString() + "]";
					recorder.addState(desc);

				}
				dragNode = null;
				e.consume();
			}
		});
		c.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.SECONDARY && !e.isControlDown()) {
				new StructureEditorfx(new VisualNodeEditor(n, layoutGraph), e, controller, this, recorder);
				e.consume();
			} else if (e.getButton() == MouseButton.SECONDARY && e.isControlDown()) {
				setLayoutRoot(n);
			} else {
				controller.onNodeSelected(n);
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
	public final void setLayoutRoot(LayoutNode newRoot) {
		if (newRoot == null)
			newRoot = getTWRoot();
		if (!newRoot.isVisible() || newRoot.isCollapsed())
			newRoot = getTWRoot();
		if (!layoutGraph.nodes().contains(newRoot))
			newRoot = getTWRoot();
		LayoutNode oldRoot = controller.setLayoutRoot(newRoot);
		if (oldRoot != null)
			((Circle) oldRoot.getSymbol()).setStroke(null);
		if (newRoot.isRoot())
			((Circle) newRoot.getSymbol()).setStroke(Color.WHITE);
		else
			((Circle) newRoot.getSymbol()).setStroke(Color.BLACK);
	}

	@Override
	public final void onNewNode(LayoutNode node) {
		createNodeVisualisation(node);
		createParentLines(node, isParentLineVisible);
		resetZorder(pane.getChildren());
	}

	private void createParentLines(LayoutNode child, BooleanProperty show) {
		// each node has a line connected to its parent
		LayoutNode parent = child.getParent();
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

	@SuppressWarnings("unchecked")
	private void createGraphLines(LayoutNode n, BooleanProperty show) {
		Iterable<LayoutEdge> edges = (Iterable<LayoutEdge>) SequenceQuery.get(n.edges(Direction.OUT));
		for (LayoutEdge edge : edges) {
			createGraphLine(edge, show);
		}
	}

	private void createGraphLine(LayoutEdge edge, BooleanProperty show) {

		LayoutNode startNode = (LayoutNode) edge.startNode();
		LayoutNode endNode = (LayoutNode) edge.endNode();

		Circle fromCircle = (Circle) startNode.getSymbol();
		Circle toCircle = (Circle) endNode.getSymbol();
		Line line = new Line();
		line.strokeWidthProperty().bind(lineWidth);
		Text text = new Text(edge.getDisplayText(edgeSelect.get()));
		text.fontProperty().bind(font);
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

	protected void textPropertyChange(Line line, Text text, LayoutNode startNode, LayoutNode endNode) {
		boolean collapsed = startNode.isCollapsed() || endNode.isCollapsed();
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

	private static void setExpandBindings(LayoutNode node, double w, double h,
			BooleanProperty parentLineVisibleProperty, List<Animation> timelines, double duration,
			BooleanProperty animate) {
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
		if (animate.get())
			animateTo(circle, x, y, timelines, duration);
		else {
			circle.setCenterX(x);
			circle.setCenterY(y);
		}

	}

	private static void collapse(TreeNode parent, DoubleProperty xp, DoubleProperty yp, List<Animation> timelines,
			double duration, BooleanProperty animate) {
		LayoutNode vParent = (LayoutNode) parent;
		setCollapseBindings(vParent, xp, yp, timelines, duration, animate);
		vParent.setCollapse(true);
		for (TreeNode child : parent.getChildren())
			collapse(child, xp, yp, timelines, duration, animate);
	}

	private static void setCollapseBindings(LayoutNode node, DoubleProperty xp, DoubleProperty yp,
			List<Animation> timelines, double duration, BooleanProperty animate) {
		Circle circle = (Circle) node.getSymbol();
		// Some subtrees may already be collapsed
		if (node.isCollapsed()) {
			circle.centerXProperty().unbind();
			circle.centerYProperty().unbind();
		}
		if (animate.get()) {

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
		} else {
			circle.setVisible(false);
			node.setVisible(false);
			circle.centerXProperty().bind(xp);
			circle.centerYProperty().bind(yp);
		}
	}

	@Override
	public final void collapseTreeFrom(LayoutNode childRoot, double duration) {
		collapseTree(childRoot, duration);
	}

	private void collapseTree(LayoutNode childRoot, double duration) {
		List<Animation> timelines = new ArrayList<>();
		LayoutNode parent = childRoot.getParent();
		Circle circle = (Circle) parent.getSymbol();
		DoubleProperty xp = circle.centerXProperty();
		DoubleProperty yp = circle.centerYProperty();
		// Recursively, collapse the tree to the position of the parent of the tree.
		collapse(childRoot, xp, yp, timelines, duration, animate);
		if (animate.get()) {
			ParallelTransition pt = new ParallelTransition();
			pt.getChildren().addAll(timelines);
			pt.setOnFinished(e -> {
				// Hide every edge between this tree and other non-collapsed nodes
				hideEdges(childRoot);
				setLayoutRoot(controller.getLayoutRoot());
			});
			pt.play();
		} else {
			hideEdges(childRoot);
			setLayoutRoot(controller.getLayoutRoot());
		}
	}

	private static void hideEdges(LayoutNode vNode) {
		for (Edge e : vNode.edges()) {
			LayoutNode sn = (LayoutNode) e.startNode();
			LayoutNode en = (LayoutNode) e.endNode();
			if (sn.isCollapsed() || en.isCollapsed()) {
				LayoutEdge ve = (LayoutEdge) e;
				Line line = (Line) ve.getSymbol().getFirst();
				if (line.visibleProperty().isBound()) {
					line.visibleProperty().unbind();
					line.setVisible(false);
				}
			}
		}
		for (LayoutNode vChild : vNode.getChildren())
			hideEdges(vChild);
	}

	@Override
	public final void expandTreeFrom(LayoutNode childRoot, double duration) {
		expandTree(childRoot, duration);
	}

	private void expandTree(LayoutNode childRoot, double duration) {
		List<Animation> timelines = new ArrayList<>();
		double w = pane.getWidth();
		double h = pane.getHeight();
		// Recursively unbind nodes from the position of the sub-tree's parent.
		expand(childRoot, w, h, isParentLineVisible, timelines, duration, animate);

		if (animate.get()) {
			ParallelTransition pt = new ParallelTransition();
			pt.getChildren().addAll(timelines);
			pt.setOnFinished(e -> {
				showEdges(childRoot, isCrossLinkLineVisible);
			});
			pt.play();
		} else
			showEdges(childRoot, isCrossLinkLineVisible);
	}

	private static void showEdges(LayoutNode vNode, BooleanProperty show) {
		for (Edge e : vNode.edges()) {
			LayoutNode sn = (LayoutNode) e.startNode();
			LayoutNode en = (LayoutNode) e.endNode();
			if (!sn.isCollapsed() && !en.isCollapsed())
				if (sn.isVisible() && en.isVisible()) {
					LayoutEdge ve = (LayoutEdge) e;
					ve.setVisible(true);
					Line line = (Line) ve.getSymbol().getFirst();
					if (!line.visibleProperty().isBound()) {
						line.visibleProperty().bind(show);
					}
				}
		}
		for (LayoutNode vChild : vNode.getChildren())
			showEdges(vChild, show);
	}

	private static void expand(TreeNode parent, double w, double h, BooleanProperty parentLineVisibleProperty,
			List<Animation> timelines, double duration, BooleanProperty animate) {
		setExpandBindings((LayoutNode) parent, w, h, parentLineVisibleProperty, timelines, duration, animate);
		for (TreeNode child : parent.getChildren())
			expand(child, w, h, parentLineVisibleProperty, timelines, duration, animate);
	}

	@Override
	public final void removeView(LayoutNode visualNode) {
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) visualNode.getSymbol());
		sceneNodes.add((Node) visualNode.getText());
		sceneNodes.add((Node) visualNode.getParentLine().getFirst());
		sceneNodes.add((Node) visualNode.getParentLine().getSecond());
		for (LayoutNode child : visualNode.getChildren()) {
			sceneNodes.add((Node) child.getParentLine().getFirst());
			sceneNodes.add((Node) child.getParentLine().getSecond());
			child.removeParentLine();
		}
		for (Edge e : visualNode.edges()) {
			LayoutEdge ve = (LayoutEdge) e;
			sceneNodes.add((Node) ve.getText());
			sceneNodes.add((Node) ve.getSymbol().getFirst());
			sceneNodes.add((Node) ve.getSymbol().getSecond());
		}
		pane.getChildren().removeAll(sceneNodes);
	}

	@Override
	public final void onNewEdge(LayoutEdge edge, double duration) {
		createGraphLine(edge, isCrossLinkLineVisible);
		LayoutNode vn = (LayoutNode) edge.endNode();
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
	public final void removeView(LayoutEdge edge) {
		removeFromBinding(edge);
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) edge.getText());
		sceneNodes.add((Node) edge.getSymbol().getFirst());
		sceneNodes.add((Node) edge.getSymbol().getSecond());
		pane.getChildren().removeAll(sceneNodes);
	}

	private void removeFromBinding(LayoutEdge edge) {
		List<LayoutEdge> edges = getReplicateEdges((LayoutNode) edge.startNode(), (LayoutNode) edge.endNode());
		if (edges.size() == 1)
			return;
		edges.remove(edge);
		stackEdges(edges);
	}

	private List<LayoutEdge> getReplicateEdges(LayoutNode startNode, LayoutNode endNode) {
		List<LayoutEdge> result = new ArrayList<>();
		for (ALEdge e : startNode.edges(Direction.OUT)) {
			LayoutEdge ve = (LayoutEdge) e;
			if (ve.endNode().id().equals(endNode.id()) && ve.hasText())
				result.add((LayoutEdge) e);
		}
		for (ALEdge e : endNode.edges(Direction.OUT)) {
			LayoutEdge ve = (LayoutEdge) e;
			if (ve.endNode().id().equals(startNode.id()) && ve.hasText())
				result.add((LayoutEdge) e);
		}
		return result;
	}

	private void arrangeLineText(LayoutNode startNode, LayoutNode endNode) {
		List<LayoutEdge> edges = getReplicateEdges(startNode, endNode);
		if (edges.size() <= 1)
			// nothing to do
			return;
		stackEdges(edges);
	}

	private void stackEdges(List<LayoutEdge> edges) {
		edges.sort((e1, e2) -> {
			Text t1 = (Text) e1.getText();
			Text t2 = (Text) e2.getText();
			return t1.getText().compareTo(t2.getText());
		});

		// Set the first edge text as per normal i.e mid point of line.
		// Then stack all the remaining texts below it.

		LayoutEdge firstEdge = edges.get(0);
		LayoutNode startNode = (LayoutNode) firstEdge.startNode();
		LayoutNode endNode = (LayoutNode) firstEdge.endNode();
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
	public final void onNewParent(LayoutNode child) {
		createParentLines(child, isParentLineVisible);
		resetZorder(pane.getChildren());
	}

	@Override
	public final void onRemoveParentLink(LayoutNode vnChild) {
		List<Node> sceneNodes = new ArrayList<>();
		sceneNodes.add((Node) vnChild.getParentLine().getFirst());
		sceneNodes.add((Node) vnChild.getParentLine().getSecond());
		vnChild.removeParentLine();
		pane.getChildren().removeAll(sceneNodes);
	}

	@Override
	public final void onNodeRenamed(LayoutNode vNode) {
		Text text = (Text) vNode.getText();
		text.setText(vNode.getDisplayText(nodeSelect.get()));
	}

	@Override
	public final void onEdgeRenamed(LayoutEdge vEdge) {
		Text text = (Text) vEdge.getText();
		text.setText(vEdge.getDisplayText(edgeSelect.get()));
	}

	private LayoutNode getTWRoot() {
		for (LayoutNode n : layoutGraph.nodes()) {
			if (n.isRoot())
				return n;
		}
		return null;
	}

	@Override
	public final void doLayout(LayoutNode root, double jitterFraction, LayoutType layoutType, boolean pcShowing,
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
		default: {
			/** SpringGraph */
			layout = new FRLayout(layoutGraph, pcShowing, xlShowing, sideline);
			break;
		}
		}
		layout.compute(jitterFraction);

		// always scaled within unit space

		// rescale to provide border for a little node radius
		Timeline timeline = new Timeline();

		for (LayoutNode node : layoutGraph.nodes())
			if (!node.isCollapsed()) {
				double x = ILayout.rescale(node.getX(), 0, 1, 0.05, 0.95);
				double y = ILayout.rescale(node.getY(), 0, 1, 0.05, 0.95);
				node.setPosition(x, y);
				Circle c = (Circle) node.getSymbol();
				if (animate.get()) {
					KeyFrame f = getKeyFrame(c, node.getX() * pane.getWidth(), node.getY() * pane.getHeight(),
							duration);
					timeline.getKeyFrames().add(f);
				} else {
					c.setCenterX(node.getX() * pane.getWidth());
					c.setCenterY(node.getY() * pane.getHeight());
				}

			}

		if (animate.get())
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
	}

	@Override
	public final void collapsePredef() {
		for (LayoutNode root : layoutGraph.roots()) {
			if (root.isRoot()) {
				LayoutNode predef = (LayoutNode) get(root.getChildren(),
						selectOne(hasTheName(ConfigurationReservedNodeId.categories.id())));
				collapseTree(predef, 1.0);
			}
		}
	}

	@Override
	public final void onHighlightAll() {
		Set<LayoutNode> highlightNodes = new HashSet<>();
		for (LayoutNode n : layoutGraph.nodes())
			highlightNodes.add(n);
		updateElementColour(highlightNodes);
	}

	@Override
	public final void onHighlightLocalGraph(LayoutNode root, int pathLength) {
		Set<LayoutNode> focusNodes = new HashSet<>();
		traversal(isParentLineVisible.getValue(), isCrossLinkLineVisible.getValue(), root, 0, pathLength, focusNodes);
		focusNodes.add(root);
		updateElementColour(focusNodes);
	}

	private void updateElementColour(Set<LayoutNode> focusNodes) {
		for (LayoutNode n : layoutGraph.nodes())
			dimNode(n);

		ObservableList<Node> obs = FXCollections.observableArrayList();
		for (LayoutNode n : focusNodes)
			unDimNode(n, focusNodes, obs);

		resetZorder(obs);

	}

	@SuppressWarnings("unchecked")
	private void unDimNode(LayoutNode n, Set<LayoutNode> focusNodes, ObservableList<Node> obs) {
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
			for (LayoutEdge e : (Iterable<LayoutEdge>) get(n.edges(Direction.OUT))) {
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
	private void dimNode(LayoutNode n) {
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
		for (LayoutEdge e : (Iterable<LayoutEdge>) get(n.edges(Direction.OUT))) {
			((Shape) e.getSymbol().getFirst()).setEffect(colorAdjust);
			((Shape) e.getSymbol().getSecond()).setEffect(colorAdjust);
			((Text) e.getText()).setEffect(colorAdjust);
		}
	}

	@SuppressWarnings("unchecked")
	private static void showNode(LayoutNode n, BooleanProperty parentVisibleProperty,
			BooleanProperty edgeVisibleProperty, Set<LayoutNode> visibleNodes) {
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
		for (LayoutEdge e : (Iterable<LayoutEdge>) get(n.edges(Direction.OUT))) {
			if (visibleNodes.contains(e.endNode())) {
				s = (Shape) e.getSymbol().getFirst();
				s.visibleProperty().bind(edgeVisibleProperty);
				e.setVisible(s.isVisible());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void hideNode(LayoutNode n) {
		hideShape((Shape) n.getSymbol());
		n.setVisible(false);
		if (n.getParent() != null) {
			hideShape((Shape) n.getParentLine().getFirst());
		}
		for (LayoutEdge e : (Iterable<LayoutEdge>) get(n.edges(Direction.OUT))) {
			hideShape((Shape) e.getSymbol().getFirst());
			e.setVisible(false);
		}
	}

	private static void updateGraphVisibility(TreeGraph<LayoutNode, LayoutEdge> g, Set<LayoutNode> visibleNodes,
			BooleanProperty parentLineVisibleProperty, BooleanProperty edgeLineVisibleProperty) {
		for (LayoutNode n : g.nodes())
			hideNode(n);
		for (LayoutNode n : visibleNodes) {
			showNode(n, parentLineVisibleProperty, edgeLineVisibleProperty, visibleNodes);
		}

	}

	private static void hideShape(Shape s) {
		if (s.visibleProperty().isBound())
			s.visibleProperty().unbind();
		s.setVisible(false);
	}

	@SuppressWarnings("unchecked")
	private static void traversal(boolean treeVisible, boolean edgesVisible, LayoutNode root, int depth, int pathLength,
			Set<LayoutNode> nnNodes) {
		if (depth < pathLength) {
			Set<LayoutNode> nn = new HashSet<>();

			if (treeVisible) {
				LayoutNode parent = root.getParent();
				if (parent != null && !nnNodes.contains(parent))
					nn.add(root.getParent());

				for (LayoutNode n : root.getChildren())
					if (!n.isCollapsed())
						if (!nnNodes.contains(n))
							nn.add(n);
			}

			if (edgesVisible) {
				Collection<LayoutEdge> outEdges = (Collection<LayoutEdge>) get(root.edges(Direction.OUT));
				for (LayoutEdge e : outEdges) {
					LayoutNode endNode = (LayoutNode) e.endNode();
					if (!endNode.isCollapsed())
						if (!nnNodes.contains(endNode))
							nn.add(endNode);
				}

				Collection<LayoutEdge> inEdges = (Collection<LayoutEdge>) get(root.edges(Direction.IN));
				for (LayoutEdge e : inEdges) {
					LayoutNode startNode = (LayoutNode) e.startNode();
					if (!startNode.isCollapsed())
						if (!nnNodes.contains(startNode))
							nn.add(startNode);
				}
			}
			nnNodes.addAll(nn);

			for (LayoutNode n : nn)
				traversal(treeVisible, edgesVisible, n, depth + 1, pathLength, nnNodes);

		}
	}

	@Override
	public final void onRollback(TreeGraph<LayoutNode, LayoutEdge> layoutGraph) {
		final double duration = animateFast;
		this.layoutGraph = layoutGraph;
		pane.getChildren().clear();
		this.initialiseView(duration);
		this.setTextListeners();
	}

}
