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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;

import au.edu.anu.twcore.ecosystem.runtime.timer.TimeUtil;
import au.edu.anu.twuifx.images.Images;
import au.edu.anu.twuifx.mm.propertyEditors.LabelButtonControl;
import fr.cnrs.iees.twcore.constants.TimeScaleType;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.cnrs.iees.omhtk.utils.Duple;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Property editor for {@link DateTimeItem}.
 * 
 * @author Ian Davies - 23 Sep. 2022
 *
 */
public class DateTimeTypeEditor extends AbstractPropertyEditor<String, LabelButtonControl> {
	private LabelButtonControl view;
	private DateTimeItem dtItem;

	/**
	 * @param property The {@link DateTimeItem}
	 * @param control The {@link LabelButtonControl}
	 */
	public DateTimeTypeEditor(Item property, Pane control) {
		super(property, (LabelButtonControl) control);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param property The {@link DateTimeItem}
	 */
	public DateTimeTypeEditor(Item property) {
		this(property, new LabelButtonControl("Ellipsis16.gif", Images.class.getPackageName()));
		view = this.getEditor();
		dtItem = (DateTimeItem) this.getProperty();
		// we need to find the timeline to create the meta-data for time editing
		view.setOnAction(e -> onAction());
	}

	private void onAction() {
		String oldString = (String) dtItem.getValue();
		String newString = oldString;
		newString = editDateTime(oldString);
		if (!newString.equals(oldString)) {
			setValue(newString);
			getProperty().setValue(newString);
		}
	}

	private String editDateTime(String currentValue) {
		Dialog<ButtonType> dlg = new Dialog<ButtonType>();
		dlg.setTitle(getProperty().getName());
		// dlg.initOwner(Dialogs);
		ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
		dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		BorderPane pane = new BorderPane();
		dlg.getDialogPane().setContent(pane);
		HBox top = new HBox();
		pane.setTop(top);
		top.setAlignment(Pos.CENTER);
		top.setSpacing(5);
		top.getChildren().addAll(new Label("Time scale:"), new Label(dtItem.getTimeScaleType().name()));

		GridPane grid = new GridPane();
		pane.setCenter(grid);
		Set<TimeUnits> forbidden = new HashSet<>();
		if (!(dtItem.getTUMin().compareTo(TimeUnits.DECADE) >= 0)) {
			forbidden.add(TimeUnits.DECADE);
			forbidden.add(TimeUnits.CENTURY);
			forbidden.add(TimeUnits.MILLENNIUM);
		}

		if (dtItem.getTimeScaleType().equals(TimeScaleType.GREGORIAN)) {
			forbidden.add(TimeUnits.DECADE);
			forbidden.add(TimeUnits.CENTURY);
			forbidden.add(TimeUnits.MILLENNIUM);
			forbidden.add(TimeUnits.WEEK);
			forbidden.add(TimeUnits.MILLISECOND);
		}
		// TODO we should be able to use units = new
		// ArrayList<>(dtItem.getTimeScaleType().validTimeUnits(smallest,largest));
		SortedSet<TimeUnits> validUnits = TimeScaleType.validTimeUnits(dtItem.getTimeScaleType());
		if (dtItem.getTimeScaleType().equals(TimeScaleType.MONO_UNIT))
			for (TimeUnits unit : validUnits)
				if (!dtItem.getTUMin().equals(unit))
					forbidden.add(unit);

		List<TimeUnits> units = new ArrayList<>();
		for (TimeUnits unit : validUnits)
			if (!forbidden.contains(unit))
				if ((dtItem.getTUMin().compareTo(unit) <= 0) && dtItem.getTUMax().compareTo(unit) >= 0)
					units.add(unit);
		units.sort((t1, t2) -> t1.compareTo(t2));

		long currentSetting = Long.parseLong(currentValue);
		long[] factors = null;
		if (!units.isEmpty()) {
			if (dtItem.getTimeScaleType().equals(TimeScaleType.GREGORIAN))
				factors = TimeUtil.factorInexactTime(currentSetting, units);
			else
				factors = TimeUtil.factorExactTime(currentSetting, units);
		}
		if (units.isEmpty()) {
			units.add(TimeUnits.UNSPECIFIED);
			factors = new long[1];
			factors[0] = currentSetting;
		}
		List<Duple<TimeUnits, Spinner<Integer>>> lstSpinners = new ArrayList<>();
		int i = 0;
		for (TimeUnits unit : units) {
			Spinner<Integer> spinner = new Spinner<>();
			spinner.setEditable(true);
			SpinnerValueFactory<Integer> factory;
			if (units.size() == 1)
				factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-Integer.MAX_VALUE, Integer.MAX_VALUE,
						(int) factors[i]);
			else
				factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-Integer.MAX_VALUE, Integer.MAX_VALUE,
						(int) factors[i]);

			spinner.setValueFactory(factory);
			spinner.setMaxWidth(100);

			// handles null when spinner is +-ed
			spinner.valueProperty().addListener(
					(observableValue, oldValue, newValue) -> handleSpin(spinner, observableValue, oldValue, newValue));

			// This forces the edited value to be committed when losing focus (i.e. leaving
			// the dlg)
			spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue)
					spinner.increment(0);
			});
			lstSpinners.add(new Duple<TimeUnits, Spinner<Integer>>(unit, spinner));
			grid.add(new Label(unit.abbreviation()), units.size() - i, 0);
			grid.add(spinner, units.size() - i, 1);
			i++;
		}
		dlg.setResizable(true);
		Optional<ButtonType> result = dlg.showAndWait();
		if (result.get().equals(ok)) {
			// we assume the shortest is the base unit
			Long time = 0L;
			if (!dtItem.getTimeScaleType().equals(TimeScaleType.GREGORIAN)) {
				for (Duple<TimeUnits, Spinner<Integer>> duple : lstSpinners) {
					int n = duple.getSecond().getValue();
					if (n != 0) {
						long factor = TimeUtil.timeUnitExactConversionFactor(duple.getFirst(), dtItem.getTUMin());
						time += (factor * n);
					}
				}
			} else {
				LocalDateTime baseDateTime = TimeUtil.longToDate(currentSetting, dtItem.getTUMin());
				LocalDateTime dateTime = TimeUtil.longToDate(currentSetting, dtItem.getTUMin());
				for (Duple<TimeUnits, Spinner<Integer>> duple : lstSpinners) {
					long n = TimeUtil.getDateTimeField(baseDateTime, duple.getFirst());
					long diff = duple.getSecond().getValue() - n;
					if (diff != 0)
						dateTime = TimeUtil.getIncrementedDate(dateTime, duple.getFirst(), diff);
				}
				time = TimeUtil.dateToLong(dateTime, dtItem.getTUMin(), baseDateTime);
			}
			return time.toString();

		}
		return currentValue;

	}

	private void handleSpin(Spinner<Integer> spinner, ObservableValue<?> observableValue, Number oldValue,
			Number newValue) {
		try {
			// handle blanks
			if (newValue == null) {
				spinner.getValueFactory().setValue((int) oldValue);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
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
