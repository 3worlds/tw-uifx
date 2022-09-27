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

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

/**
 * Static class of TextFormatters.
 * 
 * @author Ian Davies -15 Sept 2021
 */
public class TextFilters {
	private static Pattern doublePattern = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

	private TextFilters() {
	};

	/**
	 * A formatter for enforcing correctly formed doubles in text fields.
	 * 
	 * @param def Default value
	 * @return A double formatter.
	 * @see <a href=
	 *      "https://stackoverflow.com/questions/45977390/how-to-force-a-double-input-in-a-textfield-in-javafx">
	 *      Stack overflow.</a>
	 */
	public static TextFormatter<Double> getDoubleFormatter(Double def) {
		return new TextFormatter<>(converter, def, getDoubleFilter());
	}

	private static UnaryOperator<TextFormatter.Change> getDoubleFilter() {
		UnaryOperator<TextFormatter.Change> result = c -> {
			String text = c.getControlNewText();
			if (doublePattern.matcher(text).matches()) {
				return c;
			} else {
				return null;
			}
		};
		return result;

	}

	private static StringConverter<Double> converter = new StringConverter<Double>() {

		@Override
		public Double fromString(String s) {
			if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
				return 0.0;
			} else {
				return Double.valueOf(s);
			}
		}

		@Override
		public String toString(Double d) {
			return d.toString();
		}
	};
}
