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
package au.edu.anu.twuifx.dialogs;

import java.util.List;
import au.edu.anu.rscs.aot.collections.tables.StringTable;
import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twcore.experiment.Design;
import au.edu.anu.twcore.experiment.Experiment;
import au.edu.anu.twcore.experiment.Treatment;
import fr.cnrs.iees.graph.Direction;
import fr.cnrs.iees.graph.impl.ALDataEdge;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.twcore.constants.ExperimentDesignType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
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
		if (edt != null && edt.equals(ExperimentDesignType.singleRun)) {
			ta.appendText("Replicates: " + Integer.toString(nReps) + "\n");
			if (nReps > 1)
				ta.appendText("Deployment: parallel\n");
		}
		if (edt != null)
			switch (edt) {
			case crossFactorial: {
				List<List<Property>> lst = exp.getTreatmentList();
				Treatment treatment = (Treatment) get(exp.getChildren(), selectOne(hasTheLabel(N_TREATMENT.label())));
				List<ALDataEdge> treatments = (List<ALDataEdge>) get(treatment.edges(Direction.OUT),
						selectOneOrMany(hasTheLabel(E_TREATS.label())));
				StringBuilder sb = new StringBuilder().append("rep(").append(nReps).append(")");
				int total = nReps;
				for (ALDataEdge e : treatments) {
					StringTable tbl = (StringTable) e.properties().getPropertyValue(P_TREAT_VALUES.key());
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
				List<List<Property>> lst = exp.getTreatmentList();
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
				total *= nReps;
				sb.append(")=").append(" total runs(").append(total).append(")\n");
				String s = sb.toString();
				// s = s.replaceFirst(" + ", "");

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
			default :{
				// do nothing
			}
			}

		dlg.showAndWait();

	}

}
