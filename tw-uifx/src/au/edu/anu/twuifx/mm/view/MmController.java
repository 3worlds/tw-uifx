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
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

import javafx.scene.effect.DropShadow;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.errorMessaging.ErrorList;
import au.edu.anu.rscs.aot.errorMessaging.ErrorListListener;
import au.edu.anu.rscs.aot.errorMessaging.ErrorMessagable;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.MMModel;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twapps.mm.graphEditor.IGraphVisualiser;
import au.edu.anu.twapps.mm.layout.LayoutType;
import au.edu.anu.twapps.mm.userProjectFactory.IDETypes;
import au.edu.anu.twapps.mm.userProjectFactory.UserProjectLinkFactory;
import au.edu.anu.twapps.mm.IMMModel;
import au.edu.anu.twapps.mm.visualGraph.VisualEdge;
import au.edu.anu.twapps.mm.visualGraph.VisualNode;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.graphState.IGraphStateListener;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.TwPaths;
import au.edu.anu.twcore.userProject.UserProjectLink;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.SimpleMMPropertyItem;
import au.edu.anu.twuifx.mm.propertyEditors.StringTable.StringTableItem;
import au.edu.anu.twuifx.mm.propertyEditors.dateTimeType.DateTimeItem;
import au.edu.anu.twuifx.mm.propertyEditors.fileType.FileTypeItem;
import au.edu.anu.twuifx.mm.propertyEditors.integerRangeType.IntegerRangeItem;
import au.edu.anu.twuifx.mm.propertyEditors.intervalType.IntervalItem;
import au.edu.anu.twuifx.mm.propertyEditors.populationType.PopTypeItem;
import au.edu.anu.twuifx.mm.propertyEditors.statsType.StatsTypeItem;
import au.edu.anu.twuifx.mm.propertyEditors.trackerType.TrackerTypeItem;
import au.edu.anu.twuifx.modelLibrary.LibraryTable;
import au.edu.anu.twuifx.mm.visualise.GraphVisualiserfx;
import au.edu.anu.twuifx.utils.UiHelpers;
import au.edu.anu.ymuit.util.CenteredZooming;
import fr.cnrs.iees.graph.DataHolder;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.ElementAdapter;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.impl.TreeGraphNode;
import fr.cnrs.iees.graph.io.GraphImporter;
import fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels;
import fr.cnrs.iees.twcore.constants.DateTimeType;
import fr.cnrs.iees.twcore.constants.FileType;
import fr.cnrs.iees.twcore.constants.PopulationVariablesSet;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;
import fr.cnrs.iees.twcore.constants.TrackerType;
import fr.ens.biologie.generic.utils.Interval;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

public class MmController implements ErrorListListener, IMMController, IGraphStateListener {
	@FXML
	private MenuItem miAbout;

	@FXML
	private ToggleButton btnXLinks;

	@FXML
	private ToggleButton btnChildLinks;

	@FXML
	private BorderPane rootPane;

	@FXML
	private Menu menuNew;

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
	private TextArea textAreaErrorMsgs;

	@FXML
	private RadioButton rb1;

	@FXML
	private RadioButton rb2;

	@FXML
	private RadioButton rb3;

	@FXML
	private Spinner<Integer> spinFontSize;

	@FXML
	private Spinner<Integer> spinNodeSize;

	@FXML
	private Spinner<Integer> spinJitter;

	@FXML
	private Label lblChecking;

	@FXML
	private ComboBox<LayoutType> cbxLayoutChoice;

	public enum Verbosity {
		brief, medium, full;
	}

	private IMMModel model;
	private Stage stage;
	private ToggleGroup tgArchetype;
	private Verbosity verbosity = Verbosity.brief;

	private List<ErrorMessagable> lstErrorMsgs = new ArrayList<>();

	private StringProperty userProjectPath = new SimpleStringProperty("");
	private IntegerProperty nodeRadiusProperty = new SimpleIntegerProperty(0);
	private IntegerProperty jitterProperty = new SimpleIntegerProperty(1);
	private ObjectProperty<Font> fontProperty;

