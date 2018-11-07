/*******************************************************************************
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml.vowpalwabbit.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.dkpro.tc.ml.vowpalwabbit.VowpalWabbitTestTask;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public abstract class VowpalWabbit {
	static RuntimeProvider runtimeProvider = null;
	final static PlatformDetector detector = new PlatformDetector();

	private static final String classpath = "classpath:/org/dkpro/tc/ml/vowpalwabbit/";

	public VowpalWabbit() {
		//
	}

	protected static List<String> assembleCommand(File binary, String... parameters) {

		List<String> command = new ArrayList<>();

		command.add(binary.getAbsolutePath());
		for (String p : parameters) {
			command.add(p);
		}

		return command;
	}

	public static File getExecutable() throws Exception {
		
		catchWindows32BitUsers();

		if (runtimeProvider == null) {
			String platform = detector.getPlatformId();
			LogFactory.getLog(VowpalWabbitTestTask.class.getName())
					.info("Load binary for platform: [" + platform + "]");

			runtimeProvider = new RuntimeProvider(classpath);
		}

		return runtimeProvider.getFile("vw");
	}

	public static PlatformDetector getPlatformDetector() {
		return detector;
	}
	
    private static void catchWindows32BitUsers()
    {
        PlatformDetector pd = new PlatformDetector();
        if (pd.getOs().equals(PlatformDetector.OS_WINDOWS)
                && pd.getArch().equals(PlatformDetector.ARCH_X86_32)) {
            throw new UnsupportedOperationException(
                    "VowpalWabbit is not available for 32bit Windows operating systems. Please use a 64bit version.");
        }
    }

}
