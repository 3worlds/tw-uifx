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

package au.edu.anu.twuifx.widgets.deprecated;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.SpaceData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.widgets.helpers.SimpleWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import au.edu.anu.ymuit.ui.colour.ColourContrast;
import au.edu.anu.ymuit.ui.colour.PaletteSize;
import au.edu.anu.ymuit.util.CenteredZooming;
import au.edu.anu.ymuit.util.Decimals;
import javafx.geometry.Side;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.BorderListType;
import fr.cnrs.iees.twcore.constants.BorderType;
import fr.cnrs.iees.twcore.constants.EdgeEffectCorrection;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import fr.cnrs.iees.twcore.constants.SpaceType;
import fr.cnrs.iees.uit.space.Distance;
import fr.ens.biologie.generic.utils.Duple;
import fr.ens.biologie.generic.utils.Interval;
import fr.ens.biologie.generic.utils.Tuple;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

/**
 * @author Ian Davies
 *
 * @date 12 Feb 2020
 *
 *       Widget to show spatial map of objects and their relations.
 *
 */
@Deprecated // replaced by SpaceWidget1
public class SimpleSpatial2DWidget1 extends AbstractDisplayWidget<SpaceData, Metadata> implements WidgetGUI {
	private static final double labelFontSize = 9.5;
	private static final double axisFontSize = 13.0;
	private static final double nodeRadius = 7.0;
	private static final double lineWidth = 1.0;
	private static final double paperSize = 500.0;
	private BorderPane zoomTarget;
	private Canvas canvas;
	private ScrollPane scrollPane;
	private Label lblItem;
	private Label lblTime;
	private Bounds spaceBounds;
	private String widgetId;
	private final WidgetTrackingPolicy<TimeData> policy;
	private final WidgetTimeFormatter timeFormatter;
	/**
	 * Hierarchically organised locations. Colours can be applied to any level in
	 * the data label hierarchy from species to stages to individuals.
	 */
	private final Map<String, Duple<DataLabel, double[]>> mpPoints;
	/** Lines are non-hierarchical. Just use the datalabel string as a lookup */
	private final Set<Tuple<DataLabel, DataLabel, String>> stLines;
	private final Map<String, Color> lineTypeColours;

	private final Map<String, Duple<Integer, Color>> mpColours;
	/** Temporary store for initialising data */
	private final List<SpaceData> lstInitialData;

	private FlowPane legend;
	private Slider sldrElements;
	private Slider sldrResolution;
	private EdgeEffectCorrection eec;
	private double tickSize;
	private int nXAxisTicks;
	private int nYAxisTicks;
	private double offsetX;
	private double offsetY;
	private BorderListType borderList;
	private List<Color> lstColoursAvailable;

	private String units;
	private DecimalFormat pointFormat;
	private DecimalFormat axisFormat;

	private AnchorPane topAxis;
	private AnchorPane bottomAxis;
	private AnchorPane leftAxis;
	private AnchorPane rightAxis;
	private Map<Double, Duple<Label, Label>> xAxes;
	private Map<Double, Duple<Label, Label>> yAxes;

	private double scaledLineWidth;
	private double scaledNodeRadius;
	private Font scaledFont;

	public SimpleSpatial2DWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.SPACE);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		// Thread-safe is no longer necessary
//		mpPoints = new ConcurrentHashMap<>();// Thread-safe is no longer necessary
		mpPoints = new HashMap<>();
		lstColoursAvailable = new ArrayList<>();
//		mpColours = new ConcurrentHashMap<>();// Thread-safe is no longer necessary
		mpColours = new HashMap<>();
		// Thread-safe is no longer necessary
