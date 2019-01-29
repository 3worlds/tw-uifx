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

package au.edu.anu.twuifx.mm.view;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.math.util.MathUtils;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.devenv.DevEnv;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.Controllable;
import au.edu.anu.twapps.mm.GraphState;
import au.edu.anu.twapps.mm.ModelMaker;
import au.edu.anu.twapps.mm.Modelable;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualGraph;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.errorMessaging.ErrorMessagable;
import au.edu.anu.twcore.errorMessaging.ErrorMessageListener;
import au.edu.anu.twcore.errorMessaging.Verbosity;
import au.edu.anu.twcore.errorMessaging.archetype.ArchComplianceManager;
import au.edu.anu.twcore.errorMessaging.codeGenerator.CodeComplianceManager;
import au.edu.anu.twcore.errorMessaging.deploy.DeployComplianceManager;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.mm.editors.structure.SpecifiedNode;
import au.edu.anu.twuifx.mm.editors.structure.StructureEditable;
import au.edu.anu.twuifx.mm.editors.structure.StructureEditorfx;
import au.edu.anu.twuifx.mm.propertyEditors.SimplePropertyItem;
import au.edu.anu.twuifx.mm.visualise.GraphVisualisablefx;
import au.edu.anu.twuifx.mm.visualise.GraphVisualiser;
import au.edu.anu.twuifx.mm.visualise.TreeColours;
import au.edu.anu.twuifx.mm.visualise.font;
import au.edu.anu.twuifx.utils.UiHelpers;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.tree.TreeNode;
import au.edu.anu.rscs.aot.graph.AotNode;
import au.edu.anu.rscs.aot.queries.base.*;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;

public class MmController implements ErrorMessageListener, Controllable {

	@FXML
	private ToggleButton btnXLinks;

	@FXML
	private ToggleButton btnChildLinks;

	@FXML
	private BorderPane rootPane;

	@FXML
	private MenuItem menuNewProject;

	@FXML
	private Menu menuOpen;

	@FXML
	private MenuItem menuItemImport;

	@FXML
	private MenuItem menuItemSave;

	@FXML
	private MenuItem menuItemSaveAs;

	@FXML
	private MenuItem menuItemExit;

	@FXML
	private MenuItem miSetCodePath;

	@FXML
	private MenuItem miDisconnect;

	@FXML
	private Button btnCheck;

	@FXML
	private Circle trafficLight;

	@FXML
	private SplitPane splitPane1;
	@FXML
	private SplitPane splitPane2;

	@FXML
	private TabPane tabPaneProperties;

	@FXML
	private PropertySheet nodePropertySheet;

	@FXML
	private PropertySheet allElementsPropertySheet;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private StackPane scrollContent;

	@FXML
	private Group group;

	@FXML
	private AnchorPane zoomTarget;

	@FXML
	private Button btnLayout;

	@FXML
	private Button btnDeploy;

	@FXML
	private TextFlow textFlowErrorMsgs;

	@FXML
	private RadioButton rb1;

	@FXML
	private RadioButton rb2;

	@FXML
	private RadioButton rb3;

	@FXML
	private Label lblStatus;

	@FXML
	private Spinner<Integer> spinFontSize;

	@FXML
	private Spinner<Integer> spinNodeSize;

	private Modelable model;
	private Stage stage;
	private ToggleGroup tgArchetype;
	private Verbosity verbosity = Verbosity.brief;

	private List<ErrorMessagable> lstErrorMsgs = new ArrayList<>();

	private double drawWidth;
	private double drawHeight;
	private StringProperty userProjectPath = new SimpleStringProperty("");
	private BooleanProperty validProject = new SimpleBooleanProperty();

	private IntegerProperty nodeRadiusProperty = new SimpleIntegerProperty(0);
	private Color hoverColor = Color.RED;
	private DropShadow ds = new DropShadow();
	private VisualGraph visualGraph;
	private Font font;
	private int fontSize;

