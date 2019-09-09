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

import java.text.DecimalFormat;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.rscs.aot.collections.tables.DoubleTable;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
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
import javafx.scene.text.Font;

/**
 * @author Ian Davies
 *
 * @date 2 Sep 2019
 */
public class SingleGridWidget extends AbstractDisplayWidget implements Widget {
	
	protected SingleGridWidget(int statusType) {
		super(statusType, -1);
		// TODO Auto-generated constructor stub
	}

	private Label lblName;
	private Label lblXY;
	private Label lblValue;
	private Label lblHigh;
	private Label lblLow;
	private DoubleTable data;
	//private TimeDisplayManager tdm;
	private AnchorPane zoomTarget;
	private Canvas canvas;
	private ScrollPane scrollPane;
	//private PaletteTypes paletteType;
	//private Palette palette;
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
	private String id;

	@Override
	public void setProperties(String id,SimplePropertyList properties) {
		this.id = id;
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

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
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
		Preferences.putDouble(id + keyScaleX, zoomTarget.getScaleX());
		Preferences.putDouble(id + keyScaleY, zoomTarget.getScaleY());
		Preferences.putDouble(id + keyScrollH, scrollPane.getHvalue());
		Preferences.putDouble(id + keyScrollV, scrollPane.getVvalue());
		Preferences.putDouble(id + keyResolution, resolution);
		Preferences.putDouble(id + keyDecimalPlaces, decimalPlaces);
		Preferences.putDouble(id + keyMinValue, minValue);
		Preferences.putDouble(id + keyMaxValue, maxValue);
		//Preferences.putDouble(id + keyPalette, paletteType);

	}

	@Override
	public void getPreferences() {
		//paletteType = (PaletteTypes) pref.get(prefix + keyPalette, PaletteTypes.BLUELIMERED);
		//palette = paletteType.getPalette();
		Image image = getLegend(10, 100);
		paletteImageView.setImage(image);
		minValue = Preferences.getDouble(id+ keyMinValue, 0.0);
		maxValue = Preferences.getDouble(id+  keyMaxValue, 1.0);

		zoomTarget.setScaleX(Preferences.getDouble(id+ keyScaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(id+ keyScaleY, zoomTarget.getScaleY()));
		scrollPane.setHvalue(Preferences.getDouble(id+  keyScrollH, scrollPane.getHvalue()));
		scrollPane.setVvalue(Preferences.getDouble(id+  keyScrollV, scrollPane.getVvalue()));
		decimalPlaces =Preferences.getInt(id+  keyDecimalPlaces, 2);
		//formatter = UiUtil.getDecimalFormat(decimalPlaces);
		lblLow.setText(formatter.format(minValue));
		lblHigh.setText(formatter.format(maxValue));

		//tdm.loadPreferences(pref, prefix);

		resolution = Preferences.getInt(id+  keyResolution, 1);
		//dataToCanvas();

		
	}
	

private Pane buildStatusBar() {
	VBox pane = new VBox();
	HBox valuePane = new HBox();
	valuePane.setSpacing(5.0);
	HBox timePane = new HBox();
	timePane.setSpacing(5.0);
	lblXY = makeLabel("");
	lblValue = makeLabel("");
	//tdm.getTimeLabel().setFont(font);
	valuePane.getChildren().addAll(makeLabel("[x,y]"), lblXY, makeLabel("="), lblValue);
	//timePane.getChildren().addAll(tdm.getTimeLabel());
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
//		Color c = palette.getColour(height - h - 1, 0, height);
//		for (int w = 0; w < width; w++) {
//			pw.setColor(w, h, c);
//		}
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
	//UiUtil.zoomConfig(scrollPane, content, group, zoomTarget);
	zoomTarget.setOnMouseMoved(e -> onMouseMove(e));
	return scrollPane;
}

private void onMouseMove(MouseEvent e) {
	int x = (int) (e.getX() / resolution);
	int y = (int) (e.getY() / resolution);
	lblXY.setText("[" + x + "," + y + "]");
	if (x < mx & y < my & x >= 0 & y >= 0) {
//		Double d = getData(x, y);
//		lblValue.setText(formatter.format(d));
	}
}

// TODO: properly type those methods
@Override
public void onDataMessage(Object data) {
	// TODO Auto-generated method stub
	
}

@Override
public void onMetaDataMessage(Object meta) {
	// TODO Auto-generated method stub
	
}

@Override
public void onStatusMessage(State state) {
	// TODO Auto-generated method stub
	
}

}
