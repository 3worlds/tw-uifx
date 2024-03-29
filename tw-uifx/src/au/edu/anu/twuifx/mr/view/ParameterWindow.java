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

package au.edu.anu.twuifx.mr.view;

import org.controlsfx.control.PropertySheet;

import au.edu.anu.twapps.dialogs.*;
import fr.cnrs.iees.omugi.graph.impl.ALEdge;
import fr.cnrs.iees.omugi.graph.impl.TreeGraph;
import fr.cnrs.iees.omugi.graph.impl.TreeGraphDataNode;
import javafx.scene.control.Button;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @author Ian Davies - 3 Jan 2020
 */

/**
 * For editing runTime parameters from ModelRunner
 * 
 */
public class ParameterWindow implements IRunTimeParameterizer{
	private Stage stage;
	private Button btnApply;
	private Button btnSave;
	private Button btnOpen;
	private PropertySheet propertySheet;

	/**
	 * @param graph  TODO: Not implemented
	 * @param ownerStage TODO: Not implemented
	 * @param showToggle TODO: Not implemented
	 */
	public ParameterWindow(TreeGraph<TreeGraphDataNode, ALEdge> graph, Stage ownerStage, BooleanProperty showToggle) {
		BorderPane content = new BorderPane();
		Scene scene = new Scene(content, 400, 500);
		stage = new Stage();
		stage.setTitle("Parameters");
		stage.setScene(scene);
		stage.initOwner((Window) DialogService.getImplementation().owner());
		stage.setX(ownerStage.getX() + 200);
		stage.setY(ownerStage.getY() + 100);
		scene.getWindow().setOnCloseRequest((e) -> {
			showToggle.set(false);
			stage.hide();
			e.consume();
		});

		propertySheet = new PropertySheet();
		content.setCenter(propertySheet);
		HBox bottomContent = new HBox();
		bottomContent.setAlignment(Pos.BOTTOM_RIGHT);
		bottomContent.setSpacing(5);
		bottomContent.setPadding(new Insets(10, 5, 10, 5));

		content.setBottom(bottomContent);

		btnApply = new Button("Apply");
		btnSave = new Button("Save");
		btnOpen = new Button("Open");
		bottomContent.getChildren().addAll(btnApply, btnSave, btnOpen);
		//RunTimeData.testingRuntimeGraphStuff(graph);
	}

	// move to interface
	public void show(boolean show) {
		if (show) {
			stage.show();
		}else
			stage.hide();
	}

}
