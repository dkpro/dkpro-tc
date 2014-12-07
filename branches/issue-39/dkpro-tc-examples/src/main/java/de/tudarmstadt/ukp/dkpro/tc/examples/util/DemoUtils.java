/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.examples.util;

import java.io.File;
import java.util.Map;

public class DemoUtils {

    /**
     * Set the DKPRO_HOME environment variable to some folder in "target".
     * This is mainly used to ensure that demo experiments run even if people have not set DKPRO_HOME before.
     * 
     * If DKPRO_HOME is already set, nothing is done (in order not to override already working environments).
     * 
     * It is highly recommended not to use that anywhere else than in the demo experiments,
     * as DKPRO_HOME is usually also used to store other data required for (real) experiments. 
     * 
     * @param experimentName
     * @return True if DKPRO_HOME was correctly set and false if nothing was done.
     */
    public static boolean setDkproHome(String experimentName) {
    	String dkproHome = "DKPRO_HOME";
    	Map<String, String> env = System.getenv();
    	if (!env.containsKey(dkproHome)) {
    		System.out.println("DKPRO_HOME not set.");
    		
        	File folder = new File("target/results/" + experimentName);
        	folder.mkdirs();
        	
        	System.setProperty(dkproHome, folder.getPath());
        	System.out.println("Setting DKPRO_HOME to: " + folder.getPath());
        	
        	return true;
    	}
    	else {
    		System.out.println("DKPRO_HOME already set to: " + env.get(dkproHome));
    		System.out.println("Keeping those settings.");
    		
    		return false;
    	}
    }
}
