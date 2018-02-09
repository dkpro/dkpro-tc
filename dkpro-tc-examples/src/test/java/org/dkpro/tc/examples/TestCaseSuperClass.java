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
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;

public class TestCaseSuperClass {
	
	/*
	 * This variable enables a logging of the runtime each test takes to complete.
	 * Helpful for speeding up Jenkins builds once in a while. 
	 */
	static boolean LOG_RUNTIME_OF_TEST_CASES = true;

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

		FileUtils.writeStringToFile(new File("target/runtime.txt"), logInformation.toString(), "utf-8", true);
	}
	
	static StringBuilder logInformation = new StringBuilder();
	static String className = "";

	private static void logInfo(Description description, String status, long nanos) {
		
		if(!LOG_RUNTIME_OF_TEST_CASES){
			return;
		}

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
		String now = formatter.format(System.currentTimeMillis());

		String currentClassName = description.getClassName();

		if (!className.equals(currentClassName)) {
			logInformation.append("\n" + currentClassName + " / " + now + "\n");
			className = currentClassName;
		}

		String testId = description.getMethodName();
		String l = String.format("%s took %4d seconds [%s]", testId, nanos/1000000000,
				status);

		logInformation.append(l + "\n");
	}

	@Rule
	public Stopwatch stopwatch = new Stopwatch() {
		@Override
		protected void succeeded(long nanos, Description description) {
			logInfo(description, "succeeded", nanos);
		}

		@Override
		protected void failed(long nanos, Throwable e, Description description) {
			logInfo(description, "failed", nanos);
		}

		@Override
		protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
			logInfo(description, "skipped", nanos);
		}

		@Override
		protected void finished(long nanos, Description description) {
		}
	};

}
