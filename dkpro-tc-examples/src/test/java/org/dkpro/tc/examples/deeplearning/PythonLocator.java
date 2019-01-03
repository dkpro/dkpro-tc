/**
 * Copyright 2019
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
package org.dkpro.tc.examples.deeplearning;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.tc.examples.TestCaseSuperClass;

public class PythonLocator
    extends TestCaseSuperClass
{

    public static String getEnvironment() throws Exception
    {

        for (String pathCandidate : new String[] { "/usr/local/bin/python3", "/usr/bin/python3" }) {
            try {
                
                if(!new File(pathCandidate).exists()) {
                    System.err.println("No python found at ["+pathCandidate+"]");
                    continue;
                }
                

                boolean keras = kerasAvailable(pathCandidate);
                boolean numpy = numpyAvailable(pathCandidate);

                if (keras && numpy) {
                   System.err.println("Use Python at [" + pathCandidate + "]");
                    return pathCandidate;
                }
            }
            catch (Exception e) {
                // ignore
            }
        }

        return null;

    }

    private static boolean kerasAvailable(String pathCandidate) throws Exception
    {
        List<String> command = new ArrayList<>();
        command.add(pathCandidate);
        command.add("-c");
        command.add("\"import keras\"");

        List<String> o = startOnCmdGetOutput(command);
        return o.isEmpty();
    }
    
    private static boolean numpyAvailable(String pathCandidate) throws Exception
    {
        List<String> command = new ArrayList<>();
        command.add(pathCandidate);
        command.add("-c");
        command.add("\"import numpy\"");

        List<String> o = startOnCmdGetOutput(command);
        return o.isEmpty();
    }

    private static List<String> startOnCmdGetOutput(List<String> command) throws Exception
    {
        ProcessBuilder pb = new ProcessBuilder(command).command(command);
        Process start = pb.start();
        start.waitFor();
        
        List<String> output = new ArrayList<>();
        try (InputStream inputStream = start.getInputStream()) {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            String l = null;
            while ((l = r.readLine()) != null) {
                output.add(l);
            }
        }
        
        return output;
    }

}