//		stLines = Collections.newSetFromMap(new ConcurrentHashMap<Tuple<DataLabel, DataLabel, String>, Boolean>());
		stLines = new HashSet<>();
		lineTypeColours = new HashMap<>();
		lstInitialData = new ArrayList<>();
		units = "";
		xAxes = new HashMap<>();
		yAxes = new HashMap<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.widgetId = id;
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		if (policy.canProcessMetadataMessage(meta)) {
			timeFormatter.onMetaDataMessage(meta);
			SpaceType type = (SpaceType) meta.properties().getPropertyValue(P_SPACETYPE.key());

			switch (type) {
			case continuousFlatSurface: {
				Interval xLimits = (Interval) meta.properties().getPropertyValue(P_SPACE_XLIM.key());
				Interval yLimits = (Interval) meta.properties().getPropertyValue(P_SPACE_YLIM.key());
				spaceBounds = new BoundingBox(xLimits.inf(), yLimits.inf(), xLimits.sup() - xLimits.inf(),
						yLimits.sup() - yLimits.inf());
				borderList = (BorderListType) meta.properties().getPropertyValue(P_SPACE_BORDERTYPE.key());
				eec = BorderListType.getEdgeEffectCorrection(borderList);

				tickSize = getTickSize();

				offsetX = getTickOffset(spaceBounds.getMinX(), tickSize);
				offsetY = getTickOffset(spaceBounds.getMinY(), tickSize);

				nXAxisTicks = getNTicks(spaceBounds.getMinX(), spaceBounds.getMaxX(), tickSize, offsetX);
				nYAxisTicks = getNTicks(spaceBounds.getMinY(), spaceBounds.getMaxY(), tickSize, offsetY);

				units = (String) meta.properties().getPropertyValue(P_SPACE_UNITS.key());

				// We dont really need it as it can be worked out.
				double prec = (Double) meta.properties().getPropertyValue(P_SPACE_PREC.key());
				if (prec < Double.MIN_VALUE)
					prec = 1;
				int ndp = 0;
				while (prec < 1.0) {
					prec = prec * 10;
					ndp++;
				}
				pointFormat = Decimals.getDecimalFormat(ndp);
				axisFormat = Decimals.getDecimalFormat(ndp - 1);

				return;
			}
			case squareGrid: {
				Double cellSize = (Double) meta.properties().getPropertyValue(P_SPACE_CELLSIZE.key());
				int xnCells = (Integer) meta.properties().getPropertyValue(P_SPACE_NX.key());
				int ynCells = (Integer) meta.properties().getPropertyValue(P_SPACE_NY.key());
				spaceBounds = new BoundingBox(0, 0, cellSize * xnCells, cellSize * ynCells);
				borderList = (BorderListType) meta.properties().getPropertyValue(P_SPACE_BORDERTYPE.key());
				eec = BorderListType.getEdgeEffectCorrection(borderList);
				// TODO: ticksize etc should be rounded to units of cellsize
				tickSize = getTickSize();
				offsetX = getTickOffset(spaceBounds.getMinX(), tickSize);
				offsetY = getTickOffset(spaceBounds.getMinY(), tickSize);

				nXAxisTicks = getNTicks(spaceBounds.getMinX(), spaceBounds.getMaxX(), tickSize, offsetX);
				nYAxisTicks = getNTicks(spaceBounds.getMinY(), spaceBounds.getMaxY(), tickSize, offsetY);

				if (meta.properties().hasProperty(P_SPACE_UNITS.key()))
					units = (String) meta.properties().getPropertyValue(P_SPACE_UNITS.key());
				double prec = (Double) meta.properties().getPropertyValue(P_SPACE_PREC.key());
				if (prec < Double.MIN_VALUE)
					prec = 1;
				int ndp = 0;
				while (prec < 1.0) {
					prec = prec * 10;
					ndp++;
				}
				pointFormat = Decimals.getDecimalFormat(ndp);
				axisFormat = Decimals.getDecimalFormat(ndp - 1);

				return;
			}
			default: {
				// Eventually, the archetype should prevent this situation
				throw new TwuifxException(type + " not supported.");
			}
			}
		}
	}

	private CheckBox chbxLabels;
	private CheckBox chbxLegend;
	private CheckBox chbxGrid;
	private CheckBox chbxBoundaries;
	private CheckBox chbxLines;
	private ObjectProperty<Font> fontProperty;

	@Override
	public Object getUserInterfaceContainer() {

		BorderPane container = new BorderPane();
		Label lbl = new Label(widgetId + " [" + units + "]");
		Font smallFont = new Font("System Regular", 11.0);

		ToolBar tlbr = new ToolBar();
		tlbr.getItems().add(lbl);

		GridPane gp = new GridPane();
		tlbr.getItems().add(new Separator());
		tlbr.getItems().add(gp);
		sldrElements = new Slider();
		sldrElements.setMin(0.1);
		sldrElements.setMax(2.0);
		sldrElements.setValue(1.0);
		sldrElements.setShowTickMarks(true);
		sldrElements.valueProperty().addListener((observableValue, oldValue, newValue) -> {
			setElementScales();
		});
		sldrElements.setScaleX(0.75);
		sldrElements.setScaleY(0.75);
		Label sllb;
		sllb = new Label("Elements");
		sllb.setFont(smallFont);
		gp.add(sllb, 0, 0);
		gp.add(sldrElements, 1, 0);
		tlbr.getItems().add(new Separator());

		sldrResolution = new Slider();
		sldrResolution.setMin(1.0);
		sldrResolution.setMax(5.0);
		sldrResolution.setValue(1.0);
		sldrResolution.setShowTickMarks(true);
		sldrResolution.valueProperty().addListener((observableValue, oldValue, newValue) -> {
			setPaperWidth();
		});
		sldrResolution.setScaleX(0.75);
		sldrResolution.setScaleY(0.75);
		sllb = new Label("Resolution");
		sllb.setFont(smallFont);

		gp.add(sllb, 0, 1);
		gp.add(sldrResolution, 1, 1);

		chbxLabels = new CheckBox("Labels");
		chbxLabels.setFont(smallFont);
		chbxLabels.selectedProperty().addListener((obs, oldValue, newValue) -> {
			drawScene();
		});
		gp.add(chbxLabels, 2, 0);

		chbxLegend = new CheckBox("Legend");
		chbxLegend.setFont(smallFont);
		chbxLegend.selectedProperty().addListener((obs, o, n) -> {
			legend.setVisible(n);
			placeLegend();
		});
		gp.add(chbxLegend, 2, 1);

		chbxGrid = new CheckBox("Grid");
		chbxGrid.setFont(smallFont);
		chbxGrid.selectedProperty().addListener((obx, o, n) -> {
			drawScene();
		});
		gp.add(chbxGrid, 3, 0);

		chbxBoundaries = new CheckBox("Boundaries");
		chbxBoundaries.setFont(smallFont);
		chbxBoundaries.selectedProperty().addListener((obs, o, n) -> {
			drawScene();
		});
		gp.add(chbxBoundaries, 3, 1);

		chbxLines = new CheckBox("Relations");
		chbxLines.setFont(smallFont);
		chbxLines.selectedProperty().addListener((obs, o, n) -> {
			drawScene();
		});
		gp.add(chbxLines, 4, 0);

//		gp.setGridLinesVisible(true);

		container.setTop(tlbr);
		centerContainer = new BorderPane();

		container.setCenter(centerContainer);
		zoomTarget = new BorderPane();
		canvas = new Canvas();
		canvas.setOnMouseClicked(e -> onMouseClicked(e));
		zoomTarget.setCenter(canvas);
		topAxis = new AnchorPane();
		bottomAxis = new AnchorPane();
		leftAxis = new AnchorPane();
		rightAxis = new AnchorPane();

		fontProperty = new SimpleObjectProperty<Font>(getSystemFont(axisFontSize));
		double startX = getStartValue(spaceBounds.getMinX(), offsetX, tickSize);
		for (int i = 0; i < nXAxisTicks; i++) {
			double value = i * tickSize + startX;
			String s = axisFormat.format(value);
			Label t = new Label(s);
			t.fontProperty().bind(fontProperty);
			Label b = new Label(s);
			b.fontProperty().bind(fontProperty);
			topAxis.getChildren().add(t);
			bottomAxis.getChildren().add(b);
			xAxes.put(value, new Duple<Label, Label>(b, t));
		}
		{
			double value = spaceBounds.getMinX();
			String s = pointFormat.format(value);
			Label t = new Label(s);
			t.fontProperty().bind(fontProperty);
			Label b = new Label(s);
			b.fontProperty().bind(fontProperty);
			topAxis.getChildren().add(t);
			bottomAxis.getChildren().add(b);
			xAxes.put(value, new Duple<Label, Label>(b, t));
		}
		{
			double value = spaceBounds.getMaxX();
			String s = pointFormat.format(value);
			Label t = new Label(s);
			t.fontProperty().bind(fontProperty);
			Label b = new Label(s);
			b.fontProperty().bind(fontProperty);
			topAxis.getChildren().add(t);
			bottomAxis.getChildren().add(b);
			xAxes.put(value, new Duple<Label, Label>(b, t));
		}

		double startY = getStartValue(spaceBounds.getMinY(), offsetY, tickSize);
		for (int i = 0; i < nYAxisTicks; i++) {
			double value = i * tickSize + startY;
			String s = axisFormat.format(value);
			Label l = new Label(s);
			l.fontProperty().bind(fontProperty);
			Label r = new Label(s);
			r.fontProperty().bind(fontProperty);
			leftAxis.getChildren().add(l);
			rightAxis.getChildren().add(r);
			yAxes.put(value, new Duple<Label, Label>(l, r));
		}
		{
			double value = spaceBounds.getMinY();
			String s = pointFormat.format(value);
			Label l = new Label(s);
			l.fontProperty().bind(fontProperty);
			Label r = new Label(s);
			leftAxis.getChildren().add(l);
			r.fontProperty().bind(fontProperty);
			rightAxis.getChildren().add(r);
			yAxes.put(value, new Duple<Label, Label>(l, r));
		}
		{
			double value = spaceBounds.getMaxY();
			String s = pointFormat.format(value);
			Label l = new Label(s);
			l.fontProperty().bind(fontProperty);
			Label r = new Label(s);
			r.fontProperty().bind(fontProperty);
			leftAxis.getChildren().add(l);
			rightAxis.getChildren().add(r);
			yAxes.put(value, new Duple<Label, Label>(l, r));
		}

		zoomTarget.setTop(topAxis);
		zoomTarget.setBottom(bottomAxis);
		zoomTarget.setLeft(leftAxis);
		zoomTarget.setRight(rightAxis);

		Group group = new Group(zoomTarget);
		StackPane content = new StackPane(group);
		scrollPane = new ScrollPane(content);
		scrollPane.setPannable(true);
		scrollPane.setContent(content);
		scrollPane.setMinSize(170, 170);
		CenteredZooming.center(scrollPane, content, group, zoomTarget);
		centerContainer.setCenter(scrollPane);

		HBox statusBar = new HBox();
		// statusBar.setAlignment(Pos.CENTER);
		statusBar.setSpacing(5);
		lblItem = new Label("");
		lblTime = new Label("");

		statusBar.getChildren().addAll(new Label("Tracker time"), lblTime, new Label("	"), lblItem);
		container.setBottom(statusBar);

		legend = new FlowPane();
		legend.setHgap(3);
		legend.setVgap(3);

		getUserPreferences();

		setPaperWidth();

		return container;
	}

	private Font getSystemFont(double size) {
		return Font.font("Courier New", size);
	}

	private void setPaperWidth() {
		fontProperty.set(getSystemFont(axisFontSize * sldrResolution.getValue()));
//		System.out.println(sldrResolution.getValue()+"\t"+zoomTarget.getScaleX()+"\t"+(zoomTarget.getScaleX()/sldrResolution.getValue()));
//		zoomTarget.setScaleX(zoomTarget.getScaleX()/sldrResolution.getValue());
//		zoomTarget.setScaleY(zoomTarget.getScaleY()/sldrResolution.getValue());
		resizeCanvas();
		setElementScales();
	}

	private void setElementScales() {
		double scale = sldrElements.getValue();
		scale = scale * sldrResolution.getValue();
		scaledNodeRadius = scale * nodeRadius;
		scaledFont = Font.font("Courier New", scale * labelFontSize);
		scaledLineWidth = scale * lineWidth;
		drawScene();
	}

	private void processDataMessage(SpaceData data) {
		Platform.runLater(() -> {
			lblTime.setText(timeFormatter.getTimeText(data.time()));
			boolean refreshLegend = updateData(data);
			if (refreshLegend)
				updateLegend();
			drawScene();
		});
	}

	@Override
	public void onDataMessage(SpaceData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial)) {
				lstInitialData.add(data);
			} else
				processDataMessage(data);
		}
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {

			mpPoints.clear();
			stLines.clear();
			mpColours.clear();

			for (SpaceData data : lstInitialData)
				processDataMessage(data);

			lstInitialData.clear();
		}
	}

