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

import java.util.Optional;

import org.controlsfx.control.PropertySheet;

import au.edu.anu.twapps.dialogs.DialogsFactory;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

/**
 * @author Ian Davies -22 Jan 2020
 */
public class ISParametersDlg {
//	private Button btnSave;
//	private Button btnOpen;
	private PropertySheet propertySheet;
	private Dialog<ButtonType> dlg;
	private ButtonType ok;
	
		/**
		 * A dialog to edit the values of constants in a run-time dynamic graph.
		 * <p>
		 * NB: this has not been enabled yet.
		 * @param dynamicGraph A {@link TreeGraph} of run-time state.
		 */
		public ISParametersDlg(TreeGraph<TreeGraphDataNode, ALEdge> dynamicGraph) {
		dlg = new Dialog<ButtonType>();
		dlg.setTitle("Edit parameters");
		dlg.initOwner((Window) DialogsFactory.owner());
		ok = new ButtonType("Ok", ButtonData.OK_DONE);

		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane content = new BorderPane();
		dlg.getDialogPane().setContent(content);
		propertySheet = new PropertySheet();
		content.setCenter(propertySheet);
		// populate the propertysheet;
		
		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
		}
		
	};

}
