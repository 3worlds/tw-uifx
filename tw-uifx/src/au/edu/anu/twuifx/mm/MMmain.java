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

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.edu.anu.omhtk.jars.Jars;
import au.edu.anu.twapps.mm.MMModel;
import au.edu.anu.twuifx.mr.MRmain;
import fr.cnrs.iees.OmugiClassLoader;
import fr.cnrs.iees.twcore.generators.ProjectJarGenerator;
import fr.ens.biologie.generic.utils.Logging;
import javafx.application.Application;

public class MMmain {
	private static String usage = "Usage:\n" + MMmain.class.getName()
			+ "default logging level, class:level.";

	public static void main(String[] args) {
////		System.out.println("Current language: "+System.getProperty("user.language"));
//		if (Jars.getRunningJarFilePath(MMmain.class) == null) {
//			System.out.println("MM is NOT running from jar");
//			System.out.println(Thread.currentThread().getContextClassLoader().getName());
//			URL[] paths = new URL[1];
//			try {
//				paths[0] = new File ("/home/gignoux/3w/twfx.jar").toURI().toURL();
//				ClassLoader cld = new URLClassLoader(paths,	Thread.currentThread().getContextClassLoader());
//				Thread.currentThread().setContextClassLoader(cld);
//				cld.loadClass("javafx.application.Application");
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		} else {
//			System.out.println("MM is running from jar "+Jars.getRunningJarFilePath(MMmain.class));
//			System.out.println("BEFORE: "+Thread.currentThread().getContextClassLoader().getName());
//			System.out.println("SYSTEM: "+ClassLoader.getSystemClassLoader().getName());
//			
//			URL[] paths = new URL[1];
//			try {
//				paths[0] = new File ("/home/gignoux/3w/twfx.jar").toURI().toURL();
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			ClassLoader cld = new URLClassLoader(paths,	Thread.currentThread().getContextClassLoader());
//			System.out.println("INTER: "+cld);
//			Thread.currentThread().setContextClassLoader(cld);
//			try {
//				cld.loadClass("javafx.application.Application");
//				Class<?> bidon = Class.forName("javafx.application.Application");
//				System.out.println(bidon.getCanonicalName());				
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//// this is now deprecated - lots of warnings			
////			https://stackoverflow.com/questions/5380275/replacement-system-classloader-for-classes-in-jars-containing-jars
////			Field scl = ClassLoader.class.getDeclaredField("scl"); // Get system class loader
////	        scl.setAccessible(true); // Set accessible
////	        scl.set(null, new YourClassLoader()); // Update it to your class loader
//			
////			
////			OmugiClassLoader.setJarClassLoader(new File ("/home/ian/3w/tw.jar"),new File ("/home/ian/3w/twfx.jar"));
////			Thread.currentThread().setContextClassLoader(OmugiClassLoader.getJarClassLoader());
//			
//			System.out.println("AFTER: "+Thread.currentThread().getContextClassLoader());
////			leaves the classLoader NULL
//		}
			

		// Flaky ??
		ProjectJarGenerator.setModelRunnerClass(MRmain.class);
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
//				Class<?> c = Class.forName(klass,true,OmugiClassLoader.getJarClassLoader());
				Class<?> c = Class.forName(klass,true,OmugiClassLoader.getAppClassLoader());
				Level lvl = Level.parse(level);
				Logger log = Logging.getLogger(c);
				log.setLevel(lvl);
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		//Thread.currentThread().setContextClassLoader(OmugiClassLoader.getJarClassLoader());
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		Package[] pks = cl.getDefinedPackages();
//		System.out.println("#pks: "+pks.length);
//		for (int i=0; i<pks.length; i++)
//			System.out.println(pks[i].getName());
		
		Application.launch(ModelMakerfx.class);
	}

}