// This be synchronized to prevent errors when resetting during a slow
	// simulation
	private synchronized boolean updateData(SpaceData data) {
		boolean updateLegend = false;
		// Update points

		// delete points in the point list
		int pc = mpPoints.size();
		int pd = 0;
		for (DataLabel lab : data.pointsToDelete()) {
			// It's an error if the lab is NOT found in the list before
			if (mpPoints.remove(lab.toString()) == null)
				System.out.println("Warning: Attempt to delete non-existing point. [" + lab + "]");
			else {
				pd++;
// 				JG Fix: we dont want the legend to shrink every time a colour disappears:
//				if ( uninstallColour(lab))
//				updateLegend = true;
//				IDD: ok but will quickly start to recycle colours
			}
		}

		// add points
		int pa = 0;
		for (DataLabel lab : data.pointsToCreate().keySet()) {
			Duple<DataLabel, double[]> newValue = new Duple<>(lab, data.pointsToCreate().get(lab));
			// It's an error if the lab IS found in the list before
			if (mpPoints.put(lab.toString(), newValue) != null)
				throw new TwuifxException("Attempt to add an already existing point. [" + lab + "]");
			pa++;
			if (installPointColour(lab))
				updateLegend = true;

		}
		// move points in the point list
//		int pm = 0;
		for (DataLabel lab : data.pointsToMove().keySet()) {
			Duple<DataLabel, double[]> newValue = new Duple<>(lab, data.pointsToMove().get(lab));
			// It's an error if the lab is NOT in the list
			if (mpPoints.put(lab.toString(), newValue) == null)
				throw new TwuifxException("Attempt to move a non-existing point. [" + lab + "]");
//			pm++;
			if (installPointColour(lab))
				updateLegend = true;
		}

		int pu = mpPoints.size();
		if (pu != (pc - pd + pa))
			System.out.println("Points don't add up. [" + pc + "-" + pd + "+" + pa + "=" + pu + "]");

		// update lines
		int lc = stLines.size();
		int la = 0;
		for (Tuple<DataLabel, DataLabel, String> line : data.linesToCreate()) {
			boolean added = stLines.add(line);
			// PROBLEM HERE: there may be two relations OF DIFFERENT TYPES relating the
			// same two points....
			// so we must allow for identical ends but different types.

			// Thats ok: The lines are just Tuples - it doesn't matter what the start and
			// end points are
			if (installLineColour(line.getThird()))
				updateLegend = true;
			if (!added) {
				throw new TwuifxException("Attempt to add already existing line. [" + line + "]");
			} else
				la++;
		}

		// remove lines
		int ld = 0;
		for (Tuple<DataLabel, DataLabel, String> line : data.linesToDelete()) {
			boolean removed = stLines.remove(line);
			if (!removed) {
//				throw new TwuifxException("Attempt to delete a non-existing line. [" + line+"]");
//				This is allowed now. Forgot why.
//				System.out.println("Warning: Attempt to delete a non-existing line. [" + line + "]");
			} else
				ld++;
		}

		// IMPORTANT (JG): remove line entries which end or start nodes have been
		// removed just above.

		int ldnr = 0;
		Iterator<Tuple<DataLabel, DataLabel, String>> itline = stLines.iterator();
		while (itline.hasNext()) {
			Tuple<DataLabel, DataLabel, String> line = itline.next();
			if (!mpPoints.containsKey(line.getFirst().toString())
					|| !mpPoints.containsKey(line.getSecond().toString())) {
				itline.remove();
				ldnr++;
			}
		}

		// This will fail on reset unless synchronized
		int lu = stLines.size();
		if (lu != (lc - (ld + ldnr) + la))
			System.out.println("Lines don't add up. [" + lc + "-(" + (ld + ldnr) + "+" + la + "=" + lu + "]");
//		System.out.println("Stored points: "+hPointsMap.size());
//		System.out.println("Stored lines: "+lineReferences.size());

		return updateLegend;
	}

	private boolean installLineColour(String type) {
		boolean update = false;
		if (lineTypeColours.get(type) == null) {
			// arbitrary offset to list of colours to separate them from point colours
			int dx = 10;
			if (colour64)
				dx = 13;
			lineTypeColours.put(type, getColour(lineTypeColours.size() + dx));
			update = true;
		}
		return update;
	}

	private boolean installPointColour(DataLabel dl) {
		boolean update = false;
		String cKey = getColourKey(dl);
		Duple<Integer, Color> value = mpColours.get(cKey);
		if (value == null) {
			value = new Duple<Integer, Color>(1, getColour(mpColours.size()));
			update = true;
		} else
			value = new Duple<Integer, Color>(value.getFirst() + 1, value.getSecond());
		mpColours.put(cKey, value);
		return update;
	}

	private String getColourKey(DataLabel dl) {
		String result = "";
		for (int i = 0; i < Math.min(colourHLevel + 1, dl.size()); i++)
			result += "." + dl.get(i);
		return result.replaceFirst(".", "");
	}

