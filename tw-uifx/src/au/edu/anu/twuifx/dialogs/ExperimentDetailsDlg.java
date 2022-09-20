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

import au.edu.anu.twcore.experiment.Experiment;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ButtonBar.ButtonData;
import static au.edu.anu.rscs.aot.queries.CoreQueries.*;
import static au.edu.anu.rscs.aot.queries.base.SequenceQuery.get;
import static fr.cnrs.iees.twcore.constants.ConfigurationNodeLabels.*;

/**
 * @author Ian Davies - 5 Jan 2022
 */
public class ExperimentDetailsDlg {
	/**
	 * Show a dialog box to display the experiment design of a 3Worlds configuration graph.
	 * 
	 * @param config The configuration graph
	 */
	public ExperimentDetailsDlg(TreeGraph<TreeGraphDataNode, ALEdge> config) {
		Experiment exp = (Experiment) get(config.root().getChildren(), selectOne(hasTheLabel(N_EXPERIMENT.label())));

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
		ta.appendText(exp.getExperimentDesignDetails().toDetailString());

		dlg.showAndWait();

	}

}
