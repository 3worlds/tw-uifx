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
package au.edu.anu.twuifx.mm;

import java.util.logging.Level;
import java.util.logging.Logger;

import au.edu.anu.twapps.mm.MMModel;
import fr.cnrs.iees.OmugiClassLoader;
import fr.cnrs.iees.twcore.generators.ProjectJarGenerator;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Application;

public class MMmain {
	private static String usage = "Usage:\n" + MMmain.class.getName()
			+ "default logging level, class:level.";

	public static void main(String[] args) {
		System.out.println("Current language: "+System.getProperty("user.language"));

		// Flaky I know but...
		ProjectJarGenerator.mainClass = au.edu.anu.twuifx.mr.MRmain.class.getName();
		// pass logging args on to deployed MR
		MMModel.mmArgs = args;

		
		// enact logging args 
		if (args.length > 0)
			Logging.setDefaultLogLevel(Level.parse(args[0]));
		else
			Logging.setDefaultLogLevel(Level.OFF);
		
		for (int i = 1; i<args.length;i++) {
			String[] pair = args[i].split(":");
			if (pair.length != 2) {
				System.out.println(usage);
				System.exit(-1);
			}
			String klass = pair[0];
			String level = pair[1];
			try {
				Class<?> c = Class.forName(klass,true,OmugiClassLoader.getAppClassLoader());
				Level lvl = Level.parse(level);
				Logger log = Logging.getLogger(c);
				log.setLevel(lvl);
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		Application.launch(ModelMakerfx.class);
	}

}
