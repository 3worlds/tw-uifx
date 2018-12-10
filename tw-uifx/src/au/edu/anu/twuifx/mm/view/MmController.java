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

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.awt.Rectangle;
import java.io.File;
import org.controlsfx.control.PropertySheet;


public class MmController /*implements ArchComplianceListener, CodeComplianceListener, DeployComplianceListener*/ {

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

	//private ModelMakerModel model;
	private Stage stage;
	private ToggleGroup tgArchetype;
	//private Verbosity verbosity = Verbosity.brief;

	//private List<ArchComplianceMessageable> lstArchMsgs = new ArrayList<>();
	//private List<CodeComplianceMessageable> lstCodeMsgs = new ArrayList<>();
	//private List<DeploymentComplianceMessageable> lstDeploymentMsgs = new ArrayList<>();

	private double drawWidth;
	private double drawHeight;
	private StringProperty userProjectPath = new SimpleStringProperty("");

	public void initFontSize(int size) {
		spinFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 20, size));
		//VisualNode.setFontSize(size);
	}

	public void initNodeRadius(int size) {
		spinNodeSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 40, size));
		//VisualNode.setNodeRadius(size);		
	}

	@FXML
	public void initialize() {
		spinFontSize.setMaxWidth(75.0);
		spinNodeSize.setMaxWidth(75.0);

		spinFontSize.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, //
					Integer oldValue, Integer newValue) {
				//VisualNode.setFontSize(newValue);
				for (Node n : zoomTarget.getChildren()) {
					if (n instanceof Text) {
						Text t = (Text) n;
//						t.setFont(VisualNode.getFont());
					}
				}
			}
		});

		spinNodeSize.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, //
					Integer oldValue, Integer newValue) {
				//VisualNode.setNodeRadius(newValue);
			}
		});

		//StatusText.setStatusProperty(lblStatus);
		// StatusText.message("Initialising user interface");
		btnLayout.setTooltip(new Tooltip("Apply layout function"));
		btnXLinks.setTooltip(new Tooltip("Show/hide cross-links"));
		btnChildLinks.setTooltip(new Tooltip("Show/hide parent-child edges"));
		// propertySheet.setMode(mode);
		// Set a handler to update the menu when openMenu is shown
		menuOpen.addEventHandler(Menu.ON_SHOWING, event -> updateOpenProjectsMenu(menuOpen));

		// https://stackoverflow.com/questions/38604780/javafx-zoom-scroll-in-scrollpane?rq=1
		// This class has all the housework for managing graph
		//model = new ModelMakerModel(this, zoomTarget);

		// Listen for Archetype error messages
		//ArchCompliance.addListener(this);

		// Listen for compile error messages
		//CodeCompliance.addListener(this);
		// anchorPane.boundsInLocalProperty().addListener(new ChangeListern);

		// Listen for deployment error messages
		//DeployCompliance.addListener(this);

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
//		UiUtil.zoomConfig(scrollPane, scrollContent, group, zoomTarget);
		// StatusText.clear();
		// AnchorPane.setRightAnchor(lblUserProjectPath, 0.0);

	}

	private void verbosityChange(Observable t) {
		RadioButton rb = (RadioButton) tgArchetype.getSelectedToggle();
//		if (rb == rb1)
//			verbosity = Verbosity.brief;
//		else if (rb == rb2)
//			verbosity = Verbosity.medium;
//		else
//			verbosity = Verbosity.full;
		textFlowErrorMsgs.getChildren().clear();
//		for (ArchComplianceMessageable msg : lstArchMsgs)
//			textFlowErrorMsgs.getChildren().add(getArchMessageText(msg));
//		for (CodeComplianceMessageable msg : lstCodeMsgs)
//			textFlowErrorMsgs.getChildren().add(getCodeMessageText(msg));
//		for (DeploymentComplianceMessageable msg : lstDeploymentMsgs)
//			textFlowErrorMsgs.getChildren().add(getDeployMessageText(msg));
	}

	@FXML
	void handleDisconnectJavaProject(ActionEvent event) {
		userProjectPath.set("");
		// userSrcPath.set("");
		//model.checkGraph();
	}

	@FXML
	void handleSetCodePath(ActionEvent event) {
//		File prjFile = Dialogs.getCodePath("Select java project", userProjectPath.get());
//		if (prjFile != null) {
//			userProjectPath.set("");
//			// userSrcPath.set("");
//			String prjtmp = prjFile.getAbsolutePath().replace("\\", "/");
//			if (checkIsAProject(prjtmp) & !prjtmp.equals(userProjectPath.get())) {
//				userProjectPath.set(prjtmp);
//				// userSrcPath.set(srctmp);
//				//model.checkGraph();
//			}
//		}
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
//		ArchCompliance.clear();
//		CodeCompliance.clear();
//		DeployCompliance.clear();
//		model.checkGraph();
	}

	@FXML
	void handleMenuExit(ActionEvent event) {
//		if (model.canClose("closing")) {
//			Platform.exit();
//			System.exit(0);
//		}
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
//		model.openProject(file);
	}

	@FXML
	void handlePaneOnMouseClicked(MouseEvent event) {
//		model.onPaneMouseClicked(event);

	}

	@FXML
	void handlePaneOnMouseMoved(MouseEvent event) {
//		model.onPaneMouseMoved(event);
		captureDrawingSize();
	}

	@FXML
	void handleLayout(ActionEvent event) {
//		model.runLayout();

	}

	@FXML
	void handleOnDeploy(ActionEvent event) {
//		if (!GraphState.hasChanged()) {
//			StatusText.message("Deploying simulator");
//			DeployCompliance.clear();
//			btnDeploy.setDisable(true);
//			Runnable task = () -> {
//				model.createSimulatorAndDeploy();
//				Platform.runLater(() -> {
//					btnDeploy.setDisable(false);
//					StatusText.clear();
//				});
//			};
//			ExecutorService executor = Executors.newSingleThreadExecutor();
//			executor.execute(task);
//		} else
//			Dialogs.errorAlert("Run simulator", "", "Project must be saved before creating simulator");
	}

	private void updateOpenProjectsMenu(Menu menuOpen) {
//		Map<MenuItem, File> map = new HashMap<>();
//		Uid cid = null;
//		if (Project.isOpen())
//			cid = Project.getProjectUid();
//		menuOpen.getItems().clear();
//		File[] f = Project.getProjectPaths();
//		String[] names = Project.getProjectDisplayNames(f);
//		for (int i = 0; i < f.length; i++) {
//			MenuItem mi = new MenuItem(names[i]);
//			// Stop the first underscore from being removed
//			// https://bugs.openjdk.java.net/browse/JDK-8095296
//			mi.setMnemonicParsing(false);
//			menuOpen.getItems().add(mi);
//			map.put(mi, f[i]);
//			Uid ui = Project.getProjectUid(f[i].getAbsolutePath());
//			if (Objects.equals(cid, ui))
//				mi.setDisable(true);
//			else
//				mi.setDisable(false);
//			mi.setOnAction((e) -> {
//				File file = map.get(e.getSource());
//				openProject(file);
//			});
//		}
	}

