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

import au.edu.anu.twuifx.modelLibrary.templates.TemplatesDummy;
import au.edu.anu.twuifx.modelLibrary.tutorials.TutorialsDummy;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.io.GraphImporter;
import au.edu.anu.twuifx.modelLibrary.models.ModelsDummy;

/**
 * A lookup enum for entries in ModelMaker "New" menu. Display order is the
 * declaration order in this enum. Entries are grouped as either a Template,
 * Tutorial or Model with utg files placed in the appropriate package.
 */
public enum LibraryTable {
	/*-			   Menu name,		|     File name,	|         category,          |  package file assocation */
	Template1(/**/"1 Blank", /*          */"Blank.utg", /*        */LibraryType.Template, TemplatesDummy.class), //
	Template2(/**/"2 SimpleClock", /*    */"SimpleClock_1.utg", /**/LibraryType.Template, TemplatesDummy.class), //
	//
	Tut1(/*     */"1 Logistic", /*     */"Logistic_1.utg", /*     */LibraryType.Tutorial, TutorialsDummy.class), //
	Tut2(/*     */"2 LotkaVolterra", /**/"LotkaVolterra_1.utg", /**/LibraryType.Tutorial, TutorialsDummy.class), //
	Tut3(/*     */"3 I.D.H.(clock)", /**/"IdhClock.utg", /*		  */LibraryType.Tutorial, TutorialsDummy.class), //
	//Tut6(/*     */"4 I.D.H.", /*	   */"Idh_2.utg", /*		  */LibraryType.Tutorial, TutorialsDummy.class), //
	Tut4(/*     */"4 I.D.H.(event)", /**/"IdhEvent.utg", /*		  */LibraryType.Tutorial, TutorialsDummy.class), //
	Tut5(/*     */"5 Panmixia", /*	   */"Panmixia.utg", /*		  */LibraryType.Tutorial, TutorialsDummy.class), //
	Tut6(/*     */"6 Spatial", /*	   */"Spatial.utg", /*		  */LibraryType.Tutorial, TutorialsDummy.class), //
	Tut7(/*     */"7 Boids", /*	   		*/"Flock.utg", /*		  */LibraryType.Tutorial, TutorialsDummy.class), //
	//
	//Model1(/*   */"Genetics", /*       */"Gddm_1.utg", /**/LibraryType.Model, ModelsDummy.class), //
	;

	private final String displayName;
	private final String fileName;
	private final LibraryType libraryType;
	private final Class<?> pkclass;

	private LibraryTable(String displayName, String fileName, LibraryType lt, Class<?> pkclass) {
		this.displayName = displayName;
		this.fileName = fileName;
		this.libraryType = lt;
		this.pkclass = pkclass;
	}

	public String displayName() {
		return displayName;
	}

//	public String fileName() {
//		return fileName;
//	}

	public LibraryType libraryType() {
		return libraryType;
	}

	@SuppressWarnings("unchecked")
	public TreeGraph<TreeGraphDataNode, ALEdge> getGraph() {
		return (TreeGraph<TreeGraphDataNode, ALEdge>) GraphImporter.importGraph(fileName, pkclass);

	}

}
