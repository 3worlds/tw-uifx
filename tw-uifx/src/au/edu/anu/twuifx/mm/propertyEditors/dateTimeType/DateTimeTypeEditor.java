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

package au.edu.anu.twuifx.mm.propertyEditors.dateTimeType;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;

public class DateTimeTypeEditor  extends AbstractPropertyEditor<String, Pane> {
	private static LabelButtonControl view = new LabelButtonControl("Ellipsis16.gif", Images.imagePackage);

	public DateTimeTypeEditor(DateTimeItem property, Pane control) {
		super(property, control);
		// TODO Auto-generated constructor stub
	}

	public DateTimeTypeEditor(DateTimeItem property) {
		this(property,view);
		view.setOnAction(e-> onAction());
	}
	private void onAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValue(String value) {
		LabelButtonControl control = (LabelButtonControl) getEditor();
		control.setText(value);		
	}

	@Override
	protected ObservableValue<String> getObservableValue() {
		LabelButtonControl control = (LabelButtonControl) getEditor();
		return control.getTextProperty();
	}

}
