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
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
import au.edu.anu.rscs.aot.collections.tables.Dimensioner;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.errorMessaging.ErrorList;
import au.edu.anu.rscs.aot.errorMessaging.ErrorListListener;
import au.edu.anu.rscs.aot.errorMessaging.ErrorMessagable;
import au.edu.anu.rscs.aot.util.IntegerRange;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.Caretaker;
import au.edu.anu.twapps.mm.IMMController;
import au.edu.anu.twapps.mm.MMModel;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twapps.mm.graphEditor.IGraphVisualiser;
import au.edu.anu.twapps.mm.layout.LayoutType;
import au.edu.anu.twapps.mm.userProjectFactory.IDETypes;
import au.edu.anu.twapps.mm.userProjectFactory.UserProjectLinkFactory;
import au.edu.anu.twapps.mm.IMMModel;
import au.edu.anu.twapps.mm.MMMemento;
import au.edu.anu.twapps.mm.visualGraph.ElementDisplayText;
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
import au.edu.anu.twuifx.mm.propertyEditors.borderList.BorderListItem;
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
import fr.cnrs.iees.twcore.constants.BorderListType;
import fr.cnrs.iees.twcore.constants.DateTimeType;
import fr.cnrs.iees.twcore.constants.FileType;
import fr.cnrs.iees.twcore.constants.PopulationVariablesSet;
import fr.cnrs.iees.twcore.constants.StatisticalAggregatesSet;
import fr.cnrs.iees.twcore.constants.TrackerType;
import fr.cnrs.iees.twcore.constants.TwFunctionTypes;
import fr.ens.biologie.generic.utils.Interval;

import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

public class MmController implements ErrorListListener, IMMController, IGraphStateListener {
	@FXML
	private MenuItem miImportSnippets;

	@FXML
	private MenuItem miClearSnippets;

	@FXML
	private MenuItem miAbout;

	@FXML
	private MenuItem miUndo;

	@FXML
	private MenuItem miRedo;

	@FXML
	private ToggleButton btnXLinks;

	@FXML
	private ToggleButton btnChildLinks;

	@FXML
	private ToggleButton tglSideline;

	@FXML
	private Button btnSelectAll;

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

//	@FXML
//	private Button btnDocument;

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
	private ComboBox<ElementDisplayText> cbNodeTextChoice;

	@FXML
	private ComboBox<ElementDisplayText> cbEdgeTextChoice;

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

	private LayoutType currentLayout;

	private TreeGraph<VisualNode, VisualEdge> visualGraph;
	private Font font;
	private int fontSize;

	// TODO: make menu options and preferences entry for this choice when netbeans
	// and IntelliJ have been tested
	private IDETypes ideType = IDETypes.eclipse;

	/*******************************************************************************
	 * NB any function that causes checking to take place (e.g. an edit) also causes
	 * SetButtonState() to be called.
	 *******************************************************************************/

	// --------------------------- FXML Start ----------------------
	@FXML
	public void initialize() {

		cbNodeTextChoice.getItems().addAll(ElementDisplayText.values());
		cbNodeTextChoice.getSelectionModel().select(ElementDisplayText.RoleName);
		cbEdgeTextChoice.getItems().addAll(ElementDisplayText.values());
		cbEdgeTextChoice.getSelectionModel().select(ElementDisplayText.RoleName);

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

		btnLayout.setTooltip(getFastToolTip("Re-apply layout"));
		btnXLinks.setTooltip(getFastToolTip("Show/hide cross-links"));
		btnChildLinks.setTooltip(getFastToolTip("Show/hide parent-child edges"));
		btnSelectAll.setTooltip(getFastToolTip("Show all nodes"));
		tglSideline.setTooltip(getFastToolTip("Move isolated nodes aside"));
		cbEdgeTextChoice.setTooltip(getFastToolTip("Edge text display options"));
		cbNodeTextChoice.setTooltip(getFastToolTip("Node text display options"));

		/** Set a handler to refresh the Open menu items when selected */
		menuOpen.addEventHandler(Menu.ON_SHOWING, event -> updateOpenProjectsMenu(menuOpen));

		/** add template entries to the "New" menu */
		buildNewMenu();

		/** This class has all the housework for managing graph */
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

		// Listen for error msgs from error system
		ErrorList.addListener(this);

		// Setup zooming from the graph display pane (zoomTarget)
		// zoomTarget.setOnScroll(event -> UiHelpers.zoom(zoomTarget, event));
		CenteredZooming.center(scrollPane, scrollContent, group, zoomTarget);
		// are prefs saved regardless of graphState??
	}

