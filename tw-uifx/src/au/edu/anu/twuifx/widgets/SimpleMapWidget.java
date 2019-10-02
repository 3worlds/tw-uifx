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

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twcore.data.runtime.MapData;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.ymuit.ui.colour.Palette;
import au.edu.anu.ymuit.ui.colour.PaletteTypes;
import au.edu.anu.ymuit.util.CenteredZooming;
import au.edu.anu.ymuit.util.Decimals;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */
public class SimpleMapWidget extends AbstractDisplayWidget<MapData, Metadata> implements Widget {

	private Label lblName;
	private Label lblXY;
	private Label lblValue;
	private Label lblHigh;
	private Label lblLow;
	private AnchorPane zoomTarget;
	private Canvas canvas;
	private ScrollPane scrollPane;
	private PaletteTypes paletteType;
	private Palette palette;
	// private String formatString;
	private double minValue;
	private double maxValue;
	private DecimalFormat formatter;
	private int resolution;
	private int decimalPlaces;
	// private IntegerProperty resolutionProperty;
	private ImageView paletteImageView;

	private static double fontSize = 10;
	private static Font font = Font.font("Verdana", fontSize);
	private int mx;
	private int my;

	private Number[][] numbers;
	private int sender;
	private String widgetId;
	private WidgetTimeFormatter timeFormatter;

	private static Logger log = Logging.getLogger(SimpleMapWidget.class);

