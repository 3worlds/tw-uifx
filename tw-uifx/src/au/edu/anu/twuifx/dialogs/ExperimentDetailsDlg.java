package au.edu.anu.twuifx.dialogs;

import java.util.List;
import java.util.Optional;

import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.experiment.Design;
import au.edu.anu.twcore.experiment.Experiment;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.twcore.constants.ExperimentDesignType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javafx.scene.control.ButtonBar.ButtonData;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.get;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_DESIGN_TYPE;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_EXP_NREPLICATES;

public class ExperimentDetailsDlg {
	public ExperimentDetailsDlg(TreeGraph<TreeGraphDataNode, ALEdge> g) {
		Experiment exp = (Experiment) get(g.root().getChildren(), selectOne(hasTheLabel(N_EXPERIMENT.label())));
		int nReps = 1;
		if (exp.properties().hasProperty(P_EXP_NREPLICATES.key()))
			nReps = (Integer) exp.properties().getPropertyValue(P_EXP_NREPLICATES.key());
		Design dsgn = (Design) get(exp.getChildren(), selectOne(hasTheLabel(N_DESIGN.label())));
		ExperimentDesignType edt = null;
		String expType = "From file";
		String expDesc = "";

		if (dsgn.properties().hasProperty(P_DESIGN_TYPE.key())) {
			edt = (ExperimentDesignType) dsgn.properties().getPropertyValue(P_DESIGN_TYPE.key());
			expType = edt.name();
			expDesc = edt.description();
		}

		ButtonType close = new ButtonType("Close", ButtonData.OK_DONE);
		Dialog<ButtonType> dlg = new Dialog<>();
		dlg.setTitle("Experiment details");
		dlg.initOwner((Window) Dialogs.owner());
		dlg.getDialogPane().getButtonTypes().addAll(close);
		dlg.setResizable(true);
		BorderPane borderPane = new BorderPane();
		dlg.getDialogPane().setContent(borderPane);
		ScrollPane scrollPane = new ScrollPane();
		borderPane.setCenter(scrollPane);
		GridPane gridPane = new GridPane();
		scrollPane.setContent(gridPane);
//		gridPane.setVgap(15);
		gridPane.setHgap(10);

		// col,row
		TextField tf;
		gridPane.add(new Label("Type"), 0, 0);
		tf = new TextField(expType);
		tf.setEditable(false);
		gridPane.add(tf, 1, 0);
		gridPane.add(new Label("Description"), 0, 1);
		tf = new TextField(expDesc);
//		tf.setMinWidth(Region.USE_PREF_SIZE);
		tf.setPrefWidth(400);
		tf.setEditable(false);
		gridPane.add(tf, 1, 1);
		gridPane.add(new Label("Replicates"), 0, 2);
		tf = new TextField(Integer.toString(nReps));
		tf.setEditable(false);
		gridPane.add(tf, 1, 2);
		if (edt != null && edt.equals(edt.crossFactorial)) {
			gridPane.add(new Label("Simulator"), 0, 3);
			gridPane.add(new Label("Factors"), 1, 3);
			List<List<Property>> lst = Experiment.buildSimpleFactorialTreatmentList(exp);
			int sim = 0;
			for (int r = 0; r < nReps; r++)
				for (List<Property> factors : lst) {
					String f = factors.toString().replaceAll("Property:", "");
					f = f.replace("[[", "");
					f = f.replace("]]", "");
					f = f.replace("]","");
					f = f.replace("[","");
					gridPane.add(new Label(Integer.toString(sim)), 0, sim+4);
					tf = new TextField(f);
					tf.setEditable(false);

//					tf.setPrefWidth(1000);
					gridPane.add(tf, 1, sim+4);
					sim++;
				}
		}
//		for (int c = 0; c < gridPane.getColumnCount(); c++) {
//			ColumnConstraints cc = new ColumnConstraints();
//			if (c==gridPane.getColumnCount()-1) {
//				cc.setFillWidth(true);
//				cc.setHgrow(Priority.ALWAYS);
//			
//			}else {
//				cc.setFillWidth(false);
//				cc.setHgrow(Priority.NEVER);
//			}
//			gridPane.getColumnConstraints().add(cc);
//
//		}

//		Optional<ButtonType> result = dlg.showAndWait();
		dlg.show();

	}

}
