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
package fr.cnrs.iees.versioning;

public class VersionSettings {

	// Change these fields to suit the project
	// ====================================================

	/**
	 * The organisation name as will appear in the ivy module specification - it is
	 * a good idea to keep it consistent with the project src directory (although
	 * not required).
	 */
	protected static String ORG = "au.edu.anu.tw-uifx";

	/**
	 * The name of the ivy module (this will be the name of the generated jar file
	 * for dependent projects).
	 */
	protected static String MODULE = "tw-uifx";

	/**
	 * The ivy status of the module: integration, milestone, or release are the ivy
	 * defaults But we can define ours like bronze, gold, silver, or crap,
	 * supercrap, ultracrap.
	 */
	protected static String STATUS = "integration";

	/** The license under which this module (= jar) is distributed */
	protected static String LICENSE = "gpl3";

	/** The url to the text of the license */
	protected static String LICENSE_URL = "https://www.gnu.org/licenses/gpl-3.0.txt";

	/** A (long) description of the ivy module */
	protected static String DESCRIPTION = "This module contains only fx code for the UI of ModelMaker and ModelRunner.";

	/**
	 * <p>
	 * Dependencies on other modules (they will be integrated in the ivy script).
	 * </p>
	 *
	 * <p>
	 * This is a (n * 4) table of Strings.<br/>
	 * Every line is a new dependency. On every line, the 4 Strings must match the
	 * ivy fields:
	 * <dl>
	 * <dt>org</dt>
	 * <dd>for <em>organisation</em></dd>
	 * <dt>name</dt>
	 * <dd>for the module <em>name</em></dd>
	 * <dt>rev</dt>
	 * <dd>for the <em>revision</em> or version number. The '+' can be conveniently
	 * used to specify 'any version'.</dd>
	 * <dt>m:classifier</dt>
	 * <dd>for the <em>type of artifact</em> within the module. It's actually a
	 * maven field, not an ivy filed. It enables to identify different artifacts
	 * such as source, javadoc, linux or windows specific packagings. This field is
	 * optional, put 'null' if not needed. Valid values are: <em>sources, javadoc,
	 * _os</em>. 'sources' and 'javadoc' will be used directly, '_os' will be
	 * replaced by the OS family (ie linux, mac or win) - to match the needs of
	 * javafx components. For other libraries, you should check in the maven central
	 * repository the actual file names by clicking on the 'View All' button in the
	 * top table, on the 'Files' table entry</em></dd>
	 * </dl>
	 * The field can be empty (just needs the external braces).<br/>
	 * Example value:
	 *
	 * <pre>
	 * { { "org.galaxy.jupiter", "crap", "1.0.+" }, { "org.ocean.lostIsland", "strungk", "3.12.254" } }
	 * </pre>
	 * </p>
	 * <p>
	 * Wildcards for revision numbers are indicated <a href=
	 * "http://ant.apache.org/ivy/history/master/ivyfile/dependency.html">there</a>.
	 * </p>
	 *
	 */
	protected static String[][] DEPS = { 
			{ "fr.cnrs.iees.omhtk", "generics", "[0.5.1,)", null },
			{ "fr.cnrs.iees.omugi", "omugi", "[0.6.1,)", null },
			{ "fr.cnrs.iees.tw-core", "tw-core", "[0.6.5,)", null },
			{ "au.edu.anu.aot", "aot", "[0.2.1,)", null },
			{ "fr.cnrs.iees.tw-models", "tw-models", "[0.0.6,)", null},
			{ "au.edu.anu.tw-apps", "tw-apps", "[0.3.0,)", null },
			{ "au.edu.anu.qgraph", "qgraph", "[0.3.0,)", null },
			{ "au.edu.anu.ymuit", "ymuit", "[0.1.11,)", null },
			{ "fr.cnrs.iees.rvgrid", "rvgrid", "[0.1.1,)", null },
			// javafx: version 17.0.4 has long term support until September 2026
			// do not use early access builds. cf https://gluonhq.com/products/javafx/
			// but maven central only provides 17.0.2
			{ "org.openjfx", "javafx-fxml", "17.0.2", "_os" },
			{ "org.openjfx", "javafx-controls", "17.0.2", "_os" },
			{ "org.openjfx", "javafx-graphics", "17.0.2", "_os" },
			{ "org.openjfx", "javafx-base", "17.0.2", "_os" },
			{ "org.controlsfx", "controlsfx", "[11,)", null }, 
			{"org.apache.commons","commons-math3","[3.6.1,)",null},
			{ "de.gsi", "chartfx", "[11,)", null }, // pom?
			{ "de.gsi", "chartfx-samples", "[11,)", null },
			{ "de.gsi.chart", "chartfx-chart", "[11,)", null },
			{ "de.gsi.dataset", "chartfx-dataset", "[11,)", null },
			{ "de.gsi.math", "chartfx-math", "[11,)", null },
			{ "de.gsi.acc", "chartfx-acc", "[11,)", null },
			{ "de.gsi", "microservice", "[11,)", null },
			// This is required by chart-fx - tw-core loads this api, but as version 1.7.6
			// but chartfx cannot use that version, it is set to use version 2.0.0
			{ "org.slf4j", "slf4j-api", "2.0.0-alpha0", null}
		};

	/**
	 * The name of the main class to put in the jar manifest, if any. This enables
	 * users to run the jar using this class as the entry point. Of course this must
	 * be a fully qualified valid java class name found in the jar.
	 */
	protected static String MAINCLASS = null;

}
