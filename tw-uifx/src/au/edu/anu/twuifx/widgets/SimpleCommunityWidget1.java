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

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.system.SystemComponent;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.ymuit.ui.colour.PaletteTypes;
import au.edu.anu.ymuit.util.CenteredZooming;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
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
 * Widget to show spatial map of objects and their relations.
 * 
 */
public class SimpleCommunityWidget1 extends AbstractDisplayWidget<TimeData, Metadata> implements Widget {
	private AnchorPane zoomTarget;
	private Canvas canvas;
	private ScrollPane scrollPane;
	private int resolution;


	public SimpleCommunityWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.TIME);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void putPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataMessage(TimeData data) {
		for (SystemComponent sc: data.getCommunity().allItems()){
			System.out.println("Time: "+data.time()+" SC: "+sc.id());
		}
		
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// TODO Auto-generated method stub
		
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
		//lblXY.setText("[" + x + "," + y + "]");
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
