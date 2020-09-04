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

package au.edu.anu.twuifx.widgets;

import java.util.ArrayList;
import java.util.Collection;
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
import au.edu.anu.ymuit.util.CenteredZooming;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.EdgeEffectCorrection;
import fr.cnrs.iees.twcore.constants.SpaceType;
import fr.ens.biologie.generic.utils.Duple;
import fr.ens.biologie.generic.utils.Interval;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import java.util.logging.Logger;

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
public class SimpleSpaceWidget1 extends AbstractDisplayWidget<SpaceData, Metadata> implements WidgetGUI {
	private AnchorPane zoomTarget;
	private Canvas canvas;
	private ScrollPane scrollPane;
	private Label lblItem;
	private Label lblTime;
	private Bounds spaceBounds;
	private String widgetId;
	private WidgetTrackingPolicy<TimeData> policy;
	private WidgetTimeFormatter timeFormatter;
	/**
	 * Hierarchically organised locations. Colours should be applied to any level in
	 * the data label hierarchy from species to stages to individuals.
	 */
	private Map<String, Duple<DataLabel, double[]>> hPointsMap;
	/** Lines are non-hierarchical. Just use the datalabel string as a lookup */
	private Set<Duple<DataLabel, DataLabel>> lineReferences;

	private List<Color> colours;
	private final Map<String, Duple<Integer, Color>> colourMap;
	private GridPane legend;

	private int resolution;
	private int symbolRadius;
	private boolean symbolFill;
	private boolean wrapAll;
	private Color bkgColour;
	private Color lineColour;
	private double contrast;
	private boolean colour64;
	private boolean showLines;
	private int colourHLevel;

	private static Logger log = Logging.getLogger(SimpleSpaceWidget1.class);

