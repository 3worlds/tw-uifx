/**************************************************************************
 *  TW-APPS - Applications used by 3Worlds                                *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            * 
 *                                                                        *
 *  TW-APPS contains ModelMaker and ModelRunner, programs used to         *
 *  construct and run 3Worlds configuration graphs. All code herein is    *
 *  independent of UI implementation.                                     *
 *                                                                        *
 **************************************************************************                                       
 *  This file is part of TW-APPS (3Worlds applications).                  *
 *                                                                        *
 *  TW-APPS is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-APPS is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *                         
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-APPS.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>                   *
  **************************************************************************/

package au.edu.anu.twuifx.modelLibrary;

/**
 * @author Ian Davies
 *
 * @date 18 Nov 2019
 */
// Lookup struc for entries in ModelMaker "New" menu. Display order is the declaration order in this enum.
public enum LibraryTable {
	Empty(/*             */"Empty",/*                    */"vide.ugt"), //
	Basic1(/*            */"Basic 1",/*                  */"deBase1.ugt"), //
	Basic2(/*            */"Basic 2",/*                  */"deBase2.ugt"), //
	SimplePhysical(/*    */"Basic physical model",/*     */ "physiqueSimple.ugt"), //
	TestTrack7(/*        */"Data tracker 7",/*           */ "traqueurDeDonnées7.ugt"), //
	;

	private final String displayName;
	private final String fileName;

	private LibraryTable(String displayName, String fileName) {
		this.displayName = displayName;
		this.fileName = fileName;
	}

	public String displayName() {
		return displayName;
	}

	public String fileName() {
		return fileName;
	}

}
