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
import au.edu.anu.ymuit.ui.colour.ColourContrast;
import au.edu.anu.ymuit.util.CenteredZooming;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Duple;
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
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Window;
import javafx.util.Duration;

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
	private Bounds spaceBounds;
	private String widgetId;
	private WidgetTrackingPolicy<TimeData> policy;
	private WidgetTimeFormatter timeFormatter;
	private Map<String, Map<String, double[]>> items;
	private List<Color> colours;
	private Map<String, Color> itemColours;
	private Map<Bounds, String> mouseMap;
	private Tooltip tooltip;
	private GridPane legend;

	private int resolution;
	private int symbolRadius;
	private boolean symbolFill;
	private Color bkg;

	private static Logger log = Logging.getLogger(SimpleSpaceWidget1.class);
	// static {log.setLevel(Level.INFO);} use args for MM or MR e.g.
	// au.edu.anu.twuifx.widgets.SimpleSpaceWidget1:INFO

	public SimpleSpaceWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.SPACE);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
		items = new HashMap<>();
		colours = new ArrayList<>();
		itemColours = new HashMap<>();
		mouseMap = new HashMap<>();
		bkg = Color.WHITE;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.widgetId = id;
		policy.setProperties(id, properties);
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// senderId IS set here (== 0)
		System.out.println("meta msg: " + meta.properties().toString());
		log.info(meta.toString());
		timeFormatter.onMetaDataMessage(meta);
		Interval xLimits = (Interval) meta.properties().getPropertyValue(P_SPACE_XLIM.key());
		Interval yLimits = (Interval) meta.properties().getPropertyValue(P_SPACE_YLIM.key());
		spaceBounds = new BoundingBox(xLimits.inf(), yLimits.inf(), xLimits.sup() - xLimits.inf(),
				yLimits.sup() - yLimits.inf());
	}

	@Override
	public void onDataMessage(SpaceData data) {
		System.out.println("Data msg: " + data);
		log.info(data.toString()); // something weird with the logging??
		if (policy.canProcessDataMessage(data)) {
			boolean updateLegend = false;
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
					// should we assign a colour to this key here?
					if (!itemColours.containsKey(key)) {
						itemColours.put(key, getColour(items.size() - 1));
					}
				} else {// lines/relations
					Duple<double[], double[]> line = data.line();
					// wait and see
				}

			} else if (data.delete()) {
				DataLabel dl = data.itemLabel();
				String name = dl.getEnd();
				String key = dl.toString().replace(">" + name, "").replace(">", ".");
				Map<String, double[]> value = items.get(key);
				value.remove(name);
				// Don't remove empty system entries as new entries will acquire the same
				// colour e.g if bears become extinct and rabbits appear for the first time,
				// they
				// will have the bear's colour!.
//				if (value.isEmpty()) {
//					items.remove(key);
//					if (itemColours.containsKey(key))
//						itemColours.remove(key);
//				}
			} else {
				// relocate - wait and see
			}
			final boolean flag = updateLegend;

			Platform.runLater(() -> {
				// watch out for crazy problems here with changes to the items
				drawSpace(flag);
			});
		}
	}

	@Override
	public void onStatusMessage(State state) {
		// log.info(state.toString());
		// !! Sim sends dataMessage before this method receivers WAIT state so we can't
		// clear the hash maps here!!!
		if (isSimulatorState(state, waiting)) {
			System.out.println("RESET");
			// drawSpace();
		}
	}

//---- Drawing ---
	private void drawSpace(boolean updateLegend) {
		mouseMap.clear();
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
				point = point.add(symbolRadius, symbolRadius);
				gc.strokeOval(point.getX(), point.getY(), size, size);
				if (symbolFill)
					gc.fillOval(point.getX(), point.getY(), size, size);
				mouseMap.put(new BoundingBox(point.getX(), point.getY(), size, size), key + "." + name);
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
		gc.fillRect(0, 0, canvas.getWidth(),canvas.getHeight());
		gc.strokeRect(1,1, canvas.getWidth()-3, canvas.getHeight()-2);
	};

	private Color getColour(int idx) {
		if (colours.size() < items.size())
			colours = ColourContrast.getContrastingColours(bkg, items.size() + 50);
		return colours.get(idx);

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
		Point2D result = new Point2D(dx, dy);
		return new Point2D(dx, dy);
	}

