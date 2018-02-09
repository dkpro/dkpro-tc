/**
 * Copyright 2018
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
package org.dkpro.tc.examples;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;

public class TestCaseSuperClass {

	static StringBuilder logInformation = new StringBuilder();

	private static void logInfo(Description description, String status, long nanos) {
		String testName = description.getMethodName();
		String l = String.format("Test %s %s, spent %d microseconds", testName, status,
				TimeUnit.NANOSECONDS.toMicros(nanos));
		
		logInformation.append(l+"\n");
	}

	@Rule
	public Stopwatch stopwatch = new Stopwatch() {
		@Override
		protected void succeeded(long nanos, Description description) {
		}

		@Override
		protected void failed(long nanos, Throwable e, Description description) {
		}

		@Override
		protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
		}

		@Override
		protected void finished(long nanos, Description description) {
			logInfo(description, "finished", nanos);
		}
	};

	public final static String HOME = "target/results";

	@Before
	public void setup() throws Exception {
		/*
		 * Sets the logging, configuraiton files are found under src/test/* to
		 * increase verbosity for debugging
		 */
		System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
		System.setProperty("DKPRO_HOME", HOME);
	}

	@After
	public void cleanUp() throws Exception {
		FileUtils.deleteDirectory(new File(HOME));
		
		FileUtils.writeStringToFile(new File("target/runtime.txt"), logInformation.toString(),"utf-8", true);
	}

}