	private TreeGraph<VisualNode, VisualEdge> visualGraph;
	private Font font;
	private int fontSize;

	// TODO: make menu options and preferences entry for this choice when netbeans
	// and IntelliJ have been tested
	private IDETypes ideType = IDETypes.eclipse;

	@FXML
	public void initialize() {
		cbxLayoutChoice.getItems().setAll(LayoutType.values());
		cbxLayoutChoice.setValue(LayoutType.OrderedTree);

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
		buildNewMenu();

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
		ErrorList.addListener(this);
		// Setup zooming from the graph display pane (zoomTarget)
		CenteredZooming.center(scrollPane, scrollContent, group, zoomTarget);
		// are prefs saved regardless of graphState??

	}

	@SuppressWarnings("unchecked")
	private void buildNewMenu() {
		Map<MenuItem, LibraryTable> map = new HashMap<>();
		menuNew.getItems().clear();
		for (LibraryTable entry : LibraryTable.values()) {
			MenuItem mi = new MenuItem(entry.displayName());
			mi.setMnemonicParsing(false);
			map.put(mi, entry);
			menuNew.getItems().add(mi);
			mi.setOnAction((e) -> {
				if (model.canClose()) {
					LibraryTable lt = map.get(e.getSource());
					TreeGraph<TreeGraphDataNode, ALEdge> templateGraph = (TreeGraph<TreeGraphDataNode, ALEdge>) GraphImporter
							.importGraph(lt.fileName(), LibraryTable.class);

					String uName = System.getProperty("user.name");
					TreeGraphDataNode rn = templateGraph.root();
					StringTable authors = (StringTable) rn.properties().getPropertyValue(P_MODEL_AUTHORS.key());
					authors.setByInt(uName, 0);

					model.doNewProject(templateGraph);
					setButtonState();
				}
			});
		}

	}

