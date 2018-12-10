package au.edu.anu.twuifx.mm;

import java.io.File;
import java.io.IOException;

import javax.tools.ToolProvider;

import au.edu.anu.rscs.aot.logging.Logger;
import au.edu.anu.rscs.aot.logging.LoggerFactory;
import fr.ens.biologie.threeWorlds.ui.modelMakerfx.util.Dialogs;
import fr.ens.biologie.threeWorlds.ui.modelMakerfx.util.Utilities;
import fr.ens.biologie.threeWorlds.ui.modelMakerfx.view.MmController;
import fr.ens.biologie.threeWorlds.build.jar.TwDepJar;
import fr.ens.biologie.threeWorlds.resources.core.constants.ProjectPaths;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author Ian Davies
 *
 *
 *         29 Nov. 2017
 */
public class ModelMakerfx extends Application {
	private Stage mainStage;
	private Parent root;
	private Logger log = LoggerFactory.getLogger(ModelMakerfx.class, "3Worlds");
	private MmController controller;
	static {
		Utilities.loadDefaultLogging();
	}

	private void createMainWindow() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ModelMakerfx.class.getResource("view/Mm.fxml"));
		root = (Parent) loader.load();
		controller = loader.getController();
		Scene scene = new Scene(root);
		mainStage.setScene(scene);

		controller.setStage(mainStage);

		scene.getWindow().setOnCloseRequest((e) -> {
			if (!controller.model().canClose("closing")) {
				e.consume();
			
			} else {
				Platform.exit();
//				System.exit(0);
			}
		});
	}

	private void checkResources() {
		File file = new File(
				ProjectPaths.USER_ROOT + File.separator + ProjectPaths.TW_ROOT + File.separator + TwDepJar.TW_DEP_JAR);
		if (!file.exists())
			Dialogs.errorAlert("Resource Error", "Required Java dependency jar not found",
					"Use TwSetup to create " + file.getAbsolutePath());

		javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
			Dialogs.errorAlert("Resource Error", "Java compiler not found",
					"Check you have the Java Development Kit installed");

		if (!file.exists() || compiler == null) {
			Platform.exit();
			System.exit(0);
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		checkResources();
		// setUserAgentStylesheet(STYLESHEET_CASPIAN); // causes stylesheet warning from
		// search bar of propertySheet
		mainStage = primaryStage;
		mainStage.setTitle("3Worlds Model Maker");
		createMainWindow();
		Dialogs.setParent(root.getScene().getWindow());
		getFramePreferences();
		mainStage.show();

	}

	private void getFramePreferences() {
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		double w = screenBounds.getWidth() / 1.5;
		double h = w / 1.618;
		double x = (screenBounds.getWidth() - w) / 2;
		double y = (screenBounds.getHeight() - h) / 3;
		mainStage.setWidth(w);
		mainStage.setHeight(h);
		mainStage.setX(x);
		mainStage.setY(y);

	}

	@Override
	public void stop() {
		controller.putPreferences();
		log.debug("Stopping program");
		// Without this, threads remain when debugging.
		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
