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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import fr.cnrs.iees.twcore.constants.SpaceType;
import fr.ens.biologie.generic.utils.Interval;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.scene.Group;
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
	private Map<String, Map<String, double[]>> items;
	private List<Color> colours;
	private final Map<String, Color> itemColours;
	private GridPane legend;

	private int resolution;
	private int symbolRadius;
	private boolean symbolFill;
	private Color bkg;
	private double contrast;
	private boolean colour64;

	private static Logger log = Logging.getLogger(SimpleSpaceWidget1.class);

	public SimpleSpaceWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.SPACE);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		items = new HashMap<>();
		colours = new ArrayList<>();
		itemColours = new HashMap<>();
		new HashMap<>();
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

	private long lastTime = Long.MIN_VALUE;
	private List<SpaceData> batchData = new ArrayList<>();

	@Override
	public void onDataMessage(SpaceData data) {
		if (policy.canProcessDataMessage(data)) {
			Platform.runLater(() -> {
				if (data.time() == lastTime) {
					batchData.add(data);
				} else {
					boolean doDraw = false;
					for (SpaceData d : batchData)
						doDraw = doDraw || updateData(d);
					drawSpace(doDraw);
					batchData.clear();
					batchData.add(data);
					lastTime = data.time();
				}
			});
		}
	}

	int step = 0;
	String sep = "\t";

	private boolean updateData(final SpaceData data) {
		boolean updateLegend = false;
		lblTime.setText(timeFormatter.getTimeText(data.time()));
		lblItem.setText("");
		if (data.create()) {
			if (data.isPoint()) {
				DataLabel dl = data.itemLabel();
				String name = dl.getEnd();
				String key = dl.toString().replace(">" + name, "").replace(">", ".");
				Map<String, double[]> value = items.get(key);
				if (value == null) {
					value = new HashMap<>();
					updateLegend = true;
				}
				items.put(key, value);
				value.put(name, data.coordinates());
				// Assign a colour to new items?
				if (!itemColours.containsKey(key)) {
					itemColours.put(key, getColour(items.size() - 1));
				}
			} else {// lines/relations
				log.warning("Adding relations not yet implemented.");
				// wait and see
			}

		} else if (data.delete()) {
			DataLabel dl = data.itemLabel();
			String name = dl.getEnd();
			String key = dl.toString().replace(">" + name, "").replace(">", ".");
			Map<String, double[]> value = items.get(key);
			if (value != null) { // JG sometimes this happens, although it shouldnt...
				if (value.containsKey(name))
					value.remove(name);
				else
					log.warning("Request to delete non-existent name [" + name + "] in system [" + key + "] " + data);
			} else
				log.warning("Request to delete name [" + name + "] in non-existent system [" + key + "] " + data);
			// Don't remove empty system entries as new entries will acquire the same name
		} else
			log.warning("Request for unknown op: " + data);
		// relocate i.e move something - wait and see

		return updateLegend;

	}

	@Override
	public void onStatusMessage(State state) {
		log.info(state.toString());
		if (isSimulatorState(state, waiting)) {
			items.clear();
			itemColours.clear();
			drawSpace(true);
		}
	}

