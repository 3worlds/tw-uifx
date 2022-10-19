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

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import javafx.scene.effect.DropShadow;
import au.edu.anu.omhtk.Language;
import au.edu.anu.omhtk.preferences.IPreferences;
import au.edu.anu.omhtk.preferences.PrefImpl;
import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.omugi.collections.tables.*;
import au.edu.anu.aot.errorMessaging.*;
import au.edu.anu.rscs.aot.util.*;
import au.edu.anu.twapps.dialogs.DialogsFactory;
import au.edu.anu.twapps.mm.*;
import au.edu.anu.twapps.mm.configGraph.ConfigGraph;
import au.edu.anu.twapps.mm.layout.LayoutType;
import au.edu.anu.twapps.mm.undo.*;
import au.edu.anu.twapps.mm.userProjectFactory.IDETypes;
import au.edu.anu.twapps.mm.userProjectFactory.UserProjectLinkFactory;
import au.edu.anu.twapps.mm.layoutGraph.*;
import au.edu.anu.twcore.graphState.*;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.userProject.UserProjectLink;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.*;
import au.edu.anu.twuifx.mm.propertyEditors.DoubleTable.DoubleTableItem;
import au.edu.anu.twuifx.mm.propertyEditors.IntTable.IntTableItem;
import au.edu.anu.twuifx.mm.propertyEditors.StringTable.StringTableItem;
import au.edu.anu.twuifx.mm.propertyEditors.borderList.BorderListItem;
import au.edu.anu.twuifx.mm.propertyEditors.boxType.BoxItem;
import au.edu.anu.twuifx.mm.propertyEditors.dateTimeType.DateTimeItem;
import au.edu.anu.twuifx.mm.propertyEditors.fileType.FileTypeItem;
import au.edu.anu.twuifx.mm.propertyEditors.integerRangeType.IntegerRangeItem;
import au.edu.anu.twuifx.mm.propertyEditors.intervalType.IntervalItem;
import au.edu.anu.twuifx.mm.propertyEditors.populationType.PopTypeItem;
import au.edu.anu.twuifx.mm.propertyEditors.statsType.StatsTypeItem;
import au.edu.anu.twuifx.mm.propertyEditors.trackerType.TrackerTypeItem;
import au.edu.anu.twuifx.mm.visualise.GraphVisualiserfx;
import au.edu.anu.twuifx.utils.UiHelpers;
import au.edu.anu.ymuit.util.CenteredZooming;
import fr.cnrs.iees.graph.DataHolder;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.ElementAdapter;
import fr.cnrs.iees.graph.impl.*;
import fr.cnrs.iees.omhtk.utils.Duple;
import fr.cnrs.iees.omhtk.utils.Interval;
import fr.cnrs.iees.twcore.constants.*;
import fr.cnrs.iees.twmodels.LibraryTable;
import fr.cnrs.iees.uit.space.Box;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;
import static au.edu.anu.qgraph.queries.CoreQueries.*;
import static au.edu.anu.qgraph.queries.base.SequenceQuery.get;

/**
 * Javafx implementation of {@link IMMController}. It also implements
 * {@link ErrorListListener} to display verification and model compile error
 * messages and {IGraphStateListener}.to update controls depending on the state
 * of the configuration graph.
 * 
 * @author Ian Davies - 23 Sep. 2022
 *
 */
public class MMControllerfx implements ErrorListListener, MMController, GraphStateListener {
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
	private Spinner<Integer> spinJitter;

	@FXML
	private Spinner<Integer> spinPathLength;

	@FXML
	private Label lblChecking;

	@FXML
	private RadioButton rbl1;

	@FXML
	private RadioButton rbl2;

	@FXML
	private RadioButton rbl3;

	@FXML
	private RadioButton rbl4;

	@FXML
	private TextField txfLayoutRoot;

	@FXML
	private ComboBox<ElementDisplayText> cbNodeTextChoice;

	@FXML
	private ComboBox<ElementDisplayText> cbEdgeTextChoice;

	@FXML
	private CheckBox cbAnimate;

	private enum Verbosity {
		brief, medium, full;
	}

	@FXML
	private Slider sldrElements;

	private RadioButton[] rbLayouts;

	private MMModel model;
	private GraphVisualiser visualiser;

	private Stage stage;
	private ToggleGroup tgArchetype;
	private ToggleGroup tgLayout;
	private Verbosity verbosity = Verbosity.brief;

	private List<ErrorMessagable> lstErrorMsgs = new ArrayList<>();

	private StringProperty userProjectPath = new SimpleStringProperty("");

	private DoubleProperty nodeRadiusProperty = new SimpleDoubleProperty(0);
	private DoubleProperty lineWidthProperty = new SimpleDoubleProperty(0);

	private IntegerProperty jitterProperty = new SimpleIntegerProperty(1);
	private ObjectProperty<Font> fontProperty;

	private LayoutType currentLayout;

	private TreeGraph<LayoutNode, LayoutEdge> visualGraph;
	private Font font;
	private static final double fontSize = 9.5;
	private static final double nodeRadius = 7.0;
	private static final double lineWidth = 1.0;

	// TODO: make menu options and preferences entry for this choice when netbeans
	// and IntelliJ have been tested
	private IDETypes ideType = IDETypes.eclipse;

	HostServices hostServices;

