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

package au.edu.anu.twuifx.mm;

import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

import au.edu.anu.omhtk.Language;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twapps.mm.undo.Caretaker;
import fr.cnrs.iees.io.parsing.ValidPropertyTypes;
import au.edu.anu.twcore.graphState.GraphState;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.project.TwPaths;
import au.edu.anu.twuifx.dialogs.Dialogsfx;
import au.edu.anu.twuifx.graphState.GraphStatefx;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.view.DefaultWindowSettings;
import au.edu.anu.twuifx.mm.view.MmController;
import au.edu.anu.ymuit.ui.colour.PaletteTypes;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Author Ian Davies
 *
 * Date 10 Dec. 2018
 */
public class ModelMakerfx extends Application implements ProjectPaths, TwPaths {
	private Stage mainStage;
	private Parent root;
	private MmController controller;

	private void createMainWindow() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		String skinFile = "view/MmEN.fxml";
		if (Language.French())
			skinFile = "view/MmFR.fxml";
		URL URLView = ModelMakerfx.class.getResource(skinFile);
		loader.setLocation(URLView);
		root = (Parent) loader.load();
		controller = loader.getController();
		Scene scene = new Scene(root);
		mainStage.setScene(scene);
		controller.setStage(mainStage);
		mainStage.getIcons().add(new Image(Images.class.getResourceAsStream("MmIcon16.png")));

		controller.setHostServices(getHostServices());

		scene.getWindow().setOnShown((e) -> {
			final String welcomeWindow = "Welcome_Window";
			Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
			// Uncomment for debugging
			// prefs.putBoolean(welcomeWindow, true);
			if (prefs.getBoolean(welcomeWindow, true))
				showWelcomeWindow(welcomeWindow, prefs, scene.getWindow());

		});
		scene.getWindow().setOnCloseRequest((e) -> {
			if (!controller.canClose()) {
				e.consume();
			} else {
				Caretaker.finalise();
				stop();
			}
		});
	}

	static {
//	// on behalf of PaletteTypes in ymuit which has no dependence on omugi
	ValidPropertyTypes.recordPropertyType(PaletteTypes.class.getSimpleName(), PaletteTypes.class.getName(),
			PaletteTypes.defaultValue());
	}	

	@Override
	public void start(Stage primaryStage) throws Exception {
		/**
		 * setUserAgentStylesheet(STYLESHEET_CASPIAN);
		 * 
		 * Not used because this stylesheet elicits a warning from search bar of
		 * propertySheet.
		 */
		mainStage = primaryStage;
		mainStage.setTitle(DefaultWindowSettings.defaultName());
		createMainWindow();
		Dialogs.setImplementation(new Dialogsfx(root.getScene().getWindow()));
		GraphState.initialise(new GraphStatefx(mainStage.titleProperty(), controller.getUserProjectPathProperty()));
		GraphState.addListener(controller);
		mainStage.setHeight(DefaultWindowSettings.getHeight());
		mainStage.setWidth(DefaultWindowSettings.getWidth());
		mainStage.setX(DefaultWindowSettings.getX());
		mainStage.setY(DefaultWindowSettings.getY());
		mainStage.show();
	}

	// If we end up with many suppressable msg boxes then develop language files for
	// this
	// situation including a 'reset defaults' option.
	private static String enFirstTimeMsg = "To begin a new project, select\n'Projects → New → Templates → 1 Blank'.\n\n"
			+ "To read the tutorial exercises, select\nHelp → Tutorials'.\n\n"
			+ "To create and run example models, select\n'Projects -> New -> Tutorials...'.";
	private static String enTitle = "Welcome to ModelMaker";
	private static String enHeader = "Getting started";
	private static String enChBxSuppress = "Don't show again";

	private static String frFirstTimeMsg = "Pour démarrer un nouveau projet, sélectionnez\n"
			+ "<<Projets → Nouveau → Templates → 1 Blank>>.\n\n"
			+ "Pour lire la documentation des tutoriels, sélectionnez \n<<Aide → Tutoriels>>.\n\n"
			+ "Pour créer et exécuter des exemples de modèles, sélectionnez\n<<Projets → Nouveau → Tutoriels...>>.";
	private static String frTitle = "Bienvenue dans ModelMaker";
	private static String frHeader = "Commencer";
	private static String frChBxSuppress = "Ne plus afficher";

	private void showWelcomeWindow(String key, Preferences prefs, Window parent) {
		Stage stage = new Stage();
		stage.initOwner(parent);
		stage.initModality(Modality.NONE);
		HBox root = new HBox();
		ImageView imageView = new ImageView(new Image(Images.class.getResourceAsStream("3worlds-5.jpg")));
		imageView.preserveRatioProperty().set(true);
		imageView.setFitHeight(300);
		VBox leftContent = new VBox();
		leftContent.setAlignment(Pos.BASELINE_RIGHT);
		Label l1 = new Label("Three Worlds");
		Label l2 = new Label("M. C. Escher (1955)");
		leftContent.getChildren().addAll(imageView, l1, l2, new Label(""));
		Scene scene = new Scene(root);
		Button btnClose = new Button("Close");
//		btnClose.setStyle("-fx-background-color: DarkOrange");
		HBox bottom = new HBox();
		bottom.setSpacing(20);
		CheckBox chBxSuppress = new CheckBox(enChBxSuppress);
		chBxSuppress.setOnAction((e) -> {
			prefs.putBoolean(key, !chBxSuppress.isSelected());
		});

		bottom.getChildren().addAll(chBxSuppress, btnClose);
		BorderPane rightContent = new BorderPane();
		rightContent.setBottom(bottom);
		BorderPane.setMargin(bottom, new Insets(12, 12, 12, 12));
		bottom.setAlignment(Pos.CENTER_RIGHT);
		stage.setScene(scene);
		stage.setX(mainStage.getX() + 0.3 * mainStage.getWidth());
		stage.setY(mainStage.getY() + 0.3 * mainStage.getHeight());
		Label header = new Label();
		BorderPane.setAlignment(header, Pos.CENTER);
		BorderPane.setMargin(header, new Insets(12, 12, 12, 12));
		rightContent.setTop(header);
		BorderPane.setAlignment(header, Pos.CENTER);
		TextArea txAreaInfo = new TextArea();
		txAreaInfo.setEditable(false);
		txAreaInfo.setPrefWidth(470);
		// prevent user selection
		txAreaInfo.setTextFormatter(new TextFormatter<String>(change -> {
			change.setAnchor(change.getCaretPosition());
			return change;
		}));
		btnClose.setOnAction((e) -> {
			stage.close();
		});

		HBox content = new HBox();
		content.getChildren().addAll(leftContent, txAreaInfo);
		rightContent.setCenter(content);
		root.getChildren().addAll(leftContent, rightContent);
		// root.setSpacing(10);
		if (Language.French()) {
			stage.setTitle(frTitle);
			header.setText(frHeader);
			txAreaInfo.setText(frFirstTimeMsg);
			chBxSuppress.setText(frChBxSuppress);
		} else {
			stage.setTitle(enTitle);
			header.setText(enHeader);
			txAreaInfo.setText(enFirstTimeMsg);
			chBxSuppress.setText(enChBxSuppress);
		}
		stage.show();

	}

	@Override
	public void stop() {
		controller.putPreferences();
		Platform.exit();
		System.exit(0);
	}

	public static void main(String[] args) {
		System.out.println("Before 'launch(args)'");
		launch(args);
	}

}
