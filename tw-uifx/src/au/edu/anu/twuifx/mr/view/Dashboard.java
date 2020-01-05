package au.edu.anu.twuifx.mr.view;

import org.controlsfx.control.PropertySheet;

import au.edu.anu.twapps.dialogs.Dialogs;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToolBar;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @author Ian Davies
 *
 * @date 3 Jan 2020
 */
public class Dashboard {
	private TreeGraph<TreeGraphDataNode, ALEdge> graph;
	private BooleanProperty showToggle;
	private Stage stage;
	private Button btnApply;
	private Button btnSave;
	private Button btnOpen;
	private PropertySheet propertySheet;
	
	

	public Dashboard(TreeGraph<TreeGraphDataNode, ALEdge> graph, Stage ownerStage, BooleanProperty showToggle) {
		this.graph = graph;
		this.showToggle = showToggle;
		BorderPane content = new BorderPane();
		Scene scene = new Scene(content, 400, 500);
		stage = new Stage();
		stage.setTitle("Dashboard");
		stage.setScene(scene);
		stage.initOwner((Window) Dialogs.owner());
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
		bottomContent.getChildren().addAll(btnApply,btnSave,btnOpen);
		RunTimeData.listStuff(graph);
		

	}

	public void show(boolean show) {
		if (show)
			stage.show();
		else
			stage.hide();
	}

}