	private Tooltip getFastToolTip(String text) {
		Tooltip result = new Tooltip(text);
		result.setShowDelay(Duration.millis(200));
		result.setHideDelay(Duration.millis(200));
		return result;
	}

	@FXML
	void handleDisconnectJavaProject(ActionEvent event) {
		userProjectPath.set("");
		String header = "'" + Project.getDisplayName() + "' is now disconnected from Java project '"
				+ UserProjectLink.projectRoot().getName() + "'.";
		UserProjectLink.unlinkUserProject();
		ConfigGraph.validateGraph();
		Dialogs.infoAlert("Project disconnected", header, "");
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

	@FXML
	void handleCheck(ActionEvent event) {
		ConfigGraph.validateGraph();
	}

	@FXML
	void handleMenuExit(ActionEvent event) {
		if (model.canClose()) {
			Caretaker.finalise();
			putPreferences();
			Platform.exit();
			System.exit(0);
		}
	}

	@FXML
	void handleLayout(ActionEvent event) {
		final double duration = GraphVisualiserfx.animateSlow;

		doLayout(duration);

	}

	@FXML
	void handleImport(ActionEvent event) {
		model.doImport();
	}

	@FXML
	void handleSave(ActionEvent event) {
		model.doSave();
	}

	@FXML
	void handleSaveAs(ActionEvent event) {
		model.doSaveAs();
		for (VisualNode root : visualGraph.roots())
			if (root.cClassId().equals(N_ROOT.label()))
				visualiser.onNodeRenamed(root);
	}

	@FXML
	void handleOnDeploy(ActionEvent event) {
		model.doDeploy();
	}

	@FXML
	void handlePaneOnMouseClicked(MouseEvent e) {
		if (newNode != null) {
			newNode.setPosition(e.getX() / zoomTarget.getWidth(), e.getY() / zoomTarget.getHeight());
			visualiser.onNewNode(newNode);
			zoomTarget.setCursor(Cursor.DEFAULT);
			String desc = "New node [" + newNode.getConfigNode().toShortString() + "]";
			lastSelectedNode = newNode;
			newNode = null;
			initialisePropertySheets();
			GraphState.setChanged();
			ConfigGraph.validateGraph();

			model.addState(desc);
		}
	}

	@FXML
	void doRedo(ActionEvent event) {
		// try and recover from a deleted project directory
		MMMemento m = (MMMemento) Caretaker.succ();
		if (mementoFilesExist(m)) {
			model.restore(m);
			GraphState.setChanged();
		} else {
			Caretaker.initialise();
			model.doSave();
		}
		setUndoRedoBtns();
	}

	private boolean mementoFilesExist(MMMemento m) {
		return (m.getState().getFirst().exists() && m.getState().getSecond().exists()
				&& m.getState().getThird().exists());
	}

	@FXML
	void doUndo(ActionEvent event) {
		MMMemento m = (MMMemento) Caretaker.prev();
		if (mementoFilesExist(m)) {
			model.restore(m);
			GraphState.setChanged();
		} else {
			Caretaker.initialise();
			model.doSave();
		}
		setUndoRedoBtns();
	}

	@FXML
	void onSelectAll(ActionEvent event) {
		visualiser.onSelectAll();
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

//	@FXML
//	void doDocumentation(ActionEvent event) {
//		DocoGenerator gen = new DocoGenerator(ConfigGraph.getGraph());
//		gen.generate();
//	}

	@FXML
	void onImportSnippets(ActionEvent event) {

		List<String> errorList = new ArrayList<>();
		Map<String, TreeGraphDataNode> snippetNodes = new HashMap<>();
		Map<String, List<String>> snippetCodes = UserProjectLink.getSnippets();
		for (TreeGraphDataNode n : ConfigGraph.getGraph().nodes())
			if (n.classId().equals(N_SNIPPET.label())) {
				char c[] = n.getParent().id().toCharArray();
				c[0] = Character.toLowerCase(c[0]);
				snippetNodes.put(new String(c), n);
			}

		for (Map.Entry<String, TreeGraphDataNode> e : snippetNodes.entrySet())
			if (!snippetCodes.containsKey(e.getKey()))
				errorList.add("No '" + e.getKey() + "' code found for " + e.getValue().getParent().id() + "->"
						+ e.getValue().id() + ".");

		for (Map.Entry<String, List<String>> e : snippetCodes.entrySet()) {
			if (!snippetNodes.containsKey(e.getKey()))
				errorList.add("No snippet node present to receive '" + e.getKey() + "' code.");
		}

		Map<String, List<String>> successfulImports = new HashMap<>();
		if (!snippetNodes.isEmpty()) {

			for (Map.Entry<String, List<String>> method : snippetCodes.entrySet()) {
				TreeGraphDataNode snippetNode = snippetNodes.get(method.getKey());
				if (snippetNode != null) {// may have no lines
					List<String> lines = method.getValue();
					StringTable newValue = new StringTable(new Dimensioner(lines.size()));
					for (int i = 0; i < lines.size(); i++)
						newValue.setByInt(lines.get(i), i);
					snippetNode.properties().setProperty(P_SNIPPET_JAVACODE.key(), newValue);
					successfulImports.put(snippetNode.getParent().id() + "->" + snippetNode.id(), lines);
					GraphState.setChanged();
				}
			}
		}
		String title = "Snippet Import";
		String header;
		header = "Import code from " + ConfigGraph.getGraph().root().id() + ".java";
		String content = "";
		for (Map.Entry<String, List<String>> entry : successfulImports.entrySet()) {
			content += entry.getKey() + "\n";
//			for (String line : entry.getValue()) {
//				content += line + "\n";
//			}
		}

		for (String error : errorList) {
			content += error + "\n";
		}
		// Best if we have a list of paired and unpaired code-snippet node
		Dialogs.infoAlert(title, header, content);
	}

	@FXML
	void doClearSnippets(ActionEvent event) {
		boolean changed = false;
		for (TreeGraphDataNode n : ConfigGraph.getGraph().nodes())
			if (n.classId().equals(N_SNIPPET.label())) {
				if (n.getParent() != null) {
					TreeGraphDataNode func = (TreeGraphDataNode) n.getParent();
					TwFunctionTypes ft = (TwFunctionTypes) func.properties().getPropertyValue(P_FUNCTIONTYPE.key());
					String entry = "";
					if (ft.returnType().equals("boolean"))
						entry = "\treturn false;";
					StringTable newValue = new StringTable(new Dimensioner(1));
					newValue.fillWith(entry);
					n.properties().setProperty(P_SNIPPET_JAVACODE.key(), newValue);
					changed = true;
				} else {
					Dialogs.warnAlert("Clear snippet", "Function undefined",
							"'" + n.id() + "' does not have a defining function\n and has not been cleared.");
				}
			}
		if (changed) {
			initialisePropertySheets();
			GraphState.setChanged();
		}
	}

	// ---------------FXML End -------------------------

	// ---------------IMMController Start ---------------------
	@Override
	public void onProjectClosing() {
		GraphState.clear();
		if (visualiser != null)
			visualiser.close();
		visualiser = null;
		nodePropertySheet.getItems().clear();
		allElementsPropertySheet.getItems().clear();
		zoomTarget.getChildren().clear();
		visualGraph = null;
		lastSelectedNode = null;
		UserProjectLink.unlinkUserProject();
		stage.setTitle(DefaultWindowSettings.defaultName());
		setButtonState();
		putPreferences();
	}

	@Override
	public void onProjectOpened(TreeGraph<VisualNode, VisualEdge> layoutGraph) {
		this.visualGraph = layoutGraph;
		Cursor oldCursor = setWaitCursor();
		visualiser = new GraphVisualiserfx(visualGraph, //
				zoomTarget, //
				nodeRadiusProperty, //
				btnChildLinks.selectedProperty(), //
				btnXLinks.selectedProperty(), //
				cbNodeTextChoice.getSelectionModel().selectedItemProperty(), //
				cbEdgeTextChoice.getSelectionModel().selectedItemProperty(), //
				tglSideline.selectedProperty(), //
				fontProperty, this, model);

		final double duration = GraphVisualiserfx.animateFast;
		visualiser.initialiseView(duration);

		initialisePropertySheets();

		getPreferences();

		setCursor(oldCursor);
		stage.setTitle(Project.getDisplayName());
	}

	private VisualNode newNode;

	@Override
	public void onNewNode(VisualNode node) {
		zoomTarget.setCursor(Cursor.CROSSHAIR);
		newNode = node;
	}

	@Override
	public void onNewEdge(VisualEdge e) {
		initialisePropertySheets();
	}

	@Override
	public void onEdgeDeleted() {
		initialisePropertySheets();
	}

	@Override
	public void onNodeDeleted() {
		lastSelectedNode = null;
		initialisePropertySheets();
	}

	@Override
	public void onTreeCollapse() {
		lastSelectedNode = null;
		initialisePropertySheets();
	}

	@Override
	public void onTreeExpand() {
		lastSelectedNode = null;
		initialisePropertySheets();
	}

	@Override
	public void doLayout(double duration) {
		callLayout(null, currentLayout, duration);
	}

	@Override
	public void doFocusedLayout(VisualNode root, LayoutType layout, double duration) {
		callLayout(root, layout, duration);
		currentLayout = layout;
	}

	// used only by Visualiser?? Passed by a node onClicked if not for local popup
	// menu
	@Override
	public void onNodeSelected(VisualNode node) {
		lastSelectedNode = node;
		fillNodePropertySheet(lastSelectedNode);
	}

	@Override
	public void onElementRenamed() {
		initialisePropertySheets();
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
	public void setDefaultTitle() {
		stage.setTitle(DefaultWindowSettings.defaultName());

	}

	@Override
	public void collapsePredef() {
		visualiser.collapsePredef();

	}

	@Override
	public LayoutType getCurrentLayout() {
		return currentLayout;
	}

	@Override
	public void onRollback(TreeGraph<VisualNode, VisualEdge> layoutGraph) {
		visualGraph = layoutGraph;
		visualiser.onRollback(layoutGraph);
		lastSelectedNode = null;
		initialisePropertySheets();
	}

	// -------------- IMMController End ---------------------

	// -------------- Preferencable Start ---------------------
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
	private static final String CurrentLayoutKey = "currentLayout";

	private static final String NodeTextDisplayChoice = "nodeTextDisplayChoice";
	private static final String EdgeTextDisplayChoice = "edgeTextDisplayChoice";
	private static final String ScrollHValue = "HValue";
	private static final String ScrollVValue = "VValue";

	public void putPreferences() {
		if (Project.isOpen()) {

			Preferences.putString(UserProjectPath, userProjectPath.get());
			Preferences.putEnum(allElementsPropertySheet.idProperty().get() + Mode, allElementsPropertySheet.getMode());
			Preferences.putEnum(nodePropertySheet.idProperty().get() + Mode, nodePropertySheet.getMode());
			Preferences.putDouble(splitPane1.idProperty().get(), splitPane1.getDividerPositions()[0]);
			Preferences.putDouble(splitPane2.idProperty().get(), splitPane2.getDividerPositions()[0]);
			Preferences.putDouble(zoomTarget.idProperty().get() + scaleX, zoomTarget.getScaleX());
			Preferences.putDouble(zoomTarget.idProperty().get() + scaleY, zoomTarget.getScaleY());
			Preferences.putDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
			Preferences.putBoolean(mainMaximized, stage.isMaximized());
			Preferences.putBoolean(btnXLinks.idProperty().get(), btnXLinks.isSelected());
			Preferences.putBoolean(btnChildLinks.idProperty().get(), btnChildLinks.isSelected());
			Preferences.putBoolean(tglSideline.idProperty().get(), tglSideline.isSelected());
			Preferences.putInt(fontSizeKey, fontSize);
			Preferences.putInt(nodeSizeKey, nodeRadiusProperty.get());
			Preferences.putInt(jitterKey, jitterProperty.get());
			Preferences.putInt(tabPaneProperties.idProperty().get(),
					tabPaneProperties.getSelectionModel().getSelectedIndex());
			Preferences.putInt(AccordionSelection, UiHelpers.getExpandedPaneIndex(allElementsPropertySheet));
			Preferences.putEnum(CurrentLayoutKey, currentLayout);
			Preferences.putEnum(NodeTextDisplayChoice, cbNodeTextChoice.getSelectionModel().getSelectedItem());
			Preferences.putEnum(EdgeTextDisplayChoice, cbEdgeTextChoice.getSelectionModel().getSelectedItem());
			Preferences.putDouble(scrollPane.idProperty().get() + ScrollHValue, scrollPane.getHvalue());
			Preferences.putDouble(scrollPane.idProperty().get() + ScrollVValue, scrollPane.getVvalue());

			Preferences.flush();
		}
	}

	public void getPreferences() {
		Preferences.initialise(Project.makeProjectPreferencesFile());
		// get path string for user project
		String prjtmp = Preferences.getString(UserProjectPath, "");
		if (!prjtmp.equals("")) {
			// check java project still exists.
			UserProjectLink.unlinkUserProject();
			if (UserProjectLinkFactory.makeEnv(new File(prjtmp), ideType)) {
				userProjectPath.set(UserProjectLink.projectRoot().getAbsolutePath());
			} else
				userProjectPath.set("");
		} else
			userProjectPath.set("");

		double[] ws = Preferences.getDoubles(mainFrameName, DefaultWindowSettings.getX(), DefaultWindowSettings.getY(),
				DefaultWindowSettings.getWidth(), DefaultWindowSettings.getHeight());
		stage.setX(ws[0]);
		stage.setY(ws[1]);
		stage.setWidth(ws[2]);
		stage.setHeight(ws[3]);
		stage.setMaximized(Preferences.getBoolean(mainMaximized, stage.isMaximized()));

		setFontSize(Preferences.getInt(fontSizeKey, 10));
		setNodeRadius(Preferences.getInt(nodeSizeKey, 8));
		setJitter(Preferences.getInt(jitterKey, 0));
		tabPaneProperties.getSelectionModel()
				.select(Math.max(0, Preferences.getInt(tabPaneProperties.idProperty().get(), 0)));

		currentLayout = (LayoutType) Preferences.getEnum(CurrentLayoutKey, LayoutType.OrderedTree);
		cbNodeTextChoice.getSelectionModel()
				.select((ElementDisplayText) Preferences.getEnum(NodeTextDisplayChoice, ElementDisplayText.RoleName));
		cbEdgeTextChoice.getSelectionModel()
				.select((ElementDisplayText) Preferences.getEnum(EdgeTextDisplayChoice, ElementDisplayText.RoleName));

		PropertySheet.Mode mode = (PropertySheet.Mode) Preferences
				.getEnum(allElementsPropertySheet.idProperty().get() + Mode, PropertySheet.Mode.CATEGORY);
		allElementsPropertySheet.setMode(mode);
		mode = (PropertySheet.Mode) Preferences.getEnum(nodePropertySheet.idProperty().get() + Mode,
				PropertySheet.Mode.NAME);
		nodePropertySheet.setMode(mode);
		int idx = Preferences.getInt(AccordionSelection, -1);
		UiHelpers.setExpandedPane(allElementsPropertySheet, idx);

		btnXLinks.selectedProperty().set(Preferences.getBoolean(btnXLinks.idProperty().get(), true));
		btnChildLinks.selectedProperty().set(Preferences.getBoolean(btnChildLinks.idProperty().get(), true));
		tglSideline.selectedProperty().set(Preferences.getBoolean(tglSideline.idProperty().get(), false));

		zoomTarget.setScaleX(Preferences.getDouble(zoomTarget.idProperty().get() + scaleX, 1));
		zoomTarget.setScaleY(Preferences.getDouble(zoomTarget.idProperty().get() + scaleY, 1));

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
					scrollPane.setHvalue(Preferences.getDouble(scrollPane.idProperty().get() + ScrollHValue, 0));
					scrollPane.setVvalue(Preferences.getDouble(scrollPane.idProperty().get() + ScrollVValue, 0));

				});
			}
		});

	}

	// -------------- Preferencable End ---------------------

	// -------------- ModelMakerfx Start---------------------
	public boolean canClose() {
		return model.canClose();
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	// humm... suspect. Should refer to UserProjectLink
	public StringProperty getUserProjectPathProperty() {
		return userProjectPath;
	}

	// -------------- ModelMakerfx End ---------------------

	// ----------------- ErrorListListener Start ------------
	@Override
	public void onStartCheck() {
		Platform.runLater(() -> {
			btnDeploy.setDisable(true);
//			btnDocument.setDisable(true);
			btnCheck.setDisable(true);
			lblChecking.setVisible(true);
			trafficLight.fillProperty().set(Color.RED);

			textAreaErrorMsgs.clear();
			lstErrorMsgs.clear();
		});
		// Don't know why the trafficLight and disable buttons don't work unless this
		// method is initiated by the Check button itself.
	}

	private boolean isValid = false;

	@Override
	public void onEndCheck(boolean valid) {
		isValid = valid;
		Platform.runLater(() -> {
			btnDeploy.setDisable(false);
//			btnDocument.setDisable(false);
			btnCheck.setDisable(false);
			lblChecking.setVisible(false);
			setButtonState();
		});
	}

	@Override
	public void onReceiveMsg(ErrorMessagable msg) {
		Platform.runLater(() -> {
			lstErrorMsgs.add(msg);
			refreshErrorMessages();
			setButtonState();
		});
	}

	// ----------------- ErrorListListener END ------------

	// ----------------- GraphStatetListener Start ------------
	@Override
	public void onStateChange(boolean state) {
		setButtonState();
	}
	// ----------------- GraphStatetListener End ------------

	private void buildNewMenu() {
		Map<MenuItem, LibraryTable> map = new HashMap<>();
		menuNew.getItems().clear();
		Menu muTemplates = new Menu("Templates");
		Menu muTutorials = new Menu("Tutorials");
		Menu muModels = new Menu("Model Library");
		menuNew.getItems().addAll(muTemplates, muTutorials, muModels);
		for (LibraryTable entry : LibraryTable.values()) {
			MenuItem mi = new MenuItem(entry.displayName());
			mi.setMnemonicParsing(false);
			map.put(mi, entry);
			switch (entry.libraryType()) {
			case Template: {
				muTemplates.getItems().add(mi);
				break;
			}
			case Tutorial: {
				muTutorials.getItems().add(mi);
				break;
			}
			default: {
				muModels.getItems().add(mi);
			}
			}

			mi.setOnAction((e) -> {

				LibraryTable lt = map.get(e.getSource());
				TreeGraph<TreeGraphDataNode, ALEdge> libGraph = lt.getGraph();
				TreeGraphDataNode root = libGraph.root();
				if (root == null || !root.classId().equals(N_ROOT.label())) {
					String rnames = lt.displayName() + " roots:\n";
					int i = 0;
					for (TreeGraphDataNode r : libGraph.roots())
						rnames += ((++i) + ") " + r.toShortString() + "\n");
					String title = "Library graph error";
					String content = "Graphs must have a single root (ref '" + N_ROOT.label() + "').";
					Dialogs.errorAlert(title, content, rnames);
					return;
				}

				if (root.properties().hasProperty(P_MODEL_BUILTBY.key())) {
					DateTimeFormatter fm = DateTimeFormatter.ofPattern("d MMM uuuu");
					LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
					String date = " (" + currentDate.format(fm) + ")";
					root.properties().setProperty(P_MODEL_BUILTBY.key(), System.getProperty("user.name") + date);
				}

				model.doNewProject(libGraph);
			});
		}

	}

	private void setFontSize(int size) {
		this.fontSize = size;
		spinFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 40, fontSize));
		fontProperty.set(Font.font("Verdana", fontSize));
	}

	private void setNodeRadius(int size) {
		spinNodeSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 40, size));
		nodeRadiusProperty.set(size);
	}

	private void setJitter(int size) {
		spinJitter.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, size));
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
		refreshErrorMessages();
	}

	private void refreshErrorMessages() {
		textAreaErrorMsgs.clear();
		sortErrors();
		int count = 0;
		for (ErrorMessagable msg : lstErrorMsgs) {
			count++;
			textAreaErrorMsgs.appendText(count + ". " + getMessageText(msg));
		}

	}

	private void callLayout(VisualNode root, LayoutType layout, double duration) {
		int size = jitterProperty.get();
		double dSize = size;
		dSize = dSize / 10.0;
		visualiser.doLayout(root, dSize, layout, btnChildLinks.isSelected(), btnXLinks.isSelected(),
				tglSideline.isSelected(), duration);
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
			MenuItem mi = new MenuItem((i + 1) + " " + names[i]);
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
				model.doOpenProject(file);
				textAreaErrorMsgs.clear();
				lstErrorMsgs.clear();
				isValid = false;
			});
		}
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

	private void sortErrors() {
		lstErrorMsgs.sort(new Comparator<ErrorMessagable>() {

			@Override
			public int compare(ErrorMessagable m1, ErrorMessagable m2) {
				// keep the same order regardless of the verbosity
				return m1.verbose1().compareToIgnoreCase(m2.verbose1());
			}
		});

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
			// ObservableList<Item> list = null;
			if (cn instanceof DataHolder) {
				ObservableList<Item> list = getNodeItems((TreeGraphDataNode) cn, cn.id(), showNonEditables,
						visualNode.isPredefined());
				nodePropertySheet.getItems().setAll(list);
			}
		}
	}

	private ObservableList<Item> getNodeItems(TreeGraphDataNode node, String category, boolean showNonEditable,
			boolean isPredef) {

		ObservableList<Item> result = FXCollections.observableArrayList();
		String propertyDesciption = null;
		if (node.properties().hasProperty(P_FIELD_DESCRIPTION.key()))
			propertyDesciption = (String) node.properties().getPropertyValue(P_FIELD_DESCRIPTION.key());
		for (String key : node.properties().getKeysAsSet())

			if (node.properties().getPropertyValue(key) != null) {
				boolean editable = model.propertyEditable(node.classId(), key);
				if (editable || showNonEditable) {
					boolean canEdit = editable;
					if (isPredef)
						canEdit = false;
					result.add(makeItemType(key, node, canEdit, category, propertyDesciption));
				}
			}

		// get the out edges here
		for (ALEdge edge : node.edges(Direction.OUT))
			if (edge instanceof ALDataEdge) {
				ALDataEdge dataEdge = (ALDataEdge) edge;
				for (String key : dataEdge.properties().getKeysAsSet()) {
					boolean editable = model.propertyEditable(edge.classId(), key);
					if (editable || showNonEditable) {
						boolean canEdit = editable;
						if (isPredef)
							canEdit = false;
						result.add(makeItemType(key, dataEdge, canEdit, category, "Something"));
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
					obsSubList = getNodeItems((TreeGraphDataNode) cn, cat, showNonEditables, vn.isPredefined());
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
			return new StatsTypeItem(this, key, (ElementAdapter) element, editable, category, description);
		else if (value instanceof PopulationVariablesSet)
			return new PopTypeItem(this, key, (ElementAdapter) element, editable, category, description);
		else if (value instanceof DateTimeType) {
			return new DateTimeItem(this, key, (ElementAdapter) element, editable, category, description);
		} else if (value instanceof TrackerType) {
			return new TrackerTypeItem(this, key, (ElementAdapter) element, editable, category, description);
		} else if (value instanceof Interval) {
			return new IntervalItem(this, key, (ElementAdapter) element, editable, category, description);
		} else if (value instanceof IntegerRange) {
			return new IntegerRangeItem(this, key, (ElementAdapter) element, editable, category, description);
		} else if (value instanceof BorderListType) { // must come before StringTable
			if (((BorderListType) value).size() == 4)
				return new BorderListItem(this, key, (ElementAdapter) element, editable, category, description);
		} else if (value instanceof StringTable) {
			StringTable st = (StringTable) value;
			if (st.getDimensioners().length == 1)
				return new StringTableItem(this, key, (ElementAdapter) element, editable, category, description);
		}
		// DoubleTable dt = (DoubleTable) value;
		// if (dt.getDimensioners().length ==1)
		// return new
		// DoubleTableItem(this,key,(ElementAdapter)element,true,category,description);
//		}
		return new SimpleMMPropertyItem(this, key, (ElementAdapter) element, editable, category, description);
	}

	private void initialisePropertySheets() {
//		lastSelectedNode = null;
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

	private VisualNode lastSelectedNode = null;

	private void setUndoRedoBtns() {
		miRedo.setDisable(!Caretaker.hasSucc());
		if (Caretaker.hasSucc()) {
			miRedo.setText("Redo '" + Caretaker.getSuccDescription() + "'");
		} else
			miRedo.setText("Redo");

		miUndo.setDisable(!Caretaker.hasPrev());
		if (Caretaker.hasPrev()) {
			miUndo.setText("Undo '" + Caretaker.getPrevDescription() + "'");
		} else
			miUndo.setText("Undo");

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
		cbNodeTextChoice.setDisable(!isOpen);
		cbEdgeTextChoice.setDisable(!isOpen);
		btnSelectAll.setDisable(!isOpen);
		tglSideline.setDisable(!isOpen);
		btnLayout.setDisable(!isOpen);
		btnCheck.setDisable(!isOpen);
		boolean cleanAndValid = isClean && isValid;
		btnDeploy.setDisable(!cleanAndValid);
//		btnDocument.setDisable(!cleanAndValid);
		boolean snp = !cleanAndValid || !UserProjectLink.haveUserProject();
		miImportSnippets.setDisable(snp);
		miClearSnippets.setDisable(!isOpen);
		miRedo.setDisable(!Caretaker.hasSucc());
		if (Caretaker.hasSucc()) {
			miRedo.setText("Redo '" + Caretaker.getSuccDescription() + "'");
		} else
			miRedo.setText("Redo");

		setUndoRedoBtns();

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

}