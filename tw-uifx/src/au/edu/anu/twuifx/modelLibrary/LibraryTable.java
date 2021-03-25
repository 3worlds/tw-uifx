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
import au.edu.anu.twuifx.modelLibrary.models.ModelsDummy;
import au.edu.anu.twuifx.modelLibrary.tests.TestsDummy;
import fr.cnrs.iees.graph.impl.ALEdge;
import fr.cnrs.iees.graph.impl.TreeGraph;
import fr.cnrs.iees.graph.impl.TreeGraphDataNode;
import fr.cnrs.iees.graph.io.GraphImporter;

/**
 * A lookup enum for entries in ModelMaker "New" menu. Display order is the
 * declaration order in this enum. Entries are grouped as either a Template,
 * Tutorial or Model with utg files placed in the appropriate package.
 * 
 * This whole system can be moved to TW-APPS.
 */
/**
 * @author Ian Davies
 *
 * @date 11 Nov. 2020
 * 
 * TODO: Move this system to tw-core to prevent rebuilding the time consuming twuifx lib.
 */
public enum LibraryTable {
	/*-	Menu name,	|	ProposedName|	File name,	|	category,	|package file association */
	Template1("1 Blank", "Prj1", "Blank.utg", LibraryType.Template, TemplatesDummy.class), //
	Template2("2 SimpleClock", "Prj1", "SimpleClock_1.utg", LibraryType.Template, TemplatesDummy.class), //
	//
	Tut1("1 Logistic", "Logistic1", "Logistic_1.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	Tut2("2 LotkaVolterra", "LotkaVolterra1", "LotkaVolterra_1.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	Tut3("3 I.D.H.(clock)", "IdhClock1", "IdhClock.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	Tut4("4 I.D.H.(event)", "IdhEvent1", "IdhEvent.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	Tut5("5 Panmixia", "Panmixia1", "Panmixia.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	Tut6("6 Spatial", "Spatial1", "Spatial.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	Tut7("7 Boids", "Boids1", "Flock.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	Tut8("8 LittleForest", "LittleForest1", "LittleForest.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	Tut9("9 Headless(Logistic)", "Headless1", "LogisticHeadless.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	Tut10("10 Random number generators", "Rng1", "Rng_1.utg", LibraryType.Tutorial, TutorialsDummy.class), //
	//
	Model1("1 Animal", "Animal1","Animal.utg",LibraryType.Model, ModelsDummy.class), //
	Model2("2 Palms","Palms1","Palms.utg",LibraryType.Model,ModelsDummy.class),//
	Model3("3 Resproutch","Resproutch1","Resproutch.utg",LibraryType.Model,ModelsDummy.class),//
	//
	Test1 ("1 TestRelations","TestRelations1","TestRelations.utg",LibraryType.Test,TestsDummy.class),//
	Test2 ("2 TestLifeCycle","TestLifeCycle1","TestLifeCycle.utg",LibraryType.Test,TestsDummy.class),//
	Test3 ("3 WrapTest","WrapTest1","WrapTest.utg",LibraryType.Test,TestsDummy.class),//
	Test4 ("4 TestXYPlot","TestXYPlot1","TestXYPlot.utg",LibraryType.Test,TestsDummy.class),//
	Test5 ("5 ParallelTest (Palms)","Palms1","ParallelTestPalms.utg",LibraryType.Test,TestsDummy.class),//
	Test6 ("6 ParallelTest (Logistic)","Logistic1","ParallelTestLogistic.utg",LibraryType.Test,TestsDummy.class),//
	
	;

	private final String displayName;
	private final String proposedName;
	private final String fileName;
	private final LibraryType libraryType;
	private final Class<?> pkclass;

	private LibraryTable(String displayName, String proposedName, String fileName, LibraryType lt, Class<?> pkclass) {
		this.displayName = displayName;
		this.proposedName = proposedName;
		this.fileName = fileName;
		this.libraryType = lt;
		this.pkclass = pkclass;
	}

	public String displayName() {
		return displayName;
	}

	public String proposedName() {
		return proposedName;
	}

	public LibraryType libraryType() {
		return libraryType;
	}

	@SuppressWarnings("unchecked")
	public TreeGraph<TreeGraphDataNode, ALEdge> getGraph() {
		return (TreeGraph<TreeGraphDataNode, ALEdge>) GraphImporter.importGraph(fileName, pkclass);

	}

}
