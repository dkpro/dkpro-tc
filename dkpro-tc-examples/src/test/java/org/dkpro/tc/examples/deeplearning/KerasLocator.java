/**
 * Copyright 2017
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.dkpro.tc.examples.TestCaseSuperClass;

public class KerasLocator extends TestCaseSuperClass {

	public static String getEnvironment() throws Exception {

		for (String pathCandidate : new String[] { "/usr/local/bin/python3", "/usr/bin/python3" }) {
			List<String> command = new ArrayList<>();
			command.add(pathCandidate);
			command.add("-c");
			command.add("import keras");

			ProcessBuilder pb = new ProcessBuilder(command).command(command);
			Process start = pb.start();
			start.waitFor();

			InputStream inputStream = start.getInputStream();
			List<String> output = new ArrayList<>();
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
			String l = null;
			while ((l = r.readLine()) != null) {
				output.add(l);
			}
			r.close();

			boolean keras = output.isEmpty();

			if (keras) {
				LogFactory.getLog(KerasLocator.class.getName()).info("Use Python at [" + pathCandidate + "]");
				return pathCandidate;
			}
		}

		return null;

	}

}
