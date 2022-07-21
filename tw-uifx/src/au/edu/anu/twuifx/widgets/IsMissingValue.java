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
package au.edu.anu.twuifx.widgets;

import org.apache.commons.lang3.function.TriFunction;

import fr.cnrs.iees.io.parsing.ValidPropertyTypes;

/**
 * Indicates if a value is considered outside the given range.
 * 
 * @author Ian Davies - 19 July 2022
 */
public enum IsMissingValue implements TriFunction<Double, Double, Double, Boolean> {
	/**
	 * true if v < min, false otherwise
	 */
	LT_MIN((min, max, v) -> {
		return ((double) v < (double) min) ? true : false;
	}),
	/**
	 * true if v <= min, false otherwise
	 */
	LTEQ_MIN((min, max, v) -> {
		return ((double) v <= (double) min) ? true : false;
	}),
	/**
	 * true if v >= max, false otherwise
	 */
	GTEQ_MAX((min, max, v) -> {
		return ((double) v >= (double) max) ? true : false;
	}),
	/**
	 * true if v > max, false otherwise
	 */
	GT_MAX((min, max, v) -> {
		return ((double) v > (double) max) ? true : false;
	}),
	/**
	 * Always false
	 */
	NEVER((min, max, v) -> true);

	private TriFunction<Double, Double, Double, Boolean> tf;

	private IsMissingValue(TriFunction<Double, Double, Double, Boolean> f) {
		tf = f;
	}

	@Override
	public Boolean apply(final Double min, final Double max, final Double v) {
		return tf.apply(min, max, v);
	}

	public static IsMissingValue defaultValue() {
		return NEVER;
	}

	static {
		ValidPropertyTypes.recordPropertyType(IsMissingValue.class.getSimpleName(), IsMissingValue.class.getName(),
				IsMissingValue.defaultValue());

	}
}
