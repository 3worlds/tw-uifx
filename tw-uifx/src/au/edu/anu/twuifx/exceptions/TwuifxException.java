/**************************************************************************
 *  TW-CORE - 3Worlds Core classes and methods                            *
 *                                                                        *
 *  Copyright 2018: Shayne Flint, Jacques Gignoux & Ian D. Davies         *
 *       shayne.flint@anu.edu.au                                          * 
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-CORE is a library of the principle components required by 3W       *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-CORE (3Worlds Core).                          *
 *                                                                        *
 *  TW-CORE is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-CORE is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-CORE.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
 *                                                                        *
 **************************************************************************/
package au.edu.anu.twuifx.exceptions;

import fr.ens.biologie.generic.Textable;

/**
 * @author shayne.flint@anu.edu.au
 *
 * 
 * 
 */
//NB: this was previously AotException
//Policy is to make an exception at least for each library
//The general advice for exceptions is to throw early and catch late.
public class TwuifxException extends RuntimeException {

	private static final long serialVersionUID = -7822249861382174336L;

	public TwuifxException(Textable item, String message) {
		super("[on " + item + "]\n[" + message + "]");
	}

	public TwuifxException(String message) {
		super("[" + message + "]");
	}

	public TwuifxException(Exception e) {
		super(e);
	}

	public TwuifxException(String message, Exception e) {
		super("[" + message + "]\n[original exception: " + e + "]");
		e.printStackTrace();
	}

}