//-------------------------------------------- Drawing ---

	private void drawSpace(boolean updateLegend) {
		if (updateLegend)
			legend.getChildren().clear();
		int size = 2 * symbolRadius;
		GraphicsContext gc = canvas.getGraphicsContext2D();
		resizeCanvas(spaceBounds.getWidth(), spaceBounds.getHeight());
		clearCanvas();
		for (Map.Entry<String, Map<String, double[]>> entry : items.entrySet()) {
			String key = entry.getKey();
			Map<String, double[]> members = entry.getValue();
			Color colour = itemColours.get(key);
			if (updateLegend)
				addLegendItem(key, colour);
			gc.setStroke(colour);
			gc.setFill(colour);
			members.forEach((name, coords) -> {
				Point2D point = getPoint(coords);
				point = point.add(-symbolRadius, -symbolRadius);
				gc.strokeOval(point.getX(), point.getY(), size, size);
				if (symbolFill)
					gc.fillOval(point.getX(), point.getY(), size, size);
			});
		}
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
		gc.setFill(bkg);
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

	private Point2D getPoint(double[] coords) {
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
	private static final String keyContrast = "contrast";
	private static final String keyColour64 = "colour64";

	@Override
	public void putUserPreferences() {
		Preferences.putDouble(widgetId + keyScaleX, zoomTarget.getScaleX());
		Preferences.putDouble(widgetId + keyScaleY, zoomTarget.getScaleY());
		Preferences.putDouble(widgetId + keyScrollH, scrollPane.getHvalue());
		Preferences.putDouble(widgetId + keyScrollV, scrollPane.getVvalue());
		Preferences.putInt(widgetId + keyResolution, resolution);
		Preferences.putInt(widgetId + keySymbolRad, symbolRadius);
		Preferences.putBoolean(widgetId + keySymbolFill, symbolFill);
		Preferences.putDoubles(widgetId + keyBKG, bkg.getRed(), bkg.getGreen(), bkg.getBlue());
		Preferences.putDouble(widgetId + keyContrast, contrast);
		Preferences.putBoolean(widgetId + keyColour64, colour64);
	}

	private static final int firstUse = -1;

	@Override
	public void getUserPreferences() {
		zoomTarget.setScaleX(Preferences.getDouble(widgetId + keyScaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(widgetId + keyScaleY, zoomTarget.getScaleY()));
		scrollPane.setHvalue(Preferences.getDouble(widgetId + keyScrollH, scrollPane.getHvalue()));
		scrollPane.setVvalue(Preferences.getDouble(widgetId + keyScrollV, scrollPane.getVvalue()));
		resolution = Preferences.getInt(widgetId + keyResolution, 50);
		symbolRadius = Preferences.getInt(widgetId + keySymbolRad, firstUse);
		if (symbolRadius == firstUse) {
			// onMeatData has run therefore spaceBounds is valid
			double s = Math.max(spaceBounds.getWidth(), spaceBounds.getHeight());
			// assume a nominal canvas size of 200
			resolution = Math.max(1, (int) (200.0 / s));
			symbolRadius = 2;
		}
		symbolFill = Preferences.getBoolean(widgetId + keySymbolFill, true);
		double[] rgb = Preferences.getDoubles(widgetId + keyBKG, Color.WHITE.getRed(), Color.WHITE.getGreen(),
				Color.WHITE.getBlue());
		bkg = new Color(rgb[0], rgb[1], rgb[2], 1.0);
		contrast = Preferences.getDouble(widgetId + keyContrast, 0.2);
		colour64 = Preferences.getBoolean(widgetId + keyColour64, true);
		if (colour64)
			colours = ColourContrast.getContrastingColours64(bkg, contrast);
		else
			colours = ColourContrast.getContrastingColours(bkg, contrast);

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

	private void addLegendItem(String name, Color colour) {
		Rectangle rect = new Rectangle();
		rect.setStroke(bkg);
		rect.setFill(bkg);
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

	private void edit() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle(widgetId);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		dialog.initOwner((Window) Dialogs.owner());
		GridPane content = new GridPane();
		content.setVgap(5);
		content.setHgap(3);
		// -----
		Label lbl = new Label("Fill symbols");
		CheckBox chbxFill = new CheckBox("");
		chbxFill.setSelected(symbolFill);
		// col, row
		GridPane.setHalignment(lbl, HPos.RIGHT);
		content.add(lbl, 0, 0);
		content.add(chbxFill, 1, 0);

		// -----
		Spinner<Integer> spResolution = new Spinner<>();
		spResolution.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, resolution));
		spResolution.setMaxWidth(100);
		spResolution.setEditable(true);
		lbl = new Label("Resolution");
		GridPane.setHalignment(lbl, HPos.RIGHT);
		content.add(lbl, 0, 1);
		content.add(spResolution, 1, 1);

		// -----
		Spinner<Integer> spRadius = new Spinner<>();
		spRadius.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, symbolRadius));
		spRadius.setMaxWidth(100);
		spRadius.setEditable(true);
		lbl = new Label("Symbol radius");
		GridPane.setHalignment(lbl, HPos.RIGHT);
		content.add(lbl, 0, 2);
		content.add(spRadius, 1, 2);

		// -----
		Label lbl2 = new Label("64 Colour system");
		CheckBox chbxCS = new CheckBox("");
		chbxCS.setSelected(colour64);
		GridPane.setHalignment(lbl2, HPos.RIGHT);
		content.add(lbl2, 0, 3);
		content.add(chbxCS, 1, 3);
		// ----
		Label lbl3 = new Label("Background colour");
		ColorPicker colorPicker = new ColorPicker(bkg);
		GridPane.setHalignment(lbl3, HPos.RIGHT);
		content.add(lbl3, 0, 4);
		content.add(colorPicker, 1, 4);
		// ----
		Label lbl4 = new Label("Contrast (0.0-1.0");
		TextField tfContrast = new TextField(Double.toString(contrast));
		content.add(lbl4, 0, 5);
		content.add(tfContrast, 1, 5);

		dialog.getDialogPane().setContent(content);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			symbolFill = chbxFill.isSelected();
			resolution = spResolution.getValue();
			symbolRadius = spRadius.getValue();
			contrast = Double.parseDouble(tfContrast.getText());
			colour64 = chbxCS.isSelected();
			bkg = colorPicker.getValue();
			if (colour64)
				colours = ColourContrast.getContrastingColours64(bkg, contrast);
			else
				colours = ColourContrast.getContrastingColours(bkg, contrast);

			itemColours.clear();

			items.forEach((k, v) -> {
				int idx = itemColours.size();
				itemColours.put(k, getColour(idx));
			});
			drawSpace(true);
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
		for (Map.Entry<String, Map<String, double[]>> entry : items.entrySet()) {
			String key = entry.getKey();
			Map<String, double[]> members = entry.getValue();
			for (Map.Entry<String, double[]> member : members.entrySet()) {
				double x = member.getValue()[0];
				double y = member.getValue()[1];
				if (box.contains(x, y)) {
					return key + "." + member.getKey() + "[" + x + "," + y + "]";
				}
			}
		}
		return "";
	}

}
