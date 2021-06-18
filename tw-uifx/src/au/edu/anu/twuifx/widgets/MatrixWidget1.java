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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.data.runtime.Output2DData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.WidgetGUI;
import au.edu.anu.twuifx.exceptions.TwuifxException;
import au.edu.anu.twuifx.widgets.helpers.SimCloneWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTimeFormatter;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import au.edu.anu.ymuit.ui.colour.Palette;
import au.edu.anu.ymuit.ui.colour.PaletteTypes;
import au.edu.anu.ymuit.util.CenteredZooming;
import au.edu.anu.ymuit.util.Decimals;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.SimulatorStatus;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Window;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */

/**
 * TODO: Change approach to show many grids.
 * 
 * Accept all simulators but user widget properties determines how many to
 * display at one time.
 * 
 * A view is one simulator. Within each are n 2d matrices of any size.
 * 
 * Awaiting metaData info (numeric only : i.e java.lang.Number
 */
public class MatrixWidget1 extends AbstractDisplayWidget<Output2DData, Metadata> implements WidgetGUI {

	private Label lblName;
	private Label lblHigh;
	private Label lblLow;
	private PaletteTypes paletteType;

	private static double fontSize = 10;
	private static Font font = Font.font("Verdana", fontSize);

	final private Map<Integer, Number[][]> senderGrids;
	final private List<D2Display> displays;
	private String widgetId;
	private final WidgetTimeFormatter timeFormatter;
	private final WidgetTrackingPolicy<TimeData> policy;

	private int nViews;

	private static Logger log = Logging.getLogger(MatrixWidget1.class);

