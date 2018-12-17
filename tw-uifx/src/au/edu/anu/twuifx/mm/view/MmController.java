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

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.controlsfx.control.PropertySheet;

import au.edu.anu.omhtk.preferences.PrefImpl;
import au.edu.anu.omhtk.preferences.Preferable;
import au.edu.anu.twapps.devenv.DevEnv;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.graphviz.GraphVisualisation;
import au.edu.anu.twapps.mm.GraphState;
import au.edu.anu.twapps.mm.ModelListener;
import au.edu.anu.twapps.mm.ModelMakerModel;
import au.edu.anu.twcore.errorMessaging.ErrorMessagable;
import au.edu.anu.twcore.errorMessaging.ErrorMessageListener;
import au.edu.anu.twcore.errorMessaging.Verbosity;
import au.edu.anu.twcore.errorMessaging.archetype.ArchComplianceManager;
import au.edu.anu.twcore.errorMessaging.codeGenerator.CodeComplianceManager;
import au.edu.anu.twcore.errorMessaging.deploy.DeployComplianceManager;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.mm.visualise.GVizfx;
import fr.cnrs.iees.graph.generic.Graph;

public class MmController implements ErrorMessageListener, ModelListener {

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

	private ModelMakerModel model;
	private Stage stage;
	private ToggleGroup tgArchetype;
	private Verbosity verbosity = Verbosity.brief;

	private List<ErrorMessagable> lstErrorMsgs = new ArrayList<>();

	private double drawWidth;
	private double drawHeight;
	private StringProperty userProjectPath = new SimpleStringProperty("");

