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

package au.edu.anu.twuifx.mm.propertyEditors.borderList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twapps.dialogs.DialogsFactory;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.omugi.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.twcore.constants.BorderListType;
import fr.cnrs.iees.twcore.constants.BorderType;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

/**
 * Property editor for {@link BorderListItem}.
 * 
 * @author Ian Davies - 25 Sep 2020
 */
public class BorderListEditor extends AbstractPropertyEditor<String, LabelButtonControl> {

	/**
	 * @param property The {@link BorderListItem}
	 * @param control The {@link LabelButtonControl}.
	 */
	public BorderListEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
	}

	/**
	 * @param property The {@link BorderListItem}
	 */
	public BorderListEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.class.getPackageName()));
		this.getEditor().setOnAction(e -> onAction());
	}

	private void onAction() {
		BorderListItem item = (BorderListItem) getProperty();
		TreeGraphDataNode spaceNode = (TreeGraphDataNode) item.getElement();
		BorderListType currentBLT = (BorderListType) spaceNode.properties().getPropertyValue(P_SPACE_BORDERTYPE.key());

		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setResizable(true);
		dlg.setTitle(item.getElement().toShortString() + "#" + P_SPACE_BORDERTYPE.key());
		dlg.initOwner((Window) DialogsFactory.owner());
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

		GridPane content = new GridPane();
		content.setVgap(2);
		content.setHgap(2);

		content.add(new Label("dim "), 0, 0);
		content.add(new Label("sym "), 1, 0);
		content.add(new Label("lower bound"), 2, 0);
		content.add(new Label("upper bound"), 3, 0);
		int nDims = currentBLT.size() / 2;

		List<ComboBox<BorderType>> cmbLower = new ArrayList<>();
		List<ComboBox<BorderType>> cmbUpper = new ArrayList<>();
		List<CheckBox> chkBxSymm = new ArrayList<>();
		for (int i = 0; i < nDims; i++) {
			cmbLower.add(new ComboBox<>());
			cmbLower.get(i).setOnAction(e -> {
				int idx = cmbLower.indexOf(e.getSource());
				BorderType lo = cmbLower.get(idx).getSelectionModel().getSelectedItem();
				BorderType hi = cmbUpper.get(idx).getSelectionModel().getSelectedItem();
				if (lo.equals(BorderType.wrap) || lo.equals(hi)) {
					chkBxSymm.get(idx).setSelected(true);
					if (lo.equals(BorderType.wrap))
						cmbUpper.get(idx).getSelectionModel().select(lo);
					cmbUpper.get(idx).setVisible(false);
				}
			});

			cmbUpper.add(new ComboBox<>());
			cmbUpper.get(i).setOnAction(e -> {
				int idx = cmbUpper.indexOf(e.getSource());
				BorderType lo = cmbLower.get(idx).getSelectionModel().getSelectedItem();
				BorderType hi = cmbUpper.get(idx).getSelectionModel().getSelectedItem();
				if (hi.equals(BorderType.wrap) || lo.equals(hi)) {
					chkBxSymm.get(idx).setSelected(true);
					if (hi.equals(BorderType.wrap))
						cmbLower.get(idx).getSelectionModel().select(hi);
					cmbUpper.get(idx).setVisible(false);
				}
			});

			CheckBox cb = new CheckBox("");
			chkBxSymm.add(cb);
			cb.setOnAction((e) -> {
				// Take care not to assume order of BorderType enum
				CheckBox src = (CheckBox) e.getSource();
				int idx = chkBxSymm.indexOf(src);
				cmbUpper.get(idx).setVisible(!src.isSelected());
				int wrapIdx = BorderType.wrap.ordinal();
				int nValues = BorderType.values().length;
				if (!src.isSelected()) {// if asymm
					int lo = cmbLower.get(idx).getSelectionModel().getSelectedIndex();
					int hi = cmbUpper.get(idx).getSelectionModel().getSelectedIndex();
					// neither bound can be wrap (i.e. 0)
					while (lo == wrapIdx) {
						lo = (lo + 1) % nValues;
					}
					if (hi == lo || hi == wrapIdx) {
						hi = (hi + 1) % nValues;
						while (hi == wrapIdx || hi == lo)
							hi = (hi + 1) % nValues;
					}
					cmbLower.get(idx).getSelectionModel().select(lo);
					cmbUpper.get(idx).getSelectionModel().select(hi);
				}
			});
			Label lbl = new Label(Integer.toString(i + 1));
			content.add(lbl, 0, i + 1);
			content.add(cb, 1, i + 1);
			content.add(cmbLower.get(i), 2, i + 1);
			content.add(cmbUpper.get(i), 3, i + 1);

			GridPane.setHalignment(lbl, HPos.CENTER);
			GridPane.setHalignment(cmbLower.get(i), HPos.RIGHT);
			GridPane.setHalignment(cmbUpper.get(i), HPos.LEFT);
			String lower = currentBLT.getWithFlatIndex(i * 2);
			String upper = currentBLT.getWithFlatIndex(i * 2 + 1);
			cmbLower.get(i).getItems().addAll(BorderType.values());
			cmbUpper.get(i).getItems().addAll(BorderType.values());
			cmbLower.get(i).getSelectionModel().select(BorderType.valueOf(lower));
			cmbUpper.get(i).getSelectionModel().select(BorderType.valueOf(upper));

			if (upper.equals(lower)) {
				cb.setSelected(true);
				cmbUpper.get(i).setVisible(false);
			}

		}

		dlg.getDialogPane().setContent(content);

		Optional<ButtonType> result = dlg.showAndWait();
		String entry = "";
		if (result.get().equals(ok)) {
			for (int i = 0; i < nDims; i++) {
				String lower = cmbLower.get(i).getSelectionModel().getSelectedItem().name();
				String upper = lower;
				if (!chkBxSymm.get(i).isSelected())
					upper = cmbUpper.get(i).getSelectionModel().getSelectedItem().name();
				entry += "," + lower;
				entry += "," + upper;
			}
			entry = entry.replaceFirst(",", "");
			String value = "([" + nDims * 2 + "]" + entry + ")";

			// NB Leave it to the queries to validate these settings.
			// Update: query should no longer be required.
			setValue(value);

		}

	}

	@Override
	public void setValue(String value) {
		getEditor().setText(value);
	}

	@Override
	protected ObservableValue<String> getObservableValue() {
		return getEditor().getTextProperty();
	}

}