	/*******************************************************************************
	 * NB any function that causes checking to take place (e.g. an edit) also causes
	 * SetButtonState() to be called.
	 *******************************************************************************/

	// --------------------------- FXML Start ----------------------
	@FXML
	public void initialize() {
		/** This class has all the housework for managing graph */
		model = new MMModelImpl(this);

		cbNodeTextChoice.getItems().addAll(ElementDisplayText.values());
		cbNodeTextChoice.getSelectionModel().select(ElementDisplayText.RoleName);
		cbEdgeTextChoice.getItems().addAll(ElementDisplayText.values());
		cbEdgeTextChoice.getSelectionModel().select(ElementDisplayText.RoleName);
		spinJitter.setMaxWidth(75.0);
		spinPathLength.setMaxWidth(75.0);

		font = Font.font("Verdana", 10);
		fontProperty = new SimpleObjectProperty<Font>(font);

		spinJitter.valueProperty().addListener(new ChangeListener<Integer>() {

			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer aoldValue, Integer newValue) {
				setJitter(newValue);

			}

		});

		// Tooltips
		btnLayout.setTooltip(getFastToolTip("Re-apply layout [Alt+L]"));
		btnXLinks.setTooltip(getFastToolTip("Toggle cross-links"));
		btnChildLinks.setTooltip(getFastToolTip("Toggle parent-child edges"));
		tglSideline.setTooltip(getFastToolTip("Move isolated nodes aside"));
		cbEdgeTextChoice.setTooltip(getFastToolTip("Edge text display options"));
		cbNodeTextChoice.setTooltip(getFastToolTip("Node text display options"));
		sldrElements.setTooltip(getFastToolTip("Adjust the size of nodes and text"));
		spinJitter.setTooltip(getFastToolTip("Add a random offset to node positions"));
		spinPathLength.setTooltip(getFastToolTip("Set the path length when showing nearby nodes"));
		rbl1.setTooltip(getFastToolTip("Use OrderedTree layout"));
		rbl2.setTooltip(getFastToolTip("Use RadialTree1 layout"));
		rbl3.setTooltip(getFastToolTip("Use RadialTree2 layout"));
		rbl4.setTooltip(getFastToolTip("Use SpringGraph layout"));
		txfLayoutRoot.setTooltip(getFastToolTip("Current selected display root for tree layouts"));
		btnCheck.setTooltip(getFastToolTip("Verify model specifications and compile [Alt+V]"));
		btnDeploy.setTooltip(getFastToolTip("Run the simulation experiment [Alt+D]"));
		rb1.setTooltip(getFastToolTip("Display requried actions only"));
		rb2.setTooltip(getFastToolTip("Display actions and specification constraints"));
		rb3.setTooltip(getFastToolTip("Display all message info"));
		// don't display these. Tips get in the way!
//		allElementsPropertySheet.setTooltip(getFastToolTip("All editable properties for currently displayed nodes and edges"));
//		nodePropertySheet.setTooltip(getFastToolTip("All properties for currently selected node and its out-edges"));
		/** Set a handler to refresh the Open menu items when selected */
		menuOpen.addEventHandler(Menu.ON_SHOWING, event -> updateOpenProjectsMenu(menuOpen));

		/** add template entries to the "New" menu */
		buildNewMenu();

		// build a toggle group for the verbosity level of archetype error
		// messages
		tgArchetype = new ToggleGroup();
		rb1.setToggleGroup(tgArchetype);
		rb2.setToggleGroup(tgArchetype);
		rb3.setToggleGroup(tgArchetype);
		tgArchetype.selectedToggleProperty().addListener(t -> {
			verbosityChange(t);
		});
		rbLayouts = new RadioButton[4];
		rbLayouts[0] = rbl1;
		rbLayouts[1] = rbl2;
		rbLayouts[2] = rbl3;
		rbLayouts[3] = rbl4;
		tgLayout = new ToggleGroup();
		rbl1.setToggleGroup(tgLayout);
		rbl2.setToggleGroup(tgLayout);
		rbl3.setToggleGroup(tgLayout);
		rbl4.setToggleGroup(tgLayout);
		tgLayout.selectedToggleProperty().addListener(t -> {
			RadioButton rb = (RadioButton) tgLayout.getSelectedToggle();
			if (rb == rbl1)
				currentLayout = LayoutType.OrderedTree;
			else if (rb == rbl2)
				currentLayout = LayoutType.RadialTree1;
			else if (rb == rbl3)
				currentLayout = LayoutType.RadialTree2;
			else if (rb == rbl4)
				currentLayout = LayoutType.SpringGraph;
		});

		// Listen for error msgs from error system
		ErrorMessageManager.addListener(this);

		// Setup zooming from the graph display pane (zoomTarget)
		// zoomTarget.setOnScroll(event -> UiHelpers.zoom(zoomTarget, event));
		CenteredZooming.center(scrollPane, scrollContent, group, zoomTarget);