//-------------------------------------------- Drawing ---

	private synchronized void drawScene() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFont(scaledFont);
		drawPaper(gc);
		gc.setLineWidth(scaledLineWidth);
		gc.setLineDashes(0);

		if (chbxLines.isSelected()) {
			// gc.setStroke(lineColour);
			for (Tuple<DataLabel, DataLabel, String> lineReference : stLines) {
				String sType = lineReference.getThird();
				Color lineTypeColour = lineTypeColours.get(sType);
				gc.setStroke(lineTypeColour);
				// now we need a line type colour system.
				String sKey = lineReference.getFirst().toString();
				String eKey = lineReference.getSecond().toString();
				Duple<DataLabel, double[]> sEntry = mpPoints.get(sKey);
				Duple<DataLabel, double[]> eEntry = mpPoints.get(eKey);
				if (sEntry == null)
					throw new TwuifxException("Line error. Start point not found " + sKey);
				if (eEntry == null)
					throw new TwuifxException("Line error. End point not found " + eKey);
				double[] start = sEntry.getSecond();
				double[] end = eEntry.getSecond();
				// Clone if altering: may need to limit lines to intersection with he map edge
				if (eec == null) {
					drawLine(gc, start[0], start[1], end[0], end[1], true);
				} else if (eec.equals(EdgeEffectCorrection.periodic))
					drawPeriodicLines(gc, start, end);
				else if (eec.equals(EdgeEffectCorrection.tubular)) {
					// assume first dim means 'x'
					drawTubularLines(gc, start, end);
				} else {
					drawLine(gc, start[0], start[1], end[0], end[1], true);
				}
			}
		}

		double size = 2 * scaledNodeRadius;
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setTextBaseline(VPos.CENTER);
		for (Map.Entry<String, Duple<DataLabel, double[]>> entry : mpPoints.entrySet()) {
			Duple<DataLabel, double[]> value = entry.getValue();
			String cKey = getColourKey(value.getFirst());
			Duple<Integer, Color> colourEntry = mpColours.get(cKey);
//			if (colourEntry != null) {
			Color colour = colourEntry.getSecond();
			gc.setStroke(colour);
			double[] coords = value.getSecond();
			Point2D point = spaceToCanvas(coords);
			point = point.add(-scaledNodeRadius, -scaledNodeRadius);
			gc.strokeOval(point.getX(), point.getY(), size, size);
			if (symbolFill) {
				gc.setFill(colour);
				gc.fillOval(point.getX(), point.getY(), size, size);
			}
			if (chbxLabels.isSelected()) {
				gc.setFill(fontColour);
				String label = value.getFirst().getEnd();
				gc.fillText(label, point.getX() + scaledNodeRadius, point.getY() + scaledNodeRadius);
			}
		}