	public void initFontSize(int size) {
		spinFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 20, size));
		GVizfx.setFontSize(size);
	}

	public void initNodeRadius(int size) {
		spinNodeSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 40, size));
		GVizfx.setNodeRadius(size);
	}

	@FXML
	public void initialize() {
		spinFontSize.setMaxWidth(75.0);
		spinNodeSize.setMaxWidth(75.0);

		spinFontSize.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, //
					Integer oldValue, Integer newValue) {
				GVizfx.setFontSize(newValue);
				for (Node n : zoomTarget.getChildren()) {
					if (n instanceof Text) {
						Text t = (Text) n;
						t.setFont(GVizfx.getFont());
					}
				}
			}
		});

		spinNodeSize.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, //
					Integer oldValue, Integer newValue) {
				GVizfx.setNodeRadius(newValue);
			}
		});

		btnLayout.setTooltip(new Tooltip("Apply layout function"));
		btnXLinks.setTooltip(new Tooltip("Show/hide cross-links"));
		btnChildLinks.setTooltip(new Tooltip("Show/hide parent-child edges"));
		// Set a handler to update the menu when openMenu is shown
		menuOpen.addEventHandler(Menu.ON_SHOWING, event -> updateOpenProjectsMenu(menuOpen));

		// This class has all the housework for managing graph
		GraphVisualisation.initialise(new GVizfx());
		model = new ModelMakerModel();
		model.addListener(this);
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
		model.checkGraph();
	}

	@FXML
	void handleSetCodePath(ActionEvent event) {
		File jprjFile = Dialogs.selectDirectory("Select java project", userProjectPath.get());
		if (jprjFile != null) {
			String tmp = jprjFile.getAbsolutePath().replace("\\", "/");
			if (!tmp.equals(userProjectPath.get()))
				if (DevEnv.isJavaProject(jprjFile)) {
					userProjectPath.set(tmp);
					model.checkGraph();
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
		model.checkGraph();
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
		model.openProject(file);
	}

	@FXML
	void handlePaneOnMouseClicked(MouseEvent e) {
		model.onPaneMouseClicked(e.getX(), e.getY(), zoomTarget.getWidth(), zoomTarget.getHeight());

	}

	@FXML
	void handlePaneOnMouseMoved(MouseEvent e) {
		model.onPaneMouseMoved(e.getX(), e.getY(), zoomTarget.getWidth(), zoomTarget.getHeight());
		captureDrawingSize();
	}

	@FXML
	void handleLayout(ActionEvent event) {
		model.runLayout();
	}

	@FXML
	void handleOnDeploy(ActionEvent event) {
		if (!GraphState.hasChanged()) {
			DeployComplianceManager.clear();
			btnDeploy.setDisable(true);
			Runnable task = () -> {
				model.createSimulatorAndDeploy();
				Platform.runLater(() -> {
					btnDeploy.setDisable(false);
				});
			};
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(task);
		} else
			Dialogs.errorAlert("Run simulator", "", "Project must be saved before creating simulator");
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

	public ModelMakerModel model() {
		return model;
	}

	@FXML
	void handleNewProject(ActionEvent event) {
		model.newProject();
	}

	@FXML
	void handleSave(ActionEvent event) {
		model.save();

	}

	@FXML
	void handleSaveAs(ActionEvent event) {
		model.saveAs();

	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public Cursor setCursor(Cursor cursor) {
		Cursor oldCursor = stage.getScene().getCursor();
		stage.getScene().setCursor(cursor);
		return oldCursor;
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
		model.importProject();
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
			Preferable p = new PrefImpl(Project.makePreferencesFile());
			p.putString(UserProjectPath, userProjectPath.get());
			p.putBoolean(allElementsPropertySheet.idProperty().get() + Mode,
					(allElementsPropertySheet.getMode() == PropertySheet.Mode.NAME));
			p.putBoolean(nodePropertySheet.idProperty().get() + Mode,
					(nodePropertySheet.getMode() == PropertySheet.Mode.NAME));
			p.putDouble(zoomTarget.idProperty().get() + width, zoomTarget.getWidth());
			p.putDouble(zoomTarget.idProperty().get() + height, zoomTarget.getHeight());
			p.putDouble(splitPane1.idProperty().get(), splitPane1.getDividerPositions()[0]);
			p.putDouble(splitPane2.idProperty().get(), splitPane2.getDividerPositions()[0]);
			p.putDouble(zoomTarget.idProperty().get() + scaleX, zoomTarget.getScaleX());
			p.putDouble(zoomTarget.idProperty().get() + scaleY, zoomTarget.getScaleY());
			p.putDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
			p.putBoolean(mainMaximized, stage.isMaximized());
			p.putBoolean(btnXLinks.idProperty().get(), btnXLinks.isSelected());
			p.putBoolean(btnChildLinks.idProperty().get(), btnChildLinks.isSelected());
			p.putInt(fontSizeKey, GVizfx.getFontSize());
			p.putInt(nodeSizeKey, GVizfx.getNodeRadius());
			p.flush();
		}
	}

	// called when opening a project
	public void getPreferences() {
		GraphState.setTitleProperty(stage.titleProperty(), userProjectPath);
		Preferable p = new PrefImpl(Project.makePreferencesFile());
		double[] r = p.getDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
		Platform.runLater(() -> {
			stage.setX(r[0]);
			stage.setY(r[1]);
			stage.setWidth(r[2]);
			stage.setHeight(r[3]);
			stage.setMaximized(p.getBoolean(mainMaximized, stage.isMaximized()));
		});

		initFontSize(p.getInt(fontSizeKey, 10));
		initNodeRadius(p.getInt(nodeSizeKey, 10));
		tabPaneProperties.getSelectionModel().select(Math.max(0, p.getInt(tabPaneProperties.idProperty().get(), 0)));

		Platform.runLater(() -> {
			boolean m = p.getBoolean(nodePropertySheet.idProperty().get() + Mode, true);
			PropertySheet.Mode md = PropertySheet.Mode.CATEGORY;
			if (m)
				md = PropertySheet.Mode.NAME;
			nodePropertySheet.setMode(md);

			m = p.getBoolean(allElementsPropertySheet.idProperty().get() + Mode, true);
			md = PropertySheet.Mode.CATEGORY;
			if (m)
				md = PropertySheet.Mode.NAME;
			allElementsPropertySheet.setMode(md);

			String prjtmp = p.getString(UserProjectPath, "");
			if (!prjtmp.equals("")) {
				if (!DevEnv.isJavaProject(new File(prjtmp))) {
					prjtmp = "";
				}
			}
			userProjectPath.set(prjtmp);
		});
		btnXLinks.selectedProperty().set(p.getBoolean(btnXLinks.idProperty().get(), true));
		btnChildLinks.selectedProperty().set(p.getBoolean(btnChildLinks.idProperty().get(), true));

		drawWidth = p.getDouble(zoomTarget.idProperty().get() + width, zoomTarget.getMinWidth());
		drawHeight = p.getDouble(zoomTarget.idProperty().get() + height, zoomTarget.getMinHeight());
		zoomTarget.setPrefWidth(drawWidth);
		zoomTarget.setPrefHeight(drawHeight);

		zoomTarget.setScaleX(p.getDouble(zoomTarget.idProperty().get() + scaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(p.getDouble(zoomTarget.idProperty().get() + scaleY, zoomTarget.getScaleY()));
		// get splitPans later after UI has settled down
		splitPane1.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {
					double pos;
					double[] positions = new double[1];
					pos = p.getDouble(splitPane1.idProperty().get(), splitPane1.getDividerPositions()[0]);
					positions[0] = pos;
					splitPane1.setDividerPositions(positions);

					pos = p.getDouble(splitPane2.idProperty().get(), splitPane2.getDividerPositions()[0]);
					positions[0] = pos;
					splitPane2.setDividerPositions(positions);
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
		// Platform.runLater(() -> {
		nodePropertySheet.getItems().clear();
		allElementsPropertySheet.getItems().clear();
		zoomTarget.getChildren().clear();
		// });
	}

	@Override
	public void onProjectOpened(Graph layoutGraph, boolean valid) {
		getPreferences();
		// buildAllElementsPropertySheet()
		// set the buttons
		
	}

	@Override
	public void onEndDrawing() {
		zoomTarget.setPrefHeight(Control.USE_COMPUTED_SIZE);
		zoomTarget.setPrefWidth(Control.USE_COMPUTED_SIZE);
	}

	@Override
	public void onStartDrawing() {
		zoomTarget.setPrefHeight(getDrawingHeight());
		zoomTarget.setPrefWidth(getDrawingWidth());		
	}

}