	public MatrixWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM2);
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimCloneWidgetTrackingPolicy();
		senderGrids = new HashMap<>();
		displays = new ArrayList<>();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		this.widgetId = id;
		policy.setProperties(id, properties);
		nViews = 1;
		if (properties.hasProperty("nViews"))
			nViews = (Integer) properties.getPropertyValue("nViews");
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		if (policy.canProcessMetadataMessage(meta))
			timeFormatter.onMetaDataMessage(meta);
	}

	@Override
	public void onDataMessage(Output2DData data) {
		if (policy.canProcessDataMessage(data)) {
			if (data.status().equals(SimulatorStatus.Initial))
				throw new TwuifxException("Handling initial data not implemented for this widget.");
			else
				processDataMessage(data);
		}
	}

	private void processDataMessage(Output2DData data) {
		senderGrids.put(data.sender(), data.map());
		Platform.runLater(() -> {
			for (D2Display d : displays) {
				if (d.getSender() == data.sender()) {
					d.setTime(timeFormatter.getTimeText(data.time()));
					d.draw();
				}
			}
		});
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
		for (D2Display d : displays) {
			d.clear();
		}

	}

	@Override
	public Object getUserInterfaceContainer() {
		BorderPane content = new BorderPane();
		content.setTop(buildNamePane());
		content.setLeft(buildPalettePane());
		GridPane displayGrid = new GridPane();
		content.setCenter(displayGrid);

		int nSenders = policy.getDataMessageRange().getLast() - policy.getDataMessageRange().getFirst();
		nViews = Math.min(nSenders, nViews);

		int nCols = (int) Math.max(1, Math.sqrt(nViews));
		int nRows = nCols;
		if ((nRows * nCols) < nViews)
			nCols++;
		int display = 0;
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++) {
				D2Display d = new D2Display(display++, nSenders);
				displays.add(d);
				displayGrid.add(d.getContainer(), c, r);
				d.getContainer().setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
			}
		double rowSize = (1.0 / (double) nRows) * 100.0;
		double colSize = (1.0 / (double) nCols) * 100.0;
		for (int r = 0; r < nRows; r++) {
			RowConstraints row1 = new RowConstraints();
			row1.setPercentHeight(rowSize);
			displayGrid.getRowConstraints().addAll(row1);
		}
		for (int c = 0; c < nCols; c++) {
			ColumnConstraints col1 = new ColumnConstraints();
			col1.setPercentWidth(colSize);
			displayGrid.getColumnConstraints().addAll(col1);
		}

		getUserPreferences();

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
	private static final String keySender = "sender";

	private Palette palette;
	private double minValue;
	private double maxValue;
	private DecimalFormat formatter;
	private int resolution;
	private int decimalPlaces;
	private ImageView paletteImageView;

	@Override
	public void putUserPreferences() {
		for (int i = 0; i < displays.size(); i++) {
			D2Display d = displays.get(i);
			Preferences.putDouble(widgetId + keyScaleX + i, d.zoomTarget().getScaleX());
			Preferences.putDouble(widgetId + keyScaleY + i, d.zoomTarget().getScaleY());
			Preferences.putDouble(widgetId + keyScrollH + i, d.scrollPane().getHvalue());
			Preferences.putDouble(widgetId + keyScrollV + i, d.scrollPane().getVvalue());
			Preferences.putInt(widgetId + keySender + i, d.getSender());
		}
		Preferences.putInt(widgetId + keyResolution, resolution);
		Preferences.putInt(widgetId + keyDecimalPlaces, decimalPlaces);
		Preferences.putDouble(widgetId + keyMinValue, minValue);
		Preferences.putDouble(widgetId + keyMaxValue, maxValue);
		Preferences.putEnum(widgetId + keyPalette, paletteType);
	}

	@Override
	public void getUserPreferences() {
		for (int i = 0; i < displays.size(); i++) {
			D2Display d = displays.get(i);
			d.zoomTarget().setScaleX(Preferences.getDouble(widgetId + keyScaleX + i, d.zoomTarget().getScaleX()));
			d.zoomTarget().setScaleY(Preferences.getDouble(widgetId + keyScaleY + i, d.zoomTarget().getScaleY()));
			d.scrollPane().setHvalue(Preferences.getDouble(widgetId + keyScrollH + i, d.scrollPane().getHvalue()));
			d.scrollPane().setVvalue(Preferences.getDouble(widgetId + keyScrollV + i, d.scrollPane().getVvalue()));
			d.setSender(Preferences.getInt(widgetId + keySender + i, i));

		}
		resolution = Preferences.getInt(widgetId + keyResolution, 50);
		decimalPlaces = Preferences.getInt(widgetId + keyDecimalPlaces, 2);
		minValue = Preferences.getDouble(widgetId + keyMinValue, 0.0);
		maxValue = Preferences.getDouble(widgetId + keyMaxValue, 1.0);
		paletteType = (PaletteTypes) Preferences.getEnum(widgetId + keyPalette, PaletteTypes.BrownYellowGreen);

		formatter = Decimals.getDecimalFormat(decimalPlaces);

		lblLow.setText(formatter.format(minValue));
		lblHigh.setText(formatter.format(maxValue));

		palette = paletteType.getPalette();
		Image image = getLegend(10, 100);
		paletteImageView.setImage(image);

	}

	private static Label makeLabel(String s) {
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
		lblName = new Label(widgetId);
		lblName.setFont(Font.font("Verdana", 16));
		pane.setCenter(lblName);
		return pane;
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
		content.setVgap(5);
		content.setHgap(3);
		dialog.getDialogPane().setContent(content);
		int row = 0;
		int col = 0;

		// -- Palette
		ComboBox<PaletteTypes> cmbPalette = new ComboBox<>();
		cmbPalette.getItems().addAll(PaletteTypes.values());
		cmbPalette.getSelectionModel().select(paletteType);
		addGridControl("Palette", row++, col, cmbPalette, content);

		// -- minValue
		TextField tfMinValue = new TextField(Double.toString(minValue));
		tfMinValue.setMaxWidth(50);
		tfMinValue.setTextFormatter(
				new TextFormatter<>(change -> (change.getControlNewText().matches(Dialogs.vsReal) ? change : null)));
		addGridControl("Minimum z", row++, col, tfMinValue, content);

		// -- maxValue
		TextField tfMaxValue = new TextField(Double.toString(maxValue));
		tfMaxValue.setMaxWidth(50);
		tfMaxValue.setTextFormatter(
				new TextFormatter<>(change -> (change.getControlNewText().matches(Dialogs.vsReal) ? change : null)));
		addGridControl("Maximun z", row++, col, tfMaxValue, content);

		// --- resolution
		Spinner<Integer> spResolution = new Spinner<>();
		spResolution.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, resolution));
		spResolution.setMaxWidth(100);
		spResolution.setEditable(true);
		addGridControl("Resolution", row++, col, spResolution, content);

		// -- format
		Spinner<Integer> spDP = new Spinner<>();
		spDP.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, decimalPlaces));
		spDP.setMaxWidth(100);
		spDP.setEditable(true);
		addGridControl("Decimal places", row++, col, spDP, content);

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.get().equals(ok)) {
			paletteType = cmbPalette.getValue();
			palette = paletteType.getPalette();
			resolution = spResolution.getValue();
			decimalPlaces = spDP.getValue();
			minValue = Double.parseDouble(tfMinValue.getText());
			maxValue = Double.parseDouble(tfMaxValue.getText());
			if (maxValue < minValue) {
				double tmp = maxValue;
				maxValue = minValue;
				minValue = tmp;
			}
			Image image = getLegend(10, 100);
			paletteImageView.setImage(image);
			formatter = Decimals.getDecimalFormat(decimalPlaces);
			lblLow.setText(formatter.format(minValue));
			lblHigh.setText(formatter.format(maxValue));
			for (D2Display d : displays) {
				d.draw();
			}
		}
	}

	private class D2Display {
		private final int nSenders;
		private int sender;
		private final ComboBox<String> cmbxSender;
		private final BorderPane container;
		private final BorderPane zoomTarget;
		private final Canvas canvas;
		private final ScrollPane scrollPane;
		private Label lblTime;
		private Label lblValue;
		private Label lblXY;

		public D2Display(int sender, int nSenders) {
			this.sender = sender;
			this.nSenders = nSenders;
			cmbxSender = new ComboBox<String>();
			for (int i = 0; i < nSenders; i++)
				cmbxSender.getItems().add(Integer.toString(i));
			cmbxSender.setOnAction(e -> {
				this.sender = cmbxSender.getSelectionModel().getSelectedIndex();
				draw();
			});

			if (nSenders == 1)
				cmbxSender.setDisable(true);

			container = new BorderPane();

			lblTime = makeLabel("");
			lblValue = makeLabel("");
			lblXY = makeLabel("");
			HBox topBar = new HBox();
			topBar.setAlignment(Pos.CENTER_LEFT);
			topBar.setSpacing(5);
			Label simCaption = new Label("Simulator");
			if (nSenders == 1)
				simCaption.setDisable(true);
			topBar.getChildren().addAll(simCaption, cmbxSender, new Label("Tracker time"), lblTime);
			container.setTop(topBar);

			HBox bottomBar = new HBox();
			bottomBar.setAlignment(Pos.CENTER_LEFT);
			bottomBar.getChildren().addAll(makeLabel("[x,y]"), lblXY, makeLabel("="), lblValue);
			bottomBar.setSpacing(5);
			container.setBottom(bottomBar);

			zoomTarget = new BorderPane();
			canvas = new Canvas();
			zoomTarget.setCenter(canvas);
			Group group = new Group(zoomTarget);
			StackPane content = new StackPane(group);
			scrollPane = new ScrollPane(content);
			scrollPane.setPannable(true);
			scrollPane.setContent(content);
			CenteredZooming.center(scrollPane, content, group, zoomTarget);
			zoomTarget.setOnMouseMoved(e -> onMouseMove(e));
			container.setCenter(scrollPane);
			scrollPane.setOnMouseMoved(e-> clearXY());

		}

		public void clear() {
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		}

		public void setSender(int s) {
			s = Math.min(s, nSenders - 1);
			sender = s;
			cmbxSender.getSelectionModel().select(sender);
		}

		public ScrollPane scrollPane() {
			return scrollPane;
		}

		public Node zoomTarget() {
			return zoomTarget;
		}

		public void setTime(String timeText) {
			lblTime.setText(timeText);
		}

		public int getSender() {
			return sender;
		}

		public Pane getContainer() {
			return container;
		}

		public void draw() {
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			Number[][] grid = senderGrids.get(sender);
			if (grid != null) {
				int mapWidth = grid.length;
				int mapHeight = grid[0].length;
				if (resizeCanvas(mapWidth, mapHeight)) {
					gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
				}
				if (resolution == 1)
					dataToCanvasPixels(gc, grid);
				else
					dataToCanvasRect(gc, grid, mapWidth, mapHeight);
			}
		}

		private boolean resizeCanvas(int mapWidth, int mapHeight) {
			if (canvas.getWidth() != (resolution * mapWidth) || canvas.getHeight() != (resolution * mapHeight)) {
				canvas.setWidth(mapWidth * resolution);
				canvas.setHeight(mapHeight * resolution);
				return true;
			} else
				return false;
		}

		private void dataToCanvasPixels(GraphicsContext gc, Number[][] grid) {
			double h = grid[0].length - 1;
			PixelWriter pw = gc.getPixelWriter();

			for (int x = 0; x < canvas.getWidth(); x++)
				for (int y = 0; y < canvas.getHeight(); y++) {
					Color c = Color.TRANSPARENT;
					if (grid[x][y] != null) { // missing value
						double v = grid[x][y].doubleValue();
						c = palette.getColour(v, minValue, maxValue);
						int flipy = (int) (h - y);
						pw.setColor(x, flipy, c);
					}
				}
		}

		private void dataToCanvasRect(GraphicsContext gc, Number[][] grid, int mapWidth, int mapHeight) {
			int w = resolution;
			double h = grid[0].length - 1;// mapHeight

			for (int x = 0; x < mapWidth; x++)
				for (int y = 0; y < mapHeight; y++) {
					Color c = Color.TRANSPARENT;
					if (grid[x][y] != null) {
						double v = grid[x][y].doubleValue();
						c = palette.getColour(v, minValue, maxValue);
//						gc.setStroke(c);
						gc.setFill(c);
						int flipy = (int) (h - y);
//						gc.strokeRect(x * w, flipy * w, w, w);
						gc.fillRect(x * w, flipy * w, w, w);
					}
				}
		}

		private void onMouseMove(MouseEvent e) {
			Number[][] grid = senderGrids.get(sender);
			int x = (int) (e.getX() / resolution);
			int y = (int) ((canvas.getHeight() - e.getY()) / resolution);
			lblXY.setText("[" + x + "," + y + "]");
			if (x < grid.length & y < grid[0].length & x >= 0 & y >= 0) {
				lblValue.setText(formatter.format(grid[x][y]));
				e.consume();
			}
			
		}
		private void clearXY() {
			lblXY.setText("");
			lblValue.setText("");
		}

	}
}
