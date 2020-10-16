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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimerTask;

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
import fr.cnrs.iees.twcore.constants.BorderListType;
import fr.cnrs.iees.twcore.constants.BorderType;
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
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Window;
import java.util.logging.Logger;

import java.util.Timer;

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
public class SimpleSpatial2DWidget1 extends AbstractDisplayWidget<SpaceData, Metadata> implements WidgetGUI {
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

	private double spaceCanvasRatio;
	private int symbolRadius;
	private boolean symbolFill;
	private Color bkgColour;
	private Color lineColour;
	private double contrast;
	private boolean colour64;
	private boolean showLines;
	private boolean showGrid;
	private boolean showEdgeEffect;
	private int colourHLevel;
	private EdgeEffectCorrection eec;
	private double tickWidth;
	private double relLineWidth;

	private BorderListType borderList;

	private static Logger log = Logging.getLogger(SimpleSpatial2DWidget1.class);

	public SimpleSpatial2DWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.SPACE);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		hPointsMap = new HashMap<>();
		colours = new ArrayList<>();
		colourMap = new HashMap<>();
		lineReferences = new HashSet<>();
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

		switch (type) {
		case continuousFlatSurface: {
			Interval xLimits = (Interval) meta.properties().getPropertyValue(P_SPACE_XLIM.key());
			Interval yLimits = (Interval) meta.properties().getPropertyValue(P_SPACE_YLIM.key());
			spaceBounds = new BoundingBox(xLimits.inf(), yLimits.inf(), xLimits.sup() - xLimits.inf(),
					yLimits.sup() - yLimits.inf());
			borderList = (BorderListType) meta.properties().getPropertyValue(P_SPACE_BORDERTYPE.key());
			eec = BorderListType.getEdgeEffectCorrection(borderList);
			tickWidth = getTickWidth();
			return;
		}
		case squareGrid: {
			Double cellSize = (Double) meta.properties().getPropertyValue(P_SPACE_CELLSIZE.key());
			int xnCells = (Integer) meta.properties().getPropertyValue(P_SPACE_NX.key());
			int ynCells = (Integer) meta.properties().getPropertyValue(P_SPACE_NY.key());
			spaceBounds = new BoundingBox(0, 0, cellSize * xnCells, cellSize * ynCells);
			borderList = (BorderListType) meta.properties().getPropertyValue(P_SPACE_BORDERTYPE.key());
			eec = BorderListType.getEdgeEffectCorrection(borderList);
			tickWidth = getTickWidth();
			return;
		}
		default: {
			// Eventually, the archetype should prevent this situation
			throw new TwuifxException(type + " not supported.");
		}
		}
	}

	private double getTickWidth() {
		double mxDim = Math.max(spaceBounds.getWidth(), spaceBounds.getHeight());
		double exp = Math.round(Math.log10(mxDim));
		double mx = Math.pow(10, exp);
		return mx / 10;
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

	private boolean updateData(final SpaceData data) {
		boolean updateLegend = false;
		// delete points in the point list
		for (DataLabel lab : data.pointsToDelete()) {
			hPointsMap.remove(lab.toString());
			updateLegend = updateLegend || uninstallColour(lab);
		}
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
		// add new lines to create
		if (!data.linesToCreate().isEmpty())
			lineReferences.addAll(data.linesToCreate());
		// remove lines
		if (!data.linesToDelete().isEmpty())
			lineReferences.removeAll(data.linesToDelete());
		// remove old lines which end points were just removed
		Iterator<Duple<DataLabel, DataLabel>> itline = lineReferences.iterator();
		while (itline.hasNext()) {
			Duple<DataLabel, DataLabel> line = itline.next();
			if (!hPointsMap.containsKey(line.getFirst().toString())
					|| !hPointsMap.containsKey(line.getSecond().toString()))
				itline.remove();
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
		GraphicsContext gc = canvas.getGraphicsContext2D();
		resizeCanvas(spaceBounds.getWidth(), spaceBounds.getHeight());
		clearCanvas(gc);

		gc.setLineWidth(relLineWidth);
		gc.setLineDashes(0);

		if (showLines) {
			gc.setStroke(lineColour);
			for (Duple<DataLabel, DataLabel> lineReference : lineReferences) {
				String sKey = lineReference.getFirst().toString();
				String eKey = lineReference.getSecond().toString();
				Duple<DataLabel, double[]> sEntry = hPointsMap.get(sKey);
				Duple<DataLabel, double[]> eEntry = hPointsMap.get(eKey);
				if (sEntry == null)
					throw new TwuifxException("Line error. Start point not found " + sKey);
				if (eEntry == null)
					throw new TwuifxException("Line error. End point not found " + eKey);

				double[] start = sEntry.getSecond();
				double[] end = eEntry.getSecond();
				if (eec == null) {
					drawALine(gc, start[0], start[1], end[0], end[1]);
				} else if (eec.equals(EdgeEffectCorrection.periodic))
					drawPeriodicLines(gc, start, end);
				else if (eec.equals(EdgeEffectCorrection.tubular)) {
					// assume first dim means 'x'
					drawTubularLines(gc, start, end);
				} else {
					drawALine(gc, start[0], start[1], end[0], end[1]);
				}
			}
		}

		int size = 2 * symbolRadius;
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

	private void drawTubularLines(GraphicsContext gc, double[] p1, double[] p2) {
		double[] left = p1;
		double[] right = p2;
		if (p1[0] > p2[0]) {
			left = p2;
			right = p1;
		}
		double newx = left[0];
		boolean xok = false;
		if ((right[0] - left[0]) <= (left[0] + spaceBounds.getMaxX() - right[0])) {
			xok = true;
		} else
			newx += spaceBounds.getMaxX();

		if (!xok) {
			double m = (right[1] - left[1]) / (newx - right[0]);
			double b = right[1] - (m * right[0]);
			double yintercept = (m * spaceBounds.getMaxX()) + b;
			drawALine(gc, right[0], right[1], spaceBounds.getMaxX(), yintercept);
			drawALine(gc, 0.0, yintercept, left[0], left[1]);
		} else {
			drawALine(gc, p1[0], p1[1], p2[0], p2[1]);
		}
	}

	private void drawPeriodicLines(GraphicsContext gc, double[] p1, double[] p2) {
		double[] left = p1;
		double[] right = p2;
		if (p1[0] > p2[0]) {
			left = p2;
			right = p1;
		}
		double newx = left[0];
		boolean xok = false;
		if ((right[0] - left[0]) <= (left[0] + spaceBounds.getMaxX() - right[0])) {
			xok = true;
		} else
			newx += spaceBounds.getMaxX();
		double newy = left[1];
		boolean yok = false;
		if (Math.abs(left[1] - right[1]) <= (spaceBounds.getMaxY() / 2.0)) {
			yok = true;
		} else if (left[1] <= right[1])
			newy += spaceBounds.getMaxY();
		else
			newy -= spaceBounds.getMaxY();
		if (!xok || !yok) {
			int quad = getQuad(newx, newy);
			double m = (newy - right[1]) / (newx - right[0]);
			double b = right[1] - (m * right[0]);

			if (quad == 3) {// right
				double yintercept = (m * spaceBounds.getMaxX()) + b;
				drawALine(gc, right[0], right[1], spaceBounds.getMaxX(), yintercept);
				drawALine(gc, 0.0, yintercept, left[0], left[1]);
			} else if (quad == 1) {// top
				double xintercept = (spaceBounds.getMaxY() - b) / m;
				if (Double.isNaN(xintercept)) // vertical line
					xintercept = left[0];
				if (Double.isInfinite(m)) {// exactly vertical
					drawALine(gc, left[0], Math.max(right[1], left[1]), left[0], spaceBounds.getMaxY());
					drawALine(gc, left[0], 0.0, left[0], Math.min(right[1], left[1]));
				} else if (m < 0) {// slope to the left
					drawALine(gc, right[0], right[1], xintercept, spaceBounds.getMaxY());
					drawALine(gc, xintercept, 0.0, left[0], left[1]);
				} else if (m > 0) {// slope to the right
					drawALine(gc, left[0], left[1], xintercept, spaceBounds.getMaxY());
					drawALine(gc, xintercept, 0.0, right[0], right[1]);
				}

			} else if (quad == 5) { // bottom
				double xintercept = -b / m;
				if (Double.isNaN(xintercept))
					xintercept = left[0];
				double[] low = left;
				double[] high = right;
				if (low[1] > high[1]) {
					low = right;
					high = left;
				}
				drawALine(gc, low[0], low[1], xintercept, 0.0);
				drawALine(gc, xintercept, spaceBounds.getMaxY(), high[0], high[1]);
			} else if (quad == 2) { // top right
				double yintercept = (m * spaceBounds.getMaxX()) + b;
				double xintercept = (spaceBounds.getMaxY() - b) / m;
				double xd2 = getD2(right[0], right[1], xintercept, spaceBounds.getMaxY());
				double yd2 = getD2(right[0], right[1], spaceBounds.getMaxX(), yintercept);
				if (xd2 < yd2) { // cross the x-axis first
					drawALine(gc, right[0], right[1], xintercept, spaceBounds.getMaxY());
					drawALine(gc, xintercept, 0.0, spaceBounds.getMaxX(), (yintercept - spaceBounds.getMaxY()));
					drawALine(gc, 0.0, (yintercept - spaceBounds.getMaxY()), left[0], left[1]);
				} else { // cross at y-axis first
					drawALine(gc, 0.0, yintercept, (xintercept - spaceBounds.getMaxX()), spaceBounds.getMaxY());
					drawALine(gc, 0.0, yintercept, (xintercept - spaceBounds.getMaxX()), spaceBounds.getMaxY());
					drawALine(gc, (xintercept - spaceBounds.getMaxX()), 0.0, left[0], left[1]);
				}
			}
		} else {
			drawALine(gc, p1[0], p1[1], p2[0], p2[1]);
		}
	}

	private static double getD2(double x1, double y1, double x2, double y2) {
		return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
	}

	private int getQuad(double x, double y) {
		if (x <= spaceBounds.getMaxX()) {// 1 or 5
			if (y > spaceBounds.getMaxY())
				return 1;
			else if (y < 0.0)
				return 5;

		} else {// 2,3 or 4
			if (y > spaceBounds.getMaxY())
				return 2;
			else if (y < 0.0)
				return 4;
			else
				return 3;

		}
		return -1;
	}

	private void drawALine(GraphicsContext gc, double x1, double y1, double x2, double y2) {
		Point2D start = scaleToCanvas(x1, y1);
		Point2D end = scaleToCanvas(x2, y2);
		gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
	}

	private void resizeCanvas(double sWidth, double sHeight) {
		int newWidth = (int) Math.round(spaceCanvasRatio * sWidth);
		int newHeight = (int) Math.round(spaceCanvasRatio * sHeight);
		if (canvas.getWidth() != newWidth || canvas.getHeight() != newHeight) {
			canvas.setWidth(newWidth);
			canvas.setHeight(newHeight);
		}
	}

	private void clearCanvas(GraphicsContext gc) {
		gc.setFill(bkgColour);
		gc.setStroke(bkgColour);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		double w = canvas.getWidth();
		double h = canvas.getHeight();
		double maxDim = Math.max(spaceBounds.getWidth(), spaceBounds.getHeight());
		double maxSize = Math.max(w, h);
		double scale = maxSize / maxDim;
		double d = scale * tickWidth;
		double lineWidth = d / 100.0;
		double dashes = d / 10.0;
		int nVLines = (int) Math.round(w / d);
		int nHLines = (int) Math.round(h / d);
		if (showGrid) {
			gc.setStroke(Color.GREY);
			gc.setLineDashes(dashes);
			gc.setLineWidth(lineWidth);
			for (int i = 0; i < nHLines; i++) {
				double y = i * d;
				gc.strokeLine(0, y, w, y);
			}
			for (int i = 0; i < nVLines; i++) {
				double x = i * d;
				gc.strokeLine(x, 0, x, h);
			}
		}
		if (showEdgeEffect) {
			double lws = lineWidth * 10;// line width scaling
			drawBorder(gc, BorderType.valueOf(borderList.getWithFlatIndex(0)), 1, 0, 1, h, lws, dashes);
			drawBorder(gc, BorderType.valueOf(borderList.getWithFlatIndex(1)), w, 0, w, h, lws, dashes);
			drawBorder(gc, BorderType.valueOf(borderList.getWithFlatIndex(2)), 0, h, w, h, lws, dashes);
			drawBorder(gc, BorderType.valueOf(borderList.getWithFlatIndex(3)), 0, 1, w, 1, lws, dashes);
		}
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
	private static final String keySpaceCanvasRatio = "spaceCanvasRatio";
	private static final String keySymbolRad = "radius";
	private static final String keySymbolFill = "fill";
	private static final String keyBKG = "bkg";
	private static final String keyLineColour = "lineColour";
	private static final String keyContrast = "contrast";
	private static final String keyColour64 = "colour64";
	private static final String keyColourHLevel = "colourHLevel";
	private static final String keyShowLines = "showLines";
	private static final String keyShowGrid = "showGrid";
	private static final String keyShowEdgeEffect = "showEdgeEffect";
	private static final String keyRelLineWidth = "relationLineWidth";

	@Override
	public void putUserPreferences() {
		Preferences.putDouble(widgetId + keyRelLineWidth, relLineWidth);
		Preferences.putDouble(widgetId + keyScaleX, zoomTarget.getScaleX());
		Preferences.putDouble(widgetId + keyScaleY, zoomTarget.getScaleY());
		Preferences.putDouble(widgetId + keyScrollH, scrollPane.getHvalue());
		Preferences.putDouble(widgetId + keyScrollV, scrollPane.getVvalue());
		Preferences.putDouble(widgetId + keySpaceCanvasRatio, spaceCanvasRatio);
		Preferences.putInt(widgetId + keyColourHLevel, colourHLevel);
		Preferences.putInt(widgetId + keySymbolRad, symbolRadius);
		Preferences.putBoolean(widgetId + keySymbolFill, symbolFill);
		Preferences.putBoolean(widgetId + keyShowGrid, showGrid);
		Preferences.putBoolean(widgetId + keyShowEdgeEffect, showEdgeEffect);
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
		relLineWidth = Preferences.getDouble(widgetId + keyRelLineWidth, 0.25);
		zoomTarget.setScaleX(Preferences.getDouble(widgetId + keyScaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(widgetId + keyScaleY, zoomTarget.getScaleY()));
		scrollPane.setHvalue(Preferences.getDouble(widgetId + keyScrollH, scrollPane.getHvalue()));
		scrollPane.setVvalue(Preferences.getDouble(widgetId + keyScrollV, scrollPane.getVvalue()));
		spaceCanvasRatio = Preferences.getDouble(widgetId + keySpaceCanvasRatio, 1.0);
		colourHLevel = Preferences.getInt(widgetId + keyColourHLevel, 0);
		symbolRadius = Preferences.getInt(widgetId + keySymbolRad, firstUse);
		if (symbolRadius == firstUse) {
			// onMetadata has run therefore spaceBounds is valid
			double s = Math.max(spaceBounds.getWidth(), spaceBounds.getHeight());
			// assume a nominal canvas size of 500
			spaceCanvasRatio = 500.0 / s;
			symbolRadius = 2;
		}
		symbolFill = Preferences.getBoolean(widgetId + keySymbolFill, true);
		showGrid = Preferences.getBoolean(widgetId + keyShowGrid, true);
		showEdgeEffect = Preferences.getBoolean(widgetId + keyShowEdgeEffect, true);
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

	}

	// --------------- GUI

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane container = new BorderPane();
//		DropShadow dropShadow = new DropShadow();
//		dropShadow.setOffsetX(2);
//		dropShadow.setOffsetY(2);
//		dropShadow.setHeight(1);
//		dropShadow.setRadius(12.0);
//		dropShadow.setColor(Color.BLUE);
		zoomTarget = new AnchorPane();
		canvas = new Canvas();
//		canvas.setEffect(dropShadow);
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
		CheckBox chbxShowGrid = new CheckBox("");
		addTableEntry("Show grid", row++, chbxShowGrid, content);
		chbxShowGrid.setSelected(showGrid);
		// -----
		CheckBox chbxShowEdgeEffect = new CheckBox("");
		addTableEntry("Show boundary type", row++, chbxShowEdgeEffect, content);
		chbxShowEdgeEffect.setSelected(showEdgeEffect);
		// -----
		TextField tfSpaceCanvasRatio = new TextField(Double.toString(spaceCanvasRatio));
		tfSpaceCanvasRatio.setTextFormatter(
				new TextFormatter<>(change -> (change.getControlNewText().matches(Dialogs.vsReal) ? change : null)));
		tfSpaceCanvasRatio.setMaxWidth(50);
		addTableEntry("Canvas:Space ratio", row++, tfSpaceCanvasRatio, content);
		// -----
		TextField tfRelLineWidth = new TextField(Double.toString(relLineWidth));
		tfRelLineWidth.setTextFormatter(
				new TextFormatter<>(change -> (change.getControlNewText().matches(Dialogs.vsReal) ? change : null)));
		tfRelLineWidth.setMaxWidth(50);
		addTableEntry("Relation line width", row++, tfRelLineWidth, content);

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
		tfContrast.setTextFormatter(
				new TextFormatter<>(change -> (change.getControlNewText().matches(Dialogs.vsReal) ? change : null)));
		addTableEntry("Contrast (0.0-1.0)", row++, tfContrast, content);

		dialog.getDialogPane().setContent(content);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			showLines = chbxShowLines.isSelected();
			symbolFill = chbxFill.isSelected();
			showGrid = chbxShowGrid.isSelected();
			showEdgeEffect = chbxShowEdgeEffect.isSelected();
			spaceCanvasRatio = Double.parseDouble(tfSpaceCanvasRatio.getText());
			relLineWidth = Double.parseDouble(tfRelLineWidth.getText());
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
		double scale = 1.0 / (double) spaceCanvasRatio;
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

}
