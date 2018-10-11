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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.ConfigurationAware;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;

/**
 * Ensures that all classifiers implement the {@link ConfigurationAware} interface.
 */
public abstract class TcClassifierTaskBase extends ExecutableTaskBase implements ConfigurationAware, Constants {

	protected Map<String, String> configuration;

	@Override
	public void execute(TaskContext aContext) throws Exception {
		writeConfiguration(aContext);
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
