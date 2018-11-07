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
package org.dkpro.tc.ml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.ConfigurationAware;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;

/**
 * Ensures that all classifiers implement the {@link ConfigurationAware} interface.
 */
public abstract class TcClassifierTaskBase extends ExecutableTaskBase implements ConfigurationAware, Constants {

	protected Map<String, String> configuration;
	
    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    protected List<Object> classificationArguments;

	@Override
	public void execute(TaskContext aContext) throws Exception {
		writeConfiguration(aContext);
		writeClassifierName(aContext);
	}

	private void writeClassifierName(TaskContext aContext) throws IOException {
		File file = aContext.getFile(Constants.MACHINE_LEARNING_BACKEND_NAME, AccessMode.READWRITE);
		TcShallowLearningAdapter adapter = (TcShallowLearningAdapter) classificationArguments.get(0);

		StringBuilder sb = new StringBuilder();
		classificationArguments.subList(1, classificationArguments.size()).stream().map(Object::toString)
				.forEach(x -> sb.append(x + " "));

		Properties p = new Properties();
		p.setProperty("backend", adapter.getName());
		p.setProperty("parametrization", sb.toString().trim());

		try (FileOutputStream fos = new FileOutputStream(file)) {
			p.store(fos, "Backend details");
		}
	}

	@Override
	public void setConfiguration(Map<String, Object> aConfig) {
		configuration = new HashMap<String, String>();
		for (Entry<String, Object> e : aConfig.entrySet()) {
			configuration.put(e.getKey(), e.getValue().toString());
		}
	}

	protected void writeConfiguration(TaskContext aContext) {
		aContext.storeBinary(CONFIGURATION_DKPRO_LAB, new PropertiesAdapter(configuration, "Configuration properties"));
	}

}