//	public ModelMakerModel model() {
//		return model;
//	}

	@FXML
	void handleNewProject(ActionEvent event) {
//		model.newProject();
	}

	@FXML
	void handleSave(ActionEvent event) {
//		model.save();

	}

	@FXML
	void handleSaveAs(ActionEvent event) {
//		model.saveAs();

	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public Cursor setCursor(Cursor cursor) {
		Cursor oldCursor = stage.getScene().getCursor();
		stage.getScene().setCursor(cursor);
		return oldCursor;
	}

//	@Override
//	public void onReceiveArchetypeMsg(ArchComplianceMessageable msg) {
//		lstArchMsgs.add(msg);
//		textFlowErrorMsgs.getChildren().add(getArchMessageText(msg));
//	}
//
//	@Override
//	public void onReceiveCodeMsg(CodeComplianceMessageable msg) {
//		lstCodeMsgs.add(msg);
//		textFlowErrorMsgs.getChildren().add(getCodeMessageText(msg));
//	}
//
//	@Override
//	public void onReceiveDeployMsg(DeploymentComplianceMessageable msg) {
//		lstDeploymentMsgs.add(msg);
//		textFlowErrorMsgs.getChildren().add(getDeployMessageText(msg));
//	}
//
//	@Override
//	public void onClearArchetypeMsgs() {
//		textFlowErrorMsgs.getChildren().clear();
//		lstArchMsgs.clear();
//	}
//
//	@Override
//	public void onClearCodeMsgs() {
//		textFlowErrorMsgs.getChildren().clear();
//		lstCodeMsgs.clear();
//	}
//
//	@Override
//	public void onClearDeployMsgs() {
//		textFlowErrorMsgs.getChildren().clear();
//		lstDeploymentMsgs.clear();
//	}
//
//	private Text getArchMessageText(ArchComplianceMessageable msg) {
//		Text t = new Text(msg.message(verbosity) + "\n");
//		return t;
//	}

//	private Text getCodeMessageText(CodeComplianceMessageable msg) {
//		Text t = new Text(msg.message(verbosity) + "\n");
//		return t;
//	}
//
//	private Text getDeployMessageText(DeploymentComplianceMessageable msg) {
//		Text t = new Text(msg.message(verbosity) + "\n");
//		return t;
//	}

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
//		model.importProject();
	}

	private Rectangle getStageRectangle() {
		return new Rectangle((int) stage.getX(), (int) stage.getY(), (int) stage.getWidth(), (int) stage.getHeight());
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
	// private static final String UserSrcPath = "userSrcPath";
	private static final String Mode = "_mode";

	// called when closing a project
	public void putPreferences() {
//		if (Project.isOpen()) {
//			Preferences p = PreferencesProject.impl();
//			p.put(UserProjectPath, userProjectPath.get());
//			// p.put(UserSrcPath, userSrcPath.get());
//			p.put(allElementsPropertySheet.idProperty().get() + Mode,
//					(allElementsPropertySheet.getMode() == PropertySheet.Mode.NAME));
//			p.put(nodePropertySheet.idProperty().get() + Mode,
//					(nodePropertySheet.getMode() == PropertySheet.Mode.NAME));
//			p.put(zoomTarget.idProperty().get() + width, zoomTarget.getWidth());
//			p.put(zoomTarget.idProperty().get() + height, zoomTarget.getHeight());
//			p.putSplitPaneDividers(splitPane1);
//			p.putSplitPaneDividers(splitPane2);
//			p.putTabSelection(tabPaneProperties);
//			p.put(zoomTarget.idProperty().get() + scaleX, zoomTarget.getScaleX());
//			p.put(zoomTarget.idProperty().get() + scaleY, zoomTarget.getScaleY());
//			p.putRectangle(mainFrameName, getStageRectangle());
//			p.putBoolean(mainMaximized, stage.isMaximized());
//			p.putBoolean(btnXLinks.idProperty().get(), btnXLinks.isSelected());
//			p.putBoolean(btnChildLinks.idProperty().get(), btnChildLinks.isSelected());
//			p.putInt(fontSizeKey, VisualNode.getFontSize());
//			p.putInt(nodeSizeKey, VisualNode.getNodeRadius());
//			PropertyDefaults.savePreferences(p);
//			p.flush();
//		}
	}

	private boolean checkIsAProject(String path) {
		return false;
//		boolean res = (new File(path + File.separator + CodeGenFiles.SRC).exists());
//		if (!res) {
//			Dialogs.errorAlert("External project", "Path to user project does not contain a src directory.", path);
//			return false;
//		} else
//			return true;
	}

	// called when opening a project
	public void getPreferences() {
//		StringProperty sp = stage.titleProperty();
////		GraphState.setTitleProperty(sp, userProjectPath);
//
////		PreferencesProject.initialise();
////		Preferences p = PreferencesProject.impl();
//
//		Rectangle r = p.getRectangle(mainFrameName, getStageRectangle());
//		Platform.runLater(() -> {
//			stage.setHeight(r.getHeight());
//			stage.setWidth(r.getWidth());
//			stage.setX(r.getX());
//			stage.setY(r.getY());
//			stage.setMaximized(p.getBoolean(mainMaximized, stage.isMaximized()));
//		});
//
//		initFontSize(p.getInt(fontSizeKey, 10));
//		initNodeRadius(p.getInt(nodeSizeKey,10));
//
//		p.getTabSelection(tabPaneProperties);
//
//		Platform.runLater(() -> {
//			boolean m = p.getBoolean(nodePropertySheet.idProperty().get() + Mode, true);
//			PropertySheet.Mode md = PropertySheet.Mode.CATEGORY;
//			if (m)
//				md = PropertySheet.Mode.NAME;
//			nodePropertySheet.setMode(md);
//
//			m = p.getBoolean(allElementsPropertySheet.idProperty().get() + Mode, true);
//			md = PropertySheet.Mode.CATEGORY;
//			if (m)
//				md = PropertySheet.Mode.NAME;
//			allElementsPropertySheet.setMode(md);
//
//			String prjtmp = (String) p.get(UserProjectPath, "");
//			// String srctmp = (String) p.get(UserSrcPath, "");
//			if (!prjtmp.equals("")) {
//				if (!checkIsAProject(prjtmp)) {
//					prjtmp = "";
//					// srctmp = "";
//				}
//			}
//			userProjectPath.set(prjtmp);
//			// userSrcPath.set(srctmp);
//		});
//		btnXLinks.selectedProperty().set(p.getBoolean(btnXLinks.idProperty().get(), true));
//		btnChildLinks.selectedProperty().set(p.getBoolean(btnChildLinks.idProperty().get(), true));
//
//		drawWidth = (Double) p.get(zoomTarget.idProperty().get() + width, zoomTarget.getMinWidth());
//		drawHeight = (Double) p.get(zoomTarget.idProperty().get() + height, zoomTarget.getMinHeight());
//		zoomTarget.setPrefWidth(drawWidth);
//		zoomTarget.setPrefHeight(drawHeight);
//
//		zoomTarget.setScaleX((Double) p.get(zoomTarget.idProperty().get() + scaleX, zoomTarget.getScaleX()));
//		zoomTarget.setScaleY((Double) p.get(zoomTarget.idProperty().get() + scaleY, zoomTarget.getScaleY()));
//		// get splitPans later after UI has settled down
//		splitPane1.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
//
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				Platform.runLater(() -> {
//					p.getSplitPaneDividers(splitPane1);
//					p.getSplitPaneDividers(splitPane2);
//					observable.removeListener(this);
//				});
//			}
//		});
//
////		PropertyDefaults.loadPreferences(p);

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

	// public String getUserSrcPath() {
	// return userSrcPath.get();
	// }

	public StringProperty getUserProjectPathProperty() {
		return userProjectPath;
	}

	// public StringProperty getUserSrcPathProperty() {
	// return userSrcPath;
	// }

	public boolean haveUserProject() {
		return !userProjectPath.get().equals("");
	}

}