		// are prefs saved regardless of graphState??
		sldrElements.setOnMouseReleased((e) -> {
			setElementScales(sldrElements.getValue());
		});
//		sldrElements.valueProperty().addListener((observableValue, oldValue, newValue) -> {
//			if (oldValue != newValue)
//				Platform.runLater(() -> {
//					// not sure this makes anything better
//					setElementScales(newValue.doubleValue());
//				});
//		});

	}

	private void setElementScales(double zoom) {
		nodeRadiusProperty.set(nodeRadius * zoom);
		lineWidthProperty.set(lineWidth * zoom);
		fontProperty.set(Font.font("Verdana", fontSize * zoom));
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
		ConfigGraph.verifyGraph();
		DialogsFactory.infoAlert("Project disconnected", header, "");
	}

	@FXML
	void handleSetCodePath(ActionEvent event) {
		File jprjFile = DialogsFactory.selectDirectory("Select java project", userProjectPath.get());
		if (jprjFile != null) {
			String tmp = jprjFile.getAbsolutePath().replace("\\", "/");
			if (!tmp.equals(userProjectPath.get())) {
				UserProjectLink.unlinkUserProject();
				if (UserProjectLinkFactory.makeEnv(jprjFile, ideType)) {
					userProjectPath.set(UserProjectLink.projectRoot().getAbsolutePath());
					ConfigGraph.verifyGraph();
					String header = "'" + Project.getDisplayName() + "' is now connected to Java project '"
							+ jprjFile.getName() + "'.";
					String content = "Make sure '" + Project.TW_DEP_JAR + "' is in the build path of '"
							+ jprjFile.getName() + "' and refresh/clean '" + jprjFile.getName() + "' from the IDE.";
					DialogsFactory.infoAlert("Project connected", header, content);
				}
			}
		}
	}

	@FXML
	void handleCheck(ActionEvent event) {
		ConfigGraph.verifyGraph();
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

		String desc = currentLayout.name() + " layout";
		model.addState(desc);
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
		for (LayoutNode rootNodes : visualGraph.roots())
			if (rootNodes.isRoot())
				visualiser.onNodeRenamed(rootNodes);
	}

	@FXML
	void handleOnDeploy(ActionEvent event) {
		model.doDeploy();
	}

	@FXML
	void handlePaneOnMouseClicked(MouseEvent e) {
		if (!e.isControlDown())
			if (newNode != null) {
				newNode.setPosition(e.getX() / zoomTarget.getWidth(), e.getY() / zoomTarget.getHeight());
				visualiser.onNewNode(newNode);
				zoomTarget.setCursor(Cursor.DEFAULT);
				String desc = "New node [" + newNode.configNode().toShortString() + "]";
				lastSelectedNode = newNode;
				newNode = null;
				initialisePropertySheets();
				GraphStateFactory.setChanged();
				ConfigGraph.verifyGraph();

				model.addState(desc);
			}
	}

	@FXML
	void doRedo(ActionEvent event) {
		// try and recover from a deleted project directory
		MMMemento m = (MMMemento) Caretaker.succ();
		if (mementoFilesExist(m)) {
			model.restore(m);
			GraphStateFactory.setChanged();
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
			GraphStateFactory.setChanged();
		} else {
			Caretaker.initialise();
			model.doSave();
		}
		setUndoRedoBtns();
	}

	@FXML
	void onSelectAll(ActionEvent event) {
//		visualiser.onSelectAll();
	}

	@FXML
	void onAbout(ActionEvent event) {
		Dialog<ButtonType> dlg = new Dialog<>();
		dlg.initOwner((Window) DialogsFactory.owner());
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
		List<String> lines;
		if (Language.French())
			lines = Resources.getTextResource("aboutMMFR.txt", getClass());
		else
			lines = Resources.getTextResource("aboutMMEN.txt", getClass());

		for (String line : lines) {
			textArea.appendText(line);
			textArea.appendText("\n");
		}

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

	@FXML
	void onImportSnippets(ActionEvent event) {
		List<String> errorList = new ArrayList<>();
		Map<String, TreeGraphDataNode> snippetNodes = new HashMap<>();
		// Extract code for the main java class of each system as Map<function name,
		// code lines>.
		// This includes imports using the root.id() as the key (NB lower case first
		// letter to make id() consistent with method names)
		String rootId = ConfigGraph.getGraph().root().id();
		TreeGraphDataNode sys = (TreeGraphDataNode) get(ConfigGraph.getGraph().root().getChildren(),
				selectZeroOrOne(hasTheLabel(N_SYSTEM.label())));
		File remoteMainModelClass = new File(UserProjectLink.srcRoot().getAbsoluteFile() + File.separator + Project.CODE
				+ File.separator + sys.id() + File.separator + rootId + ".java");

		Map<String, List<String>> snippetCodes = UserProjectLink.getSnippets(remoteMainModelClass);
		for (TreeGraphDataNode n : ConfigGraph.getGraph().nodes())
			if (n.classId().equals(N_FUNCTION.label()) || n.classId().equals(N_INITFUNCTION.label())
					|| n.classId().equals(N_ROOT.label())) {
				char c[] = n.id().toCharArray();
				c[0] = Character.toLowerCase(c[0]);
				snippetNodes.put(new String(c), n);
			}

		Map<String, String> successfulImports = new HashMap<>();
		boolean changed = false;
		if (!snippetNodes.isEmpty()) {
			for (Map.Entry<String, List<String>> method : snippetCodes.entrySet()) {
				TreeGraphDataNode snippetNode = snippetNodes.get(method.getKey());
				if (snippetNode != null) {// may have no lines
					List<String> lines = method.getValue();
					StringTable newValue = new StringTable(new Dimensioner(lines.size()));
					for (int i = 0; i < lines.size(); i++)
						newValue.setByInt(lines.get(i), i);
					String key;
					if (snippetNode.properties().hasProperty(P_MODEL_IMPORTSNIPPET.key()))
						key = P_MODEL_IMPORTSNIPPET.key();
					else
						key = P_FUNCTIONSNIPPET.key();
					StringTable currentValue = (StringTable) snippetNode.properties().getPropertyValue(key);
					if (!newValue.equals(currentValue)) {
						snippetNode.properties().setProperty(key, newValue);
						successfulImports.put(snippetNode.id(), "Snippet");
						changed = true;
					}
				}
			}
		}

		if (changed) {
			model.addState(miImportSnippets.getText());
			GraphStateFactory.setChanged();
			ConfigGraph.verifyGraph();
		}
		String title = "IDE Import";
		String header;
		header = "Import snippets: " + UserProjectLink.projectRoot().getName() + " --> "
				+ ConfigGraph.getGraph().root().id();
		String content = "";
		for (Map.Entry<String, String> entry : successfulImports.entrySet()) {
			content += entry.getValue() + ": " + entry.getKey() + "\n";
		}

		for (String error : errorList)
			content += error + "\n";

		// Best if we have a list of paired and unpaired code-snippet node
		if (content.isBlank())
			content = "No changes found.";
		DialogsFactory.infoAlert(title, header, content);
	}

	@FXML
	void doClearSnippets(ActionEvent event) {
		boolean changed = false;
		// Enable Undo for this
		for (TreeGraphDataNode n : ConfigGraph.getGraph().nodes())
			if (n.classId().equals(N_FUNCTION.label()) || n.classId().equals(N_INITFUNCTION.label())) {
				TwFunctionTypes ft = (TwFunctionTypes) n.properties().getPropertyValue(P_FUNCTIONTYPE.key());
				StringTable defValue = new StringTable(new Dimensioner(1));
				defValue.setByInt("", 0);
				if (!ft.returnStatement().isBlank()) {
					defValue.setByInt("\t" + ft.returnStatement() + ";", 0);
				}
				StringTable currentValue = (StringTable) n.properties().getPropertyValue(P_FUNCTIONSNIPPET.key());
				if (!defValue.equals(currentValue)) {
					n.properties().setProperty(P_FUNCTIONSNIPPET.key(), defValue);
					changed = true;
				}

			} else if (n.classId().equals(N_ROOT.label())) {
				StringTable defValue = new StringTable(new Dimensioner(1));
				defValue.setByInt("", 0);
				StringTable currentValue = (StringTable) n.properties().getPropertyValue(P_MODEL_IMPORTSNIPPET.key());
				if (!defValue.equals(currentValue)) {
					n.properties().setProperty(P_MODEL_IMPORTSNIPPET.key(), defValue);
					changed = true;
				}
			}

		if (changed) {
			initialisePropertySheets();
			GraphStateFactory.setChanged();
			model.addState(miClearSnippets.getText());
		}
	}

	@FXML
	void onPaneKeyReleased(KeyEvent event) {
//		System.out.println("KEY RELEASED: " + event.isShiftDown());

	}

	@FXML
	void onReference(ActionEvent event) {
		hostServices.showDocument("https://3worlds.github.io/tw-uifx/tw-uifx/doc/reference/html/reference.html");

	}

	@FXML
	void onTutorials(ActionEvent event) {
		hostServices.showDocument(
				"https://3worlds.github.io/tw-uifx/tw-uifx/doc/reference/html/reference.html#truesample-models-and-tutorials");
	}

	@FXML
	void onGitHub(ActionEvent event) {
		hostServices.showDocument("https://github.com/3worlds/3w");

	}

	// ---------------FXML End -------------------------

	// ---------------IMMController Start ---------------------
	/**
	 * Maintain a list of host services, used for opening web pages for 3Worlds
	 * gitHub.
	 * 
	 * @param hs Currently available host services.
	 */
	public void setHostServices(HostServices hs) {
		hostServices = hs;
	}

	@Override
	public void onProjectClosing() {
		GraphStateFactory.clear();
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
	public void onProjectOpened(TreeGraph<LayoutNode, LayoutEdge> layoutGraph) {
		this.visualGraph = layoutGraph;
		Cursor oldCursor = setWaitCursor();
		getPreferences();
		visualiser = new GraphVisualiserfx(visualGraph, //
				zoomTarget, //
				cbAnimate.selectedProperty(), //
				nodeRadiusProperty, //
				lineWidthProperty, //
				btnChildLinks.selectedProperty(), //
				btnXLinks.selectedProperty(), //
				cbNodeTextChoice.getSelectionModel().selectedItemProperty(), //
				cbEdgeTextChoice.getSelectionModel().selectedItemProperty(), //
				tglSideline.selectedProperty(), //
				spinPathLength.valueProperty(), //
				fontProperty, this, model);

		final double duration = GraphVisualiserfx.animateFast;
		visualiser.initialiseView(duration);

		initialisePropertySheets();

		setCursor(oldCursor);
		stage.setTitle(Project.getDisplayName());
	}

	private LayoutNode newNode;

	@Override
	public void onNewNode(LayoutNode node) {
		zoomTarget.setCursor(Cursor.CROSSHAIR);
		newNode = node;
	}

	@Override
	public void onNewEdge(LayoutEdge e) {
		initialisePropertySheets();
	}

	@Override
	public void onEdgeDeleted() {
		initialisePropertySheets();
	}

	@Override
	public void onRootNameChange() {
		setLayoutRoot(null);
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
		callLayout(layoutRoot, currentLayout, duration);
	}

	@Override
	public void doFocusedLayout(LayoutNode root, LayoutType layout, double duration) {
		callLayout(root, layout, duration);
		currentLayout = layout;
	}

	// used only by Visualiser?? Passed by a node onClicked left button
	@Override
	public void onNodeSelected(LayoutNode node) {
		lastSelectedNode = node;
		Duple<ObservableList<Item>, ObservableList<Item>> items = getObsItems();
		fillSelPropertySheet(items.getSecond());

		/**
		 * Kludge to refresh the property editors when a graph node is clicked for the
		 * first time!
		 */
		Platform.runLater(() -> {
			{
				double[] d = splitPane1.getDividerPositions();
				for (int i = 0; i < d.length; i++)
					d[i] += 0.01;
				splitPane1.setDividerPositions(d);
			}
		});

		Platform.runLater(() -> {
			double[] d = splitPane1.getDividerPositions();
			for (int i = 0; i < d.length; i++)
				d[i] -= 0.01;
			splitPane1.setDividerPositions(d);
		});
	}

	@Override
	public void onElementRenamed() {
		initialisePropertySheets();
	}

	@Override
	public void onItemEdit(Object changedItem) {
		// this relies of items begin different instances of the same data
		Item item = (Item) changedItem;
		Duple<ObservableList<Item>, ObservableList<Item>> items = getObsItems();
		if (nodePropertySheet.getItems().contains(item))
			fillAllPropertySheet(items.getFirst());
		else if (allElementsPropertySheet.getItems().contains(item)) {
			fillSelPropertySheet(items.getSecond());

		}
	}

	@Override
	public void setDefaultTitle() {
		stage.setTitle(DefaultWindowSettings.defaultName());

	}

//	@Override
//	public LayoutType getCurrentLayout() {
//		return currentLayout;
//	}

	@Override
	public void onRollback(TreeGraph<LayoutNode, LayoutEdge> layoutGraph) {
		// cbNodeTextChoice.getSelectionModel().selectedItemProperty()
		// cbEdgeTextChoice.getSelectionModel().selectedItemProperty()
		visualGraph = layoutGraph;
		visualiser.onRollback(layoutGraph);
		lastSelectedNode = null;
		initialisePropertySheets();
	}

	@Override
	public Collection<String> getUnEditablePropertyKeys(String label) {
		return model.unEditablePropertyKeys(label);
	}

	@Override
	public void onAddRemoveProperty(LayoutNode vn) {
		Duple<ObservableList<Item>, ObservableList<Item>> items = getObsItems();
		fillAllPropertySheet(items.getFirst());
		if (lastSelectedNode != null)
			if (lastSelectedNode.configNode().toShortString().equals(vn.configNode().toShortString()))
				fillSelPropertySheet(items.getSecond());
	}

	private LayoutNode layoutRoot;

	@Override
	public LayoutNode setLayoutRoot(LayoutNode newRoot) {
		LayoutNode oldRoot = layoutRoot;
		layoutRoot = newRoot;
		if (layoutRoot == null) {
			for (LayoutNode root : visualGraph.roots())
				if (root.isRoot())
					layoutRoot = root;
		}
		if (layoutRoot != null)
			txfLayoutRoot.setText(layoutRoot.configNode().toShortString());
		else {
			txfLayoutRoot.setText("");
		}
		return oldRoot;
	}

	@Override
	public LayoutNode getLayoutRoot() {
		return layoutRoot;
	}

	@Override
	public MMModel model() {
		return model;
	}

	@Override
	public GraphVisualiser visualiser() {
		return visualiser;
	}

	// -------------- IMMController End ---------------------

	// -------------- Preferencable Start ---------------------
	private static final String mainFrameName = "mainFrame";
	private static final String mainMaximized = mainFrameName + "_" + "maximized";
	private static final String scaleX = "_scaleX";
	private static final String scaleY = "_scaleY";
	private static final String UserProjectPath = "userProjectPath";
	private static final String jitterKey = "jitter";
	private static final String Mode = "_mode";
	private static final String AccordionSelection = "_AccSel";
	private static final String CurrentLayoutKey = "currentLayout";
	private static final String ElementScalesKey = "elementScales";

	private static final String NodeTextDisplayChoice = "nodeTextDisplayChoice";
	private static final String EdgeTextDisplayChoice = "edgeTextDisplayChoice";
	private static final String ScrollHValue = "HValue";
	private static final String ScrollVValue = "VValue";
	private static final String KeyPathLength = "PathLength";

	@Override
	public void putPreferences() {
		if (Project.isOpen()) {
			IPreferences prefs = Preferences.getImplementation();

			prefs.putString(UserProjectPath, userProjectPath.get());
			prefs.putEnum(allElementsPropertySheet.idProperty().get() + Mode, allElementsPropertySheet.getMode());
			prefs.putEnum(nodePropertySheet.idProperty().get() + Mode, nodePropertySheet.getMode());
			prefs.putDouble(splitPane1.idProperty().get(), splitPane1.getDividerPositions()[0]);
			prefs.putDouble(splitPane2.idProperty().get(), splitPane2.getDividerPositions()[0]);
			prefs.putDouble(zoomTarget.idProperty().get() + scaleX, zoomTarget.getScaleX());
			prefs.putDouble(zoomTarget.idProperty().get() + scaleY, zoomTarget.getScaleY());
			prefs.putDoubles(mainFrameName, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
			prefs.putBoolean(mainMaximized, stage.isMaximized());
			prefs.putBoolean(btnXLinks.idProperty().get(), btnXLinks.isSelected());
			prefs.putBoolean(btnChildLinks.idProperty().get(), btnChildLinks.isSelected());
			prefs.putBoolean(tglSideline.idProperty().get(), tglSideline.isSelected());
			prefs.putInt(jitterKey, jitterProperty.get());
			prefs.putInt(tabPaneProperties.idProperty().get(),
					tabPaneProperties.getSelectionModel().getSelectedIndex());
			prefs.putInt(AccordionSelection, UiHelpers.getExpandedPaneIndex(allElementsPropertySheet));

			prefs.putEnum(CurrentLayoutKey, currentLayout);
			prefs.putEnum(NodeTextDisplayChoice, cbNodeTextChoice.getSelectionModel().getSelectedItem());
			prefs.putEnum(EdgeTextDisplayChoice, cbEdgeTextChoice.getSelectionModel().getSelectedItem());
			prefs.putDouble(scrollPane.idProperty().get() + ScrollHValue, scrollPane.getHvalue());
			prefs.putDouble(scrollPane.idProperty().get() + ScrollVValue, scrollPane.getVvalue());

			prefs.putInt(KeyPathLength, spinPathLength.getValue());

			prefs.putDouble(ElementScalesKey, sldrElements.getValue());

			prefs.putBoolean(cbAnimate.idProperty().get(), cbAnimate.isSelected());

			prefs.flush();
		}
	}

	@Override
	public void getPreferences() {
		Preferences.setImplementation(new PrefImpl(Project.makeProjectPreferencesFile()));
		IPreferences prefs = Preferences.getImplementation();
		// get path string for user project
		String prjtmp = prefs.getString(UserProjectPath, "");
		if (!prjtmp.equals("")) {
			// check java project still exists.
			UserProjectLink.unlinkUserProject();
			if (UserProjectLinkFactory.makeEnv(new File(prjtmp), ideType)) {
				userProjectPath.set(UserProjectLink.projectRoot().getAbsolutePath());
			} else
				userProjectPath.set("");
		} else
			userProjectPath.set("");

		double[] ws = prefs.getDoubles(mainFrameName, DefaultWindowSettings.getX(), DefaultWindowSettings.getY(),
				DefaultWindowSettings.getWidth(), DefaultWindowSettings.getHeight());
		stage.setX(ws[0]);
		stage.setY(ws[1]);
		stage.setWidth(ws[2]);
		stage.setHeight(ws[3]);
		stage.setMaximized(prefs.getBoolean(mainMaximized, stage.isMaximized()));

		setJitter(prefs.getInt(jitterKey, 0));
		tabPaneProperties.getSelectionModel()
				.select(Math.max(0, prefs.getInt(tabPaneProperties.idProperty().get(), 0)));

		currentLayout = (LayoutType) prefs.getEnum(CurrentLayoutKey, LayoutType.OrderedTree);
		rbLayouts[currentLayout.ordinal()].setSelected(true);

		cbNodeTextChoice.getSelectionModel()
				.select((ElementDisplayText) prefs.getEnum(NodeTextDisplayChoice, ElementDisplayText.RoleName));
		cbEdgeTextChoice.getSelectionModel()
				.select((ElementDisplayText) prefs.getEnum(EdgeTextDisplayChoice, ElementDisplayText.RoleName));

		PropertySheet.Mode mode = (PropertySheet.Mode) prefs.getEnum(allElementsPropertySheet.idProperty().get() + Mode,
				PropertySheet.Mode.CATEGORY);
		allElementsPropertySheet.setMode(mode);
		mode = (PropertySheet.Mode) prefs.getEnum(nodePropertySheet.idProperty().get() + Mode, PropertySheet.Mode.NAME);
		nodePropertySheet.setMode(mode);
		int idx = prefs.getInt(AccordionSelection, -1);
		UiHelpers.setExpandedPane(allElementsPropertySheet, idx);

		btnXLinks.selectedProperty().set(prefs.getBoolean(btnXLinks.idProperty().get(), true));
		btnChildLinks.selectedProperty().set(prefs.getBoolean(btnChildLinks.idProperty().get(), true));
		tglSideline.selectedProperty().set(prefs.getBoolean(tglSideline.idProperty().get(), false));

		zoomTarget.setScaleX(prefs.getDouble(zoomTarget.idProperty().get() + scaleX, 1));
		zoomTarget.setScaleY(prefs.getDouble(zoomTarget.idProperty().get() + scaleY, 1));
		double s1 = prefs.getDouble(splitPane1.getId(), DefaultWindowSettings.splitter1());
		splitPane1.setDividerPositions(UiHelpers.getSplitPanePositions(s1, splitPane1.getId()));
		// get splitPanes later after UI has settled down
		splitPane1.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {
					double s1 = prefs.getDouble(splitPane1.getId(), DefaultWindowSettings.splitter1());
					double s2 = prefs.getDouble(splitPane2.getId(), DefaultWindowSettings.splitter2());
					splitPane1.setDividerPositions(UiHelpers.getSplitPanePositions(s1, splitPane1.getId()));
					splitPane2.setDividerPositions(UiHelpers.getSplitPanePositions(s2, splitPane2.getId()));
					observable.removeListener(this);
					scrollPane.setHvalue(prefs.getDouble(scrollPane.idProperty().get() + ScrollHValue, 0));
					scrollPane.setVvalue(prefs.getDouble(scrollPane.idProperty().get() + ScrollVValue, 0));

				});
			}
		});

		int pl = prefs.getInt(KeyPathLength, 1);
		spinPathLength.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, pl));

		setLayoutRoot(null);

		sldrElements.setDisable(false);
		sldrElements.setValue(prefs.getDouble(ElementScalesKey, 1.0));

		cbAnimate.selectedProperty().set(prefs.getBoolean(cbAnimate.idProperty().get(), true));

		this.setElementScales(sldrElements.getValue());
	}

	// -------------- Preferencable End ---------------------

	// -------------- ModelMakerfx Start---------------------
	/**
	 * Getter for the {@link IMMModel#canClose()}.
	 * 
	 * @return true if project can close; false otherwise.
	 */
	public boolean canClose() {
		return model.canClose();
	}

	/**
	 * Getter for the ModelMaker main Stage.
	 * 
	 * @param stage The main stage.
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Getter for the user project path string property.
	 * 
	 * @return The string property.
	 */
	public StringProperty getUserProjectPathProperty() {
		return userProjectPath;
	}

	// -------------- ModelMakerfx End ---------------------

	// ----------------- ErrorListListener Start ------------
	@Override
	public void onStartCheck() {
		Platform.runLater(() -> {
			btnDeploy.setDisable(true);
			btnCheck.setDisable(true);
			lblChecking.setVisible(true);
			trafficLight.fillProperty().set(Color.RED);
			miImportSnippets.setDisable(true);
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
		if (Language.French())
			muTutorials.setText("Tutoriels");
		Menu muModels = new Menu("Model Library");
		Menu muTests = new Menu("Test cases");
		menuNew.getItems().addAll(muTemplates, muTutorials, muModels, muTests);
		for (LibraryTable entry : model.getLibrary()) {
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
			case Model: {
				muModels.getItems().add(mi);
				break;
			}
			default: {
				muTests.getItems().add(mi);
			}
			}

			mi.setOnAction((e) -> {

				LibraryTable lt = map.get(e.getSource());
				TreeGraph<TreeGraphDataNode, ALEdge> libGraph = lt.getGraph();
				TreeGraphDataNode root = libGraph.root();
				String proposedName = lt.proposedName();
				if (root == null || !root.classId().equals(N_ROOT.label())) {
					String rnames = lt.displayName() + " roots:\n";
					int i = 0;
					for (TreeGraphDataNode r : libGraph.roots())
						rnames += ((++i) + ") " + r.toShortString() + "\n");
					String title = "Library graph error";
					String content = "Graphs must have a single root (ref '" + N_ROOT.label() + "').";
					DialogsFactory.errorAlert(title, content, rnames);
					return;
				}

				if (root.properties().hasProperty(P_MODEL_BUILTBY.key())) {
					DateTimeFormatter fm = DateTimeFormatter.ofPattern("d MMM uuuu");
					LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
					String date = " (" + currentDate.format(fm) + ")";
					root.properties().setProperty(P_MODEL_BUILTBY.key(), System.getProperty("user.name") + date);
				}

				model.doNewProject(proposedName, libGraph, lt.dependencyArchive());
			});
		}

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
		lstErrorMsgs.sort((m1, m2) -> m1.actionInfo().compareToIgnoreCase(m2.actionInfo()));
		int count = 0;
		for (ErrorMessagable msg : lstErrorMsgs) {
			count++;
			textAreaErrorMsgs.appendText(count + ". " + getMessageText(msg));
		}

	}

	private void callLayout(LayoutNode root, LayoutType layout, double duration) {
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

		String t = "";
		switch (verbosity) {
		case brief: {
			t = msg.actionInfo() + "\n\n";
			break;
		}
		case medium: {
			t = msg.detailsInfo() + "\n\n";
			break;
		}
		default: {
			t = msg.debugInfo() + "\n\n";
		}
		}

		return t;
	}

	private List<LayoutNode> getNodeList() {
		List<LayoutNode> result = new LinkedList<>();
		for (LayoutNode n : visualGraph.nodes())
			result.add(n);
		return result;
	}

	private Duple<ObservableList<Item>, ObservableList<Item>> getObsItems() {
		ObservableList<Item> allItems = FXCollections.observableArrayList();
		ObservableList<Item> selItems = FXCollections.observableArrayList();
		Duple<ObservableList<Item>, ObservableList<Item>> result = new Duple<>(allItems, selItems);
		List<LayoutNode> vNodes = getNodeList();
		vNodes.sort((n1, n2) -> n1.id().compareTo(n2.id()));
//		vNodes.sort((first, second) -> {
//			return first.id().compareTo(second.id());
//		});
		for (LayoutNode vn : vNodes) {
			if (!vn.isCollapsed()) {
				String cat = vn.getCategory();
				if (cat == null)
					cat = vn.id();
				TreeGraphNode cn = vn.configNode();
				// TODO !!! EDGES
				if (cn instanceof DataHolder) {
					TreeGraphDataNode node = (TreeGraphDataNode) cn;
					boolean selectedNode = false;
					if (lastSelectedNode != null && lastSelectedNode == vn)
						selectedNode = true;
					for (String key : node.properties().getKeysAsSet()) {
						if (node.properties().getPropertyValue(key) != null) {
							boolean editable = model.propertyEditable(node.classId(), key);
							if (vn.isPredefined())
								editable = false;

							if (selectedNode) {
								Item item = makeItemType(key, node, editable, cat, "Something");
								selItems.add(item);// must be different instances
							}
							if (editable) {
								Item item = makeItemType(key, node, editable, cat, "Something");
								allItems.add(item);// must be different instances
							}
						}
					}
					for (ALEdge edge : node.edges(Direction.OUT))
						if (edge instanceof ALDataEdge) {
							ALDataEdge dataEdge = (ALDataEdge) edge;
							for (String key : dataEdge.properties().getKeysAsSet()) {
								boolean editable = model.propertyEditable(edge.classId(), key);
								editable = editable || vn.isPredefined();//
								if (selectedNode) {
									Item item = (makeItemType(key, dataEdge, editable, cat, "Something"));
									selItems.add(item);
								}
								if (editable) {
									Item item = (makeItemType(key, dataEdge, editable, cat, "Something"));
									allItems.add(item);
								}
							}
						}
				}

			}
		}

		allItems.sort((first, second) -> first.getName().compareTo(second.getName()));
//		allItems.sort((first, second) -> {
//			return first.getName().compareTo(second.getName());
//		});

		selItems.sort((first, second) -> first.getName().compareTo(second.getName()));
//		selItems.sort((first, second) -> {
//			return first.getName().compareTo(second.getName());
//		});

		return result;
	}

	private void fillAllPropertySheet(ObservableList<Item> items) {
		allElementsPropertySheet.getItems().clear();
		allElementsPropertySheet.getItems().setAll(items);
	}

	private void fillSelPropertySheet(ObservableList<Item> items) {
		nodePropertySheet.getItems().clear();
		nodePropertySheet.getItems().setAll(items);
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
			return new BorderListItem(this, key, (ElementAdapter) element, editable, category, description);
		} else if (value instanceof StringTable) {
			StringTable st = (StringTable) value;
			if (st.getDimensioners().length == 1)
				return new StringTableItem(this, key, (ElementAdapter) element, editable, category, description);
		} else if (value instanceof DoubleTable) {
			DoubleTable dt = (DoubleTable) value;
			if (dt.getDimensioners().length == 1)
				return new DoubleTableItem(this, key, (ElementAdapter) element, editable, category, description);
		} else if (value instanceof IntTable) {
			return new IntTableItem(this, key, (ElementAdapter) element, editable, category, description);

		} else if (value instanceof Box) {
			return new BoxItem(this, key, (ElementAdapter) element, editable, category, description);
		}
		return new SimpleMMPropertyItem(this, key, (ElementAdapter) element, editable, category, description);
	}

	private void initialisePropertySheets() {
		lastSelectedNode = null;
		Duple<ObservableList<Item>, ObservableList<Item>> items = getObsItems();
		fillAllPropertySheet(items.getFirst());
		fillSelPropertySheet(items.getSecond());
//		fillGraphPropertySheet();
//		fillNodePropertySheet(lastSelectedNode);
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

	private LayoutNode lastSelectedNode = null;

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

	private void setButtonState() {
		boolean isOpen = Project.isOpen();
		boolean isClean = !GraphStateFactory.changed() & isOpen;
		boolean isConnected = UserProjectLink.haveUserProject();
		miSetCodePath.setDisable(isConnected);
		miDisconnect.setDisable(!isConnected);
		menuItemSave.setDisable(isClean);
		menuItemSaveAs.setDisable(!isOpen);
		btnChildLinks.setDisable(!isOpen);
		btnXLinks.setDisable(!isOpen);
		cbNodeTextChoice.setDisable(!isOpen);
		cbEdgeTextChoice.setDisable(!isOpen);
//		btnSelectAll.setDisable(!isOpen);
		tglSideline.setDisable(!isOpen);
//		tglNeighbourhood.setDisable(!isOpen);
		btnLayout.setDisable(!isOpen);
		// txfLayoutRoot.setDisable(!isOpen);
		rbl1.setDisable(!isOpen);
		rbl2.setDisable(!isOpen);
		rbl3.setDisable(!isOpen);
		rbl4.setDisable(!isOpen);
		cbAnimate.setDisable(!isOpen);
		sldrElements.setDisable(!isOpen);
		btnCheck.setDisable(!isOpen);
		boolean cleanAndValid = isClean && isValid;
		btnDeploy.setDisable(!cleanAndValid);
//		btnDocument.setDisable(!cleanAndValid);
//		boolean snp = !cleanAndValid || !UserProjectLink.haveUserProject();
		boolean snp = !UserProjectLink.haveUserProject();
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