//		}
	}

	private void drawTubularLines(GraphicsContext gc, double[] startPoint, double[] endPoint) {
		// TODO: Check this!
		double[] transPoint = new double[2];
		transPoint[0] = endPoint[0];
		transPoint[1] = endPoint[1];
		double tx = translate(startPoint[0], endPoint[0], spaceBounds.getMaxX());
		if (Double.isFinite(tx))
			transPoint[0] = tx;
		int quad = getQuad(transPoint);

		double m = (transPoint[1] - startPoint[1]) / (transPoint[0] - startPoint[0]);
		double b = startPoint[1] - (m * startPoint[0]);
		switch (quad) {
		case 0: {// no wrap: nothing to do
			drawLine(gc, startPoint[0], startPoint[1], endPoint[0], endPoint[1], true);
			break;
		}
		case 1: {// right
			double yi = getYAt(spaceBounds.getMaxX(), m, b);
			drawLine(gc, startPoint[0], startPoint[1], spaceBounds.getMaxX(), yi, false);
			drawLine(gc, 0.0, yi, endPoint[0], endPoint[1], true);
			break;
		}
		case 5: {// left
			double yi = getYAt(0.0, m, b);
			drawLine(gc, startPoint[0], startPoint[1], 0.0, yi, false);
			drawLine(gc, spaceBounds.getMaxX(), yi, endPoint[0], endPoint[1], true);
			break;
		}
		default: {
			throw new TwuifxException("Line to unhandled quadrant [" + quad + ": (" + startPoint[0] + ","
					+ startPoint[1] + ") > (" + endPoint[0] + "," + endPoint[1] + ")]");
		}
		}

	}

	private static double translate(double s, double e, double max) {
		double result = Double.POSITIVE_INFINITY;
		double dx = Math.abs(e - s);
		double mn = Math.min(s, e);
		double mx = Math.max(s, e);
		double dxw = mn + max - mx;
		if (dxw < dx) {
			// translate e
			if (e > s)
				result = e - max;
			else
				result = e + max;
		}
		return result;

	}

	private static int[][] q = { { 6, 5, 4 }, { 7, 0, 3 }, { 8, 1, 2 } };

	private static int shift(double v, double r) {
		if (v < 0)
			return 0;
		else if (v > r)
			return 2;
		else
			return 1;
	}

	private int getQuad(double[] p) {
		int xshift = shift(p[0], spaceBounds.getMaxX());
		int yshift = shift(p[1], spaceBounds.getMaxY());
		return q[xshift][yshift];
	}

	private static double getXAt(double y, double m, double b) {
		return (y - b) / m;
	}

	private static double getYAt(double x, double m, double b) {
		return m * x + b;
	}

	private void drawPeriodicLines(GraphicsContext gc, double[] startPoint, double[] endPoint) {
		double[] transPoint = new double[2];
		transPoint[0] = endPoint[0];
		transPoint[1] = endPoint[1];
		double tx = translate(startPoint[0], endPoint[0], spaceBounds.getMaxX());
		double ty = translate(startPoint[1], endPoint[1], spaceBounds.getMaxY());
		if (Double.isFinite(tx))
			transPoint[0] = tx;
		if (Double.isFinite(ty))
			transPoint[1] = ty;

		int quad = getQuad(transPoint);

		double m = (transPoint[1] - startPoint[1]) / (transPoint[0] - startPoint[0]);
		double b = startPoint[1] - (m * startPoint[0]);
		switch (quad) {
		case 0: {// no wrap: nothing to do
			drawLine(gc, startPoint[0], startPoint[1], endPoint[0], endPoint[1], true);
			break;
		}
		// right
		case 1: {
			double yi = getYAt(spaceBounds.getMaxX(), m, b);
			drawLine(gc, startPoint[0], startPoint[1], spaceBounds.getMaxX(), yi, false);
			drawLine(gc, 0.0, yi, endPoint[0], endPoint[1], true);
			break;
		}
		// top right
		case 2: {
			double yi = getYAt(spaceBounds.getMaxX(), m, b);
			double xi = getXAt(spaceBounds.getMaxY(), m, b);
			double xd2 = Distance.squaredEuclidianDistance(startPoint[0], startPoint[1], xi, spaceBounds.getMaxY());
			double yd2 = Distance.squaredEuclidianDistance(startPoint[0], startPoint[1], spaceBounds.getMaxX(), yi);
			if (xd2 < yd2) { // cross the x-axis first
				drawLine(gc, startPoint[0], startPoint[1], xi, spaceBounds.getMaxY(), false);
				drawLine(gc, xi, 0.0, spaceBounds.getMaxX(), (yi - spaceBounds.getMaxY()), false);
				drawLine(gc, 0.0, (yi - spaceBounds.getMaxY()), endPoint[0], endPoint[1], true);
			} else { // cross at y-axis first
				drawLine(gc, 0.0, yi, (xi - spaceBounds.getMaxX()), spaceBounds.getMaxY(), false);
				drawLine(gc, 0.0, yi, (xi - spaceBounds.getMaxX()), spaceBounds.getMaxY(), false);
				drawLine(gc, (xi - spaceBounds.getMaxX()), 0.0, endPoint[0], endPoint[1], true);
			}
			break;
		}
		// top
		case 3: {
			double xi = getXAt(spaceBounds.getMaxY(), m, b);
			if (Double.isNaN(xi)) {// vertical line
				xi = startPoint[0];
				drawLine(gc, xi, startPoint[1], xi, spaceBounds.getMaxY(), false);
				drawLine(gc, xi, 0.0, xi, endPoint[1], true);
			} else {
				drawLine(gc, startPoint[0], startPoint[1], xi, spaceBounds.getMaxY(), false);
				drawLine(gc, xi, 0.0, endPoint[0], endPoint[1], true);
			}
			break;
		}
		// top left
		case 4: {
			double yi = getYAt(0.0, m, b);
			double xi = getXAt(spaceBounds.getMaxY(), m, b);
			double xd2 = Distance.squaredEuclidianDistance(startPoint[0], startPoint[1], xi, spaceBounds.getMaxY());
			double yd2 = Distance.squaredEuclidianDistance(startPoint[0], startPoint[1], 0, yi);
			if (xd2 < yd2) { // cross the x-axis first
				drawLine(gc, startPoint[0], startPoint[1], xi, spaceBounds.getMaxY(), false);
				drawLine(gc, xi, 0.0, 0, yi - spaceBounds.getMaxY(), false);
				drawLine(gc, spaceBounds.getMaxX(), yi - spaceBounds.getMaxY(), endPoint[0], endPoint[1], true);
			} else { // cross at y-axis first
				drawLine(gc, startPoint[0], startPoint[1], 0, yi, false);
				drawLine(gc, spaceBounds.getMaxX(), yi, spaceBounds.getMaxX() + xi, spaceBounds.getMaxY(), false);
				drawLine(gc, spaceBounds.getMaxX() + xi, 0.0, endPoint[0], endPoint[1], true);
			}
			break;
		}
		// left
		case 5: {
			double yi = getYAt(0.0, m, b);
			drawLine(gc, startPoint[0], startPoint[1], 0.0, yi, false);
			drawLine(gc, spaceBounds.getMaxX(), yi, endPoint[0], endPoint[1], true);
			break;
		}
		// bottom left
		case 6: {
			double xi = getXAt(0.0, m, b);
			double yi = getYAt(0.0, m, b);
			double xd2 = Distance.squaredEuclidianDistance(startPoint[0], startPoint[1], xi, 0.0);
			double yd2 = Distance.squaredEuclidianDistance(startPoint[0], startPoint[1], 0.0, yi);
			if (xd2 < yd2) { // cross the x-axis first
				drawLine(gc, startPoint[0], startPoint[1], xi, 0.0, false);
				drawLine(gc, xi, spaceBounds.getMaxY(), 0.0, spaceBounds.getMaxY() + yi, false);
				drawLine(gc, spaceBounds.getMaxX(), spaceBounds.getMaxY() + yi, endPoint[0], endPoint[1], true);
			} else {
				drawLine(gc, startPoint[0], startPoint[1], 0.0, yi, false);
				drawLine(gc, spaceBounds.getMaxX(), yi, spaceBounds.getMaxX() + xi, 0.0, false);
				drawLine(gc, spaceBounds.getMaxX() + xi, spaceBounds.getMaxY(), endPoint[0], endPoint[1], true);
			}
			break;
		}
		// bottom
		case 7: {
			double xi = getXAt(0.0, m, b);
			if (Double.isNaN(xi)) {// vertical line
				xi = startPoint[0];
				drawLine(gc, xi, startPoint[1], xi, 0.0, false);
				drawLine(gc, xi, spaceBounds.getMaxY(), xi, endPoint[1], true);
			} else {
				drawLine(gc, startPoint[0], startPoint[1], xi, 0.0, false);
				drawLine(gc, xi, spaceBounds.getMaxY(), endPoint[0], endPoint[1], true);
			}
			break;
		}
		// bottom right
		case 8: {
			double yi = getYAt(spaceBounds.getMaxX(), m, b);
			double xi = getXAt(0.0, m, b);
			double xd2 = Distance.squaredEuclidianDistance(startPoint[0], startPoint[1], xi, 0);
			double yd2 = Distance.squaredEuclidianDistance(startPoint[0], startPoint[1], spaceBounds.getMaxX(), yi);
			if (xd2 < yd2) { // cross at x-axis first
				drawLine(gc, startPoint[0], startPoint[1], xi, 0.0, false);
				drawLine(gc, xi, spaceBounds.getMaxY(), spaceBounds.getMaxX(), spaceBounds.getMaxY() + yi, false);
				drawLine(gc, 0.0, spaceBounds.getMaxY() + yi, endPoint[0], endPoint[1], true);
			} else { // cross at y-axis first
				drawLine(gc, startPoint[0], startPoint[1], spaceBounds.getMaxX(), yi, false);
				drawLine(gc, 0.0, yi, xi - spaceBounds.getMaxX(), 0.0, false);
				drawLine(gc, xi - spaceBounds.getMaxX(), spaceBounds.getMaxY(), endPoint[0], endPoint[1], true);
			}
			break;
		}
		default: {
			throw new TwuifxException("Line to unhandled quadrant [" + quad + ": (" + startPoint[0] + ","
					+ startPoint[1] + ") > (" + endPoint[0] + "," + endPoint[1] + ")]");
		}
		}

	}

	private void drawLine(GraphicsContext gc, double x1, double y1, double x2, double y2, boolean endNode) {
		Point2D start = spaceToCanvas(x1, y1);
		Point2D end = spaceToCanvas(x2, y2);
		gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
		if (showArrows)
			drawArrow(gc, start, end, endNode);

	}

	private void drawArrow(GraphicsContext gc, Point2D start, Point2D end, boolean endNode) {
		if (!endNode && !showIntermediateArrows)
			return;
		// coordinates of the segment end - funny we end before we start
		double x0 = end.getX();
		double y0 = end.getY();
		// # coordinates of the segment start
		double x1 = start.getX();
		double y1 = start.getY();
		// # length of the arrowhead in absolute units (constant)
		double f = scaledNodeRadius;// why not
		// # sine and cosine of the arrowhead 1/2 angle (constant)
		double sin30 = 0.5;// prep
		double cos30 = Math.sqrt(3.0) / 2.0;// prep
		// #circle radius
		double rad = scaledNodeRadius;// repl
		// # compute length of the segment
		double r = Distance.euclidianDistance(x0, y0, x1, y1);
		if (r < rad)// don't bother?
			return;

		// # compute sine and cosine of the segment angle relative to x axis
		double cost = (x1 - x0) / r;
		double sint = (y1 - y0) / r;
		// # coordinates of the left-hand end of the arrow line
		double xA = x0 + f * (cos30 * cost + sin30 * sint);
		double yA = y0 + f * (cos30 * sint - sin30 * cost);
		// # coordinates of the right-hand end of the arrow line
		double xB = x0 + f * (cos30 * cost - sin30 * sint);
		double yB = y0 + f * (cos30 * sint + sin30 * cost);
		// # get arrow off-set to circle radius
		if (showIntermediateArrows && !endNode) {
			gc.strokeLine(x0, y0, xB, yB);
			gc.strokeLine(x0, y0, xA, yA);
		} else {
			double phi = Math.atan2(y1 - y0, x1 - x0);// expensive? 10x sqrt
			double dx = rad * Math.cos(phi);
			double dy = rad * Math.sin(phi);
			// # drawing the arrow - offset by radius
			gc.strokeLine(x0 + dx, y0 + dy, xB + dx, yB + dy);
			gc.strokeLine(x0 + dx, y0 + dy, xA + dx, yA + dy);
		}
	}

	private void resizeCanvas() {
		double r = getSpaceCanvasRatio();
		int newWidth = (int) Math.round(r * spaceBounds.getWidth());
		int newHeight = (int) Math.round(r * spaceBounds.getHeight());
		if (canvas.getWidth() != newWidth || canvas.getHeight() != newHeight) {
			canvas.setWidth(newWidth);
			canvas.setHeight(newHeight);
		}
	}

	private double getSpaceCanvasRatio() {
		return (sldrResolution.getValue() * paperSize) / spaceBounds.getWidth();
	}

	private void drawPaper(GraphicsContext gc) {
		gc.setFill(bkgColour);
		gc.setStroke(bkgColour);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		double w = canvas.getWidth();
		double h = canvas.getHeight();
		double maxDim = Math.max(spaceBounds.getWidth(), spaceBounds.getHeight());
		double maxSize = Math.max(w, h);
		double scale = maxSize / maxDim;
		double scaledTickSize = scale * tickSize;
		double lineWidth = scaledTickSize / 100.0;
		double dashes = scaledTickSize / 10.0;
		if (chbxGrid.isSelected()) {
			gc.setStroke(Color.GREY);
			gc.setLineDashes(dashes);
			gc.setLineWidth(lineWidth);
//			double startX = getStartValue(spaceBounds.getMinX(), offsetX, tickSize);
			double startX = offsetX;
			for (int i = 0; i < nXAxisTicks; i++) {
				double x = i * tickSize + startX;
				x = x * scale;
				gc.strokeLine(x, 0, x, h);
			}

			double startY = offsetY;
			for (int i = 0; i < nYAxisTicks; i++) {
				double y = i * tickSize + startY;
				y = h - (scale * y);
				gc.strokeLine(0, y, w, y);
			}
		}
		if (chbxBoundaries.isSelected()) {
			double lws = lineWidth * 10;// line width scaling
			drawBorder(gc, BorderType.valueOf(borderList.getWithFlatIndex(0)), 1, 0, 1, h, lws, dashes);
			drawBorder(gc, BorderType.valueOf(borderList.getWithFlatIndex(1)), w, 0, w, h, lws, dashes);
			drawBorder(gc, BorderType.valueOf(borderList.getWithFlatIndex(2)), 0, h, w, h, lws, dashes);
			drawBorder(gc, BorderType.valueOf(borderList.getWithFlatIndex(3)), 0, 1, w, 1, lws, dashes);
		}

		for (Map.Entry<Double, Duple<Label, Label>> entry : xAxes.entrySet()) {
			Label bottom = entry.getValue().getFirst();
			Label top = entry.getValue().getSecond();
			double value = entry.getKey() - spaceBounds.getMinX();
			double pos = value * scale + leftAxis.getWidth();
			double center = top.getWidth() / 2.0;
			AnchorPane.setLeftAnchor(top, pos - center);
			AnchorPane.setLeftAnchor(bottom, pos - center);
		}
		for (Map.Entry<Double, Duple<Label, Label>> entry : yAxes.entrySet()) {
			Label left = entry.getValue().getFirst();
			Label right = entry.getValue().getSecond();
			double value = entry.getKey() - spaceBounds.getMinY();
			double pos = h - (value * scale);
			AnchorPane.setTopAnchor(left, pos);
			AnchorPane.setTopAnchor(right, pos);
			AnchorPane.setRightAnchor(left, 0.0);
		}
	};

	private Color getColour(int idx) {
		return lstColoursAvailable.get(idx % lstColoursAvailable.size());
	}

	private double rescale(double value, double fMin, double fMax, double tMin, double tMax) {
		double fr = fMax - fMin;
		double tr = tMax - tMin;
		double p = (value - fMin) / fr;
		return p * tr + tMin;
	}

	private Point2D spaceToCanvas(double... coords) {
		double sx = coords[0];
		double sy = coords[1];
		double dx = rescale(sx, spaceBounds.getMinX(), spaceBounds.getMaxX(), 0, canvas.getWidth());
		double dy = rescale(sy, spaceBounds.getMinY(), spaceBounds.getMaxY(), 0, canvas.getHeight());
		dy = canvas.getHeight() - dy;
		return new Point2D(dx, dy);
	}