	public SimpleMapWidget(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, -1);
		timeFormatter = new WidgetTimeFormatter();
		log.info("Creation thread id: " + Thread.currentThread().getId());
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.widgetId = id;
		sender = (Integer) properties.getPropertyValue("sender");
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		timeFormatter.onMetaDataMessage(meta);
	}

	@Override
	public void onDataMessage(MapData data) {
		if (sender == data.sender())
			Platform.runLater(() -> {
				processOnDataMessage(data);
			});
	}

	private void processOnDataMessage(MapData data) {
		numbers = data.map();
		dataToCanvas();

	}

	private void dataToCanvas() {
		int mapWidth = numbers.length;
		int mapHeight = numbers[0].length;
		resizeCanvas(mapWidth, mapHeight);
		if (resolution == 1)
			dataToCanvasPixels();
		else
			dataToCanvasRect(mapWidth, mapHeight);
	}

	private void resizeCanvas(int mapWidth, int mapHeight) {
		if (canvas.getWidth() != (resolution * mapWidth) || canvas.getHeight() != (resolution * mapHeight)) {
			canvas.setWidth(mapWidth * resolution);
			canvas.setHeight(mapHeight * resolution);
			clearCanvas();
		}
	}

	private void clearCanvas() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	};

	private void dataToCanvasPixels() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		PixelWriter pw = gc.getPixelWriter();
		for (int x = 0; x < canvas.getWidth(); x++)
			for (int y = 0; y < canvas.getHeight(); y++) {
				Color c = Color.TRANSPARENT;
				if (numbers[x][y] != null) { // missing value
					double v = (Double) numbers[x][y];
					c = palette.getColour(v, minValue, maxValue);
					pw.setColor(x, y, c);
				}
			}
	}

	private void dataToCanvasRect(int mapWidth, int mapHeight) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		int w = resolution;
		for (int x = 0; x < mapWidth; x++)
			for (int y = 0; y < mapHeight; y++) {
				Color c = Color.TRANSPARENT;
				if (numbers[x][y] != null) {
					double v = (Double) numbers[x][y];
					c = palette.getColour(v, minValue, maxValue);
					gc.setStroke(c);
					gc.setFill(c);
					gc.fillRect(x * w, y * w, w, w);
				}
			}
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			Platform.runLater(() -> {
				processWaitState();
			});
		}
	}

	private void processWaitState() {

	}

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane content = new BorderPane();
		content.setBottom(buildStatusBar());
		content.setTop(buildNamePane());
		content.setLeft(buildPalettePane());
		content.setCenter(buildScrollPane());
		return content;
	}

	private static final String keyScaleX = "scaleX";
	private static final String keyScaleY = "scaleY";
	private static final String keyScrollH = "scrollH";
	private static final String keyScrollV = "scrollV";
	private static final String keyResolution = "resolution";
	private static final String keyDecimalPlaces = "decimalPlaces";
	private static final String keyPalette = "palette";
	private static final String keyMinValue = "minValue";
	private static final String keyMaxValue = "maxValue";

	@Override
	public void putPreferences() {
		Preferences.putDouble(widgetId + keyScaleX, zoomTarget.getScaleX());
		Preferences.putDouble(widgetId + keyScaleY, zoomTarget.getScaleY());
		Preferences.putDouble(widgetId + keyScrollH, scrollPane.getHvalue());
		Preferences.putDouble(widgetId + keyScrollV, scrollPane.getVvalue());
		Preferences.putDouble(widgetId + keyResolution, resolution);
		Preferences.putDouble(widgetId + keyDecimalPlaces, decimalPlaces);
		Preferences.putDouble(widgetId + keyMinValue, minValue);
		Preferences.putDouble(widgetId + keyMaxValue, maxValue);
		Preferences.putString(widgetId + keyPalette, paletteType.name());
	}

	@Override
	public void getPreferences() {
		paletteType = PaletteTypes
				.valueOf(Preferences.getString(widgetId + keyPalette, PaletteTypes.getDefault().name()));
		palette = paletteType.getPalette();
		Image image = getLegend(10, 100);
		paletteImageView.setImage(image);
		minValue = Preferences.getDouble(widgetId + keyMinValue, 0.0);
		maxValue = Preferences.getDouble(widgetId + keyMaxValue, 1.0);

		zoomTarget.setScaleX(Preferences.getDouble(widgetId + keyScaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(widgetId + keyScaleY, zoomTarget.getScaleY()));
		scrollPane.setHvalue(Preferences.getDouble(widgetId + keyScrollH, scrollPane.getHvalue()));
		scrollPane.setVvalue(Preferences.getDouble(widgetId + keyScrollV, scrollPane.getVvalue()));
		decimalPlaces = Preferences.getInt(widgetId + keyDecimalPlaces, 2);
		formatter = Decimals.getDecimalFormat(decimalPlaces);
		lblLow.setText(formatter.format(minValue));
		lblHigh.setText(formatter.format(maxValue));
		resolution = Preferences.getInt(widgetId + keyResolution, 1);
		// dataToCanvas();

	}

	private Pane buildStatusBar() {
		VBox pane = new VBox();
		HBox valuePane = new HBox();
		valuePane.setSpacing(5.0);
		HBox timePane = new HBox();
		timePane.setSpacing(5.0);
		lblXY = makeLabel("");
		lblValue = makeLabel("");
		// tdm.getTimeLabel().setFont(font);
		valuePane.getChildren().addAll(makeLabel("[x,y]"), lblXY, makeLabel("="), lblValue);
		// timePane.getChildren().addAll(tdm.getTimeLabel());
		pane.getChildren().addAll(valuePane, timePane);
		return pane;
	}

	private Label makeLabel(String s) {
		Label result = new Label(s);
		result.setFont(font);
		return result;
	}

	private WritableImage getLegend(int width, int height) {
		WritableImage image = new WritableImage(width, height);
		PixelWriter pw = image.getPixelWriter();
		for (int h = 0; h < height; h++) {
			Color c = palette.getColour(height - h - 1, 0, height);
			for (int w = 0; w < width; w++) {
				pw.setColor(w, h, c);
			}
		}
		return image;
	}

	private Pane buildPalettePane() {
		paletteImageView = new ImageView();
		lblHigh = makeLabel("");
		lblLow = makeLabel("");
		VBox pane = new VBox(makeLabel("high"), lblHigh, paletteImageView, lblLow, makeLabel("low"));
		pane.setAlignment(Pos.CENTER);
		return pane;
	}

	private Pane buildNamePane() {
		BorderPane pane = new BorderPane();
		lblName = new Label("Grid name");
		Font font = Font.font("Verdana", 12);
		lblName.setFont(font);
		pane.setCenter(lblName);
		return pane;
	}

	private ScrollPane buildScrollPane() {
		zoomTarget = new AnchorPane();
		canvas = new Canvas();
		zoomTarget.getChildren().add(canvas);
		Group group = new Group(zoomTarget);
		StackPane content = new StackPane(group);
		scrollPane = new ScrollPane(content);
		scrollPane.setPannable(true);
		scrollPane.setContent(content);
		scrollPane.setMinSize(170, 170);
		CenteredZooming.center(scrollPane, content, group, zoomTarget);
		zoomTarget.setOnMouseMoved(e -> onMouseMove(e));
		return scrollPane;
	}

	private void onMouseMove(MouseEvent e) {
		int x = (int) (e.getX() / resolution);
		int y = (int) (e.getY() / resolution);
		lblXY.setText("[" + x + "," + y + "]");
		if (x < mx & y < my & x >= 0 & y >= 0) 
			 lblValue.setText(formatter.format(numbers[x][y]));
		
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
	}

}
