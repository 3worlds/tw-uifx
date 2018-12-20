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

package au.edu.anu.twuifx.mr;

import java.io.File;
import java.io.InputStream;

import au.edu.anu.rscs.aot.util.Resources;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Ian Davies
 * @date 23 Sep. 2018
 * 
 * 
 */
public class MrSplash extends Preloader {
	public static long startTime;
	private static Stage stage;
	private Scene scene;
	private TextField lblNodeName;
	private ProgressBar progressBar;

	public static void hideStage() {
		stage.hide();
	}

	/*
	 * this could be a separate method that returns a Pair<TextField,ProgressBar>
	 * and be used for an about box
	 */
	@Override
	public void init() throws Exception {
		// construct the splash ui scene
		Platform.runLater(() -> {
			BorderPane root = new BorderPane();

			TextField label = new TextField("Initialising simulator...");
			label.setAlignment(Pos.CENTER);

			progressBar = new ProgressBar();
			progressBar.setMaxWidth(Double.MAX_VALUE);

			lblNodeName = new TextField("");
			lblNodeName.setEditable(false);
			lblNodeName.setFocusTraversable(false);

			label.setEditable(false);
			label.setFocusTraversable(false);

			VBox bottom = new VBox();

			bottom.getChildren().addAll(label, progressBar, lblNodeName);
			VBox.setVgrow(progressBar, Priority.ALWAYS);

			Label title = new Label("ThreeWorlds");
//			title.setStyle("-fx-effect: innershadow(gaussian, gray, 5, 1.0, 0, 0);");
			title.setFont(Font.font("System", 40));
			BorderPane.setAlignment(title, Pos.CENTER);
			title.setStyle("-fx-font-weight: bold;");
			title.setOpacity(0.8);
//			title.setStyle("-fx-effect: innershadow(gaussian, gray, 5, 1.0, 0, 0);");

			Image image;
			if (MrLauncher.runningFromJAR()) {
				InputStream ins = MrLauncher
						.getProjectResource("au/edu/anu/twuifx/images/3worlds-5.jpg");
				image = new Image(ins);
			} else {
				File file = Resources.getFile("3worlds-5.jpg", "au.edu.anu.twuifx.images");
				image = new Image(file.toURI().toString());
			}
			ImageView imageView = new ImageView(image);
			imageView.preserveRatioProperty().set(true);

			root.setTop(title);
			root.setCenter(imageView);
			root.setBottom(bottom);
			root.setStyle("-fx-effect: innershadow(gaussian, gray, 10, 0.3, 0, 0);");
			new GaussianBlur();

			scene = new Scene(root);
		});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setScene(scene);
//		stage.getScene()setStyle("-fx-effect: innershadow(gaussian, #039ed3, 2, 1.0, 0, 0);"); 
		startTime = System.currentTimeMillis();
		stage.show();
	}

	@Override
	public void handleApplicationNotification(PreloaderNotification info) {
		if (info instanceof ProgressNotification) {
			ProgressNotification note = (ProgressNotification) info;
			progressBar.setProgress(note.getProgress());
			lblNodeName.setText(ModelRunnerfx.getInitNodeName() + "...");
		}
	}
}