// ---------------------------------------- Preferences

	private static final String keyScaleX = "scaleX";
	private static final String keyScaleY = "scaleY";
	private static final String keyScrollH = "scrollH";
	private static final String keyScrollV = "scrollV";
	private static final String keyElementScale = "elementScale";
	private static final String keyPaperScale = "paperScale";
	private static final String keySymbolFill = "fill";
	private static final String keyBKG = "bkg";
//	private static final String keyLineColour = "lineColour";
	private static final String keyContrast = "contrast";
	private static final String keyColour64 = "colour64";
	private static final String keyColourHLevel = "colourHLevel";
	private static final String keyShowLines = "showLines";
	private static final String keyShowGrid = "showGrid";
	private static final String keyShowEdgeEffect = "showEdgeEffect";
	private static final String keyLegendVisible = "legendVisible";
	private static final String keyMaxLegendItems = "maxLegendItems";
	private static final String keyLegendSide = "legendSide";
	private static final String keyShowPointLabels = "showPointLabels";
	private static final String keyShowArrows = "showArrows";
	private static final String keyShowIntermediateArrows = "showIntermediateArrows";
	private static final String keyFontColour = "fontColour";
//	private static final String keyFullLegendNames = "fullLegendNames";

	private int colourHLevel;
	private boolean symbolFill;
	private Color bkgColour;
//	private Color lineColour;
	private Color fontColour;
	private double contrast;
	private boolean colour64;
//	private boolean showLines;
	private int maxLegendItems;
	private Side legendSide;
	private boolean showArrows;
	private boolean showIntermediateArrows;