	public void setFontSize(int size) {
		this.fontSize = size;
		spinFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 40, fontSize));
		fontProperty.set(Font.font("Verdana", fontSize));
	}

	public void setNodeRadius(int size) {
		spinNodeSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 40, size));
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
//		textFlowErrorMsgs.getChildren().clear();
		textAreaErrorMsgs.clear();
		sortErrors();
		for (ErrorMessagable msg : lstErrorMsgs)
			textAreaErrorMsgs.appendText(getMessageText(msg));
	}

	@FXML
	void handleDisconnectJavaProject(ActionEvent event) {
		userProjectPath.set("");
		String header = "'" + Project.getDisplayName() + "' is now disconnected from Java project '"
				+ UserProjectLink.projectRoot().getName() + "'.";
		UserProjectLink.unlinkUserProject();
		ConfigGraph.validateGraph();
		Dialogs.infoAlert("Project disconnected", header, "");
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
					String header = "'" + Project.getDisplayName() + "' is now connected to Java project '"
							+ jprjFile.getName() + "'.";
					String content = "Make sure '" + TwPaths.TW_DEP_JAR + "' is in the build path of '"
							+ jprjFile.getName() + "' and refresh/clean '" + jprjFile.getName() + "' from the IDE.";
					Dialogs.infoAlert("Project connected", header, content);
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
//		btnDeploy.setDisable(true);
//		btnCheck.setDisable(true);
//		trafficLight.fillProperty().set(Color.RED);
//		Task<Void> checkTask = new Task<Void>() {
//
//			@Override
//			protected Void call() throws Exception {
		ConfigGraph.validateGraph();
//				return null;
//			}
//		};
//		checkTask.setOnScheduled(e -> {
//		});
//		checkTask.setOnSucceeded(e -> {
//			btnCheck.setDisable(false);
//			setButtonState();
//		});
//		new Thread(checkTask).start();
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
		textAreaErrorMsgs.clear();
		lstErrorMsgs.clear();
		isValid = false;

		// setButtonState();
		// ConfigGraph.validateGraph();
	}

	@FXML
	void handlePaneOnMouseMoved(MouseEvent e) {
		// modelMaker.onPaneMouseMoved(e.getX(), e.getY(), zoomTarget.getWidth(),
		// zoomTarget.getHeight());
		// captureDrawingSize();
	}

	@FXML
	void handleLayout(ActionEvent event) {
		doLayout();
	}

	@Override
	public void doLayout() {
		callLayout(null);
	}

	@Override
	public void doFocusedLayout(VisualNode root) {
		callLayout(root);
	}

	private void callLayout(VisualNode root) {
		int size = jitterProperty.get();
		double dSize = size;
		dSize = dSize / 100.0;
		visualiser.doLayout(root, dSize, cbxLayoutChoice.getValue(), btnChildLinks.isSelected(),
				btnXLinks.isSelected());
	}

	@FXML
	void handleOnDeploy(ActionEvent event) {
		model.doDeploy();
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
	void handleSave(ActionEvent event) {
		model.doSave();
		setButtonState();
	}

	@FXML
	void handleSaveAs(ActionEvent event) {
		model.doSaveAs();
		for (VisualNode root : visualGraph.roots())
			if (root.cClassId().equals(ConfigurationNodeLabels.N_ROOT.label()))
				visualiser.onNodeRenamed(root);
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

	private static final String mainFrameName = "mainFrame";
	private static final String mainMaximized = mainFrameName + "_" + "maximized";
	private static final String scaleX = "_scaleX";
	private static final String scaleY = "_scaleY";
	private static final String UserProjectPath = "userProjectPath";
	private static final String fontSizeKey = "fontSize";
	private static final String nodeSizeKey = "nodeSize";
	private static final String jitterKey = "jitter";
	private static final String Mode = "_mode";
	private static final String AccordionSelection = "_AccSel";
	private static final String LayoutChoice = "layoutChoice";

	public void putPreferences() {
		if (Project.isOpen()) {
//			System.out.println("Write preferences");

			Preferences.putString(UserProjectPath, userProjectPath.get());
			Preferences.putBoolean(allElementsPropertySheet.idProperty().get() + Mode,
					(allElementsPropertySheet.getMode() == PropertySheet.Mode.NAME));
			Preferences.putBoolean(nodePropertySheet.idProperty().get() + Mode,
					(nodePropertySheet.getMode() == PropertySheet.Mode.NAME));
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
			Preferences.putInt(jitterKey, jitterProperty.get());
			Preferences.putInt(tabPaneProperties.idProperty().get(),
					tabPaneProperties.getSelectionModel().getSelectedIndex());
			Preferences.putInt(AccordionSelection, UiHelpers.getExpandedPaneIndex(allElementsPropertySheet));
			Preferences.putEnum(LayoutChoice, cbxLayoutChoice.getValue());

			Preferences.flush();
		}
	}

	// private File pfile;

	public void getPreferences() {
//		pfile = Project.makeProjectPreferencesFile();
//		System.out.println("Read from preferences");
		Preferences.initialise(Project.makeProjectPreferencesFile());
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

		Platform.runLater(() -> {
			double[] ws = Preferences.getDoubles(mainFrameName, DefaultWindowSettings.getX(),
					DefaultWindowSettings.getY(), DefaultWindowSettings.getWidth(), DefaultWindowSettings.getHeight());
			stage.setX(ws[0]);
			stage.setY(ws[1]);
			stage.setWidth(ws[2]);
			stage.setHeight(ws[3]);
			stage.setMaximized(Preferences.getBoolean(mainMaximized, stage.isMaximized()));
		});

		setFontSize(Preferences.getInt(fontSizeKey, 10));
		setNodeRadius(Preferences.getInt(nodeSizeKey, 8));
		setJitter(Preferences.getInt(jitterKey, 1));
		tabPaneProperties.getSelectionModel()
				.select(Math.max(0, Preferences.getInt(tabPaneProperties.idProperty().get(), 0)));

		Platform.runLater(() -> {
			LayoutType lt = (LayoutType) Preferences.getEnum(LayoutChoice, LayoutType.OrderedTree);
			cbxLayoutChoice.setValue(lt);
			boolean m = Preferences.getBoolean(nodePropertySheet.idProperty().get() + Mode, true);
			PropertySheet.Mode md = PropertySheet.Mode.CATEGORY;
			if (m)
				md = PropertySheet.Mode.NAME;
			nodePropertySheet.setMode(md);

			m = Preferences.getBoolean(allElementsPropertySheet.idProperty().get() + Mode, false);
			md = PropertySheet.Mode.CATEGORY;
			if (m)
				md = PropertySheet.Mode.NAME;
			allElementsPropertySheet.setMode(md);
			int idx = Preferences.getInt(AccordionSelection, -1);
			UiHelpers.setExpandedPane(allElementsPropertySheet, idx);
		});
		btnXLinks.selectedProperty().set(Preferences.getBoolean(btnXLinks.idProperty().get(), true));
		btnChildLinks.selectedProperty().set(Preferences.getBoolean(btnChildLinks.idProperty().get(), true));

		zoomTarget.setScaleX(Preferences.getDouble(zoomTarget.idProperty().get() + scaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(zoomTarget.idProperty().get() + scaleY, zoomTarget.getScaleY()));
		// get splitPanes later after UI has settled down
		splitPane1.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {
					double s1 = Preferences.getDouble(splitPane1.getId(), DefaultWindowSettings.splitter1());
					double s2 = Preferences.getDouble(splitPane2.getId(), DefaultWindowSettings.splitter2());
					splitPane1.setDividerPositions(UiHelpers.getSplitPanePositions(s1, splitPane1.getId()));
					splitPane2.setDividerPositions(UiHelpers.getSplitPanePositions(s2, splitPane2.getId()));
					observable.removeListener(this);
				});
			}
		});

	}

	// not used i think
	@Override
	public String getUserProjectPath() {
		return userProjectPath.get();
	}

	// humm... suspect. Should refer to UserProjectLink
	public StringProperty getUserProjectPathProperty() {
		return userProjectPath;
	}

	private String getMessageText(ErrorMessagable msg) {

		// Text t;
		String t = "";
		switch (verbosity) {
		case brief: {
//			t = new Text(msg.verbose1() + "\n\n");
			t = msg.verbose1() + "\n\n";
			break;
		}
		case medium: {
//			t = new Text(msg.verbose2() + "\n\n");
			t = msg.verbose2() + "\n\n";
			break;
		}
		default: {
//			t = new Text(msg.toString() + "\n\n");
			t = msg.toString() + "\n\n";
		}
		}

		return t;
	}

