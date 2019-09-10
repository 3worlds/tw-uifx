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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
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
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.MMModel;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twapps.mm.userProjectLinkFactory.UserProjectLinkFactory;
import au.edu.anu.twapps.mm.IMMModel;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.errorMessaging.ComplianceManager;
import au.edu.anu.twcore.errorMessaging.ErrorMessagable;
import au.edu.anu.twcore.errorMessaging.ErrorMessageListener;
import au.edu.anu.twcore.errorMessaging.Verbosity;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.graphState.IGraphStateListener;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.userProject.IDETypes;
import au.edu.anu.twcore.userProject.UserProjectLink;
import au.edu.anu.twuifx.mm.propertyEditors.SimplePropertyItem;
import au.edu.anu.twuifx.mm.propertyEditors.dateTimeType.DateTimeItem;
import au.edu.anu.twuifx.mm.propertyEditors.fileType.FileTypeItem;
import au.edu.anu.twuifx.mm.propertyEditors.statsType.StatsTypeItem;
import au.edu.anu.twuifx.mm.visualise.IGraphVisualiser;
import au.edu.anu.twuifx.mm.visualise.GraphVisualiserfx;
import au.edu.anu.twuifx.utils.UiHelpers;
import au.edu.anu.ymuit.scrolling.CenteredZooming;
import fr.cnrs.iees.graph.DataHolder;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.twcore.constants.DateTimeType;
import fr.cnrs.iees.twcore.constants.FileType;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;


public class MmController implements ErrorMessageListener, IMMController, IGraphStateListener {

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
	
    @FXML
    private Spinner<Integer> spinJitter;


	private IMMModel model;
	private Stage stage;
	private ToggleGroup tgArchetype;
	private Verbosity verbosity = Verbosity.brief;

	private List<ErrorMessagable> lstErrorMsgs = new ArrayList<>();

	private double drawWidth;
	private double drawHeight;

	private StringProperty userProjectPath = new SimpleStringProperty("");
	// not used yet but just set with ComplianceManager.haveErrors();
//	private BooleanProperty validProject = new SimpleBooleanProperty();

	private IntegerProperty nodeRadiusProperty = new SimpleIntegerProperty(0);
	private IntegerProperty jitterProperty = new SimpleIntegerProperty(1);
	private ObjectProperty<Font> fontProperty;

	private TreeGraph<VisualNode, VisualEdge> visualGraph;
	private Font font;
	private int fontSize;

	// TODO: make menu options and preferences entry for this choice when netbeans
	// and IntelliJ have been tested
	private   IDETypes ideType = IDETypes.eclipse;