//	private boolean fullLegendNames;
	private PaletteSize paletteSize = PaletteSize.veryLarge;

	@Override
	public void putUserPreferences() {
		Preferences.putDouble(widgetId + keyScaleX, zoomTarget.getScaleX());
		Preferences.putDouble(widgetId + keyScaleY, zoomTarget.getScaleY());
		Preferences.putDouble(widgetId + keyScrollH, scrollPane.getHvalue());
		Preferences.putDouble(widgetId + keyScrollV, scrollPane.getVvalue());
		Preferences.putInt(widgetId + keyColourHLevel, colourHLevel);
		Preferences.putDouble(widgetId + keyElementScale, sldrElements.getValue());
		Preferences.putDouble(widgetId + keyPaperScale, sldrResolution.getValue());
		Preferences.putBoolean(widgetId + keySymbolFill, symbolFill);
		Preferences.putBoolean(widgetId + keyShowGrid, chbxGrid.isSelected());
		Preferences.putBoolean(widgetId + keyShowEdgeEffect, chbxBoundaries.isSelected());
		Preferences.putDoubles(widgetId + keyBKG, bkgColour.getRed(), bkgColour.getGreen(), bkgColour.getBlue());
//		Preferences.putDoubles(widgetId + keyLineColour, lineColour.getRed(), lineColour.getGreen(),
//				lineColour.getBlue());
		Preferences.putDoubles(widgetId + keyFontColour, fontColour.getRed(), fontColour.getGreen(),
				fontColour.getBlue());
		Preferences.putDouble(widgetId + keyContrast, contrast);
		Preferences.putBoolean(widgetId + keyColour64, colour64);
		Preferences.putBoolean(widgetId + keyShowLines, chbxLines.isSelected());
		Preferences.putBoolean(widgetId + keyLegendVisible, chbxLegend.isSelected());
		Preferences.putInt(widgetId + keyMaxLegendItems, maxLegendItems);
		Preferences.putEnum(widgetId + keyLegendSide, legendSide);
		Preferences.putBoolean(widgetId + keyShowPointLabels, chbxLabels.isSelected());
		Preferences.putBoolean(widgetId + keyShowArrows, showArrows);
		Preferences.putBoolean(widgetId + keyShowIntermediateArrows, showIntermediateArrows);
//		Preferences.putBoolean(widgetId + keyFullLegendNames, fullLegendNames);
	}

	// called at END of UI construction because this depends on UI components.
	@Override
	public void getUserPreferences() {
		zoomTarget.setScaleX(Preferences.getDouble(widgetId + keyScaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(widgetId + keyScaleY, zoomTarget.getScaleY()));
		scrollPane.setHvalue(Preferences.getDouble(widgetId + keyScrollH, scrollPane.getHvalue()));
		scrollPane.setVvalue(Preferences.getDouble(widgetId + keyScrollV, scrollPane.getVvalue()));
		colourHLevel = Preferences.getInt(widgetId + keyColourHLevel, 0);
		sldrElements.setValue(Preferences.getDouble(widgetId + keyElementScale, 1.0));
		sldrResolution.setValue(Preferences.getDouble(widgetId + keyPaperScale, 1.0));
		symbolFill = Preferences.getBoolean(widgetId + keySymbolFill, true);
		chbxGrid.setSelected(Preferences.getBoolean(widgetId + keyShowGrid, true));
		chbxBoundaries.setSelected(Preferences.getBoolean(widgetId + keyShowEdgeEffect, true));
		double[] rgb;
		rgb = Preferences.getDoubles(widgetId + keyBKG, Color.WHITE.getRed(), Color.WHITE.getGreen(),
				Color.WHITE.getBlue());
		bkgColour = new Color(rgb[0], rgb[1], rgb[2], 1.0);

//		rgb = Preferences.getDoubles(widgetId + keyLineColour, Color.GREY.getRed(), Color.GREY.getGreen(),
//				Color.GREY.getBlue());
//		lineColour = new Color(rgb[0], rgb[1], rgb[2], 1.0);

		rgb = Preferences.getDoubles(widgetId + keyFontColour, Color.BLACK.getRed(), Color.BLACK.getGreen(),
				Color.BLACK.getBlue());
		fontColour = new Color(rgb[0], rgb[1], rgb[2], 1.0);

		contrast = Preferences.getDouble(widgetId + keyContrast, 0.2);
		colour64 = Preferences.getBoolean(widgetId + keyColour64, false);
		// small= 8, medium=27, large = 64, veryLarge 125 colours max

		if (colour64)
			lstColoursAvailable = ColourContrast.getContrastingColours64(paletteSize, bkgColour, contrast);
		else
			lstColoursAvailable = ColourContrast.getContrastingColours(paletteSize, bkgColour, contrast);
		legendSide = (Side) Preferences.getEnum(widgetId + keyLegendSide, Side.BOTTOM);
		chbxLegend.setSelected(Preferences.getBoolean(widgetId + keyLegendVisible, true));
		maxLegendItems = Preferences.getInt(widgetId + keyMaxLegendItems, 10);
		chbxLines.setSelected(Preferences.getBoolean(widgetId + keyShowLines, true));
		chbxLabels.setSelected(Preferences.getBoolean(widgetId + keyShowPointLabels, false));
		showArrows = Preferences.getBoolean(widgetId + keyShowArrows, false);
		showIntermediateArrows = Preferences.getBoolean(widgetId + keyShowIntermediateArrows, true);
//		fullLegendNames = Preferences.getBoolean(widgetId + keyFullLegendNames, false);

	}

	// --------------- GUI
	private BorderPane centerContainer;

	private void placeLegend() {
		centerContainer.setLeft(null);
		centerContainer.setRight(null);
		centerContainer.setTop(null);
		centerContainer.setBottom(null);
		if (chbxLegend.isSelected())
			switch (legendSide) {
			case TOP: {
				legend.setOrientation(Orientation.HORIZONTAL);
				centerContainer.setTop(legend);
				break;
			}
			case BOTTOM: {
				legend.setOrientation(Orientation.HORIZONTAL);
				centerContainer.setBottom(legend);
				break;
			}
			case LEFT: {
				legend.setOrientation(Orientation.VERTICAL);
				centerContainer.setLeft(legend);
				break;
			}
			default: {
				legend.setOrientation(Orientation.VERTICAL);
				centerContainer.setRight(legend);
			}
			}

	}

	private void updateLegend() {
		legend.getChildren().clear();

		int count = 0;
		for (Map.Entry<String, Duple<Integer, Color>> entry : mpColours.entrySet()) {
			count++;
			if (count <= maxLegendItems) {
				String legendName = entry.getKey();
				addLegendItem(legendName, entry.getValue().getSecond(), true);
			}
		}
		for (Map.Entry<String, Color> entry : lineTypeColours.entrySet()) {
			count++;
			if (count <= maxLegendItems)
				addLegendItem(entry.getKey(), entry.getValue(), false);
		}
		if (count > maxLegendItems)
			legend.getChildren().add(new Label("more ..."));

	}

	private void addLegendItem(String name, Color colour, boolean isPoint) {
		StackPane stackPane = new StackPane();
		Rectangle r = new Rectangle(12, 12);
		r.setStroke(bkgColour);
		r.setFill(bkgColour);
		Shape s = null;
		if (isPoint) {
			if (!symbolFill) {
				s = new Circle(4, Color.TRANSPARENT);
			} else
				s = new Circle(4, colour);
			s.setStroke(colour);
		} else {
			s = new Line(0, 6, 12, 6);
			s.setStroke(colour);
			s.setFill(colour);
		}
		stackPane.getChildren().addAll(r, s);
		StackPane.setAlignment(s, Pos.CENTER);
		StackPane.setAlignment(s, Pos.CENTER);

		legend.getChildren().add(new Label(name, stackPane));
	}

	@Override
	public Object getMenuContainer() {
		Menu mu = new Menu(widgetId);
		MenuItem miEdit = new MenuItem("Edit...");
		mu.getItems().add(miEdit);
		miEdit.setOnAction(e -> edit());
		return mu;
	}

	private static void addGridControl(String name, int row, int col, Node ctrl, GridPane grid) {
		Label lbl = new Label(name);
		grid.add(lbl, col, row);
		grid.add(ctrl, col + 1, row);
		GridPane.setHalignment(lbl, HPos.RIGHT);
		GridPane.setHalignment(ctrl, HPos.LEFT);
		GridPane.setValignment(ctrl, VPos.CENTER);
	}

	private void edit() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle(widgetId);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		dialog.initOwner((Window) Dialogs.owner());
		GridPane content = new GridPane();
		content.setVgap(15);
		content.setHgap(10);

		GridPane pointsGrid = new GridPane();
		GridPane linesGrid = new GridPane();
		GridPane paperGrid = new GridPane();
		GridPane legendGrid = new GridPane();

//		pointsGrid.setGridLinesVisible(true);
//		linesGrid.setGridLinesVisible(true);
//		paperGrid.setGridLinesVisible(true);
//		legendGrid.setGridLinesVisible(true);

		pointsGrid.setVgap(15);
		pointsGrid.setHgap(10);

		linesGrid.setVgap(15);
		linesGrid.setHgap(10);

		paperGrid.setVgap(15);
		paperGrid.setHgap(10);

		legendGrid.setVgap(15);
		legendGrid.setHgap(10);

		TitledPane tp;
		tp = new TitledPane("Points", pointsGrid);
		tp.setCollapsible(false);
		content.add(tp, 0, 0);
		GridPane.setValignment(tp, VPos.TOP);

		tp = new TitledPane("Lines", linesGrid);
		tp.setCollapsible(false);
		content.add(tp, 0, 1);
		GridPane.setValignment(tp, VPos.TOP);

		tp = new TitledPane("Paper", paperGrid);
		tp.setCollapsible(false);
		content.add(tp, 1, 0);
		GridPane.setValignment(tp, VPos.TOP);

		tp = new TitledPane("Legend", legendGrid);
		tp.setCollapsible(false);
		content.add(tp, 1, 1);
		GridPane.setValignment(tp, VPos.TOP);

//
		int col = 0;

		// ---------------------------------Points
		int row = 0;
		// -----
		CheckBox chbxFill = new CheckBox("");
		addGridControl("Solid", row++, col, chbxFill, pointsGrid);
		chbxFill.setSelected(symbolFill);
		// ----
		Spinner<Integer> spHLevel = new Spinner<>();
		addGridControl("Hierarchical colour level", row++, col, spHLevel, pointsGrid);
		spHLevel.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, colourHLevel));
		spHLevel.setMaxWidth(100);
		spHLevel.setEditable(true);
		// -----
		CheckBox chbxCS = new CheckBox("");
		addGridControl("64 Colour system", row++, col, chbxCS, pointsGrid);
		chbxCS.setSelected(colour64);
		// -----
		ColorPicker cpFont = new ColorPicker(fontColour);
		addGridControl("Font colour", row++, col, cpFont, pointsGrid);
		GridPane.setValignment(cpFont, VPos.TOP);

		// --------------------------------------- Lines
		row = 0;
		// -----