	public SimpleSpaceWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.SPACE);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		hPointsMap = new HashMap<>();
		colours = new ArrayList<>();
		colourMap = new HashMap<>();
		lineReferences = new HashSet<>();
		new HashMap<>(); // ???
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.widgetId = id;
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		log.info(meta.properties().toString());
		timeFormatter.onMetaDataMessage(meta);
		SpaceType type = (SpaceType) meta.properties().getPropertyValue(P_SPACETYPE.key());
		wrapAll = false;
		switch (type) {
		case continuousFlatSurface: {
			Interval xLimits = (Interval) meta.properties().getPropertyValue(P_SPACE_XLIM.key());
			Interval yLimits = (Interval) meta.properties().getPropertyValue(P_SPACE_YLIM.key());
			spaceBounds = new BoundingBox(xLimits.inf(), yLimits.inf(), xLimits.sup() - xLimits.inf(),
					yLimits.sup() - yLimits.inf());
			EdgeEffectCorrection ec = (EdgeEffectCorrection) meta.properties()
					.getPropertyValue(P_SPACE_EDGEEFFECTS.key());
			wrapAll = ec.equals(EdgeEffectCorrection.periodic);
			return;
		}
		case squareGrid: {
			Double cellSize = (Double) meta.properties().getPropertyValue(P_SPACE_CELLSIZE.key());
			int xnCells = (Integer) meta.properties().getPropertyValue(P_SPACE_NX.key());
			int ynCells = (Integer) meta.properties().getPropertyValue(P_SPACE_NY.key());
			spaceBounds = new BoundingBox(0, 0, cellSize * xnCells, cellSize * ynCells);
			return;
		}
		default: {
			// Eventually, the archetype should prevent this situation
			throw new TwuifxException(type + " not supported.");
		}
		}
	}

	@Override
	public void onDataMessage(SpaceData data) {
		if (policy.canProcessDataMessage(data)) {
			Platform.runLater(() -> {
				boolean refreshLegend = updateData(data);
				drawSpace();
				if (refreshLegend)
					updateLegend();
			});
		}
	}

	int step = 0;
	String sep = "\t";

	private boolean updateData(final SpaceData data) {
		boolean updateLegend = false;
		// add new points in the point list
		for (DataLabel lab : data.pointsToCreate().keySet()) {
			Duple<DataLabel, double[]> value = new Duple<>(lab, data.pointsToCreate().get(lab));
			// NB it would be an error if the lab was found in the list before
			if (hPointsMap.put(lab.toString(), value) != null)
				log.warning("Point to create " + lab + " already present in space widget list");
			updateLegend = updateLegend || installColour(lab);
		}
		// move points in the point list
		for (DataLabel lab : data.pointsToMove().keySet()) {
			Duple<DataLabel, double[]> value = new Duple<>(lab, data.pointsToMove().get(lab));
			// replaces previous value of coordinates for this lab - OK
			if (hPointsMap.put(lab.toString(), value) == null)
				log.warning("Point to move " + lab + " absent from space widget list");
			updateLegend = updateLegend || installColour(lab);
		}
		// delete points in the point list (including possibly new or moved ones of
		// previous lists
		for (DataLabel lab : data.pointsToDelete()) {
			hPointsMap.remove(lab.toString());
			updateLegend = updateLegend || uninstallColour(lab);
		}
		// Here, all point coordinates have been updated
		// add new lines
		lineReferences.addAll(data.linesToCreate());
		// remove lines
		lineReferences.removeAll(data.linesToDelete());
		Iterator<Duple<DataLabel, DataLabel>> it = lineReferences.iterator();
		Collection<DataLabel> deletedPoints = data.pointsToDelete();
		while (it.hasNext()) {
			Duple<DataLabel, DataLabel> line = it.next();
			if (deletedPoints.contains(line.getFirst()) || deletedPoints.contains(line.getSecond()))
				it.remove();
		}
		return updateLegend;
	}

	private boolean uninstallColour(DataLabel dl) {
		String cKey = getColourKey(dl);
		Duple<Integer, Color> value = colourMap.get(cKey);
		if (value != null) {
			if (value.getFirst() == 1) {
				colourMap.remove(cKey);
				return true;
			} else {
				value = new Duple<Integer, Color>(value.getFirst() - 1, value.getSecond());
				colourMap.put(cKey, value);
				return false;
			}
		}
		return false;
	}

	private boolean installColour(DataLabel dl) {
		boolean update = false;
		String cKey = getColourKey(dl);
		Duple<Integer, Color> value = colourMap.get(cKey);
		if (value == null) {
			value = new Duple<Integer, Color>(1, getColour(colourMap.size()));
			update = true;
		} else
			value = new Duple<Integer, Color>(value.getFirst() + 1, value.getSecond());
		colourMap.put(cKey, value);
		return update;
	}

	private String getColourKey(DataLabel dl) {
		String result = "";
		for (int i = 0; i < Math.min(colourHLevel + 1, dl.size()); i++)
			result += "." + dl.get(i);
		return result.replaceFirst(".", "");
	}

	@Override
	public void onStatusMessage(State state) {
		log.info(state.toString());
		if (isSimulatorState(state, waiting)) {
			hPointsMap.clear();
			lineReferences.clear();
			colourMap.clear();
			legend.getChildren().clear();
			drawSpace();
		}
	}