	@FXML
	public void initialize() {
		spinFontSize.setMaxWidth(75.0);
		spinNodeSize.setMaxWidth(75.0);
		spinJitter.setMaxWidth(75.0);
		
		font = Font.font("Verdana", 10);
		fontProperty = new SimpleObjectProperty<Font>(font);

		spinFontSize.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, //
					Integer oldValue, Integer newValue) {
				setFontSize(newValue);
			}
		});

		spinNodeSize.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, //
					Integer oldValue, Integer newValue) {
				setNodeRadius(newValue);
			}
		});
		
		spinJitter.valueProperty().addListener(new ChangeListener<Integer>() {

			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer aoldValue, Integer newValue) {
				setJitter(newValue);
				
			}
			
		});

		btnLayout.setTooltip(new Tooltip("Apply layout function"));
		btnXLinks.setTooltip(new Tooltip("Show/hide cross-links"));
		btnChildLinks.setTooltip(new Tooltip("Show/hide parent-child edges"));
		// Set a handler to update the menu when openMenu is shown
		menuOpen.addEventHandler(Menu.ON_SHOWING, event -> updateOpenProjectsMenu(menuOpen));

		// This class has all the housework for managing graph
		model = new MMModel(this);

		// build a toggle group for the verbosity level of archetype error
		// messages
		tgArchetype = new ToggleGroup();
		rb1.setToggleGroup(tgArchetype);
		rb2.setToggleGroup(tgArchetype);
		rb3.setToggleGroup(tgArchetype);
		tgArchetype.selectedToggleProperty().addListener(t -> {
			verbosityChange(t);
		});

		// Listen for error msgs from validategraph
		ComplianceManager.addListener(this);
		// Setup zooming from the graph display pane (zoomTarget)
		CenteredZooming.center(scrollPane, scrollContent, group, zoomTarget);
		// are prefs saved regardless of graphState??

	}

	public void setFontSize(int size) {
		this.fontSize = size;
		spinFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 40, fontSize));
		fontProperty.set(Font.font("Verdana", fontSize));
	}

	public void setNodeRadius(int size) {
		spinNodeSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 40, size));
		nodeRadiusProperty.set(size);
	}
	
	private void setJitter(int size) {
		spinJitter.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, size));
		jitterProperty.set(size);			
	};

	private void verbosityChange(Observable t) {
		RadioButton rb = (RadioButton) tgArchetype.getSelectedToggle();
		if (rb == rb1)
			verbosity = Verbosity.brief;
		else if (rb == rb2)
			verbosity = Verbosity.medium;
		else
			verbosity = Verbosity.full;
		textFlowErrorMsgs.getChildren().clear();
		sortErrors();
		for (ErrorMessagable msg : lstErrorMsgs)
			textFlowErrorMsgs.getChildren().add(getMessageText(msg));
	}

	@FXML
	void handleDisconnectJavaProject(ActionEvent event) {
		userProjectPath.set("");
		UserProjectLink.unlinkUserProject();
		ConfigGraph.validateGraph();
		;
	}

	@FXML
	void handleSetCodePath(ActionEvent event) {
		File jprjFile = Dialogs.selectDirectory("Select java project", userProjectPath.get());
		if (jprjFile != null) {
			String tmp = jprjFile.getAbsolutePath().replace("\\", "/");
			if (!tmp.equals(userProjectPath.get())) {
				UserProjectLink.unlinkUserProject();
				if (UserProjectLinkFactory.makeEnv(jprjFile, ideType)) {
					userProjectPath.set(UserProjectLink.projectRoot().getAbsolutePath());
					ConfigGraph.validateGraph();
				}
			}
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
		ConfigGraph.validateGraph();
	}

	@FXML
	void handleMenuExit(ActionEvent event) {
		if (model.canClose()) {
			putPreferences();
			Platform.exit();
			System.exit(0);
		}
	}

	private void openProject(File file) {
		model.doOpenProject(file);
		setButtonState();
	}

	@FXML
	void handlePaneOnMouseMoved(MouseEvent e) {
		// modelMaker.onPaneMouseMoved(e.getX(), e.getY(), zoomTarget.getWidth(),
		// zoomTarget.getHeight());
		// captureDrawingSize();
	}

	@FXML
	void handleLayout(ActionEvent event) {
		int size = jitterProperty.get();
		double dSize = size;
		dSize = dSize/100.0;
		visualiser.doLayout(dSize);
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

	@FXML
	void handleNewProject(ActionEvent event) {
		model.doNewProject();
		setButtonState();
	}

	@FXML
	void handleSave(ActionEvent event) {
		model.doSave();
		setButtonState();
	}

	@FXML
	void handleSaveAs(ActionEvent event) {
		model.doSaveAs();
		setButtonState();
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	void handleImport(ActionEvent event) {
		model.doImport();
		setButtonState();
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
	private static final String jitterKey = "jitter";
	private static final String Mode = "_mode";

	public void putPreferences() {
		if (Project.isOpen()) {
//			Preferences.initialise(Project.makeProjectPreferencesFile());
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
			Preferences.putInt(jitterKey,jitterProperty.get());
			Preferences.flush();
		}
	}

	// called when opening a project
	public void getPreferences() {
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
		setJitter(Preferences.getInt(jitterKey, 1));
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
			// should store in preferences??
			if (!prjtmp.equals("")) {
				UserProjectLink.unlinkUserProject();
				if (UserProjectLinkFactory.makeEnv(new File(prjtmp), ideType)) {
					userProjectPath.set(UserProjectLink.projectRoot().getAbsolutePath());
				} else
					userProjectPath.set("");
			} else
				userProjectPath.set("");
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
					splitPane1.setDividerPositions(UiHelpers.getSplitPanePositions(splitPane1,splitPane1.getId()));
					splitPane2.setDividerPositions(UiHelpers.getSplitPanePositions(splitPane2,splitPane2.getId()));
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

	@Override
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

//================== ERROR MSG LISTENER =============
	private void sortErrors() {
		lstErrorMsgs.sort(new Comparator<ErrorMessagable>() {

			@Override
			public int compare(ErrorMessagable m1, ErrorMessagable m2) {
				return m1.message(verbosity).compareToIgnoreCase(m2.message(verbosity));
			}
		});

	}

	@Override
	public void onReceiveMsg(ErrorMessagable msg) {
		lstErrorMsgs.add(msg);
		sortErrors();
		ObservableList<Node> children = textFlowErrorMsgs.getChildren();
		children.clear();
		for (ErrorMessagable m : lstErrorMsgs)
			children.add(getMessageText(m));
	}

	@Override
	public void onClear() {
		textFlowErrorMsgs.getChildren().clear();
		lstErrorMsgs.clear();
	}
	// ===============================================

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
		setButtonState();
	}

	@Override
	public void onProjectOpened(TreeGraph<VisualNode, VisualEdge> layoutGraph) {
		this.visualGraph = layoutGraph;
		Cursor oldCursor = setWaitCursor();
		getPreferences();
		visualiser = new GraphVisualiserfx(visualGraph, //
				zoomTarget, //
				nodeRadiusProperty, //
				btnChildLinks.selectedProperty(), //
				btnXLinks.selectedProperty(), //
				fontProperty, this);
		visualiser.initialiseView();
		initialisePropertySheets();
		setCursor(oldCursor);
		setButtonState();
		GraphState.setChanged();
		GraphState.clear();
	}

	private IGraphVisualiser visualiser;

	private List<VisualNode> getNodeList() {
		List<VisualNode> result = new LinkedList<>();
		for (VisualNode n : visualGraph.nodes())
			result.add(n);
		return result;
	}

	// TODO
	private void fillNodePropertySheet(VisualNode visualNode) {
		nodePropertySheet.getItems().clear();
		if (visualNode != null) {
			TreeGraphNode cn = visualNode.getConfigNode();
			boolean showNonEditables = true;
			ObservableList<Item> list = null;
			if (cn instanceof DataHolder) {
				list = getNodeItems((TreeGraphDataNode) cn, cn.id(), showNonEditables);
				nodePropertySheet.getItems().setAll(list);
			}
		}
	}

	private ObservableList<Item> getNodeItems(TreeGraphDataNode node, String category, boolean showNonEditable) {

		ObservableList<Item> result = FXCollections.observableArrayList();
		for (String key : node.properties().getKeysAsSet())
			if (node.properties().getPropertyValue(key) != null) {
				boolean editable = model.propertyEditable(node.classId(), key);
				if (editable || showNonEditable) {
					String propertyDesciption = "";// TODO: Property description should be from the archetype?
					result.add(makeItemType(key, node, editable, category, propertyDesciption));
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
			return first.id().compareTo(second.id());
		});
		ObservableList<Item> obsList = FXCollections.observableArrayList();
		boolean showNonEditables = false;
		for (VisualNode vn : nodeList) {
			// Hide clutter by not showing collapsed trees;
			if (!vn.isCollapsed()) {
				String cat = vn.getCategory();
				TreeGraphNode cn = vn.getConfigNode();
				ObservableList<Item> obsSubList = null;
				if (cn instanceof DataHolder) {
					obsSubList = getNodeItems((TreeGraphDataNode) cn, cat, showNonEditables);
					obsList.addAll(obsSubList);
				}
			}
		}
		allElementsPropertySheet.getItems().setAll(obsList);
	}

	private Item makeItemType(String key, TreeGraphDataNode n, boolean editable, String category, String description) {
		Object value = n.properties().getPropertyValue(key);
		// TODO other Item types to come...
		if (value instanceof FileType) {
			FileTypeItem fti = new FileTypeItem(key, n, true, category, description);
			// FileType ft = (FileType) value; not sure how we can manage extensions. There
			// is no 'type' info to distinguish this node.
			// fti.setExtensions(ft.getExtensions());
			return fti;
		} else if (value instanceof StatisticalAggregatesSet)
			return new StatsTypeItem(key, n, true, category, description);
		else if (value instanceof DateTimeType) {
			return new DateTimeItem(key, n, true, category, description);
		} else if (value instanceof StringTable) {
			// return new StringTableTypeItem(key, n, true, category, description);
		}
		return new SimplePropertyItem(key, n, editable, category, description);
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

	@Override
	public void onNodeSelected(VisualNode node) {
		fillNodePropertySheet(node);
	}

	private VisualNode newNode;

	@Override
	public void onNewNode(VisualNode node) {
		zoomTarget.setCursor(Cursor.CROSSHAIR);
		newNode = node;
	}

	@FXML
	void handlePaneOnMouseClicked(MouseEvent e) {
		if (newNode != null) {
			newNode.setPosition(e.getX() / zoomTarget.getWidth(), e.getY() / zoomTarget.getHeight());
			visualiser.onNewNode(newNode);
			zoomTarget.setCursor(Cursor.DEFAULT);
			newNode = null;
			initialisePropertySheets();
			setButtonState();
			GraphState.setChanged();
			ConfigGraph.validateGraph();
		}
	}

	@Override
	public void onNodeDeleted() {
		initialisePropertySheets();
		setButtonState();
	}

	@Override
	public void onTreeCollapse() {
		initialisePropertySheets();
		setButtonState();
	}

	@Override
	public void onTreeExpand() {
		initialisePropertySheets();
		setButtonState();
	}

	@Override
	public void onStateChange(boolean state) {
		setButtonState();
	}

	public void setButtonState() {
		boolean isOpen = Project.isOpen();
		boolean saveable = !GraphState.changed() & isOpen;
		boolean isConnected = haveUserProject();
		miSetCodePath.setDisable(isConnected);
		miDisconnect.setDisable(!isConnected);
		menuItemSave.setDisable(saveable);
		menuItemSaveAs.setDisable(!isOpen);
		btnChildLinks.setDisable(!isOpen);
		btnXLinks.setDisable(!isOpen);
		btnLayout.setDisable(!isOpen);
		btnCheck.setDisable(!isOpen);
		btnDeploy.setDisable(saveable & !isValid);

		if (isOpen) {
			trafficLight.setOpacity(1.0);
		} else {
			trafficLight.setOpacity(0.5);
			trafficLight.fillProperty().set(Color.RED);
		}
		if (isValid)
			trafficLight.fillProperty().set(Color.GREEN);
		else
			trafficLight.fillProperty().set(Color.RED);
	}

	private boolean isValid = false;

	@Override
	public void state(boolean valid) {
		isValid = valid;
		setButtonState();
	}

}