//================== ERROR MSG LISTENER =============
	@Override
	public void onStartCheck() {
		// System.out.println("Start check: "+Thread.currentThread().getName());
		btnDeploy.setDisable(true);
		btnCheck.setDisable(true);
		lblChecking.setVisible(true);
		trafficLight.fillProperty().set(Color.RED);

		textAreaErrorMsgs.clear();
		lstErrorMsgs.clear();
		// Don't know why the trafficLight and disable buttons don't work unless this
		// method is initiated by the Check button itself.
	}

	@Override
	public void onEndCheck(boolean valid) {
		isValid = valid;
		// System.out.println("End check: "+Thread.currentThread().getName());
		Platform.runLater(() -> {
			// System.out.println("Finish check: "+Thread.currentThread().getName());
			// System.out.println("----------------------------------------");
			btnDeploy.setDisable(false);
			btnCheck.setDisable(false);
			lblChecking.setVisible(false);
			setButtonState();
		});
	}

	private void sortErrors() {
		lstErrorMsgs.sort(new Comparator<ErrorMessagable>() {

			@Override
			public int compare(ErrorMessagable m1, ErrorMessagable m2) {
				// keep the same order regardless of the verbosity
				return m1.verbose1().compareToIgnoreCase(m2.verbose1());
			}
		});

	}

	@Override
	public void onReceiveMsg(ErrorMessagable msg) {
		Platform.runLater(() -> {
			lstErrorMsgs.add(msg);
			sortErrors();
			textAreaErrorMsgs.clear();
			for (ErrorMessagable m : lstErrorMsgs)
				textAreaErrorMsgs.appendText(getMessageText(m));
		});
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
		lastSelectedNode = null;
		UserProjectLink.unlinkUserProject();
		setButtonState();
		putPreferences();
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
		// GraphState.setChanged();
		GraphState.clear();
		ConfigGraph.validateGraph();
		setButtonState();
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
					String propertyDesciption = "Desciption property from archtype";
					result.add(makeItemType(key, node, editable, category, propertyDesciption));
				}
			}

		// get the out edges here
		for (ALEdge edge : node.edges(Direction.OUT))
			if (edge instanceof ALDataEdge) {
				ALDataEdge dataEdge = (ALDataEdge) edge;
				for (String key : dataEdge.properties().getKeysAsSet()) {
					boolean editable = model.propertyEditable(edge.classId(), key);
					if (editable || showNonEditable) {
						result.add(makeItemType(key, dataEdge, editable, category, "Something"));
					}
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
				if (cat == null)
					cat = vn.id();
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

	private Item makeItemType(String key, DataHolder element, boolean editable, String category, String description) {
		Object value = element.properties().getPropertyValue(key);
		if (value instanceof FileType) {
			FileTypeItem fti = new FileTypeItem(this, key, (ElementAdapter) element, true, category, description);
			return fti;
		} else if (value instanceof StatisticalAggregatesSet)
			return new StatsTypeItem(this, key, (ElementAdapter) element, true, category, description);
		else if (value instanceof PopulationVariablesSet)
			return new PopTypeItem(this, key, (ElementAdapter) element, true, category, description);
		else if (value instanceof DateTimeType) {
			return new DateTimeItem(this, key, (ElementAdapter) element, true, category, description);
		} else if (value instanceof TrackerType) {
			return new TrackerTypeItem(this, key, (ElementAdapter) element, true, category, description);
		} else if (value instanceof Interval) {
			return new IntervalItem(this, key, (ElementAdapter) element, true, category, description);
		} else if (value instanceof IntegerRange) {
			return new IntegerRangeItem(this, key, (ElementAdapter) element, true, category, description);
		} else if (value instanceof StringTable) {
			StringTable st = (StringTable) value;
			if (st.getDimensioners().length == 1)
				return new StringTableItem(this, key, (ElementAdapter) element, editable, category, description);
		} // else if (value instanceof DoubleTable) {
			// DoubleTable dt = (DoubleTable) value;
			// if (dt.getDimensioners().length ==1)
			// return new
			// DoubleTableItem(this,key,(ElementAdapter)element,true,category,description);
//		}
		return new SimpleMMPropertyItem(this, key, (ElementAdapter) element, editable, category, description);
	}

	private void initialisePropertySheets() {
		lastSelectedNode = null;
		fillGraphPropertySheet();
		fillNodePropertySheet(lastSelectedNode);
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

	private VisualNode lastSelectedNode = null;

	@Override
	public void onNodeSelected(VisualNode node) {
		lastSelectedNode = node;
		fillNodePropertySheet(lastSelectedNode);
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
			lastSelectedNode = newNode;
			initialisePropertySheets();
			setButtonState();
			GraphState.setChanged();
			ConfigGraph.validateGraph();
		}
	}

	@Override
	public void onNodeDeleted() {
		lastSelectedNode = null;
		initialisePropertySheets();
		setButtonState();
	}

	@Override
	public void onTreeCollapse() {
		lastSelectedNode = null;
		initialisePropertySheets();
		setButtonState();
	}

	@Override
	public void onTreeExpand() {
		lastSelectedNode = null;
		initialisePropertySheets();
		setButtonState();
	}

	@Override
	public void onStateChange(boolean state) {
		setButtonState();
	}

	public void setButtonState() {
		boolean isOpen = Project.isOpen();
		boolean isClean = !GraphState.changed() & isOpen;
		boolean isConnected = UserProjectLink.haveUserProject();
		miSetCodePath.setDisable(isConnected);
		miDisconnect.setDisable(!isConnected);
		menuItemSave.setDisable(isClean);
		menuItemSaveAs.setDisable(!isOpen);
		btnChildLinks.setDisable(!isOpen);
		btnXLinks.setDisable(!isOpen);
		btnLayout.setDisable(!isOpen);
		btnCheck.setDisable(!isOpen);
		boolean cleanAndValid = isClean && isValid;
		btnDeploy.setDisable(!cleanAndValid);

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
	public void onElementRenamed() {
		initialisePropertySheets();
		setButtonState();
	}

	@Override
	public void onItemEdit(Object changedItem) {
		Item item = (Item) changedItem;
		if (nodePropertySheet.getItems().contains(item))
			fillGraphPropertySheet();
		else if (allElementsPropertySheet.getItems().contains(item)) {
			fillNodePropertySheet(lastSelectedNode);

		}
	}

	@Override
	public void onNewEdge(VisualEdge e) {
		initialisePropertySheets();
		setButtonState();
	}

	@Override
	public void onEdgeDeleted() {
		initialisePropertySheets();
		setButtonState();

	}

	@FXML
	void onAbout(ActionEvent event) {
		Dialog<ButtonType> dlg = new Dialog<>();
		dlg.initOwner((Window) Dialogs.owner());
		dlg.setTitle("About ModelMaker");
		ButtonType done = new ButtonType("Close", ButtonData.OK_DONE);
		HBox content = new HBox();
		VBox leftContent = new VBox();
		VBox rightContent = new VBox();
		ImageView imageView = new ImageView(new Image(Images.class.getResourceAsStream("3worlds-5.jpg")));
		imageView.preserveRatioProperty().set(true);
		rightContent.getChildren().addAll(imageView, new Label("Three Worlds - M. C. Escher (1955)"));
		TextFlow textFlow = new TextFlow();
		textFlow.setPrefWidth(400);
		textFlow.setTextAlignment(TextAlignment.CENTER);
		textFlow.setLineSpacing(10);
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(4);
		dropShadow.setOffsetY(6);
		dropShadow.setHeight(5);
		Text text_1 = new Text("ModelMaker\n");
		text_1.setEffect(dropShadow);
		text_1.setFont(Font.font("Helvetica", 30));

		Text text_2 = new Text("\"Though the organisms may claim our primary interest, " + //
				"when we are trying to think fundamentally, we cannot separate them from their special environment, " + //
				"with which they form one physical system\"");

		text_2.setFont(Font.font("Helvetica", FontPosture.ITALIC, 12));

		Text text_3 = new Text(" A. G. Tanlsey (1935).\n");
		text_3.setFont(Font.font("Helvetica", 12));

		textFlow.getChildren().addAll(text_1, text_2, text_3);

		content.getChildren().addAll(rightContent, leftContent);
		TextArea textArea = new TextArea();
		Scanner sc = new Scanner(MmController.class.getResourceAsStream("aboutMM.txt"));

		while (sc.hasNext()) {
			textArea.appendText(sc.nextLine());
			textArea.appendText("\n");
		}
		sc.close();

		textArea.setWrapText(true);
		textArea.setPrefHeight(400);
		textArea.setEditable(false);
		TextFlow authors = new TextFlow();
		authors.setTextAlignment(TextAlignment.CENTER);
		authors.getChildren().add(new Text("Authors: J. Gignoux, I. Davies and S. Flint"));
		leftContent.getChildren().addAll(textFlow, textArea, new Label(), authors);

		dlg.getDialogPane().setContent(content);
		dlg.getDialogPane().getButtonTypes().addAll(done);
		// dlg.setResizable(true);
		textArea.selectPositionCaret(0);
		textArea.deselect();
		dlg.showAndWait();
	}

}