// --------------- Preferences

	private static final String keyScaleX = "scaleX";
	private static final String keyScaleY = "scaleY";
	private static final String keyScrollH = "scrollH";
	private static final String keyScrollV = "scrollV";
	private static final String keyResolution = "resolution";
	private static final String keySymbolRad = "radius";
	private static final String keySymbolFill = "fill";

	@Override
	public void putUserPreferences() {
		Preferences.putDouble(widgetId + keyScaleX, zoomTarget.getScaleX());
		Preferences.putDouble(widgetId + keyScaleY, zoomTarget.getScaleY());
		Preferences.putDouble(widgetId + keyScrollH, scrollPane.getHvalue());
		Preferences.putDouble(widgetId + keyScrollV, scrollPane.getVvalue());
		Preferences.putInt(widgetId + keyResolution, resolution);
		Preferences.putInt(widgetId + keySymbolRad, symbolRadius);
		Preferences.putBoolean(widgetId + keySymbolFill, symbolFill);
	}

	@Override
	public void getUserPreferences() {
		zoomTarget.setScaleX(Preferences.getDouble(widgetId + keyScaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(widgetId + keyScaleY, zoomTarget.getScaleY()));
		scrollPane.setHvalue(Preferences.getDouble(widgetId + keyScrollH, scrollPane.getHvalue()));
		scrollPane.setVvalue(Preferences.getDouble(widgetId + keyScrollV, scrollPane.getVvalue()));
		resolution = Preferences.getInt(widgetId + keyResolution, 10);
		symbolRadius = Preferences.getInt(widgetId + keySymbolRad, 5);
		symbolFill = Preferences.getBoolean(widgetId + keySymbolFill, true);
	}

	// --------------- GUI

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane container = new BorderPane();
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(4);
		dropShadow.setOffsetY(6);
		dropShadow.setHeight(5);
		zoomTarget = new AnchorPane();
		canvas = new Canvas();
		canvas.setEffect(dropShadow);
		tooltip = new Tooltip();
		Tooltip.install(canvas, tooltip);
		tooltip.setShowDelay(new Duration(500));
		tooltip.setHideDelay(new Duration(500));
		canvas.setOnMouseMoved(e -> onMouseMove(e));
		canvas.setOnMouseExited(e -> {
			tooltip.setText("");
			tooltip.hide();
		});
		zoomTarget.getChildren().add(canvas);
		Group group = new Group(zoomTarget);
		StackPane content = new StackPane(group);
		scrollPane = new ScrollPane(content);
		scrollPane.setPannable(true);
		scrollPane.setContent(content);
		scrollPane.setMinSize(170, 170);
		CenteredZooming.center(scrollPane, content, group, zoomTarget);
		container.setCenter(scrollPane);
		legend = new GridPane();
		legend.setHgap(3);

		container.setRight(legend);
		return container;
	}

	private void addLegendItem(String name, Color colour) {
		int idx = legend.getChildren().size();
		Circle circle = null;
		if (!symbolFill)
			circle = new Circle(0, 1, 4, Color.TRANSPARENT);
		else
			circle = new Circle(0, 1, 4, colour);
		circle.setStroke(colour);
		legend.add(circle, 0, idx);
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
		// TODO Dialog box for this widget
		// resolution
		// symbolRadius;
		// solid
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

		dialog.getDialogPane().setContent(content);
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			symbolFill = chbxFill.isSelected();
			resolution = spResolution.getValue();
			symbolRadius = spRadius.getValue();
			drawSpace(true);
		}
	}

	private void onMouseMove(MouseEvent e) {
		String name = findName(e);
		if (name != null) {
			tooltip.setText(name);
			e.consume();
		}
	}

	private String findName(MouseEvent e) {
		for (Map.Entry<Bounds, String> entry : mouseMap.entrySet())
			if (entry.getKey().contains(e.getX(), e.getY()))
				return entry.getValue();
		return null;
	}

}
