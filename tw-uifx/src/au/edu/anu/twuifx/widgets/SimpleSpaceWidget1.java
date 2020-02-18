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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import au.edu.anu.omhtk.preferences.Preferences;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.SpaceData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ecosystem.structure.SpaceNode;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.ymuit.util.CenteredZooming;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Duple;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * @author Ian Davies
 *
 * @date 12 Feb 2020
 * 
 *       Widget to show spatial map of objects and their relations.
 * 
 */
public class SimpleSpaceWidget1 extends AbstractDisplayWidget<SpaceData, Metadata> implements Widget {
	private AnchorPane zoomTarget;
	private Canvas canvas;
	private ScrollPane scrollPane;
	private int resolution=100;
	private List<Point2D> dummyPoints;
	private List<Duple<Point2D, Point2D>> dummyRelations;
	private Random rnd = new Random();
	private Bounds bounds;
	private String widgetId;
	private WidgetTrackingPolicy<TimeData> policy;
	private WidgetTimeFormatter timeFormatter;
	private SpaceNode spaceNode;

	public SimpleSpaceWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.SPACE);
		// get an outedge to a space node
		// then, when msg arrives space = spaceNode.getInstance(sc.Id)
		// but we don't know which space and we don't want to construct one!!
		// We shouldn't need the space set sent in the time msg
		
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();

		bounds = new BoundingBox(0, 0, 1, 1);
		dummyPoints = new ArrayList<>();
		dummyRelations = new ArrayList<>();
		for (int i = 0; i < 10; i++)
			dummyPoints.add(new Point2D.Double(rnd.nextDouble(), rnd.nextDouble()));
		dummyRelations.add(new Duple<Point2D, Point2D>(dummyPoints.get(0), dummyPoints.get(1)));
		dummyRelations.add(new Duple<Point2D, Point2D>(dummyPoints.get(2), dummyPoints.get(3)));
		dummyRelations.add(new Duple<Point2D, Point2D>(dummyPoints.get(4), dummyPoints.get(5)));
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// TODO Auto-generated method stub

	}

	private static final String keyScaleX = "scaleX";
	private static final String keyScaleY = "scaleY";
	private static final String keyScrollH = "scrollH";
	private static final String keyScrollV = "scrollV";
	private static final String keyResolution = "resolution";

	@Override
	public void putPreferences() {
		Preferences.putDouble(widgetId + keyScaleX, zoomTarget.getScaleX());
		Preferences.putDouble(widgetId + keyScaleY, zoomTarget.getScaleY());
		Preferences.putDouble(widgetId + keyScrollH, scrollPane.getHvalue());
		Preferences.putDouble(widgetId + keyScrollV, scrollPane.getVvalue());
		Preferences.putDouble(widgetId + keyResolution, resolution);
	}

	@Override
	public void getPreferences() {
		zoomTarget.setScaleX(Preferences.getDouble(widgetId + keyScaleX, zoomTarget.getScaleX()));
		zoomTarget.setScaleY(Preferences.getDouble(widgetId + keyScaleY, zoomTarget.getScaleY()));
		scrollPane.setHvalue(Preferences.getDouble(widgetId + keyScrollH, scrollPane.getHvalue()));
		scrollPane.setVvalue(Preferences.getDouble(widgetId + keyScrollV, scrollPane.getVvalue()));
		resolution = Preferences.getInt(widgetId + keyResolution, 1);
	}

	@Override
	public void onDataMessage(SpaceData data) {

		// for debugging only
		System.out.println(data);
		
		if (policy.canProcessDataMessage(data)) {
			
			// add processing code here, here is the pseudocode I sent you before
			// caution: this Point is the fr.cnrs.iees.uit.space.Point. Can have any dimension (here it's 2)
//			if (data.create()) { // means an item must be added to the display
//			    if (data.isPoint()) { // means the item is a point, ie a SystemComponent
//			    	data.coordinates() ... // returns the point coordinates as a double[]
//			        // write your display code here
//			    }
//			    else { // means the item is a line (actually just a pair of points), ie a SystemRelation
//			    	data.line() ... // returns the line pair of coordinates as a Duple<double[],double[]>
//			        // write your display code here
//			    }
//			}
//			else if (data.delete()) { // means the item is to be removed from the display
//				data.itemLabel()... // returns the label of the item to remove, be it a relation or a component
//			    // write your display cod here - I am more or less assuming that your points are in a Map<DataLabel,"location">
//			}
			
			jiggle(dummyPoints, rnd);// pseudo model update
			Platform.runLater(() -> {
				drawSpace(bounds, dummyPoints, dummyRelations);
			});
		}
	}

	private void drawSpace(Bounds bounds, List<Point2D> lst2, List<Duple<Point2D, Point2D>> dummyRelations2) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		resizeCanvas((int)bounds.getWidth(),(int)bounds.getHeight());

	}

	private void resizeCanvas(int mapWidth, int mapHeight) {
		if (canvas.getWidth() != (resolution * mapWidth) || canvas.getHeight() != (resolution * mapHeight)) {
			canvas.setWidth(mapWidth * resolution);
			canvas.setHeight(mapHeight * resolution);
			clearCanvas();
		}
	}

	private static void jiggle(List<Point2D> lst, Random rnd) {
		for (Point2D p : lst) {
			boolean nx = rnd.nextBoolean();
			boolean ny = rnd.nextBoolean();
			double dx = rnd.nextDouble() * 0.001;
			double dy = rnd.nextDouble() * 0.001;
			double x = p.getX();
			double y = p.getY();
			if (nx)
				x -= dx;
			else
				x += dx;
			if (ny)
				y -= dy;
			else
				y += dy;
			x = Math.max(0, Math.min(1.0, x));
			y = Math.max(0, Math.min(1.0, y));
			p.setLocation(x, y);
		}

	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		timeFormatter.onMetaDataMessage(meta);

	}

	@Override
	public void onStatusMessage(State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getUserInterfaceContainer() {
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
		// lblXY.setText("[" + x + "," + y + "]");
//		if (x < mx & y < my & x >= 0 & y >= 0) 
//			 lblValue.setText(formatter.format(numbers[x][y]));

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
//				if (numbers[x][y] != null) { // missing value
//					double v = (Double) numbers[x][y];
//					c = palette.getColour(v, minValue, maxValue);
//					pw.setColor(x, y, c);
//				}
			}
	}

	private void dataToCanvasRect(int mapWidth, int mapHeight) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		int w = resolution;
		for (int x = 0; x < mapWidth; x++)
			for (int y = 0; y < mapHeight; y++) {
				Color c = Color.TRANSPARENT;
//				if (numbers[x][y] != null) {
//					double v = (Double) numbers[x][y];
//					c = palette.getColour(v, minValue, maxValue);
//					gc.setStroke(c);
//					gc.setFill(c);
//					gc.fillRect(x * w, y * w, w, w);
//				}
			}
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
	}

}