	public void setFontSize(int size) {
		spinFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 20, size));
		font = Font.font("Verdana", size);
		ObjectProperty<Font> fontp = new SimpleObjectProperty<Font>(font);
		fontp.get().font(size);


		fontSize = size;
	}

	public void setNodeRadius(int size) {
		spinNodeSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 40, size));
		nodeRadiusProperty.set(size);
	}

	@FXML
	public void initialize() {

		spinFontSize.setMaxWidth(75.0);
		spinNodeSize.setMaxWidth(75.0);

		spinFontSize.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, //
					Integer oldValue, Integer newValue) {
				setFontSize(newValue);
				for (Node n : zoomTarget.getChildren()) {
					if (n instanceof Text) {
						Text t = (Text) n;
						t.setFont(font);
					}
				}
			}
		});

		spinNodeSize.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, //
					Integer oldValue, Integer newValue) {
				setNodeRadius(newValue);
			}
		});

		btnLayout.setTooltip(new Tooltip("Apply layout function"));
		btnXLinks.setTooltip(new Tooltip("Show/hide cross-links"));
		btnChildLinks.setTooltip(new Tooltip("Show/hide parent-child edges"));
		// Set a handler to update the menu when openMenu is shown
		menuOpen.addEventHandler(Menu.ON_SHOWING, event -> updateOpenProjectsMenu(menuOpen));

		// This class has all the housework for managing graph
		model = new ModelMaker(this);

		// build a toggle group for the verbosity level of archetype error
		// messages
		tgArchetype = new ToggleGroup();
		rb1.setToggleGroup(tgArchetype);
		rb2.setToggleGroup(tgArchetype);
		rb3.setToggleGroup(tgArchetype);
		tgArchetype.selectedToggleProperty().addListener(t -> {
			verbosityChange(t);
		});

		// Setup zooming from the graph display pane (zoomTarget)
		zoomConfig(scrollPane, scrollContent, group, zoomTarget);

	}

	private void verbosityChange(Observable t) {
		RadioButton rb = (RadioButton) tgArchetype.getSelectedToggle();
		if (rb == rb1)
			verbosity = Verbosity.brief;
		else if (rb == rb2)
			verbosity = Verbosity.medium;
		else
			verbosity = Verbosity.full;
		textFlowErrorMsgs.getChildren().clear();
		for (ErrorMessagable msg : lstErrorMsgs)
			textFlowErrorMsgs.getChildren().add(getMessageText(msg));
	}

	@FXML
	void handleDisconnectJavaProject(ActionEvent event) {
		userProjectPath.set("");
		validProject.set(model.validateGraph());
	}

	@FXML
	void handleSetCodePath(ActionEvent event) {
		File jprjFile = Dialogs.selectDirectory("Select java project", userProjectPath.get());
		if (jprjFile != null) {
			String tmp = jprjFile.getAbsolutePath().replace("\\", "/");
			if (!tmp.equals(userProjectPath.get()))
				if (DevEnv.isJavaProject(jprjFile)) {
					userProjectPath.set(tmp);
					validProject.set(model.validateGraph());
				}
		}
	}

	// }
	private static void zoomConfig(ScrollPane scrollPane, StackPane scrollContent, Group group, Region zoomTarget) {
		Tooltip.install(zoomTarget, new Tooltip("Zoom: Ctrl+mouse wheel"));

		// Manage zooming
		group.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
			scrollContent.setMinWidth(newBounds.getWidth());
			scrollContent.setMinHeight(newBounds.getHeight());
		});
		scrollPane.viewportBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
			// use vieport size, if not too small for zoomTarget
			scrollContent.setPrefSize(newBounds.getWidth(), newBounds.getHeight());
		});
		scrollContent.setOnScroll(event -> handleContentOnScroll(event, scrollPane, group, zoomTarget));

	}

	private static void handleContentOnScroll(ScrollEvent event, ScrollPane scrollPane, Group group,
			Region zoomTarget) {

		if (event.isControlDown()) {
			event.consume();
			final double zoomFactor = event.getDeltaY() > 0 ? 1.05 : 1 / 1.05;
			Bounds groupBounds = group.getLayoutBounds();
			final Bounds viewportBounds = scrollPane.getViewportBounds();

			// calculate pixel offsets from [0, 1] range
			double valX = scrollPane.getHvalue() * (groupBounds.getWidth() - viewportBounds.getWidth());
			double valY = scrollPane.getVvalue() * (groupBounds.getHeight() - viewportBounds.getHeight());
			// convert content coordinates to zoomTarget coordinates
			Point2D posInZoomTarget = zoomTarget
					.parentToLocal(group.parentToLocal(new Point2D(event.getX(), event.getY())));

			// calculate adjustment of scroll position (pixels)
			Point2D adjustment = zoomTarget.getLocalToParentTransform()
					.deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

			// do the resizing
			zoomTarget.setScaleX(zoomFactor * zoomTarget.getScaleX());
			zoomTarget.setScaleY(zoomFactor * zoomTarget.getScaleY());

			// refresh ScrollPane scroll positions & content bounds
			scrollPane.layout();

			/**
			 * Convert back to [0, 1] range. Values that are too large or small are
			 * automatically corrected by ScrollPane.
			 */
			groupBounds = group.getLayoutBounds();
			scrollPane.setHvalue((valX + adjustment.getX()) / (groupBounds.getWidth() - viewportBounds.getWidth()));
			scrollPane.setVvalue((valY + adjustment.getY()) / (groupBounds.getHeight() - viewportBounds.getHeight()));
		}
	}

	// Property to be bound to xlink lines
	public BooleanProperty xLinksProperty() {
		return btnXLinks.selectedProperty();
	}

	// Property to be bound to child lines
	public BooleanProperty childLinksProperty() {
		return btnChildLinks.selectedProperty();
	}

	@FXML
	void handleCheck(ActionEvent event) {
		ArchComplianceManager.clear();
		CodeComplianceManager.clear();
		DeployComplianceManager.clear();
		validProject.set(model.validateGraph());
	}

	@FXML
	void handleMenuExit(ActionEvent event) {
		if (model.canClose()) {
			Platform.exit();
			System.exit(0);
		}
	}

	public void enableButtons() {
		miSetCodePath.setDisable(false);
		miDisconnect.setDisable(false);
		btnLayout.setDisable(false);
		menuItemSave.setDisable(false);
		menuItemSaveAs.setDisable(false);
		btnChildLinks.setDisable(false);
		btnXLinks.setDisable(false);
		btnCheck.setDisable(false);
		trafficLight.setOpacity(1.0);
	}

	private void openProject(File file) {
		model.doOpenProject(file);
	}

	@FXML
	void handlePaneOnMouseClicked(MouseEvent e) {
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

	@FXML
	void handlePaneOnMouseMoved(MouseEvent e) {
		// modelMaker.onPaneMouseMoved(e.getX(), e.getY(), zoomTarget.getWidth(),
		// zoomTarget.getHeight());
		// captureDrawingSize();
	}

	@FXML
	void handleLayout(ActionEvent event) {
		model.doLayout();
	}

	@FXML
	void handleOnDeploy(ActionEvent event) {
		model.doDeploy();
		// if (!GraphState.hasChanged()) {
		// DeployComplianceManager.clear();
		// btnDeploy.setDisable(true);
		// Runnable task = () -> {
		// modelMaker.createSimulatorAndDeploy();
		// Platform.runLater(() -> {
		// btnDeploy.setDisable(false);
		// });
		// };
		// ExecutorService executor = Executors.newSingleThreadExecutor();
		// executor.execute(task);
		// } else
		// Dialogs.errorAlert("Run simulator", "", "Project must be saved before
		// creating simulator");
	}

	private void updateOpenProjectsMenu(Menu menuOpen) {
		Map<MenuItem, File> map = new HashMap<>();
		String cid = null;
		if (Project.isOpen())
			cid = Project.getProjectDateTime();
		menuOpen.getItems().clear();
		File[] files = Project.getAllProjectPaths();
		String[] names = Project.extractDisplayNames(files);
		for (int i = 0; i < files.length; i++) {
			MenuItem mi = new MenuItem(names[i]);
			// // Stop the first underscore from being removed - not needed anymore?
			// // https://bugs.openjdk.java.net/browse/JDK-8095296
			mi.setMnemonicParsing(false);
			menuOpen.getItems().add(mi);
			map.put(mi, files[i]);
			String id = Project.extractDateTime(files[i]);
			if (Objects.equals(cid, id))
				mi.setDisable(true);
			else
				mi.setDisable(false);
			mi.setOnAction((e) -> {
				File file = map.get(e.getSource());
				openProject(file);
			});
		}
	}

	// public ModelMakerModel model() {
	// return modelMaker;
	// }

	@FXML
	void handleNewProject(ActionEvent event) {
		model.doNewProject();
	}

	@FXML
	void handleSave(ActionEvent event) {
		model.doSave();

	}

	@FXML
	void handleSaveAs(ActionEvent event) {
		model.doSaveAs();

	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setValid(boolean ok) {
		if (ok) {
			trafficLight.fillProperty().set(Color.GREEN);
			btnDeploy.setDisable(false);
		} else {
			trafficLight.fillProperty().set(Color.RED);
			btnDeploy.setDisable(true);
		}
	}

	@FXML
	void handleImport(ActionEvent event) {
		model.doImport();
	}

	public double getDrawingWidth() {
		return drawWidth;
	}

	public double getDrawingHeight() {
		return drawHeight;
	}

	public void captureDrawingSize() {
		drawWidth = zoomTarget.getWidth();
		drawHeight = zoomTarget.getHeight();
	}

	private static final String mainFrameName = "mainFrame";
	private static final String mainMaximized = mainFrameName + "_" + "maximized";
	private static final String height = "_height";
	private static final String width = "_width";
	private static final String scaleX = "_scaleX";
	private static final String scaleY = "_scaleY";
	private static final String UserProjectPath = "userProjectPath";
	private static final String fontSizeKey = "fontSize";
	private static final String nodeSizeKey = "nodeSize";
	private static final String Mode = "_mode";

	public void putPreferences() {
		if (Project.isOpen()) {
			Preferences.initialise(Project.makeProjectPreferencesFile());
			Preferences.putString(UserProjectPath, userProjectPath.get());
			Preferences.putBoolean(allElementsPropertySheet.idProperty().get() + Mode,
					(allElementsPropertySheet.getMode() == PropertySheet.Mode.NAME));
			Preferences.putBoolean(nodePropertySheet.idProperty().get() + Mode,
					(nodePropertySheet.getMode() == PropertySheet.Mode.NAME));
			Preferences.putDouble(zoomTarget.idProperty().get() + width, zoomTarget.getWidth());
			Preferences.putDouble(zoomTarget.idProperty().get() + height, zoomTarget.getHeight());
			Preferences.putDouble(splitPane1.idProperty().get(), splitPane1.getDividerPositions()[0]);
			Preferences.putDouble(splitPane2.idProperty().get(), splitPane2.getDividerPositions()[0]);
			Preferences.putDouble(zoomTarget.idProperty().get() + scaleX, zoomTarget.getScaleX());
			Preferences.putDouble(zoomTarget.idProperty().get() + scaleY, zoomTarget.getScaleY());
			Preferences.putDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
			Preferences.putBoolean(mainMaximized, stage.isMaximized());
			Preferences.putBoolean(btnXLinks.idProperty().get(), btnXLinks.isSelected());
			Preferences.putBoolean(btnChildLinks.idProperty().get(), btnChildLinks.isSelected());
			Preferences.putInt(fontSizeKey, fontSize);
			Preferences.putInt(nodeSizeKey, nodeRadiusProperty.get());
			Preferences.flush();
		}
	}

	// called when opening a project
	public void getPreferences() {
		GraphState.setTitleProperty(stage.titleProperty(), userProjectPath);
		Preferences.initialise(Project.makeProjectPreferencesFile());
		double[] r = Preferences.getDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(),
				stage.getHeight());
		Platform.runLater(() -> {
			stage.setX(r[0]);
			stage.setY(r[1]);
			stage.setWidth(r[2]);
			stage.setHeight(r[3]);
			stage.setMaximized(Preferences.getBoolean(mainMaximized, stage.isMaximized()));
		});

		setFontSize(Preferences.getInt(fontSizeKey, 10));
		setNodeRadius(Preferences.getInt(nodeSizeKey, 10));
		tabPaneProperties.getSelectionModel()
				.select(Math.max(0, Preferences.getInt(tabPaneProperties.idProperty().get(), 0)));

		Platform.runLater(() -> {
			boolean m = Preferences.getBoolean(nodePropertySheet.idProperty().get() + Mode, true);
			PropertySheet.Mode md = PropertySheet.Mode.CATEGORY;
			if (m)
				md = PropertySheet.Mode.NAME;
			nodePropertySheet.setMode(md);

			m = Preferences.getBoolean(allElementsPropertySheet.idProperty().get() + Mode, true);
			md = PropertySheet.Mode.CATEGORY;
			if (m)
				md = PropertySheet.Mode.NAME;
			allElementsPropertySheet.setMode(md);

			String prjtmp = Preferences.getString(UserProjectPath, "");
			if (!prjtmp.equals("")) {
				if (!DevEnv.isJavaProject(new File(prjtmp))) {
					prjtmp = "";
				}
			}
			userProjectPath.set(prjtmp);
		});
		btnXLinks.selectedProperty().set(Preferences.getBoolean(btnXLinks.idProperty().get(), true));
		btnChildLinks.selectedProperty().set(Preferences.getBoolean(btnChildLinks.idProperty().get(), true));

		drawWidth = Preferences.getDouble(zoomTarget.idProperty().get() + width, zoomTarget.getMinWidth());
		drawHeight = Preferences.getDouble(zoomTarget.idProperty().get() + height, zoomTarget.getMinHeight());
		zoomTarget.setPrefWidth(drawWidth);
		zoomTarget.setPrefHeight(drawHeight);

		zoomTarget.setScaleX(Preferences.getDouble(zoomTarget.idProperty().get() + scaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(zoomTarget.idProperty().get() + scaleY, zoomTarget.getScaleY()));
		// get splitPans later after UI has settled down
		splitPane1.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {
					splitPane1.setDividerPositions(UiHelpers.getSplitPanePositions(splitPane1));
					splitPane2.setDividerPositions(UiHelpers.getSplitPanePositions(splitPane2));
					observable.removeListener(this);
				});
			}
		});

	}

	public PropertySheet getNodePropertySheet() {
		return nodePropertySheet;
	}

	public PropertySheet getAllElementsPropertySheet() {
		return allElementsPropertySheet;

	}

	public String getUserProjectPath() {
		return userProjectPath.get();
	}

	public StringProperty getUserProjectPathProperty() {
		return userProjectPath;
	}

	public boolean haveUserProject() {
		return !userProjectPath.get().equals("");
	}

	private Text getMessageText(ErrorMessagable msg) {
		Text t = new Text(msg.message(verbosity) + "\n");
		return t;
	}

	@Override
	public void onReceiveMsg(ErrorMessagable msg) {
		lstErrorMsgs.add(msg);
		textFlowErrorMsgs.getChildren().add(getMessageText(msg));
	}

	@Override
	public void onClear() {
		textFlowErrorMsgs.getChildren().clear();
		lstErrorMsgs.clear();
	}

	@Override
	public void onProjectClosing() {
		visualiser.close();
		visualiser = null;
		// clearGraphView(visualGraph);
		// clearPropertySheets();
		// setButtons(no project);
		nodePropertySheet.getItems().clear();
		allElementsPropertySheet.getItems().clear();
		zoomTarget.getChildren().clear();
		visualGraph = null;

	}

	@Override
	public void onProjectOpened(VisualGraph visualGraph) {
		this.visualGraph = visualGraph;
		Cursor oldCursor = setWaitCursor();
		getPreferences();
		visualiser = new GraphVisualiser(visualGraph,zoomTarget,nodeRadiusProperty);
		visualiser.initialiseView();
		initialisePropertySheets();
		setCursor(oldCursor);
		// setButtons(have project);
	}

	private GraphVisualisablefx visualiser;
	}

	private static void collapseTree(VisualNode parent) {
		Circle circle = (Circle) parent.getSymbol();
		DoubleProperty xp = circle.centerXProperty();
		DoubleProperty yp = circle.centerYProperty();
		for (TreeNode child : parent.getChildren()) {
			collapse(child, xp, yp);
		}
	}

	private static void collapse(TreeNode node, DoubleProperty xp, DoubleProperty yp) {
		VisualNode vn = (VisualNode) node;
		setCollapseBindings(vn, xp, yp);
		vn.setCollapse(true);
		for (TreeNode child : node.getChildren())
			collapse(child, xp, yp);
	}

	private static final Double animateDuration = 1000.0;

	private static void setCollapseBindings(VisualNode node, DoubleProperty xp, DoubleProperty yp) {
		Circle circle = (Circle) node.getSymbol();
		// Some subtrees may already be collapsed
		if (node.isCollapsed()) {
			circle.centerXProperty().unbind();
			circle.centerYProperty().unbind();
		}
		KeyValue endX = new KeyValue(circle.centerXProperty(), xp.getValue(), Interpolator.EASE_IN);
		KeyValue endY = new KeyValue(circle.centerYProperty(), yp.getValue(), Interpolator.EASE_IN);
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

	private static final Color treeEdgeColor = Color.GREEN;

	private void createTreeLines(VisualNode n, BooleanProperty show) {
		// each node has a line connected to its parent
		VisualNode parent = n.getParent();
		if (parent != null) {
			Circle parentCircle = (Circle) parent.getSymbol();
			Circle childCircle = (Circle) n.getSymbol();
			Line line = new Line();
			n.setParentLine(line);

			// bindings
			// TODO can we have a colour property?
			// line.strokeProperty().bind(treeEdgeColourProperty());
			line.setStroke(treeEdgeColor);
			line.startXProperty().bind(parentCircle.centerXProperty());
			line.startYProperty().bind(parentCircle.centerYProperty());

			line.endXProperty().bind(childCircle.centerXProperty());
			line.endYProperty().bind(childCircle.centerYProperty());

			line.visibleProperty().bind(show);

			zoomTarget.getChildren().add(line);
			line.toBack();

		}
	}

	private void createGraphLines(VisualNode n, BooleanProperty show) {
		Iterable<VisualEdge> edges = (Iterable<VisualEdge>) SequenceQuery.get(n.getEdges(Direction.OUT));
		for (VisualEdge edge : edges) {
			createGraphLine(edge, show);
		}
	}

	/*
	 * edge labels of identical lines will obscure each other. Therefore we have to
	 * pull some tricks to concatentate all the infor in one text object and set the
	 * others to ""
	 */
	private static final Color graphEdgeColor = Color.GRAY;

	private void createGraphLine(VisualEdge edge, BooleanProperty show) {
		VisualNode startNode = (VisualNode) edge.startNode();
		VisualNode endNode = (VisualNode) edge.endNode();
		@SuppressWarnings("unchecked")
		Iterable<VisualEdge> edges = (Iterable<VisualEdge>) SequenceQuery.get(startNode.getEdges(Direction.OUT),
				selectZeroOrMany(notQuery(hasTheLabel(edge.classId()))));

		String newLabel = "";
		for (VisualEdge e : edges) {
			if (e.endNode().uniqueId().equals(endNode.uniqueId())) {
				newLabel += e.classId() + "/";
				Text text = (Text) e.getText();
				if (text != null)
					text.setText("");
			}
		}
		newLabel += edge.classId();

		Circle fromCircle = (Circle) startNode.getSymbol();
		Circle toCircle = (Circle) endNode.getSymbol();
		Line line = new Line();
		Text text = new Text(newLabel);
		text.setFont(font);
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

		// possible change listener but does not fully work
		text.xProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				textPropertyChange(line, text, startNode, endNode);

			}
		});
		zoomTarget.getChildren().addAll(line, text);
		line.toBack();

	}

	protected void textPropertyChange(Line line, Text text, VisualNode startNode, VisualNode endNode) {
		boolean collapsed = startNode.isCollapsed() || endNode.isCollapsed();
		double[] p1 = { line.getStartX(), line.getStartY() };
		double[] p2 = { line.getEndX(), line.getEndY() };
		double distance = MathUtils.distance(p1, p2);
		// or dy small (horizontal) and dx shorter than label??
		if ((distance < (8 * nodeRadiusProperty.get())) | collapsed) {
			text.visibleProperty().unbind();
			text.setVisible(false);
		} else {
			text.visibleProperty().bind(line.visibleProperty());
		}
	}

	private StructureEditable gse;
	private VisualNode dragNode;

	private void createNodeVisualisation(VisualNode n) {
		double x = n.getX() * zoomTarget.getWidth();
		double y = n.getY() * zoomTarget.getHeight();

		Circle c = new Circle(x, y, nodeRadiusProperty.get());
		c.radiusProperty().bind(nodeRadiusProperty);
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
				double w = zoomTarget.getWidth();
				double h = zoomTarget.getHeight();
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
				double w = zoomTarget.getWidth();
				double h = zoomTarget.getHeight();
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

		text.setFont(font);

		// Bind text position relative to circle center
		text.xProperty().bind(c.centerXProperty().add(nodeRadiusProperty));
		text.yProperty().bind(c.centerYProperty().add(nodeRadiusProperty.divide(2)));
		text.visibleProperty().bind(c.visibleProperty());
		zoomTarget.getChildren().addAll(c, text);
		/*
		 * put text behind circle - can't work entirely because other circles will be
		 * behind this text. It would require a second loop.
		 */
		c.toFront();
		text.toBack();
	}

	private void setNodePropertySheet(VisualNode n) {

	}

	private List<VisualNode> getNodeList() {
		List<VisualNode> result = new LinkedList<>();
		for (VisualNode n : visualGraph.nodes())
			result.add(n);
		return result;
	}

	private void fillNodePropertySheet(VisualNode visualNode) {
		nodePropertySheet.getItems().clear();
		AotNode cn = visualNode.getConfigNode();
		boolean showNonEditables = true;
		ObservableList<Item> list = getNodeItems(cn,cn.uniqueId(),showNonEditables);
		nodePropertySheet.getItems().setAll(list);
	}

	private ObservableList<Item> getNodeItems(AotNode node, String category, boolean showNonEditable) {

		ObservableList<Item> result = FXCollections.observableArrayList();
		for (String key : node.getKeysAsSet())
			if (node.getPropertyValue(key) != null) {
				boolean editable = model.propertyEditable(node.classId(), key);
				if (editable || showNonEditable) {
					String propertyDesciption = "";// TODO: Property description should be from the archetype?
					result.add(makeItemType(key, node, editable, category,propertyDesciption));
				}
			}
		result.sort((first, second) -> {
			return first.getName().compareTo(second.getName());
		});
		return result;
	}

	private void fillGraphPropertySheet() {
		allElementsPropertySheet.getItems().clear();
		// sort so order is consistent
		List<VisualNode> nodeList = getNodeList();
		nodeList.sort((first, second) -> {
			return first.uniqueId().compareTo(second.uniqueId());
		});
		ObservableList<Item> obsList = FXCollections.observableArrayList();
		boolean showNonEditables = false;
		for (VisualNode vn : nodeList) {
			// Hide clutter by not showing collapsed trees;
			if (!vn.isCollapsed()) {
				String cat = vn.getCategory();
				AotNode cn = vn.getConfigNode();
				ObservableList<Item> obsSubList = getNodeItems(cn, cat, showNonEditables);
				obsList.addAll(obsSubList);
			}
		}
		allElementsPropertySheet.getItems().setAll(obsList);
	}

	private Item makeItemType(String key, AotNode n, boolean editable, String category,String description) {
		Object value = n.getPropertyValue(key);
		// TODO other Item types to come...
		// if (value instanceof FileType) {
		// FileType ft = (FileType) value;
		// FileTypeItem fti = new FileTypeItem(key, n, true, category, new
		// ModelCheck(this));
		// fti.setExtensions(ft.getExtensions());
		// return fti;
		// } else if (value instanceof StatisticalAggregatesSet)
		// return new StatsTypeItem(key, n, true, category, new ModelCheck(this));
		// else if (value instanceof DateTimeType) {
		// return new DateTimeTypeItem(key, n, true, category, new ModelCheck(this));
		// } else if (value instanceof StringTable) {
		// return new StringTableTypeItem(key, n, true, category, new ModelCheck(this));
		// }
		return new SimplePropertyItem(key, n, editable, category,description, model);
	}

	private void initialisePropertySheets() {
		fillGraphPropertySheet();
	}

	private Cursor setCursor(Cursor cursor) {
		Cursor oldCursor = stage.getScene().getCursor();
		stage.getScene().setCursor(cursor);
		return oldCursor;
	}

	private Cursor setWaitCursor() {
		Cursor result = stage.getScene().getCursor();
		stage.getScene().setCursor(Cursor.WAIT);
		return result;
	}

	public boolean canClose() {
		return model.canClose();
	}

}