//-------------------------------------------- Drawing ---

	private void drawSpace() {
		int size = 2 * symbolRadius;
		resizeCanvas(spaceBounds.getWidth(), spaceBounds.getHeight());
		clearCanvas();
		GraphicsContext gc = canvas.getGraphicsContext2D();
		if (showLines) {
			gc.setStroke(lineColour);
			for (Duple<DataLabel, DataLabel> lineReference : lineReferences) {
				double[] start = hPointsMap.get(lineReference.getFirst().toString()).getSecond();
				double[] end = hPointsMap.get(lineReference.getSecond().toString()).getSecond();
				if (wrapAll)
					drawLines(gc, start, end);
				else {
					Point2D p1 = scaleToCanvas(start);
					Point2D p2 = scaleToCanvas(end);
					gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				}
			}
		}
		for (Map.Entry<String, Duple<DataLabel, double[]>> entry : hPointsMap.entrySet()) {
			Duple<DataLabel, double[]> value = entry.getValue();
			String cKey = getColourKey(value.getFirst());
			Color colour = colourMap.get(cKey).getSecond();
			gc.setStroke(colour);
			double[] coords = value.getSecond();
			Point2D point = scaleToCanvas(coords);
			point = point.add(-symbolRadius, -symbolRadius);
			gc.strokeOval(point.getX(), point.getY(), size, size);
			if (symbolFill) {
				gc.setFill(colour);
				gc.fillOval(point.getX(), point.getY(), size, size);
			}
		}
	}

	private void drawLines(GraphicsContext gc, double[] start, double[] end) {
		boolean xok = false;
		double x1 = Math.min(start[0], end[0]);
		double x2 = Math.max(start[0], end[0]);
		double y1 = Math.min(start[1], end[1]);
		double y2 = Math.max(start[1], end[1]);
		double[] left = {0.0,0.0};
		if(start[0]<end[0]) {
			left[0]=start[0];
			left[1]=start[1];
		}
		else {
			left[0]=end[0];
			left[1]=end[1];			
		}

		double newx = x1;
		double newy = y1;
		if ((x2 - x1) <= (x1 + spaceBounds.getMaxX() - x2)) {
			xok = true;
		} else {// project point
			newx = x1 + spaceBounds.getMaxX();
		}
		boolean yok = false;
		if ((y2 - y1) <= (y1 + spaceBounds.getMaxY() - y2)) {
			yok = true;
		} else {// project point
			newy = y1 + spaceBounds.getMaxY();
		}
		if (!xok || !yok) {
			//swap
			x1 = x2;
			y1 = y2;
			x2 = newx;
			y2 = newy;
			double m = (y2 - y1) / (x2 - x1);// NB: can be infinity
			double b = y1 - (m * x1);
			if (!xok && yok) {// exit right only
				double yx = (m * spaceBounds.getMaxX()) + b;
				drawALine(gc, x1, y1, spaceBounds.getMaxX(), yx);
				drawALine(gc, 0.0, left[1], left[0], left[1]);
			} else if (xok && !yok) {// exit top only
				double xx = (spaceBounds.getMaxY() - b) / m;// intercept
				if (Double.isNaN(xx))// vertical line
					xx = x1;
				drawALine(gc, x1, y1, xx, spaceBounds.getMaxY());
				drawALine(gc, xx, 0.0, left[0], left[1]);
			} else if (!xok && !yok) {
				double yx = (m * spaceBounds.getMaxX()) + b;// intercept
				double xx = (spaceBounds.getMaxY() - b) / m;// intercept
				if (xx == yx) {// out the corner
					drawALine(gc, x1, y1, spaceBounds.getMaxX(), spaceBounds.getMaxY());
					drawALine(gc, 0.0, 0.0, left[0], left[1]);
				} else if (yx < xx) {// out the RH side - may be incorrect in case of rectangle
					drawALine(gc, x1, y1, spaceBounds.getMaxX(), yx);
					drawALine(gc, 0.0, left[1], left[0], left[1]);
				} else { // out the top - may be incorrect in case of rectangle
					drawALine(gc, x1, y1, xx, spaceBounds.getMaxY());
					drawALine(gc, xx, left[1], left[0], left[1]);
				}
			}

		} else {// nothing to do - normal single line
			Point2D p1 = scaleToCanvas(start);
			Point2D p2 = scaleToCanvas(end);
			gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		}
	}

	private void drawALine(GraphicsContext gc, double x1, double y1, double x2, double y2) {
		Point2D start = scaleToCanvas(x1, y1);
		Point2D end = scaleToCanvas(x2, y2);
		gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
	}

	private void resizeCanvas(double sWidth, double sHeight) {
		int newWidth = (int) Math.round(resolution * sWidth);
		int newHeight = (int) Math.round(resolution * sHeight);
		if (canvas.getWidth() != newWidth || canvas.getHeight() != newHeight) {
			canvas.setWidth(newWidth);
			canvas.setHeight(newHeight);
		}
	}

	private void clearCanvas() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(bkgColour);
		gc.setStroke(Color.BLACK);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	};

	private Color getColour(int idx) {
		return colours.get(idx % colours.size());
	}

	private double rescale(double value, double fMin, double fMax, double tMin, double tMax) {
		double fr = fMax - fMin;
		double tr = tMax - tMin;
		double p = (value - fMin) / fr;
		return p * tr + tMin;
	}

	private Point2D scaleToCanvas(double... coords) {
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
	private static final String keyResolution = "resolution";
	private static final String keySymbolRad = "radius";
	private static final String keySymbolFill = "fill";
	private static final String keyBKG = "bkg";
	private static final String keyLineColour = "lineColour";
	private static final String keyContrast = "contrast";
	private static final String keyColour64 = "colour64";
	private static final String keyColourHLevel = "colourHLevel";
	private static final String keyShowLines = "showLines";
//	private static final String keyUseModPath = "useModPath";

	@Override
	public void putUserPreferences() {
		Preferences.putDouble(widgetId + keyScaleX, zoomTarget.getScaleX());
		Preferences.putDouble(widgetId + keyScaleY, zoomTarget.getScaleY());
		Preferences.putDouble(widgetId + keyScrollH, scrollPane.getHvalue());
		Preferences.putDouble(widgetId + keyScrollV, scrollPane.getVvalue());
		Preferences.putInt(widgetId + keyResolution, resolution);
		Preferences.putInt(widgetId + keyColourHLevel, colourHLevel);
		Preferences.putInt(widgetId + keySymbolRad, symbolRadius);
		Preferences.putBoolean(widgetId + keySymbolFill, symbolFill);
		Preferences.putDoubles(widgetId + keyBKG, bkgColour.getRed(), bkgColour.getGreen(), bkgColour.getBlue());
		Preferences.putDoubles(widgetId + keyLineColour, lineColour.getRed(), lineColour.getGreen(),
				lineColour.getBlue());
		Preferences.putDouble(widgetId + keyContrast, contrast);
		Preferences.putBoolean(widgetId + keyColour64, colour64);
		Preferences.putBoolean(widgetId + keyShowLines, showLines);
//		Preferences.putBoolean(widgetId+keyUseModPath, useModPath);
	}

	private static final int firstUse = -1;

	@Override
	public void getUserPreferences() {
		zoomTarget.setScaleX(Preferences.getDouble(widgetId + keyScaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(widgetId + keyScaleY, zoomTarget.getScaleY()));
		scrollPane.setHvalue(Preferences.getDouble(widgetId + keyScrollH, scrollPane.getHvalue()));
		scrollPane.setVvalue(Preferences.getDouble(widgetId + keyScrollV, scrollPane.getVvalue()));
		resolution = Preferences.getInt(widgetId + keyResolution, 50);
		colourHLevel = Preferences.getInt(widgetId + keyColourHLevel, 0);
		symbolRadius = Preferences.getInt(widgetId + keySymbolRad, firstUse);
		if (symbolRadius == firstUse) {
			// onMetadata has run therefore spaceBounds is valid
			double s = Math.max(spaceBounds.getWidth(), spaceBounds.getHeight());
			// assume a nominal canvas size of 200
			resolution = Math.max(1, (int) (200.0 / s));
			symbolRadius = 2;
		}
		symbolFill = Preferences.getBoolean(widgetId + keySymbolFill, true);
		double[] rgb;
		rgb = Preferences.getDoubles(widgetId + keyBKG, Color.WHITE.getRed(), Color.WHITE.getGreen(),
				Color.WHITE.getBlue());
		bkgColour = new Color(rgb[0], rgb[1], rgb[2], 1.0);

		rgb = Preferences.getDoubles(widgetId + keyLineColour, Color.GREY.getRed(), Color.GREY.getGreen(),
				Color.GREY.getBlue());
		lineColour = new Color(rgb[0], rgb[1], rgb[2], 1.0);

		contrast = Preferences.getDouble(widgetId + keyContrast, 0.2);
		colour64 = Preferences.getBoolean(widgetId + keyColour64, true);
		showLines = Preferences.getBoolean(widgetId + keyShowLines, true);
		if (colour64)
			colours = ColourContrast.getContrastingColours64(bkgColour, contrast);
		else
			colours = ColourContrast.getContrastingColours(bkgColour, contrast);

//		useModPath = Preferences.getBoolean(widgetId+keyUseModPath, true);

	}

	// --------------- GUI

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane container = new BorderPane();
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(2);
		dropShadow.setOffsetY(2);
		dropShadow.setHeight(1);
		dropShadow.setRadius(12.0);
		zoomTarget = new AnchorPane();
		canvas = new Canvas();
		canvas.setEffect(dropShadow);
		canvas.setOnMouseClicked(e -> onMouseClicked(e));
		zoomTarget.getChildren().add(canvas);
		Group group = new Group(zoomTarget);
		StackPane content = new StackPane(group);
		scrollPane = new ScrollPane(content);
		scrollPane.setPannable(true);
		scrollPane.setContent(content);
		scrollPane.setMinSize(170, 170);
		CenteredZooming.center(scrollPane, content, group, zoomTarget);
		container.setCenter(scrollPane);
		HBox bottom = new HBox();
		bottom.setSpacing(5);
		lblItem = new Label("");
		lblTime = new Label("");

		bottom.getChildren().addAll(lblTime, new Label("System: "), lblItem);
		container.setBottom(bottom);

		legend = new GridPane();
		legend.setHgap(3);

		container.setRight(legend);

		getUserPreferences();

		return container;
	}

	private void updateLegend() {
		legend.getChildren().clear();
		int count = 0;
		int maxItems = 20;
		for (Map.Entry<String, Duple<Integer, Color>> entry : colourMap.entrySet()) {
			count++;
			if (count < maxItems)
				addLegendItem(entry.getKey(), entry.getValue().getSecond());
		}
		if (count > maxItems) {
			int idx = legend.getChildren().size();
			legend.add(new Label("..."), 1, idx);
		}
	}

	private void addLegendItem(String name, Color colour) {
		Rectangle rect = new Rectangle();
		rect.setStroke(bkgColour);
		rect.setFill(bkgColour);
		rect.setX(0);
		rect.setY(0);
		rect.setWidth(14);
		rect.setHeight(14);

		int idx = legend.getChildren().size();
		Circle circle = null;
		if (!symbolFill)
			circle = new Circle(0, 0, 4, Color.TRANSPARENT);
		else
			circle = new Circle(0, 0, 4, colour);
		circle.setStroke(colour);
		legend.add(rect, 0, idx);
		legend.add(circle, 0, idx);
		GridPane.setHalignment(circle, HPos.CENTER);
		legend.add(new Label(name), 1, idx);
	}

	@Override
	public Object getMenuContainer() {
		Menu mu = new Menu(widgetId);
		MenuItem miEdit = new MenuItem("Edit...");
		mu.getItems().add(miEdit);
		miEdit.setOnAction(e -> edit());
		return mu;
	}

	private void addTableEntry(String name, int row, Node ctrl, GridPane grid) {
		Label lbl = new Label(name);
		grid.add(lbl, 0, row);
		grid.add(ctrl, 1, row);
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
		content.setAlignment(Pos.BASELINE_RIGHT); // try removing this line

		int row = 0;

		// -----
		CheckBox chbxFill = new CheckBox("");
		addTableEntry("Fill symbols", row++, chbxFill, content);
		chbxFill.setSelected(symbolFill);
		// -----
		CheckBox chbxShowLines = new CheckBox("");
		addTableEntry("Show lines", row++, chbxShowLines, content);
		chbxShowLines.setSelected(showLines);
		// -----
//		CheckBox chbxUseModPath = new CheckBox("");
//		addTableEntry("Show lines", 2, chbxUseModPath, content);
//		chbxUseModPath.setSelected(useModPath);

		// -----
		Spinner<Integer> spResolution = new Spinner<>();
		addTableEntry("Resolution", row++, spResolution, content);
		spResolution.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, resolution));
		spResolution.setMaxWidth(100);
		spResolution.setEditable(true);

		// -----
		Spinner<Integer> spRadius = new Spinner<>();
		addTableEntry("Symbol radius", row++, spRadius, content);
		spRadius.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, symbolRadius));
		spRadius.setMaxWidth(100);
		spRadius.setEditable(true);

		// ----
		Spinner<Integer> spHLevel = new Spinner<>();
		addTableEntry("Hierarchical colour level", row++, spHLevel, content);
		spHLevel.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, colourHLevel));
		spHLevel.setMaxWidth(100);
		spHLevel.setEditable(true);
		// -----
		CheckBox chbxCS = new CheckBox("");
		addTableEntry("64 Colour system", row++, chbxCS, content);
		chbxCS.setSelected(colour64);
		// ----
		ColorPicker cpBkg = new ColorPicker(bkgColour);
		addTableEntry("Background colour", row++, cpBkg, content);
		GridPane.setValignment(cpBkg, VPos.TOP);
		// -----
		ColorPicker cpLine = new ColorPicker(lineColour);
		addTableEntry("Line colour", row++, cpLine, content);
		GridPane.setValignment(cpLine, VPos.TOP);
		// ----
		TextField tfContrast = new TextField(Double.toString(contrast));
		addTableEntry("Contrast (0.0-1.0)", row++, tfContrast, content);

		dialog.getDialogPane().setContent(content);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			showLines = chbxShowLines.isSelected();
			symbolFill = chbxFill.isSelected();