//		ColorPicker cpLine = new ColorPicker(lineColour);
//		addGridControl("Colour", row++, col, cpLine, linesGrid);
//		GridPane.setValignment(cpLine, VPos.TOP);
		// -----
		CheckBox chbxShowArrows = new CheckBox("");
		addGridControl("Arrowheads", row++, col, chbxShowArrows, linesGrid);
		chbxShowArrows.setSelected(showArrows);
		// -----
		CheckBox chbxShowIntermediateArrows = new CheckBox("");
		addGridControl("Intermediate arrowheads", row++, col, chbxShowIntermediateArrows, linesGrid);
		chbxShowIntermediateArrows.setSelected(showIntermediateArrows);

		// --------------------------------------- Paper
		// -----
		row = 0;
		// ----
		ColorPicker cpBkg = new ColorPicker(bkgColour);
		addGridControl("Colour", row++, col, cpBkg, paperGrid);
		GridPane.setValignment(cpBkg, VPos.TOP);
		// ----
		TextField tfContrast = new TextField(Double.toString(contrast));
		tfContrast.setMaxWidth(50);
		tfContrast.setTextFormatter(
				new TextFormatter<>(change -> (change.getControlNewText().matches(Dialogs.vsReal) ? change : null)));
		addGridControl("Contrast (0.0-1.0)", row++, col, tfContrast, paperGrid);

		// ---------------------------- Legend
		row = 0;
		// ----
		ComboBox<Side> cmbSide = new ComboBox<>();
		cmbSide.getItems().addAll(Side.values());
		cmbSide.getSelectionModel().select(legendSide);
		addGridControl("Side", row++, col, cmbSide, legendGrid);
		// ----
		Spinner<Integer> spMaxLegendItems = new Spinner<>();
		spMaxLegendItems.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, maxLegendItems));
		spMaxLegendItems.setMaxWidth(100);
		spMaxLegendItems.setEditable(true);
		addGridControl("Max items", row++, col, spMaxLegendItems, legendGrid);
		// spacer

		dialog.getDialogPane().setContent(content);
		boolean newLegend = false;
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			if (symbolFill != chbxFill.isSelected()) {
				symbolFill = chbxFill.isSelected();
				newLegend = true;
			}
			if (contrast != Double.parseDouble(tfContrast.getText())) {
				contrast = Double.parseDouble(tfContrast.getText());
				newLegend = true;
			}
			if (colour64 != chbxCS.isSelected()) {
				colour64 = chbxCS.isSelected();
				newLegend = true;
			}
			if (bkgColour != cpBkg.getValue()) {
				bkgColour = cpBkg.getValue();
				newLegend = true;
			}
			if (colourHLevel != spHLevel.getValue()) {
				colourHLevel = spHLevel.getValue();
				newLegend = true;
			}

//			lineColour = cpLine.getValue();
			fontColour = cpFont.getValue();
			if (colour64)
				lstColoursAvailable = ColourContrast.getContrastingColours64(paletteSize, bkgColour, contrast);
			else
				lstColoursAvailable = ColourContrast.getContrastingColours(paletteSize, bkgColour, contrast);

			legendSide = cmbSide.getValue();
			maxLegendItems = spMaxLegendItems.getValue();
			showArrows = chbxShowArrows.isSelected();
			showIntermediateArrows = chbxShowIntermediateArrows.isSelected();

			if (newLegend) {

				mpColours.clear();
				mpPoints.forEach((k, v) -> {
					installPointColour(v.getFirst());
				});

				lineTypeColours.clear();
				stLines.forEach((e) -> {
					installLineColour(e.getThird());
				});
				updateLegend();
			}

			placeLegend();
			resizeCanvas();
			drawScene();
		}
	}

	private void onMouseClicked(MouseEvent e) {
		String name = findName(e);
		lblItem.setText(name);
	}

	private String findName(MouseEvent e) {
		double scale = 1.0 / getSpaceCanvasRatio();
		double size = (scaledNodeRadius * 2) * scale;
		double rad = scaledNodeRadius * scale;
		double clickX = (e.getX() * scale) + spaceBounds.getMinX();
		double clickY = ((canvas.getHeight() - e.getY()) * scale) + spaceBounds.getMinY();
		BoundingBox box = new BoundingBox(clickX - rad, clickY - rad, size, size);
//		System.out.println(e.getX() + "," + e.getY());
		for (Map.Entry<String, Duple<DataLabel, double[]>> entry : mpPoints.entrySet()) {
			String key = entry.getKey();
			Duple<DataLabel, double[]> value = entry.getValue();
			double x = value.getSecond()[0];
			double y = value.getSecond()[1];
//			System.out.println(e.getX() + "," + e.getY()+":"+x+","+y);

			if (box.contains(x, y)) {
				return key + " [" + pointFormat.format(x) + "," + pointFormat.format(y) + "]";
			}
		}
		return "";
	}

	private static void drawBorder(GraphicsContext gc, BorderType bt, double x1, double y1, double x2, double y2,
			double lineWidthScale, double dashWidth) {
		gc.setLineJoin(StrokeLineJoin.ROUND);
		switch (bt) {
		case wrap: {
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(dashWidth * 4);
			gc.setLineWidth(1.0 * lineWidthScale);
			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
		case reflection: {
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(0);
			gc.setLineWidth(4.0 * lineWidthScale);
			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
		case sticky: {
			gc.setStroke(Color.GREY);
			gc.setLineDashes(0);
			gc.setLineWidth(4.0 * lineWidthScale);
			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
		case oblivion: {
			gc.setStroke(Color.WHITE);
			gc.setLineDashes(0);
			gc.setLineWidth(2.0);
			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
		default: {
			// infinite
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(0);
			gc.setLineWidth(2.0 * lineWidthScale);
			gc.strokeLine(x1, y1, x2, y2);
			break;
		}
		}

	}

	private double getTickSize() {
		double range = Math.max(spaceBounds.getWidth(), spaceBounds.getHeight());
		double tickSize = Math.pow(10, Math.round(Math.log10(range) - 1));
		if (range / tickSize < 5.0)
			tickSize = tickSize / 2.0;
		else
			while (range / tickSize > 10.0)
				tickSize = tickSize * 2.0;
		return tickSize;
	}

	private static double getTickOffset(double min, double tickSize) {
		return tickSize - (min % tickSize);
	}

	private static int getNTicks(double min, double max, double tickSize, double offset) {
		double start = min + offset;
		int n = 0;

		while (((n * tickSize) + start) < max)
			n++;

		if (offset == tickSize)
			n = n--;
		return n;
	}

	private double getStartValue(double min, double offset, double tickSize) {
		return min + offset;
	}
//	private boolean uninstallColour(DataLabel dl) {
//	String cKey = getColourKey(dl);
//	Duple<Integer, Color> value = mpColours.get(cKey);
//	if (value != null) {
//		if (value.getFirst() == 1) {
//			mpColours.remove(cKey);
//			return true;
//		} else {
//			value = new Duple<Integer, Color>(value.getFirst() - 1, value.getSecond());
//			mpColours.put(cKey, value);
//			return false;
//		}
//	}
//	return false;
//}

}
