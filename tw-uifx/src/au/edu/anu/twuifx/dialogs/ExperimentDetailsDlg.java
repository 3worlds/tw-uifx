package au.edu.anu.twuifx.dialogs;

import java.util.List;
import java.util.Optional;

import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twapps.dialogs.Dialogs;
import au.edu.anu.twcore.experiment.Design;
import au.edu.anu.twcore.experiment.Experiment;
import au.edu.anu.twcore.experiment.Treatment;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.twcore.constants.ExperimentDesignType;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
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
import static fr.cnrs.iees.twcore.constants.ConfigurationEdgeLabels.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

public class ExperimentDetailsDlg {
	@SuppressWarnings("unchecked")
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
		// dlg.initOwner((Window) Dialogs.owner());
		dlg.getDialogPane().getButtonTypes().addAll(close);
		dlg.setResizable(true);
		BorderPane borderPane = new BorderPane();
		dlg.getDialogPane().setContent(borderPane);
		TextArea ta = new TextArea();
		borderPane.setCenter(ta);
		ta.setEditable(false);
		ta.appendText("Type: " + expType + "\n");
		ta.appendText("Description: " + expDesc + "\n");
		if (edt != null && edt.equals(edt.singleRun)) {
			ta.appendText("Replicates: " + Integer.toString(nReps) + "\n");
			if (nReps > 1)
				ta.appendText("Deployment: parallel\n");
		}
		switch (edt) {
		case crossFactorial: {
			List<List<Property>> lst = Experiment.buildTreatmentList(edt, exp);
			Treatment treatment = (Treatment) get(exp.getChildren(), selectOne(hasTheLabel(N_TREATMENT.label())));
			List<ALDataEdge> treatments = (List<ALDataEdge>) get(treatment.edges(Direction.OUT),
					selectOneOrMany(hasTheLabel(E_TREATS.label())));
			StringBuilder sb = new StringBuilder().append("rep(").append(nReps).append(")");
			int total = nReps;
			for (ALDataEdge e : treatments) {
				StringTable tbl = (StringTable) e.properties().getPropertyValue(P_TREAT_VALUES.key());
				String p = e.endNode().id();
				sb.append(" x ").append(e.endNode().id()).append("(").append(tbl.size()).append(")");
				total *= tbl.size();
			}
			sb.append("=").append(" total runs(").append(total).append(")\n");
			ta.appendText("Design: " + sb.toString());
			ta.appendText("Simulator: Factors:\n");
			int sim = 0;
			for (int r = 0; r < nReps; r++)
				for (List<Property> factors : lst) {
					String f = factors.toString().replaceAll("Property:", "");
					f = f.replace("[[", "");
					f = f.replace("]]", "");
					f = f.replace("]", "");
					f = f.replace("[", "");
					ta.appendText(Integer.toString(sim++) + ": " + f + "\n");
				}
			break;
		}
		// TODO repetitive code
		case sensitivityAnalysis: {
			List<List<Property>> lst = Experiment.buildTreatmentList(edt, exp);
			Treatment treatment = (Treatment) get(exp.getChildren(), selectOne(hasTheLabel(N_TREATMENT.label())));
			List<ALDataEdge> treatments = (List<ALDataEdge>) get(treatment.edges(Direction.OUT),
					selectOneOrMany(hasTheLabel(E_TREATS.label())));
			StringBuilder sb = new StringBuilder().append("rep(").append(nReps).append(") x (");
			int total = 0;
			for (ALDataEdge e : treatments) {
				StringTable tbl = (StringTable) e.properties().getPropertyValue(P_TREAT_VALUES.key());
				String p = e.endNode().id();
				sb.append(" + ").append(e.endNode().id()).append("(").append(tbl.size()).append(")");
				total += tbl.size();
			}
			total *=nReps;
			sb.append(")=").append(" total runs(").append(total).append(")\n");
			String s = sb.toString();
			//s = s.replaceFirst(" + ", "");
			
	
			ta.appendText("Design: " + s);
			ta.appendText("Simulator: Factors:\n");
			int sim = 0;
			for (int r = 0; r < nReps; r++)
				for (List<Property> factors : lst) {
					String f = factors.toString().replaceAll("Property:", "");
					f = f.replace("[[", "");
					f = f.replace("]]", "");
					f = f.replace("]", "");
					f = f.replace("[", "");
					ta.appendText(Integer.toString(sim++) + ": " + f + "\n");
				}
			break;
		}
		}

		dlg.showAndWait();

	}

}