//			useModPath = chbxUseModPath.isSelected();
			resolution = spResolution.getValue();
			symbolRadius = spRadius.getValue();
			contrast = Double.parseDouble(tfContrast.getText());
			colour64 = chbxCS.isSelected();
			bkgColour = cpBkg.getValue();
			lineColour = cpLine.getValue();
			colourHLevel = spHLevel.getValue();
			if (colour64)
				colours = ColourContrast.getContrastingColours64(bkgColour, contrast);
			else
				colours = ColourContrast.getContrastingColours(bkgColour, contrast);

			colourMap.clear();

			hPointsMap.forEach((k, v) -> {
				installColour(v.getFirst());

			});
			drawSpace();
			updateLegend();
		}
	}

	private void onMouseClicked(MouseEvent e) {
		String name = findName(e);
		lblItem.setText(name);
	}

	private String findName(MouseEvent e) {
		double scale = 1.0 / (double) resolution;
		double size = (symbolRadius * 2) * scale;
		double rad = symbolRadius * scale;
		double clickX = (e.getX() * scale) + spaceBounds.getMinX();
		double clickY = ((canvas.getHeight() - e.getY()) * scale) + spaceBounds.getMinY();
		BoundingBox box = new BoundingBox(clickX - rad, clickY - rad, size, size);
		for (Map.Entry<String, Duple<DataLabel, double[]>> entry : hPointsMap.entrySet()) {
			String key = entry.getKey();
			Duple<DataLabel, double[]> value = entry.getValue();
			double x = value.getSecond()[0];
			double y = value.getSecond()[1];
			if (box.contains(x, y)) {
				return key + " [" + x + "," + y + "]";
			}
		}
		return "";
	}

}
